package games.stendhal.server.actions.admin;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.json.simple.JSONValue;
import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPClass;
import marauroa.common.game.Definition;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import marauroa.common.game.RPObject;
import marauroa.common.game.DetailLevel;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.events.AdminInspectEvent;
import games.stendhal.server.entity.npc.SpeakerNPC;
import utilities.QuestHelper;
import utilities.PlayerTestHelper;

public class AdminInspectWindowTest {
    @BeforeClass public static void initialize() throws Exception { QuestHelper.setUpBeforeClass(); }
    private RPAction request(Player target) {
        RPAction action=new RPAction(); action.put("type","inspect");
        action.put("target","#"+target.getID().getObjectID()); action.put("visual","1"); return action;
    }
    @Test public void regularPlayerCannotReceiveInspectionData() {
        StendhalRPZone zone=new StendhalRPZone("inspect-forbidden");
        Player actor=PlayerTestHelper.createPlayer("viewer"), target=PlayerTestHelper.createPlayer("target");
        zone.add(actor);zone.add(target);new InspectAction().perform(actor,request(target));
        assertFalse(actor.events().stream().anyMatch(event->event.getName().equals(Events.ADMIN_INSPECT)));
    }
    @Test public void privateSnapshotIncludesRawQuestKeysAndPreservesRequestPath() {
        StendhalRPZone zone=new StendhalRPZone("inspect-authorized");
        Player actor=PlayerTestHelper.createPlayer("administrator"), target=PlayerTestHelper.createPlayer("Zed Example");
        actor.setAdminLevel(6);zone.add(actor);zone.add(target);
        target.setQuest("susi","2026");target.setQuest("plink_toy","done;123;1");
        RPAction request=request(target);new InspectAction().perform(actor,request);
        RPEvent event=actor.events().stream().filter(e->e.getName().equals(Events.ADMIN_INSPECT)).findFirst().get();
        assertEquals("open",event.get("phase"));assertNotNull(JSONValue.parse(event.get("payload")));
        assertTrue(event.get("payload").contains("susi"));assertTrue(event.get("payload").contains("plink_toy"));
        assertFalse(target.events().stream().anyMatch(e->e.getName().equals(Events.ADMIN_INSPECT)));
        assertEquals(Definition.PRIVATE, RPClass.getRPClass("player").getDefinition(Definition.DefinitionClass.RPEVENT,Events.ADMIN_INSPECT).getFlags());
        request.put("inspection_window",event.get("window_id"));new InspectAction().perform(actor,request);
        assertEquals("refresh",actor.events().get(actor.events().size()-1).get("phase"));
    }
    @Test public void localObjectRefreshIsRejectedAfterTheViewerChangesZone() {
        StendhalRPZone first=new StendhalRPZone("inspect-first"),second=new StendhalRPZone("inspect-second");
        Player admin=PlayerTestHelper.createPlayer("moving-administrator");admin.setAdminLevel(6);first.add(admin);
        RPAction action=new RPAction();action.put("type","inspect");action.put("target","#1");action.put("visual","1");
        action.put("inspection_window","old-window");action.put("inspection_zone",first.getName());first.remove(admin);second.add(admin);
        action.put("target","#"+admin.getID().getObjectID());
        new InspectAction().perform(admin,action);
        assertFalse(admin.events().stream().anyMatch(e->e.getName().equals(Events.ADMIN_INSPECT)));
    }

    @Test public void itemAndNpcSnapshotsUseTheSameStructuredFormat() {
        Item item=SingletonRepository.getEntityManager().getItem("miecz");
        assertTrue(AdminInspectionSnapshot.create(item).contains("Atrybuty"));
        SpeakerNPC npc=new SpeakerNPC("Inspect Merchant");
        assertTrue(AdminInspectionSnapshot.create(npc).contains("Stan rozmowy"));
    }
    @Test public void eventSerializesWithoutChangingExistingPlayerFieldCodes() throws Exception {
        assertEquals(143,RPClass.getRPClass("player").getCode(Definition.DefinitionClass.ATTRIBUTE,"dead"));
        assertEquals(225,RPClass.getRPClass("player").getCode(Definition.DefinitionClass.RPEVENT,Events.NPC_SHOP));
        Player actor=PlayerTestHelper.createPlayer("inspect-save");
        actor.addEvent(new AdminInspectEvent("{\"sections\":[]}","{}","window",false));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();actor.writeObject(new OutputSerializer(bytes),DetailLevel.FULL);
        RPObject restored=new RPObject();restored.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
        assertTrue(restored.events().stream().anyMatch(e->Events.ADMIN_INSPECT.equals(e.getName())&&e.get("payload").contains("sections")));
    }

}
