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
		int skillLevel = Level.getLevel(xp);
		if (skillLevel >= Level.maxLevel()) { return 0; }
		long gap = (long) Level.getXP(skillLevel + 1) - Level.getXP(skillLevel);
		long bonus = gap * difficulty.getPercent() / 100;
		return (int) Math.min(bonus, (long) Level.getXP(Level.maxLevel()) - xp);
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
