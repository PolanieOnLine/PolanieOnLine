/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 ***************************************************************************/
package games.stendhal.server.maps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import utilities.RPClass.CreatureTestHelper;

/** Regression coverage for the strength configured for fog creature spawns. */
public class AlternativeCreaturesConfigurationTest {
	private static final String CONFIGURATOR = AlternativeCreatures.class.getName();

	@BeforeClass
	public static void setUpWorld() {
		MockStendlRPWorld.get();
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void zoneConfigurationsDoNotMisspellFactor() throws Exception {
		int checked = 0;
		try (Stream<Path> files = Files.walk(Paths.get("data/conf/zones"))) {
			final Iterator<Path> iterator = files.filter(p -> p.toString().endsWith(".xml")).iterator();
			while (iterator.hasNext()) {
				final Path file = iterator.next();
				final NodeList configurators = readConfigurators(file);
				for (int i = 0; i < configurators.getLength(); i++) {
					final Element element = (Element) configurators.item(i);
					if (CONFIGURATOR.equals(element.getAttribute("class-name"))) {
						assertFalse(file.toString(), parameters(element).containsKey("factory"));
						checked++;
					}
				}
			}
		}
		assertTrue("No fog creature configurations were checked", checked > 0);
	}

	@Test
	public void correctedSpawnsApplyTheirAuthoredStrength() throws Exception {
		final String[] files = {"desert", "dragon", "dragon_knights", "koscielisko", "tatry", "zakopane"};
		final int[] expectedCounts = {7, 1, 1, 1, 2, 1};
		for (int f = 0; f < files.length; f++) {
			int checked = 0;
			final NodeList configurators = readConfigurators(Paths.get("data/conf/zones/pol", files[f] + ".xml"));
			for (int i = 0; i < configurators.getLength(); i++) {
				final Element element = (Element) configurators.item(i);
				if (!CONFIGURATOR.equals(element.getAttribute("class-name"))) {
					continue;
				}
				final Map<String, String> attributes = parameters(element);
				if (!attributes.containsKey("creature")) {
					continue;
				}
				final String creature = attributes.get("creature");
				final boolean halfStrength = creature.equals("łucznik imperium lider")
						|| creature.equals("książę szkieletów")
						|| creature.equals("jeździec chaosu na czerwonym smoku")
						|| creature.equals("pokutnik nocny");
				final double factor = halfStrength ? 0.5 : 0.75;
				assertEquals(files[f] + ": " + creature, String.valueOf(factor), attributes.get("factor"));
				assertConfiguredStats(attributes, factor);
				checked++;
			}
			assertEquals(files[f], expectedCounts[f], checked);
		}
	}

	@Test
	public void omittedFactorKeepsDefaultStrength() {
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("creature", "dwugłowy czarny smok");
		attributes.put("spawnX", "5");
		attributes.put("spawnY", "5");
		assertConfiguredStats(attributes, 0.1);
	}

	private void assertConfiguredStats(final Map<String, String> attributes, final double factor) {
		final Creature base = SingletonRepository.getEntityManager().getCreature(attributes.get("creature"));
		final StendhalRPZone zone = new StendhalRPZone("fog_configuration_test", 200, 200);
		new AlternativeCreatures().configureZone(zone, attributes);
		assertEquals(1, zone.getRespawnPointList().size());
		final Creature fog = zone.getRespawnPointList().get(0).getPrototypeCreature();
		assertEquals("mgielny " + base.getName(), fog.getName());
		assertEquals(scaledCombatStat(base.getAtk(), factor), fog.getAtk());
		assertEquals(scaledCombatStat(base.getDef(), factor), fog.getDef());
		assertEquals(scaledCombatStat(base.getBaseHP(), factor), fog.getBaseHP());
		assertEquals(scaledCombatStat(base.getHP(), factor), fog.getHP());
		assertEquals((int) (base.getLevel() * (1 + factor)), fog.getLevel());
		assertEquals((int) (base.getXP() * (1 + factor)), fog.getXP());
	}

	private int scaledCombatStat(final int value, final double factor) {
		return Math.min(Short.MAX_VALUE, (int) (value * (1 + factor)));
	}

	private NodeList readConfigurators(final Path file) throws Exception {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		return factory.newDocumentBuilder().parse(file.toFile()).getElementsByTagNameNS("stendhal", "configurator");
	}

	private Map<String, String> parameters(final Element configurator) {
		final Map<String, String> result = new HashMap<>();
		final NodeList parameters = configurator.getElementsByTagNameNS("stendhal", "parameter");
		for (int i = 0; i < parameters.getLength(); i++) {
			final Element parameter = (Element) parameters.item(i);
			result.put(parameter.getAttribute("name"), parameter.getTextContent().trim());
		}
		return result;
	}
}
