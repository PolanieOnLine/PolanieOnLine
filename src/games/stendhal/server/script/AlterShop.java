/***************************************************************************
 *                   (C) Copyright 2022 - Stendhal                         *
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
 
public class AlterShop extends ScriptImpl {
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
		if (argc < 4) {
			showUsage(admin);
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Za mało argumentów");
			return;
		}

 		final String player_name = args.get(0);
		final Player target = SingletonRepository.getRuleProcessor().getPlayer(player_name);
		final String shop_type = args.get(1).toLowerCase();
		final int shop_count;
		// All remaining arguments are the enemy's name
		final String item = String.join(" ", args.subList(3, args.size()));

		// Player does not exist
		if (target == null) {
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Brak takiego gracza: " + player_name);
			return;
		}

		// Admin tries to change kill count to string value
		try {
			shop_count = Integer.parseInt(args.get(2));
		} catch (NumberFormatException e) {
			showUsage(admin);
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Argument 3 (liczba) musi być wartością całkowitą");
			return;
		}

 		if (shop_type.equals("bought")) {
 			target.incBoughtForItem(item, shop_count);
		} else if (shop_type.equals("sold")) {
			target.incSoldForItem(item, shop_count);
		} else {
			showUsage(admin);
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Argument 2 (typ) musi być jeden: \"bought\" lub \"sold\"");
			return;
		}
 
 		// Notify player of changes
		target.sendPrivateText(NotificationType.SUPPORT, "Twoja ilość przedmiotów o nazwie #'" + item + "' (typ: #'" + shop_type + "') została zmieniona do wartości "
				+ Integer.toString(shop_count) + " przez " + admin.getTitle());

		admin.sendPrivateText(NotificationType.SUPPORT, "Zmieniłeś wartość u gracza #'" + target.getName() + "' dla przedmiotu #'" + item + "' (typ: #'" + shop_type + "') do "
				+ "liczby " + Integer.toString(shop_count) + ".");
	}
 
 	/**
	 * Shows help text.
	 *
	 * @param admin Administrator invoking script
	 */
	private void showUsage(final Player admin) {
		List<String> usage = Arrays.asList(
				"\nUżycie:",
				"    /script AlterShop.class <gracz> <bought|sold> <liczba> <przedmiot>",
				"    /script AlterShop.class !help",
				"Args:",
				"    gracz:\t Gracz do modyfikacji.",
				"    bought:\t Zmiana ilości kupionych przedmiotów.",
				"    sold:\t Zmiana ilości sprzedanych przedmiotów.",
				"    liczba:\t Wartość liczbowa do zmiany.",
				"    przedmiot:\t Nazwa przedmiotu.");
		admin.sendPrivateText(NotificationType.CLIENT, String.join("\n", usage));
	}
}
