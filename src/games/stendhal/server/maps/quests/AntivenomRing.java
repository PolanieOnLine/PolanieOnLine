/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
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
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
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
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Antivenom Ring
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Jameson (the retired apothecary)</li>
 * <li>Other NPCs to give hints at location of apothecary's lab (undecided)</li>
 * <li>Another NPC that fuses the ring (undecided)</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Bring note to apothecary to Jameson.</li>
 * <li>As a favor to Klaas, Jameson will help you to strengthen your medicinal ring.</li>
 * <li>Bring Jameson a medicinal ring, venom gland, 2 mandragora and 5 fairycakes.</li>
 * <li>Jameson requires a bottle big enough to hold venom extracted from gland.</li>
 * <li>Bring Jameson a giant bottle.</li>
 * <li>Jameson realizes he doesn't have a way to extract the venom.</li>
 * <li>Find [NPC undecided] who will extract the venom into the giant bottle.</li>
 * <li>Take the bottle filled with venom back to Jameson.</li>
 * <li>Jameson concocts a mixture to infuse the ring.</li>
 * <li>Take mixture and ring to [NPC undecided] to be fused.</li>
 * <li>[NPC undecided] will also have requirements for the player.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>2000 XP</li>
 * <li>antivenom ring</li>
 * <li>Karma: 25???</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 * 
 * 
 * @author AntumDeluge
 */
public class AntivenomRing extends AbstractQuest {

	private static final String QUEST_SLOT = "antivenom_ring";
	
	//public static final String NEEDED_ITEMS = "pierścień leczniczy=1;gruczoł jadowy=1;mandragora=2;mufinka=5";
	
	/* Items taken to ??? to create cobra venom */
	public static final String EXTRACTION_ITEMS = "gruczoł jadowy=1;fiolka=1";
	
	/* Items taken to apothecary to create antivenom */
	public static final String MIX_ITEMS = "jad kobry=1;mandragora=2;mufinka=5";
	
	/* Items taken to ??? to create antivenom ring */
	public static final int REQUIRED_MONEY = 10000;
	public static final String FUSION_ITEMS = "antyjad=1;pierścień leczniczy=1";
	
	//private static final int REQUIRED_MINUTES = 30;
	
	private static final int EXTRACTION_TIME = 10;
	
	private static final int MIX_TIME = 10;
	
	private static final int FUSION_TIME = 30;
	
