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
			.description("Józek marzy o tym, by tchnąć w moją złotą ciupagę legendę pustynnych duchów i prosi mnie o udział w jego eksperymencie.")
			.internalName("zlota_ciupaga_was")
			.repeatableAfterMinutes(5 * 24 * 60) // Powtarzalne co 5 dni
			.region(Region.DESERT)
			.questGiverNpc("Józek");

		quest.history()
			.whenNpcWasMet("Spotkaliśmy Józka w pustynnej chatce.")
			.whenQuestWasRejected("Szkoda mi materiałów na ulepszenie złotej ciupagi.")
			.whenQuestWasAccepted("Józek powierzył mi listę rzadkich składników, które pozwolą jego pustynnej pracowni upleść legendarny wąs dla mojej ciupagi.")
			.whenTaskWasCompleted("Znalazłem wszystko, czego potrzebuje Józek, by jego piaskowe runy zadziałały.")
			.whenTimeWasNotEnded("Rytuał pustynnego kucia wciąż trwa. Muszę poczekać osiem godzin, aż duchy wąsa ukoją metal.")
			.whenTimeWasPassed("Piaski ucichły, a pustynny wiatr niesie wieści, że Józek zakończył rytuał.")
			.whenQuestWasCompleted("Józek oddał mi broń, w której drzemie echo pustyni – nową złotą ciupagę z dumą góralskiego wąsa!")
			.whenQuestCanBeRepeated("Może znów poproszę Józka, by przy pustynnych ogniskach wyśpiewał dla mnie kolejny wąs.");

		quest.offer()
			.respondToCraftIssue("Wybacz, lecz zanim pozwolę duchom pustyni tchnąć w twoją broń nowy blask, musisz zdobyć szacunek Wielkoluda, pokonać złotą śmierć i wykazać się wyjątkową karmą.")
			.respondToRequest("Odkąd badałem piaskowe wąsy, szukam śmiałka, który pozwoli mi przetestować legendarny rytuał na swojej złotej ciupadze. Podejmiesz się?")
			.respondToUnrepeatableRequest("Rytuał wymaga skupienia. Daj mi chwilę, a pustynny wiatr znów mnie zainspiruje.")
			.respondToRepeatedRequest("Wróciłeś po kolejny wąs z legendy? Piaski znów zatańczą, jeśli tylko chcesz.")
			.respondToAccept("Doskonale! Aby duchy pustyni zatańczyły wokół twojej broni, potrzebuję:\n#1 złota ciupaga\n#1 złoty róg\n#4 drewna\n#70 sztabek złota\noraz #'120 000' money. Nie zapomnij o zapłacie dla wędrownego bajarza!")
			.respondToReject("Cóż, legenda poczeka na innego śmiałka.")
			.rejectionKarmaPenalty(10.0)
			.remind("Nie zapomnij – duchy pustyni czekają na:\n#1 złota ciupaga\n#1 złoty róg\n#4 drewna\n#70 sztabek złota\noraz #'120 000' money. Tylko komplet otworzy bramy pustynnej kuźni.");

		quest.task().craftItem("złota ciupaga z wąsem")
			.waitingTime(8 * 60) // 8 godzin
			.playerMinLevel(200).playerMinKarma(200).completedQuest("help_wielkolud_basehp").requestMonster("złota śmierć")
			.requiredItem(1, "złota ciupaga").requiredItem(1, "złoty róg").requiredItem(4, "polano").requiredItem(70, "sztabka złota").requiredItem(120000, "money")
			.respondToCraft("Świetnie! Rozstawię piaskowe lustra i zaproszę duchy wąsów. Twoja #'[itemName]' nasyci się legendą za około osiem godzin.")
			.respondToCraftReject("Gdy tylko poczujesz piaskowe natchnienie, wróć do mnie.");

		quest.complete()
			.greet("Rytuał dobiegł końca! Wąs tańczy na ostrzu, a oto twoja odmieniona ciupaga.")
			.rewardWith(new IncreaseXPAction(250000))
			.rewardWith(new IncreaseKarmaAction(100.0));

		return quest;
	}
}
