/*
 * @(#) src/games/stendhal/server/entity/area/LifeHealAreaFactory.java
 *
 * $Id$
 */

package games.stendhal.server.entity.mapstuff.area;

import games.stendhal.server.core.config.factory.ConfigurableFactoryContext;

/**
 * A base factory for <code>LifeHealArea</code> objects.
 */
public class LifeHealAreaFactory extends OccupantAreaFactory {
	/**
	 * Extract the damage ratio from a context.
	 * 
	 * @param ctx
	 *            The configuration context.
	 * @return The damage ratio (or 10% is unset).
	 */
	protected double getDamageRatio(final ConfigurableFactoryContext ctx) {
		return ctx.getInt("damage-ratio", 1) / 100.0;
	}

	/**
	 * Extract the minimum damage amount from a context.
	 * 
	 * @param ctx
	 *            The configuration context.
	 * @return The minimum damage amount (or 10 is unset).
	 */
	protected int getMimimumDamage(final ConfigurableFactoryContext ctx) {
		return ctx.getInt("minimum-damage", 1);
	}

	//
	// OccupantAreaFactory
	//

	/**
	 * Creates the OccupantArea.
	 * 
	 * @param ctx
	 *            The configuration context.
	 * @return An OccupantArea.
	 * @throws IllegalArgumentException
	 *             in case of an invalid configuration
	 */
	@Override
	protected OccupantArea createArea(final ConfigurableFactoryContext ctx) {
		return new LifeHealArea(getWidth(ctx), getHeight(ctx),
				getInterval(ctx), getDamageRatio(ctx), getMimimumDamage(ctx));
	}
}
