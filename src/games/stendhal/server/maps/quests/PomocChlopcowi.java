/***************************************************************************
 *                 (C) Copyright 2019-2022 - PolanieOnLine                 *
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
import games.stendhal.server.entity.npc.quest.KillCreaturesTask;
import games.stendhal.server.entity.npc.quest.QuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class PomocChlopcowi implements QuestManuscript {
	public QuestBuilder<?> story() {
		QuestBuilder<KillCreaturesTask> quest = new QuestBuilder<>(new KillCreaturesTask());

		quest.info()
			.name("Pomoc Adasiowi")
			.description("W domku bohaterów w piwnicy zakorzeniły się potwory. Młody chłopiec Adaś, potrzebuje mojej pomocy w pozbyciu się ich.")
			.internalName("pomoc_adasiowi")
			.repeatable(false)
			.region(Region.ZAKOPANE_CITY)
			.questGiverNpc("Adaś");

		quest.history()
			.whenNpcWasMet("Napotkany został młody chłopiec Adaś w domku bohaterów.")
			.whenQuestWasRejected("Nie zajmuję się sprzątaniem piwnic, mam lepsze zajęcia do roboty.")
			.whenQuestWasAccepted("Nie mogę odmówić chłopcowi pomocy w pozbyciu się szczurów i węży z piwnicy.")
			.whenTaskWasCompleted("Piwnica w domku bohaterów stała się wolna od groźnych szkodników.")
			.whenQuestWasCompleted("Adaś podziękował mi i nazwał mnie swym wybawcą.");

		quest.offer()
			.respondToRequest("Mamcia kazała mi przynieść konfitury ale #piwnica jest pełna szczurów. Pomożesz mi?")
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
			.greet("Nasz wybawca! Nareszcie! Dziękuję!")
			.rewardWith(new IncreaseXPAction(550))
			.rewardWith(new IncreaseKarmaAction(5.0))
			.rewardWith(new EquipItemAction("buty skórzane"));

		return quest;
	}
}
