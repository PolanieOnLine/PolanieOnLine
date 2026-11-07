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
import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.ados.abandonedkeep.OrcKillGiantDwarfNPC;
import games.stendhal.server.maps.ados.abandonedkeep.OrcWeaponArmorGuyNPC;
import games.stendhal.server.maps.ados.library.LibrarianNPC;
import games.stendhal.server.maps.ados.wall.GreeterSoldierNPC;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.EmotionCrystals;
import games.stendhal.server.maps.quests.KillDhohrNuggetcutter;
import games.stendhal.server.maps.quests.trail.AdosKeepSites.Kind;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import marauroa.common.game.RPObject;

public class AdosKeepStoryTest {
	private static final String[] NAMES = { "Julius", "Zogfang", "Hagnurk",
			"Wikipedian", "Czerwony Kryształ", "Purpurowy Kryształ",
			"Żółty Kryształ", "Różowy Kryształ", "Niebieski Kryształ" };
	private final Map<String, SpeakerNPC> oldNpcs = new HashMap<>();
	private final List<StendhalRPZone> zones = new ArrayList<>();
	private List<IQuest> loaded, previous;
	private final StendhalQuestSystem system = StendhalQuestSystem.get();
	private StendhalRPZone outside, keep, wall, library;
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
		outside = zone(AdosKeepProgress.OUTSIDE);
		keep = zone(AdosKeepProgress.KEEP);
		wall = zone("0_ados_wall_n2");
		library = zone("int_ados_library");
		new GreeterSoldierNPC().configureZone(wall, null);
		new OrcKillGiantDwarfNPC().configureZone(keep, null);
		new OrcWeaponArmorGuyNPC().configureZone(keep, null);
		new LibrarianNPC().configureZone(library, null);
		new AdosKeepSites().configureZone(outside, null);
		new AdosKeepSites().configureZone(keep, null);
		for (Step step : Step.values())
			if (step.chapter.equals(TrailQuestLine.KEEP_CHAPTER))
				system.loadQuest(new AdosKeepQuest(step));
		player = PlayerTestHelper.createPlayer("keep-story");
		wall.add(player);
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
			if (step == Step.KEEP_TRAIL)
				break;
			player.setQuest(step.slot, "done");
		}
		player.setXP(Level.getXP(level));
		player.setLevel(level);
	}

	private AdosKeepSites.Evidence evidence(Kind kind) {
		StendhalRPZone zone = kind.zone.equals(AdosKeepProgress.KEEP) ? keep
				: outside;
		for (Object entity : zone.getEntitiesOfClass(
				AdosKeepSites.Evidence.class))
			if (((AdosKeepSites.Evidence) entity).kind == kind)
				return (AdosKeepSites.Evidence) entity;
		throw new AssertionError(kind);
	}

	private boolean use(Kind kind) {
		AdosKeepSites.Evidence prop = evidence(kind);
		move(prop.getZone());
		player.setPosition(prop.getX(), prop.getY() + 1);
		return prop.onUsed(player);
	}

	private void tracks() {
		ready(75);
		accept("Julius", "starej warowni");
		assertTrue(use(Kind.FRAGMENT));
		assertTrue(use(Kind.TRACKS));
		visit("Julius");
		say("Julius", "stara warownia");
	}

	private void witnesses() {
		tracks();
		accept("Julius", "uciekinierach");
		visit("Hagnurk");
		say("Hagnurk", "hałasach w warowni");
		visit("Zogfang");
		say("Zogfang", "hałasy w warowni");
		String history = AdosKeepProgress.history(player,
				Step.KEEP_WITNESSES).toString();
		assertTrue(history, history.contains("Zogfang słyszał")
				&& history.contains("Hagnurk stracił"));
		visit("Julius");
		say("Julius", "uciekinierzy");
	}

	private void inscriptions() {
		witnesses();
		accept("Wikipedian", "starych napisach");
		assertTrue(use(Kind.EAST));
		assertTrue(use(Kind.WEST));
		visit("Wikipedian");
		say("Wikipedian", "stare napisy");
	}

	private AdosKeepGuardian guardian() {
		CreatureRespawnPoint source = keep.getRespawnPointList().get(0);
		AdosKeepGuardian guard = (AdosKeepGuardian) source.getPrototypeCreature().getNewInstance();
		guard.setRespawnPoint(source);
		guard.setPosition(45, 108);
		keep.add(guard);
		return guard;
	}

	private void fight() {
		inscriptions();
		accept("Zogfang", "kamiennym strażniku");
		move(keep);
		player.setPosition(44, 108);
		AdosKeepGuardian guard = guardian();
		guard.onDamaged(player, guard.getHP() + guard.getArmorHP());
		guard.onDead(player);
		visit("Zogfang");
		say("Zogfang", "kamienny strażnik");
	}

	@Test
	public void fiveQuestsContinueTheMainStoryAtLevelSeventyFive() {
		int count = 0;
		for (TrailQuestLine.Step step : TrailQuestLine.Step.values()) {
			if ("Stara warownia".equals(step.chapter)) {
				assertEquals(75, step.minimumLevel);
				count++;
			}
		}
		assertEquals(5, count);
	}

	@Test
	public void levelAndMountainEndingGateTheChapter() {
		visit("Julius");
		say("Julius", "warownia");
		assertFalse(player.hasQuest(Step.KEEP_TRAIL.slot));
		ready(74);
		assertTrue(system.getMainQuests(player).isEmpty());
		assertTrue(
				TrailQuestLine.objective(player, Step.KEEP_TRAIL.slot).contains(
						"75"));
		visit("Julius");
		assertFalse(say("Julius", "stara warownia").contains("75"));
		assertFalse(player.hasQuest(Step.KEEP_TRAIL.slot));
		ready(75);
		accept("Julius", "stara warownia");
		assertEquals("clues;0", player.getQuest(Step.KEEP_TRAIL.slot));
		assertEquals(1, system.getMainQuests(player).size());
	}

	@Test
	public void fullStoryUsesTheActualTopicsAndRewardsExactlyOnce() {
		ready(75);
		int xp = player.getXP();
		fight();
		assertTrue(player.isQuestCompleted(Step.KEEP_GUARDIAN.slot));
		accept("Julius", "drogi do Ados");
		visit("Hagnurk");
		say("Hagnurk", "bezpiecznej drodze");
		assertEquals("ready", player.getQuest(Step.KEEP_RETURN.slot));
		visit("Julius");
		say("Julius", "droga do Ados");
		assertTrue(player.isQuestCompleted(Step.KEEP_RETURN.slot));
		assertEquals(62501, player.getXP() - xp);
		assertEquals(1700, player.getTotalNumberOf("money"));
		Item reward = player.getFirstEquipped("hełm chaosu");
		assertNotNull(reward);
		assertEquals(ItemRarity.RARE, reward.getRarity());
		assertEquals(player.getName(), reward.getBoundTo());
		assertTrue(ItemAffixState.hasAny(reward));
		xp = player.getXP();
		say("Julius", "droga do Ados");
		assertEquals(xp, player.getXP());
		assertEquals(1, player.getTotalNumberOf("hełm chaosu"));
		assertTrue(system.getMainQuests(player).isEmpty());
	}

	@Test
	public void evidenceIsPersonalRangeCheckedAndSurvivesReload() {
		ready(75);
		accept("Julius", "stara warownia");
		assertFalse(use(Kind.WEST));
		assertTrue(use(Kind.FRAGMENT));
		assertFalse(use(Kind.FRAGMENT));
		Player restored = new Player(player);
		player.getZone().remove(player);
		outside.add(restored);
		player = restored;
		assertEquals("clues;2", player.getQuest(Step.KEEP_TRAIL.slot));
		assertFalse(
				TrailQuestLine.objective(player, Step.KEEP_TRAIL.slot).contains(
						"odłamek"));
		AdosKeepSites.Evidence prop = evidence(Kind.TRACKS);
		player.setPosition(1, 1);
		assertFalse(prop.onUsed(player));
		Player other = PlayerTestHelper.createPlayer("keep-other");
		other.setQuest(Step.MOUNTAIN_RETURN.slot, "done");
		other.setQuest(Step.KEEP_TRAIL.slot, "clues;0");
		wall.add(other);
		other.setPosition(13, 123);
		assertFalse(prop.onUsed(other));
		wall.remove(other);
		outside.add(other);
		assertTrue(prop.onUsed(other));
		assertEquals("clues;1", other.getQuest(Step.KEEP_TRAIL.slot));
		assertEquals("clues;2", player.getQuest(Step.KEEP_TRAIL.slot));
		assertTrue(use(Kind.TRACKS));
		assertEquals(3,
				AdosKeepProgress.history(player, Step.KEEP_TRAIL).size());
	}

	@Test
	public void guardianRequiresTheOriginalSpawnAndNearbyCredit() {
		ready(75);
		player.setQuest(Step.KEEP_INSCRIPTIONS.slot, "done");
		accept("Zogfang", "kamienny strażnik");
		move(keep);
		player.setPosition(44, 108);
		Creature copy = TrailCreatureVariants.create(
				AdosKeepProgress.GUARDIAN);
		copy.setPosition(45, 108);
		keep.add(copy);
		AdosKeepProgress.recordGuardianKill(player, copy);
		assertEquals("fight", player.getQuest(Step.KEEP_GUARDIAN.slot));
		AdosKeepGuardian guard = guardian();
		guard.setRespawnPoint(null);
		AdosKeepProgress.recordGuardianKill(player, guard);
		assertEquals("fight", player.getQuest(Step.KEEP_GUARDIAN.slot));
		guard.setRespawnPoint(new CreatureRespawnPoint(keep, 1, 1, guard, 1));
		AdosKeepProgress.recordGuardianKill(player, guard);
		assertEquals("fight", player.getQuest(Step.KEEP_GUARDIAN.slot));
		guard.setRespawnPoint(keep.getRespawnPointList().get(0));
		player.setPosition(1, 1);
		AdosKeepProgress.recordGuardianKill(player, guard);
		assertEquals("fight", player.getQuest(Step.KEEP_GUARDIAN.slot));
		player.setPosition(44, 108);
		guard.onDamaged(player, guard.getHP() + guard.getArmorHP());
		guard.onDead(player);
		assertEquals("report", player.getQuest(Step.KEEP_GUARDIAN.slot));
	}

	@Test
	public void guardianIgnoresTravellersButRetaliatesAndReleasesOldTargets() {
		move(keep);
		player.setPosition(44, 108);
		AdosKeepGuardian guard = guardian();
		assertTrue(guard.getEnemyList().isEmpty());
		ready(75);
		player.setQuest(Step.KEEP_INSCRIPTIONS.slot, "done");
		player.setQuest(Step.KEEP_GUARDIAN.slot, "fight");
		assertTrue(guard.getEnemyList().contains(player));
		Player other = PlayerTestHelper.createPlayer("keep-traveller");
		keep.add(other);
		other.setPosition(44, 108);
		assertFalse(guard.getEnemyList().contains(other));
		player.setPosition(39, 108);
		guard.rememberAttacker(player);
		assertTrue(guard.getEnemyList().contains(player));
		guard.setTarget(player);
		player.setPosition(55, 108);
		guard.logic();
		assertFalse(guard.isAttacking());
		player.setPosition(44, 108);
		guard.setTarget(player);
		player.setQuest(Step.KEEP_GUARDIAN.slot, "report");
		guard.logic();
		assertFalse(guard.isAttacking());
		Player archer = new Player(new RPObject(other)) {
			@Override
			public boolean canDoRangeAttack(RPEntity target, int range) {
				return true;
			}
		};
		keep.add(archer);
		archer.setPosition(39, 108);
		guard.rememberAttacker(archer);
		guard.setIdle();
		guard.setIdle();
		assertEquals(45, guard.getX());
		assertEquals(108, guard.getY());
		assertEquals(0.0, guard.getSpeed(), 0.0);
		assertTrue(guard.getNewInstance() instanceof AdosKeepGuardian);
		assertEquals(1050, guard.getBaseHP());
		assertEquals(262, guard.getBaseArmorHP());
	}

	@Test
	public void fullBagKeepsTheFinalRewardAvailableAfterRebirth() {
		ready(75);
		player.setQuest(Step.KEEP_GUARDIAN.slot, "done");
		player.setQuest(Step.KEEP_RETURN.slot, "ready");
		while (player.equip("bag",
				SingletonRepository.getEntityManager().getItem("miecz"))) {
		}
		int xp = player.getXP();
		visit("Julius");
		say("Julius", "droga do Ados");
		assertEquals("ready", player.getQuest(Step.KEEP_RETURN.slot));
		assertEquals(xp, player.getXP());
		RPObject first = player.getSlot("bag").iterator().next();
		player.getSlot("bag").remove(first.getID());
		player.setXP(0);
		player.setLevel(0);
		say("Julius", "droga do Ados");
		assertTrue(player.isQuestCompleted(Step.KEEP_RETURN.slot));
		assertEquals(1, player.getTotalNumberOf("hełm chaosu"));
	}

	@Test
	public void existingNpcQuestsKeepTheirOwnYesAndNoTransitions() {
		for (int i = 4; i < NAMES.length; i++) {
			SpeakerNPC npc = new SpeakerNPC(NAMES[i]);
			npc.addGreeting();
			library.add(npc);
		}
		system.loadQuest(new EmotionCrystals());
		system.loadQuest(new KillDhohrNuggetcutter());
		ready(75);
		visit("Julius");
		say("Julius", "task");
		say("Julius", "tak");
		assertTrue(player.isQuestInState("emotion_crystals", 0, "start"));
		assertFalse(player.hasQuest(Step.KEEP_TRAIL.slot));
		say("Julius", "stara warownia");
		say("Julius", "tak");
		assertEquals("clues;0", player.getQuest(Step.KEEP_TRAIL.slot));
		player.setQuest(Step.KEEP_INSCRIPTIONS.slot, "done");
		visit("Zogfang");
		say("Zogfang", "task");
		say("Zogfang", "tak");
		assertTrue(player.hasQuest("kill_dhohr_nuggetcutter"));
		assertFalse(player.hasQuest(Step.KEEP_GUARDIAN.slot));
		say("Zogfang", "kamienny strażnik");
		say("Zogfang", "nie");
		assertFalse(player.hasQuest(Step.KEEP_GUARDIAN.slot));
		say("Zogfang", "kamienny strażnik");
		say("Zogfang", "tak");
		assertEquals("fight", player.getQuest(Step.KEEP_GUARDIAN.slot));
	}

	@Test
	public void chapterFitsExistingMapsWithoutTheHigherLevelAreas()
			throws Exception {
		for (String zone : new String[] { AdosKeepProgress.OUTSIDE,
				AdosKeepProgress.KEEP }) {
			String file = zone.equals(AdosKeepProgress.KEEP)
					? "data/maps/Level -1/ados/abandoned_keep.tmx"
					: "data/maps/Level 0/ados/mountain_n2_w.tmx";
			LayerDefinition collision = TMXLoader.load(file).getLayer(
					"collision");
			collision.build();
			LayerDefinition roof = TMXLoader.load(file).getLayer("3_roof");
			roof.build();
			Set<Integer> seen = new HashSet<>();
			Queue<Integer> queue = new ArrayDeque<>();
			int start = zone.equals(AdosKeepProgress.KEEP) ? 126 * 128 + 13
					: 125 * 128 + 126;
			seen.add(start);
			queue.add(start);
			while (!queue.isEmpty()) {
				int p = queue.remove();
				for (int[] d : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 },
						{ 0, -1 } }) {
					int x = p % 128 + d[0], y = p / 128 + d[1], n = y * 128 + x;
					boolean danger = zone.equals(AdosKeepProgress.KEEP)
							? Math.max(Math.abs(x - 118),
									Math.abs(y - 35)) <= 12
									|| Math.max(Math.abs(x - 123),
											Math.abs(y - 103)) <= 12
							: y < 58;
					if (x >= 0 && x < 128 && y >= 0 && y < 128 && !danger
							&& collision.getTileAt(x, y) == 0 && seen.add(n))
						queue.add(n);
				}
			}
			List<int[]> points = new ArrayList<>();
			for (Kind kind : Kind.values())
				if (kind.zone.equals(zone))
					points.add(new int[] { kind.x, kind.y });
			if (zone.equals(AdosKeepProgress.KEEP)) {
				points.add(new int[] { 45, 108 });
				points.add(new int[] { 10, 107 });
				points.add(new int[] { 106, 5 });
			} else
				points.add(new int[] { 13, 126 });
			for (int[] p : points) {
				assertTrue(file + " unreachable " + p[0] + "," + p[1],
						seen.contains(p[1] * 128 + p[0]));
				assertEquals(0, roof.getTileAt(p[0], p[1]));
				assertEquals(0, collision.getTileAt(p[0], p[1] + 1));
			}
		}
	}
}
