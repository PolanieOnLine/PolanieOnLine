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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.common.parser.Sentence;
import games.stendhal.common.parser.SimilarExprMatcher;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Quest to get the scuba gear.
 * <p>
 *
 * PARTICIPANTS: <ul><li> Edward the diving instructor</ul>
 *
 *
 * STEPS: <ul><li> This quest is about players getting the ability to dive and earn the necessary equipment.
 *  The instructor will as a question and once the player answers correctly will reward them with scuba gear.</ul>
 *
 *
 * REWARD:
 * <ul>
 * <li> 100 XP
 * <li> some karma (5)
 * <li> The Scuba Gear
 * </ul>
 *
 * REPETITIONS: <ul><li> no repetitions</ul>
 *
 * @author soniccuz based on (LookUpQuote by dine)
 */

public class ScubaLicenseQuiz extends AbstractQuest {
	private static final String QUEST_SLOT = "get_diving_license";

	private static Map<String, String> anwsers = new HashMap<String, String>();
	static {
		anwsers.put("Czego doświadczasz, gdy pęcherzyki azotu blokują przepływ krwi w organizmie?",
						"choroba dekompresyjna");
		anwsers.put("Ile procent tlenu jest w powietrzu? Podaj mi liczbę.",
						"21");
		anwsers.put("Fale są powodowane przez ...",
						"wiatr");
		anwsers.put("Większość obrażeń u nurka spowodowana jest przez ryby i wodne zwierzęta ponieważ one ... się ciebie.",
						"boją");
		anwsers.put("Nigdy nie powinieneś nurkować, gdy masz ...",
						"przeziębienie");
	}


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
		res.add("Spotkałem Edwarda byłego nurka, który uczy inne osoby. Jeżeli zdam jego egzamin to dostanę licencję nurka.");
		if (!player.isQuestCompleted(QUEST_SLOT)) {
			res.add("Pytanie na które muszę odpowiedzieć " + player.getQuest(QUEST_SLOT) + ".");
		} else {
			res.add("Zdałem egzamin Edwarda i dostałem licencję nurka.");
		}
		return res;
	}

	private void createLicense() {
		final SpeakerNPC instructor = npcs.get("Edward");

		instructor.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new GreetingMatchesNameCondition(instructor.getName()), true,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (!player.hasQuest(QUEST_SLOT)) {
						npc.say("Cześć jestem w Faiumoni jedynym nauczycielem nurkowania. Jeżeli chcesz odkrywać wspaniał podwodny świat morski to potrzebujesz #licencji i #zbroi #akwalungowej.");
					} else if (!player.isQuestCompleted(QUEST_SLOT)) {
						final String name = player.getQuest(QUEST_SLOT);
						npc.say("Wróciłeś! Wieżę, że studiowałeś i możesz odpowiedzieć na pytanie. " + name);
						npc.setCurrentState(ConversationStates.QUESTION_1);
					} else {
						npc.say("Witaj na pokładzie!");
					}
				}
			});

		instructor.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, Arrays.asList("exam", "test", "egzamin")),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Jesteś gotowy na test?",
				null);

		// TODO: point to diving location
		instructor.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, Arrays.asList("exam", "test", "egzamin")),
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Już zdałeś egzamin! Teraz znajdź dobre miejsce na odkrywanie oceanu.",
				null);

		instructor.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, Arrays.asList("exam", "test", "egzamin")),
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String name = player.getQuest(QUEST_SLOT);
						npc.say("Wieżę, że studiowałeś i możesz odpowiedzieć na pytanie. " + name);
					}
				});

		instructor.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dobrze nurkowanie nie jest dla każdego, ale gdy zmienisz zdanie to wróć do mnie. #Ucz się w tym czasie.", null);

		instructor.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.QUESTION_1, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String name = Rand.rand(anwsers.keySet());
					npc.say("Bardzo dobrze. Oto twoje pytanie. " + name);
					player.setQuest(QUEST_SLOT, name);
				}
			});

		/*
		instructor.add(ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Too bad. You're not qualified to dive until you know the answer. You should #study.", null);
		*/

		// TODO: rewrite this to use standard conditions and actions
		instructor.addMatching(ConversationStates.QUESTION_1, Expression.JOKER, new JokerExprMatcher(), null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String name = player.getQuest(QUEST_SLOT);
					final String quote = anwsers.get(name);

					final Sentence answer = sentence.parseAsMatchingSource();
					final Sentence expected = ConversationParser.parse(quote, new SimilarExprMatcher());

					if (answer.matchesFull(expected)) {
						npc.say("Zgadza się dobra robota! Teraz jesteś licencjonowanym nurkiem! Oto twój akwalung.");
						//For now I'm just handing out scuba gear until there's a license to give.
						final Item ScubaGear = SingletonRepository.getEntityManager().getItem("zbroja akwalungowa");
						ScubaGear.setBoundTo(player.getName());
						player.equipOrPutOnGround(ScubaGear);
						player.addXP(100);
						player.addKarma(5);
						player.setQuest(QUEST_SLOT, "done");
						player.notifyWorldAboutChanges();
					} else if (ConversationPhrases.GOODBYE_MESSAGES.contains(sentence.getTriggerExpression().getNormalized())) {
						npc.say("Dowidzenia - do zobaczenia następnym razem!");
						npc.setCurrentState(ConversationStates.IDLE);
					} else {
						npc.setCurrentState(ConversationStates.ATTENDING);
						npc.say("Źle. #Naucz się i wróć do mnie.");
					}
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Egzamin na Licencję Nurkowania",
				"Edward przyznaje licencję nurkowania dla tych co zdali egzamin.",
				false);
		createLicense();
	}
	@Override
	public String getName() {
		return "DivingLicenseQuiz";
	}

	@Override
	public String getRegion() {
		return Region.ATHOR_ISLAND;
	}
	@Override
	public String getNPCName() {
		return "Edward";
	}

	/**
	 * is scuba diving possible?
	 */
	public static class ScubaCondition implements ChatCondition {

        @Override
        public boolean fire(Player player, Sentence sentence, Entity npc) {
            return player.isEquippedItemInSlot("armor", "zbroja akwalungowa") && player.isQuestCompleted("get_diving_license");
        }

        @Override
        public int hashCode() {
            return -13527181;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ScubaCondition;
        }

        @Override
        public String toString() {
            return "scuba?";
        }
	}
}
