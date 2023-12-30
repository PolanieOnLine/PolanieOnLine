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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ConditionalAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerCanEquipItemCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemdataItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestRegisteredCondition;
import games.stendhal.server.entity.npc.quest.BringItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Traps for Klaas
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Klaas (the Seaman that takes care of Athor's ferry's cargo)</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Klaas asks you to bring him rodent traps.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>1000 XP</li>
 * <li>5 greater antidote
 * <li>note to apothecary (if Antivenom Ring quest not started)
 * <li>Karma: 10</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Every 24 hours</li>
 * </ul>
 */
public class TrapsForKlaas implements QuestManuscript {
	private final static String QUEST_SLOT = "traps_for_klaas";

	@Override
	public BringItemQuestBuilder story() {
		BringItemQuestBuilder quest = new BringItemQuestBuilder();

		quest.info()
			.name("Pułapki Klaasa")
			.description("Klaas, opiekun ładunku na promie Athor, potrzebuje pułapek na gryzonie.")
			.internalName(QUEST_SLOT)
			.repeatableAfterMinutes(24 * 60)
			.minLevel(0)
			.region(Region.ATHOR_ISLAND)
			.questGiverNpc("Klaas");

		quest.history()
			.whenNpcWasMet("Rozmawiałem z Klaasem na statku do Athor.")
			.whenQuestWasRejected("Nie obchodzi mnie radzenie sobie z gryzoniami.")
			.whenQuestWasAccepted("Obiecałem zebrać 20 pułapek na gryzonie i dostarczyć je Klaasowi.")
			.whenTaskWasCompleted("Mam wystarczająco pułapek.")
			.whenQuestWasCompleted("Dostarczyłem pułapki na gryzonie Klaasowi. Otrzymałem trochę doświadczenia i antidotum.")
			.whenQuestCanBeRepeated("Powinienem sprawdzić, czy Klaas znowu nie potrzebuje mojej pomocy.");

		quest.offer()
			.respondToRequest("Szczury tu na dole dostały się do magazynu jedzenia. Czy pomożesz mi pozbyć się tego szkodnika?")
			.respondToUnrepeatableRequest("Dzięki za pułapki. Teraz jedzenie będzie bezpieczne. Ale może niedługo znów będę potrzebować twojej pomocy.")
			.respondToRepeatedRequest("Szczury tu na dole dostały się do magazynu jedzenia. Czy pomożesz mi pozbyć się tego szkodnika?")
			.respondToAccept("Dzięki, potrzebuję, abyś przyniósł mi 20 #pułapek na #gryzonie. Proszę się śpieszyć! Nie możemy sobie pozwolić na utratę więcej jedzenia.")
			.respondToReject("Nie marnuj mi czasu. Muszę chronić ładunek.")
			.rejectionKarmaPenalty(5.0)
			.remind("Wierzę, że już prosiłem cię o przyniesienie 20 pułapek na gryzonie.");

		final SpeakerNPC npc = NPCList.get().get("Klaas");
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("rodent trap", "trap", "rodent traps", "traps", "pułapka na gryzonie", "pułapka", "pułapki na gryzonie", "pułapki"),
				new QuestActiveCondition("traps_for_klaas"),
				ConversationStates.ATTENDING,
				"Nie znam nikogo, kto by je sprzedawał. Ale kiedyś słyszałem historię o facetu, który zabił dużego szczura i odkrył, że pułapka klapnęła się mu na stopę.",
				null);

		quest.task()
			.requestItem(20, "pułapka na gryzonie");

		quest.complete()
			.greet("Czy przyniosłeś jakieś pułapki?")
			.respondToReject("Proszę pospiesz się! Właśnie znalazłem kolejną skrzynię z jedzeniem, którą przegryzły.")
			.respondToAccept("Dzięki! Muszę je szybko rozstawić. Weź te antidotum jako nagrodę.")
			.rewardWith(new IncreaseXPAction(1000))
			.rewardWith(new IncreaseKarmaAction(10))
			.rewardWith(new EquipItemAction("mocne antidotum", 5))
			.rewardWith(new ConditionalAction(
					giveNoteRewardCondition,
					new MultipleActions(
							equipNoteAction,
							new SayTextAction("Kiedyś znałem starego #aptekarza. Weź tę notatkę do niego. Może pomoże ci w czymś."))));

		// Player has lost note
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestRegisteredCondition("antivenom_ring"),
						new NotCondition(new PlayerHasItemdataItemWithHimCondition("karteczka", info_string)),
						new PlayerCanEquipItemCondition("karteczka"),
						new QuestCompletedCondition(QUEST_SLOT),
						new QuestNotStartedCondition("antivenom_ring")),
				ConversationStates.ATTENDING,
				"Zgubiłeś notatkę? Cóż, myślę, że mogę napisać ci kolejną, ale tym razem bądź ostrożny."
				+ " Pamiętaj, aby dowiedzieć się w okolicy o #aptekarzu.",
				equipNoteAction);

		// player lost note, but doesn't have room in inventory
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new NotCondition(new PlayerHasItemdataItemWithHimCondition("karteczka", info_string)),
						new NotCondition(new PlayerCanEquipItemCondition("karteczka")),
						new QuestCompletedCondition(QUEST_SLOT),
						new QuestNotStartedCondition("antivenom_ring")),
				ConversationStates.ATTENDING,
				"Zgubiłeś notatkę? Cóż, mogę napisać kolejną. Ale wydaje się, że nie masz miejsca, aby ją przygarnąć.",
				null);

		return quest;
	}

	// action that gives player note to apothecary
	private static final String info_string = "liścik do aptekarza";
	final ChatAction equipNoteAction = new ChatAction() {
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			final Item note = SingletonRepository.getEntityManager().getItem("karteczka");
			note.setItemData(info_string);
			note.setDescription("Oto liścik skierowany do aptekarza. To rekomendacja od Klaasa.");
			note.setBoundTo(player.getName());
			player.equipOrPutOnGround(note);
		}
	};

	final ChatCondition giveNoteRewardCondition = new ChatCondition() {
		private final String avrQuestSlot = "antivenom_ring";

		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			if (!new QuestRegisteredCondition(avrQuestSlot).fire(player, sentence, npc)) {
				return false;
			}

			// note has already been given to Jameson & Antivenom Ring quest has already been started or completed
			if (player.getQuest(avrQuestSlot) != null) {
				return false;
			}

			// player already has a note
			// FIXME: PlayerOwnsItemIncludingBankCondition currently doesn't support itemdata items
			if (player.isEquippedWithItemdata("karteczka", info_string)) {
				return false;
			}

			return true;
		}
	};
}