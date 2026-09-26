package games.stendhal.server.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.RPClassGenerator;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

public class NpcShopEventSerializationTest {
    @BeforeClass
    public static void setUpClass() {
        new RPClassGenerator().createRPClassesWithoutBaking();
    }

    @Test
    public void playerCanSendNpcShopEvent() throws Exception {
        final RPClass playerClass = RPClass.getRPClass("player");
        assertNotNull(playerClass.getDefinition(
                DefinitionClass.RPEVENT, Events.NPC_SHOP));

        final RPObject player = new RPObject();
        player.setRPClass("player");
        final RPEvent shop = new RPEvent(Events.NPC_SHOP);
        shop.put("phase", "open");
        shop.put("npc_id", 1);
        shop.put("npc_name", "Test");
        player.addEvent(shop);

        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        player.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);

        final RPObject restored = new RPObject();
        restored.readObject(new InputSerializer(
                new ByteArrayInputStream(bytes.toByteArray())));
        assertEquals(1, restored.events().size());
        assertEquals(Events.NPC_SHOP, restored.events().get(0).getName());
    }
}
