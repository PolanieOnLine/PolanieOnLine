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

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Quest to buy ice cream for a little girl.
 * You have to get approval from her mother before giving it to her
 *
 * @author kymara
 *
 *
 * QUEST: Ice Cream for Annie
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Annie Jones (a little girl playing in Kalavan City Gardens) </li>
 * <li> Mrs Jones (Annie's mum) </li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Annie asks you for an icecream. </li>
 * <li> You buy icecream from Sam who is nearby. </li>
 * <li> Speak to Mrs Jones, Annie's mum. </li>
 * <li> Now give the icecream to Annie. </li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>a <item>present</item></li>
 * <li>500 XP</li>
 * <li>20 karma</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>Every 60 minutes</li>
 * </ul>
 */
public class IcecreamForAnnie extends AbstractQuest {

	// constants
	private static final String QUEST_SLOT = "icecream_for_annie";

	/** The delay between repeating quests. */
	private static final int REQUIRED_MINUTES = 60;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void icecreamStep() {
		final SpeakerNPC npc = npcs.get("Annie Jones");

		// first conversation with annie. be like [strike]every good child[/strike] kymara was when she was little and advertise name and age.
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES, 
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.ATTENDING, 
				"Cześć, nazywam się Annie i mam pięć lat.",
				null);

		// player is supposed to speak to mummy now
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES, 
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new PlayerHasItemWithHimCondition("lody")),
				ConversationStates.IDLE, 
				"Mamusia powiedziała, że nie powinnam z tobą rozmawiać. Jesteś nieznajomym.",
				null);
		
		// player didn't get ice cream, meanie
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES, 
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(new PlayerHasItemWithHimCondition("lody"))),
				ConversationStates.ATTENDING, 
				"Cześć. Jestem głodna.",
				null);
		
		// player got ice cream and spoke to mummy
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES, 
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "mummy"),
						new PlayerHasItemWithHimCondition("lody")),
				ConversationStates.QUESTION_1, 
				"Pychota! Czy ta porcja lodów jest dla mnie?",
				null);
		
		// player spoke to mummy and hasn't got ice cream
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES, 
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "mummy"),
						new NotCondition(new PlayerHasItemWithHimCondition("lody"))),
				ConversationStates.ATTENDING, 
				"Cześć. Jestem głodna.",
				null);
		
		// player is in another state like eating 
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES, 
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStartedCondition(QUEST_SLOT),
						new QuestNotInStateCondition(QUEST_SLOT, "start"),
						new QuestNotInStateCondition(QUEST_SLOT, "mummy")),
				ConversationStates.ATTENDING, 
				"Cześć.",
				null);
		
		// player rejected quest
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES, 
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.ATTENDING, 
				"Cześć.",
				null);
		
		// player asks about quest for first time (or rejected)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Jestem głodna! Chciałabym porcję lodów, proszę. Waniliowe z polewą czekoladową. Zdobędziesz takie dla mnie?",
				null);

		// shouldn't happen
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Więcej już nie zjem! Dziękuję!",
				null);
		
		// player can repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"), new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED, 
				"Mam nadzieję, że następna porcja lodów nie będzie obżarstwem. Czy możesz zdobyć je dla mnie?",
				null);	
		
		// player can't repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"), new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING, 
				"Zjadłam za dużo lodów. Niedobrze mi.",
				null);	
		
		// player should be bringing ice cream not asking about the quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestActiveCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"))),
				ConversationStates.ATTENDING,	
				"Łeeeeeeeee! Gdzie jest moja porcja lodów....?",
				null);

		// Player agrees to get the ice cream
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, 
				null,
				ConversationStates.ATTENDING, 
				"Dziękuję!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0));
		
		// Player says no, they've lost karma
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, 
				null, 
				ConversationStates.IDLE,
				"Dobrze. Zapytam moją mamusię.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
		
		// Player has got ice cream and spoken to mummy
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("lody"));
		reward.add(new EquipItemAction("prezent"));
		reward.add(new IncreaseXPAction(500));
		reward.add(new SetQuestAction(QUEST_SLOT, "eating;"));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT,1));
		reward.add(new IncreaseKarmaAction(10.0));
		reward.add(new InflictStatusOnNPCAction("lody"));
		
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, 
				new PlayerHasItemWithHimCondition("lody"),
				ConversationStates.ATTENDING, 
				"Dziękuję bardzo! Jesteś bardzo miły. Weź ten prezent.",
				new MultipleActions(reward));
		
		// player did have ice cream but put it on ground after question?
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, 
				new NotCondition(new PlayerHasItemWithHimCondition("lody")),
				ConversationStates.ATTENDING, 
				"Hej, gdzie jest moja porcja lodów?!",
				null);

		// Player says no, they've lost karma
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES, 
				null, 
				ConversationStates.IDLE,
				"Łeeeee! Jesteś wielkim, grubym łajdakiem.",
				new DecreaseKarmaAction(5.0));
	}
	
	private void meetMummyStep() {
		final SpeakerNPC mummyNPC = npcs.get("Mrs Jones");

		// player speaks to mummy before annie
		mummyNPC.add(ConversationStates.IDLE, 
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(mummyNPC.getName()),
							new QuestNotStartedCondition(QUEST_SLOT)),
					ConversationStates.ATTENDING, "Witaj miło Cię poznać.",
					null);

		// player is supposed to begetting icecream
		mummyNPC.add(ConversationStates.IDLE, 
					ConversationPhrases.GREETING_MESSAGES, 
					new AndCondition(new GreetingMatchesNameCondition(mummyNPC.getName()),
							new QuestInStateCondition(QUEST_SLOT, "start")),
					ConversationStates.ATTENDING, 
					"Cześć, widzę, że spotkałeś moją córkę Annie. Mam nadzieję, że nie była zbyt wymagająca. Wyglądasz na miłą osobę.",
					new SetQuestAction(QUEST_SLOT, "mummy"));

		// any other state
		mummyNPC.add(ConversationStates.IDLE, 
					ConversationPhrases.GREETING_MESSAGES,
					new GreetingMatchesNameCondition(mummyNPC.getName()), true,
					ConversationStates.ATTENDING, "Witaj ponownie.", null);
	}
	
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Porcja Lodów dla Annie",
				"Najlepszą niespodzianką dla małej dziewczynki jak Annie są zimne lody w letni dzień podczas zabawy na podwórku. Ale uważaj: zapytaj przedtem jej mamy o pozwolenie!",
				true);
		icecreamStep();
		meetMummyStep();
	}
	

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Annie Jones jest słodka dziewczynką grającą w ogrodach miasta Kalavan.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie lubię słodkich dziewczynek.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start","mummy") || isCompleted(player)) {
			res.add("Mała Annie chce lody.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start","mummy") && player.isEquipped("lody") || isCompleted(player)) {
			res.add("Znalazłem smaczne lody dla Annie.");
		}
        if ("mummy".equals(questState) || isCompleted(player)) {
            res.add("Rozmawiałem z panią Jones, zgodziła się mogę dać lody do córki.");        
        }
        if (isCompleted(player)) {
            if (isRepeatable(player)) {
                res.add("Dałem lody do Annie, ona dała mi prezent. Być może mam ochotę na jeszcze jeden prezent.");
            } else {
                res.add("Annie jest zajęta jedzeniem lodów, które jej dałem, a ona dała mi w zamian prezent.");
            }			
		}
		return res;
	}
	
	@Override
	public String getName() {
		return "IcecreamForAnnie";
	}

		// Getting to Kalavan is not too feasible till this level
	@Override
	public int getMinLevel() {
		return 10;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"eating;"),
				 new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"eating;").fire(player, null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.KALAVAN;
	}
	@Override
	public String getNPCName() {
		return "Annie Jones";
	}
}
