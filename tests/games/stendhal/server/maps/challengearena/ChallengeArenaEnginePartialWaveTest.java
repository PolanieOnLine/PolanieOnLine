package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.util.Area;
import marauroa.common.game.RPEvent;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

/** Real placement on four available cells, then opening the fifth cell. */
public class ChallengeArenaEnginePartialWaveTest {

	private final long[] now = {1000L};
	private StendhalRPZone zone;
	private Player player;
	private Creature blocker;
	private ChallengeArenaEngine engine;
	private ChallengeArenaCreatureSpawner spawner;

	@BeforeClass
	public static void setupWorld() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	@SuppressWarnings("unchecked")
	public void setupArena() throws Exception {
		zone = new StendhalRPZone("arena-partial-wave", 40, 40);
		MockStendlRPWorld.get().addRPZone(zone);
		for (int y = 0; y < 40; y++) {
			for (int x = 0; x < 40; x++) {
				if (y != 20 || x < 20 || x > 28) {
					zone.collisionMap.setCollide(x, y);
				}
			}
		}
		player = PlayerTestHelper.createPlayer("PartialWavePlayer");
		zone.add(player);
		player.setPosition(20, 20);
		player.setHP(100);
		blocker = new Creature();
		blocker.setBaseHP(100);
		blocker.setHP(100);
		zone.add(blocker);
		blocker.setPosition(28, 20);
		ChallengeArenaManager.reserve(player.getName());
		player.setQuest(ChallengeArenaState.QUEST_SLOT,
				ChallengeArenaState.start(ChallengeArenaTier.LEGEND)
						.withSpawnedCreatures(5).serialize());
		ChallengeArenaInfo info = new ChallengeArenaInfo(new Area(zone,
				new Rectangle2D.Double(20, 20, 9, 1)), zone,
				zone.getName(), 20, 20, 20, 20);
		engine = new ChallengeArenaEngine(player, info, () -> now[0]);
		// Keep real placement, collision, tracking and engine behavior. Only
		// constrain the random template catalogue to a single one-tile creature.
		Field field = ChallengeArenaEngine.class.getDeclaredField("spawner");
		field.setAccessible(true);
		spawner = (ChallengeArenaCreatureSpawner) field.get(engine);
		field = ChallengeArenaCreatureSpawner.class.getDeclaredField("sortedCreatures");
		field.setAccessible(true);
		List<Creature> templates = (List<Creature>) field.get(spawner);
		templates.clear();
		templates.add(SingletonRepository.getEntityManager().getCreature("szczur"));
		player.clearEvents();
	}

	@After
	public void cleanup() {
		if (engine != null) {
			engine.stop();
		}
		ChallengeArenaManager.clearForTests();
		MockStendlRPWorld.get().removeZone(zone);
	}

	@Test
	public void missingCreatureIsAddedWhileTheOtherFourAreStillAlive() {
		engine.onTurnReached(1);
		assertEquals(9, state().getSpawnedCreatures());
		assertEquals(4, activeCreatures().size());
		assertFalse(spawner.areAllCreaturesDead());
		zone.remove(blocker);
		now[0] += 2500;
		engine.onTurnReached(2);
		assertEquals("Fill the missing fifth enemy without waiting for four kills",
				10, state().getSpawnedCreatures());
		assertEquals(5, activeCreatures().size());
		assertEquals("Do not announce wave 2 twice", 1, waveMessages().size());
	}

	@Test
	public void missingCreatureDoesNotShiftEveryFollowingWaveOrIsolateChampion() {
		engine.onTurnReached(1);
		assertEquals(9, state().getSpawnedCreatures());
		for (Creature creature : activeCreatures()) {
			creature.setHP(0);
			zone.remove(creature);
		}
		zone.remove(blocker);
		now[0] += 2500;
		engine.onTurnReached(2);
		assertEquals("Only the one missing enemy belongs to this wave",
				10, state().getSpawnedCreatures());
		assertEquals(1, activeCreatures().size());
		for (int wave = 3; wave <= 9; wave++) {
			for (Creature creature : activeCreatures()) {
				creature.setHP(0);
				zone.remove(creature);
			}
			now[0] += 2500;
			engine.onTurnReached(wave);
			assertEquals(wave * 5, state().getSpawnedCreatures());
			assertEquals("Every later wave, including the champion finale, has five enemies",
					5, activeCreatures().size());
		}
		assertEquals(8, waveMessages().size()); // This fixture starts at wave 2.
		assertTrue(waveMessages().get(0).contains("Fala 2 z 9"));
		assertTrue(waveMessages().get(7).contains("Fala 9 z 9"));
	}

	@Test
	public void temporaryFailureDoesNotForfeitAPartiallySpawnedWave() {
		engine.onTurnReached(1);
		assertEquals(9, state().getSpawnedCreatures());
		now[0] += 2500;
		engine.onTurnReached(2);
		assertEquals(ChallengeArenaState.Lifecycle.ACTIVE, state().getLifecycle());
		assertEquals(9, state().getSpawnedCreatures());
		assertEquals(4, activeCreatures().size());
		zone.remove(blocker);
		now[0] += 2500;
		engine.onTurnReached(3);
		assertEquals(10, state().getSpawnedCreatures());
	}

