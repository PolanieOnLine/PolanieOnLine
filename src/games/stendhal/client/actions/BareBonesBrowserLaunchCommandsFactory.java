/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import games.stendhal.client.update.ClientGameConfiguration;

/**
 * Factory to create all known {@link SlashAction}s that open a specified URL in the browser
 *
 * @author madmetzger
 */
class BareBonesBrowserLaunchCommandsFactory {
	private static Map<String, String> commandsAndUrls;

	private static void initialize() {
		String server_page = ClientGameConfiguration.get("DEFAULT_SERVER_WEB");

		commandsAndUrls = new HashMap<String, String>();
		commandsAndUrls.put("beginnersguide", server_page + "/wprowadzenie.html");
		commandsAndUrls.put("faq", server_page + "/faq.html");
		commandsAndUrls.put("manual", server_page + "/wprowadzenie.html");
		commandsAndUrls.put("rules", server_page + "/regulamin/regulamin-gry-polanieonline");
		commandsAndUrls.put("changepassword", server_page + "/account/ustawienia.html");
		commandsAndUrls.put("loginhistory", server_page + "/account/history.html");
		commandsAndUrls.put("merge", server_page + "/account/ustawienia.html");
		commandsAndUrls.put("halloffame", server_page + "/aleja-slaw/all_overview.html");
	}

	/**
	 * creates {@link SlashAction}s for all in initialize specified values
	 *
	 * @return map of the created actions
	 */
	static Map<String, SlashAction> createBrowserCommands() {
		initialize();
		Map<String, SlashAction> commandsMap = new HashMap<String, SlashAction>();
		for(Entry<String, String> entry : commandsAndUrls.entrySet()) {
			commandsMap.put(entry.getKey(), new BareBonesBrowserLaunchCommand(entry.getValue()));
		}
		return commandsMap;
	}

}
