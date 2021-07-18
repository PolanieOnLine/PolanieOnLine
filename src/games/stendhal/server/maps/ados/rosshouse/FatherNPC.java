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
package games.stendhal.server.maps.ados.rosshouse;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * <p>Creates a normal version of Mr. Ross in the ross house.
 */
public class FatherNPC implements ZoneConfigurator {
	public final static String MRROSS_OUTFIT = "body=0,dress=34,head=0,eyes=0,hair=27";

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		createDadNPC(zone);
	}

	public void createDadNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Mr. Ross") {
			@Override
			protected void createDialog() {
			    addGreeting("Witam.");
			    addJob("Szukam mojej córki Susi.");
			    addHelp("Jeżeli potrzebujesz pomocy w znalezieniu jakiegoś budynku w Ados to strażnik Julius da Tobie mapę. Znajdziesz go przy wejściu do miasta.");
			    addOffer("Nie mam nic do zaoferowania, ale w Ados są dwa miejsca, gdzie możesz się posilić - tawerna i bar.");
			    addQuest("Pod koniec października wybieramy się na #Mine #Town #Revival #Weeks");
			    addGoodbye("Do widzenia. Miło było Cię poznać.");

				// Revival Weeks
				add(
					ConversationStates.ATTENDING,
					Arrays.asList("Semos", "Mine", "Town", "Revival", "Weeks"),
					ConversationStates.ATTENDING,
					"Podczas Revival Weeks pod koniec października świętujemy stare i prawie martwe Mine Town na północ od miasta Semos.",
					null);
			}

			/*
			 * (non-Javadoc)
			 * @see games.stendhal.server.entity.npc.SpeakerNPC#onGoodbye(games.stendhal.server.entity.RPEntity)
			 */
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("Oto tata Susi, Mr. Ross. Uspokoił się trochę po ostatniej przygodzie córki.");
		npc.setOutfit(MRROSS_OUTFIT);
		npc.setGender("M");
		npc.setPosition(12, 7);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}

}
