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

import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.CraftItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class ZlotaCiupagaJedenWas implements QuestManuscript {
	public CraftItemQuestBuilder story() {
		CraftItemQuestBuilder quest = new CraftItemQuestBuilder();

		quest.info()
			.name("Złota Ciupaga z Wąsem")
			.description("Józek zaproponował ulepszenie mojej złotej ciupagi, którą wcześniej wykonał dla mnie kowal Andrzej.")
			.internalName("zlota_ciupaga_was")
			.repeatableAfterMinutes(5 * 24 * 60) // Powtarzalne co 5 dni
			.region(Region.DESERT)
			.questGiverNpc("Józek");

		quest.history()
			.whenNpcWasMet("Spotkaliśmy Józka w pustynnej chatce.")
			.whenQuestWasRejected("Szkoda mi materiałów na ulepszenie złotej ciupagi.")
			.whenQuestWasAccepted("Postanowiliśmy ulepszyć moją złotą ciupagę! Potrzebuję: 1 złotej ciupagi, 1 złotego rogu, 4 sztuki drewna, 70 sztabek złota oraz 120 000 monet.")
			.whenTaskWasCompleted("Mam już wszystkie potrzebne przedmioty do wykonania złotej ciupagi.")
			.whenTimeWasNotEnded("Muszę odczekać 8 godzin. Tyle zajmie czasu Józkowi na ulepszenie mojej ciupagi.")
			.whenTimeWasPassed("Mam przeczucie iż Józek skończył prace nad moją nową ciupagą!")
			.whenQuestWasCompleted("Józek chętnie wręczył mi swój owoc ciężkiej pracy i mam teraz jeszcze lepszą ciupagę na złoczyńców!")
			.whenQuestCanBeRepeated("Wrócę chyba do Józka, a może wykona dla mnie drugą taką!");

		quest.offer()
			.respondToUnstartableForge("Wybacz mi proszę, ale musisz zasłużyć na uznanie Wielkoluda jak i pokonać samodzielnie złotą śmierć oraz zdobyć nieco większe doświadczenie i mieć wspaniałą karmę!")
			.respondToRequest("Od jakiegoś czasu testuję ulepszanie broni. Może chcesz, abym udoskonalił dla Ciebie złotą ciupagę?")
			.respondToUnrepeatableRequest("Jestem aktualnie troszkę zmęczony. Wróć do mnie za jakiś czas.")
			.respondToRepeatedRequest("Potrzebujesz kolejnej ulepszonej złotej ciupagi?")
			.respondToAccept("Słusznie! Przynieś mi te przedmioty potrzebne do nowej ciupagi:\n#1 złota ciupaga\n#1 złoty róg\n#4 drewna\n#70 sztabek złota\noraz #'120 000' money. Pamiętaj! Pieniądze najważniejsze!")
			.respondToReject("Cóż, to Twoja strata.")
			.rejectionKarmaPenalty(10.0)
			.remind("Pamiętasz potrzebną listę? Przynieś mi te przedmioty potrzebne do nowej ciupagi:\n#1 złota ciupaga\n#1 złoty róg\n#4 drewna\n#70 sztabek złota\noraz #'120 000' money.");

		quest.task().craftItem("złota ciupaga z wąsem")
			.minutesToForge(8 * 60) // 8 godzin
			.playerMinLevel(200).playerMinKarma(200).completedQuest("help_wielkolud_basehp").requestMonster("złota śmierć")
			.requiredItem(1, "złota ciupaga").requiredItem(1, "złoty róg").requiredItem(4, "polano").requiredItem(70, "sztabka złota").requiredItem(120000, "money")
			.respondToForging("Super! Przygotuję tylko narzędzia i biorę się do pracy! Twoja #'[itemName]' będzie gotowa za około 8 godzin.")
			.respondToRejectForge("Jak się zastanowisz to wróć.");

		quest.complete()
			.greet("Skończyłem swą pracę nad ciupagą! Warto było chyba nieco poczekać. Proszę, a oto i ona!")
			.rewardWith(new IncreaseXPAction(250000))
			.rewardWith(new IncreaseKarmaAction(100.0));

		return quest;
	}
}
