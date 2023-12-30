/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import java.util.Arrays;

import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.KillCreaturesQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

/**
 * QUEST: CleanStorageSpace
 * <p>
 * PARTICIPANTS:
 * <li> Eonna
 * <p>
 * STEPS:
 * <li> Eonna asks you to clean her storage space.
 * <li> You go kill at least a rat, a cave rat and a cobra.
 * <li> Eonna checks your kills and then thanks you.
 * <p>
 * REWARD:
 * <li> 500 XP, karma
 * <p>
 * REPETITIONS:
 * <li> None.
 */
public class CleanStorageSpace implements QuestManuscript {
	public KillCreaturesQuestBuilder story() {
		KillCreaturesQuestBuilder quest = new KillCreaturesQuestBuilder();

		quest.info()
			.name("Porządki w Piwnicy")
			.description("W piwnicy Eonny zagnieździły się szczury i węże. Potrzebuje mnie, prawdziwego bohatera, abym pomógł jej.")
			.internalName("clean_storage")
			.notRepeatable()
			.minLevel(0)
			.region(Region.SEMOS_CITY)
			.questGiverNpc("Eonna");

		quest.history()
			.whenNpcWasMet("Eonna napotkana została w jej domku, blisko piekarni.")
			.whenQuestWasRejected("Nie zajmuję się sprzątaniem piwnic, mam lepsze zajęcia do roboty.")
			.whenQuestWasAccepted("Nie mogę odmówić Eonnie pomocy w pozbyciu się szczurów i węży z jej piwnicy.")
			.whenTaskWasCompleted("Piwnica Eonny stała się wolna od groźnych szkodników.")
			.whenQuestWasCompleted("Eonna podziękowała mi i nazwała mnie swoim bohaterem.");

		quest.offer()
			.respondToRequest("Moja #piwnica jest pełna szkodników. Pomożesz mi nieznajomy?")
			.respondToUnrepeatableRequest("Dziękuję jeszcze raz! Sądzę, że na dole jest nadal czysto.")
			.respondToAccept("Och, dziękuję! Poczekam tutaj, a jeżeli spróbują uciec to uderzę je moją miotłą!")
			.respondToReject("*chlip* Cóż, może ktoś inny będzie moim bohaterem...")
			.respondTo("basement", "storage space", "piwnica", "piwnicy").saying("Tak, idź na dół po schodach. Tam jest cała gromada obrzydliwych szczurów. Chyba widziałam tam też węża. Powinieneś uważać... Wciąż chcesz mi pomóc?")
			.remind("Nie pamiętasz, że obiecałeś mi pomóc w oczyszczeniu mojej #piwnicy ze szczurów?");

		final SpeakerNPC npc = NPCList.get().get("Eonna");
		npc.addReply(Arrays.asList("basement", "piwnicy"), "W dół po schodach, jak mówiłam. Proszę, pozbądź się tych wszystkich szczurów i zobacz, czy nie uda ci się znaleźć również węża!");

		quest.task()
			.requestKill(1, "szczur")
			.requestKill(1, "szczur jaskiniowy")
			.requestKill(1, "wąż");

		quest.complete()
			.greet("Nareszcie! Mój bohater się odnalazł, dziękuję!")
			.rewardWith(new IncreaseXPAction(100))
			.rewardWith(new IncreaseKarmaAction(5.0))
			.rewardWith(new EquipItemAction("buteleczka wody", 5));

		return quest;
	}
}
