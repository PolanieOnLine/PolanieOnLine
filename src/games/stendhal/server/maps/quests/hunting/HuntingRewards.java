package games.stendhal.server.maps.quests.hunting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import games.stendhal.common.Level;
import games.stendhal.common.Rand;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.creature.Creature;

/** Modest, independent hunting loot pool; never uses the paid arena pool. */
public final class HuntingRewards {
	private static final List<String> EQUIPMENT = Collections.unmodifiableList(Arrays.asList(
			"mieczyk", "miecz", "miecz zaczepny", "pałasz", "półtorak", "miecz dwuręczny",
			"toporek", "topór", "topór bojowy", "berdysz", "halabarda",
			"skórzana zbroja", "skórzany kirys", "skórzana zbroja łuskowa",
			"skórzany hełm", "puklerz"));
	private static final List<String> SUPPLIES = Collections.unmodifiableList(Arrays.asList(
			"mały eliksir", "chleb", "polano", "ruda żelaza", "węgiel"));

	private HuntingRewards() { }
	public static List<String> getEquipmentNames() { return EQUIPMENT; }
	public static List<String> getSupplyNames() { return SUPPLIES; }

	/** A percentage of the full current skill-level gap, capped at the skill limit. */
	public static int skillXPBonus(int xp, HuntingDifficulty difficulty) {
		return skillXPBonus(xp, difficulty.getPercent() * 100);
	}

	/** The same frozen task percentage is applied independently to each skill. */
	public static int skillXPBonus(int xp, int basisPoints) {
		if (basisPoints < 1 || basisPoints > 10000) {
			throw new IllegalArgumentException("Invalid hunting bonus");
		}
		int skillLevel = Level.getLevel(xp);
		if (skillLevel >= Level.maxLevel()) { return 0; }
		long gap = (long) Level.getXP(skillLevel + 1) - Level.getXP(skillLevel);
		long bonus = gap * basisPoints / 10000;
		return (int) Math.min(bonus, (long) Level.getXP(Level.maxLevel()) - xp);
	}

	/** Work estimate, not a combat simulation: HP represents hits to land;
	 * attack and defence add a modest allowance for danger and resistance. */
	public static double strength(Creature creature) {
		int attack = Math.max(creature.getAtk(), creature.getRatk());
		return Math.max(1, creature.getBaseHP())
				* (1.0 + (Math.max(0, attack) + (double) Math.max(0, creature.getDef())) / 100.0);
	}

	/** Median public opponent near the character level, independent of the chosen
	 * difficulty. A nearest-level fallback also supports the beginning of the game. */
	public static double referenceStrength(int level, List<Creature> creatures) {
		List<Double> scores = new ArrayList<>();
		int nearest = Integer.MAX_VALUE;
		for (Creature creature : HuntingTargetSelector.select(
				level, HuntingDifficulty.MEDIUM, creatures, Creature::getLevel)) {
			scores.add(strength(creature));
		}
		for (Creature creature : creatures) {
			nearest = Math.min(nearest, Math.abs(level - creature.getLevel()));
		}
		if (scores.isEmpty()) {
			for (Creature creature : creatures) {
				if (Math.abs(level - creature.getLevel()) == nearest) { scores.add(strength(creature)); }
			}
		}
		if (scores.isEmpty()) { throw new IllegalArgumentException("No hunting reference creatures"); }
		Collections.sort(scores);
		int middle = scores.size() / 2;
		double reference = scores.size() % 2 == 0 ? (scores.get(middle - 1) + scores.get(middle)) / 2 : scores.get(middle);
		// Scale all three difficulties together if an unusually tough target
		// would exceed the one-level safety limit. Capping only its reward would
		// otherwise make that hard task pay less per unit of work than easy tasks.
		for (HuntingDifficulty difficulty : HuntingDifficulty.values()) {
			for (Creature creature : HuntingTargetSelector.select(
					level, difficulty, creatures, Creature::getLevel)) {
				// The same reference cap must cover adaptive endgame targets.
				double referenceShare = difficulty == HuntingDifficulty.EASY ? 0.10
						: difficulty == HuntingDifficulty.MEDIUM ? 0.25 : 0.50;
				reference = Math.max(reference, strength(creature) * referenceShare * 30 / 25.0);
			}
		}
		return reference;
	}

