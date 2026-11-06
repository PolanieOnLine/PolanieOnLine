package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
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
import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.trail.MineTrailEvidence.Kind;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import games.stendhal.server.maps.semos.bank.CustomerAdvisorNPC;
import games.stendhal.server.maps.semos.library.HistorianGeographerNPC;
import games.stendhal.server.maps.semos.mines.DwarfGuardianNPC;
import games.stendhal.server.maps.semos.mines.MinerNPC;
import games.stendhal.server.maps.semos.townhall.LeaderNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class SemosMineStoryTest {
	private static final String[] NAMES = { "Lieutenant Drilenun", "Dagobert",
			"Barbarus", "Phalk", "Zynn Iwuhos" };
	private List<IQuest> loaded, oldQuests;
	private final Map<String, SpeakerNPC> oldNpcs = new HashMap<>();
	private final List<StendhalRPZone> zones = new ArrayList<>();
	private final StendhalQuestSystem system = StendhalQuestSystem.get();
	private StendhalRPZone townhall, mine, library, bank;
	private Player player;

	@BeforeClass
	public static void init() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		Field field = StendhalQuestSystem.class.getDeclaredField("quests");
		field.setAccessible(true);
		loaded = (List<IQuest>) field.get(null);
		oldQuests = new ArrayList<>(loaded);
		loaded.clear();
		for (String name : NAMES)
			oldNpcs.put(name, NPCList.get().remove(name));
		townhall = zone("int_semos_townhall");
		mine = zone(MineTrailProgress.ZONE);
		library = zone("int_semos_library");
		bank = zone("int_semos_bank");
		new LeaderNPC().configureZone(townhall, null);
		new MinerNPC().configureZone(mine, null);
		new DwarfGuardianNPC().configureZone(mine, null);
		new HistorianGeographerNPC().configureZone(library, null);
		new CustomerAdvisorNPC().configureZone(bank, null);
		new MineTrailSites().configureZone(mine, null);
		system.loadQuest(new LetterFromZakopaneQuest());
		system.loadQuest(new RadanDungeonQuest(Step.RADAN_PASSAGE));
		for (Step step : Step.values())
			if (step.chapter.equals(TrailQuestLine.MINE_CHAPTER))
				system.loadQuest(new MineTrailQuest(step));
		player = PlayerTestHelper.createPlayer("mine-story");
		townhall.add(player);
		MockStendhalRPRuleProcessor.get().addPlayer(player);
	}

	private StendhalRPZone zone(String name) {
		StendhalRPZone zone = new StendhalRPZone(name, 128, 128);
		zones.add(zone);
		MockStendlRPWorld.get().addRPZone(zone);
		return zone;
	}

	@After
	public void tearDown() {
		loaded.clear();
		loaded.addAll(oldQuests);
		MockStendhalRPRuleProcessor.get().clearPlayers();
		for (String name : NAMES) {
			NPCList.get().remove(name);
			if (oldNpcs.get(name) != null)
				NPCList.get().add(oldNpcs.get(name));
		}
		for (StendhalRPZone zone : zones)
			MockStendlRPWorld.get().removeZone(zone);
	}

	private String say(String name, String text) {
		SpeakerNPC npc = NPCList.get().get(name);
		assertTrue(name + ": " + text, npc.getEngine().step(player, text));
		return getReply(npc);
	}

	private void visit(String name) {
		NPCList.get().get(name).getEngine().setCurrentState(
				ConversationStates.IDLE);
		say(name, "hi");
	}

	private void accept(String name, String topic) {
		visit(name);
		say(name, topic);
		say(name, "tak");
	}

	private void ready(int level) {
		for (Step step : Step.values()) {
			if (step == Step.MINE_LEDGER)
				break;
			player.setQuest(step.slot, "done");
		}
		player.setXP(Level.getXP(level));
		player.setLevel(level);
	}

	private void move(StendhalRPZone zone) {
		if (player.getZone() != zone) {
			player.getZone().remove(player);
			zone.add(player);
		}
	}

	private MineTrailEvidence evidence(Kind kind) {
		for (Entity entity : mine.getEntitiesOfClass(MineTrailEvidence.class)) {
			MineTrailEvidence prop = (MineTrailEvidence) entity;
			if (prop.kind == kind)
				return prop;
		}
		throw new AssertionError(kind);
	}

	private boolean use(Kind kind) {
		move(mine);
		MineTrailEvidence prop = evidence(kind);
		player.setPosition(prop.getX() + (kind == Kind.VOICES ? 1 : -1),
				prop.getY());
		return prop.onUsed(player);
	}

	private void ledger() {
		ready(30);
		accept("Lieutenant Drilenun", "nocnej zmianie");
		visit("Barbarus");
		say("Barbarus", "nocnej zmianie");
		assertTrue(use(Kind.ROSTER));
		say("Barbarus", "nocna zmiana");
	}

	private void tracks() {
		ledger();
		accept("Barbarus", "ślady nocnej zmiany");
		assertTrue(use(Kind.RATIONS));
		assertTrue(use(Kind.BOOTS));
		say("Barbarus", "ślady nocnej zmiany");
	}

	private void voices() {
		tracks();
		accept("Barbarus", "głosach spod skały");
		assertTrue(use(Kind.VOICES));
		visit("Zynn Iwuhos");
		say("Zynn Iwuhos", "rozkazy spod ziemi");
	}

	private Creature courier(int x, int y) {
		Creature creature = SingletonRepository.getEntityManager().getCreature(
				MineTrailProgress.COURIER);
		creature.setPosition(x, y);
		mine.add(creature);
		return creature;
	}

	@Test
	public void levelThirtyAndTheRadanEndingAreBothRequired() {
		player.setXP(Level.getXP(30));
		player.setLevel(30);
		visit("Lieutenant Drilenun");
		say("Lieutenant Drilenun", "nocna zmiana");
		assertFalse(player.hasQuest(Step.MINE_LEDGER.slot));
		ready(29);
		assertFalse(Step.MINE_LEDGER.available(player));
		assertTrue(system.getMainQuests(player).isEmpty());
		String objective = TrailQuestLine.objective(player,
				Step.MINE_LEDGER.slot);
		assertTrue(objective, objective.contains("30"));
		visit("Lieutenant Drilenun");
		String reply = say("Lieutenant Drilenun", "nocna zmiana");
		assertFalse(reply, reply.contains("30"));
		assertFalse(reply, reply.contains("Powiedz:"));
		assertNotEquals(objective, reply);
		assertFalse(player.hasQuest(Step.MINE_LEDGER.slot));
		ready(30);
		visit("Lieutenant Drilenun");
		say("Lieutenant Drilenun", "nocna zmiana");
		say("Lieutenant Drilenun", "nie");
		assertFalse(player.hasQuest(Step.MINE_LEDGER.slot));
		say("Lieutenant Drilenun", "nocna zmiana");
		say("Lieutenant Drilenun", "tak");
		assertEquals("barbarus", player.getQuest(Step.MINE_LEDGER.slot));
		assertEquals(1, system.getMainQuests(player).size());
		IQuest quest = system.getQuest(player, Step.MINE_LEDGER.title);
		assertEquals(30, quest.getMinLevel());
		assertEquals(Region.SEMOS_MINES, quest.getRegion());
	}

	@Test
	public void remindersBelongToTheSpeakerAndDoNotAdvanceTheInvestigation() {
		ready(30);
		accept("Lieutenant Drilenun", "nocna zmiana");
		visit("Barbarus");
		say("Barbarus", "nocna zmiana");
		String minerReply = say("Barbarus", "nocna zmiana");
		assertFalse(minerReply, minerReply.contains("Barbarusa"));
		assertNotEquals(TrailQuestLine.objective(player, Step.MINE_LEDGER.slot),
				minerReply);
		visit("Lieutenant Drilenun");
		String officerReply = say("Lieutenant Drilenun", "nocna zmiana");
		assertNotEquals(minerReply, officerReply);
		assertTrue(officerReply, officerReply.contains("Barbarus"));
		assertEquals("register", player.getQuest(Step.MINE_LEDGER.slot));
		assertEquals(0, player.getTotalNumberOf("money"));
		player.setQuest(Step.MINE_LEDGER.slot, "done");
		visit("Zynn Iwuhos");
		String historianReply = say("Zynn Iwuhos", "rozkazy spod ziemi");
		assertTrue(historianReply, historianReply.contains("Barbarus"));
		assertFalse(historianReply, historianReply.contains("słyszałem"));
		assertFalse(player.hasQuest(Step.MINE_VOICES.slot));
	}

	@Test
	public void existingDrilenunOffersStillAcceptTheirOwnQuest() {
		player.setQuest(Step.DEBT.slot, "done");
		accept("Lieutenant Drilenun", "list");
		assertEquals("bank", player.getQuest(Step.SEMOS_LETTER.slot));
		assertFalse(player.hasQuest(Step.MINE_LEDGER.slot));
		player.setQuest(Step.SEMOS_PICKUP.slot, "done");
		accept("Lieutenant Drilenun", "chodnik");
		assertEquals("search", player.getQuest(Step.RADAN_PASSAGE.slot));
	}

	@Test
	public void cluesCannotSkipTheStoryAndBothTracksSurviveReload() {
		ready(30);
		accept("Lieutenant Drilenun", "nocna zmiana");
		assertFalse(use(Kind.ROSTER));
		assertFalse(use(Kind.VOICES));
		assertFalse(use(Kind.DISPATCH));
		visit("Zynn Iwuhos");
		say("Zynn Iwuhos", "rozkazy spod ziemi");
		assertFalse(player.hasQuest(Step.MINE_VOICES.slot));
		visit("Barbarus");
		say("Barbarus", "nocna zmiana");
		assertTrue(use(Kind.ROSTER));
		say("Barbarus", "nocna zmiana");
		accept("Barbarus", "ślady nocnej zmiany");
		assertTrue(use(Kind.BOOTS));
		assertFalse(use(Kind.BOOTS));
		assertEquals("clues;2", player.getQuest(Step.MINE_TRACKS.slot));
		Player restored = new Player(player);
		player.getZone().remove(player);
		mine.add(restored);
		player = restored;
		assertTrue(TrailQuestLine.objective(player,
				Step.MINE_TRACKS.slot).contains("skrzynkę"));
		assertTrue(use(Kind.RATIONS));
		assertEquals("report", player.getQuest(Step.MINE_TRACKS.slot));
		say("Barbarus", "ślady nocnej zmiany");
		assertTrue(player.isQuestCompleted(Step.MINE_TRACKS.slot));
	}

	@Test
	public void fullStoryRequiresTheCourierAndRewardsOnce() {
		voices();
		assertTrue(player.isQuestCompleted(Step.MINE_VOICES.slot));
		accept("Phalk", "łącznika");
		assertFalse(use(Kind.DISPATCH));
		visit("Phalk");
		say("Phalk", "rozkaz dla straży");
		assertEquals("guard", player.getQuest(Step.MINE_DISPATCH.slot));
		move(mine);
		player.setPosition(111, 32);
		Creature courier = courier(112, 32);
		courier.setRespawnPoint(
				new CreatureRespawnPoint(mine, 112, 32, courier, 1));
		courier.onDamaged(player, courier.getHP() + courier.getArmorHP());
		courier.onDead(player);
		assertEquals("orders", player.getQuest(Step.MINE_DISPATCH.slot));
		assertTrue(use(Kind.DISPATCH));
		visit("Lieutenant Drilenun");
		say("Lieutenant Drilenun", "druga zmiana");
		assertEquals("identified", player.getQuest(Step.MINE_DISPATCH.slot));
		visit("Phalk");
		say("Phalk", "rozkaz dla straży");
		assertEquals("return", player.getQuest(Step.MINE_DISPATCH.slot));
		int xp = player.getXP(), coins = player.getTotalNumberOf("money");
		visit("Lieutenant Drilenun");
		say("Lieutenant Drilenun", "druga zmiana");
		assertTrue(player.isQuestCompleted(Step.MINE_DISPATCH.slot));
		assertEquals(xp + 10000, player.getXP());
		assertEquals(coins + 200, player.getTotalNumberOf("money"));
		assertEquals(5, player.getTotalNumberOf("duży eliksir"));
		xp = player.getXP();
		coins = player.getTotalNumberOf("money");
		say("Lieutenant Drilenun", "druga zmiana");
		visit("Phalk");
		say("Phalk", "rozkaz dla straży");
		assertEquals(xp, player.getXP());
		assertEquals(coins, player.getTotalNumberOf("money"));
		assertEquals(5, player.getTotalNumberOf("duży eliksir"));
		assertTrue(system.getMainQuests(player).isEmpty());
	}

	@Test
	public void evidenceIsPersonalAndRequiresRangeAndSameZone() {
		ledger();
		accept("Barbarus", "ślady nocnej zmiany");
		MineTrailEvidence prop = evidence(Kind.BOOTS);
		move(mine);
		player.setPosition(1, 1);
		assertFalse(prop.onUsed(player));
		player.setPosition(prop.getX() - 1, prop.getY());
		assertTrue(prop.onUsed(player));
		Player other = PlayerTestHelper.createPlayer("other-miner");
		other.setQuest(Step.MINE_LEDGER.slot, "done");
		other.setQuest(Step.MINE_TRACKS.slot, "clues;0");
		townhall.add(other);
		other.setPosition(prop.getX() - 1, prop.getY());
		assertFalse(prop.onUsed(other));
		townhall.remove(other);
		mine.add(other);
		assertTrue(prop.onUsed(other));
		assertSame(mine, prop.getZone());
		assertEquals("clues;2", other.getQuest(Step.MINE_TRACKS.slot));
		assertEquals("clues;2", player.getQuest(Step.MINE_TRACKS.slot));
	}

	@Test
	public void summonedRemoteAndWrongSpawnCouriersDoNotCount() {
		ready(30);
		player.setQuest(Step.MINE_VOICES.slot, "done");
		accept("Phalk", "łącznik");
		move(mine);
		player.setPosition(111, 32);
		Creature courier = courier(112, 32);
		TrailQuestLine.recordGuardKill(player, courier);
		assertEquals("guard", player.getQuest(Step.MINE_DISPATCH.slot));
		courier.setRespawnPoint(
				new CreatureRespawnPoint(mine, 10, 10, courier, 1));
		TrailQuestLine.recordGuardKill(player, courier);
		assertEquals("guard", player.getQuest(Step.MINE_DISPATCH.slot));
		courier.setRespawnPoint(
				new CreatureRespawnPoint(mine, 112, 32, courier, 1));
		player.setPosition(1, 1);
		TrailQuestLine.recordGuardKill(player, courier);
		assertEquals("guard", player.getQuest(Step.MINE_DISPATCH.slot));
		move(townhall);
		player.setPosition(111, 32);
		TrailQuestLine.recordGuardKill(player, courier);
		assertEquals("guard", player.getQuest(Step.MINE_DISPATCH.slot));
		move(mine);
		player.setPosition(111, 32);
		courier.setPosition(115, 34);
		TrailQuestLine.recordGuardKill(player, courier);
		assertEquals("orders", player.getQuest(Step.MINE_DISPATCH.slot));
	}

	@Test
	public void investigationFitsTheRealMineBeforeTheHighLevelBlockade()
			throws Exception {
		LayerDefinition collision = TMXLoader.load(
				"data/maps/Level -1/semos/mine_n2.tmx").getLayer("collision");
		collision.build();
		Set<Integer> reachable = new HashSet<>();
		Queue<Integer> queue = new ArrayDeque<>();
		queue.add(78 * 128 + 57);
		reachable.add(78 * 128 + 57);
		while (!queue.isEmpty()) {
			int point = queue.remove();
			for (int[] d : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 },
					{ 0, -1 } }) {
				int x = point % 128 + d[0], y = point / 128 + d[1],
						next = y * 128 + x;
				if (x >= 0 && x < 121 && y >= 0 && y < 128
						&& collision.getTileAt(x, y) == 0
						&& reachable.add(next))
					queue.add(next);
			}
		}
		for (Kind kind : Kind.values()) {
			assertTrue(kind.name(), reachable.contains(kind.y * 128 + kind.x));
			boolean approach = false;
			for (int[] d : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 },
					{ 0, -1 } })
				approach |= reachable.contains(
						(kind.y + d[1]) * 128 + kind.x + d[0]);
			assertTrue(kind.name(), approach);
		}
		assertTrue(
				"Shaft " + Kind.VOICES.x + "," + Kind.VOICES.y
						+ " must touch rock: "
						+ collision.getTileAt(Kind.VOICES.x - 1, Kind.VOICES.y),
				collision.getTileAt(Kind.VOICES.x - 1, Kind.VOICES.y) > 0);
		assertTrue(reachable.contains(26 * 128 + 118));
		assertTrue(reachable.contains(32 * 128 + 112));
		Creature courier = SingletonRepository.getEntityManager().getCreature(
				MineTrailProgress.COURIER);
		assertEquals(30, courier.getLevel());
		assertEquals(240, courier.getBaseHP());
		assertEquals(60, courier.getBaseArmorHP());
		assertEquals(1, mine.getRespawnPointList().size());
		assertEquals(MineTrailProgress.COURIER, mine.getRespawnPointList().get(
				0).getPrototypeCreature().getName());
	}
}
