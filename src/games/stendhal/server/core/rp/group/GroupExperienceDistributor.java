/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp.group;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.server.entity.player.Player;

/** Divides a fixed experience reward without creating or losing XP. */
public final class GroupExperienceDistributor {
	private GroupExperienceDistributor() {
		// utility class
	}

	/**
	 * Splits experience equally. Any indivisible remainder goes to the first
	 * members in the supplied stable ordering.
	 *
	 * @param members eligible group members
	 * @param experience total experience to divide
	 * @return experience assigned to each member; zero shares are omitted
	 */
	public static Map<Player, Integer> splitEqually(final List<Player> members,
			final int experience) {
		final Map<Player, Integer> shares = new LinkedHashMap<Player, Integer>();
		if ((experience <= 0) || members.isEmpty()) {
			return shares;
		}

		final int baseShare = experience / members.size();
		final int remainder = experience % members.size();
		for (int i = 0; i < members.size(); i++) {
			final int share = baseShare + (i < remainder ? 1 : 0);
			if (share > 0) {
				shares.put(members.get(i), Integer.valueOf(share));
			}
		}
		return shares;
	}
}
