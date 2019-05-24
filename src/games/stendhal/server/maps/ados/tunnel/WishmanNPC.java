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
package games.stendhal.server.maps.ados.tunnel;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * Outside entrance to dragon lair in -1_ados_outside_w.
 */
public class WishmanNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildWishman(zone);
	}

	private void buildWishman(final StendhalRPZone zone) {
		final SpeakerNPC wishman = new SpeakerNPC("Wishman") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			public void createDialog() {
				addGreeting("Pozdrawiam podróżniku. Co mogę dla Ciebie zrobić?");
				addOffer("Jesteśmy tutaj szczęśliwi. Nazywamy to miejsce domem dla naszych wspaniałych smoków.");
				addJob("Jestem podejrzliwy w stosunku do osób, które chcą skrzywdzić nasze smoki. Są wszystkim co pozostało z naszej sławy.");
				addHelp("Może chcesz poznać moich braci, którzy są w tunelu. Miej na uwadze, żeby uważać na morderców. Zajęli oni tunele.");
				addGoodbye("Na razie. Niech twoje serce zawsze będzie wolne, a życie długie.");
				// all other behaviour is defined in the quest.
			}
		};

		wishman.setDescription("Oto Wishman kiedyś storm trooper w mrocznych legionach Blordroughtów. Teraz strażnik wszystkiego co pozostało po smokach.");
		wishman.setEntityClass("stormtroopernpc");
		wishman.setPosition(30, 28);
		wishman.initHP(100);
		zone.add(wishman);
	}
}
