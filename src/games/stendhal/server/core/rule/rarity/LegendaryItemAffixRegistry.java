/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;
import games.stendhal.server.entity.item.Item;

/** Registry of signature affixes reserved for legendary item drops. */
public final class LegendaryItemAffixRegistry {
	private static final Set<String> DEEP_WOUNDS_WEAPON_CLASSES =
			classes("sword", "dagger", "axe", "whip");
	private static final Set<String> ARMOR_BREAKER_WEAPON_CLASSES =
			classes("club", "sword", "dagger", "axe");
	private static final Set<String> LONGSHOT_WEAPON_CLASSES =
			classes("ranged", "wand");
	private static final Set<String> EXECUTIONER_WEAPON_CLASSES =
			classes("dagger", "axe");
	private static final Set<String> SWORD_CLASSES = classes("sword");
	private static final Set<String> CLUB_CLASSES = classes("club");
	private static final Set<String> WHIP_CLASSES = classes("whip");
	private static final Set<String> RANGED_CLASSES = classes("ranged");
	private static final Set<String> WAND_CLASSES = classes("wand");

	private static final LegendaryItemAffixRegistry INSTANCE =
			new LegendaryItemAffixRegistry(Arrays.<ItemAffixDefinition>asList(
					new MarkerAffixDefinition(
							WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE,
							DEEP_WOUNDS_WEAPON_CLASSES),
					new MarkerAffixDefinition(
							WeaponArmorInteractionService.LEGENDARY_ARMOR_BREAKER_ATTRIBUTE,
							ARMOR_BREAKER_WEAPON_CLASSES),
					new MarkerAffixDefinition(
							WeaponAffixCombatService.LEGENDARY_LONGSHOT_ATTRIBUTE,
							LONGSHOT_WEAPON_CLASSES),
					new MarkerAffixDefinition(
							WeaponAffixCombatService.LEGENDARY_EXECUTIONER_ATTRIBUTE,
							EXECUTIONER_WEAPON_CLASSES),
					new MarkerAffixDefinition(ParryService.LEGENDARY_DUEL_MASTER_ATTRIBUTE,
							SWORD_CLASSES),
					new MarkerAffixDefinition(
							WeaponAffixCombatService.LEGENDARY_CRUSHING_BLOW_ATTRIBUTE,
							CLUB_CLASSES),
					new MarkerAffixDefinition(
							WeaponAffixCombatService.LEGENDARY_STUNNING_FORCE_ATTRIBUTE,
							CLUB_CLASSES),
					new MarkerAffixDefinition(
							WeaponAffixCombatService.LEGENDARY_BINDING_STRIKE_ATTRIBUTE,
							WHIP_CLASSES),
					new MercilessReachAffixDefinition(),
					new MarkerAffixDefinition(
							WeaponAffixCombatService.LEGENDARY_FALCON_EYE_ATTRIBUTE,
							RANGED_CLASSES),
					new MarkerAffixDefinition(
							WeaponAffixCombatService.LEGENDARY_FIRST_SALVO_ATTRIBUTE,
							RANGED_CLASSES),
					new MarkerAffixDefinition(
							WeaponAffixCombatService.LEGENDARY_POWER_OVERLOAD_ATTRIBUTE,
							WAND_CLASSES),
					new MarkerAffixDefinition(
							WeaponAffixCombatService.LEGENDARY_ARCANE_FOCUS_ATTRIBUTE,
							WAND_CLASSES),
					new EquipmentAffixDefinition(
							LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE),
					new EquipmentAffixDefinition(
							LegendaryEquipmentAffixService.IRON_WILL_ATTRIBUTE),
					new EquipmentAffixDefinition(
							LegendaryEquipmentAffixService.UNYIELDING_PROTECTION_ATTRIBUTE),
					new EquipmentAffixDefinition(
							LegendaryEquipmentAffixService.RELIC_POWER_ATTRIBUTE),
					new EquipmentAffixDefinition(
							LegendaryEquipmentAffixService.HERO_EYE_ATTRIBUTE),
					new EquipmentAffixDefinition(
							LegendaryEquipmentAffixService.GUARDIAN_SEAL_ATTRIBUTE)));

	private final List<ItemAffixDefinition> definitions;
	private final Map<String, ItemAffixDefinition> byId;

	LegendaryItemAffixRegistry(final List<ItemAffixDefinition> definitions) {
		if (definitions == null) {
			throw new IllegalArgumentException(
					"Legendary affix definitions must not be null");
		}
		final List<ItemAffixDefinition> copy =
				new ArrayList<ItemAffixDefinition>();
		final Map<String, ItemAffixDefinition> ids =
				new LinkedHashMap<String, ItemAffixDefinition>();
		for (final ItemAffixDefinition definition : definitions) {
			if (definition == null || definition.getId() == null
					|| definition.getId().trim().length() == 0) {
				throw new IllegalArgumentException(
						"Legendary affix id must not be empty");
			}
			if (ids.containsKey(definition.getId())) {
				throw new IllegalArgumentException(
						"Duplicate legendary affix id: " + definition.getId());
			}
			copy.add(definition);
			ids.put(definition.getId(), definition);
		}
		this.definitions = Collections.unmodifiableList(copy);
		this.byId = Collections.unmodifiableMap(ids);
	}

