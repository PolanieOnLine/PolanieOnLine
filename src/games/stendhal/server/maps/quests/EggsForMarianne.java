/***************************************************************************
 *                    (C) Copyright 2019-2024 - Stendhal                   *
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

import games.stendhal.server.entity.npc.action.EquipRandomItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.BringItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

/**
 * QUEST: EggsForMarianne
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Marianne, a little girl looking for eggs</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Marianne asks you for eggs for her pancakes</li>
 * <li> You collect a dozen of eggs from chickens</li>
 * <li> You give a dozen of eggs to Marianne.</li>
 * <li> Marianne gives you some flowers in return.<li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> some pansy or daisies</li>
 * <li> 100 XP</li>
 * <li> Karma: 50</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> Unlimited, at least 60 minutes have to elapse before repeating</li>
 * </ul>
 */
public class EggsForMarianne implements QuestManuscript {
	@Override
	public BringItemQuestBuilder story() {
		BringItemQuestBuilder quest = new BringItemQuestBuilder();

		quest.info()
			.name("Jajka dla Marianny")
			.description("Mama Marianny chce zrobić placki i potrzebuje jajek.")
			.internalName("eggs_for_marianne")
			.repeatableAfterMinutes(60)
			.minLevel(0)
			.region(Region.DENIRAN_CITY)
			.questGiverNpc("Marianne");

		quest.history()
			.whenNpcWasMet("Spotkałem Mariannę w Mieście Deniran.")
			.whenQuestWasRejected("Poprosiła mnie, żebym przyniósł jej jajka, ale nie chcę jej pomagać.")
			.whenQuestWasAccepted("Poprosiła mnie, żebym przyniósł 12 jajek.")
			.whenTaskWasCompleted("Znalazłem jajka dla Marianny.")
			.whenQuestWasCompleted("Oddałem Mariannie jajka. W zamian dała mi kwiaty.")
			.whenQuestCanBeRepeated("Marianna znowu potrzebuje jajek.");

		quest.offer()
			.respondToRequest(
				"Potrzebuję tuzina jajek. " +
				"Moja mama poprosiła mnie, żebym zebrała jajka, bo zamierza zrobić mi placki! " +
				"Tylko boję się zbliżać do tych kurczaków! " +
				"Czy mógłbyś przynieść mi jajka?")
			.respondToUnrepeatableRequest("Dzięki! Myślę, że jajka, które już mi przyniosłeś, wystarczą na jakiś czas...")
			.respondToRepeatedRequest("Moja mama znowu potrzebuje jajek! Czy mógłbyś przynieść tuzin?")
			.respondToAccept(
				"Dobra. Jajka znajdziesz, polując na kurczaki... Boję się zbliżać do tych nieznośnych kurczaków! " +
				"Proszę, wróć, gdy znajdziesz wystarczającą ilość jajek dla mnie!")
			.respondToReject(
				"Ojej, co mam zrobić z tymi wszystkimi kwiatami? " +
				"Chyba po prostu zostawię je przy jakichś grobach...")
			.rejectionKarmaPenalty(5.0)
			.remind("Obiecałeś przyniesienie mi tuzina jajek...");

		quest.task()
			.requestItem(12, "jajo");

		quest.complete()
			.greet("Cześć ponownie! Masz kilka jajek, widzę. Masz może dla mnie te 12 jajek?")
			.respondToReject("Och... cóż, mam nadzieję, że szybko coś znajdziesz. Zaczynam być głodna!")
			.respondToAccept(null)
			.rewardWith(new IncreaseXPAction(50))
			.rewardWith(new IncreaseKarmaAction(50))
			.rewardWith(new EquipRandomItemAction("bratek=12;stokrotki=12", false, "Dziękuję! Weź [this_these] [number_item]!"));

		return quest;
	}
}