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
package games.stendhal.server.entity.mapstuff.game.movevalidator;

import games.stendhal.server.entity.item.token.BoardToken;
import games.stendhal.server.entity.mapstuff.game.GameBoard;
import games.stendhal.server.entity.player.Player;

/**
 * Was this item pulled from its home place?
 *
 * @author hendrik
 */
public class MovementSourceIsHomeValidator implements MoveValidator {

	@Override
	public boolean validate(GameBoard board, Player player, BoardToken token, int xIndex, int yIndex) {
		if (!token.wasMovedFromHomeInLastMove()) {
			player.sendPrivateText("Możesz przesunąć krążki znajdujące się tylko na stosie poza planszą gry.");
			return false;
		}
		return true;
	}

}