	// NPCs involved in quest
	private final SpeakerNPC mixer = npcs.get("Jameson");
	// FIXME: find NPCs for these roles
	private final SpeakerNPC extractor = npcs.get("");
	private final SpeakerNPC fuser = npcs.get("Hogart");
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Znalazłem aptekarza pustelnika w laboratorium w górach Semos.");
		final String[] questState = player.getQuest(QUEST_SLOT).split(",");
		if ("done".equals(questState)) {
			res.add("Znalazłem wszystko o co poprosił mnie Jameson. Użył specjalnej mikstury na moim pierścieniu, która zwiększyła odporność na truciznę. Dostałem także PD i karmę.");
		}
		else if ("rejected".equals(questState)) {
			res.add("Trucizna jest zbyt niebezpieczna. Nie chcę sobie zaszkodzić.");
		}
		else {
			if (questState[0].split("=")[0] == "mixing") {
				res.add(mixer.getName() + " sporządza antyjad.");
			}
			else {
				final ItemCollection missingMixItems = new ItemCollection();
				missingMixItems.addFromQuestStateString(questState[0]);
				res.add("Wciąż potrzebuję zanieść Jamesonowi " + Grammar.enumerateCollection(missingMixItems.toStringList()) + ".");
			}
			
			if (questState[1].split("=")[0] == "extracting") {
				res.add(extractor.getName() + " wyodrębnia jad.");
		}
			
			if (questState[2].split("=")[0] == "fusing") {
				res.add(fuser.getName() + " robi pierścień.");
			}
		}
		return res;
	}
	
	private void prepareHintNPCs() {
		final SpeakerNPC hintNPC1 = npcs.get("Valo");
		final SpeakerNPC hintNPC2 = npcs.get("Haizen");
		final SpeakerNPC hintNPC3 = npcs.get("Ortiv Milquetoast");
		
		// Valo is asked about an apothecary
		hintNPC1.add(ConversationStates.ATTENDING,
				Arrays.asList("apothecary", "aptekarz"),
				null,
				ConversationStates.ATTENDING,
				"Hmmm, tak znałem osobę dawno temu, która studiowała medycynę i antytrucizny. Ostatnio słyszałem, że na #emeryturę przeniósł się w góry.",
				null);
		
		hintNPC1.add(ConversationStates.ATTENDING,
				Arrays.asList("retreat", "retreats", "retreating", "emeryturę"),
				null,
				ConversationStates.ATTENDING,
				"Prawdopodobnie się ukrywa. Zwracaj uwagę na ukryte przejścia.",
				null);
		
		// Haizen is asked about an apothecary
		hintNPC2.add(ConversationStates.ATTENDING,
				Arrays.asList("apothecary", "aptekarz"),
				null,
				ConversationStates.ATTENDING,
				"Tak była jedna osoba w Kalavan, ale z powodu problemów z przywódcami został zmuszony do odejścia. Słyszałem, że #ukrywa się gdzieś w rejonie Semos.",
				null);
		
		hintNPC2.add(ConversationStates.ATTENDING,
				Arrays.asList("hide", "hides", "hiding", "hidden", "ukrywa", "ukryty", "ukrycia"),
				null,
				ConversationStates.ATTENDING,
				"Gdybym ja się ukrywał to na pewno zbudowałbym ukryty pokój z ukrytym wejściem.",
				null);
		
		// Ortiv Milquetoast is asked about an apothecary
		hintNPC3.add(ConversationStates.ATTENDING,
				Arrays.asList("apothecary", "aptekarz"),
				null,
				ConversationStates.ATTENDING,
				"Musisz porozmawiać z moim kolegą Jamesonem. Został on zmuszony do #ukrycia się z powodu problemów w Kalavan. Nie wiem gdzie, ale przynosi mi podczas każdej wizyty przepyszne gruszki.",
				null);
		
		hintNPC3.add(ConversationStates.ATTENDING,
				Arrays.asList("hide", "hides", "hiding", "hidden", "ukrywa", "ukryty", "ukrycia"),
				null,
				ConversationStates.ATTENDING,
				"Napomknął, że zbudował ukryte laboratorium i coś o ukrytych drzwiach.",
				null);
	}

	/**
	 * Quest starting point
	 */
	private void requestAntivenom() {
		// If player has note to apothecary then quest is offered
		mixer.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(mixer.getName()),
						new PlayerHasItemWithHimCondition("liścik do aptekarza"),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED, 
				"Oh wiadomość od Klaasa. Jest dla mnie?",
				null);
        
		// In case player dropped note before speaking to Jameson
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(mixer.getName()),
						new PlayerHasItemWithHimCondition("liścik do aptekarza"),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED, 
				"Oh wiadomość od Klaasa. Jest dla mnie?",
				null);
        
		// Player accepts quest
		mixer.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(new SetQuestAction(QUEST_SLOT, 0, MIX_ITEMS),
						new IncreaseKarmaAction(5.0),
						new DropItemAction("liścik do aptekarza"),
						new SayRequiredItemsFromCollectionAction(QUEST_SLOT, 0, "Klaas poprosił mnie o pomoc tobie. Mogę zrobić tobie pierścień, który zwiększy twoją odporność na truciznę. Musisz przynieść mi [items]. Masz jakiś ze sobą?")
				)
		);
		
		// Player tries to leave without accepting/rejecting the quest
		mixer.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.GOODBYE_MESSAGES,
				null,
				ConversationStates.QUEST_OFFERED,
				"To nie pytanie typu \"tak\" lub \"nie\". Powiedziałem czy ten liścik, który masz jest dla mnie?",
				null);
		
		// Player rejects quest
		mixer.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				// NPC walks away
				ConversationStates.IDLE,
				"Cóż zachowaj go.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
		
		// Player asks for quest without having Klass's note
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new PlayerHasItemWithHimCondition("liścik do aptekarza")),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Przykro mi, ale jestem teraz zajęty. Może mógłbyś porozmaiwać z #Klaasem.",
				null);
		
		// Player asks for quest after it is started
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStartedCondition(QUEST_SLOT),
						new QuestNotCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING, 
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Wciąż czekam, aż przyniesiesz mi [items]. Masz jakiś ze sobą?"));
		
		// Quest has previously been completed.
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1, 
				"Bardzo dziękuję. Minęło tak dużo czasu od kiedy jadłem mufinkę. Jesteś zadowolony z pierścienia?",
				null);
		
		// Player is enjoying the ring
		mixer.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Wspaniale!",
				null);
		
		// Player is not enjoying the ring
		mixer.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Oh to źle.",
				null);
		/*
        // Player asks about required items
		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("gland", "venom gland", "glands", "venom glands", "gruczoł jadowy"),
				null,
				ConversationStates.QUESTION_1,
				"Niektóre #węże mają gruczoł, w którym mieści się jad.",
				null);
		
		npc.add(ConversationStates.QUESTION_1,
				Arrays.asList("mandragora", "mandragoras", "root of mandragora", "roots of mandragora", "root of mandragoras", "roots of mandragoras"),
				null,
				ConversationStates.QUESTION_1,
				"To jest moje ulubione ziele i bardzo rzadkie. W Kalavan jest ukryta ścieżka wśród drzew, a na jej końcu znajdziesz to co szukasz.",
				null);
		*/
		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("cake", "fairy cake", "mufinka"),
				null,
				ConversationStates.QUESTION_1,
				"Oh one są najlepszym lekarstwem jakie mogłem spróbować. Tylko najbardziej anielskie istoty mogą zrobić tak anielskie jedzenie.",
				null);
		
		// Player asks about rings
		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("ring", "rings", "pierścień", "pierścienie"),
				null,
				ConversationStates.QUESTION_1,
				"Jest wiele rodzai pierścieni.",
				null);
		
		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("medicinal ring", "medicinal rings", "pierścień leczniczy"),
				null,
				ConversationStates.QUESTION_1,
				"Niektóre trujące potwory noszą go ze sobą.",
				null);
		
		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("antivenom ring", "antivenom rings", "pierścień antyjadowy", "pierścienie antyjadowy"),
				null,
				ConversationStates.QUESTION_1,
				"Jeżeli przyniesiesz mi to co potrzebuję to będę mógł wzmocnić #pierścień #leczniczy.",
				null);
		
		mixer.add(ConversationStates.QUESTION_1,
				Arrays.asList("antitoxin ring", "antitoxin rings", "gm antitoxin ring", "gm antitoxin rings", "pierścień antytoksyczny", "pierścienie antytoksyczne", "gm pierścień antytoksyczny", "gm pierścienie antytoksyczne"),
				null,
				ConversationStates.QUESTION_1,
				"Heh! Oto ostateczna ochroną przed zatruciami. Powodzenia w zdobyciu!",
				null);
		/*
		// Player asks about snakes
		npc.add(ConversationStates.QUESTION_1,
				Arrays.asList("snake", "snakes", "cobra", "cobras", "wąż", "węże", "kobra", "kobry"),
				null,
				ConversationStates.QUESTION_1,
				"Słyszałem najnowszą wieść, że odkryto jamę pełną węży gdzieś w Ados, ale nie sprawdzałem tego. Ten rodzaj pracy lepiej pozostawić podróżnikom.",
				null);
		
        // Player asks about required items
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("gland", "venom gland", "glands", "venom glands", "gruczoł", "jadowy", "gruczoł jadowy"),
				null,
				ConversationStates.ATTENDING,
				"Parę #węży posiada gruczoł jadowy, w których znajduje się jad.",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("mandragora", "mandragoras", "root of mandragora", "roots of mandragora", "root of mandragoras", "roots of mandragoras"),
				null,
				ConversationStates.ATTENDING,
				"To mój ulubiony z pośród wszystkich ziół i den z najrzadszych. Obok Kalavan jest ukryta ścieżka pomiędzy drzewami. Na jej końcu znajdziesz to czego szukasz.",
				null);
		*/
		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("cake", "fairy cake", "mufinka"),
				null,
				ConversationStates.ATTENDING,
				"Oh są one najlepszą przekąską jaką próbowałem. Tylko najbardziej niebiańskie istoty mogły zrobić tak nieziemskie jedzenie.",
				null);
		
		// Player asks about rings
		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("ring", "rings", "pierścień", "pierścienie"),
				null,
				ConversationStates.ATTENDING,
				"Jest wiele różnych typów pierścieni.",
				null);
		
		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("medicinal ring", "medicinal rings", "pierścień leczniczy", "pierścienie lecznicze"),
				null,
				ConversationStates.ATTENDING,
				"Parę jadowitych potworów ma je przy sobie.",
				null);
		
		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("antivenom ring", "antivenom rings", "pierścień antyjadowy", "pierścienie antyjadowe"),
				null,
				ConversationStates.ATTENDING,
				"Jeżeli przyniesiesz mi to co potrzebuję to będę mógł wzmocnić #pierścień #leczniczy.",
				null);
		
		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("antitoxin ring", "antitoxin rings", "gm antitoxin ring", "gm antitoxin rings", "pierścień antytoksynowy gm", "pierścienie antytoksynowe gm"),
				null,
				ConversationStates.ATTENDING,
				"Heh! To jest ostateczna ochrona przed trucizną. Powodzenia w zdobyciu jednego!",
				null);
		/*
		// Player asks about snakes
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("snake", "snakes", "cobra", "cobras", "wąż", "węże", "kobra", "kobry"),
				null,
				ConversationStates.ATTENDING,
				"Słyszałem najnowszą wieść, że odkryto jamę pełną węży gdzieś w Ados, ale nie sprawdzałem tego. Ten rodzaj pracy lepiej pozostawić podróżnikom.",
				null);
		*/
	}

	private void mixAntivenom() {
		// FIXME: Condition must apply to "mixing" state and anything afterward
		mixer.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(mixer.getName()), 
						new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(new QuestInStateCondition(QUEST_SLOT, 0, "mixing"))),
				ConversationStates.ATTENDING,
				"Witaj ponownie! Czy przyniosłeś #'przedmioty', o które prosiłem?",
				null);
		
		// player asks what is missing (says "items")
		mixer.add(ConversationStates.ATTENDING,
				Arrays.asList("item", "items", "ingredient", "ingredients", "przedmiot", "przedmioty", "składnik", "składniki"),
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Przyniosłeś coś?"));

		// player says has a required item with him (says "yes")
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.YES_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2,
				"Co przyniosłeś?",
				null);
		
		// Players says has required items (alternate conversation state)
		mixer.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2,
				"Co przyniosłeś?",
				null);
		
		// player says does not have a required item with him (says "no")
		mixer.add(ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Dobrze. Wciąż potrzebuję [items]"));
		
		// Players says does not have required items (alternate conversation state)
		mixer.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Dobrze. Daj znać, gdy coś znajdziesz.",
				null);//new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Okay. I still need [items]"));
		
		List<String> GOODBYE_NO_MESSAGES = new ArrayList<String>();
		for (String message : ConversationPhrases.GOODBYE_MESSAGES) {
			GOODBYE_NO_MESSAGES.add(message);
		}
		for (String message : ConversationPhrases.NO_MESSAGES) {
			GOODBYE_NO_MESSAGES.add(message);
		}		
		
		// player says "bye" while listing items
		mixer.add(ConversationStates.QUESTION_2,
				GOODBYE_NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Dobrze. Wciąż potrzebuję [items]"));
		
		// Returned too early; still working
		mixer.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(mixer.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "enhancing;"),
				new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, MIX_TIME))),
				ConversationStates.IDLE,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, MIX_TIME, "Jeszcze nie skończyłem pierścienia. Wróć później za "));
		
