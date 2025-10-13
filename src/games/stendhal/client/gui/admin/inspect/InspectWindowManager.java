/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.admin.inspect;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import games.stendhal.client.gui.j2DClient;
import games.stendhal.common.constants.InspectMessageConstants;

/**
 * Manages the Inspect player window for administrators.
 */
public final class InspectWindowManager {
	private static final Logger LOGGER = Logger.getLogger(InspectWindowManager.class);
	private static final InspectWindowManager INSTANCE = new InspectWindowManager();

	private InspectWindow window;

	private InspectWindowManager() {
	}

	public static InspectWindowManager getInstance() {
		return INSTANCE;
	}

	public boolean handleMessage(final String text) {
		if (text == null || !text.startsWith(InspectMessageConstants.PREFIX)) {
			return false;
		}

		final String encoded = text.substring(InspectMessageConstants.PREFIX.length());
		try {
			final byte[] decodedBytes = Base64.getDecoder().decode(encoded);
			final String json = new String(decodedBytes, StandardCharsets.UTF_8);
			final Object parsed = JSONValue.parse(json);
			if (!(parsed instanceof JSONObject)) {
				LOGGER.error("Odebrano nieprawidłowy format danych inspect");
				return true;
			}
			final InspectData data = InspectData.fromJson((JSONObject) parsed);
			display(data);
		} catch (final IllegalArgumentException exception) {
			LOGGER.error("Nie można zdekodować danych inspect", exception);
		}
		return true;
	}

	private void display(final InspectData data) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (window == null) {
					window = new InspectWindow();
				}
				window.updateData(data);
				attachAndShow();
			}
		});
	}

	private void attachAndShow() {
		final j2DClient ui = j2DClient.get();
		if (ui == null) {
			LOGGER.warn("Interfejs klienta nie jest jeszcze dostępny, nie można wyświetlić okna inspect");
			return;
		}

		if (window.getParent() == null) {
			ui.addWindow(window);
			window.center();
		}

		window.setVisible(true);
	}
}
