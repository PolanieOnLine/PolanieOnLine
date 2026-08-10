package games.stendhal.server.entity.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Guards the compatibility path from legacy creature profiles to Bleeding 2.0. */
public class BleedingAttackerFactoryTest {
	@Test
	public void legacyFactoryCreatesBleeding2Attacker() {
		final StatusAttacker attacker = BloodAttackerFactory.get("15", 1884);

		assertNotNull(attacker);
		assertTrue(attacker instanceof BleedingAttacker);
		assertEquals(15.0, attacker.getProbability(), 0.0);
		assertEquals(BleedingAttacker.DEFAULT_DAMAGE_FACTOR,
				((BleedingAttacker) attacker).getDamageFactor(), 0.0);
	}

	@Test
	public void creatureAttackStatDoesNotChangeWoundConfiguration() {
		final BleedingAttacker weakCreature = BloodAttackerFactory.get("20;0.30", 10);
		final BleedingAttacker strongCreature = BloodAttackerFactory.get("20;0.30", 5000);

		assertEquals(weakCreature.getProbability(), strongCreature.getProbability(), 0.0);
		assertEquals(weakCreature.getDamageFactor(), strongCreature.getDamageFactor(), 0.0);
		assertEquals(0.30, strongCreature.getDamageFactor(), 0.0);
	}

	@Test
	public void explicitFactorySupportsDefaultAndCustomDamageFactor() {
		final BleedingAttacker defaultDamage = BleedingAttackerFactory.get("12");
		final BleedingAttacker customDamage = BleedingAttackerFactory.get("12;0.35");

		assertEquals(BleedingAttacker.DEFAULT_DAMAGE_FACTOR,
				defaultDamage.getDamageFactor(), 0.0);
		assertEquals(0.35, customDamage.getDamageFactor(), 0.0);
	}
}
