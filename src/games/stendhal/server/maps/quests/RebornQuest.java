/***************************************************************************
 *                 (C) Copyright 2018-2021 - PolanieOnLine                 *
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
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SendPrivateMessageAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Zadanie, które resetuje poziom graczowi z 597 na 0
 *
 * I reset: + 1000 base_hp
 * II reset: + 1000 base_hp
 * III reset: + 1000 base_hp
 * IV reset: + 1000 base_hp
 * V reset: + 2000 base_hp
 *
 * Łącznie quest daje 6000 dodatkowego zdrowia.
 *
 * @author KarajuSs 00:33:57 11-07-2018
 */

public class RebornQuest extends AbstractQuest {
	private static final String QUEST_SLOT = "reset_level";
	private static final String EXTRA_REWARD = "reborn_extra_reward";
	/** LOGGER **/
	private static Logger logger = Logger.getLogger(RebornQuest.class);
	/** NPC **/
	private final SpeakerNPC npc = npcs.get("Yerena");

	/** WARTOŚCI DO ZRESETOWANIA **/
	private static final int XP_TO_RESET = 0;
	private static final int LEVEL_TO_RESET = 0;
	private static final int BASEHP_TO_RESET = 5970;
	/** TELEPORT **/
	private static final String HOME = "int_zakopane_home";

	/** DIALOGI **/
	private final String POWITANIE_1 = "Dzielny wojowniku, czy jesteś gotów by narodzić się na nowo?";
	private final String POWITANIE_2 = "Abym mógła cofnąć Ciebie w czasie to musisz osiągnąć maksymalny poziom! Aktualnie twój poziom to: #";
	private final String POWITANIE_3 = "Witaj ponownie. Przybyłeś znów, by narodzić się na nowo?";

	private final String INFORMACJA_1 = "Pamiętaj, iż &'stracisz' zdobyte doświadczenie w tym świecie, lecz #'zadania', #'umiejętności' oraz otrzymasz mały bonus do #zdrowia! Chcesz tego? (#'tak')";
	private final String INFORMACJA_2 = "Proszę... Zastanów się jeszcze raz. Czy jesteś tego pewien? (#'tak')";
	private final String INFORMACJA_3 = "Cofnięcie się w czasie spowoduje, iż &'stracisz' swój aktualny #'poziom', lecz twoje #umiejętności zostaną takie jakie były wcześniej oraz otrzymasz mały bonus do #zdrowia! Aktualne zdrowie również pozostanie bez zmian. Czy jesteś tego pewien? (#'tak')";

	private final String INFORMACJA_4 = "Pamiętaj, iż &'stracisz' zdobyte doświadczenie w tym świecie, lecz #'zadania' oraz #'umiejętności' już nie, jak i otrzymasz mały bonus do #zdrowia! Chcesz tego? (#'tak')";
	private final String INFORMACJA_5 = "Cofnięcie się w czasie spowoduje, iż &'stracisz' swój aktualny #'poziom', lecz twoje #umiejętności zostaną takie jakie były wcześniej oraz otrzymasz mały bonus do #zdrowia! Czy jesteś tego pewien? (#'tak')";

	private final String ODRZUCENIE = "To jest tylko Twoja decyzja czy chcesz ponownie poczuć przygodę na zerowym poziomie. Życzę powodzenia!";
	private final String NAGRODA = "Została zagięta Twoja teraźniejszość przez potężnego smoka czasu, #'Yereny'... abyś mógł przeżyć przygodę jeszcze raz... Podążaj nową ścieżką, którą sobie obierzesz...";
	private final String UKONCZONE = "Wybacz... lecz nie czuję się za dobrze, aby ponownie użyć swej mocy...";
	
	private final String DODATKOWA_NAGRODA = NAGRODA + " Otrzymałeś również pamiątkę po swoich wcześniejszych podróżach!";

