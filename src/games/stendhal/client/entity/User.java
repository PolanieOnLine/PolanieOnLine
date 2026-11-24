/***************************************************************************
 *                (C) Copyright 2003-2022 - Faiumoni e.V.                  *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.entity;

import static games.stendhal.client.ClientLanguage.getText;

import static games.stendhal.common.constants.Actions.MODE;
import static games.stendhal.common.constants.Actions.TYPE;
import static games.stendhal.common.constants.Actions.WALK;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.GameObjects;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.common.Direction;
import games.stendhal.common.NotificationType;
import games.stendhal.common.grammar.Grammar;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * This class identifies the user of this client.
 *
 * @author durkham, hendrik
 */
public class User extends Player {
	private static final StaticUserProxy NO_USER = new NoUserProxy();
	private static final String IGNORE_SLOT = "!ignore";

	private static String groupLootmode;
	private static String groupExpmode;
	private static Set<String> groupMembers = Collections.emptySet();
	private static StaticUserProxy userProxy = NO_USER;

	private final Set<String> ignore = new HashSet<String>();
	private final Deque<ConfirmedStep> confirmedSteps = new ArrayDeque<>();
	private int confirmedX, confirmedY;
	private double serverMovementSpeed;
	private Direction confirmedDirection = Direction.DOWN;
	private boolean startingConfirmedMovement = true;
	private Direction keyboardStartup = Direction.STOP;
	private double keyboardStartupTime;
	private double lastWalkingSpeed = 1;

	/**
	 * creates a User object
	 */
	public User() {
		userProxy = new NormalUserProxy(this);
	}

	/**
	 * gets the User object
	 *
	 * @return user object
	 */
	public static User get() {
		return userProxy.getUser();
	}

	/**
	 * is the user object not set, yet?
	 *
	 * @return true, if the the user object is unknown; false if it is known
	 */
	public static boolean isNull() {
		return userProxy == NO_USER;
	}

	/**
	 * Resets the class to uninitialized.
	 */
	static void setNull() {
		userProxy = NO_USER;
	}

	/**
	 * gets the name of the player's character
	 *
	 * @return charname or <code>null</code>
	 */
	public static String getCharacterName() {
		return userProxy.getName();
	}

	/**
	 * gets the level of the current user
	 *
	 * @return level
	 */
	public static int getPlayerLevel() {
		return userProxy.getPlayerLevel();
	}

	/**
	 * gets the server release version
	 *
	 * @return server release version or <code>null</code>
	 */
	public static String getServerRelease() {
		return userProxy.getServerRelease();
	}

	/**
	 * is the specified charname a buddy of us?
	 *
	 * @param name charname to test
	 * @return true, if it is a buddy, false if it is not a buddy or the user object is unknown.
	 */
	public static boolean hasBuddy(String name) {
		return userProxy.hasBuddy(name);
	}

	/**
	 * is this user an admin with an adminlevel equal or above 600?
	 *
	 * @return true, if the user is an admin; false otherwise
	 */
	public static boolean isAdmin() {
		return userProxy.isAdmin();
	}

	/**
	 * is the player in a group which shares the loot?
	 *
	 * @return true if this player is a group and it uses shared looting
	 */
	public static boolean isGroupSharingLoot() {
		return "shared".equals(groupLootmode);
	}

	/**
	 * is the named player ignored?
	 *
	 * @param name name of player
	 * @return true, if the player should be ignored; false otherwise
	 */
	public static boolean isIgnoring(String name) {
		return userProxy.isIgnoring(name);
	}

	/**
	 * checks if the specified player is in the same group as this player
	 *
	 * @param otherPlayer name of the other player
	 * @return true if the other player is in the same group
	 */
	public static boolean isPlayerInGroup(String otherPlayer) {
		return groupMembers.contains(otherPlayer);
	}

