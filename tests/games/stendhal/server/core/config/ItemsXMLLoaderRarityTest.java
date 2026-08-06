/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.List;

import org.junit.Test;

import games.stendhal.server.core.rule.defaultruleset.DefaultItem;

public class ItemsXMLLoaderRarityTest {
	@Test
	public void readsRarityMetadataAndResetsOptionalDefinitionValues()
			throws Exception {
		final List<DefaultItem> items = new ItemsXMLLoader().load(
				new URI("testrarityitems.xml"));

		assertEquals(3, items.size());
		assertEquals(Boolean.FALSE, items.get(0).getRarityEnabled());
		assertEquals("boss", items.get(0).getRarityProfile());
		assertEquals(123, items.get(0).getValue());
		assertEquals(4.0, items.get(0).getWeight(), 0.0);

		assertNull(items.get(1).getRarityEnabled());
		assertEquals("default", items.get(1).getRarityProfile());
		assertEquals(0, items.get(1).getValue());
		assertEquals(0.0, items.get(1).getWeight(), 0.0);
		assertFalse(items.get(1).getAttributes().isEmpty());

		assertEquals(Boolean.TRUE, items.get(2).getRarityEnabled());
	}
}
