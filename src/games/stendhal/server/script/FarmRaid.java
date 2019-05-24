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
 * A raid safe for lowest level players
 */
public class FarmRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("prosiak", 4);
		attackArmy.put("krowa", 3);
		attackArmy.put("kokoszka", 4);
		attackArmy.put("koza", 3);
		attackArmy.put("koń", 2);
		attackArmy.put("pisklak", 5);
		attackArmy.put("byk", 2);
		attackArmy.put("baran", 5);
		attackArmy.put("mysz domowa", 5);
		attackArmy.put("biały koń", 2);
		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return "Rajd bezpieczny dla wojowników z niskim poziomem.";
	}
}
