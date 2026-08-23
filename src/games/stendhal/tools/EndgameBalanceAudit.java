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
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

/**
 * Fixed-loadout endgame balance audit.
 *
 * The normal balancer answers whether a creature is reasonable for a
 * same-level automatically selected build. This tool instead measures how
 * authored high-level creatures cope with real mithril loadouts.
 *
 * All items are COMMON and have no random affixes. MITHRIL_SET is the six-piece
 * set used by the achievement plus the mithril dagger. MITHRIL_COMBAT adds the
 * mithril shield, cloak, amulet and two rings. MITHRIL_COMBAT_MAX additionally
 * applies every configured item upgrade. The tool is report-only and never
 * changes creature definitions.
 */
public final class EndgameBalanceAudit {
	private static final int MAX_COMBAT_TURNS = 5000;
	private static final int DEFAULT_PLAYER_LEVEL = 350;
	private static final int DEFAULT_MIN_LEVEL = 250;
	private static final int DEFAULT_MAX_LEVEL = 500;
	private static final int DEFAULT_ROUNDS = 30;
	private static final int DEFAULT_ATTRITION_SERIES = 10;
	private static final int DEFAULT_ATTRITION_FIGHTS = 5;

	private static final String SLOT_WEAPON = "rhand";
	private static final String SLOT_SHIELD = "lhand";
	private static final String SLOT_ARMOR = "armor";
	private static final String SLOT_HEAD = "head";
	private static final String SLOT_LEGS = "legs";
	private static final String SLOT_FEET = "feet";
	private static final String SLOT_GLOVE = "glove";
	private static final String SLOT_BELT = "pas";
	private static final String SLOT_CLOAK = "cloak";
	private static final String SLOT_NECK = "neck";
	private static final String SLOT_FINGER = "finger";
	private static final String SLOT_FINGER_B = "fingerb";

	private enum Loadout {
		MITHRIL_SET(false, false),
		MITHRIL_COMBAT(true, false),
		MITHRIL_COMBAT_MAX(true, true);

		private final boolean complete;
		private final boolean maxUpgrades;

		Loadout(final boolean complete, final boolean maxUpgrades) {
			this.complete = complete;
			this.maxUpgrades = maxUpgrades;
		}
	}

	private enum Threat {
		TRIVIAL,
		EASY,
		CONTESTED,
		DANGEROUS,
		LETHAL
	}

	private static final class PlayerBuild {
		private final Player player;
		private final String equipment;

		PlayerBuild(final Player player, final String equipment) {
			this.player = player;
			this.equipment = equipment;
		}
	}

	private static final class FightResult {
		private final int turns;
		private final int playerHp;
		private final boolean won;
		private final int incomingDamage;
		private final int lifestealHealing;

		FightResult(final int turns, final int playerHp, final boolean won,
				final int incomingDamage, final int lifestealHealing) {
			this.turns = turns;
			this.playerHp = playerHp;
			this.won = won;
			this.incomingDamage = incomingDamage;
			this.lifestealHealing = lifestealHealing;
		}
	}

	private static final class CombatSummary {
		private final int meanTurns;
		private final int meanPlayerHp;
		private final int wins;
		private final int rounds;
		private final int meanIncomingDamage;
		private final int meanLifestealHealing;

		CombatSummary(final int meanTurns, final int meanPlayerHp, final int wins,
				final int rounds, final int meanIncomingDamage,
				final int meanLifestealHealing) {
			this.meanTurns = meanTurns;
			this.meanPlayerHp = meanPlayerHp;
			this.wins = wins;
			this.rounds = rounds;
			this.meanIncomingDamage = meanIncomingDamage;
			this.meanLifestealHealing = meanLifestealHealing;
		}

		double winRate() {
			return rounds == 0 ? 0.0 : wins * 100.0 / rounds;
		}
	}

	private static final class AttritionSummary {
		private final int completedSeries;
		private final int series;
		private final double meanFightsSurvived;
		private final int meanEndHp;

