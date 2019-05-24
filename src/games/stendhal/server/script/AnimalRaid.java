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

/**
 * @author kymara
 * 
 * Not safe for players below level 5
 */
public class AnimalRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("małpa", 2);
		attackArmy.put("żmija", 2);
		attackArmy.put("bóbr", 2);
		attackArmy.put("tygrys", 2);
		attackArmy.put("lew", 3);
		attackArmy.put("panda", 2);
		attackArmy.put("pingwin", 4);
		attackArmy.put("kaiman", 3);
		attackArmy.put("miś", 2);
		attackArmy.put("niedźwiedź grizli", 1);
		attackArmy.put("słoń", 3);
		attackArmy.put("krokodyl", 2);

		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return " * Niebezpieczny dla wojowników poniżej poziomu 5";
	}
}
