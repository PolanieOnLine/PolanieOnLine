package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.Level;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.krakow.archeologist.ArcheologistNPC;
import games.stendhal.server.maps.krakow.forest.WagabundusNPC;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.MithrilRing;
import games.stendhal.server.maps.quests.trail.OathTrailProgress.Encounter;
import games.stendhal.server.maps.quests.trail.OathTrailSites.Kind;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import marauroa.common.game.RPObject;

public class OathTrailStoryTest {
	private static final String[] NAMES = { "Wagabundus", "Amileusz", "Wszebor",
			"Mieczysław" };
	private final Map<String, SpeakerNPC> oldNpcs = new HashMap<>();
	private final List<StendhalRPZone> zones = new ArrayList<>();
	private final StendhalQuestSystem system = StendhalQuestSystem.get();
	private List<IQuest> loaded, previous;
	private StendhalRPZone north, west, south, house;
	private Player player;

	@BeforeClass
	public static void init() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	@SuppressWarnings("unchecked")
	public void setup() throws Exception {
		Field f = StendhalQuestSystem.class.getDeclaredField("quests");
		f.setAccessible(true);
		loaded = (List<IQuest>) f.get(null);
		previous = new ArrayList<>(loaded);
		loaded.clear();
		for (String name : NAMES)
			oldNpcs.put(name, NPCList.get().remove(name));
		north = zone(OathTrailProgress.NORTH);
		west = zone(OathTrailProgress.WEST);
		south = zone("0_krakow_wisla_s");
		house = zone("int_krakow_archeologist_house");
		new WagabundusNPC().configureZone(south, null);
		new ArcheologistNPC().configureZone(house, null);
		new OathTrailSites().configureZone(north, null);
		new OathTrailSites().configureZone(west, null);
		for (Step step : Step.values())
			if (step.chapter.equals(TrailQuestLine.OATH_CHAPTER))
				system.loadQuest(new OathTrailQuest(step));
		player = PlayerTestHelper.createPlayer("oath-story");
		south.add(player);
		MockStendhalRPRuleProcessor.get().addPlayer(player);
	}

	private StendhalRPZone zone(String name) {
		StendhalRPZone z = new StendhalRPZone(name, 128, 128);
		zones.add(z);
		MockStendlRPWorld.get().addRPZone(z);
		return z;
	}

	@After
	public void cleanup() {
		loaded.clear();
		loaded.addAll(previous);
		MockStendhalRPRuleProcessor.get().clearPlayers();
		for (String name : NAMES) {
			NPCList.get().remove(name);
			if (oldNpcs.get(name) != null)
				NPCList.get().add(oldNpcs.get(name));
		}
		for (StendhalRPZone z : zones)
			MockStendlRPWorld.get().removeZone(z);
	}

	private void move(StendhalRPZone z) {
		if (player.getZone() != z) {
			player.getZone().remove(player);
			z.add(player);
		}
	}

	private String say(String name, String topic) {
		SpeakerNPC npc = NPCList.get().get(name);
		assertTrue(name + ": " + topic, npc.getEngine().step(player, topic));
		return getReply(npc);
	}

	private String visit(String name) {
		SpeakerNPC npc = NPCList.get().get(name);
		move(npc.getZone());
		player.setPosition(npc.getX(), npc.getY() + 1);
		npc.getEngine().setCurrentState(ConversationStates.IDLE);
		return say(name, "hi");
	}

	private void accept(String name, String topic) {
		visit(name);
		say(name, topic);
		say(name, "tak");
	}

	private void ready(int level) {
		player.setQuest("wegiel_na_opal", "done");
		for (Step step : Step.values()) {
			if (step == Step.OATH_WATCH)
				break;
			player.setQuest(step.slot, "done");
		}
		player.setXP(Level.getXP(level));
		player.setLevel(level);
	}

	private OathTrailSites.Evidence evidence(Kind kind) {
		StendhalRPZone z = kind.zone.equals(OathTrailProgress.NORTH) ? north
				: west;
		for (Object e : z.getEntitiesOfClass(OathTrailSites.Evidence.class))
			if (((OathTrailSites.Evidence) e).kind == kind)
				return (OathTrailSites.Evidence) e;
		throw new AssertionError(kind);
	}

	private boolean use(Kind kind) {
		OathTrailSites.Evidence e = evidence(kind);
		move(e.getZone());
		player.setPosition(e.getX(), e.getY() + 1);
		return e.onUsed(player);
	}

	private OathTrailGuard guard(Encounter role) {
		StendhalRPZone z = role.zone.equals(OathTrailProgress.NORTH) ? north
				: west;
		CreatureRespawnPoint source = z.getRespawnPointList().get(0);
		OathTrailGuard guard = (OathTrailGuard) source.getPrototypeCreature().getNewInstance();
		guard.setRespawnPoint(source);
		guard.setPosition(role.x, role.y);
		z.add(guard);
		return guard;
	}

