package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

@RunWith(Parameterized.class)
public class TrailEncounterLootTest {
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> encounters() {
		return Arrays.asList(new Object[][] {
				{ "mountain", (Supplier<Creature>) () -> new MountainTrailGuard(
						TrailCreatureVariants.create("wspólnik Radana"),
						MountainTrailProgress.Encounter.MEETING_HUMAN) },
				{ "keep", (Supplier<Creature>) () -> new AdosKeepGuardian(
						TrailCreatureVariants.create("strażnik warowni")) },
				{ "oath",
						(Supplier<Creature>) () -> new OathTrailGuard(
								TrailCreatureVariants.create(
										"poległy wartownik"),
								OathTrailProgress.Encounter.WATCH) },
				{ "warsaw",
						(Supplier<Creature>) () -> new WarsawFortressGuard(
								TrailCreatureVariants.create(
										"wartownik buntowników"),
								WarsawFortressProgress.Encounter.GATE) },
				{ "coast",
						(Supplier<Creature>) () -> new CoastTrailGuard(
								TrailCreatureVariants.create("rabuś wybrzeża"),
								CoastTrailProgress.Encounter.BEACH) },
				{ "salt",
						(Supplier<Creature>) () -> new SaltMineGuard(
								TrailCreatureVariants.create("solny szlam"),
								SaltMineProgress.Encounter.SEEP) },
				{ "dragon",
						(Supplier<Creature>) () -> new DragonTrailGuard(
								TrailCreatureVariants.create(
										"strażnik łowców jaj"),
								DragonTrailProgress.Encounter.CAPTOR) },
				{ "bell",
						(Supplier<Creature>) () -> new BellTrailGuard(
								TrailCreatureVariants.create(
										"strażnik milczącego dzwonu"),
								BellTrailProgress.Encounter.WARDEN) },
				{ "radan", (Supplier<Creature>) () -> new RadanDungeonScene(
						PlayerTestHelper.createPlayer("loot-owner"),
						new StendhalRPZone("loot-scene", 40, 28)).new Fighter(
								TrailCreatureVariants.create("człowiek Radana"),
								1) } });
	}

	@BeforeClass
	public static void init() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	private final Supplier<Creature> factory;

	public TrailEncounterLootTest(String name, Supplier<Creature> factory) {
		this.factory = factory;
	}

	@Test
	public void scriptedFightsLeaveNoCarriedOrGeneratedLootDuringHalloween()
			throws Exception {
		String previous = System.getProperty("stendhal.minetown");
		try {
			System.setProperty("stendhal.minetown", "true");
			Creature guard = factory.get();
			guard.addDropItem(
					SingletonRepository.getEntityManager().getItem("miecz"));
			guard.addDropItem("money", 100.0, 10);
			Corpse corpse = new Corpse(guard, "loot-owner");
			Method drop = Creature.class.getDeclaredMethod("dropItemsOn",
					Corpse.class);
			drop.setAccessible(true);
			drop.invoke(guard, corpse);
			assertEquals(0, corpse.size());
		} finally {
			if (previous == null) {
				System.clearProperty("stendhal.minetown");
			} else {
				System.setProperty("stendhal.minetown", previous);
			}
		}
	}
}
