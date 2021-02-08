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
		commandsAndUrls.put("beginnersguide", "https://polanieonline.eu/wprowadzenie.html");
		commandsAndUrls.put("faq", "https://polanieonline.eu/faq.html");
		commandsAndUrls.put("manual", "https://polanieonline.eu/wprowadzenie.html");
		commandsAndUrls.put("rules", "https://polanieonline.eu/regulamin/regulamin-gry-polanieonline-2.html");
		commandsAndUrls.put("changepassword", "https://polanieonline.eu/account/ustawienia.html");
		commandsAndUrls.put("loginhistory", "https://polanieonline.eu/account/history.html");
		commandsAndUrls.put("merge", "https://polanieonline.eu/account/ustawienia.html");
		commandsAndUrls.put("halloffame", "https://polanieonline.eu/aleja-slaw/all_overview.html");
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
