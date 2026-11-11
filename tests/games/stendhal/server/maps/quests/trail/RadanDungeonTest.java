package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;
import java.lang.reflect.Field;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.QuestMarker;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.creature.Cat;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.mapstuff.portal.Teleporter;
import games.stendhal.server.entity.mapstuff.sound.LoopedSoundSource;
import games.stendhal.server.core.config.zone.EntitySetupDescriptor;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import games.stendhal.server.maps.semos.townhall.LeaderNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import marauroa.common.game.RPObject;

public class RadanDungeonTest {
	private List<IQuest> loaded, oldQuests;
	private SpeakerNPC oldLeader, oldBanker;
	private StendhalRPZone outside, template, townhall;
	private Player player;
	private final StendhalQuestSystem system = StendhalQuestSystem.get();

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
		oldLeader = NPCList.get().remove("Lieutenant Drilenun");
		oldBanker = NPCList.get().remove("Dagobert");
		outside = new StendhalRPZone(RadanDungeonSite.PUBLIC_ZONE, 64, 64);
		townhall = new StendhalRPZone("int_semos_townhall", 40, 40);
		template = new StendhalRPZone(RadanDungeonFactory.TEMPLATE, 40, 28);
		StendhalMapStructure map = TMXLoader.load(
				"data/maps/interiors/semos/radan_hideout.tmx");
		map.build();
		template.addTilesets(template.getName() + ".tilesets",
				map.getTilesets());
		for (String name : new String[] { "0_floor", "1_terrain", "2_object",
				"3_roof" })
			template.addLayer(template.getName() + "." + name,
					map.getLayer(name));
		template.addCollisionLayer(template.getName() + ".collision",
				map.getLayer("collision"));
		template.addProtectionLayer(template.getName() + ".protection",
				map.getLayer("protection"));
		MockStendlRPWorld.get().addRPZone(outside);
		MockStendlRPWorld.get().addRPZone(townhall);
		MockStendlRPWorld.get().addRPZone(template);
		new LeaderNPC().configureZone(townhall, null);
		townhall.add(new SpeakerNPC("Dagobert") {
			@Override
			protected void createDialog() {
			}
		});
		new RadanDungeonSite().configureZone(outside, null);
		// Existing quest shares Drilenun: the new consent state must not steal
		// its YES.
		system.loadQuest(new LetterFromZakopaneQuest());
		for (Step step : Step.values())
			if (step.chapter.equals(TrailQuestLine.DUNGEON_CHAPTER))
				system.loadQuest(new RadanDungeonQuest(step));
		player = PlayerTestHelper.createPlayer("radan-story");
		outside.add(player);
		MockStendhalRPRuleProcessor.get().addPlayer(player);
	}

	@Test
	public void automaticFinaleDoesNotAdvertiseASeparateQuestOffer() {
		player.setLevel(30);
		player.setQuest(Step.RADAN_TRANSPORT.slot, "done");
		assertTrue(Step.RADAN_FINALE.available(player));
		assertEquals(QuestMarker.NONE,
				new RadanDungeonQuest(Step.RADAN_FINALE).getNPCQuestMarker(player,
						NPCList.get().get("Lieutenant Drilenun")));
		assertFalse(player.hasQuest(Step.RADAN_FINALE.slot));
	}

	@After
	public void tearDown() {
		if (player != null && player.getZone() != null
				&& player.getZone() != outside && player.getZone() != townhall)
			player.getZone().remove(player);
		loaded.clear();
		loaded.addAll(oldQuests);
		MockStendhalRPRuleProcessor.get().clearPlayers();
		NPCList.get().remove("Lieutenant Drilenun");
		if (oldLeader != null)
			NPCList.get().add(oldLeader);
		NPCList.get().remove("Dagobert");
		if (oldBanker != null)
			NPCList.get().add(oldBanker);
		MockStendlRPWorld.get().removeZone(template);
		MockStendlRPWorld.get().removeZone(outside);
		MockStendlRPWorld.get().removeZone(townhall);
	}

	private String say(SpeakerNPC npc, String words) {
		assertTrue(words, npc.getEngine().step(player, words));
		return getReply(npc);
	}

	private SpeakerNPC leader() {
		return NPCList.get().get("Lieutenant Drilenun");
	}

	private void accept() {
		player.setQuest(Step.SEMOS_PICKUP.slot, "done");
		leader().getEngine().setCurrentState(ConversationStates.IDLE);
		say(leader(), "hi");
		say(leader(), "chodnik");
		say(leader(), "tak");
		assertEquals("search", player.getQuest(Step.RADAN_PASSAGE.slot));
	}

	private RadanDungeonSite.PublicProp prop(boolean entrance) {
		RadanDungeonSite.PublicProp prop = new RadanDungeonSite.PublicProp(entrance);
		PlayerPrivateScene.assign(prop, player);
		prop.setPosition(entrance ? RadanDungeonSite.ENTRANCE_X
				: RadanDungeonSite.ENTRANCE_X - 1, RadanDungeonSite.ENTRANCE_Y);
		outside.add(prop);
		return prop;
	}

	private StendhalRPZone enter() {
		player.setPosition(RadanDungeonSite.ENTRANCE_X, RadanDungeonSite.ENTRANCE_Y - 1);
		assertTrue(prop(true).onUsed(player));
		return player.getZone();
	}

	private void start() {
		accept();
		player.setPosition(RadanDungeonSite.ENTRANCE_X - 1, RadanDungeonSite.ENTRANCE_Y - 1);
		assertTrue(prop(false).onUsed(player));
		enter();
	}

	private RadanDungeonScene.Fighter fighter(int role) {
		for (RadanDungeonScene.Fighter fighter : typed(player.getZone(),
				RadanDungeonScene.Fighter.class))
			if (fighter.role == role)
				return fighter;
		throw new AssertionError("Missing fighter " + role);
	}

	private void defeat(int role) {
		RadanDungeonScene.Fighter fighter = fighter(role);
		fighter.onDamaged(player, fighter.getHP());
		fighter.onDead(player, true);
	}

	private SpeakerNPC actor(boolean radan) {
		for (RadanDungeonScene.Actor actor : typed(player.getZone(),
				RadanDungeonScene.Actor.class))
			if (actor.radan == radan)
				return actor;
		throw new AssertionError("Missing actor");
	}

	private void talk(boolean radan, String topic) {
		SpeakerNPC npc = actor(radan);
		player.setPosition(npc.getX() - 1, npc.getY());
		npc.getEngine().setCurrentState(ConversationStates.IDLE);
		say(npc, "hi");
		say(npc, topic);
	}

	private boolean mechanism(boolean chain) {
		for (RadanDungeonScene.Mechanism prop : typed(player.getZone(),
				RadanDungeonScene.Mechanism.class)) {
			if (prop.chain == chain) {
				player.setPosition(prop.getX(), prop.getY() + 1);
				return prop.onUsed(player);
			}
		}
		throw new AssertionError("Missing mechanism");
	}

	private void rescue() {
		defeat(1);
		defeat(2);
		talk(false, "więzy");
	}

	@Test
	public void consentClueAndPrecedingStoryAreRequired() {
		player.setPosition(RadanDungeonSite.ENTRANCE_X, RadanDungeonSite.ENTRANCE_Y - 1);
		assertFalse(prop(true).onUsed(player));
		say(leader(), "hi");
		say(leader(), "chodnik");
		assertFalse(player.hasQuest(Step.RADAN_PASSAGE.slot));
		accept();
		assertFalse(prop(true).onUsed(player));
		player.setPosition(1, 1);
		assertFalse(prop(false).onUsed(player));
		player.setPosition(RadanDungeonSite.ENTRANCE_X - 1, RadanDungeonSite.ENTRANCE_Y - 1);
		assertTrue(prop(false).onUsed(player));
		assertNotNull(TrailQuestLine.objective(player, "radan_passage"));
		StendhalRPZone zone = enter();
		assertNotSame(outside, zone);
		assertTrue(player.isQuestCompleted(Step.RADAN_PASSAGE.slot));
		assertEquals("guards;0", player.getQuest(Step.RADAN_RESCUE.slot));
		assertEquals(1, system.getMainQuests(player).size());
	}

	@Test
	public void fullExpeditionOpensGatesAndRewardsOnlyOnce() {
		start();
		StendhalRPZone zone = player.getZone();
		assertTrue(zone.collides(player, 17, 9));
		talk(false, "więzy");
		assertFalse(player.hasQuest(Step.RADAN_TRANSPORT.slot));
		rescue();
		assertFalse(zone.collides(player, 17, 9));
		assertTrue(zone.collides(player, 29, 21));
		int xp = player.getXP();
		talk(false, "więzy");
		assertEquals(xp, player.getXP());
		assertFalse(mechanism(true));
		assertEquals("brake", player.getQuest(Step.RADAN_TRANSPORT.slot));
		assertTrue(mechanism(false));
		assertFalse(mechanism(false));
		assertTrue(mechanism(true));
		assertFalse(zone.collides(player, 29, 21));
		assertFalse(mechanism(true));
		talk(true, "transport");
		assertEquals("guard", player.getQuest(Step.RADAN_FINALE.slot));
		RadanDungeonScene.Fighter boss = fighter(4);
		assertEquals(300, boss.getBaseHP());
		assertEquals(75, boss.getBaseArmorHP());
		defeat(4);
		talk(true, "transport");
		assertEquals("return", player.getQuest(Step.RADAN_FINALE.slot));
		leader().getEngine().setCurrentState(ConversationStates.IDLE);
		say(leader(), "hi");
		say(leader(), "Radan");
		assertTrue(player.isQuestCompleted(Step.RADAN_FINALE.slot));
		Item boots = player.getFirstEquipped("buty żelazne");
		assertNotNull(boots);
		assertEquals(ItemRarity.RARE, boots.getRarity());
		assertEquals(player.getName(), boots.getBoundTo());
		assertTrue(ItemAffixState.hasAny(boots));
		xp = player.getXP();
		int coins = player.getTotalNumberOf("money");
		say(leader(), "Radan");
		assertEquals(xp, player.getXP());
		assertEquals(coins, player.getTotalNumberOf("money"));
		assertEquals(150, coins);
		assertEquals(9803, xp);
		assertEquals(1, player.getTotalNumberOf("buty żelazne"));
	}

	@Test
	public void logoutReconstructsEachCheckpointAndCleansPrivateNpcs() {
		start();
		defeat(1);
		StendhalRPZone first = player.getZone();
		first.remove(player);
		assertEquals(RadanDungeonSite.PUBLIC_ZONE, player.get("zoneid"));
		assertNull(MockStendlRPWorld.get().getZone(first.getName()));
		assertNull(NPCList.get().get("Bartek prywatny " + player.getName()));
		player = new Player(player);
		outside.add(player);
		enter();
		assertEquals(1, typed(player.getZone(),
				RadanDungeonScene.Fighter.class).size());
		assertEquals("guards;1", player.getQuest(Step.RADAN_RESCUE.slot));
		defeat(2);
		talk(false, "więzy");
		mechanism(false);
		first = player.getZone();
		first.remove(player);
		outside.add(player);
		enter();
		assertEquals("chain", player.getQuest(Step.RADAN_TRANSPORT.slot));
		assertFalse(player.getZone().collides(player, 17, 9));
		assertTrue(mechanism(true));
		defeat(4);
		first = player.getZone();
		first.remove(player);
		outside.add(player);
		enter();
		assertTrue(typed(player.getZone(),
				RadanDungeonScene.Fighter.class).isEmpty());
		talk(true, "transport");
		assertEquals("return", player.getQuest(Step.RADAN_FINALE.slot));
		assertEquals(4803, player.getXP());
	}

	@Test
	public void foreignPlayersCannotEnterOrUseAnotherScene() throws Exception {
		start();
		StendhalRPZone zone = player.getZone();
		Player intruder = PlayerTestHelper.createPlayer("intruder");
		outside.add(intruder);
		intruder.setPosition(RadanDungeonSite.ENTRANCE_X, RadanDungeonSite.ENTRANCE_Y - 1);
		assertTrue(zone.collides(intruder, 6, 23));
		RadanDungeonScene.Mechanism prop = typed(zone,
				RadanDungeonScene.Mechanism.class).get(0);
		intruder.setPosition(prop.getX(), prop.getY() + 1);
		assertFalse(prop.onUsed(intruder));
		zone.remove(player);
		outside.add(player);
		player.setPosition(prop.getX(), prop.getY() + 1);
		assertFalse(prop.onUsed(player));
		assertEquals("guards;0", player.getQuest(Step.RADAN_RESCUE.slot));
	}

	@Test
	public void fullBagKeepsTheRewardAvailable() {
		player.setQuest(Step.RADAN_TRANSPORT.slot, "done");
		player.setQuest(Step.RADAN_FINALE.slot, "return");
		while (player.equip("bag",
				SingletonRepository.getEntityManager().getItem("miecz"))) {
		}
		say(leader(), "hi");
		say(leader(), "Radan");
		assertEquals("return", player.getQuest(Step.RADAN_FINALE.slot));
		assertEquals(0, player.getXP());
		assertEquals(0, player.getTotalNumberOf("buty żelazne"));
		RPObject item = player.getSlot("bag").iterator().next();
		player.getSlot("bag").remove(item.getID());
		say(leader(), "Radan");
		assertTrue(player.isQuestCompleted(Step.RADAN_FINALE.slot));
		assertEquals(1, player.getTotalNumberOf("buty żelazne"));
	}

	@Test
	public void closingTheInstancePreservesDeathDropsAndAbandonedAnimals()
			throws Exception {
		start();
		StendhalRPZone zone = player.getZone();
		Corpse corpse = new Corpse(player, "zbrojny Radana");
		Item sword = SingletonRepository.getEntityManager().getItem("miecz");
		corpse.add(sword);
		zone.add(corpse);
		Item loose = SingletonRepository.getEntityManager().getItem("polano");
		loose.setPosition(6, 22);
		zone.add(loose);
		Cat abandoned = new Cat();
		abandoned.setPosition(7, 22);
		zone.add(abandoned);
		Field listenerField = Corpse.class.getDeclaredField("corpseDegradator");
		listenerField.setAccessible(true);
		TurnListener decay = (TurnListener) listenerField.get(corpse);
		int remaining = SingletonRepository.getTurnNotifier().getRemainingTurns(
				decay);
		// A crowded return map must never cause releaseMember() to discard
		// belongings.
		AreaEntity crowded = new AreaEntity(64, 64) {
			@Override
			public boolean isObstacle(Entity entity) {
				return true;
			}
		};
		outside.add(crowded);
		zone.remove(player);
		outside.add(player);
		assertSame(outside, corpse.getZone());
		assertSame(sword, corpse.getSlot("content").iterator().next());
		assertSame(outside, loose.getZone());
		assertSame(outside, abandoned.getZone());
		assertEquals(remaining,
				SingletonRepository.getTurnNotifier().getRemainingTurns(decay));
		assertNull(MockStendlRPWorld.get().getZone(zone.getName()));
	}

	@Test
	public void normalExitKeepsTheOwnedPetAndWebMapIdentity() {
		start();
		StendhalRPZone zone = player.getZone();
		Cat cat = new Cat(player);
		cat.setPosition(7, 23);
		assertEquals(zone.getName(), zone.getAttributes().get("runtime_zone"));
		player.setPosition(6, 23);
		Teleporter exit = typed(zone, Teleporter.class).get(0);
		assertTrue(exit.onUsed(player));
		assertSame(outside, player.getZone());
		assertSame(outside, cat.getZone());
		assertSame(cat, player.getPet());
	}

	@Test
	public void actualPlayerDeathLeavesARecoverableCorpseAndAnExitCheckpoint() {
		start();
		StendhalRPZone zone = player.getZone();
		StendhalRPZone afterlife = new StendhalRPZone("int_afterlife", 10, 10);
		afterlife.setEntryPoint(2, 2);
		MockStendlRPWorld.get().addRPZone(afterlife);
		try {
			Cat cat = new Cat(player);
			cat.setPosition(7, 23);
			player.onDead(fighter(1), false);
			assertSame(afterlife, player.getZone());
			assertSame(outside, cat.getZone());
			assertEquals(1, typed(outside, Corpse.class).size());
			assertEquals("guards;0", player.getQuest(Step.RADAN_RESCUE.slot));
			assertNull(MockStendlRPWorld.get().getZone(zone.getName()));
			afterlife.remove(player);
			outside.add(player);
			enter();
			assertEquals(2, typed(player.getZone(),
					RadanDungeonScene.Fighter.class).size());
		} finally {
			MockStendlRPWorld.get().removeZone(afterlife);
		}
	}

	@Test
	public void privateMapInheritsXmlFireSoundsAndDarkness() throws Exception {
		NodeList zones = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
				new File(
						"data/conf/zones/stendhal/semos.xml")).getElementsByTagName(
								"zone");
		Element config = null;
		for (int i = 0; i < zones.getLength(); i++) {
			Element candidate = (Element) zones.item(i);
			if (RadanDungeonFactory.TEMPLATE.equals(
					candidate.getAttribute("name")))
				config = candidate;
		}
		assertNotNull(config);
		NodeList attributes = config.getElementsByTagName("attributes");
		assertEquals("Missing darkness configuration", 1,
				attributes.getLength());
		NodeList params = ((Element) attributes.item(0)).getElementsByTagName(
				"parameter");
		for (int i = 0; i < params.getLength(); i++) {
			Element param = (Element) params.item(i);
			template.getAttributes().put(param.getAttribute("name"),
					param.getTextContent());
		}
		NodeList entities = config.getElementsByTagName("entity");
		for (int i = 0; i < entities.getLength(); i++) {
			Element entity = (Element) entities.item(i);
			Element implementation = (Element) entity.getElementsByTagName(
					"implementation").item(0);
			EntitySetupDescriptor setup = new EntitySetupDescriptor(
					Integer.parseInt(entity.getAttribute("x")),
					Integer.parseInt(entity.getAttribute("y")));
			setup.setImplementation(implementation.getAttribute("class-name"));
			NodeList parameters = implementation.getElementsByTagName(
					"parameter");
			for (int j = 0; j < parameters.getLength(); j++) {
				Element param = (Element) parameters.item(j);
				setup.setParameter(param.getAttribute("name"),
						param.getTextContent());
			}
			setup.setup(template);
		}
		start();
		StendhalRPZone zone = player.getZone();
		assertEquals("multiply", zone.getAttributes().get("color_method"));
		assertEquals(template.getAttributes().get("color"),
				zone.getAttributes().get("color"));
		assertEquals("bleach", zone.getAttributes().get("blend_method"));
		List<LoopedSoundSource> sounds = typed(zone, LoopedSoundSource.class);
		assertEquals(10, sounds.size());
		Set<String> coordinates = new HashSet<>();
		for (LoopedSoundSource sound : sounds) {
			assertEquals("pol-ognisko", sound.get("sound"));
			assertEquals(5, sound.getInt("radius"));
			assertEquals(12, sound.getInt("volume"));
			coordinates.add(sound.getX() + "," + sound.getY());
			assertFalse(
					typed(template, LoopedSoundSource.class).contains(sound));
		}
		for (String point : new String[] { "2,23", "10,23", "10,17", "2,17",
				"2,11", "2,4", "14,4", "14,11", "28,4", "31,23" })
			assertTrue(coordinates.contains(point));
	}

	@Test
	public void actualMapAndPublicEntranceHaveReachableApproaches()
			throws Exception {
		LayerDefinition collision = TMXLoader.load(
				"data/maps/interiors/semos/radan_hideout.tmx").getLayer(
						"collision");
		collision.build();
		Set<Integer> reached = flood(collision, 40, 28, 6, 23);
		for (int[] point : new int[][] { { 6, 24 }, { 4, 6 }, { 6, 10 },
				{ 11, 8 }, { 23, 8 }, { 26, 8 }, { 33, 21 }, { 35, 23 } })
			assertTrue("Unreachable cell " + point[0] + "," + point[1],
					reached.contains(point[1] * 40 + point[0]));
		LayerDefinition publicMap = TMXLoader.load(
				"data/maps/Level -1/semos/dungeon.tmx").getLayer("collision");
		publicMap.build();
		reached = flood(publicMap, 64, 64, 28, 38);
		assertTrue(reached.contains(37 * 64 + 27));
		assertTrue(reached.contains(38 * 64 + 25));
	}

	private static <T extends Entity> List<T> typed(StendhalRPZone zone,
			Class<T> type) {
		List<T> result = new ArrayList<>();
		for (Entity entity : zone.getEntitiesOfClass(type))
			result.add(type.cast(entity));
		return result;
	}

	private static Set<Integer> flood(LayerDefinition collision, int width,
			int height, int x, int y) {
		Set<Integer> found = new HashSet<>();
		Queue<Integer> queue = new ArrayDeque<>();
		found.add(y * width + x);
		queue.add(y * width + x);
		while (!queue.isEmpty()) {
			int p = queue.remove();
			for (int[] d : new int[][] { { 1, 0 }, { -1, 0 }, { 0, 1 },
					{ 0, -1 } }) {
				int nx = p % width + d[0], ny = p / width + d[1],
						next = ny * width + nx;
				if (nx >= 0 && nx < width && ny >= 0 && ny < height
						&& collision.getTileAt(nx, ny) == 0 && found.add(next))
					queue.add(next);
			}
		}
		return found;
	}
}
