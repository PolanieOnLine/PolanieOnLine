/* $Id$ */
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

/**
 * QUEST: Plink's Toy
 * <p>
 * PARTICIPANTS: <ul><li> Plink <li> some wolves </ul>
 * 
 * STEPS: <ul><li> Plink tells you that he got scared by some wolves and ran away
 * dropping his teddy. <li> Find the teddy in the Park Of Wolves <li> Bring it back to
 * Plink </ul>
 * 
 * REWARD: <ul><li> a smile <li> 200 XP <li> 10 Karma </ul>
 * 
 * REPETITIONS: <ul><li> None. </ul>
 */
public class PlinksToy extends AbstractQuest {

	private static final String QUEST_SLOT = "plinks_toy";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			if (player.isEquipped("pluszowy miś")) {
				res.add("Plink opowiedział o misiu, którego mam ze sobą");
			}
			return res;
		}
		res.add("Spotkałem Plinka");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę szukać pluszowego misia Plinka");
			return res;
		}
		res.add("Nie chcę pomóc Plink w szukaniu jego misia");
		if (player.isEquipped("pluszowy miś") || isCompleted(player)) {
			res.add("Znalazłem pluszowego misia Plinka");
		}
		if (isCompleted(player)) {
			res.add("Dałem Plinkowi jego misia.");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Plink");

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotCompletedCondition(QUEST_SLOT),
					new NotCondition(new PlayerHasItemWithHimCondition("pluszowy miś"))),
			ConversationStates.QUEST_OFFERED,
			"*płacz* Wilki są w #parku! *płacz* Uciekłem, ale upuściłem mojego #misia! Proszę przyniesiesz go dla mnie? *pociągniecie nosem* Proszę?",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.IDLE, "*pociągnięcie nosem* Dziękuję bardzo! *uśmiech*",
			new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.QUEST_OFFERED,
			"*pociągnięcie nosem* Ale... ale... PROSZĘ! *płacz*", null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("wolf", "wolves", "wilków"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Przyszły od równiny, a teraz chodzą po #parku, który jest na wschód stąd. Nie powinienem się do nich zbliżać, one są niebezpieczne.",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("park", "parku"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Moi rodzice mówili mi żebym sam nie chodził do parku, ale się zgubiłem podczas zabawy... Proszę nie mów moim rodzicom! Czy możesz mi przynieść misia #teddy z powrotem?",
			null);

		npc.add(ConversationStates.QUEST_OFFERED, "pluszowy miś", null,
			ConversationStates.QUEST_OFFERED,
			"Miś jest moją ulubioną zabawką! Przyniesiesz mi ją?",
			null);
	}

	private void step_2() {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("0_semos_plains_n");
		final PassiveEntityRespawnPoint teddyRespawner = new PassiveEntityRespawnPoint("pluszowy miś", 1500);
		teddyRespawner.setPosition(107, 84);
		teddyRespawner.setDescription("Oto miś leżący w piasku.");
		zone.add(teddyRespawner);

		teddyRespawner.setToFullGrowth();
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Plink");

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("pluszowy miś"));
		reward.add(new IncreaseXPAction(200));
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
			"Zgubiłem misia w #parku na wschód stąd, gdzie jest pełno #wilków.",
			null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Już znalazłeś mojego pluszowego misia nie daleko wilków! Wciąż go głaszczę i przytulam :)", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zabawka Plinka",
				"Plink chce, abym znalazł jego misia.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "PlinksToy";
	}

	@Override
	public String getNPCName() {
		return "Plink";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
}
