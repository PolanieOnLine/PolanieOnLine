/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
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
import games.stendhal.server.entity.npc.action.EquipRandomItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.BringItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Campfire
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Sally, a scout sitting next to a campfire near Or'ril</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Sally asks you for wood for her campfire</li>
 * <li> You collect 10 pieces of wood in the forest</li>
 * <li> You give the wood to Sally.</li>
 * <li> Sally gives you 10 meat or ham in return.<li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 10 meat or ham</li>
 * <li> 50 XP</li>
 * <li> Karma: 10</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> Unlimited, but 60 minutes of waiting are required between repetitions</li>
 * </ul>
 */
public class Campfire implements QuestManuscript {
	@Override
	public BringItemQuestBuilder story() {
		BringItemQuestBuilder quest = new BringItemQuestBuilder();

		quest.info()
			.name("Obozowisko")
			.description("Sally chce rozpalić ognisko, ale nie ma drewna.")
			.internalName("campfire")
			.repeatableAfterMinutes(60)
			.minLevel(0)
			.region(Region.ORRIL)
			.questGiverNpc("Sally");

		quest.history()
			.whenNpcWasMet("Spotkałem Sally na południe od Zamku Orril.")
			.whenQuestWasRejected("Nie chcę jej pomagać.")
			.whenQuestWasAccepted("Poprosiła mnie, żebym przyniósł 10 kawałków drewna, aby podtrzymać ogień.")
			.whenTaskWasCompleted("Znalazłem drewno potrzebne do podtrzymania ognia.")
			.whenQuestWasCompleted("Oddałem drewno Sally. W zamian dała mi jedzenie i węgiel drzewny. Zdobyłem również 50 punktów doświadczenia.")
			.whenQuestCanBeRepeated("Ognisko Sally znowu potrzebuje nieco drewna.");

		quest.offer()
			.respondToRequest("Potrzebuję więcej drewna, aby podtrzymać ogień w moim ognisku, ale nie mogę go zostawić bez opieki, żeby iść po nie! Czy mógłbyś przynieść trochę z lasu? Potrzebuję dziesięć kawałków.")
			.respondToUnrepeatableRequest("Dzięki, ale myślę, że drewno, które przyniosłeś, wystarczy na [remaining_time].")
			.respondToRepeatedRequest("Moje ognisko znowu potrzebuje drewna, dziesięć kawałków #drewna będzie wystarczające. Czy mógłbyś przynieść te kawałki #drewna z lasu dla mnie? Proszę powiedz tak!")
			.respondToAccept("Dobra. Drewno znajdziesz na północ od tego miejsca. Wróć, gdy zdobędziesz dziesięć kawałków drewna!")
			.respondToReject("Ojej, jak mam ugotować całe to mięso? Chyba będę musiała to po prostu podać zwierzętom...")
			.rejectionKarmaPenalty(5.0)
			.remind("Proszę, nie zapomnij, że obiecałeś przynieść dziesięć kawałków drewna!");

		quest.task()
			.requestItem(10, "polano");

		quest.complete()
			.greet("Cześć znów! Masz drewno, widzę; czy masz te 10 kawałków drewna, o które wcześniej pytałam?")
			.respondToReject("Och... cóż, mam nadzieję, że szybko coś znajdziesz; ten ogień zaraz zgaśnie!")
			.respondToAccept(null)
			.rewardWith(new IncreaseXPAction(50))
			.rewardWith(new IncreaseKarmaAction(10))
			.rewardWith(new EquipItemAction("węgiel drzewny", 10))
			.rewardWith(new EquipRandomItemAction("mięso=10;szynka=10", false, "Dziękuję! Weź proszę [this_these] [number_item] oraz węgiel drzewny!"));

		// completions count is stored in 3rd index of quest slot
		quest.setCompletionsIndexes(2);

		return quest;
	}

}