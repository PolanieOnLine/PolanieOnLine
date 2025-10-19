/***************************************************************************
 *               (C) Copyright 2024-2025 - PolanieOnLine                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.desert.blackriver;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.useable.GoldenCauldronEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import java.util.Map;

/**
 * Places Draconia and her golden cauldron inside Józek's forge.
 */
public class GoldenCauldronCorner implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		GoldenCauldronEntity.generateRPClass();
		buildCauldron(zone);
		buildNPC(zone);
	}

	private void buildCauldron(final StendhalRPZone zone) {
		final GoldenCauldronEntity cauldron = new GoldenCauldronEntity();
		cauldron.setPosition(10, 6);
		cauldron.setEntityClass("useable_entity");
		cauldron.setEntitySubclass("golden_cauldron");
		cauldron.setDescription(
		"Oto kocioł Draconii. Możesz w nim przygotować wywar wąsatych smoków.");
		zone.add(cauldron);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Draconia") {
			@Override
			protected void createDialog() {
				addGreeting(
				"Witaj, podróżniku. Mój kocioł czeka na kolejne wyzwanie.");
				addHelp("Jeżeli pragniesz ulepszyć złotą ciupagę, przygotuj #wywar "
				+ "wąsatych smoków w moim kotle.");
				addReply("wywar",
				"Wrzuć do kotła wszystkie wymagane składniki, a następnie "
				+ "kliknij 'Mieszaj'. Pamiętaj, że tylko osoba, która "
				+ "otworzyła kocioł, może dokończyć wywar.");
				addJob("Pilnuję, by płomienie nie zgasły w kuźni Józka.");
				addGoodbye("Niech płomienie prowadzą Cię ku sile.");
			}
		};

		npc.setPosition(8, 5);
		npc.setEntityClass("witchnpc");
		npc.setGender("F");
		npc.setDescription(
		"Oto Draconia, strażniczka wąsatych smoków i ich tajemnych mikstur.");
		zone.add(npc);
	}
}
