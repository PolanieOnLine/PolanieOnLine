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
package games.stendhal.client.gui.achievementlog;

import java.awt.Window;
import java.util.List;

import javax.swing.SwingUtilities;

import games.stendhal.client.entity.User;

/**
 * @author KarajuSs
 */
public class AchievementLogController {
	/** Controller instance */
	private static AchievementLogController instance;

	/**
	 * Progress window. This should be accessed only through getProgressLog(),
	 * which ensures the window has been created.
	 */
	private AchievementLog achievementLog;

	private List<String> aList;

	/**
	 * Get the book controller instance.
	 *
	 * @return controller instance
	 */
	public static synchronized AchievementLogController get() {
		if (instance == null) {
			instance = new AchievementLogController();
		}
		return instance;
	}

	/**
	 * Create a new ProgressLogController.
	 */
	private AchievementLogController() {
	}

	private List<String> setList(List<String> list) {
		if (aList == null) {
			aList = list;
		}
		return aList;
	}

	public List<String> getList() {
		return aList;
	}

	public void showAchievementList(final List<String> list) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setList(list);
				showWindow();
			}
		});
	}

	/**
	 * Get the log window, and create it if it has not been created before.
	 * This method must be called in the event dispatch thread.
	 *
	 * @return log window
	 */
	private AchievementLog getAchievementLog() {
		if (achievementLog == null) {
			achievementLog = new AchievementLog(User.getCharacterName() + " dziennik osiągnięć");
		}
		return achievementLog;
	}

	/**
	 * Show the window, if it's not already visible. This must not be called
	 * outside the event dispatch thread.
	 */
	private void showWindow() {
		Window window = getAchievementLog().getWindow();
		window.setVisible(true);
	}
}