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
 * @author Tomko
 */
public class MagicyRaid extends CreateRaid {

	@Override
	protected Map<String, Integer> createArmy() {
		final Map<String, Integer> attackArmy = new HashMap<String, Integer>();
		attackArmy.put("pustelnik", 10);
		attackArmy.put("uczeń czarnoksiężnika", 10);
		attackArmy.put("czarnoksiężnik", 5);
		attackArmy.put("uczeń czarnoksiężnika mroku", 10);
		attackArmy.put("czarnoksiężnik mroku", 5);
		attackArmy.put("pokutnik z bagien", 5);
		attackArmy.put("pokutnik z wrzosowisk", 5);
		attackArmy.put("pokutnik nocny", 5);
		attackArmy.put("pokutnik wieczorny", 5);
		attackArmy.put("pokutnik z łąk", 5);
		attackArmy.put("czarownica z Aenye", 5);
		attackArmy.put("mega mag", 1);

		return attackArmy;
	}
}
