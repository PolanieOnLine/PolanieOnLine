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

import java.util.HashMap;
import java.util.Map;

/**
 * @author kymara
 *
 * Atak hordy zbójeckiej
 */
public class ZbojnicyRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("zbójnik leśny oszust", 4);
		attackArmy.put("zbójnik leśny", 5);
		attackArmy.put("zbójnik leśny tchórzliwy", 4);
		attackArmy.put("zbójnik leśny starszy", 3);
		attackArmy.put("zbójnik leśny zwiadowca", 3);
		attackArmy.put("banitka", 2);
		attackArmy.put("banita", 3);
		attackArmy.put("banita gajowy", 2);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Atak hordy zbójeckiej.";
	}
}
