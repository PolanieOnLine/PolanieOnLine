/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
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
import games.stendhal.server.core.rule.damage.WeaponDamageRangeService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ItemTooltipService;
import games.stendhal.server.entity.item.StackableItem;

/**
 * Selects rarity and applies its concrete modifiers exactly once.
 */
public final class ItemRarityService {
	private static final String MISSILE_CLASS = "missile";
	private static final String RING_CLASS = "ring";

	private static final Set<String> INTEGRAL_STATS = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("atk", "ratk", "damage_min",
					"damage_max", "rate", "def",
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
	private final ItemAffixGenerator affixGenerator;
	private final Map<String, ItemRarityProfile> profiles =
			new ConcurrentHashMap<String, ItemRarityProfile>();

	public ItemRarityService(final Random random) {
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		this.random = random;
		this.affixGenerator = new ItemAffixGenerator(random);
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
		if (item == null || item instanceof StackableItem
				|| MISSILE_CLASS.equals(item.getItemClass())) {
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

		// The range belongs to the item instance and must exist before rarity
		// scales its endpoints. Restore contexts deliberately keep saved legacy
		// weapons without a range unchanged.
		WeaponDamageRangeService.initialize(item, context);

		if (context.isRestore() || item.has(Item.RARITY_ID) || !isEligible(item)) {
			ItemTooltipService.update(item);
			return;
		}

		final String profileId = selectProfile(item, context);
		final ItemRarityProfile profile = getProfile(profileId);
		final ItemRarity rarity;
		if (context.getSource() == ItemCreationContext.Source.QUEST
				&& RING_CLASS.equals(item.getItemClass())) {
			rarity = ItemRarity.COMMON;
		} else if (context.getRarity() != null) {
			rarity = context.getRarity();
		} else if (context.getSource() == ItemCreationContext.Source.QUEST) {
			rarity = ItemRarity.COMMON;
		} else {
			rarity = rollBestRarity(profile, context.getRarityRolls());
		}
		final ItemRarityProfile.Tier tier = profile.getTier(rarity);
		final ItemRarityModifiers fixed = context.getModifiers();
		Double sharedDamageMultiplier = null;

		// Positive lifesteal declared by the item definition is a guaranteed
		// regular affix, not a free base statistic. Common items have no regular
		// affix slots. Negative values are intentional weapon drawbacks and stay
		// independent of rarity.
		suppressPositiveIntrinsicLifestealForCommon(item, rarity);

		for (final String statistic : INTEGRAL_STATS) {
			if (item.has(statistic) && item.getInt(statistic) > 0) {
				final double multiplier;
				if (isBaseDamageStatistic(statistic)
						&& !hasExplicitFixedMultiplier(fixed, statistic)) {
					if (sharedDamageMultiplier == null) {
						sharedDamageMultiplier = Double.valueOf(chooseMultiplier(
								"atk", tier, fixed,
								context.isRandomizeModifiers()));
					}
					multiplier = sharedDamageMultiplier.doubleValue();
				} else {
					multiplier = chooseMultiplier(statistic, tier, fixed,
							context.isRandomizeModifiers());
				}
				applyIntegral(item, statistic, multiplier);
				item.setRarityModifier(statistic, multiplier);
			}
		}
		for (final String statistic : FLOAT_STATS) {
			if (item.has(statistic) && item.getDouble(statistic) > 0.0) {
				final double multiplier = chooseMultiplier(statistic, tier, fixed,
						context.isRandomizeModifiers());
				if (Double.compare(multiplier, 1.0) != 0) {
					final double scaled = Math.min((double) Float.MAX_VALUE,
							item.getDouble(statistic) * multiplier);
					item.put(statistic, ItemRollPrecision.round(scaled));
				}
				item.setRarityModifier(statistic, multiplier);
			}
		}

		final double valueMultiplier = chooseValueMultiplier(tier, fixed);
		item.setValue(roundToInt(item.getDefinitionValue() * valueMultiplier));
		item.setRarityModifier(ItemRarityModifiers.VALUE, valueMultiplier);
		item.setRarity(rarity);
		item.put(Item.RARITY_PROFILE, profile.getId());
		// Random affixes are a second persistent instance layer. Fresh contexts
		// generate according to their source/options; RESTORE never rerolls them.
		affixGenerator.generate(item, context);
		ItemTooltipService.update(item);
	}

	private void suppressPositiveIntrinsicLifestealForCommon(final Item item,
			final ItemRarity rarity) {
		if (rarity == ItemRarity.COMMON && WeaponAffixService.isWeapon(item)
				&& item.has(WeaponAffixService.LIFESTEAL_ATTRIBUTE)
				&& item.getDouble(WeaponAffixService.LIFESTEAL_ATTRIBUTE) > 0.0) {
			item.remove(WeaponAffixService.LIFESTEAL_ATTRIBUTE);
		}
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

	/**
	 * Replaces only the rarity-controlled layer with an exact deterministic Epic
	 * reward template. The existing object, binding, slots and quest provenance
	 * remain untouched.
	 */
	boolean promoteToQuestReward(final Item item, final Item epicTemplate) {
		if (item == null || epicTemplate == null || !isEligible(item)
				|| !item.getName().equals(epicTemplate.getName())
				|| epicTemplate.getRarityOrCommon() != ItemRarity.EPIC
				|| item.getRarityOrCommon().ordinal() >= ItemRarity.EPIC.ordinal()) {
			return false;
		}
		if (item.hasMap(Item.RARITY_MODIFIERS)) {
			item.removeMap(Item.RARITY_MODIFIERS);
		}
		for (final String statistic : INTEGRAL_STATS) {
			if (epicTemplate.has(statistic)) {
				item.put(statistic, epicTemplate.getInt(statistic));
			}
		}
		for (final String statistic : FLOAT_STATS) {
			if (epicTemplate.has(statistic)) {
				item.put(statistic, epicTemplate.getDouble(statistic));
			}
		}
		for (final Map.Entry<String, Double> modifier
				: epicTemplate.getRarityModifiers().entrySet()) {
			item.setRarityModifier(modifier.getKey(), modifier.getValue().doubleValue());
		}
		item.setValue(epicTemplate.getValue());
		item.setRarity(ItemRarity.EPIC);
		item.put(Item.RARITY_PROFILE, epicTemplate.get(Item.RARITY_PROFILE));
		ItemTooltipService.update(item);
		return true;
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

	private ItemRarity rollBestRarity(final ItemRarityProfile profile,
			final int rarityRolls) {
		ItemRarity best = ItemRarity.COMMON;
		for (int roll = 0; roll < rarityRolls; roll++) {
			final ItemRarity candidate = profile.roll(nextRandom());
			if (candidate.ordinal() > best.ordinal()) {
				best = candidate;
			}
		}
		return best;
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
		final double selected;
		if (fixed != null) {
			final Double supplied = findFixedMultiplier(statistic, fixed);
			selected = supplied == null ? 1.0 : supplied.doubleValue();
		} else if (!randomize) {
			selected = tier.midpointStatMultiplier();
		} else {
			selected = randomBetween(tier.getMinimumStatMultiplier(),
					tier.getMaximumStatMultiplier());
		}
		return ItemRollPrecision.roundPositive(selected);
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

	private boolean hasExplicitFixedMultiplier(final ItemRarityModifiers fixed,
			final String statistic) {
		return fixed != null && fixed.getMultiplier(statistic) != null;
	}

	private boolean isBaseDamageStatistic(final String statistic) {
		return "atk".equals(statistic) || "ratk".equals(statistic)
				|| "damage_min".equals(statistic)
				|| "damage_max".equals(statistic);
	}

	private boolean isAttack(final String statistic) {
		return isBaseDamageStatistic(statistic)
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
		final double selected = fixed != null && fixed.getValueMultiplier() != null
				? fixed.getValueMultiplier().doubleValue()
				: tier.getValueMultiplier();
		return ItemRollPrecision.roundPositive(selected);
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
