/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
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
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
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
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class PlaszczKapturka extends AbstractQuest {
	private static final String QUEST_SLOT = "plaszcz_kapturka";

	private static final int REQUIRED_MINUTES = 30; // 30 minut

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			if (player.isEquipped("płaszcz kapturka")) {
				res.add("Balbina opowiedziała mi o swoim marzeniu, aby zostać czerwonym kapturkiem");
			}
			return res;
		}
		res.add("Spotkałem Balbine wraz z przyjaciółmi w parku zabaw");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę pomagać Balbinie");
			return res;
		}
		res.add("Nie chcę pomagać Balbinie w sprawie płaszcza czarwonego kapturka");
		if (player.isEquipped("płaszcz kapturka") || isCompleted(player)) {
			res.add("Mam już płaszcz kapturka dla Balbiny");
		}
		if (isCompleted(player)) {
			res.add("Dałem Balbinie jej wymarzony płaszcz czerwonego kapturka.");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Balbina");

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Chciałabym zostać czerwonym kapturkiem tak jak w tych książkach! Tylko, że potrzebuję płaszcz czerwonego kapturka. Pomożesz?",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.IDLE, "Dziękuję! Porozmawiaj z moją mamą gdzie można dostać taki płaszcz, bo ja nie wiem.",
			new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.QUEST_OFFERED,
			"*płacz* Smutno mi teraz! :(",
			null);
	}

	private void step_2() {
		final SpeakerNPC mummyNPC = npcs.get("Amanda");

		mummyNPC.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(mummyNPC.getName()),
				new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING, "Cześć miło cię poznać.",
			null);

		mummyNPC.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(mummyNPC.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING, 
			"Oh, już spotkałeś moją córkę Balbine. Wyglądasz na miłą osobę. Moja córka zawsze marzyła zostać czerownym #'kapturkiem', ale brakuje jej czegoś.",
			new SetQuestAction(QUEST_SLOT, "mummy"));

		mummyNPC.addReply("kapturkiem", "Czytałeś książki dla dzieci o czerwonym kapturku? Moja córka potrzebuje #'płaszcz czerwonego kapturka', a ja nie mam czasu, aby przejść się do #'krawca'. Muszę pilnować swoje dziecko tutaj.");
		mummyNPC.addReply("płaszcz czerwonego kapturka", "To jest taki czerwony płaszcz, przynajmniej w wielu książkach jest tak opisywany.");
		mummyNPC.addReply("krawca", "Jeśli się go poprosi to może uszyje dla nas #'płaszcz czerwonego kapturka'. Znajduje się pod królewskim grodem, w chatce wraz z Rymarzem. Powiedz mu imię mojej córki to od razu będzie wiedział o co chodzi.");

		// any other state
		mummyNPC.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new GreetingMatchesNameCondition(mummyNPC.getName()),
			true,
			ConversationStates.ATTENDING, "Witaj ponownie.", null);
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Krawiec");

		npc.add(ConversationStates.ATTENDING, 
			"Balbina",
			new QuestInStateCondition(QUEST_SLOT, "mummy"),
			ConversationStates.QUEST_OFFERED,
			"Balbina to ta dziewczynka, która chce zostać czerwonym kapturkiem?",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.IDLE,
			"O to dobrze kojarzę. Rozumiem, że chciałaby, abym uszył dla niej płaszcz czerwonego kapturka, zgadza się? To przynieś mi trochę skóry czerwonego smoka, może być tak z 5 sztuk. Możesz ruszać.",
			new SetQuestAction(QUEST_SLOT, "krawiec"));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Nie? To mi się tylko zdawało.",
			null);
	}

	private void step_4() {
		final SpeakerNPC npc = npcs.get("Krawiec");

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("skóra czerwonego smoka",5));
		reward.add(new IncreaseXPAction(500));
		reward.add(new SetQuestAction(QUEST_SLOT, "sewing;"));
		reward.add(new IncreaseKarmaAction(10));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "krawiec"),
				new PlayerHasItemWithHimCondition("skóra czerwonego smoka",5)),
			ConversationStates.ATTENDING, 
			"Masz już tą skórę, o którą Cię poprosiłem?",
			null);

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "krawiec"),
				new NotCondition(new PlayerHasItemWithHimCondition("skóra czerwonego smoka",5))),
			ConversationStates.IDLE, 
			"Nie masz tego przy sobie o co Ciebie poprosiłem... Idź i wróć kiedy już to zdobędziesz.",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, "krawiec"),
			ConversationStates.IDLE,
			"Dobrze, to biorę się do pracy. Za jakieś 30 minut powinienem skończyć.",
			new MultipleActions(reward));

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.NO_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, "krawiec"),
			ConversationStates.ATTENDING,
			"Oby na pewno?",
			null);
	}

	private void step_5() {
		final SpeakerNPC npc = npcs.get("krawiec");

		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "sewing;"),
				new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
			ConversationStates.IDLE, 
			null,
			new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Jeszcze nie skończyłem szyć. Wróć za "));

		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "sewing;"),
				new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
			ConversationStates.ATTENDING, 
			"Właśnie skończyłem szyć! Proszę, oto płaszcz czerwonego kapturka. Możesz teraz pójść i zanieść Balbinie.", 
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final Item plaszcz = SingletonRepository.getEntityManager().getItem("płaszcz czerwonego kapturka");
					player.equipOrPutOnGround(plaszcz);
					player.addXP(1500);
					player.addKarma(10);
					player.setQuest(QUEST_SLOT, "balbina");
				};
			});
	}

	private void step_6() {
		final SpeakerNPC npc = npcs.get("Balbina");

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("płaszcz czerwonego kapturka"));
		reward.add(new IncreaseXPAction(5000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(20));

		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "balbina"),
				new PlayerHasItemWithHimCondition("płaszcz czerwonego kapturka")),
			ConversationStates.ATTENDING, 
			"O widzę, że udało Ci się zdobyć dla mnie ten płaszcz. Dziękuję! *uśmiech*",
			new MultipleActions(reward));

		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "balbina"),
				new NotCondition(new PlayerHasItemWithHimCondition("płaszcz czerwonego kapturka"))),
			ConversationStates.ATTENDING, 
			"*płacz* Miałeś przynieść mi płaszcz czerwonego kapturka. *płacz* :(",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Mogę spełnić w końcu swoje marzenie! :)", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Płaszcz Kapturka",
				"Balbina chce, abym zdobył dla niej czerwony płaszcz kapturka.",
				false);
		step_1();
		step_2();
		step_3();
		step_4();
		step_5();
		step_6();
	}

	@Override
	public String getName() {
		return "PlaszczKapturka";
	}

	@Override
	public String getNPCName() {
		return "Balbina";
	}

	@Override
	public String getRegion() {
		return Region.KRAKOW_CITY;
	}
}