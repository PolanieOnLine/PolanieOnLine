package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.TARGET;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

public class MarkScrollAction implements ActionListener {

	@Override
	public void onAction(Player player, RPAction action) {

		// can't try to drop the scroll straight away as teleport may not be allowed
		if(player.isEquipped("niezapisany zwój", 1)) {

			final StendhalRPZone zone = player.getZone();
			final int x = player.getX();
			final int y = player.getY();

			if (zone.isTeleportInAllowed(x, y)) {
				
				player.drop("niezapisany zwój", 1);

				String infostring = zone.getName() + " " + x + " " + y;

				Item scroll = SingletonRepository.getEntityManager().getItem("zwój zapisany");				
				scroll.setInfoString(infostring);
				
				// add a description if the player wanted one
				if (action.has(TARGET)) {
					String description = action.get(TARGET) + " " + action.get("args");
					scroll.setDescription("Oto zwój zapisany przez " + player.getName() + ". Napis głosi: \""+ description +"\". ");
				}

				player.equipOrPutOnGround(scroll);

			} else {
				player.sendPrivateText("Silna antymagiczna aura blokuje działanie zwoju na tym obszarze!");
			}
		} else {
			player.sendPrivateText("Nie masz niezapisanego zwoju do zapisania.");
		}			
	} 

}


