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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.chest.Chest;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropInfostringItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasInfostringItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.game.SlotIsFullException;

public class PrinceSupply extends AbstractQuest {
	private static final Logger logger = Logger.getLogger(PrinceSupply.class);

	public static final String QUEST_SLOT = "prince_supply";
	private final SpeakerNPC npc = npcs.get("Książę");

	private void prepareRequestingStep() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Poszukuję osoby, która odbije część wyposażenia dla moich rycerzy. Jesteś chętny?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Dziękuję, ale wykonałeś swoje zadanie.",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Wejdź do budynku z arsenałem, znajduje się obok kuźni kowala.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
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
								new PlayerHasInfostringItemWithHimCondition("kolczuga", QUEST_SLOT),
								new PlayerHasInfostringItemWithHimCondition("zbroja płytowa", QUEST_SLOT),
								new PlayerHasInfostringItemWithHimCondition("spodnie kolcze", QUEST_SLOT),
								new PlayerHasInfostringItemWithHimCondition("hełm kolczy", QUEST_SLOT),
								new PlayerHasInfostringItemWithHimCondition("buty kolcze", QUEST_SLOT)))),
			ConversationStates.ATTENDING, 
			"Masz wrócić do mnie z potrzebnym wyposażeniem!",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropInfostringItemAction("kolczuga", QUEST_SLOT));
		reward.add(new DropInfostringItemAction("zbroja płytowa", QUEST_SLOT));
		reward.add(new DropInfostringItemAction("spodnie kolcze", QUEST_SLOT));
		reward.add(new DropInfostringItemAction("hełm kolczy", QUEST_SLOT));
		reward.add(new DropInfostringItemAction("buty kolcze", QUEST_SLOT));
		reward.add(new IncreaseXPAction(9500));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
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
					new PlayerHasInfostringItemWithHimCondition("kolczuga", QUEST_SLOT),
					new PlayerHasInfostringItemWithHimCondition("zbroja płytowa", QUEST_SLOT),
					new PlayerHasInfostringItemWithHimCondition("spodnie kolcze", QUEST_SLOT),
					new PlayerHasInfostringItemWithHimCondition("hełm kolczy", QUEST_SLOT),
					new PlayerHasInfostringItemWithHimCondition("buty kolcze", QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Dziękuję w imieniu całego królestwa...",
			new MultipleActions(reward));
	}

	private static void prepareChest() {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_warszawa_arsenal");

		final Chest chest = new Chest();
		chest.setPosition(4, 2);
		zone.add(chest);

		try {
			Item item = SingletonRepository.getEntityManager().getItem("kolczuga");
			item.setInfoString(QUEST_SLOT);
			item.setDescription("Oto kolczuga należąca do specjalnego wyposażenia armii Książęcej.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("zbroja płytowa");
			item.setInfoString(QUEST_SLOT);
			item.setDescription("Oto zbroja płytowa należąca do specjalnego wyposażenia armii Książęcej.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("spodnie kolcze");
			item.setInfoString(QUEST_SLOT);
			item.setDescription("Oto spodnie kolcze należące do specjalnego wyposażenia armii Książęcej.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("hełm kolczy");
			item.setInfoString(QUEST_SLOT);
			item.setDescription("Oto hełm kolczy należące do specjalnego wyposażenia armii Książęcej.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("buty kolcze");
			item.setInfoString(QUEST_SLOT);
			item.setDescription("Oto buty kolcze należące do specjalnego wyposażenia armii Książęcej.");
			chest.add(item);
		} catch (SlotIsFullException e) {
			logger.info("Could not add items to quest chest", e);
		}
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Włamanie",
				"Tajemnicza osoba w tawernie w Kuźnicach potrzebuje w pewnej sprawie pomocy.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
		prepareChest();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Rozmawiałem z tajemniczą osobą.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie dam się namówić na złe zamiary!");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Zgodziłem się włamać do domu sołtysa.");
		}
		if ("done".equals(questState)) {
			res.add("Przekazałem zawartość skrzynki tajemniczej osobie.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Włamanie";
	}

	@Override
	public String getRegion() {
		return Region.TATRY_MOUNTAIN;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