		AttritionSummary(final int completedSeries, final int series,
				final double meanFightsSurvived, final int meanEndHp) {
			this.completedSeries = completedSeries;
			this.series = series;
			this.meanFightsSurvived = meanFightsSurvived;
			this.meanEndHp = meanEndHp;
		}

		double completionRate() {
			return series == 0 ? 0.0 : completedSeries * 100.0 / series;
		}
	}

	private EndgameBalanceAudit() {
	}

	public static void main(final String[] args) throws Exception {
		new RPClassGenerator().createRPClasses();
		final CreatureGroupsXMLLoader loader =
				new CreatureGroupsXMLLoader("/data/conf/creatures.xml");
		final List<DefaultCreature> creatures = loader.load();
		Collections.sort(creatures, new Comparator<DefaultCreature>() {
			@Override
			public int compare(final DefaultCreature a, final DefaultCreature b) {
				return a.getLevel() - b.getLevel();
			}
		});

		final EntityManager em = SingletonRepository.getEntityManager();
		final int playerLevel = intProperty("balance.endgame.playerLevel",
				DEFAULT_PLAYER_LEVEL, 1, 500);
		final int minLevel = intProperty("balance.endgame.minLevel",
				DEFAULT_MIN_LEVEL, 0, 500);
		final int maxLevel = intProperty("balance.endgame.maxLevel",
				DEFAULT_MAX_LEVEL, minLevel, 500);
		final int rounds = intProperty("balance.endgame.rounds",
				DEFAULT_ROUNDS, 1, 1000);
		final int attritionSeries = intProperty("balance.endgame.attritionSeries",
				DEFAULT_ATTRITION_SERIES, 1, 1000);
		final int attritionFights = intProperty("balance.endgame.attritionFights",
				DEFAULT_ATTRITION_FIGHTS, 1, 100);

		System.out.println("EndgameBalanceAudit: playerLevel=" + playerLevel
				+ " creatureLevels=" + minLevel + ".." + maxLevel
				+ " rounds=" + rounds + " attrition=" + attritionSeries
				+ "x" + attritionFights);
		System.out.println("All loadouts: COMMON rarity, no random affixes.");

		final EnumMap<Loadout, PlayerBuild> builds =
				new EnumMap<Loadout, PlayerBuild>(Loadout.class);
		final EnumMap<Loadout, EnumMap<Threat, Integer>> totals =
				new EnumMap<Loadout, EnumMap<Threat, Integer>>(Loadout.class);
		for (final Loadout loadout : Loadout.values()) {
			final PlayerBuild build = createPlayer(em, playerLevel, loadout);
			builds.put(loadout, build);
			final EnumMap<Threat, Integer> counts =
					new EnumMap<Threat, Integer>(Threat.class);
			for (final Threat threat : Threat.values()) {
				counts.put(threat, Integer.valueOf(0));
			}
			totals.put(loadout, counts);
			System.out.println("\n" + loadout + " build: level=" + playerLevel
					+ " ATK=" + build.player.getAtk() + " DEF="
					+ build.player.getDef() + " HP=" + build.player.getBaseHP()
					+ " itemATK=" + format(build.player.getItemAtk())
					+ " itemDEF=" + format(build.player.getItemDef())
					+ " attackRate=" + build.player.getAttackRate());
			System.out.println("gear: " + build.equipment);
		}

		final List<String> suspicious = new ArrayList<String>();
		for (final DefaultCreature definition : creatures) {
			if (definition.getLevel() < minLevel || definition.getLevel() > maxLevel) {
				continue;
			}

			System.out.println("\n=== " + definition.getCreatureName()
					+ " level=" + definition.getLevel() + " ATK="
					+ definition.getAtk() + " DEF=" + definition.getDef()
					+ " HP=" + definition.getHP() + " XP=" + definition.getXP()
					+ " mechanics=" + mechanics(definition) + " ===");

			for (final Loadout loadout : Loadout.values()) {
				final PlayerBuild build = builds.get(loadout);
				final Creature target = definition.getCreature();
				final CombatSummary summary = combat(build.player, target, rounds);
				final AttritionSummary attrition = attrition(build.player, target,
						attritionSeries, attritionFights);
				final Threat threat = assess(build, summary);
				increment(totals.get(loadout), threat);

				final double leftHp = relativeHp(build.player, summary.meanPlayerHp);
				final double attritionHp = relativeHp(build.player, attrition.meanEndHp);
				final double sustain = summary.meanIncomingDamage <= 0 ? 0.0
						: summary.meanLifestealHealing * 100.0
								/ summary.meanIncomingDamage;
				System.out.println(loadout + ": " + threat + " win="
						+ percent(summary.winRate()) + " leftHP="
						+ percent(leftHp * 100.0) + " turns=" + summary.meanTurns
						+ " incoming=" + summary.meanIncomingDamage
						+ " lifesteal=" + summary.meanLifestealHealing
						+ " sustain=" + percent(sustain)
						+ " attrition=" + percent(attrition.completionRate())
						+ " seriesComplete, fights="
						+ String.format(Locale.ENGLISH, "%.2f",
								attrition.meanFightsSurvived) + "/" + attritionFights
						+ " endHP=" + percent(attritionHp * 100.0));

				if (loadout == Loadout.MITHRIL_COMBAT_MAX
						&& definition.getLevel() >= playerLevel
						&& !hasPartiallyModeledOffense(definition)
						&& threat == Threat.TRIVIAL
						&& attrition.completionRate() >= 99.0
						&& attritionHp >= 0.80) {
					suspicious.add(definition.getCreatureName() + " (level "
							+ definition.getLevel() + ") win="
							+ percent(summary.winRate()) + " leftHP="
							+ percent(leftHp * 100.0) + " attritionEndHP="
							+ percent(attritionHp * 100.0));
				}
			}
		}

		System.out.println("\nEndgame threat summary:");
		for (final Loadout loadout : Loadout.values()) {
			System.out.println(loadout + ":");
			for (final Threat threat : Threat.values()) {
				System.out.println("\t" + threat + ": "
						+ totals.get(loadout).get(threat));
			}
		}

		System.out.println("\nSame-or-higher-level creatures trivialized by the"
				+ " maxed complete mithril loadout:");
		if (suspicious.isEmpty()) {
			System.out.println("\t(none)");
		} else {
			for (final String line : suspicious) {
				System.out.println("\t" + line);
			}
		}
	}

