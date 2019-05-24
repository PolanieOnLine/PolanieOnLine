/***************************************************************************
 *                   (C) Copyright 2011-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.outside;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds a NPC outside Magician house in Ados  (name:Venethiel) who is the pupil of Magician Haizen
 *
 * @author geomac
 */
public class MagicianHouseGreeterNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		createMagicianHouseGreeterNPC(zone);
	}

	private void createMagicianHouseGreeterNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Venethiel") {

			@Override
			protected void createDialog() {
				addGreeting("Cześć. Jestem podekscytowana magicznym #labiryntem!");
				addHelp("Jeśli się zmęczysz to powinieneś się wylogować, aby wrócić do #Haizena.");
				addReply(Arrays.asList("maze", "labiryntem", "labiryncie", "labiryntu"), "Obawiam się, że zgubisz drogę, ale są tam #zwoje do odnalezienia.");
				addReply(Arrays.asList("scrolls", "zwoje"), "Masz tylko dziesięć minut na zebranie zwoi w #labiryncie.");
				addQuest("Pytam wojowników o ukończenie #labiryntu. Haizen zrobi mnie swoją #asystentką.");
				addReply(Arrays.asList("assistant", "asystentką"), "Pewnego dnia nauczę się jak używać magii.");
				addReply(Arrays.asList("Haizen", "Haizena"), "On uczy magii.");
				addOffer("Mogę udzielić Tobie kilku #rad.");
				addReply(Arrays.asList("advice", "rad"), "Pomocne będzie częste spoglądanie na mapkę.");
				addJob("Mam nadzieję, że wkrótce zostanę #asystentką Haizens.");
				addGoodbye("Dziękuję i życzę miłego dnia.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("Oto Venethiel. Chce się nauczyć magii.");
		npc.setEntityClass("magicianhousegreeternpc");
		npc.setPosition(70, 52);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}
}