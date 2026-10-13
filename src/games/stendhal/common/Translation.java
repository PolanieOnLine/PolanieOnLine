/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

/** Shared reader for the game's existing UTF-8 language dictionaries. */
public final class Translation {
	public static final String DEFAULT_LANGUAGE = "pl";
	private static final Logger logger = Logger.getLogger(Translation.class);
	private static final Map<String, Map<String, String>> dictionaries = loadDictionaries();

	private Translation() { }

	public static boolean isSupported(String language) {
		return language != null && ("pl".equals(language.trim().toLowerCase(Locale.ROOT))
				|| "en".equals(language.trim().toLowerCase(Locale.ROOT)));
	}

	public static String normalize(String language) {
		return isSupported(language) ? language.trim().toLowerCase(Locale.ROOT) : DEFAULT_LANGUAGE;
	}

	public static String getText(String language, String text) {
		if (text == null) return null;
		String translated = dictionaries.get(normalize(language)).get(text);
		return translated == null || translated.trim().isEmpty() ? text : translated;
	}

	private static Map<String, Map<String, String>> loadDictionaries() {
		Map<String, Map<String, String>> result = new HashMap<>();
		for (String language : new String[] {"pl", "en"}) {
			String path = "data/languages/" + language + ".txt";
			InputStream stream = Translation.class.getClassLoader().getResourceAsStream(path);
			Map<String, String> entries = new HashMap<>();
			if (stream == null) {
				logger.warn("Missing language dictionary: " + path);
			} else {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.startsWith("\uFEFF")) line = line.substring(1);
						if (line.trim().isEmpty() || line.trim().startsWith("#")) continue;
						int separator = line.indexOf('=');
						if (separator < 1) {
							logger.warn("Malformed translation in " + path + ": " + line);
							continue;
						}
						String value = line.substring(separator + 1);
						if (!value.trim().isEmpty()) entries.put(line.substring(0, separator), value);
					}
				} catch (IOException error) { logger.error("Cannot read " + path, error); }
			}
			result.put(language, Collections.unmodifiableMap(entries));
		}
		return Collections.unmodifiableMap(result);
	}
}