/*		// player says he didn't bring any items (says no)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Dobrze. Daj znać, gdy coś znajdziesz.", 
				null);

		// player says he didn't bring any items to different question
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Dobrze. Daj znać, gdy coś znajdziesz.", 
				null);
		*/
		// player offers item that isn't in the list.
		mixer.add(ConversationStates.QUESTION_2, "",
			new AndCondition(new QuestActiveCondition(QUEST_SLOT),
					new NotCondition(new TriggerInListCondition(MIX_ITEMS))),
			ConversationStates.QUESTION_2,
			"Nie wieżę, że prosiłem o to.", null);

		ChatAction mixAction = new MultipleActions (
		new SetQuestAction(QUEST_SLOT, 1, "mixing"),
		new SetQuestToTimeStampAction(QUEST_SLOT, 4),
		new SayTextAction("Dziękuję. Biorę się do pracy nad sprządzeniem antyjadu jak zjem parę mufinek. Wróć za " + MIX_TIME + " minutę.")
		);
		
		/* add triggers for the item names */
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(MIX_ITEMS);
		for (final Map.Entry<String, Integer> item : items.entrySet()) {
			mixer.add(ConversationStates.QUESTION_2,
					item.getKey(),
					new QuestActiveCondition(QUEST_SLOT),
					ConversationStates.QUESTION_2,
					null,
					new CollectRequestedItemsAction(
							item.getKey(),
							QUEST_SLOT,
							"Wspaniale! Masz coś jeszcze ze sobą?",
							"Już mi to przyniosłeś.",
							mixAction,
							ConversationStates.IDLE
							)
			);
		}

		final List<ChatAction> mixReward = new LinkedList<ChatAction>();
		//reward.add(new IncreaseXPAction(2000));
		//reward.add(new IncreaseKarmaAction(25.0));
		mixReward.add(new EquipItemAction("antyjad", 1, true));
		mixReward.add(new SetQuestAction(QUEST_SLOT, 1, "mixing=done"));

		mixer.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(mixer.getName()),
						new QuestInStateCondition(QUEST_SLOT, 1, "mixing"),
						new TimePassedCondition(QUEST_SLOT, 1, MIX_TIME)
				),
			ConversationStates.IDLE, 
			"Ukończyłem sporządzanie antyjadu. Teraz zjem resztę mufinek.", 
			new MultipleActions(mixReward));

	}
	
	private void requestCobraVenom() {
		// Player asks for antivenom
		extractor.add(ConversationStates.ATTENDING,
				Arrays.asList("jameson", "antivenom", "extract", "cobra", "venom", "sporządź", "antyjad"),
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(new QuestInStateCondition(QUEST_SLOT, 1, "extracting=done")
						)
				),
				ConversationStates.QUESTION_1,
				"To co potrzebujesz to trochę jadu do stworzenia antyjadu? Mogę wyodrębnić jad z gruczołu jadowego kobry, ale potrzebuję fiolki, aby w niej przechować. Czy mógłbyś przenieść mi ten przedmiot?",
				null);
		
		// Player will retrieve items
		extractor.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new MultipleActions(new SetQuestAction(QUEST_SLOT, 1, EXTRACTION_ITEMS),
						new SayRequiredItemsFromCollectionAction(QUEST_SLOT, 1, "Dobrze! Potrzebuję [items].  Masz jakiś ze sobą?")
				)
		);
	}
	
	private void extractCobraVenom() {
		
	}
	
	private void requestAntivenomRing() {
		// Greeting while quest is active
		fuser.add(
				ConversationStates.ATTENDING,
				Arrays.asList("jameson", "antivenom", "ring", "fuse", "antyjad", "pierścień"),
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(
								new QuestInStateCondition(QUEST_SLOT, 2, "fusing=done")
								)
						),
				ConversationStates.QUESTION_1,
				null,
				new MultipleActions(new SetQuestAction(QUEST_SLOT, 2, FUSION_ITEMS),
						new SayRequiredItemsFromCollectionAction(QUEST_SLOT, 2, "Potrzebujesz potężnego przedmiotu, aby chronił cię przed trucizną? Mogę połączyć pierścień leczniczy z antyjadem, aby wzmocnić pierścień, ale nie będzie to tanie. Potrzebuję [items]. Moja cena to " + Integer.toString(REQUIRED_MONEY) + ". Zdobędziesz to wszystko dla mnie?")
				)
		);
		
		// Player will retrieve items
		fuser.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new MultipleActions(new SetQuestAction(QUEST_SLOT, 2, EXTRACTION_ITEMS),
						new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "W porządku masz jakieś przedmioty, o które cię prosiłem?")
				)
		);
	}
	
	private void fuseAntivenomRing() {
		
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pierścień Antyjadowy",
				"Jak przysługę dla starego przyjaciela aptekarz Jameson wzmocni pierścień leczniczy.",
				false);
		prepareHintNPCs();
		requestAntivenom();
		mixAntivenom();
		requestCobraVenom();
		extractCobraVenom();
		requestAntivenomRing();
		fuseAntivenomRing();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "AntivenomRing";
	}

	public String getTitle() {
		return "AntivenomRing";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
	@Override
	public String getNPCName() {
		return "Jameson";
	}
}
