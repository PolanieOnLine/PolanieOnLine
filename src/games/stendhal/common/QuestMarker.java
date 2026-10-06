/***************************************************************************
 *                   Copyright (C) 2026 - PolanieOnLine                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 ***************************************************************************/
package games.stendhal.common;

/** Compact, shared protocol for player-specific NPC quest indicators. */
public enum QuestMarker {
	NONE("", 0),
	IN_PROGRESS("progress", 1),
	REPEATABLE("repeatable", 2),
	AVAILABLE("available", 3),
	READY("ready", 4);

	public static final String ATTRIBUTE = "quest_markers";
	public static final String ZONE_KEY = "_zone";
	private final String code;
	private final int priority;

	QuestMarker(final String code, final int priority) {
		this.code = code;
		this.priority = priority;
	}

	public String getCode() {
		return code;
	}

	public QuestMarker prefer(final QuestMarker other) {
		return other != null && other.priority > priority ? other : this;
	}

	public static QuestMarker fromCode(final String code) {
		for (final QuestMarker marker : values()) {
			if (marker.code.equals(code)) {
				return marker;
			}
		}
		return NONE;
	}
}
