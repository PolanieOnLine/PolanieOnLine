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

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Mixture for Ortiv
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Ortiv Milquetoast, the retired teacher who lives in the Kirdneh River house</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Ortiv asks you for some ingredients for a mixture which will help him to keep the assassins and bandits in the cellar</li>
 * <li>Find the ingredients</li>
 * <li>Take the ingredients back to Ortiv</li>
 * <li>Ortiv gives you a reward</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>karma +35</li>
 * <li>5000 XP</li>
 * <li>a bounded assassin dagger</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 *
 * @author Vanessa Julius
 */
public class MixtureForOrtiv extends AbstractQuest {

	public static final String QUEST_SLOT = "mixture_for_ortiv";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "flasza=1;arandula=2;skrzydlica=10;kokuda=1;muchomor=12;lukrecja=2;jabłko=10;napój z winogron=30;czosnek=2;moździerz z tłuczkiem=1";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Ortiv Milquetoast emerytowanego nauczyciela w jego domku w Kirdneh River.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę teraz pomagać Ortivowi. Powinien sam poszukać składników.");
		} else if (!"done".equals(questState)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("Wciąż muszę przynieść dla Ortiv " + Grammar.enumerateCollection(missingItems.toStringList()) + ".");
		} else {
			res.add("Pomogłem dla Ortiv. Teraz może spać bezpiecznie w swoim łóżku. On nagrodził mnie podniesieniem XP i sztyletem mordercy.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Ortiv Milquetoast");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new LevelGreaterThanCondition(2),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
			ConversationStates.QUESTION_1, 
			"Oh obcy znalazł mój dom. Witaj! Możesz mi w czymś pomóc?", null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT,"rejected")),
			ConversationStates.QUEST_OFFERED, 
			"Hej czy myślałeś o ponownej pomocy? Zrobisz to?", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Aktualnie pracuję nad miksturą trzymającą szajkę tam na dole... Może później mi pomożesz w zdobyciu #składników, których potrzebuję.",
			null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Aktualnie pracuję nad miksturą trzymającą szajkę tam na dole... Może później mi pomożesz w zdobyciu #składników, których potrzebuję.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("ingredients", "składników", "składniki"),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Byłem nauczyciele alchemi, a teraz próbuję coś zmieszać razem. Potrzebuję składników na to i nadziei, że mi pomożesz. Pomożesz?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5.0),
							    new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Oh to wspaniale nieznajomy! Możliwe, że uratowałeś mi tym życie! Proszę przynieś mi te składniki: [items].")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Mysłałem, że mi pomożesz... Ale myliłem się, niestety... ",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.addReply("jabłko", "Jabłka są ulubionym przysmakiem zabójców. Widziałem parę jabłoni na wschód " +
				"Semos blisko Orril i Nalwor river.");

			npc.addReply("flasza", "Słyszałem o młodej kobiecie w Semos, która je sprzedaje.");

			npc.addReply("muchomor", "Toadstools są trujące. Słyszałem, że jacyś myśliwi raz je zjedli" +
					" i przez kilka dni byli chorzy.");

			npc.addReply("arandula", "Na północ od Semos, nie daleko wzgórza z drzewami rosną zioła zwane arandula. Powiedzieli mi to moi przyjaciele.");

			npc.addReply("skrzydlica","Skrzydlica jest ciężka do znalezienia...jest okryta białymi prążkami na przemian z czerwonymi, zielonymi " +
					" lub brązowymi. Słyszałem o miejscach w Faiumoni gdzie możesz ją złowić, ale uważaj. Każdy kolec jest trujący!");
			
			npc.addReply("kokuda","Kokuda ciężko jest znaleść. Byłbym wdzięczny, gdybyś zdobył jakąś z wyspy Athor...");

			npc.addReply("lukrecja", "To małe ciągotki, które w mieście magów sprzedaje młoda dziewczyna. Są wspaniałe i słodkie.");
			
			npc.addReply("napój z winogron", "Hmm nie ma nic lepszego niż połączenie dwóch rzeczy razem podczas delektowania się szklanką tego napoju *kaszlnięcie*, ale potrzebuję też mikstury... Założe się, że możesz kupić go w tawernie lub pubie...");
			
			npc.addReply("czosnek", "Znam zabójców i bandytów i nie są wampirami, ale staram się używać tego także przeciwko nim. Jest miła ogrodniczka w Kalavan City Gardens, która może sprzedać kilka główek jej hodowli.");
			
			npc.addReply(Arrays.asList("pestle","mortar","moździerz","moździerz z tłuczkiem"), "Może jakiś piekarz lub kucharz używa tego.");
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Ortiv Milquetoast");
	
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUESTION_2,
				"Witaj ponownie! Cieszę się, że cię widzę. Przyniosłeś #składniki do mojej mikstury?",
				null);
		
		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.QUESTION_2, Arrays.asList("ingredients", "składników", "składniki"), null,
				ConversationStates.QUESTION_2, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Przyniosłeś coś?"));

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "Wspaniale co jeszcze przynisłeś?",
				null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("Dziękuję bardzo! Teraz mogę zacząć mieszanie mikstury, która, miejmy nadzieję jest bezpieczna wewnątrz mojego własnego domu, bez morderców i bandytów, pochodzących z dołu. Poniżej znajduje się sztylet zabójcy dla Ciebie. Musiałam zabrać go jednemu z moich uczniów w klasie."),
				new IncreaseXPAction(5000),
				new IncreaseKarmaAction(25),
				new EquipItemAction("sztylet mordercy", 1 ,true)
				);
		/* add triggers for the item names */
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		for (final Map.Entry<String, Integer> item : items.entrySet()) {
			npc.add(ConversationStates.QUESTION_2, item.getKey(), null,
					ConversationStates.QUESTION_2, null,
					new CollectRequestedItemsAction(
							item.getKey(), QUEST_SLOT,
							"Cudownie! Przyniosłeś coś jeszcze ze sobą?", "Już przyniosłeś mi ten składnik.",
							completeAction, ConversationStates.ATTENDING));
		}

		/* player says he didn't bring any items (says no) */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dobrze muszę być trochę cierpliwy. Daj mi znać jeżeli będę mógł jakoś #pomóc.", 
				null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dobrze, muszę być trochę cierpliwy. Daj mi znać jeżeli będę mógł jakoś #pomóc.", null);

		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING, 
				"Dziękuję bardzo! Mogę teraz spać bezpiecznie! Uratowałeś mnie!", null);
	}
	

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Mikstura dla Ortiva",
				"Ortiv poprosił mnie o składniki do jego mikstury, która pomoże mu trzymać zabójców i bandytów w piwnicy.",
				true);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "MixtureForOrtiv";
	}

	public String getTitle() {
		
		return "Mikstua dla Ortiva";
	}

	@Override
	public String getNPCName() {
		return "Ortiv Milquetoast";
	}
	
	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}
}
