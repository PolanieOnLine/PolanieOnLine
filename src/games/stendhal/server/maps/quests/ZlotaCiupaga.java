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
import games.stendhal.server.entity.npc.quest.BringMultiItemsTask;
import games.stendhal.server.entity.npc.quest.QuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class ZlotaCiupaga implements QuestManuscript {
	public QuestBuilder<?> story() {
		QuestBuilder<BringMultiItemsTask> quest = new QuestBuilder<>(new BringMultiItemsTask());

		quest.info()
			.name("Złota Ciupaga")
			.description("Andrzej o ile na to zasługujesz wykona dla Ciebie złotą ciupagę.")
			.internalName("zlota_ciupaga")
			.repeatableAfterMinutes(24 * 60) // Powtarzalne co 24 godziny
			.forgingDelay(6 * 60) // Czas produkcji złotej ciupagi 6 godzin
			.region(Region.ZAKOPANE_CITY)
			.questGiverNpc("Kowal Andrzej");

		quest.history()
			.whenNpcWasMet("Spotkany kowal Andrzej w swej kuźni, niedaleko poczty.")
			.whenQuestWasRejected("Miejscowy kowal zaproponował wykonanie dla mnie złotej ciupagi, lecz nie czuję potrzeby posiadania jej.")
			.whenQuestWasAccepted("Wraz z kowal postanowiliśmy wykonać wspólnymi siłami dla mnie nową i ulepszoną złotą ciupagę jak na prawdziwego górala przystało! Potrzebuję: 1 ciupagi, 5 sztuk drewna, 25 sztabek złota oraz 50 000 monet.")
			.whenTaskWasCompleted("Mam już wszystkie potrzebne przedmioty do wykonania złotej ciupagi.")
			.whenQuestWasCompleted("Kowal Andrzej był zadowolony po wykonanej ciężkiej pracy nad tworzeniem złotej ciupagi dla mnie.")
			.whenQuestCanBeRepeated("Kowal chyba mógłby wykonać dla mnie kolejną taką ciupagę ze szczerego złota!");

		quest.offer()
			.needQuestsCompleted("kill_herszt_basehp").needLevelCondition("greater", 99).needKarmaCondition("greater", 99)
			.respondToUnstartable("Wybacz mi proszę, ale musisz zasłużyć na uznanie gazdy Jędrzeja oraz zdobyć trochę większe doświadczenie i mieć dobrą karmę!")
			.respondToRequest("Jakiś czas temu wykonywałem nietypowe zlecenie w naszej kuźni, a chodzi dokładnie o wykonanie ciupagi ze szczerego złota. Może dzielny wojak chce również taką?")
			.respondToUnrepeatableRequest("Jestem aktualnie troszkę zmęczony. Wróć do mnie za jakiś czas.")
			.respondToRepeatedRequest("Potrzebujesz kolejnej złotej ciupagi?")
			.respondToAccept("Słusznie! Przynieś mi te przedmioty potrzebne do nowej ciupagi:\n#1 ciupaga\n#5 drewna\n#25 sztabek złota\noraz #'50 000' money.")
			.acceptationKarmaReward(10)
			.respondToReject("Cóż, to Twoja strata.")
			.rejectionKarmaPenalty(10)
			.remind("Pamiętasz potrzebną listę? Przynieś mi te przedmioty potrzebne do nowej ciupagi:\n#1 ciupaga\n#5 drewna\n#25 sztabek złota\noraz #'50 000' money.");

		quest.task()
			.requestItem(1, "ciupaga")
			.requestItem(5, "polano")
			.requestItem(25, "sztabka złota")
			.requestItem(50000, "money");

		quest.forging()
			.respondToAccept("Genialnie! Biorę się do pracy! Wróć do mnie za około 6 godzin.")
			.respondToReject("Jak się zastanowisz to wróć.");

		quest.complete()
			.greet("Skończyłem swą pracę nad ciupagą! Warto chyba było trochę poczekać. Proszę, a oto i ona!")
			.rewardWith(new IncreaseXPAction(15000))
			.rewardWith(new IncreaseKarmaAction(25))
			.rewardWith(new EquipItemAction("złota ciupaga", 1, true));

		return quest;
	}
}
