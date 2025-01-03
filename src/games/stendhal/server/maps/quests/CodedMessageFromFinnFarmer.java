/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToFutureRandomTimeStampAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.TimeReachedCondition;
import games.stendhal.server.entity.npc.condition.TriggerMatchesQuestSlotCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Coded Message from Finn Farmer
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Finn Farmer, a little boy playing in the backyard</li>
 * <li>George, a child in Ados park</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Finn Farmer tells you a coded message</li>
 * <li>Relay the coded message to George, who will tell you a new coded message as answer</li>
 * <li>Relay the answer to Finn Farmer</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>XP +500</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 *   <li>You can repeat it once per day.</li>
 * </ul>O
 * 
 * @author kymara, hendrik
 */
public class CodedMessageFromFinnFarmer extends AbstractQuest {
	private static final String QUEST_SLOT = "coded_message";
	private final SpeakerNPC npc = npcs.get("Finn Farmer");

	private static final int QUEST_INDEX_STATUS = 0;
	private static final int QUEST_INDEX_MESSAGE = 1;
	private static final int QUEST_INDEX_REPEATED = 2;
	private static final int QUEST_INDEX_TIME = 3;
	private static final int REQUIRED_MINUTES = 24 * 60;

	private String[][] TEMPLATES = new String[][] {
		new String[] {
			"Banan",
			"Jaskółka",
			"Słoń",
			"Pluszowy miś",
			"Księżyc",
			"Jowisz",
			"Delta"
		},

		new String[] {
			"leży w",
			"wstaje z",
			"opuścił",
			"wszedł",
			"lata nad",
			"idzie do",
			"śpi w"
		},

		new String[] {
			"palenisku.",
			"budynku.",
			"dziurze.",
			"mieście.",
			"statku.",
			"jaskini.",
			"lesie."
		},
	};

	/**
	 * generates a coded message
	 *
	 * @return a coded message
	 */
	String generateRandomMessage() {
		StringBuilder res = new StringBuilder();
		for (String[] element : TEMPLATES) {
			res.append(element[Rand.rand(element.length)]);
			res.append(" ");
		}
		return res.toString().trim();
	}

