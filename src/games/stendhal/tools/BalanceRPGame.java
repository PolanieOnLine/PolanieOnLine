/* $Id$ */
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
package games.stendhal.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.config.CreatureGroupsXMLLoader;
import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.transformer.PlayerTransformer;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.damage.CriticalHitService;
import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

/**
 * Offline creature balance helper.
 *
 * The original tool represented every player with one synthetic, fully equipped
 * build whose item statistics were overwritten according to level. This version
 * keeps the optimizer but reports three deterministic player profiles built from
 * real item definitions: WEAK, EXPECTED and STRONG.
 *
 * Only item definitions with an explicit min_level are considered by the
 * automatic equipment selector. Some quest/event rewards intentionally rely on
 * quest conditions rather than item min_level; treating those as level-zero
 * equipment made the balancer equip late-game rewards on new characters. The
 * real starter items are handled explicitly.
 *
 * Profiles use the six core combat slots of the old balancer. New characters
 * start with weapon and armour; head/legs/feet enter the model from level 5 and
 * a shield from level 10. Jewellery, glyphs and consumables are deliberately not
 * assumed in the EXPECTED baseline.
 *
 * Useful properties:
 * -Dbalance.profile=weak|expected|strong
 * -Dbalance.rounds=100
 * -Dbalance.maxLevel=30
 * -Dbalance.reportOnly=true
 */
public class BalanceRPGame {

	private static final int DEFAULT_ROUNDS = 100;
	private static final int HIGHEST_LEVEL = 500;
	private static final int MAX_COMBAT_TURNS = 5000;
	private static final double DEFAULT_DURATION_THRESHOLD = 0.2;
	private static final int DEFAULT_ITEM_ATTACK_RATE = 5;

	private static final String SLOT_WEAPON = "rhand";
	private static final String SLOT_SHIELD = "lhand";
	private static final String SLOT_ARMOR = "armor";
	private static final String SLOT_HEAD = "head";
	private static final String SLOT_LEGS = "legs";
	private static final String SLOT_FEET = "feet";

	private static final String STARTER_WEAPON = "maczuga";
	private static final String STARTER_AXE = "ciupaga startowa";
	private static final String STARTER_ARMOR = "skórzana zbroja";

	private static final String[] BALANCE_SLOTS = {
		SLOT_WEAPON, SLOT_SHIELD, SLOT_ARMOR, SLOT_HEAD, SLOT_LEGS, SLOT_FEET
	};

