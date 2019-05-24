/* $Id$ */
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
package games.stendhal.server.entity.slot;

import games.stendhal.common.TradeState;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.player.Player;

/**
 * Slots of players which are use to offer items for trading.
 *
 * @author hendrik
 */
public class PlayerTradeSlot extends PlayerSlot {

	/**
	 * Creates a new PlayerTradeSlot.
	 *
	 * @param name name of slot
	 */
	public PlayerTradeSlot(final String name) {
		super(name);
	}

	@Override
	public boolean isReachableForTakingThingsOutOfBy(final Entity entity) {
		TradeState tradeState = ((Player) getOwner()).getTradeState();
		if (tradeState == TradeState.NO_ACTIVE_TRADE || tradeState == TradeState.OFFERING_TRADE) {
			setErrorMessage("W tym momencie z nikim nie handlujesz.");
			return false;
		}
		if (tradeState != TradeState.MAKING_OFFERS) {
			setErrorMessage("Twoja oferta została zablokowana. Możesz anulować wymianę jeżeli chcesz coś zmienić.");
			return false;
		}
		return super.isReachableForTakingThingsOutOfBy(entity);
	}

	@Override
	public boolean isTargetBoundCheckRequired() {
		return true;
	}
}