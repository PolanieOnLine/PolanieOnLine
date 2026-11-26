package games.stendhal.server.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.List;

import org.junit.Test;

import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;

public class CreaturesXMLLoaderVisibilityTest {
	@Test
	public void legendaryIceElementalDefaultsToFullyVisible() throws Exception {
		final List<DefaultCreature> definitions = new CreaturesXMLLoader().load(
				new URI("/data/conf/creatures/stendhal/elemental.xml"));
		DefaultCreature ice = null;
		for (final DefaultCreature creature : definitions) {
			if ("legendarny żywioł lodu".equals(creature.getCreatureName())) ice = creature;
		}
		assertNotNull(ice);
		assertEquals(100, ice.getVisibility());
	}

	@Test
	public void eachDefinitionHasItsOwnVisibilityAndExplicitZeroIsPreserved() throws Exception {
		final List<DefaultCreature> definitions = new CreaturesXMLLoader().load(new URI(
				"/games/stendhal/server/core/config/testcreaturevisibility.xml"));
		final int[] expected = {100, 25, 100, 0, 100};
		assertEquals(expected.length, definitions.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals("Creature " + i, expected[i], definitions.get(i).getVisibility());
		}
	}
}