	/**
	 * updates the group information
	 *
	 * @param members members
	 * @param lootmode lootmode
	 * @param expmode expmode
	 */
	public static void updateGroupStatus(Collection<String> members, String lootmode, String expmode) {
		Set<String> oldGroupMembers = groupMembers;

		if (members == null) {
			groupMembers = Collections.emptySet();
		} else {
			groupMembers = new HashSet<>(members);
		}
		groupLootmode = lootmode;
		groupExpmode = expmode;

		// fire change event to color of player object on minimap
		for (IEntity entity : GameObjects.getInstance()) {
			if ((entity instanceof Player)
					&& (oldGroupMembers.contains(entity.getName())
							|| groupMembers.contains(entity.getName()))) {
				((Player) entity).fireChange(RPEntity.PROP_GROUP_MEMBERSHIP);
			}
		}
	}

	/**
	 * calculates the squared distance between the user and the specified coordinates
	 *
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return the squared distance
	 */
	static double squaredDistanceTo(final double x, final double y) {
		return userProxy.squareDistanceTo(x, y);
	}

	/**
	 * Add players to the set of ignored players.
	 * Player names are the attributes prefixed with '_'.
	 *
	 * @param ignoreObj The container object for player names
	 */
	private void addIgnore(RPObject ignoreObj) {
		for (String attr : ignoreObj) {
			if (attr.charAt(0) == '_') {
				ignore.add(attr.substring(1));
			}
		}
	}

	/**
	 * Remove players from the set of ignored players.
	 * Player names are the attributes prefixed with '_'.
	 *
	 * @param ignoreObj The container object for player names
	 */
	private void removeIgnore(RPObject ignoreObj) {
		for (String attr : ignoreObj) {
			if (attr.charAt(0) == '_') {
				ignore.remove(attr.substring(1));
			}
		}
	}

	/**
	 * Returns the objectid for the named item.
	 *
	 * @param slotName
	 *            name of slot to search
	 * @param itemName
	 *            name of item
	 * @return objectid or <code>-1</code> in case there is no such item
	 */
	public int findItem(final String slotName, final String itemName) {
		RPSlot slot = getSlot(slotName);
		if (slot == null) {
			return -1;
		}
		for (final RPObject item : slot) {
			if (item.get("name").equals(itemName)) {
				return item.getID().getObjectID();
			}
		}

		return -1;
	}

	/**
	 * checks whether the user owns a pet
	 *
	 * @return true, if the user owns a pet; false otherwise
	 */
	public boolean hasPet() {
		return rpObject.has("pet");
	}

	/**
	 * gets the ID of a pet
	 *
	 * @return ID of pet
	 */
	public int getPetID() {
		return rpObject.getInt("pet");
	}

	/**
	 * checks whether the user owns a sheep
	 *
	 * @return true, if the user owns a sheep; false otherwise
	 */
	public boolean hasSheep() {
		return rpObject.has("sheep");
	}

	/**
	 * gets the ID of a sheep
	 *
	 * @return ID of sheep
	 */
	public int getSheepID() {
		return rpObject.getInt("sheep");
	}

	public boolean hasGoat() {
		return rpObject.has("goat");
	}

	public int getGoatID() {
		return rpObject.getInt("goat");
	}

	/**
	 * gets the zone name
	 *
	 * @return zone name
	 */
	public String getZoneName() {
		return getID().getZoneID();
	}

	/**
	 * Is this object the user of this client?
	 *
	 * @return true
	 */
	@Override
	public boolean isUser() {
		return true;
	}

	@Override
	protected void onAway(final String message) {
		super.onAway(message);

		String text;
		if (message == null) {
			text = "Nie jesteś już " + Grammar.genderVerb(getGender(), "oznaczony") + " jako " + Grammar.genderVerb(getGender(), "oddalony") + ".";
		} else {
			text = Grammar.genderVerb(getGender(), "Zostałeś") + " " + Grammar.genderVerb(getGender(), "oznaczony") + " jako " + Grammar.genderVerb(getGender(), "oddalony") + ".";
		}
		notifyUser(getText(text), NotificationType.INFORMATION);
	}

