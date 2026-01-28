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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.ImproverOfferWindow;

/**
 * Handles improver offer payloads.
 */
class ImproverOfferEvent extends Event<RPEntity> {
	private static ImproverOfferWindow activeWindow;

	@Override
	public void execute() {
		if (!event.has("payload")) {
			return;
		}
		final Object parsed = JSONValue.parse(event.get("payload"));
		if (!(parsed instanceof JSONObject)) {
			return;
		}
		final JSONObject payload = (JSONObject) parsed;
		final String action = (String) payload.get("action");
		if ("close".equals(action)) {
			closeActiveWindow();
			return;
		}

		final String item = (String) payload.get("item");
		final Long level = (Long) payload.get("level");
		final Long chance = (Long) payload.get("chance");
		final String costText = (String) payload.get("costText");
		final JSONArray resources = (JSONArray) payload.get("resources");
		if (item == null || level == null || chance == null || resources == null) {
			return;
		}

		closeActiveWindow();
		activeWindow = new ImproverOfferWindow("Ulepszenia");
		activeWindow.setOffer(item, level.intValue(), chance.intValue(), costText, resources);
		activeWindow.open();
	}

	private void closeActiveWindow() {
		if (activeWindow != null) {
			activeWindow.close();
			activeWindow = null;
		}
	}
}
