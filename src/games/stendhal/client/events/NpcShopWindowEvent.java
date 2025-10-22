/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
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

import javax.swing.SwingUtilities;

import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.shop.NpcShopWindowManager;
import marauroa.common.game.RPEvent;

class NpcShopWindowEvent extends Event<RPEntity> {
	@Override
	public void execute() {
		final RPEvent currentEvent = event;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				NpcShopWindowManager.get().handle(currentEvent);
			}
		});
	}
}
