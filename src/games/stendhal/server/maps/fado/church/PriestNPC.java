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
package games.stendhal.server.maps.fado.church;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;

import java.util.Arrays;
import java.util.Map;


/**
 * Creates a priest NPC who can celebrate marriages between two
 * players.
 *
 * The marriage itself is done in a separate quest file
 *
 * @author daniel/kymara
 *
 */
public class PriestNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		SpeakerNPC priest = new SpeakerNPC("Priest") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam w kościele!");
				new HealerAdder().addHealer(this, 1000);
				addJob("Jestem księdzem i dam #ślub tym, którzy mają złoty pierścień na wymianę i są zaręczeni.");
				addHelp("Pomogę Tobie w poślubieniu twojej miłości, ale musisz być zaręczony przez Sister Benedicta i musisz mieć #pierścionek, aby dać swojemu partnerowi.");
				addQuest("Dam #ślub tym osobom, które zaręczyły się w określony sposób. Porozmawiaj z Sister Benedicta jeżeli nie jesteś jeszcze zaręczony. Pamiętaj o przyniesieniu #pierścionka ślubnego!");
				addGoodbye("Idź w pokoju.");
				addReply(Arrays.asList("ring", "pierścionka", "pierścionek"), "Gdy się zaręczysz to możesz pójść do Ognira, który pracuje tutaj w Fado, aby wyrobić pierścionek ślubny. Wiem, że też sprzedaje pierścionki zaręczynowe, ale one są tylko jako dekoracja. Jakie to rozpustne!");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		priest.setDescription("Oto ksiądz w kościole w Fado");
		priest.setEntityClass("priestnpc");
		priest.setPosition(11, 5);
		priest.setDirection(Direction.DOWN);
		priest.initHP(100);
		zone.add(priest);
	}
}
