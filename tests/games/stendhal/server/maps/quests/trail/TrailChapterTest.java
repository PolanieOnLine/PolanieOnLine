package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.koscielisko.plains.BrzezdomNPC;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.WegielNaOpal;
import games.stendhal.server.maps.zakopane.shop.StasekNPC;
import games.stendhal.server.maps.zakopane.tavern.JagnaNPC;
import games.stendhal.server.maps.zakopane.townhall.GazdaWojtekNPC;
import games.stendhal.server.maps.zakopane.townhall.GosciradNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class TrailChapterTest {
    private static final String[] NAMES = {"Stasek", "Gościrad", "Jagna", "Brzezdom", "Gazda Wojtek"};
    private final StendhalQuestSystem system = StendhalQuestSystem.get();
    private final Map<String, SpeakerNPC> oldNpcs = new HashMap<>();
    private List<IQuest> loaded;
    private List<IQuest> oldQuests;
    private Player player;
    private StendhalRPZone shop, townhall, forest, tavern;

    @BeforeClass public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }

    @Before @SuppressWarnings("unchecked") public void setUp() throws Exception {
        Field field = StendhalQuestSystem.class.getDeclaredField("quests"); field.setAccessible(true);
        loaded = (List<IQuest>) field.get(null); oldQuests = new ArrayList<>(loaded); loaded.clear();
        for (String name : NAMES) oldNpcs.put(name, NPCList.get().remove(name));
        shop = zone("int_zakopane_shop"); townhall = zone("int_zakopane_townhall");
        forest = zone("0_koscielisko_e"); tavern = zone("int_zakopane_tavern");
        new StasekNPC().configureZone(shop, null);
        new GosciradNPC().configureZone(townhall, null);
        new GazdaWojtekNPC().configureZone(townhall, null);
        new JagnaNPC().configureZone(tavern, null);
        new BrzezdomNPC().configureZone(forest, null);
        // Real existing dialogues must coexist with the new chapter.
        new WegielNaOpal().addToWorld();
        system.loadQuest(new UnexpectedDeliveryQuest()); system.loadQuest(new LastGuestQuest());
        system.loadQuest(new ForgedSealQuest()); system.loadQuest(new DebtToZakopaneQuest());
        player = PlayerTestHelper.createPlayer("trail-investigator"); shop.add(player);
        MockStendhalRPRuleProcessor.get().addPlayer(player);
    }

    private StendhalRPZone zone(String name) {
        StendhalRPZone result = new StendhalRPZone(name, 128, 128);
        MockStendlRPWorld.get().addRPZone(result); return result;
    }
    @After public void tearDown() {
        loaded.clear(); loaded.addAll(oldQuests);
        MockStendhalRPRuleProcessor.get().clearPlayers();
        for (String name : NAMES) { NPCList.get().remove(name); if (oldNpcs.get(name) != null) NPCList.get().add(oldNpcs.get(name)); }
        for (StendhalRPZone zone : new StendhalRPZone[] {shop, townhall, forest, tavern}) MockStendlRPWorld.get().removeZone(zone);
    }
    private String say(String name, String text) {
        SpeakerNPC npc = NPCList.get().get(name);
        assertTrue(name + " should understand " + text, npc.getEngine().step(player, text));
        return getReply(npc);
    }
    private void visit(String name) {
        SpeakerNPC npc = NPCList.get().get(name);
        npc.getEngine().setCurrentState(ConversationStates.IDLE); say(name, "hi");
    }
    private void accept(String name, String topic) { visit(name); say(name, topic); say(name, "tak"); }
    private boolean use(TrailEvidence.Kind kind) {
        TrailEvidence prop = new TrailEvidence(kind);
        StendhalRPZone target = kind == TrailEvidence.Kind.SHIPMENT ? shop : kind == TrailEvidence.Kind.REGISTRY ? townhall : forest;
        target.add(prop); prop.setPosition(60, 61);
        if (player.getZone() != target) { player.getZone().remove(player); target.add(player); }
        player.setPosition(59, 61);
        boolean result = prop.onUsed(player); target.remove(prop); return result;
    }
    private void ready() { player.setQuest("wegiel_na_opal", "done"); }
    private void delivery() {
        ready(); accept("Stasek", "dostawa"); assertEquals("inspect", player.getQuest("trail_delivery"));
        assertTrue(use(TrailEvidence.Kind.SHIPMENT)); assertEquals("archive", player.getQuest("trail_delivery"));
        assertTrue(use(TrailEvidence.Kind.REGISTRY));
        visit("Gościrad"); say("Gościrad", "zamówienie"); assertTrue(player.isQuestCompleted("trail_delivery"));
    }
    private void guest() {
        delivery(); accept("Gościrad", "woźnica");
        visit("Jagna"); say("Jagna", "woźnica");
        visit("Brzezdom"); say("Brzezdom", "woźnicę");
        assertTrue(use(TrailEvidence.Kind.TRACKS));
        say("Brzezdom", "woźnicy"); assertTrue(player.isQuestCompleted("trail_guest"));
    }

    @Test public void prerequisiteAndOffersCannotSkipTheStory() {
        assertTrue(system.getMainQuests(player).isEmpty());
        visit("Stasek"); say("Stasek", "dostawa"); assertFalse(player.hasQuest("trail_delivery"));
        ready(); assertEquals(1, system.getMainQuests(player).size());
        say("Stasek", "task"); say("Stasek", "nie"); assertFalse(player.hasQuest("trail_delivery"));
        say("Stasek", "dostawa"); say("Stasek", "tak");
        assertEquals("inspect", player.getQuest("trail_delivery"));
        visit("Gościrad"); say("Gościrad", "woźnica"); assertFalse(player.hasQuest("trail_guest"));
        assertFalse(use(TrailEvidence.Kind.REGISTRY)); assertFalse(use(TrailEvidence.Kind.DOCUMENTS));
    }

    @Test public void completeStoryRecordsEvidenceWitnessesAndOnlyOneRewardPerQuest() {
        guest(); accept("Brzezdom", "skład");
        assertEquals("guard", player.getQuest("trail_seal")); assertFalse(use(TrailEvidence.Kind.DOCUMENTS));
        Creature guard = SingletonRepository.getEntityManager().getCreature("strażnik składu");
        guard.setPosition(62, 61); forest.add(guard);
        guard.setRespawnPoint(new CreatureRespawnPoint(forest, 62, 61, guard, 1));
        guard.onDamaged(player, guard.getHP()); guard.onDead(player);
        assertEquals("documents", player.getQuest("trail_seal"));
        assertTrue(use(TrailEvidence.Kind.DOCUMENTS));
        say("Brzezdom", "skład"); assertTrue(player.isQuestCompleted("trail_seal"));
        accept("Gazda Wojtek", "dowody"); assertEquals("witnesses;0", player.getQuest("trail_debt"));
        say("Gazda Wojtek", "dowody"); assertFalse(player.isQuestCompleted("trail_debt"));
        visit("Stasek"); say("Stasek", "dowody"); say("Stasek", "dowody");
        assertEquals("witnesses;1", player.getQuest("trail_debt"));
        visit("Jagna"); say("Jagna", "dowody"); assertEquals("report", player.getQuest("trail_debt"));
        visit("Gazda Wojtek"); String ending = say("Gazda Wojtek", "dowody"); assertTrue(ending, ending.contains("Semos"));
        assertTrue(player.isQuestCompleted("trail_debt")); assertTrue(system.getMainQuests(player).isEmpty());
        int xp = player.getXP(); int coins = player.getTotalNumberOf("money");
        say("Gazda Wojtek", "dowody"); visit("Brzezdom"); say("Brzezdom", "skład");
        visit("Gościrad"); say("Gościrad", "zamówienie");
        assertEquals(xp, player.getXP()); assertEquals(coins, player.getTotalNumberOf("money"));
    }

    @Test public void publicCluesRemainAvailableForOtherPlayersAndRequireSameZoneAndRange() {
        ready(); accept("Stasek", "dostawa");
        TrailEvidence prop = new TrailEvidence(TrailEvidence.Kind.SHIPMENT); prop.setPosition(7, 10); shop.add(prop);
        player.setPosition(30, 30); assertFalse(prop.onUsed(player)); assertEquals("inspect", player.getQuest("trail_delivery"));
        player.setPosition(6, 10); assertTrue(prop.onUsed(player)); assertFalse(prop.onUsed(player));
        Player other = PlayerTestHelper.createPlayer("trail-other"); other.setQuest("trail_delivery", "inspect");
        townhall.add(other); other.setPosition(6, 10); assertFalse(prop.onUsed(other));
        townhall.remove(other); shop.add(other); assertTrue(prop.onUsed(other));
        assertEquals("archive", other.getQuest("trail_delivery")); assertEquals(shop, prop.getZone());
    }

    @Test public void guardCreditRequiresAnActiveInvestigationAtTheStockpile() {
        Creature guard = SingletonRepository.getEntityManager().getCreature("strażnik składu"); forest.add(guard); guard.setPosition(62, 61);
        TrailQuestLine.recordGuardKill(player, guard); assertFalse(player.hasQuest("trail_seal"));
        player.setQuest("trail_seal", "guard");
        TrailQuestLine.recordGuardKill(player, guard); assertEquals("guard", player.getQuest("trail_seal"));
        shop.remove(player); forest.add(player); player.setPosition(61, 61);
        guard.setRespawnPoint(new CreatureRespawnPoint(forest, 10, 10, guard, 1));
        TrailQuestLine.recordGuardKill(player, guard); assertEquals("guard", player.getQuest("trail_seal"));
        guard.setRespawnPoint(new CreatureRespawnPoint(forest, 62, 61, guard, 1));
        // A genuine guard still counts if it follows the player away from its original tile.
        guard.setPosition(80, 80); player.setPosition(79, 80);
        TrailQuestLine.recordGuardKill(player, guard); assertEquals("documents", player.getQuest("trail_seal"));
        TrailQuestLine.recordGuardKill(player, guard); assertEquals("documents", player.getQuest("trail_seal"));
    }

    @Test public void worldSitesUseSharedClientTilesAndRespawnTheCorrectGuard() {
        new TrailSites().configureZone(shop, null);
        new TrailSites().configureZone(townhall, null);
        new TrailSites().configureZone(forest, null);
        assertEquals(1, forest.getRespawnPointList().size());
        CreatureRespawnPoint point = forest.getRespawnPointList().get(0);
        assertEquals("strażnik składu", point.getPrototypeCreature().getName());
        assertEquals(62, point.getX()); assertEquals(61, point.getY());
        assertTrue(point.getPrototypeCreature().getAIProfiles().containsKey("no_elite"));
        point.onTurnReached(0);
        assertEquals(1, point.size());
    }

    @Test public void journalShowsCurrentClueAndRetainsProgressAfterReload() {
        ready(); accept("Stasek", "dostawa");
        String title = system.getMainQuests(player).get(0);
        assertTrue(system.getQuestProgressDetails(player, title).get(0).contains("skrzynię"));
        assertTrue(use(TrailEvidence.Kind.SHIPMENT));
        Player reloaded = new Player(player); assertEquals("archive", reloaded.getQuest("trail_delivery"));
        assertTrue(system.getQuestProgressDetails(reloaded, title).get(0).contains("rejestr"));
        assertFalse(reloaded.hasQuest("trail_guest"));
    }
}
