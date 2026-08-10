/***************************************************************************
 *                 (C) Copyright 2024 - PolanieOnLine                 *
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

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.UserInterface;

/**
 * Opens the producer window when instructed by the server.
 */
class ProducerWindowEvent extends Event<RPEntity> {
	@Override
	public void execute() {
		UserInterface ui = ClientSingletonRepository.getUserInterface();
		if (ui == null) {
			return;
		}

		String npcName = event.has("npc") ? event.get("npc") : null;
		String npcTitle = event.has("title") ? event.get("title") : null;
		ui.showProducerWindow(npcName, npcTitle);
	}
}