	private static PlayerBuild createPlayer(final EntityManager em,
			final int level, final Loadout loadout) {
		final Player result =
				(Player) new PlayerTransformer().transform(new RPObject());
		result.setLevel(level);
		result.setBaseHP(100 + 10 * level);
		result.setAtk(10 + level);
		result.setDef(10 + level);

		final StringBuilder equipment = new StringBuilder();
		equip(em, result, equipment, SLOT_WEAPON, "sztylecik z mithrilu",
				loadout.maxUpgrades);
		equip(em, result, equipment, SLOT_ARMOR, "zbroja z mithrilu",
				loadout.maxUpgrades);
		equip(em, result, equipment, SLOT_HEAD, "hełm z mithrilu",
				loadout.maxUpgrades);
		equip(em, result, equipment, SLOT_LEGS, "spodnie z mithrilu",
				loadout.maxUpgrades);
		equip(em, result, equipment, SLOT_GLOVE, "rękawice z mithrilu",
				loadout.maxUpgrades);
		equip(em, result, equipment, SLOT_BELT, "pas z mithrilu",
				loadout.maxUpgrades);
		equip(em, result, equipment, SLOT_FEET, "buty z mithrilu",
				loadout.maxUpgrades);
		if (loadout.complete) {
			equip(em, result, equipment, SLOT_SHIELD, "tarcza z mithrilu",
					loadout.maxUpgrades);
			equip(em, result, equipment, SLOT_CLOAK, "płaszcz z mithrilu",
					loadout.maxUpgrades);
			equip(em, result, equipment, SLOT_NECK, "amulecik z mithrilu",
					loadout.maxUpgrades);
			equip(em, result, equipment, SLOT_FINGER, "pierścień z mithrilu",
					loadout.maxUpgrades);
			equip(em, result, equipment, SLOT_FINGER_B, "pierścień z mithrilu",
					loadout.maxUpgrades);
		}
		return new PlayerBuild(result, equipment.toString());
	}

