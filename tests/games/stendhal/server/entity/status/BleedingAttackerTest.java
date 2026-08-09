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
		final BleedingAttacker bleeding = new BleedingAttacker(100.0, 0.20);

		bleeding.onHit(target, attacker, 100);

		final List<BleedingStatus> wounds = target.getStatusList()
				.getAllStatusByClass(BleedingStatus.class);
		assertEquals(1, wounds.size());
		assertEquals(20, wounds.get(0).getTotalDamage());
		assertEquals(BleedingAttacker.DEFAULT_TICKS,
				wounds.get(0).getTicksRemaining());
		assertSame(attacker, wounds.get(0).getSource());
	}

	@Test
	public void fullBleedingResistanceBlocksEvenCertainProc() {
		final Player attacker = PlayerTestHelper.createPlayer("bleed attacker");
		final Player target = PlayerTestHelper.createPlayer("bleed target");
		target.put("resist_bleeding", 1.0);
		final BleedingAttacker bleeding = new BleedingAttacker(100.0, 0.20);

		bleeding.onHit(target, attacker, 100);

		assertFalse(target.hasStatus(StatusType.BLEEDING));
	}

	@Test
	public void deterministicRollUsesPercentScale() {
		assertTrue(BleedingAttacker.rollChance(15.0, 1500));
		assertFalse(BleedingAttacker.rollChance(15.0, 1501));
	}

	@Test
	public void factorySupportsDefaultAndExplicitDamageFactor() {
		final BleedingAttacker defaultBleed = BleedingAttackerFactory.get("15");
		final BleedingAttacker strongBleed = BleedingAttackerFactory.get("20;0.30");

		assertEquals(15.0, defaultBleed.getProbability(), 0.0);
		assertEquals(BleedingAttacker.DEFAULT_DAMAGE_FACTOR,
				defaultBleed.getDamageFactor(), 0.0);
		assertEquals(20.0, strongBleed.getProbability(), 0.0);
		assertEquals(0.30, strongBleed.getDamageFactor(), 0.0);
	}


	@Test
	public void totalDamageUsesActualHitAndRoundsPredictably() {
		assertEquals(20, BleedingAttacker.calculateTotalDamage(100, 0.20));
		assertEquals(1, BleedingAttacker.calculateTotalDamage(1, 0.20));
		assertEquals(0, BleedingAttacker.calculateTotalDamage(0, 0.20));
	}
}
