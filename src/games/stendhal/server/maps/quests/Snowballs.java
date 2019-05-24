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

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Snowballs
 * <p>
 * PARTICIPANTS:
 * <li> Mr. Yeti, a creature in a dungeon needs help
 * <p>
 * STEPS:
 * <li> Mr. Yeti asks for some snow, and wants you to get 25 snowballs.
 * <li> You collect 25 snowballs from ice golems.
 * <li> You give the snowballs to Mr. Yeti.
 * <li> Mr. Yeti gives you 20 cod or perch.
 * <p>
 * REWARD: <li> 20 cod or perch <li> 50 XP <li> 20 karma in total
 * <p>
 * REPETITIONS: <li> Unlimited, but 2 hours of waiting is
 * required between repetitions
 */
 
public class Snowballs extends AbstractQuest {

	private static final int REQUIRED_SNOWBALLS = 25;
	
	private static final int REQUIRED_MINUTES = 120;

	private static final String QUEST_SLOT = "snowballs";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public boolean isCompleted(final Player player) {
		return player.hasQuest(QUEST_SLOT)
				&& !player.getQuest(QUEST_SLOT).equals("start") 
				&& !player.getQuest(QUEST_SLOT).equals("rejected");
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "start"), new QuestStartedCondition(QUEST_SLOT), new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES)).fire(player, null, null);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Poszedłem do jaskiń lodowych i spotkałem się z Panem Yeti.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chciałem tym razem pomóc Mr. Yeti, a on nazłość wysłał mnie w daleką podróż...");
			return res;
		}
		res.add("Mr. Yeti poprosił mnie, abym zebrał dla niego trochę śnieżek. Obiecałem mu to.");
		if ((player.isEquipped("śnieżka", REQUIRED_SNOWBALLS)) || isCompleted(player)) {
			res.add("Znalazłem trochę śnieżek, gdy zabiłem kilku lodowych golemów. ");
		}
		if (isCompleted(player)) {
			res.add("Sprawiłem, że Pan Yeti poczuł się szczęśliwy, gdy dałem mu śnieżki, o które prosił.");
		}
		if(isRepeatable(player)){
			res.add("Pan Yeti potrzebuje śnieżek ponownie!");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Mr. Yeti");

		// says hi without having started quest before
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witam nieznajomego! Czy widziałeś moją śnieżną rzeźbę? Czy możesz mi wyświadczyć #przysługę?",
				null);
		
		// says hi - got the snow yeti asked for 
		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new PlayerHasItemWithHimCondition("śnieżka", REQUIRED_SNOWBALLS)),
				ConversationStates.QUEST_ITEM_BROUGHT, 
				"Witam nieznajomego! Widzę, że masz śnieżki, o które pytałem. Czy te śnieżki są dla mnie?",
				null);

		// says hi - didn't get the snow yeti asked for 
		npc.add(ConversationStates.IDLE,
      ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(new PlayerHasItemWithHimCondition("śnieżka", REQUIRED_SNOWBALLS))),
				ConversationStates.ATTENDING, 
				"Wróciłeś? Nie zapomnij, że obiecałeś zebrać kupkę śnieżek dla mnie!",
				null);

		// says hi - quest was done before and is now repeatable
		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStartedCondition(QUEST_SLOT),
						new QuestNotInStateCondition(QUEST_SLOT, "start"),
						new TimePassedCondition(QUEST_SLOT, REQUIRED_MINUTES)),
				ConversationStates.ATTENDING,
				"Witam ponownie! Czy widziałeś moją śnieżną rzeźbę? Czy możesz znów wyświadczyć mi #przysługę?",
				null);

		// says hi - quest was done before and is not yet repeatable
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStartedCondition(QUEST_SLOT),
						new QuestNotInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, REQUIRED_MINUTES, "Mam wystarczającą ilość śniegu na moją rzeźbę. Dziękuję za pomoc! " 
						+ "Mogę zacząć nową za" ));

		// asks about quest - has never started it
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Uwielbiam tworzyć śnieżne rzeźby, ale śnieg w tej jaskini nie jest wystarczająco dobry. Pomożesz mi w zdobyciu kilku śnieżek? Potrzebuję w sumie dwadzieścia pięć.",
				null);
		
		// asks about quest but already on it
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Już mi obiecałeś, że przyniesiesz kilka śnieżek! Dwadzieścia pięć kulek pamiętaj ...",
			null);
		
		// asks about quest - has done it but it's repeatable now
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStartedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "start"), new TimePassedCondition(QUEST_SLOT, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Uwielbiam tworzyć śnieżne rzeźby, ale śnieg w tej jaskini nie jest wystarczająco dobry. Pomożesz mi w zdobyciu kilku śnieżek? Potrzebuję w sumie dwadzieścia pięć.",
				null);

		// asks about quest - has done it and it's too soon to do again
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStartedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "start"), new NotCondition(new TimePassedCondition(QUEST_SLOT, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				"Mam wystarczająco dużo śniegu, aby skończyć moją rzeźbę, ale dziękuję, że pytałeś.",
				null);

		// player is willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dobrze. Możesz dostać śnieżki z lodowych golemów w jaskini, ale bądź ostrożny, bo tam jest coś dużego! Wróć, gdy będziesz miał dwadzieścia pięć śnieżek.",
				new SetQuestAction(QUEST_SLOT, "start"));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Co ty tutaj robisz? Odejdź!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void prepareBringingStep() {

		final SpeakerNPC npc = npcs.get("Mr. Yeti");

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("śnieżka", REQUIRED_SNOWBALLS));
		reward.add(new IncreaseXPAction(50));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT));
		// player gets either cod or perch, which we don't have a standard action for
		// and the npc says the name of the reward, too
		reward.add(new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						String rewardClass;
						if (Rand.throwCoin() == 1) {
							rewardClass = "dorsz";
						} else {
							rewardClass = "okoń";
						}
						npc.say("Dziękuję! Weź trochę " + rewardClass + "! Nie lubię ich jeść.");
						final StackableItem reward = (StackableItem) SingletonRepository.getEntityManager().getItem(rewardClass);
						reward.setQuantity(20);
						player.equipOrPutOnGround(reward);
						player.addKarma(20.0);
						player.notifyWorldAboutChanges();
					}
				});

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES, 
			new PlayerHasItemWithHimCondition("śnieżka", REQUIRED_SNOWBALLS),
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(reward));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES, 
			new NotCondition(new PlayerHasItemWithHimCondition("śnieżka", REQUIRED_SNOWBALLS)),
			ConversationStates.ATTENDING, 
			"Hej! Gdzie położyłeś śnieżki?",
			null);

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Och Mam nadzieję, że szybko mi dostarczysz! Chciałbym skończyć moją rzeźbę!",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Śnieżki dla Mr. Yeti",
				"Mieszkańcy lodowego regionu w Faiumoni potrzebują twojej pomocy w uzbieraniu kilku śnieżek dla nich.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getName() {
		return "Snowballs";
	}

		// the djinns, ice golems and ice elementals on the way to yeti caves are quite dangerous
	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getNPCName() {
		return "Mr. Yeti";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_YETI_CAVE;
	}
	
}
