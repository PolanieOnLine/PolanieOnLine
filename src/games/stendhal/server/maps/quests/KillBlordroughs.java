/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/**
 * QUEST: KillBlordroughs
 *
 * PARTICIPANTS: <ul>
 * <li> Mrotho
 * <li> some creatures
 * </ul>
 *
 * STEPS:<ul>
 * <li> Mrotho asking you to kill 100 blordrough warriors.
 * <li> Kill them and go back to Mrotho for your reward.
 * </ul>
 *
 *
 * REWARD:<ul>
 * <li> 500k XP
 * <li> 50k moneys
 * <li> 100 karma for killing 100 creatures
 * <li> 50 karma for killing every 50 next creatures
 * </ul>
 *
 * REPETITIONS: <ul><li> once a week.</ul>
 */
public class KillBlordroughs extends AbstractQuest {
	private static KillBlordroughs instance;

	private static final String QUEST_NPC = "Mrotho";
	private static final String QUEST_SLOT = "kill_blordroughs";

	private final long questdelay = TimeUtil.MILLISECONDS_IN_WEEK;
	protected final int killsnumber = 100;

	private SpeakerNPC npc;

	private static Logger logger = Logger.getLogger(KillBlordroughs.class);

	protected static List<String> BLORDROUGHS = Arrays.asList(
           "blordrough kwatermistrz",
           "uzbrojony lider",
           "superczłowiek",
           "żołnierz blordrough",
           "elitarny żołnierz",
           "piechota blordrough",
           "kapitan blordrough",
           "generał blordrough");

	/**
	 * Get the static instance.
	 *
	 * @return
	 * 		KillBlordroughs
	 */
	public static KillBlordroughs getInstance() {
		if (instance == null) {
			instance = new KillBlordroughs();
		}

		return instance;
	}

	@Override
	public int getMinLevel() {
		return 114; // level of weakest blordrough
	}

	/**
	 * function returns list of blordrough creatures.
	 * @return - list of blordrough creatures
	 */
	protected LinkedList<Creature> getBlordroughs() {
		LinkedList<Creature> blordroughs = new LinkedList<Creature>();
		final EntityManager manager = SingletonRepository.getEntityManager();
		for (int i=0; i<BLORDROUGHS.size(); i++) {
			Creature creature = manager.getCreature(BLORDROUGHS.get(i));
			if (!creature.isAbnormal()) {
				blordroughs.add(creature);
			}
		}
		return blordroughs;
	}

	/**
	 * function checking if quest is active for player or no.
	 * @param player - player for who we will check quest state.
	 * @return - true if player's quest is active.
	 */
	private boolean questInProgress(final Player player) {
		if(player.getQuest(QUEST_SLOT)!=null) {
			return !player.getQuest(QUEST_SLOT,0).equals("done");
		}
		return false;
	}

	/**
	 * function decides, if quest can be given to player
	 * @param player - player for which we will check quest slot
	 * @param currenttime
	 * @return - true if player can get quest.
	 */
	private boolean questCanBeGiven(final Player player, final Long currenttime) {
		if (!player.hasQuest(QUEST_SLOT)) {
			return true;
		}
		if (player.getQuest(QUEST_SLOT, 0).equals("done")) {
			final String questLast = player.getQuest(QUEST_SLOT, 1);
			final Long time = currenttime -
				Long.parseLong(questLast);
			if (time > questdelay) {
				return true;
			}
		}
		return false;
	}

	/**
	 * function will return NPC answer how much time remains.
	 * @param player - chatting player.
	 * @param currenttime
	 * @return - NPC's reply string
	 */
	private String getNPCTextReply(final Player player, final Long currenttime) {
		String reply = "";
		String questLast = player.getQuest(QUEST_SLOT, 1);
		if (questLast != null) {
			final long timeRemaining = Long.parseLong(questLast) +
					questdelay - currenttime;

			if (timeRemaining > 0) {
				reply = "Proszę sprawdź za "
						+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
						+ ".";
			} else {
				// something wrong.
				reply = "Nie chcę decydować za ciebie.";
				logger.error("wrong time count	for player "+player.getName()+": "+
						"aktualny czas to "+currenttime+
						", czas ukończenia zadania to "+questLast,
						new Throwable());
			}
		}
		return reply;
	}

