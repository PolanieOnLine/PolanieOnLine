/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;

public class KilledConditionTest {

	/**
	 * Tests for hashCode.
	 */
	@Test
	public final void testHashCode() {

		assertEquals(new KilledCondition("szczur").hashCode(),
				new KilledCondition("szczur").hashCode());
		assertEquals("i would expect this equal", new KilledCondition("szczur",
				"mouse").hashCode(),
				new KilledCondition("mouse", "szczur").hashCode());

	}

	/**
	 * Tests for fire.
	 */
	@Test
	public final void testFire() {
		KilledCondition kc = new KilledCondition();
		assertTrue(kc.fire(null, null, null));
		Player bob = PlayerTestHelper.createPlayer("player");

		assertTrue("bob has killed all of none", kc.fire(bob, null, null));
		kc = new KilledCondition("szczur");
		assertFalse(kc.fire(bob, null, null));
		bob.setSoloKill("szczur");
		assertTrue("bob killed a szczur ", kc.fire(bob, null, null));

		bob = PlayerTestHelper.createPlayer("player");
		new KilledCondition(Arrays.asList("szczur"));
		assertFalse(kc.fire(bob, null, null));
		bob.setSoloKill("szczur");
		assertTrue("bob killed a szczur ", kc.fire(bob, null, null));
	}

	/**
	 * Tests for toString.
	 */
	@Test
	public final void testToString() {
		final KilledCondition kc = new KilledCondition("szczur");
		assertEquals("KilledCondition <[szczur]>", kc.toString());
	}

	/**
	 * Tests for equalsObject.
	 */
	@Test
	public final void testEqualsObject() {
		assertEquals(new KilledCondition("szczur"), new KilledCondition("szczur"));
		assertEquals("i would expect this equal", new KilledCondition("szczur",
				"mouse"), new KilledCondition("mouse", "szczur"));
	}

}
