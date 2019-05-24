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
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * Zadanie, które resetuje poziom graczowi z 597 na 0
 *
 * @author KarajuSs 00:33:57 11-07-2018
 */

public class ResetLVL extends AbstractQuest {

	private static final String QUEST_SLOT = "reset_level";

	private static int XP_TO_RESET = 0;
	private static int LEVEL_TO_RESET = 0;

	private static Logger logger = Logger.getLogger(ResetLVL.class);

	@Override
	public List<String> getHistory(Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem smoka Yerena w jaskini w domku na górze Zakopane.");
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Odmówiłem cofnięcia się w czasie.");
		if ("rejected".equals(questState)) {
			return res;
		}
		res.add("Postanowiłem wysłuchać smoka Yerena i cofnąć się w czasie.");
		if ("start".equals(questState)) {
			return res;
		}
		res.add("Yerena cofnęła mój poziom i od teraz muszę na nowo zdobywać punkty doświadczenia!");
		if ("done".equals(questState)) {
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

	private void offerresetlevel() {
		final SpeakerNPC npc = npcs.get("Yerena");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.getLevel() == 597) {
							raiser.say("Dzielny wojowniku, czy jesteś gotów cofnąć swój aktualny poziom?");
						} else {
							npc.say("Abym mógła cofnąć Ciebie w czasie to musisz osiągnąć maksymalny poziom! Twój aktualny poziom to: #"
									+ player.getLevel());
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"To jest tylko Twoja decyzja czy chcesz być ponownie na zerowym poziomie. Życzę powodzenia!",
				new SetQuestAction(QUEST_SLOT, "rejected"));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_1,
				"Pamiętaj, że twój poziom zostanie &'zresetowany', ale #'zadania', #'skille' jak i #'punkty życia' już nie! Chcesz tego? (#'tak')",
				new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(ConversationStates.INFORMATION_1,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_2,
				"Proszę, zastanów się jeszcze raz. Czy jesteś tego pewien? (#'tak')",
				null);

		npc.add(ConversationStates.INFORMATION_2,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_3,
				"Cofnięcie się w czasie zpowoduje, że twój aktualny #poziom się &'wyzeruje', ale twoje #umiejętności zostaną takie jakie były wcześniej! Twoje aktualne zdrowie również zostaną bez zmian. Czy jesteś tego pewien? (#'tak')",
				null);

		// Jeżeli gracz wróci do smoka
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start")),
				ConversationStates.INFORMATION_4,
		        "Witaj ponownie. Przyszedłeś, aby cofnąć swój aktualny poziom?",
				null);

		npc.add(ConversationStates.INFORMATION_4,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_5,
				"Cofnięcie się w czasie zpowoduje, że twój aktualny #poziom się &'wyzeruje', ale twoje #umiejętności zostaną takie jakie były wcześniej! Twoje aktualne zdrowie również zostaną bez zmian. Czy jesteś tego pewien? (#'tak')",
				null);

		npc.add(ConversationStates.INFORMATION_5,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.INFORMATION_6,
				"Proszę, zastanów się jeszcze raz. Czy jesteś tego pewien? (#'tak')",
				null);
	}

	private void startresetlevel() {
		final SpeakerNPC npc = npcs.get("Yerena");

		npc.add(new ConversationStates[] { ConversationStates.INFORMATION_3, ConversationStates.INFORMATION_6 },
				ConversationPhrases.YES_MESSAGES,
				null, ConversationStates.ATTENDING,
				"Proszę bardzo! Cofnąłeś się do momentu kiedy miałeś jeszcze poziom zerowy! Życzę Ci powodzenia na nowej drodze!",
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.hasQuest(QUEST_SLOT) && (player.getLevel() == 597)) {

							player.setXP(XP_TO_RESET);
							player.setLevel(LEVEL_TO_RESET);

							player.setQuest(QUEST_SLOT, "done");
						}
					}
				});

		npc.add(new ConversationStates[] { ConversationStates.QUEST_OFFERED,
				ConversationStates.INFORMATION_1,
				ConversationStates.INFORMATION_2,
				ConversationStates.INFORMATION_3},
				ConversationPhrases.NO_MESSAGES,
				null, ConversationStates.ATTENDING,
				"To jest tylko Twoja decyzja czy chcesz być ponownie na zerowym poziomie. Życzę powodzenia!",
				new SetQuestAction(QUEST_SLOT, "rejected"));

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Smok, który włada czasem",
				"Yerena potrafi cofnąć wojownika w czasie kiedy był jeszcze początkujący.",
				false);
		offerresetlevel();
		startresetlevel();
	}

	@Override
	public String getName() {
		return "ResetLVL";
	}

	@Override
	public String getNPCName() {
		return "Yerena";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
