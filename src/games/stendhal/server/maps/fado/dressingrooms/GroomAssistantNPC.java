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
package games.stendhal.server.maps.fado.dressingrooms;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.OutfitChangerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.OutfitChangerBehaviour;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Dressing rooms at fado hotel.
 *
 * @author kymara
 */
public class GroomAssistantNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildDressingRoom(zone);
	}

	private void buildDressingRoom(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Timothy") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(20, 10));
				nodes.add(new Node(20, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				// greeting in Marriage quest
				addJob("Pomagam panom młodym w odpowiednim doborze ubrania na ich ślub.");
				addHelp("Powiedz mi #ubierz jeżeli chciałbyś założyć #garnitur ( #suit ) na swój ślub.");
				addReply(Arrays.asList("garnitur", "suit"), "Jeżeli chcesz dobrze wyglądać musisz założyć #garnitur na swój ślub. Powiedz tylko #ubierz. Kosztuje to 50 monet.");
				addQuest("Raczej powinieneś myśleć o swoim ślubie.");
				addGoodbye("Dowidzenia. Mam nadzieje, że poszczęści Ci się w życiu.");

				final Map<String, Integer> priceList = new HashMap<String, Integer>();
				priceList.put("suit", 50);
				final OutfitChangerBehaviour behaviour = new OutfitChangerBehaviour(priceList);
				new OutfitChangerAdder().addOutfitChanger(this, behaviour, Arrays.asList("wear", "ubierz"));
			}
		};

		npc.setEntityClass("executivenpc");
		npc.setPosition(20, 10);
		npc.initHP(100);
		npc.setDescription("Widzisz Timothy. Czeka na osoby, ktore wkrótce ożenią się.");
		zone.add(npc);
	}
}
