/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.item.upgrade;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;

/** Immutable server-owned preview of one concrete item-upgrade attempt. */
public final class ItemUpgradePreview {
	private final Item item;
	private final String requestToken;
	private final String displayName;
	private final ItemRarity rarity;
	private final int currentLevel;
	private final int nextLevel;
	private final int maximumLevel;
	private final ItemUpgradeStats currentStats;
	private final ItemUpgradeStats upgradedStats;
	private final ItemUpgradeRequirements requirements;
	private final double successProbability;
	private final double karmaModifier;
	private final boolean upgradeAllowed;
	private final ItemUpgradeResult.Status blockingStatus;

	ItemUpgradePreview(final Item item, final String requestToken,
			final String displayName, final ItemRarity rarity,
			final int currentLevel, final int nextLevel,
			final int maximumLevel, final ItemUpgradeStats currentStats,
			final ItemUpgradeStats upgradedStats,
			final ItemUpgradeRequirements requirements,
			final double successProbability, final double karmaModifier,
			final boolean upgradeAllowed,
			final ItemUpgradeResult.Status blockingStatus) {
		this.item = item;
		this.requestToken = requestToken;
		this.displayName = displayName;
		this.rarity = rarity;
		this.currentLevel = currentLevel;
		this.nextLevel = nextLevel;
		this.maximumLevel = maximumLevel;
		this.currentStats = currentStats;
		this.upgradedStats = upgradedStats;
		this.requirements = requirements;
		this.successProbability = successProbability;
		this.karmaModifier = karmaModifier;
		this.upgradeAllowed = upgradeAllowed;
		this.blockingStatus = blockingStatus;
	}

	public Item getItem() {
		return item;
	}

	public String getRequestToken() {
		return requestToken;
	}

	public String getDisplayName() {
		return displayName;
	}

	public ItemRarity getRarity() {
		return rarity;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public int getNextLevel() {
		return nextLevel;
	}

	public int getMaximumLevel() {
		return maximumLevel;
	}

	public ItemUpgradeStats getCurrentStats() {
		return currentStats;
	}

	public ItemUpgradeStats getUpgradedStats() {
		return upgradedStats;
	}

	public ItemUpgradeRequirements getRequirements() {
		return requirements;
	}

	public double getSuccessProbability() {
		return successProbability;
	}

	public int getSuccessPercent() {
		return (int) Math.round(successProbability * 100.0);
	}

	double getKarmaModifier() {
		return karmaModifier;
	}

	public boolean isUpgradeAllowed() {
		return upgradeAllowed;
	}

	public ItemUpgradeResult.Status getBlockingStatus() {
		return blockingStatus;
	}
}