	private static void equip(final EntityManager em, final Player player,
			final StringBuilder equipment, final String slot,
			final String itemName, final boolean maxUpgrade) {
		final Item item = em.getItem(itemName, commonItemContext());
		if (item == null) {
			throw new IllegalStateException("Missing configured endgame item: "
					+ itemName);
		}
		if (maxUpgrade && item.getMaxUpgradeLevel() > 0) {
			item.setUpgradeLevel(item.getMaxUpgradeLevel());
		}
		player.equip(slot, item);
		if (equipment.length() > 0) {
			equipment.append(", ");
		}
		equipment.append(slot).append('=').append(item.getName());
		if (item.getUpgradeLevel() > 0) {
			equipment.append('+').append(item.getUpgradeLevel());
		}
		if (SLOT_WEAPON.equals(slot)) {
			final double lifesteal = item.has("lifesteal")
					? item.getDouble("lifesteal") : 0.0;
			equipment.append("{dmg=").append(format(item.getAverageDamage()))
					.append(",rate=").append(item.getAttackRate())
					.append(",lifesteal=")
					.append(percent(lifesteal * 100.0)).append('}');
		} else if (item.getDefense() != 0) {
			equipment.append("{def=").append(item.getDefense()).append('}');
		}
	}

	private static ItemCreationContext commonItemContext() {
		return ItemCreationContext.builder(ItemCreationContext.Source.DEFAULT)
				.withFactoryRarity(ItemRarity.COMMON)
				.randomizeModifiers(false)
				.generateAffixes(false)
				.build();
	}

	private static CombatSummary combat(final Player fighter,
			final Creature target, final int rounds) {
		long turns = 0;
		long hp = 0;
		long incoming = 0;
		long lifesteal = 0;
		int wins = 0;
		for (int i = 0; i < rounds; i++) {
			fighter.setHP(fighter.getBaseHP());
			final FightResult result = fight(fighter, target);
			turns += result.turns;
			hp += result.playerHp;
			incoming += result.incomingDamage;
			lifesteal += result.lifestealHealing;
			if (result.won) {
				wins++;
			}
		}
		return new CombatSummary((int) (turns / rounds), (int) (hp / rounds),
				wins, rounds, (int) (incoming / rounds),
				(int) (lifesteal / rounds));
	}

	private static AttritionSummary attrition(final Player fighter,
			final Creature target, final int series, final int fightsPerSeries) {
		int completed = 0;
		int totalFights = 0;
		long endHp = 0;
		for (int i = 0; i < series; i++) {
			fighter.setHP(fighter.getBaseHP());
			int survived = 0;
			for (int fightIndex = 0; fightIndex < fightsPerSeries; fightIndex++) {
				final FightResult result = fight(fighter, target);
				if (!result.won) {
					break;
				}
				survived++;
			}
			totalFights += survived;
			if (survived == fightsPerSeries) {
				completed++;
			}
			endHp += Math.max(0, fighter.getHP());
		}
		return new AttritionSummary(completed, series,
				totalFights / (double) series, (int) (endHp / series));
	}

