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
package games.stendhal.server.actions.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Log4J;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;

public class AdminLevelActionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get().clearPlayers();
		Log4J.init();
		AdminLevelAction.register();
	}
	@After
	public void tearDown() throws Exception {
		MockStendhalRPRuleProcessor.get().clearPlayers();
	}

	/**
	 * Tests for adminLevelAction0.
	 */
	@Test
	public final void testAdminLevelAction0() {
		final Player pl = PlayerTestHelper.createPlayer("player");
		final Player bob = PlayerTestHelper.createPlayer("bob");
		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		MockStendhalRPRuleProcessor.get().addPlayer(bob);

		pl.put("adminlevel", 5000);

		final RPAction action = new RPAction();
		action.put("type", "adminlevel");
		action.put("target", "bob");
		action.put("newlevel", "0");
		CommandCenter.execute(pl, action);
		assertEquals("Zmieniono poziom administratora bob z 0 na 0.", pl.events().get(0).get("text"));
		assertEquals("player zmienił Twój poziom administratora z 0 na 0.", bob.events().get(0).get("text"));
	}

	/**
	 * Tests for adminLevelActioncasterNotSuper.
	 */
	@Test
	public final void testAdminLevelActioncasterNotSuper() {
		final Player pl = PlayerTestHelper.createPlayer("bob");
		pl.put("adminlevel", 4999);

		MockStendhalRPRuleProcessor.get().addPlayer(pl);

		final RPAction action = new RPAction();
		action.put("type", "adminlevel");
		action.put("target", "bob");
		action.put("newlevel", "0");
		CommandCenter.execute(pl, action);
		assertEquals(
				"Potrzebujesz 5000 poziomu administratora, aby móc go zmieniać.",
				pl.events().get(0).get("text"));
	}

	/**
	 * Tests for adminLevelActionOverSuper.
	 */
	@Test
	public final void testAdminLevelActionOverSuper() {
		final Player pl = PlayerTestHelper.createPlayer("player");
		final Player bob = PlayerTestHelper.createPlayer("bob");
		// bad bad
		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		MockStendhalRPRuleProcessor.get().addPlayer(bob);

		pl.put("adminlevel", 5000);

		final RPAction action = new RPAction();
		action.put("type", "adminlevel");
		action.put("target", "bob");
		action.put("newlevel", "5001");
		CommandCenter.execute(pl, action);
		assertEquals("Zmieniono poziom administratora bob z 0 na 5000.", pl
				.events().get(0).get("text"));
		assertEquals(5000, pl.getAdminLevel());
		assertEquals(5000, bob.getAdminLevel());
		assertEquals("player zmienił Twój poziom administratora z 0 na 5000.", bob
				.events().get(0).get("text"));
	}

	/**
	 * Tests for adminLevelActionPlayerFound.
	 */
	@Test
	public final void testAdminLevelActionPlayerFound() {
		final Player pl = PlayerTestHelper.createPlayer("bob");
		pl.put("adminlevel", 5000);

		MockStendhalRPRuleProcessor.get().addPlayer(pl);

		final RPAction action = new RPAction();
		action.put("type", "adminlevel");
		action.put("target", "bob");
		CommandCenter.execute(pl, action);
		assertEquals("bob posiada 5000 poziom administratora", pl.events().get(0).get("text"));
	}
	/**
	 * Tests for adminLevelActionPlayerGhosted.
	 */
	@Test
	public final void testAdminLevelActionPlayerGhosted() {
		final Player pl = PlayerTestHelper.createPlayer("bob");
		pl.put("adminlevel", 5000);
		pl.setGhost(true);
		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		final Player nonAdmin = PlayerTestHelper.createPlayer("nonAdmin");
		final Player admin = PlayerTestHelper.createPlayer("admin");
		admin.setAdminLevel(5000);

		final RPAction action = new RPAction();
		action.put("type", "adminlevel");
		action.put("target", "bob");

		CommandCenter.execute(admin, action);
		assertTrue(AdministrationAction.isPlayerAllowedToExecuteAdminCommand(admin, "ghostmode", false));
		assertEquals("bob posiada 5000 poziom administratora", admin.events().get(0).get("text"));

		CommandCenter.execute(nonAdmin, action);
		assertEquals("Wojownik \"bob\" nie został znaleziony", nonAdmin.events().get(0).get("text"));

	}

	/**
	 * Tests for adminLevelActionPlayerFoundNoInteger.
	 */
	@Test
	public final void testAdminLevelActionPlayerFoundNoInteger() {
		final Player pl = PlayerTestHelper.createPlayer("bob");
		pl.put("adminlevel", 5000);

		MockStendhalRPRuleProcessor.get().addPlayer(pl);

		final RPAction action = new RPAction();
		action.put("type", "adminlevel");
		action.put("target", "bob");
		action.put("newlevel", "1.3");
		CommandCenter.execute(pl, action);
		assertEquals("Poziom administratora musi być liczbą całkowitą", pl
				.events().get(0).get("text"));
	}

	/**
	 * Tests for adminLevelActionPlayerNotFound.
	 */
	@Test
	public final void testAdminLevelActionPlayerNotFound() {
		final Player pl = PlayerTestHelper.createPlayer("player");
		pl.put("adminlevel", 5000);
		final RPAction action = new RPAction();
		action.put("type", "adminlevel");
		action.put("target", "bob");
		CommandCenter.execute(pl, action);

		assertEquals("Wojownik \"bob\" nie został znaleziony", pl.events().get(0).get("text"));
	}

}
