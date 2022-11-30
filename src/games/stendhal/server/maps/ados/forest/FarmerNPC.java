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
package games.stendhal.server.maps.ados.forest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;

/**
 * Builds Karl, the farmer NPC.
 * He gives horse hairs needed for the BowsForOuchit quest
 * He gives help to newcomers about the area
 * He suggests you can buy milk from his wife Philomena
 * @author kymara
 */
public class FarmerNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildFarmer(zone);
	}

	private void buildFarmer(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Karl") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(64, 76));
				nodes.add(new Node(64, 86));
				nodes.add(new Node(68, 86));
				nodes.add(new Node(68, 84));
				nodes.add(new Node(76, 84));
				nodes.add(new Node(68, 84));
				nodes.add(new Node(68, 86));
				nodes.add(new Node(60, 86));
				nodes.add(new Node(60, 89));
				nodes.add(new Node(60, 86));
				nodes.add(new Node(64, 86));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Heja! Miło Cię widzieć w naszym gospodarstwie.");
				addJob("Och praca tutaj jest ciężka. Nawet nie myślę o tym, że mógłbyś mi pomóc.");
				addReply("Philomena","Ona jest w domku na południowy-zachód stąd.");
				addHelp("Potrzebujesz pomocy? Mogę coś ci opowiedzieć o #sąsiedztwie.");
				addReply(Arrays.asList("neighborhood.", "sąsiedztwie."),"Na północy znajduje się jaskinia z niedźwiedziami i innymi potworami. Jeżeli pójdziesz na północny-wschód " +
						"to po pewnym czasie dojdziesz do dużego miasta Ados. Na wschodzie jest duuuuża skała. Balduin " +
						"wciąż tam mieszka? Chcesz wyruszyć na południowy-wschód? Cóż.. możesz tamtędy dojść do Ados, ale " +
						"droga jest trochę trudniejsza.");
				addQuest("Nie mam teraz czasu na takie rzeczy. Praca.. praca.. praca..");
				addReply(Arrays.asList("empty sack", "puste worki"),"Och, mam tego mnóstwo na sprzedaż. Czy chcesz kupić #'pusty worek'?");
				addGoodbye("Do widzenia, do widzenia. Bądź ostrożny.");

				// shop
				final Map<String, Integer> offerings = new HashMap<String, Integer>();
                offerings.put("pusty worek", 10);
                final Map<String, Integer> allOfferings = new HashMap<String, Integer>();
				allOfferings.putAll(offerings);
				allOfferings.put("końskie włosie", 20);
				final SellerBehaviour behaviour = new SellerBehaviour(allOfferings);
				final Map<String, ChatCondition> conditions = new HashMap<String, ChatCondition>();
				conditions.put("końskie włosie", new QuestCompletedCondition("bows_ouchit"));
				behaviour.addConditions(this, conditions);
				new SellerAdder().addSeller(this, behaviour, false);

				final String offerReply = "Nasze mleko jest najlepsze. Zapytaj moją żonę #Philomena o mleko.";
				add(
					ConversationStates.ATTENDING,
					ConversationPhrases.OFFER_MESSAGES,
					new QuestNotCompletedCondition("bows_ouchit"),
					false,
					ConversationStates.ATTENDING,
					offerReply + " Sprzedaję "
						+ Grammar.enumerateCollection(offerings.keySet()) + ".",
					null);

				add(
					ConversationStates.ATTENDING,
					ConversationPhrases.OFFER_MESSAGES,
					new QuestCompletedCondition("bows_ouchit"),
					false,
					ConversationStates.ATTENDING,
					offerReply + " Ostatnio mam tego nadmiar i teraz sprzedaję "
						+ Grammar.enumerateCollection(allOfferings.keySet())
						+ ".",
					null);
			}
		};

		npc.setDescription("Oto Karl, miły starszy rolnik.");
		npc.setEntityClass("beardmannpc");
		npc.setGender("M");
		npc.setPosition(64, 76);
		zone.add(npc);
	}
}
