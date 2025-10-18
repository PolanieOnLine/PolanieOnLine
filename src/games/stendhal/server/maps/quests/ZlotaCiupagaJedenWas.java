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
			.description("Józek, kowal spod wydm, obiecał nasycić moją złotą ciupagę pustynnym blaskiem.")
			.internalName("zlota_ciupaga_was")
			.repeatableAfterMinutes(5 * 24 * 60) // Powtarzalne co 5 dni
			.region(Region.DESERT)
			.questGiverNpc("Józek");

		quest.history()
			.whenNpcWasMet("Spotkaliśmy Józka w jego piaskowej kuźni pachnącej jałowcem.")
			.whenQuestWasRejected("Odłożyłem sprawę; niech iskry Józka przygasną beze mnie.")
			.whenQuestWasAccepted("Józek kazał mi zebrać 1 złotą ciupagę, 1 złoty róg, cztery polana, siedemdziesiąt sztabek złota i 120 000 monet.")
			.whenTaskWasCompleted("Mam już wszystkie dary dla paleniska Józka.")
			.whenTimeWasNotEnded("Kuźnia huczy jeszcze osiem godzin; trzeba cierpliwie czekać.")
			.whenTimeWasPassed("Żar pewnie już stygnie, pora odebrać ciupagę.")
			.whenQuestWasCompleted("Józek oddał mi ciupagę nasiąkniętą pustynnym ogniem.")
			.whenQuestCanBeRepeated("Może znów poproszę Józka, by rozpalił dla mnie młot.");

		quest.offer()
			.respondToCraftIssue("Najpierw pokaż, że znasz obyczaje Wielkoluda, pokonaj złotą śmierć i wzmocnij swe imię dobrą karmą.")
			.respondToRequest("Od lat zapisuję pieśni o broni. Chcesz, abym twoją ciupagę nasycił pustynnym żarem?")
			.respondToUnrepeatableRequest("Dziś kowadło stygnie. Wróć, gdy znów rozpalę palenisko.")
			.respondToRepeatedRequest("Znowu pragniesz, bym wygładził twoją złotą ciupagę?")
			.respondToAccept("Słuchaj uważnie. Przynieś wszystko naraz:\n#1 złota ciupaga\n#1 złoty róg\n#4 drewna\n#70 sztabek złota\noraz #'120 000' money. Dopiero wtedy zakręci się miech.")
			.respondToReject("Jak chcesz, mój młot poczeka na innego śmiałka.")
			.rejectionKarmaPenalty(10.0)
			.remind("Lista dla twojej pamięci:\n#1 złota ciupaga\n#1 złoty róg\n#4 drewna\n#70 sztabek złota\noraz #'120 000' money.");

		quest.task().craftItem("złota ciupaga z wąsem")
			.waitingTime(8 * 60) // 8 godzin
			.playerMinLevel(200).playerMinKarma(200).completedQuest("help_wielkolud_basehp").requestMonster("złota śmierć")
			.requiredItem(1, "złota ciupaga").requiredItem(1, "złoty róg").requiredItem(4, "polano").requiredItem(70, "sztabka złota").requiredItem(120000, "money")
			.respondToCraft("Dobrze. Rozżarzę miechy i wciągnę piasek do ognia. Twoja #'[itemName]' będzie gotowa za jakieś osiem godzin.")
			.respondToCraftReject("Gdy zmienisz zdanie, palenisko znów przyjmie twe dary.");

		quest.complete()
			.greet("Żar spełnił swoje zadanie. Oto ciupaga, której klinga pamięta mój młot.")
			.rewardWith(new IncreaseXPAction(250000))
			.rewardWith(new IncreaseKarmaAction(100.0));

		return quest;
	}
}
