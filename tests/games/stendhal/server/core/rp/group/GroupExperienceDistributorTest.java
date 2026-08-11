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
package games.stendhal.server.core.rp.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class GroupExperienceDistributorTest {
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
	public void splitPreservesTotalExperienceAndUsesStableRemainder() {
		final Player anna = PlayerTestHelper.createPlayer("anna");
		final Player bartek = PlayerTestHelper.createPlayer("bartek");
		final Player celina = PlayerTestHelper.createPlayer("celina");
		final Map<Player, Integer> shares = GroupExperienceDistributor.splitEqually(
				Arrays.asList(anna, bartek, celina), 100);

		assertEquals(Integer.valueOf(34), shares.get(anna));
		assertEquals(Integer.valueOf(33), shares.get(bartek));
		assertEquals(Integer.valueOf(33), shares.get(celina));
		assertEquals(100, shares.values().stream().mapToInt(Integer::intValue).sum());
	}

	@Test
	public void splitDoesNotCreateExperienceWhenRewardIsSmallerThanGroup() {
		final Player anna = PlayerTestHelper.createPlayer("anna");
		final Player bartek = PlayerTestHelper.createPlayer("bartek");
		final Player celina = PlayerTestHelper.createPlayer("celina");
		final Map<Player, Integer> shares = GroupExperienceDistributor.splitEqually(
				Arrays.asList(anna, bartek, celina), 2);

		assertEquals(Integer.valueOf(1), shares.get(anna));
		assertEquals(Integer.valueOf(1), shares.get(bartek));
		assertFalse(shares.containsKey(celina));
		assertEquals(2, shares.values().stream().mapToInt(Integer::intValue).sum());
	}

	@Test
	public void equalModeOnlyIncludesOnlineMembersInTheSameZone() {
		final StendhalRPZone combatZone = new StendhalRPZone("group_exp_combat", 20, 20);
		final StendhalRPZone otherZone = new StendhalRPZone("group_exp_other", 20, 20);
		final Player anna = PlayerTestHelper.createPlayer("anna");
		final Player bartek = PlayerTestHelper.createPlayer("bartek");
		final Player celina = PlayerTestHelper.createPlayer("celina");
		final Player offline = PlayerTestHelper.createPlayer("offline");
		ruleProcessor.addPlayer(anna);
		ruleProcessor.addPlayer(bartek);
		ruleProcessor.addPlayer(celina);
		combatZone.add(anna);
		combatZone.add(bartek);
		otherZone.add(celina);

		group = SingletonRepository.getGroupManager().createGroup(anna.getName());
		group.addMember(bartek.getName());
		group.addMember(celina.getName());
		group.addMember(offline.getName());
		group.setExpmode("equal");

		final List<Player> eligible = group.getOnlineMembersInSameZone(anna);
		assertEquals(Arrays.asList(anna, bartek), eligible);
	}
}
