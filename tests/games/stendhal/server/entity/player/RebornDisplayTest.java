/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************/
package games.stendhal.server.entity.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.common.Level;
import utilities.PlayerTestHelper;

public class RebornDisplayTest {

	@Test
	public void syncPublishesPositiveRebornCount() {
		final Player player = PlayerTestHelper.createPlayer("reborn-badge");
		player.put(RebornSystem.ATTR_REBORNS, 7);

		RebornDisplay.sync(player);

		assertTrue(player.has(RebornDisplay.ATTR_REBORN_BADGE));
		assertEquals(7, player.getInt(RebornDisplay.ATTR_REBORN_BADGE));
	}

	@Test
	public void syncHidesBadgeWithoutReborns() {
		final Player player = PlayerTestHelper.createPlayer("no-reborn-badge");
		player.put(RebornDisplay.ATTR_REBORN_BADGE, 4);
		player.put(RebornSystem.ATTR_REBORNS, 0);

		RebornDisplay.sync(player);

		assertFalse(player.has(RebornDisplay.ATTR_REBORN_BADGE));
	}

	@Test
	public void performRebornUpdatesBadgeImmediately() {
		final Player player = PlayerTestHelper.createPlayer("reborn-badge-update");
		player.put(RebornSystem.ATTR_REBORNS, 1);
		player.put(RebornSystem.ATTR_REWARDS, 7);
		player.setLevel(Level.maxLevel());
		player.setBaseHP(100 + Level.maxLevel() * 10 + 1000);
		player.setHP(player.getBaseHP());

		assertEquals(2, RebornSystem.performReborn(player));
		assertEquals(2, player.getInt(RebornDisplay.ATTR_REBORN_BADGE));
	}

	@Test
	public void loginMigrationRestoresVolatileBadgeEveryTime() {
		final Player player = PlayerTestHelper.createPlayer("reborn-badge-login");
		player.put(RebornSystem.ATTR_REBORNS, 6);
		player.put(RebornSystem.ATTR_REWARDS, 7);

		RebornSystem.migrateLegacyData(player);
		assertEquals(6, player.getInt(RebornDisplay.ATTR_REBORN_BADGE));

		player.remove(RebornDisplay.ATTR_REBORN_BADGE);
		assertFalse(player.has(RebornDisplay.ATTR_REBORN_BADGE));

		RebornSystem.migrateLegacyData(player);
		assertEquals(6, player.getInt(RebornDisplay.ATTR_REBORN_BADGE));
	}
}
