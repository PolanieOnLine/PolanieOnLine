/* Copyright (C) 2003-2026 - Faiumoni e. V., PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.engine;

import games.stendhal.common.Translation;

/** Server context for the game's existing language dictionaries. */
public class Translate {
	private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

	public static void init() {
		Translation.getText(Translation.DEFAULT_LANGUAGE, "");
	}

	public static String getText(String text) {
		return Translation.getText(threadLocal.get(), text);
	}

	public static String getText(String language, String text) {
		return Translation.getText(language, text);
	}

	public static void setThreadLanguage(String language) {
		if (language == null) threadLocal.remove();
		else threadLocal.set(Translation.normalize(language));
	}
}
