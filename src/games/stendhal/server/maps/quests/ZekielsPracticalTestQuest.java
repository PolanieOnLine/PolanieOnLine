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
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.Direction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Zekiels practical test
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Zekiel, guardian of the wizard's tower </li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Zekiel the guardian asks you to bring him 6 beeswax and 2 iron to make magic candles with. </li>
 * <li> Bring the items to Zekiel. </li>
 * <li> You can start the practical test. </li>
 * <li> Zekiel informs you about the test and wizards. </li>
 * <li> You will be send to 6 levels now at which you have to choose the right creature. </li>
 * <li> If you made the right choices, you'll be able to reach the spire everytime you want. </li>  
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> 9,000 XP total </li>
 * <li> some karma (20 total) </li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> None </li>
 * </ul>
 */
public class ZekielsPracticalTestQuest extends AbstractQuest {

	private static final int REQUIRED_IRON = 2;

	private static final int REQUIRED_BEESWAX = 6;

	private static final String QUEST_SLOT = "zekiels_practical_test";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void prepareQuestOfferingStep() {
		final SpeakerNPC npc = npcs.get("Zekiel the guardian");

		// player asks about quest when he has not started it
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Na  początek potrzebujesz 6 magicznych świec. Dostarcz mi 6 plastrów #wosku #pszczelego oraz 2 sztabki #żelaza, " +
				"a wyczaruję je na test praktyczny.",
				new SetQuestAction(QUEST_SLOT,"start"));
		
