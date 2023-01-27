/***************************************************************************
 *                     Copyright © 2023 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp.achievement.factory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static utilities.PlayerTestHelper.createPlayer;

import org.junit.Before;
import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import utilities.AchievementTestHelper;

public class ItemAchievementFactoryTest extends AchievementTestHelper {
	private final Player player;

	public ItemAchievementFactoryTest() {
		player = createPlayer("player");
	}

	@Before
	public void setUp() {
		assertNotNull(player);
		init(player);
	}

	private void checkSingleItemLoot(final String id, final String item,
			final int amount, final int inc) {
		assertTrue(achievementEnabled(id));
		while (player.getNumberOfLootsForItem(item) < amount) {
			assertFalse(achievementReached(player, id));
			player.incLootForItem(item, inc);
		}
		assertTrue(achievementReached(player, id));
	}

	private void checkSingleItemLoot(final String id, final String item,
			final int amount) {
		checkSingleItemLoot(id, item, amount, 1);
	}

	private void checkItemSetLoot(final String id, final String[] items) {
		assertTrue(achievementEnabled(id));
		for (final String item: items) {
			assertFalse(achievementReached(player, id));
			player.incLootForItem(item, 1);
		}
		assertTrue(achievementReached(player, id));
	}

	@Test
	public void testFirstPocketMoney() {
		checkSingleItemLoot("item.money.100", "money", 100, 10);
	}

	@Test
	public void testGoldshower() {
		checkSingleItemLoot("item.money.10000", "money", 10000, 100);
	}

	@Test
	public void testMovingUpInTheWorld() {
		checkSingleItemLoot("item.money.100000", "money", 100000, 1000);
	}

	@Test
	public void testYouDontNeedItAnymore() {
		checkSingleItemLoot("item.money.1000000", "money", 1000000, 10000);
	}

	@Test
	public void testCheeseWiz() {
		checkSingleItemLoot("item.cheese.2000", "cheese", 2000, 100);
	}

	@Test
	public void testHamHocks() {
		checkSingleItemLoot("item.ham.2500", "ham", 2500, 100);
	}

	@Test
	public void testAmazonsMenace() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_RED, ItemSetsAchievementFactory.RED);
	}

	@Test
	public void testFeelingBlue() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_BLUE, ItemSetsAchievementFactory.BLUE);
	}

	@Test
	public void testNalworsBane() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_ELVISH, ItemSetsAchievementFactory.ELVISH);
	}

	@Test
	public void testShadowDweller() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_SHADOW, ItemSetsAchievementFactory.SHADOW);
	}

	@Test
	public void testChaoticLooter() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_CHAOS, ItemSetsAchievementFactory.CHAOS);
	}

	@Test
	public void testGoldenBoy() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_GOLDEN, ItemSetsAchievementFactory.GOLDEN);
	}

	@Test
	public void testComeToTheDarkSide() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_BLACK, ItemSetsAchievementFactory.BLACK);
	}

	@Test
	public void testExcellentStuff() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_MAINIO, ItemSetsAchievementFactory.MAINIO);
	}

	@Test
	public void testABitXeno() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_XENO, ItemSetsAchievementFactory.XENO);
	}

	@Test
	public void testDragonSlayer() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_DRAGON_CLOAKS, ItemSetsAchievementFactory.DRAGON_CLOAKS);
	}

	@Test
	public void testRoyallyEndowed() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_ROYAL, ItemSetsAchievementFactory.ROYAL);
	}

	@Test
	public void testMagicSupplies() {
		checkItemSetLoot(ItemSetsAchievementFactory.ID_MAGIC, ItemSetsAchievementFactory.MAGIC);
	}
}
