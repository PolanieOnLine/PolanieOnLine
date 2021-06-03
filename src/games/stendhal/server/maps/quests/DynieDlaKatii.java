/***************************************************************************
 *                   (C) Copyright 2019-2021 - Stendhal                    *
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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
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
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class DynieDlaKatii extends AbstractQuest {
	private static final String QUEST_SLOT = "dynie_dla_katii";

	// NPC
	private static final String NPC_NAME = "Katia";
	private final SpeakerNPC npc = npcs.get(NPC_NAME);

	private static final int WYMAGANE_DYNIE = 10;
	private static final int REQUIRED_MINUTES = 240;

	private void prepareRequestingStep() {
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("straszna dynia", WYMAGANE_DYNIE)),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Witaj ponownie! Widzę, że masz kilka strasznych dyń. " +
			"Czy chcesz mi je przekazać?",
			null);

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(
							new PlayerHasItemWithHimCondition("straszna dynia", WYMAGANE_DYNIE))),
			ConversationStates.ATTENDING,
			"Już wróciłeś? " +
			"Obiecałeś, że zbierzesz dla mnie trochę strasznych dyń... " + 
			"Nie wydaje się, abyś miał przy sobie wystarczająco dużo dyń!",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Masz przynieść dla mnie 10 strasznych dyń! Zapamiętaj jeszcze raz, #'straszne dynie'!",
			null);

		// first time player asks/ player had rejected
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Co byś powiedział wojowniku na zebranie dla mnie 10 #'strasznych dyń'?",
				null);

		// player returns - enough time has passed
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestNotInStateCondition(QUEST_SLOT, "start"),
						new QuestStartedCondition(QUEST_SLOT),
						new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Chcesz ponownie pozbierać dla mnie #'straszne dynie'?",
				null);

		// player returns - enough time has passed
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "start"),
				new QuestStartedCondition(QUEST_SLOT),
				new NotCondition(new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,REQUIRED_MINUTES,
						"Dziękuję! " +
						"Na chwilę obecną mam ich za dużo oraz zabrakło mi srebrnych skrzynek! " +
						"Przyjdź do mnie później."));

		// player is willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"To dobrze! Ostatnio podczas halloween, straszne dynie można zdobyć od szczurów, starców, wieśniaków, gajowych, zbójników leśnych, w tym banici oraz zbójników górskich! Powodzenia w zbieraniu!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Cóż... Może innym razem.",
			new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void prepareBringingStep() {
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("straszna dynia", WYMAGANE_DYNIE));
		reward.add(new IncreaseXPAction(2000));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT));
		reward.add(new IncreaseKarmaAction(10));
		reward.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				npc.say("Dziękuję Ci! Oto nagroda ode mnie, srebrna skrzynia! Nie wiadomo co się w niej tam skrywa!");
				final Item reward = SingletonRepository.getEntityManager().getItem("srebrna skrzynia");
				reward.setBoundTo(player.getName());
				player.equipOrPutOnGround(reward);
				player.notifyWorldAboutChanges();
			}
		});

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("straszna dynia", WYMAGANE_DYNIE),
			ConversationStates.ATTENDING, null,
			new MultipleActions(reward));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemWithHimCondition("straszna dynia", WYMAGANE_DYNIE)),
			ConversationStates.ATTENDING,
			"Hej! Nie oszukasz mnie! Kieszenie masz puste, gdzie mógłbyś zostawić te wielkie dynie?",
			null);

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Och ... cóż, mam nadzieję, że znajdziesz kiedyś.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Straszne Dynie",
				"Katia przebrała się za czarownicę oraz wszystkim innym przebierańcom daje tajemniczą srebrną skrzynkę za przyniesienie strasznych dyń do niej!",
				true);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Poznałem Katie.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę zbierać dla niej dyń...");
			return res;
		}
		res.add("Chcę pozbierać kilka dyń!");
		if (player.isEquipped("straszna dynia", WYMAGANE_DYNIE) || isCompleted(player)) {
			res.add("Zdobyłem straszne dynie dla Katii");
		}
		if (isCompleted(player)) {
			res.add("Zaniosłem Katii dynie." +
		             "W zamian otrzymałem tajemniczą srebrną skrzynkę.");
		}
		if(isRepeatable(player)){
			res.add("Ponownie mogę poszukać kilku dyń dla Katii!");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Straszne Dynie";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getNPCName() {
		return NPC_NAME;
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
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
				new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES)).fire(player, null, null);
	}
}
