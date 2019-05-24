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
package games.stendhal.server.maps.athor.cocktail_bar;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cocktail Bar at the Athor island beach (Inside / Level 0).
 *
 * @author kymara
 */
public class BarmanNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBar(zone);
	}

	private void buildBar(final StendhalRPZone zone) {
		final SpeakerNPC barman = new SpeakerNPC("Pedro") {

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addJob("Mogę przyrządzić różne koktajle! Powiedz tylko #zrób.");
				addQuest("Co powiedziałeś?");
				addOffer("Może mogę #zrobić koktail z #kokosa i #ananasa dla ochłody... Powiedz tylko #zrób.");
				addReply("ananasa","Ananasy nie rosną na Athor musisz sam je zdobyć.");
				addReply("kokosa","Używam mleka z nich, aby #zrobić twój koktail. Poszukaj ich pod palmami.");
				addHelp("Chcesz napój z oliwką? Jestem do usług!");
				addGoodbye("Zdrówko!");

				// make cocktail!
				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("kokos", 1);
				requiredResources.put("ananas", 1);
				final ProducerBehaviour mixerBehaviour = new ProducerBehaviour("barman_mix_pina",
						 Arrays.asList("mix", "zrób"), "napój z oliwką", requiredResources, 2 * 60);
				new ProducerAdder().addProducer(this, mixerBehaviour, "Aloha!");
			}
		};

		barman.setEntityClass("barmannpc");
		barman.setPosition(9, 5);
		barman.setDirection(Direction.DOWN);
		barman.initHP(100);
		barman.setDescription("Widzisz Pedro, barmana. Może zmiksuję pezyszny koktajl dla ciebie.");
		zone.add(barman);
	}
}
