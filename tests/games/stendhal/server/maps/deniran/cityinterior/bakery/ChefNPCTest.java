package games.stendhal.server.maps.deniran.cityinterior.bakery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

public class ChefNPCTest extends ZonePlayerAndNPCTestImpl {

    private static final String ZONE_NAME = "testzone";

    private static final String QUEST = "pizzadelivery";

    public ChefNPCTest() {
        setNpcNames("Preston");
        setZoneForPlayer(ZONE_NAME);
        addZoneConfigurator(new ChefNPC(), ZONE_NAME);

    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        QuestHelper.setUpBeforeClass();
        setupZone(ZONE_NAME);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        player.removeQuest(QUEST);
    }

    @Test
    public void testJob() {
        final SpeakerNPC npc = getNPC("Preston");
        final Engine en = npc.getEngine();
        en.step(player, "hi");
        assertTrue(npc.isTalking());
        assertEquals("Witamy w Denirańskiej piekarni.", getReply(npc));

        en.step(player, "job");
        assertTrue(npc.isTalking());
        assertEquals("Otworzyłem piekarnie w Deniran.", getReply(npc));

        en.step(player, "Do widzenia.");
        assertFalse(npc.isTalking());

    }

}
