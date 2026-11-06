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
import games.stendhal.common.QuestRewardPreview;
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
import games.stendhal.server.maps.quests.RosaBasementRats;
import games.stendhal.server.events.ProgressStatusEvent;
import games.stendhal.server.maps.gdansk.ship.FerryConveyerNPC;
import games.stendhal.server.maps.gdansk.knighthouse.MalteisNPC;
import games.stendhal.server.maps.gdansk.tavern.RosaNPC;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import games.stendhal.server.maps.quests.trail.CoastTrailProgress.Encounter;
import games.stendhal.server.maps.quests.trail.CoastTrailSites.Kind;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import marauroa.common.game.RPObject;

public class CoastTrailStoryTest {
	private static final String[] NAMES = { "Staszek", "Lubor", "Malteis",
			"Rosa" };
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
		for (String name : new String[] { CoastTrailProgress.WEST,
				CoastTrailProgress.EAST, CoastTrailProgress.CELLAR,
				"int_gdansk_knight_room", "int_gdansk_tavern_0" }) {
			StendhalRPZone z = new StendhalRPZone(name, 128, 128);
			zones.put(name, z);
			MockStendlRPWorld.get().addRPZone(z);
		}
		new FerryConveyerNPC().configureZone(zones.get(CoastTrailProgress.WEST),
				null);
		new MalteisNPC().configureZone(zones.get("int_gdansk_knight_room"),
				null);
		new RosaNPC().configureZone(zones.get("int_gdansk_tavern_0"), null);
		for (String zone : new String[] { CoastTrailProgress.WEST,
				CoastTrailProgress.EAST, CoastTrailProgress.CELLAR })
			new CoastTrailSites().configureZone(zones.get(zone), null);
		for (Step step : Step.values())
			if (step.chapter.equals(TrailQuestLine.COAST_CHAPTER))
				system.loadQuest(new CoastTrailQuest(step));
		player = PlayerTestHelper.createPlayer("coast-story");
		zones.get(CoastTrailProgress.WEST).add(player);
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
			if (step == Step.COAST_WRECK)
				break;
			player.setQuest(step.slot, "done");
		}
		player.setXP(Level.getXP(level));
		player.setLevel(level);
	}

	private CoastTrailSites.Evidence evidence(Kind kind) {
		for (Object e : zones.get(kind.zone).getEntitiesOfClass(
				CoastTrailSites.Evidence.class))
			if (((CoastTrailSites.Evidence) e).kind == kind)
				return (CoastTrailSites.Evidence) e;
		throw new AssertionError(kind);
	}

	private boolean use(Kind kind) {
		CoastTrailSites.Evidence e = evidence(kind);
		move(e.getZone());
		player.setPosition(e.getX(), e.getY() + 1);
		return e.onUsed(player);
	}

	private CoastTrailGuard guard(Encounter role) {
		StendhalRPZone z = zones.get(role.zone);
		for (CreatureRespawnPoint source : z.getRespawnPointList())
			if (source.getX() == role.x && source.getY() == role.y) {
				CoastTrailGuard guard = (CoastTrailGuard) source.getPrototypeCreature().getNewInstance();
				guard.setRespawnPoint(source);
				guard.setPosition(role.x, role.y);
				z.add(guard);
				return guard;
			}
		throw new AssertionError(role);
	}

	private void kill(Encounter role) {
		move(zones.get(role.zone));
		player.setPosition(role.x - 1, role.y);
		CoastTrailGuard g = guard(role);
		g.onDamaged(player, g.getHP() + g.getArmorHP());
		g.onDead(player);
	}

	private void wreck() {
		accept("Staszek", "zaginioną Mewę");
		assertTrue(use(Kind.CRATE));
		assertTrue(use(Kind.ROPE));
		visit("Lubor");
		say("Lubor", "zaginioną Mewę");
		visit("Staszek");
		say("Staszek", "zaginiona Mewa");
	}

	@Test
	public void coastChapterFollowsWarsawAtLevel200() {
		int count = 0;
		String previous = Step.WARSAW_TRUCE.slot;
		for (Step step : Step.values())
			if ("Światła na mieliźnie".equals(step.chapter)) {
				assertEquals(200, step.minimumLevel);
				assertEquals(previous, step.previousSlot());
				previous = step.slot;
				count++;
			}
		assertEquals(6, count);
		visit("Staszek");
		say("Staszek", "zaginiona Mewa");
		assertFalse(player.hasQuest(Step.COAST_WRECK.slot));
		ready(199);
		visit("Staszek");
		say("Staszek", "zaginiona Mewa");
		assertTrue(system.getMainQuests(player).isEmpty());
		ready(200);
		accept("Staszek", "zaginiona Mewa");
		assertEquals("traces;0", player.getQuest(Step.COAST_WRECK.slot));
		assertEquals(1, system.getMainQuests(player).size());
	}

	@Test
	public void wholeStoryUsesClickableTopicsAndRewardsOnlyOnce() {
		ready(200);
		int xp = player.getXP();
		wreck();
		accept("Lubor", "fałszywym świetle");
		assertTrue(use(Kind.FALSE_LIGHT));
		kill(Encounter.BEACH);
		visit("Lubor");
		say("Lubor", "fałszywe światło");
		accept("Malteis", "ładunku z Mewy");
		assertTrue(use(Kind.CARGO));
		visit("Rosa");
		say("Rosa", "ładunek z Mewy");
		visit("Malteis");
		say("Malteis", "ładunek z Mewy");
		accept("Malteis", "rabusiów wybrzeża");
		assertFalse(use(Kind.PAPERS));
		kill(Encounter.CHIEF);
		assertTrue(use(Kind.PAPERS));
		visit("Malteis");
		say("Malteis", "rabusie wybrzeża");
		accept("Lubor", "bezpieczny brzeg");
		assertTrue(use(Kind.RIGGING));
		assertTrue(use(Kind.WEST_LAMP));
		assertTrue(use(Kind.EAST_LAMP));
		visit("Lubor");
		say("Lubor", "bezpieczny brzeg");
		accept("Staszek", "ratunku dla Jaskółki");
		assertFalse(use(Kind.SIGNAL));
		kill(Encounter.SABOTEUR);
		assertTrue(use(Kind.SIGNAL));
		visit("Staszek");
		say("Staszek", "ratunek dla Jaskółki");
		assertFalse(player.isQuestCompleted(Step.COAST_RETURN.slot));
		visit("Lubor");
		say("Lubor", "bezpiecznym kursie");
		visit("Staszek");
		say("Staszek", "ratunek dla Jaskółki");
		assertTrue(player.isQuestCompleted(Step.COAST_RETURN.slot));
		assertEquals(300003, player.getXP() - xp);
		assertEquals(8000, player.getTotalNumberOf("money"));
		Item cloak = player.getFirstEquipped("płaszcz chaosu");
		assertNotNull(cloak);
		assertEquals(ItemRarity.RARE, cloak.getRarity());
		assertTrue(ItemAffixState.hasAny(cloak));
		assertEquals(player.getName(), cloak.getBoundTo());
		assertEquals(0, player.getTotalNumberOf("czarny płaszcz"));
		assertEquals(0, player.getTotalNumberOf("pas z mithrilu"));
		assertTrue(cloak.getDescription().contains("mewę"));
		xp = player.getXP();
		say("Staszek", "ratunek dla Jaskółki");
		assertEquals(xp, player.getXP());
		assertEquals(1, player.getTotalNumberOf("płaszcz chaosu"));
		assertTrue(system.getMainQuests(player).isEmpty());
	}

	@Test
	public void wreckCluesArePersonalRangeCheckedAndPersistent() {
		assertFalse(use(Kind.ROPE));
		ready(200);
		accept("Staszek", "zaginiona Mewa");
		assertTrue(use(Kind.CRATE));
		assertFalse(use(Kind.CRATE));
		Player copy = new Player(player);
		player.getZone().remove(player);
		zones.get(CoastTrailProgress.WEST).add(copy);
		player = copy;
		assertEquals("traces;1", player.getQuest(Step.COAST_WRECK.slot));
		player.setPosition(1, 1);
		assertFalse(evidence(Kind.ROPE).onUsed(player));
		Player other = PlayerTestHelper.createPlayer("coast-other");
		zones.get(CoastTrailProgress.WEST).add(other);
		other.setPosition(105, 59);
		assertFalse(evidence(Kind.ROPE).onUsed(other));
		assertTrue(use(Kind.ROPE));
		assertEquals("survivor", player.getQuest(Step.COAST_WRECK.slot));
		assertFalse(TrailQuestLine.objective(player,
				Step.COAST_WRECK.slot).contains("strzępy"));
	}

	@Test
	public void wrongLampDoesNotResetCheckpointsAndRebirthDoesNotLoseAcceptedWork() {
		ready(200);
		for (Step step : Step.values())
			if (step.chapter.equals(TrailQuestLine.COAST_CHAPTER))
				player.setQuest(step.slot, "done");
		player.setQuest(Step.COAST_LAMPS.slot, "rigging");
		assertFalse(use(Kind.EAST_LAMP));
		assertEquals("rigging", player.getQuest(Step.COAST_LAMPS.slot));
		assertTrue(use(Kind.RIGGING));
		assertFalse(use(Kind.EAST_LAMP));
		assertEquals("lamps;0", player.getQuest(Step.COAST_LAMPS.slot));
		assertTrue(use(Kind.WEST_LAMP));
		assertFalse(use(Kind.WEST_LAMP));
		Player copy = new Player(player);
		player.getZone().remove(player);
		zones.get(CoastTrailProgress.EAST).add(copy);
		player = copy;
		player.setLevel(0);
		player.setXP(0);
		assertEquals("lamps;1", player.getQuest(Step.COAST_LAMPS.slot));
		assertTrue(use(Kind.EAST_LAMP));
		assertFalse(use(Kind.RIGGING));
		visit("Lubor");
		say("Lubor", "bezpieczny brzeg");
		assertTrue(player.isQuestCompleted(Step.COAST_LAMPS.slot));
	}

	@Test
	public void onlyOriginalQuestOpponentsGrantCreditAndTravellersAreIgnored() {
		move(zones.get(CoastTrailProgress.EAST));
		player.setPosition(107, 64);
		CoastTrailGuard g = guard(Encounter.BEACH);
		assertTrue(g.getEnemyList().isEmpty());
		for (Encounter role : Encounter.values())
			assertTrue(SingletonRepository.getEntityManager().getCreature(
					role.name).isAbnormal());
		ready(200);
		player.setQuest(Step.COAST_WRECK.slot, "done");
		player.setQuest(Step.COAST_LIGHTS.slot, "fight");
		assertTrue(g.getEnemyList().contains(player));
		Creature ordinary = SingletonRepository.getEntityManager().getCreature(
				Encounter.BEACH.name);
		g.getZone().add(ordinary);
		CoastTrailProgress.recordKill(player, ordinary);
		assertEquals("fight", player.getQuest(Step.COAST_LIGHTS.slot));
		CreatureRespawnPoint source = g.getRespawnPoint();
		g.setRespawnPoint(null);
		CoastTrailProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.COAST_LIGHTS.slot));
		g.setRespawnPoint(new CreatureRespawnPoint(g.getZone(), 1, 1, g, 1));
		CoastTrailProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.COAST_LIGHTS.slot));
		g.setRespawnPoint(source);
		player.setPosition(1, 1);
		CoastTrailProgress.recordKill(player, g);
		assertEquals("fight", player.getQuest(Step.COAST_LIGHTS.slot));
		player.setPosition(107, 64);
		g.onDamaged(player, g.getHP() + g.getArmorHP());
		g.onDead(player);
		assertEquals("report", player.getQuest(Step.COAST_LIGHTS.slot));
		CoastTrailGuard next = guard(Encounter.BEACH);
		next.setTarget(player);
		next.logic();
		assertFalse(next.isAttacking());
		assertTrue(next.getNewInstance().isAbnormal());
	}

	@Test
	public void finalCloakAndCoinsWaitUntilBothCanBeCarried() {
		ready(200);
		for (Step step : Step.values())
			if (step.chapter.equals(TrailQuestLine.COAST_CHAPTER))
				player.setQuest(step.slot, "done");
		player.setQuest(Step.COAST_RETURN.slot, "ready");
		StackableItem coins = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"money");
		coins.setQuantity(2500);
		while (player.getSlotToEquip(coins) != null) {
			String slot = player.getSlotToEquip(coins).getName();
			assertTrue(player.equip(slot,
					SingletonRepository.getEntityManager().getItem("miecz")));
		}
		int xp = player.getXP();
		visit("Staszek");
		say("Staszek", "ratunek dla Jaskółki");
		assertEquals("ready", player.getQuest(Step.COAST_RETURN.slot));
		assertEquals(xp, player.getXP());
		RPObject first = player.getSlot("bag").iterator().next();
		player.getSlot("bag").remove(first.getID());
		say("Staszek", "ratunek dla Jaskółki");
		assertEquals("ready", player.getQuest(Step.COAST_RETURN.slot));
		assertEquals(0, player.getTotalNumberOf("płaszcz chaosu"));
		first = player.getSlot("bag").iterator().next();
		player.getSlot("bag").remove(first.getID());
		player.setLevel(0);
		player.setXP(0);
		say("Staszek", "ratunek dla Jaskółki");
		assertTrue(player.isQuestCompleted(Step.COAST_RETURN.slot));
		assertEquals(2500, player.getTotalNumberOf("money"));
		assertEquals(1, player.getTotalNumberOf("płaszcz chaosu"));
		for (Object e : player.getZone().getEntitiesOfClass(Item.class))
			assertNotEquals("money", ((Item) e).getName());
	}

	@Test
	public void journalPreviewShowsOnlyThisStepsGuaranteedRewards() {
		QuestRewardPreview early = system.getQuest(
				Step.COAST_WRECK.title).getQuestInfo(player).getRewards();
		assertEquals(Step.COAST_WRECK.xp, early.xp);
		assertEquals(Step.COAST_WRECK.coins, early.coins);
		assertTrue(early.items.isEmpty());
		QuestRewardPreview finale = system.getQuest(
				Step.COAST_RETURN.title).getQuestInfo(player).getRewards();
		assertEquals(1, finale.items.size());
		assertEquals("płaszcz chaosu", finale.items.get(0).name);
		assertEquals("rare", finale.items.get(0).rarity);
		assertTrue(finale.items.get(0).bound);
		ProgressStatusEvent event = new ProgressStatusEvent("Zadania główne",
				Step.COAST_RETURN.title, "opis", Collections.emptyList());
		event.setRewards(finale, true);
		QuestRewardPreview received = QuestRewardPreview.read(event);
		assertTrue(received.completed);
		assertEquals(finale.items.get(0).subclass,
				received.items.get(0).subclass);
		assertEquals(0, player.getTotalNumberOf("płaszcz chaosu"));
		assertFalse(player.hasQuest(Step.COAST_RETURN.slot));
		assertNull(QuestRewardPreview.read(
				new ProgressStatusEvent("Produkcja", Collections.emptyList())));
	}

	@Test
	public void unfinishedBasementQuestDoesNotBlockRosasStoryDialogue() {
		system.loadQuest(new RosaBasementRats());
		ready(200);
		player.setQuest(Step.COAST_WRECK.slot, "done");
		player.setQuest(Step.COAST_LIGHTS.slot, "done");
		player.setQuest(Step.COAST_CARGO.slot, "rosa");
		visit("Rosa");
		say("Rosa", "task");
		say("Rosa", "tak");
		String basementQuest = player.getQuest("pomoc_w_tawernie_rosa");
		visit("Rosa");
		say("Rosa", "ładunek z Mewy");
		assertEquals("report", player.getQuest(Step.COAST_CARGO.slot));
		assertEquals(basementQuest, player.getQuest("pomoc_w_tawernie_rosa"));
		say("Rosa", "piwnica");
		assertEquals(basementQuest, player.getQuest("pomoc_w_tawernie_rosa"));
		assertEquals(ConversationStates.ATTENDING,
				NPCList.get().get("Rosa").getEngine().getCurrentState());
		player.setSoloKillCount("szczur", 8);
		player.setSoloKillCount("szczur jaskiniowy", 5);
		player.setSoloKillCount("wąż", 1);
		visit("Rosa");
		assertTrue(player.isQuestCompleted("pomoc_w_tawernie_rosa"));
		assertEquals("report", player.getQuest(Step.COAST_CARGO.slot));
	}

	@Test
	public void ferryConsentCannotAcceptTheMainQuest() {
		ready(200);
		visit("Staszek");
		SpeakerNPC npc = NPCList.get().get("Staszek");
		npc.getEngine().setCurrentState(ConversationStates.SERVICE_OFFERED);
		say("Staszek", "nie");
		assertFalse(player.hasQuest(Step.COAST_WRECK.slot));
		say("Staszek", "zaginiona Mewa");
		say("Staszek", "nie");
		assertFalse(player.hasQuest(Step.COAST_WRECK.slot));
		say("Staszek", "zaginiona Mewa");
		say("Staszek", "tak");
		assertEquals("traces;0", player.getQuest(Step.COAST_WRECK.slot));
	}

	@Test
	public void everyInteractionAndOpponentFitsTheExistingMaps()
			throws Exception {
		for (String name : new String[] { CoastTrailProgress.WEST,
				CoastTrailProgress.EAST, CoastTrailProgress.CELLAR }) {
			String file = name.equals(CoastTrailProgress.CELLAR)
					? "data/maps/interiors/gdansk/storage_-1.tmx"
					: "data/maps/Level 0/gdansk/"
							+ (name.equals(CoastTrailProgress.WEST) ? "city_nw"
									: "city_ne")
							+ ".tmx";
			LayerDefinition c = TMXLoader.load(file).getLayer("collision");
			c.build();
			LayerDefinition roof = TMXLoader.load(file).getLayer("3_roof");
			roof.build();
			int w = c.getWidth(), h = c.getHeight();
			Set<Integer> seen = new HashSet<>();
			Queue<Integer> q = new ArrayDeque<>();
			if (name.equals(CoastTrailProgress.CELLAR)) {
				seen.add(10 * w + 26);
				q.add(10 * w + 26);
			} else
				for (int x = 0; x < w; x++)
					if (c.getTileAt(x, h - 1) == 0) {
						seen.add((h - 1) * w + x);
						q.add((h - 1) * w + x);
					}
			while (!q.isEmpty()) {
				int p = q.remove();
				for (int[] d : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 },
						{ 0, -1 } }) {
					int x = p % w + d[0], y = p / w + d[1], n = y * w + x;
					if (x >= 0 && x < w && y >= 0 && y < h
							&& c.getTileAt(x, y) == 0 && seen.add(n))
						q.add(n);
				}
			}
			for (Kind k : Kind.values())
				if (k.zone.equals(name)) {
					assertTrue(k.name, seen.contains(k.y * w + k.x));
					assertEquals(k.name, 0, roof.getTileAt(k.x, k.y));
					assertTrue(k.name + " approach",
							seen.contains((k.y + 1) * w + k.x));
				}
			for (Encounter role : Encounter.values())
				if (role.zone.equals(name))
					assertTrue(role.name, seen.contains(role.y * w + role.x));
			if (name.equals(CoastTrailProgress.WEST))
				assertTrue(seen.contains(60 * w + 110));
		}
	}
}
