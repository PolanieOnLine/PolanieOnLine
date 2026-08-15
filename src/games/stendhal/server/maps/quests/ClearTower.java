/***************************************************************************
 *                   (C) Copyright 2003-2024 - PolanieOnLine               *
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

import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.KillCreaturesQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class ClearTower implements QuestManuscript {
	@Override
	public KillCreaturesQuestBuilder story() {
		KillCreaturesQuestBuilder quest = new KillCreaturesQuestBuilder();

		quest.info()
			.name("Wyczyszczenie Starej Wieży")
			.description("Czarnoksiężnik chce odzyskać starą wieżę i sprawdzić, czy potrafisz poradzić sobie z jej mieszkańcami.")
			.internalName("clear_tower")
			.notRepeatable()
			.minLevel(35)
			.region(Region.ZAKOPANE_CITY)
			.questGiverNpc("Czarnoksiężnik");

		quest.history()
			.whenNpcWasMet("Spotkałem Czarnoksiężnika w starej wieży niedaleko Zakopanego.")
			.whenQuestWasRejected("Odmówiłem oczyszczenia starej wieży.")
			.whenQuestWasAccepted("Zgodziłem się oczyścić starą wieżę z potworów, które zajęły jej wnętrze.")
			.whenTaskWasCompleted("Wieża została oczyszczona. Mogę wrócić do Czarnoksiężnika.")
			.whenQuestWasCompleted("Czarnoksiężnik uznał, że poradziłem sobie z pierwszą próbą i zapowiedział kolejne zadanie.");

		quest.offer()
			.respondToRequest("Zanim powierzę ci poważniejsze sprawy, chcę zobaczyć jak radzisz sobie w walce. W mojej starej wieży zalęgły się potwory i przeszkadzają mi w pracy. Oczyścisz ją?")
			.respondToUnrepeatableRequest("Wieża jest już oczyszczona. Teraz mogę zająć się tym, co naprawdę mnie interesuje.")
			.respondToAccept("Dobrze. Oczyść wieżę dokładnie i wróć do mnie, gdy żaden z jej mieszkańców nie będzie już stanowił problemu.")
			.respondToReject("Szkoda. W takim razie nie mam powodu powierzać ci trudniejszych spraw.")
			.rejectionKarmaPenalty(10.0)
			.acceptWith(new IncreaseKarmaAction(5.0))
			.remind("W starej wieży nadal są potwory. Oczyść ją dokładnie, zanim do mnie wrócisz.");

		quest.task()
			.requestKill(1, "starszy gargulec")
			.requestKill(1, "mroczny gargulec")
			.requestKill(1, "trujący gargulec")
			.requestKill(1, "gargulec")
			.requestKill(1, "nietoperz")
			.requestKill(1, "nietoperz wampir")
			.requestKill(1, "pająk ptasznik")
			.requestKill(1, "pająk")
			.requestKill(1, "wściekły szczur")
			.requestKill(1, "krwiożerczy szczur")
			.requestKill(1, "szczur zombie");

		quest.complete()
			.greet("Dobrze. Wieża znowu nadaje się do pracy. Skoro poradziłeś sobie z pierwszą próbą, mam dla ciebie kolejne zadanie.")
			.rewardWith(new IncreaseXPAction(10000))
			.rewardWith(new IncreaseKarmaAction(15.0));

		return quest;
	}
}