	/** Normalised to 25 opponents; no fixed minimum payout for trivial targets.
	 * A full skill-level gap per hunt is the safety limit, not several levels. */
	public static int rewardBasisPoints(HuntingDifficulty difficulty, int count,
			Creature target, double reference) {
		if (count < 1 || count > 50 || !Double.isFinite(reference) || reference <= 0) {
			throw new IllegalArgumentException("Invalid hunting task weight");
		}
		double bonus = difficulty.getPercent() * 100.0 * count / 25.0 * strength(target) / reference;
		return (int) Math.max(1, Math.min(10000, Math.round(bonus)));
	}

	public static String percentageLabel(int basisPoints) {
		return java.math.BigDecimal.valueOf(basisPoints, 2).stripTrailingZeros().toPlainString().replace('.', ',') + "%";
	}

	/** New hunts freeze their reward in field 4. Existing hunts are preserved,
	 * but their kill count still matters; old one-kill tasks cannot pay a full hunt. */
	public static int questBasisPoints(String quest) {
		String[] parts = quest.split(";", -1);
		if (parts.length > 4) {
			try {
				int saved = Integer.parseInt(parts[4]);
				if (saved >= 1 && saved <= 10000) { return saved; }
			} catch (NumberFormatException ignored) { /* Safely handle old quest data. */ }
		}
		String[] kills = parts[0].split(",", -1);
		int count = 25;
		if (kills.length == 5) {
			try { count = Math.max(1, Math.min(50, Integer.parseInt(kills[2]))); }
			catch (NumberFormatException ignored) { /* Use a standard hunt. */ }
		}
		return Math.min(10000, HuntingDifficulty.fromQuest(quest).getPercent() * 100 * count / 25);
	}

	public static ItemRarity rarityForRoll(HuntingDifficulty difficulty, int roll) {
		if (roll < 0 || roll >= 100) { throw new IllegalArgumentException("Invalid hunting rarity roll"); }
		if (difficulty == HuntingDifficulty.HARD) {
			return roll < 10 ? ItemRarity.EPIC : ItemRarity.RARE;
		}
		return difficulty == HuntingDifficulty.MEDIUM && roll < 25 ? ItemRarity.RARE : ItemRarity.COMMON;
	}

	public static Item createReward(HuntingDifficulty difficulty, int earnedLevel) {
		if (earnedLevel < 0 || earnedLevel > Level.maxLevel()) {
			throw new IllegalArgumentException("Invalid hunting reward level");
		}
		if (difficulty == HuntingDifficulty.EASY) {
			StackableItem reward = (StackableItem) SingletonRepository.getEntityManager()
					.getItem(SUPPLIES.get(Rand.rand(SUPPLIES.size())), ItemCreationContext.quest());
			reward.setQuantity("mały eliksir".equals(reward.getName()) ? 5 : 10);
			return reward;
		}
		List<String> eligible = new ArrayList<>();
		for (String name : EQUIPMENT) {
			Item item = SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.restore());
			if (item != null && item.getMinLevel() <= earnedLevel && ItemRarityService.getInstance().isEligible(item)) {
				eligible.add(name);
			}
		}
		if (eligible.isEmpty()) { return null; }
		return SingletonRepository.getEntityManager().getItem(eligible.get(Rand.rand(eligible.size())),
				ItemCreationContext.builder(ItemCreationContext.Source.QUEST)
				.withQuestRarity(rarityForRoll(difficulty, Rand.rand(100)))
				.randomizeModifiers(true).generateAffixes(true).build());
	}
}
