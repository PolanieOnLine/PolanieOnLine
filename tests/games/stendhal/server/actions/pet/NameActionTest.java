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
package games.stendhal.server.actions.pet;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;
import utilities.RPClass.SheepTestHelper;

public class NameActionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Tests for onActionNoArgs.
	 */
	@Test
	public void testOnActionNoArgs() {
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		final Player bob = PlayerTestHelper.createPlayer("bob");
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Wpisz stare, a później nowe imie."));
	}

	/**
	 * Tests for onActiondoesnotownoldname.
	 */
	@Test
	public void testOnActiondoesnotownoldname() {
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "oldname");
		action.put("args", "newname");
		final Player bob = PlayerTestHelper.createPlayer("bob");
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Nie masz oldname"));
	}

	/**
	 * Tests for onActionOwnsPetOfDifferentName.
	 */
	@Test
	public void testOnActionOwnsPetOfDifferentName() {
		final StendhalRPZone zone = new StendhalRPZone("zone");
		MockStendlRPWorld.get().addRPZone(zone);

		SheepTestHelper.generateRPClasses();
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "noname");
		action.put("args", "newname");
		final Sheep pet = new Sheep();

		zone.add(pet);
		final Player bob = PlayerTestHelper.createPlayer("bob");
		zone.add(bob);

		bob.setSheep(pet);
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Twoje zwierzątko nie nazywa się 'noname'"));
		assertThat(pet.getTitle(), is("sheep"));
	}

	/**
	 * Tests for onActionName.
	 */
	@Test
	public void testOnActionName() {
		final StendhalRPZone zone = new StendhalRPZone("zone");
		MockStendlRPWorld.get().addRPZone(zone);

		SheepTestHelper.generateRPClasses();
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "sheep");
		action.put("args", "newname");
		final Sheep pet = new Sheep();

		zone.add(pet);
		final Player bob = PlayerTestHelper.createPlayer("bob");
		zone.add(bob);

		bob.setSheep(pet);
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Zmieniłeś imię z 'sheep' na 'newname'"));
		assertThat(pet.getTitle(), is("newname"));
	}

	/**
	 * Tests for onActionNameWithQuotesAndSpace.
	 */
	@Test
	public void testOnActionNameWithQuotesAndSpace() {
		final StendhalRPZone zone = new StendhalRPZone("zone");
		MockStendlRPWorld.get().addRPZone(zone);

		SheepTestHelper.generateRPClasses();
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "sheep");
		action.put("args", "' newname '");
		final Sheep pet = new Sheep();

		zone.add(pet);
		final Player bob = PlayerTestHelper.createPlayer("bob");
		zone.add(bob);

		bob.setSheep(pet);
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Zmieniłeś imię z 'sheep' na 'newname'"));
		assertThat(pet.getTitle(), is("newname"));
	}

	/**
	 * Tests for onActionNameToExisting.
	 */
	@Test
	public void testOnActionNameToExisting() {
		final StendhalRPZone zone = new StendhalRPZone("zone");
		MockStendlRPWorld.get().addRPZone(zone);

		SheepTestHelper.generateRPClasses();
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "sheep");
		action.put("args", "sheep");
		final Sheep pet = new Sheep();

		zone.add(pet);
		final Player bob = PlayerTestHelper.createPlayer("bob");
		zone.add(bob);

		bob.setSheep(pet);
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Masz już zwierzątko zwane 'sheep'"));
	}

	/**
	 * Tests for onActionRename.
	 */
	@Test
	public void testOnActionRename() {
		final StendhalRPZone zone = new StendhalRPZone("zone");
		MockStendlRPWorld.get().addRPZone(zone);

		SheepTestHelper.generateRPClasses();
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "oldname");
		action.put("args", "newname");
		final Sheep pet = new Sheep();
		pet.setTitle("oldname");
		zone.add(pet);
		final Player bob = PlayerTestHelper.createPlayer("bob");
		zone.add(bob);

		bob.setSheep(pet);
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Zmieniłeś imię z 'oldname' na 'newname'"));
		assertThat(pet.getTitle(), is("newname"));
	}

	/**
	 * Tests for onActionRenameBack.
	 */
	@Test
	public void testOnActionRenameBack() {
		final StendhalRPZone zone = new StendhalRPZone("zone");
		MockStendlRPWorld.get().addRPZone(zone);

		SheepTestHelper.generateRPClasses();
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "oldname");
		action.put("args", "sheep");
		final Sheep pet = new Sheep();
		pet.setTitle("oldname");
		zone.add(pet);
		final Player bob = PlayerTestHelper.createPlayer("bob");
		zone.add(bob);

		bob.setSheep(pet);
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Zmieniłeś imię z 'oldname' na 'sheep'"));
		assertThat(pet.getTitle(), is("sheep"));
	}

	/**
	 * Tests for onActionRenameWithGenericName.
	 */
	@Test
	public void testOnActionRenameWithGenericName() {
		final StendhalRPZone zone = new StendhalRPZone("zone");
		MockStendlRPWorld.get().addRPZone(zone);

		SheepTestHelper.generateRPClasses();
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "sheep");
		action.put("args", "newname");
		final Sheep pet = new Sheep();
		pet.setTitle("oldname");
		zone.add(pet);
		final Player bob = PlayerTestHelper.createPlayer("bob");
		zone.add(bob);

		bob.setSheep(pet);
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Zmieniłeś imię z 'oldname' na 'newname'"));
		assertThat(pet.getTitle(), is("newname"));
	}

	/**
	 * Tests for onActionRenameBackWithGenericName.
	 */
	@Test
	public void testOnActionRenameBackWithGenericName() {
		final StendhalRPZone zone = new StendhalRPZone("zone");
		MockStendlRPWorld.get().addRPZone(zone);

		SheepTestHelper.generateRPClasses();
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "sheep");
		action.put("args", "sheep");
		final Sheep pet = new Sheep();
		pet.setTitle("oldname");
		zone.add(pet);
		final Player bob = PlayerTestHelper.createPlayer("bob");
		zone.add(bob);

		bob.setSheep(pet);
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Zmieniłeś imię z 'oldname' na 'sheep'"));
		assertThat(pet.getTitle(), is("sheep"));
	}

	/**
	 * Tests for onActionLongestName.
	 */
	@Test
	public void testOnActionLongestName() {
		final StendhalRPZone zone = new StendhalRPZone("zone");
		MockStendlRPWorld.get().addRPZone(zone);

		SheepTestHelper.generateRPClasses();
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "oldname");
		action.put("args", "01234567890123456789");
		final Sheep pet = new Sheep();
		pet.setTitle("oldname");
		zone.add(pet);
		final Player bob = PlayerTestHelper.createPlayer("bob");
		zone.add(bob);

		bob.setSheep(pet);
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Zmieniłeś imię z 'oldname' na '01234567890123456789'"));
		assertThat(pet.getTitle(), is("01234567890123456789"));
	}

	/**
	 * Tests for onActiontooLongName.
	 */
	@Test
	public void testOnActiontooLongName() {
		final StendhalRPZone zone = new StendhalRPZone("zone");
		MockStendlRPWorld.get().addRPZone(zone);

		SheepTestHelper.generateRPClasses();
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "oldname");
		action.put("args", "012345678901234567890");

		final Sheep pet = new Sheep();
		pet.setTitle("oldname");
		zone.add(pet);
		final Player bob = PlayerTestHelper.createPlayer("bob");
		zone.add(bob);

		bob.setSheep(pet);
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Nowe imie Twojego zwierzątka nie może być dłuższe niż 20 znaków."));
		assertThat(pet.getTitle(), is("oldname"));
	}

	/**
	 * Tests for onActionEmptyName.
	 */
	@Test
	public void testOnActionEmptyName() {
		final StendhalRPZone zone = new StendhalRPZone("zone");
		MockStendlRPWorld.get().addRPZone(zone);

		SheepTestHelper.generateRPClasses();
		final RPAction action = new RPAction();
		final NameAction nameAction = new NameAction();
		action.put("target", "sheep");
		action.put("args", "   ");
		final Sheep pet = new Sheep();
		zone.add(pet);
		final Player bob = PlayerTestHelper.createPlayer("bob");
		zone.add(bob);

		bob.setSheep(pet);
		nameAction.onAction(bob, action);
		assertThat(bob.events().get(0).get("text"), is("Nie podałeś imienia."));
		assertThat(pet.getTitle(), is("sheep"));
	}
}
