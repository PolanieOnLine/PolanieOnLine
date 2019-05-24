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
 * Less safe for players below level 50
 */
public class ElementalRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("żywioł ognia", 7);
		attackArmy.put("żywioł wody", 7);
		attackArmy.put("żywioł lodu", 7);
		attackArmy.put("żywioł ziemi", 7);
		attackArmy.put("dżin", 5);
		attackArmy.put("żywioł powietrza", 7);
		return attackArmy;
	}

	@Override
	protected String getInfo() {
		return "Mniej bezpieczny dla wojowników poniżej poziomu 50";
	}
}
