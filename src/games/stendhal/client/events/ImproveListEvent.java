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
 * Receives an improvement list for a specific NPC and forwards it to the dialog.
 */
public class ImproveListEvent extends Event<RPEntity> {
	@Override
	public void execute() {
		if (!event.has("npc") || !event.has("items")) {
		return;
	}
		ItemImprovementController.showList(event.get("npc"), event.get("items"));
	}
}
