/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.ados.city.ManWithHatNPC;
import games.stendhal.server.util.KillsForQuestCounter;
import games.stendhal.server.util.ResetSpeakerNPC;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.Pair;

/**
 * QUEST: KillMonks
 *
 * PARTICIPANTS: <ul>
 * <li> Andy on Ados cemetery
 * <li> Darkmonks and normal monks
 * </ul>
 *
 * STEPS:<ul>
 * <li> Andy who is sad about the death of his wife, wants revenge for her death
 * <li> Kill 25 monks and 25 darkmonks for him for reaching his goal
 * </ul>
 *
 *
 * REWARD:<ul>
 * <li> 15000 XP
 * <li> 1-5 soup
 * <li> some karma
 * </ul>
 *
 * REPETITIONS: <ul><li>once in two weeks</ul>
 *
 * @author Vanessa Julius, idea by anoyyou
 */
public class KillMonks extends AbstractQuest {
	private static final String QUEST_SLOT = "kill_monks";
	private final SpeakerNPC npc = npcs.get("Andy");

	protected HashMap<String, Pair<Integer, Integer>> creaturestokill = new HashMap<String, Pair<Integer,Integer>>();
	public KillMonks() {
		super();
		creaturestokill.put("mnich", new Pair<Integer, Integer>(0, 25));
		creaturestokill.put("mnich ciemności", new Pair<Integer, Integer>(0, 25));
	}

	private void step_1() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Moja kochana żona została zamordowana, gdy szła do Wo'fol, aby zamówić pizzę u Kroipa. Jacyś mnichowie ją napadli i nie miała szansy. Teraz chcę się zemścić! Może mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new TimePassedCondition(QUEST_SLOT, 1, TimeUtil.MINUTES_IN_WEEK*2)),
				ConversationStates.QUEST_OFFERED,
				"Ci mnichowie są okrutni, a ja wciąż nie mogę dokonać mojej zemsty. Może znów mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, TimeUtil.MINUTES_IN_WEEK*2)), new QuestStateStartsWithCondition(QUEST_SLOT, "killed")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, TimeUtil.MINUTES_IN_WEEK*2, "Ci mnichowie dostali lekcje, ale możliwe, że znów będę potrzebował twojej pomocy za"));


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		actions.add(new IncreaseKarmaAction(5));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, creaturestokill));


		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję! Zabij 25 mnichów i 25 mnichów ciemności w imię mojej ukochanej żony.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Co za szkoda... Może kiedyś zmienisz zdanie i pomożesz smutnemu człowiekowi.",
				new MultipleActions(
						new SetQuestAction(QUEST_SLOT, 0, "rejected"),
						new DecreaseKarmaAction(5)));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {
		ChatAction addRandomNumberOfItemsAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				//add random number of soups
				final StackableItem soup = (StackableItem) SingletonRepository.getEntityManager()
						.getItem("zupa");
				int amount;
				// between 1 and 5 soup
				amount = Rand.rand(4) + 1;
				soup.setQuantity(amount);
				player.equipOrPutOnGround(soup);
			}
		};

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(addRandomNumberOfItemsAction);
		actions.add(new IncreaseXPAction(15000));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "killed"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT,2,1));

		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);
		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Bardzo dziękuję! Teraz mogę spać trochęspokojniej. Proszę, przyjmij tę zupę.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Proszę pomóż mi w dokonaniu zemsty!",
				null);
	}

	private String howManyWereKilled(final Player player, final String questState) {
		KillsForQuestCounter killsCounter = new KillsForQuestCounter(questState);
		int killedMonks = 25 - killsCounter.remainingKills(player, "mnich");
		int killedDarkMonks = 25 - killsCounter.remainingKills(player, "mnich ciemności");
		return "Wciąż muszę zabić " + Grammar.quantityplnoun(killedMonks, "mnich") + " i " + Grammar.quantityplnoun(killedDarkMonks, "mnich") + " ciemności.";
	}

	private List<String> howManyWereKilledFormatted(final Player player, final String questState) {
		KillsForQuestCounter killsCounter = new KillsForQuestCounter(questState);
		int killedMonks = 25 - killsCounter.remainingKills(player, "mnich");
		int killedDarkMonks = 25 - killsCounter.remainingKills(player, "mnich ciemności");

		List<String> entries = new ArrayList<>();
		entries.add("Mnisi: <tally>" + killedMonks + "</tally>");
		entries.add("Mnisi ciemności: <tally>" + killedDarkMonks + "</tally>");
		return entries;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zguba Mnichów",
				"Żona Andiego została zamordowana przez mnichów, a teraz on chce dokonać na nich zemsty.",
				false, 2);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public boolean removeFromWorld() {
		return ResetSpeakerNPC.reload(new ManWithHatNPC(), "Andy");
	}

	@Override
	public List<String> getHistory(final Player player) {
		return getHistory(player, false);
	}

	@Override
	public List<String> getFormattedHistory(final Player player) {
		return getHistory(player, true);
	}

	private List<String> getHistory(final Player player, boolean formatted) {
			final List<String> res = new ArrayList<>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			res.add(player.getGenderVerb("Spotkałem") + " Andiego w mieście Ados. Poprosił mnie o pomszczenie jego żony.");
			final String questStateFull = player.getQuest(QUEST_SLOT);
			final String[] parts = questStateFull.split(";");
			final String questState = parts[0];

			if ("rejected".equals(questState)) {
				res.add("Odrzuciłem prośbę.");
			}
			if ("start".equals(questState)) {
				res.add(player.getGenderVerb("Obiecałem") + " zabić 25 mnichów i 25 mnichów ciemności, aby dokonać zemsty za żone Andiego.");
				if (formatted) {
					res.addAll(howManyWereKilledFormatted(player, parts[1]));
				} else {
					res.add(howManyWereKilled(player, parts[1]));
				}
			}
			if (isCompleted(player)) {
				if(isRepeatable(player)){
					res.add("Po dwóch tygodniach powinienem może odwiedzić Andiego. Być może potrzebuje ponownie mojej pomocy!");
				} else {
					res.add(player.getGenderVerb("Zabiłem") + " paru mnichów, a Andi może teraz spać trochę spokojnie!");
				}
			}
			int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
			if (repetitions > 0) {
				res.add("Zemściłem się dla Andiego "
						+ Grammar.quantityplnoun(repetitions, "razy") + ".");
			}
			return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Zguba Mnichów";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public int getMinLevel() {
		return 27; // level of monk
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
				 new TimePassedCondition(QUEST_SLOT, 1, TimeUtil.MINUTES_IN_WEEK*2)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"killed").fire(player, null, null);
	}
}
