/***************************************************************************
*                      (C) Copyright 2024 - PolanieOnLine                 *
***************************************************************************/
/***************************************************************************
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 2 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
***************************************************************************/
package games.stendhal.client.gui.improvement;

import games.stendhal.client.sprite.Sprite;

/**
* Immutable container describing a single improvable item row.
*/
public class ItemImprovementEntry {
	private final int id;
	private final String name;
	private final String itemClass;
	private final String itemSubclass;
	private final int improve;
	private final int maxImprove;
	private final int cost;
	private final double chance;
	private final String requirements;
	private final Sprite icon;

	ItemImprovementEntry(final int id, final String name, final String itemClass, final String itemSubclass,
				final int improve, final int maxImprove, final int cost, final double chance, final String requirements,
				final Sprite icon) {
		this.id = id;
		this.name = name;
		this.itemClass = itemClass;
		this.itemSubclass = itemSubclass;
		this.improve = improve;
		this.maxImprove = maxImprove;
		this.cost = cost;
		this.chance = chance;
		this.requirements = requirements;
		this.icon = icon;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getItemClass() {
		return itemClass;
	}

	public String getItemSubclass() {
		return itemSubclass;
	}

	public int getImprove() {
		return improve;
	}

	public int getMaxImprove() {
		return maxImprove;
	}

	public int getCost() {
		return cost;
	}

	public double getChance() {
		return chance;
	}

	public String getRequirements() {
		return requirements;
	}

	public Sprite getIcon() {
		return icon;
	}
}
