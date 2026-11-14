package games.stendhal.server.entity.npc.action;

import java.util.Objects;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TutorialNotifier;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityTransferSnapshot;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** Completes a persistent item transformation without rerolling the input's affixes. */
public final class EquipUpgradedItemAction implements ChatAction {
	private final String questSlot;
	private final String targetName;
	private final ItemRarity minimumRarity;

	public EquipUpgradedItemAction(final String questSlot, final String targetName, final ItemRarity minimumRarity) {
		this.questSlot = Objects.requireNonNull(questSlot);
		this.targetName = Objects.requireNonNull(targetName);
		this.minimumRarity = Objects.requireNonNull(minimumRarity);
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
		final String snapshot = player.getQuest(questSlot, CaptureItemUpgradeAction.SNAPSHOT_INDEX);
		final boolean hasSnapshot = snapshot != null && !snapshot.isEmpty();
		final ItemCreationContext context = hasSnapshot ? ItemCreationContext.restore()
				: ItemCreationContext.builder(ItemCreationContext.Source.QUEST)
						.withRarity(minimumRarity).randomizeModifiers(false).generateAffixes(true).build();
		final Item result = SingletonRepository.getEntityManager().getItem(targetName, context);
		if (result == null) {
			throw new IllegalStateException("Unknown upgraded item: " + targetName);
		}
		if (hasSnapshot) {
			ItemRarityTransferSnapshot.apply(result, snapshot, minimumRarity);
		}
		result.setBoundTo(player.getName());
		player.equipOrPutOnGround(result);
		TutorialNotifier.equippedByNPC(player, result);
		player.setQuest(questSlot, CaptureItemUpgradeAction.SNAPSHOT_INDEX, "");
		player.notifyWorldAboutChanges();
	}
}
