/***************************************************************************
 *                   (C) Copyright 2018-2021 - Stendhal                    *
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
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.chest.StoredChest;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemdataItemAction;
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
import games.stendhal.server.entity.npc.condition.PlayerHasItemdataItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.game.SlotIsFullException;

public class PrinceSupply extends AbstractQuest {
	private static final Logger logger = Logger.getLogger(PrinceSupply.class);

	public static final String QUEST_SLOT = "prince_supply";
	private final SpeakerNPC npc = npcs.get("Książę");

	private static final int REQUIRED_MINUTES = 1440;

	private void prepareRequestingStep() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Poszukuję osoby, która odbije część wyposażenia dla moich rycerzy. Jesteś chętny?",
			null);

		// player asks about quest which he has done already and he is allowed to repeat it
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES),
					new QuestStateStartsWithCondition(QUEST_SLOT, "done;")),
			ConversationStates.QUEST_OFFERED,
			"Moja armia musi być przygotowana na wygnanie buntowników z zamku! Odbijesz mi arsenał?",
			null);
		
		// player asks about quest which he has done already but it is not time to repeat it
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
				new NotCondition(
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
					new QuestStateStartsWithCondition(QUEST_SLOT, "done;")),
			ConversationStates.ATTENDING,
			null,
			new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES,
			"Musimy przeliczyć wyposażenie. Wróć do mnie w ciągu "));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Wejdź do budynku z arsenałem, znajduje się obok kuźni kowala.",
			new MultipleActions(
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", -5.0),
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						PrinceSupply.prepareChest();
					}
				}));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Być może nie zasłużyłeś na odpowiednią nagrodę.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void prepareBringingStep() {
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(
						new AndCondition(
								new PlayerHasItemdataItemWithHimCondition("kolczuga", QUEST_SLOT),
								new PlayerHasItemdataItemWithHimCondition("zbroja płytowa", QUEST_SLOT),
								new PlayerHasItemdataItemWithHimCondition("spodnie kolcze", QUEST_SLOT),
								new PlayerHasItemdataItemWithHimCondition("hełm kolczy", QUEST_SLOT),
								new PlayerHasItemdataItemWithHimCondition("buty kolcze", QUEST_SLOT)))),
			ConversationStates.ATTENDING, 
			"Masz wrócić do mnie z potrzebnym wyposażeniem!",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemdataItemAction("kolczuga", QUEST_SLOT));
		reward.add(new DropItemdataItemAction("zbroja płytowa", QUEST_SLOT));
		reward.add(new DropItemdataItemAction("spodnie kolcze", QUEST_SLOT));
		reward.add(new DropItemdataItemAction("hełm kolczy", QUEST_SLOT));
		reward.add(new DropItemdataItemAction("buty kolcze", QUEST_SLOT));
		reward.add(new IncreaseXPAction(9500));
		reward.add(new SetQuestAction(QUEST_SLOT, "done;"));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		reward.add(new IncreaseKarmaAction(15));
		reward.add(
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser npc) {
					PrinceSupply.prepareChest();
				}
			});

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new PlayerHasItemdataItemWithHimCondition("kolczuga", QUEST_SLOT),
					new PlayerHasItemdataItemWithHimCondition("zbroja płytowa", QUEST_SLOT),
					new PlayerHasItemdataItemWithHimCondition("spodnie kolcze", QUEST_SLOT),
					new PlayerHasItemdataItemWithHimCondition("hełm kolczy", QUEST_SLOT),
					new PlayerHasItemdataItemWithHimCondition("buty kolcze", QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Dziękuję w imieniu całego królestwa...",
			new MultipleActions(reward));
	}

	private static void prepareChest() {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_warszawa_arsenal");

		final StoredChest chest = new StoredChest();
		chest.setPosition(4, 2);
		zone.add(chest);

		try {
			Item item = SingletonRepository.getEntityManager().getItem("kolczuga");
			item.setItemData(QUEST_SLOT);
			item.setDescription("Oto kolczuga należąca do specjalnego wyposażenia armii Książęcej.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("zbroja płytowa");
			item.setItemData(QUEST_SLOT);
			item.setDescription("Oto zbroja płytowa należąca do specjalnego wyposażenia armii Książęcej.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("spodnie kolcze");
			item.setItemData(QUEST_SLOT);
			item.setDescription("Oto spodnie kolcze należące do specjalnego wyposażenia armii Książęcej.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("hełm kolczy");
			item.setItemData(QUEST_SLOT);
			item.setDescription("Oto hełm kolczy należące do specjalnego wyposażenia armii Książęcej.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("buty kolcze");
			item.setItemData(QUEST_SLOT);
			item.setDescription("Oto buty kolcze należące do specjalnego wyposażenia armii Książęcej.");
			chest.add(item);
		} catch (SlotIsFullException e) {
			logger.info("Could not add items to quest chest", e);
		}
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Odbicie Arsenału",
				"Książęca armia musi odbić swój arsenał z rąk buntowników.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(Grammar.genderVerb(player.getGender(), "Rozmawiałem") + " z księciem.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie wykonam zadania księcia, ponieważ obawiam się, że zginę!");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add(Grammar.genderVerb(player.getGender(), "Zgodziłem") + " się na odzyskanie arsenał dla armii książecej.");
		}

		if (isCompleted(player)) {
			res.add(Grammar.genderVerb(player.getGender(), "Przekazałem") + " potrzebny arsenał Księciu.");
		}
		if(isRepeatable(player)){
			res.add("Podejrzewam, że Książe przeliczył już wyposażenie armii i będzie znów potrzebował pomocy.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Odbicie Arsenału";
	}

	@Override
	public String getRegion() {
		return Region.WARSZAWA;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public boolean isCompleted(final Player player) {
		return player.hasQuest(QUEST_SLOT) && !"start".equals(player.getQuest(QUEST_SLOT)) && !"rejected".equals(player.getQuest(QUEST_SLOT));
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(
				new QuestNotInStateCondition(QUEST_SLOT, "start"),
				new QuestStartedCondition(QUEST_SLOT),
				new TimePassedCondition(QUEST_SLOT, REQUIRED_MINUTES)).fire(player, null, null);
	}
}
