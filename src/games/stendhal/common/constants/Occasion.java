/***************************************************************************
 *                    (C) Copyright 2018-2026 - Arianne                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.common.constants;

/**
 * Runtime flags for occasions such as Christmas & Mine Town Weeks.
 *
 * Historically these values were {@code static final} snapshots of system
 * properties. That made changing an occasion at runtime ineffective for code
 * using this class. The fields remain source-compatible, but can now be
 * refreshed after a controlled runtime property change.
 */
public final class Occasion {
	private Occasion() {
		// utility class
	}

	/** Stendhal Christmas event. */
	public static volatile Boolean CHRISTMAS;
	/** Stendhal Easter event. */
	public static volatile Boolean EASTER;
	/** Halloween/Mine Town Weeks. */
	public static volatile Boolean MINETOWN;
	/** Mine Town construction event. */
	public static volatile Boolean MINETOWN_CONSTRUCTION;

	/** PolanieOnLine 50% more XP event. */
	public static volatile Boolean MOREXP;
	/** PolanieOnLine birthday event. */
	public static volatile Boolean BIRTHDAY;
	/** Second-world mode. */
	public static volatile Boolean SECOND_WORLD;

	static {
		refresh();
	}

	/**
	 * Refreshes all occasion flags from their canonical system properties.
	 *
	 * Runtime event controllers must call this after changing one of those
	 * properties. Using volatile references makes the refreshed values visible
	 * to other threads without requiring callers to synchronize on this class.
	 */
	public static synchronized void refresh() {
		CHRISTMAS = enabled("stendhal.christmas");
		EASTER = enabled("stendhal.easter");
		MINETOWN = enabled("stendhal.minetown");
		MINETOWN_CONSTRUCTION = enabled("stendhal.minetownconstruction");
		MOREXP = enabled("pol.morexp");
		BIRTHDAY = enabled("pol.birthday");
		SECOND_WORLD = enabled("server.secondworld");
	}

	private static Boolean enabled(final String property) {
		return Boolean.valueOf(System.getProperty(property) != null);
	}
}
