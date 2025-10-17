package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.TARGET;
import static games.stendhal.common.constants.Actions.TARGET_PATH;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.useable.GoldenCauldronEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.game.RPAction;

public class GoldenCauldronAction implements ActionListener {
	private static final String TYPE = "golden_cauldron";

	public static void register() {
		CommandCenter.register(TYPE, new GoldenCauldronAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		final String actionZone = action.get("zone");
		if (actionZone != null && !actionZone.equals(player.getZone().getName())) {
			return;
		}

		final String command = action.get("action");
		if (command == null) {
			return;
		}

		final GoldenCauldronEntity cauldron = resolveTarget(player, action);
		if (cauldron == null) {
			return;
		}

		switch (command) {
		case "mix":
			cauldron.mix(player);
			break;
		case "close":
			cauldron.close(player);
			break;
		default:
			break;
		}
	}

	private GoldenCauldronEntity resolveTarget(final Player player, final RPAction action) {
		Entity entity = null;
		if (action.has(TARGET_PATH)) {
			entity = EntityHelper.getEntityFromPath(player, action.getList(TARGET_PATH));
		} else if (action.has(TARGET)) {
			entity = EntityHelper.entityFromTargetName(action.get(TARGET), player);
		}
		if (entity instanceof GoldenCauldronEntity) {
			return (GoldenCauldronEntity) entity;
		}
		return null;
	}
}
