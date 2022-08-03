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

import java.util.Arrays;

import games.stendhal.common.MathHelper;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseBaseHPOnlyOnceAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.KillCreaturesTask;
import games.stendhal.server.entity.npc.quest.QuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class ProsbaWielkoluda implements QuestManuscript {
	final static String QUEST_SLOT = "help_wielkolud";
	@Override
	public QuestBuilder<?> story() {
		QuestBuilder<KillCreaturesTask> quest = new QuestBuilder<>(new KillCreaturesTask());

		quest.info()
			.name("Wsparcie dla Wielkoluda")
			.description("Wielkolud poszukuje wojaka, który pozbędzie się grasujących pokutników i lawiny kamiennej z przejścia, co utrudniają życie innym.")
			.internalName(QUEST_SLOT)
			.repeatableAfterMinutes(2 * MathHelper.MINUTES_IN_ONE_WEEK) // Powtarzalne co 2 tygodnie
			.minLevel(100)
			.region(Region.KOSCIELISKO)
			.questGiverNpc("Wielkolud");

		quest.history()
			.whenNpcWasMet("Napotkany został Wielkolud na niewielkim wzgórzu Kościeliska.")
			.whenQuestWasRejected("Nie czuję zagrożenia ze strony pokutników.")
			.whenQuestWasAccepted("Pokonam wredne upiory oraz utoruję scieżkę dla innych!")
			.whenTaskWasCompleted("Pokutniky od teraz unikają tego regionu! Hura!")
			.whenQuestWasCompleted("Wielkolud nagrodził mnie swymi radami życiowymi oraz podzielił się swoim doświadczeniem.")
			.whenQuestCanBeRepeated("Ścieżka dla turystów znów się zawaliła oraz ponownie pojawiły się upiory. Muszę porozmawiać z Wielkoludem na ten temat.");

		quest.offer()
			.respondToRequest("Ostatnio nawiedzają te rejony straszliwe upiory, które ciągle przeszkadzają w moich interesach. Czy możesz mi pomóc w pozbyciu się ich?")
			.respondToUnrepeatableRequest("Chwilowo nie zauważyłem jeszcze problemów. \"Turyści\"... trafiają do mnie bez przeszkód. Odwiedź mnie nieco później, mam takie przeczucie iż niedługo mogą powrócić...")
			.respondToRepeatedRequest("Ścieżka znów została zablokowana... Pomożesz?")
			.respondToAccept("Wspaniale! Proszę, pozbądź się tych #pokutników. Kręcą się w okolicy Kościeliska i przeszkadzają innym w dotarciu do mnie. Zwłaszcza #'lawina kamienna' jest dokuczliwa ponieważ zablokowała główną ścieżkę!")
			.acceptationKarmaReward(5)
			.respondToReject("Rozumiem. Brzmi straszliwie, w końcu to upiory. Poczekam na kogoś odpowiedniego do tego zadania.")
			.rejectionKarmaPenalty(5)
			.remind("Już poprosiłem Ciebie o pozbycie się #pokutników i lawiny kamiennej!");

		NPCList.get().get("Wielkolud").addReply(Arrays.asList("pokutnik", "pokutników", "pokutnicy", "upiory"),
				"Głównie w moich interesach przeszkadzają te upiory: #'pokutnik z bagien', #'pokutnik z wrzosowisk', #'pokutnik z łąk', #'pokutnik wieczorny' oraz #'pokutnik nocny'.");

		quest.task()
			.requestKill(1, "lawina kamienna")
			.requestKill(1, "pokutnik z bagien")
			.requestKill(1, "pokutnik z wrzosowisk")
			.requestKill(1, "pokutnik z łąk")
			.requestKill(1, "pokutnik wieczorny")
			.requestKill(1, "pokutnik nocny");

		quest.complete()
			.greet("Spisałeś się wyśmienicie! Twe męstwo i odwagę będą potomni wspominać!")
			.rewardWith(new IncreaseXPAction(50000))
			.rewardWith(new IncreaseKarmaAction(25))
			.rewardWith(new IncreaseBaseHPOnlyOnceAction(QUEST_SLOT, 10))
			.rewardWith(new EquipItemAction("bryłka mithrilu", 1));

		return quest;
	}
}