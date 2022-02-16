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
 
public class CheckShop extends ScriptImpl {
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
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Za mało argumentów");
			return;
		}

 		final String player_name = args.get(0);
		final Player target = SingletonRepository.getRuleProcessor().getPlayer(player_name);
		final String shop_type = args.get(1).toLowerCase();
		final int shop_count;
		// All remaining arguments are the enemy's name
		final String item = String.join(" ", args.subList(2, args.size()));

		// Player does not exist
		if (target == null) {
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Brak takiego gracza: " + player_name);
			return;
		}

 		if (shop_type.equals("bought")) {
 			shop_count = target.getQuantityOfBoughtItems(item);
		} else if (shop_type.equals("sold")) {
			shop_count = target.getQuantityOfSoldItems(item);
		} else {
			showUsage(admin);
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Argument 2 (typ) musi być jeden: \"bought\" lub \"sold\"");
			return;
		}

		admin.sendPrivateText(NotificationType.SUPPORT, "Gracz #'" + target.getName() + "' dla przedmiotu #'" + item + "' (typ: #'" + shop_type + "') "
				+ "dokonał transakcji w ilości sztuk: #" + shop_count + ".");
	}
 
 	/**
	 * Shows help text.
	 *
	 * @param admin Administrator invoking script
	 */
	private void showUsage(final Player admin) {
		List<String> usage = Arrays.asList(
				"\nUżycie:",
				"    /script CheckShop.class <gracz> <bought|sold> <przedmiot>",
				"    /script CheckShop.class !help",
				"Args:",
				"    gracz:\t Gracz do modyfikacji.",
				"    bought:\t Liczba kupionych przedmiotów.",
				"    sold:\t Liczba sprzedanych przedmiotów.",
				"    przedmiot:\t Nazwa przedmiotu.");
		admin.sendPrivateText(NotificationType.CLIENT, String.join("\n", usage));
	}
}
