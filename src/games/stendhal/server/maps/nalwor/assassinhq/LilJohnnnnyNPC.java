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
package games.stendhal.server.maps.nalwor.assassinhq;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Inside assassin headquarters classroom area.
 */
public class LilJohnnnnyNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildLilJohnnnny(zone);
	}

	private void buildLilJohnnnny(final StendhalRPZone zone) {
		final SpeakerNPC liljohnnnny = new SpeakerNPC("lil johnnnny") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Nie chciałem #go skrzywdzić.");
				addJob("Praca? Teraz nie mogę o tym myśleć.");
				addHelp("Powiedział, że moja matka nosi wojskowe #buty.");
				addOffer("Chcę, aby przeprosił za to co powiedział!");
				addGoodbye("Cóż jeżeli musisz odejść...");
				addQuest("Nie. Nie mam nic dla Ciebie. Może później.");
				addReply(Arrays.asList("him", "go"),"Robił sobie ze mnie #żarty.");
				addReply(Arrays.asList("fun", "żarty"),"Powiedział, że moja mama nosi wojskowe #buty.");
				addReply(Arrays.asList("boots", "buty"),"Pożałuje, że to powiedział!");
			}
		};

		liljohnnnny.setDescription("Oto lil johnnnny, knujący swój następny plan.");
		liljohnnnny.setEntityClass("liljohnnnnynpc");
		liljohnnnny.setPosition(23, 2);
		liljohnnnny.initHP(100);
		zone.add(liljohnnnny);
	}
}
