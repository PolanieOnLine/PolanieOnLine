/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.entity.item.behavior;

import java.util.Map;
import games.stendhal.server.core.navigation.CompassService;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/** The keepsake switches the owner's compass overlay without consuming the item. */
public final class WanderersCompassUseBehavior implements UseBehavior {
	/** Constructor used by the item definition loader. */
	public WanderersCompassUseBehavior(Map<String, String> parameters) {
	}

	@Override
	public boolean use(RPEntity user, Item item) {
		if (!(user instanceof Player) || item.getBaseContainer() != user) {
			return false;
		}
		Player player = (Player) user;
		if (item.isBound() && !player.getName().equals(item.getBoundTo())) {
			return false;
		}
		if (!player.isQuestCompleted("wanderers_compass")) {
			player.sendPrivateText("Ten kompas jeszcze cię nie prowadzi. Porozmawiaj z Sabałą w Zakopanem.");
			return false;
		}
		boolean hide = !player.has(CompassService.HIDDEN) || player.getInt(CompassService.HIDDEN) != 1;
		player.put(CompassService.UNLOCKED, 1);
		player.put(CompassService.HIDDEN, hide ? 1 : 0);
		CompassService.update(player);
		player.sendPrivateText(hide
				? "Chowasz kompas z ekranu. Użyj go ponownie, aby go pokazać."
				: "Kompas jest znowu widoczny na ekranie.");
		player.notifyWorldAboutChanges();
		return true;
	}
}
