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
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.mapstuff.chest.StoredChest;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropInfostringItemAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
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

public class Burglary extends AbstractQuest {
	private static final Logger logger = Logger.getLogger(Burglary.class);

	public static final String QUEST_SLOT = "burglary";
	private final SpeakerNPC npc = npcs.get("Tajemnicza osoba");

	private void prepareRequestingStep() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"*szept* Będę miał dla ciebie bardzo specjalne zadanie. Jesteś zainteresowany?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Wykonałeś to co miałeś do zrobienia.",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"*szept* Weź te wytrychy. Włamiesz się do domu sołtysa. Tam powinna gdzieś znaleźć się skrzynka, potrzebuję jej zawartości.",
			new MultipleActions(
					new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", -5.0),
					new EquipItemAction("wytrychy", 1, true),
					new ChatAction() {
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							Burglary.prepareChest();
						}
					}));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"...",
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
								new PlayerHasInfostringItemWithHimCondition("góralski kapelusz", QUEST_SLOT),
								new PlayerHasInfostringItemWithHimCondition("cuha góralska", QUEST_SLOT),
								new PlayerHasInfostringItemWithHimCondition("sztabka złota", QUEST_SLOT)))),
			ConversationStates.ATTENDING, 
			"Nie próbuj mnie oszukiwać! *szept* Wróć z tymi przedmiotami do mnie...",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropInfostringItemAction("góralski kapelusz", QUEST_SLOT));
		reward.add(new DropInfostringItemAction("cuha góralska", QUEST_SLOT));
		reward.add(new DropItemAction("wytrychy"));
		reward.add(new IncreaseXPAction(4000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(5));
		reward.add(
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser npc) {
					player.dropWithInfostring("sztabka złota", QUEST_SLOT, 15);
					Burglary.prepareChest();
				}
			});

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new PlayerHasInfostringItemWithHimCondition("góralski kapelusz", QUEST_SLOT),
					new PlayerHasInfostringItemWithHimCondition("cuha góralska", QUEST_SLOT),
					new PlayerHasInfostringItemWithHimCondition("sztabka złota", QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Dzięki... Tym razem się przekona co to znaczy być okradzionym...",
			new MultipleActions(reward));
	}

	private static void prepareChest() {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_tatry_kuznice_soltys_house");

		final StoredChest chest = new StoredChest();
		chest.setPosition(5, 23);
		zone.add(chest);

		try {
			Item item = SingletonRepository.getEntityManager().getItem("góralski kapelusz");
			item.setInfoString(QUEST_SLOT);
			item.setDescription("Oto góralski kapelusz należący do garderoby Sołtysa.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("cuha góralska");
			item.setInfoString(QUEST_SLOT);
			item.setDescription("Oto cuha góralska należąca do garderoby Sołtysa.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("sztabka złota");
			((StackableItem) item).setQuantity(15);
			item.setInfoString(QUEST_SLOT);
			item.setDescription("Oto sztabki złota, które są własnością Sołtysa.");
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
