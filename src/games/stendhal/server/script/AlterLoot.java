/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
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

import java.util.Arrays;
import java.util.List;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * Alters number of loots a player has for an item.
 */
public class AlterLoot extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		super.execute(admin, args);

		final int argc = args.size();

		// Show help
		if (argc < 1 || args.get(0).equals("!help")) {
			showUsage(admin);
			return;
		}

		// Admin did not input correct number of arguments
		if (argc < 3) {
			showUsage(admin);
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Zbyt mało argumentów");
			return;
		}

		final String player_name = args.get(0);
		final Player target = SingletonRepository.getRuleProcessor().getPlayer(player_name);
		final int new_count;
		// All remaining arguments are the item's name
		final String item = String.join(" ", args.subList(2, args.size()));

		// Player does not exist
		if (target == null) {
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Nie ma takiego gracza: " + player_name);
			return;
		}

		// Admin tries to change kill count to string value
		try {
			new_count = Integer.parseInt(args.get(1));
		} catch (NumberFormatException e) {
			showUsage(admin);
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Argument <ilość> musi być wartością liczbową");
			return;
		}

		final int diff = new_count - target.getNumberOfLootsForItem(item);
		target.incLootForItem(item, diff);
		final int actual_count = target.getNumberOfLootsForItem(item);

		if (actual_count != new_count) {
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Próbowano ustawić liczbę łupów dla " + item + " do " + Integer.toString(new_count) + "."
					+ " Aktualna wartość: " + Integer.toString(actual_count));
			return;
		}

		// Notify player of changes
		target.sendPrivateText(NotificationType.SUPPORT, "Ilość zdobytych przedmiotów dla #'" + item + "' została zmieniono do "
				+ Integer.toString(actual_count) + " przez " + admin.getTitle());
	}

	/**
	 * Shows help text.
	 *
	 * @param admin Administrator invoking script
	 */
	private void showUsage(final Player admin) {
		List<String> usage = Arrays.asList(
				"\nUżyj:",
				"    /script AlterLoot.class <wojownik> <ilość> <przedmiot>",
				"    /script AlterLoot.class !help",
				"Argumenty:",
				"    wojownik:\tGracz do modyfikacji.",
				"    ilość:\tWartość do zmiany ile się zdobyło łupu.",
				"    przedmiot:\tNazwa przedmiotu.");
		admin.sendPrivateText(NotificationType.CLIENT, String.join("\n", usage));
	}
}