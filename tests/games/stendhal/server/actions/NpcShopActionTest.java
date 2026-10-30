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
import games.stendhal.server.entity.npc.behaviour.impl.QuestCompletedSellerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.QuestCompletedBuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.behaviour.impl.SeedSellerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.ValueBuyerBehaviour;
import games.stendhal.server.core.rule.rarity.ItemRarityModifiers;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
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

    @Test
    public void sellsOnlyTheSelectedInstanceAndRejectsARepeatedCheckout() {
        final StendhalRPZone zone = new StendhalRPZone("npc_shop_selected_sale");
        final Player player = PlayerTestHelper.createPlayer("shop_selected_sale");
        player.setPosition(5, 5); zone.add(player);
        final SpeakerNPC npc = new SpeakerNPC("selected sale buyer");
        npc.setPosition(6, 5); npc.addGreeting();
        new BuyerAdder().addBuyer(npc, new BuyerBehaviour(Collections.singletonMap("sztylecik", 10)));
        zone.add(npc);
        final Item first = SingletonRepository.getEntityManager().getItem("sztylecik", ItemRarity.COMMON);
        final Item second = SingletonRepository.getEntityManager().getItem("sztylecik", ItemRarity.COMMON);
        first.put("improve", 1); second.put("improve", 2);
        second.setRarityModifier(ItemRarityModifiers.VALUE, 2.0);
        player.getSlot("bag").add(first); player.getSlot("bag").add(second);
        final RPAction open = action(npc, "1"); open.put("command", "open");
        final NpcShopAction service = new NpcShopAction(); service.onAction(player, open);
        final RPEvent catalogue = lastShopEvent(player);
        int index = catalogue.getList("buy_items").get(0).contains("\"improve\":\"2\"") ? 0 : 1;
        final RPAction sale = action(npc, "1"); sale.put("mode", "sell");
        sale.put("selection", catalogue.getList("buy_keys").get(index));
        sale.put("window_id", catalogue.get("window_id")); sale.put("request_id", "1");
        assertEquals("20", catalogue.getList("buy_price_values").get(index));
        service.onAction(player, sale);
        assertEquals(20, player.getTotalNumberOf("money"));
        assertEquals(1, player.getTotalNumberOf("sztylecik"));
        assertTrue(player.getAllEquipped("sztylecik").get(0) == first);
        service.onAction(player, sale);
        assertEquals(20, player.getTotalNumberOf("money"));
        assertTrue(player.getAllEquipped("sztylecik").get(0) == first);
    }

    @Test
    public void removingTheSelectedItemDoesNotSellAnotherMatchingItem() {
        final StendhalRPZone zone = new StendhalRPZone("npc_shop_stale_sale");
        final Player player = PlayerTestHelper.createPlayer("shop_stale_sale");
        player.setPosition(5, 5); zone.add(player);
        final SpeakerNPC npc = new SpeakerNPC("stale sale buyer");
        npc.setPosition(6, 5); npc.addGreeting();
        new BuyerAdder().addBuyer(npc, new BuyerBehaviour(Collections.singletonMap("sztylecik", 10)));
        zone.add(npc);
        final Item first = SingletonRepository.getEntityManager().getItem("sztylecik", ItemRarity.COMMON);
        final Item second = SingletonRepository.getEntityManager().getItem("sztylecik", ItemRarity.COMMON);
        player.getSlot("bag").add(first); player.getSlot("bag").add(second);
        final NpcShopAction service = new NpcShopAction();
        final RPAction open = action(npc, "1"); open.put("command", "open"); service.onAction(player, open);
        final RPEvent catalogue = lastShopEvent(player);
        final RPAction sale = action(npc, "1"); sale.put("mode", "sell");
        sale.put("selection", catalogue.getList("buy_keys").get(1));
        sale.put("window_id", catalogue.get("window_id")); sale.put("request_id", "1");
        player.drop(second);
        service.onAction(player, sale);
        assertEquals(0, player.getTotalNumberOf("money"));
        assertEquals(1, player.getTotalNumberOf("sztylecik"));
        assertTrue(player.getAllEquipped("sztylecik").get(0) == first);
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
    @Test public void seedCatalogueKeepsTheCultivarOfferName() {
        final StendhalRPZone zone=new StendhalRPZone("shop-seeds");
        final Player player=PlayerTestHelper.createPlayer("seed-customer"); player.setPosition(5,5); zone.add(player);
        final SpeakerNPC npc=new SpeakerNPC("seed-merchant"); npc.setPosition(6,5); npc.addGreeting();
        new SellerAdder().addSeller(npc,new SeedSellerBehaviour());zone.add(npc);
        final RPAction open=action(npc,"1");open.put("command","open");new NpcShopAction().onAction(player,open);
        assertTrue(lastShopEvent(player).getList("sell_names").contains("nasiona stokrotki"));
        PlayerTestHelper.equipWithMoney(player,40);
        final RPAction buy=action(npc,"2");buy.put("item","nasiona stokrotki");new NpcShopAction().onAction(player,buy);
        assertEquals(2,player.getTotalNumberOf("nasiona"));
        assertEquals("stokrotki",player.getAllEquipped("nasiona").get(0).getItemData());
    }

    @Test public void sellLimitReflectsTheArenaWalletCap() {
        final StendhalRPZone zone=new StendhalRPZone("shop-cap");
        final Player player=PlayerTestHelper.createPlayer("cap-customer");player.setPosition(5,5);zone.add(player);
        final SpeakerNPC npc=new SpeakerNPC("cap-merchant");npc.setPosition(6,5);npc.addGreeting();
        new BuyerAdder().addBuyer(npc,new ValueBuyerBehaviour(SingletonRepository.getEntityManager().getDefaultItems()));zone.add(npc);
        final StackableItem stack=(StackableItem)SingletonRepository.getEntityManager().getItem("ametyst",ItemRarity.COMMON);
        stack.setQuantity(10);player.getSlot("bag").add(stack);
        final int unit=new ValueBuyerBehaviour(SingletonRepository.getEntityManager().getDefaultItems()).getChargeForItem(stack,1,player);
        PlayerTestHelper.equipWithMoney(player,Integer.MAX_VALUE-unit*2);
        final RPAction open=action(npc,"1");open.put("command","open");new NpcShopAction().onAction(player,open);
        assertEquals("2",lastShopEvent(player).getList("buy_quantities").get(0));
    }

    @Test
    public void emptyBagStillShowsTheCompleteBuyingCatalogue() {
        final StendhalRPZone zone = new StendhalRPZone("shop_empty_bag");
        final Player player = PlayerTestHelper.createPlayer("empty-bag-customer");
        player.setPosition(5, 5); zone.add(player);
        final SpeakerNPC npc = new SpeakerNPC("empty bag buyer");
        npc.setPosition(6, 5); npc.addGreeting();
        new BuyerAdder().addBuyer(npc, new BuyerBehaviour(Collections.singletonMap("sztylecik", 10)));
        zone.add(npc);
        final RPAction open = action(npc, "1"); open.put("command", "open");
        final NpcShopAction shop = new NpcShopAction();
        shop.onAction(player, open);
        final RPEvent catalogue = lastShopEvent(player);
        assertEquals(Collections.singletonList("sztylecik"), catalogue.getList("buy_names"));
        assertEquals("0", catalogue.getList("buy_quantities").get(0));
        assertEquals("10", catalogue.getList("buy_price_values").get(0));
        final RPAction sale = action(npc, "1"); sale.put("mode", "sell");
        sale.put("window_id", catalogue.get("window_id"));
        sale.put("selection", catalogue.getList("buy_keys").get(0));
        shop.onAction(player, sale);
        assertEquals(0, player.getTotalNumberOf("money"));
    }

    @Test
    public void openingAndRefreshingShopDoesNotChangeBagItemPerceptions() {
        final StendhalRPZone zone = new StendhalRPZone("shop_read_only_catalogue");
        final Player player = PlayerTestHelper.createPlayer("read-only-customer");
        player.setPosition(5, 5); zone.add(player);
        final SpeakerNPC npc = new SpeakerNPC("read only buyer");
        npc.setPosition(6, 5); npc.addGreeting();
        new BuyerAdder().addBuyer(npc, new BuyerBehaviour(Collections.singletonMap("sztylecik", 10)));
        zone.add(npc);
        final Item item = SingletonRepository.getEntityManager().getItem("sztylecik", ItemRarity.COMMON);
        player.getSlot("bag").add(item);
        player.resetAddedAndDeleted();
        final NpcShopAction shop = new NpcShopAction();
        for (final String command : new String[] {"open", "refresh"}) {
            final RPAction request = action(npc, "1"); request.put("command", command);
            shop.onAction(player, request);
            final RPObject added = new RPObject();
            final RPObject removed = new RPObject();
            item.getDifferences(added, removed);
            assertTrue("Shop removed item maps: " + removed, removed.maps().isEmpty());
            assertTrue("Shop changed item maps: " + added, added.maps().isEmpty());
            assertTrue(item.getContainer() == player);
        }
    }

    @Test
    public void backgroundRefreshDoesNotRestartTheNpcConversation() {
        final StendhalRPZone zone = new StendhalRPZone("shop_quiet_refresh");
        final Player player = PlayerTestHelper.createPlayer("quiet-shop-customer");
        player.setPosition(5, 5); zone.add(player);
        final int[] greetings = {0};
        final SpeakerNPC npc = new SpeakerNPC("quiet shop seller") {
            @Override public void listenTo(final Player customer, final String text) {
                if ("hi".equals(text)) { greetings[0]++; }
                super.listenTo(customer, text);
            }
        };
        npc.setPosition(6, 5); npc.addGreeting(); npc.addGoodbye();
        new SellerAdder().addSeller(npc, new SellerBehaviour(Collections.singletonMap("sztylecik", 5)));
        zone.add(npc);
        final NpcShopAction shop = new NpcShopAction();
        final RPAction open = action(npc, "1"); open.put("command", "open"); shop.onAction(player, open);
        npc.listenTo(player, "bye");
        npc.setAttending(null);
        final RPAction refresh = action(npc, "1"); refresh.put("command", "refresh");
        for (int i = 0; i < 3; i++) { shop.onAction(player, refresh); }
        assertEquals(1, greetings[0]);
        assertTrue(npc.getAttending() == null);
        assertTrue(lastShopEvent(player).getList("sell_names").contains("sztylecik"));
        PlayerTestHelper.equipWithMoney(player, 10);
        shop.onAction(player, action(npc, "1"));
        assertEquals(1, player.getTotalNumberOf("sztylecik"));
        assertEquals(2, greetings[0]);
    }

    @Test
    public void questGatedMerchantsExposeTheirLockAndUnlockAfterQuestCompletion() {
        for (final boolean buying : new boolean[] {true, false}) {
            final StendhalRPZone zone = new StendhalRPZone("shop_quest_gate_" + buying);
            final Player player = PlayerTestHelper.createPlayer("quest-shop-customer-" + buying);
            player.setPosition(5, 5); zone.add(player);
            final SpeakerNPC npc = new SpeakerNPC("quest shop merchant " + buying);
            npc.setPosition(6, 5); npc.addGreeting(); zone.add(npc);
            if (buying) {
                new SellerAdder().addSeller(npc, new QuestCompletedSellerBehaviour("shop_unlock", "Najpierw ukończ zadanie.", Collections.singletonMap("sztylecik", 5)));
                PlayerTestHelper.equipWithMoney(player, 10);
            } else {
                new BuyerAdder().addBuyer(npc, new QuestCompletedBuyerBehaviour("shop_unlock", "Najpierw ukończ zadanie.", Collections.singletonMap("sztylecik", 5)));
                player.getSlot("bag").add(SingletonRepository.getEntityManager().getItem("sztylecik", ItemRarity.COMMON));
            }
            final NpcShopAction shop = new NpcShopAction();
            final RPAction open = action(npc, "1"); open.put("command", "open"); shop.onAction(player, open);
            final String prefix = buying ? "sell" : "buy";
            final RPEvent locked = lastShopEvent(player);
            assertTrue(locked.get(prefix + "_requirement").contains("zadani"));
            assertTrue(locked.getList(prefix + "_names").contains("sztylecik"));
            final RPAction purchase = action(npc, "1"); purchase.put("mode", buying ? "buy" : "sell");
            purchase.put("window_id", locked.get("window_id"));
            if (!buying) { purchase.put("selection", locked.getList("buy_keys").get(0)); }
            shop.onAction(player, purchase);
            assertEquals(buying ? 10 : 0, player.getTotalNumberOf("money"));
            assertEquals(buying ? 0 : 1, player.getTotalNumberOf("sztylecik"));
            player.setQuest("shop_unlock", "done");
            final RPAction refresh = action(npc, "1"); refresh.put("command", "refresh"); shop.onAction(player, refresh);
            assertEquals("", lastShopEvent(player).get(prefix + "_requirement"));
            shop.onAction(player, purchase);
            assertEquals(5, player.getTotalNumberOf("money"));
            assertEquals(buying ? 1 : 0, player.getTotalNumberOf("sztylecik"));
        }
    }

    @Test
    public void questGateIsCheckedAgainBeforeConfirmingAnEarlierQuote() {
        final StendhalRPZone zone = new StendhalRPZone("shop_revoked_gate");
        final Player player = PlayerTestHelper.createPlayer("revoked-shop-customer");
        player.setPosition(5, 5); zone.add(player);
        player.setQuest("shop_unlock", "done"); PlayerTestHelper.equipWithMoney(player, 10);
        final SpeakerNPC npc = new SpeakerNPC("revoked shop seller");
        npc.setPosition(6, 5); npc.addGreeting(); zone.add(npc);
        new SellerAdder().addSeller(npc, new QuestCompletedSellerBehaviour("shop_unlock", "Najpierw ukończ zadanie.", Collections.singletonMap("sztylecik", 5)));
        final NpcShopAction shop = new NpcShopAction();
        final RPAction request = action(npc, "1"); request.put("command", "request"); shop.onAction(player, request);
        final RPEvent quote = lastShopEvent(player);
        assertEquals("offer", quote.get("phase"));
        player.setQuest("shop_unlock", "start");
        final RPAction confirm = action(npc, "1"); confirm.put("command", "confirm");
        confirm.put("window_id", quote.get("window_id")); confirm.put("request_token", quote.get("request_token"));
        shop.onAction(player, confirm);
        assertEquals(10, player.getTotalNumberOf("money"));
        assertEquals(0, player.getTotalNumberOf("sztylecik"));
        assertTrue(lastShopEvent(player).get("sell_requirement").contains("zadani"));
    }

}
