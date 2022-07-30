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
package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.maps.ados.market.BBQGrillmasterNPC;
import games.stendhal.server.maps.semos.mines.MinerNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

/**
 * JUnit test for the CoalForHaunchy quest.
 * @author bluelads, M. Fuchs
 */
public class CoalForHaunchyTest extends ZonePlayerAndNPCTestImpl {

	private String questSlot;
	private static final String ZONE_NAME = "0_ados_city_n2";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone(ZONE_NAME);
	}

	public CoalForHaunchyTest() {
		super(ZONE_NAME, "Haunchy", "Barbarus");
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		new BBQGrillmasterNPC().configureZone(zone, null);
		new MinerNPC().configureZone(zone, null);

		quest = new BuiltQuest(new CoalForHaunchy().story());
		quest.addToWorld();

		questSlot = quest.getSlotName();
	}

	@Test
	public void testQuest() {
		SpeakerNPC haunchy = SingletonRepository.getNPCList().get("Haunchy Meatoch");
		Engine haunchyEng = haunchy.getEngine();

		SpeakerNPC barbarus = SingletonRepository.getNPCList().get("Barbarus");
		Engine barbarusEng = barbarus.getEngine();

		// -----------------------------------------------
		// start with Haunchy

		haunchyEng.step(player, "hi");
		assertEquals("Hej! Wspaniały dzień na grilla?", getReply(haunchy));
		haunchyEng.step(player, "task");
		assertEquals("Nie mogę wykorzystać polan do tego wielkiego grilla. Aby utrzymać temperaturę potrzebuję węgla, ale nie zostało go dużo. Problem w tym, że nie mogę go zdobyć ponieważ moje steki mogłyby się spalić i dlatego muszę tu zostać. Czy mógłbyś przynieść mi 25 kawałków #węgla do mojego grilla?", getReply(haunchy));
		haunchyEng.step(player, "coal");
		assertEquals("Węgiel nie jest łatwy do znalezienia. Zwykle możesz go znaleźć gdzieś w ziemi, ale być może będziesz mieć szczęście i znajdziesz go w starych tunelach Semos... Pomożesz mi?", getReply(haunchy));
		haunchyEng.step(player, "yes");
		assertEquals("Dziękuję Ci! Na pewno dam ci miłą i smaczną nagrodę.", getReply(haunchy));
		haunchyEng.step(player, "coal");
		assertEquals("Czasami mógłbyś mi wyświadczyć #'przysługę'...", getReply(haunchy));
		haunchyEng.step(player, "bye");
		assertEquals("Życzę miłego dnia! Zawsze podtrzymuj ogień!", getReply(haunchy));

		// -----------------------------------------------
		// now talk to Barbarus

		barbarusEng.step(player, "hi");
		assertEquals("Witaj!", getReply(barbarus));
		barbarusEng.step(player, "buy kilof");
		assertEquals("kilof kosztuje 400. Chcesz kupić to?", getReply(barbarus));
		barbarusEng.step(player, "yes");
		assertEquals("Przepraszam, ale nie masz wystarczająco dużo pieniędzy!", getReply(barbarus));
		barbarusEng.step(player, "bye");
		assertEquals("Miło było cię zobaczyć. Powodzenia!", getReply(barbarus));

		barbarusEng.step(player, "hi");
		assertEquals("Witaj!", getReply(barbarus));
		barbarusEng.step(player, "buy kilof");
		assertEquals("kilof kosztuje 400. Chcesz kupić to?", getReply(barbarus));
		PlayerTestHelper.equipWithMoney(player, 400);
		barbarusEng.step(player, "yes");
		assertEquals("Gratulacje! Oto twój kilof!", getReply(barbarus));
		// You see a pick. It is a tool which helps you to get some coal.
		assertTrue(player.isEquipped("kilof"));
		barbarusEng.step(player, "bye");
		assertEquals("Miło było cię zobaczyć. Powodzenia!", getReply(barbarus));

		// get 10 coals
		PlayerTestHelper.equipWithStackableItem(player, "węgiel", 10);
		haunchyEng.step(player, "hi");
		assertEquals("Hej! Wspaniały dzień na grilla?", getReply(haunchy));
		haunchyEng.step(player, "task");
		assertEquals("Na szczęście mój grill wciąż pali. Ale proszę pospiesz się i przynieś mi 25 węgla, tak jak obiecałeś.", getReply(haunchy));
		haunchyEng.step(player, "bye");
		assertEquals("Życzę miłego dnia! Zawsze podtrzymuj ogień!", getReply(haunchy));

		// get another 15 coals
		PlayerTestHelper.equipWithStackableItem(player, "węgiel", 25);
		haunchyEng.step(player, "hi");
		assertTrue(getReply(haunchy).matches("Dziękuję! Przyjmij oto .* grillowany stek z mojego grilla!"));
		assertTrue(player.isEquipped("grillowany stek"));
		assertEquals("done", player.getQuest(questSlot, 0));
		haunchyEng.step(player, "bye");
		assertEquals("Życzę miłego dnia! Zawsze podtrzymuj ogień!", getReply(haunchy));

		// -----------------------------------------------

		haunchyEng.step(player, "hi");
		assertEquals("Hej! Wspaniały dzień na grilla?", getReply(haunchy));
		haunchyEng.step(player, "task");
		assertEquals("Zapas węgla jest wystarczająco spory. Nie będę potrzebował go przez jakiś czas.", getReply(haunchy));
		haunchyEng.step(player, "bye");
		assertEquals("Życzę miłego dnia! Zawsze podtrzymuj ogień!", getReply(haunchy));

		// -----------------------------------------------

		haunchyEng.step(player, "hi");
		assertEquals("Hej! Wspaniały dzień na grilla?", getReply(haunchy));
		haunchyEng.step(player, "coal");
		assertEquals("Czasami mógłbyś mi wyświadczyć #'przysługę'...", getReply(haunchy));
		haunchyEng.step(player, "favour");
		assertEquals("Zapas węgla jest wystarczająco spory. Nie będę potrzebował go przez jakiś czas.", getReply(haunchy));
		haunchyEng.step(player, "offer");
		assertEquals("Mam nadzieje, że moje steki będą wkrótce gotowe. Bądź cierpliwy lub spróbuj przedtem innych przysmaków.", getReply(haunchy));
		haunchyEng.step(player, "help");
		assertEquals("Niestety steki nie są jeszcze gotowe... Jeżeli jesteś głodny i nie możesz czekać to może przejrzysz oferty przy wyjściu jak na przykład oferty Blacksheep koło chatek rybackich w Ados lub możesz popłynąć promem do Athor, aby zdobyć trochę przekąsek...", getReply(haunchy));
		haunchyEng.step(player, "task");
		assertEquals("Zapas węgla jest wystarczająco spory. Nie będę potrzebował go przez jakiś czas.", getReply(haunchy));
		haunchyEng.step(player, "bye");
		assertEquals("Życzę miłego dnia! Zawsze podtrzymuj ogień!", getReply(haunchy));
	}
}
