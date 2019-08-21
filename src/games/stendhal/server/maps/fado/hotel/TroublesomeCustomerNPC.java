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
import games.stendhal.server.entity.npc.behaviour.impl.MonologueBehaviour;

import java.util.Map;

/**
 * Provides Groongo Rahnnt, The Troublesome Customer in Fado's Hotel.
 *
 * Groongo offers a quest to bring him the meal he's been awaiting to order.
 *
 * @author omero
 *
 */
public class TroublesomeCustomerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String[] rants = {
			"... Bah! Zastanawiam się jak długo musi czekąć głodny klient nim pojawi się kelner...",
			"... Boh! Od czasu do czasu ja też chciałbym zjeść jakiś skromny posiłek...",
			"... Doh! Nic dziwnego, że to miejsce jest prawie opuszczone... Jeden czeka wieczność na skromny posiłek...",
			"... Gah! To miejsce musi zatrudniać niewidzialnych kelenerów i leniwego szefa kuchni...",
			"... Hgh! HalOoo?! Zastanawiam się... Czy jest TU KTOŚ?!",
			"... Mah! Już policzyłem wszystkie płytki na podłodze... Dwa razy...",
			"... Meh! Zacznę tutaj nacinać nogi stołu za każdą spędzoną minutę czekając na obsługę...",
			"... Ugh! Powinienem zacząć liczyć WSZYSTKIE robaki atakujące to miejsce..."
			/**
			"... Duh! I should start counting ALL the bugs infesting this place...",
			"... Wah! I should start counting ALL the bugs infesting this place...",
			"... Woh! I should start counting ALL the bugs infesting this place...",
			*/
		};
		new MonologueBehaviour(buildNPC(zone), rants, 1);
	}

	private SpeakerNPC buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Groongo Rahnnt") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Gah! Już czas, aby ktoś się tutaj pojawił! Czy jesteś gotowy, aby teraz wykonać swoje #'zadanie'?!");
				addHelp("POMOCY?! Chcesz, abym JA ci ...pomógł... TOBIE?! Zapytaj mnie o zadanie i dam ci jedno!");
				addJob("Ah! Nareszcie... Chcesz #zadanie?");
				addOffer("Wykonaj dla mnie #zadanie, a dostaniesz dobry napiwek!");
				addGoodbye("Ty... TY... Uciekaj teraz!");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.RIGHT);
			}
		};

		// Finalize Groongo details
		npc.setEntityClass("customeronchairnpc");
		npc.setDescription("Oto Groongo Rahnt. Jest niecierpliwy, że nikt nie zwraca na niego uwagi!");
		npc.setPosition(70, 24);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);

		return npc;
	}
}
