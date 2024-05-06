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

import games.stendhal.server.entity.npc.action.EquipRandomAmountOfItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.PlaySoundAction;
import games.stendhal.server.entity.npc.quest.BringItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

/**
 * QUEST: The Amazon Princess
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Princess Esclara, the Amazon Princess in a Hut on Amazon Island</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>The princess asks you for an exotic drink</li>
 * <li>Find someone who serves exotic drinks</li>
 * <li>Take exotic drink back to princess</li>
 * <li>Princess gives you a reward</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>Karma +25 in all</li>
 * <li>Some fish pie, random between 2 and 7.</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>You can repeat it once an hour.</li>
 * </ul>
 */
public class AmazonPrincess implements QuestManuscript {
	@Override
	public BringItemQuestBuilder story() {
		BringItemQuestBuilder quest = new BringItemQuestBuilder();

		quest.info()
			.name("Księżniczka Amazonki")
			.description("Spragniona księżniczka chce się napić.")
			.internalName("amazon_princess")
			.repeatableAfterMinutes(60)
			.minLevel(70)
			.region(Region.AMAZON_ISLAND)
			.questGiverNpc("Księżniczka Esclara");

		quest.history()
			.whenNpcWasMet("Księżniczka Esclara przywitała mnie w jej domu na wyspie Amazonek.")
			.whenQuestWasRejected("Poprosiła mnie, żebym przyniósł jej drinka, ale uważam, że nie powinna go pić.")
			.whenQuestWasAccepted("Księżniczka jest spragniona, obiecałem jej egzotyczny napój.")
			.whenTaskWasCompleted("Znalazłem napój z oliwką dla księżniczki, myślę, że jej zasmakuje.")
			.whenQuestWasCompleted("Księżniczka Esclara uwielbia napój z oliwką, którą jej przyniosłem. Dała mi placki rybne i całusa!!")
			.whenQuestCanBeRepeated("Ale założę się, że jest będzie spragniona ponownie. Może dostanę więcej placków rybnych.");

		quest.offer()
			.respondToRequest("Szukam napoju, powinien być egzotyczny. Możesz mi przynieść?")
			.respondToUnrepeatableRequest("Jestem pewna, że nie będę teraz w stanie wypić kolejnego takiego napoju.") // TODO:  for at least [time_remaining]
			.respondToRepeatedRequest("Ostatni koktajl, który mi przyniosłeś, był taki cudowny. Przyniesiesz mi jeszcze jeden?")
			.respondToAccept("Dziękuję! Jeśli będziesz w posiadaniu takiego napoju, na pewno dam ci miłą nagrodę.")
			.respondToReject("Och nieważne. A więc do zobaczenia.")
			.rejectionKarmaPenalty(10.0)
			.remind("Lubię te egzotyczne napoje, tylko zapomniałam nazwy mojego ulubionego.");

		// Get Drink Step : athor/cocktail_bar/BarmanNPC.java he serves drinks to all, not just those with the quest
		quest.task()
			.requestItem(1, "napój z oliwką");

		quest.complete()
			.greet("Ach, rozumiem, masz §'napój z oliwką'. Czy to dla mnie?")
			.respondToReject("No cóż, mam nadzieję, że ktoś inny pomoże.")
			.respondToAccept(null)
			.rewardWith(new IncreaseKarmaAction(15))
			.rewardWith(new EquipRandomAmountOfItemAction("tarta z rybnym nadzieniem", 1, 6, 1,
					"Dziękuję! Przyjmij proszę [this_these] [number_item] mojej kuchni, oraz tego całusa ode mnie."))
			.rewardWith(new PlaySoundAction("kiss-female-01"))
			.rewardWith(new InflictStatusOnNPCAction("napój z oliwką"));

		// completions count is stored in 3rd index of quest slot
		quest.setCompletionsIndexes(2);

		return quest;
	}
}