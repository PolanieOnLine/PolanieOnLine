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
package games.stendhal.client.gui.buddies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractListModel;

import org.apache.log4j.Logger;

/**
 * A <code>ListModel</code> for buddies that keeps itself sorted first by online
 * status and secondarily by buddy name.
 */
class BuddyListModel extends AbstractListModel<Buddy> {
	/**
	 * serial version uid
	 */
	private static final long serialVersionUID = -2770930669293485239L;

	/*
	 * LinkedHashMap would nicely combine order and fast searches, but
	 * unfortunately it does not allow sorting after creation. (Likewise for
	 * TreeMap).
	 *
	 * The map is for a quick lookup, the list is needed for the ordering.
	 */
	private final List<Buddy> buddyList = new ArrayList<Buddy>();
	private final List<Buddy> visibleBuddies = new ArrayList<Buddy>();
	private final Map<String, Buddy> buddyMap = new HashMap<String, Buddy>();
	private String filterText = "";

	BuddyListModel() {
		rebuildVisibleList();
	}

	@Override
	public Buddy getElementAt(int index) {
		return visibleBuddies.get(index);
	}

	@Override
	public int getSize() {
		return visibleBuddies.size();
	}

	/**
	 * Update the active name filter for the visible buddy list.
	 *
	 * @param filter the new filter string, case insensitive; {@code null} clears the filter
	 */
	void setFilter(String filter) {
		String normalized = normalizeFilter(filter);
		if (!filterText.equals(normalized)) {
			filterText = normalized;
			rebuildVisibleList();
		}
	}

	/**
	 * Set the online status of a buddy. Add a new buddy if one by the wanted
	 * name does not already exist.
	 *
	 * @param name name of the buddy
	 * @param online <code>true</code> if the buddy is at the moment online,
	 * 	false otherwise
	 */
	void setOnline(String name, boolean online) {
		if (name == null) {
			Logger.getLogger(BuddyListModel.class).error("Buddy with no name set " + (online ? "online" : "offline"));
			return;
		}
		Buddy buddy = buddyMap.get(name);
		boolean requiresResort = false;
		if (buddy == null) {
			buddy = new Buddy(name);
			buddy.setOnline(online);
			buddyList.add(buddy);
			buddyMap.put(name, buddy);
			requiresResort = true;
		} else {
			requiresResort = buddy.setOnline(online);
		}
		if (requiresResort) {
			Collections.sort(buddyList);
			rebuildVisibleList();
		}
	}

	/**
	 * Remove a buddy from the list.
	 *
	 * @param name name of the removed player
	 */
	void removeBuddy(String name) {
		Buddy buddy = buddyMap.remove(name);
		if (buddy != null) {
			buddyList.remove(buddy);
			rebuildVisibleList();
		}
	}

	private void rebuildVisibleList() {
		int oldSize = visibleBuddies.size();
		visibleBuddies.clear();
		for (Buddy buddy : buddyList) {
			if (matchesFilter(buddy)) {
				visibleBuddies.add(buddy);
			}
		}
		int newSize = visibleBuddies.size();
		if (newSize > oldSize) {
			fireIntervalAdded(this, oldSize, newSize - 1);
		} else if (newSize < oldSize) {
			fireIntervalRemoved(this, newSize, oldSize - 1);
		}
		if (newSize > 0) {
			fireContentsChanged(this, 0, newSize - 1);
		} else if (oldSize > 0) {
			fireContentsChanged(this, 0, 0);
		}
	}

	private boolean matchesFilter(Buddy buddy) {
		if (filterText.isEmpty()) {
			return true;
		}
		return buddy.getName().toLowerCase(Locale.ROOT).contains(filterText);
	}

	private String normalizeFilter(String filter) {
		if (filter == null) {
			return "";
		}
		return filter.trim().toLowerCase(Locale.ROOT);
	}
}
