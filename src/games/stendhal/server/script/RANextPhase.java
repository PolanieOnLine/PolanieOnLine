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
import games.stendhal.server.maps.quests.Janosik;

/**
 * Showing what is current ThePiedPiper quest state, and when it will switch to next.
 *
 * @author yoriy
 */
public class RANextPhase extends ScriptImpl {
	@Override
	public void execute(final Player admin, final List<String> args) {
		Janosik.switchToNextPhase();
		final String Phase = Janosik.getPhase().toString();
        admin.sendPrivateText("Zmeniono na "+Phase);
	}
}
