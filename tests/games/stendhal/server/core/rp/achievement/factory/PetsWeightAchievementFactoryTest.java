package games.stendhal.server.core.rp.achievement.factory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.entity.creature.Cat;
import games.stendhal.server.entity.creature.DomesticAnimal;
import games.stendhal.server.entity.creature.Goat;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class PetsWeightAchievementFactoryTest {
	private Player player;
	private StendhalRPZone zone;

	@BeforeClass
	public static void init() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		zone = new StendhalRPZone("pet-weight-regression", 20, 20);
		MockStendlRPWorld.get().addRPZone(zone);
		player = PlayerTestHelper.createPlayer("pet-owner");
		zone.add(player);
	}

	@After
	public void tearDown() {
		MockStendlRPWorld.get().removeZone(zone);
	}

	private Achievement achievement(String id) {
		for (Achievement achievement : new PetsWeightAchievementFactory().createAchievements()) {
			if (id.equals(achievement.getIdentifier())) {
				return achievement;
			}
		}
		throw new AssertionError("Missing achievement " + id);
	}

	private void unresolvedAnimalIsSafe(String attribute, String id) {
		player.put(attribute, 123456);
		assertFalse(achievement(id).isFulfilled(player));
		assertTrue("A missing live object must not erase ownership", player.has(attribute));
	}

	@Test
	public void missingPetDoesNotInterruptAchievementChecks() {
		unresolvedAnimalIsSafe("pet", "weight.pet.100");
	}

	@Test
	public void missingSheepDoesNotInterruptAchievementChecks() {
		unresolvedAnimalIsSafe("sheep", "weight.sheep.100");
	}

	@Test
	public void missingGoatDoesNotInterruptAchievementChecks() {
		unresolvedAnimalIsSafe("goat", "weight.goat.100");
	}

	@Test
	public void achievementsStillRequireAnExistingAnimalOfAtLeast100Weight() {
		String[] attributes = { "pet", "sheep", "goat" };
		DomesticAnimal[] animals = { new Cat(), new Sheep(), new Goat() };
		for (int i = 0; i < animals.length; i++) {
			Achievement achievement = achievement("weight." + attributes[i] + ".100");
			assertFalse(achievement.isFulfilled(player));
			DomesticAnimal animal = animals[i];
			zone.add(animal);
			player.put(attributes[i], animal.getID().getObjectID());
			animal.setWeight(99);
			assertFalse(achievement.isFulfilled(player));
			animal.setWeight(100);
			assertTrue(achievement.isFulfilled(player));
		}
	}
}
