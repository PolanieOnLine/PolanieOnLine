package games.stendhal.server.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Actions;
import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import utilities.PlayerTestHelper;

/** Tests the single action shop checkout, without a client supplied quote. */
public class NpcShopActionTest {

    @BeforeClass
    public static void setUpClasses() {
        MockStendlRPWorld.get();
        MockStendhalRPRuleProcessor.get();
        PlayerTestHelper.generatePlayerRPClasses();
    }

    @Test
    public void onePurchaseActionPaysAndDeliversExactlyOneItem() {
        final StendhalRPZone zone = new StendhalRPZone("npc_shop_direct_purchase");
        final Player player = PlayerTestHelper.createPlayer("shop_direct_purchase");
        player.setPosition(5, 5);
        zone.add(player);
        final SpeakerNPC npc = seller("shop direct seller", zone, 5, 8);

        PlayerTestHelper.equipWithMoney(player, 20);
        new NpcShopAction().onAction(player, action(npc, "1"));

        assertEquals(1, player.getTotalNumberOf("sztylecik"));
        assertEquals(15, player.getTotalNumberOf("money"));
        final RPEvent result = lastShopEvent(player);
        assertEquals("result", result.get("phase"));
        assertTrue(result.get("message").startsWith("Zakup udany:"));
    }

    @Test
    public void invalidQuantityCannotChargeOrDeliverItems() {
        final StendhalRPZone zone = new StendhalRPZone("npc_shop_invalid_quantity");
        final Player player = PlayerTestHelper.createPlayer("shop_invalid_quantity");
        player.setPosition(5, 5);
        zone.add(player);
        final SpeakerNPC npc = seller("shop invalid quantity seller", zone, 5, 8);

        PlayerTestHelper.equipWithMoney(player, 20);
        new NpcShopAction().onAction(player, action(npc, "0"));

        assertEquals(0, player.getTotalNumberOf("sztylecik"));
        assertEquals(20, player.getTotalNumberOf("money"));
        assertEquals("refresh", lastShopEvent(player).get("phase"));
    }

    @Test
    public void merchantRejectsDistantPurchase() {
        final StendhalRPZone zone = new StendhalRPZone("npc_shop_distant_purchase");
        final Player player = PlayerTestHelper.createPlayer("shop_distant_purchase");
        player.setPosition(5, 5);
        zone.add(player);
        final SpeakerNPC npc = seller("shop distant seller", zone, 30, 30);

        PlayerTestHelper.equipWithMoney(player, 20);
        new NpcShopAction().onAction(player, action(npc, "1"));

        assertEquals(0, player.getTotalNumberOf("sztylecik"));
        assertEquals(20, player.getTotalNumberOf("money"));
        assertEquals("refresh", lastShopEvent(player).get("phase"));
    }

    private static SpeakerNPC seller(final String name, final StendhalRPZone zone,
            final int x, final int y) {
        final SpeakerNPC npc = new SpeakerNPC(name);
        npc.setPosition(x, y);
        npc.addGreeting();
        new SellerAdder().addSeller(npc, new SellerBehaviour(
                Collections.singletonMap("sztylecik", 5)));
        zone.add(npc);
        return npc;
    }

    private static RPAction action(final SpeakerNPC npc, final String quantity) {
        final RPAction action = new RPAction();
        action.put(Actions.TYPE, Actions.NPC_SHOP);
        action.put("command", "purchase");
        action.put("npc_id", npc.getID().getObjectID());
        action.put("mode", "buy");
        action.put("item", "sztylecik");
        action.put("quantity", quantity);
        return action;
    }

    private static RPEvent lastShopEvent(final Player player) {
        RPEvent result = null;
        for (final RPEvent event : player.events()) {
            if (Events.NPC_SHOP.equals(event.getName())) {
                result = event;
            }
        }
        if (result == null) {
            throw new AssertionError("Missing NPC shop event");
        }
        return result;
    }
}
