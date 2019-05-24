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
 * Not safe for players below level 150
 */
public class ChaosRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("wojownik chaosu", 4);
		attackArmy.put("żołnierz chaosu", 3);
		attackArmy.put("komandor chaosu", 4);
		attackArmy.put("lord chaosu", 3);
		attackArmy.put("czarnoksiężnik chaosu", 3);
		attackArmy.put("chaosu lord wywyższony", 3);
		attackArmy.put("jeździec smoków chaosu", 3);
		attackArmy.put("jeździec chaosu na czerwonym smoku", 2);
		attackArmy.put("jeździec chaosu na zielonym smoku", 2);
		attackArmy.put("czarny olbrzym", 1);
		attackArmy.put("czarny smok", 1);
		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poni|ej poziomu 150";
	}
}
