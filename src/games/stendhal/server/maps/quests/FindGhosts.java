/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * QUEST: Find Ghosts
 * 
 * PARTICIPANTS: 
 * <ul>
 * <li> Carena</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Carena asks you to find the 4 other spirits on Faiumoni</li>
 * <li> You go find them and remember their names</li>
 * <li> You return and say the names</li>
 * <li> Carena checks you have met them, then gives reward</li>
 * <li> Note: you can meet the ghosts before you started the quest with her</li>
 * </ul>
 * 
 * REWARD: 
 * <ul>
 * <li> base HP bonus of 100</li>
 * <li> 5000 XP</li>
 * <li> Karma: 15</li>
 * </ul>
 * 
 * REPETITIONS: 
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class FindGhosts extends AbstractQuest {

	private static Logger logger = Logger.getLogger(FindGhosts.class);

	public static final String QUEST_SLOT = "find_ghosts";
	
	private static final List<String> NEEDED_SPIRITS = 
		Arrays.asList("mary", "ben", "zak", "goran");

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private List<String> missingNames(final Player player) {
		if (!player.hasQuest(QUEST_SLOT)) {
			return NEEDED_SPIRITS;
		}
		/*
		 * the format of the list quest slot is
		 * "looking;name;name;...:said;name;name;..."
		 */
		final String npcDoneText = player.getQuest(QUEST_SLOT).toLowerCase();
		final String[] doneAndFound = npcDoneText.split(":");
		final List<String> result = new LinkedList<String>();
		if (doneAndFound.length > 1) {
		    final String[] done = doneAndFound[1].split(";");
		    final List<String> doneList = Arrays.asList(done);
		    for (final String name : NEEDED_SPIRITS) {
				if (!doneList.contains(name)) {
				    result.add(name);
				}
		    }
		}
		return result;
	}

	private void askingStep() {
		final SpeakerNPC npc = npcs.get("Carena");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new OrCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestInStateCondition(QUEST_SLOT, "rejected")),
			ConversationStates.QUEST_OFFERED,
			"Czuję się taka samotna. Spotykam tylko potwory i żywych ludzi. Jeżeli wiedziałabym o innych #duchach to poczułabym się lepiej.",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Potrzebuję pomocy w znalezieniu innych duchów takich jak ja. Proszę znajdź je i powiedz mi ich imiona.",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Dziękuję! Czuję się teraz lepiej znając imiona innych duchów na Faiumoni.",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, 
			null,
			ConversationStates.ATTENDING,
			"To wspaniale z twojej strony. Powodzenia w szukaniu ich.",
			new SetQuestAction(QUEST_SLOT, "looking:said"));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Och nieważne. Może dlatego, że jestem duchem to nie mogę zaoferować Ci lepszej nagrody.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -15.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("spirits", "spirit", "duch", "duchy", "duchach"),
			null,
			ConversationStates.QUEST_OFFERED,
			"Czuję, że są 4 inne duchy. Gdybym znała ich imiona to mogłabym się z nimi skontaktować. Znajdziesz je i powiesz mi ich imiona?",
			null);
	}

	private void findingStep() {
		// see the separate GhostNPC classes for what happens when a player
		// finds a ghost (with or without quest slot defined)
	}

	private void tellingStep() {

		final SpeakerNPC npc = npcs.get("Carena");

		// the player returns to Carena after having started the quest, or found
		// some ghosts.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
			ConversationStates.QUESTION_1,
			"Jeżeli znajdziesz #duchy to proszę wyjaw mi ich imiona.", null);

		for(final String spiritName : NEEDED_SPIRITS) {
			npc.add(ConversationStates.QUESTION_1, spiritName, null,
				ConversationStates.QUESTION_1, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String name = spiritName;

						// although all names are stored as lower case from now on, 
						// older versions did not,
						// so we have to be compatible with them
						final String npcQuestText = player.getQuest(QUEST_SLOT).toLowerCase();
						final String[] npcDoneText = npcQuestText.split(":");
		    			final String lookingStr;
		    			final String saidStr;
						if (npcDoneText.length > 1) {
							lookingStr = npcDoneText[0];
							saidStr = npcDoneText[1];
						} else {
							// compatibility with broken quests
							logger.warn("Player " + player.getTitle() + " found with find_ghosts quest slot in state " + player.getQuest(QUEST_SLOT) + " - now setting this to done.");
							player.setQuest(QUEST_SLOT, "done");
							npc.say("Wygląda na to, że po tym wszystkim już je znalazłeś. Czuję się zakłopotana.");
							player.notifyWorldAboutChanges();
							npc.setCurrentState(ConversationStates.ATTENDING);
							return;
						}

						final List<String> looking = Arrays.asList(lookingStr.split(";"));
						final List<String> said = Arrays.asList(saidStr.split(";"));
						String reply = "";
						List<String> missing = missingNames(player);
						final boolean isMissing = missing.contains(name);

						if (isMissing && looking.contains(name) && !said.contains(name)) {
							// we haven't said the name yet so we add it to the list
							player.setQuest(QUEST_SLOT, lookingStr
									+ ":" + saidStr + ";" + name);
							reply = "Dziękuje.";
						} else if (!looking.contains(name)) {
							// we have said it was a valid name but haven't met them
							reply = "Nie wierzę, że rozmawiałeś z duchem o tym imieniu.";
						} else if (!isMissing && said.contains(name)) {
							// we have said the name so we are stupid!
							reply = "Mówiłeś mi już o tym duchu.";
						} else {
							assert false;
						}

						// we may have changed the missing list
						missing = missingNames(player);

						if (!missing.isEmpty()) {
							reply += " Jeżeli spotkasz inne duchy to proszę powiedz mi ich imiona.";
							npc.say(reply);
						} else {
							player.setBaseHP(100 + player.getBaseHP());
							player.heal(100, true);
							player.addXP(5000);
							player.addKarma(15);
							reply += " Znam teraz 4 inne duchy. Może teraz będę mogła się skontaktować z nimi za pomocą telepatii. Nie mogłam dać Ci nic z materialnych rzeczy i dlatego zwiększyłam twoją żywotność. Będziesz mógł żyć dłużej.";
							npc.say(reply);
							player.setQuest(QUEST_SLOT, "done");
							player.notifyWorldAboutChanges();
							npc.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});
		}

		final List<String> triggers = new ArrayList<String>();
		triggers.add(ConversationPhrases.NO_EXPRESSION);
		triggers.addAll(ConversationPhrases.GOODBYE_MESSAGES);
		npc.add(ConversationStates.QUESTION_1, triggers, null,
				ConversationStates.IDLE, "Nie ma problemu. Wróć później.", null);

		// player says something which isn't in the needed spirits list.
		npc.add(
			ConversationStates.QUESTION_1,
			"",
			new NotCondition(new TriggerInListCondition(NEEDED_SPIRITS)),
			ConversationStates.QUESTION_1,
			"Przepraszam, ale nie rozumiem Ciebie. Jakie to było imię?",
			null);

		npc.add(
			ConversationStates.QUESTION_1,
			Arrays.asList("spirits", "spirit", "duch", "duchy", "duchach"),
			null,
			ConversationStates.QUESTION_1,
			"Szukam czegoś więcej o innych duchach, które są martwe i utknęły na ziemi właśnie jako duchy. Proszę wyjaw mi ich imiona.",
			null);

		// the player goes to Carena and says hi, and has no quest or is completed.
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new NotCondition(new QuestActiveCondition(QUEST_SLOT))),
				ConversationStates.ATTENDING, "Ałłłuuuuuuu!", 
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Poszukiwania Duchów",
				"Pewnego razu paru podróżnych rozmawiało z duchami, które odwiedziły ich podczas podróży przez Faiumoni. Jeden z nich młody duch zwany Carena ukryty gdzieś w okolicach Ados potrzebuje pomocy...",
				true);
		askingStep();
		findingStep();
		tellingStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			res.add("Carena jest samotna i chce wiedzieć o innych duchach na świecie. Muszę znaleźć je wszystkie i powiedzieć jej, ich imiona.");
			if ("rejected".equals(player.getQuest(QUEST_SLOT))) {
				res.add("Uh, nie dzięki, duchy są straszne.");
				return res;
			}
			if (!isCompleted(player)) {
				res.add("Znalazłem " + missingNames(player).size() + " " + Grammar.plnoun(missingNames(player).size(), "ghost") + " aby powiedzieć Carenie.");
			} else {
				res.add("Carena ucieszyła się, że są inne duchy na świecie. Za moje zaangażowanie podniosła moją żywotność.");
			}
			return res;
	}
	
	@Override
	public String getName() {
		return "FindGhosts";
	}
	
	@Override
	public int getMinLevel() {
		return 10;
	}

	@Override
	public String getNPCName() {
		return "Carena";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