		// player asks about quest when he has already completed it
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Podjąłeś się praktycznego testu i masz wolny dostęp do wieży. #Teleportuję cię " +
			"do wieży. W czym jeszcze mogę Ci #pomóc?",
			null);
		
		// player asks about quest when he is in the initial bringing candles stage
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING, 
				"Nie posiadasz #składników potrzebnych do stworzenia magicznych świec.",
				null);

		// player asks about quest when he is in the practical test stage
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "candles_done"),
				ConversationStates.ATTENDING, 
				"Nie ukończyłeś testu praktycznego. Możesz #rozpocząć go lub możesz nauczyć się " +
				"więcej o #czarodziejach nim zaczniesz.",
				null);
		
		// we should only answer to these ingredients questions if the candles stage is not yet done
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("beeswax", "wosk pszczeli", "wosk", "pszczeli"),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING, 
			    "Potrzebny jest do wyczarowania magicznych świec. Mój znajomy pszczelarz ma pasiekę niedaleko Kalavan i u niego dostaniesz go najszybciej.",
			    null);
		
		// we should only answer to these ingredients questions if the candles stage is not yet done
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("iron", "żelazo", "żelazo", "żelaza"), 
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING, 
				"Podstawa świecy będzie z tego zrobiona. Zgłoś się do kowala w Zakopanem lub Semos w sprawie żelaza.",
				null);
		
		// we should only answer to these ingredients questions if the candles stage is not yet done
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("ingredients", "składników", "składnik", "ingredients", "skladnik"), 
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING, 
				"Potrzebujesz 6 plastrów #wosku #pszczelego oraz 2 sztaby #żelaza aby wyczarować 6 magicznych świec.",
				null);
	}

	private void bringItemsStep() {
		final SpeakerNPC npc = npcs.get("Zekiel the guardian");

		// player returns with iron but no beeswax
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT,"start"),
					new NotCondition(new PlayerHasItemWithHimCondition("wosk pszczeli",REQUIRED_BEESWAX)),
					new PlayerHasItemWithHimCondition("żelazo",REQUIRED_IRON)),
			ConversationStates.ATTENDING, 
			"Witaj, widzę, że zdobyłeś żelazo, ale wciąż potrzebujesz 6 plastrów wosku pszczelego. Wróć kiedy zdobędziesz " +
			"wszystkie #składniki.", 
			null);

		// player returns with beeswax but no iron
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT,"start"),
					new NotCondition(new PlayerHasItemWithHimCondition("żelazo",REQUIRED_IRON)),
					new PlayerHasItemWithHimCondition("wosk pszczeli",REQUIRED_BEESWAX)),
			ConversationStates.ATTENDING, 
			"Witaj, widzę, że zdobyłeś wosk pszczeli, ale wciąż brakuje ci 2 sztabek żelaza. Wróć kiedy zdobędziesz " +
			"wszystkie #składniki.", 
			null);
		
		//player returns with beeswax and iron
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT,"start"),
					new PlayerHasItemWithHimCondition("żelazo",REQUIRED_IRON),
					new PlayerHasItemWithHimCondition("wosk pszczeli",REQUIRED_BEESWAX)),
			ConversationStates.ATTENDING,
			"Witaj. Wreszcie przyniosłeś mi wszystkie składniki, które potrzebuję to wyczarowania magicznych świec. Teraz"+
			"możliwe jest #rozpoczęcie praktycznego testu.",
			new MultipleActions(
					new SetQuestAction(QUEST_SLOT,"candles_done"),
					new DropItemAction("wosk pszczeli", 6),
					new DropItemAction("żelazo", 2),
					new IncreaseXPAction(4000),
					new IncreaseKarmaAction(10)));

		// player returned after climbing the tower partially. reset status to candles done and start again
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new OrCondition(
								new QuestInStateCondition(QUEST_SLOT,"first_step"),
								new QuestInStateCondition(QUEST_SLOT,"second_step"),
								new QuestInStateCondition(QUEST_SLOT,"third_step"),
								new QuestInStateCondition(QUEST_SLOT,"fourth_step"),
								new QuestInStateCondition(QUEST_SLOT,"fifth_step"),
								new QuestInStateCondition(QUEST_SLOT,"sixth_step"),
								new QuestInStateCondition(QUEST_SLOT,"last_step"))),
			ConversationStates.ATTENDING, 
			"Witaj! Nie zdałeś egzaminu praktycznego. Powiedz mi, jeśli chcesz go teraz #powtórzyć " +
			"albo jeśli chcesz się najpierw czegoś #dowiedzieć.",
			new SetQuestAction(QUEST_SLOT, "candles_done"));
	}

	private void practicalTestStep() {
		final SpeakerNPC npc = npcs.get("Zekiel the guardian");

		// player returns after bringing the candles but hasn't tried to climb tower
		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT,"candles_done")),
			ConversationStates.ATTENDING, 
			"Przypuszczam, że wróciłeś, by #rozpocząć praktyczny test.",
			null);

		// player asks to start the practical part of the quest
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("start", "powtórzyć", "rozpocząć", "rozpoczęcie", "wysłać", "rozpocznij"), 
			new QuestInStateCondition(QUEST_SLOT,"candles_done"),
			ConversationStates.ATTENDING, 
			"Najpierw należy #poznać kilka ważnych rzeczy dotyczących testu i czarodziejów. " +
			"Mogę #wysłać Cię, abyś zrealizował pierwszy etap testu, jeśli jesteś gotowy.",
			null);

		// player wants to know how the practical quest works
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("know", "learn", "poznać", "dowiedzieć"),
			new QuestInStateCondition(QUEST_SLOT,"candles_done"),
			ConversationStates.ATTENDING, 
			"Na każdym etapie znajdziesz na północy, południu, wschodzie i zachodzie celę, w której będzie czekać na Ciebie powtór. " +
			" Wybierz istotę, która będzie powiązana z domeną #czarodziejów oraz z historią, za pomocą magicznego miejsca" +
			" pomiędzy dwoma posoągami czarnoksiężnika przed celą. Nie martw się, nie będziesz musiał walczyć z tą istotą," +
			" którą wybierzesz. Jeśli Twój wybór będzie rozdąsny, wyczaruję dla Ciebie magiczne świece, jeśli nie, zostaniesz teleportowany" +
			" spowrotem do mnie. Użyj świecy na błyszczącym rogu heksagramu i tym samym ukończ etap. Jeśli chcesz" +
			" zrezygnować z praktycznego testu, wystarczy użyć magicznego przejścia w środku heksagramu." +
			" Jeśli uważasz, że jesteś gotowy, to czas Cię #wysłać na start początek etapu.",
			null);
		
		// player asks about wizards: give him a parchment of information. 
		// this overrides the normal answer to wizards if the player is in the correct quest slot
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("wizards", "czarodziejów", "czarodziejach"),
				new QuestInStateCondition(QUEST_SLOT,"candles_done"),
				ConversationStates.ATTENDING, 
				"Weź ten pergamin ze wskazówkami o siedmiu czarodziejach. Będą one potrzebne we wszystkich etapach, do których Cię #wyślę. " +
				"Wysłuchaj uważnie mojej wiadomości z informacją, z jaką domeną jesteś wprowadzony do każdego etapu albo nie będziesz umiał dokonać prawdiłowego wyboru.",
				new ExamineChatAction("wizards-parchment.png", "Parchment", "The wizards circle"));

		// incase the player still has candles, remove them from him
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("send", "wyślę", "wyślij"), 
			new AndCondition(
					new QuestInStateCondition(QUEST_SLOT,"candles_done"),
					new PlayerHasItemWithHimCondition("świeca")),
			ConversationStates.ATTENDING, 
			"Zanim wyślę Cię do pierwszego etapu, musisz wyrzucić wszystkie świecie, jakie masz przy sobie.",
			null);

		// send the player, so long as he doesn't not have candles, and record which step he is on
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("send", "wyślę", "wyślij"),
			new AndCondition(
					new QuestInStateCondition(QUEST_SLOT,"candles_done"),
					new NotCondition(new PlayerHasItemWithHimCondition("świeca"))),
			ConversationStates.IDLE, 
			null,
			new MultipleActions(
					new SetQuestAction(QUEST_SLOT, "first_step"),
					new TeleportAction("int_semos_wizards_tower_1", 15, 16, Direction.DOWN)));
	}

	private void finishQuestStep() {
		
		// NOTE: this is a different NPC from Zekiel the guardian used above. This one 'finishes' the quest
		// and is in int_semos_wizards_tower_7, not the basement.
		final SpeakerNPC npc = npcs.get("Zekiel");

		// player got to the last level of the tower
		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT,"last_step")),
			ConversationStates.ATTENDING, 
			"Bardzo dobrze łowco przygód! Zdałeś praktyczny test. Teraz możesz wchodzić do wieży, kiedy tylko zechcesz.",
			new MultipleActions(
				new IncreaseXPAction(5000),
				new IncreaseKarmaAction(10),
				new SetQuestAction(QUEST_SLOT, "done")));
	}

	private void questFinished() {
		
		// this is the basement level normal Zekiel the guardian again
		final SpeakerNPC npc = npcs.get("Zekiel the guardian");

		// player returns having completed the quest
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Witaj podróżniku, jak mogę Ci teraz #pomóc ?",
			null);
		
		// player asks for help, having completed the quest
		npc.add(ConversationStates.ATTENDING, 
			ConversationPhrases.HELP_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Mogę #teleportować Cię do #wieży. Jestem również magazynierem #sklepu w wieży #czarodziejów.",
			null);

		// player asks about the store, having completed the quest
		npc.add(ConversationStates.ATTENDING,Arrays.asList
			("storekeeper", "sklepu", "sklep"),
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Sklep jest na piętrze u szczytu wieży. Będę tutaj, gdybyś zechciał tam wejść. ",
			null);

		// send a player who has completed the quest to the top spire
		npc.add(ConversationStates.ATTENDING,Arrays.asList
			("teleport", "teleportować"),
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.IDLE, null,
			new TeleportAction("int_semos_wizards_tower_8", 21, 22, Direction.UP));

		// player who has completed quest asks about the tower or test, offer the teleport or help
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("tower", "test", "wieży"),
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Zdałeś już egzamin praktyczny i możesz swobonie zwiedzać wieżę. Mogę #teleportować Cię do wierzy lub #pomóc Ci w inny sposób?",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Praktyczny Test Zekiela",
				"Zekiel, strażnik magicznej wieży, chce uczyć cię na temat domen czarodziejów i ich historii.",
				true);
		
		prepareQuestOfferingStep();
		bringItemsStep();
		practicalTestStep();
		finishQuestStep();
		questFinished();
	}

	@Override
	public List<String> getHistory(final Player player) {
		LinkedList<String> history = new LinkedList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return history;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		history.add("Wpuszczam do wieży Kręgu Czarodziejów. Zekiel, strażnik tego miejsa, zapytał mnie o przedmioty potrzebne do zrobienia magicznych świec.");
		if (questState.equals("start") && player.isEquipped("wosk pszczeli", REQUIRED_BEESWAX) && player.isEquipped("żelazo", REQUIRED_IRON)
				|| questState.equals("candles_done") || questState.endsWith("_step") || questState.equals("done")) {
			history.add("Zbieram plastry wosku pszczelego i żelazo potrzebne do zrobienia magicznych świec.");
		}
		if (questState.endsWith("_step")) {
			history.add("Doszedłem do " + questState.replace("_", " ") + " etapu testu w Wieży Czarodziejów.");
		}
		if (questState.equals("done")) {
			history.add("Zdałem test praktyczny i mogę wejść do wieży lub skorzystać ze usług sklepu.");
		}
		return history;
	}

	@Override
	public String getName() {
		return "ZekielsPracticalTest";
	}

	@Override
	public String getNPCName() {
		return "Zekiel the guardian";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
	
	@Override
	public int getMinLevel() {
		return 30;
	}
}