	/** STATUS **/
	private final static String DONE1 = "done;reborn_1";
	private final static String DONE2 = "done;reborn_2";
	private final static String DONE3 = "done;reborn_3";
	private final static String DONE4 = "done;reborn_4";
	private final static String DONE5 = "done;reborn_5";

	private ChatAction Welcome() {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (player.getLevel() == 597) {
					raiser.say(POWITANIE_1);
				} else {
					raiser.say(POWITANIE_2 + player.getLevel());
					raiser.setCurrentState(ConversationStates.ATTENDING);
				}
			}
		};
	}

	private void reset_level() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.OFFERED_1_REBORN,
				null, Welcome());

		npc.add(ConversationStates.OFFERED_1_REBORN,
				ConversationPhrases.YES_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.INFORMATION_1,
				INFORMACJA_1,
				new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(ConversationStates.INFORMATION_1,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_2,
				INFORMACJA_2,
				null);

		npc.add(ConversationStates.INFORMATION_2,
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.INFORMATION_3,
				INFORMACJA_3,
				null);

		npc.add(ConversationStates.INFORMATION_2,
				ConversationPhrases.YES_MESSAGES,
				new OrCondition(
					new QuestInStateCondition(QUEST_SLOT, "start;2"),
					new QuestInStateCondition(QUEST_SLOT, "start;3"),
					new QuestInStateCondition(QUEST_SLOT, "start;4"),
					new QuestInStateCondition(QUEST_SLOT, "start;5")),
				ConversationStates.INFORMATION_3,
				INFORMACJA_5,
				null);

		// Odrzucenie przez gracza
		npc.add(new ConversationStates[] { ConversationStates.INFORMATION_1, ConversationStates.INFORMATION_2, ConversationStates.INFORMATION_3,
				ConversationStates.INFORMATION_4, ConversationStates.INFORMATION_5, ConversationStates.INFORMATION_6 },
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			ODRZUCENIE,
			null);

		// Jeżeli gracz wróci do smoka
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new OrCondition(
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new QuestInStateCondition(QUEST_SLOT, "start;2"),
						new QuestInStateCondition(QUEST_SLOT, "start;3"),
						new QuestInStateCondition(QUEST_SLOT, "start;4"),
						new QuestInStateCondition(QUEST_SLOT, "start;5"))),
				ConversationStates.INFORMATION_4,
				POWITANIE_3,
				null);

		npc.add(ConversationStates.INFORMATION_4,
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.INFORMATION_5,
				INFORMACJA_3,
				null);

		npc.add(ConversationStates.INFORMATION_4,
				ConversationPhrases.YES_MESSAGES,
				new OrCondition(
					new QuestInStateCondition(QUEST_SLOT, "start;2"),
					new QuestInStateCondition(QUEST_SLOT, "start;3"),
					new QuestInStateCondition(QUEST_SLOT, "start;4"),
					new QuestInStateCondition(QUEST_SLOT, "start;5")),
				ConversationStates.INFORMATION_5,
				INFORMACJA_5,
				null);

		npc.add(ConversationStates.INFORMATION_5,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_6,
				INFORMACJA_2,
				null);

		npc.add(new ConversationStates[] { ConversationStates.OFFERED_1_REBORN, ConversationStates.INFORMATION_3, ConversationStates.INFORMATION_6 },
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
					new TeleportAction(HOME, 11, 4, Direction.DOWN),
					new SendPrivateMessageAction(NAGRODA),
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							if (player.hasQuest(QUEST_SLOT) && (player.getLevel() == 597)) {
								// Ustaw graczowi zerowy poziom wraz z zerową ilością doświadczenia
								player.setXP(XP_TO_RESET);
								player.setLevel(LEVEL_TO_RESET);
								player.setHP((player.getHP() - BASEHP_TO_RESET) + 1000);
								player.setBaseHP((player.getBaseHP() - BASEHP_TO_RESET) + 1000);
	
								// Ustaw zadanie na zakończone
								player.setQuest(QUEST_SLOT, DONE1);
							}
						}
					}));
	}
	
	private void second_attempt() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, DONE1),
				ConversationStates.OFFERED_2_REBORN,
				null, Welcome());

		npc.add(ConversationStates.OFFERED_2_REBORN,
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, DONE1),
				ConversationStates.INFORMATION_1,
				INFORMACJA_4,
				new SetQuestAction(QUEST_SLOT, "start;2"));

		npc.add(new ConversationStates[] { ConversationStates.OFFERED_2_REBORN, ConversationStates.INFORMATION_3, ConversationStates.INFORMATION_6 },
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "start;2"),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
					new TeleportAction(HOME, 11, 4, Direction.DOWN),
					new SendPrivateMessageAction(NAGRODA),
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							if (player.hasQuest(QUEST_SLOT) && (player.getLevel() == 597)) {
								// Ustaw graczowi zerowy poziom wraz z zerową ilością doświadczenia
								player.setXP(XP_TO_RESET);
								player.setLevel(LEVEL_TO_RESET);
								player.setHP((player.getHP() - BASEHP_TO_RESET) + 1000);
								player.setBaseHP((player.getBaseHP() - BASEHP_TO_RESET) + 1000);

								// Ustaw zadanie na zakończone
								player.setQuest(QUEST_SLOT, DONE2);
							}
						}
					}));
	}

	private void third_attempt() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, DONE2),
				ConversationStates.OFFERED_3_REBORN,
				null, Welcome());

		npc.add(ConversationStates.OFFERED_3_REBORN,
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, DONE2),
				ConversationStates.INFORMATION_1,
				INFORMACJA_4,
				new SetQuestAction(QUEST_SLOT, "start;3"));

		npc.add(new ConversationStates[] { ConversationStates.OFFERED_3_REBORN, ConversationStates.INFORMATION_3, ConversationStates.INFORMATION_6 },
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "start;3"),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
					new TeleportAction(HOME, 11, 4, Direction.DOWN),
					new SendPrivateMessageAction(DODATKOWA_NAGRODA),
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							if (player.hasQuest(QUEST_SLOT) && (player.getLevel() == 597)) {
								// Ustaw graczowi zerowy poziom wraz z zerową ilością doświadczenia
								player.setXP(XP_TO_RESET);
								player.setLevel(LEVEL_TO_RESET);
								player.setHP((player.getHP() - BASEHP_TO_RESET) + 1000);
								player.setBaseHP((player.getBaseHP() - BASEHP_TO_RESET) + 1000);

								if (player.getQuest(EXTRA_REWARD + "3") == null) {
									final Item ldagger = SingletonRepository.getEntityManager().getItem("sztylet leworęczny");
									final Item rdagger = SingletonRepository.getEntityManager().getItem("sztylet praworęczny");
									ldagger.setBoundTo(player.getName());
									rdagger.setBoundTo(player.getName());
									player.equipOrPutOnGround(ldagger);
									player.equipOrPutOnGround(rdagger);
								}
	
								// Ustaw zadanie na zakończone
								player.setQuest(QUEST_SLOT, DONE3);
								player.setQuest(EXTRA_REWARD + "3", "done");
							}
						}
					}));
	}
	
	private void fourth_attempt() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, DONE3),
				ConversationStates.OFFERED_4_REBORN,
				null, Welcome());

		npc.add(ConversationStates.OFFERED_4_REBORN,
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, DONE3),
				ConversationStates.INFORMATION_1,
				INFORMACJA_4,
				new SetQuestAction(QUEST_SLOT, "start;4"));

		npc.add(new ConversationStates[] { ConversationStates.OFFERED_4_REBORN, ConversationStates.INFORMATION_3, ConversationStates.INFORMATION_6 },
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "start;4"),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
					new TeleportAction(HOME, 11, 4, Direction.DOWN),
					new SendPrivateMessageAction(DODATKOWA_NAGRODA),
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							if (player.hasQuest(QUEST_SLOT) && (player.getLevel() == 597)) {
								// Ustaw graczowi zerowy poziom wraz z zerową ilością doświadczenia
								player.setXP(XP_TO_RESET);
								player.setLevel(LEVEL_TO_RESET);
								player.setHP((player.getHP() - BASEHP_TO_RESET) + 1000);
								player.setBaseHP((player.getBaseHP() - BASEHP_TO_RESET) + 1000);

								if (player.getQuest(EXTRA_REWARD + "4") == null) {
									final Item naszyjnik = SingletonRepository.getEntityManager().getItem("amulecik z mithrilu");
									naszyjnik.setBoundTo(player.getName());
									player.equipOrPutOnGround(naszyjnik);
								}

								// Ustaw zadanie na zakończone
								player.setQuest(QUEST_SLOT, DONE4);
								player.setQuest(EXTRA_REWARD + "4", "done");
							}
						}
					}));
	}

	private void fifth_attempt() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, DONE4),
				ConversationStates.OFFERED_5_REBORN,
				null, Welcome());

		npc.add(ConversationStates.OFFERED_5_REBORN,
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, DONE4),
				ConversationStates.INFORMATION_1,
				INFORMACJA_4,
				new SetQuestAction(QUEST_SLOT, "start;5"));

		npc.add(new ConversationStates[] { ConversationStates.OFFERED_5_REBORN, ConversationStates.INFORMATION_3, ConversationStates.INFORMATION_6 },
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "start;5"),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
					new TeleportAction(HOME, 11, 4, Direction.DOWN),
					new SendPrivateMessageAction(DODATKOWA_NAGRODA),
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							if (player.hasQuest(QUEST_SLOT) && (player.getLevel() == 597)) {
								// Ustaw graczowi zerowy poziom wraz z zerową ilością doświadczenia
								player.setXP(XP_TO_RESET);
								player.setLevel(LEVEL_TO_RESET);
								player.setHP((player.getHP() - BASEHP_TO_RESET) + 2000);
								player.setBaseHP((player.getBaseHP() - BASEHP_TO_RESET) + 2000);

								if (player.getQuest(EXTRA_REWARD + "5") == null) {
									final Item excalibur = SingletonRepository.getEntityManager().getItem("ekskalibur");
									excalibur.setBoundTo(player.getName());
									player.equipOrPutOnGround(excalibur);
								}

								// Ustaw zadanie na zakończone
								player.setQuest(QUEST_SLOT, DONE5);
								player.setQuest(EXTRA_REWARD + "5", "done");
							}
						}
					}));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, DONE5),
				ConversationStates.ATTENDING,
				UKONCZONE,
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Władca Czasu",
				"Yerena potrafi cofnąć wojownika w czasie, do momentu jego pierwszych narodzin.",
				false);
		// Pierwszy reset
		reset_level();
		// Drugi reset
		second_attempt();
		// Trzeci reset
		third_attempt();
		// Czwarty reset
		fourth_attempt();
		// Piąty reset
		fifth_attempt();
	}

	@Override
	public List<String> getHistory(Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(player.getGenderVerb("Spotkałem") + " smoka Yerena w jaskini, w domku na górze Zakopane.");
		final String questState = player.getQuest(QUEST_SLOT);
		res.add(player.getGenderVerb("Odmówiłem") + " cofnięcia się w czasie.");
		if ("rejected".equals(questState)) {
			return res;
		}
		res.add(player.getGenderVerb("Postanowiłem") + " wysłuchać smoka Yerena i cofnąć się w czasie.");
		if ("start".equals(questState)) {
			return res;
		}
		res.add("Yerena cofnęła mój poziom i od teraz muszę na nowo zdobywać punkty doświadczenia!");
		if ("done_reborn".equals(questState)) {
			return res;
		}

		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasujące do stanu poszukiwania " + questState);
		return debug;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Władca Czasu";
	}

	@Override
	public String getNPCName() {
		return "Yerena";
	}
}