	private static final Set<String> MELEE_WEAPON_CLASSES =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
					"club", "sword", "dagger", "axe", "whip")));

	private static final List<String> suggestions = new LinkedList<String>();
	private static double durationThreshold;
	private static Player player;

	private enum PlayerProfile {
		WEAK(0.15, 5, 0.75),
		EXPECTED(0.35, 0, 1.00),
		STRONG(0.60, 0, 1.25);

		private final double equipmentQuantile;
		private final int gearLevelLag;
		private final double skillLevelFactor;

		PlayerProfile(final double equipmentQuantile, final int gearLevelLag,
				final double skillLevelFactor) {
			this.equipmentQuantile = equipmentQuantile;
			this.gearLevelLag = gearLevelLag;
			this.skillLevelFactor = skillLevelFactor;
		}

		double getEquipmentQuantile() {
			return equipmentQuantile;
		}

		int getGearLevelCap(final int playerLevel) {
			return Math.max(0, playerLevel - gearLevelLag);
		}

		int getCombatSkill(final int playerLevel) {
			return 10 + Math.max(0,
					(int) Math.round(playerLevel * skillLevelFactor));
		}

		ItemRarity getRarity(final String slot, final int level) {
			if (level < 10 || this == WEAK) {
				return ItemRarity.COMMON;
			}

			if (this == EXPECTED) {
				if (level >= 25
						&& (SLOT_WEAPON.equals(slot) || SLOT_ARMOR.equals(slot))) {
					return ItemRarity.RARE;
				}
				if (level >= 15 && SLOT_WEAPON.equals(slot)) {
					return ItemRarity.RARE;
				}
				return ItemRarity.COMMON;
			}

			if (level >= 30
					&& (SLOT_WEAPON.equals(slot) || SLOT_ARMOR.equals(slot))) {
				return ItemRarity.EPIC;
			}
			if (level >= 20 || SLOT_WEAPON.equals(slot)
					|| SLOT_ARMOR.equals(slot)) {
				return ItemRarity.RARE;
			}
			return ItemRarity.COMMON;
		}

		static PlayerProfile fromProperty() {
			final String value = System.getProperty(
					"balance.profile", "expected").trim()
							.toUpperCase(Locale.ENGLISH);
			try {
				return valueOf(value);
			} catch (final IllegalArgumentException e) {
				throw new IllegalArgumentException(
						"Unknown balance.profile '" + value
								+ "'. Expected weak, expected or strong.", e);
			}
		}
	}

	private static final class PlayerBuild {
		private final Player player;
		private final String equipment;

		PlayerBuild(final Player player, final String equipment) {
			this.player = player;
			this.equipment = equipment;
		}
	}

	private static final class CombatResult {
		private final int turns;
		private final int playerHp;
		private final boolean playerWon;

		CombatResult(final int turns, final int playerHp,
				final boolean playerWon) {
			this.turns = turns;
			this.playerHp = playerHp;
			this.playerWon = playerWon;
		}
	}

	private static final class CombatSummary {
		private final int meanTurns;
		private final int meanPlayerHp;
		private final int wins;
		private final int rounds;

		CombatSummary(final int meanTurns, final int meanPlayerHp,
				final int wins, final int rounds) {
			this.meanTurns = meanTurns;
			this.meanPlayerHp = meanPlayerHp;
			this.wins = wins;
			this.rounds = rounds;
		}

		double getWinRate() {
			return rounds == 0 ? 0.0 : wins * 100.0 / rounds;
		}

		boolean isUsefulForXp() {
			return wins > 0 && meanTurns < MAX_COMBAT_TURNS;
		}
	}

	/** Simple optimizer retained from the original tool. */
	private static class Optimizer {
		private final Creature creature;

		Optimizer(final Creature creature) {
			this.creature = creature;
		}

		void step(final int leftHP, final int rounds) {
			float stepSize = leftHP / (float) player.getBaseHP();
			stepSize = Math.signum(stepSize)
					* Math.min(Math.abs(stepSize), 0.5f);

			final int oldAtk = creature.getAtk();
			int newAtk = Math.max(1, Math.round(creature.getAtk()
					+ stepSize * creature.getAtk()));
			if ((leftHP < 0) && (newAtk == oldAtk)) {
				newAtk--;
			}

			final int level = creature.getLevel();
			final int oldDef = creature.getDef();
			int newDef = oldDef;
			final double preferred = preferredDuration(level);
			if (!isWithinDurationRange(preferred, rounds)) {
				if ((leftHP > 0) || (preferred < rounds)) {
					newDef = Math.max(1, (int) (creature.getDef()
							+ preferred - rounds + 0.5));
				}
			} else {
				newDef = Math.max(1, (int) (creature.getDef()
						+ 5 * stepSize * creature.getDef() + 0.5f));
			}

			if (newDef > 1.1 * oldDef) {
				newDef = Math.max((int) (1.1 * oldDef), oldDef + 1);
			} else if (newDef < 0.9 * oldDef) {
				newDef = Math.max(1,
						Math.min((int) (0.9 * oldDef), oldDef - 1));
			}

			creature.setAtk(newAtk);
			creature.setDef(newDef);
		}
	}

	public static void main(final String[] args) throws Exception {
		new RPClassGenerator().createRPClasses();

		final CreatureGroupsXMLLoader loader =
				new CreatureGroupsXMLLoader("/data/conf/creatures.xml");
		final List<DefaultCreature> creatures = loader.load();
		Collections.sort(creatures, new Comparator<DefaultCreature>() {
			@Override
			public int compare(final DefaultCreature first,
					final DefaultCreature second) {
				return first.getLevel() - second.getLevel();
			}
		});

		final EntityManager em = SingletonRepository.getEntityManager();
		final PlayerProfile referenceProfile = PlayerProfile.fromProperty();
		final int rounds = intProperty("balance.rounds", DEFAULT_ROUNDS, 1,
				10000);
		final int maxLevel = intProperty("balance.maxLevel", HIGHEST_LEVEL, 0,
				HIGHEST_LEVEL);
		final boolean reportOnly = Boolean.parseBoolean(
				System.getProperty("balance.reportOnly", "false"));

		System.out.println("BalanceRPGame: reference profile="
				+ referenceProfile + ", rounds=" + rounds + ", maxLevel="
				+ maxLevel + ", reportOnly=" + reportOnly);

		final Collection<DefaultCreature> creaturesToBalance =
				selectCreatures(creatures, args);

		for (final DefaultCreature creature : creaturesToBalance) {
			final int level = creature.getLevel();
			if (level > maxLevel) {
				continue;
			}

			final Creature target = creature.getCreature();
			durationThreshold = DEFAULT_DURATION_THRESHOLD;

			System.out.println("\n=== " + creature.getCreatureName()
					+ " (level " + level + ") ===");
			System.out.println("Current creature: ATK=" + target.getAtk()
					+ " DEF=" + target.getDef() + " HP="
					+ target.getBaseHP() + " XP=" + creature.getXP());

			PlayerBuild referenceBuild = null;
			CombatSummary referenceSummary = null;
			for (final PlayerProfile profile : PlayerProfile.values()) {
				final PlayerBuild build = createPlayer(em, level, profile);
				final CombatSummary summary = combat(build.player, target, rounds);
				printProfileResult(profile, build, summary);

				if (profile == referenceProfile) {
					referenceBuild = build;
					referenceSummary = summary;
				}
			}

			if (referenceBuild == null || referenceSummary == null) {
				throw new IllegalStateException(
						"Reference player profile was not created");
			}

			player = referenceBuild.player;
			Integer proposedXPValue = referenceSummary.isUsefulForXp()
					? Integer.valueOf(proposedXp(creature.getLevel(),
							referenceSummary.meanTurns)) : null;
			printReferenceXp(referenceProfile, creature, proposedXPValue);

			if (!reportOnly) {
				final Optimizer optimizer = new Optimizer(target);
				boolean balanced = isCorrectResult(level,
						referenceSummary.meanTurns,
						referenceSummary.meanPlayerHp
								/ (double) player.getBaseHP());
				int tries = 0;

				while (!balanced) {
					optimizer.step(referenceSummary.meanPlayerHp,
							referenceSummary.meanTurns);
					referenceSummary = combat(player, target, rounds);
					proposedXPValue = referenceSummary.isUsefulForXp()
							? Integer.valueOf(proposedXp(creature.getLevel(),
									referenceSummary.meanTurns)) : null;
					if (proposedXPValue != null) {
						creature.setLevel(creature.getLevel(),
								proposedXPValue.intValue());
					}

					System.out.println("Optimizer " + referenceProfile
							+ ": ATK=" + target.getAtk() + " DEF="
							+ target.getDef() + " HP=" + target.getBaseHP()
							+ " turns=" + referenceSummary.meanTurns
							+ " leftHP=" + referenceSummary.meanPlayerHp
							+ " winRate="
							+ formatPercent(referenceSummary.getWinRate()));

					balanced = isCorrectResult(level,
							referenceSummary.meanTurns,
							referenceSummary.meanPlayerHp
									/ (double) player.getBaseHP());

					tries++;
					if (tries % 200 == 0) {
						durationThreshold *= 1.1;
					}
					if (tries >= 2000) {
						System.out.println("WARNING: optimizer stopped after "
								+ tries + " attempts");
						break;
					}
				}
			}

			final boolean changed = creature.getAtk() != target.getAtk()
					|| creature.getDef() != target.getDef()
					|| creature.getHP() != target.getBaseHP();
			final StringBuilder line = new StringBuilder();
			line.append(creature.getCreatureName());
			line.append(" (level ").append(creature.getLevel()).append("):");
			line.append(changed ? " *\t" : "  \t");
			line.append("ATK: ").append(target.getAtk());
			line.append("\t\tDEF: ").append(target.getDef());
			line.append("\t\tHP: ").append(target.getBaseHP());
			if (System.getProperty("showxp") != null) {
				line.append("\t\tXP: ")
						.append(proposedXPValue == null ? "n/a" : proposedXPValue);
			}
			suggestions.add(line.toString());
		}

		if (suggestions.isEmpty()) {
			System.out.println("\nNo suggestions available\n");
		} else {
			System.out.println("\nSuggested values:");
			for (final String suggestion : suggestions) {
				System.out.println("\t" + suggestion);
			}
			System.out.println();
		}
	}

	private static Collection<DefaultCreature> selectCreatures(
			final List<DefaultCreature> creatures, final String[] args) {
		if (args.length == 0) {
			return creatures;
		}

		final List<String> names = new ArrayList<String>();
		names.addAll(Arrays.asList(args));
		final List<DefaultCreature> selected =
				new ArrayList<DefaultCreature>();
		for (final DefaultCreature creature : creatures) {
			final String creatureName = creature.getCreatureName();
			if (names.contains(creatureName)) {
				selected.add(creature);
				names.removeAll(Collections.singleton(creatureName));
			}
		}

		if (!names.isEmpty()) {
			System.out.println("WARNING: Unknown creature(s): " + names);
		}
		return selected;
	}

	private static PlayerBuild createPlayer(final EntityManager em,
			final int level, final PlayerProfile profile) {
		final Player result =
				(Player) new PlayerTransformer().transform(new RPObject());
		result.setLevel(level);
		result.setBaseHP(100 + 10 * level);
		result.setAtk(profile.getCombatSkill(level));
		result.setDef(profile.getCombatSkill(level));

		final StringBuilder equipment = new StringBuilder();
		for (final String slot : BALANCE_SLOTS) {
			if (!isSlotExpectedAtLevel(slot, level, profile)) {
				continue;
			}

			final String starterName = starterItemName(slot, level, profile);
			final Item item;
			if (starterName != null) {
				item = em.getItem(starterName,
						commonItemContext());
			} else {
				final DefaultItem template = selectEquipmentTemplate(
						em, slot, level, profile);
				if (template == null) {
					continue;
				}
				item = em.getItem(template.getItemName(),
						itemContext(profile, slot, level));
			}

			if (item == null) {
				continue;
			}
			result.equip(slot, item);
			appendEquipment(equipment, slot, item);
		}

		return new PlayerBuild(result, equipment.toString());
	}

	private static boolean isSlotExpectedAtLevel(final String slot,
			final int level, final PlayerProfile profile) {
		if (SLOT_WEAPON.equals(slot) || SLOT_ARMOR.equals(slot)) {
			return true;
		}
		final int gearLevel = profile.getGearLevelCap(level);
		if (SLOT_HEAD.equals(slot) || SLOT_LEGS.equals(slot)
				|| SLOT_FEET.equals(slot)) {
			return gearLevel >= 5;
		}
		return SLOT_SHIELD.equals(slot) && gearLevel >= 10;
	}

	private static String starterItemName(final String slot, final int level,
			final PlayerProfile profile) {
		if (level > 2) {
			return null;
		}
		if (SLOT_ARMOR.equals(slot)) {
			return STARTER_ARMOR;
		}
		if (SLOT_WEAPON.equals(slot)) {
			if (level == 0 || profile == PlayerProfile.WEAK) {
				return STARTER_WEAPON;
			}
			return STARTER_AXE;
		}
		return null;
	}

	private static DefaultItem selectEquipmentTemplate(final EntityManager em,
			final String slot, final int level, final PlayerProfile profile) {
		final int gearLevelCap = profile.getGearLevelCap(level);
		final List<DefaultItem> candidates =
				collectEquipmentCandidates(em, slot, gearLevelCap);
		if (candidates.isEmpty()) {
			return null;
		}

		Collections.sort(candidates, new Comparator<DefaultItem>() {
			@Override
			public int compare(final DefaultItem first,
					final DefaultItem second) {
				final int scoreCompare = Double.compare(
						equipmentScore(first, slot),
						equipmentScore(second, slot));
				if (scoreCompare != 0) {
					return scoreCompare;
				}
				return first.getItemName().compareTo(second.getItemName());
			}
		});

		final int index = (int) Math.round(
				profile.getEquipmentQuantile() * (candidates.size() - 1));
		return candidates.get(Math.max(0,
				Math.min(candidates.size() - 1, index)));
	}

	private static List<DefaultItem> collectEquipmentCandidates(
			final EntityManager em, final String slot, final int levelCap) {
		final List<DefaultItem> candidates = new ArrayList<DefaultItem>();
		for (final DefaultItem item : em.getDefaultItems()) {
			if (item == null || item.isUnattainable() || item.getCreator() == null
					|| item.getEquipableSlots() == null
					|| !item.getEquipableSlots().contains(slot)
					|| !hasConfiguredAttribute(item, "min_level")
					|| minimumLevel(item) > levelCap) {
				continue;
			}

			if (SLOT_WEAPON.equals(slot)) {
				if (!MELEE_WEAPON_CLASSES.contains(item.getItemClass())
						|| configuredAverageDamage(item) <= 0.0) {
					continue;
				}
			} else if (SLOT_SHIELD.equals(slot)) {
				if (!"shield".equals(item.getItemClass())
						|| configuredInt(item, "def", 0) <= 0) {
					continue;
				}
			} else if (configuredInt(item, "def", 0) <= 0) {
				continue;
			}
			candidates.add(item);
		}
		return candidates;
	}

	private static boolean hasConfiguredAttribute(final DefaultItem item,
			final String attribute) {
		final Map<String, String> attributes = item.getAttributes();
		return attributes != null && attributes.containsKey(attribute);
	}

	private static int minimumLevel(final DefaultItem item) {
		return Math.max(0, configuredInt(item, "min_level", 0));
	}

	private static double equipmentScore(final DefaultItem item,
			final String slot) {
		if (SLOT_WEAPON.equals(slot)) {
			return configuredAverageDamage(item)
					/ Math.max(1.0,
							configuredInt(item, "rate", DEFAULT_ITEM_ATTACK_RATE));
		}
		return configuredInt(item, "def", 0);
	}

	private static double configuredAverageDamage(final DefaultItem item) {
		final int attack = Math.max(configuredInt(item, "atk", 0),
				configuredInt(item, "ratk", 0));
		final int minimum = Math.max(0,
				configuredInt(item, "damage_min", attack));
		final int maximum = Math.max(minimum,
				configuredInt(item, "damage_max", minimum));
		return (minimum + maximum) / 2.0;
	}

	private static int configuredInt(final DefaultItem item,
			final String attribute, final int defaultValue) {
		final Map<String, String> attributes = item.getAttributes();
		if (attributes == null) {
			return defaultValue;
		}
		final String value = attributes.get(attribute);
		if (value == null || value.trim().length() == 0) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (final NumberFormatException e) {
			return defaultValue;
		}
	}

	private static ItemCreationContext commonItemContext() {
		return ItemCreationContext.builder(ItemCreationContext.Source.DEFAULT)
				.withFactoryRarity(ItemRarity.COMMON)
				.randomizeModifiers(false)
				.generateAffixes(false)
				.build();
	}

	private static ItemCreationContext itemContext(
			final PlayerProfile profile, final String slot, final int level) {
		return ItemCreationContext.builder(ItemCreationContext.Source.DEFAULT)
				.withFactoryRarity(profile.getRarity(slot, level))
				.randomizeModifiers(false)
				.generateAffixes(false)
				.build();
	}

	private static void appendEquipment(final StringBuilder equipment,
			final String slot, final Item item) {
		if (equipment.length() > 0) {
			equipment.append(", ");
		}
		equipment.append(slot).append('=').append(item.getName());
		if (item.has(Item.RARITY_ID)) {
			equipment.append('[').append(item.get(Item.RARITY_ID)).append(']');
		}
		if (SLOT_WEAPON.equals(slot)) {
			equipment.append("{dmg=")
					.append(String.format(Locale.ENGLISH, "%.1f",
							item.getAverageDamage()))
					.append(",rate=").append(item.getAttackRate()).append('}');
		} else if (item.getDefense() > 0) {
			equipment.append("{def=").append(item.getDefense()).append('}');
		}
	}

	private static void printProfileResult(final PlayerProfile profile,
			final PlayerBuild build, final CombatSummary summary) {
		final double leftHpPercent = build.player.getBaseHP() == 0
				? 0.0
				: summary.meanPlayerHp * 100.0 / build.player.getBaseHP();
		System.out.println(profile + ": winRate="
				+ formatPercent(summary.getWinRate()) + " turns="
				+ summary.meanTurns + " leftHP="
				+ String.format(Locale.ENGLISH, "%.1f%%", leftHpPercent)
				+ " ATK=" + build.player.getAtk() + " DEF="
				+ build.player.getDef() + " itemATK="
				+ String.format(Locale.ENGLISH, "%.1f",
						build.player.getItemAtk())
				+ " itemDEF="
				+ String.format(Locale.ENGLISH, "%.1f",
						build.player.getItemDef())
				+ " gear: " + build.equipment);
	}

	private static void printReferenceXp(final PlayerProfile profile,
			final DefaultCreature creature, final Integer proposedXp) {
		if (proposedXp == null) {
			System.out.println("Reference " + profile
					+ ": proposedXP=n/a currentXP=" + creature.getXP());
		return;
		}
		System.out.println("Reference " + profile + ": proposedXP="
				+ proposedXp + " currentXP=" + creature.getXP());
	}

	private static CombatSummary combat(final Player fighter,
			final Creature target, final int rounds) {
		int totalTurns = 0;
		int totalPlayerHp = 0;
		int wins = 0;
		for (int index = 0; index < rounds; index++) {
			final CombatResult result = combat(fighter, target);
			totalTurns += result.turns;
			totalPlayerHp += result.playerHp;
			if (result.playerWon) {
				wins++;
			}
		}
		return new CombatSummary(
				(int) (totalTurns / (rounds * 1.0)),
				(int) (totalPlayerHp / (rounds * 1.0)), wins, rounds);
	}

	private static CombatResult combat(final Player fighter,
			final Creature target) {
		target.setHP(target.getBaseHP());
		fighter.setHP(fighter.getBaseHP());

		int turns = 0;
		int healAmount = 0;
		int healRate = 0;
		final String healer = target.getAIProfile("heal");
		if (healer != null) {
			final String[] healingAttributes = healer.split(",");
			healAmount = Integer.parseInt(healingAttributes[0]);
			healRate = Integer.parseInt(healingAttributes[1]);
		}

		while (turns < MAX_COMBAT_TURNS) {
			turns++;
			if ((healAmount != 0) && (turns % healRate == 0)) {
				target.setHP(Math.min(target.getBaseHP(),
						target.getHP() + healAmount));
			}

			if ((turns % Math.max(1, fighter.getAttackRate()) == 0)
					&& fighter.canHit(target)) {
				int damage = fighter.damageDone(target,
						fighter.getItemAtkForAttack(), fighter.getDamageType());
				damage = WeaponArmorInteractionService.applyDamageMultiplier(
						damage, fighter.getWeapons(), target);
				damage = WeaponAffixCombatService.applyConditionalDamageBonuses(
						damage, fighter.getWeapons(), target, false);
				if (CriticalHitService.rollCritical(fighter)) {
					damage = CriticalHitService.applyCriticalDamage(fighter, damage);
				}
				damage = Math.max(0, Math.min(damage, target.getHP()));
				if (damage > 0) {
					fighter.handleLifesteal(fighter, fighter.getWeapons(), damage);
					target.setHP(target.getHP() - damage);
				}
				if (target.getHP() <= 0) {
					return new CombatResult(turns, fighter.getHP(), true);
				}
			}

			if ((turns % Math.max(1, target.getAttackRate()) == 0)
					&& target.canHit(fighter)) {
				if (!ParryService.rollParry(fighter)) {
					int damage = target.damageDone(fighter,
							target.getItemAtkForAttack(), fighter.getDamageType());
					damage = Math.max(0, Math.min(damage, fighter.getHP()));
					fighter.setHP(fighter.getHP() - damage);
					target.handleLifesteal(target, target.getWeapons(), damage);
				}
				if (fighter.getHP() <= 0) {
					return new CombatResult(turns, fighter.getHP(), false);
				}
			}
		}
		return new CombatResult(turns, fighter.getHP(), false);
	}

	private static int proposedXp(final int level, final int meanTurns) {
		return (int) ((2 * level + 1) * (meanTurns / 2.0));
	}

	private static boolean isCorrectResult(final int level,
			final int meanTurns, final double relativeLeftHP) {
		if (!isWithinDurationRange(preferredDuration(level), meanTurns)) {
			return false;
		}
		return relativeLeftHP <= 0.1 && relativeLeftHP >= 0.0;
	}

	private static boolean isWithinDurationRange(final double preferred,
			final double real) {
		return real < (1.0 + durationThreshold) * preferred
				&& real > (1.0 - durationThreshold) * preferred;
	}

	private static double preferredDuration(final int level) {
		return 150 + level;
	}

	private static int intProperty(final String key, final int defaultValue,
			final int minimum, final int maximum) {
		final String value = System.getProperty(key);
		if (value == null || value.trim().length() == 0) {
			return defaultValue;
		}
		try {
			final int parsed = Integer.parseInt(value.trim());
			if (parsed < minimum || parsed > maximum) {
				throw new IllegalArgumentException(key + " must be in ["
						+ minimum + ", " + maximum + "]");
			}
			return parsed;
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException(
					key + " must be an integer: " + value, e);
		}
	}

	private static String formatPercent(final double value) {
		return String.format(Locale.ENGLISH, "%.1f%%", value);
	}
}
