/***************************************************************************
 *                    Copyright © 2003-2023 - Arianne                      *
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
import games.stendhal.server.entity.npc.quest.BringItemTask;
import games.stendhal.server.entity.npc.quest.QuestBuilder;
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
	public QuestBuilder<?> story() {
		QuestBuilder<BringItemTask> quest = new QuestBuilder<>(new BringItemTask());

		final String npcName = "Zelan";
		final int quantity = 10;
		final String itemName = "róg jednorożca";
		final String plItemName = "rogów jednorożca";
		final int rewardSoup = 3;
		final int rewardMoney = 20000;

		quest.info()
			.name("Rogi Jednorożca dla Zelana")
			.internalName("unicorn_horns_for_zelan")
			.description("Zelan potrzebuje pomocy przy zbieraniu " + plItemName + ".")
			.region(Region.ATLANTIS)
			.questGiverNpc(npcName)
			// 3 days
			.repeatableAfterMinutes(60 * 24 * 3);

		quest.history()
			.whenNpcWasMet(npcName + " spytał mnie czy zdobędę " + quantity
					+ " " + plItemName + ".")
			.whenQuestWasRejected("Nie chcę pomagać " + npcName + ".")
			.whenQuestWasAccepted("Pomogę w zbieraniu rogów.")
			.whenTaskWasCompleted("Mam już wystarczająco " + plItemName + ".")
			.whenQuestWasCompleted(npcName + " ma teraz możliwość wykonania swoich sztyletów.")
			.whenQuestCanBeRepeated("Muszę zapytać czy " + npcName + " będzie potrzebował"
					+ " więcej pomocy.")
			.whenCompletionsShown("Otrzymał ode mnie pomoc " + npcName + " [count]"
					+ " [time].");

		quest.offer()
			.respondToRequest("Cześć! Potrzebuję " + plItemName + ", żeby zrobić kilka sztyletów."
					+ " Jest to naprawdę niebezpieczne w lasach otaczających Atlantydę."
					+ " Jeśli jesteś odważny, przydałaby mi się pomoc w zbieraniu"
					+ " " + plItemName + ". Pomożesz mi?")
			.respondToAccept("Świetnie! Uważaj, tam jest dużo ogromnych"
					+ " stworów, a te centaury są naprawdę okropne.")
			.respondToReject("W porządku, znajdę kogoś, kto mi pomoże.")
			.acceptedKarmaReward(10.0)
			.rejectionKarmaPenalty(10.0)
			//~ .remind("I have already asked you to get " + quantity
					//~ + " " + plItemName + ". Are you #done?");
			.remind("Spytałem się już ciebie o zdobyciu dla mnie " + quantity + " " + plItemName
					+ ".")
			.respondToUnrepeatableRequest("Dzięki, ale nie potrzebuję więcej"
					+ " pomocy jeszcze.")
			.respondToRepeatedRequest("Chcę wykonać więcej sztyletów więc będę"
					+ " potrzebował znów pomocy. Czy zechcesz zebrać więcej "
					+ plItemName + " dla mnie?");

		quest.task()
			.requestItem(quantity, itemName);

		quest.complete()
			.greet("Czy masz przy sobie " + quantity + " " + plItemName + "?")
			.respondToReject("Pytałem wcześniej o przyniesienie dla mnie " + quantity + " "
					+ plItemName + ".")
			.respondToAccept("Wielkie dzięki! W nagrodę mogę dać "
					+ rewardSoup + " zup oraz " + rewardMoney + " money.")
			.rewardWith(new IncreaseXPAction(50000))
			.rewardWith(new IncreaseKarmaAction(30.0))
			.rewardWith(new EquipItemAction("zupa", rewardSoup))
			.rewardWith(new EquipItemAction("money", rewardMoney));

		return quest;
	}
}