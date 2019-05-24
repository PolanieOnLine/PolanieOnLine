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

import games.stendhal.common.NotificationType;
import games.stendhal.server.actions.admin.GhostModeAction;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * moves the view to the specified coordinates
 *
 * @author hendrik
 */
public class Observer extends ScriptImpl {

	@Override
	public void execute(Player admin, List<String> args) {
		if (!Boolean.parseBoolean(System.getProperty("stendhal.observer", "false"))) {
			admin.sendPrivateText(NotificationType.ERROR, "Skrypt nie jest dozwolony na tym serwerze");
			return;
		}

		if (admin.getAdminLevel() < 2000) {
			admin.sendPrivateText(NotificationType.ERROR, "wymagany jest 2000 poziom administratora,");
			return;
		}

		if (args.size() != 1 || (!args.get(0).equals("show") && !args.get(0).equals("hide"))) {
			admin.sendPrivateText("Użyj: /script Observer.class hide|show");
			return;
		}

		new GameEvent(admin.getName(), "observer", args.get(0)).raise();

		// teleport to usually empty zone to hide issues in marauroa when the object is removed
		admin.teleport(SingletonRepository.getRPWorld().getZone("int_admin_playground"), 3, 3, null, admin);
		if (args.get(0).equals("hide")) {
			GhostModeAction.activateGhostmode(admin);
			admin.hide();
		} else {
			admin.unhide();
		}
		admin.teleport(SingletonRepository.getRPWorld().getZone("int_semos_house"), 3, 3, null, admin);

	}

}
