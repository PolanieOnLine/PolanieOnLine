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
package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.common.Rand;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ImageEffectEvent;
import games.stendhal.server.events.SoundEvent;

/**
 * A coal source is a spot where a player can pick for coal. He
 * needs a pick, time, and luck.
 *
 * Picking coals takes 7-11 seconds; during this time, the player keep standing
 * next to the coal source. In fact, the player only has to be there when the
 * prospecting action has finished. Therefore, make sure that two sources
 * are always at least 5 sec of walking away from each other, so that the player
 * can't prospect at several sites simultaneously.
 *
 * @author hendrik
 */
public class CoalSource extends SourceEntity {
	/**
	 * The name of the item to be found.
	 */
	private final String itemName;

	/**
	 * Sound effects
	 */
	private final String startSound = "pick-metallic-1";
	private final int SOUND_RADIUS = 20;

	/**
	 * Create a gold source.
	 */
	public CoalSource() {
		this("węgiel");
	}

	/**
	 * Create a coal source.
	 *
	 * @param itemName
	 *            The name of the item to be prospected.
	 */
	public CoalSource(final String itemName) {
		this.itemName = itemName;

		setRPClass("useable_entity");
		put("type", "useable_entity");
		put("class", "source");
		put("name", "coal_source");
		put("state", 0);
		setMenu("Wydobądź węgiel|Użyj");
		setDescription("Jest coś czarnego na skale.");
		handleRespawn();
	}

	/**
	 * source name.
	 */
	@Override
	public String getName() {
		return("kawałek węgla");
	}

	/**
	 * Decides if the activity was successful.
	 *
	 * @return <code>true</code> if successful.
	 */
	@Override
	protected boolean isSuccessful(final Player player) {
		return getState() > 0;
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
	    setMiningXP(player, successful, itemName, 50);
	    if (successful) {
	    	setState(getState()- 1);
			handleRespawn();
	    }
	}

	/**
	 * triggers the respawn if the coal was compeletly picked
	 */
	private void handleRespawn() {
		if (getState() == 0) {
			final int time = Rand.randExponential(6000);
			int turn = Math.max(time, 6000);
			TurnNotifier.get().notifyInTurns(turn, new Refiller());
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
		player.sendPrivateText("Rozpocząłeś wydobywanie węgla.");
        addEvent(new SoundEvent(startSound, SOUND_RADIUS, 100, SoundLayer.AMBIENT_SOUND));
        notifyWorldAboutChanges();
        addEvent(new ImageEffectEvent("mining", true));
        notifyWorldAboutChanges();
	}

	/**
	 * refills the coal
	 *
	 * @author hendrik
	 */
	private class Refiller implements TurnListener {
		@Override
		public void onTurnReached(int currentTurn) {
			setState(Rand.randUniform(1, 3));
		}
	}
}
