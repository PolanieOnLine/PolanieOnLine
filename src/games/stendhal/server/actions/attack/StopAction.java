/***************************************************************************
 *                   (C) Copyright 2003-2013 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions.attack;

import static games.stendhal.common.constants.Actions.ATTACK;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.common.Direction;

import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import marauroa.common.game.RPAction;

/**
 * stops attacking another creature or player
 */
public class StopAction implements ActionListener {

	/**
	 * register action
	 */
	public static void register() {
		CommandCenter.register("stop", new StopAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		player.cancelKeyboardTurn();
		if (action.has("finish_id") && action.get("finish_id").length() <= 64) {
			player.put("movement_request", action.get("finish_id"));
		} else if (player.has("movement_request")) {
			player.put("movement_request", "");
		}
		if (!finishKeyboardStep(player, action)) player.requestStop();

		if (action.has(ATTACK)) {
			player.stopAttack();
		}
		if (action.has("finish_zone")) {
			// Confirm even a rejected/no-op start. Otherwise an unchanged speed of
			// zero would leave the client waiting indefinitely for its final tile.
			player.put("x", player.getX());
			player.put("y", player.getY());
			player.put("speed", player.getSpeed());
		}

		player.notifyWorldAboutChanges();
	}

	/** A key release may finish its visible step, using the ordinary collision-checked walking path. */
	private boolean finishKeyboardStep(final Player player, final RPAction action) {
		if (action.has(ATTACK) || player.has("autowalk") || player.stopped() || player.getZone() == null
				|| !action.has("finish_zone") || !action.get("finish_zone").equals(player.getZone().getName())
				|| !action.has("finish_x") || !action.has("finish_y") || !action.has("finish_dir")) return false;
		try {
			final Direction direction = Direction.build(action.getInt("finish_dir"));
			final int targetX = action.getInt("finish_x"), targetY = action.getInt("finish_y");
			if (direction == Direction.STOP || direction != player.getDirection()
					|| (direction.getdx() == 0 && targetX != player.getX())
					|| (direction.getdy() == 0 && targetY != player.getY())) return false;
			final long distance = ((long) targetX - player.getX()) * direction.getdx()
					+ ((long) targetY - player.getY()) * direction.getdy();
			// Never turn back towards an older client position or accept a long walking request here.
			if (distance <= 0) {
				player.clearPath();
				player.stop();
				return true;
			}
			final double speed = player.getMovementSpeed();
			if (!Double.isFinite(speed) || speed <= 0 || distance > Math.min(16, Math.max(1, Math.ceil(speed)))) return false;
			final List<Node> nodes = new ArrayList<>();
			for (int step = 0; step <= distance; step++) {
				nodes.add(new Node(player.getX() + step * direction.getdx(), player.getY() + step * direction.getdy()));
			}
			player.setPath(new FixedPath(nodes, false));
			if (player.has("pathset")) player.remove("pathset");
			return true;
		} catch (final NumberFormatException exception) {
			return false;
		}
	}
}
