/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.rule.defaultruleset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import marauroa.common.resource.ClassPathResourceProvider;

public class ItemDefinitionsResourceTest {

	@Test
	public void testPreparedDefinitionsAreAppliedToFutureItems() throws Exception {
		final DefaultEntityManager manager = (DefaultEntityManager) SingletonRepository.getEntityManager();
		final ItemDefinitionsResource resource = new ItemDefinitionsResource(manager);
		final ClassPathResourceProvider provider = new ClassPathResourceProvider(
				Thread.currentThread().getContextClassLoader());

		final Map<String, DefaultItem> original = resource.load(provider);
		final Map<String, DefaultItem> candidate = resource.load(provider);
		resource.validate(candidate);

		final String itemName = candidate.keySet().iterator().next();
		final DefaultItem prepared = candidate.get(itemName);
		final double changedWeight = prepared.getWeight() + 1.0;
		prepared.setWeight(changedWeight);

		try {
			resource.apply(candidate);
			final Item createdAfterApply = manager.getItem(itemName, ItemCreationContext.restore());
			assertEquals(changedWeight, createdAfterApply.getWeight(), 0.0001);
		} finally {
			resource.apply(original);
		}
	}

	@Test
	public void testRuntimeReloadRejectsChangedItemNameSet() throws Exception {
		final DefaultEntityManager manager = (DefaultEntityManager) SingletonRepository.getEntityManager();
		final ItemDefinitionsResource resource = new ItemDefinitionsResource(manager);
		final Map<String, DefaultItem> candidate = resource.load(new ClassPathResourceProvider(
				Thread.currentThread().getContextClassLoader()));

		candidate.remove(candidate.keySet().iterator().next());
		try {
			resource.validate(candidate);
			fail("Runtime reload must reject removed item definitions");
		} catch (final IllegalArgumentException expected) {
			// expected
		}
	}
}
