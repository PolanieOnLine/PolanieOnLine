package games.stendhal.server.maps.quests.mithrilring;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;

class SearchingSchemes {
	private MithrilRingInfo mithrilring;

	public SearchingSchemes(final MithrilRingInfo mithrilring) {
		this.mithrilring = mithrilring;
	}

	private final NPCList npcs = SingletonRepository.getNPCList();

	private void offerQuestStep() {
		final SpeakerNPC npc = npcs.get(MithrilRingInfo.NPC);

		final Map<String,Integer> items = new HashMap<String, Integer>();
			items.put("pierwsza część schematu",1);
			items.put("druga część schematu",1);
			items.put("trzecia część schematu",1);
			items.put("czwarta część schematu",1);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new OrCondition(
				new QuestNotStartedCondition(mithrilring.getQuestSlot()),
				new QuestInStateCondition(mithrilring.getQuestSlot(), "rejected")),				
			ConversationStates.QUEST_OFFERED, 
			"Chciałbyś może dowiedzieć się trochę o pewnym starym schemacie?",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			null,			
			new MultipleActions(
				new SetQuestAction(mithrilring.getQuestSlot(), "schemes;"),
				new StartRecordingRandomItemCollectionAction(mithrilring.getQuestSlot(), 1, items,
				"Dobrze... Możliwe, że cię to zainteresuje... Niedawno przeczytałem bardzo starą księgę, "
				+ "a w niej było coś na temat pewnego #pierścienia.")));
		
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Trudno... Może znajdzie się innych ochotnik, by skompletować części schematu...",
			new SetQuestAndModifyKarmaAction(mithrilring.getQuestSlot(), "rejected", -5.0));

		npc.addReply(Arrays.asList("pierścienia", "pierścień", "ring"),
				"Dokładnie mam na myśli #schemat, z którego można wykonać go.");

		npc.addReply(Arrays.asList("schemat", "schematy", "scheme", "schemes"),
				"Krasnoludy podzielili jeden cały schemat na cztery różne #części."
				+ "Jeśli zbierzesz je wszystkie to postaram się dla ciebie złożyć w jeden.");

		npc.addReply(Arrays.asList("części", "część", "part", "parts"),
				"Pierwszą część znajdziesz u #Mieczysława. Powiedz mu #schemat."
				+ "Jeśli się nie mylę, drugą jego część powinieneś znaleźć u #Czarknoksiężnika. "
				+ "Niestety, ale reszty nie jestem w stanie odczytać gdzie mogą się znajdować... "
				+ "W tych miejscach zostały powyrywane kartki... Popytaj się tego #maga.");

		npc.addReply(Arrays.asList("Mieczysław", "mieczysława"),
				"Znajdziesz go w Gdańskim muzeum. Wystarczy, że podejdziesz do niego i powiesz #schemat.");

		npc.addReply(Arrays.asList("maga", "mag", "czarnoksiężnik", "czarnoksiężnika"),
				"Zasiaduje w wieży w Zakopanem. Wystarczy, że podejdziesz do niego i powiesz #schemat.");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestCompletedCondition(mithrilring.getQuestSlot()),
			ConversationStates.ATTENDING, 
			"Nie znam więcej starych schematów... Wykonałeś już ten, który udało mi się odkryć.",
			null);
	}
	
	private void searchingStep() {
		final SpeakerNPC npc = npcs.get(MithrilRingInfo.FIRST_SCHEME_NPC);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("schemat", "schematy", "schemes"),
			new QuestInStateCondition(mithrilring.getQuestSlot(), "schemes"),				
			ConversationStates.ATTENDING,
			"Schemat? Jedynie co mamy w posiadaniu to tylko jedną część starego schematu na przedmiot."
			+ " Masz na myśli ten od krasnoludów?",
			null);
	}

	public void addToWorld() {
		offerQuestStep();
		searchingStep();
	}
}
