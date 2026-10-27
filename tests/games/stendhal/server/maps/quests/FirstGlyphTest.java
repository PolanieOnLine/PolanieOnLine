package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.glyph.GlyphExperienceService;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.semos.plains.ExperiencedWarriorNPC;
import games.stendhal.server.maps.semos.library.LibrarianNPC;
import games.stendhal.server.maps.semos.temple.HealerNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import marauroa.common.game.RPObject;

public class FirstGlyphTest {
    private FirstGlyph quest;
    private Player player;
    private StendhalRPZone zone;
    private SpeakerNPC speaking;

    @BeforeClass public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }
    @Before public void setUp() {
        for (String name : new String[] {"Starkad", "Ceryl", "Ilisa"}) SingletonRepository.getNPCList().remove(name);
        zone = new StendhalRPZone("knowledge_quest", 128, 128);
        MockStendlRPWorld.get().addRPZone(zone);
        new ExperiencedWarriorNPC().configureZone(zone, null);
        new LibrarianNPC().configureZone(zone, null);
        new HealerNPC().configureZone(zone, null);
        quest = new FirstGlyph();
        quest.addToWorld();
        player = PlayerTestHelper.createPlayer("learner");
        player.setLevel(20);
        zone.add(player);
        MockStendhalRPRuleProcessor.get().addPlayer(player);
    }
    @After public void tearDown() {
        MockStendhalRPRuleProcessor.get().clearPlayers();
        for (String name : new String[] {"Starkad", "Ceryl", "Ilisa"}) SingletonRepository.getNPCList().remove(name);
    }
    private void visit(String name) {
        if (speaking != null) say("bye");
        speaking = SingletonRepository.getNPCList().get(name);
        say("hi");
    }
    private void say(String words) { speaking.getEngine().stepTest(player, words); }
    private void start() { visit("Starkad"); say("task"); say("yes"); }
    private String state() { return player.getQuest(FirstGlyph.QUEST_SLOT); }
    private void herbs(int amount) {
        StackableItem item = (StackableItem) SingletonRepository.getEntityManager().getItem("arandula");
        item.setQuantity(amount);
        assertTrue(player.equip("bag", item));
    }
    private Creature creature(String name) {
        Creature creature = SingletonRepository.getEntityManager().getCreature(name);
        creature.setPosition(60, 60);
        zone.add(creature);
        return creature;
    }
    private void readAndClean() {
        start();
        visit("Ceryl"); say("zatarty znak"); say("historia"); say("wiedza");
        assertEquals("herbs", state());
        visit("Ilisa"); say("zatarty znak"); herbs(3); say("przygotuj napar");
        assertEquals("trial;0", state());
    }
    private void observeAll() {
        for (String name : new String[] {"goblin", "wilk", "dzik"}) FirstGlyph.recordKill(player, creature(name));
        assertEquals("trial;7", state());
    }

    @Test public void completeStoryPersistsProgressAndAwardsOneProtectedGlyph() {
        start();
        assertEquals("archive", state());
        visit("Ceryl"); say("zatarty znak"); say("historia"); say("złoto");
        assertEquals("archive", state());
        say("wiedza"); assertEquals("herbs", state());
        visit("Ilisa"); say("zatarty znak"); herbs(2); say("przygotuj napar");
        assertEquals("herbs", state());
        assertEquals(2, player.getNumberOfEquipped("arandula"));
        herbs(1); say("przygotuj napar");
        assertEquals("trial;0", state());
        assertEquals(0, player.getNumberOfEquipped("arandula"));
        // Exercise the real death/reward hook for the first observation.
        Creature goblin = creature("goblin");
        goblin.onDamaged(player, goblin.getHP());
        // The mock processor does not execute its queued death callback.
        goblin.onDead(player);
        assertEquals("trial;1", state());
        Player restored = new Player(new RPObject(player));
        assertEquals("trial;1", restored.getQuest(FirstGlyph.QUEST_SLOT));
        assertEquals(quest.getHistory(player), quest.getHistory(restored));
        FirstGlyph.recordKill(player, creature("wilk"));
        FirstGlyph.recordKill(player, creature("dzik"));
        visit("Starkad"); say("glif wiedzy");
        assertFalse(player.isEquipped(GlyphExperienceService.ITEM_NAME));
        visit("Ceryl"); say("zatarty znak");
        assertEquals("return", state());
        visit("Starkad"); say("glif wiedzy");
        assertEquals("done", state());
        Item reward = player.getFirstEquipped(GlyphExperienceService.ITEM_NAME);
        assertNotNull(reward);
        assertEquals(player.getName(), reward.getBoundTo());
        assertTrue(reward.isUndroppableOnDeath());
        assertNull(reward.getRarity());
        say("task"); say("glif wiedzy");
        assertEquals(1, player.getAllEquipped(GlyphExperienceService.ITEM_NAME).size());
    }

    @Test public void outOfOrderVisitsAndOldOrArenaKillsCannotSkipTheStory() {
        FirstGlyph.recordKill(player, creature("goblin"));
        assertFalse(player.hasQuest(FirstGlyph.QUEST_SLOT));
        start();
        FirstGlyph.recordKill(player, creature("goblin"));
        herbs(3); visit("Ilisa"); say("przygotuj napar");
        assertEquals("archive", state());
        assertEquals(3, player.getNumberOfEquipped("arandula"));
        visit("Ceryl"); say("wiedza");
        visit("Ilisa"); say("przygotuj napar");
        assertEquals("trial;0", state());
        Creature arenaGoblin = SingletonRepository.getEntityManager().getCreature("goblin");
        new StendhalRPZone("int_fado_battle_pit", 64, 64).add(arenaGoblin);
        FirstGlyph.recordKill(player, arenaGoblin);
        assertEquals("trial;0", state());
        FirstGlyph.recordKill(player, creature("szczur"));
        assertEquals("trial;0", state());
        FirstGlyph.recordKill(player, creature("goblin"));
        FirstGlyph.recordKill(player, creature("goblin"));
        assertEquals("trial;1", state());
    }

    @Test public void fullBagKeepsTheRewardAvailableUntilThereIsRoom() {
        readAndClean(); observeAll();
        visit("Ceryl"); say("zatarty znak");
        while (!player.getSlot("bag").isFull()) {
            player.getSlot("bag").add(SingletonRepository.getEntityManager().getItem("tarta"));
        }
        visit("Starkad"); say("glif wiedzy");
        assertEquals("return", state());
        assertFalse(player.isEquipped(GlyphExperienceService.ITEM_NAME));
        Item removable = (Item) player.getSlot("bag").iterator().next();
        removable.removeFromWorld();
        say("glif wiedzy");
        assertEquals("done", state());
        assertEquals(1, player.getAllEquipped(GlyphExperienceService.ITEM_NAME).size());
    }

    @Test public void minimumLevelAndRefusalDoNotPreventALaterAttempt() {
        player.setLevel(19); start();
        assertFalse(player.hasQuest(FirstGlyph.QUEST_SLOT));
        player.setLevel(20); say("task"); say("no");
        assertFalse(player.hasQuest(FirstGlyph.QUEST_SLOT));
        say("task"); say("yes");
        assertEquals("archive", state());
    }

    @Test public void translatedStoryKeywordsWorkWithoutChangingTheSavedStages() {
        visit("Starkad"); say("misión"); say("sí");
        assertEquals("archive", state());
        visit("Ceryl"); say("signo borrado"); say("conocimiento");
        assertEquals("herbs", state());
        visit("Ilisa"); herbs(3); say("prepara la infusión");
        assertEquals("trial;0", state());
    }
}
