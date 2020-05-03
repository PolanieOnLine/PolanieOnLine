/***************************************************************************
 *                   (C) Copyright 2003-2014 - Stendhal                    *
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
import games.stendhal.server.entity.npc.action.CreateSlotAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EnableFeatureAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * QUEST: Hungry Joshua 
 * 
 * PARTICIPANTS: 
 * <ul>
 * <li> Xoderos the blacksmith in Semos</li>
 * <li> Joshua the blacksmith in Ados</li>
 * </ul>
 * 
 * STEPS: 
 * <ul>
 * <li> Talk with Xoderos to activate the quest.</li>
 * <li> Make 5 sandwiches.</li>
 * <li> Talk with Joshua to give him the sandwiches.</li>
 * <li> Return to Xoderos with a message from Joshua.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> 750 XP</li>
 * <li> Karma: 10</li>
 * <li> ability to use the keyring</li>
 * </ul>
 * 
 * REPETITIONS: 
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class HungryJoshua extends AbstractQuest {
	private static final int FOOD_AMOUNT = 5;

	private static final String QUEST_SLOT = "hungry_joshua";


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
		res.add("Zapytałem Xoderos w kuźni w Semos czy nie ma dla mnie jakiegoś zadania.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę pomagać Xoderosowi i Joshui.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "joshua", "done")) {
			res.add("Zgodziłem się na dostarczenie 5 kanapek do Joshui i powiedzenie mu, że mam jego 'jedzenie'.");
		}
		if (questState.equals("start") && player.isEquipped("kanapka",
				FOOD_AMOUNT)
				|| questState.equals("done")) {
			res.add("Mam pięć kanapek, które zabiorę do Joshui.");
		}
		if (questState.equals("joshua") || questState.equals("done")) {
			res.add("Wziąłem jedzenie do Joshui, a on poprosił mnie o przekazanie wiadomości jego bratu Xoderosowi, że wszystko u niego jest w porządku mówiąc 'Joshua'.");
		}
		if (questState.equals("done")) {
			res.add("Przekazałem wiadomość Xoderosowi, a on naprawił mój rzemyk.");
		}
		return res;
	}

	private void step_1() {

		final SpeakerNPC npc = npcs.get("Xoderos");
		
		/** If quest is not started yet, start it. */
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, "Martwię się o mojego brata, który mieszka w Ados. Potrzebuję kogoś kto przekaże mu #jedzenie.",
			null);
		
		/** In case quest is completed */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Mój brat ma jedzenia pod dostatkiem. Bardzo dziękuję.", null);

		/** In case quest is completed */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("food", "jedzenie"),
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Mój brat ma wystarczająco dużo kanapek. Dziękuję.", null);

		/** If quest is not started yet, start it. */
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("food","jedzenie"),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Sądzę, że pięć kanapek mu wystarczy. Mój brat nazywa się #Joshua. Czy możesz pomóc?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję. Powiedz mu #jedzenie lub #kanapka to będzie wiedział, że nie jesteś tylko klientem.",
			new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Pozwolisz, aby głodował! Mam nadzieję, że ktoś będzie bardziej wspaniałomyślny.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			"Joshua",
			null,
			ConversationStates.QUEST_OFFERED,
			"Jest złotnikiem w Ados. Nie mają tam zapasów jedzenia. Pomożesz mu?",
			null);

		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("food", "kanapka", "sandwiches", "jedzenie", "kanapki", "kanapka"), 
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"#Joshua będzie coraz głodniejszy! Proszę pospiesz się!", null);

		npc.add(ConversationStates.ATTENDING, "Joshua",
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Mój brat jest złotnikiem w Ados.", null);
		
		/** remind to take the sandwiches */
		npc.add(
			ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, 
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Proszę nie zapomnij o pięciu #kanapkach dla #Joshua!",
			null);
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Joshua");

		/** If player has quest and has brought the food, ask for it */
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("food", "kanapka", "sandwiches", "jedzenie", "kanapki"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("kanapka", FOOD_AMOUNT)),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Och wspaniale! Czy mój brat Xoderos przysłał Ciebie z tymi kanapkami?",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("kanapka", FOOD_AMOUNT));
		reward.add(new IncreaseXPAction(350));
		reward.add(new SetQuestAction(QUEST_SLOT, "joshua"));
		reward.add(new IncreaseKarmaAction(15));
		reward.add(new InflictStatusOnNPCAction("kanapka"));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES, 
			new PlayerHasItemWithHimCondition("kanapka", FOOD_AMOUNT),
			ConversationStates.ATTENDING,
			"Dziękuję! Proszę daj znać Xoderosowi, że ze mną jest wszystko w porządku. Powiedz moje imię Joshua, a będzie wiedział, że ja Ciebie przysłałem. Prawdopodobnie da ci coś w zamian.",
			new MultipleActions(reward));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES, 
			new NotCondition(new PlayerHasItemWithHimCondition("kanapka", FOOD_AMOUNT)),
			ConversationStates.ATTENDING, "Hej! Gdzie położyłeś te kanapki?", null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Och jestem taki głodny proszę powiedz #tak, że są dla mnie.",
			null);
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Xoderos");
		
		/** remind to complete the quest */
		npc.add(
			ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, 
			new QuestInStateCondition(QUEST_SLOT, "joshua"),
			ConversationStates.ATTENDING,
			"Mam nadzieję, że #Joshua ma się dobrze ....",
			null); 
		
		/** Remind player about the quest */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("food", "kanapka", "sandwiches", "jedzenie", "kanapki"), 
			new QuestInStateCondition(QUEST_SLOT, "joshua"),
			ConversationStates.ATTENDING,
			"Chciałbym, abyś mi przekazał, że #Joshua ma się dobrze ...", null);
			
		// ideally, make it so that this slot being done means
		// you get a keyring object instead what we currently
		// have - a button in the settings panel
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new IncreaseXPAction(400));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		if (System.getProperty("stendhal.container") != null) {
			reward.add(new CreateSlotAction(ImmutableList.of("belt", "back")));
			reward.add(new EquipItemAction("keyring", 1, true));
		} else {
			reward.add(new EnableFeatureAction("keyring"));	
		}
		/** Complete the quest */
		npc.add(
			ConversationStates.ATTENDING, "Joshua", 
			new QuestInStateCondition(QUEST_SLOT, "joshua"),
			ConversationStates.ATTENDING,
			"Jestem wdzięczny, że Joshua ma się dobrze. Co mogę dla Ciebie zrobić? Wiem. Naprawię to uszkodzone kółko od kluczy, które nosisz ... proszę, powinno już działać!",
			new MultipleActions(reward));
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Głodny Joshua",
				"Xoderos martwi się o swojego brata Joshuę, który mieszka w Ados, ponieważ miastu brakuje zapasów.",
				false);
		step_1();
		step_2();
		step_3();
	}
	@Override
	public String getName() {
		return "HungryJoshua";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Xoderos";
	}
}