	private static FightResult fight(final Player fighter, final Creature target) {
		target.setHP(target.getBaseHP());
		int turns = 0;
		int incomingDamage = 0;
		int lifestealHealing = 0;
		int healAmount = 0;
		int healRate = 0;
		final String healer = target.getAIProfile("heal");
		if (healer != null) {
			final String[] healing = healer.split(",");
			if (healing.length >= 2) {
				try {
					healAmount = Integer.parseInt(healing[0]);
					healRate = Integer.parseInt(healing[1]);
				} catch (final NumberFormatException e) {
					healAmount = 0;
					healRate = 0;
				}
			}
		}

		while (turns < MAX_COMBAT_TURNS) {
			turns++;
			if (healAmount != 0 && healRate > 0 && turns % healRate == 0) {
				target.setHP(Math.min(target.getBaseHP(),
						target.getHP() + healAmount));
			}

			if (turns % Math.max(1, fighter.getAttackRate()) == 0
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
					final int hpBefore = fighter.getHP();
					fighter.handleLifesteal(fighter, fighter.getWeapons(), damage);
					lifestealHealing += Math.max(0, fighter.getHP() - hpBefore);
					target.setHP(target.getHP() - damage);
				}
				if (target.getHP() <= 0) {
					return new FightResult(turns, fighter.getHP(), true,
							incomingDamage, lifestealHealing);
				}
			}

			if (turns % Math.max(1, target.getAttackRate()) == 0
					&& target.canHit(fighter)) {
				if (!ParryService.rollParry(fighter)) {
					int damage = target.damageDone(fighter,
							target.getItemAtkForAttack(), fighter.getDamageType());
					damage = WeaponArmorInteractionService.applyDamageMultiplier(
							damage, target.getWeapons(), fighter);
					damage = WeaponAffixCombatService.applyConditionalDamageBonuses(
							damage, target.getWeapons(), fighter, false);
					damage = Math.max(0, Math.min(damage, fighter.getHP()));
					incomingDamage += damage;
					fighter.setHP(fighter.getHP() - damage);
					target.handleLifesteal(target, target.getWeapons(), damage);
				}
				if (fighter.getHP() <= 0) {
					return new FightResult(turns, fighter.getHP(), false,
							incomingDamage, lifestealHealing);
				}
			}
		}
		return new FightResult(turns, fighter.getHP(), false,
				incomingDamage, lifestealHealing);
	}

	private static Threat assess(final PlayerBuild build,
			final CombatSummary summary) {
		final double hp = relativeHp(build.player, summary.meanPlayerHp);
		if (summary.winRate() >= 99.0 && hp >= 0.90) {
			return Threat.TRIVIAL;
		}
		if (summary.winRate() >= 95.0 && hp >= 0.70) {
			return Threat.EASY;
		}
		if (summary.winRate() >= 70.0 && hp >= 0.30) {
			return Threat.CONTESTED;
		}
		if (summary.winRate() >= 30.0) {
			return Threat.DANGEROUS;
		}
		return Threat.LETHAL;
	}

	private static String mechanics(final DefaultCreature definition) {
		final List<String> values = new ArrayList<String>();
		if (definition.getRatk() > 0) {
			values.add("ranged-partial");
		}
		if (definition.getStatusAttack() != null
				&& definition.getStatusAttack().trim().length() > 0) {
			values.add("status-partial");
		}
		final Map<String, String> ai = definition.getAiProfiles();
		if (ai != null) {
			if (ai.containsKey("poisonous")) {
				values.add("poison-partial");
			}
			if (ai.containsKey("heal")) {
				values.add("heal-modeled");
			}
			if (ai.containsKey("boss")) {
				values.add("boss");
			}
			if (ai.containsKey("lifesteal")) {
				values.add("lifesteal");
			}
		}
		return values.isEmpty() ? "normal" : values.toString();
	}

	private static boolean hasPartiallyModeledOffense(
			final DefaultCreature definition) {
		if (definition.getRatk() > 0) {
			return true;
		}
		if (definition.getStatusAttack() != null
				&& definition.getStatusAttack().trim().length() > 0) {
			return true;
		}
		final Map<String, String> ai = definition.getAiProfiles();
		return ai != null && ai.containsKey("poisonous");
	}

	private static void increment(final EnumMap<Threat, Integer> counts,
			final Threat threat) {
		counts.put(threat, Integer.valueOf(counts.get(threat).intValue() + 1));
	}

	private static double relativeHp(final Player player, final int hp) {
		return player.getBaseHP() <= 0 ? 0.0 : hp / (double) player.getBaseHP();
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
			throw new IllegalArgumentException(key + " must be an integer: "
					+ value, e);
		}
	}

	private static String format(final double value) {
		return String.format(Locale.ENGLISH, "%.1f", value);
	}

	private static String percent(final double value) {
		return String.format(Locale.ENGLISH, "%.1f%%", value);
	}
}
