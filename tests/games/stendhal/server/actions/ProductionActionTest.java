package games.stendhal.server.actions;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.mapstuff.useable.UseableEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.krakow.woodcutter.CharcoalBurnerNPC;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;

/** Exercises the window against the actual persistent charcoal recipe. */
public class ProductionActionTest {
    private static final String SLOT = "smolislaw_burn_charcoal";
    private static int sequence;
    private Player player;
    private SpeakerNPC npc;
    private UseableEntity worksite;
    private ActionListener handler;

    @Before public void setUp() throws Exception {
        NPCList.get().remove("Smolisław");
        SingletonRepository.getProducerRegister().getProducers().removeIf(p -> p.first().equals("Smolisław"));
        StendhalRPZone zone = new StendhalRPZone("production_window_" + ++sequence, 128, 128);
        MockStendlRPWorld.get().addRPZone(zone);
        new CharcoalBurnerNPC().configureZone(zone, null);
        npc = NPCList.get().get("Smolisław");
        for (RPObject entity : zone) {
            if (entity instanceof UseableEntity && entity.has("name") && "charcoal_mound".equals(entity.get("name"))) {
                worksite = (UseableEntity) entity;
            }
        }
        assertNotNull("The charcoal mound is an interactive world object", worksite);
        player = PlayerTestHelper.createPlayer("producer-" + sequence);
        player.setPosition(56, 57);
        zone.add(player);
        handler = (ActionListener) Class.forName("games.stendhal.server.actions.ProductionAction")
                .getDeclaredConstructor().newInstance();
    }

    @After public void tearDown() { NPCList.get().remove("Smolisław"); }

    private RPEvent send(String command, int amount, RPEvent previous) {
        RPAction action = new RPAction();
        action.put("npc_id", worksite.getID().getObjectID());
        action.put("command", command);
        action.put("amount", amount);
        action.put("request_id", "test-" + command + "-" + amount);
        if (previous != null) action.put("window_id", previous.get("window_id"));
        handler.onAction(player, action);
        RPEvent latest = null;
        for (RPEvent event : player.events()) {
            if ("production_event".equals(event.getName())) latest = event;
        }
        assertNotNull("Window receives an authoritative response", latest);
        return latest;
    }

    private void resources(int wood, int money) {
        assertTrue(PlayerTestHelper.equipWithStackableItem(player, "polano", wood));
        assertTrue(PlayerTestHelper.equipWithMoney(player, money));
    }

    @Test public void opensWithRealSpritesAndBatchCosts() {
        resources(50, 100);
        RPEvent data = send("open", 10, null);
        assertEquals("open", data.get("phase"));
        assertEquals("węgiel drzewny", data.get("name"));
        assertFalse(data.get("subclass").isEmpty());
        assertEquals(300, data.getInt("duration"));
        assertEquals("20", data.getList("material_values").get(0));
        assertEquals("50", data.getList("material_values").get(1));
        assertEquals("idle", data.get("order_state"));
        assertEquals(worksite.getID().getObjectID(), data.getInt("npc_id"));
    }

    @Test public void usingTheMoundOpensTheWindowOnlyWhenStandingNextToIt() {
        assertTrue(worksite.onUsed(player));
        assertEquals("open", send("open", 1, null).get("phase"));
        player.setPosition(10, 10);
        assertFalse(worksite.onUsed(player));
    }

    @Test public void startsOnlyOnceAndRechecksResources() {
        resources(60, 150);
        RPEvent data = send("open", 10, null);
        send("start", 10, data);
        String order = player.getQuest(SLOT);
        send("start", 10, data);
        assertEquals(order, player.getQuest(SLOT));
        assertEquals(40, player.getNumberOfEquipped("polano"));
        assertEquals(100, player.getNumberOfEquipped("money"));
    }

    @Test public void missingResourcesAndInvalidAmountsDoNotChargeAnything() {
        resources(20, 49);
        RPEvent data = send("open", 10, null);
        send("start", 10, data);
        send("start", 0, data);
        send("start", 1001, data);
        assertFalse(player.hasQuest(SLOT));
        assertEquals(20, player.getNumberOfEquipped("polano"));
        assertEquals(49, player.getNumberOfEquipped("money"));
    }

    @Test public void reopeningRestoresOldOrderAndCollectsOnlyOnce() {
        player.setQuest(SLOT, "10;węgiel drzewny;" + (System.currentTimeMillis() - 301000));
        RPEvent data = send("open", 1, null);
        assertEquals("ready", data.get("order_state"));
        assertEquals(10, data.getInt("order_amount"));
        send("collect", 1, data);
        send("collect", 1, data);
        assertEquals("done", player.getQuest(SLOT));
        assertEquals(10, player.getNumberOfEquipped("węgiel drzewny"));
    }

    @Test public void cannotCollectEarlyOrCreateAnOrderRemotely() {
        resources(20, 50);
        RPEvent data = send("open", 10, null);
        send("start", 10, data);
        send("collect", 10, data);
        assertEquals(0, player.getNumberOfEquipped("węgiel drzewny"));
        player.setQuest(SLOT, "done");
        player.setPosition(10, 10);
        RPEvent rejected = send("start", 10, data);
        assertEquals("too_far", rejected.get("status"));
        assertEquals("done", player.getQuest(SLOT));
    }

    @Test public void aStaleConversationConfirmationCannotReplaceWindowOrder() {
        resources(40, 100);
        npc.getEngine().step(player, "hi");
        npc.getEngine().step(player, "wypal 5 węgla drzewnego");
        RPEvent data = send("open", 10, null);
        send("start", 10, data);
        String order = player.getQuest(SLOT);
        npc.getEngine().step(player, "tak");
        assertEquals(order, player.getQuest(SLOT));
        assertEquals(20, player.getNumberOfEquipped("polano"));
    }
}
