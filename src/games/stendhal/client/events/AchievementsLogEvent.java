/***************************************************************************
 *                      (C) Copyright 2020 - Stendhal                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.events;

import org.apache.log4j.Logger;

import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.achievementlog.AchievementLogController;

/**
 * @author KarajuSs
 */
public class AchievementsLogEvent extends Event<RPEntity> {
	private static final Logger logger = Logger.getLogger(AchievementsLogEvent.class);

	@Override
	public void execute() {
		try {
			AchievementLogController.get().showAchievementList(event.get("achievements").split(";"));
		} catch (RuntimeException e) {
			logger.error("Failed to process progress status. Event: " + event, e);
		}
	}
}
