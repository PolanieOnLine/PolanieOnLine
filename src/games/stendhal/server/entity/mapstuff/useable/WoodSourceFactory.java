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
package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.server.core.config.factory.ConfigurableFactory;
import games.stendhal.server.core.config.factory.ConfigurableFactoryContext;

/**
 * A factory for <code>WoodSource</code> objects.
 */
public class WoodSourceFactory implements ConfigurableFactory {

	/**
	 * Extract the species name from a context.
	 * 
	 * @param ctx
	 *            The configuration context.
	 * @return The species name.
	 * @throws IllegalArgumentException
	 *             If the attribute is invalid.
	 */
	protected String getSpecies(final ConfigurableFactoryContext ctx) {
		return ctx.getRequiredString("species");
	}

	//
	// ConfigurableFactory
	//

	/**
	 * Create a personal wood source.
	 * 
	 * @param ctx
	 *            Configuration context.
	 * 
	 * @return A WoodSource.
	 * 
	 * @see WoodSource
	 */
	public Object create(final ConfigurableFactoryContext ctx) {
		return new WoodSource(getSpecies(ctx));
	}
}
