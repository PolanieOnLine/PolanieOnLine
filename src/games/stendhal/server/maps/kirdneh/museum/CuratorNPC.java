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
package games.stendhal.server.maps.kirdneh.museum;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds a Curator NPC in Kirdneh museum .
 *
 * @author kymara
 */
public class CuratorNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Hazel") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam w Muzeum Kirdneh.");
				addJob("Jestem kuratorem tego muzeum. Oznacza to, że ja organizuję wystawy i szukam nowych #eksponatów.");
				addHelp("To miejsce jest na rzadkie artefakty i specjalne #eksponaty.");
				addReply(Arrays.asList("exhibits", "eksponaty", "eksponatów"),"Być może będziesz mieć dryg do wyszukiwania przedmiotów i chciałbyś zrobić #zadanie dla mnie.");
				// remaining behaviour defined in games.stendhal.server.maps.quests.WeeklyItemQuest
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("kirdnehscrolls")));
				addGoodbye("Dowidzenia. Miło się z tobą rozmawiało.");
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.RIGHT);
			}
		};

		npc.setEntityClass("curatornpc");
		npc.setPosition(2, 38);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		npc.setDescription("Oto Hazel, kustosz muzeum Kirdneh.");
		zone.add(npc);
	}
}
