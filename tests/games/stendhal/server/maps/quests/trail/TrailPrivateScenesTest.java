package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.PlayerPrivateSpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class TrailPrivateScenesTest {
	private StendhalRPZone zone;
	private final List<Player> players = new ArrayList<>();

	@BeforeClass
	public static void init() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		zone = new StendhalRPZone("int_zakopane_shop", 128, 128);
		MockStendlRPWorld.get().addRPZone(zone);
		new TrailSites().configureZone(zone, null);
	}

	@After
	public void tearDown() {
		for (Player player : players) {
			if (zone.has(player.getID())) {
				zone.remove(player);
			}
		}
		MockStendlRPWorld.get().removeZone(zone);
		MockStendhalRPRuleProcessor.get().clearPlayers();
		turns();
	}

	@Test
	public void inactiveStoryHasNoClueOnTheMap() {
		enter("spectator", null);
		turns();
		assertTrue(zone.getEntitiesOfClass(TrailEvidence.class).isEmpty());
	}

	@Test
	public void twoPlayersGetIndependentCluesAndCompletionRemovesOnlyOne() {
		final Player first = enter("first-investigator", "inspect");
		final Player second = enter("second-investigator", "inspect");
		turns();
		assertEquals(1, clues(first).size());
		assertEquals(1, clues(second).size());
		final TrailEvidence firstClue = clues(first).get(0);
		first.setPosition(firstClue.getX(), firstClue.getY());
		second.setPosition(firstClue.getX(), firstClue.getY());
		assertFalse(firstClue.onUsed(second));
		assertTrue(firstClue.onUsed(first));
		turns();
		assertEquals(0, clues(first).size());
		assertEquals(1, clues(second).size());
	}

	@Test
	public void leavingCleansSceneAndReturningRespectsSavedProgress() {
		final Player player = enter("returning-investigator", "inspect");
		turns();
		assertEquals(1, clues(player).size());
		zone.remove(player);
		assertEquals(0, clues(player).size());
		player.setQuest(TrailQuestLine.Step.DELIVERY.slot, "archive");
		zone.add(player);
		turns();
		assertEquals(0, clues(player).size());
	}

	@Test
	public void combatProgressAndEnemiesAreIndependentForTwoOwners() {
		final StendhalRPZone forest = new StendhalRPZone(TrailQuestLine.FOREST, 128, 128);
		MockStendlRPWorld.get().addRPZone(forest);
		new TrailSites().configureZone(forest, null);
		final Player first = enter("first-fighter", null);
		final Player second = enter("second-fighter", null);
		try {
			for (final Player player : new Player[] {first, second}) {
				player.setQuest(TrailQuestLine.Step.GUEST.slot, "done");
				player.setQuest(TrailQuestLine.Step.SEAL.slot, "guard");
				zone.remove(player);
				forest.add(player);
				player.setPosition(TrailQuestLine.STOCK_X - 1, TrailQuestLine.STOCK_Y);
			}
			turns();
			final Creature firstGuard = guard(forest, first);
			final Creature secondGuard = guard(forest, second);
			final int firstHealth = firstGuard.getHP();
			firstGuard.onDamaged(second, 10000);
			assertEquals(firstHealth, firstGuard.getHP());
			firstGuard.addDropItem(SingletonRepository.getEntityManager().getItem("straszna dynia"));
			firstGuard.onDamaged(first, 10000);
			assertEquals(0, firstGuard.getHP());
			firstGuard.onDead(first);
			assertEquals("documents", first.getQuest(TrailQuestLine.Step.SEAL.slot));
			assertEquals("guard", second.getQuest(TrailQuestLine.Step.SEAL.slot));
			for (final Entity corpse : forest.getEntitiesOfClass(Corpse.class)) {
				assertEquals(0, ((Corpse) corpse).size());
				assertTrue(PlayerPrivateScene.isOwnedBy(corpse, first));
			}
			turns();
			assertEquals(null, guard(forest, first));
			assertTrue(forest.has(secondGuard.getID()));
			final Sheep companion = new Sheep();
			companion.setOwner(second);
			companion.setPosition(secondGuard.getX() - 1, secondGuard.getY());
			forest.add(companion);
			secondGuard.onDamaged(companion, 10000);
			secondGuard.onDead(companion);
			forest.remove(companion);
			assertEquals("documents", second.getQuest(TrailQuestLine.Step.SEAL.slot));
			turns();
			forest.remove(second);
			assertEquals(null, guard(forest, second));
		} finally {
			for (final Player player : new Player[] {first, second}) {
				if (forest.getPlayers().contains(player)) {
					forest.remove(player);
				}
			}
			MockStendlRPWorld.get().removeZone(forest);
		}
	}

	@Test
	public void leavingCancelsEffectsOnRemovedEnemies() {
		TrailSceneRegistry.encounter(zone, "cleanup-test", 10, 10,
				player -> player.isQuestInState(TrailQuestLine.Step.DELIVERY.slot, "inspect"),
				() -> SingletonRepository.getEntityManager().getCreature("szczur"));
		final Player player = enter("poisoned-scene-owner", "inspect");
		turns();
		final Creature enemy = guard(zone, player);
		enemy.getStatusList().inflictStatus(new PoisonStatus(-10, 2, -1, player), player);
		zone.remove(player);
		assertTrue(enemy.getStatusList().getStatuses().isEmpty());
		final int health = enemy.getHP();
		turns();
		assertEquals(health, enemy.getHP());
	}

	@Test
	public void privateNpcCopiesKeepRealQuestDialogueAndSeparateConversationState() {
		final String[] names = {"Margaret", "Lieutenant Drilenun", "Tragarz Olek"};
		final Map<String, SpeakerNPC> previous = new HashMap<>();
		for (final String name : names) {
			previous.put(name, NPCList.get().remove(name));
		}
		final StendhalRPZone city = new StendhalRPZone("0_semos_city", 128, 128);
		MockStendlRPWorld.get().addRPZone(city);
		final Player first = enter("first-carrier-contact", null);
		final Player second = enter("second-carrier-contact", null);
		try {
			city.add(new SpeakerNPC("Margaret"));
			city.add(new SpeakerNPC("Lieutenant Drilenun"));
			new SemosTrailSites().configureZone(city, null);
			new FalsePickupQuest().addToWorld();
			for (final Player player : new Player[] {first, second}) {
				player.setQuest(TrailQuestLine.Step.SEMOS_SIGN.slot, "done");
				player.setQuest(TrailQuestLine.Step.SEMOS_PICKUP.slot, "contact");
				zone.remove(player);
				city.add(player);
				player.setPosition(48, 41);
			}
			turns();
			final SpeakerNPC firstCarrier = actor(city, first);
			final SpeakerNPC secondCarrier = actor(city, second);
			assertTrue(firstCarrier.canStartConversationWith(first));
			assertFalse(firstCarrier.canStartConversationWith(second));
			assertFalse(NPCList.get().get("Tragarz Olek").canStartConversationWith(first));
			second.put("text", "hi");
			NPCList.get().get("Tragarz Olek").preLogic();
			firstCarrier.preLogic();
			assertFalse("A hidden dialogue template must ignore spoken greetings",
					NPCList.get().get("Tragarz Olek").isTalking());
			assertFalse("An outsider cannot occupy another player's conversation", firstCarrier.isTalking());
			second.remove("text");
			assertEquals(100, firstCarrier.getInt("resistance"));
			assertTrue(firstCarrier.getEngine().step(first, "hi"));
			assertEquals(ConversationStates.IDLE, secondCarrier.getEngine().getCurrentState());
			assertTrue(firstCarrier.getEngine().step(first, "suchy ładunek"));
			assertEquals("question", first.getQuest(TrailQuestLine.Step.SEMOS_PICKUP.slot));
			assertEquals("contact", second.getQuest(TrailQuestLine.Step.SEMOS_PICKUP.slot));
			assertTrue(firstCarrier.getEngine().step(first, "kwit"));
			assertEquals("return", first.getQuest(TrailQuestLine.Step.SEMOS_PICKUP.slot));
			first.setQuest(TrailQuestLine.Step.SEMOS_PICKUP.slot, "done");
			turns();
			assertEquals(null, actor(city, first));
			assertTrue(city.has(secondCarrier.getID()));
		} finally {
			for (final Player player : new Player[] {first, second}) {
				if (city.getPlayers().contains(player)) {
					city.remove(player);
				}
			}
			for (final Entity npc : new ArrayList<>(city.getEntitiesOfClass(SpeakerNPC.class))) {
				city.remove(npc);
			}
			MockStendlRPWorld.get().removeZone(city);
			for (final String name : names) {
				if (previous.get(name) != null) {
					NPCList.get().add(previous.get(name));
				}
			}
		}
	}

	private Creature guard(final StendhalRPZone current, final Player owner) {
		for (final Entity entity : current.getEntitiesOfClass(Creature.class)) {
			if (PlayerPrivateScene.isOwnedBy(entity, owner)) {
				return (Creature) entity;
			}
		}
		return null;
	}

	private SpeakerNPC actor(final StendhalRPZone current, final Player owner) {
		for (final Entity entity : current.getEntitiesOfClass(PlayerPrivateSpeakerNPC.class)) {
			if (PlayerPrivateScene.isOwnedBy(entity, owner)) {
				return (SpeakerNPC) entity;
			}
		}
		return null;
	}

	private Player enter(final String name, final String state) {
		final Player player = PlayerTestHelper.createPlayer(name);
		if (state != null) {
			player.setQuest("wegiel_na_opal", "done");
			player.setQuest(TrailQuestLine.Step.DELIVERY.slot, state);
		}
		players.add(player);
		MockStendhalRPRuleProcessor.get().addPlayer(player);
		zone.add(player);
		return player;
	}

	private List<TrailEvidence> clues(final Player player) {
		final List<TrailEvidence> result = new ArrayList<>();
		for (Entity entity : zone.getEntitiesOfClass(TrailEvidence.class)) {
			if (entity.has("#perception_value") && player.getName().equals(entity.get("#perception_value"))) {
				result.add((TrailEvidence) entity);
			}
		}
		return result;
	}

	private void turns() {
		final TurnNotifier notifier = TurnNotifier.get();
		final int start = Math.max(0, notifier.getCurrentTurnForDebugging());
		for (int turn = start + 1; turn <= start + 3; turn++) {
			notifier.logic(turn);
		}
	}
}
