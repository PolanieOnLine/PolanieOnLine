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
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.KillCreaturesQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.TimeUtil;

/**
 * QUEST: Clean Athors underground
 *
 * PARTICIPANTS: <ul>
 * <li> NPC on Athor island
 * <li> one of each creature in Athor underground
 * </ul>
 *
 * STEPS:<ul>
 * <li> John on Athor island asks players to kill some creatures of the dungeon for him, cause he can't explore it otherwise
 * <li> Kill them for him and go back to the NPC to get your reward
 * </ul>
 *
 *
 * REWARD:<ul>
 * <li> 20000 XP
 * <li> 10 greater potion
 * <li> Karma: 11 total (10 + 1)
 * </ul>
 *
 * REPETITIONS: <ul><li>once in a week</ul>
 *
 * @author Vanessa Julius, idea by anoyyou
 */
public class CleanAthorsUnderground implements QuestManuscript {
	@Override
	public KillCreaturesQuestBuilder story() {
		KillCreaturesQuestBuilder quest = new KillCreaturesQuestBuilder();

		quest.info()
			.name("Sprzątanie Podziemia Athor")
			.description("John i jego żona Jane chcą zwiedzić podziemia Athor podczas swoich wakacji, ale niestety nie mogą.")
			.internalName("clean_athors_underground")
			.repeatableAfterMinutes(TimeUtil.MINUTES_IN_WEEK)
			.minLevel(70)
			.region(Region.ATHOR_ISLAND)
			.questGiverNpc("John");

		quest.history()
			.whenNpcWasMet("Poznałem Johna na wyspie Athor.")
			.whenQuestWasRejected("Nie zamierzam zabijać stworzeń atakujących podziemia pod wyspą Athor.")
			.whenQuestWasAccepted("Muszę zabić po jednym z każdej istoty z podziemia Athor, aby pomóc Johnowi i Jane spędzić miłe wakacje!")
			.whenTaskWasCompleted("Zabiłem kilka stworzeń i powinienem wrócić do Johna.")
			.whenQuestWasCompleted("John i Jane w końcu mogą cieszyć się wakacjami!")
			.whenQuestCanBeRepeated("Minęło trochę czasu odkąd odwiedziłem Johna i Jane na wyspie Athor. Może teraz znowu potrzebują mojej pomocy.");

		quest.offer()
			.respondToRequest("Moja żona Jane i ja jesteśmy na wakacjach na wyspie Athor. #Niestety nie możemy zwiedzić całej wyspy ponieważ "
					+ "okropne #potwory uniemożliwiają nam to za każdym razem. Możesz nam pomóc zabijając parę z nich, aby uprzyjemnić nam wakacje?")
			.respondToUnrepeatableRequest("Te #potwory nie wrócą szybko i dzięki temu możemy zobaczyć wspaniałe miejsca.")
			.respondToRepeatedRequest("Te #potwory wróciły od tamtego czasu, gdy nam pomogłeś. Możesz znów nam pomóc?")
			.respondToAccept("Cudownie! Nie możemy się doczekać na twój powrót. Zabij po jednym z tych potworów w podziemiach wyspy Athor. Założe się, że dostaniesz je wszystkie!")
			.respondToReject("Och, nie ważne. W takim razie pójdziemy dalej się opalać. Nie dlatego, że już jesteśmy zmęczeni tym...")
			.respondTo("unfortunately", "niestety").saying("Tak, niestety. Chcieliśmy spedzić wspaniale czas, ale jedyne co zrobiliśmy to spędziliśmy czas na plaży.")
			.respondTo("creatures", "potwory", "potworów").saying("Chcemy zwiedzić pierwszą część podziemi, która wygląda na bardzo interesującą, ale te okropne coś tam rzucają się na nas, nawet mumie! Pomożesz?")
			.remind("Proszę uwolnij te wspaniałe miejsca od tych okropnych potworów!");

		quest.task()
			.requestKill(1, "mumia")
			.requestKill(1, "mumia królewska")
			.requestKill(1, "mnich")
			.requestKill(1, "mnich ciemności")
			.requestKill(1, "nietoperz")
			.requestKill(1, "brązowy glut")
			.requestKill(1, "zielony glut")
			.requestKill(1, "czarny glut")
			.requestKill(1, "minotaur")
			.requestKill(1, "błękitny smok")
			.requestKill(1, "kamienny golem");

		quest.complete()
			.greet("Wspaniale! Jak widzę zabiłeś te okropne potwory! Mam nadzieję, że nie wrócą zbyt szybko, bo nie będziemy mieli szansy na zwiedzenie paru miejsc."
					+ " Proszę, przyjmij te wielkie eliksiry w nagrodę za swoją pomoc.")
			.rewardWith(new IncreaseXPAction(20000))
			.rewardWith(new IncreaseKarmaAction(10.0))
			.rewardWith(new EquipItemAction("wielki eliksir", 10));

		// completions count is stored in 3rd index of quest slot
		quest.setCompletionsIndexes(2);

		return quest;
	}
}