	/**
	 * prepare Finn Farmer to start the quest
	 */
	private void step1() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, QUEST_INDEX_STATUS, "deliver_to_george"),
				ConversationStates.ATTENDING,
				"Dziękuję, że zgodziłeś się przekazać George'owi tą wiadomość:",
				new SayTextAction("[quest.coded_message:1]"));


		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestNotActiveCondition(QUEST_SLOT),
						new NotCondition(
							new TimeReachedCondition(QUEST_SLOT, QUEST_INDEX_TIME))),
				ConversationStates.ATTENDING,
				"Może jutro będę miał kolejną wiadomość.",
				null);

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestNotActiveCondition(QUEST_SLOT),
						new TimeReachedCondition(QUEST_SLOT, QUEST_INDEX_TIME)),
				ConversationStates.QUEST_OFFERED,
				"Mam pilną wiadomość dla #George'#a! Jest naprawdę ważna! Ale moi rodzice nie pozwalają mi samemu na spacerować po Ados, dlatego że jestem dzieckiem! Czy mógłbyś dostarczyć mu tą wiadomość?",
				null);

		npc.add(ConversationStates.QUEST_OFFERED, 
				Arrays.asList("george", "georgowi"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Znajdź Tommiego. Może jest w Parku Ados. George nie będzie daleko. Czy mógłbyś dostarczyć mu tą wiadomość?",
				null);

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES,
				ConversationStates.IDLE,
				"Dobrze. W takim razie nie zdradzę ci żadnych sekretów.",
				new MultipleActions(
						new DecreaseKarmaAction(10),
						new SetQuestAction(QUEST_SLOT, QUEST_INDEX_STATUS, "rejected")
				));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.YES_MESSAGES,
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
					new CreateAndSayCodedMessage(),
					new SetQuestAction(QUEST_SLOT, QUEST_INDEX_STATUS, "deliver_to_george")
				));
	}

	/**
	 * let George accept the message and answer with a new coded message
	 */
	private void step2() {
		final SpeakerNPC npc = npcs.get("George");

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, QUEST_INDEX_STATUS, "deliver_to_george"),
				ConversationStates.ATTENDING,
				"Nie powinienem rozmawiać z obcymi, ale wyglądasz na takiego co ma coś ważnego do powiedzenia. Co to jest?",
				null);

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, QUEST_INDEX_STATUS, "deliver_to_finn"),
				ConversationStates.IDLE,
				"Dziękuję, że zgodziłeś się przekazać wiadomość Finnowi:",
				new SayTextAction("[quest.coded_message:1]"));

		npc.add(ConversationStates.ATTENDING, 
				"", 
				new AndCondition(
					new TriggerMatchesQuestSlotCondition(QUEST_SLOT, QUEST_INDEX_MESSAGE),
					new QuestInStateCondition(QUEST_SLOT, QUEST_INDEX_STATUS, "deliver_to_george")
				), 
				ConversationStates.IDLE,
				"Rzeczywiście jest całkiem interesujące. Proszę daj znać Finnowi:",
				new MultipleActions(
					new CreateAndSayCodedMessage(),
					new SetQuestAction(QUEST_SLOT, QUEST_INDEX_STATUS, "deliver_to_finn")
				));

		npc.add(ConversationStates.ATTENDING, 
				"", 
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, QUEST_INDEX_STATUS, "deliver_to_george"),
					new NotCondition(new TriggerMatchesQuestSlotCondition(QUEST_SLOT, 1)),
					new TriggerMightbeACodedMessageCondition()
				),
				ConversationStates.ATTENDING,
				"Och? To wszystko nie ma sensu!",
				null);
	}

	/**
	 * let Finn Farmer accept the answer
	 */
	private void step3() {
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, QUEST_INDEX_STATUS, "deliver_to_finn"),
				ConversationStates.QUEST_ITEM_BROUGHT,
				"I co powiedział George?",
				null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT, 
				"",
				new TriggerMatchesQuestSlotCondition(QUEST_SLOT, QUEST_INDEX_MESSAGE),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
					new SetQuestAction(QUEST_SLOT, QUEST_INDEX_STATUS, "done"),
					new IncrementQuestAction(QUEST_SLOT, QUEST_INDEX_REPEATED, 1),
					new SetQuestToTimeStampAction(QUEST_SLOT, QUEST_INDEX_TIME),
					new SetQuestToFutureRandomTimeStampAction(QUEST_SLOT, QUEST_INDEX_TIME, REQUIRED_MINUTES, REQUIRED_MINUTES),
					new IncreaseXPAction(500),
					new IncreaseKarmaAction(20),
					new SayTextAction("Dziękuję za powiedzenie George'owi!"),
					new SayTextAction("!me tańczę ze szczęścia."),
					new SayTextAction("To było naprawdę ważne!"),
					new SayTextAction("Jego odpowiedź jest naprawdę interesująca!"),
					new SayTextAction("Będę wyczekiwać!"),
					new SayTextAction("Może jutro będę miał kolejną wiadomość.")
				));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT, 
				"", 
				new AndCondition(
					new NotCondition(new TriggerMatchesQuestSlotCondition(QUEST_SLOT, QUEST_INDEX_MESSAGE)),
					new TriggerMightbeACodedMessageCondition()
				),
				ConversationStates.QUEST_ITEM_BROUGHT,
				"Och? To wszystko nie ma sensu!",
				null);
	}

	@Override
	public void addToWorld() {
		step1();
		step2();
		step3();
		fillQuestInfo(
			"Zaszyfrowana Wiadomość",
			"Finn Farmer ma ważną oraz tajną informację dla George'a.",
			false, QUEST_INDEX_REPEATED);
	}

	@Override
	public List<String> getHistory(Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(player.getGenderVerb("Rozmawiałem") + " w Ados z chłopcem o imieniu Finn Farmer.");
		res.add("Finn poprosił mnie o przekazanie George'owi tajnej wiadomości. Mogę go spotkać niedaleko jego psa Tommy w parku Ados.");

		final String questState = player.getQuest(QUEST_SLOT, 0);
		if (questState.equals("rejected")) {
			res.add("Odrzuciłem jego prośbę.");
			return res;
		}

		res.add(player.getGenderVerb("Zgodziłem") + " się na przekazanie wiadomości.");
		if (questState.equals("deliver_to_george")) {
			return res;
		}

		res.add("George przekazał mi tajną wiadomość, którą muszę dostarczyć Finnowi.");
		if (questState.equals("deliver_to_finn")) {
			return res;
		}
		
		res.add("Zakończyłem moją misję jako posłaniec. Finn Farmer prawie oszalał z radości, gdy przekazałem mu odpowiedź George'a.");
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Zaszyfrowana Wiadomość";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public boolean isRepeatable(Player player) {
		return new TimeReachedCondition(QUEST_SLOT, QUEST_INDEX_TIME).fire(player,null, null);
	}

	/**
	 * creates, stores and says a coded message
	 *
	 * @author hendrik
	 */
	public class CreateAndSayCodedMessage implements ChatAction {

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			String codedMessage = generateRandomMessage();
			player.setQuest(QUEST_SLOT, QUEST_INDEX_MESSAGE, codedMessage);
			npc.say(codedMessage);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof CreateAndSayCodedMessage;
		}

		@Override
		public int hashCode() {
			return -47;
		}

		@Override
		public String toString() {
			return "codedmessage!";
		}
	}

	/**
	 * does the sentence look like a possible coded message?
	 *
	 * @author hendrik
	 */
	public class TriggerMightbeACodedMessageCondition implements ChatCondition {

		@Override
		public boolean fire(Player player, Sentence sentence, Entity npc) {
			String originalText = sentence.getOriginalText();
			int counter = 0;
			for (int i = 0; i < originalText.length(); i++) {
				if (originalText.charAt(i) == ' ') {
					counter++;
				}
			}
			return counter >= 3;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof TriggerMightbeACodedMessageCondition;
		}

		@Override
		public int hashCode() {
			return -37;
		}

		@Override
		public String toString() {
			return "codedmessage?";
		}
	}
}
