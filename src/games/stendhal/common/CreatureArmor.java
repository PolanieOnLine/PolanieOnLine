/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;

/** Player-facing names for the existing semantic creature armor types. */
public enum CreatureArmor {
	NONE("none", "Brak pancerza"),
	LIGHT("light", "Pancerz lekki"),
	MEDIUM("medium", "Pancerz średni"),
	HEAVY("heavy", "Pancerz ciężki");

	private final String id;
	private final String label;

	CreatureArmor(final String id, final String label) {
		this.id = id;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public String getSpritePath() {
		return this == NONE ? null : "data/sprites/status/armor/" + id + ".png";
	}

	/** Unknown values are not presented as a guessed armor type. */
	public static CreatureArmor fromId(final String id) {
		if (id == null) {
			return null;
		}
		switch (id) {
		case "none": return NONE;
		case "light": return LIGHT;
		case "medium": return MEDIUM;
		case "heavy": return HEAVY;
		default: return null;
		}
	}
}
