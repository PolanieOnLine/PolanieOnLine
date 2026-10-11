/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.creature;

import java.awt.Shape;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Corpse;

/**
 * An creature that will only target enemies which are within a specified area.
 *
 * @author hendrik
 */
public class ArenaCreature extends Creature {

	private final Shape arena;
	private boolean guaranteedRareDrops;
	private Corpse lootCorpse;

	/**
	 * ArenaCreature.
	 *
	 * @param copy
	 *            creature to wrap
	 * @param arena
	 *            arena
	 */
	public ArenaCreature(final Creature copy, final Shape arena) {
		super(copy);
		this.arena = arena;
		this.guaranteedRareDrops = copy instanceof ArenaCreature
				&& ((ArenaCreature) copy).guaranteedRareDrops;
	}

	public void setGuaranteedRareDrops(final boolean guaranteed) {
		this.guaranteedRareDrops = guaranteed;
	}

	@Override
	protected void dropItemsOn(final Corpse corpse) {
		lootCorpse = corpse;
		super.dropItemsOn(corpse);
	}

	public Corpse getLootCorpse() {
		return lootCorpse;
	}

	@Override
	protected Item createDroppedItem(final EntityManager manager, final String name) {
		return guaranteedRareDrops ? manager.getItem(name, ItemCreationContext.bossDrop())
				: super.createDroppedItem(manager, name);
	}

	@Override
	public List<RPEntity> getEnemyList() {
		// only return those enemies which are in the arena
		final List<RPEntity> standardEnemyList = super.getEnemyList();
		final List<RPEntity> resultList = new LinkedList<RPEntity>();

		for (final RPEntity enemy : standardEnemyList) {
			if (arena.contains(enemy.getX(), enemy.getY())) {
				resultList.add(enemy);
			}
		}
		return resultList;
	}

	@Override
	public Creature getNewInstance() {
		return new ArenaCreature(this, arena);
	}

}
