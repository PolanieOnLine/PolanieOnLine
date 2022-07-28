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
package games.stendhal.server.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for TimeUtil.
 */
public class TimeUtilTest {

	/**
	 * tests for timeUtil()
	 */
	@Test
	public void testTimeUtil() {
		Assert.assertEquals("5 sekundy", TimeUtil.timeUntil(5, true));
		Assert.assertEquals("7 minut, 1 sekundy", TimeUtil.timeUntil(421, true));
		Assert.assertEquals("22 godziny, 59 minut, 49 sekundy", TimeUtil.timeUntil(82789, true));
		Assert.assertEquals("11 tygodni, 20 godziny, 58 minut, 2 sekundy", TimeUtil.timeUntil(6728282, true));
		Assert.assertEquals("138 tygodni, 3 dni, 4 godziny, 6 minut, 12 sekundy", TimeUtil.timeUntil(83736372, true));
	}

	/**
	 * Tests for approxTimeUntil.
	 */
	@Test
	public void testApproxTimeUntil() {
		Assert.assertEquals("mniej niż minutę", TimeUtil.approxTimeUntil(5));
		Assert.assertEquals("7 minut", TimeUtil.approxTimeUntil(421));
		Assert.assertEquals("23 godziny", TimeUtil.approxTimeUntil(82789));
		Assert.assertEquals("ponad 11 tygodni", TimeUtil.approxTimeUntil(6728282));
		Assert.assertEquals("około 138 i pół tygodni", TimeUtil.approxTimeUntil(83736372));
	}

}
