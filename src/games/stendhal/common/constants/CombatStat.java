/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common.constants;

import java.util.Locale;

/** Stable wire keys and presentation labels; numerical limits belong to combat services. */
public enum CombatStat {
	CRITICAL_CHANCE("Szansa na trafienie krytyczne"),
	CRITICAL_DAMAGE_BONUS("Dodatkowe obrażenia krytyczne"),
	PARRY_CHANCE("Szansa na parowanie"),
	RESIST_POISONED("Odporność na zatrucie"),
	RESIST_BLEEDING("Odporność na krwawienie"),
	RESIST_SHOCKED("Odporność na porażenie"),
	RESIST_CONFUSED("Odporność na dezorientację"),
	RESIST_HEAVY("Odporność na spowolnienie"),
	RESIST_STUNNED("Odporność na ogłuszenie");

	public static final String ATTRIBUTE = "combat_stats";
	private final String label;
	CombatStat(String label) { this.label = label; }
	public String getKey() { return name().toLowerCase(Locale.ROOT); }
	public String getLabel() { return label; }
	public String getLimitKey() { return getKey() + ".limit"; }
}
