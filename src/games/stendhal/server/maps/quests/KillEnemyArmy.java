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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledInSumForQuestCondition;
import games.stendhal.server.entity.npc.condition.KillsQuestSlotNeedUpdateCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.Pair;


/**
 * QUEST: KillEnemyArmy
 *
 * PARTICIPANTS: <ul>
 * <li> Despot Halb Errvl
 * <li> some creatures
 * </ul>
 *
 * STEPS:<ul>
 * <li> Despot asking you to kill some of enemy forces.
 * <li> Kill them and go back to Despot for your reward.
 * </ul>
 *
 *
 * REWARD:<ul>
 * <li> 100k of XP, or 300 karma.
 * <li> random moneys - from 10k to 60k, step 10k.
 * <li> 100 karma for killing 100% creatures
 * <li> 50 karma for killing every 50% next creatures
 * </ul>
 *
 * REPETITIONS: <ul><li> once a week.</ul>
 */

 public class KillEnemyArmy extends AbstractQuest {

	private static final String QUEST_NPC = "Despot Halb Errvl";
	private static final String QUEST_SLOT = "kill_enemy_army";
	private static final int delay = MathHelper.MINUTES_IN_ONE_WEEK;
	
	protected HashMap<String, Pair<Integer, String>> enemyForces = new HashMap<String, Pair<Integer,String>>();
	protected HashMap<String, List<String>> enemys = new HashMap<String, List<String>>();




	public KillEnemyArmy() {
		super();
		// fill monster types map
		enemyForces.put("blordrough",
				new Pair<Integer, String>(50,"Wojska Blordrough zamieszkują tunele Ados. W bezpośrednim starciu są dużo silniejsi. Dlatego Blordrough opanowały część ziem Deniran."));
		enemyForces.put("madaram",
				new Pair<Integer, String>(100,"Ich siły są gdzieś pod Fado. Są ohydne."));
		enemyForces.put("mroczne elfy",
				new Pair<Integer, String>(100,"Mroczne Elfy można znaleźć w podziemiach Nalwor. Używają trucizny w bitwach, którą zbierają z różnych jadowitych stworzeń."));
		enemyForces.put("chaosy",
				new Pair<Integer, String>(150,"Są silni i szaleni. Tylko dzięki moim elitarnym łucznikom możemy zapobiec ich ekspansji."));
		enemyForces.put("górski krasnal",
				new Pair<Integer, String>(150,"To moi historyczni sąsiedzi, żyjący w kopalniach Semos."));
		enemyForces.put("górski ork",
				new Pair<Integer, String>(150,"Głupie stworzenia, ale bardzo silne. Gdzieś w kopalniach Semos można je znaleźć."));
		enemyForces.put("imperial",
				new Pair<Integer, String>(200,"Pochodzą one z zamku w podziemnym mieście Sedah, rządzonej przez ich cesarza Dalmunga."));
		enemyForces.put("barbarzyńca",
				new Pair<Integer, String>(200,"Różne plemiona barbarzyńskie żyją na powierzchni w rejonie północno-zachodnim Góry Ados. Nie są niebezpieczne, ale głośne."));
		enemyForces.put("oni",
				new Pair<Integer, String>(200,"Bardzo dziwny naród, żyjący w swoim zamku w lesie Fado. Istnieją pogłoski, że wchodzą w sojusz z czarodziejami z magicznego miasta."));


		/*
		 * those are not interesting
		enemyForces.put("dwarf",
				new Pair<Integer, String>(275,""));
		enemyForces.put("elf",
				new Pair<Integer, String>(300,""));
		enemyForces.put("skeleton",
				new Pair<Integer, String>(500,""));
		enemyForces.put("gnome",
				new Pair<Integer, String>(1000,""));
		*/

		/*
		 *  fill creatures map
		 */

		enemys.put("blordrough",
				Arrays.asList("blordrough kwatermistrz",
							  "uzbrojony lider",
							  "superczłowiek"));
		enemys.put("mroczne elfy",
				Arrays.asList("elf mikrus",
							  "elf ciemności łucznik",
							  "elf ciemności",
							  "elf ciemności łucznik elitarny",
							  "elf ciemności kapitan",
							  "elf ciemności rycerz",
							  "elf ciemności generał",
							  "elf ciemności czarownik",
							  "elf ciemności królewicz",
							  "elf ciemności czarnoksiężnik",
							  "elf ciemności admirał",
							  "elf ciemności mistrz",
						"elf ciemności matrona"));
		enemys.put("chaosy",
				Arrays.asList("żołnierz chaosu",
							  "wojownik chaosu",
							  "komandor chaosu",
							  "czarnoksiężnik chaosu",
							  "jeździec smoków chaosu",
							  "lord chaosu",
							  "jeździec chaosu na zielonym smoku",
							  "chaosu lord wywyższony",
							  "jeździec chaosu na czerwonym smoku"));
		enemys.put("górski krasnal",
				Arrays.asList("górski krasnal",
							  "górski starszy krasnal",
							  "górski krasnal strażnik",
							  "górski krasnal bohater",
							  "górski krasnal lider",
							  "Dhohr Nuggetcutter",
							  "gigantyczny krasnal",
							  "krasnal golem"));
		enemys.put("górski ork",
				Arrays.asList("górski ork",
							  "górski ork wojownik",
							  "górski ork łowca",
							  "szef górskich orków"));
		enemys.put("imperial",
				Arrays.asList("obrońca imperium",
							  "weteran imperium",
							  "łucznik imperium",
							  "kapłan imperium",
							  "elitarny strażnik imperium",
							  "uczony imperium",
							  "wysoki kapłan imperium",
							  "łucznik imperium lider",
							  "elitarny łucznik imperium",
							  "imperialny lider",
							  "szef żołnierzy imperium",
							  "rycerz imperium",
							  "komandor imperium",
							  "eksperyment imperium",
							  "imperialny sługa demonów",
							  "mutant imperium",
							  "generał imperium",
							  "imperialny lord demonów",
							  "cesarz dalmung",
							  "imperialny generał gigant"));
		enemys.put("madaram",
				Arrays.asList("madaram wieśniak",
							  "madaram komandos",
							  "madaram żołnierz",
							  "madaram znachor",
							  "madaram z toporem",
							  "madaram królowa",
							  "madaram bohater",
							  "madaram kawalerzysta",
							  "madaram myśliwy",
							  "madaram łamacz mieczy",
							  "madaram łucznik",
							  "madaram wietrzny wędrowca",
							  "kasarkutominubat"));
		/*
		 * exclude amazoness ( because they dont want to leave their island? )
		enemys.put("amazoness",
				Arrays.asList("amazoness archer",
						      "amazoness hunter",
						      "amazoness coastguard",
						      "amazoness archer commander",
						      "amazoness elite coastguard",
						      "amazoness bodyguard",
						      "amazoness coastguard mistress",
						      "amazoness commander",
						      "amazoness vigilance",
						      "amazoness imperator",
						      "amazoness giant"));
		 */
		enemys.put("oni",
				Arrays.asList("oni wojownik",
							  "oni łucznik",
							  "oni kapłan",
							  "oni król",
							  "oni królowa"));
		enemys.put("barbarzyńca",
				Arrays.asList("barbarzyńca",
						      "barbarzyńca wilczur",
						      "barbarzyńca elitarny",
						      "barbarzyńca kapłan",
						      "barbarzyńca szaman",
						      "barbarzyńca lider",
						      "król barbarzyńca"));
	}

	/**
	 * function for choosing random enemy from map
	 * @return - enemy forces caption
	 */
	protected String chooseRandomEnemys() {
		final List<String> enemyList = new LinkedList<String>(enemyForces.keySet());
		final int enemySize = enemyList.size();
		final int position  = Rand.rand(enemySize);
		return enemyList.get(position);
	}

	/**
	 * function returns difference between recorded number of enemy creatures
	 *     and currently killed creatures numbers.
	 * @param player - player for who we counting this
	 * @return - number of killed enemy creatures
	 */
	private int getKilledCreaturesNumber(final Player player) {
		int count = 0;
		String temp;
		int solo;
		int shared;
		int recsolo;
		int recshared;
		final String enemyType = player.getQuest(QUEST_SLOT,1);
		final List<String> monsters = Arrays.asList(player.getQuest(QUEST_SLOT,2).split(","));
		final List<String> creatures = enemys.get(enemyType);
		for(int i=0; i<creatures.size(); i++) {
			String tempName = creatures.get(i);
			temp = monsters.get(i*5+3);
			if (temp == null) {
				recsolo = 0;
			} else {
				recsolo = Integer.parseInt(temp);
			}
			temp = monsters.get(i*5+4);
			if (temp == null) {
				recshared = 0;
			} else {
				recshared = Integer.parseInt(temp);
			}

			temp = player.getKeyedSlot("!kills", "solo."+tempName);
			if (temp==null) {
				solo = 0;
			} else {
				solo = Integer.parseInt(temp);
			}

			temp = player.getKeyedSlot("!kills", "shared."+tempName);
			if (temp==null) {
				shared = 0;
			} else {
				shared = Integer.parseInt(temp);
			}

			count = count + solo - recsolo + shared - recshared;
		}
		return count;
	}


	class GiveQuestAction implements ChatAction {
		/**
		 * function will update player quest slot.
		 * @param player - player for which we will record quest.
		 */
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser speakerNPC) {
			final String monstersType = chooseRandomEnemys();
			speakerNPC.say("Potrzebuję pomocy, aby pokonać #wrogą  " + monstersType +
					" armię. Są poważnym zagrożeniem. Zabij co najmniej " + enemyForces.get(monstersType).first()+
					" każdego z "+ monstersType +
					" żołnierzy a wynagrodzę cię.");
			final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
			List<String> sortedcreatures = enemys.get(monstersType);
			player.setQuest(QUEST_SLOT, 0, "start");
			player.setQuest(QUEST_SLOT, 1, monstersType);
			for(int i=0; i<sortedcreatures.size(); i++) {
				toKill.put(sortedcreatures.get(i), new Pair<Integer, Integer>(0,0));
			}
			new StartRecordingKillsAction(QUEST_SLOT, 2, toKill).fire(player, sentence, speakerNPC);
		}
	}

	class RewardPlayerAction implements ChatAction {
		/**
		 * function will complete quest and reward player.
		 * @param player - player to be rewarded.
		 */
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser speakerNPC) {
			final String monsters = player.getQuest(QUEST_SLOT, 1);
			int killed=getKilledCreaturesNumber(player);
			int killsnumber = enemyForces.get(monsters).first();
			int moneyreward = 10000*(5*killed/killsnumber-1);
			if(killed == killsnumber) {
				// player killed no more no less then needed soldiers
				speakerNPC.say("Dobra robota! Oto zapłata. Gdy będziesz potrzebował znów pracy jako najemnik, wróć za tydzień. Moi zwiadowcy doniosą mi, gdy znowu ktoś odważy się nas zaatakować.");
			} else {
				// player killed more then needed soldiers
				speakerNPC.say("Bardzo dobrze! Zabiłeś "+(killed-killsnumber)+" extra "+
						Grammar.plnoun(killed-killsnumber, "soldier")+"! Weź te monety, i pamiętaj, życzę abyś za tydzień wrócił tu spowrotem!");
			}
			int karmabonus = 50*(2*killed/killsnumber-1);
			final StackableItem money = (StackableItem)
					SingletonRepository.getEntityManager().getItem("money");
			money.setQuantity(moneyreward);

			player.equipOrPutOnGround(money);
			player.addKarma(karmabonus);
		
		}
	}



	/**
	 * class for quest talking.
	 */
	class ExplainAction implements ChatAction {

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
				final String monsters = player.getQuest(QUEST_SLOT, 1);
				int killed=getKilledCreaturesNumber(player);
				int killsnumber = enemyForces.get(monsters).first();

				if(killed==0) {
					// player killed no creatures but asked about quest again.
					npc.say("Muszę Ci jeszcze raz objaśniać!! Chyba nie jesteś idiotą. Miałeś rozprawić się z #wrogą " + monsters + " armią!");
					return;
				}
				if(killed < killsnumber) {
					// player killed less then needed soldiers.
					npc.say("Zabiłeś tylko "+killed+" "+Grammar.plnoun(killed, player.getQuest(QUEST_SLOT, 1))+
							". Musisz zabić co najmniej "+killsnumber+" "+Grammar.plnoun(killed, player.getQuest(QUEST_SLOT, 1)));
					return;
				}

		}
	}

	/**
	 * class for quest talking.
	 */
	class FixAction implements ChatAction {

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
				//final String monsters = player.getQuest(QUEST_SLOT, 1);
			    Logger.getLogger(KillEnemyArmy.class).warn("Fixing malformed quest string of player <"+
				                                            player.getName()+
				                                            ">: ("+
				                                            player.getQuest(QUEST_SLOT)+
				                                            ")");
				npc.say("Przepraszam, ale nie zwracałem uwagi. " +
						"Co teraz potrzebuję:");
				new GiveQuestAction().fire(player, sentence, npc);
		}
	}


	/**
	 * add quest state to npc's fsm.
	 */
	private void step_1() {
		
		SpeakerNPC npc = npcs.get(QUEST_NPC);

		// quest can be given
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					new AndCondition(
						new QuestCompletedCondition(QUEST_SLOT),
						new TimePassedCondition(QUEST_SLOT, 1, delay))),
				ConversationStates.ATTENDING,
				null,
				new GiveQuestAction());

		// time is not over
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestCompletedCondition(QUEST_SLOT),
						new NotCondition(
								new TimePassedCondition(QUEST_SLOT, 1, delay))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, delay, "Musisz sprawdzić ponownie za"));

		// explanations
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("enemy", "wróg", "wrogą", "wroga"),
				new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							npc.say(enemyForces.get(player.getQuest(QUEST_SLOT, 1)).second());
						}
				});

		// explanations
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("enemy", "wróg", "wrogą", "wroga"),
				new QuestNotInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				"Tak, moi wrogowie są wszędzie, chcą mnie zabić! Myślę, że jesteś jednym z nich. Trzymaj się z dala ode mnie!",
				null);

		// update player's quest slot or blank it if failed...
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KillsQuestSlotNeedUpdateCondition(QUEST_SLOT, 1, enemys, true)),
				ConversationStates.ATTENDING,
				null,
				new FixAction());

		// checking for kills
		final List<String> creatures = new LinkedList<String>(enemyForces.keySet());
		for(int i=0; i<enemyForces.size(); i++) {
			final String enemy = creatures.get(i);

			  // player killed enough enemies.
		      npc.add(ConversationStates.ATTENDING,
		    		  ConversationPhrases.QUEST_MESSAGES,
		    		  new AndCondition(
		    				  new QuestInStateCondition(QUEST_SLOT, 1, enemy),
		    				  new KilledInSumForQuestCondition(QUEST_SLOT, 2, enemyForces.get(enemy).first())),
		    		  ConversationStates.ATTENDING,
		    		  null,
		    		  new MultipleActions(
		    				  new RewardPlayerAction(),
		    				  new IncreaseXPAction(100000),
		    				  new IncrementQuestAction(QUEST_SLOT,3,1),
		    				  // empty the 2nd index as we use it later
		    				  new SetQuestAction(QUEST_SLOT,2,""),
		    				  new SetQuestToTimeStampAction(QUEST_SLOT,1),
		    				  new SetQuestAction(QUEST_SLOT,0,"done")));

		      // player killed not enough enemies.
		      npc.add(ConversationStates.ATTENDING,
		    		  ConversationPhrases.QUEST_MESSAGES,
		    		  new AndCondition(
		    				  new QuestInStateCondition(QUEST_SLOT, 1, enemy),
		    				  new NotCondition(
		    						  new KilledInSumForQuestCondition(QUEST_SLOT, 2, enemyForces.get(enemy).first()))),
		    		  ConversationStates.ATTENDING,
		    		  null,
		    		  new ExplainAction());

		}
	}

	/**
	 * add quest to the Stendhal world.
	 */
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zabij Wrogą Armię",
				"Despot Halb Errvl poprosił mnie o zabicie kilku jego wrogów.",
				true);
		step_1();
	}

	/**
	 * return name of quest slot.
	 */
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	/**
	 * return name of quest.
	 */
	@Override
	public String getName() {
		return "KillEnemyArmy";
	}
	
	@Override
	public int getMinLevel() {
		return 80;
	}	
	
	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
	
 	@Override
 	public List<String> getHistory(final Player player) {
 		LinkedList<String> history = new LinkedList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return history;
		}
		
		if(player.getQuest(QUEST_SLOT, 0).equals("start")) {
	        final String givenEnemies = player.getQuest(QUEST_SLOT, 1);
	        final int givenNumber = enemyForces.get(givenEnemies).first(); 
	        // updating firstly
			if(new KillsQuestSlotNeedUpdateCondition(QUEST_SLOT, 2, enemys.get(givenEnemies), true).fire(player, null, null)) {
				// still need update??
			}
	        final int killedNumber = getKilledCreaturesNumber(player);
	        
			history.add("Despot Halb Errvl poprosił mnie o zabicie "+
					givenNumber+" "+
					Grammar.plnoun(givenNumber, givenEnemies));
			String kn = Integer.valueOf(killedNumber).toString();
			if(killedNumber == 0) {
				kn="no";
			}
			history.add("Aktualnie zabiłem "+
					kn+" "+
					Grammar.plnoun(killedNumber, givenEnemies));
			if(new KilledInSumForQuestCondition(QUEST_SLOT, 2, givenNumber).fire(player, null, null)) {
				history.add("Zabiłem wystaczająco dużo potworów, aby dostać moją nagrodę.");
			} else {
				int enemyleft = givenNumber - killedNumber;
				history.add("Zostało "+enemyleft+" "+
						Grammar.plnoun(enemyleft, givenEnemies)+" do zabicia.");	
			}
		}
		
		if(isCompleted(player)) {
			history.add("Ukończyłem zadanie Despot's Halb Errvl i otrzymałem moja nagrodę!");
		}	
		if (isRepeatable(player)) {
			history.add("Despot Halb Errvl dostaje znowu paranoi o swoim bezpieczeństwie. Mogę teraz zaoferować swoje usługi.");
		} 
		int repetitions = player.getNumberOfRepetitions(getSlotName(), 3);
		if (repetitions > 0) {
			history.add("Rozgromiłem "
					+ Grammar.quantityplnoun(repetitions, "całą armię") + " dla Despot Halb Errvl.");
		}
		return history; 
 	}

	@Override
	public String getNPCName() {
		return "Despot Halb Errvl";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
}

