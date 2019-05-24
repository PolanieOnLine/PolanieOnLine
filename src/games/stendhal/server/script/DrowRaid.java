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
 * @author miguel
 *
 * Not safe for players below level 150
 */
public class DrowRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("elf ciemności", 20);
		attackArmy.put("elf ciemności łucznik", 5);
		attackArmy.put("elf ciemności łucznik elitarny", 5);
		attackArmy.put("elf ciemności kapitan", 7);
		attackArmy.put("elf ciemnosci rycerz", 3);
		attackArmy.put("elf ciemności generał", 1);
		attackArmy.put("elf ciemności czarownik", 2);
		attackArmy.put("elf ciemności królewicz", 1);
		attackArmy.put("elf ciemności czarnoksiężnik", 3);
		attackArmy.put("elf ciemności matrona", 1);
		attackArmy.put("elf ciemności mistrz", 1);
		attackArmy.put("elf ciemności komandos", 3);
		attackArmy.put("elf ciemności admirał", 3);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poniżej poziomu 150.";
	}
}
