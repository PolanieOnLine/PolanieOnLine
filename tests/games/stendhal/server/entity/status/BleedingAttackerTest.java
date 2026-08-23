package games.stendhal.server.entity.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class BleedingAttackerTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		MockStendlRPWorld.get();
	}

	@Test
	public void zeroDamageHitCannotApplyBleeding() {
		final Player attacker = PlayerTestHelper.createPlayer("bleed attacker");
		final Player target = PlayerTestHelper.createPlayer("bleed target");
		final BleedingAttacker bleeding = new BleedingAttacker(100.0, 0.20);

		bleeding.onHit(target, attacker, 0);

		assertFalse(target.hasStatus(StatusType.BLEEDING));
	}

	@Test
	public void successfulHitCreatesWoundFromActualDamage() {
		final Player attacker = PlayerTestHelper.createPlayer("bleed attacker");
		final Player target = PlayerTestHelper.createPlayer("bleed target");
		final BleedingAttacker bleeding = new BleedingAttacker(100.0, 0.25);

		bleeding.onHit(target, attacker, 100);

		final List<BleedingStatus> wounds = target.getStatusList()
				.getAllStatusByClass(BleedingStatus.class);
		assertEquals(1, wounds.size());
		assertEquals(25, wounds.get(0).getTotalDamage());
		assertEquals(BleedingAttacker.DEFAULT_TICKS,
				wounds.get(0).getTicksRemaining());
		assertSame(attacker, wounds.get(0).getSource());
	}

	@Test
	public void fullBleedingResistanceBlocksEvenCertainProc() {
		final Player attacker = PlayerTestHelper.createPlayer("bleed attacker");
		final Player target = PlayerTestHelper.createPlayer("bleed target");
		target.put("resist_bleeding", 1.0);
		final BleedingAttacker bleeding = new BleedingAttacker(100.0, 0.25);

		bleeding.onHit(target, attacker, 100);

		assertFalse(target.hasStatus(StatusType.BLEEDING));
	}

	@Test
	public void deterministicRollUsesPercentScale() {
		assertTrue(BleedingAttacker.rollChance(15.0, 1500));
		assertFalse(BleedingAttacker.rollChance(15.0, 1501));
	}

	@Test
	public void totalDamageUsesActualHitAndRoundsPredictably() {
		assertEquals(25, BleedingAttacker.calculateTotalDamage(100, 0.25));
		assertEquals(1, BleedingAttacker.calculateTotalDamage(1, 0.25));
		assertEquals(0, BleedingAttacker.calculateTotalDamage(0, 0.25));
	}

	@Test
	public void creatureBleedingUsesTargetHpMinimumForSmallHits() {
		assertEquals(100, BleedingAttacker.calculateTotalDamage(60, 1.0,
				5000, 0.02, 0.05));
	}

	@Test
	public void creatureBleedingKeepsHitScaledDamageInsideGuardRails() {
		assertEquals(180, BleedingAttacker.calculateTotalDamage(180, 1.0,
				5000, 0.02, 0.05));
	}

	@Test
	public void creatureBleedingCapsVeryLargeHitsByTargetHp() {
		assertEquals(250, BleedingAttacker.calculateTotalDamage(400, 1.0,
				5000, 0.02, 0.05));
	}

	@Test
	public void ordinaryBleedingConstructorDoesNotGainCreatureGuardRails() {
		final BleedingAttacker bleeding = new BleedingAttacker(10.0, 0.25);

		assertEquals(0.0, bleeding.getMinimumTargetHpFactor(), 0.0);
		assertEquals(0.0, bleeding.getMaximumTargetHpFactor(), 0.0);
		assertEquals(25, BleedingAttacker.calculateTotalDamage(100,
				bleeding.getDamageFactor()));
	}
}
