/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2024 - Stendhal                        *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.events;

import games.stendhal.client.entity.RPEntity;

/**
 * Placeholder handler for improver offer payloads.
 */
class ImproverOfferEvent extends Event<RPEntity> {
	@Override
	public void execute() {
		// no-op (handled by web client); keep to avoid unknown event warnings
	}
}