	/**
	 * The object added/changed attribute(s).
	 *
	 * @param object
	 *            The base object.
	 * @param changes
	 *            The changes.
	 */
	@Override
	public void onChangedAdded(final RPObject object, final RPObject changes) {
		/* TODO: enable when walking bug fix is finished. */
		/*
		if (!this.stopped()) {
			boolean shouldStop = true;
			String debugString = "Stopped on:";
			if (StendhalClient.get().directionKeyIsPressed()) {
				shouldStop = false;
			} else {
				debugString += " !directionKeyIsPressed()";
			}
			if (object.has(AUTOWALK)) {
				shouldStop = false;
			} else {
				debugString += " !has(AUTOWALK)";
			}
			if (object.has(PATHSET)) {
				shouldStop = false;
			} else {
				debugString += " !has(PATHSET)";
			}

			if (shouldStop) {
				// Stop the character's movement.
				this.stopMovement();

				if (logger.isDebugEnabled() || Testing.DEBUG) {
					logger.info(debugString);
				}
			}
		}
		*/

		super.onChangedAdded(object, changes);

		// The first time we ignore it.
		if (object != null) {
			notifyUserAboutPlayerOnlineChanges(changes);

			if (changes.hasSlot(IGNORE_SLOT)) {
				RPObject ign = changes.getSlot(IGNORE_SLOT).getFirst();
				if (ign != null) {
					addIgnore(ign);
				}
			}
		}
	}

	@Override
	public void onChangedRemoved(final RPObject base, final RPObject diff) {
		super.onChangedRemoved(base, diff);
		if (diff.hasSlot(IGNORE_SLOT)) {
			RPObject ign = diff.getSlot(IGNORE_SLOT).getFirst();
			if (ign != null) {
				removeIgnore(ign);
			}
		}
	}

	@Override
	public void onHealed(final int amount) {
		super.onHealed(amount);

		String pointDesc = Grammar.quantityplnoun(amount, "punkt") + " życia";
		notifyUser(getText(getTitle() + " odzyskał " + pointDesc + "."), NotificationType.HEAL);
	}

	private void notifyUser(String message, NotificationType type) {
		ClientSingletonRepository.getUserInterface().addEventLine(new HeaderLessEventLine(message, type));
	}

	private void notifyUserAboutPlayerOnlineChanges(RPObject changes) {
		notifyUserAboutPlayerStatus(changes, "offline", " opuścił PolanieOnLine.");
		notifyUserAboutPlayerStatus(changes, "online", " zawitał do PolanieOnLine.");
	}

	private void notifyUserAboutPlayerStatus(RPObject changes, String status, String messageEnd) {
		if (changes.has(status)) {
			String[] players = changes.get(status).split(",");
			for (String playername : players) {
				notifyUser(getText(playername + messageEnd), NotificationType.INFORMATION);
			}
		}
	}

	/** Begin a bounded first-step preview while the server commits the same tile. */
	public synchronized void prepareKeyboardMove(final Direction direction, final RPAction action) {
		if (getRPObject() == null || !getRPObject().has("zoneid")
				|| !stopped() || !confirmedSteps.isEmpty() || keyboardStartup != Direction.STOP
				|| serverMovementSpeed > 0 || direction == Direction.STOP || getRPObject().has("autowalk")
				|| Math.abs(x - confirmedX) > 0.000001 || Math.abs(y - confirmedY) > 0.000001) return;
		final Direction movement = isConfused() ? direction.oppositeDirection() : direction;
		final double oldX = x, oldY = y;
		x += movement.getdx(); y += movement.getdy();
		final boolean blocked;
		try {
			blocked = GameObjects.getInstance().collides(this);
		} finally {
			x = oldX; y = oldY;
		}
		if (blocked) return;
		action.put("start_step", "");
		keyboardStartup = movement;
		keyboardStartupTime = 0;
		setDirection(movement);
		setSpeed(movement.getdx() * lastWalkingSpeed, movement.getdy() * lastWalkingSpeed);
		fireChange(PROP_SPEED);
	}

	/** A different input source explicitly replaces the unconfirmed first step. */
	public synchronized void cancelKeyboardStart() {
		if (keyboardStartup == Direction.STOP) return;
		keyboardStartup = Direction.STOP;
		if (Math.abs(x - confirmedX) + Math.abs(y - confirmedY) > 0.000001) {
			confirmedSteps.addLast(new ConfirmedStep(confirmedX, confirmedY, 120));
			applyConfirmedStep();
		} else {
			setSpeed(0, 0);
		}
		fireChange(PROP_SPEED);
	}

