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
package games.stendhal.server.maps.nalwor.hell;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds the reaper in hell.
 * Remaining behaviour will be in games.stendhal.server.maps.quests.SolveRiddles
 * @author kymara
 */
public class ReaperNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		SpeakerNPC npc = createNPC("Grim Reaper");
		npc.setPosition(63, 76);
		zone.add(npc);
	}
	
	static SpeakerNPC createNPC(String name) {
		final SpeakerNPC npc = new SpeakerNPC(name) {

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Jeżeli chcesz #opuścić to miejsce to musisz rozwiązać #zagadkę");
				addReply(Arrays.asList("riddle", "zagadkę"), "Przedstawię Tobie problem zaprzątający umysł jeżeli chcesz #opuścić. Oczywiście możesz gnić w piekle jeżeli tego chcesz ... ");
				// Remaining behaviour is in games.stendhal.server.maps.quests.SolveRiddles
				addJob("Zabieram dusze żyjących.");
				addHelp("Trzymam klucze do bram piekła. Chcesz #opuścić?");
				addOffer("Dopóki nie zażyczysz sobie, abym wziął twoją duszę ... ");
				addGoodbye("Stary porządek rzeczy odszedł w niepamięć ... ");
			}
		};
		npc.setEntityClass("grim_reaper_npc");
		npc.setPosition(63, 76);
		npc.initHP(100);
		npc.setDescription("Oto Grim Reaper. Jego zagadki dają wolność.");
		return npc;
	}
}
