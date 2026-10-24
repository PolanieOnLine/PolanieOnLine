package games.stendhal.server.core.rule.item;

import games.stendhal.server.core.rule.rarity.ItemRarityModifiers;
import games.stendhal.server.core.rule.rarity.ItemRarityProfile;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;

/** Refreshes prices after XML value changes without rerolling saved equipment. */
public final class ItemValueService {
	private ItemValueService() { }

	public static void refresh(final Item item) {
		if (item == null) return;
		final double multiplier = getValueMultiplier(item);
		item.setValue((int) Math.min(Integer.MAX_VALUE,
				Math.max(0L, Math.round(item.getDefinitionValue() * multiplier))));
	}

	/** Shared pricing policy for catalogue shops, the arena buyer and login. */
	public static double getValueMultiplier(final Item item) {
		if (item == null) return 1.0;
		final Double saved = item.getRarityModifier(ItemRarityModifiers.VALUE);
		final ItemRarity rarity = item.getRarity();
		String profile = item.has(Item.RARITY_PROFILE)
				? item.get(Item.RARITY_PROFILE) : item.getRarityProfile();
		if (profile == null || profile.trim().isEmpty()) profile = ItemRarityProfile.DEFAULT_ID;
		final boolean builtInProfile = ItemRarityProfile.DEFAULT_ID.equals(profile)
				|| ItemRarityProfile.BOSS_ID.equals(profile);
		final boolean oldRarePrice = rarity == ItemRarity.RARE && builtInProfile
				&& saved != null && Double.compare(saved.doubleValue(), 1.20) == 0;
		if (!oldRarePrice && saved != null && Double.isFinite(saved.doubleValue())
				&& saved.doubleValue() >= 0) {
			return saved.doubleValue();
		}
		// Only the obsolete standard Rare price is migrated. Other explicit/custom
		// multipliers and every combat/affix/upgrade field remain untouched.
		final double multiplier = rarity == null ? 1.0 : ItemRarityService.getInstance()
				.getProfile(profile).getTier(rarity).getValueMultiplier();
		if (rarity != null) item.setRarityModifier(ItemRarityModifiers.VALUE, multiplier);
		return multiplier;
	}
}
