package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.core.rule.defaultruleset.DefaultEntityManager;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.impl.DropItem;
import marauroa.common.game.RPSlot;
import utilities.QuestHelper;

public class QuestCreatureMapBindingTest {
	private static final String[][] VARIANTS = {
			{ "strażnik składu", "bandyta" }, { "człowiek Radana", "bandyta" },
			{ "strażnik więźnia", "bandyta" }, { "zbrojny Radana", "bandyta" },
			{ "wspólnik Radana", "bandyta" },
			{ "wartownik buntowników", "rycerz karmazynowy" },
			{ "zarządca Radomir", "czarny rycerz" },
			{ "rabuś wybrzeża", "pirat" },
			{ "herszt nadbrzeżnych rabusiów", "kapitan piratów" },
			{ "niszczyciel sygnałów", "pirat" },
			{ "strażnik milczącego dzwonu", "archanioł" },
			{ "echo strażnika", "archanioł" },
			{ "łącznik duergarów", "duergar" },
			{ "wysłannik duergarów", "duergar elitarny" },
			{ "dowódca duergarów", "duergar bohater" },
			{ "żywiołak solanki", "żywioł wody" },
			{ "strażnik warowni", "kamienny golem" },
			{ "strażnik łowców jaj", "bandyta" },
			{ "herszt łowców jaj", "barbarzyńca lider" },
			{ "solny szlam", "zielony glut" }, { "poległy wartownik", "duch" },
			{ "strażnik dawnej chorągwi", "duch" } };

	@BeforeClass
	public static void init() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Test
	public void ordinaryMapMarkersKeepTheirOriginalMonsters() {
		EntityManager manager = SingletonRepository.getEntityManager();
		assertEquals("duergar",
				manager.getCreature(
						"../../tileset/logic/creature/dwarf_duergar.png",
						6).getName());
		assertEquals("bandyta",
				manager.getCreature(
						"../../tileset/logic/creature/human_evil.png",
						1).getName());
		assertEquals("łowca",
				manager.getCreature(
						"../../tileset/logic/creature/human_evil.png",
						2).getName());
		assertEquals("kamienny golem",
				manager.getCreature("../../tileset/logic/creature/golem.png",
						0).getName());
	}

	@Test
	public void questVariantsAreAbsentFromTheGlobalCreatureRegistry() {
		EntityManager manager = SingletonRepository.getEntityManager();
		for (String[] variant : VARIANTS) {
			String name = variant[0];
			assertNull(name, manager.getDefaultCreature(name));
			Creature creature = TrailCreatureVariants.create(name);
			assertEquals(name, creature.getName());
			assertEquals(name, creature.getNewInstance().getName());
		}
	}

	@Test
	public void customizingAnEncounterLeavesNativeTemplatesAndOtherInstancesUntouched() {
		EntityManager manager = SingletonRepository.getEntityManager();
		for (String[] variant : VARIANTS) {
			DefaultCreature nativeDefinition = manager.getDefaultCreature(
					variant[1]);
			Creature nativeBefore = manager.getCreature(variant[1]);
			Map<String, String> profiles = new LinkedHashMap<>(
					nativeDefinition.getAiProfiles());
			List<DropItem> drops = new ArrayList<>(
					nativeDefinition.getDropItems());
			Creature first = TrailCreatureVariants.create(variant[0]);
			Creature second = TrailCreatureVariants.create(variant[0]);
			assertEquals(nativeBefore.get("class"), first.get("class"));
			assertEquals(nativeBefore.get("subclass"), first.get("subclass"));
			assertTrue(first.getAIProfiles().containsKey("no_elite"));
			assertTrue(first.getAllStatusAttackers().isEmpty());
			for (RPSlot slot : first.slots()) {
				assertEquals(variant[0] + ": inherited equipment", 0, slot.size());
			}
			int hp = second.getBaseHP();
			first.initHP(1);
			first.setName("changed encounter");
			first.getAIProfiles().clear();
			first.clearDropItemList();
			first.addDropItem("money", 100.0, 10, 10);
			assertEquals(hp, second.getBaseHP());
			assertEquals(variant[0], second.getName());
			assertTrue(second.getAIProfiles().containsKey("no_elite"));
			assertEquals(profiles, nativeDefinition.getAiProfiles());
			assertEquals(drops, nativeDefinition.getDropItems());
			Creature nativeAfter = manager.getCreature(variant[1]);
			assertEquals(nativeBefore.getName(), nativeAfter.getName());
			assertEquals(nativeBefore.getBaseHP(), nativeAfter.getBaseHP());
			assertEquals(nativeBefore.getAtk(), nativeAfter.getAtk());
			for (RPSlot slot : nativeBefore.slots()) {
				assertEquals(slot.size(), nativeAfter.getSlot(slot.getName()).size());
			}
		}
	}

	@Test
	public void explicitlyUnmappingAnExistingDefinitionClearsItsOldMarker() {
		DefaultEntityManager manager = new DefaultEntityManager();
		String marker = "../../tileset/logic/creature/binding-probe.png";
		DefaultCreature definition = new DefaultCreature("human", "bandit",
				"binding probe", marker + ":0");
		assertTrue(manager.addCreature(definition));
		assertTrue(manager.isCreature(marker, 0));
		definition.setTileId(DefaultCreature.NO_TILE_ID);
		assertTrue(manager.addCreature(definition));
		assertFalse(manager.isCreature(marker, 0));
		assertSame(definition, manager.getDefaultCreature("binding probe"));
		definition.setTileId(marker + ":0");
		assertTrue(manager.addCreature(definition));
		DefaultCreature renamed = new DefaultCreature("human", "bandit",
				"BINDING PROBE", DefaultCreature.NO_TILE_ID);
		assertTrue(manager.addCreature(renamed));
		assertFalse(manager.isCreature(marker, 0));
	}

}
