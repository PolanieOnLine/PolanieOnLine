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


import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QUEST: Imperial princess
 
 * PARTICIPANTS: 
 * <ul>
 * <li> The Princess and King in Kalavan Castle</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Princess asks you to fetch a number of herbs and potions</li>
 * <li> You bring them</li>
 * <li> She recommends you to her father</li>
 * <li> you speak with him</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> XP</li>
 * <li> ability to buy houses in Kalavan</li>
 * <li> 10 Karma</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class ImperialPrincess extends AbstractQuest {
	
	/** The player is asked to get a number of herbs depending on level. 
	 * So if they are level 40, they must bring 1 + 1 arandula
	 */
	private static final int ARANDULA_DIVISOR = 40;

	/** The player is asked to get a number of herbs depending on level. 
	 * So if they are level 40, they must bring 4 + 1  potions
	 */
	private static final int POTION_DIVISOR = 10;
	
	/** The player is asked to get a number of herbs depending on level. 
	 * So if they are level 40, they must bring 2 + 1 antidotes
	 */
	private static final int ANTIDOTE_DIVISOR = 20;

	// It is called Imperial Princess because the soldiers in this castle are Imperial soldiers.
	private static final String QUEST_SLOT = "imperial_princess";

	
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
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Princess Ylflia zapytał mnie o zioła i mikstury, aby złagodzić ból jeńców w piwnicach Kalavan.");
		if (!questState.equals("recommended") && !questState.equals("done")) {
			res.add("Muszę powiedzieć Princess Ylflia, że mam \" zioła \", kiedy zbiorę wszystkie zioła, mikstury i odtrutki, które ona potrzebuję.");
		}
		if (player.isQuestInState(QUEST_SLOT, "recommended", "done")) {
			res.add("Przyniosłem Princess Ylflia wszystkie składniki potrzebne do uzdrawiania i powiedziała mi, że ona mnie poleci swemu ojcu, królowi.");
		}
		if (questState.equals("done")) {
			res.add("King Cozart nadał mi obywatelstwo Kalavan.");
		}
		return res;
		}

	private void step_1() {

		final SpeakerNPC npc = npcs.get("Princess Ylflia");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Nie mogę uwolnić więźniów w piwnicy, ale mogę zrobić jedną rzecz. Złagodzić im ból. " +
				"Potrzebuję #ziół do tego.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT,"recommended"),
				ConversationStates.ATTENDING, 
				"Porozmawiaj z moim ojcem, Królem. Zapytałam się go o przyznanie Tobie obywatelstwa Kalavan, " +
				"aby wyrazić Ci moją wdzięczność.",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(new QuestInStateCondition(QUEST_SLOT,"recommended"))),
				ConversationStates.ATTENDING, 
				"Już o coś się Ciebie pytałam.",
				null);	

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Uwięzione potwory wyglądają dużo lepiej od ostatniego razu. Odważyłam się zaryzykować i zejść do piwnicy. Dziękuję!",
				null);

		/** If quest is not started yet, start it. 
		 * The amount of each item that the player must collect depends on their level when they started the quest.
		 */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("herbs", "ziół", "zioła"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Potrzebuję "
								+ Integer.toString(1 + player.getLevel()
										/ ARANDULA_DIVISOR)
								+ " #arandula, 1 #kokuda, 1 #sclaria, 1 #kekik, "
								+ Integer.toString(1 + player.getLevel()
										/ POTION_DIVISOR)
								+ " eliksir i "
								+ Integer.toString(1 + player.getLevel()
										/ ANTIDOTE_DIVISOR)
								+ " antidotum. Zdobędziesz to dla mnie?");
					}
				});

		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję! Musimy być subtelni w tej sprawie. Nie chcę, aby naukowcy coś podejrzewali i wtrącili się. " +
				"Gdy wrócisz ze składnikami to powiedz mi hasło #zioła.",
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						// store the current level in case it increases before
						// she see them next.
						player.setQuest(QUEST_SLOT, Integer.toString(player.getLevel()));
						player.addKarma(10);						
					}
				});

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Pozwalasz im cierpieć! Jakie to podłe.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
		
		// give some hints of where to find herbs. No warranties!
		npc.addReply(
				"kokuda",
				"Wiem, że to zioło można tylko znaleźć na wyspie Athor, sądzę, że tam dobrze" +
				"strzegą sekretu.");
		npc.addReply(
				"sclaria",
				"Uzdrowiciele, którzy używają sclaria zbierają je w różnych miejscach w okolicach Or'ril, w lesie Nalwor. " +
				" Jestem pewna, że znajdziesz je bez problemu.");
		npc.addReply(
				"kekik",
				"Przyjaciel mojej pokojówki Jenny ma źródło niedaleko niego. Mogą występować na lesistych terenach nad rzeką we wschodniej" +
				" części Nalwor.");
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Princess Ylflia");

		/** If player has quest and not in state recommended,
		 * we can check the slot to see what the stored level was.
		 * If the player has brought the right number of herbs, get them */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("herb", "herbs", "ziół", "zioła"),
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(new QuestInStateCondition(QUEST_SLOT,"recommended"))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						try {
							final int level = Integer.parseInt(player.getQuest(QUEST_SLOT));
							final int required_arandula = 1
								+ level / ARANDULA_DIVISOR;
							final int required_antidote = 1
								+ level / ANTIDOTE_DIVISOR;
							final int required_potion = 1
								+ level	/ POTION_DIVISOR;
							if (player.isEquipped("kekik")
								&& player.isEquipped("kokuda")
								&& player.isEquipped("sclaria")
								&& player.isEquipped("arandula",
										required_arandula)
								&& player.isEquipped("eliksir", required_potion)
								&& player.isEquipped("antidotum",
										required_antidote))
							{
								player.drop("kekik");
								player.drop("kokuda");
								player.drop("sclaria");
								player.drop("antidotum", required_antidote);
								player.drop("eliksir", required_potion);
								player.drop("arandula", required_arandula);
								raiser.say("Doskonale! Zarekomenduję Ciebie mojemu ojcu jako dobrą " +
										"i pomocną osobę. On się zgodzi ze mną, że nadajesz się na " +
										"obywatela Kalavan.");
								player.addXP(level * 400);
								player.setQuest(QUEST_SLOT, "recommended");
								player.notifyWorldAboutChanges();
							} else { 
								//reminder of the items to bring
								raiser.say("Cii! Nie mów nic dopóki nie będziesz miał "
									+ required_arandula
									+ " arandula, 1 #kokuda, 1 #sclaria, 1 #kekik, "
									+ required_potion
									+ " eliksir i "
									+ required_antidote
									+ " antidotum. Nie chce, aby ktoś się domyślił naszego przepisu.");
							}
						} catch (final NumberFormatException e) {
							// Should not happen but catch the exception
							raiser.say("Dziwne. Nie rozumiem co się teraz stało. " +
									"Przepraszam, ale jestem trochę zamroczona. Zapytaj kogoś innego o pomoc.");
						}
					}
				});

		/** The player asked about herbs but he brought them already and needs to speak to the King next */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("herb", "herbs", "ziół", "zioła"), 
				new QuestInStateCondition(QUEST_SLOT,"recommended"),
				ConversationStates.ATTENDING, 
				"Zioła, które przyniosłeś spisały się wyśmienicie. Powiedziałam ojcu, że może Ci zaufać. Powinieneś " +
				"pójść do niego i porozmawiać z nim.",
				null);
		
		/** The player asked about herbs but the quest was finished */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("herb", "herbs", "ziół", "zioła"), 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Dziękuję za zioła, które mi przyniosłeś do uleczenia potworów. Ciesze się, że mogę Cię zarekomendować mojemu ojcu do " +
				"nadania obywatelstwa Kalavan.",
				null);
	}

	private void step_3() {
		/** The King is also in the castle and he is the father of the Princess who gave the quest */
		final SpeakerNPC npc = npcs.get("King Cozart");

		/** Complete the quest by speaking to King, who will return right back to idle once he rewards the player*/
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "recommended")),  
			ConversationStates.IDLE,
			"Pozdrawiam! Moja cudowna córka poprosiła mnie o przyznanie Tobie obywatelstwa miasta Kalavan. Rozpatrywanie zostało zakończone. Teraz wybacz mi, że wrócę do mojego posiłku. Dowidzenia.",
			new MultipleActions(new IncreaseXPAction(500), new SetQuestAction(QUEST_SLOT, "done")));

		/** If you aren't in the condition to speak to him (not completed quest, or already spoke) the King will dismiss you */
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotInStateCondition(QUEST_SLOT, "recommended")),  
			ConversationStates.IDLE, 
			"Zostaw mnie! Nie widzisz, że próbuję jeść?",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Obywatelstwo Kalavan",
				"Jeżeli chcesz dostać oficjalne obywatelstwo Kalavan City to najpierw musisz zapytać o pozwolenie króla nim porozmawiasz z jego córką Princess Ylflia...",
				true);
		step_1();
		step_2();
		step_3();
	}
	@Override
	public String getName() {
		return "ImperialPrincess";
	}

		@Override
	public int getMinLevel() {
		return 50;
	}
	
	@Override
	public String getRegion() {
		return Region.KALAVAN;
	}

	@Override
	public String getNPCName() {
		return "Princess Ylflia";
	}
}