	@Test
	public void permanentlyBlockedPartialWaveEndsInsteadOfHangingForever() {
		engine.onTurnReached(1);
		assertEquals(9, state().getSpawnedCreatures());
		for (Creature creature : activeCreatures()) {
			creature.setHP(0);
			zone.remove(creature);
		}
		for (int x = 24; x <= 28; x++) {
			zone.collisionMap.setCollide(x, 20);
		}
		now[0] += 2500;
		engine.onTurnReached(2);
		assertEquals("A temporary placement failure is not an immediate forfeit",
				ChallengeArenaState.Lifecycle.ACTIVE, state().getLifecycle());
		now[0] += 60000;
		engine.onTurnReached(3);
		assertEquals(ChallengeArenaState.Lifecycle.FAILED, state().getLifecycle());
		assertEquals(0, activeCreatures().size());
		assertFalse(ChallengeArenaManager.isReservedBy(player.getName()));
	}

	@Test
	public void longFightDoesNotConsumeTheBlockedWaveTimeout() {
		engine.onTurnReached(1);
		now[0] += 120000;
		engine.onTurnReached(2);
		assertEquals(ChallengeArenaState.Lifecycle.ACTIVE, state().getLifecycle());
		assertEquals(4, activeCreatures().size());
		zone.remove(blocker);
		now[0] += 2500;
		engine.onTurnReached(3);
		assertEquals(10, state().getSpawnedCreatures());
	}

	private ChallengeArenaState state() {
		return ChallengeArenaState.parse(player.getQuest(ChallengeArenaState.QUEST_SLOT));
	}

	@Test
	public void completelyUnspawnableNewWaveStillFailsWithoutAwardingProgress() {
		for (int x = 24; x <= 28; x++) {
			zone.collisionMap.setCollide(x, 20);
		}
		engine.onTurnReached(1);
		assertEquals(ChallengeArenaState.Lifecycle.FAILED, state().getLifecycle());
		assertEquals(5, state().getSpawnedCreatures());
		assertEquals(0, waveMessages().size());
	}

	@Test
	public void forfeitDuringPartialWaveCleansUpAndKeepsTheRunFailed() {
		engine.onTurnReached(1);
		assertEquals(4, activeCreatures().size());
		assertTrue(engine.forfeit(player));
		assertEquals(ChallengeArenaState.Lifecycle.FAILED, state().getLifecycle());
		assertEquals(0, activeCreatures().size());
		assertFalse(ChallengeArenaManager.isReservedBy(player.getName()));
	}

	@Test
	public void successfulRefillRestartsTimeoutEvenIfItsCreatureDiesBeforeNextTurn() {
		Creature extraBlocker = addBlocker(27);
		engine.onTurnReached(1);
		assertEquals(8, state().getSpawnedCreatures());
		for (Creature creature : activeCreatures()) {
			// This helper normally excludes only the fixture's original blocker.
			if (creature != extraBlocker) {
				creature.setHP(0);
				zone.remove(creature);
			}
		}
		for (int x = 24; x <= 26; x++) {
			addBlocker(x);
		}
		now[0] += 2500;
		engine.onTurnReached(2); // No slots free; start the idle timeout.
		now[0] += 59000;
		zone.remove(extraBlocker);
		engine.onTurnReached(3); // Exactly one of the two missing enemies fits.
		assertEquals(9, state().getSpawnedCreatures());
		for (marauroa.common.game.RPObject object : zone) {
			if (object instanceof games.stendhal.server.entity.creature.ArenaCreature
					&& ((Creature) object).getHP() > 0) {
				((Creature) object).setHP(0);
				zone.remove(object);
				break;
			}
		}
		addBlocker(27);
		now[0] += 2500;
		engine.onTurnReached(4);
		assertEquals("Progress grants a fresh idle timeout, not the old almost-expired one",
				ChallengeArenaState.Lifecycle.ACTIVE, state().getLifecycle());
		assertEquals(9, state().getSpawnedCreatures());
	}

	private Creature addBlocker(final int x) {
		Creature creature = new Creature();
		creature.setBaseHP(100);
		creature.setHP(100);
		zone.add(creature);
		creature.setPosition(x, 20);
		return creature;
	}

	private List<Creature> activeCreatures() {
		List<Creature> result = new ArrayList<>();
		for (marauroa.common.game.RPObject object : zone) {
			if (object instanceof Creature && object != blocker
					&& ((Creature) object).getHP() > 0) {
				result.add((Creature) object);
			}
		}
		return result;
	}

	private List<String> waveMessages() {
		List<String> result = new ArrayList<>();
		for (RPEvent event : player.events()) {
			if (event.has("text") && event.get("text").startsWith("Arena Wyzwań. Fala ")) {
				result.add(event.get("text"));
			}
		}
		return result;
	}
}
