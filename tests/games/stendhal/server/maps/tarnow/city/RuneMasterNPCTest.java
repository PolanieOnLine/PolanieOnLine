package games.stendhal.server.maps.tarnow.city;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.common.constants.Events;
import games.stendhal.server.actions.ItemUpgradeAction;
import games.stendhal.server.entity.mapstuff.useable.GlyphWorkbench;
import games.stendhal.server.events.ItemUpgradeEvent;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPEvent;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import games.stendhal.server.core.rule.glyph.GlyphProgressionService;
import games.stendhal.server.core.rule.glyph.GlyphUpgradeService;
import games.stendhal.server.core.rule.glyph.GlyphCraftingService;
import games.stendhal.server.core.rule.glyph.GlyphWorkbenchCatalog;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class RuneMasterNPCTest {
    @BeforeClass public static void init() {
        MockStendlRPWorld.get(); ItemTestHelper.generateRPClasses();
    }

    @Test public void costsCoverAllTwentyOneGlyphsAndTripleOnlyAdditionalMaterials() {
        GlyphUpgradeService service = new GlyphUpgradeService();
        int kinds = 0;
        for (DefaultItem template : SingletonRepository.getEntityManager().getDefaultItems()) {
            if (template.getImplementation() != Glyph.class) continue;
            Item definition = template.getItem(ItemCreationContext.restore());
            if (!GlyphProgressionService.isUpgradeable(definition)) continue;
            kinds++;
            assertEquals(24, service.getRequiredFragments(definition, 2));
            assertEquals(60, service.getRequiredFragments(definition, 3));
            assertEquals(200000L, service.getRequiredCoins(definition, 2));
            assertEquals(500000L, service.getRequiredCoins(definition, 3));
            Map<String,Integer> second = service.getRequiredMaterials(definition, 2);
            Map<String,Integer> third = service.getRequiredMaterials(definition, 3);
            assertFalse(second.isEmpty());
            for (Map.Entry<String,Integer> material : second.entrySet()) {
                assertNotNull(material.getKey(), SingletonRepository.getEntityManager().getItem(material.getKey()));
                assertEquals(material.getValue() * 3, third.get(material.getKey()).intValue());
            }
            assertFalse(second.containsKey("fragment glifu")); assertFalse(second.containsKey("money"));
        }
        assertEquals(21, kinds);
        Item mokosz = glyph("glif daru Mokoszy");
        assertEquals(1000, service.getRequiredMaterials(mokosz, 2).get("polano").intValue());
        assertEquals(100, service.getRequiredMaterials(mokosz, 2).get("serce olbrzyma").intValue());
    }

    @Test public void upgradeKeepsEquippedItemAndBindingAndNeverHealsOrReplaysPayment() {
        Player player = PlayerTestHelper.createPlayer("glyph-upgrade");
        Item glyph = glyph("glif daru Mokoszy"); glyph.setBoundTo(player.getName());
        player.getSlot("healing_rune").add(glyph);
        player.setBaseHP(1100); player.setHP(400);
        GlyphUpgradeService service = new GlyphUpgradeService();
        fund(player, service, glyph, 2);
        assertTrue(service.prepareUpgrade(player, glyph));
        assertTrue(service.upgrade(player, glyph, 1));
        assertSame(glyph, player.getSlot("healing_rune").iterator().next());
        assertEquals(player.getName(), glyph.getBoundTo());
        assertEquals(2, GlyphProgressionService.getLevel(glyph));
        assertEquals(1250, player.getBaseHP()); assertEquals(400, player.getHP());
        assertEquals(0L, MoneyUtils.getTotalMoneyInCopper(player));
        assertEquals(0, player.getNumberOfEquipped("polano"));
        assertEquals(0, player.getNumberOfEquipped("fragment glifu"));
        assertFalse(service.upgrade(player, glyph, 1));
        fund(player, service, glyph, 3);
        assertTrue(service.prepareUpgrade(player, glyph));
        assertTrue(service.upgrade(player, glyph, 2));
        assertEquals(1500, player.getBaseHP()); assertEquals(400, player.getHP());
        assertFalse(service.prepareUpgrade(player, glyph));
    }

    @Test public void missingPaymentOrMaterialAndDirtyFragmentsNeverConsumeAnything() {
        for (String missing : new String[] {"money", "fragment glifu", "polano", "serce olbrzyma"}) {
            Player player = PlayerTestHelper.createPlayer("missing-" + missing);
            Item glyph = glyph("glif daru Mokoszy"); player.getSlot("bag").add(glyph);
            GlyphUpgradeService service = new GlyphUpgradeService(); fund(player, service, glyph, 2);
            assertTrue(service.prepareUpgrade(player, glyph));
            assertTrue(player.drop(missing, 1));
            long coins = MoneyUtils.getTotalMoneyInCopper(player);
            int wood = player.getNumberOfEquipped("polano");
            int fragments = player.getNumberOfEquipped("fragment glifu");
            assertFalse(missing, service.upgrade(player, glyph, 1));
            assertEquals(coins, MoneyUtils.getTotalMoneyInCopper(player));
            assertEquals(wood, player.getNumberOfEquipped("polano"));
            assertEquals(fragments, player.getNumberOfEquipped("fragment glifu"));
            assertEquals(1, GlyphProgressionService.getLevel(glyph));
        }
        Player player = PlayerTestHelper.createPlayer("dirty-fragments");
        Item glyph = glyph("glif daru Mokoszy"); player.getSlot("bag").add(glyph);
        GlyphUpgradeService service = new GlyphUpgradeService(); fund(player, service, glyph, 2);
        player.getFirstEquipped("fragment glifu").setItemData("spękany");
        assertTrue(service.prepareUpgrade(player, glyph));
        assertFalse(service.upgrade(player, glyph, 1));
        assertEquals(200000L, MoneyUtils.getTotalMoneyInCopper(player));
    }

    @Test public void movingReplacingOrRelevelingPreviewRejectsPaymentAndForeignAndKnowledgeGlyphs() {
        Player player = PlayerTestHelper.createPlayer("stale-glyph");
        Item glyph = glyph("glif daru Mokoszy"); player.getSlot("bag").add(glyph);
        GlyphUpgradeService service = new GlyphUpgradeService(); fund(player, service, glyph, 2);
        assertTrue(service.prepareUpgrade(player, glyph));
        player.getSlot("bag").remove(glyph.getID()); player.getSlot("healing_rune").add(glyph);
        assertFalse(service.upgrade(player, glyph, 1));
        assertEquals(200000L, MoneyUtils.getTotalMoneyInCopper(player));
        assertTrue(service.prepareUpgrade(player, glyph));
        assertFalse(service.upgrade(player, glyph("glif daru Mokoszy"), 1));
        assertTrue(service.prepareUpgrade(player, glyph)); GlyphProgressionService.applyLevel(glyph, 2);
        assertFalse(service.upgrade(player, glyph, 1));
        glyph.setBoundTo("someone-else"); assertFalse(service.prepareUpgrade(player, glyph));
        Item knowledge = glyph("glif wiedzy"); player.getSlot("bag").add(knowledge);
        assertFalse(service.prepareUpgrade(player, knowledge));
        assertEquals(200000L, MoneyUtils.getTotalMoneyInCopper(player));
    }

    @Test public void unexpectedResourceFailureRestoresExactStacksAndMoney() {
        Player player = new Player(PlayerTestHelper.createPlayer("interrupted-upgrade")) {
            @Override public boolean drop(Item item, int amount) {
                if ("serce olbrzyma".equals(item.getName())) throw new IllegalStateException("Interrupted resource debit");
                return super.drop(item, amount);
            }
        };
        Item glyph = glyph("glif daru Mokoszy"); player.getSlot("bag").add(glyph);
        GlyphUpgradeService service = new GlyphUpgradeService(); fund(player, service, glyph, 2);
        Item wood = player.getFirstEquipped("polano");
        assertTrue(service.prepareUpgrade(player, glyph));
        assertFalse(service.upgrade(player, glyph, 1));
        assertEquals(200000L, MoneyUtils.getTotalMoneyInCopper(player));
        assertEquals(1000, player.getNumberOfEquipped("polano"));
        assertTrue(player.getAllEquipped("polano").stream().anyMatch(item -> item == wood));
        assertEquals(24, player.getNumberOfEquipped("fragment glifu"));
        assertEquals(1, GlyphProgressionService.getLevel(glyph));
    }

    @Test public void interruptedCoinDebitRefundsEarlierStacksWithoutTouchingMaterials() {
        for (boolean throwsError : new boolean[] {false, true}) {
            Player player = new Player(PlayerTestHelper.createPlayer("interrupted-coins-" + throwsError)) {
                private int debits;
                @Override public boolean drop(String name, int amount) {
                    if ("money".equals(name) && ++debits == 2) {
                        if (throwsError) throw new IllegalStateException("Interrupted coin debit");
                        return false;
                    }
                    return super.drop(name, amount);
                }
            };
            Item glyph = glyph("glif daru Mokoszy"); player.getSlot("bag").add(glyph);
            GlyphUpgradeService service = new GlyphUpgradeService(); fund(player, service, glyph, 2);
            ((StackableItem)player.getFirstEquipped("money")).setQuantity(100000);
            add(player, "money", 100000);
            assertTrue(service.prepareUpgrade(player, glyph));
            assertFalse(service.upgrade(player, glyph, 1));
            assertEquals(200000L, MoneyUtils.getTotalMoneyInCopper(player));
            assertEquals(1000, player.getNumberOfEquipped("polano"));
            assertEquals(24, player.getNumberOfEquipped("fragment glifu"));
            assertEquals(1, GlyphProgressionService.getLevel(glyph));
        }
    }

    private Item glyph(String name) {
        return SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.quest());
    }

    @Test public void workbenchOpensWindowAndUpgradesTheSelectedCopyWithAnExactPreview() {
        Player player = PlayerTestHelper.createPlayer("rune-window");
        Item first = glyph("glif daru Mokoszy"), second = glyph("glif daru Mokoszy");
        GlyphProgressionService.applyLevel(second, 2);
        player.getSlot("bag").add(first); player.getSlot("bag").add(second);
        fund(player, new GlyphUpgradeService(), second, 3);
        npc(player);
        GlyphWorkbench table = workbench(player);
        assertNotNull(table); assertEquals(2.0, table.getWidth(), 0.0); assertEquals(1.0, table.getHeight(), 0.0);
        player.setPosition(table.getX(), table.getY() + 1);
        assertTrue(table.onUsed(player));
        RPEvent open = lastUpgradeEvent(player);
        assertEquals("glyph", open.get("service")); assertEquals("open", open.get("phase"));
        RPAction reopen = new RPAction(); reopen.put("service", "glyph"); reopen.put("command", "open");
        reopen.put("npc_id", table.getID().getObjectID()); reopen.put("window_request", "table-click");
        new ItemUpgradeAction().onAction(player, reopen);
        assertEquals("table-click", lastUpgradeEvent(player).get("window_request"));
        assertFalse(open.get("window_id").equals(lastUpgradeEvent(player).get("window_id")));
        action(player, table, "preview", second, null);
        RPEvent preview = lastUpgradeEvent(player);
        assertEquals(2, preview.getInt("upgrade_level"));
        assertEquals(500000, preview.getInt("fee"));
        assertTrue(preview.getList("current_stat_values").contains("250"));
        assertTrue(preview.getList("upgraded_stat_values").contains("500"));
        assertTrue(preview.getList("material_values").contains("3000"));
        assertTrue(preview.getList("material_values").contains("60"));
        assertEquals(preview.getList("material_names").size(), preview.getList("material_glyph_levels").size());
        assertEquals("2", preview.getList("material_glyph_levels").get(0));
        String token = preview.get("request_token");
        action(player, table, "upgrade", second, token);
        assertEquals(1, GlyphProgressionService.getLevel(first)); assertEquals(3, GlyphProgressionService.getLevel(second));
        assertEquals(0L, MoneyUtils.getTotalMoneyInCopper(player));
        action(player, table, "upgrade", second, token);
        assertEquals(3, GlyphProgressionService.getLevel(second));
    }

    @Test public void catalogContainsEveryRecipeAndDimsUnavailableRowsWithoutHidingThem() {
        assertEquals(63, GlyphWorkbenchCatalog.entries().size());
        assertEquals(21, GlyphWorkbenchCatalog.entries().stream().filter(entry -> entry.level == 1).count());
        Player player = PlayerTestHelper.createPlayer("catalog-empty"); npc(player);
        GlyphWorkbench table = workbench(player); player.setPosition(table.getX(), table.getY() + 1); table.onUsed(player);
        RPEvent event = lastUpgradeEvent(player);
        assertEquals(63, event.getList("recipe_ids").size());
        assertEquals(63, event.getList("recipe_ready").size());
        assertFalse(event.getList("recipe_ready").contains("1"));
        RPAction select = new RPAction(); select.put("service", "glyph"); select.put("command", "preview");
        select.put("npc_id", table.getID().getObjectID()); select.put("glyph_recipe", GlyphWorkbenchCatalog.find("glif siły", 1).id);
        new ItemUpgradeAction().onAction(player, select);
        RPEvent detail = lastUpgradeEvent(player);
        assertEquals("create", detail.get("operation"));
        assertTrue(detail.getList("material_names").contains("fragment glifu"));
        assertEquals(0, detail.getInt("can_upgrade"));
        assertNull(GlyphWorkbenchCatalog.find("glif wiedzy", 1));
        for (Map.Entry<String, Integer> resource : GlyphCraftingService.getRecipes().get("glif siły").entrySet()) {
            Item ingredient = SingletonRepository.getEntityManager().getItem(resource.getKey());
            if (ingredient instanceof StackableItem) ((StackableItem)ingredient).setQuantity(resource.getValue());
            player.getSlot("bag").add(ingredient);
        }
        new ItemUpgradeAction().onAction(player, select);
        detail = lastUpgradeEvent(player); assertEquals(1, detail.getInt("can_upgrade"));
        select.put("command", "upgrade"); select.put("request_token", detail.get("request_token"));
        new ItemUpgradeAction().onAction(player, select);
        assertEquals(1, player.getNumberOfEquipped("glif siły"));
        new ItemUpgradeAction().onAction(player, select);
        assertEquals(1, player.getNumberOfEquipped("glif siły"));
    }

    @Test public void craftingCreatesLevelOneOnceAndMissingIngredientsOrFullBagConsumeNothing() {
        Player player = PlayerTestHelper.createPlayer("glyph-crafting");
        for (Map.Entry<String, Integer> resource : GlyphCraftingService.getRecipes().get("glif siły").entrySet()) {
            Item item = SingletonRepository.getEntityManager().getItem(resource.getKey());
            if (item instanceof StackableItem) ((StackableItem)item).setQuantity(resource.getValue() + 1);
            player.getSlot("bag").add(item);
        }
        // One non-stackable sword, plus one spare of each stackable material.
        String token = GlyphCraftingService.prepare(player, "glif siły"); assertNotNull(token);
        Item result = GlyphCraftingService.craft(player, "glif siły", token); assertNotNull(result);
        assertSame(result, player.getFirstEquipped("glif siły")); assertEquals(1, GlyphProgressionService.getLevel(result));
        assertNull(GlyphCraftingService.craft(player, "glif siły", token));
        assertEquals(1, player.getNumberOfEquipped("fragment glifu"));
        assertNull(GlyphCraftingService.prepare(player, "glif siły"));
        Player full = PlayerTestHelper.createPlayer("glyph-full-bag");
        for (Map.Entry<String, Integer> resource : GlyphCraftingService.getRecipes().get("glif żywej wody").entrySet()) {
            add(full, resource.getKey(), resource.getValue() + 1);
        }
        while (!full.getSlot("bag").isFull()) full.getSlot("bag").add(glyph("glif tarczy"));
        int fragments = full.getNumberOfEquipped("fragment glifu");
        token = GlyphCraftingService.prepare(full, "glif żywej wody"); assertNotNull(token);
        assertNull(GlyphCraftingService.craft(full, "glif żywej wody", token));
        assertEquals(fragments, full.getNumberOfEquipped("fragment glifu"));
        assertEquals(0, full.getNumberOfEquipped("glif żywej wody"));
    }

    @Test public void windowRejectsWrongTokensAndRemoteUseWithoutTakingResources() {
        Player player = PlayerTestHelper.createPlayer("rune-window-security");
        Item glyph = glyph("glif daru Mokoszy"); player.getSlot("bag").add(glyph);
        fund(player, new GlyphUpgradeService(), glyph, 2);
        npc(player); GlyphWorkbench table = workbench(player); assertNotNull(table);
        player.setPosition(table.getX(), table.getY() + 1); table.onUsed(player);
        action(player, table, "preview", glyph, null);
        action(player, table, "upgrade", glyph, "forged");
        assertEquals(1, GlyphProgressionService.getLevel(glyph));
        action(player, table, "preview", glyph, null);
        String token = lastUpgradeEvent(player).get("request_token");
        player.setPosition(100, 90);
        assertFalse(table.onUsed(player));
        action(player, table, "upgrade", glyph, token);
        assertEquals(1, GlyphProgressionService.getLevel(glyph));
        assertEquals(200000L, MoneyUtils.getTotalMoneyInCopper(player));
        assertEquals(1000, player.getNumberOfEquipped("polano"));
    }

    private GlyphWorkbench workbench(Player player) {
        for (RPObject object : player.getZone()) if (object instanceof GlyphWorkbench) return (GlyphWorkbench)object;
        return null;
    }
    private RPEvent lastUpgradeEvent(Player player) {
        RPEvent result = null;
        for (RPEvent event : player.events()) if (Events.ITEM_UPGRADE.equals(event.getName())) result = event;
        assertNotNull(result); return result;
    }
    private void action(Player player, GlyphWorkbench table, String command, Item item, String token) {
        RPAction action = new RPAction();
        action.put("service", "glyph"); action.put("command", command); action.put("npc_id", table.getID().getObjectID());
        if (item != null) {
            action.put("target_path", ItemUpgradeEvent.decodePath(ItemUpgradeEvent.encodePath(item)));
            action.put("glyph_recipe", GlyphWorkbenchCatalog.find(item.getName(), Math.min(3, GlyphProgressionService.getLevel(item) + 1)).id);
        }
        if (token != null) action.put("request_token", token);
        new ItemUpgradeAction().onAction(player, action);
    }

    @Test public void originalRecipesDoNotConsumeAdvancedGlyphsAndStillCreateLevelOne() {
        Player player = PlayerTestHelper.createPlayer("rune-recipe");
        Item advanced = glyph("glif siły"); GlyphProgressionService.applyLevel(advanced, 2);
        player.getSlot("bag").add(advanced);
        add(player, "rubin", 18); add(player, "pióro azazela", 12);
        add(player, "pióro lilith", 4); add(player, "fragment glifu", 5);
        player.setQuest("glyph_creator", "start;glif Peruna");
        SpeakerNPC npc = npc(player);
        try {
            say(npc, player, "hi"); say(npc, player, "glif Peruna");
            assertEquals(0, player.getNumberOfEquipped("glif Peruna"));
            assertEquals(18, player.getNumberOfEquipped("rubin"));
            Item basic = glyph("glif siły"); player.getSlot("bag").add(basic);
            say(npc, player, "glif Peruna");
            assertEquals(1, player.getNumberOfEquipped("glif Peruna"));
            assertEquals(1, GlyphProgressionService.getLevel(player.getFirstEquipped("glif Peruna")));
            assertSame(advanced, player.getFirstEquipped("glif siły"));
        } finally { SingletonRepository.getNPCList().remove("Zoryk Runiczny"); }
    }

    private SpeakerNPC npc(Player player) {
        SingletonRepository.getNPCList().remove("Zoryk Runiczny");
        StendhalRPZone zone = new StendhalRPZone("glyph_upgrade_dialog", 128, 128);
        MockStendlRPWorld.get().addRPZone(zone); zone.add(player);
        new RuneMasterNPC().configureZone(zone, null);
        return SingletonRepository.getNPCList().get("Zoryk Runiczny");
    }
    private void say(SpeakerNPC npc, Player player, String message) {
        npc.getEngine().stepTest(player, message);
    }
    private void fund(Player player, GlyphUpgradeService service, Item glyph, int target) {
        add(player, "money", (int)service.getRequiredCoins(glyph, target));
        add(player, "fragment glifu", service.getRequiredFragments(glyph, target));
        for (Map.Entry<String,Integer> material : service.getRequiredMaterials(glyph, target).entrySet()) {
            int half = material.getValue() / 2;
            add(player, material.getKey(), half); add(player, material.getKey(), material.getValue() - half);
        }
    }
    private void add(Player player, String name, int quantity) {
        if (quantity <= 0) return;
        StackableItem item = (StackableItem)SingletonRepository.getEntityManager().getItem(name);
        item.setQuantity(quantity); player.getSlot("bag").add(item);
    }
}
