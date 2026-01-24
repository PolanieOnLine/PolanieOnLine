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

import java.util.ArrayList;
import java.util.List;

import games.stendhal.client.ChatOptions;
import games.stendhal.client.ChatOptions.Option;
import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
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

		List<Option> parsedOptions = parseOptions(event.get("options"));
		String npcName = event.get("npc");
		String title = event.get("title");
		ChatOptions.update(npcName, title, parsedOptions);

		if (ClientSingletonRepository.getUserInterface().isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append("Opcje dialogu dla " + npcName + ": ");
			boolean first = true;
			for (Option option : parsedOptions) {
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

		if (ChatOptions.getAttendingNPC() == null) {
			if (ClientSingletonRepository.getUserInterface().isChatOptionsDialogVisible()) {
				ClientSingletonRepository.getUserInterface().closeChatOptionsDialog();
			}
			return;
		}

		if (ClientSingletonRepository.getUserInterface().isChatOptionsDialogVisible()) {
			ClientSingletonRepository.getUserInterface().refreshChatOptionsDialog();
		}
	}

	private List<Option> parseOptions(final String rawOptions) {
		List<Option> options = new ArrayList<Option>();
		if (rawOptions == null || rawOptions.isEmpty()) {
			return options;
		}
		String[] optionsList = rawOptions.split("\t");
		for (String optionListEntry : optionsList) {
			if (optionListEntry == null || optionListEntry.isEmpty()) {
				continue;
			}
			String[] option = optionListEntry.split("\\|~\\|", -1);
			String trigger = option.length > 0 ? option[0] : "";
			String label = option.length > 1 ? option[1] : trigger;
			options.add(new Option(trigger, label));
		}
		return options;
	}
}
