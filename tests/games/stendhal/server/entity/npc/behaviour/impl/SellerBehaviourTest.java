/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.behaviour.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;

public class SellerBehaviourTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new RPClassGenerator().createRPClassesWithoutBaking(); // for Player, NPC and TradeStateChangeEvent RPClasses
	}

	/**
	 * Tests for sellerBehaviour.
	 */
	@Test
	public void testSellerBehaviour() {
		final SellerBehaviour sb = new SellerBehaviour();
		assertTrue(sb.dealtItems().isEmpty());
		assertTrue(sb.getItemNames().isEmpty());
	}

	/**
	 * Tests for sellerBehaviourMapOfStringInteger.
	 */
	@Test
	public void testSellerBehaviourMapOfStringInteger() {

		final Map<String, Integer> pricelist = new HashMap<String, Integer>();
		SellerBehaviour sb = new SellerBehaviour(pricelist);
		assertTrue(sb.dealtItems().isEmpty());
		assertTrue(sb.getItemNames().isEmpty());

		pricelist.put("item1", 10);
		pricelist.put("item2", 20);

		sb = new SellerBehaviour(pricelist);
		assertEquals(sb.dealtItems().size(), 2);
		assertTrue(sb.dealtItems().contains("item1"));
		assertTrue(sb.dealtItems().contains("item2"));
	}

	/**
	 * Tests for bottlesGlasses.
	 */
	@Test
	public void testBottlesGlasses() {
		final Map<String, Integer> pricelist = new HashMap<String, Integer>();
		pricelist.put("dingo", 3);
		pricelist.put("sztylecik", 200);

		final SellerBehaviour sb = new SellerBehaviour(pricelist);
		final SpeakerNPC npc = new SpeakerNPC("npc");
		npc.addGreeting("blabla");
		new SellerAdder().addSeller(npc, sb);
	    final Player player = PlayerTestHelper.createPlayer("bob");

	    npc.getEngine().step(player, "hi");
	    npc.getEngine().step(player, "buy 1 eliksir");
		assertEquals("Nie sprzedaję eliksiry.", getReply(npc));

	    npc.getEngine().step(player, "buy napój z winogron");
		assertEquals("Nie sprzedaję napóje z winogron.", getReply(npc));

	    npc.getEngine().step(player, "buy sztylecik");
		assertEquals("Sztylecik kosztuje 200 monet. Chcesz to kupić?", getReply(npc));
	    npc.getEngine().step(player, "yes");
		assertEquals("Przepraszam, ale nie masz wystarczająco dużo pieniędzy!", getReply(npc));

		PlayerTestHelper.equipWithMoney(player, 200);
	    npc.getEngine().step(player, "buy sztylecik");
		assertEquals("Sztylecik kosztuje 200 monet. Chcesz to kupić?", getReply(npc));
	    npc.getEngine().step(player, "yes");
		assertEquals("Gratulacje! Oto twój sztylecik!", getReply(npc));
		assertTrue(player.isEquipped("sztylecik", 1));
		assertEquals(0, player.getTotalNumberOf("money"));

		PlayerTestHelper.equipWithMoney(player, 600);
	    npc.getEngine().step(player, "buy trzy sztylecik");
		assertEquals("Możesz kupić tylko pojedynczo sztylecik. Sztylecik kosztuje 200 monet. Chcesz to kupić?", getReply(npc));
	    npc.getEngine().step(player, "yes");
		assertEquals("Gratulacje! Oto twój sztylecik!", getReply(npc));
		assertNull(PlayerTestHelper.getPrivateReply(player));
		assertEquals(2, player.getTotalNumberOf("sztylecik"));
		assertEquals(400, player.getTotalNumberOf("money"));
	}

	/**
	 * Tests for selling scrolls.
	 */
	@Test
	public void testScrolls() {
		final Map<String, Integer> pricelist = new HashMap<String, Integer>();
		pricelist.put("zwój fado", 1000);
		pricelist.put("niezapisany zwój", 3000);
		final SellerBehaviour sb = new SellerBehaviour(pricelist);
		final SpeakerNPC npc = new SpeakerNPC("npc");
		npc.addGreeting("Hello!");
		new SellerAdder().addSeller(npc, sb);
	    final Player player = PlayerTestHelper.createPlayer("bob");

	    npc.getEngine().step(player, "hi");
		assertEquals("Hello!", getReply(npc));

	    npc.getEngine().step(player, "buy zwój fado");
		assertEquals("Zwój fado kosztuje 1000 monet. Chcesz to kupić?", getReply(npc));
	    npc.getEngine().step(player, "no");

	    npc.getEngine().step(player, "buy dwa niezapisany zwój");
		assertEquals("2 niezapisany zwój kosztuje 6000 monet. Chcesz je kupić?", getReply(npc));
	    npc.getEngine().step(player, "no");
		assertEquals("Dobrze w czym jeszcze mogę pomóc?", getReply(npc));

	    npc.getEngine().step(player, "buy zwój");
		assertEquals("Jest więcej niż jeden zwój. Powiedz mi jaki rodzaj zwój chcesz użyć.", getReply(npc));

	    npc.getEngine().step(player, "buy anything-else");
		assertEquals("Nie sprzedaję anything-elsa.", getReply(npc));
	}

}