	private void kill(Encounter role) {
		move(role.zone.equals(OathTrailProgress.NORTH) ? north : west);
		player.setPosition(role.x - 1, role.y);
		OathTrailGuard g = guard(role);
		g.onDamaged(player, g.getHP() + g.getArmorHP());
		g.onDead(player);
	}

	private void watch() {
		ready(100);
		accept("Wagabundus", "poległych wartowników");
		kill(Encounter.WATCH);
		visit("Wszebor");
		say("Wszebor", "polegli wartownicy");
		visit("Wagabundus");
		say("Wagabundus", "polegli wartownicy");
	}

	private void names() {
		watch();
		accept("Amileusz", "znaku oddziału");
		assertTrue(use(Kind.NAMES));
		assertTrue(use(Kind.SHIELD));
		visit("Amileusz");
		say("Amileusz", "znak oddziału");
	}

	private void grave() {
		names();
		accept("Wagabundus", "samotnym grobie");
		assertTrue(use(Kind.GRAVE));
		visit("Wagabundus");
		say("Wagabundus", "samotny grób");
	}

	private void proof() {
		grave();
		accept("Amileusz", "ostatnim rozkazie");
		assertTrue(use(Kind.ORDER));
		visit("Amileusz");
		say("Amileusz", "ostatni rozkaz");
	}

	private void banner() {
		proof();
		accept("Wszebor", "chorągwi oddziału");
		assertFalse(use(Kind.BANNER));
		kill(Encounter.BANNER);
		assertTrue(use(Kind.BANNER));
		visit("Wszebor");
		say("Wszebor", "chorągiew oddziału");
	}

	@Test
	public void sixQuestsContinueTheStoryAtLevelOneHundred() {
		int count = 0;
		for (Step s : Step.values())
			if (s.chapter.equals(TrailQuestLine.OATH_CHAPTER)) {
				assertEquals(100, s.minimumLevel);
				count++;
			}
		assertEquals(6, count);
		visit("Wagabundus");
		say("Wagabundus", "polegli wartownicy");
		assertFalse(player.hasQuest(Step.OATH_WATCH.slot));
		ready(99);
		visit("Wagabundus");
		assertFalse(say("Wagabundus", "polegli wartownicy").contains("100"));
		assertTrue(system.getMainQuests(player).isEmpty());
		ready(100);
		accept("Wagabundus", "polegli wartownicy");
		assertEquals("fight", player.getQuest(Step.OATH_WATCH.slot));
		assertEquals(1, system.getMainQuests(player).size());
	}

	@Test
	public void wholeStoryUsesActualHighlightedTopicsAndRewardsOnlyOnce() {
		ready(100);
		int xp = player.getXP();
		banner();
		accept("Wagabundus", "wspólny pochówek");
		assertTrue(use(Kind.GRAVE));
		assertTrue(use(Kind.MOUND));
		assertTrue(use(Kind.MEMORIAL));
		visit("Wszebor");
		say("Wszebor", "dotrzymanym słowie");
		visit("Wagabundus");
		say("Wagabundus", "wspólny pochówek");
		assertTrue(player.isQuestCompleted(Step.OATH_BURIAL.slot));
		assertEquals(92002, player.getXP() - xp);
		assertEquals(2000, player.getTotalNumberOf("money"));
		Item item = player.getFirstEquipped("magiczna tarcza płytowa");
		assertNotNull(item);
		assertEquals(ItemRarity.RARE, item.getRarity());
		assertEquals(player.getName(), item.getBoundTo());
		assertTrue(ItemAffixState.hasAny(item));
		assertTrue(item.getDescription().contains("Borzywoja"));
		xp = player.getXP();
		say("Wagabundus", "wspólny pochówek");
		assertEquals(xp, player.getXP());
		assertEquals(1, player.getTotalNumberOf("magiczna tarcza płytowa"));
		assertTrue(system.getMainQuests(player).isEmpty());
	}

