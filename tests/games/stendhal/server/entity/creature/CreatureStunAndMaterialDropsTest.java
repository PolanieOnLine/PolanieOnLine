/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************/
package games.stendhal.server.entity.creature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.transform.stream.StreamSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import games.stendhal.common.constants.Occasion;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.impl.DropItem;
import games.stendhal.server.entity.status.StatusAttacker;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.RPClass.CreatureTestHelper;

/** Loaded creature definitions, stun profiles and material drop odds for 1.42.1. */
public class CreatureStunAndMaterialDropsTest {
	private static final String[] FILES = {
		"stendhal/giant", "pol/giant", "stendhal/elemental", "pol/elemental",
		"stendhal/troll", "pol/alien", "stendhal/kobold", "stendhal/gnome",
		"stendhal/dwarf", "pol/wraith", "pol/beast", "stendhal/angel",
		"pol/angel", "pol/solemn"
	};
	private static final Map<String, Element> DEFINITIONS = new HashMap<>();

	@BeforeClass
	public static void loadDefinitions() throws Exception {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		for (final String file : FILES) {
			final NodeList creatures = factory.newDocumentBuilder()
					.parse(Paths.get("data/conf/creatures", file + ".xml").toFile())
					.getElementsByTagNameNS("stendhal", "creature");
			for (int i = 0; i < creatures.getLength(); i++) {
				final Element creature = (Element) creatures.item(i);
				DEFINITIONS.put(creature.getAttribute("name"), creature);
			}
		}
		MockStendlRPWorld.get();
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void changedFilesMatchCreatureSchema() throws Exception {
		final Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
				.newSchema(Paths.get("data/conf/creatures.xsd").toFile());
		for (final String file : FILES) {
			schema.newValidator().validate(new StreamSource(
					Paths.get("data/conf/creatures", file + ".xml").toFile()));
		}
	}

	@Test
	public void largeHumanoidsLoadExactlyOneStunAttacker() {
		final String[] weaker = {"cyklop", "kobold olbrzymi", "olbrzymi gnom",
			"olbrzymia panna gnom", "gigantyczny krasnal", "kasarkutominubat"};
		final String[] giants = {"amazonka olbrzymia", "olbrzym", "olbrzym starszy",
			"olbrzym mistrz", "czarny olbrzym", "imperialny generał gigant",
			"lodowy olbrzym", "lodowy starszy olbrzym", "mega mag", "szarak gigant",
			"czarny szkielet gigant", "gigantyczne monstrum", "superczłowiek olbrzym",
			"kostucha wielka", "kostucha różowa wielka", "kostucha złota wielka"};
		for (final String name : weaker) {
			assertStun(name, 5.0);
		}
		for (final String name : giants) {
			assertStun(name, 10.0);
		}
		assertStun("gigantyczna lili", 12.0);
		// Existing stun users keep their authored probabilities.
		assertStun("trol jaskiniowy", 8.0);
	}

	@Test
	public void greatReapersKeepOtherAbilitiesAndOrdinaryReapersDoNotGainStun() {
		final String[] great = {"kostucha wielka", "kostucha różowa wielka", "kostucha złota wielka"};
		for (final String name : great) {
			assertEquals(name, "5", creature(name).getAIProfile("bleeding_attack"));
			assertEquals(name, "0.05", creature(name).getAIProfile("lifesteal"));
		}
		assertEquals("35,50", creature("kostucha wielka").getAIProfile("heal"));
		assertEquals("35,50", creature("kostucha różowa wielka").getAIProfile("heal"));
		assertEquals("15,50", creature("kostucha złota wielka").getAIProfile("heal"));
		assertTrue(creature("kostucha złota wielka").getAIProfiles().containsKey("boss"));
		final String[] ordinary = {"kostucha", "kostucha różowa", "kostucha złota"};
		for (final String name : ordinary) {
			assertNull(name, creature(name).getAIProfile("status_attackers"));
		}
	}

	@Test
	public void everyGiantClassHasStunWithoutReplacingOtherAbilities() {
		int checked = 0;
		for (final Element definition : DEFINITIONS.values()) {
			final Element type = (Element) definition.getElementsByTagNameNS("stendhal", "type").item(0);
			final String creatureClass = type.getAttribute("class");
			if ("giant_human".equals(creatureClass) || "giant".equals(creatureClass)) {
				final String name = definition.getAttribute("name");
				assertTrue(name, creature(name).getAIProfile("status_attackers").contains("StunnedStatus,"));
				checked++;
			}
		}
		assertEquals(12, checked);
		assertEquals("5", creature("mega mag").getAIProfile("bleeding_attack"));
		assertEquals("20,50", creature("mega mag").getAIProfile("heal"));
		assertEquals("0.05", creature("gigantyczna lili").getAIProfile("lifesteal"));
		assertTrue(creature("czarny olbrzym").getAIProfiles().containsKey("boss"));
		assertEquals("archer,attack weakest,dual attack", creature("czarny olbrzym").getAIProfile("strategy"));
	}

	@Test
	public void largeAnimalsAndTowerDoNotGainStun() {
		final String[] excluded = {"szczur olbrzymi", "nietoperz olbrzymi",
			"nietoperz olbrzymi wampir", "orzeł gigant", "rekin", "olbrzymia wieża"};
		for (final String name : excluded) {
			assertNull(name, creature(name).getAIProfile("status_attackers"));
			assertFalse(name, creature(name).getAllStatusAttackers().stream()
					.anyMatch(attacker -> "stunned".equals(attacker.getStatusName())));
		}
	}

	@Test
	public void ordinaryAngelFeathersUseEightyEightyFiveAndNinetyPercent() {
		assertDrop("upadły anioł", "pióro upadłego anioła", 80.0);
		assertDrop("anioł", "pióro anioła", 80.0);
		assertDrop("archanioł", "pióro archanioła", 85.0);
		assertDrop("anioł ciemności", "pióro mrocznego anioła", 85.0);
		assertDrop("archanioł ciemności", "pióro archanioła ciemności", 85.0);
		assertDrop("serafin", "pióro serafina", 85.0);
		assertDrop("azazel", "pióro azazela", 85.0);
		assertDrop("cherubin", "pióro cherubina", 90.0);
		assertDrop("lilith", "pióro lilith", 90.0);
	}

	@Test
	public void specifiedAngelsAndSkeletonsNeverDropGiantHearts() {
		final String[] removed = {"serafin", "azazel", "cherubin", "lilith",
			"świąteczny serafin", "świąteczny azazel", "świąteczny cherubin",
			"świąteczna lilith", "książę szkieletów", "szkielet anioła"};
		for (final String name : removed) {
			assertNull(name, configuredDrop(name, "serce olbrzyma"));
			assertNull(name, loadedDrop(name, "serce olbrzyma"));
		}
	}

	@Test
	public void fourSpecifiedGiantsDropOneHeartWithEightyFivePercentChance() {
		final String[] giants = {"lodowy starszy olbrzym", "superczłowiek olbrzym",
			"mega mag", "gigantyczna lili"};
		for (final String name : giants) {
			assertDrop(name, "serce olbrzyma", 85.0);
		}
	}

	@Test
	public void unrelatedMaterialDropsKeepTheirOdds() {
		assertDrop("świąteczny serafin", "pióro serafina", 30.0);
		assertDrop("świąteczny azazel", "pióro azazela", 30.0);
		assertDrop("świąteczny cherubin", "pióro cherubina", 30.0);
		assertDrop("świąteczna lilith", "pióro lilith", 30.0);
		assertDrop("szkielet anioła", "pióro anioła", 0.02);
		assertDrop("anioł", "serce olbrzyma", 85.0);
		assertDrop("archanioł ciemności", "serce olbrzyma", 90.0);
		assertDrop("szarak gigant", "serce olbrzyma", 60.0);
		assertDrop("olbrzym", "serce olbrzyma", 80.0);
	}

	private void assertStun(final String name, final double probability) {
		final NodeList profiles = DEFINITIONS.get(name).getElementsByTagNameNS("stendhal", "profile");
		int profileCount = 0;
		for (int i = 0; i < profiles.getLength(); i++) {
			if ("status_attackers".equals(((Element) profiles.item(i)).getAttribute("name"))) {
				profileCount++;
			}
		}
		assertEquals(name, 1, profileCount);
		final Creature creature = creature(name);
		int count = 0;
		for (final StatusAttacker attacker : creature.getAllStatusAttackers()) {
			if ("stunned".equals(attacker.getStatusName())) {
				assertEquals(name, probability, attacker.getProbability(), 0.0);
				count++;
			}
		}
		assertEquals(name, 1, count);
	}

	private void assertDrop(final String name, final String item, final double probability) {
		final Element configured = configuredDrop(name, item);
		assertNotNull(name + ": " + item, configured);
		assertEquals(probability, Double.parseDouble(configured.getAttribute("probability")), 0.0);
		assertEquals("[1,1]", configured.getAttribute("quantity"));
		final DropItem loaded = loadedDrop(name, item);
		assertNotNull(name + ": " + item, loaded);
		final double effective = Occasion.SECOND_WORLD ? Math.min(100.0, probability * 1.5) : probability;
		assertEquals(effective, loaded.probability, 0.000001);
		assertEquals(1, loaded.min);
		assertEquals(1, loaded.max);
	}

	private Element configuredDrop(final String name, final String item) {
		final Element creature = DEFINITIONS.get(name);
		assertNotNull(name, creature);
		final Element drops = (Element) creature.getElementsByTagNameNS("stendhal", "drops").item(0);
		final NodeList items = drops.getElementsByTagNameNS("stendhal", "item");
		for (int i = 0; i < items.getLength(); i++) {
			final Element drop = (Element) items.item(i);
			if (item.equals(drop.getAttribute("value"))) {
				return drop;
			}
		}
		return null;
	}

	private DropItem loadedDrop(final String name, final String item) {
		for (final DropItem drop : creature(name).dropsItems) {
			if (item.equals(drop.name)) {
				return drop;
			}
		}
		return null;
	}

	private Creature creature(final String name) {
		final Creature result = SingletonRepository.getEntityManager().getCreature(name);
		assertNotNull(name, result);
		return result;
	}
}
