/***************************************************************************
 *                   (C) Copyright 2022 - Faiumoni e.V.                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.quest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * defines general information about this quest
 *
 * @author hendrik
 */
public class QuestInfoBuilder {
	private String name = "<unnamed>";
	private String description = "";
	private String internalName = null;
	private int repeatableAfterMinutes = -1;
	private int minLevel = 0;
	private String region = "somewhere";
	private String questGiverNpc = null;
	private final List<String> rewards = new ArrayList<String>();
	private final List<String> requirements = new ArrayList<String>();
	private final List<String> costs = new ArrayList<String>();

	// hide constructor
	QuestInfoBuilder() {
		super();
	}

	public QuestInfoBuilder name(String name) {
		this.name = name;
		return this;
	}

	public QuestInfoBuilder description(String description) {
		this.description = description;
		return this;
	}

	public QuestInfoBuilder internalName(String internalName) {
		this.internalName = internalName;
		return this;
	}

	public QuestInfoBuilder repeatableAfterMinutes(int repeatableAfterMinutes) {
		this.repeatableAfterMinutes = repeatableAfterMinutes;
		return this;
	}

	public QuestInfoBuilder notRepeatable() {
		this.repeatableAfterMinutes = -1;
		return this;
	}

	public QuestInfoBuilder minLevel(int minLevel) {
		this.minLevel = minLevel;
		return this;
	}

	public QuestInfoBuilder region(String region) {
		this.region = region;
		return this;
	}

	public QuestInfoBuilder questGiverNpc(String questGiverNpc) {
		this.questGiverNpc = questGiverNpc;
		return this;
	}

	public QuestInfoBuilder rewards(String... rewards) {
		return rewards(Arrays.asList(rewards));
	}

	public QuestInfoBuilder rewards(List<String> rewards) {
		this.rewards.clear();
		if (rewards != null) {
			this.rewards.addAll(rewards);
		}
		return this;
	}

	public QuestInfoBuilder requirements(String... requirements) {
		return requirements(Arrays.asList(requirements));
	}

	public QuestInfoBuilder requirements(List<String> requirements) {
		this.requirements.clear();
		if (requirements != null) {
			this.requirements.addAll(requirements);
		}
		return this;
	}

	public QuestInfoBuilder costs(String... costs) {
		return costs(Arrays.asList(costs));
	}

	public QuestInfoBuilder costs(List<String> costs) {
		this.costs.clear();
		if (costs != null) {
			this.costs.addAll(costs);
		}
		return this;
	}

	String getQuestGiverNpc() {
		return questGiverNpc;
	}

	void simulate(QuestSimulator simulator) {
		simulator.info("Quest: " + name + " (internal: " + internalName + ")");
		simulator.info(description);
		simulator.info("");

		// TODO
	}

	String getName() {
		return name;
	}

	String getDescription() {
		return description;
	}

	String getInternalName() {
		return internalName;
	}

	int getRepeatableAfterMinutes() {
		return repeatableAfterMinutes;
	}

	int getMinLevel() {
		return minLevel;
	}

	String getRegion() {
		return region;
	}

	List<String> getRewards() {
		return rewards;
	}

	List<String> getRequirements() {
		return requirements;
	}

	List<String> getCosts() {
		return costs;
	}
}
