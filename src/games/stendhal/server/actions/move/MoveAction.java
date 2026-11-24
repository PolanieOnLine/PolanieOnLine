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
package games.stendhal.server.actions.move;

import static games.stendhal.common.constants.Actions.DIR;
import static games.stendhal.common.constants.Actions.MOVE;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.common.Direction;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.events.TutorialNotifier;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import marauroa.common.game.RPAction;

/**
 * moves into the specified direction
 */
public class MoveAction implements ActionListener {

	/**
	 * registers the action
	 */
	public static void register() {
		final MoveAction move = new MoveAction();
		CommandCenter.register(MOVE, move);
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		if (action.has(DIR)) {
			final int dirval = action.getInt(DIR);
			boolean applyNow = true;

			if (dirval < 0) {
				player.removeClientDirection(Direction.build(-dirval));
				applyNow = !player.hasQueuedKeyboardTurn();
			} else {
				if (action.has("input_id") && action.get("input_id").length() <= 64) player.put("movement_request", action.get("input_id"));
				else if (player.has("movement_request")) player.put("movement_request", "");
				final Direction previous = player.getDirection();
				player.addClientDirection(Direction.build(dirval));
				applyNow = !finishStepBeforeTurn(player, action, previous);
				player.removeAwayStatus();
			}

			if (applyNow) player.applyClientDirection(true);
		}

		TutorialNotifier.move(player);
		player.notifyWorldAboutChanges();
	}

	private boolean finishStepBeforeTurn(final Player player, final RPAction action, final Direction previous) {
		if (player.stopped() || player.has("autowalk") || player.getZone() == null
				|| !action.has("turn_zone") || !player.getZone().getName().equals(action.get("turn_zone"))
				|| !action.has("turn_x") || !action.has("turn_y") || !action.has("turn_from")) return false;
		try {
			final int x = action.getInt("turn_x"), y = action.getInt("turn_y");
			if (previous == Direction.STOP || Direction.build(action.getInt("turn_from")) != previous
					|| Direction.build(action.getInt(DIR)) == previous
					|| (previous.getdx() == 0 && x != player.getX()) || (previous.getdy() == 0 && y != player.getY())) return false;
			final long distance = ((long) x - player.getX()) * previous.getdx() + ((long) y - player.getY()) * previous.getdy();
			final double speed = player.getMovementSpeed();
			if (distance <= 0 || !Double.isFinite(speed) || speed <= 0
					|| distance > Math.min(16, Math.max(1, Math.ceil(speed)))) return false;
			final List<Node> nodes = new ArrayList<>();
			for (int step = 0; step <= distance; step++) {
				nodes.add(new Node(player.getX() + step * previous.getdx(), player.getY() + step * previous.getdy()));
			}
			player.queueKeyboardTurn(new FixedPath(nodes, false), x, y);
			return true;
		} catch (final NumberFormatException exception) {
			return false;
		}
	}
}
