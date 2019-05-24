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
 * @author gummipferd
 *
 * Less safe for players below level 40
 */
public class OrcRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("ork wojownik", 7);
		attackArmy.put("ork łowca", 5);
		attackArmy.put("szef orków", 3);
		attackArmy.put("ork", 6);
		attackArmy.put("górski ork", 3);
		attackArmy.put("trol", 4);
		attackArmy.put("czerwony trol", 7);
		attackArmy.put("trol jaskiniowy", 2);
		attackArmy.put("zielony smok", 3);

		return attackArmy;
	}
}
