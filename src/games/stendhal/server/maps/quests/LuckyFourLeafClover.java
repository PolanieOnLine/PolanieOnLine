/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
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

import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.deniran.cityoutside.CloverHunterNPC;
import games.stendhal.server.util.ResetSpeakerNPC;

/**
 * Rewards:
 * - 20 greater potions
 * - 1,000 XP
 * - karma
 * - 10,000 ATK XP (first time only)
 * - 10,000 DEF XP (first time only)
 * - +150 max HP (first time only)
 *
 * TODO:
 * - maybe convert to manuscript
 */
public class LuckyFourLeafClover extends AbstractQuest {
	private static String QUEST_SLOT = "lucky_four_leaf_clover";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Szczęśliwa Czterolistna Koniczynka";
	}

	@Override
	public String getNPCName() {
		return "Maple";
	}

	@Override
	public String getRegion() {
		return Region.DENIRAN_CITY;
	}

	@Override
	public List<String> getHistory(Player player) {
		List<String> res = new LinkedList<>();

		String npcName = getNPCName();
		String state = player.getQuest(QUEST_SLOT, 0);
		int count = MathHelper.parseIntDefault(player.getQuest(QUEST_SLOT, 1), 0);
		boolean hasClover = player.isEquipped("czterolistna koniczyna");

		res.add(npcName + " zapytała mnie, aby odnaleźć rzadką czterolistną koniczynkę.");
		if ("rejected".equals(state)) {
			res.add("Nie dziękuję, mam już wystarczająco dużo szczęścia, którego potrzebuje.");
		} else if ("start".equals(state)) {
			if (hasClover) {
				res.add("Została znaleziona! Jakie niebywałe szczęście!");
			} else {
				res.add("Jeszcze nie mam ani jeden.");
			}
		} else if ("done".equals(state)) {
			res.add("Fortuna mi sprzyja. Odnaleziona została czterolistna koniczynka.");
		}
		if (count > 0) {
			res.add("Została znaleziona oraz oddana w ilości " + count + " " + Grammar.plnoun(count, "czterolistna koniczyna")
					+ " do " + npcName + ".");
		}
		return res;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(getName(), getNPCName() + " poszukuje bardzo rzadkiej czterolistnej koniczynki", true, 1);
		requestStep();
		bringStep();
	}

	private void requestStep() {
		SpeakerNPC npc = SingletonRepository.getNPCList().get(getNPCName());

		ChatCondition isRepeat = new QuestStateGreaterThanCondition(QUEST_SLOT, 1, 0);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Już prosiłam, abyś znalazł mi czterolistną koniczynę.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new QuestNotActiveCondition(QUEST_SLOT),
					new NotCondition(isRepeat)),
			ConversationStates.QUEST_OFFERED,
			"Szukam rzadkiej czterolistnej koniczyny. Chcesz mi pomóc znaleźć taką?",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new QuestNotActiveCondition(QUEST_SLOT),
					isRepeat),
			ConversationStates.QUEST_OFFERED,
			"Chcę kolejną czterolistną koniczynę. Chcesz pomóc ponownie?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"W porządku, i tak znajdę jakąś na własną rękę.",
			new MultipleActions(
					new DecreaseKarmaAction(20),
					new SetQuestAction(QUEST_SLOT, 0, "rejected")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Świetnie! Przynieś mi, jeśli taką znajdziesz. A jeśli chcesz wskazówek na temat polowania na #koniczynę"
					+ ", po prostu zapytaj.",
			new SetQuestAction(QUEST_SLOT, 0, "start"));
	}

	private void bringStep() {
		SpeakerNPC npc = SingletonRepository.getNPCList().get(getNPCName());

		ChatCondition hasClover = new PlayerHasItemWithHimCondition("czterolistna koniczyna");
		ChatCondition canGiveClover = new AndCondition(
				new QuestActiveCondition(QUEST_SLOT),
				hasClover);
		ChatCondition isRepeat = new QuestStateGreaterThanCondition(QUEST_SLOT, 1, 0);

		ChatAction baseReward = new MultipleActions(
				new SetQuestAction(QUEST_SLOT, 0, "done"),
				new IncrementQuestAction(QUEST_SLOT, 1, 1),
				new DropItemAction("czterolistna koniczyna"),
				new EquipItemAction("duży eliksir", 20),
				new IncreaseXPAction(1000),
				new IncreaseKarmaAction(50));
		ChatAction firstReward = new MultipleActions(
				baseReward,
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						player.setAtkXP(player.getAtkXP() + 10000);
						player.setDefXP(player.getDefXP() + 10000);
						player.setBaseHP(player.getBaseHP() + 150);
					}
				});

		// TODO: add condition for saying "done" when not carrying clover

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			canGiveClover,
			ConversationStates.QUEST_ITEM_BROUGHT,
			"O mój! Znalazłeś koniczynę. Ma cztery liście! Czy mogę to mieć? Proszę, proszę, proszę...",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			//Arrays.asList("clover", "four-leaf clover", "koniczyna", "koniczynka", "czterolistna koniczyna"), // will interfere with standard response
			ConversationPhrases.FINISH_MESSAGES,
			canGiveClover,
			ConversationStates.QUEST_ITEM_BROUGHT,
			"O mój! Znalazłeś koniczynę. Ma cztery liście! Czy mogę to mieć? Proszę, proszę, proszę...",
			null);

		npc.add(
				ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Hmph! W takim razie znajdę jedną dla siebie.",
				// denying her the clover costs karma
				new DecreaseKarmaAction(25));

		// player dropped clover
		npc.add(
				ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(hasClover),
				ConversationStates.IDLE,
				"Co próbujesz wyciągnąć!? Widziałam, jak to upuściłeś.",
				// denying her the clover costs karma
				new DecreaseKarmaAction(25));

		npc.add(
				ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						hasClover,
						new NotCondition(isRepeat)),
				ConversationStates.IDLE,
				"Dziękuję bardzo! Mam zamiar dodać to do mojej kolekcji. Oto kilka eliksirów, które pozwolą mi okazać"
						+ " moją wdzięczność.",
				firstReward);

		npc.add(
				ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						hasClover,
						isRepeat),
				ConversationStates.IDLE,
				"Dziękuję bardzo! Mam zamiar dodać to do mojej kolekcji. Oto kilka eliksirów, które pozwolą mi okazać"
						+ " moją wdzięczność.",
				baseReward);
	}

	@Override
	public boolean removeFromWorld() {
		// TODO: should clover spawner be initialized & removed with quest instead of zone configurator?
		SpeakerNPC npc = SingletonRepository.getNPCList().get(getNPCName());
		StendhalRPZone npcZone = npc.getZone();
		if (npcZone != null) {
			return ResetSpeakerNPC.reload(new CloverHunterNPC(), npcZone, getNPCName());
		}
		return ResetSpeakerNPC.reload(new CloverHunterNPC(), getNPCName());
	}

	@Override
	public boolean isRepeatable(Player player) {
		// always repeatable
		return true;
	}
}