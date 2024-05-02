/***************************************************************************
 *                 (C) Copyright 2019-2021 - PolanieOnLine                 *
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

import games.stendhal.common.Direction;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EnableFeatureAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

public class MagicznaTorba extends AbstractQuest {
	public static final String QUEST_SLOT = "magic_bag";
	private final SpeakerNPC npc = npcs.get("Wizariusz");

	private void prepareRequestingStep() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"W jednej z moich portali znajduje się lodowa krypta, a w niej zalęgły się lodowe stwory, jeden z nich zabrał mój cenny pergamin, który zawiera w sobie lodowe zaklęcia. Zwróciłbyś go do mnie?",
			null);

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(QUEST_SLOT)),
			ConversationStates.IDLE, 
			"Już pomogłeś mi wystarczająco, nie zawracaj mi teraz... jak wy to mówicie? a... gitary!",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dobrze... przeteleportuję cię do lodowej krypty gdy powiesz mi #teleportuj. Podczas teleportacji mogą występować bóle głowy, także miej to na uwadze!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"No cóż... Żegnaj!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("teleportuj", "tp", "teleport"),
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String zoneName = "-1_ice_vault";
					final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
					player.teleport(zone, 24, 14, Direction.DOWN, null);
					player.notifyWorldAboutChanges();
				}
			});
	}

	private void prepareBringingStep() {
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("lodowy zwój")),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			"Wyczuwam mój magiczny pergamin. Możesz mi go zwrócić?",
			null);

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(new PlayerHasItemWithHimCondition("lodowy zwój"))),
			ConversationStates.ATTENDING, 
			"Ach tak... To Ty, którego miałem przeteleportować po mój pergamin...",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("lodowy zwój"));
		reward.add(new IncreaseXPAction(5000));
		reward.add(new IncreaseKarmaAction(5));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new EnableFeatureAction("magicbag"));

		final List<ChatAction> reward2 = new LinkedList<ChatAction>();
		reward2.add(new DropItemAction("lodowy zwój"));
		reward2.add(new IncreaseXPAction(-10000));
		reward2.add(new IncreaseKarmaAction(-20));
		reward2.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward2.add(new EnableFeatureAction("magicbag"));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("lodowy zwój"),
			ConversationStates.ATTENDING,
			"Dobra robota dzielny rycerzu! Takiej współpracy się właśnie spodziewałem, a oto Twoja nagroda.",
			new MultipleActions(reward));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			new PlayerHasItemWithHimCondition("lodowy zwój"),
			ConversationStates.ATTENDING,
			"Jesteś absolutnie pewien, że nie chcesz mi go oddać? Cóż... wezmę go siłą...",
			new MultipleActions(reward2));
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Magiczna Torba",
				"Wizariusz obiecał nagrodę, która może mnie zainteresować.",
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
		res.add(player.getGenderVerb("Rozmawiałem") + " z Wizariuszem.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie zamierzam być pieskiem na posyłki...");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add(player.getGenderVerb("Zgodziłem") + " się zwrócić magiczny pergamin.");
		}
		if ("start".equals(questState) && player.isEquipped("lodowy zwój")
				|| "done".equals(questState)) {
			res.add("Mam pergamin dla Wizariusza.");
		}
		if ("done".equals(questState)) {
			res.add(player.getGenderVerb("Zwróciłem") + " zwój czarownikowi.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Magiczna Torba";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
