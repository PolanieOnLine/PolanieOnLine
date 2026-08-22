/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.action;

import java.util.Locale;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TutorialNotifier;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemCreationContext.Source;
import games.stendhal.server.core.rule.rarity.ItemRarityModifiers;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/**
 * Equips the specified item.
 */
@Dev(category = Category.ITEMS_OWNED, label="Item+")
public class EquipItemAction implements ChatAction {
	private static Logger logger = Logger.getLogger(EquipItemAction.class);

	private final String itemName;
	private final int amount;
	private final boolean bind;
	private final ItemCreationContext creationContext;

	/**
	 * Creates a new EquipItemAction.
	 *
	 * @param itemName
	 *            name of item
	 */
	public EquipItemAction(final String itemName) {
		this(itemName, 1, false);
	}

	/**
	 * Creates a new EquipItemAction.
	 *
	 * @param itemName
	 *            name of item
	 * @param amount
	 *            for StackableItems
	 */
	public EquipItemAction(final String itemName, final int amount) {
		this(itemName, amount, false);
	}

	/**
	 * Creates a new EquipItemAction.
	 *
	 * @param itemName
	 *            name of item
	 * @param amount
	 *            for StackableItems
	 * @param bind
	 *            bind to player
	 */
	@Dev
	public EquipItemAction(final String itemName, final int amount, final boolean bind) {
		this(itemName, amount, bind, ItemCreationContext.quest());
	}

	/**
	 * Creates a deterministic quest reward with a forced rarity.
	 *
	 * @param itemName name of item
	 * @param amount amount for stackable items
	 * @param bind bind to player
	 * @param rarity forced rarity
	 */
	public EquipItemAction(final String itemName, final int amount, final boolean bind,
			final ItemRarity rarity) {
		this(itemName, amount, bind, questContext(rarity, null, false));
	}

	/**
	 * Creates a deterministic quest reward with fixed modifiers.
	 *
	 * @param itemName name of item
	 * @param amount amount for stackable items
	 * @param bind bind to player
	 * @param rarity forced rarity
	 * @param modifiers fixed modifiers
	 */
	public EquipItemAction(final String itemName, final int amount, final boolean bind,
			final ItemRarity rarity, final ItemRarityModifiers modifiers) {
		this(itemName, amount, bind, questContext(rarity, modifiers, false));
	}

	/**
	 * Creates a quest reward with optional explicitly requested modifier
	 * randomization.
	 *
	 * @param itemName name of item
	 * @param amount amount for stackable items
	 * @param bind bind to player
	 * @param rarity forced rarity
	 * @param randomizeModifiers whether modifiers should be randomized
	 */
	public EquipItemAction(final String itemName, final int amount, final boolean bind,
			final ItemRarity rarity, final boolean randomizeModifiers) {
		this(itemName, amount, bind, questContext(rarity, null, randomizeModifiers));
	}

	/**
	 * String based constructor intended for scripted quests.
	 */
	public EquipItemAction(final String itemName, final int amount, final boolean bind,
			final String rarityId) {
		this(itemName, amount, bind, requireRarity(rarityId));
	}

	/**
	 * String based constructor intended for repeatable scripted quests.
	 */
	public EquipItemAction(final String itemName, final int amount, final boolean bind,
			final String rarityId, final boolean randomizeModifiers) {
		this(itemName, amount, bind, requireRarity(rarityId), randomizeModifiers);
	}

	/**
	 * String based constructor for scripted quests with fixed modifiers.
	 * Modifier entries are separated by commas or semicolons, for example
	 * {@code attack-multiplier=1.30;value-multiplier=2.00}.
	 */
	public EquipItemAction(final String itemName, final int amount, final boolean bind,
			final String rarityId, final String modifierSpec) {
		this(itemName, amount, bind, requireRarity(rarityId), parseModifiers(modifierSpec));
	}

