/***************************************************************************
 *                   (C) Copyright 2003-2026 - PolanieOnLine               *
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

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.CraftItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class BringMagic implements QuestManuscript {
	@Override
	public CraftItemQuestBuilder story() {
		CraftItemQuestBuilder quest = new CraftItemQuestBuilder();

		quest.info()
			.name("Magiczne Zasoby")
			.description("Czarnoksiężnik potrzebuje pięciu rodzajów magii, aby wzmocnić hełm kolczy.")
			.internalName("bring_magic")
			.notRepeatable()
			.minLevel(120)
			.region(Region.ZAKOPANE_CITY)
			.questGiverNpc("Czarnoksiężnik");

		quest.history()
			.whenNpcWasMet("Spotkałem Czarnoksiężnika w starej wieży niedaleko Zakopanego.")
			.whenQuestWasRejected("Odmówiłem pomocy przy ostatnim doświadczeniu Czarnoksiężnika.")
			.whenQuestWasAccepted("Czarnoksiężnik poprosił mnie o sto porcji magii ziemi, płomieni, deszczu, mroku i światła.")
			.whenTaskWasCompleted("Dostarczyłem magiczne zasoby i Czarnoksiężnik przygotował zaklęcie dla hełmu.")
			.whenTimeWasNotEnded("Czarnoksiężnik nadal przygotowuje zaklęcie dla hełmu.")
			.whenTimeWasPassed("Zaklęcie jest gotowe. Czarnoksiężnik potrzebuje jeszcze hełmu kolczego.")
			.whenQuestWasCompleted("Czarnoksiężnik przemienił mój hełm kolczy w magiczny hełm kolczy.");

		quest.offer()
			.offerState(ConversationStates.QUEST_3_OFFERED)
			.respondToCraftIssue("Najpierw dokończ sprawę górskich elfów. Hełm z tamtej próby będzie potrzebny w ostatnim doświadczeniu.")
			.respondToRequest("Pozostał ostatni etap moich badań. Potrzebuję stu porcji magii ziemi, płomieni, deszczu, mroku i światła. Z ich pomocą wzmocnię hełm kolczy, który ode mnie otrzymałeś. Podejmiesz się tego?")
			.respondToUnrepeatableRequest("Ostatnie doświadczenie zostało zakończone. Magiczny hełm kolczy jest już w twoich rękach.")
			.respondToAccept("Dobrze. Przynieś mi wszystko naraz\n#'100 magii ziemi'\n#'100 magii płomieni'\n#'100 magii deszczu'\n#'100 magii mroku'\n#'100 magii światła'")
			.respondToReject("Szkoda. Bez tych zasobów nie przeprowadzę ostatniego doświadczenia.")
			.rejectionKarmaPenalty(15.0)
			.acceptWith(new IncreaseKarmaAction(10.0))
			.remind("Do przygotowania zaklęcia potrzebuję wszystkiego naraz\n#'100 magii ziemi'\n#'100 magii płomieni'\n#'100 magii deszczu'\n#'100 magii mroku'\n#'100 magii światła'");

		quest.task()
			.craftItem("magiczny hełm kolczy")
			.waitingTime(0)
			.completedQuest("kill_mountain_elves")
			.requiredItem(100, "magia ziemi")
			.requiredItem(100, "magia płomieni")
			.requiredItem(100, "magia deszczu")
			.requiredItem(100, "magia mroku")
			.requiredItem(100, "magia światła")
			.legacyForgingState("helmet")
			.craftWith(new IncreaseXPAction(50000))
			.craftWith(new IncreaseKarmaAction(10.0))
			.respondToCraft("To wystarczy. Połączę te energie w jedno zaklęcie. Teraz przynieś mi hełm kolczy, który dostałeś po poprzedniej próbie.")
			.respondToCraftReject("Wróć, gdy będziesz mieć przy sobie wszystkie potrzebne rodzaje magii.");

		quest.complete()
			.greet("Masz hełm kolczy. Oddasz mi go na chwilę?")
			.respondToAccept("Gotowe. Część przyniesionej magii wzmocniła hełm i ochroni cię przed siłami natury.")
			.respondToReject("Dobrze. Wróć z hełmem, gdy zdecydujesz się dokończyć doświadczenie.")
			.requiredItem(1, "hełm kolczy")
			.respondToMissingItem("Nie masz przy sobie hełmu kolczego. Przynieś go, a dokończę ostatnie doświadczenie.")
			.rewardWith(new IncreaseXPAction(5000))
			.rewardWith(new IncreaseKarmaAction(20.0));

		return quest;
	}
}
