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
package games.stendhal.client.events;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.common.NotificationType;
import games.stendhal.client.gui.ArenaObservationWindow;

/**
 * Chat options
 *
 * @author hendrik
 */
class ChatOptionsEvent extends Event<RPEntity> {
	/**
	 * executes the event
	 */
	@Override
	public void execute() {
		if (entity.equals(User.get()) && event.has("arena_phase")
				&& "__arena_observation__".equals(event.get("npc"))) {
			ArenaObservationWindow.show(event);
			return;
		}
		if (!ClientSingletonRepository.getUserInterface().isDebugEnabled()) {
			return;
		}

		if (!entity.equals(User.get())) {
			return;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("Opcje dialogu dla " + event.get("npc") + ": ");
		String[] optionsList = event.get("options").split("\t");
		boolean first = true;
		for (String optionListEntry : optionsList) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			String[] option = optionListEntry.split("\\|~\\|");
			sb.append(option[1]);
		}
		ClientSingletonRepository.getUserInterface().addEventLine(new HeaderLessEventLine(sb.toString(), NotificationType.DETAILED));
	}
}
