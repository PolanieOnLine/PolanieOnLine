/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item.scroll;

import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Pet;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
//import games.stendhal.server.entity.player.PetOwner;;

/**
 * Represents an empty/blank pet scroll.
 */
public class BlankPetScroll extends Scroll {

	// private static final Logger logger = Logger.getLogger(EmptyScroll.class);

	/**
	 * Creates a new blank pet scroll.
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public BlankPetScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public BlankPetScroll(final BlankPetScroll item) {
		super(item);
	}

	/**
	 * Use a blank pet scroll.
	 *
	 * @param player
	 * @return always true
	 */
	@Override
	protected boolean useScroll(final Player player) {
		final StendhalRPZone zone = player.getZone();
		final String petName = player.getPet().getName();

		if (zone.isTeleportInAllowed(player.getX(), player.getY())) {
			final Item summonPetScroll = SingletonRepository.getEntityManager().getItem(
					"zwój przywołania zwierzątka");
			summonPetScroll.setItemData(petName);
			player.equipOrPutOnGround(summonPetScroll);

			final Pet pet = player.getPet();

			if (pet != null) {
				//petOwner.storePet(pet);
				player.removePet(pet);
				pet.getZone().remove(pet);
				player.sendPrivateText("O dziwo zwierzątko dołączyło za pomocą zwoju. Po prostu weszło na stronę!");
			}

			return true;
		} else {
			player.sendPrivateText("Silna antymagiczna aura na tym obszarze blokuje działanie tego zwoju!");
			return false;
		}
	}

}
