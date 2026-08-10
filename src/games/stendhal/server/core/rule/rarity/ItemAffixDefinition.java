/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.Random;

import games.stendhal.server.entity.item.Item;

/**
 * One stable random item-affix definition.
 *
 * Definitions own eligibility and materialization of the gameplay attribute.
 * Selection, uniqueness and persistence are handled by the shared affix layer.
 */
public interface ItemAffixDefinition {
	/** @return stable persistent identifier of this affix */
	String getId();

	/** @return item attribute materialized for combat/presentation */
	String getAttribute();

	/** @return whether this affix can currently be rolled for the item */
	boolean isEligible(Item item);

	/**
	 * Materializes one freshly rolled value on the item.
	 *
	 * @return {@code true} when the affix was applied
	 */
	boolean apply(Item item, Random random);
}
