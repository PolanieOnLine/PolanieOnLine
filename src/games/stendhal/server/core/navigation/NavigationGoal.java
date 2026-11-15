/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.navigation;

/** A read-only authored objective. Coordinates always belong to zone. */
public final class NavigationGoal {
	public final String zone, label, stage, identity;
	public final int x, y;
	public final int remainingSteps;

	public NavigationGoal(String zone, int x, int y, String label, String stage) {
		this(zone, x, y, label, stage, -1);
	}

	public NavigationGoal(String zone, int x, int y, String label, String stage, int remainingSteps) {
		this(zone, x, y, label, stage, remainingSteps, x + "," + y);
	}

	/** Moving objectives retain their identity when their coordinates change. */
	public NavigationGoal(String zone, int x, int y, String label, String stage, String identity) {
		this(zone, x, y, label, stage, -1, identity);
	}

	private NavigationGoal(String zone, int x, int y, String label, String stage, int remainingSteps, String identity) {
		this.zone = zone;
		this.x = x;
		this.y = y;
		this.label = label;
		this.stage = stage;
		this.remainingSteps = remainingSteps;
		this.identity = identity;
	}
}
