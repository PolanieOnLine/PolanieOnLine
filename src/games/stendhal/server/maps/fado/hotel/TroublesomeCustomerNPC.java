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

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.MonologueBehaviour;

/**
 * Provides Groongo Rahnnt, The Troublesome Customer in Fado's Hotel Restaurant.
 *
 * Groongo Rahnnt offers a quest to bring him a decent meal he's been awaiting to order.
 * Groongo Rahnnt offered quest will involve Stefan, the chef of Fado's Hotel Restaurant
 *
 * @author omero
 */
public class TroublesomeCustomerNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String[] rants = {
			"... Bah! Zastanawiam się jak długo musi czekąć głodny klient nim pojawi się kelner...",
			"... Boh! Od czasu do czasu ja też chciałbym zjeść jakiś skromny posiłek...",
			"... Doh! Nic dziwnego, że to miejsce jest prawie opuszczone... Jeden czeka wieczność na skromny posiłek...",
			"... Gah! To miejsce musi zatrudniać niewidzialnych kelenerów i leniwego szefa kuchni...",
			"... Mah! Już policzyłem wszystkie płytki na podłodze... Dwa razy...",
			"... Meh! Zacznę tutaj nacinać nogi stołu za każdą spędzoną minutę czekając na obsługę...",
			"... Ugh! Powinienem zacząć liczyć WSZYSTKIE robaki atakujące to miejsce..."
		};
		//minutes between rants, 5
		new MonologueBehaviour(buildNPC(zone), rants, 5);
	}

	private SpeakerNPC buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Groongo Rahnnt") {
			@Override
			protected void createDialog() {
				addGreeting("Gah! W końcu ktoś się pojawił, aby wykonać swoje #'zadanie'...");
				addHelp("Zamiast tego powinieneś poprosić mnie o #'zadanie'!");
				addJob("Ach! Wreszcie ktoś chce #'zadanie'...");
				addOffer("Wykonaj dla mnie #zadanie, a dostaniesz dobry napiwek!");
				addGoodbye("Ty... TY... Uciekaj teraz!");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.RIGHT);
			}
		};

		// Finalize Groongo Rahnnt, the Fado's Hotel Restaurant Troublesome Customer
		npc.setDescription("Oto Groongo Rahnnt. Jest niecierpliwy, że nikt nie zwraca na niego uwagi!");
		npc.setEntityClass("troublesomecustomernpc");
		npc.setGender("M");
		npc.setPosition(70, 24);
		npc.setDirection(Direction.RIGHT);
		zone.add(npc);

		return npc;
	}
}
