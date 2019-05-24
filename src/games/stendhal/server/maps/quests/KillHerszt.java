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
// Based on KillDhohrNuggetcutter.
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.Pair;

/**
 * QUEST: Kill Herszt
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Gazda Jędrzej
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Gazda Jędrzej asks you to kill remainging zbójniks and banits from area
 * <li> You go kill Herszt and you get the reward from Gazda Jędrzej
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> mithril nugget
 * <li> 5000 XP
 * <li> Karma: 20
 * <li> Once base HP bonus of 20
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> not for players with white skull.
 * <li> from 30 level.
 * <li> after 14 days.
 * </ul>
 */

public class KillHerszt extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_herszt";
	private static final String GAZDA_JEDRZEJ_BASEHP_QUEST_SLOT = "gazda_jedrzej_basehp";
	private static final String GAZDA_JEDRZEJ_NAGRODA_QUEST_SLOT = "gazda_jedrzej_nagroda";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Gazda Jędrzej");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				null,
				ConversationStates.QUEST_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (!player.hasQuest(QUEST_SLOT) || player.getQuest(QUEST_SLOT).equals("rejected")) {
							raiser.say("Nie możemy uwolnić się od zbójników grasujących na tym terenie, a w szczególności od Herszta górskich zbójników. Czy mógłbyś udać się do pobliskiej jaskini i pozbyć się ich?");
						} else if (player.getQuest(QUEST_SLOT, 0).equals("start")) {
							raiser.say("Już się Ciebie pytałem o zabicie zbójników!");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						} else if (player.getQuest(QUEST_SLOT).startsWith("killed;")) {
							final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
							final long delay = 2 * MathHelper.MILLISECONDS_IN_ONE_WEEK;
							final long timeRemaining = (Long.parseLong(tokens[1]) + delay) - System.currentTimeMillis();
							if (timeRemaining > 0) {
								raiser.say("Bardzo dziękuję za pomoc. Może przyjdziesz później. Zbójnicy mogą powrócić. Wróć za " + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
								raiser.setCurrentState(ConversationStates.ATTENDING);
								return;
							}
							raiser.say("Znowu potrzebujemy Twojej pomocy. Czy mógłbyś znowu nam pomóc?");
						} else {
							raiser.say("Dziękuję za pomoc w potrzebie. Teraz czujemy się bezpieczniej i pewniej.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});

		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
		toKill.put("zbójnik górski herszt", new Pair<Integer, Integer>(0,1));
		toKill.put("zbójnik górski", new Pair<Integer, Integer>(0,4));
		toKill.put("zbójnik górski goniec", new Pair<Integer, Integer>(0,4));
		toKill.put("zbójnik górski złośliwy", new Pair<Integer, Integer>(0,3));
		toKill.put("zbójnik górski zwiadowca", new Pair<Integer, Integer>(0,3));
		toKill.put("zbójnik górski starszy", new Pair<Integer, Integer>(0,1));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseKarmaAction(5.0));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Wspaniale! Proszę znajdź ich. Kręcą się gdzieś tutaj. Na pewną są w jaskini. Niech zapłacą za swoje winy!",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Rozumiem. Każdy się ich boi. Poczekam na kogoś odpowiedniego do tego zadania.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void step_2() {
		/* Player has to kill the zbojnik*/
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Gazda Jędrzej");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Idź zabić herszta górskich zbójników i jego kolegów.");
				}
		});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Wieść o twych czynach dotarła tu przed tobą! Godzien jesteś czci rycerskiej, lecz pamiętaj, że droga nie będzie łatwa, bo na każdym kroku musisz udowodnić swoje męstwo i odwagę!");
							player.setQuest(GAZDA_JEDRZEJ_NAGRODA_QUEST_SLOT, "start");
							player.drop("pióro herszta hordy zbójeckiej");
							player.addKarma(20.0);
							player.addXP(5000);
							final List<ChatAction> actions = new LinkedList<ChatAction>();
							if (!player.hasQuest(GAZDA_JEDRZEJ_BASEHP_QUEST_SLOT) || player.getQuest(GAZDA_JEDRZEJ_BASEHP_QUEST_SLOT).equals("rejected") || !player.getQuest(GAZDA_JEDRZEJ_BASEHP_QUEST_SLOT).equals("done")) {
								actions.add(new SetQuestAction(GAZDA_JEDRZEJ_BASEHP_QUEST_SLOT, "start"));
							}
							if (!"done".equals(player.getQuest(GAZDA_JEDRZEJ_BASEHP_QUEST_SLOT))) {
								player.setBaseHP(20 + player.getBaseHP());
								player.heal(20, true);
								player.setQuest(GAZDA_JEDRZEJ_BASEHP_QUEST_SLOT, "done");
							}
							player.setQuest(QUEST_SLOT, "killed;" + System.currentTimeMillis());
							player.setQuest(GAZDA_JEDRZEJ_NAGRODA_QUEST_SLOT, "done");
		 			}
				});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zabij Herszta",
				"Gazda Jędrzej chce abym zajął się rozbójnikami, którzy swoją siedzibę mają w jaskini nie daleko miasta.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			if (!isCompleted(player)) {
				res.add("Muszę zabić herszta i wszystkich jego kumpli na prośbę Gazdy Jęrzeja.");
			} else if(isRepeatable(player)){
				res.add("Gazda Jędrzej potrzebuje jeszcze raz pomocy i nagrodzi mnie, czy mam mu pomóc?");
			} else {
				res.add("Moja wyprawa na zbójników uspokoiła na jakiś czas nerwy Gazdy Jędrzeja.");
			}
			return res;
	}

	@Override
	public String getName() {
		return "KillHerszt";
	}

	// The kill requirements and surviving in the zone requires at least this level
	@Override
	public int getMinLevel() {
		return 30;
		}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
				new TimePassedCondition(QUEST_SLOT, 1, 2*MathHelper.MINUTES_IN_ONE_WEEK)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"killed").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Gazda Jędrzej";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
