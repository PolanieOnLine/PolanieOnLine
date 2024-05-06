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

import games.stendhal.common.MathHelper;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.KillCreaturesQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Kill Gnomes
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Jenny, by the mill in Semos Plains
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Gnomes have been stealing carrots so Jenny asks you to kill some.
 * <li> You go kill the Gnomes in the gnome village and you get the reward from Jenny
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> 10 potions
 * <li> 1000 XP
 * <li> No karma (deliberately. Killing gnomes is mean!)
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> after 7 days.
 * </ul>
 */
public class KillGnomes implements QuestManuscript {
	@Override
	public KillCreaturesQuestBuilder story() {
		KillCreaturesQuestBuilder quest = new KillCreaturesQuestBuilder();

		quest.info()
			.name("Pozbycie się Gnomów")
			.description("Jenny nie jest szczęśliwa tym iż gnomy wciąż kradną jej cenne marchewki.")
			.internalName("kill_gnomes")
			.repeatableAfterMinutes(MathHelper.MINUTES_IN_ONE_WEEK)
			.minLevel(10)
			.region(Region.SEMOS_SURROUNDS)
			.questGiverNpc("Jenny");

		quest.history()
			.whenNpcWasMet("Spotkałem Jenny w okolicy młynku, na północ od Semos.")
			.whenQuestWasRejected("Nie chcę zostać mordercą tych słodkich gnomków.")
			.whenQuestWasAccepted("Zgodziłem się, aby pozbyć się niektórych gnomów, a szczególnie ich lidera. Dam im lekcję pokory!")
			.whenTaskWasCompleted("Gnomy trzymają się teraz z dala od marchewek Jenny. Hura!")
			.whenQuestWasCompleted("Jenny w nagrodę dała mi trochę eliksirów.")
			.whenQuestCanBeRepeated("Te brzydkie gnomy, zapomniały lekcji jaką im dałem. Kradną jeszcze więcej! Jenny znów potrzebuje mojej pomocy.");

		quest.offer()
			.respondToRequest("Gnomy kradną marchewki z naszej farmy na północ od Semos. "
					+ "Potrzebują chyba dobrej lekcji. Pomożesz?")
			.respondToUnrepeatableRequest("Gnomy nie sprawiają problemu od momentu, gdy pokazałeś im czym jest pokora.")
			.respondToRepeatedRequest("Te zuchwałe gnomy znowu kradną nasze marchewki. Sądzę, że potrzebują kolejnej lekcji. Pomożesz?")
			.respondToAccept("Doskonale. Obozowisko gnomów znajdziesz na północny-zachód od Semos. Upewnij się, że ubiłeś kilku liderów, co najmniej jednego zwiadowcę i jednego kawalerzystę.")
			.respondToReject("Masz rację. Chyba to zbyt okropne, aby wybijać gnomy, które kradną marchewki. "
					+ "Może farma powinna zwiększyć ochronę.")
			// no karma penalty for rejecting the quest because killing gnomes is evil
			.rejectionKarmaPenalty(0)
			.remind("Musisz nauczyć te zuchwałe gnomy lekcji zabijając kilku dla przykładu! "
					+ "Upewnij się, że dostałeś kilku liderów, co najmniej jednego zwiadowcę i jednego kawalerzystę.");

		quest.task()
			.requestKill(1, "gnom")
			.requestKill(1, "gnom zwiadowca")
			.requestKill(1, "gnom kawalerzysta");

		quest.complete()
			.greet("Widzę, że ubiłeś gnomy, które okradały farmę. Mam nadzieje, że przez jakiś czas nie będą się zbliżać do marchewek! "
					+ "Proszę weź te mikstury w dowód uznania.")
			.rewardWith(new IncreaseXPAction(1000))
			.rewardWith(new EquipItemAction("eliksir", 10));

		// completions count is stored in 3rd index of quest slot
		quest.setCompletionsIndexes(2);

		return quest;
	}
}
