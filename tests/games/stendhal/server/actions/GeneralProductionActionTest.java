package games.stendhal.server.actions;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.MultiProducerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.MultiProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.ProductionRecipe;
import games.stendhal.server.entity.mapstuff.useable.ProductionWorksite;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import utilities.PlayerTestHelper;

/** Exercises the same recipes through the window, including batches and quest gates. */
public class GeneralProductionActionTest {
    private static int sequence;
    private Player player;
    private SpeakerNPC npc;
    private int request;

    @Before public void setUp() {
        StendhalRPZone zone = new StendhalRPZone("general_production_" + ++sequence, 32, 32);
        MockStendlRPWorld.get().addRPZone(zone);
        npc = new SpeakerNPC("Window producer " + sequence); npc.setPosition(5, 5); zone.add(npc);
        player = PlayerTestHelper.createPlayer("window customer " + sequence); player.setPosition(5, 6); zone.add(player);
    }

    private ProducerBehaviour single(String slot, String name, int batch, int limit, int wait, String quest) {
        Map<String, Integer> resources = new HashMap<>(); resources.put("polano", 2);
        ProducerBehaviour behaviour = new ProducerBehaviour(slot, Arrays.asList("make"), name, batch, resources, 30);
        behaviour.setUnitsPerTime(limit); behaviour.setWaitingTime(wait);
        new ProducerAdder().addProducer(npc, behaviour, quest, null); return behaviour;
    }

    private RPEvent send(String command, int amount, String recipe, RPEvent previous) {
        ProductionRecipe.ensureWorksite(npc);
        ProductionWorksite worksite = null;
        for (Object object : npc.getZone()) if (object instanceof ProductionWorksite && ((ProductionWorksite) object).getProducer() == npc) worksite = (ProductionWorksite) object;
        assertNotNull("Producer has a separate clickable worksite", worksite);
        player.setPosition(worksite.getX(), worksite.getY() + 1);
        RPAction action = new RPAction(); action.put("npc_id", worksite.getID().getObjectID()); action.put("command", command);
        action.put("amount", amount); action.put("request_id", "request-" + ++request);
        if (recipe != null) action.put("recipe", recipe);
        if (previous != null) action.put("window_id", previous.get("window_id"));
        new ProductionAction().onAction(player, action);
        RPEvent latest = null;
        for (RPEvent event : player.events()) if ("production_event".equals(event.getName())) latest = event;
        assertNotNull("Producer worksite opens a production window", latest); return latest;
    }

    @Test public void npcListsMultipleRecipesAndKeepsEachOrdersExistingSlot() {
        single("window_iron", "sztabka żelaza", 1, 1000, 0, null);
        single("window_gold", "sztabka złota", 1, 1000, 0, null);
        PlayerTestHelper.equipWithStackableItem(player, "polano", 20);
        RPEvent opened = send("open", 1, null, null);
        assertEquals(2, opened.getList("recipe_ids").size());
        int index = opened.getList("recipe_names").indexOf("sztabka złota");
        String gold = opened.getList("recipe_ids").get(index);
        RPEvent preview = send("refresh", 3, gold, opened);
        assertEquals("sztabka złota", preview.get("name"));
        send("start", 3, gold, opened);
        assertTrue(player.getQuest("window_gold").startsWith("3;sztabka złota;"));
        assertFalse(player.hasQuest("window_iron"));
        assertEquals(14, player.getNumberOfEquipped("polano"));
    }

    @Test public void batchesChargeResourcesPerBatchAndRejectFractions() {
        single("window_arrows", "strzała", 5, 0, 0, null);
        PlayerTestHelper.equipWithStackableItem(player, "polano", 10);
        RPEvent opened = send("open", 1, null, null);
        assertEquals(5, opened.getInt("amount_step"));
        assertEquals(5, opened.getInt("amount"));
        assertEquals(30, opened.getInt("duration"));
        assertEquals("2", opened.getList("material_values").get(0));
        send("start", 6, null, opened);
        assertFalse(player.hasQuest("window_arrows"));
        send("start", 10, null, opened);
        assertEquals(6, player.getNumberOfEquipped("polano"));
    }

