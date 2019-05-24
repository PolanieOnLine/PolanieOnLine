/***************************************************************************
 *                   (C) Copyright 2003-2016 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.revivalweeks;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestToYearAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestSmallerThanCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TriggerExactlyInListCondition;
import games.stendhal.server.maps.ados.rosshouse.LittleGirlNPC;

public class FoundGirl implements LoadableContent {
	private SpeakerNPC npc;
	private ChatCondition noFriends;
	private ChatCondition anyFriends;
	private ChatCondition oldFriends;
	private ChatCondition currentFriends;

	private void buildConditions() {
		noFriends = new QuestNotStartedCondition("susi");
		anyFriends = new QuestStartedCondition("susi");
		oldFriends = new OrCondition(
				new QuestInStateCondition("susi", "friends"),
				new QuestSmallerThanCondition("susi", Calendar.getInstance().get(Calendar.YEAR)));
		currentFriends = new QuestInStateCondition("susi", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
	}

	final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_semos_frank_house");

	private void createGirlNPC() {

		npc = new SpeakerNPC("Susi") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 17));
				nodes.add(new Node(4, 27));
				nodes.add(new Node(7, 27));
				nodes.add(new Node(7, 17));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				// done outside
			}
		};

		//	npcs.add(npc);
		npc.setOutfit(new Outfit(0, 4, 7, 32, 13));
		npc.setPosition(4, 17);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setSpeed(1.0);
		zone.add(npc);
	}

	private void addDialog() {

		// greeting
		addGreetingDependingOnQuestState();

		npc.addJob("Jestem dziewczynką czekającą na mojego tatę, aby zabrał mnie do domu. Świetnie się bawiliśmy na #Mine #Town #Revival #Weeks!");
		npc.addGoodbye("Miłej zabawy!");
		npc.addHelp("Baw się dobrze.");
		npc.addOffer("Mogę zaoferować moją #przyjaźń.");

		// Revival Weeks
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("Mine", "Town", "Revival", "Weeks", "Mine Town", 
					"Mine Town Revival", "Mine Town Revival Weeks", "Mine Town", "Revival Weeks"),
			ConversationStates.ATTENDING,
			"Podczas Revival Weeks #świętujemy stary i prawie martwy Mine Town. "
			+ "Kilka lat temu festyn został odwołany poniewż mieszkańcy Ados szukali mnie, gdy się zgubiłam. "
			+ "Teraz, gdy się odnalazłam to możemy znowu świętować!",
			null);
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("celebrate", "celebration", "party", "świętujemy"),
			ConversationStates.ATTENDING,
			"Możesz zdobyć kostium od Saskia na zewnątrz tego domku lub spróbować rozwiązać trudną zagadkę w innym domku, albo zagrać w grę Tic Tac Toe z #przyjaciółmi.",
			null);

		// friends
		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("friend", "friends", "friendship", "przyjaźń", "przyjaciółmi", "przyjaciela"),
			new QuestInStateCondition("susi", Integer.toString(Calendar.getInstance().get(Calendar.YEAR))), 
			ConversationStates.ATTENDING,
			"Dziękuję za bycie moim przyjacielem.", null);

		addFirstQuest();
		addSecondQuest();

		// quest
		addQuest();
	}


	private void addGreetingDependingOnQuestState() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						noFriends),
				ConversationStates.ATTENDING,
				"Zgadnij mamy następny #Mine #Town #Revival #Weeks.", null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES, 
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						anyFriends),
				ConversationStates.ATTENDING,
				null, new SayTextAction("Cześć [name] miło było cię znów spotkać. "
						+ "Zgadnij mamy następny #Mine #Town #Revival #Weeks."));
		// TODO: Tell old friends about renewal
	}


	private void addQuest() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				noFriends,
				ConversationStates.ATTENDING, "Szukam #przyjaciela.", null);
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				oldFriends,
				ConversationStates.ATTENDING, "Powinniśmy odnowić naszą #przyjaźń.", null);
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				currentFriends,
				ConversationStates.ATTENDING,
				"Poznałam wielu przyjaciół podczas #Mine #Town #Revival #Weeks.",
				null);
	}

	private void addFirstQuest() {
		// initial friends quest
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("friend", "friends", "friendship", "przyjaźń", "przyjaciółmi", "przyjaciela"),
			noFriends,
			ConversationStates.INFORMATION_1,
			"Proszę powtórz:\r\n                        \"Kółko jest okrągłe,\"",
			null);
		npc.add(ConversationStates.INFORMATION_1,
			"",
			new TriggerExactlyInListCondition("Kółko jest okrągłe,", "Kółko jest okrągłe"),
			ConversationStates.INFORMATION_2, "\"nie ma końca.\"",
			null);
		npc.add(ConversationStates.INFORMATION_2, 
			"",
			new TriggerExactlyInListCondition("nie ma końca.", "nie ma końca"),
			ConversationStates.INFORMATION_3,
			"\"To jak długie,\"", null);
		npc.add(ConversationStates.INFORMATION_3, 
			"",
			new TriggerExactlyInListCondition(
				"To jak długie,", "To jak długie",
				"To jak długie,", "To jak długie"),
			ConversationStates.INFORMATION_4,
			"\"Będę twoim przyjacielem.\"", null);

		ChatAction reward = new MultipleActions(new IncreaseKarmaAction(10), new IncreaseXPAction(25), new SetQuestToYearAction("susi"));
		npc.add(ConversationStates.INFORMATION_4, 
			"",
			new TriggerExactlyInListCondition("Będę twoim przyjacielem.", "Będę twoim przyjacielem"),
			ConversationStates.ATTENDING,
			"Fajnie. Jesteśmy teraz przyjaciółmi.",
			reward);
	}

	private void addSecondQuest() {
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("friend", "friends", "friendship", "przyjaźń", "przyjaciółmi", "przyjaciela"),
				oldFriends,
				ConversationStates.INFORMATION_5,
				"Proszę powtórz:\r\n                        \"Zdobywajmy przyjaciół,\"",
				null);
		npc.add(ConversationStates.INFORMATION_5, 
				"",
				new TriggerExactlyInListCondition("Zdobywajmy przyjaciół,", "Zdobywajmy przyjaciół"),
				ConversationStates.INFORMATION_6, "\"ale utrzymujmy kontakty ze starymi.\"",
				null);
		npc.add(ConversationStates.INFORMATION_6, "",
				new TriggerExactlyInListCondition("ale utrzymujmy kontakty ze starymi.", "ale utrzymujmy kontakty ze starymi"),
				ConversationStates.INFORMATION_7, "\"Jeden jest srebrny,\"",
				null);
		npc.add(ConversationStates.INFORMATION_7, "",
				new TriggerExactlyInListCondition("Jeden jest srebrny,", "Jeden jest srebrny"),
				ConversationStates.INFORMATION_8, "\"a inny złoty.\"",
				null);

		// lowercase "and" is ignored, even in full match mode
		ChatAction reward = new MultipleActions(new IncreaseKarmaAction(15), new IncreaseXPAction(50), new SetQuestToYearAction("susi"));
		npc.add(ConversationStates.INFORMATION_8, "",
				new TriggerExactlyInListCondition("a inny złoty.", "a inny złoty", "a inny złoty.", "a inny złoty"),
				ConversationStates.ATTENDING,
				"Fajnie! Jesteśmy teraz lepszymi przyjaciółmi.",
				reward);
	}

	/**
	 * removes an NPC from the world and NPC list
	 *
	 * @param name name of NPC
	 */
	private void removeNPC(String name) {
		SpeakerNPC npc = NPCList.get().get(name);
		if (npc == null) {
			return;
		}
		npc.getZone().remove(npc);
	}


	/**
	 * removes Susi from her home in Ados and adds her to the Mine Towns.
	 */
	@Override
	public void addToWorld() {
		removeNPC("Susi");

		buildConditions();
		createGirlNPC();
		addDialog();
	}


	/**
	 * removes Susi from the Mine Town and places her back into her home in Ados.
	 *
	 * @return <code>true</code>, if the content was removed, <code>false</code> otherwise
	 */
	@Override
	public boolean removeFromWorld() {
		removeNPC("Susi");

		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_ados_ross_house");
		new LittleGirlNPC().createGirlNPC(zone);

		return true;
	}
}
