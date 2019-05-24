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
package games.stendhal.server.maps.semos.townhall;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

public class DecencyAndMannersWardenNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosVillageBench(zone);
	}

	private void buildSemosVillageBench(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Ketteh Wehoh") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addHelp("Jestem miejskim Strażnikiem Dobrych Obyczajów i Manier. Mogę poradzić jak zachowywać się w różnych sytuacjach. Na przykład nie chodź nago. Słyszałeś kiedyś o #kolorowym #ubraniu?");
				addReply(Arrays.asList("outfit", "colour", "outfit colouring", "kolorowym", "ubraniu", "kolorowym ubraniu"),
						"Możesz pokolorować swoje ubranie ulubionym kolorem. Wybierz wygląd i naciśnij prawy przycisk na sobie i pokoloruj swoje włosy lub ubranie! Może na pomarańczowo. Pasuje do Halloween!");
				addJob("Moją pracą jest zadbanie o zachowanie poziomu obyczajów w Semos. Znam protokół na każdą sytuację i wszystkie sposoby manipulacji. To złe. Czasami miesza mi się czy użyć łyżeczki czy widelca, ale w Semos nikt nie używa sztućców.");
				addQuest("Jedyne zadanie jakie mam dla Ciebie to bycie uprzejmym w stosunku do innych.");
				addGoodbye();
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				super.onGoodbye(player);
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("Ketteh Wehoh jest miejscowym Strażnikiem Dobrych Obyczajów i Manier. Rezyduje w ratuszu Semos.");
		npc.setEntityClass("elegantladynpc");

		npc.setDirection(Direction.RIGHT);
		npc.setPosition(13, 38);
		
		npc.initHP(100);
		zone.add(npc);
	}
}
