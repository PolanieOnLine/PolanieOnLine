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
import marauroa.common.game.RPClass;

/**
 * A mithril source is a spot where a player can prospect for mithril. He
 * needs a kilof and lina, time, and luck.
 *
 * Prospecting takes 7-11 seconds; during this time, the player keep standing
 * next to the mithril source. In fact, the player only has to be there when the
 * prospecting action has finished. Therefore, make sure that two mithril sources
 * are always at least 5 sec of walking away from each other, so that the player
 * can't prospect for mithril at several sites simultaneously.
 *
 * @author daniel
 * @changes artur, KarajuSs
 */
public class SourceMithril extends SourceEntity {
	private static final Logger logger = Logger.getLogger(SourceMithril.class);
	
	private final static String sourceClass = "source_mithril";

	private final String startSound = "pickaxe_01";
	private final String successSound = "rocks-1";
	private final int SOUND_RADIUS = 20;

	/**
	 * The name of the item to be found.
	 */
	private final String itemName;

	/**
	 * Create a mithril source.
	 */
	public SourceMithril() {
		this("bryłka mithrilu");
	}

	/**
	 * source name.
	 */
	@Override
	public String getName() {
		return("surowców");
	}

	/**
	 * Create a mithril source.
	 *
	 * @param itemName
	 *            The name of the item to be prospected.
	 */
	public SourceMithril(final String itemName) {
		this.itemName = itemName;
		put("class", "source");
		put("name", sourceClass);
		setMenu("Wydobądź|Użyj");
		setDescription("Wszystko wskazuje na to, że tutaj coś jest.");
	}

	public static void generateRPClass() {
		final RPClass rpclass = new RPClass(sourceClass);
		rpclass.isA("entity");
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
					player.incMiningXP(1500);
				}

				player.sendPrivateText("Wydobyłeś " + Grammar.a_noun(item.getTitle()) + ".");
			} else {
				logger.error("could not find item: " + itemName);
			}
		} else {
			if (skill != null) {
				player.incMiningXP(150);
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
		player.sendPrivateText("Rozpocząłeś wydobywanie mithrilu.");
		notifyWorldAboutChanges();
		addEvent(new ImageEffectEvent("mining", true));
		notifyWorldAboutChanges();
	}
}
