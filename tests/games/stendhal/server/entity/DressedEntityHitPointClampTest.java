/***************************************************************************
 *                   (C) Copyright 2003-2026 - PolanieOnLine               *
 ***************************************************************************/
package games.stendhal.server.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;

public class DressedEntityHitPointClampTest {

	@Test
	public void clampsBaseAndCurrentHpBeforeWritingShortAttributes() {
		final Player player = PlayerTestHelper.createPlayer("hp_clamp");
		final int tooHigh = Short.MAX_VALUE + 1000;

		player.setBaseHP(tooHigh);
		player.setHP(tooHigh);

		assertEquals(Short.MAX_VALUE, player.getBaseHP());
		assertEquals(Short.MAX_VALUE, player.getHP());
		assertEquals(Short.MAX_VALUE, player.getInt("base_hp"));
		assertEquals(Short.MAX_VALUE, player.getInt("hp"));
	}

	@Test
	public void levelUpCannotPushHpPastShortMaximum() {
		final Player player = PlayerTestHelper.createPlayer("hp_level_clamp");
		player.setBaseHP(Short.MAX_VALUE - 5);
		player.setHP(Short.MAX_VALUE - 5);

		player.addXP(50);

		assertEquals(1, player.getLevel());
		assertEquals(Short.MAX_VALUE, player.getBaseHP());
		assertEquals(Short.MAX_VALUE, player.getHP());
		assertEquals(Short.MAX_VALUE, player.getInt("base_hp"));
		assertEquals(Short.MAX_VALUE, player.getInt("hp"));
	}
}
