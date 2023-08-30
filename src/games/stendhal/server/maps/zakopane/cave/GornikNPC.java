/***************************************************************************
 *                 (C) Copyright 2020-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.cave;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */
public class GornikNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Górnik") {
			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Aktualnie zajmuję się #wykopaliskami. Jeśli masz ochotę #pomóc, możesz podnieść mój stary kilof, który leży w chatce na stole.");
				addHelp("Być może będę potrzebował pomocy od takiego rycerza w pewnym #zadaniu.");
				addGoodbye();

				addReply("wykopaliskami",
						"Próbuję uzyskać informacje o właściwościach wszystkich surowców jakie można wydobyć mym #kilofem.");
				addReply("kilofem",
						"Kilof jest potrzebny, by wykopać rudę surowca ze złoża. Ja bynajmniej znam tylko #'zardzewiały kilof', #'kilof', #'kilof stalowy', #'kilof złoty' oraz #'kilof obsydianowy'.");

				addReply("zardzewiały kilof",
						"Zardzewiały kilof jest najsłabszym kilofem, cały zardzewiały, trzon spruchniały. Nim trzeba sporo się napocić, aby cokolwiek wykopać oraz trzeba napomnieć iż nie wykopiesz każdego minerału tym kilofem. Średnio potrzeba 30 sekund na wydobycie surowca.");
				addReply("kilof",
						"Zwyczajny kilof, większość surowców da się nim wykopać. Niestety, lecz drewniany trzon nie pozwala na mocne zamachnięcie się przez co czas trwania wydobywania średnio wynosi 20 sekund.");
				addReply("kilof stalowy",
						"Zwykły kilof z wzmocnionym trzonem ze stali, który pozwala na wydobywanie z większości złóż. Średnio potrzeba 16 sekund na wydobycie.");
				addReply("kilof złoty",
						"Kilof stalowy, lecz ostrze wykonane ze złota, przez co wygląda obłędnie oraz pozwala na wydobywanie z nieco trwadszych złóż. Średnio potrzeba 12 sekund na wydobycie.");
				addReply("kilof obsydianowy",
						"Najdoskonalszy kilof jaki kiedykolwiek widziałem na swe oczy. Otrze wykonane z obsydianu przez co jest najwytrzymalsze, ostrze jest tak ostre, że istnieje szansa na wydobycie podwójnej ilości surowca ze złoża. Tym kilofem można wykopać każdy minerał jaki istnieje, a średni czas wydobywania wynosi 6 sekund.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.RIGHT);
			}
		};

		npc.setDescription("Oto Górnik. Może opowie nam o różnych kilofach.");
		npc.setEntityClass("npcgornik");
		npc.setGender("M");
		npc.setDirection(Direction.RIGHT);
		npc.setPosition(18, 70);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
