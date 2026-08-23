package games.stendhal.server.entity.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Guards the Bleeding 2.0 creature profile factory. */
public class BleedingAttackerFactoryTest {
	@Test
	public void factoryCreatesBleeding2Attacker() {
		final BleedingAttacker attacker = BleedingAttackerFactory.get("15");

		assertNotNull(attacker);
		assertTrue(attacker instanceof BleedingAttacker);
		assertEquals(15.0, attacker.getProbability(), 0.0);
		assertEquals(BleedingAttacker.CREATURE_DEFAULT_DAMAGE_FACTOR,
				attacker.getDamageFactor(), 0.0);
		assertEquals(BleedingAttacker.CREATURE_MIN_TARGET_HP_FACTOR,
				attacker.getMinimumTargetHpFactor(), 0.0);
		assertEquals(BleedingAttacker.CREATURE_MAX_TARGET_HP_FACTOR,
				attacker.getMaximumTargetHpFactor(), 0.0);
	}

	@Test
	public void profileConfigurationIsDeterministic() {
		final BleedingAttacker first = BleedingAttackerFactory.get("20;0.30");
		final BleedingAttacker second = BleedingAttackerFactory.get("20;0.30");

		assertEquals(first.getProbability(), second.getProbability(), 0.0);
		assertEquals(first.getDamageFactor(), second.getDamageFactor(), 0.0);
		assertEquals(0.30, second.getDamageFactor(), 0.0);
		assertEquals(first.getMinimumTargetHpFactor(),
				second.getMinimumTargetHpFactor(), 0.0);
		assertEquals(first.getMaximumTargetHpFactor(),
				second.getMaximumTargetHpFactor(), 0.0);
	}

	@Test
	public void factorySupportsDefaultAndCustomDamageFactor() {
		final BleedingAttacker defaultDamage = BleedingAttackerFactory.get("12");
		final BleedingAttacker customDamage = BleedingAttackerFactory.get("12;0.35");

		assertEquals(BleedingAttacker.CREATURE_DEFAULT_DAMAGE_FACTOR,
				defaultDamage.getDamageFactor(), 0.0);
		assertEquals(0.35, customDamage.getDamageFactor(), 0.0);
		assertEquals(BleedingAttacker.CREATURE_MIN_TARGET_HP_FACTOR,
				customDamage.getMinimumTargetHpFactor(), 0.0);
		assertEquals(BleedingAttacker.CREATURE_MAX_TARGET_HP_FACTOR,
				customDamage.getMaximumTargetHpFactor(), 0.0);
	}
}
