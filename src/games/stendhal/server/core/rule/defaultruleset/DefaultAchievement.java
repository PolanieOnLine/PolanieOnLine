/***************************************************************************
 *                      (C) Copyright 2021 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rule.defaultruleset;

import java.util.Map;

import games.stendhal.server.core.rp.achievement.Category;

public class DefaultAchievement {
	private String clazz = "achievements";
	private String subclass;

	private String identifier;
	private String title;
	private String description;
	private boolean active;

	private Category category;

	private String classname;

	private String condition;

	private int baseScore;

	private Map<String, String> attributes = null;

	private Class< ? > implementation = null;

	/**
	 * create a new achievement
	 *
	 * @param identifier
	 * @param title
	 * @param category
	 * @param description
	 * @param baseScore
	 * @param active
	 * @param condition
	 */
	public DefaultAchievement(String identifier, String title, Category category, String description, int baseScore, boolean active, String condition) {
		this.identifier = identifier;
		this.title = title;
		this.category = category;
		this.condition = condition;
		this.description = description;
		this.baseScore = baseScore;
		this.active = active;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(final Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public String getAchievementClass() {
		return clazz;
	}

	public void setAchievementSubclass(final String subclass) {
		this.subclass = subclass;
	}

	public String getAchievementSubclass() {
		return subclass;
	}

	public String getId() {
		return identifier;
	}

	public boolean isActive() {
		return active;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setDescription(final String text) {
		this.description = text;
	}

	public String getDescription() {
		return description;
	}

	public void setClassName(final String classname) {
		this.classname = classname;
	}

	public String getClassName() {
		return classname;
	}

	public void setCondition(final String condition) {
		this.condition = condition;
	}

	public String getCondition() {
		return condition;
	}

	public void setScore(final int score) {
		this.baseScore = score;
	}

	public int getScore() {
		return baseScore;
	}

	public void setImplementation(final Class< ? > implementation) {
		this.implementation = implementation;
	}

	public Class< ? > getImplementation() {
		return implementation;
	}
}
