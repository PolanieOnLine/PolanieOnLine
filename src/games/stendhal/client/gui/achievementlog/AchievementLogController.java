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
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import games.stendhal.client.entity.User;
import games.stendhal.common.grammar.Grammar;

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

	private String[] alist;

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

	private String[] setList(String[] strings) {
		if (alist == null) {
			alist = strings;
		}
		return alist;
	}

	public List<String> getList() {
		return Arrays.asList(alist);
	}

	public void showAchievementList(final String[] strings) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setList(strings);
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
			achievementLog = new AchievementLog(Grammar.suffix_s(User.getCharacterName()) + " dziennik osiągnięć");
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