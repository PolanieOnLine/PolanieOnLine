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
package games.stendhal.server.maps.semos.tavern;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

public class StichardRallmanNPC implements ZoneConfigurator {
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
		final SpeakerNPC stallman = new SpeakerNPC("Stichard Rallman") {

			@Override
            public void say(final String text) {
				setDirection(Direction.DOWN);
				super.say(text, false);
			}

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witojcie w #'PolskaGRA'! Grać możecie bez płacenia dudków");
				addJob("Głoszę idee #'wolnego oprogramowania'!");
				addHelp("Pomóż #POL być jeszcze lepszym. Poświęć swój czas, powiedz znajomym, aby zagrali, twórz mapy.");
				addReply(Arrays.asList("wolnego", "wolnym", "free", "wolnego oprogramowania"),
					"#'Wolne oprogramowanie' oznacza wolność tworzenia, a nie tylko brak opłat. Aby zrozumieć koncepcję to powinieneś się zastanowić nad #'wolnością' jak w #'wolności słowa', a nie jak w ''darmowym piwie''.");
				addReply(Arrays.asList("pol", "polska", "polskagra", "gra", "polskaonline"),
					"PolskaGRA, PolskaOnLine, Stendhal są właśnie #'wolnym oprogramowaniem' na licencji #'GNU GPL'. Możesz ją uruchamiać, kopiować, dystrybuować, studiować, zmieniać i poprawiać to oprogramowanie.");
				addReply("gnu", "http://www.gnu.org/");
				addReply("gpl", "http://www.gnu.org/licenses/gpl.html");

				addGoodbye();
			}
		};

		stallman.setEntityClass("richardstallmannpc");
		stallman.setDescription("Stichard Rallman wie wszystko o wolnym oprogramowaniu i licencjach.");
		stallman.setPosition(26, 11);
		stallman.setDirection(Direction.DOWN);
		stallman.initHP(100);
		zone.add(stallman);
	}
}
