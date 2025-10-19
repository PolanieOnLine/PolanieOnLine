/***************************************************************************
 *                   (C) Copyright 2003-2016 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LevelTest {

	/**
	 * Tests for maxLevel.
	 */
	@Test
	public final void testMaxLevel() {
		assertEquals(1000, Level.maxLevel());
	}

	/**
	 * Tests for getLevel.
	 */
	@Test
	public final void testGetLevel() {
		// assertELevel.getLevel()
	}

	/**
	 * Tests for getXP.
	 */
	@Test
	public final void testGetXP() {
		assertEquals(0L, Level.getXP(0));
		assertEquals(50L, Level.getXP(1));
		assertEquals(9_753_800L, Level.getXP(100));

		assertEquals(9_975_036_200L, Level.getXP(Level.maxLevel()));
	}

	/**
	 * Tests for getNegativeXP.
	 */
	@Test
	public final void testGetNegativeXP() {
		assertEquals(-1L, Level.getXP(-1));
		assertEquals(-1L, Level.getXP(-10));
	}

	/**
	 * Tests for getMoreThanMaxXP.
	 */
	@Test
	public final void testGetMoreThanMaxXP() {
		assertEquals(10_005_016_300L, Level.getXP(Level.maxLevel() + 1));
		assertEquals(-1L, Level.getXP(Level.maxLevel() + 2));
	}

	/**
	 * Tests for changeLevel.
	 */
	@Test
	public final void testChangeLevel() {
		assertEquals(0, Level.changeLevel(0, 49));

		assertEquals(1, Level.changeLevel(0, 50));
		assertEquals(1, Level.changeLevel(50, 100));
		assertEquals(2, Level.changeLevel(0, 100));
		assertEquals(Level.maxLevel() - 1, Level.changeLevel(0,
				Level.getXP(Level.maxLevel() - 1)));
		assertEquals(Level.maxLevel(), Level.changeLevel(0,
				Level.getXP(Level.maxLevel())));
	}

	/**
	 * Tests for getWisdom.
	 */
	@Test
	public final void testGetWisdom() {
		assertEquals(0.0, Level.getWisdom(0), 0.001);
		assertEquals(0.9999522881542902, Level.getWisdom(Level.maxLevel()), 0.001);
	}

	/**
	 * Tests for getWisdomOverMaxlevel.
	 */
	@Test
	public final void testGetWisdomOverMaxlevel() {
		assertEquals(Level.getWisdom(Level.maxLevel()),
				Level.getWisdom(Level.maxLevel() + 1), 0.001);
	}

}
