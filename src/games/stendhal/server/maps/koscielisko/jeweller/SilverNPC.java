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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.MultiProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.MultiProducerBehaviour;

/**
 * @author ?
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
					+ "i tak ja obrabiam rudę srebra oraz kryształy diamentu, wystarczy, że powiesz #'odlej sztabka srebra' lub #'oszlifuj diament'.\n"
					+ "Ziemirad zajmuje się obrabianiem kryształy rubinu.\n"
					+ "Sobek jest bardzo dobry w obróbce kryształów obsydianu.\n"
					+ "Mieszek, ☺ ach ten Mieszek, kawalarz z niego, ale do rzeczy. On obrobi Tobie kryształ szmaragdu.\n"
					+ "No i najzdolniejszy z całej czwórki Krzesim, zdolności swe poświęcił kryształom szafiru.\n"
					+ "Jeżeli chcesz obrobić któryś z tych kamieni to podejdź i powiedz #mistrz, a czeladnicy będą wiedzieć, że ja ciebie wysłałem.");
				addHelp ("Jakiego typu pomocy oczekujesz? Jeżeli chcesz się czegoś dowiedzieć czym się zajmuję to powiedz #praca, a chętnie opowiem.");
				addGoodbye();

				final HashSet<String> productsNames = new HashSet<String>();
                productsNames.add("diament");
                productsNames.add("sztabka srebra");

				final Map<String, Integer> required_diamond = new TreeMap<String, Integer>();
				required_diamond.put("polano", 5);
				required_diamond.put("kryształ diamentu", 1);
				required_diamond.put("money", 200);

				final Map<String, Integer> required_silver = new TreeMap<String, Integer>();
				required_silver.put("polano", 2);
				required_silver.put("ruda srebra", 1);
				required_silver.put("money", 100);

				final HashMap<String, Map<String, Integer>> requiredResourcesPerProduct = new HashMap<String, Map<String, Integer>>();
                requiredResourcesPerProduct.put("diament", required_diamond);
                requiredResourcesPerProduct.put("sztabka srebra", required_silver);

                final HashMap<String, Integer> productionTimesPerProduct = new HashMap<String, Integer>();
                productionTimesPerProduct.put("diament", 14 * 60);
                productionTimesPerProduct.put("sztabka srebra", 10 * 60);

                final HashMap<String, Boolean> productsBound = new HashMap<String, Boolean>();
                productsBound.put("diament", false);
                productsBound.put("sztabka srebra", false);

                final MultiProducerBehaviour behaviour = new MultiProducerBehaviour(
                        "drogosz_grindandcast",
                        Arrays.asList("grind", "cast", "oszlifuj", "odlej"),
                        productsNames,
                        requiredResourcesPerProduct,
                        productionTimesPerProduct,
                        productsBound);

				new MultiProducerAdder().addMultiProducer(this, behaviour,
						"Pozdrawiam. Sądzę, że jesteś zainteresowany srebrem bądź diamentem. Jeżeli tak to zapytaj mnie o #'pomoc'.");
				addReply("polano",
						"Musisz Drwala się zapytać. On wie jak zdobyć drzewo.");
				addReply(Arrays.asList("ruda srebra", "kryształ diamentu"),
						"Jak mi wiadomo kamienie szlachetne wszelkiej maści znajdują się w kopalniach. Mój stary przyjaciel #Bercik może więcej o nich opowiedzieć.");
				addReply("Bercik",
						"Bercika znajdziesz na kościelisku niedaleko Zakopanego. Pamiętam, że wokoło kręciło się sporo białych tygrysów.");
			}
		};

		drogosz.setDescription("Oto Drogosz, zajmuje się słynnym zakładem złotniczym.");
		drogosz.setEntityClass("richardstallmannpc");
		drogosz.setGender("M");
		drogosz.setPosition(15, 3);
		drogosz.setDirection(Direction.DOWN);
		zone.add(drogosz);
	}
}
