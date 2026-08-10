/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import games.stendhal.server.core.rule.rarity.LegendaryEquipmentAffixService;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
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
	 *
	 * Defensive equipment and jewellery are intentionally collected separately:
	 * RPEntity.getDefenseItems() excludes rings and necklaces because they do not
	 * necessarily contribute DEF, while status-resistance affixes on jewellery
	 * still need to participate in status checks.
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
				getEquipmentResistance(getResistanceEquipment(target), attribute));
		return 1.0 - (1.0 - intrinsic) * (1.0 - equipment);
	}

	/**
	 * Combines item sources as independent probabilities. Legendary Iron Will and
	 * Guardian Seal each contribute an additional 20 percentage points against
	 * the supported negative status families, including stun.
	 */
	public static double getEquipmentResistance(final List<Item> equipment,
			final String attribute) {
		if (equipment == null || equipment.isEmpty() || attribute == null) {
			return 0.0;
		}
		double failure = 1.0;
		for (final Item item : equipment) {
			if (item == null) {
				continue;
			}
			if (item.has(attribute)) {
				failure *= 1.0 - clampFraction(item.getDouble(attribute));
			}
			if (isUniversalLegendaryResistanceAttribute(attribute)
					&& (item.has(LegendaryEquipmentAffixService.IRON_WILL_ATTRIBUTE)
							|| item.has(LegendaryEquipmentAffixService.GUARDIAN_SEAL_ATTRIBUTE))) {
				failure *= 1.0
						- LegendaryEquipmentAffixService.UNIVERSAL_STATUS_RESISTANCE;
			}
		}
		return 1.0 - failure;
	}

	private static List<Item> getResistanceEquipment(final RPEntity target) {
		final List<Item> result = new ArrayList<Item>();
		final List<Item> defenseItems = target.getDefenseItems();
		if (defenseItems != null) {
			result.addAll(defenseItems);
		}
		if (target instanceof Player) {
			final Player player = (Player) target;
			addIdentityUnique(result, player.getRing());
			addIdentityUnique(result, player.getRingB());
			addIdentityUnique(result, player.getNecklace());
		}
		return result;
	}

	private static void addIdentityUnique(final List<Item> items,
			final Item candidate) {
		if (candidate == null) {
			return;
		}
		for (final Item item : items) {
			if (item == candidate) {
				return;
			}
		}
		items.add(candidate);
	}

	private static boolean isUniversalLegendaryResistanceAttribute(
			final String attribute) {
		return getResistanceAttribute(StatusType.POISONED).equals(attribute)
				|| getResistanceAttribute(StatusType.BLEEDING).equals(attribute)
				|| getResistanceAttribute(StatusType.SHOCKED).equals(attribute)
				|| getResistanceAttribute(StatusType.CONFUSED).equals(attribute)
				|| getResistanceAttribute(StatusType.HEAVY).equals(attribute)
				|| getResistanceAttribute(StatusType.STUNNED).equals(attribute);
	}

	private static double clampFraction(final double value) {
		if (Double.isNaN(value)) {
			return 0.0;
		}
		return Math.min(1.0, Math.max(0.0, value));
	}
}