	@Test
	public void cluesArePersonalRangeCheckedAndKeepTheirHistoryAfterRelog() {
		assertFalse(use(Kind.NAMES));
		watch();
		accept("Amileusz", "znak oddziału");
		assertTrue(use(Kind.SHIELD));
		assertFalse(use(Kind.SHIELD));
		Player restored = new Player(player);
		player.getZone().remove(player);
		north.add(restored);
		player = restored;
		assertEquals("clues;1", player.getQuest(Step.OATH_NAMES.slot));
		assertFalse(
				TrailQuestLine.objective(player, Step.OATH_NAMES.slot).contains(
						"tarczy"));
		player.setPosition(1, 1);
		assertFalse(evidence(Kind.NAMES).onUsed(player));
		Player other = PlayerTestHelper.createPlayer("oath-other");
		north.add(other);
		other.setPosition(58, 41);
		assertFalse(evidence(Kind.NAMES).onUsed(other));
		assertTrue(use(Kind.NAMES));
		assertEquals(3,
				OathTrailProgress.history(player, Step.OATH_NAMES).size());
		visit("Amileusz");
		say("Amileusz", "znak oddziału");
		assertEquals(4,
				OathTrailProgress.history(player, Step.OATH_NAMES).size());
	}

	@Test
	public void burialRequiresRemainsThenMoundThenBannerThenRelease() {
		banner();
		accept("Wagabundus", "wspólny pochówek");
		assertFalse(use(Kind.MEMORIAL));
		assertFalse(use(Kind.MOUND));
		visit("Wszebor");
		say("Wszebor", "dotrzymane słowo");
		assertEquals("remains", player.getQuest(Step.OATH_BURIAL.slot));
		assertTrue(use(Kind.GRAVE));
		assertFalse(use(Kind.GRAVE));
		assertFalse(use(Kind.MEMORIAL));
		assertTrue(use(Kind.MOUND));
		assertFalse(use(Kind.MOUND));
		Player restored = new Player(player);
		player.getZone().remove(player);
		north.add(restored);
		player = restored;
		assertEquals("burial;1", player.getQuest(Step.OATH_BURIAL.slot));
		assertTrue(use(Kind.MEMORIAL));
		visit("Wagabundus");
		say("Wagabundus", "wspólny pochówek");
		assertFalse(player.isQuestCompleted(Step.OATH_BURIAL.slot));
		visit("Wszebor");
		say("Wszebor", "dotrzymane słowo");
		assertEquals("ready", player.getQuest(Step.OATH_BURIAL.slot));
		assertTrue(visit("Wszebor").contains("odeszli"));
		assertFalse(say("Wszebor", "dowódca").contains("Czekamy"));
		Player other = PlayerTestHelper.createPlayer("oath-before-burial");
		north.add(other);
		player = other;
		assertTrue(visit("Wszebor").contains("nie zwolnił"));
		assertTrue(say("Wszebor", "dowódca").contains("Czekamy"));
	}

