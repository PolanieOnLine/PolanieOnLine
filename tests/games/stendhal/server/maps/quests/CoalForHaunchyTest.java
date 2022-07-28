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

		quest = new CoalForHaunchy();
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
		assertEquals("Nie mogę wykorzystać polan do tego wielkiego grilla. Aby utrzymać temperaturę potrzebuję węgla, ale nie zostało go dużo. Problem w tym, że nie mogę go zdobyć ponieważ moje steki mogłby się spalić i dlatego muszę tu zostać. Czy mógłbyś przynieść mi 25 kawałków #węgla do mojego grilla?", getReply(haunchy));
		haunchyEng.step(player, "coal");
		assertEquals("Węgiel nie jest łatwo znaleźć. Normalnie możesz go znaleźć pod ziemią, ale może będziesz miał szczęście i znajdziesz w tunelach starej kopalni Semos...", getReply(haunchy));
		haunchyEng.step(player, "yes");
		assertEquals("Dziękuję! Jeżeli znalazłeś 25 kawałków to powiedz mi #węgiel to będę widział, że masz. Będę wtedy pewien, że będę mógł ci dać pyszną nagrodę.", getReply(haunchy));
		haunchyEng.step(player, "coal");
		assertEquals("Nie masz wystaczającej ilości węgla. Proszę idź i wydobądź dla mnie kilka kawałków węgla kamiennego.", getReply(haunchy));
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
		assertEquals("Nie masz wystaczającej ilości węgla. Proszę idź i wydobądź dla mnie kilka kawałków węgla kamiennego.", getReply(haunchy));
		haunchyEng.step(player, "bye");
		assertEquals("Życzę miłego dnia! Zawsze podtrzymuj ogień!", getReply(haunchy));

		// get another 15 coals
		PlayerTestHelper.equipWithStackableItem(player, "węgiel", 25);
		haunchyEng.step(player, "hi");
		assertEquals("Hej! Wspaniały dzień na grilla?", getReply(haunchy));
		haunchyEng.step(player, "task");
		// We get one or more grilled steaks a reward:
		// You see a fresh grilled steak. It smells awesome and is really juicy. It is a special quest reward for player, and cannot be used by others. Stats are (HP: 200).
		assertTrue(getReply(haunchy).matches("Dziękuję!! Przyjmij te .* grillowany stek z mojego grilla!"));
		assertTrue(player.isEquipped("grillowany stek"));
		assertEquals("waiting", player.getQuest(questSlot, 0));
		haunchyEng.step(player, "bye");
		assertEquals("Życzę miłego dnia! Zawsze podtrzymuj ogień!", getReply(haunchy));

		// -----------------------------------------------

		haunchyEng.step(player, "hi");
		assertEquals("Hej! Wspaniały dzień na grilla?", getReply(haunchy));
		haunchyEng.step(player, "task");
		assertEquals("Zapas węgla jest wystarczająco spory. Nie będę potrzebował go w ciągu 2 dni.", getReply(haunchy));
		haunchyEng.step(player, "bye");
		assertEquals("Życzę miłego dnia! Zawsze podtrzymuj ogień!", getReply(haunchy));

		// -----------------------------------------------

		haunchyEng.step(player, "hi");
		assertEquals("Hej! Wspaniały dzień na grilla?", getReply(haunchy));
		haunchyEng.step(player, "coal");
		assertEquals("Czasami mógłbyś mi wyświadczyć #'przysługę'...", getReply(haunchy));
		haunchyEng.step(player, "favour");
		assertEquals("Zapas węgla jest wystarczająco spory. Nie będę potrzebował go w ciągu 2 dni.", getReply(haunchy));
		haunchyEng.step(player, "offer");
		assertEquals("Mam nadzieje, że moje steki będą wkrótce gotowe. Bądź cierpliwy lub spróbuj przedtem innych przysmaków.", getReply(haunchy));
		haunchyEng.step(player, "help");
		assertEquals("Niestety steki nie są jeszcze gotowe... Jeżeli jesteś głodny i nie możesz czekać to może przejrzysz oferty przy wyjściu jak na przykład oferty Blacksheep koło chatek rybackich w Ados lub możesz popłynąć promem do Athor, aby zdobyć trochę przekąsek...", getReply(haunchy));
		haunchyEng.step(player, "task");
		assertEquals("Zapas węgla jest wystarczająco spory. Nie będę potrzebował go w ciągu 2 dni.", getReply(haunchy));
		haunchyEng.step(player, "bye");
		assertEquals("Życzę miłego dnia! Zawsze podtrzymuj ogień!", getReply(haunchy));
	}
}
