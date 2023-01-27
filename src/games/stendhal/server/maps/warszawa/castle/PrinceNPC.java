/***************************************************************************
 *                 (C) Copyright 2021-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.warszawa.castle;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */
public class PrinceNPC implements ZoneConfigurator {
    @Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Książę") {
			@Override
			public void createDialog() {
				addGreeting("Co cię sprowadza do mnie i do mojego oddziału młody człowieku?");
				addJob("Z moim oddziałem rycerzy staramy się wypędzieć buntowników z tego zamku!");
				addHelp("Może mógłbyś mi w czymś #'pomóc'...");
				addOffer("Możesz dla mnie wykonać #zadanie, a w zamian Cię wynagrodzę... Tylko taką ofertę mogę złożyć.");
				addGoodbye();
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("Oto Książę. Sprawia wrażenie stanowczego przywódcy.");
		npc.setEntityClass("npcwarsawking");
		npc.setGender("M");
		npc.setPosition(94, 85);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
