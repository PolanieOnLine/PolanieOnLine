/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.factory.EntityFactory;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class GroundItemTooltipTest {
	@Test
	public void groundItemTooltipUsesStructuredRarityPresentation() {
		final RPObject object = ItemTestHelper.createItem("miecz testowy");
		object.put("rarity_id", "epic");
		final IEntity item = EntityFactory.createEntity(object);

		final String tooltip = GroundContainer.buildGroundItemToolTip(item);

		assertTrue(tooltip.contains("MIECZ TESTOWY"));
		assertTrue(tooltip.contains("Epicki"));
		assertTrue(tooltip.contains("#9b59b6"));
	}

	@Test
	public void noGroundEntityDoesNotCreateTooltip() {
		assertNull(GroundContainer.buildGroundItemToolTip(null));
	}
}
