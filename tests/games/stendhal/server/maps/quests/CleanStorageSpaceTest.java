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
package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.maps.semos.storage.HousewifeNPC;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

public class CleanStorageSpaceTest extends ZonePlayerAndNPCTestImpl {

	private static final String ZONE_NAME = "testzone";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone(ZONE_NAME);
	}

	public CleanStorageSpaceTest() {
		setNpcNames("Eonna");
		setZoneForPlayer(ZONE_NAME);
		addZoneConfigurator(new HousewifeNPC(), ZONE_NAME);
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		final CleanStorageSpace cf = new CleanStorageSpace();
		cf.addToWorld();
	}

	/**
	 * Tests for hiAndbye.
	 */
	@Test
	public void testHiAndbye() {
		assertTrue(!player.hasKilled("szczur"));

		final SpeakerNPC npc = getNPC("Eonna");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertTrue(npc.isTalking());
		assertEquals("Witaj młody bohaterze.", getReply(npc));
		assertTrue(en.step(player, "job"));
		assertTrue(npc.isTalking());
		assertEquals("Jestem gospodynią domową.", getReply(npc));
		assertTrue(en.step(player, "help"));
		assertTrue(npc.isTalking());
		assertEquals("Uwielbiam pomagać Landerowi. Jego kanapki są wspaniałe! Czy wiesz, że szuka pomocnika?",
				getReply(npc));
		assertTrue(en.step(player, "bye"));
		assertFalse(npc.isTalking());
		assertEquals("Do widzenia.", getReply(npc));
	}

	@Test
	public void doQuest() {
		final SpeakerNPC npc = getNPC("Eonna");
		final Engine en = npc.getEngine();
		assertFalse(npc.isTalking());

		assertTrue(en.step(player, "hi"));
		assertTrue(npc.isTalking());
		assertEquals("Witaj młody bohaterze.", getReply(npc));
		assertTrue(en.step(player, "task"));
		assertTrue(npc.isTalking());
		assertEquals(
				"Moja #piwnica jest pełna szczurów. Pomożesz mi?",
				getReply(npc));
		assertTrue(en.step(player, "basement"));
		assertTrue(npc.isTalking());
		assertEquals(
				"Tak, idź na dół po schodach. Tam jest cała gromada obrzydliwych szczurów. Chyba widziałam tam też węża. Powinieneś uważać... wciąż chcesz mi pomóc?",
				getReply(npc));
		assertTrue(en.step(player, "yes"));
		assertEquals(
				"Och, dziękuję! Poczekam tutaj, a jeżeli spróbują uciec to uderzę je moją miotłą!",
				getReply(npc));
		assertTrue(en.step(player, "bye"));
		assertFalse(npc.isTalking());
		assertEquals("Do widzenia.", getReply(npc));
		player.setSoloKill("szczur");
		assertTrue(player.hasKilled("szczur"));
		player.setSharedKill("szczur jaskiniowy");
		player.setSharedKill("wąż");
		assertTrue(en.step(player, "hi"));
		assertTrue(npc.isTalking());
		assertEquals("Nareszcie bohater! Dziękuję!", getReply(npc));

		assertEquals("done", player.getQuest("clean_storage"));
	}

}
