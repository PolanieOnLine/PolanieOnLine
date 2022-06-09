package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.TARGET;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.scroll.MarkedScroll;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

public class MarkScrollAction implements ActionListener {

	@Override
	public void onAction(Player player, RPAction action) {
		int count = 1;
		if (action.has("quantity")) {
			count = Integer.parseInt(action.get("quantity"));
		}

		// can't try to drop the scroll straight away as teleport may not be allowed
		if(player.isEquipped("niezapisany zwój", count)) {
			final StendhalRPZone zone = player.getZone();
			final int x = player.getX();
			final int y = player.getY();

			if (zone.isTeleportInAllowed(x, y)) {
				player.drop("niezapisany zwój", count);

				final MarkedScroll scroll = (MarkedScroll)
						SingletonRepository.getEntityManager().getItem("zwój zapisany");
				scroll.setQuantity(count);

				// add a description if the player wanted one
				if (action.has(TARGET)) {
					String description = action.get(TARGET) + " " + action.get("args");
					scroll.setDescription("Oto zwój zapisany przez " + player.getName() + ". Napis głosi: \""+ description +"\". ");
				}

				// set infostring after description
				scroll.setInfoString(zone.getName() + " " + x + " " + y);

				player.equipOrPutOnGround(scroll);
			} else {
				player.sendPrivateText("Silna antymagiczna aura blokuje działanie zwoju na tym obszarze!");
			}
		} else {
			if (count > 1) {
				player.sendPrivateText("Nie posiadasz zbyt wiele zwoi do zapisania.");
			} else {
				player.sendPrivateText("Nie posiadasz jakiegokolwiek zwoju do zapisania.");
			}
		}			
	} 

}
