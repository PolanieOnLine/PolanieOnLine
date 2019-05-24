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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.Task;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * Logout all players in the game.
 *
 * @author edi18028
 */
public class LogoutAllPlayers extends ScriptImpl {
	@Override
	public void execute(final Player admin, final List<String> args) {
		super.execute(admin, args);

		SingletonRepository.getRuleProcessor().getOnlinePlayers().forAllPlayersExecute(
			new Task<Player>() {
				public void execute(final Player player) {
					if (!player.getName().equals(admin.getName()) || !player.getName().equals("postman")) {
						SingletonRepository.getRuleProcessor().getRPManager().disconnectPlayer(player);
					}
				}
		});
		admin.sendPrivateText("Wszyscy wojownicy zostali wylogowani");
	}
}
