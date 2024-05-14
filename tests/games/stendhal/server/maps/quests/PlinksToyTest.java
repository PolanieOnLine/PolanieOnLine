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
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.semos.plains.LittleBoyNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.PassiveEntityRespawnPointTestHelper;

public class PlinksToyTest {
	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		PassiveEntityRespawnPointTestHelper.generateRPClasses();
	}

	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone("0_semos_plains_n");
		MockStendlRPWorld.get().addRPZone(zone);
		new LittleBoyNPC().configureZone(zone, null);

		final AbstractQuest quest = new BuiltQuest(new PlinksToy().story());
		quest.addToWorld();

		player = PlayerTestHelper.createPlayer("player");
	}

	/**
	 * Tests for quest.
	 */
	@Test
	public void testQuest() {
		npc = SingletonRepository.getNPCList().get("Plink");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("*płacze* W parku były wilki! *pociągnięcie nosem* Uciekłem, ale upuściłem mojego pluszaka! Czy możesz go dla mnie znaleźć? *pociągnięcie nosem* Proszę?", getReply(npc));
		en.step(player, "miś");
		assertEquals("Pluszak to moja ulubiona zabawka! Czy możesz go dla mnie przynieść?", getReply(npc));
		en.step(player, "no");
		assertEquals("*pociągnięcie nosem* Ale... ale... PROSZĘ! *płacze*", getReply(npc));
		en.step(player, "bye");

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("*płacze* W parku były wilki! *pociągnięcie nosem* Uciekłem, ale upuściłem mojego pluszaka! Czy możesz go dla mnie znaleźć? *pociągnięcie nosem* Proszę?", getReply(npc));
		en.step(player, "park");
		assertEquals("Rodzice mi mówili, żebym nie chodził do parku sam, ale zgubiłem się, gdy się bawiłem... Proszę, nie mów im! Czy możesz przynieść mi mojego pluszaka z powrotem?", getReply(npc));
		en.step(player, "yes");
		assertEquals("*pociągnięcie nosem* Dziękuję bardzo! *uśmiech*", getReply(npc));
		en.step(player, "bye");

		// player.setQuest("plink_toy", "done");

		// -----------------------------------------------
	}
}
