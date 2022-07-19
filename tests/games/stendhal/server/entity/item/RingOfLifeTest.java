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
package games.stendhal.server.entity.item;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Log4J;
import utilities.RPClass.ItemTestHelper;

public class RingOfLifeTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();

	}
	/**
	 * Tests for describe.
	 */
	@Test
	public void testDescribe() {
		final RingOfLife ring = new RingOfLife();
		assertThat(ring.isBroken(), is(false));
		assertThat(ring.describe(), is("Oto §'pierścień szmaragdowy'. Noś go, a uchroni Cię przed konsekwencjami związanymi ze śmiercią."));

		ring.damage();
		assertThat(ring.isBroken(), is(true));
		assertThat(ring.describe(), is("Oto §'pierścień szmaragdowy'. Kamień stracił blask i nie posiada mocy."));
	}

	/**
	 * Tests for onUsed.
	 */
	@Test
	public void testOnUsed() {
		final RingOfLife ring = new RingOfLife();
		assertThat(ring.isBroken(), is(false));
		assertThat(ring.describe(), is("Oto §'pierścień szmaragdowy'. Noś go, a uchroni Cię przed konsekwencjami związanymi ze śmiercią."));

		ring.onUsed(null);
		assertThat(ring.isBroken(), is(false));
		assertThat(ring.describe(), is("Oto §'pierścień szmaragdowy'. Noś go, a uchroni Cię przed konsekwencjami związanymi ze śmiercią."));
	}



	/**
	 * Tests for repair.
	 */
	@Test
	public void testRepair() {
		final RingOfLife ring = new RingOfLife();
		assertThat(ring.isBroken(), is(false));
		assertThat(ring.describe(), is("Oto §'pierścień szmaragdowy'. Noś go, a uchroni Cię przed konsekwencjami związanymi ze śmiercią."));

		ring.damage();
		assertThat(ring.isBroken(), is(true));
		assertThat(ring.describe(), is("Oto §'pierścień szmaragdowy'. Kamień stracił blask i nie posiada mocy."));

		ring.repair();
		assertThat(ring.isBroken(), is(false));
		assertThat(ring.describe(), is("Oto §'pierścień szmaragdowy'. Noś go, a uchroni Cię przed konsekwencjami związanymi ze śmiercią."));
	}

}
