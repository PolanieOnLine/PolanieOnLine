package games.stendhal.client.gui.stats;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class MasteryI18n {
	private static final Map<String, String> EN = new HashMap<String, String>();
	private static final Map<String, String> PL = new HashMap<String, String>();
	static {
		EN.put("ui.mastery.locked.requirements", "Required: {0} resets + max lvl");
		EN.put("ui.mastery.locked.progress", "Progress: {0}/{1} resets, {2}/{3} lvl");
		EN.put("ui.mastery.level", "★ Mastery lvl: {0}");
		EN.put("ui.mastery.exp_to_next", "To next level: {0} EXP");
		EN.put("ui.mastery.max_reached", "MAX {0}");
		EN.put("ui.mastery.levelup", "Mastery level up! New level: {0}");

		PL.put("ui.mastery.locked.requirements", "Wymagane: {0} resetów + max lvl");
		PL.put("ui.mastery.locked.progress", "Postęp: {0}/{1} resetów, {2}/{3} lvl");
		PL.put("ui.mastery.level", "★ Poziom mastery: {0}");
		PL.put("ui.mastery.exp_to_next", "Do następnego poziomu: {0} EXP");
		PL.put("ui.mastery.max_reached", "MAX {0}");
		PL.put("ui.mastery.levelup", "Awans mastery! Nowy poziom: {0}");
	}

	private MasteryI18n() {}

	static String text(String key, Object... args) {
		Map<String, String> dict = Locale.getDefault().getLanguage().toLowerCase(Locale.ROOT).startsWith("pl") ? PL : EN;
		String pattern = dict.containsKey(key) ? dict.get(key) : EN.get(key);
		return MessageFormat.format(pattern, args);
	}
}
