package games.stendhal.client.events;

import games.stendhal.client.entity.Entity;
import games.stendhal.client.gui.imageviewer.ItemRecipeListImageViewerEvent;

class ShowItemRecipeListEvent extends Event<Entity> {
	/**
	 * executes the event
	 */
	@Override
	public void execute() {
		new ItemRecipeListImageViewerEvent(event).view();
	}
}
