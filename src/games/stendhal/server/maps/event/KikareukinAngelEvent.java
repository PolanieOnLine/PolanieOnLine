/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.event;

import org.apache.log4j.Logger;

public final class KikareukinAngelEvent extends ConfiguredMapEvent {
	private static final Logger LOGGER = Logger.getLogger(KikareukinAngelEvent.class);

	public KikareukinAngelEvent() {
		super(LOGGER, MapEventConfigLoader.load(MapEventConfigLoader.KIKAREUKIN_ANGEL_PREVIEW));
	}
}
