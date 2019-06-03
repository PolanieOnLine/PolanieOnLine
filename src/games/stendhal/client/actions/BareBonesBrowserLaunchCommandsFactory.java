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

/**
 * Factory to create all known {@link SlashAction}s that open a specified URL in the browser
 *
 * @author madmetzger
 */
class BareBonesBrowserLaunchCommandsFactory {

	private static Map<String, String> commandsAndUrls;

	private static void initialize() {
		commandsAndUrls = new HashMap<String, String>();
		commandsAndUrls.put("beginnersguide", "http://www.polanieonline.eu/player-guide/dla-poczatkujacych.html");
		commandsAndUrls.put("faq", "http://www.polanieonline.eu/player-guide/faq.html");
		commandsAndUrls.put("manual", "http://www.polanieonline.eu/player-guide/przewodnik.html");
		commandsAndUrls.put("rules", "http://www.polanieonline.eu/rules/regulamin-gry-polanieonline-mmorpg.html");
		commandsAndUrls.put("changepassword", "http://www.polanieonline.eu/account/change-password.html");
		commandsAndUrls.put("loginhistory", "http://www.polanieonline.eu/account/history.html");
		commandsAndUrls.put("merge", "http://www.polanieonline.eu/account/merge.html");
		commandsAndUrls.put("halloffame", "http://www.polanieonline.eu/world/hall-of-fame/active_overview.html");
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
