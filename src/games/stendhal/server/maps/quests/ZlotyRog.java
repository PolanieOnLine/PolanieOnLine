/***************************************************************************
 *                 (C) Copyright 2003-2026 - PolanieOnLine                 *
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

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.CraftItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class ZlotyRog implements QuestManuscript {
	@Override
	public CraftItemQuestBuilder story() {
		CraftItemQuestBuilder quest = new CraftItemQuestBuilder();

		quest.info()
			.name("Złoty Róg")
			.description("Bartłomiej zbiera pióra potrzebne jego bratu do wykonania złotego rogu.")
			.internalName("zloty_rog")
			.repeatableAfterMinutes(4 * 24 * 60)
			.region(Region.ZAKOPANE_CITY)
			.questGiverNpc("Bartłomiej");

		quest.history()
			.whenNpcWasMet("Spotkałem Bartłomieja w jaskiniach Zakopanego.")
			.whenQuestWasRejected("Nie zdecydowałem się zamówić złotego rogu.")
			.whenQuestWasAccepted("Bartłomiej poprosił mnie o pióra siedmiu rodzajów. Jego brat wykorzysta je do wykonania złotego rogu.")
			.whenTaskWasCompleted("Dostarczyłem wszystkie pióra potrzebne do wykonania złotego rogu.")
			.whenTimeWasNotEnded("Brat Bartłomieja nadal pracuje nad moim złotym rogiem.")
			.whenTimeWasPassed("Złoty róg powinien być już gotowy. Mogę wrócić do Bartłomieja.")
			.whenQuestWasCompleted("Odebrałem złoty róg wykonany przez brata Bartłomieja.")
			.whenQuestCanBeRepeated("Brat Bartłomieja odpoczął i może wykonać kolejny złoty róg.");

		quest.offer()
			.respondToCraftIssue("Najpierw ukończ sprawę kolekcjonera broni, osiągnij poziom 200, zachowaj co najmniej 500 karmy i samodzielnie pokonaj archanioła.")
			.respondToCraftNotReady("Mój brat wciąż pracuje nad twoim złotym rogiem. Wróć za")
			.respondToRequest("Mój brat zna tajemnicę wyrabiania złotych rogów. Potrzebuje jednak rzadkich piór, zanim rozpocznie pracę. Chcesz zamówić jeden z nich?")
			.respondToUnrepeatableRequest("Mój brat musi odpocząć. Będzie gotowy na kolejne zlecenie za [remaining_time].")
			.respondToRepeatedRequest("Mój brat może już wykonać kolejny złoty róg. Chcesz zebrać dla niego nowe pióra?")
			.respondToAccept("Przynieś mi wszystkie pióra naraz\n#'100 piórek gołębich'\n#'20 piór anioła'\n#'10 piór archanioła'\n#'8 piór mrocznego anioła'\n#'20 piór upadłego anioła'\n#'7 piór archanioła ciemności'\n#'2 pióra serafina'")
			.respondToReject("Rozumiem. Złoty róg poczeka na innego wędrowca.")
			.rejectionKarmaPenalty(10.0)
			.acceptWith(new IncreaseKarmaAction(10.0))
			.remind("Do wykonania złotego rogu potrzebuję wszystkiego naraz\n#'100 piórek gołębich'\n#'20 piór anioła'\n#'10 piór archanioła'\n#'8 piór mrocznego anioła'\n#'20 piór upadłego anioła'\n#'7 piór archanioła ciemności'\n#'2 pióra serafina'");

		quest.task()
			.craftItem("złoty róg")
			.rarity(ItemRarity.EPIC)
			.waitingTime(60)
			.playerMinLevel(200)
			.playerMinKarma(500)
			.completedQuest("weapons_collector")
			.requestSoloKill("archanioł")
			.requiredItem(100, "piórko")
			.requiredItem(20, "pióro anioła")
			.requiredItem(10, "pióro archanioła")
			.requiredItem(8, "pióro mrocznego anioła")
			.requiredItem(20, "pióro upadłego anioła")
			.requiredItem(7, "pióro archanioła ciemności")
			.requiredItem(2, "pióro serafina")
			.legacyForgingState("make")
			.respondToCraft("Masz wszystkie pióra. Mój brat rozpocznie pracę i złoty róg będzie gotowy za około godzinę.")
			.respondToCraftReject("Wróć, gdy będziesz mieć przy sobie wszystkie potrzebne pióra.");

		quest.complete()
			.greet("Mój brat skończył pracę. Oto twój złoty róg.")
			.rewardWith(new IncreaseXPAction(20000))
			.rewardWith(new IncreaseKarmaAction(100.0));

		return quest;
	}
}
