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
	public void combatExperienceUsesRebornBonus() {
		final StendhalRPZone combatZone = new StendhalRPZone("reborn_exp_combat", 20, 20);
		final Player anna = addPlayer("anna", combatZone);
		anna.put(RebornSystem.ATTR_REBORNS, 2);

		final RewardTestEntity defeated = new RewardTestEntity();
		defeated.setName("test target");
		defeated.addContribution(anna, 100);

		// 5 percent of 8880 is 444. Two reborns add 10 percent, rounded to 488.
		defeated.rewardContributors(8880);
		assertEquals(488, anna.getXP());
	}

	@Test
	public void directExperienceDoesNotUseRebornCombatBonus() {
		final Player anna = PlayerTestHelper.createPlayer("direct-exp");
		anna.put(RebornSystem.ATTR_REBORNS, 2);

		anna.addXP(444);
		assertEquals(444, anna.getXP());
	}

	@Test
	public void equalModeAppliesRebornBonusAfterSplittingForEachRecipient() {
		final StendhalRPZone combatZone = new StendhalRPZone("reborn_equal_exp", 20, 20);
		final Player anna = addPlayer("anna", combatZone);
		final Player bartek = addPlayer("bartek", combatZone);
		final Player celina = addPlayer("celina", combatZone);

		anna.put(RebornSystem.ATTR_REBORNS, 2);
		bartek.put(RebornSystem.ATTR_REBORNS, 1);

		group = SingletonRepository.getGroupManager().createGroup(anna.getName());
		group.addMember(bartek.getName());
		group.addMember(celina.getName());
		group.setExpmode("equal");

		final RewardTestEntity defeated = new RewardTestEntity();
		defeated.setName("test target");
		defeated.addContribution(anna, 60);
		defeated.addContribution(bartek, 40);
		defeated.rewardContributors(2000);

		// Base shares are 34, 33 and 33. Bonuses are then applied per recipient.
		assertEquals(37, anna.getXP());
		assertEquals(35, bartek.getXP());
		assertEquals(33, celina.getXP());
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
