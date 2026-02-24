package games.stendhal.server.entity.item;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

/**
 * a stackable box which can be unwrapped.
 */
public abstract class StackableBox extends StackableItem {

	private static final Logger logger = Logger.getLogger(StackableBox.class);
	private static final List<String> STACK_METADATA_ATTRIBUTES = Arrays.asList("itemdata", "bound",
			"persistent", "undroppableondeath", "amount", "frequency", "regen", "atk", "range", "state",
			"description", "dest");

	public StackableBox(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	public StackableBox(final StackableBox item) {
		super(item);
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (this.isContained()) {
			RPObject base = this.getContainer();

			while (base.isContained()) {
				base = base.getContainer();
			}

			if (!user.nextTo((Entity) base)) {
				logger.debug("Consumable item is too far");
				user.sendPrivateText("Przedmiot jest zbyt daleko.");
				return false;
			}
		} else {
			if (!user.nextTo(this)) {
				logger.debug("Consumable item is too far");
				user.sendPrivateText("Przedmiot jest zbyt daleko.");
				return false;
			}
		}

		if (user instanceof Player) {
			return useMe((Player) user);
		}

		logger.error("user is not a instance of Player but: " + user, new Throwable());
		return false;
	}

	protected void addRewardToInventory(final Player player, final Item reward) {
		if (!(reward instanceof StackableItem)) {
			player.equipOrPutOnGround(reward);
			return;
		}

		final StackableItem rewardStack = (StackableItem) reward;
		int remaining = rewardStack.getQuantity();

		for (final Item existingItem : player.getAllEquipped(reward.getName())) {
			if (!(existingItem instanceof StackableItem)) {
				continue;
			}
			final StackableItem existingStack = (StackableItem) existingItem;
			if (!existingStack.isStackable(rewardStack)) {
				continue;
			}

			final int free = existingStack.getCapacity() - existingStack.getQuantity();
			if (free <= 0) {
				continue;
			}

			final int moved = Math.min(free, remaining);
			existingStack.setQuantity(existingStack.getQuantity() + moved);
			remaining -= moved;
			if (remaining == 0) {
				return;
			}
		}

		while (remaining > 0) {
			final int chunkSize = Math.min(remaining, rewardStack.getCapacity());
			final StackableItem chunk = createRewardChunk(rewardStack, chunkSize);
			player.equipOrPutOnGround(chunk);
			remaining -= chunkSize;
		}
	}

	private StackableItem createRewardChunk(final StackableItem source, final int quantity) {
		final StackableItem chunk = (StackableItem) SingletonRepository.getEntityManager().getItem(source.getName());
		chunk.setQuantity(quantity);
		for (final String attribute : STACK_METADATA_ATTRIBUTES) {
			if (source.has(attribute)) {
				chunk.put(attribute, source.get(attribute));
			}
		}
		return chunk;
	}

	protected abstract boolean useMe(final Player player);
}
