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

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Creates Mircea, an old witch who lives in Imorgens house, Fado forest.
 *
 * @author Vanessa Julius
 */
public class OldWitchNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Mircea") {

			@Override
			protected void createPath() {
				setPath(null);
			
			}
			

			@Override
			protected void createDialog() {
				addGreeting("Cześć *Kaszlnięcie*");
				addJob("Byłam potężną czarownicą, ale zaryzykowałam i straciłam moje moce walcząc z paroma #wilkołakami.");
				addReply(Arrays.asList("werewolves", "wilkołakami"), "Są naprawdę niebezpieczne! Wcześniej spotkałam kilka na drodze do Kikareukin i zostałam bardzo otruta. Dlatego muszę tutaj odpocząć.");
				addQuest("Oh wyglądasz na bardzo miłą i ufną osobę, ale nie mam dla Ciebie żadnej pracy.");
				addHelp("Może moja siostra #Imorgen, która jest na zewnątrz może ci pomóc. Wie dużo o ludziach w Faiumoni.");
				addReply("Imorgen", "Mam nadzieje, że w przyszłości będzie poteżną czarownicą jak ja, ale wciąż musi się dużo nauczyć.");
				addOffer("Chciałabym ci sprzedać jedną z moich znanych mikstur, ale nie mogę *kaszlnięcie*.");
				addGoodbye("Mam nadzieje, że nie długo się spotkamy *westchnięcie*.");
			}
		};

		npc.setEntityClass("oldwitchnpc");
		npc.setPosition(12, 4);
		npc.initHP(100);
		npc.setDescription("Oto Mircea. Jest starszą bliźniaczą siostrą czarownicą, która z każdą sekundą wygląda na słabszą.");
		zone.add(npc);
	}
}
