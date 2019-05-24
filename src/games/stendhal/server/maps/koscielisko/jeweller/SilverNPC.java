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
package games.stendhal.server.maps.koscielisko.jeweller;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
/*
 * Twilight zone is a copy of sewing room in dirty colours with a delirious sick lda (like Ida) in it
 */
public class SilverNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildDrogosz(zone);
	}

	private void buildDrogosz(final StendhalRPZone zone) {
		final SpeakerNPC drogosz = new SpeakerNPC("mistrz Drogosz") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog(){
				addGreeting("Witaj!");
				addJob("Jesteśmy znanym zakładem złotniczym. Ja i moi czeladnicy zajmujemy się obróbką wielu kamieni szlachetnych\n"
					+ "i tak ja obrabiam rudę srebra, wystarczy, że powiesz #odlej.\n"
					+ "Ziemirad zajmuje się obrabianiem kryształy rubinu.\n"
					+ "Sobek jest bardzo dobry w obróbce kryształów obsydianu.\n"
					+ "Mieszek, ☺ ach ten Mieszek, kawalarz z niego, ale do rzeczy. On obrobi Tobie kryształ szmaragdu.\n"
					+ "No i najzdolniejszy z całej czwórki Krzesim, zdolności swe poświęcił kryształom szafiru.\n"
					+ "Jeżeli chcesz obrobić któryś z tych kamieni to podejdź i powiedz #mistrz, a czeladnicy będą wiedzieć, że ja ciebie wysłałem.");
				addHelp ("Jakiego typu pomocy oczekujesz? Jeżeli chcesz się czegoś dowiedzieć czym się zajmuję to powiedz #praca, a chętnie opowiem.");
				addGoodbye("Dowidzenia.");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("polano", 2);
				requiredResources.put("ruda srebra", 1);
				requiredResources.put("money", 100);

				final ProducerBehaviour behaviour = new ProducerBehaviour(
					"drogosz_cast_silver", Arrays.asList("cast", "odlej"), "sztabka srebra",
					requiredResources, 10 * 60);

				new ProducerAdder().addProducer(this, behaviour,
						"Pozdrawiam. Sądzę, że jesteś zainteresowany srebrem. Jeżeli tak to zapytaj mnie o #'pomoc'.");
				addReply("polano",
						"Musisz Drwala się zapytać. On wie jak zdobyć drzewo.");
				addReply("ruda srebra",
						"Jak mi wiadomo kamienie szlachetne wszelkiej maści znajdują się w kopalniach. Mój stary przyjaciel #Bercik może więcej o nich opowiedzieć.");
				addReply("Bercik",
						"Bercika znajdziesz na kościelisku niedaleko Zakopanego. Pamiętam, że wokoło kręciło się sporo białych tygrysów.");
			}
		};

		drogosz.setEntityClass("richardstallmannpc");
		drogosz.setPosition(15, 3);
		drogosz.setDirection(Direction.DOWN);
		drogosz.initHP(100);
		zone.add(drogosz);
	}
}
