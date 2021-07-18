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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.OutfitChangerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.OutfitChangerBehaviour;

/**
 * Dressing rooms at fado hotel.
 *
 * @author kymara
 */
public class BrideAssistantNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Tamara") {
			@Override
			protected void createDialog() {
				// greetings in Marriage quest
				addJob("Pomagam pannom młodym w doborze sukienki na ich ślub.");
				addHelp("Powiedz mi #ubierz jeżeli chcesz założyć #suknię (#'gown') na swój ślub.");
				addQuest("Chyba nie chcesz myśleć o takich sprawach przed wielkim dniem!");
				addReply("suknię", "Każda panna młoda potrzebuje pięknej sukni ślubnej. Kosztuje to 100 monet jeżeli chcesz nosić #suknię ślubną to powiedz #ubierz.");
				addGoodbye("Życzę szczęśliwych chwil!");

				final Map<String, Integer> priceList = new HashMap<String, Integer>();
				priceList.put("gown", 100);
				final OutfitChangerBehaviour behaviour = new OutfitChangerBehaviour(priceList, true);
				new OutfitChangerAdder().addOutfitChanger(this, behaviour, Arrays.asList("wear", "ubierz"));
			}
		};

		npc.setDescription("Oto Tamara. Czeka na nowych narzeczonych, jest ciekawa kto stanie na następnym ślubnym kobiercu.");
		npc.setEntityClass("woman_003_npc");
		npc.setGender("F");
		npc.setDirection(Direction.RIGHT);
		npc.setPosition(3, 10);
		zone.add(npc);
	}
}
