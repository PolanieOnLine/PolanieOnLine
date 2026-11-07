package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Test;
import games.stendhal.common.Level;
import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.dragon.GuardianNPC;
import games.stendhal.server.maps.dragon.YoungBoyNPC;
import games.stendhal.server.maps.dragon.wizardtower.WizardNPC;
import games.stendhal.server.maps.quests.MagicznaTorba;
import games.stendhal.server.maps.quests.SmoczyAmulet;
import games.stendhal.server.maps.quests.challenges.ChallengeDragons;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import games.stendhal.server.maps.quests.trail.DragonTrailProgress.Encounter;
import games.stendhal.server.maps.quests.trail.DragonTrailSites.Kind;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import marauroa.common.game.RPObject;

public class DragonTrailStoryTest {
	private static final String[] NAMES = { "Racirad", "Wizariusz", "Robercik",
			"Radowit", "Ostrobor" };
	private final Map<String, SpeakerNPC> oldNpcs = new HashMap<>();
	private final Map<String, StendhalRPZone> zones = new LinkedHashMap<>();
	private final StendhalQuestSystem system = StendhalQuestSystem.get();
	private List<IQuest> loaded, previous;
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
		for (String name : new String[] { DragonTrailProgress.SOUTH,
				DragonTrailProgress.NORTH, DragonTrailProgress.TOWER }) {
			StendhalRPZone z = new StendhalRPZone(name, 128, 128);
			zones.put(name, z);
			MockStendlRPWorld.get().addRPZone(z);
		}
		new GuardianNPC().configureZone(zones.get(DragonTrailProgress.SOUTH),
				null);
		new YoungBoyNPC().configureZone(zones.get(DragonTrailProgress.NORTH),
				null);
		new WizardNPC().configureZone(zones.get(DragonTrailProgress.TOWER),
				null);
		for (StendhalRPZone z : zones.values())
			new DragonTrailSites().configureZone(z, null);
		system.loadQuest(new MagicznaTorba());
		system.loadQuest(new SmoczyAmulet());
		system.loadQuest(new ChallengeDragons());
		for (Step step : Step.values())
			if (step.chapter.equals(TrailQuestLine.DRAGON_CHAPTER))
				system.loadQuest(new DragonTrailQuest(step));
		player = PlayerTestHelper.createPlayer("dragon-story");
		zones.get(DragonTrailProgress.NORTH).add(player);
		MockStendhalRPRuleProcessor.get().addPlayer(player);
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
		for (StendhalRPZone z : zones.values())
			MockStendlRPWorld.get().removeZone(z);
	}

	private void move(StendhalRPZone zone) {
		if (player.getZone() != zone) {
			player.getZone().remove(player);
			zone.add(player);
		}
	}

	private String say(String name, String topic) {
		SpeakerNPC npc = NPCList.get().get(name);
		assertTrue(name + ": " + topic, npc.getEngine().step(player, topic));
		return getReply(npc);
	}

	private void visit(String name) {
		SpeakerNPC npc = NPCList.get().get(name);
		move(npc.getZone());
		player.setPosition(npc.getX(), npc.getY() + 1);
		npc.getEngine().setCurrentState(ConversationStates.IDLE);
		say(name, "hi");
	}

	private void accept(String name, String topic) {
		visit(name);
		say(name, topic);
		say(name, "tak");
	}

	private void ready(int level) {
		player.setQuest("wegiel_na_opal", "done");
		for (Step step : Step.values()) {
			if (step == Step.EMBER_CAMP)
				break;
			player.setQuest(step.slot, "done");
		}
		player.setXP(Level.getXP(level));
		player.setLevel(level);
	}

	private void before(Step step) {
		ready(400);
		for (Step s : Step.values()) {
			if (s == step)
				break;
			player.setQuest(s.slot, "done");
		}
	}

	private DragonTrailSites.Evidence evidence(Kind kind) {
		for (Object e : zones.get(kind.zone).getEntitiesOfClass(
				DragonTrailSites.Evidence.class))
			if (((DragonTrailSites.Evidence) e).kind == kind)
				return (DragonTrailSites.Evidence) e;
		throw new AssertionError(kind);
	}

	private boolean use(Kind kind) {
		DragonTrailSites.Evidence e = evidence(kind);
		move(e.getZone());
		player.setPosition(e.getX(), e.getY() + 1);
		return e.onUsed(player);
	}

	private DragonTrailGuard guard(Encounter role) {
		StendhalRPZone z = zones.get(DragonTrailProgress.NORTH);
		for (CreatureRespawnPoint source : z.getRespawnPointList())
			if (source.getX() == role.x && source.getY() == role.y) {
				DragonTrailGuard g = (DragonTrailGuard) source.getPrototypeCreature().getNewInstance();
				g.setRespawnPoint(source);
				g.setPosition(role.x, role.y);
				z.add(g);
				return g;
			}
		throw new AssertionError(role);
	}

	private void kill(Encounter role) {
		move(zones.get(DragonTrailProgress.NORTH));
		player.setPosition(role.x - 1, role.y);
		DragonTrailGuard g = guard(role);
		g.onDamaged(player, g.getHP() + g.getArmorHP());
		g.onDead(player);
	}

	private void camp() {
		accept("Racirad", "spalonym obozie");
		assertTrue(use(Kind.CRATE));
		assertTrue(use(Kind.ASH));
		visit("Radowit");
		say("Radowit", "spalonym obozie");
		visit("Racirad");
		say("Racirad", "spalony obóz");
	}

	@Test
	public void stolenEggsFollowWieliczkaAtLevel400() {
		int count = 0;
		String previous = Step.SALT_RETURN.slot;
		for (Step step : Step.values())
			if ("Popiół pod łuskami".equals(step.chapter)) {
				assertEquals(400, step.minimumLevel);
				assertEquals(previous, step.previousSlot());
				previous = step.slot;
				count++;
			}
		assertEquals(6, count);
		visit("Racirad");
		say("Racirad", "spalony obóz");
		assertFalse(player.hasQuest(Step.EMBER_CAMP.slot));
		ready(399);
		visit("Racirad");
		say("Racirad", "spalony obóz");
		assertTrue(system.getMainQuests(player).isEmpty());
		ready(400);
		accept("Racirad", "spalony obóz");
		assertEquals("traces;0", player.getQuest(Step.EMBER_CAMP.slot));
		assertEquals(1, system.getMainQuests(player).size());
	}

	@Test
	public void wholeStoryUsesActualNpcsAndRewardsOnlyOnce() {
		ready(400);
		int xp = player.getXP();
		camp();
		accept("Racirad", "porzuconym transporcie");
		assertTrue(use(Kind.CARGO));
		visit("Robercik");
		say("Robercik", "porzuconym transporcie");
		accept("Wizariusz", "zapachu jaj");
		assertTrue(use(Kind.MIXTURE));
		say("Wizariusz", "zapachu jaj");
		accept("Racirad", "pojmanym przewodniku");
		kill(Encounter.CAPTOR);
		visit("Ostrobor");
		say("Ostrobor", "pojmanego przewodnika");
		accept("Ostrobor", "skradzionym lęgu");
		assertFalse(use(Kind.EGGS));
		kill(Encounter.CHIEF);
		assertTrue(use(Kind.EGGS));
		visit("Ostrobor");
		say("Ostrobor", "skradziony lęg");
		accept("Ostrobor", "powrocie do gniazda");
		assertFalse(use(Kind.NEST));
		assertFalse(use(Kind.TRAPS));
		assertTrue(use(Kind.WASH));
		assertTrue(use(Kind.TRAPS));
		assertTrue(use(Kind.NEST));
		visit("Racirad");
		say("Racirad", "powrocie do gniazda");
		assertTrue(player.isQuestCompleted(Step.EMBER_RETURN.slot));
		assertEquals(1200002, player.getXP() - xp);
		assertEquals(25000, player.getTotalNumberOf("money"));
		Item chest = player.getFirstEquipped("złota skrzynia");
		assertNotNull(chest);
		assertEquals(player.getName(), chest.getBoundTo());
		assertEquals(Step.EMBER_RETURN.slot, chest.get("itemdata"));
		xp = player.getXP();
		say("Racirad", "powrót do gniazda");
		assertEquals(xp, player.getXP());
		assertEquals(1, player.getTotalNumberOf("złota skrzynia"));
		assertTrue(system.getMainQuests(player).isEmpty());
	}

	@Test
	public void campEvidenceIsPersonalAndSurvivesRelog() {
		assertFalse(use(Kind.CRATE));
		ready(400);
		accept("Racirad", "spalony obóz");
		assertTrue(use(Kind.CRATE));
		assertFalse(use(Kind.CRATE));
		Player copy = new Player(player);
		player.getZone().remove(player);
		zones.get(DragonTrailProgress.SOUTH).add(copy);
		player = copy;
		assertEquals("traces;1", player.getQuest(Step.EMBER_CAMP.slot));
		player.setPosition(1, 1);
		assertFalse(evidence(Kind.ASH).onUsed(player));
		Player other = PlayerTestHelper.createPlayer("dragon-other");
		zones.get(DragonTrailProgress.SOUTH).add(other);
		other.setPosition(34, 120);
		assertFalse(evidence(Kind.ASH).onUsed(other));
		visit("Radowit");
		say("Radowit", "spalony obóz");
		assertEquals("traces;1", player.getQuest(Step.EMBER_CAMP.slot));
		assertTrue(use(Kind.ASH));
		assertEquals("witness", player.getQuest(Step.EMBER_CAMP.slot));
		visit("Radowit");
		say("Radowit", "spalony obóz");
		assertEquals("report", player.getQuest(Step.EMBER_CAMP.slot));
	}

	@Test
	public void guideAndReturnCannotSkipFightsOrCleaningAndContinueAfterRebirth() {
		before(Step.EMBER_GUIDE);
		accept("Racirad", "pojmany przewodnik");
		visit("Ostrobor");
		say("Ostrobor", "pojmany przewodnik");
		assertEquals("fight", player.getQuest(Step.EMBER_GUIDE.slot));
		assertFalse(use(Kind.EGGS));
		assertFalse(use(Kind.NEST));
		kill(Encounter.CAPTOR);
		visit("Ostrobor");
		say("Ostrobor", "pojmany przewodnik");
		assertTrue(player.isQuestCompleted(Step.EMBER_GUIDE.slot));
		before(Step.EMBER_RETURN);
		accept("Ostrobor", "powrót do gniazda");
		assertFalse(use(Kind.NEST));
		assertTrue(use(Kind.WASH));
		assertFalse(use(Kind.WASH));
		Player copy = new Player(player);
		player.getZone().remove(player);
		zones.get(DragonTrailProgress.NORTH).add(copy);
		player = copy;
		player.setLevel(0);
		player.setXP(0);
		assertEquals("traps", player.getQuest(Step.EMBER_RETURN.slot));
		assertTrue(use(Kind.TRAPS));
		assertTrue(use(Kind.NEST));
		visit("Racirad");
		say("Racirad", "powrót do gniazda");
		assertTrue(player.isQuestCompleted(Step.EMBER_RETURN.slot));
	}

	@Test
	public void onlyOriginalOpponentsGrantCreditAndIgnoreOrdinaryTravellers() {
		player.setPosition(98, 46);
		DragonTrailGuard g = guard(Encounter.CAPTOR);
		assertTrue(g.getEnemyList().isEmpty());
		for (Encounter role : Encounter.values())
			assertTrue(TrailCreatureVariants.create(
					role.name).isAbnormal());
		before(Step.EMBER_GUIDE);
		player.setQuest(Step.EMBER_GUIDE.slot, "fight");
		assertTrue(g.getEnemyList().contains(player));
		Creature ordinary = TrailCreatureVariants.create(
				Encounter.CAPTOR.name);
		g.getZone().add(ordinary);
		DragonTrailProgress.recordKill(player, ordinary);
		assertEquals("fight", player.getQuest(Step.EMBER_GUIDE.slot));
		CreatureRespawnPoint source = g.getRespawnPoint();
		g.setRespawnPoint(null);
		DragonTrailProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.EMBER_GUIDE.slot));
		g.setRespawnPoint(new CreatureRespawnPoint(g.getZone(), 1, 1, g, 1));
		DragonTrailProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.EMBER_GUIDE.slot));
		g.setRespawnPoint(source);
		player.setPosition(1, 1);
		DragonTrailProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.EMBER_GUIDE.slot));
		player.setPosition(98, 46);
		g.onDamaged(player, g.getHP() + g.getArmorHP());
		g.onDead(player);
		assertEquals("guide", player.getQuest(Step.EMBER_GUIDE.slot));
		DragonTrailGuard next = guard(Encounter.CAPTOR);
		next.setTarget(player);
		next.logic();
		assertFalse(next.isAttacking());
		assertTrue(next.getNewInstance().isAbnormal());
	}

	@Test
	public void chestAndCoinsWaitForCapacityWithoutAffectingExistingChests() {
		before(Step.EMBER_RETURN);
		player.setQuest(Step.EMBER_RETURN.slot, "report");
		assertTrue(player.equip("bag",
				SingletonRepository.getEntityManager().getItem(
						"złota skrzynia")));
		StackableItem coins = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"money");
		coins.setQuantity(7000);
		while (player.getSlotToEquip(coins) != null) {
			String slot = player.getSlotToEquip(coins).getName();
			assertTrue(player.equip(slot,
					SingletonRepository.getEntityManager().getItem("miecz")));
		}
		int xp = player.getXP();
		visit("Racirad");
		say("Racirad", "powrót do gniazda");
		assertEquals("report", player.getQuest(Step.EMBER_RETURN.slot));
		assertEquals(xp, player.getXP());
		RPObject first = null;
		for (RPObject item : player.getSlot("bag"))
			if (((Item) item).getName().equals("miecz")) {
				first = item;
				break;
			}
		assertNotNull(first);
		player.getSlot("bag").remove(first.getID());
		say("Racirad", "powrót do gniazda");
		assertEquals("report", player.getQuest(Step.EMBER_RETURN.slot));
		assertEquals(1, player.getTotalNumberOf("złota skrzynia"));
		for (RPObject item : player.getSlot("bag"))
			if (((Item) item).getName().equals("miecz")) {
				player.getSlot("bag").remove(item.getID());
				break;
			}
		say("Racirad", "powrót do gniazda");
		assertTrue(player.isQuestCompleted(Step.EMBER_RETURN.slot));
		assertEquals(2, player.getTotalNumberOf("złota skrzynia"));
		assertEquals(7000, player.getTotalNumberOf("money"));
		for (Object e : player.getZone().getEntitiesOfClass(Item.class))
			assertNotEquals("money", ((Item) e).getName());
	}

	@Test
	public void questCorpsesNeverContainGeneratedOrCarriedEventLoot() {
		String previousEvent = System.getProperty("stendhal.minetown");
		try {
			System.setProperty("stendhal.minetown", "true");
			for (Encounter role : Encounter.values()) {
				DragonTrailGuard g = guard(role);
				player.setPosition(role.x - 1, role.y);
				// Carried event loot makes the regression deterministic even if
				// the seasonal roll misses.
				g.addDropItem(SingletonRepository.getEntityManager().getItem(
						"straszna dynia"));
				g.addDropItem("money", 100.0, 1);
				g.onDamaged(player, g.getHP() + g.getArmorHP());
				g.onDead(player);
			}
			int corpses = 0;
			for (Object entity : player.getZone().getEntitiesOfClass(
					Corpse.class)) {
				corpses++;
				assertEquals(0, ((Corpse) entity).size());
			}
			assertEquals(2, corpses);
		} finally {
			if (previousEvent == null)
				System.clearProperty("stendhal.minetown");
			else
				System.setProperty("stendhal.minetown", previousEvent);
		}
	}

	@Test
	public void wizardKeepsBothMainAndOldQuestAvailableAndConsentSeparate() {
		player.setQuest("magic_bag", "done");
		visit("Wizariusz");
		assertEquals(ConversationStates.IDLE,
				NPCList.get().get("Wizariusz").getEngine().getCurrentState());
		before(Step.EMBER_SCENT);
		visit("Wizariusz");
		assertEquals(ConversationStates.ATTENDING,
				NPCList.get().get("Wizariusz").getEngine().getCurrentState());
		say("Wizariusz", "zapach jaj");
		say("Wizariusz", "nie");
		assertFalse(player.hasQuest(Step.EMBER_SCENT.slot));
		for (String state : new String[] { "start", "done" }) {
			player.setQuest("magic_bag", state);
			visit("Wizariusz");
			assertEquals(ConversationStates.ATTENDING, NPCList.get().get(
					"Wizariusz").getEngine().getCurrentState());
		}
		player.equip("bag",
				SingletonRepository.getEntityManager().getItem("lodowy zwój"));
		player.setQuest("magic_bag", "start");
		visit("Wizariusz");
		assertEquals(ConversationStates.ATTENDING,
				NPCList.get().get("Wizariusz").getEngine().getCurrentState());
		say("Wizariusz", "zapach jaj");
		say("Wizariusz", "tak");
		assertEquals("mixture", player.getQuest(Step.EMBER_SCENT.slot));
		assertEquals("start", player.getQuest("magic_bag"));
		player.setLevel(0);
		player.setXP(0);
		visit("Wizariusz");
		assertEquals(ConversationStates.ATTENDING,
				NPCList.get().get("Wizariusz").getEngine().getCurrentState());
		assertTrue(use(Kind.MIXTURE));
		say("Wizariusz", "zapach jaj");
		assertTrue(player.isQuestCompleted(Step.EMBER_SCENT.slot));
		visit("Wizariusz");
		assertEquals(ConversationStates.QUEST_ITEM_BROUGHT,
				NPCList.get().get("Wizariusz").getEngine().getCurrentState());
		say("Wizariusz", "tak");
		assertTrue(player.isQuestCompleted("magic_bag"));
	}

	@Test
	public void everyStoryObjectAndActorHasAnApproachOnNativeMaps()
			throws Exception {
		for (String zone : new String[] { DragonTrailProgress.SOUTH,
				DragonTrailProgress.NORTH, DragonTrailProgress.TOWER }) {
			String file = zone.equals(DragonTrailProgress.TOWER)
					? "data/maps/interiors/dragon/wizard_tower_4.tmx"
					: "data/maps/Level 0/dragon/"
							+ (zone.equals(DragonTrailProgress.SOUTH) ? "land_s"
									: "land_n")
							+ ".tmx";
			LayerDefinition c = TMXLoader.load(file).getLayer("collision");
			c.build();
			LayerDefinition roof = TMXLoader.load(file).getLayer("3_roof");
			roof.build();
			int w = c.getWidth(), h = c.getHeight();
			int sx = 24, sy = zone.equals(DragonTrailProgress.TOWER) ? 23
					: zone.equals(DragonTrailProgress.SOUTH) ? 123 : 127;
			Set<Integer> seen = new HashSet<>();
			Queue<Integer> q = new ArrayDeque<>();
			seen.add(sy * w + sx);
			q.add(sy * w + sx);
			while (!q.isEmpty()) {
				int n = q.remove();
				for (int[] d : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 },
						{ 0, -1 } }) {
					int x = n % w + d[0], y = n / w + d[1], v = y * w + x;
					if (x >= 0 && x < w && y >= 0 && y < h
							&& c.getTileAt(x, y) == 0 && seen.add(v))
						q.add(v);
				}
			}
			for (Kind k : Kind.values())
				if (k.zone.equals(zone)) {
					assertTrue(k.name, seen.contains(k.y * w + k.x));
					assertTrue(k.name + " approach",
							seen.contains((k.y + 1) * w + k.x));
					assertEquals(k.name, 0, roof.getTileAt(k.x, k.y));
				}
			if (zone.equals(DragonTrailProgress.NORTH))
				for (Encounter role : Encounter.values())
					assertTrue(role.name, seen.contains(role.y * w + role.x));
			for (String name : NAMES) {
				SpeakerNPC npc = NPCList.get().get(name);
				if (npc.getZone() != zones.get(zone))
					continue;
				boolean approach = false;
				for (int[] d : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 },
						{ 0, -1 } })
					approach |= seen.contains(
							(npc.getY() + d[1]) * w + npc.getX() + d[0]);
				assertTrue(name + " approach", approach);
			}
		}
	}
}
