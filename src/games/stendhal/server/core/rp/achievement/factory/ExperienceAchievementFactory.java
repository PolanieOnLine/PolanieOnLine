/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2022 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
/**
 * Factory for experience achievements
 *
 * @author madmetzger
 */
public class ExperienceAchievementFactory extends AbstractAchievementFactory {

	public static final String ID_PAROBEK = "xp.level.010";
	public static final String ID_CHLOP = "xp.level.050";
	public static final String ID_KMIEC = "xp.level.100";
	public static final String ID_MIESZCZANIN = "xp.level.150";
	public static final String ID_SZLACHCIC = "xp.level.200";
	public static final String ID_RYCERZ = "xp.level.250";
	public static final String ID_BARONET = "xp.level.300";
	public static final String ID_BARON = "xp.level.350";
	public static final String ID_WICEHRABIA = "xp.level.400";
	public static final String ID_HRABIA = "xp.level.450";
	public static final String ID_MAGNAT = "xp.level.500";
	public static final String ID_KSIAZE = "xp.level.550";
	public static final String ID_KROL = "xp.level.597";

	@Override
	protected Category getCategory() {
		return Category.EXPERIENCE;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		xpAchievements.add(createAchievement(
				ID_PAROBEK, "Parobek", "Osiągnął poziom 10",
				Achievement.EASY_BASE_SCORE, true,
				new LevelGreaterThanCondition(9)));

		xpAchievements.add(createAchievement(
				ID_CHLOP, "Chłop", "Osiągnął poziom 50",
				Achievement.EASY_BASE_SCORE, true,
				new LevelGreaterThanCondition(49)));

		xpAchievements.add(createAchievement(
				ID_KMIEC, "Kmieć", "Osiągnął poziom 100",
				Achievement.EASY_BASE_SCORE, true,
				new LevelGreaterThanCondition(99)));

		xpAchievements.add(createAchievement(
				ID_MIESZCZANIN, "Mieszczanin", "Osiągnął poziom 150",
				Achievement.EASY_BASE_SCORE, true,
				new LevelGreaterThanCondition(149)));

		xpAchievements.add(createAchievement(
				ID_SZLACHCIC, "Szlachcic", "Osiągnął poziom 200",
				Achievement.MEDIUM_BASE_SCORE, true,
				new LevelGreaterThanCondition(199)));

		xpAchievements.add(createAchievement(
				ID_RYCERZ, "Rycerz", "Osiągnął poziom 250",
				Achievement.MEDIUM_BASE_SCORE, true,
				new LevelGreaterThanCondition(249)));

		xpAchievements.add(createAchievement(
				ID_BARONET, "Baronet", "Osiągnął poziom 300",
				Achievement.MEDIUM_BASE_SCORE, true,
				new LevelGreaterThanCondition(299)));

		xpAchievements.add(createAchievement(
				ID_BARON, "Baron", "Osiągnął poziom 350",
				Achievement.MEDIUM_BASE_SCORE, true,
				new LevelGreaterThanCondition(349)));

		xpAchievements.add(createAchievement(
				ID_WICEHRABIA, "Wicehrabia", "Osiągnął poziom 400",
				Achievement.HARD_BASE_SCORE, true,
				new LevelGreaterThanCondition(399)));

		xpAchievements.add(createAchievement(
				ID_HRABIA, "Hrabia", "Osiągnął poziom 450",
				Achievement.HARD_BASE_SCORE, true,
				new LevelGreaterThanCondition(449)));

		xpAchievements.add(createAchievement(
				ID_MAGNAT, "Magnat", "Osiągnął poziom 500",
				Achievement.HARD_BASE_SCORE, true,
				new LevelGreaterThanCondition(499)));

		xpAchievements.add(createAchievement(
				ID_KSIAZE, "Książe", "Osiągnął poziom 550",
				Achievement.HARD_BASE_SCORE, true,
				new LevelGreaterThanCondition(549)));

		xpAchievements.add(createAchievement(
				ID_KROL, "Król", "Osiągnął poziom 597",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new LevelGreaterThanCondition(596)));

		return xpAchievements;
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

	}

}
