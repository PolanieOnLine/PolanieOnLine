/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
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
 * Handles top-center announcement banners.
 */
class ScreenAnnouncementEvent extends Event<RPEntity> {
	@Override
	public void execute() {
		entity.onScreenAnnouncement(
				event.has("title") ? event.get("title") : "",
				event.has("text") ? event.get("text") : "",
				event.has("category") ? event.get("category") : "default");
	}
}
