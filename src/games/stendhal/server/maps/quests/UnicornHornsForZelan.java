/***************************************************************************
 *                    Copyright © 2003-2024 - Arianne                      *
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
import games.stendhal.server.entity.npc.quest.BringItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Unicorn Horns for Zelan
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Zelan</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Zelan needs 10 unicorn horns.</li>
 * <li>Bring the unicorn horns to him for a reward.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>50000 xp</li>
 * <li>karma</li>
 * <li>3 soup</li>
 * <li>20000 money</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>3 days</li>
 * </ul>
 */
public class UnicornHornsForZelan implements QuestManuscript {
	@Override
	public BringItemQuestBuilder story() {
		BringItemQuestBuilder quest = new BringItemQuestBuilder();

		quest.info()
			.name("Rogi Jednorożca dla Zelana")
			.internalName("unicorn_horns_for_zelan")
			.description("Zelan potrzebuje pomocy przy zbieraniu rogów jednorożca.")
			.region(Region.ATLANTIS)
			.questGiverNpc("Zelan")
			// 3 days
			.repeatableAfterMinutes(60 * 24 * 3);

		quest.history()
			.whenNpcWasMet("Zelan spytał mnie czy zdobędę 10 rogów jednorożca.")
			.whenQuestWasRejected("Nie chcę pomagać Zelanowi.")
			.whenQuestWasAccepted("Pomogę w zbieraniu rogów.")
			.whenTaskWasCompleted("Mam już wystarczająco rogów jednorożca.")
			.whenQuestWasCompleted("Zelan ma teraz możliwość wykonania swoich sztyletów.")
			.whenQuestCanBeRepeated("Muszę zapytać czy Zelan będzie potrzebował więcej pomocy.")
			.whenCompletionsShown("Otrzymał Zelan ode mnie pomoc [count] [time].");

		quest.offer()
			.respondToRequest("Cześć! Potrzebuję rogów jednorożca, żeby zrobić kilka sztyletów."
					+ " Jest to naprawdę niebezpieczne w lasach otaczających Atlantydę."
					+ " Jeśli jesteś odważny, przydałaby mi się pomoc w zbieraniu"
					+ " rogów jednorożca. Pomożesz mi?")
			.respondToAccept("Świetnie! Uważaj, tam jest dużo ogromnych"
					+ " stworów, a te centaury są naprawdę okropne.")
			.respondToReject("W porządku, znajdę kogoś, kto mi pomoże.")
			.rejectionKarmaPenalty(10.0)
			.remind("Poprosiłem cię o przyniesienie 10 rogów jednorożca.")
			.respondToUnrepeatableRequest("Dzięki, ale nie potrzebuję więcej pomocy jeszcze.")
			.respondToRepeatedRequest("Chcę wykonać więcej sztyletów więc będę"
					+ " potrzebował znów pomocy. Czy zechcesz zebrać więcej"
					+ " rogów jednorożca dla mnie?");

		quest.task()
			.requestItem(10, "róg jednorożca");

		quest.complete()
			.greet("Czy znalazłeś dla mnie rogi jednorożca?")
			.respondToReject("Poprosiłem cię o przyniesienie 10 rogów jednorożca.")
			.respondToAccept("Wielkie dzięki! W nagrodę mogę dać 3 zupy oraz 20000 sztuk złota.")
			.rewardWith(new IncreaseXPAction(50000))
			.rewardWith(new IncreaseKarmaAction(30.0))
			.rewardWith(new EquipItemAction("zupa", 3))
			.rewardWith(new EquipItemAction("money", 20000));

		// completions count is stored in 3rd index of quest slot
		quest.setCompletionsIndexes(2);

		return quest;
	}
}