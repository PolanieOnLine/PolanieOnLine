/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.events;

import static games.stendhal.common.constants.Events.DROPPEDLIST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
//import games.stendhal.server.entity.creature.Creature;
//import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.SyntaxException;

public class ItemLogEvent extends RPEvent {
	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(ItemLogEvent.class);

	private final List<DefaultItem> items;
	private final List<String> dropped;

	public static void generateRPClass() {
		try {
			final RPClass rpclass = new RPClass(DROPPEDLIST);
			rpclass.addAttribute("dropped_items", Type.VERY_LONG_STRING, Definition.PRIVATE);
		} catch (final SyntaxException e) {
			logger.error("cannot generateRPClass", e);
		}
	}

	public ItemLogEvent(final Player player) {
		super(DROPPEDLIST);

		items = new ArrayList<>();
		dropped = new ArrayList<>();

		final StringBuilder formatted = new StringBuilder();
		final EntityManager em = SingletonRepository.getEntityManager();

		int itemCount = 0;
		for (final DefaultItem i1 : em.getDefaultItems()) {
			/*for (final Creature c : em.getCreatures()) {
				List<Item> droppables = c.getDroppables();
				if (droppables.isEmpty()) {
					return;
				}

				for (Item i2 : droppables) {
					if (i1.getName() == i2.getName()) {
						items.add(i2);
					}
				}
			}*/
			items.add(i1);
			itemCount = player.getNumberOfLootsForItem(i1.getItemName());
			if (itemCount > 0) {
				dropped.add(i1.getItemName());
			}
		}

		// sort alphabetically
		final Comparator<DefaultItem> sorter = new Comparator<DefaultItem>() {
			@Override
			public int compare(final DefaultItem c1, final DefaultItem c2) {
				return (c1.getItemName().toLowerCase().compareTo(c2.getItemName().toLowerCase()));
			}
		};
		Collections.sort(items, sorter);

		formatted.append(getFormattedString(items));

		put("dropped_items", formatted.toString());
	}

	private String getFormattedString(final List<DefaultItem> items) {
		final StringBuilder sb = new StringBuilder();
		final int itemCount = items.size();
		int idx = 0;

		for (final DefaultItem item: items) {
			String name = item.getItemName();
			Boolean looted = false;

			if (dropped.contains(name)) {
				looted = true;
			}

			// hide the names of creatures not killed by player
			//if (!looted) {
			//	name = "???";
			//}

			sb.append(name + "," + looted.toString());
			if (idx != itemCount - 1) {
				sb.append(";");
			}

			idx++;
		}

		return sb.toString();
	}
}
