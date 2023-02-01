/***************************************************************************
 *                    Copyright © 2020-2023 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package utilities.quest_runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static utilities.SpeakerNPCTestHelper.getSpeakerNPC;

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.QuestUtils;

public class StillBelievingStub {
	public static void doQuestBunny(final Player player) {
		final String questSlot = QuestUtils.evaluateQuestSlotName("meet_bunny_[year]");
		assertNull(player.getQuest(questSlot));
		final SpeakerNPC bunny = getSpeakerNPC("Zajączek Wielkanocny");
		assertNotNull(bunny);
		final Engine en = bunny.getEngine();
		en.step(player, "hi");
		assertEquals(ConversationStates.IDLE, en.getCurrentState());
		assertEquals("done", player.getQuest(questSlot, 0));
	}

	public static void doQuestSanta(final Player player) {
		final String questSlot = QuestUtils.evaluateQuestSlotName("meet_santa_[seasonyear]");
		assertNull(player.getQuest(questSlot));
		final SpeakerNPC santa = getSpeakerNPC("Święty Mikołaj");
		assertNotNull(santa);
		final Engine en = santa.getEngine();
		en.step(player, "hi");
		assertEquals(ConversationStates.IDLE, en.getCurrentState());
		assertEquals(
			"Wesołych Świąt! Mam prezent i czapkę dla Ciebie. Do widzenia i pamiętaj,"
					+ " aby być grzecznym jeżeli chcesz dostąc prezent w przyszłym roku!",
			utilities.SpeakerNPCTestHelper.getReply(santa));
		assertEquals("done", player.getQuest(questSlot));
	}

	public static void doQuestGuslarz(final Player player) {
		final String questSlot = QuestUtils.evaluateQuestSlotName("meet_guslarz_[seasonyear]");
		assertNull(player.getQuest(questSlot));
		final SpeakerNPC guslarz = getSpeakerNPC("Guślarz");
		assertNotNull(guslarz);
		final Engine en = guslarz.getEngine();
		en.step(player, "hi");
		assertEquals(ConversationStates.IDLE, en.getCurrentState());
		assertEquals("done", player.getQuest(questSlot, 0));
	}
}
