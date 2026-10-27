package games.stendhal.server.maps.tarnow.city;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import games.stendhal.server.core.rule.glyph.GlyphProgressionService;
import games.stendhal.server.core.rule.glyph.GlyphUpgradeService;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.SpeakerNPCTestHelper;
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

    @Test public void conversationSelectsExactCopyAndShowsBothEffectsAndAllCosts() {
        Player player = PlayerTestHelper.createPlayer("rune-dialog");
        Item first = glyph("glif daru Mokoszy"), second = glyph("glif daru Mokoszy");
        GlyphProgressionService.applyLevel(second, 2);
        player.getSlot("bag").add(first); player.getSlot("bag").add(second);
        fund(player, new GlyphUpgradeService(), second, 3);
        SpeakerNPC npc = npc(player);
        try {
            say(npc, player, "hi"); say(npc, player, "ulepsz");
            String list = SpeakerNPCTestHelper.getReply(npc);
            assertTrue(list, list.contains("Mokoszy I")); assertTrue(list, list.contains("Mokoszy II"));
            say(npc, player, "2");
            String preview = SpeakerNPCTestHelper.getReply(npc);
            for (String expected : new String[] {"250", "500", "60", "500000", "3000", "polano", "serce olbrzyma", "magia ziemi"}) {
                assertTrue(preview, preview.contains(expected));
            }
            assertEquals(2, GlyphProgressionService.getLevel(second));
            say(npc, player, "tak");
            assertEquals(1, GlyphProgressionService.getLevel(first));
            assertEquals(3, GlyphProgressionService.getLevel(second));
            assertEquals(0L, MoneyUtils.getTotalMoneyInCopper(player));
            say(npc, player, "tak"); assertEquals(3, GlyphProgressionService.getLevel(second));
        } finally { SingletonRepository.getNPCList().remove("Zoryk Runiczny"); }
    }

    @Test public void englishAndSpanishUpgradeConversationsTranslateAndAllowCancellation() {
        for (String language : new String[] {"en", "es"}) {
            Player player = PlayerTestHelper.createPlayer("rune-dialog-" + language); player.setLanguage(language);
            Item glyph = glyph("glif daru Mokoszy"); player.getSlot("bag").add(glyph);
            fund(player, new GlyphUpgradeService(), glyph, 2);
            SpeakerNPC npc = npc(player);
            try {
                String command = "en".equals(language) ? "upgrade" : "mejorar";
                say(npc, player, "hi"); say(npc, player, command); say(npc, player, "1");
                String preview = SpeakerNPCTestHelper.getReply(npc);
                assertTrue(preview, preview.contains("en".equals(language) ? "Current power:" : "Poder actual:"));
                assertTrue(preview, preview.contains("en".equals(language) ? "you have:" : "tienes:"));
                say(npc, player, "no");
                assertEquals(1, GlyphProgressionService.getLevel(glyph));
                assertEquals(200000L, MoneyUtils.getTotalMoneyInCopper(player));
                say(npc, player, command); say(npc, player, "1");
                say(npc, player, "en".equals(language) ? "yes" : "sí");
                assertEquals(2, GlyphProgressionService.getLevel(glyph));
            } finally { SingletonRepository.getNPCList().remove("Zoryk Runiczny"); }
        }
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
