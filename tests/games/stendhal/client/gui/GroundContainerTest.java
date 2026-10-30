package games.stendhal.client.gui;

import java.awt.Point;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.client.entity.Item;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPObject;

public class GroundContainerTest {
    @BeforeClass public static void initializeClasses() { MockStendlRPWorld.get(); }

    @Test public void detachedItemCannotGenerateADropAction() {
        final RPObject object = new RPObject();
        object.setRPClass("item"); object.put("id", 42); object.put("name", "sztylecik");
        final Item item = new Item(); item.initialize(object);
        // No screen/client is needed: an invalid source must return before using either.
        new GroundContainer(null, null, null).dropEntity(item, -1, new Point(0, 0));
    }
}
