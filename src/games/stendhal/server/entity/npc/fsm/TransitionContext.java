/***************************************************************************
 *                   (C) Copyright 2012 - Faiumoni e. V.                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.fsm;

import games.stendhal.server.maps.quests.IQuest;

/**
 * manages the current context for transitions
 *
 * @author hendrik
 */
public class TransitionContext {

	private static final ThreadLocal<String> currentLabel = new ThreadLocal<String>();
	private static final ThreadLocal<IQuest> markerQuest = new ThreadLocal<IQuest>();

	public static IQuest getMarkerQuest() {
		return markerQuest.get();
	}

	public static void setMarkerQuest(final IQuest quest) {
		if (quest == null) {
			markerQuest.remove();
		} else {
			markerQuest.set(quest);
		}
	}

	/**
	 * sets the label for the current context
	 *
	 * @param label label
	 */
	public static void set(String label) {
		currentLabel.set(label);
	}

	/**
	 * gets the current label
	 *
	 * @return current label
	 */
	public static String get() {
		return currentLabel.get();
	}

	/**
	 * gets the TransitionContext, prefering the specified one.
	 *
	 * @param label a label that will take precedence, if it is defined.
	 * @return label
	 */
	public static String getWithFallback(String label) {
		String res = get();
		if ((label != null) && (!label.equals(""))) {
			res = label;
		}
		if (res == null) {
			res = "";
		}
		return res;
	}
}
