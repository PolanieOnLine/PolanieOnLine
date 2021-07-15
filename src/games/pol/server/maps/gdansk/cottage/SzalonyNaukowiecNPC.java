/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.gdansk.cottage;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * @author KarajuSs
 */
public class SzalonyNaukowiecNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Aron") {

			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			public void createDialog() {
				addJob("Jestem naukowcem i pracuję nad bardzo #'mocnym eliksirem', który uleczy nawet umarłych. Jeszcze nie mam pojęcia jak go nazwać!");
				addReply(Arrays.asList("mocny eliksir", "mocnym eliksirem", "eliksirem"),
						"Taki eliksir potrafiłby uleczyć nawet umarłych. *HIHIHIHI*");
				addReply("królikiem doświadczalnym",
						"Tak... *HIHIHI*.. królikiem... Jak moja #receptura się sprawdzi możesz doznać bardzo mocnego ożywienia, dodatkowej siły.");
				addReply("receptura",
						"Moja TAJNA receptura na #'duży smoczy eliksir'. *HIHIHIHI*");
				addOffer("Mogę sporządzić dla Ciebie #'duży smoczy eliksir'. Do tego będę potrzebował 3 razy #'krew smoka' oraz bardzo dużo pieniędzy! *HIHIHI* Powiedz tylko #sporządź.");
				addReply("duży smoczy eliksir",
						"Jest to bardzo i to bardzo silny eliksir. *HIHIHIHI* Jeżeli chcesz to poproś mnie, abym go przyrządził mówiąc #'sporządź 1 duży smoczy eliksir'.");
				addHelp("Jeżeli chcesz być mądry tak jak ja to powinieneś odwiedzić bibliotekę. Tam jest sporo pomocy naukowych.");
				addGoodbye("Do widzenia.");

				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 1200);
				requiredResources.put("smocza krew", 3);
				final ProducerBehaviour behaviour = new ProducerBehaviour("aron_concoct_potion",
						Arrays.asList("concoct", "sporządź"), "duży smoczy eliksir", requiredResources, 2 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"Witaj poszukiwaczu, co Ciebie sprowadza do mojej TAJNEJ kryjówki? Chcesz być moim #'królikiem doświadczalnym'?");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.UP);
			}
		};

		npc.setDescription("Oto Aron, odziany w jakąś dziwną białą szatą. Wygląda na jakiegoś szalonego naukowca, lepiej z nim długo nie rozmawiać.");
		npc.setEntityClass("madscientistnpc");
		npc.setGender("M");
		npc.setDirection(Direction.UP);
		npc.setPosition(26, 8);
		zone.add(npc);
	}
}
