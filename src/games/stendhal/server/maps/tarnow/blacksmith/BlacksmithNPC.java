/***************************************************************************
 *                   (C) Copyright 2024 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.tarnow.blacksmith;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.MultiProducerAdder;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.maps.tarnow.blacksmith.forge.ForgeItems;

public class BlacksmithNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("kowal Przemysław") {
//			@Override
//			protected void createPath() {
//				final List<Node> nodes = new LinkedList<Node>();
//				nodes.add(new Node(86, 70));
//				nodes.add(new Node(86, 74));
//				nodes.add(new Node(80, 74));
//				nodes.add(new Node(86, 74));
//				nodes.add(new Node(86, 70));
//				setPath(new FixedPath(nodes, true));
//			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Zajmuję się wytwórstwem narzędzi oraz uzbrojenia dla pobliskich armii.");
				addGoodbye();

				new MultiProducerAdder().addMultiProducer(this, ForgeItems.getBehaviour(), "forge_newarms",
					"Witaj w moich skromnych progach! W czymś mogę #pomóc?");

				String productionList = "Oto moja aktualna lista usług:"
					+ "\n- #'hełm ciemnomithrilowy'"
					+ "\n- #'zbroja ciemnomithrilowa'"
					+ "\n- #'pas ciemnomithrilowy'"
					+ "\n- #'spodnie ciemnomithrilowe'"
					+ "\n- #'płaszcz ciemnomithrilowy'"
					+ "\n- #'tarcza ciemnomithrilowa'"
					+ "\nJeżeli jesteś zainteresowany to powiedz mi #stwórz i wybierz jakie uzbrojenie potrzebujesz.";

				add(ConversationStates.ANY,
					ConversationPhrases.HELP_MESSAGES,
					new QuestCompletedCondition("forge_newarms"),
					ConversationStates.ATTENDING,
					"Mogę wykonać dla ciebie nowe uzbrojenie, jeśli zechcesz. " + productionList,
					null);

				add(ConversationStates.ANY,
					ConversationPhrases.HELP_MESSAGES,
					new NotCondition(new QuestCompletedCondition("forge_newarms")),
					ConversationStates.ATTENDING,
					"Żartujesz sobie? Jestem aktualnie zajęty innymi sprawami.",
					null);

				add(ConversationStates.ANY,
					ConversationPhrases.HELP_MESSAGES,
					new QuestCompletedCondition("forge_newarms"),
					ConversationStates.ATTENDING,
					"Mogę zaoferować ulepszenie twoich przedmiotów z mithrilu za pomocą klejnotu ciemnolitu. " + productionList,
					null);

				add(ConversationStates.ANY,
					ConversationPhrases.OFFER_MESSAGES,
					new NotCondition(new QuestCompletedCondition("forge_newarms")),
					ConversationStates.ATTENDING,
					"Nie mam nic Tobie do zaoferowania...",
					null);
			}
		};

		npc.setDescription("Oto kowal Przemysław. Jest w trakcie udoskonalania wyposażenia dla przyszłych rycerzy.");
		npc.setEntityClass("blacksmithnpc");
		npc.setGender("M");
		npc.setPosition(1, 1);
		zone.add(npc);
	}
}
