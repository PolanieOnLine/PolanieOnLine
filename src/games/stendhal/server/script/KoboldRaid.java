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
 * Less safe for players below level 10
 */
public class KoboldRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("kobold", 7);
		attackArmy.put("kobold łucznik", 3);
		attackArmy.put("kobold lider", 7);
		attackArmy.put("kobold żołnierz", 7);
		attackArmy.put("kobold olbrzymi", 2);
		attackArmy.put("kobold weteran", 7);
		return attackArmy;
	}
	@Override
	protected String getInfo() {
		return "Mniej bezpieczny dla wojowników poniżej poziomu 10.";
	}
}
