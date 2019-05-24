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
// Based on FishermansLicenseQuiz.

package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.game.RPObjectNotFoundException;

/**
 * QUEST: Staz na gornika.
 * 
 * PARTICIPANTS: 
 * <ul>
 * <li>Bercik the miner</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> The miner puts all stones onto the table and the player must
 *  	identify the names of the stone in the correct order.</li>
 * <li> The player has one try per day.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> 500 XP</li>
 * <li> Karma: 15</li>
 * <li> Skill: mining</li>
 * <li> Kilof</li>
 * <li> Lina</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> If the player has failed the quiz, he can retry after 24 hours.</li>
 * <li> After passing the quiz, no more repetitions are possible.</li>
 * </ul>
 * 
 * @author dine
 */

public class StazNaGornika extends AbstractQuest {
	static final String QUEST_SLOT = "cech_gornika";

	// TODO: use standard conditions and actions

	private final List<String> speciesList = Arrays.asList("szmaragd", "szafir", "ametyst", "kryształ ametystu",
			"rubin", "obsydian", "diament", "bursztyn", "ruda żelaza", "ruda srebra", "bryłka złota", "bryłka mithrilu", "sztabka srebra", "sztabka mithrilu", "sztabka złota");

	private int currentSpeciesNo;

	private static StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(
			"int_koscielisko_stones_room");

	private Item miningOnTable;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Bercika. Po zaliczeniu egzaminu na górnika, moje szanse na wydobycie kamieni zwiększą się.");
		if (!player.isQuestCompleted(QUEST_SLOT)) {
			if (remainingTimeToWait(player)>0) {
				res.add("Jest zbyt wcześnie, aby spróbować ponownie, przystąpić do egzaminu.");
			} else {
				res.add("Minelo sporo czasu od oblania ostatniego egzaminu, mogę teraz spróbować ponownie.");
			}
		} else {
			res.add("Egzamin zaliczyłem z wynikiem pozytywnym. Teraz moje szanse znalezienia kamieni szlachetnych są dużo większe.");
		}
		return res;
	}

	public void cleanUpTable() {
		if (miningOnTable != null) {
			try {
				zone.remove(miningOnTable);
			} catch (final RPObjectNotFoundException e) {
				// The item timed out, or an admin destroyed it.
				// So no need to clean up the table.
			}
			miningOnTable = null;
		}
	}

	private void startQuiz() {
		Collections.shuffle(speciesList);
		currentSpeciesNo = -1;

		putNextMiningOnTable();
	}

	private String getCurrentSpecies() {
		return speciesList.get(currentSpeciesNo);
	}

	private void putNextMiningOnTable() {
		currentSpeciesNo++;
		cleanUpTable();
		miningOnTable = SingletonRepository.getEntityManager()
				.getItem(getCurrentSpecies());
		miningOnTable.setDescription("Co to za kamień?");

		miningOnTable.setPosition(19, 4);
		zone.add(miningOnTable);
	}

	private long remainingTimeToWait(final Player player) {
		if (!player.hasQuest(QUEST_SLOT)) {
			// The player has never tried the quiz before.
			return 0L;
		}
		final long timeLastFailed = Long.parseLong(player.getQuest(QUEST_SLOT));
		final long onedayInMilliseconds = 60 * 60 * 24 * 1000; 
		final long timeRemaining = timeLastFailed + onedayInMilliseconds
				- System.currentTimeMillis();

		return timeRemaining;
	}

	private void createQuizStep() {
		final SpeakerNPC minerman = npcs.get("Bercik");

		// Don't Use condition here, because of FishermansLicenseCollector
		minerman.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, null,
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						if (player.isQuestCompleted(StazNaGornika.QUEST_SLOT)) {
							npc.say("Masz już uprawnienia górnicze i nie mam dla Ciebie zadania.");
						} else {
							npc.say("Niczego nie chcę, ale możesz zdać #egzamin górniczy.");
						}
					}
				});

		minerman.add(ConversationStates.ATTENDING, Arrays.asList("exam", "egzamin", "egzaminu"), null,
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						if (player.isQuestCompleted(StazNaGornika.QUEST_SLOT)) {
							npc.say("Już masz uprawnienia górnicze.");
						} else {
							final long timeRemaining = remainingTimeToWait(player);
							if (timeRemaining > 0L) {
								npc.say("Quiz możesz zrobić tylko raz dziennie. Wróc za "
									+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
									+ ".");
							} else {
								npc.say("Jesteś gotowy na egzamin?");
								npc.setCurrentState(ConversationStates.QUEST_OFFERED);
							}
						}
					}
				});

		minerman.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Wróć, gdy będziesz gotowy.",
				null);

		minerman.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_1,
				"Dobrze. Pierwsze pytanie brzmi: Co to jest?",
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						startQuiz();
						player.setQuest(QUEST_SLOT, "" + System.currentTimeMillis());
					}
				});

		minerman.addMatching(ConversationStates.QUESTION_1, Expression.JOKER, new JokerExprMatcher(),
				new NotCondition(new TriggerInListCondition(ConversationPhrases.GOODBYE_MESSAGES)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						if (sentence.getTriggerExpression().matches(ConversationParser.createTriggerExpression(getCurrentSpecies()))) {
							if (currentSpeciesNo == speciesList.size() - 1) {
								npc.say("Zgadza się! Gratulacje zdałeś egzamin."
								+ "W nagrodę weź linę i kilof. Narzędzia te ułatwią tobie pracę pod ziemią");
								cleanUpTable();
								player.setQuest(QUEST_SLOT, "done");
								player.addKarma(15);
								player.addXP(500);
								final Item kilof  = SingletonRepository.getEntityManager().getItem("kilof");
								kilof.setBoundTo(player.getName());
								player.equipOrPutOnGround(kilof);
								final Item lina = SingletonRepository.getEntityManager().getItem("lina");
								lina.setBoundTo(player.getName());
								player.equipOrPutOnGround(lina);
								player.setSkill("mining", Double.toString(0.2));
								player.notifyWorldAboutChanges();
							} else {
								npc.say("Zgadza się! Jak się to nazywa?");
								putNextMiningOnTable();
								npc.setCurrentState(ConversationStates.QUESTION_1);
							}
						} else {
							npc.say("Pomyliłeś się. Niestety oblałeś, ale możesz za 24 godziny ponownie spróbować.");
							cleanUpTable();
							// remember the current time, as you can't do the
							// quiz twice a day.
							player.setQuest(QUEST_SLOT, "" + System.currentTimeMillis());
						}
					}
				});

		minerman.add(ConversationStates.ANY, ConversationPhrases.GOODBYE_MESSAGES,
				ConversationStates.IDLE, "Dowidzenia.", new ChatAction() {

			// this should be put into a custom ChatAction for this quest when the quest is refactored
			@Override
			public void fire(final Player player, final Sentence sentence,
					final EventRaiser npc) {
				cleanUpTable();
			}
		});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Egzamin na Górnika",
			"Bercik chce sprawdzić moją wiedzę na temat kamieni szlachetnych.",
			false);
		createQuizStep();
	}

	@Override
	public String getName() {
		return "StazNaGornika";
	}
	@Override
	public String getNPCName() {
		return "Bercik";
	}
}