    @Test public void questRequirementAndCooldownCannotBeBypassedByTheWindow() {
        single("window_daily", "zwój kalavan", 1, 7, 24, "window_unlock");
        PlayerTestHelper.equipWithStackableItem(player, "polano", 10);
        RPEvent opened = send("open", 1, null, null);
        assertEquals(0, opened.getInt("can_start"));
        send("start", 1, null, opened); assertFalse(player.hasQuest("window_daily"));
        player.setQuest("window_unlock", "done");
        player.setQuest("window_daily", "done;1;" + System.currentTimeMillis());
        RPEvent preview = send("refresh", 1, null, opened);
        assertEquals(0, preview.getInt("can_start"));
        send("start", 1, null, opened);
        assertTrue(player.getQuest("window_daily").startsWith("done;"));
        assertEquals(10, player.getNumberOfEquipped("polano"));
    }

    @Test public void staleDialogueConfirmationAndForgedRecipeCannotReplaceAWindowOrder() {
        single("window_guarded", "sztabka żelaza", 1, 1000, 0, null);
        PlayerTestHelper.equipWithStackableItem(player, "polano", 20);
        npc.getEngine().step(player, "hi"); npc.getEngine().step(player, "make 2 sztabka żelaza");
        RPEvent opened = send("open", 1, null, null);
        send("start", 3, "other_npc|sztabka złota", opened);
        assertFalse(player.hasQuest("window_guarded"));
        send("start", 3, null, opened); String order = player.getQuest("window_guarded");
        npc.getEngine().step(player, "yes");
        assertEquals(order, player.getQuest("window_guarded"));
        assertEquals(14, player.getNumberOfEquipped("polano"));
    }

    @Test public void worksiteAppearsAfterNpcPlacementAndIsRemovedWithItsOwner() {
        SpeakerNPC early = new SpeakerNPC("Early producer " + sequence) {
            @Override protected void createDialog() {
                Map<String, Integer> costs = new HashMap<>(); costs.put("polano", 1);
                new ProducerAdder().addProducer(this, new ProducerBehaviour("early_order", Arrays.asList("make"), "chleb", costs, 30), null);
            }
        };
        early.setPosition(15, 15); StendhalRPZone zone = npc.getZone(); zone.add(early);
        ProductionRecipe.ensureWorksite(early); ProductionRecipe.ensureWorksite(early);
        int count = 0; ProductionWorksite found = null;
        for (Object object : zone) if (object instanceof ProductionWorksite && ((ProductionWorksite) object).getProducer() == early) { count++; found = (ProductionWorksite) object; }
        assertEquals(1, count); assertEquals(2, (int) found.getWidth()); assertEquals(1, (int) found.getHeight());
        assertEquals("production_table_cooking", found.get("name"));
        zone.remove(early); assertFalse(zone.has(found.getID()));
    }

    @Test public void zoneUnloadCleansUpWorksitesWithoutInvalidatingItsEntityIteration() {
        single("window_unload", "sztabka żelaza", 1, 1000, 0, null);
        ProductionRecipe.ensureWorksite(npc);
        npc.getZone().onRemoved();
        // onRemoved iterates active entities, whose owned worksites may be removed.
    }

    @Test public void multiProducerRestoresAndCollectsTheActualOrderedProductAfterSelectionChanges() {
        HashMap<String, Map<String, Integer>> costs = new HashMap<>();
        HashMap<String, Integer> times = new HashMap<>(); HashMap<String, Boolean> bound = new HashMap<>();
        for (String name : Arrays.asList("sztabka żelaza", "sztabka złota")) {
            Map<String, Integer> resources = new HashMap<>(); resources.put("polano", 1);
            costs.put(name, resources); times.put(name, 30); bound.put(name, false);
        }
        new MultiProducerAdder().addMultiProducer(npc, new MultiProducerBehaviour("window_multi", Arrays.asList("make"),
                new HashSet<>(costs.keySet()), costs, times, bound), null);
        player.setQuest("window_multi", "4;sztabka złota;" + (System.currentTimeMillis() - 121000));
        RPEvent opened = send("open", 1, null, null);
        assertEquals("sztabka złota", opened.get("order_name"));
        assertEquals("ready", opened.get("order_state"));
        String iron = opened.getList("recipe_ids").get(opened.getList("recipe_names").indexOf("sztabka żelaza"));
        RPEvent preview = send("refresh", 1, iron, opened);
        assertEquals("sztabka złota", preview.get("order_name"));
        send("collect", 1, iron, opened);
        assertEquals(4, player.getNumberOfEquipped("sztabka złota"));
        assertEquals(0, player.getNumberOfEquipped("sztabka żelaza"));
        assertEquals("done", player.getQuest("window_multi"));
    }
}
