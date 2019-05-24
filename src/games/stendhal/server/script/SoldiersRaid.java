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
 * Atak zbrojnego rycertswa!
 */
public class SoldiersRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("strażnik bramy", 5);
		attackArmy.put("czarny rycerz", 2);
		attackArmy.put("rycerz szmaragdowy", 3);
		attackArmy.put("rycerz szafirowy", 3);
		attackArmy.put("rycerz karmazynowy", 3);
		attackArmy.put("rycerz w złotej zbroi", 2);
		attackArmy.put("rycerz śmierci", 4);
		attackArmy.put("rycerz mithrilbourgh", 1);
		attackArmy.put("rycerz imperium", 1);
		attackArmy.put("elf albinos rycerz", 1);
		attackArmy.put("elf ciemnosci rycerz", 1);
		attackArmy.put("rycerz madaram", 1);
		attackArmy.put("rycerz chaos", 1);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Atak zbrojnego rycertswa!";
	}
}
