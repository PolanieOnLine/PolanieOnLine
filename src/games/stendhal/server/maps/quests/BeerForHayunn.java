/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.BringItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.semos.guardhouse.RetiredAdventurerNPC;
import games.stendhal.server.util.ResetSpeakerNPC;

/**
 * QUEST: Beer For Hayunn
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Hayunn Naratha (the veteran warrior in Semos)</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Hayunn asks you to buy a beer from Margaret.</li>
 * <li>Margaret sells you a beer.</li>
 * <li>Hayunn sees your beer, asks for it and then thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>20 gold coins</li>
 * <li>Karma: 10</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class BeerForHayunn implements QuestManuscript {
	public static final String QUEST_SLOT = "beer_hayunn";

	@Override
	public BringItemQuestBuilder story() {
		BringItemQuestBuilder quest = new BringItemQuestBuilder();

		quest.info()
			.name("Trunek dla Hayunna")
			.description("Hayunn Naratha, wielki wojownik w strażnicy Semos, chce specjalnego trunku.")
			.internalName(QUEST_SLOT)
			.notRepeatable()
			.minLevel(0)
			.region(Region.SEMOS_CITY)
			.questGiverNpc("Hayunn Naratha");

		quest.history()
			.whenNpcWasMet("Napotkany został Hayunn Naratha.")
			.whenQuestWasRejected("Nie chcę upić Hayunna.")
			.whenQuestWasAccepted("Obiecałem mu kupić trunek od Margaret w tawernie Semos.")
			.whenTaskWasCompleted("Mam butelkę soku z chmielu.")
			.whenQuestWasCompleted("Przekazałem trunek Hayunnowi. Zapłacił mi 20 złotych monet i zdobyłem trochę doświadczenia.");

		// TODO: new QuestCompletedCondition(OTHER_QUEST_SLOT)),
		quest.offer()
			.respondToRequest("Zaschło mi w ustach, ale nie mogą zobaczyć, jak opuszczam tę salę dydaktyczną! Czy mógłbyś mi przynieść #trunek z #tawerny?")
			.respondToUnrepeatableRequest("Mimo wszystko dziękuję, ale nie chcę za bardzo wciągnąć się w picie; Wciąż jestem na służbie, wiesz! Będę potrzebował rozumu, jeśli pojawi się uczeń...")
			.respondToAccept("Dzięki! Nigdzie nie zamierzam się ruszać, będę czekać tutaj. I oczywiście pilnuje obowiązków.")
			.respondToReject("No cóż, w takim razie zapomnij. Chyba po prostu będę miał nadzieję, że zacznie padać, a potem stanę z otwartymi ustami.")
			.rejectionKarmaPenalty(5.0)
			.remind("Hej, wciąż czekam na swój sok z chmielu, pamiętasz? Tak czy inaczej, co mogę dla ciebie zrobić?")
			.respondTo("tavern", "tawerna").saying("Jeśli nie wiesz, gdzie jest zajazd, możesz zapytać starego Monogenesa, jest dobrym w udzielaniu wskazówek. Czy zamierzasz pomóc?")
			.respondTo("beer", "piwo", "trunek", "sok z chmielu").saying("Butelka chłodnego soku z chmielu od #Margaret w zupełności wystarczy. Więc, zrobisz to?")
			.respondTo("Margaret").saying("Margaret jest oczywiście ładną barmanką w tawernie! Wygląda nieźle... heh. Pójdziesz po to dla mnie?");

		quest.task()
			.requestItem(1, "sok z chmielu");

		quest.complete()
			.greet("Hej! Czy to piwo jest dla mnie?")
			.respondToReject("Aj! Pamiętasz, że cię o to prosiłem, prawda? Naprawdę przydałoby mi się to teraz.")
			.respondToAccept("*gul gul* Ach! To trafia idealnie. Daj mi znać, jeśli będziesz czegoś potrzebować, dobrze?")
			.rewardWith(new EquipItemAction("money", 20))
			.rewardWith(new IncreaseXPAction(50))
			.rewardWith(new IncreaseKarmaAction(10));
		return quest;
	}

	public boolean removeFromWorld() {
		final boolean res = ResetSpeakerNPC.reload(new RetiredAdventurerNPC(), "Hayunn Naratha");
		// reload other associated quests
		SingletonRepository.getStendhalQuestSystem().reloadQuestSlots("meet_hayunn");
		return res;
	}
}