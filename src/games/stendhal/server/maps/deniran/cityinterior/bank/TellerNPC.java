/***************************************************************************
 *                       Copyright © 2023 - Stendhal                       *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityinterior.bank;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BankTellerAdder;

public class TellerNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		zone.add(buildNPC());
	}

	private SpeakerNPC buildNPC() {
		final SpeakerNPC npc = new SpeakerNPC("Telly");

		npc.setOutfit("body=0,head=0,eyes=0,hair=997,dress=53");
		npc.setOutfitColor("eyes", 0x691313);
		npc.setGender("M");
		npc.setPosition(7, 4);
		npc.setIdleDirection(Direction.DOWN);

		npc.addGreeting("Witamy w banku Deniran!");
		npc.addGoodbye();
		npc.addQuest("Nie ma niczego, w czym potrzebuję pomocy.");

		// manage bank account balance
		BankTellerAdder.addTeller(npc);
		npc.addOffer(npc.getJob());

		return npc;
	}
}