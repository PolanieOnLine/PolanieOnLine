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
package games.stendhal.server.maps.fado.forest;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Creates Imorgen, a young witch who lives in Fado Forest.
 *
 * @author Vanessa Julius
 */
public class WitchNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Imorgen") {

			@Override
			protected void createPath() {
				setPath(null);
			
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć wanderer!");
				addJob("Jestem praktykującą czarownicą. Wieżę, że zostanę potężną czarownicą.");
				addQuest("Moja siostra w środku jest zaniepokojona... Jest chora i boi się, a ja potrzebuję jej pomocy. Muszę się dowiedzieć #jak...");
				addReply(Arrays.asList("how", "jak"), "Pytanie jest w czym mogłabym pomóc... Może zapytam jej o to później. Wcześniej muszę skończyć moje nowe #przepisy...");
				addReply(Arrays.asList("recipe", "przepisy"), "Wątpię, abyś mógł zjeść lub wypić coś co teraz przygotuję... Ale znam dwie miłe panie, które przygotują dla Ciebie pyszną #zupę.");
				addReply(Arrays.asList("soup", "zupę"), "Florence Boullabaisse i Mother Helena są wspaniałymi kucharkami. Spotkasz je na rynku w Ados i w tawernie w Fado.");
				addHelp("Aldrin sprzedaje miód, który pewnie chciałbyś zjeść z chlebem.");
				addOffer("Przykro mi, ale nie mogę ci nic sprzedać.");
				addGoodbye("Dowidzenia i trzymaj się!");
			}
		};

		npc.setEntityClass("youngwitchnpc");
		npc.setPosition(59, 29);
		npc.initHP(100);
		npc.setDescription("Oto Imorgen. Jest młodą czarownicą z tajemniczą aurą, którą może rzucić na długi dystans.");
		zone.add(npc);
	}
}
