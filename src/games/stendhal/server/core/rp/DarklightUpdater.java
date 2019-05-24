package games.stendhal.server.core.rp;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.ZoneAttributes;
import games.stendhal.server.core.events.TurnListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for dark light colored zones.
 * 
 * @author ??? but thank you!
 */
public class DarklightUpdater implements TurnListener {
	/** Time between checking if the color should be changed. Seconds. */
	private static final int CHECK_INTERVAL = 61;
	/** Singleton instance. */
	private static final DarklightUpdater instance = new DarklightUpdater();
	/** Color corresponding to the current time. */
	Integer currentColor;

	/** Managed zones, and their attributes */
	private final List<ZoneAttributes> zones = new ArrayList<ZoneAttributes>();

	/**
	 * Create a new Darklight instance. Do not use this.
	 */
	private DarklightUpdater() {
		onTurnReached(0);
	}

	/**
	 * Get the Darklight instance.
	 *
	 * @return singleton instance
	 */
	public static DarklightUpdater get() {
		return instance;
	}

	/**
	 * Make a zone color managed by the darklight colorer.
	 *
	 * @param attr attributes of the zone
	 */
	public void manageAttributes(ZoneAttributes attr) {
		zones.add(attr);
		// Set the initial values
		attr.put("color_method", "multiply");
		setZoneColor(attr, currentColor);
	}

	@Override
	public void onTurnReached(int currentTurn) {
		updateDarktimeColor();
		SingletonRepository.getTurnNotifier().notifyInSeconds(CHECK_INTERVAL, this);
	}

	/**
	 * Update the zone color according to the hour.
	 */
	private void updateDarktimeColor() {
		setZoneColors(DarklightPhase.current().getColor());
	}

	/**
	 * Set the current darklight color. Notifies all the managed zones if
	 * the color has changed.
	 *
	 * @param color
	 */
	private void setZoneColors(Integer color) {
		if (((color == null) && (currentColor != null))
				|| ((color != null) && !color.equals(currentColor))) {
			for (ZoneAttributes attr : zones) {
				setZoneColor(attr, color);
				if (color != null) {
					attr.put("blend_method", "bleach");
				} else {
					attr.remove("blend_method");
				}
			}
		}
		currentColor = color;
	}

	/**
	 * Set the color of a zone. Sets the blend mode of the effect layers to
	 * bleach, if needed. Notifies all the players with a recent enough
	 * client.
	 *
	 * @param attr attributes of the zone
	 * @param color new color value
	 */
	private void setZoneColor(ZoneAttributes attr, Integer color) {
		if (color == null) {
			attr.remove("color");
			attr.remove("blend_method");
		} else {
			attr.put("color", color.toString());
			attr.put("blend_method", "bleach");
		}
		// Notify resident players about the changed color
		attr.getZone().notifyOnlinePlayers();
	}
}

