/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.BeforeClass;

import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Creature;
import games.stendhal.client.entity.factory.EntityFactory;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class GroundItemTooltipTest {
	@BeforeClass
	public static void initUI() {
		games.stendhal.client.util.UserInterfaceTestHelper.initUserInterface();
	}
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

	@Test
	public void creatureHoverFollowsLiveArmorUpdatesAndDoesNotGuessForLegacyServers() {
		final RPObject object = new RPObject();
		final Creature creature = new Creature();
		creature.initialize(object);
		assertNull(GroundContainer.buildGroundItemToolTip(creature));
		object.put("armor_type", "light");
		assertEquals("Pancerz lekki", GroundContainer.buildGroundItemToolTip(creature));
		object.put("armor_type", "heavy");
		assertEquals("Pancerz ciężki", GroundContainer.buildGroundItemToolTip(creature));
		object.put("armor_type", "none");
		assertEquals("Brak pancerza", GroundContainer.buildGroundItemToolTip(creature));
		object.remove("armor_type");
		assertNull(GroundContainer.buildGroundItemToolTip(creature));
	}
}
