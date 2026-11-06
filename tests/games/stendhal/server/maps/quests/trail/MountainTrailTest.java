package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Queue;
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
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.trail.MountainTrailProgress.Encounter;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import games.stendhal.server.maps.koscielisko.plains.BrzezdomNPC;
import games.stendhal.server.maps.semos.mines.MinerNPC;
import games.stendhal.server.maps.semos.townhall.LeaderNPC;
import games.stendhal.server.maps.zakopane.shop.StasekNPC;
import games.stendhal.server.maps.zakopane.tavern.JagnaNPC;
import games.stendhal.server.maps.zakopane.townhall.GazdaWojtekNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import marauroa.common.game.RPObject;

public class MountainTrailTest {
	private static final String[] NAMES = { "Barbarus", "Gazda Wojtek",
			"Stasek", "Jagna", "Brzezdom", "Lieutenant Drilenun",
			MountainTrailProgress.MINER, MountainTrailProgress.DRIVER };
	private List<IQuest> loaded, previous;
	private final Map<String, SpeakerNPC> oldNpcs = new HashMap<>();
	private final List<StendhalRPZone> zones = new ArrayList<>();
	private final StendhalQuestSystem system = StendhalQuestSystem.get();
	private StendhalRPZone mine, forest, townhall, semos, shop, tavern;
	private Player player;

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
		for (String name : NAMES)
			oldNpcs.put(name, NPCList.get().remove(name));
		mine = zone(MountainTrailProgress.MINE);
		forest = zone(TrailQuestLine.FOREST);
		townhall = zone("int_zakopane_townhall");
		semos = zone("int_semos_townhall");
		shop = zone("int_zakopane_shop");
		tavern = zone("int_zakopane_tavern");
		new MinerNPC().configureZone(mine, null);
		new LeaderNPC().configureZone(semos, null);
		new GazdaWojtekNPC().configureZone(townhall, null);
		new StasekNPC().configureZone(shop, null);
		new JagnaNPC().configureZone(tavern, null);
		new BrzezdomNPC().configureZone(forest, null);
		new MountainTrailSites().configureZone(mine, null);
		new MountainTrailSites().configureZone(forest, null);
		for (Step step : Step.values())
			if (step.chapter.equals(TrailQuestLine.MOUNTAIN_CHAPTER))
				system.loadQuest(new MountainTrailQuest(step));
		player = PlayerTestHelper.createPlayer("mountain-story");
		mine.add(player);
		MockStendhalRPRuleProcessor.get().addPlayer(player);
	}

	private StendhalRPZone zone(String name) {
		StendhalRPZone zone = new StendhalRPZone(name, 128, 128);
		zones.add(zone);
		MockStendlRPWorld.get().addRPZone(zone);
		return zone;
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
		for (StendhalRPZone zone : zones)
			MockStendlRPWorld.get().removeZone(zone);
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

	private void ready(int level) {
		player.setQuest("wegiel_na_opal", "done");
		for (Step step : Step.values()) {
			if (step == Step.MOUNTAIN_MINERS)
				break;
			player.setQuest(step.slot, "done");
		}
		player.setXP(Level.getXP(level));
		player.setLevel(level);
	}

	private void miners() {
		ready(50);
		accept("Barbarus", "zaginieni górnicy");
		visit(MountainTrailProgress.MINER);
		say(MountainTrailProgress.MINER, "górników");
		assertEquals("report", player.getQuest(Step.MOUNTAIN_MINERS.slot));
		visit("Barbarus");
		say("Barbarus", "zaginieni górnicy");
	}

	private void threats() {
		miners();
		accept("Gazda Wojtek", "pogróżki");
		visit("Jagna");
		say("Jagna", "pogróżki");
		visit("Stasek");
		say("Stasek", "pogróżki");
		String history = MountainTrailProgress.history(player,
				Step.MOUNTAIN_THREATS).toString();
		assertTrue(history, history.contains("Stasek rozpoznał")
				&& history.contains("Jagna widziała"));
		visit("Gazda Wojtek");
		say("Gazda Wojtek", "pogróżki");
	}

	private CreatureRespawnPoint point(Encounter role) {
		for (CreatureRespawnPoint point : forest.getRespawnPointList())
			if (point.getX() == role.x && point.getY() == role.y)
				return point;
		throw new AssertionError(role);
	}

	private MountainTrailGuard guard(Encounter role) {
		CreatureRespawnPoint source = point(role);
		MountainTrailGuard guard = (MountainTrailGuard) source.getPrototypeCreature().getNewInstance();
		guard.setPosition(role.x, role.y);
		guard.setRespawnPoint(source);
		forest.add(guard);
		return guard;
	}

	private void kill(Encounter role) {
		move(forest);
		player.setPosition(role.x - 1, role.y);
		MountainTrailGuard guard = guard(role);
		guard.onDamaged(player, guard.getHP() + guard.getArmorHP());
		guard.onDead(player);
	}

	private void meeting() {
		threats();
		accept("Brzezdom", "obcych na polanie");
		kill(Encounter.MEETING_DUERGAR);
		kill(Encounter.MEETING_HUMAN);
		visit(MountainTrailProgress.DRIVER);
		say(MountainTrailProgress.DRIVER, "więzy");
		visit("Brzezdom");
		say("Brzezdom", "obcy na polanie");
	}

	private MountainTrailSites.Cargo cargo() {
		return (MountainTrailSites.Cargo) forest.getEntitiesOfClass(
				MountainTrailSites.Cargo.class).iterator().next();
	}

	private void weapons() {
		meeting();
		accept("Stasek", "ukrytą broń");
		move(forest);
		player.setPosition(90, 66);
		assertTrue(cargo().onUsed(player));
		visit("Stasek");
		say("Stasek", "ukryta broń");
		visit("Lieutenant Drilenun");
		say("Lieutenant Drilenun", "transporcie broni");
	}

	private void battle() {
		weapons();
		accept("Brzezdom", "zbiórkę oddziału");
		kill(Encounter.COMMANDER);
		kill(Encounter.BATTLE_HUMAN);
		kill(Encounter.BATTLE_DUERGAR);
		visit("Brzezdom");
		say("Brzezdom", "zbiórka oddziału");
	}

	@Test
	public void chapterContinuesTheMineWithSixLevelFiftyQuests() {
		int count = 0;
		for (TrailQuestLine.Step step : TrailQuestLine.Step.values()) {
			if ("Ludzie Radana".equals(step.chapter)) {
				assertEquals(50, step.minimumLevel);
				count++;
			}
		}
		assertEquals(6, count);
	}

	@Test
	public void levelAndPreviousChapterGateTheFirstOffer() {
		visit("Barbarus");
		say("Barbarus", "zaginieni górnicy");
		assertFalse(player.hasQuest(Step.MOUNTAIN_MINERS.slot));
		ready(49);
		assertFalse(Step.MOUNTAIN_MINERS.available(player));
		assertTrue(system.getMainQuests(player).isEmpty());
		assertTrue(TrailQuestLine.objective(player,
				Step.MOUNTAIN_MINERS.slot).contains("50"));
		visit("Barbarus");
		assertFalse(say("Barbarus", "zaginieni górnicy").contains("50"));
		assertFalse(player.hasQuest(Step.MOUNTAIN_MINERS.slot));
		ready(50);
		accept("Barbarus", "zaginieni górnicy");
		assertEquals("search", player.getQuest(Step.MOUNTAIN_MINERS.slot));
		assertEquals(1, system.getMainQuests(player).size());
	}

	@Test
	public void fullChapterResolvesBothTownsAndRewardsOnlyOnce() {
		ready(50);
		int beforeXP = player.getXP();
		battle();
		assertTrue(player.isQuestCompleted(Step.MOUNTAIN_BATTLE.slot));
		accept("Gazda Wojtek", "wieści z lasu");
		say("Gazda Wojtek", "wieści z lasu");
		visit("Lieutenant Drilenun");
		say("Lieutenant Drilenun", "wieści z lasu");
		assertEquals("reports;3", player.getQuest(Step.MOUNTAIN_RETURN.slot));
		visit("Barbarus");
		say("Barbarus", "wieści z lasu");
		assertEquals("ready", player.getQuest(Step.MOUNTAIN_RETURN.slot));
		visit("Gazda Wojtek");
		say("Gazda Wojtek", "wieści z lasu");
		assertTrue(player.isQuestCompleted(Step.MOUNTAIN_RETURN.slot));
		// Combat still awards the engine's minimum one XP for each of the five
		// opponents.
		assertEquals(42005, player.getXP() - beforeXP);
		assertEquals(1000, player.getTotalNumberOf("money"));
		Item reward = player.getFirstEquipped("cuha góralska");
		assertNotNull(reward);
		assertEquals(ItemRarity.RARE, reward.getRarity());
		assertEquals(player.getName(), reward.getBoundTo());
		assertTrue(ItemAffixState.hasAny(reward));
		int xp = player.getXP();
		say("Gazda Wojtek", "wieści z lasu");
		assertEquals(xp, player.getXP());
		assertEquals(1, player.getTotalNumberOf("cuha góralska"));
		assertTrue(system.getMainQuests(player).isEmpty());
	}

	@Test
	public void meetingCreditSurvivesRelogAndCannotComeFromSummonedOrRemoteCopies() {
		threats();
		accept("Brzezdom", "obcy na polanie");
		kill(Encounter.MEETING_DUERGAR);
		assertEquals("guards;2", player.getQuest(Step.MOUNTAIN_MEETING.slot));
		String objective = TrailQuestLine.objective(player,
				Step.MOUNTAIN_MEETING.slot);
		assertTrue(objective, objective.contains("wspólnika Radana"));
		assertFalse(objective, objective.contains("wysłannika"));
		Player restored = new Player(player);
		player.getZone().remove(player);
		forest.add(restored);
		player = restored;
		MountainTrailGuard copy = guard(Encounter.MEETING_HUMAN);
		copy.setRespawnPoint(null);
		player.setPosition(90, 65);
		MountainTrailProgress.recordKill(player, copy);
		assertEquals("guards;2", player.getQuest(Step.MOUNTAIN_MEETING.slot));
		copy.setRespawnPoint(point(Encounter.MEETING_HUMAN));
		player.setPosition(1, 1);
		MountainTrailProgress.recordKill(player, copy);
		assertEquals("guards;2", player.getQuest(Step.MOUNTAIN_MEETING.slot));
		kill(Encounter.MEETING_HUMAN);
		assertEquals("rescue", player.getQuest(Step.MOUNTAIN_MEETING.slot));
	}

	@Test
	public void strongerGuardsIgnoreOtherTravellersAndKeepTheirRoleWhenRespawned() {
		MountainTrailGuard guard = guard(Encounter.COMMANDER);
		move(forest);
		player.setPosition(79, 48);
		assertTrue(guard.getEnemyList().isEmpty());
		ready(50);
		player.setQuest(Step.MOUNTAIN_CARGO.slot, "done");
		player.setQuest(Step.MOUNTAIN_BATTLE.slot, "battle;0");
		assertTrue(guard.getEnemyList().contains(player));
		Player other = PlayerTestHelper.createPlayer("newcomer");
		forest.add(other);
		other.setPosition(79, 48);
		assertFalse(guard.getEnemyList().contains(other));
		assertSame(Encounter.COMMANDER,
				((MountainTrailGuard) guard.getNewInstance()).role);
		player.setPosition(74, 48);
		guard.rememberAttacker(player);
		assertTrue(
				"An eligible attacker must not get free ranged hits beyond the short acquisition radius",
				guard.getEnemyList().contains(player));
		player.setPosition(79, 48);
		guard.setTarget(player);
		assertTrue(guard.isAttacking());
		player.setPosition(90, 48);
		guard.logic();
		assertFalse("An acquired target outside the encounter must be released",
				guard.isAttacking());
		player.setPosition(79, 48);
		guard.setTarget(player);
		player.setQuest(Step.MOUNTAIN_BATTLE.slot, "battle;4");
		guard.logic();
		assertFalse(
				"The guard must release a player who no longer needs this opponent",
				guard.isAttacking());
		player.setPosition(1, 1);
		assertFalse(guard.getEnemyList().contains(player));
	}

	@Test
	public void cargoCannotSkipTheRescueOrBeReadRemotely() {
		ready(50);
		move(forest);
		player.setPosition(90, 66);
		assertFalse(cargo().onUsed(player));
		player.setQuest(Step.MOUNTAIN_MEETING.slot, "done");
		accept("Stasek", "ukryta broń");
		move(forest);
		player.setPosition(1, 1);
		assertFalse(cargo().onUsed(player));
		player.setPosition(90, 66);
		assertTrue(cargo().onUsed(player));
		assertFalse(cargo().onUsed(player));
		assertEquals("stasek", player.getQuest(Step.MOUNTAIN_CARGO.slot));
	}

	@Test
	public void blockedRewardKeepsItsCheckpointAndWorksAfterRebirth() {
		ready(50);
		player.setQuest(Step.MOUNTAIN_BATTLE.slot, "done");
		player.setQuest(Step.MOUNTAIN_RETURN.slot, "ready");
		while (player.equip("bag",
				SingletonRepository.getEntityManager().getItem("miecz"))) {
		}
		int xp = player.getXP();
		visit("Gazda Wojtek");
		say("Gazda Wojtek", "wieści z lasu");
		assertEquals("ready", player.getQuest(Step.MOUNTAIN_RETURN.slot));
		assertEquals(xp, player.getXP());
		RPObject item = player.getSlot("bag").iterator().next();
		player.getSlot("bag").remove(item.getID());
		player.setXP(0);
		player.setLevel(0);
		say("Gazda Wojtek", "wieści z lasu");
		assertTrue(player.isQuestCompleted(Step.MOUNTAIN_RETURN.slot));
		assertEquals(1, player.getTotalNumberOf("cuha góralska"));
	}

	@Test
	public void everyActorAndApproachFitsExistingMapScenery() throws Exception {
		for (String zone : new String[] { MountainTrailProgress.MINE,
				TrailQuestLine.FOREST }) {
			String file = zone.equals(MountainTrailProgress.MINE)
					? "data/maps/Level -1/semos/mine_n2.tmx"
					: "data/maps/Level 0/koscielisko/e.tmx";
			List<int[]> points = new ArrayList<>();
			if (zone.equals(MountainTrailProgress.MINE))
				points.add(new int[] { 76, 65 });
			else {
				points.add(new int[] { 89, 65 });
				points.add(new int[] { 90, 65 });
				points.add(new int[] { 86, 53 });
				for (Encounter role : Encounter.values())
					points.add(new int[] { role.x, role.y });
			}
			LayerDefinition collision = TMXLoader.load(file).getLayer(
					"collision");
			collision.build();
			Set<Integer> reachable = new HashSet<>();
			Queue<Integer> queue = new ArrayDeque<>();
			int start = zone.equals(MountainTrailProgress.MINE) ? 78 * 128 + 57
					: 86 * 128 + 44;
			reachable.add(start);
			queue.add(start);
			while (!queue.isEmpty()) {
				int point = queue.remove();
				for (int[] d : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 },
						{ 0, -1 } }) {
					int x = point % 128 + d[0], y = point / 128 + d[1],
							next = y * 128 + x;
					if (x >= 0
							&& x < (zone.equals(MountainTrailProgress.MINE)
									? 121
									: 128)
							&& y >= 0 && y < 128
							&& collision.getTileAt(x, y) == 0
							&& reachable.add(next))
						queue.add(next);
				}
			}
			for (int[] point : points)
				assertTrue(
						file + " unreachable actor at " + point[0] + ","
								+ point[1],
						reachable.contains(point[1] * 128 + point[0]));
			for (String layer : new String[] { "collision", "3_roof" }) {
				LayerDefinition tiles = TMXLoader.load(file).getLayer(layer);
				tiles.build();
				for (int[] point : points) {
					assertEquals(file + " " + layer, 0,
							tiles.getTileAt(point[0], point[1]));
					assertEquals(0, tiles.getTileAt(point[0], point[1] + 1));
				}
			}
		}
	}
}
