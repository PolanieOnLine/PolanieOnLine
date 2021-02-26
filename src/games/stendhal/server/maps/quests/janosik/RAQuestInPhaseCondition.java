/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.janosik;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.Janosik;
import games.stendhal.server.maps.quests.janosik.IRAQuestConstants.RA_Phase;

public class RAQuestInPhaseCondition implements ChatCondition {

	private RA_Phase phase;

	public RAQuestInPhaseCondition(RA_Phase ph) {
		phase = ph;
	}

	@Override
	public boolean fire(Player player, Sentence sentence, Entity npc) {
		if(Janosik.getPhase().compareTo(phase)==0) {
			return true;
		}
		return false;
	}
}
