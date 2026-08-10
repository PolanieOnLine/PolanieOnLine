/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.client.events;

import games.stendhal.client.entity.Entity;
import games.stendhal.client.gui.ItemUpgradeWindow;

/** Opens or refreshes the desktop item-upgrade window. */
final class ItemUpgradeEvent extends Event<Entity> {
	@Override
	public void execute() {
		ItemUpgradeWindow.show(event);
	}
}
