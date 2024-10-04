/***************************************************************************
 *                 Copyright © 2023-2024 - Faiumoni e. V.                  *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityinterior.accessoryshop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.mapstuff.sign.OutfitShopSign;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.shop.OutfitShopInventory;

/**
 * An NPC that sells special accessories for player outfits that do not expire.
 */
public class DealerNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		zone.add(buildNPC());
		zone.add(buildSign());
	}

	private SpeakerNPC buildNPC() {
		final SpeakerNPC gwen = new SpeakerNPC("Gwen");
		gwen.setEntityClass("gwennpc");
		gwen.setGender("F");

		gwen.setDescription("Oto Gwen. Zajmuje się modnymi dodatkami.");
		gwen.addGreeting();
		gwen.addOffer("Proszę sprawdzić księgę na krześle, aby zapoznać się z listą akcesoriów, które oferuję.");
		gwen.addGoodbye();

		gwen.setPathAndPosition(new FixedPath(Arrays.asList(
				new Node(14, 4),
				new Node(14, 7),
				new Node(11, 7),
				new Node(11, 10),
				new Node(6, 10),
				new Node(6, 6),
				new Node(9, 6),
				new Node(9, 4)
			), true));
		gwen.addSuspend(30, Direction.DOWN, 4);

		return gwen;
	}

	private OutfitShopSign buildSign() {
		// TODO: move to XML
		final OutfitShopInventory inventory = SingletonRepository.getOutfitShopsList().get("deniran_accessories");
		final Map<String, String> addLayers = new HashMap<>();
		if (inventory != null) {
			for (final String name: inventory.keySet()) {
				addLayers.put(name, "body=0,head=0,eyes=0,dress=11,hair=11");
			}
		}
		final OutfitShopSign sign = new OutfitShopSign("deniran_accessories", "Deniran - Akcesoria",
				"Gwen sprzedaje następujące akcesoria", addLayers, true);
		sign.setPosition(12, 2);
		sign.setEntityClass("book_purple");
		return sign;
	}
}