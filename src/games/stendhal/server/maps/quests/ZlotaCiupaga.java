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

import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.CraftItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class ZlotaCiupaga implements QuestManuscript {
	public CraftItemQuestBuilder story() {
		CraftItemQuestBuilder quest = new CraftItemQuestBuilder();

		quest.info()
			.name("Złota Ciupaga")
			.description("Andrzej o ile na to zasługujesz wykona dla Ciebie złotą ciupagę.")
			.internalName("zlota_ciupaga")
			.repeatableAfterMinutes(24 * 60) // Powtarzalne co 24 godziny
			.region(Region.ZAKOPANE_CITY)
			.questGiverNpc("Kowal Andrzej");

		quest.history()
			.whenNpcWasMet("Zastaliśmy pewnego miejscowego kowala Andrzeja w swej kuźni, niedaleko poczty.")
			.whenQuestWasRejected("Miejscowy kowal zaproponował wykonanie dla mnie złotej ciupagi, lecz nie czuję potrzeby posiadania jej.")
			.whenQuestWasAccepted("Wraz z kowalem postanowiliśmy wykonać wspólnymi siłami dla mnie nową złotą ciupagę jak na prawdziwego górala przystało! Potrzebuję: 1 ciupagi, 5 sztuk drewna, 25 sztabek złota oraz 50 000 monet.")
			.whenTaskWasCompleted("Mam już wszystkie potrzebne przedmioty do wykonania złotej ciupagi.")
			.whenTimeWasNotEnded("Muszę odczekać 6 godzin. Tyle zajmie czasu Andrzejowi na wykonanie mojej ciupagi.")
			.whenTimeWasPassed("Mam przeczucie iż miejscowy kowal skończył prace nad moją nową ciupagą!")
			.whenQuestWasCompleted("Kowal Andrzej był zadowolony po wykonanej ciężkiej pracy nad tworzeniem złotej ciupagi dla mnie.")
			.whenQuestCanBeRepeated("Kowal chyba mógłby wykonać dla mnie kolejną taką ciupagę ze szczerego złota!");

		quest.offer()
			.respondToCraftIssue("Wybacz mi proszę, ale musisz zasłużyć na uznanie gazdy Jędrzeja oraz zdobyć nieco większe doświadczenie i mieć dobrą karmę!")
			.respondToRequest("Jakiś czas temu wykonywałem nietypowe zlecenie w naszej kuźni, a chodzi dokładnie o wykonanie ciupagi ze szczerego złota. Może dzielny wojak chce również taką?")
			.respondToUnrepeatableRequest("Jestem aktualnie troszkę zmęczony. Wróć do mnie za jakiś czas.")
			.respondToRepeatedRequest("Potrzebujesz kolejnej złotej ciupagi?")
			.respondToAccept("Słusznie! Przynieś mi te przedmioty potrzebne do nowej ciupagi:\n#1 ciupaga\n#5 drewna\n#25 sztabek złota\noraz #'50 000' money.")
			.respondToReject("Cóż, to Twoja strata.")
			.rejectionKarmaPenalty(10.0)
			.remind("Pamiętasz potrzebną listę? Przynieś mi te przedmioty potrzebne do nowej ciupagi:\n#1 ciupaga\n#5 drewna\n#25 sztabek złota\noraz #'50 000' money.");

		quest.task().craftItem("złota ciupaga")
			.waitingTime(6 * 60) // 6 godzin
			.playerMinLevel(100).playerMinKarma(100).completedQuest("kill_herszt_basehp")
			.requiredItem(1, "ciupaga").requiredItem(5, "polano").requiredItem(25, "sztabka złota").requiredItem(50000, "money")
			.respondToCraft("Genialnie! Powoli wezmę się do pracy i twoja #'[itemName]' będzie gotowa za około 6 godzin. Wróć gdy skończę!")
			.respondToCraftReject("Jak się zastanowisz to wróć.");

		quest.complete()
			.greet("Skończyłem swą pracę nad ciupagą! Warto było chyba nieco poczekać. Proszę, a oto i ona!")
			.rewardWith(new IncreaseXPAction(15000))
			.rewardWith(new IncreaseKarmaAction(25.0));

		return quest;
	}
}
