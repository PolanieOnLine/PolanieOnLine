/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.NPCEmoteAction;
import games.stendhal.server.entity.npc.action.PlaySoundAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerIsWearingOutfitCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.SystemPropertyCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.fado.city.SmallBoyNPC;
import games.stendhal.server.util.ResetSpeakerNPC;

/**
 * QUEST: Balloon for Bobby
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Bobby (the boy in fado city)</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Mine town weeks must be on for the quest to work</li>
 * <li>If you have a balloon, Bobby asks you if he can have it</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>200 XP</li>
 * <li>50 Karma</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Infinite, but only valid during mine town weeks </li>
 * </ul>
 */

public class BalloonForBobby extends AbstractQuest {
	public static final String QUEST_SLOT = "balloon_bobby";
	private final SpeakerNPC npc = npcs.get("Bobby");

	private static final Outfit balloonOutfit = new Outfit("detail=1");

	final OrCondition wearsColorlessBalloon = new OrCondition(
			new PlayerIsWearingOutfitCondition(new Outfit("detail=2")),
			new PlayerIsWearingOutfitCondition(new Outfit("detail=3")));

	private void prepareRequestQuestStep() {
		// Player asks Bobby for "quest".
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Czy mógłbyś zdobyć dla mnie #'balonik'? Nim zaczną się dni miasta, "
						+ "bo wtedy sam będę mógł zdobyć :)",
				null);

		// Player asks for quest after quest is started.
		npc.add(ConversationStates.ANY,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Mam nadzieję, że wkrótce zdobędziesz dla mnie #'balonik'. Chyba, że już jest święto zwane Mine Town Weeks, "
						+ "bo wtedy sam będę mógł zdobyć :)",
				null);

		// Player agrees to get a balloon.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Jej!",
				new SetQuestAction(QUEST_SLOT, 0, "start"));

		// Player refuses to get a balloon.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Ouu. :'(",
				new SetQuestAction(QUEST_SLOT, 0, "rejected"));

		// Player asks about "balloon".
		npc.add(ConversationStates.ANY,
				Arrays.asList("balloon", "balonik"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Pewnego dnia będę miał wystarczająco dużo baloników, aby odlecieć!",
				null);
	}

	// If the player has a balloon (and it is mine town weeks),
	// ask if Bobby can have it
	private void prepareGreetWithBalloonStep() {
		// Add conditions for balloon
		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new NotCondition(
								new SystemPropertyCondition("stendhal.minetown")),
						new PlayerIsWearingOutfitCondition(balloonOutfit)),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Cześć! Czy ten balonik jest dla mnie?",
				null);
	}

	// If the player has a balloon but refused to give it to booby
	// after him greeting, he now has another chance.
	// (Unless it's not mine town week)
	private void prepareAttendingWithBalloonStep() {
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("balloon", "balonik"),
				new AndCondition(
						new QuestStartedCondition(QUEST_SLOT),
						new NotCondition(
								new SystemPropertyCondition("stendhal.minetown")),
						new PlayerIsWearingOutfitCondition(balloonOutfit)),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Czy ten balonik jest dla mnie?",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("balloon", "balonik"),
				new AndCondition(
						new QuestStartedCondition(QUEST_SLOT),
						new NotCondition(
								new SystemPropertyCondition("stendhal.minetown")),
						new NotCondition(
								new PlayerIsWearingOutfitCondition(balloonOutfit)),
						new NotCondition(wearsColorlessBalloon)),
				ConversationStates.ATTENDING,
				"Nie masz dla mnie balonika :(",
				null);

		// make sure player knows they need to get a colorful balloon
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("balloon", "balonik"),
				new AndCondition(
						new QuestStartedCondition(QUEST_SLOT),
						wearsColorlessBalloon),
				ConversationStates.ATTENDING,
				"Um, chciałbym balonik nieco bardziej kolorowy. :(",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("balloon", "balonik"),
				new AndCondition(
						new SystemPropertyCondition("stendhal.minetown"),
						new NotCondition(wearsColorlessBalloon)),
				ConversationStates.ATTENDING,
				"Chmury podpowiedziały mi, że dni mine town wciąż trwają - Mogę sam zdobyć balonik. Wróć, gdy skończy się święto :)",
				null);
	}

	// Let player decide if he wants to give the balloon to bobby
	private void prepareQuestItemQuestionStep() {
		// The player has a balloon but wants to keep it to himself
		npc.add(
				ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
						new PlaySoundAction("pout-1"),
						new NPCEmoteAction("dąsa się.", false))
				);

		// Rewards to give to the player if he gives Bobby the balloon
		// NOTE: Also changes the players outfit to get rid of the balloon
		List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				player.setPerpetualOutfitLayer("detail", 0);
				// remove detail layer coloring
				player.unsetOutfitColor("detail");
			}
		});
		reward.add(new IncreaseXPAction(200));
		reward.add(new IncreaseKarmaAction(50));
		reward.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		reward.add(new IncrementQuestAction(QUEST_SLOT, 1, 1));

		// The player has a balloon and gives it to Bobby
		npc.add(
				ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Hurra! Leć baloniku! Leć!",
				new MultipleActions(reward));
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Balonik Bobbiego",
				"Młody chłopiec Bobby w Fado wpatruje się w niebo, szukając balonów. On je kocha i chce mieć jednego dla siebie.",
				true);
		prepareRequestQuestStep();
		prepareGreetWithBalloonStep();
		prepareAttendingWithBalloonStep();
		prepareQuestItemQuestionStep();
	}

	@Override
	public boolean removeFromWorld() {
		return ResetSpeakerNPC.reload(new SmallBoyNPC(), "Bobby");
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			final List<String> questInfo = Arrays.asList(player.getQuest(QUEST_SLOT).split(";"));
			final String questState = questInfo.get(0);
			int completedCount = 0;

			if (questInfo.size() > 1) {
				completedCount = Integer.parseInt(questInfo.get(1));
			}
 			if (questState.equals("rejected")) {
				res.add("Nienawidzę balonów.");
			} else if (questState.equals("start")) {
				res.add("Kocham baloniki! Pomogę chłopcowi o imieniu " + npc.getName() + ", aby zdobył chociaż jeden.");
			} else if (questState.equals("done")) {
				String balloon = "balonik";
				if (completedCount > 1 || completedCount > 20 && completedCount < 25) {
					balloon = balloon + "i";
				} else {
					balloon = balloon + "ów";
				}
 				res.add(player.getGenderVerb("Znalazłem") + " i dałem ładne " + Integer.toString(completedCount) + " " + balloon + " dla " + npc.getName() + ".");
			}
		}

		return res;
	}

	@Override
	public String getName() {
		return "Balonik Bobbiego";
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return true;
	}
}
