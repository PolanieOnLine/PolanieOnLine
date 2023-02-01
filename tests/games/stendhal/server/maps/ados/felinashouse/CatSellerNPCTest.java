/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.felinashouse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;
import utilities.RPClass.CatTestHelper;

/**
 * Test buying cats.
 * @author Martin Fuchs
 */
public class CatSellerNPCTest extends ZonePlayerAndNPCTestImpl {

	private static final String ZONE_NAME = "int_ados_felinas_house";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CatTestHelper.generateRPClasses();
		QuestHelper.setUpBeforeClass();

		setupZone(ZONE_NAME);
	}

	public CatSellerNPCTest() {
		setNpcNames("Felina");
		setZoneForPlayer(ZONE_NAME);
		addZoneConfigurator(new CatSellerNPC(), ZONE_NAME);
	}

	/**
	 * Tests for hiAndDo widzenia.
	 */
	@Test
	public void testHiAndBye() {
		final SpeakerNPC npc = getNPC("Felina");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi Felina"));
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));

		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia.", getReply(npc));
	}

	/**
	 * Tests for buyCat.
	 */
	@Test
	public void testBuyCat() {
		final SpeakerNPC npc = getNPC("Felina");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));

		assertTrue(en.step(player, "job"));
		assertEquals("Sprzedaję koty. Kiedy je sprzedaję są małymi kociętami, ale kiedy się takim kotkiem #zaopiekujesz to wyrasta na dużego kota.", getReply(npc));

		assertTrue(en.step(player, "care"));
		assertEquals("Koty kochają kurczaka i rybę. Wystarczy położyć kawałek na ziemi, a kot podejdzie i zje. Możesz sprawdzić jego wagę klikając prawym przyciskiem  na niego i wybierając 'Zobacz'. Jego waga będzie rosła po zjedzeniu każdego kawałka kurczaka.", getReply(npc));

		// There is currently no quest response defined for Felina.
		assertFalse(en.step(player, "quest"));

		assertTrue(en.step(player, "buy"));
		assertEquals("Cat kosztuje 100 monet. Chcesz to kupić?", getReply(npc));
		assertTrue(en.step(player, "no"));
		assertEquals("Dobrze w czym jeszcze mogę pomóc?", getReply(npc));

		assertTrue(en.step(player, "buy dog"));
		assertEquals("Nie sprzedaję dogi.", getReply(npc));

		assertTrue(en.step(player, "buy house"));
		assertEquals("Nie sprzedaję housa.", getReply(npc));

		assertTrue(en.step(player, "buy someunknownthing"));
		assertEquals("Nie sprzedaję someunknownthingi.", getReply(npc));

		assertTrue(en.step(player, "buy a glass of wine"));
		assertEquals("Nie sprzedaję wina.", getReply(npc));

		assertTrue(en.step(player, "buy a hand full of peace"));
		assertEquals("Nie sprzedaję handy full of peace.", getReply(npc));

		assertTrue(en.step(player, "buy cat"));
		assertEquals("Cat kosztuje 100 monet. Chcesz to kupić?", getReply(npc));

		assertTrue(en.step(player, "no"));
		assertEquals("Dobrze w czym jeszcze mogę pomóc?", getReply(npc));

		assertTrue(en.step(player, "buy cat"));
		assertEquals("Cat kosztuje 100 monet. Chcesz to kupić?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Nie masz tyle pieniędzy.", getReply(npc));

		assertTrue(en.step(player, "buy two cats"));
		assertEquals("2 caty kosztuje 200 monet. Chcesz je kupić?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Hmm... Nie sądzę, abyś mógł zaopiekować się więcej niż jednym kotem naraz.", getReply(npc));

		// equip with enough money to buy the cat
		assertTrue(equipWithMoney(player, 500));
		assertTrue(en.step(player, "buy cat"));
		assertEquals("Cat kosztuje 100 monet. Chcesz to kupić?", getReply(npc));

		assertFalse(player.hasPet());

		assertTrue(en.step(player, "yes"));
		assertEquals("Proszę bardzo, mały słodki kiciuś! Twój kotek żywi się każdym kawałkiem kurczaka lub rybą, którą położysz na ziemi. Ciesz się!", getReply(npc));

		assertTrue(player.hasPet());

		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia.", getReply(npc));
	}

	/**
	 * Tests for sellCat.
	 */
	@Test
	public void testSellCat() {
		final SpeakerNPC npc = getNPC("Felina");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));

		assertTrue(en.step(player, "sell cat"));
		assertEquals("Sprzedać??? Jakiego rodzaju potworem jesteś? Dlaczego w ogóle chciałbyś sprzedać swojego pięknego kota?", getReply(npc));
	}

}
