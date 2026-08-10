/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.entity;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Tracks the short-lived visual marker for a successfully parried attack.
 * The regular combat resolution remains BLOCKED internally; this marker lets
 * player views replace the generic block icon with the dedicated parry icon.
 */
public final class ParryIndicator {
	private static final long DISPLAY_TIME_MILLIS = 1200L;
	private static final Map<RPEntity, Long> PARRIES =
			new WeakHashMap<RPEntity, Long>();

	private ParryIndicator() {
		// utility class
	}

	public static synchronized void mark(final RPEntity entity) {
		if (entity != null) {
			PARRIES.put(entity, Long.valueOf(System.currentTimeMillis()));
		}
	}

	public static synchronized void clear(final RPEntity entity) {
		if (entity != null) {
			PARRIES.remove(entity);
		}
	}

	public static synchronized boolean isActive(final RPEntity entity) {
		final Long markedAt = PARRIES.get(entity);
		if (markedAt == null) {
			return false;
		}
		if (System.currentTimeMillis() - markedAt.longValue() >= DISPLAY_TIME_MILLIS) {
			PARRIES.remove(entity);
			return false;
		}
		return true;
	}
}
