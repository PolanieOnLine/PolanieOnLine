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
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.mapstuff.chest.StoredChest;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.DropItemdataItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemdataItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.game.RPObject;
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
		final ChatCondition hasRequiredGold =
				new PlayerHasItemdataItemWithHimCondition("sztabka złota", QUEST_SLOT, 15);
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(
						new AndCondition(
								new PlayerHasItemdataItemWithHimCondition("góralski kapelusz", QUEST_SLOT),
								new PlayerHasItemdataItemWithHimCondition("cuha góralska", QUEST_SLOT),
								hasRequiredGold))),
			ConversationStates.ATTENDING, 
			"Nie próbuj mnie oszukiwać! *szept* Wróć z tymi przedmiotami do mnie...",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemdataItemAction("góralski kapelusz", QUEST_SLOT));
		reward.add(new DropItemdataItemAction("cuha góralska", QUEST_SLOT));
		reward.add(new DropItemAction("wytrychy"));
		reward.add(new IncreaseXPAction(4000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(5));
		reward.add(
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser npc) {
					Burglary.prepareChest();
				}
			});

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemdataItemWithHimCondition("góralski kapelusz", QUEST_SLOT),
					new PlayerHasItemdataItemWithHimCondition("cuha góralska", QUEST_SLOT),
					hasRequiredGold),
			ConversationStates.ATTENDING,
			"Dzięki... Tym razem się przekona co to znaczy być okradzionym...",
			new MultipleActions(reward) {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser npc) {
					if (!player.dropWithItemdata("sztabka złota", QUEST_SLOT, 15)) {
						npc.say("Wróć z piętnastoma sztabkami złota należącymi do Sołtysa.");
						return;
					}
					super.fire(player, sentence, npc);
				}
			});
	}

	private static void prepareChest() {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_tatry_kuznice_soltys_house");

		final List<StoredChest> existingChests = zone.getEntitiesAt(5, 23, StoredChest.class);
		final StoredChest chest;
		if (existingChests.isEmpty()) {
			chest = new StoredChest();
			chest.setPosition(5, 23);
			zone.add(chest);
		} else {
			chest = existingChests.get(0);
		}

		try {
			addMissingQuestItem(chest, "góralski kapelusz", 1,
					"Oto góralski kapelusz należący do garderoby Sołtysa.");
			addMissingQuestItem(chest, "cuha góralska", 1,
					"Oto cuha góralska należąca do garderoby Sołtysa.");
			addMissingQuestItem(chest, "sztabka złota", 15,
					"Oto sztabki złota, które są własnością Sołtysa.");
		} catch (SlotIsFullException e) {
			logger.info("Could not add items to quest chest", e);
		}
		chest.notifyWorldAboutChanges();
	}

	private static void addMissingQuestItem(final StoredChest chest, final String name,
			final int amount, final String description) {
		int missing = amount;
		for (final RPObject object : chest.getSlot("content")) {
			if (object instanceof Item) {
				final Item item = (Item) object;
				if (name.equals(item.getName()) && QUEST_SLOT.equals(item.getItemData())) {
					missing -= item.getQuantity();
					if (missing <= 0) {
						return;
					}
				}
			}
		}
		final Item item = SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.quest());
		if (item instanceof StackableItem) {
			((StackableItem) item).setQuantity(missing);
		}
		item.setItemData(QUEST_SLOT);
		item.setDescription(description);
		chest.add(item);
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
		res.add(player.getGenderVerb("Rozmawiałem") + " z tajemniczą osobą.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie dam się namówić na złe zamiary!");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add(player.getGenderVerb("Zgodziłem") + " się włamać do domu sołtysa.");
		}
		if ("done".equals(questState)) {
			res.add(player.getGenderVerb("Przekazałem") + " zawartość skrzynki tajemniczej osobie.");
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
