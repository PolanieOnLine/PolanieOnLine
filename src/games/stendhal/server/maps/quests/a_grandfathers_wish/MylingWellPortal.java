/***************************************************************************
 *                    Copyright © 2003-2022 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.a_grandfathers_wish;

import static games.stendhal.server.maps.quests.AGrandfathersWish.QUEST_SLOT;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.portal.AccessCheckingPortal;
import games.stendhal.server.entity.player.Player;

public class MylingWellPortal extends AccessCheckingPortal {
	public MylingWellPortal() {
		super();
	}

	@Override
	protected boolean isAllowed(final RPEntity user) {
		if (!(user instanceof Player)) {
			return false;
		}

		final Player player = (Player) user;
		final String find_myling = player.getQuest(QUEST_SLOT, 1);
		if (find_myling == null || find_myling.equals("")) {
			rejectedMessage = "Nie ma powodu, aby teraz wchodzić do tej studni.";
			return false;
		}
		if (find_myling.equals("find_myling:done")) {
			return true;
		}
		if (!player.isEquipped("lina")) {
			rejectedMessage = "Aby zejść do tej studni, potrzebujesz liny.";
			if (!find_myling.equals("find_myling:well_rope")) {
				player.setQuest(QUEST_SLOT, 1, "find_myling:well_rope");
			}
			return false;
		}

		return true;
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (isAllowed(user) && user instanceof Player) {
			return usePortal((Player) user);
		}

		rejected(user);
		return false;
	}

	@Override
	protected boolean usePortal(final Player player) {
		final boolean ret = super.usePortal(player);

		if (!player.getQuest(QUEST_SLOT, 1).equals("find_myling:done")) {
			// rope is hung so player can use anytime now
			player.drop("lina");
			player.setQuest(QUEST_SLOT, 1, "find_myling:done");
			player.sendPrivateText("Czy to jest Niall!? Biedny chłopiec. Muszę"
				+ " natychmiast powiedzieć Eliasowi.");
		} else if (player.getQuest(QUEST_SLOT, 3).equals("cure_myling:start")
				&& player.isEquipped("woda święcona z popiołem")) {
			player.sendPrivateText("Powinienem móc użyć tutaj wody"
				+ " święconej.");
		}

		return ret;
	}
}