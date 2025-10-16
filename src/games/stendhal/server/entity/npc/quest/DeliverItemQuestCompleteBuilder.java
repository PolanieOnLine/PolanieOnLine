/***************************************************************************
 *                 (C) Copyright 2003-2024 - Faiumoni e.V.                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.quest;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.StringUtils;

/**
 * defines how the NPC react after the player completes the quest
 *
 * @author hendrik
 */
public class DeliverItemQuestCompleteBuilder extends QuestCompleteBuilder {

	private static Logger logger = Logger.getLogger(DeliverItemQuestCompleteBuilder.class);

	private DeliverItemTask deliverItemTask;
	private String respondToItemWithoutQuest;
	private String respondToItemForOtherNPC;
	private String respondToMissingItem;
	private String npcStatusEffect;

	DeliverItemQuestCompleteBuilder(DeliverItemTask deliverItemTask) {
		this.deliverItemTask = deliverItemTask;
	}

	private class HandOverItemAction implements ChatAction {
		private final DeliverItemTask deliverItemTask;
		private final String questSlot;

		public HandOverItemAction(DeliverItemTask deliverItemTask, String questSlot) {
			this.deliverItemTask = deliverItemTask;
			this.questSlot= questSlot;
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			if (player.isEquipped(deliverItemTask.getItemName())) {
				final DeliverItemOrder data = deliverItemTask.getOrders().get(npc.getName());
				Map<String, Object> params = new HashMap<>();
				params.put("flavor", data.getFlavor());
				params.put("tip", data.getTip());
				for (final Item item : player.getAllEquipped(deliverItemTask.getItemName())) {
					final String flavor = item.getItemData();
					if (data.getFlavor().equals(flavor)) {
						player.drop(item);
						// Check whether the player was supposed to deliver this item.
						if (player.hasQuest(questSlot) && !player.isQuestCompleted(questSlot)) {
							final boolean tooLate = deliverItemTask.isDeliveryTooLate(player, questSlot);
							if (tooLate) {
								npc.say(StringUtils.substitute(data.getRespondToSlowDelivery(), params));
								player.addXP(data.getXp() / 2);
							} else {
								npc.say(StringUtils.substitute(data.getRespondToFastDelivery(), params));
								final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
								money.setQuantity(data.getTip());
								player.equipOrPutOnGround(money);
								player.addXP(data.getXp());
								player.addKarma(5);
							}
							if (npcStatusEffect != null) {
								new InflictStatusOnNPCAction(npcStatusEffect).fire(player, null, npc);
							}
							player.setQuest(questSlot, 0, "done");
							new SetQuestToTimeStampAction(questSlot, 1).fire(player, null, npc);
							new IncrementQuestAction(questSlot, 2, 1).fire(player, null, npc);
							if (!tooLate) {
								// store number of on-time deliveries
								new IncrementQuestAction(questSlot, 3, 1).fire(player, null, npc);
							}
							deliverItemTask.putOffUniform(player);
						} else {
							// Item could be from a previous failed attempt to do this quest.
							npc.say(respondToItemWithoutQuest);
						}
						return;
					}
				}
				// The player has brought the item to the wrong NPC, or it's a plain item.
				npc.say(StringUtils.substitute(respondToItemForOtherNPC, params));

			} else {
				npc.say(respondToMissingItem);
			}
		}
	}


	public DeliverItemQuestCompleteBuilder respondToItemWithoutQuest(String respondToItemWithoutQuest) {
		this.respondToItemWithoutQuest = respondToItemWithoutQuest;
		return this;
	}

	public DeliverItemQuestCompleteBuilder respondToItemForOtherNPC(String respondToItemForOtherNPC) {
		this.respondToItemForOtherNPC = respondToItemForOtherNPC;
		return this;
	}

	public DeliverItemQuestCompleteBuilder respondToMissingItem(String respondToMissingItem) {
		this.respondToMissingItem = respondToMissingItem;
		return this;
	}

	public DeliverItemQuestCompleteBuilder npcStatusEffect(String npcStatusEffect) {
		this.npcStatusEffect = npcStatusEffect;
		return this;
	}

	@Override
	void simulate(String npc, QuestSimulator simulator) {
		if (deliverItemTask.getOrders().isEmpty()) {
			simulator.info("Brak skonfigurowanych dostaw do zakończenia.");
			simulator.info("");
			return;
		}

		Map.Entry<String, DeliverItemOrder> sample = deliverItemTask.getOrders().entrySet().iterator().next();
		String customerName = sample.getKey();
		DeliverItemOrder order = sample.getValue();

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("flavor", order.getFlavor());
		params.put("tip", Integer.valueOf(order.getTip()));

		simulator.playerSays("hi");
		simulator.playerSays(deliverItemTask.getItemName());
		simulator.npcSays(customerName, respondToMissingItem);
		simulator.info("");

		simulator.info("Gracz przynosi przedmiot bez aktywnego zadania.");
		simulator.playerSays("hi");
		simulator.playerSays(deliverItemTask.getItemName());
		simulator.npcSays(customerName, respondToItemWithoutQuest);
		simulator.info("");

		simulator.info("Gracz dostarcza " + order.getFlavor() + " na czas.");
		simulator.playerSays("hi");
		simulator.playerSays(deliverItemTask.getItemName());
		String fastResponse = StringUtils.substitute(order.getRespondToFastDelivery(), params);
		simulator.npcSays(customerName, fastResponse);
		simulator.info("Nagroda: " + order.getTip() + " money i " + order.getXp() + " XP wraz z premią karmy.");
		if (npcStatusEffect != null) {
			simulator.info("NPC otrzymuje efekt statusu: " + npcStatusEffect + ".");
		}
		simulator.info("");

		simulator.info("Gracz spóźnia się z dostawą.");
		simulator.playerSays("hi");
		simulator.playerSays(deliverItemTask.getItemName());
		String slowResponse = StringUtils.substitute(order.getRespondToSlowDelivery(), params);
		simulator.npcSays(customerName, slowResponse);
		simulator.info("Nagroda: połowa doświadczenia (" + (order.getXp() / 2) + " XP) bez napiwku.");
		simulator.info("Slot zadania zostaje ustawiony na "done" wraz ze znacznikiem czasu.");
		simulator.info("");
	}

	@Override
	void build(SpeakerNPC mainNpc, String questSlot, ChatCondition questCompletedCondition, ChatAction questCompleteAction) {
		ChatAction handOverItemAction = new HandOverItemAction(this.deliverItemTask, questSlot);
		for (final String name : deliverItemTask.getOrders().keySet()) {
			final SpeakerNPC npc = NPCList.get().get(name);
			if (npc == null) {
				logger.error("NPC " + name + " is used in the DeliveryItemQuest " + questSlot + " but they do not exist in game.", new Throwable());
				continue;
			}

			npc.add(ConversationStates.ATTENDING, deliverItemTask.getItemName(), null,
				ConversationStates.ATTENDING, null,
				handOverItemAction);
		}
	}
}
