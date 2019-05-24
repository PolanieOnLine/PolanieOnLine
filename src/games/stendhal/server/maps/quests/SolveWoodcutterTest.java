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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Sentence;
import games.stendhal.common.parser.SimilarExprMatcher;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * QUEST: Quest to solve a test on woodcutter
 * <p>
 * 
 * PARTICIPANTS: <ul><li>Drwal</ul>
 * 
 * 
 * STEPS: <ul><li> Drwal sets you a test 
 * <li> Player tries to answer
 * <li> Drwal compares answers to configuration file on server 
 * </ul>
 * 
 * 
 * REWARD: <ul><li>2000 XP <li> siekierka </ul>
 * 
 * REPETITIONS: <ul><li>Test you can repeat until you pass him. When you leave during test you lost some karma.
 *   If you say something to break test Drwal takes you less karma then if you leave test without word.</ul>
 * 
 * @author edi18028 based on kymara's quest
 */

public class SolveWoodcutterTest extends AbstractQuest {
	private static final String QUEST_SLOT = "woodcutter_license";
	private static final String TEMP_QUEST_SLOT = "question_woodcutter_license";
	private static final String TEST_QUEST_SLOT = "test_woodcutter_license";
	private static final String KARMA_QUEST_SLOT = "karma_woodcutter_license";
	private Questions questions;

	private static class Questions {
		private static Logger logger = Logger.getLogger(Questions.class);

		private static final String QUESTIONS_XML = "/data/conf/woodcutter_test.xml";
		private static final String QUESTIONS_EXAMPLE_XML = "/data/conf/woodcutter_test-example.xml";

		Map<String, Collection<String>> questionMap;

		public Questions() {
			questionMap = new HashMap<String, Collection<String>>();
			new QuestionLoader().load(questionMap);
		}

