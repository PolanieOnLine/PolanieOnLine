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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Get a list of unique achievement categories.
     *
     * @return List of unique categories
     */
    public List<String> getCategories() {
        Set<String> categories = new HashSet<>();
        for (String achievement : getList()) {
            String category = achievement.split(":")[0];
            categories.add(category);
        }
        return new ArrayList<>(categories);
    }

    /**
     * Get a list of achievements for a specific category.
     *
     * @param category the category to filter achievements by
     * @return List of achievements belonging to the given category
     */
    public List<String> getAchievementsByCategory(String category) {
        List<String> achievementsByCategory = new ArrayList<>();
        for (String achievement : getList()) {
            if (achievement.startsWith(category + ":")) {
                achievementsByCategory.add(achievement);
            }
        }
        return achievementsByCategory;
    }
}
