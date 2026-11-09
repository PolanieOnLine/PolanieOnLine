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
import games.stendhal.common.Level;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import marauroa.common.game.RPObject;
import games.stendhal.common.QuestMarker;
import games.stendhal.common.QuestRewardPreview;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.player.QuestMarkerService;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.kikareukin.cave.Skille350NPC;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.trail.BellTrailProgress.Encounter;
import games.stendhal.server.maps.quests.trail.BellTrailSites.Kind;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class BellTrailStoryTest {
	private final StendhalQuestSystem system = StendhalQuestSystem.get();
	private final Map<String, SpeakerNPC> previousNpcs = new HashMap<>();
	private List<IQuest> loaded, previous;
	private StendhalRPZone cave, islands;
	private Player player;
	private static final String[] NAMES = { "Festris", "Niemir", "Witosław",
			"Borzęta", "Milan" };

	@BeforeClass
	public static void init() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	@SuppressWarnings("unchecked")
	public void setup() throws Exception {
		Field field = StendhalQuestSystem.class.getDeclaredField("quests");
		field.setAccessible(true);
		loaded = (List<IQuest>) field.get(null);
		previous = new ArrayList<>(loaded);
		loaded.clear();
		for (String name : NAMES) {
			previousNpcs.put(name, NPCList.get().remove(name));
		}
		cave = new StendhalRPZone(BellTrailProgress.CAVE, 128, 128);
		islands = new StendhalRPZone(BellTrailProgress.ISLANDS, 128, 128);
		MockStendlRPWorld.get().addRPZone(cave);
		MockStendlRPWorld.get().addRPZone(islands);
		new Skille350NPC().configureZone(cave, null);
		new BellTrailSites().configureZone(cave, null);
		new BellTrailSites().configureZone(islands, null);
		for (Step step : Step.values()) {
			if (TrailQuestLine.BELL_CHAPTER.equals(step.chapter)) {
				system.loadQuest(new BellTrailQuest(step));
			}
		}
		player = PlayerTestHelper.createPlayer("bell-story");
		player.setQuest("FestrisReward", "done");
		cave.add(player);
		MockStendhalRPRuleProcessor.get().addPlayer(player);
	}

	@After
	public void cleanup() {
		loaded.clear();
		loaded.addAll(previous);
		MockStendhalRPRuleProcessor.get().clearPlayers();
		for (String name : NAMES) {
			NPCList.get().remove(name);
			if (previousNpcs.get(name) != null) {
				NPCList.get().add(previousNpcs.get(name));
			}
		}
		MockStendlRPWorld.get().removeZone(cave);
		MockStendlRPWorld.get().removeZone(islands);
	}

	private void ready(int level) {
		player.setQuest("wegiel_na_opal", "done");
		for (Step step : Step.values()) {
			if (step == Step.BELL_RETURNED) {
				break;
			}
			player.setQuest(step.slot, "done");
		}
		player.setXP(Level.getXP(level));
		player.setLevel(level);
	}

	private void before(Step target) {
		ready(550);
		for (Step step : Step.values()) {
			if (step == target) {
				break;
			}
			player.setQuest(step.slot, "done");
		}
	}

	private void move(StendhalRPZone zone) {
		if (player.getZone() != zone) {
			player.getZone().remove(player);
			zone.add(player);
		}
	}

	private String say(String name, String text) {
		SpeakerNPC npc = NPCList.get().get(name);
		assertTrue(name + ": " + text, npc.getEngine().step(player, text));
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

	private BellTrailSites.Evidence evidence(Kind kind) {
		BellTrailSites.Evidence prop = new BellTrailSites.Evidence(kind);
		PlayerPrivateScene.assign(prop, player);
		StendhalRPZone zone = islands;
		zone.add(prop);
		return prop;
	}

	private void refreshScene() {
		TrailSceneRegistry.refreshLater(player);
		TurnNotifier notifier = TurnNotifier.get();
		int start = Math.max(0, notifier.getCurrentTurnForDebugging());
		for (int turn = start + 1; turn <= start + 3; turn++) {
			notifier.logic(turn);
		}
	}

	private boolean use(Kind kind) {
		move(islands);
		player.setPosition(kind.x, kind.y + 1);
		return evidence(kind).onUsed(player);
	}

	private BellTrailGuard guard(Encounter role, boolean legitimate) {
		StendhalRPZone zone = islands;
		if (legitimate && player.getZone() == zone) {
			refreshScene();
			for (Entity entity : zone.getEntitiesOfClass(BellTrailGuard.class)) {
				BellTrailGuard current = (BellTrailGuard) entity;
				if (PlayerPrivateScene.isOwnedBy(current, player) && current.role == role
						&& current.getRespawnPoint() != null) {
					return current;
				}
			}
			assertFalse("An active owned encounter must be reconstructed", BellTrailProgress.needs(player, role));
		}
		BellTrailGuard guard = new BellTrailGuard(
				TrailCreatureVariants.create(role.name), role);
		PlayerPrivateScene.assign(guard, player);
		guard.setPosition(role.x, role.y);
		if (legitimate) {
			guard.setRespawnPoint(new CreatureRespawnPoint(zone, role.x, role.y, guard, 1));
		}
		zone.add(guard);
		return guard;
	}

	private void kill(Encounter role) {
		move(islands);
		player.setPosition(role.x - 1, role.y);
		BellTrailGuard guard = guard(role, true);
		guard.onDamaged(player, guard.getHP() + guard.getArmorHP());
		guard.onDead(player);
	}

	private void arrive(Kind kind) {
		for (BellTrailEscort npc : BellTrailEscort.group(player)) {
			npc.setPosition(kind.x + 1, kind.y);
		}
		assertTrue(use(kind));
	}

	@Test
	public void finalChapterStartsAt550AfterTheDragonLands() {
		Step finale = Step.values()[Step.values().length - 1];
		assertEquals("Milczący dzwon", finale.chapter);
		assertEquals(550, finale.minimumLevel);
		assertEquals(Step.EMBER_RETURN.slot, Step.BELL_RETURNED.previousSlot());
		ready(549);
		visit("Festris");
		say("Festris", "zaginiona wyprawa");
		assertFalse(player.hasQuest(Step.BELL_RETURNED.slot));
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player,
				NPCList.get().get("Festris")));
		ready(550);
		assertEquals(QuestMarker.MAIN_AVAILABLE, QuestMarkerService.getMarker(
				player, NPCList.get().get("Festris")));
	}

	@Test
	public void wholeStoryRescuesBothWoundedAndPaysOnlyOnce() {
		ready(550);
		accept("Festris", "zaginiona wyprawa");
		visit("Niemir");
		say("Niemir", "wyprawa");
		visit("Festris");
		say("Festris", "zaginiona wyprawa");
		assertTrue(player.isQuestCompleted(Step.BELL_RETURNED.slot));
		accept("Witosław", "ranni");
		visit("Borzęta");
		say("Borzęta", "ranni");
		visit("Milan");
		say("Milan", "ranni");
		visit("Witosław");
		say("Witosław", "ranni");
		accept("Borzęta", "głos z mgły");
		assertTrue(use(Kind.LOG));
		assertFalse(use(Kind.LOG));
		assertTrue(use(Kind.INSCRIPTION));
		assertTrue(use(Kind.BELL));
		visit("Borzęta");
		say("Borzęta", "głos z mgły");
		accept("Witosław", "milczący dzwon");
		assertFalse(use(Kind.BELL));
		assertTrue(use(Kind.WEST_WARD));
		assertTrue(use(Kind.EAST_WARD));
		kill(Encounter.WARDEN);
		assertEquals("silence", player.getQuest(Step.BELL_GUARD.slot));
		assertTrue(use(Kind.BELL));
		visit("Witosław");
		say("Witosław", "milczący dzwon");
		accept("Witosław", "powrót wyprawy");
		assertTrue(use(Kind.ASSEMBLY));
		assertEquals(2, BellTrailEscort.group(player).size());
		assertFalse(use(Kind.SAFE_CAMP));
		assertFalse(use(Kind.FIRST_STOP));
		arrive(Kind.FIRST_STOP);
		assertEquals("ambush;1", player.getQuest(Step.BELL_ESCAPE.slot));
		assertFalse(use(Kind.BRIDGE_STOP));
		kill(Encounter.ECHO);
		arrive(Kind.BRIDGE_STOP);
		arrive(Kind.SAFE_CAMP);
		assertTrue(BellTrailEscort.group(player).isEmpty());
		visit("Festris");
		assertEquals(QuestMarker.READY, QuestMarkerService.getMarker(player,
				NPCList.get().get("Festris")));
		say("Festris", "powrót wyprawy");
		assertTrue(player.isQuestCompleted(Step.BELL_ESCAPE.slot));
		assertEquals(1, player.getTotalNumberOf("złota skrzynia"));
		int xp = player.getXP(), money = player.getTotalNumberOf("money");
		say("Festris", "powrót wyprawy");
		assertEquals(xp, player.getXP());
		assertEquals(money, player.getTotalNumberOf("money"));
		assertEquals(1, player.getTotalNumberOf("złota skrzynia"));
		assertNull(TrailQuestLine.nextObjective(player));
	}

	@Test
	public void guardsNeedRealSpawnAndNeverAwardCombatExperience() {
		before(Step.BELL_GUARD);
		player.setQuest(Step.BELL_GUARD.slot, "fight");
		move(islands);
		player.setPosition(Encounter.WARDEN.x - 1, Encounter.WARDEN.y);
		BellTrailGuard fake = guard(Encounter.WARDEN, false);
		BellTrailProgress.recordKill(player, fake);
		assertEquals("fight", player.getQuest(Step.BELL_GUARD.slot));
		assertEquals(0, fake.getXP());
		int xp = player.getXP();
		kill(Encounter.WARDEN);
		assertEquals("silence", player.getQuest(Step.BELL_GUARD.slot));
		assertEquals(xp, player.getXP());
	}

	@Test
	public void escortsArePersonalAndResumeAtSavedStopAfterLeavingZone() {
		before(Step.BELL_ESCAPE);
		player.setQuest(Step.BELL_ESCAPE.slot, "escort;2");
		assertTrue(use(Kind.ASSEMBLY));
		List<BellTrailEscort> first = BellTrailEscort.group(player);
		assertEquals(2, first.size());
		assertEquals(BellTrailProgress.ROUTE[2][0], first.get(0).getX());
		assertTrue(use(Kind.ASSEMBLY));
		assertEquals(2, BellTrailEscort.group(player).size());
		Player other = PlayerTestHelper.createPlayer("bell-other");
		islands.add(other);
		assertTrue(BellTrailEscort.group(other).isEmpty());
		move(cave);
		for (BellTrailEscort npc : first) {
			npc.logic();
		}
		assertTrue(islands.getEntitiesOfClass(BellTrailEscort.class).isEmpty());
		assertEquals("escort;2", player.getQuest(Step.BELL_ESCAPE.slot));
		assertTrue(use(Kind.ASSEMBLY));
		assertEquals(2, BellTrailEscort.group(player).size());
	}

	@Test
	public void rewardPreviewContainsOnlyGuaranteedFinalRewards() {
		QuestRewardPreview rewards = system.getQuest(
				Step.BELL_ESCAPE.title).getQuestInfo(player).getRewards();
		assertEquals(650000, rewards.xp);
		assertEquals(12500, rewards.coins);
		assertEquals(1, rewards.items.size());
		assertEquals("złota skrzynia", rewards.items.get(0).name);
		assertFalse(player.hasQuest(Step.BELL_ESCAPE.slot));
	}

	@Test
	public void nativeMapsHaveReachablePropsAndTheConvoyActuallyWalks()
			throws Exception {
		StendhalMapStructure map = TMXLoader.load(
				"data/maps/Level 6/kikareukin/islands.tmx");
		map.build();
		islands.addCollisionLayer(islands.getName() + ".collision",
				map.getLayer("collision"));
		for (Kind kind : Kind.values()) {
			assertFalse(kind.name, islands.collides(kind.x, kind.y));
			assertFalse(kind.name, Path.searchPath(islands, 109, 28, kind.x,
					kind.y, 200).isEmpty());
		}
		for (Encounter role : Encounter.values()) {
			for (int x = role.x; x < role.x + 3; x++) {
				for (int y = role.y; y < role.y + 2; y++) {
					assertFalse(role.name + " footprint",
							islands.collides(x, y));
				}
			}
		}
		before(Step.BELL_ESCAPE);
		player.setQuest(Step.BELL_ESCAPE.slot, "escort;2");
		assertTrue(use(Kind.ASSEMBLY));
		player.setPosition(70, 12);
		List<BellTrailEscort> group = BellTrailEscort.group(player);
		for (int turn = 0; turn < 50; turn++) {
			for (BellTrailEscort escort : group) {
				escort.logic();
			}
		}
		for (BellTrailEscort escort : group) {
			assertTrue("The wounded must follow their guide on the native map",
					escort.nextTo(player, 3));
			assertFalse(escort.isAttackable());
		}
	}

	@Test
	public void finalRewardWaitsForBagSpaceWithoutDoublePayment() {
		before(Step.BELL_ESCAPE);
		player.setQuest(Step.BELL_ESCAPE.slot, "return");
		StackableItem coins = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"money");
		coins.setQuantity(12500);
		while (player.getSlotToEquip(coins) != null) {
			String slot = player.getSlotToEquip(coins).getName();
			assertTrue(player.equip(slot,
					SingletonRepository.getEntityManager().getItem("miecz")));
		}
		int xp = player.getXP();
		visit("Festris");
		say("Festris", "powrót wyprawy");
		assertEquals("return", player.getQuest(Step.BELL_ESCAPE.slot));
		assertEquals(xp, player.getXP());
		assertEquals(0, player.getTotalNumberOf("złota skrzynia"));
		RPObject first = player.getSlot("bag").iterator().next();
		player.getSlot("bag").remove(first.getID());
		say("Festris", "powrót wyprawy");
		assertEquals("return", player.getQuest(Step.BELL_ESCAPE.slot));
		assertEquals(0, player.getTotalNumberOf("złota skrzynia"));
		RPObject second = player.getSlot("bag").iterator().next();
		player.getSlot("bag").remove(second.getID());
		say("Festris", "powrót wyprawy");
		assertTrue(player.isQuestCompleted(Step.BELL_ESCAPE.slot));
		assertEquals(1, player.getTotalNumberOf("złota skrzynia"));
		assertEquals(12500, player.getTotalNumberOf("money"));
		for (Object entity : cave.getEntitiesOfClass(Item.class)) {
			assertNotEquals("money", ((Item) entity).getName());
		}
	}

	@Test
	public void fullInventoryCannotDuplicateARewardIntoAnExistingChestStack() {
		before(Step.BELL_ESCAPE);
		player.setQuest(Step.BELL_ESCAPE.slot, "return");
		Item chest = SingletonRepository.getEntityManager().getItem("złota skrzynia");
		chest.setBoundTo(player.getName());
		assertTrue(player.equip("bag", chest));
		StackableItem coins = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		coins.setQuantity(12500);
		while (player.getSlotToEquip(coins) != null) {
			String slot = player.getSlotToEquip(coins).getName();
			assertTrue(player.equip(slot, SingletonRepository.getEntityManager().getItem("miecz")));
		}
		int xp = player.getXP();
		visit("Festris");
		for (int i = 0; i < 2; i++) {
			say("Festris", "powrót wyprawy");
			assertEquals(1, player.getTotalNumberOf("złota skrzynia"));
			assertEquals("return", player.getQuest(Step.BELL_ESCAPE.slot));
			assertEquals(xp, player.getXP());
		}
		for (RPObject item : player.getSlot("bag")) {
			if (item != chest) {
				player.getSlot("bag").remove(item.getID());
				break;
			}
		}
		say("Festris", "powrót wyprawy");
		assertTrue(player.isQuestCompleted(Step.BELL_ESCAPE.slot));
		assertEquals(2, player.getTotalNumberOf("złota skrzynia"));
		assertEquals(12500, player.getTotalNumberOf("money"));
		say("Festris", "powrót wyprawy");
		assertEquals(2, player.getTotalNumberOf("złota skrzynia"));
	}

	@Test
	public void logoutRemovesConvoyEvenThoughThePlayerRemembersTheirLastZone() {
		before(Step.BELL_ESCAPE);
		player.setQuest(Step.BELL_ESCAPE.slot, "escort;0");
		assertTrue(use(Kind.ASSEMBLY));
		List<BellTrailEscort> convoy = BellTrailEscort.group(player);
		islands.remove(player);
		assertSame(islands, player.getZone());
		for (BellTrailEscort npc : convoy) {
			npc.logic();
		}
		assertTrue(islands.getEntitiesOfClass(BellTrailEscort.class).isEmpty());
	}

	@Test
	public void mandatoryEncountersExistOnlyAtTheOwnersActiveCheckpoint()
			throws Exception {
		assertTrue(islands.getRespawnPointList().isEmpty());
		assertTrue(islands.getEntitiesOfClass(BellTrailGuard.class).isEmpty());
		before(Step.BELL_GUARD);
		player.setQuest(Step.BELL_GUARD.slot, "fight");
		move(islands);
		refreshScene();
		BellTrailGuard warden = guard(Encounter.WARDEN, true);
		assertTrue(PlayerPrivateScene.isOwnedBy(warden, player));
		assertEquals(Encounter.WARDEN.x, warden.getX());
		assertEquals(Encounter.WARDEN.y, warden.getY());
		assertEquals(1, islands.getEntitiesOfClass(BellTrailGuard.class).size());
		player.setQuest(Step.BELL_GUARD.slot, "silence");
		refreshScene();
		assertTrue(islands.getEntitiesOfClass(BellTrailGuard.class).isEmpty());
		assertTrue(islands.getRespawnPointList().isEmpty());
	}

	@Test
	public void abandonedGuardStopsFollowingAndLeavesTheWorldWithItsOwner() {
		before(Step.BELL_GUARD);
		player.setQuest(Step.BELL_GUARD.slot, "fight");
		move(islands);
		BellTrailGuard guard = guard(Encounter.WARDEN, true);
		guard.setPosition(105, 30);
		player.setPosition(104, 30);
		guard.setTarget(player);
		islands.remove(player);
		guard.logic();
		assertNull(guard.getAttackTarget());
		assertFalse("Leaving removes the abandoned private encounter", islands.has(guard.getID()));
	}

}
