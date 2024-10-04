/***************************************************************************
 *                   (C) Copyright 2003-2024 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.spawner;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;

/**
 * A PassiveEntityRespawnPoint basically is a 1x1 area where a plant, a fruit or
 * another non-moving thing grows. This growing thing is a pickable Item (e.g. a
 * mushroom, an apple); by extending this class, it can also grow something
 * special (e.g. SheepFood).
 *
 * PassiveEntityRespawnPoint are currently invisible (fully transparent) on the
 * client side. Extend GrowingPassiveEntityRespawnPoint and implement
 * UseListener if grown item should not be dragable without special interaction.
 *
 * @author Daniel Herding
 *
 */
public class PassiveEntityRespawnPoint extends Entity implements TurnListener {
	private static Logger LOGGER = Logger.getLogger(PassiveEntityRespawnPoint.class);
	/**
	 * Tells how many turns it takes in average for a new fruit to become ripe.
	 */
	protected int meanTurnsForRegrow;

	/**
	 * Is there still a fruit that has not yet been picked up?
	 */
	protected boolean hasPickableFruit;

	/**
	 * The name of the fruit (Item) that is grown by the PlantGrower.
	 */
	private final String growingItemName;

	/** Initializes spawner & sets to full growth when added to zone if <code>true</code>. */
	private final boolean initOnAdded;


	/**
	 * Creates an item spawner.
	 *
	 * @param object
	 * @param growingItemName
	 *     Name of item to be spawned.
	 * @param meanTurnsForRegrow
	 *     Average number of turns for item to spawn.
	 */
	public PassiveEntityRespawnPoint(final RPObject object, final String growingItemName,
			final int meanTurnsForRegrow) {
		super(object);
		this.growingItemName = growingItemName;
		this.meanTurnsForRegrow = meanTurnsForRegrow;
		this.initOnAdded = false;
		setDescription("Wygląda na to, że rośnie tutaj "
				+ growingItemName + ".");

		setRPClass("plant_grower");
		put("type", "plant_grower");

		setResistance(0);
		// update();
	}

	/**
	 * Creates an item spawner.
	 *
	 * @param growingItemName
	 *     Name of item to be spawned.
	 * @param meanTurnsForRegrow
	 *     Average number of turns for item to spawn.
	 */
	public PassiveEntityRespawnPoint(final String growingItemName, final int meanTurnsForRegrow) {
		this(growingItemName, meanTurnsForRegrow, false);
	}

	/**
	 * Creates an item spawner.
	 *
	 * @param growingItemName
	 *     Name of item to be spawned.
	 * @param meanTurnsForRegrow
	 *     Average number of turns for item to spawn.
	 * @param initOnAdded
	 *     If <code>true</code>, sets to full grown and initializes respawn timer when added to zone.
	 */
	public PassiveEntityRespawnPoint(final String growingItemName, final int meanTurnsForRegrow,
			final boolean initOnAdded) {
		this.growingItemName = growingItemName;
		this.meanTurnsForRegrow = meanTurnsForRegrow;
		this.initOnAdded = initOnAdded;
		setDescription("Wygląda na to, że rośnie tutaj "
				+ growingItemName + ".");

		setRPClass("plant_grower");
		put("type", "plant_grower");

		setResistance(0);
	}

	public static void generateRPClass() {
		final RPClass grower = new RPClass("plant_grower");
		grower.isA("entity");
		grower.addAttribute("class", Type.STRING);
	}

	@Override
	public void onAdded(final StendhalRPZone zone) {
		super.onAdded(zone);
		zone.addPlantGrower(this);
		if (initOnAdded) {
			setToFullGrowth();
		}
	}

	@Override
	public void onRemoved(final StendhalRPZone zone) {
		super.onRemoved(zone);
		zone.removePlantGrower(this);
	}

	/**
	 * Is called when a fruit has been picked from this plant grower.
	 *
	 * @param picked
	 *            The fruit that has been picked. Use null for subclasses of
	 *            PlantGrower that don't use items as fruits.
	 */
	public void onFruitPicked(final Item picked) {
		hasPickableFruit = false;
		if (picked != null) {
			picked.setPlantGrower(null);
		}
		SingletonRepository.getTurnNotifier().notifyInTurns(getRandomTurnsForRegrow(), this);
	}

	protected int getRandomTurnsForRegrow() {
		return Rand.randGaussian(meanTurnsForRegrow, (int) (0.1 * meanTurnsForRegrow));
	}

	/**
	 * Creates a new fruit.
	 */
	protected Item growNewFruit() {
		Item grownItem = null;
		if (!hasPickableFruit) {
			LOGGER.debug("Growing " + growingItemName);

			final StendhalRPWorld world = SingletonRepository.getRPWorld();
			final StendhalRPZone zone = world.getZone(getID().getZoneID());

			// create a new grown item
			grownItem = SingletonRepository.getEntityManager().getItem(growingItemName);
			grownItem.setPlantGrower(this);
			grownItem.setPosition(getX(), getY());
			grownItem.setFromCorpse(true);

			// The item should not expire to avoid unnecessary loop of spawning
			// and expiring
			zone.add(grownItem, false);
			hasPickableFruit = true;
		}
		return grownItem;
	}

	public void setToFullGrowth() {
		if (!hasPickableFruit) {
			growNewFruit();
		}
		// don't grow anything new until someone picks a fruit
		SingletonRepository.getTurnNotifier().dontNotify(this);
	}

	@Override
	public void onTurnReached(final int currentTurn) {
		growNewFruit();
	}

	public void setStartState() {
		onFruitPicked(null);
	}

	public void onItemPickedUp(Player player) {
		player.incHarvestedForItem(growingItemName, 1);
	}

	/**
	 * Retrieves name of item that is grown/spawned.
	 */
	public String getItemName() {
		return growingItemName;
	}
}
