/***************************************************************************
 *                 (C) Copyright 2019-2021 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.dragon.wizardtower;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class WizardNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Wizariusz") {
			@Override
			protected void createDialog() {
				addGreeting("Emm... Witaj? Skąd się tutaj wziąłeś młody wojowniku? Czyżby szukasz mojej #pomocy?");
				addHelp("To jest nieco intrygujące, pomóż najpierw mi w pewnym małym #zadaniu, a ja przekażę Tobie coś bardzo wartościowego.");
				addOffer("Będę miał dla Ciebie #zadanie.");
				addGoodbye("Żegnaj wojowniku. Uważaj tylko na niższe piętra tej wieży!");
			}
		};

		npc.setDescription("Oto Wizariusz. Wygląda na mądrego człowieka co nosi spiczastą czapkę na głowie.");
		npc.setEntityClass("wizardnpc");
		npc.setGender("M");
		npc.setPosition(5, 41);
		zone.add(npc);
	}
}
