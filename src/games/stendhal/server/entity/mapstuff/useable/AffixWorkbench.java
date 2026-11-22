/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.server.actions.AffixRerollAction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.tarnow.enchanter.EnchanterNPC;

/** Interaction area for the workbench already painted in Przemir's workshop. */
public final class AffixWorkbench extends UseableEntity {
	public AffixWorkbench() {
		setEntityClass("furniture");
		put("name", "affix_workbench");
		put("state", 0);
		put("title", "Stół zaklinacza");
		setDescription("Oto stół zaklinacza. Leżą na nim narzędzia i kamienie do przemiany właściwości wyposażenia. Użyj go, aby skorzystać z pomocy Przemira.");
		setSize(2, 1);
		// The map supplies both the visible table and its collision.
		setResistance(0);
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (!(user instanceof Player) || getZone() == null || !EnchanterNPC.ZONE.equals(getZone().getName())
				|| user.getZone() != getZone() || !user.nextTo(this)) return false;
		final SpeakerNPC npc = NPCList.get().get(EnchanterNPC.NAME);
		if (npc == null || npc.getZone() != getZone() || user.squaredDistance(npc) > 25) return false;
		AffixRerollAction.open((Player) user, npc);
		return true;
	}
}
