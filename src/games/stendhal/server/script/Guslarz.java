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

import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.MeetGuslarz;

/**
 * Starts or stops Guslarz.
 *
 * @author edi18028
 */
public class Guslarz extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args.size() != 1) {
			admin.sendPrivateText("/script Guslarz.class {true|false}");
			return;
		}

		boolean enable = Boolean.parseBoolean(args.get(0));
		if (enable) {
			startGusla(admin);
		} else {
			stopGusla();
		}
	}

	/**
	 * Starts Gusla.
	 *
	 * @param admin adminstrator running the script
	 */
	private void startGusla(Player admin) {
		if (System.getProperty("stendhal.guslarz") != null) {
			admin.sendPrivateText("Guślarz jest aktywowany.");
			return;
		}
		System.setProperty("stendhal.guslarz", "true");
		StendhalQuestSystem.get().loadQuest(new MeetGuslarz());
	}

	/**
	 * Ends Gusla.
	 *
	 * @param admin adminstrator running the script
	 */
	private void stopGusla() {
		System.getProperties().remove("stendhal.guslarz");
		StendhalQuestSystem.get().unloadQuest(MeetGuslarz.QUEST_NAME);
	}

}
