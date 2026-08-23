/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
import games.stendhal.server.entity.creature.impl.DropItem;
import games.stendhal.server.entity.item.Item;

/** Selects safe equipment rewards from the normal creature drop ecosystem. */
public final class ChallengeArenaLootService {
	private static final int SOURCE_LEVEL_BELOW_PLAYER = 60;
	private static final int SOURCE_LEVEL_ABOVE_TARGET = 20;

	private ChallengeArenaLootService() {
	}

	/**
	 * Creates one equipment reward using normal drop rarity semantics with the
	 * extra number of rarity rolls configured by the paid tier.
	 */
	public static Item createEquipmentReward(final int playerLevel,
			final ChallengeArenaTier tier) {
		if (tier == null || !tier.awardsEquipmentChest()) {
			return null;
		}

		List<String> candidates = collectCandidates(playerLevel, tier, true);
		if (candidates.isEmpty()) {
			candidates = collectCandidates(playerLevel, tier, false);
		}
		if (candidates.isEmpty()) {
			return null;
		}

		Collections.shuffle(candidates);
		final String itemName = candidates.get(0);
		final ItemCreationContext context = ItemCreationContext
				.builder(ItemCreationContext.Source.DROP)
				.withRarityRolls(tier.getRewardRarityRolls())
				.build();
		return SingletonRepository.getEntityManager().getItem(itemName, context);
	}

	static List<String> collectCandidates(final int playerLevel,
			final ChallengeArenaTier tier, final boolean useLevelWindow) {
		final EntityManager entityManager = SingletonRepository.getEntityManager();
		final Set<String> names = new LinkedHashSet<String>();
		final int minimumSourceLevel = Math.max(1,
				playerLevel - SOURCE_LEVEL_BELOW_PLAYER);
		final int maximumSourceLevel = Math.max(1, playerLevel
				+ tier.getMaximumLevelOffset() + SOURCE_LEVEL_ABOVE_TARGET);

		for (final DefaultCreature creature : entityManager.getDefaultCreatures()) {
			if (creature == null || creature.getAiProfiles().containsKey("boss")) {
				continue;
			}
			if (useLevelWindow && (creature.getLevel() < minimumSourceLevel
					|| creature.getLevel() > maximumSourceLevel)) {
				continue;
			}
			for (final DropItem drop : creature.getDropItems()) {
				if (drop == null || drop.name == null || names.contains(drop.name)) {
					continue;
				}
				if (isSafeEquipmentReward(entityManager, drop.name)) {
					names.add(drop.name);
				}
			}
		}
		return new ArrayList<String>(names);
	}

	private static boolean isSafeEquipmentReward(final EntityManager entityManager,
			final String itemName) {
		if (!entityManager.isItem(itemName) || "zdobyczny hełm".equals(itemName)) {
			return false;
		}
		final Item item = entityManager.getItem(itemName,
				ItemCreationContext.restore());
		return item != null
				&& item.getDefinitionValue() > 0
				&& !item.has("bound")
				&& !item.has("autobind")
				&& !item.isPersistent()
				&& ItemRarityService.getInstance().isEligible(item);
	}
}
