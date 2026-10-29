package games.stendhal.client.events;

import games.stendhal.client.entity.Entity;
import games.stendhal.client.gui.ProductionWindow;

final class ProductionEvent extends Event<Entity> {
    @Override public void execute() { ProductionWindow.show(event); }
}
