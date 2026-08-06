/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import games.stendhal.common.Constants;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ItemTooltipService;
import games.stendhal.server.entity.item.StackableItem;

/**
 * Selects rarity and applies its concrete modifiers exactly once.
 */
public final class ItemRarityService {
	private static final Set<String> INTEGRAL_STATS = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("atk", "ratk", "rate", "def",
					"range", "skill_atk", "rate_increase", "health")));

	private static final Set<String> FLOAT_STATS = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("accuracy_bonus", "lifesteal",
					"atk_additional_bonus", "critical_additional_bonus",
					"def_additional_bonus", "critical_chance",
					"lifesteal_increase")));

	private static final Set<String> EXCLUDED_CLASSES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("food", "drink", "potion", "money",
					"tool", "key", "resource", "book", "document", "scroll")));

	private static final ItemRarityService INSTANCE =
			new ItemRarityService(new Random());

	private final Random random;
	private final Map<String, ItemRarityProfile> profiles =
			new ConcurrentHashMap<String, ItemRarityProfile>();

	public ItemRarityService(final Random random) {
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		this.random = random;
		registerProfile(ItemRarityProfile.defaultProfile());
	}

	public static ItemRarityService getInstance() {
		return INSTANCE;
	}

	static boolean isIntegralStatistic(final String attribute) {
		return INTEGRAL_STATS.contains(attribute);
	}

	static boolean isFloatingStatistic(final String attribute) {
		return FLOAT_STATS.contains(attribute);
	}

	static boolean isSupportedStatistic(final String attribute) {
		return isIntegralStatistic(attribute) || isFloatingStatistic(attribute);
	}

	/** Attribute names whose instance values may be changed by rarity. */
	public Set<String> getSupportedStatistics() {
		final Set<String> result = new HashSet<String>(INTEGRAL_STATS);
		result.addAll(FLOAT_STATS);
		return Collections.unmodifiableSet(result);
	}

	public void registerProfile(final ItemRarityProfile profile) {
		if (profile == null) {
			throw new IllegalArgumentException("Rarity profile must not be null");
		}
		profiles.put(profile.getId(), profile);
	}

	public ItemRarityProfile getProfile(final String id) {
		final ItemRarityProfile profile = profiles.get(id);
		return profile != null ? profile : profiles.get(ItemRarityProfile.DEFAULT_ID);
	}

	public boolean isEligible(final Item item) {
		if (item == null || item instanceof StackableItem) {
			return false;
		}
		final Boolean override = item.getRarityEnabledOverride();
		if (Boolean.FALSE.equals(override)) {
			return false;
		}
		if (Boolean.TRUE.equals(override)) {
			return true;
		}
		if (!hasSupportedStat(item) || EXCLUDED_CLASSES.contains(item.getItemClass())) {
			return false;
		}
		for (final String slot : item.getPossibleSlots()) {
			for (final String equipmentSlot : Constants.EQUIPMENT_SLOTS) {
				if (equipmentSlot.equals(slot)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Initializes a newly-created instance and always refreshes its wire tooltip.
	 */
	public void initialize(final Item item, final ItemCreationContext creationContext) {
		if (item == null) {
			return;
		}
		final ItemCreationContext context = creationContext == null
				? ItemCreationContext.defaultCreation() : creationContext;
		if (context.isRestore() || item.has(Item.RARITY_ID) || !isEligible(item)) {
			ItemTooltipService.update(item);
			return;
		}

		final String profileId = selectProfile(item, context);
		final ItemRarityProfile profile = getProfile(profileId);
		final ItemRarity rarity;
		if (context.getRarity() != null) {
			rarity = context.getRarity();
		} else if (context.getSource() == ItemCreationContext.Source.QUEST) {
			rarity = ItemRarity.COMMON;
		} else {
			rarity = profile.roll(nextRandom());
		}
		final ItemRarityProfile.Tier tier = profile.getTier(rarity);
		final ItemRarityModifiers fixed = context.getModifiers();

		for (final String statistic : INTEGRAL_STATS) {
			if (item.has(statistic) && item.getInt(statistic) > 0) {
				final double multiplier = chooseMultiplier(statistic, tier, fixed,
						context.isRandomizeModifiers());
				applyIntegral(item, statistic, multiplier);
				item.setRarityModifier(statistic, multiplier);
			}
		}
		for (final String statistic : FLOAT_STATS) {
			if (item.has(statistic) && item.getDouble(statistic) > 0.0) {
				final double multiplier = chooseMultiplier(statistic, tier, fixed,
						context.isRandomizeModifiers());
				item.put(statistic, Math.min((double) Float.MAX_VALUE,
						item.getDouble(statistic) * multiplier));
				item.setRarityModifier(statistic, multiplier);
			}
		}

		final double valueMultiplier = chooseValueMultiplier(tier, fixed);
		item.setValue(roundToInt(item.getDefinitionValue() * valueMultiplier));
		item.setRarityModifier(ItemRarityModifiers.VALUE, valueMultiplier);
		item.setRarity(rarity);
		item.put(Item.RARITY_PROFILE, profile.getId());
		ItemTooltipService.update(item);
	}

	public void markLegacyCommon(final Item item) {
		if (item == null) {
			return;
		}
		if (item.has(Item.RARITY_ID) || !isEligible(item)) {
			ItemTooltipService.update(item);
			return;
		}
		for (final String statistic : INTEGRAL_STATS) {
			if (item.has(statistic)) {
				item.setRarityModifier(statistic, 1.0);
			}
		}
		for (final String statistic : FLOAT_STATS) {
			if (item.has(statistic)) {
				item.setRarityModifier(statistic, 1.0);
			}
		}
		if (!item.has(Item.VALUE)) {
			item.setValue(item.getDefinitionValue());
		}
		item.setRarityModifier(ItemRarityModifiers.VALUE, 1.0);
		item.setRarity(ItemRarity.COMMON);
		item.put(Item.RARITY_PROFILE, selectDefinitionProfile(item));
		ItemTooltipService.update(item);
	}

	private String selectProfile(final Item item, final ItemCreationContext context) {
		if (!ItemRarityProfile.DEFAULT_ID.equals(context.getProfile())) {
			return context.getProfile();
		}
		return selectDefinitionProfile(item);
	}

	private String selectDefinitionProfile(final Item item) {
		final String profile = item.getRarityProfile();
		return profile == null ? ItemRarityProfile.DEFAULT_ID : profile;
	}

	private boolean hasSupportedStat(final Item item) {
		for (final String statistic : INTEGRAL_STATS) {
			if (item.has(statistic)) {
				return true;
			}
		}
		for (final String statistic : FLOAT_STATS) {
			if (item.has(statistic)) {
				return true;
			}
		}
		return false;
	}

	private double chooseMultiplier(final String statistic,
			final ItemRarityProfile.Tier tier,
			final ItemRarityModifiers fixed, final boolean randomize) {
		if (fixed != null) {
			final Double supplied = findFixedMultiplier(statistic, fixed);
			return supplied == null ? 1.0 : supplied.doubleValue();
		}
		if (!randomize) {
			return tier.midpointStatMultiplier();
		}
		return randomBetween(tier.getMinimumStatMultiplier(),
				tier.getMaximumStatMultiplier());
	}

	private Double findFixedMultiplier(final String statistic,
			final ItemRarityModifiers fixed) {
		Double result = fixed.getMultiplier(statistic);
		if (result != null) {
			return result;
		}
		if (isAttack(statistic)) {
			result = fixed.getAttackMultiplier();
		} else if (isDefense(statistic)) {
			result = fixed.getDefenseMultiplier();
		} else if (isSpeed(statistic)) {
			result = fixed.getSpeedMultiplier();
		} else if ("range".equals(statistic)) {
			result = fixed.getRangeMultiplier();
		}
		return result == null ? fixed.getStatMultiplier() : result;
	}

	private boolean isAttack(final String statistic) {
		return "atk".equals(statistic) || "ratk".equals(statistic)
				|| "skill_atk".equals(statistic)
				|| "atk_additional_bonus".equals(statistic)
				|| "critical_additional_bonus".equals(statistic)
				|| "critical_chance".equals(statistic)
				|| "lifesteal".equals(statistic)
				|| "lifesteal_increase".equals(statistic)
				|| "accuracy_bonus".equals(statistic);
	}

	private boolean isDefense(final String statistic) {
		return "def".equals(statistic) || "def_additional_bonus".equals(statistic)
				|| "health".equals(statistic);
	}

	private boolean isSpeed(final String statistic) {
		return "rate".equals(statistic) || "rate_increase".equals(statistic);
	}

	private double chooseValueMultiplier(final ItemRarityProfile.Tier tier,
			final ItemRarityModifiers fixed) {
		if (fixed != null && fixed.getValueMultiplier() != null) {
			return fixed.getValueMultiplier().doubleValue();
		}
		return tier.getValueMultiplier();
	}

	private void applyIntegral(final Item item, final String statistic,
			final double multiplier) {
		final double value = "rate".equals(statistic)
				? item.getInt(statistic) / multiplier
				: item.getInt(statistic) * multiplier;
		item.put(statistic, roundToShortStatistic(value));
	}

	private int roundToShortStatistic(final double value) {
		if (value >= Short.MAX_VALUE) {
			return Short.MAX_VALUE;
		}
		return Math.max(1, (int) Math.round(value + 0.000000001));
	}

	private int roundToInt(final double value) {
		if (value >= Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) Math.round(value + 0.000000001);
	}

	private double randomBetween(final double minimum, final double maximum) {
		if (minimum == maximum) {
			return minimum;
		}
		return minimum + nextRandom() * (maximum - minimum);
	}

	private double nextRandom() {
		synchronized (random) {
			return random.nextDouble();
		}
	}
}
