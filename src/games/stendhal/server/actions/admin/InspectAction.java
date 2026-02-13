/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions.admin;

import static games.stendhal.common.constants.Actions.INSPECT;
import static games.stendhal.common.constants.Actions.TARGET;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import games.stendhal.common.NotificationType;
import games.stendhal.common.constants.InspectMessageConstants;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

public class InspectAction extends AdministrationAction {
	private static final Logger LOGGER = Logger.getLogger(InspectAction.class);
	public static void register() {
		CommandCenter.register(INSPECT, new InspectAction(), 6);
	}

	@Override
	public void perform(final Player player, final RPAction action) {

		final Entity target = getTargetAnyZone(player, action);

		if (target == null) {
			final String text = "Jednostka nie została znaleziona dla akcji" + action;
			player.sendPrivateText(text);
			return;
		}

				if (target instanceof RPEntity) {
			final RPEntity inspected = (RPEntity) target;
			try {
				final JSONObject payload = InspectDataBuilder.build(inspected, String.valueOf(action.get(TARGET)));
				final String json = payload.toJSONString();
				final String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
				player.sendPrivateText(NotificationType.CLIENT, InspectMessageConstants.PREFIX + encoded);
			} catch (final RuntimeException exception) {
				LOGGER.error("Failed to build inspect payload for " + inspected.getTitle(), exception);
				player.sendPrivateText(NotificationType.ERROR,
					"Nie udało się otworzyć okna Inspect dla " + inspected.getTitle() + ".");
			}
			return;
		}

		final StringBuilder st = new StringBuilder();
		st.append("Badana jednostka o id " + action.get(TARGET)
				+ " posiada atrybuty:\r\n");
		st.append(target.toString());
		player.sendPrivateText(st.toString());
	}

}
