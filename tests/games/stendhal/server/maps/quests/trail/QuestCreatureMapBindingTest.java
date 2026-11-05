package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.core.rule.defaultruleset.DefaultEntityManager;
import utilities.QuestHelper;

public class QuestCreatureMapBindingTest {
    @BeforeClass public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }
    @Test public void ordinaryMapMarkersKeepTheirOriginalMonsters() {
        EntityManager manager = SingletonRepository.getEntityManager();
        assertEquals("duergar", manager.getCreature("../../tileset/logic/creature/dwarf_duergar.png", 6).getName());
        assertEquals("bandyta", manager.getCreature("../../tileset/logic/creature/human_evil.png", 1).getName());
        assertEquals("łowca", manager.getCreature("../../tileset/logic/creature/human_evil.png", 2).getName());
        assertEquals("kamienny golem", manager.getCreature("../../tileset/logic/creature/golem.png", 0).getName());
    }
    @Test public void questMonstersRemainCreatableByNameWithoutMapMarkers() {
        EntityManager manager = SingletonRepository.getEntityManager();
        for (String name : new String[]{"strażnik składu", "człowiek Radana", "strażnik więźnia", "zbrojny Radana", "łącznik duergarów", "wspólnik Radana", "wysłannik duergarów", "dowódca duergarów", "strażnik warowni"}) {
            DefaultCreature definition = manager.getDefaultCreature(name);
            assertNotNull(name, definition);
            assertEquals(name, "-1", definition.getTileId());
            assertNotNull(name, manager.getCreature(name));
            assertTrue(name, definition.toXML().contains("tileid=\"-1\""));
        }
    }
    @Test public void explicitlyUnmappingAnExistingDefinitionClearsItsOldMarker() {
        DefaultEntityManager manager = new DefaultEntityManager();
        String marker = "../../tileset/logic/creature/binding-probe.png";
        DefaultCreature definition = new DefaultCreature("human", "bandit", "binding probe", marker + ":0");
        assertTrue(manager.addCreature(definition));
        assertTrue(manager.isCreature(marker, 0));
        definition.setTileId(DefaultCreature.NO_TILE_ID);
        assertTrue(manager.addCreature(definition));
        assertFalse(manager.isCreature(marker, 0));
        assertSame(definition, manager.getDefaultCreature("binding probe"));
        definition.setTileId(marker + ":0");
        assertTrue(manager.addCreature(definition));
        DefaultCreature renamed = new DefaultCreature("human", "bandit", "BINDING PROBE", DefaultCreature.NO_TILE_ID);
        assertTrue(manager.addCreature(renamed));
        assertFalse(manager.isCreature(marker, 0));
    }

}
