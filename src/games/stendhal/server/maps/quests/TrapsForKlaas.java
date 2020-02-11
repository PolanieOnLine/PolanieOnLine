/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerCanEquipItemCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasInfostringItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestRegisteredCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
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
 * <li>Can sell rodent traps to Klaas</li>
 * <li>Karma: 10</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Every 24 hours</li>
 * </ul>
 */
public class TrapsForKlaas extends AbstractQuest {

	public final int REQUIRED_TRAPS = 20;

    // Time player must wait to repeat quest (1 day)
    private static final int WAIT_TIME = 60 * 24;

	private static final String QUEST_SLOT = "traps_for_klaas";
	private static final String info_string = "liścik do aptekarza";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Rozmawiałem z Klaasem.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę mieć nic do czynienia z gryzoniami.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Przyrzekłem zdobyć " + REQUIRED_TRAPS + " pułapki na gryzonie i dostarczyć je Klaasowi.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "done")) {
			res.add("Dałem pułapki na gryzonie Klaasowi. Zdobyłem trochę doświadczenia i antidota.");
		}
		if (isRepeatable(player)) {
		    res.add("Powinienem sprawdzić czy Klaas znów nie potrzebuje mojej pomocy.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Klaas");

		// Player asks for quest
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
		        new AndCondition(
		                new NotCondition(new QuestActiveCondition(QUEST_SLOT)),
		                new TimePassedCondition(QUEST_SLOT, 1, WAIT_TIME)
		                ),
			ConversationStates.QUEST_OFFERED,
			"Szczury tutaj dostają się do spichlerza. Czy pomożesz mi w uwolnieniu nas od tego plugastwa?",
			null);

        // Player requests quest before wait period ended
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
                new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WAIT_TIME)),
			ConversationStates.ATTENDING,
			null,
                new SayTimeRemainingAction(QUEST_SLOT, 1, WAIT_TIME, "Dziękuje za pułapki. Teraz jedzenie będzie bezpieczne, ale możliwe, że znów będę potrzbował twojej pomocy."));

		// Player asks for quest after already started
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Wiem, że już się ciebie pytałem o zdobycie " + REQUIRED_TRAPS + " pułapek na gryzonie.",
				null);

		// Player accepts quest
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję. Potrzebuję, abyś przyniósł mi " + REQUIRED_TRAPS + " #pułapka #na #gryzonie. Proszę pospiesz się! Już nie możemy sobie pozwolić na większą utratę jedzenia.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		// Player rejects quest
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			// Klaas walks away
			ConversationStates.IDLE,
			"Nie marnuj mojego czasu. Muszę chronić ładunek.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// Player asks about rodent traps
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("rodent trap", "trap", "rodent traps", "traps", "pułapka na gryzonie", "pułapka", "pułapki na gryzonie", "pułapki", "pułapek na gryzonie", "pułapek"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Nie znam nikogo kto nimi handluje, ale słyszałem historię o jednym człowieku, który zabił wielkiego szczura i odkrył pułapkę, która zamyka się na jego nogach.",
			null);

	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Klaas");

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
				// FIXME: PlayerOwnsItemIncludingBankCondition currently doesn't support infostring items
				if (player.isEquippedWithInfostring("karteczka", info_string)) {
					return false;
				}

				return true;
			}
		};

		// Reward
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("pułapka na gryzonie", 20));
		reward.add(new EquipItemAction("mocne antidotum", 5));
		reward.add(new IncreaseXPAction(1000));
		reward.add(new IncreaseKarmaAction(10));
        reward.add(new SetQuestAction(QUEST_SLOT, "done"));
        reward.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

        // action that gives player note to apothecary
        final ChatAction equipNoteAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item note = SingletonRepository.getEntityManager().getItem("karteczka");
				note.setInfoString(info_string);
				note.setDescription("Oto liścik do aptekarza. To jest rekomendacja od Klaasa.");
				note.setBoundTo(player.getName());
				player.equipOrPutOnGround(note);
			}
        };

		// Player has all 20 traps
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT),
						new PlayerHasItemWithHimCondition("pułapka na gryzonie")),
				ConversationStates.QUEST_ITEM_BROUGHT,
				"Przyniosłeś jakieś pułapki?",
				null);

		// Player is not carrying any traps
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(new PlayerHasItemWithHimCondition("pułapka na gryzonie"))),
			ConversationStates.ATTENDING,
			"Mógłbym użyć te #pułapki. W czym mogę ci pomóc?",
			null);

		// Player is not carrying 20 traps
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new PlayerHasItemWithHimCondition("pułapka na gryzonie"),
						new NotCondition(new PlayerHasItemWithHimCondition("pułapka na gryzonie", 20))),
				ConversationStates.ATTENDING,
				"Przykro mi, ale potrzebuję 20 #pułapek #na #gryzonie",
				null);

		// brings traps & has already started/completed antivenom ring quest
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						new NotCondition(giveNoteRewardCondition),
						new PlayerHasItemWithHimCondition("pułapka na gryzonie", 20)),
				ConversationStates.ATTENDING,
				"Dziękuję! Muszę je teraz przygotować tak szybko jak to możliwe. Weź te antidota jako nagrodę.",
				new MultipleActions(reward));
		
		// brings traps & has not started antivenom ring quest
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						giveNoteRewardCondition,
						new PlayerHasItemWithHimCondition("pułapka na gryzonie", 20)),
				ConversationStates.ATTENDING,
				"Dziękuję! Muszę je teraz przygotować tak szybko jak to możliwe. Weź te antidota jako nagrodę. Znam starego #aptekarza. Zabierz do niego ten liścik. Może w czymś ci pomoże.",
				new MultipleActions(
						new MultipleActions(reward),
						equipNoteAction));

        // Player says did not bring items
		npc.add(
            ConversationStates.QUEST_ITEM_BROUGHT,
            ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Znam starego aptekarza, ale nie wiem gdzie teraz mieszka. Może ktoś w Ados będzie wiedział.",
			null);

		// Player asks about the apothecary
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("apothecary", "aptekarza", "aptekarz"),
			null,
			ConversationStates.ATTENDING,
			"Proszę pospiesz się! Odkryłem, że dobrały się do kolejnej skrzyni z jedzeniem.",
			null);

		// Player has lost note
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestRegisteredCondition("antivenom_ring"),
						new NotCondition(new PlayerHasItemWithHimCondition(info_string)),
						new PlayerCanEquipItemCondition("karteczka"),
						new QuestCompletedCondition(QUEST_SLOT),
						new QuestNotStartedCondition("antivenom_ring")),
				ConversationStates.ATTENDING,
				"Zgubiłeś liścik? Cóż napiszę kolejny, ale bądź ostrożny tym razem."
				+ " Pamiętaj by zapytać się o #aptekarza.",
				equipNoteAction);
		
		// player lost note, but doesn't have room in inventory
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new NotCondition(new PlayerHasInfostringItemWithHimCondition("karteczka", info_string)),
						new NotCondition(new PlayerCanEquipItemCondition("karteczka")),
						new QuestCompletedCondition(QUEST_SLOT),
						new QuestNotStartedCondition("antivenom_ring")),
				ConversationStates.ATTENDING,
				"Zgubiłeś liścik do aptekarza? Cóż, mógłbym napisać kolejny, ale nie wygląda, że masz wolne miejsce na to.",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pułapki dla Klaasa",
				"Klaas opiekun towaru na promie Athor potrzebuje pułapek na gryzonie.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "TrapsForKlaas";
	}

	public String getTitle() {

		return "Pułapki dla Klaasa";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.ATHOR_ISLAND;
	}

	@Override
	public String getNPCName() {
		return "Klaas";
	}
}
