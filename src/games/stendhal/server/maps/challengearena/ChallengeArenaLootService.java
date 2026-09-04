/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
import games.stendhal.server.entity.item.Item;

/** Selects one safe equipment reward from curated Challenge Arena pools. */
public final class ChallengeArenaLootService {
	private static final int MAX_ITEM_LEVEL_ABOVE_PLAYER = 50;
	private static final String EXCLUDED_REWARD_NAME = "zdobyczny hełm";

	private static final RewardGroup[] HUNTER_GROUPS = {
		group(45,
				"zbroja chaosu", "hełm chaosu", "płaszcz chaosu",
				"spodnie chaosu", "buty chaosu", "tarcza chaosu"),
		group(25,
				"lodowa zbroja", "ognista zbroja", "lodowe spodnie",
				"ogniste spodnie", "lodowe buty", "ogniste buty",
				"lodowa tarcza", "ognista tarcza"),
		group(20,
				"zbroja mainiocyjska", "hełm mainiocyjski",
				"płaszcz mainiocyjski", "spodnie mainiocyjskie",
				"buty mainiocyjskie", "tarcza mainiocyjska",
				"rękawice mainiocyjskie"),
		group(10,
				"miecz chaosu", "miecz elfów ciemności",
				"miecz ognisty", "miecz lodowy")
	};

	private static final RewardGroup[] VETERAN_GROUPS = {
		group(35,
				"zbroja xenocyjska", "hełm xenocyjski", "płaszcz xenocyjski",
				"spodnie xenocyjskie", "buty xenocyjskie", "tarcza xenocyjska",
				"rękawice xenocyjskie"),
		group(25,
				"zbroja wampirza", "spodnie wampirze", "płaszcz wampirzy",
				"rękawice wampirze", "buty wampirze", "pas wampirzy",
				"hełm wampirzy"),
		group(20,
				"zbroja monarchistyczna", "hełm monarchistyczny",
				"płaszcz monarchistyczny", "spodnie monarchistyczne",
				"buty monarchistyczne", "tarcza monarchistyczna"),
		group(20,
				"półtorak", "czarny miecz", "złota klinga orków",
				"topór chaosu")
	};

	private static final RewardGroup[] CHAMPION_GROUPS = {
		group(35,
				"czarna zbroja", "czarny hełm", "czarny płaszcz",
				"czarne spodnie", "czarne buty", "czarna tarcza",
				"czarne rękawice", "czarny pas"),
		group(25,
				"magiczna zbroja płytowa", "magiczny hełm kolczy",
				"magiczny płaszcz", "magiczne spodnie płytowe",
				"magiczne buty płytowe", "magiczna tarcza płytowa"),
		group(15,
				"zbroja wampirza", "spodnie wampirze", "płaszcz wampirzy",
				"rękawice wampirze", "buty wampirze", "pas wampirzy",
				"hełm wampirzy"),
		group(10,
				"czarny płaszcz smoczy", "lazurowy płaszcz smoczy",
				"kościany płaszcz smoczy", "szmaragdowy płaszcz smoczy",
				"karmazynowy płaszcz smoczy"),
		group(15,
				"ognisty miecz demonów", "magiczny topór obosieczny",
				"topór Durina", "różdżka Wołosa")
	};

	private static final RewardGroup[] LEGEND_GROUPS = {
		group(25,
				"czarna zbroja", "czarny hełm", "czarny płaszcz",
				"czarne spodnie", "czarne buty", "czarna tarcza",
				"czarne rękawice", "czarny pas"),
		group(20,
				"magiczna zbroja płytowa", "magiczny hełm kolczy",
				"magiczny płaszcz", "magiczne spodnie płytowe",
				"magiczne buty płytowe", "magiczna tarcza płytowa"),
		group(15,
				"zbroja wampirza", "spodnie wampirze", "płaszcz wampirzy",
				"rękawice wampirze", "buty wampirze", "pas wampirzy",
				"hełm wampirzy"),
		group(16,
				"czarny płaszcz smoczy", "lazurowy płaszcz smoczy",
				"kościany płaszcz smoczy", "szmaragdowy płaszcz smoczy",
				"karmazynowy płaszcz smoczy"),
		group(20,
				"ognisty miecz demonów", "magiczny topór obosieczny",
				"topór Durina", "różdżka Wołosa", "czarny miecz"),
		group(4,
				"zbroja z mithrilu", "hełm z mithrilu", "spodnie z mithrilu",
				"rękawice z mithrilu", "pas z mithrilu", "buty z mithrilu",
				"tarcza z mithrilu", "płaszcz z mithrilu")
	};

	private ChallengeArenaLootService() {
	}