	/**
	 * Creates an action using a fully specified creation context.
	 */
	public EquipItemAction(final String itemName, final int amount, final boolean bind,
			final ItemCreationContext creationContext) {
		this.itemName = itemName;
		this.amount = amount;
		this.bind = bind;
		if (creationContext == null) {
			throw new IllegalArgumentException("creationContext must not be null");
		}
		this.creationContext = creationContext;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
		final Item item = SingletonRepository.getEntityManager().getItem(itemName, creationContext);
		if (item != null) {
			if (item instanceof StackableItem) {
				final StackableItem stackableItem = (StackableItem) item;
				stackableItem.setQuantity(amount);
			}
			if (bind) {
				item.setBoundTo(player.getName());
			}
			player.equipOrPutOnGround(item);
			TutorialNotifier.equippedByNPC(player, item);
			player.notifyWorldAboutChanges();
		} else {
			logger.error("Cannot find item '" + itemName + "' to equip", new Throwable());
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("equip item <");
		sb.append(amount);
		sb.append(" ");
		sb.append(itemName);
		if (bind) {
			sb.append(" (bind)");
		}
		sb.append(" ");
		sb.append(creationContext);
		sb.append(">");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + amount;
		if (itemName == null) {
			result = PRIME * result;
		} else {
			result = PRIME * result + itemName.hashCode();
		}
		if (bind) {
			result = PRIME * result;
		}
		result = PRIME * result + creationContext.hashCode();

		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final EquipItemAction other = (EquipItemAction) obj;
		if (amount != other.amount) {
			return false;
		}
		if (itemName == null) {
			if (other.itemName != null) {
				return false;
			}
		} else if (!itemName.equals(other.itemName)) {
			return false;
		}
		return bind == other.bind && creationContext.equals(other.creationContext);
	}

	public static ChatAction equipItem(String itemName) {
		return new EquipItemAction(itemName);
	}

	public static ChatAction equipBoundItem(String itemName) {
		return new EquipItemAction(itemName, 1, true);
	}

	private static ItemCreationContext questContext(final ItemRarity rarity,
			final ItemRarityModifiers modifiers, final boolean randomizeModifiers) {
		if (rarity == null) {
			throw new IllegalArgumentException("rarity must not be null");
		}

		final ItemCreationContext.Builder builder = ItemCreationContext.builder(Source.QUEST)
				.withRarity(rarity)
				.randomizeModifiers(randomizeModifiers);
		if (modifiers != null) {
			builder.withModifiers(modifiers);
		}
		return builder.build();
	}

	private static ItemRarity requireRarity(final String rarityId) {
		final ItemRarity rarity = ItemRarity.fromId(rarityId);
		if (rarity == null) {
			throw new IllegalArgumentException("Unknown item rarity: " + rarityId);
		}
		return rarity;
	}

	private static ItemRarityModifiers parseModifiers(final String modifierSpec) {
		if (modifierSpec == null || modifierSpec.trim().isEmpty()) {
			throw new IllegalArgumentException("modifierSpec must not be empty");
		}

		final ItemRarityModifiers.Builder builder = ItemRarityModifiers.builder();
		for (final String entry : modifierSpec.split("[;,]")) {
			final int separator = entry.indexOf('=');
			if (separator <= 0 || separator == entry.length() - 1) {
				throw new IllegalArgumentException("Invalid item modifier: " + entry);
			}

			final String key = entry.substring(0, separator).trim().toLowerCase(Locale.ENGLISH);
			final double value;
			try {
				value = Double.parseDouble(entry.substring(separator + 1).trim());
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("Invalid item modifier value: " + entry, e);
			}
			if (!Double.isFinite(value) || value <= 0.0) {
				throw new IllegalArgumentException("Item modifier must be finite and positive: " + entry);
			}

			if ("attack-multiplier".equals(key)) {
				builder.attackMultiplier(value);
			} else if ("defense-multiplier".equals(key)) {
				builder.defenseMultiplier(value);
			} else if ("speed-multiplier".equals(key)) {
				builder.speedMultiplier(value);
			} else if ("range-multiplier".equals(key)) {
				builder.rangeMultiplier(value);
			} else if ("value-multiplier".equals(key)) {
				builder.valueMultiplier(value);
			} else if ("stat-multiplier".equals(key)) {
				builder.statMultiplier(value);
			} else {
				throw new IllegalArgumentException("Unknown item modifier: " + key);
			}
		}
		return builder.build();
	}
}
