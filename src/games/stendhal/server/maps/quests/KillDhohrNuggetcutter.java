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

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
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
import games.stendhal.server.util.TimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Pair;

/**
 * QUEST: Kill Dhohr Nuggetcutter
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Zogfang
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Zogfang asks you to kill remaining dwarves from area
 * <li> You go kill Dhohr Nuggetcutter and you get the reward from Zogfang
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> mithril nugget
 * <li> 4000 XP
 * <li>35 karma in total
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> after 14 days.
 * </ul>
 */

public class KillDhohrNuggetcutter extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_dhohr_nuggetcutter";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("Zogfang");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				null,
				ConversationStates.QUEST_OFFERED, 
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (!player.hasQuest(QUEST_SLOT) || player.getQuest(QUEST_SLOT).equals("rejected")) {
							raiser.say("Nie możemy uwolnić naszego obszaru od krasnali. Szczególnie od takiego zwanego Dhohr Nuggetcutter. Czy mógłbyś go zabić?");
						}  else if (player.getQuest(QUEST_SLOT, 0).equals("start")) {
							raiser.say("Już się Ciebie pytałem o zabicie Dhohr Nuggetcutter!");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}  else if (player.getQuest(QUEST_SLOT).startsWith("killed;")) {
							final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
							final long delay = 2 * MathHelper.MILLISECONDS_IN_ONE_WEEK;
							final long timeRemaining = Long.parseLong(tokens[1]) + delay - System.currentTimeMillis();
							if (timeRemaining > 0) {
								raiser.say("Dziękuję za pomoc. Może przyjdziesz później. Krasnale mogą wrócić. Wróć za " + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
								raiser.setCurrentState(ConversationStates.ATTENDING);
								return;
							}
							raiser.say("Czy mógłbyś znowu nam pomóc?");
						} else {
							raiser.say("Dziękuję za pomoc w potrzebie. Teraz czujemy się bezpieczniej.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});

		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
		toKill.put("Dhohr Nuggetcutter", new Pair<Integer, Integer>(0,1));
		toKill.put("górski krasnal", new Pair<Integer, Integer>(0,2));
		toKill.put("górski starszy krasnal", new Pair<Integer, Integer>(0,2)); 
		toKill.put("górski krasnal bohater", 	new Pair<Integer, Integer>(0,2));
		toKill.put("górski krasnal lider", new Pair<Integer, Integer>(0,2));
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseKarmaAction(5.0));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		actions.add(new IncreaseKarmaAction(10));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));
		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Wspaniale! Proszę znajdź je gdzieś na tym poziomie i niech zapłacą za przekroczenie granicy!",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dobrze poczekam na kogoś odpowiedniego do tego zadania.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void step_2() {
		/* Player has to kill the dwarves*/
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Zogfang");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Idź zabić Dhohr Nuggetcuttera i jego sługusów; górskiego krasnala lidera, bohatera i starszego krasnala oraz tego pospolitego krasnala.");								
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
						raiser.say("Dziękuję bardzo. Jesteś prawdziwym wojownikiem! Weź jedno. Znaleźliśmy je rozrzucone wokoło. Nie wiemy czym są.");
							final Item mithrilnug = SingletonRepository.getEntityManager()
									.getItem("bryłka mithrilu");
							player.equipOrPutOnGround(mithrilnug);
							player.addKarma(25.0);
							player.addXP(4000);
							player.setQuest(QUEST_SLOT, "killed;" + System.currentTimeMillis());
		 			}
				});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zabij Dhohr Nuggetcutter",
				"Ork Zogfang, który pilnuje wejścia do Abandonded Keep i chce abyś zabił pozostałe w tym obszarze krasnale.",
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
				res.add("Muszę zabić Dhohr Nuggetcutter i jego kumpli naprośbę Orka Zogfanga.");
			} else if(isRepeatable(player)){
				res.add("Ork Zogfang potrzebuje jeszcze raz pomocy i nagrodzi mnie, czy mam mu pomóc?");
			} else {
				res.add("Moja wyprawa na krasnoludy uspokoiła na jakiś czas nerwy Orka Zogfanga.");
			}
			return res;
	}

	@Override
	public String getName() {
		return "KillDhohrNuggetcutter";
	}
	
	// The kill requirements and surviving in the zone requires at least this level
	@Override
	public int getMinLevel() {
		return 70;
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
		return "Zogfang";
	}
}
