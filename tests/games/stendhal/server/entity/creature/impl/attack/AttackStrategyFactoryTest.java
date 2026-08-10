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
package games.stendhal.server.entity.creature.impl.attack;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AttackStrategyFactoryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/** Tests for getArcher. */
	@Test
	public void testGetArcher() {
		final Map<String, String> profiles = new HashMap<String, String>();
		assertWrappedDelegate(profiles, HandToHand.class);
		profiles.put("archer", null);
		assertWrappedDelegate(profiles, RangeAttack.class);
	}

	/** Tests for getGandhi. */
	@Test
	public void testGetGandhi() {
		final Map<String, String> profiles = new HashMap<String, String>();
		assertWrappedDelegate(profiles, HandToHand.class);
		profiles.put("gandhi", null);
		assertWrappedDelegate(profiles, Gandhi.class);
	}

	/** Tests for getCoward. */
	@Test
	public void testGetCoward() {
		final Map<String, String> profiles = new HashMap<String, String>();
		assertWrappedDelegate(profiles, HandToHand.class);
		profiles.put("coward", null);
		assertWrappedDelegate(profiles, Coward.class);
	}

	/** Tests for getStupidCoward. */
	@Test
	public void testGetStupidCoward() {
		final Map<String, String> profiles = new HashMap<String, String>();
		assertWrappedDelegate(profiles, HandToHand.class);
		profiles.put("stupid coward", null);
		assertWrappedDelegate(profiles, StupidCoward.class);
	}

	/** Tests for getting AttackWeakest profile. */
	@Test
	public void testGetAttackWeakest() {
		final Map<String, String> profiles = new HashMap<String, String>();
		assertWrappedDelegate(profiles, HandToHand.class);
		profiles.put("attack weakest", null);
		assertWrappedDelegate(profiles, AttackWeakest.class);
	}

	private void assertWrappedDelegate(final Map<String, String> profiles,
			final Class<? extends AttackStrategy> expectedDelegate) {
		final AttackStrategy strategy = AttackStrategyFactory.get(profiles);
		assertTrue(strategy instanceof StunAwareAttackStrategy);
		assertTrue(expectedDelegate.isInstance(
				((StunAwareAttackStrategy) strategy).getDelegate()));
	}
}
