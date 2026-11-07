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
package games.stendhal.server.entity.item.consumption;

import games.stendhal.server.core.rule.glyph.GlyphRecoveryService;
import games.stendhal.server.core.rule.damage.LifeTideService;
import games.stendhal.server.entity.item.ConsumableItem;
import games.stendhal.server.entity.player.Player;

class Stuffer implements Feeder {

	@Override
	public boolean feed(final ConsumableItem item, final Player player) {
		LifeTideService.healFromPotion(player, GlyphRecoveryService.boostConsumableHealing(player,
				((ConsumableItem) item.splitOff(1)).getAmount()));
		return true;
	}

}
