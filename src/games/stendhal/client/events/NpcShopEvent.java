package games.stendhal.client.events;

import games.stendhal.client.entity.Entity;
import games.stendhal.client.gui.NpcShopWindow;

/** Otwiera lub odswieza okno sklepu w kliencie Java. */
final class NpcShopEvent extends Event<Entity> {
    @Override
    public void execute() {
        NpcShopWindow.show(event);
    }
}
