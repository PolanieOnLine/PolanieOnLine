/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

/**
 * Available modes for the main client window.
 */
public enum WindowMode {
	WINDOWED("windowed", "Okno"),
	BORDERLESS("borderless", "Pełny ekran w oknie"),
	FULLSCREEN("fullscreen", "Pełny ekran");

	private final String propertyValue;
	private final String label;

	WindowMode(String propertyValue, String label) {
		this.propertyValue = propertyValue;
		this.label = label;
	}

	/**
	 * Resolve a persisted value. Unknown values deliberately fall back to the
	 * traditional decorated window.
	 *
	 * @param value persisted value
	 * @return matching mode
	 */
	public static WindowMode fromProperty(String value) {
		for (WindowMode mode : values()) {
			if (mode.propertyValue.equals(value)) {
				return mode;
			}
		}
		return WINDOWED;
	}

	/**
	 * @return value stored in the client settings
	 */
	public String getPropertyValue() {
		return propertyValue;
	}

	/**
	 * @return whether normal window geometry should be restored and tracked
	 */
	public boolean isWindowed() {
		return this == WINDOWED;
	}

	@Override
	public String toString() {
		return label;
	}
}
