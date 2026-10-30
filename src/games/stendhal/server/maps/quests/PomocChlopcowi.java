/***************************************************************************
 *                 (C) Copyright 2019-2023 - PolanieOnLine                 *
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

import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.KillCreaturesQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;
import games.stendhal.server.core.rule.quest.StarterQuestLine;

public class PomocChlopcowi implements QuestManuscript {
	public KillCreaturesQuestBuilder story() {
		KillCreaturesQuestBuilder quest = new KillCreaturesQuestBuilder();

		quest.info()
			.name("Pomoc Adasiowi")
            .mainQuest(StarterQuestLine.CHAPTER, 2, StarterQuestLine.ADAS)
			.description("Adaś potrzebuje pomocy w oczyszczeniu piwnicy domku bohaterów, aby rodzina mogła przygotować miejsce na zimowe zapasy.")
			.internalName("pomoc_adasiowi")
			.notRepeatable()
			.minLevel(0)
			.region(Region.ZAKOPANE_CITY)
			.questGiverNpc("Adaś");

		quest.history()
			.whenNpcWasMet("Napotkany został młody chłopiec Adaś w domku bohaterów.")
			.whenQuestWasRejected("Nie zajmuję się sprzątaniem piwnic, mam lepsze zajęcia do roboty.")
			.whenQuestWasAccepted("Nie mogę odmówić chłopcowi pomocy w pozbyciu się szczurów i węży z piwnicy.")
			.whenTaskWasCompleted("Piwnica w domku bohaterów stała się wolna od groźnych szkodników.")
			.whenQuestWasCompleted("Adaś podziękował za oczyszczenie piwnicy na zimowe zapasy. Wspomniał o Fryderyku w Zakopanem S.");

		quest.offer()
			.respondToRequest("Mamcia chce zrobić miejsce na zimowe zapasy, ale nasza #piwnica jest pełna szczurów i węży. Boję się zejść po konfitury. Pomożesz mi?")
			.respondToUnrepeatableRequest("Pięknie dziękuję! Myślę, że na dole jest nadal czysto.")
			.respondToAccept("Poczekam tutaj, a jeżeli spróbują uciec to ciupagą walnę!")
			.respondToReject("*chlip* Cóż, może ktoś inny będzie moim wybawcą...")
			.respondTo("basement", "storage space", "piwnica", "piwnicy").saying("Tak, *chlip* idź na dół po schodach. Tam jest cała gromada obrzydliwych szczurów. Chyba widziałem tam też węża. Powinieneś uważać... Pomożesz mi?")
			.remind("Obiecałeś oczyścić tę piwnice ze szczurów! *chlip*");

		quest.task()
			.requestKill(1, "szczur")
			.requestKill(1, "szczur jaskiniowy")
			.requestKill(1, "wąż");

		quest.complete()
			.greet("Nasz wybawca! Nareszcie możemy przygotować piwnicę na zapasy. Dziękuję! Fryderyk w południowej części Zakopanego też szykuje się do zimy. Może potrzebuje pomocy przy żniwach? Zapytaj go o zadanie.")
			.rewardWith(new IncreaseXPAction(250))
			.rewardWith(new IncreaseKarmaAction(5.0))
			.rewardWith(new EquipItemAction("buty skórzane", 1, false));

		return quest;
	}
}
