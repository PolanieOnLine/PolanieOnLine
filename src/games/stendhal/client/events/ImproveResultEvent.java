/***************************************************************************
 *                      (C) Copyright 2024 - PolanieOnLine                 *
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
import games.stendhal.client.gui.improvement.ItemImprovementController;

/**
 * Handles the result of a single improvement attempt.
 */
public class ImproveResultEvent extends Event<RPEntity> {
	@Override
	public void execute() {
		if (!event.has("npc") || !event.has("success") || !event.has("message")) {
		return;
	}
		final Object successObj = event.get("success");
		final boolean success = Boolean.TRUE.equals(successObj)
		|| (successObj != null && Boolean.parseBoolean(successObj.toString()));
		ItemImprovementController.handleResult(event.get("npc"), success, event.get("message"));
	}
}