	/**
	 * function returns difference between recorded number of blordrough creatures
	 *     and currently killed creatures numbers.
	 * @param player - player for who we counting this
	 * @return - number of killed blordrough creatures
	 */
	private int getKilledCreaturesNumber(final Player player) {
		int count = 0;
		String temp;
		int solo;
		int shared;
		int recsolo;
		int recshared;
		final LinkedList<Creature> blordroughs = getBlordroughs();
		for(int i=0; i<blordroughs.size(); i++) {
			String tempName = blordroughs.get(i).getName();
			temp = player.getQuest(QUEST_SLOT, 1+i*2);
			if (temp == null) {
				recsolo = 0;
			} else if (temp.equals("")) {
				recsolo = 0;
			} else if (temp.startsWith("completed=")) {
				recsolo = 0;
			} else {
				recsolo = Integer.parseInt(temp);
			}
			temp = player.getQuest(QUEST_SLOT, 2+i*2);
			if (temp == null) {
				recshared = 0;
			} else if (temp.equals("")) {
				recshared = 0;
			} else if (temp.startsWith("completed=")) {
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

	/**
	 * function will update player quest slot.
	 * @param player - player for which we will record quest.
	 */
	private void writeQuestRecord(final Player player) {
		StringBuilder sb = new StringBuilder();
		LinkedList<Creature> sortedcreatures = getBlordroughs();
		sb.append("given");
		for (int i=0; i<sortedcreatures.size(); i++) {
			String temp;
			int solo;
			int shared;
			temp = player.getKeyedSlot("!kills", "solo."+sortedcreatures.get(i).getName());
			if (temp==null) {
				solo = 0;
			} else {
				solo = Integer.parseInt(temp);
			}

			temp = player.getKeyedSlot("!kills", "shared."+sortedcreatures.get(i).getName());
			if (temp==null) {
				shared = 0;
			} else {
				shared = Integer.parseInt(temp);
			}

			sb.append(";" + solo);
			sb.append(";" + shared);
		}

		sb.append(";completed=" + getCompletedCount(player));

		//player.sendPrivateText(sb.toString());
		player.setQuest(QUEST_SLOT, sb.toString());
	}

	/**
	 * function will complete quest and reward player.
	 * @param player - player to be rewarded.
	 * @param killed - number of killed creatures.
	 */
	private void rewardPlayer(final Player player, int killed) {
		int karmabonus = 50*(2*killed/killsnumber-1);
		final StackableItem money = (StackableItem) SingletonRepository.getEntityManager()
			.getItem("money");
		money.setQuantity(50000);

		player.setQuest(QUEST_SLOT, "done;" + System.currentTimeMillis() + ";completed=" + Integer.toString(getCompletedCount(player) + 1));
		player.equipOrPutOnGround(money);
		player.addKarma(karmabonus);
		player.addXP(500000);
	}

	/**
	 * Checks how many times the player has completed the quest.
	 *
	 * @param player
	 * 		Player to check.
	 * @return
	 * 		Number of times player has completed quest.
	 */
	@Override
	public int getCompletedCount(final Player player) {
		if (player.getQuest(QUEST_SLOT) != null) {
			final String[] slots = player.getQuest(QUEST_SLOT).split(";");

			final String temp = slots[slots.length - 1];
			if (temp.startsWith("completed=")) {
				return Integer.parseInt(temp.split("=")[1]);
			}

			// completion count was not previously tracked, so check if quest has been completed at least once
			if (slots[0].equals("done")) {
				return 1;
			}
		}

		return 0;
	}

	/**
	 * class for quest talking.
	 */
	class QuestAction implements ChatAction {
		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			if(questInProgress(player)) {
				int killed = getKilledCreaturesNumber(player);

				if(killed==0) {
					// player killed no creatures but asked about quest again.
					npc.say("Kazałem Ci zabić #'blordroughs', pamiętasz?");
					return;
				}
				if(killed < killsnumber) {
					// player killed less then needed soldiers.
					npc.say("Zabiłeś tylko "+killed+" blordrough "+Grammar.plnoun(killed, "żołnierzy")+".");
					return;
				}
				if(killed == killsnumber) {
					// player killed no more no less then needed soldiers
					npc.say("Dobra robota! Tu są pieniądze. Jeżeli podoba ci sią praca u mnie, powróć tu za tydzień. Myślę iż w ciągu tego czasu zbiorą ponownie swoją armię aby nas zaatakować.");
				} else {
					// player killed more then needed soldiers
					npc.say("Bardzo dobrze! Zabiłeś "+(killed-killsnumber)+" więcej "+
							Grammar.plnoun(killed-killsnumber, "żołnierzy")+"! Oto zapłata, ale  pamiętaj, że za tydzień możesz wykonać zadanie ponownie!");
				}
				rewardPlayer(player, killed);
			} else {
				final Long currtime = System.currentTimeMillis();
				if (questCanBeGiven(player, currtime)) {
					// will give quest to player.
					npc.say("Armia z Ados potrzebuje pomocy w walce z #'wojskami blordrough'. Są bardzo dokuczliwi. Zabij przynajmniej 100 blordrough żołnierzy, a otrzymasz nagrodę.");
					writeQuestRecord(player);
				} else {
					npc.say(getNPCTextReply(player, currtime));
				}
			}
		}
	}

	/**
	 * add quest state to npc's fsm.
	 */
	private void step_1() {
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new GreetingMatchesNameCondition(npc.getName()),
				false,
				ConversationStates.ATTENDING,
				"Pozdrawiam. Przyszedłeś zaciągnąć się do wojska?",
				null);
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.YES_MESSAGES,
				new GreetingMatchesNameCondition(npc.getName()),
				false,
				ConversationStates.ATTENDING,
				"Ha! Dobrze, dałbym Ci wtedy #'zadanie'...",
				null);
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new GreetingMatchesNameCondition(npc.getName()),
				false,
				ConversationStates.ATTENDING,
				"Ha! Cóż nie pozwolę Ci zapisać się do wojska, ale możesz nam #zaoferować jakąś zbroję...",
				null);
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("Blordrough","blordrough","blordroughs"),
				null,
				ConversationStates.ATTENDING,
				"Armia z Ados ma duże straty w walkach z żołnierzami blordrough. Podchodzą nas tunelami od strony Ados.",
				null);
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new GreetingMatchesNameCondition(npc.getName()),
				ConversationStates.ATTENDING,
				null,
				new QuestAction());

		// compatibility so players can say "done"
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				null,
				new QuestAction());
	}

	/**
	 * add quest to the Stendhal world.
	 */
	@Override
	public void addToWorld() {
		npc = npcs.get(QUEST_NPC);
		fillQuestInfo(
				"Pozbycie się Blordroughtów",
				"Mrotho chce, abyś pozbył się kilku żołnierzy Blordroughtów.",
				true);
		step_1();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final int completedCount = getCompletedCount(player);

		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
				return res;
		}
		res.add(player.getGenderVerb("Poznałem") + " Mrotho w barakach w mieście Ados.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.contains("done")) {
			res.add(player.getGenderVerb("Zabiłem") + " wszystkich żołnierzy blordroughs i za wsparcie " + player.getGenderVerb("otrzymałem") + " nagrodę od " + QUEST_NPC);
		} else {
			res.add(player.getGenderVerb("Zabiłem") + " " + Integer.toString(getKilledCreaturesNumber(player)) + " blordroughtów (muszę jeszcze zabić: " + Integer.toString(killsnumber) + " blordroughtów).");
		}

		if (completedCount > 0) {
			res.add("Pokonałem już " + Integer.toString(completedCount) + " armii blordrough.");
		}

        return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Pozbycie się Blordroughtów";
	}

	@Override
	public String getNPCName() {
		return "Mrotho";
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(
				new QuestCompletedCondition(QUEST_SLOT),
				new TimePassedCondition(QUEST_SLOT, 1, TimeUtil.MINUTES_IN_WEEK)).fire(player, null, null);
	}
}
