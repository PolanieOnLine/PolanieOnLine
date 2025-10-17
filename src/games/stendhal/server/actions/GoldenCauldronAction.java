package games.stendhal.server.actions;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.mapstuff.useable.GoldenCauldronEntity;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.game.RPAction;

/**
 * Handles client actions directed at Draconia's golden cauldron.
 */
public class GoldenCauldronAction implements ActionListener {
	private static final String TYPE = "golden_cauldron";

	public static void register() {
		CommandCenter.register(TYPE, new GoldenCauldronAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		final GoldenCauldronEntity cauldron = resolveTarget(player, action);
		if (cauldron == null) {
			return;
		}

		final String command = action.get("action");
		if ("open".equals(command)) {
			cauldron.onUsed(player);
		} else if ("close".equals(command)) {
			cauldron.close(player);
		} else if ("mix".equals(command)) {
			cauldron.mix(player);
		}
	}

	private GoldenCauldronEntity resolveTarget(final Player player, final RPAction action) {
		if (!action.has(games.stendhal.common.constants.Actions.TARGET_PATH)) {
			return null;
		}
		final Entity entity = EntityHelper.getEntityFromPath(player,
				action.getList(games.stendhal.common.constants.Actions.TARGET_PATH));
		if (entity instanceof GoldenCauldronEntity) {
			return (GoldenCauldronEntity) entity;
		}
		return null;
	}
}
