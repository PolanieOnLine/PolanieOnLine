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
package games.stendhal.server.script;

import java.util.HashMap;
import java.util.Map;

public class BarbarianRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("barbarzyńca", 30);
		attackArmy.put("barbarzyńca wilczur", 15);
		attackArmy.put("barbarzyńca elitarny", 12);
		attackArmy.put("barbarzyńca kapłan", 7);
		attackArmy.put("barbarzyńca szaman", 5);
		attackArmy.put("barbarzyńca lider", 3);
		attackArmy.put("król barbarzyńca", 1);

		return attackArmy;
	}

}
