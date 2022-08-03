/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/**
 * QUEST: The złota ciupaga upgrading.
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Józek will upgrade for you the złota ciupaga.
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Józek tells you about the złota ciupaga z wąsem.
 * <li> He offers to upgrade a złota ciupaga for you if you bring him what it
 * needs.
 * <li> You give him all what he ask you.
 * <li> He tells you you must have killed a złota śmierć
 * <li> Józek upgrade the złota ciupaga to złota ciupaga z wąsem for you
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>złota ciupaga z wąsem
 * <li>20000 XP
 * <li>100 Karma
 * </ul>
 *
 *
 * REPETITIONS:
 * <ul>
 * <li> None.
 * </ul>
 */
public class ZlotaCiupagaJedenWas extends AbstractQuest {
	private static final String QUEST_SLOT = "zlota_ciupaga_was";
	private final SpeakerNPC npc = npcs.get("Józek");

	private static final String NAGRODA_WIELKOLUDA_QUEST_SLOT = "help_wielkolud_basehp";

	private static final int REQUIRED_WAIT_DAYS = 5;
	private static final int REQUIRED_HOURS = 8;

	private static Logger logger = Logger.getLogger(ZlotaCiupagaJedenWas.class);

	private void step_1() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isQuestCompleted(NAGRODA_WIELKOLUDA_QUEST_SLOT)) {
						if(player.getLevel() >= 200) {
							if(player.getKarma()>=200) {
								if(player.hasKilledSolo("złota śmierć")) {
									if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
										raiser.say("Jesteś " + Grammar.genderVerb(player.getGender(), "zainteresowany") + " ulepszeniem złotej ciupagi? Mogę ją dla ciebie ulepszyć.");
									} else if (player.getQuest(QUEST_SLOT, 0).equals("start")) {
										raiser.say("Już się Ciebie pytałem czy chcesz #ulepszyć złotą ciupagę!");
									} else if (player.isQuestCompleted(QUEST_SLOT)) {
										final String[] waittokens = player.getQuest(QUEST_SLOT).split(";");
										final long waitdelay = REQUIRED_WAIT_DAYS * MathHelper.MILLISECONDS_IN_ONE_DAY;
										final long waittimeRemaining = (Long.parseLong(waittokens[1]) + waitdelay) - System.currentTimeMillis();
										if (waittimeRemaining > 0L) {
											raiser.say("Jestem zmęczony. Powróć za " + TimeUtil.approxTimeUntil((int) (waittimeRemaining / 1000L)) + ".");
										} else {
											raiser.say(Grammar.genderVerb(player.getGender(), "Przyszedłeś") + ", aby ulepszyć złotą ciupagę?");
											raiser.setCurrentState(ConversationStates.QUEST_OFFERED);
										}
									} else if (player.getQuest(QUEST_SLOT).startsWith("przedmioty")) {
										raiser.say("Już rozmawialiśmy na ten temat... Masz mi przynieść #przedmioty do ulepszenia Twojej ciupagi.");
										raiser.setCurrentState(ConversationStates.ATTENDING);
									}
								} else {
									npc.say("Rozmawiam tylko z osobami, które wykazały się w walce zabijając samodzielnie złotą śmierć.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Nie jesteś " + Grammar.genderVerb(player.getGender(), "godny") + ", aby dzierżyć tak wspaniałą broń. Popracuj nad dobrymi uczynkami. Twoja karma musi być minimum 200.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Twój stan społeczny jest zbyt niski aby podjąć te zadanie. Wróć gdy zdobędziesz 200 poziom.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					} else {
						npc.say("Widzę, że nie " + Grammar.genderVerb(player.getGender(), "pomogłeś") + " Wielkoludowi. Wróć i pomóż mu, inaczej nie mamy o czym rozmawiać!");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Będę potrzebował kilku rzeczy do #ulepszenia ciupagi.");
					player.setQuest(QUEST_SLOT, "start");
					player.addKarma(10);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Twoja strata.",
			new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("ulepszyć", "ulepszyc", "ulepszenia"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
				new QuestCompletedCondition(NAGRODA_WIELKOLUDA_QUEST_SLOT)),
			ConversationStates.ATTENDING, "Do jej ulepszenia są potrzebne przeróżne #przedmioty.",
			new SetQuestAction(QUEST_SLOT, "przedmioty"));

		final List<ChatAction> ciupagaactions = new LinkedList<ChatAction>();
		ciupagaactions.add(new DropItemAction("złota ciupaga",1));
		ciupagaactions.add(new DropItemAction("sztabka złota",70));
		ciupagaactions.add(new DropItemAction("złoty róg",1));
		ciupagaactions.add(new DropItemAction("polano",4));
		ciupagaactions.add(new DropItemAction("money",120000));
		ciupagaactions.add(new SetQuestAction(QUEST_SLOT, "forging;"));
		ciupagaactions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("done", "przedmioty"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "przedmioty"),
								 new PlayerHasItemWithHimCondition("złota ciupaga",1),
								 new PlayerHasItemWithHimCondition("sztabka złota",70),
								 new PlayerHasItemWithHimCondition("złoty róg",1),
								 new PlayerHasItemWithHimCondition("polano",4),
								 new PlayerHasItemWithHimCondition("money",120000)),
				ConversationStates.IDLE, "Widzę, że masz wszystko o co cię prosiłem. Wróć za 8 godzin, a ciupaga będzie gotowa. Przypomnij mi mówiąc #'nagroda'.",
				new MultipleActions(ciupagaactions));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przedmioty", "przypomnij"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "przedmioty"),
								 new NotCondition(
								 new AndCondition(new PlayerHasItemWithHimCondition("złota ciupaga",1),
												  new PlayerHasItemWithHimCondition("sztabka złota",70),
												  new PlayerHasItemWithHimCondition("złoty róg",1),
												  new PlayerHasItemWithHimCondition("polano",4),
												  new PlayerHasItemWithHimCondition("money",120000)))),
				ConversationStates.IDLE, "Potrzebuję:\n"
									+"#'1 złotą ciupagę'\n"
									+"#'70 sztabek złota'\n"
									+"#'1 złoty róg'\n"
									+"#'4 polana'\n"
									+"#'120000 money'\n"
									+"Proszę przynieś mi to wszystko naraz. Jeżeli zapomnisz co masz przynieść to powiedz #'przypomnij'. Dziękuję!", null);

	}

	private void step_4() {
		final int delay = REQUIRED_HOURS * MathHelper.SECONDS_IN_ONE_MINUTE;

		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, delay))),
			ConversationStates.IDLE, 
			null, 
			new SayTimeRemainingAction(QUEST_SLOT, 1, delay, "Wciąż pracuje nad ulepszaniem Twojej złotej ciupagi. Wróć za "));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("nagroda"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new TimePassedCondition(QUEST_SLOT, 1, delay)),
			ConversationStates.IDLE, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Warto było czekać. A oto złota ciupaga z wąsem. Do widzenia!");
					player.addXP(250000);
					player.addKarma(100);
					final Item zlotaCiupagaZWasem = SingletonRepository.getEntityManager().getItem("złota ciupaga z wąsem");
					zlotaCiupagaZWasem.setBoundTo(player.getName());
					player.equipOrPutOnGround(zlotaCiupagaZWasem);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done" + ";" + System.currentTimeMillis());
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Złota Ciupaga z Jednym Wąsem",
				"Józek wzmocni Twoją złotą ciupagę.",
				true);
		step_1();
		step_2();
		step_3();
		step_4();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add(Grammar.genderVerb(player.getGender(), "Spotkałem") + " się z Józkiem.");
		res.add("Zaproponował mi ulepszenie złotej ciupagi.");
		if ("rejected".equals(questState)) {
			res.add("Józek wymaga zbyt wiele za ulepszenie złotej ciupagi. Nie " + Grammar.genderVerb(player.getGender(), "zgodziłem") + " się na jego propozycje.");
			return res;
		}
		if ("start".equals(questState)) {
			return res;
		}
		res.add("Józek kazał mi przynnieść kilka przedmiotów, które są potrzebne. Gdybym " + Grammar.genderVerb(player.getGender(), "zapomniał") + " co mam przynieść mam mu powiedzieć: przypomnij.");
		if ("przedmioty".equals(questState)) {
			return res;
		}
		res.add(Grammar.genderVerb(player.getGender(), "Dostarczyłem") + " potrzebne przedmioty! Józek zabrał się za ulepszenie mojej ciupagi.");
		if (questState.startsWith("forging")) {
			if (new TimePassedCondition(QUEST_SLOT,1,REQUIRED_HOURS).fire(player, null, null)) {
				res.add("Mogę iść do Józka i sprawdzić czy już ulepszył moją ciupagę. Hasło: nagroda.");
			} else {
				res.add("Po ciupagę mam zgłosić się za 8 godzin. Hasło: nagroda.");
			}
			return res;
		}
		res.add("Warto było czekać na ciupagę z jednym wąsem. Ta piękna broń należy teraz do mnie.");
		if (isCompleted(player)) {
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Złota Ciupaga z Jednym Wąsem";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
