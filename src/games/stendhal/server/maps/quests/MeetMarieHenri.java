package games.stendhal.server.maps.quests;

import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QUEST: Meet Marie-Henri
 * <p>
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Marie-Henrie, the famous french writer in Ados library</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Marie-Henri asks you to find out the pseudonym he uses when writing
 * novels</li>
 * <li>Find out the pseudonym (the Wikipedian might help)</li>
 * <li>Name the pseudonym to Marie-Henrie</li>
 * <li>Marie-Henri gives you a reward</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>Karma +5</li>
 * <li>An empty scroll</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>No repetitions.</li>
 * </ul>
 *
 * @author RedQueen
 */
public class MeetMarieHenri extends AbstractQuest {

	public static final String QUEST_SLOT = "meet_marie_henri";

	@Override
	public void addToWorld() {
		fillQuestInfo("Spotkaj Marie-Henri",
				"Słynny francuski pisarz sprawdza ogólną wiedzę w bibliotece Ados.",
				false);
		createSteps();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "MeetMarieHenri";
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
	
	@Override
	public String getNPCName() {
		return "Marie-Henri";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Marie-Henri w księgarni Ados.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Poprosił mnie abym poszukał mu pseudonimu, którego użyje podczas pisania powieści. Nie czuje się na siłach aby mu pomóc.");
		}
		if ("start".equals(questState) || "done".equals(questState)) {
			res.add("Postaram się znaleść pseudonim dla niego.");
		}
		if ("done".equals(questState)) {
			res.add("Odpowiedziałem poprawnie na Marie-Henri pytanie, w zamian dostałem nagrodę.");
		}
		return res;
	}

	private void createSteps() {
		// player is asking for a quest
		SpeakerNPC npc = npcs.get("Marie-Henri");


		// TODO: rewrite this to use standard conditions and actions
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, null,
				ConversationStates.QUEST_OFFERED, null, new ChatAction() {
					@Override
					public void fire(final Player player,
							final Sentence sentence, final EventRaiser npc) {
						final String questState = player.getQuest(QUEST_SLOT);
						if ("done".equals(questState)) {
							npc.say("Wiem, że jesteś bardzo mądry ale w tej chwili nie mam innego zadania dla ciebie.");
							npc.setCurrentState(ConversationStates.ATTENDING);
						} else if ("start".equals(questState)) {
							npc.say("Znalazłeś już dla mnie pseudonim?");
							npc.setCurrentState(ConversationStates.QUESTION_1);
						} else {
							npc.say("Testuję w tej chwili wiedzę poszukiwaczy przygód w okolicy. "
									+ "Jeżeli wymyślisz #pseudonim dla mnie, którego będę mógł użyć podczas pisania powiećci, nagrodzę ciebie. "
									+ "Czy czujesz się na siłach?");
						}
					}
				});

		// player accepts quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Świetnie! Jeżeli będziesz znał odpowiedź, powiedz mi.",
				new SetQuestAction(QUEST_SLOT, "start"));

		// player rejects quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Nie chcesz nawet spróbować, to nie jest trudne. Rozczarowałeś mnie.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// player asks for 'pseudonym' when asked to accept quest
		npc.add(ConversationStates.QUEST_OFFERED,
				"pseudonim",
				null,
				ConversationStates.QUEST_OFFERED,
				"Nie używam swojego prawdziwego imienia. Używam pseudonimu, więc pomóż mi.",
				null);

		// player wants to answer the question
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("pseudonim", "rozwiązanie", "pytanie"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new QuestInStateCondition(QUEST_SLOT, "done"))),
				ConversationStates.QUESTION_1,
				"Czy znalazłeś pseudonim dla mnie?", null);

		// player says 'yes' when asked if he knows the correct answer
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "Więc, jak on brzmi?", null);

		// player says 'no' when asked if he knows the correct answer
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Take your time to think about it. Follow the #hints around you to find the answer.",
				null);

		// player asks for hints
		npc.add(ConversationStates.ATTENDING, Arrays.asList("wskazówka", "wskazówki"),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING,
				"Być może książki z tej półki pomogą ci...", null);

		// analyzing the answer

		// TODO: rewrite this to use standard conditions and actions
		npc.addMatching(ConversationStates.QUESTION_2, Expression.JOKER,
				new JokerExprMatcher(), null, ConversationStates.ATTENDING,
				null, new ChatAction() {
					@Override
					public void fire(final Player player,
							final Sentence sentence, final EventRaiser npc) {
						final Sentence answer = sentence.parseAsMatchingSource();
						final Sentence expected = ConversationParser.parse("stendhal");
						final Sentence lastname = ConversationParser.parse("Beyle");

						if (answer.matchesFull(expected)) {
							// answer is correct -> get reward
							npc.say("Tak, ten będzie dobry. Proszę weź ten niezapisany zwój w nagrodę.");
							final Item reward = SingletonRepository
									.getEntityManager().getItem("niezapisany zwój");
							reward.setBoundTo(player.getName());
							player.equipOrPutOnGround(reward);
							player.addXP(200);
							player.setQuest(QUEST_SLOT, "done");
							player.notifyWorldAboutChanges();
							npc.setCurrentState(ConversationStates.IDLE);
						} else if (answer.matchesFull(lastname)) {
							// player says 'Beyle'
							npc.say("Jesteś na dobrym tropie ale ja potrzebuje pseudonim nie moje nazwisko.");
							npc.setCurrentState(ConversationStates.IDLE);
						} else if (ConversationPhrases.GOODBYE_MESSAGES
								.contains(sentence.getTriggerExpression()
										.getNormalized())) {
							// player says 'bye'
							npc.say("Au revoir!");
							npc.setCurrentState(ConversationStates.IDLE);
						} else {
							// answer is not correct
							npc.say("Nie ten nie jest dobry, trzymaj się #wskazówki może ona pomoże ci.");
							npc.setCurrentState(ConversationStates.IDLE);
						}
					}
				});
	}
}