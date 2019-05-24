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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: The Amazon Princess
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Princess Esclara, the Amazon Princess in a Hut on Amazon Island</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>The princess asks you for an exotic drink</li>
 * <li>Find someone who serves exotic drinks</li>
 * <li>Take exotic drink back to princess</li>
 * <li>Princess gives you a reward</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>Karma +25 in all</li>
 * <li>Some fish pie, random between 2 and 7.</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>You can repeat it once an hour.</li>
 * </ul>
 */
public class AmazonPrincess extends AbstractQuest {

	private static final String QUEST_SLOT = "amazon_princess";

	// The delay between repeating quests is 60 minutes
	private static final int REQUIRED_MINUTES = 60;
	private static final List<String> triggers = Arrays.asList("drink", "napój","napój z oliwką");


	private void offerQuestStep() {
		final SpeakerNPC npc = npcs.get("Princess Esclara");
npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Napiłabym się drinka, powinien być egzotyczny. Czy możesz mi go przynieść?",
				null);
npc.add(ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		new QuestCompletedCondition(QUEST_SLOT),
		ConversationStates.ATTENDING,
		"Nie jestem teraz spragniona dziękuję!",
		null);

npc.add(ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		new AndCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES), new QuestStateStartsWithCondition(QUEST_SLOT, "drinking;")),
		ConversationStates.QUEST_OFFERED,
		"Ostatni napój, który mi kupiłeś był wspaniały. Przyniesiesz mi następny?",
		null);

npc.add(ConversationStates.ATTENDING,
		ConversationPhrases.QUEST_MESSAGES,
		new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)), new QuestStateStartsWithCondition(QUEST_SLOT, "drinking;")),
		ConversationStates.ATTENDING,
		null,
		new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Jestem pełna, aby wypić następny napój przez co najmniej "));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Kocham te egzotyczne napoje ale zapomniałam nazwę mojego ulubionego.",
				null);

// Player agrees to get the drink
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Dziękuję! Jeżeli go znajdziesz to powiedz #napój a będę wiedziała, że go masz. W zamian dam Ci nagrodę.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Oh nie ważne. Dowidzenia.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	/**
	 * Get Drink Step :
	 * src/games/stendhal/server/maps/athor/cocktail_bar/BarmanNPC.java he
	 * serves drinks to all, not just those with the quest
	 */
	private void bringCocktailStep() {
		final SpeakerNPC npc = npcs.get("Princess Esclara");
		npc.add(
			ConversationStates.ATTENDING, triggers,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("napój z oliwką")),
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(
						new DropItemAction("napój z oliwką"),
						new ChatAction() {
							@Override
							public void fire(final Player player,
									final Sentence sentence,
									final EventRaiser npc) {
								int pieAmount = Rand.roll1D6() + 1;
								new EquipItemAction("tarta z rybnym nadzieniem", pieAmount, true).fire(player, sentence, npc);
								npc.say("Dziękuję!! Weź tą " +
										Grammar.thisthese(pieAmount) + " " +
										Grammar.quantityplnoun(pieAmount, "tarta z rybnym nadzieniem", "") +
										" z mojej kuchni i pocałunek ode mnie.");
								new SetQuestAndModifyKarmaAction(getSlotName(), "drinking;"
																 + System.currentTimeMillis(), 15.0).fire(player, sentence, npc);
							}
						},
						new InflictStatusOnNPCAction("napój z oliwką")
						));

		npc.add(
			ConversationStates.ATTENDING, triggers,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("napój z oliwką"))),
			ConversationStates.ATTENDING,
			"Nie masz napoju z oliwką. Idź i lepiej dostarcz mi go!",
			null);

		npc.add(
			ConversationStates.ATTENDING, triggers,
			new QuestNotInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Czasami mógłbyś mi wyświadczyć #przysługę ...", null);

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Amazon Princess",
				"Spragniona księżniczka chce pić.",
				true);
		offerQuestStep();
		bringCocktailStep();
	}


	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Princess Esclara powitała mnie w domu na Amazon Island.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Prosiła mnie aby dostarczył jej napój z oliwką, ale ja nie wiem czy znajdę czas na to.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start") || isCompleted(player)) {
			res.add("Księżniczka jest spragniona, obiecałem jej egzotyczny napój. Powinienem jej powiedzieć, #napój gdy go zdobędę.");
		}
		if ("start".equals(questState) && player.isEquipped("napój z oliwką") || isCompleted(player)) {
			res.add("Znalazłem napój z oliwką dla księżniczki.");
		}
        if (isCompleted(player)) {
            if (isRepeatable(player)) {
                res.add("Dostarczyłem napój dla księżniczki, ale założę się, że jest gotowa na następny. Może będę miał więcej tart z rybą.");
            } else {
                res.add("Princess Esclara uwielbia napój z oliwką, dostarczyłem go jej. Dostałem tartę z nadzieniem rybnym i pocałunek!!");
            }
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "AmazonPrincess";
	}

	// Amazon is dangerous below this level - don't hint to go there
	@Override
	public int getMinLevel() {
		return 70;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"drinking;"),
				 new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"drinking;").fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.AMAZON_ISLAND;
	}

	@Override
	public String getNPCName() {
		return "Princess Esclara";
	}
}
