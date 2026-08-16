/***************************************************************************
 *                   (C) Copyright 2017-2026 - PolanieOnLine               *
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
import games.stendhal.server.entity.npc.quest.KillAndBringQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class SkorySmokow implements QuestManuscript {
	@Override
	public KillAndBringQuestBuilder story() {
		KillAndBringQuestBuilder quest = new KillAndBringQuestBuilder();

		quest.info()
			.name("Klucz Aligerna")
			.description("Aligern sprawdza, czy jesteś gotowy na przejście ukryte za jego chatką.")
			.internalName("aligern_key")
			.notRepeatable()
			.minLevel(200)
			.region(Region.GDANSK_CITY)
			.questGiverNpc("Aligern");

		quest.history()
			.whenNpcWasMet("Spotkałem Aligerna w chatce na plaży w Gdańsku.")
			.whenQuestWasRejected("Odmówiłem próby, którą przygotował dla mnie Aligern.")
			.whenQuestWasAccepted("Aligern nie odda mi klucza do przejścia za swoją chatką bez próby. Mam pokonać zielonego, czerwonego i błękitnego smoka oraz przynieść ich skóry jako dowód.")
			.whenTaskWasCompleted("Pokonałem trzy wskazane smoki i mam ich skóry. Mogę wrócić do Aligerna.")
			.whenQuestWasCompleted("Aligern uznał, że przeszedłem jego próbę. Otrzymałem kluczyk, który otwiera przejście za chatką prowadzące dalej do jego mistrza.");

		quest.offer()
			.respondToRequest("Za moją chatką znajduje się przejście, którego nie otwieram każdemu przybyszowi. Jeśli chcesz otrzymać mój klucz, pokaż że poradzisz sobie z tym, co czeka dalej. Pokonasz dla mnie trzy smoki i przyniesiesz ich skóry jako dowód?")
			.respondToUnrepeatableRequest("Przeszedłeś już moją próbę i otrzymałeś klucz. Dalej musisz radzić sobie sam.")
			.respondToAccept("Dobrze. Pokonaj zielonego, czerwonego i błękitnego smoka. Przynieś mi także po jednej skórze każdego z nich. Wtedy będę wiedział, że klucz trafia w odpowiednie ręce.")
			.respondToReject("Rozumiem. Bez tej próby nie oddam jednak klucza do przejścia.")
			.rejectionKarmaPenalty(5.0)
			.acceptWith(new IncreaseKarmaAction(5.0))
			.remind("Klucz dostaniesz dopiero wtedy, gdy pokonasz zielonego, czerwonego i błękitnego smoka oraz przyniesiesz mi ich trzy skóry.");

		quest.task()
			.requestKill(1, "zielony smok")
			.requestKill(1, "czerwony smok")
			.requestKill(1, "błękitny smok")
			.requestItem(1, "skóra zielonego smoka")
			.requestItem(1, "skóra czerwonego smoka")
			.requestItem(1, "skóra niebieskiego smoka");

		quest.complete()
			.greet("Dobrze. Smocze skóry są wystarczającym dowodem. Przeszedłeś moją próbę, więc przyjmij mój kluczyk. Otwiera przejście po prawej stronie chatki. Dalej znajdziesz drogę do mojego mistrza.")
			.rewardWith(new EquipItemAction("kluczyk Aligerna", 1, true))
			.rewardWith(new EquipItemAction("wielki eliksir", 10))
			.rewardWith(new IncreaseXPAction(75000))
			.rewardWith(new IncreaseKarmaAction(35.0));

		return quest;
	}
}