	/** Face the requested direction, but never invent an unconfirmed walking step. */
	public synchronized void predictMovement(final Direction direction, final boolean facing) {
		if (facing) cancelKeyboardStart();
		if (stopped()) setDirection(isConfused() ? direction.oppositeDirection() : direction);
	}

	@Override
	public synchronized void initialize(final RPObject object) {
		confirmedSteps.clear();
		keyboardStartup = Direction.STOP;
		startingConfirmedMovement = true;
		super.initialize(object);
		confirmedX = object.getInt("x");
		confirmedY = object.getInt("y");
		confirmedDirection = getDirection();
		serverMovementSpeed = object.has("speed") ? object.getDouble("speed") : 0;
		setSpeed(0, 0);
	}

	@Override
	protected synchronized void processPositioning(final RPObject base, final RPObject diff) {
		final boolean newZone = diff.has("zoneid")
				&& (!base.has("zoneid") || !diff.get("zoneid").equals(base.get("zoneid")));
		final boolean relocated = diff.has("movement_reset")
				&& (!base.has("movement_reset") || !diff.get("movement_reset").equals(base.get("movement_reset")));
		if (!diff.has("x") && !diff.has("y") && !diff.has("dir") && !diff.has("speed")
				&& !diff.has("movement_steps") && !newZone && !relocated) return;
		final int nextX = diff.has("x") ? diff.getInt("x") : confirmedX;
		final int nextY = diff.has("y") ? diff.getInt("y") : confirmedY;
		final Direction nextDirection = diff.has("dir") ? Direction.build(diff.getInt("dir")) : confirmedDirection;
		final double nextSpeed = diff.has("speed") ? diff.getDouble("speed") : serverMovementSpeed;
		final double travelSpeed = nextSpeed > 0 ? nextSpeed : serverMovementSpeed;
		final int distance = Math.abs(nextX - confirmedX) + Math.abs(nextY - confirmedY);
		if (newZone || relocated || distance > Math.max(4, Math.ceil(travelSpeed * 2)) || confirmedSteps.size() > 24) {
			confirmedSteps.clear();
			keyboardStartup = Direction.STOP;
			startingConfirmedMovement = true;
			x = nextX;
			y = nextY;
			onPosition(x, y);
		} else if (diff.has("movement_steps") && appendConfirmedRoute(diff.get("movement_steps"), nextX, nextY,
				Double.isFinite(travelSpeed) && travelSpeed > 0 ? travelSpeed : 1)) {
			startingConfirmedMovement = false;
		} else if (distance > 0) {
			// Compatibility with servers predating confirmed movement routes.
			// A perception may contain both legs of a fast mouse route. The last
			// direction identifies the final leg; keep the actual grid corner.
			final double speed = Double.isFinite(travelSpeed) && travelSpeed > 0 ? travelSpeed : distance;
			if (nextX != confirmedX && nextY != confirmedY) {
				if (nextDirection.getdx() != 0) appendConfirmedStep(confirmedX, nextY, speed, false);
				else appendConfirmedStep(nextX, confirmedY, speed, false);
			}
			appendConfirmedStep(nextX, nextY, speed, startingConfirmedMovement);
			startingConfirmedMovement = false;
		}
		if (keyboardStartup != Direction.STOP && distance == 0 && diff.has("speed") && nextSpeed == 0) {
			keyboardStartup = Direction.STOP;
			confirmedSteps.clear();
			if (Math.abs(x - confirmedX) + Math.abs(y - confirmedY) > 0.000001)
				confirmedSteps.addLast(new ConfirmedStep(confirmedX, confirmedY, 120));
		}
		final boolean stopping = nextSpeed <= 0 && (serverMovementSpeed > 0 || distance > 0);
		confirmedX = nextX;
		confirmedY = nextY;
		confirmedDirection = nextDirection;
		serverMovementSpeed = Double.isFinite(nextSpeed) && nextSpeed > 0 ? nextSpeed : 0;
		if (serverMovementSpeed > 0) lastWalkingSpeed = serverMovementSpeed;
		if (serverMovementSpeed == 0) {
			startingConfirmedMovement = true;
			if (stopping) finishConfirmedMovement(travelSpeed);
		}
		if (confirmedSteps.isEmpty()) {
			if (keyboardStartup == Direction.STOP) {
				setSpeed(0, 0);
				setDirection(confirmedDirection);
			}
		} else {
			applyConfirmedStep();
		}
		fireChange(PROP_SPEED);
	}