	public static LegendaryItemAffixRegistry getInstance() {
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
		return result;
	}

	private static final class MarkerAffixDefinition
			implements ItemAffixDefinition {
		private final String id;
		private final Set<String> eligibleClasses;

		MarkerAffixDefinition(final String id, final Set<String> eligibleClasses) {
			this.id = id;
			this.eligibleClasses = eligibleClasses;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getAttribute() {
			return id;
		}

		@Override
		public boolean isEligible(final Item item) {
			return markerEligible(item, id, eligibleClasses);
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			if (random == null) {
				throw new IllegalArgumentException("Random source must not be null");
			}
			return applyMarker(item, id, eligibleClasses);
		}
	}

	private static final class MercilessReachAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return WeaponAffixCombatService.LEGENDARY_MERCILESS_REACH_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return getId();
		}

		@Override
		public boolean isEligible(final Item item) {
			return LegendaryWeaponAffixService.isMercilessReachEligible(item);
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			return LegendaryWeaponAffixService.applyMercilessReach(item, random);
		}
	}

	private static final class EquipmentAffixDefinition
			implements ItemAffixDefinition {
		private final String id;

		EquipmentAffixDefinition(final String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getAttribute() {
			return id;
		}

		@Override
		public boolean isEligible(final Item item) {
			if (LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE.equals(id)) {
				// Keep Bastion in the fresh legendary pool until the approved
				// Wał grodu mechanics are defined. This avoids weakening the
				// signature-pool regression just to retire raw DEF prematurely.
				return LegendaryEquipmentAffixService.isBastionEligible(item);
			}
			if (LegendaryEquipmentAffixService.IRON_WILL_ATTRIBUTE.equals(id)) {
				return LegendaryEquipmentAffixService.isIronWillEligible(item);
			}
			if (LegendaryEquipmentAffixService.UNYIELDING_PROTECTION_ATTRIBUTE.equals(id)) {
				return LegendaryEquipmentAffixService.isUnyieldingProtectionEligible(item);
			}
			if (LegendaryEquipmentAffixService.RELIC_POWER_ATTRIBUTE.equals(id)) {
				return LegendaryEquipmentAffixService.isRelicPowerEligible(item);
			}
			if (LegendaryEquipmentAffixService.HERO_EYE_ATTRIBUTE.equals(id)) {
				return LegendaryEquipmentAffixService.isHeroEyeEligible(item);
			}
			if (LegendaryEquipmentAffixService.GUARDIAN_SEAL_ATTRIBUTE.equals(id)) {
				return LegendaryEquipmentAffixService.isGuardianSealEligible(item);
			}
			return false;
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			if (LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE.equals(id)) {
				return LegendaryEquipmentAffixService.applyBastion(item, random);
			}
			if (LegendaryEquipmentAffixService.IRON_WILL_ATTRIBUTE.equals(id)) {
				return LegendaryEquipmentAffixService.applyIronWill(item, random);
			}
			if (LegendaryEquipmentAffixService.UNYIELDING_PROTECTION_ATTRIBUTE.equals(id)) {
				return LegendaryEquipmentAffixService.applyUnyieldingProtection(item, random);
			}
			if (LegendaryEquipmentAffixService.RELIC_POWER_ATTRIBUTE.equals(id)) {
				return LegendaryEquipmentAffixService.applyRelicPower(item, random);
			}
			if (LegendaryEquipmentAffixService.HERO_EYE_ATTRIBUTE.equals(id)) {
				return LegendaryEquipmentAffixService.applyHeroEye(item, random);
			}
			if (LegendaryEquipmentAffixService.GUARDIAN_SEAL_ATTRIBUTE.equals(id)) {
				return LegendaryEquipmentAffixService.applyGuardianSeal(item, random);
			}
			return false;
		}
	}

	private static boolean markerEligible(final Item item, final String attribute,
			final Set<String> classes) {
		return item != null && classes.contains(item.getItemClass())
				&& !item.has(attribute);
	}

	private static boolean applyMarker(final Item item, final String attribute,
			final Set<String> classes) {
		if (!markerEligible(item, attribute, classes)) {
			return false;
		}
		item.put(attribute, 1.0);
		return true;
	}

	private static Set<String> classes(final String... values) {
		return Collections.unmodifiableSet(
				new HashSet<String>(Arrays.asList(values)));
	}
}
