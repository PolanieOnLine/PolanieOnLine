/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.kalavan.citygardens;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds the gardener in Kalavan city gardens.
 *
 * @author kymara
 */
public class GardenerNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Sue") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(100, 123));
				nodes.add(new Node(110, 123));
				nodes.add(new Node(110, 110));
				nodes.add(new Node(119, 110));
				nodes.add(new Node(119, 122));
				nodes.add(new Node(127, 122));
				nodes.add(new Node(127, 111));
				nodes.add(new Node(118, 111));
				nodes.add(new Node(118, 123));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addReply(ConversationPhrases.YES_MESSAGES, "Bardzo ciepło...");
				addReply(ConversationPhrases.NO_MESSAGES, "Lepszy niż deszczowy!");
				addJob("Jestem ogrodnikiem. Mam nadzieję, że podobają Ci się rabatki.");
				addHelp("Jeżeli przyniesiesz mi #drugie #śniadanie to wymienię go na magiczny zwój. Powiedz tylko #wymień.");
				addOffer("Moje pomidory i czosnek mają się dobrze. Mam wystarczająco dużo, aby trochę sprzedać."
						+ " Mógłbym również *kaszel* sprzedać trochę papryki habanero i fasoli pinto..."
						+ " Jeśli chciałbyś #kupić specjalne składniki na skromny posiłek!");
				addReply(Arrays.asList("drugie", "śniadanie", "lunch"),
						"Poproszę filiżankę herbaty!");
				addReply(Arrays.asList("sandwich", "kanapka"),
						"Mmm.. Chciałbym z szynką i serem.");
				addReply(Arrays.asList("zwój kalavan", "scroll"),
						"To magiczny zwój, który może cię zabrać do Kalavan. Nie pytaj mnie jak działa!");
				addQuest("Kocham filiżankę #herbaty. Przy uprawianiu ogrodu zawsze chce się pić."
						+ " Jeżeli przyniesiesz mi też #sandwich to wymienię ją za magiczny zwój."
						+ " Powiedz tylko #wymień.");
				addReply(Arrays.asList("tea", "cup of tea", "herbata", "herbaty", "filiżanka herbaty"),
						"Starsza Granny Graham może zaparzyć filiżankę herbaty. Ona jest tam w tej dużej chacie.");
				addGoodbye("Do widzenia. Podziwiaj resztę ogrodów.");
			}
		};

		npc.setDescription("Oto Sue. Jej kwiaty są przepiękne. Ona naprawdę ma do tego rękę.");
		npc.setEntityClass("gardenernpc");
		npc.setGender("F");
		npc.setPosition(100, 123);
		zone.add(npc);
	}
}