	/** Validate the complete route before adding any of its confirmed tiles. */
	private boolean appendConfirmedRoute(final String route, final int targetX, final int targetY, final double speed) {
		if (route.length() > 4096) return false;
		final String[] nodes = route.split(";");
		if (nodes.length < 2 || nodes.length > 65) return false;
		final List<int[]> points = new ArrayList<>();
		try {
			for (final String node : nodes) {
				final String[] coordinates = node.split(",");
				if (coordinates.length != 2) return false;
				points.add(new int[] {Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])});
			}
		} catch (final NumberFormatException invalid) {
			return false;
		}
		final int[] first = points.get(0), last = points.get(points.size() - 1);
		if (first[0] != confirmedX || first[1] != confirmedY || last[0] != targetX || last[1] != targetY) return false;
		for (int i = 1; i < points.size(); i++) {
			final int[] from = points.get(i - 1), to = points.get(i);
			if (Math.abs((long) to[0] - from[0]) + Math.abs((long) to[1] - from[1]) != 1) return false;
		}
		for (int i = 1; i < points.size(); i++) {
			final int[] point = points.get(i);
			appendConfirmedStep(point[0], point[1], speed, startingConfirmedMovement && i == 1);
		}
		return true;
	}

	private void appendConfirmedStep(final int targetX, final int targetY, final double speed, final boolean starting) {
		if (keyboardStartup != Direction.STOP && confirmedSteps.isEmpty()
				&& (targetX != confirmedX + keyboardStartup.getdx() || targetY != confirmedY + keyboardStartup.getdy()))
			cancelKeyboardStart();
		final ConfirmedStep previous = confirmedSteps.peekLast();
		final double fromX = previous == null ? x : previous.x;
		final double fromY = previous == null ? y : previous.y;
		final double distance = Math.abs(targetX - fromX) + Math.abs(targetY - fromY);
		if (distance < 0.000001) return;
		final boolean previewed = keyboardStartup != Direction.STOP && previous == null
				&& targetX == confirmedX + keyboardStartup.getdx() && targetY == confirmedY + keyboardStartup.getdy();
		// Retain the confirmed sample's full time budget. Starting from the
		// previewed fraction must not consume the history needed for a smooth run.
		final double timedDistance = previewed ? 1 : distance;
		if (keyboardStartup != Direction.STOP) keyboardStartup = Direction.STOP;
		// Keep one tile of confirmed history for fractional speeds. Otherwise
		// 1.1 would repeatedly catch the integer endpoint before the next tick.
		final boolean fractionalSpeed = Math.abs(speed - Math.rint(speed)) > 0.000001;
		final double startupBuffer = starting && previous == null && speed >= 1 && fractionalSpeed ? 300 / speed : 0;
		final double duration = timedDistance * 300 / speed + startupBuffer;
		confirmedSteps.addLast(new ConfirmedStep(targetX, targetY, duration));
	}

	/** Once stopped, no jitter reserve is needed; finish the confirmed tail promptly. */
	private void finishConfirmedMovement(final double speed) {
		double distance = 0, duration = 0, fromX = x, fromY = y;
		for (final ConfirmedStep step : confirmedSteps) {
			distance += Math.abs(step.x - fromX) + Math.abs(step.y - fromY);
			duration += step.remaining;
			fromX = step.x;
			fromY = step.y;
		}
		final double completionTime = speed > 0 ? distance * 300 / speed : 120;
		if (duration > completionTime && completionTime > 0) {
			final double scale = completionTime / duration;
			for (final ConfirmedStep step : confirmedSteps) step.remaining *= scale;
		}
	}

	private void applyConfirmedStep() {
		final ConfirmedStep step = confirmedSteps.peekFirst();
		if (step == null) {
			setSpeed(0, 0);
			setDirection(confirmedDirection);
			return;
		}
		final double remaining = Math.max(0.000001, step.remaining);
		setSpeed((step.x - x) * 300 / remaining, (step.y - y) * 300 / remaining);
		setDirection(Math.abs(step.x - x) > 0.000001
				? step.x > x ? Direction.RIGHT : Direction.LEFT
				: step.y > y ? Direction.DOWN : Direction.UP);
	}

	@Override
	protected synchronized void updateMovement(final int delta) {
		if (keyboardStartup != Direction.STOP && confirmedSteps.isEmpty()) {
			keyboardStartupTime = Math.min(10000, keyboardStartupTime + Math.max(0, delta) * lastWalkingSpeed);
			// React in the first frame, then ease toward half a tile while waiting.
			// This never predicts a second step or crosses the first tile boundary.
			final double fraction = -0.5 * Math.expm1(-keyboardStartupTime / 150.0);
			x = confirmedX + keyboardStartup.getdx() * fraction;
			y = confirmedY + keyboardStartup.getdy() * fraction;
			onPosition(x, y);
			return;
		}
		double remaining = Math.max(0, delta);
		while (remaining > 0 && !confirmedSteps.isEmpty()) {
			final ConfirmedStep step = confirmedSteps.peekFirst();
			final double elapsed = Math.min(remaining, step.remaining);
			final double fraction = elapsed / step.remaining;
			x += (step.x - x) * fraction;
			y += (step.y - y) * fraction;
			step.remaining -= elapsed;
			remaining -= elapsed;
			if (step.remaining < 0.000001) {
				x = step.x;
				y = step.y;
				confirmedSteps.removeFirst();
			}
			applyConfirmedStep();
			if (confirmedSteps.isEmpty()) fireChange(PROP_SPEED);
			onPosition(x, y);
		}
	}

	private static final class ConfirmedStep {
		final int x, y;
		double remaining;
		ConfirmedStep(final int x, final int y, final double duration) {
			this.x = x;
			this.y = y;
			remaining = duration;
		}
	}

	/**
	 * Stop the user's movement.
	 */
	public void stopMovement() {
		cancelKeyboardStart();
		final RPAction stopAction = new RPAction();

		stopAction.put(TYPE, WALK);
		stopAction.put(MODE, "stop");

		ClientSingletonRepository.getClientFramework().send(stopAction);
	}

	/**
	 * Interface to separate the no user special case from the normal
	 * situation.
	 */
	private static interface StaticUserProxy {
		String getName();
		int getPlayerLevel();
		String getServerRelease();
		User getUser();
		boolean hasBuddy(String buddy);
		boolean isAdmin();
		boolean isIgnoring(String name);
		double squareDistanceTo(double x, double y);
	}

	private static class NormalUserProxy implements StaticUserProxy {
		private final User user;

		NormalUserProxy(User user) {
			this.user = user;
		}

		@Override
		public String getName() {
			return user.getName();
		}

		@Override
		public int getPlayerLevel() {
			return user.getLevel();
		}

		@Override
		public String getServerRelease() {
			return user.rpObject.get("release");
		}

		@Override
		public User getUser() {
			return user;
		}

		@Override
		public boolean hasBuddy(String buddy) {
			return user.rpObject.has("buddies", buddy);
		}

		@Override
		public boolean isAdmin() {
			return ((user.rpObject != null)
					&& user.rpObject.has("adminlevel")
					&& (user.rpObject.getInt("adminlevel") >= 6));
		}

		@Override
		public double squareDistanceTo(double x, double y) {
			double xDiff = user.getX() - x;
			double yDiff = user.getY() - y;
			return xDiff * xDiff + yDiff * yDiff;
		}

		@Override
		public boolean isIgnoring(String name) {
			return user.ignore.contains(name);
		}
	}

	private static class NoUserProxy implements StaticUserProxy {
		@Override
		public String getName() {
			return null;
		}

		@Override
		public int getPlayerLevel() {
			return 0;
		}

		@Override
		public String getServerRelease() {
			return null;
		}

		@Override
		public User getUser() {
			return null;
		}

		@Override
		public boolean hasBuddy(String buddy) {
			return false;
		}

		@Override
		public boolean isAdmin() {
			return false;
		}

		@Override
		public boolean isIgnoring(String name) {
			return false;
		}

		@Override
		public double squareDistanceTo(double x, double y) {
			return Double.POSITIVE_INFINITY;
		}
	}
}
