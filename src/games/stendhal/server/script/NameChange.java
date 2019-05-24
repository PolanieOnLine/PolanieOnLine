/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.List;

import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import marauroa.common.Configuration;

/**
 * Makes client display a fake player name by changing the title attribute. If
 * args[0] equals remove, the original name is reset. Can only be used to *chage
 * the name of the player running the script.
 *
 * @author timothyb89
 */
public class NameChange extends ScriptImpl {
	private static final String CONFIG_KEY = "stendhal.scripts.namechange.enabled";

	@Override
	public void execute(final Player admin, final List<String> args) {
		// check configuration
		try {
			if (!Configuration.getConfiguration().has(CONFIG_KEY)
					|| !Boolean.parseBoolean(Configuration.getConfiguration().get(
							CONFIG_KEY))) {
				admin.sendPrivateText("Ten skrypt musi być włączony w pliku konfiguracyjnym serwera (zwykle server.ini) z kluczem "
						+ CONFIG_KEY);
				return;
			}
		} catch (final Exception e) {
			admin.sendPrivateText(e.toString());
			return;
		}
		if (args.size() < 1) {
			admin.sendPrivateText("Użyj: /script NameChange.class {nowanazwa|remove}\nZmiania twoją nazwę na nową lub usuwa efekty zmiany nazwy.\nUwaga: Nie zalecane dla normalnych postaci. Używanie w przypadku przedmiotów lub małżonków może spowodować ich uszkodzenie lub inne nieprzewidziane efekty.");
		} else {
			// do title change
			if (args.get(0).equals("remove")) {
				admin.setTitle(null);
				admin.sendPrivateText("Twoje oryginalne imię zostało odzyskane. Zmień obszar, aby zadziałały zmiany.");
			} else {
				final String title = args.get(0);

				admin.setTitle(title);
				admin.sendPrivateText("Twoje imię zmieniło się na " + title
						+ ". Przechowywane nazwy nie zostały zmienione co może doprowadzić do niespodziewanych efektów.");
			}

			admin.notifyWorldAboutChanges();
		}
	}
}
