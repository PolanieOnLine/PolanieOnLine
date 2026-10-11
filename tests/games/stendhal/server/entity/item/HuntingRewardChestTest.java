package games.stendhal.server.entity.item;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.Level;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.hunting.HuntingDifficulty;
import games.stendhal.server.maps.quests.hunting.HuntingRewards;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class HuntingRewardChestTest {
	private Player player;
	@BeforeClass public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }
	@Before public void setup() {
		player = PlayerTestHelper.createPlayer("hunter");
		new StendhalRPZone("chest_test", 64, 64).add(player);
	}
	private HuntingRewardChest chest(HuntingDifficulty difficulty, int level) {
		HuntingRewardChest chest = (HuntingRewardChest) SingletonRepository.getEntityManager().getItem(HuntingRewardChest.NAME);
		chest.setBoundTo(player.getName());
		chest.setItemData(HuntingRewardChest.createRewardData(difficulty, level));
		player.equipOrPutOnGround(chest);
		return chest;
	}
	@Test public void skillBonusUsesFullOwnSkillGapNotRemainingOrCumulativeXP() {
		for (HuntingDifficulty difficulty : HuntingDifficulty.values()) {
			int start = Level.getXP(40), end = Level.getXP(41);
			int expected = (end - start) * difficulty.getPercent() / 100;
			assertEquals(expected, HuntingRewards.skillXPBonus(start, difficulty));
			assertEquals(expected, HuntingRewards.skillXPBonus(end - 1, difficulty));
		}
	}
	@Test public void skillLimitIsSafeAndDoesNotOverflow() {
		int max = Level.getXP(Level.maxLevel());
		assertEquals(0, HuntingRewards.skillXPBonus(max, HuntingDifficulty.HARD));
		assertEquals(0, HuntingRewards.skillXPBonus(Integer.MAX_VALUE, HuntingDifficulty.HARD));
		assertEquals(1, HuntingRewards.skillXPBonus(max - 1, HuntingDifficulty.HARD));
	}
	@Test public void rarityPercentagesAreExactAndNeverLegendary() {
		int mediumRare = 0, hardRare = 0, hardEpic = 0;
		for (int roll = 0; roll < 100; roll++) {
			if (HuntingRewards.rarityForRoll(HuntingDifficulty.MEDIUM, roll) == ItemRarity.RARE) { mediumRare++; }
			if (HuntingRewards.rarityForRoll(HuntingDifficulty.HARD, roll) == ItemRarity.RARE) { hardRare++; }
			if (HuntingRewards.rarityForRoll(HuntingDifficulty.HARD, roll) == ItemRarity.EPIC) { hardEpic++; }
		}
		assertEquals(25, mediumRare); assertEquals(90, hardRare); assertEquals(10, hardEpic);
	}
	@Test public void configuredPoolExistsAndDoesNotContainTopArenaRewards() {
		for (String name : HuntingRewards.getEquipmentNames()) {
			Item item = SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.restore());
			assertNotNull(name, item);
			assertTrue(name, ItemRarityService.getInstance().isEligible(item));
			assertFalse(name, name.contains("mithril") || name.startsWith("czarn")
					|| name.startsWith("magicz") || name.contains("wampir") || name.contains("demon"));
		}
		for (String name : HuntingRewards.getSupplyNames()) {
			assertTrue(name, SingletonRepository.getEntityManager().getItem(name) instanceof StackableItem);
		}
	}
	@Test public void hardRewardAlwaysAtLeastRareAndUsableAtEarnedLevel() {
		for (int level : new int[] {0, 20, 100, Level.maxLevel()}) {
			for (int i = 0; i < 30; i++) {
				Item reward = HuntingRewards.createReward(HuntingDifficulty.HARD, level);
				assertNotNull(reward);
				assertTrue(reward.getMinLevel() <= level);
				assertTrue(reward.getRarityOrCommon() == ItemRarity.RARE || reward.getRarityOrCommon() == ItemRarity.EPIC);
			}
		}
	}
	@Test public void wrongOwnerOrAnonymousChestIsNotConsumed() {
		HuntingRewardChest chest = chest(HuntingDifficulty.HARD, 0);
		chest.setBoundTo("someone_else");
		assertFalse(chest.useMe(player)); assertEquals(1, chest.getQuantity());
		chest.remove("bound");
		assertFalse(chest.useMe(player)); assertEquals(1, chest.getQuantity());
	}
	@Test public void invalidSavedDataIsNotConsumed() {
		HuntingRewardChest chest = chest(HuntingDifficulty.HARD, 0);
		for (String invalid : new String[] {"", "HARD;-1", "HARD;999999", "HARD;0;", "BOGUS;2", "HARD;abc"}) {
			chest.setItemData(invalid);
			assertFalse(chest.useMe(player)); assertEquals(1, chest.getQuantity());
		}
	}
	@Test public void storedDifficultyLevelAndOwnerPreventIncompatibleStacking() {
		HuntingRewardChest first = chest(HuntingDifficulty.HARD, 20);
		HuntingRewardChest other = new HuntingRewardChest(first);
		assertTrue(first.isStackable(other));
		other.setItemData("HARD;21"); assertFalse(first.isStackable(other));
		other.setItemData("MEDIUM;20"); assertFalse(first.isStackable(other));
		other.setItemData("HARD;20"); other.setBoundTo("another"); assertFalse(first.isStackable(other));
	}
	@Test public void openingConsumesOnlyOneAndKeepsEarnedLevelAfterPlayerLevelsUp() {
		HuntingRewardChest chest = chest(HuntingDifficulty.HARD, 0);
		chest.setQuantity(2);
		player.setLevel(100);
		assertTrue(chest.useMe(player));
		assertEquals(1, chest.getQuantity());
		assertEquals("HARD;0", chest.getItemData());
	}
	@Test public void easyChestGivesSupplies() {
		Item reward = HuntingRewards.createReward(HuntingDifficulty.EASY, 40);
		assertTrue(reward instanceof StackableItem);
		assertTrue(HuntingRewards.getSupplyNames().contains(reward.getName()));
		assertTrue(reward.getQuantity() >= 5);
	}
	@Test public void fullInventoryDoesNotLoseChestReward() {
		HuntingRewardChest chest = chest(HuntingDifficulty.HARD, 0);
		chest.setQuantity(2); // Opening leaves the occupied chest slot in place.
		for (String name : HuntingRewards.getEquipmentNames()) {
			while (player.equipToInventoryOnly(SingletonRepository.getEntityManager().getItem(name))) { }
		}
		int before = player.getZone().getItemsOnGround().size();
		assertTrue(chest.useMe(player));
		assertEquals(1, chest.getQuantity());
		assertEquals(before + 1, player.getZone().getItemsOnGround().size());
	}
	@Test public void emptyChestCannotBeUsedAgain() {
		HuntingRewardChest chest = chest(HuntingDifficulty.EASY, 0);
		assertTrue(chest.useMe(player));
		assertEquals(0, chest.getQuantity());
		assertFalse(chest.useMe(player));
	}
}
