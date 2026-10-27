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
	RESIST_STUNNED("Odporność na ogłuszenie"),
	RESIST_LIGHT("Odporność na światło", Nature.LIGHT),
	RESIST_DARK("Odporność na mrok", Nature.DARK),
	RESIST_FIRE("Odporność na ogień", Nature.FIRE),
	RESIST_ICE("Odporność na lód", Nature.ICE),
	RESIST_EARTH("Odporność na naturę", Nature.EARTH),
	RESIST_WATER("Odporność na wodę", Nature.WATER),
	RESIST_CUT("Odporność na obrażenia fizyczne", Nature.CUT);

	public static final String ATTRIBUTE = "combat_stats";
	private final String label;
	private final Nature nature;
	CombatStat(String label) { this(label, null); }
	CombatStat(String label, Nature nature) { this.label = label; this.nature = nature; }
	public String getKey() { return name().toLowerCase(Locale.ROOT); }
	public String getLabel() { return label; }
	public Nature getNature() { return nature; }
	public String getLimitKey() { return getKey() + ".limit"; }
}
