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
 * Less safe for players below level 50
 */
public class ZombieRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("szczur zombie", 4);
		attackArmy.put("krwawy zombie", 5);
		attackArmy.put("zombie", 10);
		attackArmy.put("bezgłowy potwór", 5);
		attackArmy.put("cuchnący zombie", 5);
		attackArmy.put("wampirzyca", 5);
		attackArmy.put("wilkołak", 3);
		attackArmy.put("rycerz śmierci", 3);
		attackArmy.put("panna wampir", 1);
		attackArmy.put("lord wampir", 1);

		return attackArmy;
	}
}
