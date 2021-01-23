/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.useable.sources;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.mapstuff.useable.SourceEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ImageEffectEvent;
import games.stendhal.server.events.SoundEvent;

/**
 * @author KarajuSs
 */
public class SourceGold extends SourceEntity {
	private static final Logger logger = Logger.getLogger(SourceGold.class);

	private final static String sourceClass = "source_gold";

	private final String startSound = "pickaxe_01";
	private final String successSound = "rocks-1";
	private final int SOUND_RADIUS = 20;

	/**
	 * The name of the item to be found.
	 */
	private final String itemName;

	/**
	 * Create a gold source.
	 */
	public SourceGold() {
		this("bryłka złota");
	}

	/**
	 * source name.
	 */
	@Override
	public String getName() {
		return("surowców");
	}

	/**
	 * Create a gold source.
	 *
	 * @param itemName
	 *            The name of the item to be prospected.
	 */
	public SourceGold(final String itemName) {
		this.itemName = itemName;

		setRPClass("useable_entity");
		put("type", "useable_entity");
		put("class", "source");
		put("name", sourceClass);
		setMenu("Wydobądź|Użyj");
		setDescription("Wszystko wskazuje na to, że tutaj coś jest.");
	}

	/**
	 * Called when the activity has finished.
	 *
	 * @param player
	 *            The player that did the activity.
	 * @param successful
	 *            If the activity was successful.
	 */
	@Override
	protected void onFinished(final Player player, final boolean successful) {
		final String skill = player.getSkill("mining");

		if (successful) {
			addEvent(new SoundEvent(successSound, SOUND_RADIUS, 100, SoundLayer.AMBIENT_SOUND));
	        notifyWorldAboutChanges();

			final Item item = SingletonRepository.getEntityManager().getItem(itemName);
			if (item != null) {
				for (final String pickName : SourceEntity.NEEDED_PICKS) {
					if (pickName == "kilof") {
						if (player.isEquipped(pickName)) {
							int amount = Rand.throwCoin();
							((StackableItem) item).setQuantity(amount);
						}
					}
				}

				player.equipOrPutOnGround(item);
				player.incMinedForItem(item.getName(), item.getQuantity());
				if (skill != null) {
					player.incMiningXP(500);
				}

				player.sendPrivateText("Wydobyłeś " + Grammar.a_noun(item.getTitle()) + ".");
			} else {
				logger.error("could not find item: " + itemName);
			}
		} else {
			if (skill != null) {
				player.incMiningXP(50);
			}
			player.sendPrivateText("Nic nie wydobyłeś.");
		}
	}

	/**
	 * Called when the activity has started.
	 *
	 * @param player
	 *            The player starting the activity.
	 */
	@Override
	protected void onStarted(final Player player) {
		addEvent(new SoundEvent(startSound, SOUND_RADIUS, 100, SoundLayer.AMBIENT_SOUND));
		player.sendPrivateText("Rozpocząłeś wydobywanie złota.");
		notifyWorldAboutChanges();
		addEvent(new ImageEffectEvent("mining", true));
		notifyWorldAboutChanges();
	}
}
