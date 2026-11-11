/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;

/** A server-selected destination in the player's current zone. */
public final class NavigationTarget {
  public static final String UNLOCKED_ATTRIBUTE = "compass_unlocked";
  public static final String TARGETS_ATTRIBUTE = "compass_targets";
  public final String id;
  public final double x;
  public final double y;
  public final boolean main;
  public final String label;

  private NavigationTarget(String id, double x, double y, boolean main, String label) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.main = main;
    this.label = label;
  }

  /** Malformed entries are ignored independently of other destinations. */
  public static NavigationTarget parse(String id, String wire) {
    if (id == null || id.isEmpty() || wire == null) {
      return null;
    }
    String[] fields = wire.split("\t", -1);
    if (fields.length != 4
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
      return new NavigationTarget(id, x, y, "main".equals(fields[2]), fields[3]);
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  public double distance(double playerX, double playerY) {
    return Math.hypot(x - playerX, y - playerY);
  }

  /** South is both ends, west is 1/4, north is 1/2 and east is 3/4. */
  public double position(double playerX, double playerY) {
    if (x == playerX && y == playerY) {
      return 0.5;
    }
    return 0.5 + Math.atan2(x - playerX, playerY - y) / (Math.PI * 2);
  }
}
