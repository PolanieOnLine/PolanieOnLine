package games.stendhal.client.events;

import games.stendhal.client.entity.Entity;
import games.stendhal.client.gui.AdventCalendarDialog;

final class AdventCalendarEvent extends Event<Entity> {
	@Override
	public void execute() {
		AdventCalendarDialog.show(event);
	}
}