		/**
		 * Check if an answer mathces the riddle.
		 * 
		 * @param riddle The riddle to be answered
		 * @param answer The answer given by the player
		 * @return <code>true</code> iff the answer is correct
		 */
		public boolean matches(String question, Sentence sentence) {
			final Sentence answer = sentence.parseAsMatchingSource();

			for (String correct : questionMap.get(question)) {
				final Sentence expected = ConversationParser.parse(correct, new SimilarExprMatcher());
				if (answer.matchesFull(expected)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Get a random riddle.
		 * 
		 * @return A random ridde
		 */
		String getQuestion() {
			return Rand.rand(questionMap.keySet());
		}

		/**
		 * Loader for the riddles xml format.
		 */
		private static class QuestionLoader extends DefaultHandler {
			Map<String, Collection<String>> questions;
			String currentKey;
			String currentAnswer;

			public void load(Map<String, Collection<String>> questions) {
				this.questions = questions;

				InputStream in = getClass().getResourceAsStream(QUESTIONS_XML);

				if (in == null) {
					logger.warn(QUESTIONS_XML + " not found. Using " + QUESTIONS_EXAMPLE_XML);
					in = getClass().getResourceAsStream(QUESTIONS_EXAMPLE_XML);
					if (in == null) {
						logger.error("Failed to load " + QUESTIONS_EXAMPLE_XML);
						return;
					}
				}

				SAXParser parser;

				// Use the default (non-validating) parser
				final SAXParserFactory factory = SAXParserFactory.newInstance();
				try {
					parser = factory.newSAXParser();
					parser.parse(in, this);
				} catch (final Exception e) {
					logger.error(e);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}

			/**
			 * Add an answer to a riddle. Add the riddle too if it did not exist before.
			 * @param riddle The riddle to add an answer to
			 * @param answer Asnwer to the riddle
			 */
			private void addAnswer(String question, String answer) {
				Collection<String> answers = questions.get(question);
				if (answers == null) {
					answers = new LinkedList<String>();
					questions.put(question, answers);
				}
				answers.add(answer);
			}

			@Override
			public void startElement(final String uri, final String localName, final String qName, final Attributes attrs) {
				if (qName.equals("entry")) {
					final String key = attrs.getValue("key");
					if (key == null) {
						logger.warn("An entry without a key");
					} else {
						currentKey = key;
					}
				} else if (!(qName.equals("questions") || qName.equals("comment"))) {
					currentKey = null;
					logger.warn("Unknown XML element: " + qName);
				}
			}

			@Override
			public void endElement(final String uri, final String lName, final String qName) {
				if (qName.equals("entry")) {
					if ((currentKey != null) && (currentAnswer != null)) {
						addAnswer(currentKey, currentAnswer);
					} else {
						logger.error("Error reading questions, Key=" + currentKey + " " + " Answer=" + currentAnswer);
					}
				} else {
					currentKey = null;
					currentAnswer = null;
				}
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				if (currentKey != null) {
					currentAnswer = new String(ch, start, length);
				} else {
					currentAnswer = null;
				}
			}
		}
	}

	public SolveWoodcutterTest() {
		questions = new Questions();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void setQuestion() {
		final SpeakerNPC woodcutter = npcs.get("Drwal");

		// player has no unsolved riddle active
		woodcutter.add(ConversationStates.ATTENDING,
				Arrays.asList("test", "egzamin"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						// randomly choose from available riddles
						player.setQuest(QUEST_SLOT, "start");
						final String question = questions.getQuestion();
						npc.say("Oto pytanie 1: " + question);
						player.setQuest(TEMP_QUEST_SLOT, question);
					}
				});

		// player already was set a riddle he couldn't solve
		woodcutter.add(ConversationStates.ATTENDING,
				Arrays.asList("test", "egzamin"),
				new QuestStartedCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String karmaState = player.getQuest(KARMA_QUEST_SLOT);
					player.removeQuest(TEST_QUEST_SLOT);
					if (karmaState.equals("test1")) {
						player.addKarma(-40.0);
					} else if (karmaState.equals("test2")) {
						player.addKarma(-20.0);
					} else if (karmaState.equals("test3")) {
					} else {
						player.addKarma(-60.0);
					}
					player.removeQuest(TEMP_QUEST_SLOT);
					final String question = questions.getQuestion();
					npc.say("Oto pytanie 1: " + question);
					player.setQuest(TEMP_QUEST_SLOT, question);
					player.setQuest(KARMA_QUEST_SLOT, "test1");
				}
		});

		woodcutter.add(ConversationStates.QUESTION_1, "", null,
			ConversationStates.QUESTION_1, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String question = player.getQuest(TEMP_QUEST_SLOT);
					final String triggerText = sentence.getTriggerExpression().getNormalized();

					if (questions.matches(question, sentence)) {
						player.setQuest(KARMA_QUEST_SLOT, "test1");
						player.setQuest(TEST_QUEST_SLOT, "1");
						npc.say("Teraz kolejne pytanie. Jesteś gotowy?");
					} else if (triggerText.equals("bye") || triggerText.equals("dowidzenia")
							|| triggerText.equals("no") || triggerText.equals("nie")) {
						player.setQuest(KARMA_QUEST_SLOT, "test3");
						player.setQuest(QUEST_SLOT, "rejected");
						player.addKarma(-10.0);
						npc.say("Łatwo się poddałeś. Dowidzenia.");
						npc.setCurrentState(ConversationStates.IDLE);
					} else {
						player.setQuest(KARMA_QUEST_SLOT, "test1");
						player.setQuest(TEST_QUEST_SLOT, "0");
						npc.say("Teraz kolejne pytanie. Jesteś gotowy?");
					}
					player.removeQuest(TEMP_QUEST_SLOT);
					// clear quest slot so question is chosen randomly for player next time
				}
			});

