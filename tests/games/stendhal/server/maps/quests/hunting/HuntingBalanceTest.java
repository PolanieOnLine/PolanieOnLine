package games.stendhal.server.maps.quests.hunting;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.common.Level;
import utilities.QuestHelper;

public class HuntingBalanceTest {
	@BeforeClass public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }
	private Creature creature(int level, int hp) {
		Creature creature = SingletonRepository.getEntityManager().getCreature("szczur");
		creature.setLevel(level);
		creature.setBaseHP(hp);
		creature.setAtk(10); creature.setDef(10);
		return creature;
	}
	@Test public void nominalRewardsHaveMeaningfulDifficultyDifference() {
		Creature target = creature(30, 100);
		double reference = HuntingRewards.strength(target);
		assertEquals(500, HuntingRewards.rewardBasisPoints(HuntingDifficulty.EASY, 25, target, reference));
		assertEquals(1500, HuntingRewards.rewardBasisPoints(HuntingDifficulty.MEDIUM, 25, target, reference));
		assertEquals(3000, HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 45, target, reference));
	}
	@Test public void rewardPerKillDoesNotFavourShorterRandomAssignments() {
		Creature target = creature(30, 100);
		double reference = HuntingRewards.strength(target) * 2;
		assertEquals(200, HuntingRewards.rewardBasisPoints(HuntingDifficulty.EASY, 20, target, reference));
		assertEquals(300, HuntingRewards.rewardBasisPoints(HuntingDifficulty.EASY, 30, target, reference));
		assertEquals(600, HuntingRewards.rewardBasisPoints(HuntingDifficulty.MEDIUM, 20, target, reference));
		assertEquals(900, HuntingRewards.rewardBasisPoints(HuntingDifficulty.MEDIUM, 30, target, reference));
		assertEquals(1333, HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 40, target, reference));
		assertEquals(1667, HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 50, target, reference));
	}
	@Test public void weakerMonsterPaysLessEvenAtSameDifficulty() {
		Creature weak = creature(25, 20), strong = creature(25, 100);
		double reference = HuntingRewards.strength(strong);
		assertEquals(100, HuntingRewards.rewardBasisPoints(HuntingDifficulty.EASY, 25, weak, reference));
		assertEquals(500, HuntingRewards.rewardBasisPoints(HuntingDifficulty.EASY, 25, strong, reference));
	}
	@Test public void attackAndDefenceArePartOfTargetStrength() {
		Creature target = creature(30, 100);
		double before = HuntingRewards.strength(target);
		target.setAtk(100); assertTrue(HuntingRewards.strength(target) > before);
		before = HuntingRewards.strength(target);
		target.setDef(100); assertTrue(HuntingRewards.strength(target) > before);
		before = HuntingRewards.strength(target);
		target.setRatk(200); assertTrue(HuntingRewards.strength(target) > before);
	}
	@Test public void exceptionallyToughHardTargetDoesNotLoseItsWorkPremiumToTheCap() {
		Creature easy = creature(25, 50), medium = creature(30, 100), hard = creature(35, 600);
		double reference = HuntingRewards.referenceStrength(30, Arrays.asList(easy, medium, hard));
		int easyReward = HuntingRewards.rewardBasisPoints(HuntingDifficulty.EASY, 25, easy, reference);
		int hardReward = HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 25, hard, reference);
		assertTrue(hardReward / HuntingRewards.strength(hard) > 3.1 * easyReward / HuntingRewards.strength(easy));
		assertEquals(2400, HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 40, hard, reference));
	}
	@Test public void savedRewardDoesNotChangeWhenCreatureDefinitionsOrCharacterLevelChange() {
		assertEquals(3375, HuntingRewards.questBasisPoints("szczur,0,30,0,0;0;2;HARD;3375"));
		assertEquals(3375, HuntingRewards.questBasisPoints("done;0;3;HARD;3375"));
		assertEquals("33,75%", HuntingRewards.percentageLabel(3375));
		assertEquals(1000, HuntingRewards.questBasisPoints("szczur,0,25,0,0;0;2;EASY;1000"));
		assertEquals(2500, HuntingRewards.questBasisPoints("szczur,0,25,0,0;0;2;MEDIUM;2500"));
		assertEquals(5000, HuntingRewards.questBasisPoints("szczur,0,25,0,0;0;2;HARD;5000"));
	}
	@Test public void shortLegacyTasksCannotGiveAFullTaskBonus() {
		assertEquals(60, HuntingRewards.questBasisPoints("szczur,0,1,0,0;0;2"));
		assertEquals(120, HuntingRewards.questBasisPoints("szczur,0,2,0,0;0;2;MEDIUM"));
		assertEquals(1500, HuntingRewards.questBasisPoints("szczur,0,25,0,0;0;2;MEDIUM"));
	}
	@Test public void actualCreatureDefinitionsFavourHardHuntsPerEstimatedWork() {
		List<Creature> creatures = new ArrayList<>();
		for (games.stendhal.server.core.rule.defaultruleset.DefaultCreature definition
				: SingletonRepository.getEntityManager().getDefaultCreatures()) {
			Creature creature = definition.getCreature();
			if (!creature.isBoss() && !creature.isAbnormal()) { creatures.add(creature); }
		}
		int checked = 0;
		for (int level : new int[] { 10, 20, 30, 50, 75, 100, 150, 200, 300, 400, 500 }) {
			double reference = HuntingRewards.referenceStrength(level, creatures);
			double easyMaximum = 0, hardMinimum = Double.POSITIVE_INFINITY;
			for (Creature creature : creatures) {
				if (HuntingDifficulty.EASY.accepts(level, creature.getLevel())) {
					easyMaximum = Math.max(easyMaximum,
							HuntingRewards.rewardBasisPoints(HuntingDifficulty.EASY, 25, creature, reference) / HuntingRewards.strength(creature));
				}
				if (HuntingDifficulty.HARD.accepts(level, creature.getLevel())) {
					hardMinimum = Math.min(hardMinimum,
							HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 25, creature, reference) / HuntingRewards.strength(creature));
				}
			}
			if (easyMaximum > 0 && Double.isFinite(hardMinimum)) {
				assertTrue("level " + level + ": easy " + easyMaximum + " hard " + hardMinimum,
						// Tiny rewards round to whole basis points. Compare the
						// actual work premium, not an approximate unrounded ratio.
						hardMinimum > easyMaximum);
				checked++;
			}
		}
		assertTrue("Cover several character levels", checked >= 5);
	}
	@Test public void giantGreyAtEndgameCannotAwardEightyPercent() {
		Creature giant = SingletonRepository.getEntityManager().getCreature("szarak gigant");
		double reference = HuntingRewards.referenceStrength(Level.maxLevel(), Arrays.asList(giant));
		// All three difficulties fall back to this sole available endgame target.
		// The shared reference is now 30/25 of its strength, not its raw strength.
		assertEquals(2222, HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 40, giant, reference));
		assertEquals(2778, HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 50, giant, reference));
		assertEquals(500, HuntingRewards.rewardBasisPoints(HuntingDifficulty.EASY, 30, giant, reference));
		assertEquals(1500, HuntingRewards.rewardBasisPoints(HuntingDifficulty.MEDIUM, 30, giant, reference));
	}
	@Test public void sharedScalingKeepsDifferentHardTargetsAndCountsDistinct() {
		Creature easy = creature(25, 50), medium = creature(30, 100);
		Creature weaker = creature(34, 400), strongest = creature(35, 600);
		double reference = HuntingRewards.referenceStrength(30, Arrays.asList(easy, medium, weaker, strongest));
		assertEquals(1600, HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 40, weaker, reference));
		assertEquals(2000, HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 50, weaker, reference));
		assertEquals(2400, HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 40, strongest, reference));
		assertEquals(3000, HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 50, strongest, reference));
	}
	@Test public void eachDifficultyEnforcesItsOwnLimitEvenWithAnUndersizedReference() {
		Creature target = creature(35, 600);
		assertEquals(500, HuntingRewards.rewardBasisPoints(HuntingDifficulty.EASY, 30, target, 1));
		assertEquals(1500, HuntingRewards.rewardBasisPoints(HuntingDifficulty.MEDIUM, 30, target, 1));
		assertEquals(3000, HuntingRewards.rewardBasisPoints(HuntingDifficulty.HARD, 50, target, 1));
	}
}
