package games.stendhal.client.gui;

import static org.junit.Assert.*;
import java.lang.reflect.Method;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.client.entity.Item;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.maps.MockStendlRPWorld;

public class NpcShopWindowTest {
    @BeforeClass public static void initializeClasses() { MockStendlRPWorld.get(); }
    @Test public void catalogueSnapshotRetainsStructuredStatsInTheItemSchema() throws Exception {
        Method parse=NpcShopWindow.class.getDeclaredMethod("parseSnapshot",String.class);
        parse.setAccessible(true);
        Item item=(Item)parse.invoke(null,"{\"name\":\"miecz\",\"rarity_id\":\"epic\",\"tooltip_stats\":{\"category\":\"weapon\",\"damage_min\":\"12\"}}");
        assertEquals("epic",item.getRPObject().get("rarity_id"));
        assertEquals("12",item.getRPObject().getMap(ItemTooltip.ATTRIBUTE).get("damage_min"));
    }
}
