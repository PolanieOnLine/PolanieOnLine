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
package games.pol.server.maps.gdansk.adventure_house;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.Spot;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.MovementListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.ActiveEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.mapstuff.portal.Teleporter;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.deathmatch.CreatureSpawner;

import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;

public class AdventureCave extends StendhalRPZone {

	private static final Logger logger = Logger.getLogger(AdventureCave.class);


	/** how many creatures will be spawned.*/
	protected static final int NUMBER_OF_CREATURES = 40;
	private static final int MIN1_X = 7;
	private static final int MIN1_Y = 4;
	private static final int MAX1_X = 55;
	private static final int MAX1_Y = 59;

	/** max numbers of fails to place a creature before we just make the island as it is. */
	private static final int ALLOWED_FAILS = 15;
	/** The creatures spawned are between player level * ratio and player level. */
	private static final double LEVEL_RATIO = 0.85;

	private int numCreatures;


	public AdventureCave(final String name, final StendhalRPZone zone, final Player player) {
		super(name, zone);
		init(player);
	}

	private void init(final Player player) {
		Portal portal = new Teleporter(new Spot(player.getZone(), player.getX(), player.getY()));
		portal.setPosition(60, 19);
		add(portal);
		numCreatures = 0;
		int count = 0;
		// max ALLOWED_FAILS fails to place all creatures before we give up
		while (numCreatures < NUMBER_OF_CREATURES && count < ALLOWED_FAILS) {
			int level = Rand.randUniform((int) (player.getLevel() * LEVEL_RATIO), player.getLevel());
			CreatureSpawner creatureSpawner = new CreatureSpawner();
			Creature creature = new Creature(creatureSpawner.calculateNextCreature(level));
				if (StendhalRPAction.placeat(this, creature, Rand.randUniform(MIN1_X, MAX1_X), Rand.randUniform(MIN1_Y, MAX1_Y))) {
					numCreatures++;
				} else {
					logger.info(" could not add a creature to adventure cave: " + creature);
					count++;
				}
		}
		disallowIn();
		this.addMovementListener(new ChallengeMovementListener(player.getX(), player.getY()));
	}

	/**
	 * Get the number of monsters originally created on the zone
	 * @return number of creatures
	 */
	public int getCreatures() {
		return numCreatures;
	}

	private static final class ChallengeMovementListener implements MovementListener {
		private static final Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);
		final int returnX, returnY;

		/**
		 * Create a new ChallengeMovementListener.
		 *
		 * @param x x coordinate of the player return position from the zone
		 * @param y y coordinate of the player return position from the zone
		 */
		ChallengeMovementListener(int x, int y) {
			returnX = x;
			returnY = y;
		}

		@Override
		public Rectangle2D getArea() {
			return area;
		}

		@Override
			public void onEntered(final ActiveEntity entity, final StendhalRPZone zone, final int newX,
								  final int newY) {
				// ignore
			}

		@Override
		public void onExited(final ActiveEntity entity, final StendhalRPZone zone, final int oldX,
							 final int oldY) {
			if (!(entity instanceof Player)) {
				return;
			}
			if (zone.getPlayers().size() == 1) {
				// since we are about to destroy the arena, change the player zoneid to house1 so that
				// if they are relogging,
				// they can enter back to the bank (not the default zone of PlayerRPClass).
				// If they are scrolling out or walking out the portal it works as before.
			    	entity.put("zoneid", "int_gdansk_adventure_house");
			    // Use the correct position from the portal, so that the client
			    // client gets the right coordinates - otherwise they get
			    // overwritten by these, and the client disagrees with the server.
				entity.put("x", returnX);
				entity.put("y", returnY);

					// start a turn notifier counting down to shut down the zone in 15 minutes
					TurnNotifier.get().notifyInSeconds(15*60, new AdventureCaveRemover(zone));
			}
		}

		@Override
		public void onMoved(final ActiveEntity entity, final StendhalRPZone zone, final int oldX,
							final int oldY, final int newX, final int newY) {

			// ignore
		}

		@Override
		public void beforeMove(ActiveEntity entity, StendhalRPZone zone,
				int oldX, int oldY, int newX, int newY) {
			// does nothing, but is specified in the implemented interface
		}

	}
}
