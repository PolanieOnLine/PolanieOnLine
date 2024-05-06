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

import java.util.Arrays;

import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.action.EquipRandomAmountOfItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.BringItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Coal for Haunchy
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Haunchy Meatoch, the BBQ grillmaster on the Ados market</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Haunchy Meatoch asks you to fetch coal for his BBQ</li>
 * <li>Find some coal in Semos Mine or buy some from other players</li>
 * <li>Take the coal to Haunchy</li>
 * <li>Haunchy gives you a tasty reward</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>Karma +25 in all</li>
 * <li>XP +1000 in all</li>
 * <li>Some grilled steaks, random between 1 and 4.</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>You can repeat it each 2 days.</li>
 * </ul>
 * 
 * @author Vanessa Julius and storyteller
 */
public class CoalForHaunchy implements QuestManuscript {
	public BringItemQuestBuilder story() {
		BringItemQuestBuilder quest = new BringItemQuestBuilder();

		quest.info()
			.name("Węgiel do Grilla")
			.description("Haunchy Meatoch ma wątpliwości co do swojego ognia w grillu, jego zapas węgla może nie wystarczyć do usmażenia przepysznych steków. Czy Haunchy będzie potrzebował więcej?")
			.internalName("coal_for_haunchy")
			.repeatableAfterMinutes(2 * 24 * 60)
			.minLevel(0)
			.region(Region.ADOS_CITY)
			.questGiverNpc("Haunchy Meatoch");

		quest.history()
			.whenNpcWasMet("Haunchy Meatoch powitał mnie na rynku w Ados.")
			.whenQuestWasRejected("Poprosił mnie o dostarczenie kilku kawałków węgla, ale nie mam czasu na ich zbieranie.")
			.whenQuestWasAccepted("Ze względu, że płomień w grillu jest bardzo mały to przyrzekłem Haunchy'emu, że pomogę mu zdobyć węgiel do grilla.")
			.whenTaskWasCompleted("Mam już 25 kawałków węgla dla Haunchy'ego. Sądzę, że się ucieszy.")
			.whenQuestWasCompleted("Haunchy Meatoch był zadowolony, gdy otrzymał ode mnie węgiel. Ma go teraz wystarczająco dużo. W zamian dał mi kilka przepszynych steków ze swojego grilla!")
			.whenQuestCanBeRepeated("Założę się, że ta ilość jest znowu niska i potrzebuje więcej. Może dostanę więcej smacznych grillowanych steków.");


		quest.offer()
			.respondToRequest("Nie mogę wykorzystać polan do tego wielkiego grilla. Aby utrzymać temperaturę potrzebuję węgla, ale nie zostało go dużo. Problem w tym, że nie mogę go zdobyć ponieważ moje steki mogłyby się spalić i dlatego muszę tu zostać. Czy mógłbyś przynieść mi 25 kawałków #węgla do mojego grilla?")
			.respondToUnrepeatableRequest("Zapas węgla jest wystarczająco spory. Nie będę potrzebował go przez jakiś czas.")
			.respondToRepeatedRequest("Ostatni węgiel, który mi przyniosłeś, w większości został wykorzystany. Przyniesiesz mi więcej?")
			.respondToAccept("Dziękuję Ci! Na pewno dam ci miłą i smaczną nagrodę.")
			.respondTo("coal", "stone coal", "węgla", "węgiel").saying("Węgiel nie jest łatwy do znalezienia. Zwykle możesz go znaleźć gdzieś w ziemi, ale być może będziesz mieć szczęście i znajdziesz go w starych tunelach Semos... Pomożesz mi?")
			.respondToReject("Och nieważne. Myślałem, że lubisz grillowanie tak jak ja. A więc do zobaczenia.")
			.rejectionKarmaPenalty(10.0)
			.remind("Na szczęście mój grill wciąż pali. Ale proszę pospiesz się i przynieś mi 25 węgla, tak jak obiecałeś.");

		NPCList.get().get("Haunchy Meatoch").addReply(Arrays.asList("coal", "stone coal", "węgla", "węgiel"), "Czasami mógłbyś mi wyświadczyć #'przysługę'...");

		quest.task()
			.requestItem(25, "węgiel");

		quest.complete()
			.greet("Ach, widzę, że masz wystarczająco dużo węgla, żeby utrzymać grilla! Czy to dla mnie?")
			.respondToReject("No cóż, mam nadzieję, że ktoś inny mi pomoże, zanim mój grill zgaśnie.")
			.respondToAccept(null)
			.rewardWith(new IncreaseXPAction(200))
			.rewardWith(new IncreaseKarmaAction(20))
			.rewardWith(new EquipRandomAmountOfItemAction("grillowany stek", 1, 4, 1,
					"Dziękuję! Przyjmij oto [this_these] [number_item] z mojego grilla!"));

		// completions count is stored in 3rd index of quest slot
		quest.setCompletionsIndexes(2);

		return quest;
	}
}
