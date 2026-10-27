/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.group.Group;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.RebornSystem;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class RPEntityGroupExperienceTest {
	private final MockStendhalRPRuleProcessor ruleProcessor = MockStendhalRPRuleProcessor.get();
	private Group group;

	@BeforeClass
	public static void setUpWorld() {
		MockStendlRPWorld.get();
	}

	@Before
	public void setUp() {
		ruleProcessor.clearPlayers();
		SingletonRepository.getGroupManager().destroyGroup("anna");
	}

	@After
	public void tearDown() {
		if (group != null) {
			SingletonRepository.getGroupManager().destroyGroup("anna");
		}
		ruleProcessor.clearPlayers();
	}

	@Test
	public void equalModeAggregatesContributorsAndRewardsEveryNearbyMemberOnce() {
		final StendhalRPZone combatZone = new StendhalRPZone("equal_exp_combat", 20, 20);
		final StendhalRPZone otherZone = new StendhalRPZone("equal_exp_other", 20, 20);
		final Player anna = addPlayer("anna", combatZone);
		final Player bartek = addPlayer("bartek", combatZone);
		final Player celina = addPlayer("celina", combatZone);
		final Player distant = addPlayer("distant", otherZone);

		group = SingletonRepository.getGroupManager().createGroup(anna.getName());
		group.addMember(bartek.getName());
		group.addMember(celina.getName());
		group.addMember(distant.getName());
		group.setExpmode("equal");

		final RewardTestEntity defeated = new RewardTestEntity();
		defeated.setName("test target");
		defeated.addContribution(anna, 60);
		defeated.addContribution(bartek, 40);
		defeated.rewardContributors(2000);

		assertEquals(34, anna.getXP());
		assertEquals(33, bartek.getXP());
		assertEquals(33, celina.getXP());
		assertEquals(0, distant.getXP());
	}

	@Test
	public void firstFiveRebornsKeepBaseCombatExperience() {
		final StendhalRPZone combatZone = new StendhalRPZone("reborn_exp_combat", 20, 20);
		final Player anna = addPlayer("anna", combatZone);
		anna.put(RebornSystem.ATTR_REBORNS, 2);

		final RewardTestEntity defeated = new RewardTestEntity();
		defeated.setName("test target");
		defeated.addContribution(anna, 100);

		// 5 percent of 8880 is 444. The first five reborns do not modify it.
		defeated.rewardContributors(8880);
		assertEquals(444, anna.getXP());
	}

	@Test
	public void directExperienceDoesNotUseRebornCombatReduction() {
		final Player anna = PlayerTestHelper.createPlayer("direct-exp");
		anna.put(RebornSystem.ATTR_REBORNS, 10);

		anna.addXP(444);
		assertEquals(444, anna.getXP());
	}

	@Test
	public void lowestModeUsesTheRecipientsRebornCountNotTheKillers() {
		final StendhalRPZone combatZone = new StendhalRPZone("reborn_lowest_exp", 20, 20);
		final Player anna = addPlayer("anna", combatZone);
		final Player bartek = addPlayer("bartek", combatZone);
		anna.setLevel(10);
		bartek.setLevel(1);
		anna.put(RebornSystem.ATTR_REBORNS, 5);
		bartek.put(RebornSystem.ATTR_REBORNS, 10);
		group = SingletonRepository.getGroupManager().createGroup(anna.getName());
		group.addMember(bartek.getName());
		group.setExpmode("lowest");

		final RewardTestEntity defeated = new RewardTestEntity();
		defeated.setName("test target");
		defeated.addContribution(anna, 100);
		defeated.rewardContributors(8880);
		assertEquals(0, anna.getXP());
		assertEquals(333, bartek.getXP());
	}

	@Test
	public void equalModeAppliesRebornReductionAfterSplittingForEachRecipient() {
		final StendhalRPZone combatZone = new StendhalRPZone("reborn_equal_exp", 20, 20);
		final Player anna = addPlayer("anna", combatZone);
		final Player bartek = addPlayer("bartek", combatZone);
		final Player celina = addPlayer("celina", combatZone);

		anna.put(RebornSystem.ATTR_REBORNS, 10);
		bartek.put(RebornSystem.ATTR_REBORNS, 6);

		group = SingletonRepository.getGroupManager().createGroup(anna.getName());
		group.addMember(bartek.getName());
		group.addMember(celina.getName());
		group.setExpmode("equal");

		final RewardTestEntity defeated = new RewardTestEntity();
		defeated.setName("test target");
		defeated.addContribution(anna, 60);
		defeated.addContribution(bartek, 40);
		defeated.rewardContributors(2000);

		// Base shares are 34, 33 and 33. Apply 75%, 95% and 100% per recipient.
		assertEquals(26, anna.getXP());
		assertEquals(31, bartek.getXP());
		assertEquals(33, celina.getXP());
	}

	@Test
	public void knowledgeIncreasesPersonalCombatRewardAfterRebornReduction() {
		for (final int reborns : new int[] {0, 10}) {
			final Player anna = addPlayer("anna", new StendhalRPZone("knowledge_solo", 20, 20));
			anna.put(RebornSystem.ATTR_REBORNS, reborns);
			anna.getSlot("utility_rune").add(knowledge(anna));
			final RewardTestEntity defeated = new RewardTestEntity();
			defeated.setName("test target");
			defeated.addContribution(anna, 100);
			defeated.rewardContributors(20000);
			assertEquals(reborns == 0 ? 1100 : 825, anna.getXP());
			ruleProcessor.clearPlayers();
		}
	}

	@Test
	public void knowledgeOnlyBoostsItsOwnerAfterEqualGroupSplit() {
		final StendhalRPZone zone = new StendhalRPZone("knowledge_equal", 20, 20);
		final Player anna = addPlayer("anna", zone);
		final Player bartek = addPlayer("bartek", zone);
		anna.getSlot("utility_rune").add(knowledge(anna));
		group = SingletonRepository.getGroupManager().createGroup(anna.getName());
		group.addMember(bartek.getName());
		group.setExpmode("equal");
		final RewardTestEntity defeated = new RewardTestEntity();
		defeated.setName("test target");
		defeated.addContribution(anna, 100);
		defeated.rewardContributors(20000);
		assertEquals(550, anna.getXP());
		assertEquals(500, bartek.getXP());
	}

	@Test
	public void lowestGroupModeUsesTheRecipientsKnowledge() {
		final StendhalRPZone zone = new StendhalRPZone("knowledge_lowest", 20, 20);
		final Player anna = addPlayer("anna", zone);
		final Player bartek = addPlayer("bartek", zone);
		anna.setLevel(10);
		bartek.setLevel(1);
		bartek.getSlot("utility_rune").add(knowledge(bartek));
		group = SingletonRepository.getGroupManager().createGroup(anna.getName());
		group.addMember(bartek.getName());
		group.setExpmode("lowest");
		final int before = bartek.getXP();
		final RewardTestEntity defeated = new RewardTestEntity();
		defeated.setName("test target");
		defeated.addContribution(anna, 100);
		defeated.rewardContributors(20000);
		assertEquals(1100, bartek.getXP() - before);
	}

	@Test
	public void knowledgeInBagsHandsOrOwnedBySomeoneElseDoesNotBoostRewards() {
		for (final String slot : new String[] {"bag", "rhand", "utility_rune"}) {
			final Player anna = addPlayer("anna", new StendhalRPZone("knowledge_inactive", 20, 20));
			final Glyph glyph = knowledge(anna);
			if ("utility_rune".equals(slot)) glyph.setBoundTo("someone_else");
			anna.getSlot(slot).add(glyph);
			final RewardTestEntity defeated = new RewardTestEntity();
			defeated.setName("test target");
			defeated.addContribution(anna, 100);
			defeated.rewardContributors(20000);
			assertEquals(slot, 1000, anna.getXP());
			ruleProcessor.clearPlayers();
		}
	}

	@Test
	public void knowledgeDoesNotIncreaseOrdinaryQuestExperience() {
		final Player anna = PlayerTestHelper.createPlayer("quest-exp");
		anna.getSlot("utility_rune").add(knowledge(anna));
		anna.addXP(1000);
		assertEquals(1000, anna.getXP());
	}

	private Glyph knowledge(final Player owner) {
		final Glyph glyph = new Glyph("glif wiedzy", "glyph", "glyph_knowledge", null);
		glyph.setBoundTo(owner.getName());
		return glyph;
	}

	@Test
	public void knowledgeRoundsOnceAndCannotOverflowOrIncreaseNegativeExperience() {
		final Player player = PlayerTestHelper.createPlayer("knowledge-rounding");
		player.getSlot("utility_rune").add(knowledge(player));
		assertEquals(6, games.stendhal.server.core.rule.glyph.GlyphExperienceService.applyCombatBonus(player, 5));
		assertEquals(Integer.MAX_VALUE, games.stendhal.server.core.rule.glyph.GlyphExperienceService.applyCombatBonus(player, Integer.MAX_VALUE));
		assertEquals(0, games.stendhal.server.core.rule.glyph.GlyphExperienceService.applyCombatBonus(player, 0));
		assertEquals(-100, games.stendhal.server.core.rule.glyph.GlyphExperienceService.applyCombatBonus(player, -100));
	}

	private Player addPlayer(final String name, final StendhalRPZone zone) {
		final Player player = PlayerTestHelper.createPlayer(name);
		ruleProcessor.addPlayer(player);
		zone.add(player);
		return player;
	}

	private static final class RewardTestEntity extends RPEntity {
		private void addContribution(final Player player, final int damage) {
			damageReceived.add(player, damage);
			totalDamageReceived += damage;
		}

		private void rewardContributors(final int xp) {
			rewardKillers(xp);
		}

		@Override
		protected void dropItemsOn(final Corpse corpse) {
			// no drops needed
		}

		@Override
		public void logic() {
			// no behaviour needed
		}
	}
}
