/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import java.util.List;
import java.util.Locale;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.status.StatusType;

/** Resolves persistent status-resistance affixes from worn equipment. */
public final class EquipmentStatusResistanceService {
	/** Random equipment cannot by itself make a player fully immune. */
	public static final double MAX_AFFIX_RESISTANCE = 0.60;

	private EquipmentStatusResistanceService() {
		// utility class
	}

	/** Returns the stable item/entity attribute used by one status resistance. */
	public static String getResistanceAttribute(final StatusType statusType) {
		if (statusType == null) {
			return null;
		}
		return "resist_" + statusType.toString().toLowerCase(Locale.ENGLISH);
	}

	/**
	 * Returns final resistance as a fraction. Existing entity resistance is kept
	 * authoritative and random item affixes are combined as independent sources.
	 * The random-equipment part is capped at 60%, while an authored intrinsic
	 * entity resistance may still reach full immunity.
	 */
	public static double getResistance(final RPEntity target,
			final StatusType statusType) {
		if (target == null || statusType == null) {
			return 0.0;
		}
		final String attribute = getResistanceAttribute(statusType);
		final double intrinsic = target.has(attribute)
				? clampFraction(target.getDouble(attribute)) : 0.0;
		final double equipment = Math.min(MAX_AFFIX_RESISTANCE,
				getEquipmentResistance(target.getDefenseItems(), attribute));
		return 1.0 - (1.0 - intrinsic) * (1.0 - equipment);
	}

	/** Combines item sources as independent probabilities. */
	public static double getEquipmentResistance(final List<Item> equipment,
			final String attribute) {
		if (equipment == null || equipment.isEmpty() || attribute == null) {
			return 0.0;
		}
		double failure = 1.0;
		for (final Item item : equipment) {
			if (item == null || !item.has(attribute)) {
				continue;
			}
			failure *= 1.0 - clampFraction(item.getDouble(attribute));
		}
		return 1.0 - failure;
	}

	private static double clampFraction(final double value) {
		if (Double.isNaN(value)) {
			return 0.0;
		}
		return Math.min(1.0, Math.max(0.0, value));
	}
}
