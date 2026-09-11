/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import java.util.ArrayList;
import java.util.List;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.client.gui.npc.NPCInteractionManager;
import games.stendhal.common.NotificationType;

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
		if (!entity.equals(User.get())) {
			return;
		}

		final String npcName = event.get("npc");
		final String npcTitle = event.has("title") ? event.get("title") : null;
		final List<NPCInteractionManager.ChatOption> options = parseOptions(event.get("options"));
		NPCInteractionManager.get().updateChatOptions(npcName, npcTitle, options);

		if (ClientSingletonRepository.getUserInterface().isDebugEnabled()) {
			final StringBuilder sb = new StringBuilder();
			sb.append("Opcje dialogu dla " + npcName + ": ");
			boolean first = true;
			for (final NPCInteractionManager.ChatOption option : options) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(option.getLabel());
			}
			ClientSingletonRepository.getUserInterface().addEventLine(
					new HeaderLessEventLine(sb.toString(), NotificationType.DETAILED));
		}
	}

	private List<NPCInteractionManager.ChatOption> parseOptions(final String raw) {
		final List<NPCInteractionManager.ChatOption> options = new ArrayList<NPCInteractionManager.ChatOption>();
		if ((raw == null) || raw.isEmpty()) {
			return options;
		}
		final String[] entries = raw.split("\t");
		for (final String entry : entries) {
			if ((entry == null) || entry.isEmpty()) {
				continue;
			}
			final String[] parts = entry.split("\\|~\\|");
			if (parts.length < 2) {
				continue;
			}
			final String trigger = parts[0];
			final String label = parts[1];
			final boolean requiresParameters = (parts.length > 2) && "params".equals(parts[2]);
			options.add(new NPCInteractionManager.ChatOption(trigger, label, requiresParameters));
		}
		return options;
	}
}
