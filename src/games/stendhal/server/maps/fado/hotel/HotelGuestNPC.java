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
package games.stendhal.server.maps.fado.hotel;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds a NPC in a house on Ados market (name:Damon) who is the daughter of fisherman Fritz
 * 
 * @author Vanessa Julius 
 *
 */
public class HotelGuestNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Damon") {
		    
			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Hej!");
				addHelp("Oh, przybyłem do miasta, aby odwiedzić mojego młodszego brata, Stefana. Pochodzę z Ados, lecz po pewnych problemach z moją byłą dziewczyną, Caroline,  postanowiłem nieco popodróżować. Widziałem #'upiorny zamek', gdy szedłem do Fado...");
				addReply(Arrays.asList("spooky castle", "upiorny zamek"), "On jest naprawdę upiorny! Strażnikami są szkielety... Straszne! Dobrze, że byle czego się nie boję. Ale założę się, że coś jest #'ukryte wewnątrz zamku'...");
				addReply(Arrays.asList("hidden inside", "ukryte wewnątrz zamku"), "Obiecałem bratu odwiedzić go tak szybko, jak to będzie możliwe, więc poszedłem prosto do Fado, ale... to był zły pomysł. Ten zamek warto odwiedzić.");
				addQuest("Zadanie dla Ciebie? Ode mnie? Oj, nie... Chyba sobie ze mnie #'żartujesz'."); 
				addReply(Arrays.asList("kidding", "żartujesz"), "Czy ja wyglądam na osobę, która potrzebuje pomocy? Oczywiście, że nie! A już na pewno nie w tej chwili.");
				addJob("Czasami jestem tu, czasami tam. Jestem szybki jak #wiatr i zwinny jak pantera.");
				addReply(Arrays.asList("wind", "wiatr"), "Nie, nie jestem burzą, jak mówią #wszyscy w Ados.");
				addReply(Arrays.asList("everyone", "wszyscy"), "Moja histeryczna była dziewczyna i jej ojciec zobaczyli go gdzieś daleko na morzu. Cóż, ja nawet go nie poczułem.");
				addOffer("Cóż, nikt nic nie oferuje mnie, tak i ja nic nie oferuję Tobie. Takie życie.");
				addGoodbye("Pa!");
			}

		@Override
		protected void onGoodbye(RPEntity player) {
			setDirection(Direction.RIGHT);
		}
		
		};

		npc.setDescription("Oto Damon. Jego oczy świecą nawet w ciemności.");
		npc.setEntityClass("hotelguestnpc");
		npc.setPosition(77, 23);
		npc.initHP(100);
		zone.add(npc);
	}
}