		woodcutter.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.removeQuest(TEMP_QUEST_SLOT);
					final String question = questions.getQuestion();
					npc.say("Oto pytanie 2: " + question);
					player.setQuest(TEMP_QUEST_SLOT, question);
					player.setQuest(KARMA_QUEST_SLOT, "test2");
				}
			});

		woodcutter.add(ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Szkoda. Powodzenia następnym razem.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		woodcutter.add(ConversationStates.QUESTION_1,
			ConversationPhrases.GOODBYE_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Szkoda. Powodzenia następnym razem. Dowidzenia.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		woodcutter.add(ConversationStates.QUESTION_2,
			"",
			null,
			ConversationStates.QUESTION_2,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String question = player.getQuest(TEMP_QUEST_SLOT);
					final String doneText = player.getQuest(TEST_QUEST_SLOT);
					final String triggerText = sentence.getTriggerExpression().getNormalized();

					if (questions.matches(question, sentence)) {
						// clear quest slot so question is chosen randomly for player next time
						player.setQuest(KARMA_QUEST_SLOT, "test2");
						player.setQuest(TEST_QUEST_SLOT, doneText + ";1");
						npc.say("Ostatnie już pytanie. Jesteś gotowy?");
					} else if (triggerText.equals("bye") || triggerText.equals("dowidzenia")
							|| triggerText.equals("no") || triggerText.equals("nie")) {
						player.setQuest(KARMA_QUEST_SLOT, "test3");
						player.setQuest(QUEST_SLOT, "rejected");
						player.addKarma(-10.0);
						npc.say("Poddałeś się w połowie drogi. Dowidzenia.");
						npc.setCurrentState(ConversationStates.IDLE);
					} else {
						player.setQuest(KARMA_QUEST_SLOT, "test2");
						player.setQuest(TEST_QUEST_SLOT, doneText + ";0");
						npc.say("Ostatnie już pytanie. Jesteś gotowy?");
					}
					player.removeQuest(TEMP_QUEST_SLOT);
					// clear quest slot so question is chosen randomly for player next time
				}
			});

		woodcutter.add(ConversationStates.QUESTION_2,
			ConversationPhrases.YES_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_3,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.removeQuest(TEMP_QUEST_SLOT);
					final String question = questions.getQuestion();
					npc.say("Oto pytanie 3: " + question);
					player.setQuest(TEMP_QUEST_SLOT, question);
					player.setQuest(KARMA_QUEST_SLOT, "test3");
				}
			});

		woodcutter.add(ConversationStates.QUESTION_2,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Szkoda. Powodzenia następnym razem.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		woodcutter.add(ConversationStates.QUESTION_2,
			ConversationPhrases.GOODBYE_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Szkoda. Powodzenia następnym razem. Dowidzenia.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		woodcutter.add(ConversationStates.QUESTION_3,
			"",
			null,
			ConversationStates.QUESTION_3,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String question = player.getQuest(TEMP_QUEST_SLOT);
					final String doneText = player.getQuest(TEST_QUEST_SLOT);
					final String triggerText = sentence.getTriggerExpression().getNormalized();

					if (questions.matches(question, sentence)) {
						// clear quest slot so question is chosen randomly for player next time
						player.setQuest(TEST_QUEST_SLOT, doneText + ";1");
						if (player.getQuest(TEST_QUEST_SLOT).startsWith("1;1;1")) {
							npc.say("Gratulacje! Zaliczyłeś test.");
							player.setSkill("woodcutting", Double.toString(0.2));
							player.addXP(2000);
							final Item siekierka = SingletonRepository.getEntityManager().getItem("siekierka");
							siekierka.setBoundTo(player.getName());
							player.equipOrPutOnGround(siekierka);
							player.setQuest(QUEST_SLOT, "done");
							player.removeQuest(KARMA_QUEST_SLOT);
							player.notifyWorldAboutChanges();
							npc.setCurrentState(ConversationStates.IDLE);
						} else {
							player.setQuest(KARMA_QUEST_SLOT, "test3");
							npc.say("Niestety nie zaliczyłeś testu, ponieważ na któreś pytanie odpowiedziałeś źle. Nie mogę powiedzieć o które chodzi. Powodzenia następnym razem.");
							player.setQuest(QUEST_SLOT, "start");
							npc.setCurrentState(ConversationStates.IDLE);
						}
					} else if (triggerText.equals("bye") || triggerText.equals("dowidzenia")
							|| triggerText.equals("no") || triggerText.equals("nie")) {
						player.setQuest(KARMA_QUEST_SLOT, "test3");
						player.setQuest(QUEST_SLOT, "rejected");
						player.addKarma(-10.0);
						npc.say("Co za szkoda poddałeś się przy ostatnim pytaniu. Dowidzenia.");
						npc.setCurrentState(ConversationStates.IDLE);
					} else {
						player.setQuest(KARMA_QUEST_SLOT, "test3");
						npc.say("Niestety nie zaliczyłeś testu, ponieważ na któreś pytanie odpowiedziałeś źle. Nie mogę powiedzieć o które chodzi. Powodzenia następnym razem.");
						player.setQuest(QUEST_SLOT, "start");
						npc.setCurrentState(ConversationStates.IDLE);
					}
					player.removeQuest(TEST_QUEST_SLOT);
					player.removeQuest(TEMP_QUEST_SLOT);
					// clear quest slot so question is chosen randomly for player next time
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Rozwiąż Test na Drwala",
				"Musisz zdać test na drwala, aby zdobyć umiejętność ścinania drzew.",
				false);
		setQuestion();
	}

	@Override
	public String getName() {
		return "SolveWoodcutterTest";
	}

	// there is a minimum level requirement to get new skill
	@Override
	public int getMinLevel() {
		return 10;
	}

	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}

	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}
	@Override
	public String getNPCName() {
		return "Drwal";
	}
}
