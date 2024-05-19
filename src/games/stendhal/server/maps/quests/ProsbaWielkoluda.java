/***************************************************************************
 *                 (C) Copyright 2019-2024 - PolanieOnLine                 *
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
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseBaseHPOnlyOnceAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.KillCreaturesQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.TimeUtil;

/**
 * QUEST: Wsparcie dla Wielkoluda
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Wielkolud</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Wielkolud szuka pomocy w pozbyciu się upiorów ze wzgórza i okolic.</li>
 * <li>Wróć i odbierz wynagrodzenie od Wielkoluda.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>80000 xp</li>
 * <li>karma</li>
 * <li>10 base hp</li>
 * <li>bryłka mithrilu</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>1 week</li>
 * </ul>
 */
public class ProsbaWielkoluda implements QuestManuscript {
	@Override
	public KillCreaturesQuestBuilder story() {
		KillCreaturesQuestBuilder quest = new KillCreaturesQuestBuilder();

		quest.info()
			.name("Widmo Wzgórza")
			.description("Wielkolud poszukuje wojaka, który pozbędzie się grasujących pokutników i lawiny kamiennej z przejścia, co utrudniają życie innym.")
			.internalName("help_wielkolud")
			.repeatableAfterMinutes(TimeUtil.MINUTES_IN_WEEK) // Powtarzalne co 1 tydzien
			.minLevel(100)
			.region(Region.KOSCIELISKO)
			.questGiverNpc("Wielkolud");

		quest.history()
			.whenNpcWasMet("Napotkany został Wielkolud na niewielkim wzgórzu Kościeliska.")
			.whenQuestWasRejected("Nie czuję zagrożenia ze strony pokutników.")
			.whenQuestWasAccepted("Pokonam wredne upiory oraz utoruję scieżkę dla innych!")
			.whenTaskWasCompleted("Pokutniki od teraz unikają tego regionu! Hura!")
			.whenQuestWasCompleted("Wielkolud nagrodził mnie swymi radami życiowymi oraz podzielił się swoim doświadczeniem.")
			.whenQuestCanBeRepeated("Ścieżka dla turystów znów się zawaliła oraz ponownie pojawiły się upiory. Muszę porozmawiać z Wielkoludem na ten temat.")
			.whenCompletionsShown("Ścieżka na wzgórze Kościeliska została udrożniona [count] [raz].");

		quest.offer()
			.respondToRequest("Ostatnio nawiedzają te rejony straszliwe upiory, które ciągle przeszkadzają w moich interesach. Czy możesz mi pomóc w pozbyciu się ich?")
			.respondToUnrepeatableRequest("Chwilowo nie zauważyłem jeszcze problemów. \"Turyści\"... trafiają do mnie bez przeszkód. Odwiedź mnie nieco później, mam takie przeczucie iż niedługo mogą powrócić...")
			.respondToRepeatedRequest("Ścieżka znów została zablokowana... Pomożesz?")
			.respondToAccept("Wspaniale! Proszę, pozbądź się tych #pokutników. Kręcą się w okolicy Kościeliska i przeszkadzają innym w dotarciu do mnie. Zwłaszcza #'lawina kamienna' jest dokuczliwa ponieważ zablokowała główną ścieżkę!")
			.respondToReject("Rozumiem. Brzmi straszliwie, w końcu to upiory. Poczekam na kogoś odpowiedniego do tego zadania.")
			.rejectionKarmaPenalty(5.0)
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
			.rewardWith(new IncreaseXPAction(100000))
			.rewardWith(new IncreaseKarmaAction(20.0))
			.rewardWith(new IncreaseBaseHPOnlyOnceAction("help_wielkolud", 10))
			.rewardWith(new EquipItemAction("bryłka mithrilu", 1));

		return quest;
	}
}