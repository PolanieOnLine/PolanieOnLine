package games.stendhal.client.events;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.gui.AdminInspectWindow;
final class AdminInspectEvent extends Event<Entity> {
    @Override public void execute(){AdminInspectWindow.show(event);}
}
