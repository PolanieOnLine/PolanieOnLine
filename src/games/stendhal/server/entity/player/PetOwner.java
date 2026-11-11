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
package games.stendhal.server.entity.player;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.DomesticAnimal;
import games.stendhal.server.entity.creature.Pet;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.creature.Goat;
import marauroa.common.game.RPObject;

/**
 * Handles ownership of pets and sheep.
 *
 * @author hendrik
 */
public class PetOwner {


	/**
	 * The pet ID attribute name.
	 */
	protected static final String ATTR_PET = "pet";

	/**
	 * The sheep ID attribute name.
	 */
	protected static final String ATTR_SHEEP = "sheep";
	protected static final String ATTR_GOAT = "goat";

	private static Logger LOGGER = Logger.getLogger(PetOwner.class);

	private final Player player;

	private final PlayerSheepManager playerSheepManager;
	private final PlayerGoatManager playerGoatManager;
	private final PlayerPetManager playerPetManager;


	public PetOwner(final Player player) {
		this.player = player;
		playerSheepManager = new PlayerSheepManager(player);
		playerGoatManager = new PlayerGoatManager(player);
		playerPetManager = new PlayerPetManager(player);
	}

	public void removeSheep(final Sheep sheep) {
		if (sheep != null) {
			sheep.setOwner(null);
		}

		if (player.has(ATTR_SHEEP)) {
			player.remove(ATTR_SHEEP);
		} else {
			LOGGER.warn("Called removeSheep but player has not sheep: " + this);
		}
	}
	
	public void removeGoat(final Goat goat) {
		if (goat != null) {
			goat.setOwner(null);
		}

		if (player.has(ATTR_GOAT)) {
			player.remove(ATTR_GOAT);
		} else {
			LOGGER.warn("Called removeGoat but player has not goat: " + this);
		}
	}

	public void removePet(final Pet pet) {
		if (pet != null) {
			pet.setOwner(null);
		}

		if (player.has(ATTR_PET)) {
			player.remove(ATTR_PET);
		} else {
			LOGGER.warn("Called removePet but player has not pet: " + this);
		}
	}

	public boolean hasSheep() {
		return player.has(ATTR_SHEEP);
	}
	
	public boolean hasGoat() {
		return player.has(ATTR_GOAT);
	}

	public boolean hasPet() {
		return player.has(ATTR_PET);
	}

	/**
	 * Set the player's pet. This will also set the pet's owner.
	 *
	 * @param pet
	 *            The pet.
	 */
	public void setPet(final Pet pet) {
		player.put(ATTR_PET, pet.getID().getObjectID());
		pet.setOwner(player);
		SingletonRepository.getAchievementNotifier().onPet(player);
	}

	/**
	 * Set the player's sheep. This will also set the sheep's owner.
	 *
	 * @param sheep
	 *            The sheep.
	 */
	public void setSheep(final Sheep sheep) {
		player.put(ATTR_SHEEP, sheep.getID().getObjectID());
		sheep.setOwner(player);
		SingletonRepository.getAchievementNotifier().onPet(player);
	}

	/**
	 * Set the player's goat. This will also set the goat's owner.
	 *
	 * @param goat
	 *            The goat.
	 */
	public void setGoat(final Goat goat) {
		player.put(ATTR_GOAT, goat.getID().getObjectID());
		goat.setOwner(player);
		SingletonRepository.getAchievementNotifier().onPet(player);
	}

	/**
	 * Get the player's sheep.
	 *
	 * @return The sheep.
	 */
	public Sheep getSheep() {
		return getAnimal(ATTR_SHEEP, Sheep.class);
	}
	
	public Goat getGoat() {
		return getAnimal(ATTR_GOAT, Goat.class);
	}

	public Pet getPet() {
		return getAnimal(ATTR_PET, Pet.class);
	}

	private <T extends DomesticAnimal> T getAnimal(final String attribute, final Class<T> type) {
		if (!player.has(attribute) || !player.has("zoneid")) {
			return null;
		}
		final RPObject object = SingletonRepository.getRPWorld().get(
				new RPObject.ID(player.getInt(attribute), player.get("zoneid")));
		if (!type.isInstance(object)) {
			return null;
		}
		final T animal = type.cast(object);
		// IDs are local to a zone and can later belong to an unrelated object.
		return animal.getOwner() == player ? animal : null;
	}

	public void destroy() {
		final Sheep sheep = player.getSheep();
		final Goat goat = player.getGoat();

		if (sheep != null) {
			sheep.getZone().remove(sheep);

			/*
			 * NOTE: Once the sheep is stored there is no more trace of zoneid.
			 */
			playerSheepManager.storeSheep(sheep);
		} else {
			// Bug on pre 0.20 released
			if (player.hasSlot("#flock")) {
				player.removeSlot("#flock");
			}
		}

		if (goat != null) {
			goat.getZone().remove(goat);
			playerGoatManager.storeGoat(goat);
		} else {
			if (player.hasSlot("#goat")) {
				player.removeSlot("#goat");
			}
		}

		final Pet pet = player.getPet();

		if (pet != null) {
			pet.getZone().remove(pet);

			/*
			 * NOTE: Once the pet is stored there is no more trace of zoneid.
			 */
			playerPetManager.storePet(pet);
		}
	}

	public Pet retrievePet() {
		return playerPetManager.retrievePet();
	}

	public Sheep retrieveSheep() {
		return playerSheepManager.retrieveSheep();
	}

	public Goat retrieveGoat() {
		return playerGoatManager.retrieveGoat();
	}
}
