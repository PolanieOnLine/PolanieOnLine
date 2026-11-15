/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;

/** A server-selected destination in the player's current zone. */
public final class NavigationTarget {
	public static final String UNLOCKED_ATTRIBUTE = "compass_unlocked";
	public static final String HIDDEN_ATTRIBUTE = "compass_hidden";
	public static final String TARGETS_ATTRIBUTE = "compass_targets";
	public static final String AVAILABLE_ATTRIBUTE = "compass_available";
	public static final String TRACKED_ATTRIBUTE = "compass_tracked";
	public static final String TRACKED_TITLE_ATTRIBUTE = "compass_tracked_title";
	public final String id;
	public final double x;
	public final double y;
	public final boolean main;
	public final String label;
	public final int remainingSteps;

	private NavigationTarget(String id, double x, double y, boolean main, String label, int remainingSteps) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.main = main;
		this.label = label;
		this.remainingSteps = remainingSteps;
	}

	/** Malformed entries are ignored independently of other destinations. */
	public static NavigationTarget parse(String id, String wire) {
		if (id == null || id.isEmpty() || wire == null) {
			return null;
		}
		String[] fields = wire.split("\t", -1);
		if ((fields.length != 4 && fields.length != 5)
				|| fields[3].trim().isEmpty()
				|| !("main".equals(fields[2]) || "side".equals(fields[2]))) {
			return null;
		}
		try {
			double x = Double.parseDouble(fields[0]);
			double y = Double.parseDouble(fields[1]);
			if (!Double.isFinite(x) || !Double.isFinite(y)) {
				return null;
			}
			if (fields.length == 5 && !fields[4].matches("\\d+")) return null;
			int remaining = fields.length == 5 ? Integer.parseInt(fields[4]) : -1;
			if (fields.length == 5 && remaining < 0) return null;
			return new NavigationTarget(id, x, y, "main".equals(fields[2]), fields[3], remaining);
		} catch (NumberFormatException exception) {
			return null;
		}
	}

	public double distance(double playerX, double playerY) {
		return remainingSteps >= 0 ? remainingSteps : Math.hypot(x - playerX, y - playerY);
	}

	public String instruction(double playerX, double playerY) {
		if (x == playerX && y == playerY) return remainingSteps > 0 ? "Idź dalej" : "Jesteś na miejscu";
		String[] directions = {"Idź na północ", "Idź na północny wschód", "Idź na wschód",
				"Idź na południowy wschód", "Idź na południe", "Idź na południowy zachód",
				"Idź na zachód", "Idź na północny zachód"};
		int octant = Math.floorMod((int) Math.round(Math.atan2(x - playerX, playerY - y) / (Math.PI / 4)), 8);
		return directions[octant];
	}

	/** South is both ends, west is 1/4, north is 1/2 and east is 3/4. */
	public double position(double playerX, double playerY) {
		if (x == playerX && y == playerY) {
			return 0.5;
		}
		return 0.5 + Math.atan2(x - playerX, playerY - y) / (Math.PI * 2);
	}
}
