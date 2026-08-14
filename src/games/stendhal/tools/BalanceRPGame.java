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
import java.util.EnumMap;
import java.util.HashMap;
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
 * This tool intentionally answers two different questions:
 * 1. Is a same-level creature safe and reasonable for a normal player build?
 * 2. If it is clearly too hard, which ATK/DEF direction would move it into the
 *    target band without flattening all easy creatures into the same fight?
 *
 * Player builds are deterministic and use real configured items. The expected
 * profile also models guaranteed early-game progression: the starter weapon and
 * armour, the starter ciupaga carried by a new character, and the puklerz from
 * the introductory Pietrek quest. This is important because the old balancer
 * accidentally removed the starter ciupaga after level 2 and delayed shields
 * until level 10, creating an artificial difficulty cliff.
 *
 * For levels 0-9 the automatic item pool is deliberately restricted to starter
 * equipment plus equipment sold by the early Semos-area merchants. This keeps
 * quest/event prizes (for example a lucky min-level-0 reward) from silently
 * becoming the assumed baseline. Level 0 is always the exact starter loadout.
 * Above the onboarding range, automatic equipment selection is monotonic: when
 * the pool of level-eligible items grows, a profile is never downgraded merely
 * because the percentile moved.
 *
 * Creatures using combat mechanics which this simulator does not model fully
 * (ranged attacks, status attacks, healing AI, invulnerable-style defence, zero
 * XP or one-hit utility entities) are marked SPECIAL and never auto-optimized.
 * Very long-respawn creatures are marked ELITE and are also never flattened to
 * the ordinary same-level target band.
 *
 * Useful properties:
 * -Dbalance.profile=weak|expected|strong
 * -Dbalance.rounds=100
 * -Dbalance.maxLevel=30
 * -Dbalance.reportOnly=true
 * -Dbalance.hardenEasy=false
 */
public class BalanceRPGame {

	private static final int DEFAULT_ROUNDS = 100;
	private static final int HIGHEST_LEVEL = 500;
	private static final int MAX_COMBAT_TURNS = 5000;
	private static final int DEFAULT_ITEM_ATTACK_RATE = 5;
	private static final int MAX_OPTIMIZER_STEPS = 80;
	private static final int EARLY_GAME_MAX_LEVEL = 9;
	private static final int ELITE_RESPAWN_THRESHOLD = 5000;

	private static final String SLOT_WEAPON = "rhand";
	private static final String SLOT_SHIELD = "lhand";
	private static final String SLOT_ARMOR = "armor";
	private static final String SLOT_HEAD = "head";
	private static final String SLOT_LEGS = "legs";
	private static final String SLOT_FEET = "feet";

	private static final String STARTER_WEAPON = "maczuga";
	private static final String STARTER_AXE = "ciupaga startowa";
	private static final String STARTER_ARMOR = "skórzana zbroja";
	private static final String TUTORIAL_SHIELD = "puklerz";

	private static final String[] BALANCE_SLOTS = {
		SLOT_WEAPON, SLOT_SHIELD, SLOT_ARMOR, SLOT_HEAD, SLOT_LEGS, SLOT_FEET
	};

