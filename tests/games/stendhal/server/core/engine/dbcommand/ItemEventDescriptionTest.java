package games.stendhal.server.core.engine.dbcommand;

import static org.junit.Assert.*;

import java.util.ConcurrentModificationException;
import org.junit.Test;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Corpse;
import marauroa.common.game.RPObject;

public class ItemEventDescriptionTest {

	private final RPEntity player = new RPEntity() {
		@Override public void logic() { }
		@Override protected void dropItemsOn(Corpse corpse) { }
		@Override public String getName() { return "History tester"; }
		@Override public String toString() {
			throw new ConcurrentModificationException("Inventory is changing on the game thread");
		}
	};

	@Test public void simpleEventDescriptionDoesNotTraverseLiveInventory() {
		String description = new LogSimpleItemEventCommand(new RPObject(), player, "equip",
				null, null, null, null).toString();
		assertTrue(description.contains("player=History tester"));
		assertTrue(description.contains("event=equip"));
	}

	@Test public void mergeDescriptionDoesNotTraverseLiveInventory() {
		String description = new LogMergeItemEventCommand(player, new RPObject(), new RPObject()).toString();
		assertTrue(description.contains("player=History tester"));
	}

	@Test public void splitDescriptionDoesNotTraverseLiveInventory() {
		String description = new LogSplitItemEventCommand(player, new RPObject(), new RPObject()).toString();
		assertTrue(description.contains("player=History tester"));
	}

	@Test public void eventsWithoutPlayerStillHaveSafeDescriptions() {
		assertTrue(new LogSimpleItemEventCommand(new RPObject(), null, "destroy",
				null, null, null, null).toString().contains("player=null"));
		assertTrue(new LogMergeItemEventCommand(null, new RPObject(), new RPObject()).toString().contains("player=null"));
		assertTrue(new LogSplitItemEventCommand(null, new RPObject(), new RPObject()).toString().contains("player=null"));
	}
}
