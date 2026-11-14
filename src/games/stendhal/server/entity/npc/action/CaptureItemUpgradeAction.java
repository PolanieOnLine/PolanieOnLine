package games.stendhal.server.entity.npc.action;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemRarityTransferSnapshot;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** Saves the actual submitted item's roll before a timed crafting order consumes it. */
public final class CaptureItemUpgradeAction implements ChatAction {
	// Crafting uses 0 for state, 1 for timestamp and 2 for completion count.
	public static final int SNAPSHOT_INDEX = 3;
	private final String questSlot;
	private final String sourceName;

	public CaptureItemUpgradeAction(final String questSlot, final String sourceName) {
		this.questSlot = Objects.requireNonNull(questSlot);
		this.sourceName = Objects.requireNonNull(sourceName);
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
		final List<Item> candidates = player.getAllSubmittableEquipped(sourceName);
		if (candidates.isEmpty()) {
			throw new IllegalStateException("Missing source item for upgrade: " + sourceName);
		}
		// This uses the same ordering as DropItemAction's dropSubmittable call.
		final Item snapshot = new Item(candidates.get(0));
		if (!ItemAffixState.hasSeed(snapshot)) {
			ItemAffixState.setSeed(snapshot, ThreadLocalRandom.current().nextLong());
		}
		player.setQuest(questSlot, SNAPSHOT_INDEX, ItemRarityTransferSnapshot.encode(snapshot));
	}
}
