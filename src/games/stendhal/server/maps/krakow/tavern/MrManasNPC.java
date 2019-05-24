/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.tavern;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/**
 * Build a NPC
 *
 * @author KarajuSs
 */
public class MrManasNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

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
		final SpeakerNPC npc = new SpeakerNPC("Mr Manas") {

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Kiedyś chciałem zostać czarodziejem, ale nie mam do tego talentu.. Dlatego staram się nie poddawać w tej kwesti!");
				addOffer("Skupię od Ciebie #'magię ziemi', #'magię płomieni', #'magię deszczu', #'magię światła' oraz #'magię mroku'.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buymanas")), false);
				addGoodbye("Nie zapomnij dostarczyć dla mnie magii!");
			}
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.UP);
			}
		};

		npc.setDescription("Oto Mr Manas. Kiedyś chciał zostać czarodziejem, jednak okazało się, że nie ma do tego talentu. Lecz nie przestał interesować się magią.");
		npc.setEntityClass("npcmanas");
		npc.setPosition(12, 13);
		npc.setDirection(Direction.UP);
		zone.add(npc);
	}
}
