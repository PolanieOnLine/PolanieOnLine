/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.actions.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Log4J;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;

public class SummonAtActionTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		MockStendlRPWorld.get();
	}

	@After
	public void tearDown() {
		MockStendhalRPRuleProcessor.get().clearPlayers();
	}

	@Test
	public void testSummonFixedRareItemIntoPlayerBag() {
		final Player admin = PlayerTestHelper.createPlayer("admin");
		final Player target = PlayerTestHelper.createPlayer("target");
		MockStendhalRPRuleProcessor.get().addPlayer(admin);
		MockStendhalRPRuleProcessor.get().addPlayer(target);

		final RPAction action = new RPAction();
		action.put("target", target.getName());
		action.put("slot", "bag");
		action.put("item", "sztylecik");
		action.put("rarity", "rare");
		action.put("attack-multiplier", "1.08");

		new SummonAtAction().perform(admin, action);

		final Item item = (Item) target.getSlot("bag").getFirst();
		assertNotNull(item);
		assertEquals(ItemRarity.RARE, item.getRarity());
		assertEquals(Double.valueOf(1.08), item.getRarityModifier("atk"));
	}
}
