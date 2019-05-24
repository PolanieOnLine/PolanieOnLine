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
 * Not safe for players below level 150
 */
public class ImperialRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("obrońca imperium", 2);
		attackArmy.put("rycerz imperium", 2);
		attackArmy.put("generał imperium", 1);
		attackArmy.put("komandor imperium", 2);
		attackArmy.put("uczony imperium", 3);
		attackArmy.put("kapłan imperium", 1);
		attackArmy.put("obrońca imperium", 5);
		attackArmy.put("eksperyment imperium", 2);
		attackArmy.put("mutant imperium", 2);
		attackArmy.put("imperialny generał gigant", 2);
		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return "Nie jest bezpieczny dla wojowników poniżej 150 poziomu";
	}
}
