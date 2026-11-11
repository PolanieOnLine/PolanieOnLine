/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.navigation;

/** A read-only authored objective. Coordinates always belong to zone. */
public final class NavigationGoal {
  public final String zone, label, stage;
  public final int x, y;

  public NavigationGoal(String zone, int x, int y, String label, String stage) {
    this.zone = zone;
    this.x = x;
    this.y = y;
    this.label = label;
    this.stage = stage;
  }
}
