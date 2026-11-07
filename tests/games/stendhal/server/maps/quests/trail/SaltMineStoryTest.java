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
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.wieliczka.scientisthouse.NaukowiecNPC;
import games.stendhal.server.maps.wieliczka.townhall.ZbigniewNPC;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import games.stendhal.server.maps.quests.trail.SaltMineProgress.Encounter;
import games.stendhal.server.maps.quests.trail.SaltMineSites.Kind;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import marauroa.common.game.RPObject;

public class SaltMineStoryTest {
	private static final String[] NAMES = { "Zbigniew", "Radost", "Agrypin",
			"górnik Bogdan", "górnik Wojmir" };
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
		for (String name : new String[] { SaltMineProgress.MINE,
				"int_wieliczka_townhall", "int_wieliczka_scientist_house" }) {
			StendhalRPZone z = new StendhalRPZone(name, 128, 128);
			zones.put(name, z);
			MockStendlRPWorld.get().addRPZone(z);
		}
		new ZbigniewNPC().configureZone(zones.get("int_wieliczka_townhall"),
				null);
		new NaukowiecNPC().configureZone(
				zones.get("int_wieliczka_scientist_house"), null);
		// Agrypin's greeting is normally installed from the production XML.
		NPCList.get().get("Agrypin").addGreeting("Witaj.");
		new SaltMineSites().configureZone(zones.get(SaltMineProgress.MINE),
				null);
		for (Step step : Step.values())
			if (step.chapter.equals(TrailQuestLine.SALT_CHAPTER))
				system.loadQuest(new SaltMineQuest(step));
		player = PlayerTestHelper.createPlayer("salt-story");
		zones.get(SaltMineProgress.MINE).add(player);
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
			if (step == Step.SALT_SHIFT)
				break;
			player.setQuest(step.slot, "done");
		}
		player.setXP(Level.getXP(level));
		player.setLevel(level);
	}

	private void before(Step step) {
		ready(250);
		for (Step s : Step.values()) {
			if (s == step)
				break;
			player.setQuest(s.slot, "done");
		}
	}

	private SaltMineSites.Evidence evidence(Kind kind) {
		for (Object e : zones.get(SaltMineProgress.MINE).getEntitiesOfClass(
				SaltMineSites.Evidence.class))
			if (((SaltMineSites.Evidence) e).kind == kind)
				return (SaltMineSites.Evidence) e;
		throw new AssertionError(kind);
	}

	private boolean use(Kind kind) {
		SaltMineSites.Evidence e = evidence(kind);
		move(e.getZone());
		player.setPosition(e.getX(), e.getY() + 1);
		return e.onUsed(player);
	}

	private SaltMineGuard guard(Encounter role) {
		StendhalRPZone z = zones.get(SaltMineProgress.MINE);
		for (CreatureRespawnPoint source : z.getRespawnPointList())
			if (source.getX() == role.x && source.getY() == role.y) {
				SaltMineGuard g = (SaltMineGuard) source.getPrototypeCreature().getNewInstance();
				g.setRespawnPoint(source);
				g.setPosition(role.x, role.y);
				z.add(g);
				return g;
			}
		throw new AssertionError(role);
	}

	private void kill(Encounter role) {
		move(zones.get(SaltMineProgress.MINE));
		player.setPosition(role.x - 1, role.y);
		SaltMineGuard g = guard(role);
		g.onDamaged(player, g.getHP() + g.getArmorHP());
		g.onDead(player);
	}

	private void shift() {
		accept("Zbigniew", "zaginionej zmianie");
		visit("Radost");
		say("Radost", "zaginionej zmianie");
		assertTrue(use(Kind.SUPPORT));
		assertTrue(use(Kind.ROPES));
		visit("Radost");
		say("Radost", "zaginiona zmiana");
	}

	@Test
	public void floodedMineFollowsGdanskAtLevel250() {
		int count = 0;
		String previous = Step.COAST_RETURN.slot;
		for (Step step : Step.values())
			if ("Zalane chodniki".equals(step.chapter)) {
				assertEquals(250, step.minimumLevel);
				assertEquals(previous, step.previousSlot());
				previous = step.slot;
				count++;
			}
		assertEquals(6, count);
		visit("Zbigniew");
		say("Zbigniew", "zaginiona zmiana");
		assertFalse(player.hasQuest(Step.SALT_SHIFT.slot));
		ready(249);
		visit("Zbigniew");
		say("Zbigniew", "zaginiona zmiana");
		assertTrue(system.getMainQuests(player).isEmpty());
		ready(250);
		accept("Zbigniew", "zaginiona zmiana");
		assertEquals("foreman", player.getQuest(Step.SALT_SHIFT.slot));
		assertEquals(1, system.getMainQuests(player).size());
	}

	@Test
	public void wholeRescueUsesClickableTopicsAndRewardsOnlyOnce() {
		ready(250);
		int xp = player.getXP();
		shift();
		accept("Radost", "odciętych górnikach");
		assertTrue(use(Kind.SUPPORT));
		kill(Encounter.SEEP);
		visit("górnik Bogdan");
		say("górnik Bogdan", "odciętych górnikach");
		visit("górnik Wojmir");
		say("górnik Wojmir", "odciętych górnikach");
		visit("Radost");
		say("Radost", "odcięci górnicy");
		accept("Radost", "próbkę solanki");
		assertTrue(use(Kind.SAMPLE));
		visit("Agrypin");
		say("Agrypin", "próbkę solanki");
		accept("Agrypin", "starym zbiorniku");
		assertTrue(use(Kind.CHANNEL));
		assertTrue(use(Kind.DRAINAGE));
		visit("Radost");
		say("Radost", "starym zbiorniku");
		accept("Radost", "żywiołaku solanki");
		assertFalse(use(Kind.SEAL));
		kill(Encounter.SOURCE);
		assertTrue(use(Kind.SEAL));
		visit("Radost");
		say("Radost", "żywiołaku solanki");
		accept("Radost", "bezpiecznym szybie");
		assertFalse(use(Kind.SUPPORT));
		assertTrue(use(Kind.RELEASE));
		assertTrue(use(Kind.SUPPORT));
		assertTrue(use(Kind.ROPES));
		visit("Zbigniew");
		say("Zbigniew", "bezpieczny szyb");
		assertTrue(player.isQuestCompleted(Step.SALT_RETURN.slot));
		assertEquals(450002, player.getXP() - xp);
		assertEquals(10000, player.getTotalNumberOf("money"));
		Item helmet = player.getFirstEquipped("hełm wampirzy");
		assertNotNull(helmet);
		assertEquals(ItemRarity.RARE, helmet.getRarity());
		assertTrue(ItemAffixState.hasAny(helmet));
		assertEquals(player.getName(), helmet.getBoundTo());
		xp = player.getXP();
		say("Zbigniew", "bezpieczny szyb");
		assertEquals(xp, player.getXP());
		assertEquals(1, player.getTotalNumberOf("hełm wampirzy"));
		assertTrue(system.getMainQuests(player).isEmpty());
	}

	@Test
	public void cluesAndMinersArePersonalRangeCheckedAndPersistent() {
		assertFalse(use(Kind.ROPES));
		ready(250);
		accept("Zbigniew", "zaginiona zmiana");
		assertFalse(use(Kind.SUPPORT));
		visit("Radost");
		say("Radost", "zaginiona zmiana");
		assertTrue(use(Kind.SUPPORT));
		assertFalse(use(Kind.SUPPORT));
		Player copy = new Player(player);
		player.getZone().remove(player);
		zones.get(SaltMineProgress.MINE).add(copy);
		player = copy;
		assertEquals("marks;1", player.getQuest(Step.SALT_SHIFT.slot));
		player.setPosition(1, 1);
		assertFalse(evidence(Kind.ROPES).onUsed(player));
		Player other = PlayerTestHelper.createPlayer("salt-other");
		zones.get(SaltMineProgress.MINE).add(other);
		other.setPosition(42, 82);
		assertFalse(evidence(Kind.ROPES).onUsed(other));
		assertTrue(use(Kind.ROPES));
		assertEquals("report", player.getQuest(Step.SALT_SHIFT.slot));
		before(Step.SALT_RESCUE);
		accept("Radost", "odcięci górnicy");
		visit("górnik Bogdan");
		say("górnik Bogdan", "odcięci górnicy");
		assertEquals("brace", player.getQuest(Step.SALT_RESCUE.slot));
		assertTrue(use(Kind.SUPPORT));
		kill(Encounter.SEEP);
		visit("górnik Bogdan");
		say("górnik Bogdan", "odcięci górnicy");
		say("górnik Bogdan", "odcięci górnicy");
		assertEquals("miners;1", player.getQuest(Step.SALT_RESCUE.slot));
		visit("Radost");
		say("Radost", "odcięci górnicy");
		assertFalse(player.isQuestCompleted(Step.SALT_RESCUE.slot));
	}

	@Test
	public void drainageCannotSkipTheCreatureAndRebirthPreservesChecks() {
		before(Step.SALT_CREATURE);
		accept("Radost", "żywiołak solanki");
		assertFalse(use(Kind.RELEASE));
		assertFalse(use(Kind.SEAL));
		assertFalse(use(Kind.DRAINAGE));
		kill(Encounter.SOURCE);
		assertTrue(use(Kind.SEAL));
		visit("Radost");
		say("Radost", "żywiołak solanki");
		accept("Radost", "bezpieczny szyb");
		assertFalse(use(Kind.ROPES));
		assertTrue(use(Kind.RELEASE));
		assertTrue(use(Kind.ROPES));
		assertFalse(use(Kind.RELEASE));
		Player copy = new Player(player);
		player.getZone().remove(player);
		zones.get(SaltMineProgress.MINE).add(copy);
		player = copy;
		player.setLevel(0);
		player.setXP(0);
		assertEquals("verify;2", player.getQuest(Step.SALT_RETURN.slot));
		assertTrue(use(Kind.SUPPORT));
		assertEquals("mayor", player.getQuest(Step.SALT_RETURN.slot));
		visit("Zbigniew");
		say("Zbigniew", "bezpieczny szyb");
		assertTrue(player.isQuestCompleted(Step.SALT_RETURN.slot));
	}

	@Test
	public void onlyOriginalQuestCreaturesGrantCreditAndTravellersAreIgnored() {
		player.setPosition(49, 82);
		SaltMineGuard g = guard(Encounter.SEEP);
		assertTrue(g.getEnemyList().isEmpty());
		for (Encounter role : Encounter.values())
			assertTrue(TrailCreatureVariants.create(
					role.name).isAbnormal());
		before(Step.SALT_RESCUE);
		player.setQuest(Step.SALT_RESCUE.slot, "fight");
		assertTrue(g.getEnemyList().contains(player));
		Creature ordinary = TrailCreatureVariants.create(
				Encounter.SEEP.name);
		g.getZone().add(ordinary);
		SaltMineProgress.recordKill(player, ordinary);
		assertEquals("fight", player.getQuest(Step.SALT_RESCUE.slot));
		CreatureRespawnPoint source = g.getRespawnPoint();
		g.setRespawnPoint(null);
		SaltMineProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.SALT_RESCUE.slot));
		g.setRespawnPoint(new CreatureRespawnPoint(g.getZone(), 1, 1, g, 1));
		SaltMineProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.SALT_RESCUE.slot));
		g.setRespawnPoint(source);
		player.setPosition(1, 1);
		SaltMineProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.SALT_RESCUE.slot));
		player.setPosition(49, 82);
		g.onDamaged(player, g.getHP() + g.getArmorHP());
		g.onDead(player);
		assertEquals("miners;0", player.getQuest(Step.SALT_RESCUE.slot));
		SaltMineGuard next = guard(Encounter.SEEP);
		next.setTarget(player);
		next.logic();
		assertFalse(next.isAttacking());
		assertTrue(next.getNewInstance().isAbnormal());
	}

	@Test
	public void finalHelmetAndCoinsWaitUntilBothCanBeCarried() {
		before(Step.SALT_RETURN);
		player.setQuest(Step.SALT_RETURN.slot, "mayor");
		StackableItem coins = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"money");
		coins.setQuantity(3000);
		while (player.getSlotToEquip(coins) != null) {
			String slot = player.getSlotToEquip(coins).getName();
			assertTrue(player.equip(slot,
					SingletonRepository.getEntityManager().getItem("miecz")));
		}
		int xp = player.getXP();
		visit("Zbigniew");
		say("Zbigniew", "bezpieczny szyb");
		assertEquals("mayor", player.getQuest(Step.SALT_RETURN.slot));
		assertEquals(xp, player.getXP());
		RPObject first = player.getSlot("bag").iterator().next();
		player.getSlot("bag").remove(first.getID());
		say("Zbigniew", "bezpieczny szyb");
		assertEquals("mayor", player.getQuest(Step.SALT_RETURN.slot));
		assertEquals(0, player.getTotalNumberOf("hełm wampirzy"));
		first = player.getSlot("bag").iterator().next();
		player.getSlot("bag").remove(first.getID());
		say("Zbigniew", "bezpieczny szyb");
		assertTrue(player.isQuestCompleted(Step.SALT_RETURN.slot));
		assertEquals(3000, player.getTotalNumberOf("money"));
		assertEquals(1, player.getTotalNumberOf("hełm wampirzy"));
		for (Object e : player.getZone().getEntitiesOfClass(Item.class))
			assertNotEquals("money", ((Item) e).getName());
	}

	@Test
	public void clickableReservoirTopicCanFinishTheInvestigation() {
		before(Step.SALT_SOURCE);
		player.setQuest(Step.SALT_SOURCE.slot, "report");
		visit("Radost");
		say("Radost", "starego zbiornika");
		assertTrue(player.isQuestCompleted(Step.SALT_SOURCE.slot));
		int xp = player.getXP();
		int coins = player.getTotalNumberOf("money");
		say("Radost", "starego zbiornika");
		assertEquals(xp, player.getXP());
		assertEquals(coins, player.getTotalNumberOf("money"));
	}

	@Test
	public void ordinaryServiceConsentDoesNotAcceptStoryAndDecliningIsSafe() {
		ready(250);
		visit("Zbigniew");
		SpeakerNPC npc = NPCList.get().get("Zbigniew");
		npc.getEngine().setCurrentState(ConversationStates.QUEST_OFFERED);
		npc.getEngine().step(player, "tak");
		assertFalse(player.hasQuest(Step.SALT_SHIFT.slot));
		visit("Zbigniew");
		say("Zbigniew", "zaginiona zmiana");
		say("Zbigniew", "nie");
		assertFalse(player.hasQuest(Step.SALT_SHIFT.slot));
		say("Zbigniew", "zaginiona zmiana");
		say("Zbigniew", "tak");
		assertEquals("foreman", player.getQuest(Step.SALT_SHIFT.slot));
		before(Step.SALT_SOURCE);
		visit("Agrypin");
		npc = NPCList.get().get("Agrypin");
		npc.getEngine().setCurrentState(ConversationStates.PRODUCTION_OFFERED);
		npc.getEngine().step(player, "tak");
		assertFalse(player.hasQuest(Step.SALT_SOURCE.slot));
		accept("Agrypin", "stary zbiornik");
		assertEquals("channels;0", player.getQuest(Step.SALT_SOURCE.slot));
	}

	@Test
	public void allSitesHaveReachableApproachesOnTheUnchangedMineMap()
			throws Exception {
		String file = "data/maps/Level -1/wieliczka/salt_mines_c.tmx";
		LayerDefinition c = TMXLoader.load(file).getLayer("collision");
		c.build();
		LayerDefinition roof = TMXLoader.load(file).getLayer("3_roof");
		roof.build();
		int w = c.getWidth(), h = c.getHeight();
		Set<Integer> seen = new HashSet<>();
		Queue<Integer> q = new ArrayDeque<>();
		seen.add(79 * w + 31);
		q.add(79 * w + 31);
		while (!q.isEmpty()) {
			int p = q.remove();
			for (int[] d : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 },
					{ 0, -1 } }) {
				int x = p % w + d[0], y = p / w + d[1], n = y * w + x;
				if (x >= 0 && x < w && y >= 0 && y < h && c.getTileAt(x, y) == 0
						&& seen.add(n))
					q.add(n);
			}
		}
		for (Kind k : Kind.values()) {
			assertTrue(k.name, seen.contains(k.y * w + k.x));
			assertEquals(k.name, 0, roof.getTileAt(k.x, k.y));
			assertTrue(k.name + " approach",
					seen.contains((k.y + 1) * w + k.x));
		}
		for (Encounter role : Encounter.values())
			assertTrue(role.name, seen.contains(role.y * w + role.x));
		for (String name : new String[] { "Radost", "górnik Bogdan",
				"górnik Wojmir" }) {
			SpeakerNPC npc = NPCList.get().get(name);
			assertTrue(name, seen.contains(npc.getY() * w + npc.getX()));
			assertTrue(name + " approach",
					seen.contains((npc.getY() + 1) * w + npc.getX()));
			assertEquals(name, 0, roof.getTileAt(npc.getX(), npc.getY()));
			assertFalse(npc.getX() == 31 && npc.getY() == 79);
		}
	}
}