	/**
	 * Creates one curated equipment reward using the normal rarity semantics
	 * and the number of rarity rolls configured by the paid tier.
	 */
	public static Item createEquipmentReward(final int playerLevel,
			final ChallengeArenaTier tier) {
		if (tier == null || !tier.awardsEquipmentChest()) {
			return null;
		}

		final String itemName = selectRewardName(playerLevel, tier,
				Rand.rand(), Rand.rand());
		if (itemName == null) {
			return null;
		}

		final ItemCreationContext context = ItemCreationContext
				.builder(ItemCreationContext.Source.DROP)
				.withRarityRolls(tier.getRewardRarityRolls())
				.build();
		return SingletonRepository.getEntityManager().getItem(itemName, context);
	}

	/**
	 * Deterministic selection entry point used by regression tests. Both rolls
	 * are expected in the half-open range [0, 1).
	 */
	static String selectRewardName(final int playerLevel,
			final ChallengeArenaTier tier, final double groupRoll,
			final double itemRoll) {
		if (tier == null || !tier.awardsEquipmentChest()) {
			return null;
		}
		if (Double.isNaN(groupRoll) || groupRoll < 0.0 || groupRoll >= 1.0
				|| Double.isNaN(itemRoll) || itemRoll < 0.0 || itemRoll >= 1.0) {
			throw new IllegalArgumentException("Reward rolls must be in [0, 1)");
		}

		final EntityManager entityManager = SingletonRepository.getEntityManager();
		final List<EligibleRewardGroup> eligibleGroups =
				new ArrayList<EligibleRewardGroup>();
		int totalWeight = 0;
		for (final RewardGroup group : groupsFor(tier)) {
			final List<String> eligibleItems = eligibleItems(entityManager,
					group, playerLevel);
			if (!eligibleItems.isEmpty()) {
				eligibleGroups.add(new EligibleRewardGroup(group.weight,
						eligibleItems));
				totalWeight += group.weight;
			}
		}
		if (eligibleGroups.isEmpty() || totalWeight <= 0) {
			return null;
		}

		int groupPick = Math.min(totalWeight - 1,
				(int) Math.floor(groupRoll * totalWeight));
		for (final EligibleRewardGroup group : eligibleGroups) {
			if (groupPick < group.weight) {
				final int itemIndex = Math.min(group.items.size() - 1,
						(int) Math.floor(itemRoll * group.items.size()));
				return group.items.get(itemIndex);
			}
			groupPick -= group.weight;
		}
		return eligibleGroups.get(eligibleGroups.size() - 1).items.get(0);
	}

	static List<String> getConfiguredRewardNames(final ChallengeArenaTier tier) {
		if (tier == null) {
			return Collections.emptyList();
		}
		final List<String> result = new ArrayList<String>();
		for (final RewardGroup group : groupsFor(tier)) {
			result.addAll(group.items);
		}
		return Collections.unmodifiableList(result);
	}

	static int getConfiguredTotalWeight(final ChallengeArenaTier tier) {
		int result = 0;
		for (final RewardGroup group : groupsFor(tier)) {
			result += group.weight;
		}
		return result;
	}

	static boolean isSafeEquipmentReward(final EntityManager entityManager,
			final String itemName, final int playerLevel) {
		if (entityManager == null || itemName == null
				|| !entityManager.isItem(itemName)
				|| EXCLUDED_REWARD_NAME.equals(itemName)) {
			return false;
		}
		final Item item = entityManager.getItem(itemName,
				ItemCreationContext.restore());
		if (item == null || item.has("bound") || item.has("autobind")
				|| item.isPersistent()
				|| !ItemRarityService.getInstance().isEligible(item)) {
			return false;
		}
		final int minimumLevel = item.has("min_level")
				? item.getInt("min_level") : 0;
		return minimumLevel <= Math.max(1, playerLevel) + MAX_ITEM_LEVEL_ABOVE_PLAYER;
	}

	private static List<String> eligibleItems(final EntityManager entityManager,
			final RewardGroup group, final int playerLevel) {
		final List<String> result = new ArrayList<String>();
		for (final String itemName : group.items) {
			if (isSafeEquipmentReward(entityManager, itemName, playerLevel)) {
				result.add(itemName);
			}
		}
		return result;
	}

	private static RewardGroup[] groupsFor(final ChallengeArenaTier tier) {
		switch (tier) {
			case HUNTER:
				return HUNTER_GROUPS;
			case VETERAN:
				return VETERAN_GROUPS;
			case CHAMPION:
				return CHAMPION_GROUPS;
			case LEGEND:
				return LEGEND_GROUPS;
			default:
				return new RewardGroup[0];
		}
	}

	private static RewardGroup group(final int weight,
			final String... itemNames) {
		return new RewardGroup(weight, Arrays.asList(itemNames));
	}

	private static final class RewardGroup {
		private final int weight;
		private final List<String> items;

		private RewardGroup(final int weight, final List<String> items) {
			this.weight = weight;
			this.items = Collections.unmodifiableList(
					new ArrayList<String>(items));
		}
	}

	private static final class EligibleRewardGroup {
		private final int weight;
		private final List<String> items;

		private EligibleRewardGroup(final int weight,
				final List<String> items) {
			this.weight = weight;
			this.items = items;
		}
	}
}
