/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.events;

import games.stendhal.client.entity.Entity;
import games.stendhal.client.gui.AffixRerollWindow;

/** Receives private Przemir service state for the current player. */
final class AffixRerollEvent extends Event<Entity> {
	@Override
	public void execute() {
		AffixRerollWindow.show(event, entity.getRPObject());
	}
}
