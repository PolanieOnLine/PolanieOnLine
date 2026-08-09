/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import games.stendhal.server.core.rule.damage.CriticalHitService;
import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;
import games.stendhal.server.entity.item.Item;

/** Registry of stable random affix definitions. */
public final class ItemAffixRegistry {
	private static final ItemAffixRegistry INSTANCE = new ItemAffixRegistry(
			Arrays.<ItemAffixDefinition>asList(
					new ParryAffixDefinition(),
					new LifestealAffixDefinition(),
					new AccuracyAffixDefinition(),
					new CriticalChanceAffixDefinition(),
					new CriticalDamageAffixDefinition(),
					new BleedOnHitAffixDefinition(),
					new ExecuteDamageAffixDefinition(),
					new PoisonOnHitAffixDefinition(),
					new DistanceDamageAffixDefinition(),
					new ArmorPenetrationAffixDefinition()));

	private final List<ItemAffixDefinition> definitions;
	private final Map<String, ItemAffixDefinition> byId;

	ItemAffixRegistry(final List<ItemAffixDefinition> definitions) {
		if (definitions == null) {
			throw new IllegalArgumentException("Affix definitions must not be null");
		}
		final List<ItemAffixDefinition> copy =
				new ArrayList<ItemAffixDefinition>();
		final Map<String, ItemAffixDefinition> ids =
				new LinkedHashMap<String, ItemAffixDefinition>();
		for (final ItemAffixDefinition definition : definitions) {
			if (definition == null || definition.getId() == null
					|| definition.getId().trim().length() == 0) {
				throw new IllegalArgumentException("Affix id must not be empty");
			}
			if (ids.containsKey(definition.getId())) {
				throw new IllegalArgumentException(
						"Duplicate affix id: " + definition.getId());
			}
			copy.add(definition);
			ids.put(definition.getId(), definition);
		}
		this.definitions = Collections.unmodifiableList(copy);
		this.byId = Collections.unmodifiableMap(ids);
	}

	public static ItemAffixRegistry getInstance() {
		return INSTANCE;
	}

	public ItemAffixDefinition get(final String id) {
		return byId.get(id);
	}

	public List<ItemAffixDefinition> getDefinitions() {
		return definitions;
	}

	public List<ItemAffixDefinition> getEligible(final Item item) {
		final List<ItemAffixDefinition> result =
				new ArrayList<ItemAffixDefinition>();
		for (final ItemAffixDefinition definition : definitions) {
			if (!ItemAffixState.has(item, definition.getId())
					&& definition.isEligible(item)) {
				result.add(definition);
			}
		}
		return result;
	}

	private static final class ParryAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return ParryService.PARRY_CHANCE_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return ParryService.PARRY_CHANCE_ATTRIBUTE;
		}

		@Override
		public boolean isEligible(final Item item) {
			return ParryAffixService.isEligible(item);
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			return ParryAffixService.applySelectedAffix(item, random) > 0;
		}
	}

	private static final class LifestealAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return WeaponAffixService.LIFESTEAL_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return WeaponAffixService.LIFESTEAL_ATTRIBUTE;
		}

		@Override
		public boolean isEligible(final Item item) {
			return WeaponAffixService.isEligible(item, getAttribute());
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			return WeaponAffixService.applyLifesteal(item, random);
		}
	}

	private static final class AccuracyAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return WeaponAffixService.ACCURACY_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return WeaponAffixService.ACCURACY_ATTRIBUTE;
		}

		@Override
		public boolean isEligible(final Item item) {
			return WeaponAffixService.isEligible(item, getAttribute());
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			return WeaponAffixService.applyAccuracy(item, random);
		}
	}

	private static final class CriticalChanceAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE;
		}

		@Override
		public boolean isEligible(final Item item) {
			return WeaponAffixService.isEligible(item, getAttribute());
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			return WeaponAffixService.applyCriticalChance(item, random);
		}
	}

	private static final class CriticalDamageAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE;
		}

		@Override
		public boolean isEligible(final Item item) {
			return WeaponAffixService.isCriticalDamageEligible(item);
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			return WeaponAffixService.applyCriticalDamage(item, random);
		}
	}

	private static final class BleedOnHitAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE;
		}

		@Override
		public boolean isEligible(final Item item) {
			return WeaponAffixService.isBleedOnHitEligible(item);
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			return WeaponAffixService.applyBleedOnHit(item, random);
		}
	}

	private static final class ExecuteDamageAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return WeaponAffixCombatService.EXECUTE_DAMAGE_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return WeaponAffixCombatService.EXECUTE_DAMAGE_ATTRIBUTE;
		}

		@Override
		public boolean isEligible(final Item item) {
			return WeaponAffixService.isExecuteDamageEligible(item);
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			return WeaponAffixService.applyExecuteDamage(item, random);
		}
	}

	private static final class PoisonOnHitAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return WeaponAffixCombatService.POISON_ON_HIT_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return WeaponAffixCombatService.POISON_ON_HIT_ATTRIBUTE;
		}

		@Override
		public boolean isEligible(final Item item) {
			return WeaponAffixService.isPoisonOnHitEligible(item);
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			return WeaponAffixService.applyPoisonOnHit(item, random);
		}
	}

	private static final class DistanceDamageAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return WeaponAffixCombatService.DISTANCE_DAMAGE_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return WeaponAffixCombatService.DISTANCE_DAMAGE_ATTRIBUTE;
		}

		@Override
		public boolean isEligible(final Item item) {
			return WeaponAffixService.isDistanceDamageEligible(item);
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			return WeaponAffixService.applyDistanceDamage(item, random);
		}
	}

	private static final class ArmorPenetrationAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE;
		}

		@Override
		public boolean isEligible(final Item item) {
			return WeaponAffixService.isArmorPenetrationEligible(item);
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			return WeaponAffixService.applyArmorPenetration(item, random);
		}
	}
}