	@Test
	public void killsRequireTheCorrectOriginalGuardianAndNearbyInvestigator() {
		ready(100);
		accept("Wagabundus", "polegli wartownicy");
		move(north);
		player.setPosition(72, 38);
		Creature copy = TrailCreatureVariants.create(
				Encounter.WATCH.name);
		copy.setPosition(73, 38);
		north.add(copy);
		OathTrailProgress.recordKill(player, copy);
		assertEquals("fight", player.getQuest(Step.OATH_WATCH.slot));
		OathTrailGuard g = guard(Encounter.WATCH);
		CreatureRespawnPoint source = g.getRespawnPoint();
		g.setRespawnPoint(null);
		OathTrailProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.OATH_WATCH.slot));
		g.setRespawnPoint(new CreatureRespawnPoint(north, 1, 1, g, 1));
		OathTrailProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.OATH_WATCH.slot));
		g.setRespawnPoint(source);
		player.setPosition(1, 1);
		OathTrailProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.OATH_WATCH.slot));
		player.setPosition(72, 38);
		g.onDamaged(player, g.getHP());
		g.onDead(player);
		assertEquals("witness", player.getQuest(Step.OATH_WATCH.slot));
	}

	@Test
	public void guardiansIgnoreTravellersAndStopChasingAfterTheQuestStage() {
		move(north);
		player.setPosition(72, 38);
		OathTrailGuard g = guard(Encounter.WATCH);
		assertTrue(g.getEnemyList().isEmpty());
		for (Encounter role : Encounter.values()) {
			assertTrue(role.name + " must not be a random daily target",
					TrailCreatureVariants.create(
							role.name).isAbnormal());
		}
		assertTrue(g.isAbnormal());
		assertTrue(g.getNewInstance().isAbnormal());
		ready(100);
		player.setQuest(Step.OATH_WATCH.slot, "fight");
		assertTrue(g.getEnemyList().contains(player));
		g.setTarget(player);
		player.setPosition(90, 38);
		g.logic();
		assertFalse(g.isAttacking());
		player.setPosition(72, 38);
		g.setTarget(player);
		player.setQuest(Step.OATH_WATCH.slot, "report");
		g.logic();
		assertFalse(g.isAttacking());
		Player archer = new Player(new RPObject(player)) {
			@Override
			public boolean canDoRangeAttack(RPEntity target, int range) {
				return true;
			}
		};
		north.add(archer);
		archer.setPosition(68, 38);
		g.rememberAttacker(archer);
		g.setIdle();
		g.setIdle();
		assertEquals(73, g.getX());
		assertEquals(38, g.getY());
		assertEquals(0.0, g.getSpeed(), 0.0);
		assertTrue(g.getNewInstance() instanceof OathTrailGuard);
	}

	@Test
	public void fullBagAndRebirthPreserveTheFinalReward() {
		ready(100);
		for (Step s : Step.values())
			if (s.chapter.equals(TrailQuestLine.OATH_CHAPTER))
				player.setQuest(s.slot, "done");
		player.setQuest(Step.OATH_BURIAL.slot, "ready");
		while (player.equip("bag",
				SingletonRepository.getEntityManager().getItem("miecz"))) {
		}
		int xp = player.getXP();
		visit("Wagabundus");
		say("Wagabundus", "wspólny pochówek");
		assertEquals("ready", player.getQuest(Step.OATH_BURIAL.slot));
		assertEquals(xp, player.getXP());
		RPObject first = player.getSlot("bag").iterator().next();
		player.getSlot("bag").remove(first.getID());
		player.setLevel(0);
		player.setXP(0);
		say("Wagabundus", "wspólny pochówek");
		assertTrue(player.isQuestCompleted(Step.OATH_BURIAL.slot));
		assertEquals(1, player.getTotalNumberOf("magiczna tarcza płytowa"));
	}

	@Test
	public void amileuszKeepsHisExistingQuestConsent() {
		SpeakerNPC scholar = new SpeakerNPC("Mieczysław");
		scholar.addGreeting();
		house.add(scholar);
		system.loadQuest(new MithrilRing());
		watch();
		visit("Amileusz");
		say("Amileusz", "task");
		say("Amileusz", "tak");
		assertTrue(player.isQuestInState("mithril_ring", 0, "schemes"));
		assertFalse(player.hasQuest(Step.OATH_NAMES.slot));
		say("Amileusz", "znak oddziału");
		say("Amileusz", "nie");
		assertFalse(player.hasQuest(Step.OATH_NAMES.slot));
		say("Amileusz", "znak oddziału");
		say("Amileusz", "tak");
		assertEquals("clues;0", player.getQuest(Step.OATH_NAMES.slot));
	}

	@Test
	public void allStorySitesFitExistingReachableMapTiles() throws Exception {
		for (String zone : new String[] { OathTrailProgress.NORTH,
				OathTrailProgress.WEST }) {
			String file = "data/maps/Level 0/krakow/"
					+ (zone.equals(OathTrailProgress.NORTH) ? "n_w2" : "w2")
					+ ".tmx";
			LayerDefinition collision = TMXLoader.load(file).getLayer(
					"collision");
			collision.build();
			LayerDefinition roof = TMXLoader.load(file).getLayer("3_roof");
			roof.build();
			Set<Integer> seen = new HashSet<>();
			Queue<Integer> queue = new ArrayDeque<>();
			for (int y = 0; y < 128; y++)
				if (collision.getTileAt(127, y) == 0) {
					seen.add(y * 128 + 127);
					queue.add(y * 128 + 127);
				}
			while (!queue.isEmpty()) {
				int p = queue.remove();
				for (int[] d : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 },
						{ 0, -1 } }) {
					int x = p % 128 + d[0], y = p / 128 + d[1], n = y * 128 + x;
					if (x >= 0 && x < 128 && y >= 0 && y < 128
							&& collision.getTileAt(x, y) == 0 && seen.add(n))
						queue.add(n);
				}
			}
			List<int[]> points = new ArrayList<>();
			for (Kind k : Kind.values())
				if (k.zone.equals(zone))
					points.add(new int[] { k.x, k.y });
			for (Encounter r : Encounter.values())
				if (r.zone.equals(zone))
					points.add(new int[] { r.x, r.y });
			if (zone.equals(OathTrailProgress.NORTH))
				points.add(new int[] { 70, 38 });
			for (int[] p : points) {
				assertTrue(file + " unreachable " + p[0] + "," + p[1],
						seen.contains(p[1] * 128 + p[0]));
				assertEquals(file + " roof " + p[0] + "," + p[1], 0,
						roof.getTileAt(p[0], p[1]));
				assertEquals(0, collision.getTileAt(p[0], p[1] + 1));
			}
		}
		assertEquals("2 poległych wartowników",
				Grammar.quantityplnounCreature(2, "poległy wartownik"));
		assertEquals("2 strażników dawnej chorągwi",
				Grammar.quantityplnounCreature(2, "strażnik dawnej chorągwi"));
	}
}