	private static final Set<String> MELEE_WEAPON_CLASSES =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
					"club", "sword", "dagger", "axe", "whip")));

	/**
	 * Equipment that a new player can realistically own without relying on a
	 * remote quest, event, gamble or rare drop. It consists of the starter set
	 * and the inexpensive early merchant stock seen around the onboarding path.
	 */
	private static final Set<String> EARLY_ACCESSIBLE_ITEMS =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
					STARTER_WEAPON, STARTER_AXE, STARTER_ARMOR, TUTORIAL_SHIELD,
					"sierp", "nożyk", "sztylecik", "mieczyk",
					"drewniana tarcza", "koszula", "skórzany hełm",
					"misiurka", "peleryna", "skórzane spodnie",
					"toporek", "topór jednoręczny", "topór", "pyrlik",
					"buty skórzane", "hełm nabijany ćwiekami",
					"tarcza ćwiekowa", "miecz", "płaszcz krasnoludzki")));

	private static final List<String> suggestions = new LinkedList<String>();
	private static final Map<String, DefaultItem> equipmentSelectionCache =
			new HashMap<String, DefaultItem>();

	private static Player player;

	private enum PlayerProfile {
		WEAK(0.15, 6, 0.75),
		EXPECTED(0.45, 2, 1.00),
		STRONG(0.75, 0, 1.25);

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
			if (this == WEAK || level < 10) {
				return ItemRarity.COMMON;
			}
			if (this == EXPECTED) {
				if (level >= 30 && (SLOT_WEAPON.equals(slot)
						|| SLOT_ARMOR.equals(slot))) {
					return ItemRarity.RARE;
				}
				if (level >= 20 && SLOT_WEAPON.equals(slot)) {
					return ItemRarity.RARE;
				}
				return ItemRarity.COMMON;
			}
			if (level >= 25 && (SLOT_WEAPON.equals(slot)
					|| SLOT_ARMOR.equals(slot))) {
				return ItemRarity.EPIC;
			}
			if (level >= 10) {
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

	private enum BalanceStatus {
		BALANCED,
		TOO_HARD,
		TOO_SLOW,
		TOO_EASY,
		ELITE,
		SPECIAL
	}

	private static final class BalanceBand {
		private final double minWinRate;
		private final double minLeftHp;
		private final double maxLeftHp;
		private final int minTurns;
		private final int maxTurns;

		BalanceBand(final double minWinRate, final double minLeftHp,
				final double maxLeftHp, final int minTurns, final int maxTurns) {
			this.minWinRate = minWinRate;
			this.minLeftHp = minLeftHp;
			this.maxLeftHp = maxLeftHp;
			this.minTurns = minTurns;
			this.maxTurns = maxTurns;
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
		final boolean hardenEasy = Boolean.parseBoolean(
				System.getProperty("balance.hardenEasy", "false"));

		System.out.println("BalanceRPGame: reference profile="
				+ referenceProfile + ", rounds=" + rounds + ", maxLevel="
				+ maxLevel + ", reportOnly=" + reportOnly
				+ ", hardenEasy=" + hardenEasy);

		final EnumMap<BalanceStatus, Integer> statusCounts =
				new EnumMap<BalanceStatus, Integer>(BalanceStatus.class);
		for (final BalanceStatus status : BalanceStatus.values()) {
			statusCounts.put(status, Integer.valueOf(0));
		}

		final Collection<DefaultCreature> creaturesToBalance =
				selectCreatures(creatures, args);

		for (final DefaultCreature definition : creaturesToBalance) {
			final int level = definition.getLevel();
			if (level > maxLevel) {
				continue;
			}

			final Creature target = definition.getCreature();
			final int originalAtk = target.getAtk();
			final int originalDef = target.getDef();
			final int originalHp = target.getBaseHP();

			System.out.println("\n=== " + definition.getCreatureName()
					+ " (level " + level + ") ===");
			System.out.println("Current creature: ATK=" + target.getAtk()
					+ " DEF=" + target.getDef() + " HP="
					+ target.getBaseHP() + " XP=" + definition.getXP()
					+ " respawn=" + definition.getRespawnTime());

			final EnumMap<PlayerProfile, PlayerBuild> builds =
					new EnumMap<PlayerProfile, PlayerBuild>(PlayerProfile.class);
			final EnumMap<PlayerProfile, CombatSummary> summaries =
					new EnumMap<PlayerProfile, CombatSummary>(PlayerProfile.class);

			for (final PlayerProfile profile : PlayerProfile.values()) {
				final PlayerBuild build = createPlayer(em, level, profile);
				final CombatSummary summary = combat(build.player, target, rounds);
				builds.put(profile, build);
				summaries.put(profile, summary);
				printProfileResult(profile, build, summary);
			}

			PlayerBuild referenceBuild = builds.get(referenceProfile);
			CombatSummary referenceSummary = summaries.get(referenceProfile);
			player = referenceBuild.player;

			BalanceStatus status = assess(definition, referenceBuild,
					referenceSummary);
			printAssessment(definition, referenceBuild, referenceSummary, status);
			printProfileSpread(summaries);

			if (!reportOnly && status != BalanceStatus.SPECIAL
					&& status != BalanceStatus.ELITE
					&& (status != BalanceStatus.TOO_EASY || hardenEasy)) {
				referenceSummary = optimize(definition, target, referenceBuild,
						referenceSummary, rounds, hardenEasy);
				status = assess(definition, referenceBuild, referenceSummary);
				System.out.println("Post-balance EXPECTED/reference: ATK="
						+ target.getAtk() + " DEF=" + target.getDef()
						+ " turns=" + referenceSummary.meanTurns
						+ " leftHP=" + formatPercent(relativeLeftHp(
								referenceBuild, referenceSummary) * 100.0)
						+ " winRate=" + formatPercent(referenceSummary.getWinRate())
						+ " status=" + status);
			}

			statusCounts.put(status,
					Integer.valueOf(statusCounts.get(status).intValue() + 1));

			final boolean changed = originalAtk != target.getAtk()
					|| originalDef != target.getDef()
					|| originalHp != target.getBaseHP();
			final StringBuilder line = new StringBuilder();
			line.append(definition.getCreatureName());
			line.append(" (level ").append(definition.getLevel()).append("):");
			line.append(changed ? " *\t" : "  \t");
			line.append("ATK: ").append(originalAtk).append(" -> ")
					.append(target.getAtk());
			line.append("\tDEF: ").append(originalDef).append(" -> ")
					.append(target.getDef());
			line.append("\tHP: ").append(originalHp);
			line.append("\tXP: keep ").append(definition.getXP());
			line.append("\tRESPAWN: ").append(definition.getRespawnTime());
			line.append("\tSTATUS: ").append(status);
			suggestions.add(line.toString());
		}

		System.out.println("\nBalance summary:");
		for (final BalanceStatus status : BalanceStatus.values()) {
			System.out.println("\t" + status + ": " + statusCounts.get(status));
		}

		System.out.println("\nSuggested values (XP intentionally unchanged in this pass):");
		for (final String suggestion : suggestions) {
			System.out.println("\t" + suggestion);
		}
		System.out.println();
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

			final Item item = selectEquipmentItem(em, slot, level, profile);
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
		if (SLOT_SHIELD.equals(slot)) {
			if (profile == PlayerProfile.STRONG) {
				return level >= 3;
			}
			if (profile == PlayerProfile.EXPECTED) {
				return level >= 4;
			}
			return level >= 6;
		}
		if (profile == PlayerProfile.STRONG) {
			return level >= 3;
		}
		if (profile == PlayerProfile.EXPECTED) {
			return level >= 5;
		}
		return level >= 8;
	}

	private static Item selectEquipmentItem(final EntityManager em,
			final String slot, final int level, final PlayerProfile profile) {
		/* A freshly created level-0 character has an exact, known loadout. */
		if (level == 0) {
			final String starterName = guaranteedItemName(slot, level, profile);
			return starterName == null ? null
					: em.getItem(starterName, commonItemContext());
		}

		Item selected = null;
		final DefaultItem template = selectEquipmentTemplate(em, slot, level,
				profile);
		if (template != null) {
			selected = em.getItem(template.getItemName(),
					itemContext(profile, slot, level));
		}

		final String guaranteedName = guaranteedItemName(slot, level, profile);
		if (guaranteedName != null) {
			final Item guaranteed = em.getItem(guaranteedName,
					commonItemContext());
			if (guaranteed != null && (selected == null
					|| itemEquipmentScore(guaranteed, slot)
							> itemEquipmentScore(selected, slot))) {
				selected = guaranteed;
			}
		}
		return selected;
	}

	private static String guaranteedItemName(final String slot, final int level,
			final PlayerProfile profile) {
		if (SLOT_ARMOR.equals(slot)) {
			return STARTER_ARMOR;
		}
		if (SLOT_WEAPON.equals(slot)) {
			if (level == 0 || profile == PlayerProfile.WEAK) {
				return STARTER_WEAPON;
			}
			return STARTER_AXE;
		}
		if (SLOT_SHIELD.equals(slot)) {
			if (profile == PlayerProfile.STRONG && level >= 3) {
				return TUTORIAL_SHIELD;
			}
			if (profile == PlayerProfile.EXPECTED && level >= 4) {
				return TUTORIAL_SHIELD;
			}
			if (profile == PlayerProfile.WEAK && level >= 6) {
				return TUTORIAL_SHIELD;
			}
		}
		return null;
	}

	private static DefaultItem selectEquipmentTemplate(final EntityManager em,
			final String slot, final int level, final PlayerProfile profile) {
		final String key = profile.name() + ':' + slot + ':' + level;
		if (equipmentSelectionCache.containsKey(key)) {
			return equipmentSelectionCache.get(key);
		}

		final int gearLevelCap = profile.getGearLevelCap(level);
		final List<DefaultItem> candidates =
				collectEquipmentCandidates(em, slot, gearLevelCap,
						level <= EARLY_GAME_MAX_LEVEL);
		DefaultItem selected = null;
		if (!candidates.isEmpty()) {
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
			selected = candidates.get(Math.max(0,
					Math.min(candidates.size() - 1, index)));
		}

		if (level > 0) {
			final DefaultItem previous = selectEquipmentTemplate(em, slot,
					level - 1, profile);
			if (previous != null && (selected == null
					|| equipmentScore(previous, slot)
							> equipmentScore(selected, slot))) {
				selected = previous;
			}
		}

		equipmentSelectionCache.put(key, selected);
		return selected;
	}

	private static List<DefaultItem> collectEquipmentCandidates(
			final EntityManager em, final String slot, final int levelCap,
			final boolean earlyGame) {
		final List<DefaultItem> candidates = new ArrayList<DefaultItem>();
		for (final DefaultItem item : em.getDefaultItems()) {
			if (item == null || item.isUnattainable() || item.getCreator() == null
					|| item.getEquipableSlots() == null
					|| !item.getEquipableSlots().contains(slot)
					|| !hasConfiguredAttribute(item, "min_level")
					|| minimumLevel(item) > levelCap) {
				continue;
			}
			if (earlyGame && !EARLY_ACCESSIBLE_ITEMS.contains(item.getItemName())) {
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

	private static double itemEquipmentScore(final Item item,
			final String slot) {
		if (SLOT_WEAPON.equals(slot)) {
			return item.getAverageDamage() / Math.max(1.0, item.getAttackRate());
		}
		return item.getDefense();
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
		System.out.println(profile + ": winRate="
				+ formatPercent(summary.getWinRate()) + " turns="
				+ summary.meanTurns + " leftHP="
				+ formatPercent(relativeLeftHp(build, summary) * 100.0)
				+ " ATK=" + build.player.getAtk() + " DEF="
				+ build.player.getDef() + " itemATK="
				+ String.format(Locale.ENGLISH, "%.1f",
						build.player.getItemAtk())
				+ " itemDEF="
				+ String.format(Locale.ENGLISH, "%.1f",
						build.player.getItemDef())
				+ " gear: " + build.equipment);
	}

	private static void printProfileSpread(
			final EnumMap<PlayerProfile, CombatSummary> summaries) {
		final double weak = summaries.get(PlayerProfile.WEAK).getWinRate();
		final double expected = summaries.get(PlayerProfile.EXPECTED).getWinRate();
		final double strong = summaries.get(PlayerProfile.STRONG).getWinRate();
		final double gap = strong - weak;
		System.out.println("Profile spread: WEAK=" + formatPercent(weak)
				+ " EXPECTED=" + formatPercent(expected) + " STRONG="
				+ formatPercent(strong) + " strong-weak=" + formatPercent(gap)
				+ (gap > 45.0 ? " GEAR_SENSITIVE" : ""));
	}

	private static BalanceStatus assess(final DefaultCreature definition,
			final PlayerBuild build, final CombatSummary summary) {
		if (!isModelCovered(definition)) {
			return BalanceStatus.SPECIAL;
		}
		if (isEliteCandidate(definition)) {
			return BalanceStatus.ELITE;
		}

		final BalanceBand band = balanceBand(definition.getLevel());
		final double leftHp = relativeLeftHp(build, summary);
		if (summary.getWinRate() < band.minWinRate || leftHp < band.minLeftHp) {
			return BalanceStatus.TOO_HARD;
		}
		if (summary.meanTurns > band.maxTurns) {
			return BalanceStatus.TOO_SLOW;
		}
		if (summary.getWinRate() >= 99.0 && leftHp > band.maxLeftHp
				&& summary.meanTurns < band.minTurns) {
			return BalanceStatus.TOO_EASY;
		}
		return BalanceStatus.BALANCED;
	}

	private static void printAssessment(final DefaultCreature definition,
			final PlayerBuild build, final CombatSummary summary,
			final BalanceStatus status) {
		final BalanceBand band = balanceBand(definition.getLevel());
		System.out.println("Assessment: " + status + " target{win>="
				+ formatPercent(band.minWinRate) + ", leftHP>="
				+ formatPercent(band.minLeftHp * 100.0) + ", turns="
				+ band.minTurns + ".." + band.maxTurns + "} actual{win="
				+ formatPercent(summary.getWinRate()) + ", leftHP="
				+ formatPercent(relativeLeftHp(build, summary) * 100.0)
				+ ", turns=" + summary.meanTurns + "}");
	}

	private static BalanceBand balanceBand(final int level) {
		if (level <= 2) {
			return new BalanceBand(95.0, 0.65, 0.97, 6, 45);
		}
		if (level <= 5) {
			return new BalanceBand(90.0, 0.50, 0.94, 8, 65);
		}
		if (level <= 9) {
			return new BalanceBand(85.0, 0.40, 0.92, 10, 80);
		}
		if (level <= 19) {
			return new BalanceBand(80.0, 0.30, 0.90, 12, 105);
		}
		if (level <= 39) {
			return new BalanceBand(75.0, 0.22, 0.88, 15, 130);
		}
		return new BalanceBand(70.0, 0.15, 0.85, 18, 160);
	}

	private static boolean isEliteCandidate(final DefaultCreature definition) {
		return definition.getRespawnTime() >= ELITE_RESPAWN_THRESHOLD;
	}

	private static boolean isModelCovered(final DefaultCreature definition) {
		if (definition.getXP() <= 0 || definition.getHP() <= 1
				|| definition.getRatk() > 0) {
			return false;
		}
		if (definition.getStatusAttack() != null
				&& definition.getStatusAttack().trim().length() > 0) {
			return false;
		}
		final Map<String, String> ai = definition.getAiProfiles();
		if (ai != null && ai.get("heal") != null) {
			return false;
		}
		final int excessiveDefence = Math.max(120,
				8 * (definition.getLevel() + 10));
		return definition.getDef() < excessiveDefence;
	}

	private static CombatSummary optimize(final DefaultCreature definition,
			final Creature target, final PlayerBuild referenceBuild,
			CombatSummary summary, final int rounds, final boolean hardenEasy) {
		for (int step = 0; step < MAX_OPTIMIZER_STEPS; step++) {
			final BalanceStatus status = assess(definition, referenceBuild, summary);
			if (status == BalanceStatus.BALANCED || status == BalanceStatus.SPECIAL
					|| status == BalanceStatus.ELITE
					|| (status == BalanceStatus.TOO_EASY && !hardenEasy)) {
				return summary;
			}

			final BalanceBand band = balanceBand(definition.getLevel());
			final double leftHp = relativeLeftHp(referenceBuild, summary);
			boolean changed = false;

			if (status == BalanceStatus.TOO_HARD) {
				if (summary.meanTurns > band.maxTurns) {
					target.setDef(reduceStat(target.getDef()));
					changed = true;
				}
				if (summary.getWinRate() < band.minWinRate
						|| leftHp < band.minLeftHp) {
					target.setAtk(reduceStat(target.getAtk()));
					changed = true;
				}
			} else if (status == BalanceStatus.TOO_SLOW) {
				target.setDef(reduceStat(target.getDef()));
				changed = true;
			} else if (status == BalanceStatus.TOO_EASY && hardenEasy) {
				if (summary.meanTurns < band.minTurns) {
					target.setDef(increaseStat(target.getDef()));
				} else {
					target.setAtk(increaseStat(target.getAtk()));
				}
				changed = true;
			}

			if (!changed) {
				return summary;
			}

			summary = combat(referenceBuild.player, target, rounds);
			System.out.println("Optimizer step " + (step + 1) + ": ATK="
					+ target.getAtk() + " DEF=" + target.getDef() + " turns="
					+ summary.meanTurns + " leftHP="
					+ formatPercent(relativeLeftHp(referenceBuild, summary) * 100.0)
					+ " winRate=" + formatPercent(summary.getWinRate()));
		}
		System.out.println("WARNING: optimizer stopped after "
				+ MAX_OPTIMIZER_STEPS + " steps");
		return summary;
	}

	private static int reduceStat(final int value) {
		final int change = Math.max(1, (int) Math.round(value * 0.05));
		return Math.max(1, value - change);
	}

	private static int increaseStat(final int value) {
		final int change = Math.max(1, (int) Math.round(value * 0.04));
		return value + change;
	}

	private static double relativeLeftHp(final PlayerBuild build,
			final CombatSummary summary) {
		return build.player.getBaseHP() == 0 ? 0.0
				: summary.meanPlayerHp / (double) build.player.getBaseHP();
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
					damage = WeaponArmorInteractionService.applyDamageMultiplier(
							damage, target.getWeapons(), fighter);
					damage = WeaponAffixCombatService.applyConditionalDamageBonuses(
							damage, target.getWeapons(), fighter, false);
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
