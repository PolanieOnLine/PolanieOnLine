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
 * Not safe for players below level 80
 */
public class LichRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("szkielecik", 5);
		attackArmy.put("szkielet waleczny", 3);
		attackArmy.put("szkielet starszy", 4);
		attackArmy.put("demoniczny szkielet", 3);
		attackArmy.put("szkielet smoka", 3);
		attackArmy.put("upadły wojownik", 5);
		attackArmy.put("upadły kapłan", 3);
		attackArmy.put("upadły wysoki kapłan", 2);
		attackArmy.put("licho", 8);
		attackArmy.put("martwe licho", 3);
		attackArmy.put("wysokie licho", 3);

		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Niebezpieczny dla wojowników poniżej poziomu 80.";
	}
}
