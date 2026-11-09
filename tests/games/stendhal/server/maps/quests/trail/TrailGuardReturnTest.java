package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

@RunWith(Parameterized.class)
public class TrailGuardReturnTest {
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> guards() {
		return Arrays.asList(new Object[][] {
				{ "mountain",
						(Supplier<Creature>) () -> new MountainTrailGuard(
								TrailCreatureVariants.create(
										"wspólnik Radana"),
								MountainTrailProgress.Encounter.MEETING_HUMAN),
						Step.MOUNTAIN_MEETING, "guards;0", 91, 65 },
				{ "keep", (Supplier<Creature>) () -> new AdosKeepGuardian(
						TrailCreatureVariants.create("strażnik warowni")),
						Step.KEEP_GUARDIAN, "fight", 45, 108 },
				{ "oath",
						(Supplier<Creature>) () -> new OathTrailGuard(
								TrailCreatureVariants.create(
										"poległy wartownik"),
								OathTrailProgress.Encounter.WATCH),
						Step.OATH_WATCH, "fight", 73, 38 },
				{ "warsaw",
						(Supplier<Creature>) () -> new WarsawFortressGuard(
								TrailCreatureVariants.create(
										"wartownik buntowników"),
								WarsawFortressProgress.Encounter.GATE),
						Step.WARSAW_GATE, "fight", 23, 37 },
				{ "coast",
						(Supplier<Creature>) () -> new CoastTrailGuard(
								TrailCreatureVariants.create("rabuś wybrzeża"),
								CoastTrailProgress.Encounter.BEACH),
						Step.COAST_LIGHTS, "fight", 108, 64 },
				{ "salt",
						(Supplier<Creature>) () -> new SaltMineGuard(
								TrailCreatureVariants.create("solny szlam"),
								SaltMineProgress.Encounter.SEEP),
						Step.SALT_RESCUE, "fight", 50, 82 },
				{ "dragon",
						(Supplier<Creature>) () -> new DragonTrailGuard(
								TrailCreatureVariants.create(
										"strażnik łowców jaj"),
								DragonTrailProgress.Encounter.CAPTOR),
						Step.EMBER_GUIDE, "fight", 99, 46 }, });
	}

	@BeforeClass
	public static void init() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	private final Supplier<Creature> factory;
	private final Step step;
	private final String phase;
	private final int homeX, homeY;
	private StendhalRPZone zone;
	private Player player;
	private Creature guard;

	public TrailGuardReturnTest(String name, Supplier<Creature> factory,
			Step step, String phase, int homeX, int homeY) {
		this.factory = factory;
		this.step = step;
		this.phase = phase;
		this.homeX = homeX;
		this.homeY = homeY;
	}

	@Before
	public void setup() {
		zone = new StendhalRPZone("quest-guard-return", 128, 128);
		MockStendlRPWorld.get().addRPZone(zone);
		player = PlayerTestHelper.createPlayer("return-investigator");
		player.setQuest(step.previousSlot(), "done");
		player.setQuest(step.slot, phase);
		zone.add(player);
		MockStendhalRPRuleProcessor.get().addPlayer(player);
		guard = factory.get();
		PlayerPrivateScene.assign(guard, player);
		guard.setRespawnPoint(
				new CreatureRespawnPoint(zone, homeX, homeY, guard, 1));
		guard.setPosition(homeX + 8, homeY);
		zone.add(guard);
		player.setPosition(homeX + 9, homeY);
		guard.setTarget(player);
	}

	@After
	public void cleanup() {
		MockStendhalRPRuleProcessor.get().clearPlayers();
		MockStendlRPWorld.get().removeZone(zone);
	}

	@Test
	public void disengagingReturnsTheGuardInsteadOfLeavingAHelplessTarget() {
		guard.logic();
		assertNull(guard.getAttackTarget());
		assertEquals(homeX, guard.getX());
		assertEquals(homeY, guard.getY());
		assertFalse(guard.nextTo(player));
	}

	@Test
	public void leavingTheZoneAlsoReturnsTheGuardWhenNobodyElseIsPresent() {
		zone.remove(player);
		guard.logic();
		assertNull(guard.getAttackTarget());
		assertEquals(homeX, guard.getX());
		assertEquals(homeY, guard.getY());
	}
}
