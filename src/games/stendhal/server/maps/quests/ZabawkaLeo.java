/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ZabawkaLeo extends AbstractQuest {
	private static final String QUEST_SLOT = "zabawka_leo";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			if (player.isEquipped("pluszowy miś")) {
				res.add("Leo opowiedział o zgubionym misiu, którego mam ze sobą");
			}
			return res;
		}
		res.add("Spotkałem Leo na placu zabaw");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę szukać pluszowego misia Leo");
			return res;
		}
		res.add("Nie chcę pomóc Leo w poszukaniu jego misia");
		if (player.isEquipped("pluszowy miś") || isCompleted(player)) {
			res.add("Znalazłem pluszowego misia Leo");
		}
		if (isCompleted(player)) {
			res.add("Dałem Leo jego misia.");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Leo");

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotCompletedCondition(QUEST_SLOT),
					new NotCondition(new PlayerHasItemWithHimCondition("pluszowy miś"))),
			ConversationStates.QUEST_OFFERED,
			"*płacz* Bawiłem się z kolegą i zeszliśmy do #kanałów *płacz*, a tam były TAKIE ogromne szczury to od razu uciekliśmy i upuściłem mojego biednego #'misia'! Proszę przyniesiesz go dla mnie?",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.IDLE, "*pociągnięcie nosem* Dziękuję bardzo!",
			new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.QUEST_OFFERED,
			"*pociągnięcie nosem* Ale... ale... PROSZĘ! *płacz*", null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("kanałów", "kanały", "kanału", "kanał", "canal", "canals"),
			null,
			ConversationStates.QUEST_OFFERED,
			"*płacz* Do kanałów można zejść przez wieże, która znajduje się na rynku. *pociągnięcie nosem* Tam zawsze są drzwi otwarte.",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			Arrays.asList("pluszowy miś", "miś", "misia", "misiu", "toy", "pluszowy"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Miś jest moją ulubioną zabawką! Przyniesiesz mi ją?",
			null);
	}

	private void step_2() {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("-1_krakow_canals");
		final PassiveEntityRespawnPoint teddyRespawner = new PassiveEntityRespawnPoint("pluszowy miś", 1500);
		teddyRespawner.setPosition(63, 56);
		teddyRespawner.setDescription("Oto zmoknięty miś.");
		zone.add(teddyRespawner);

		teddyRespawner.setToFullGrowth();
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Leo");

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("pluszowy miś"));
		reward.add(new IncreaseXPAction(250));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(10));
		
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
							new OrCondition(
									new QuestNotStartedCondition(QUEST_SLOT),
									new QuestNotCompletedCondition(QUEST_SLOT)),
							new PlayerHasItemWithHimCondition("pluszowy miś")),
			ConversationStates.ATTENDING, 
			"Znalazłeś go! *przytula misia* Dziękuję, dziękuję! *uśmiech*",
			new MultipleActions(reward));

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("pluszowy miś","miś"),
			new AndCondition(new QuestNotCompletedCondition(QUEST_SLOT), new NotCondition(new PlayerHasItemWithHimCondition("pluszowy miś"))),
			ConversationStates.ATTENDING,
			"Zgubiłem misia w #'kanałach' pod rynkiem. Tam jest pełno OGROMNYCH szczurów!",
			null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Już znalazłeś mojego pluszowego misia! Wciąż go głaszczę i przytulam :)", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zabawka Leo",
				"Leo chce, abym znalazł jego misia.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "ZabawkaLeo";
	}

	@Override
	public String getNPCName() {
		return "Leo";
	}

	@Override
	public String getRegion() {
		return Region.KRAKOW_CITY;
	}
}