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

import java.util.stream.Stream;

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
 * A carbuncle source is a spot where a player can prospect for carbuncle. He
 * needs a kilof and lina, time, and luck.
 *
 * Prospecting takes 7-11 seconds; during this time, the player keep standing
 * next to the carbuncle source. In fact, the player only has to be there when the
 * prospecting action has finished. Therefore, make sure that two carbuncle sources
 * are always at least 5 sec of walking away from each other, so that the player
 * can't prospect for carbuncle at several sites simultaneously.
 *
 * @author daniel
 * @changes artur, KarajuSs
 */
public class SourceCarbuncle extends SourceEntity {
	private static final Logger logger = Logger.getLogger(SourceCarbuncle.class);

	private final static String sourceClass = "source_carbuncle";

	private final String startSound = "pickaxe_01";
	private final String successSound = "rocks-1";
	private final int SOUND_RADIUS = 20;

	/**
	 * The name of the item to be found.
	 */
	private final String itemName;

	/**
	 * Create a carbuncle source.
	 */
	public SourceCarbuncle() {
		this("kryształ rubinu");
	}

	/**
	 * source name.
	 */
	@Override
	public String getName() {
		return("surowców");
	}

	/**
	 * Create a carbuncle source.
	 *
	 * @param itemName
	 *            The name of the item to be prospected.
	 */
	public SourceCarbuncle(final String itemName) {
		this.itemName = itemName;

		setRPClass("useable_entity");
		put("type", "useable_entity");
		put("class", "source");
		put("name", sourceClass);
		setMenu("Wydobądź|Użyj");
		setDescription("Wszystko wskazuje na to, że tutaj coś jest.");

		setResistance(100);
	}

	@Override
	protected boolean isPrepared(Player player) {
		for (final String itemName : NEEDED_PICKS) {
			if (itemName != NEEDED_PICKS[0] && player.isEquipped(itemName)) {
				return true;
			}
		}

		player.sendPrivateText("Potrzebujesz kilofa do wydobywania kamieni.");
		return false;
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
				String lastItem = Stream.of(NEEDED_PICKS).reduce((first,last) -> last).get();
				if (player.isEquipped(lastItem)) {
					int amount = Rand.throwCoin();
					((StackableItem) item).setQuantity(amount);
				}

				player.equipOrPutOnGround(item);
				player.incMinedForItem(item.getName(), item.getQuantity());
				if (skill != null) {
					player.incMiningXP(630);
				}

				player.sendPrivateText("Wydobyłeś " + Grammar.a_noun(item.getTitle()) + ".");
			} else {
				logger.error("could not find item: " + itemName);
			}
		} else {
			if (skill != null) {
				player.incMiningXP(63);
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
		player.sendPrivateText("Rozpocząłeś wydobywanie surowca.");
		notifyWorldAboutChanges();
		addEvent(new ImageEffectEvent("mining", true));
		notifyWorldAboutChanges();
	}
}
