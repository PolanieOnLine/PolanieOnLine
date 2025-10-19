/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
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

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.maps.fado.forest.OldWomanNPC;
import games.stendhal.server.maps.kirdneh.city.GossipNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

public class FindJefsMomTest extends ZonePlayerAndNPCTestImpl {

	private static final String CITY_ZONE_NAME = "0_kirdneh_city";
	private static final String FOREST_ZONE_NAME = "0_fado_forest_s";

	private static final String AMBER_TALK_GIVE_FLOWER = "O, rozumiem. :) Mój syn Jef poprosił Cię, żebyś mnie poszukał. Co za kochany i troskliwy chłopiec! Proszę, daj mu tę bielikrasę. Kocham te kwiaty! Bedzie wiedział, że ze mną wszystko #w #porządku, jeśli mu ją dasz!";
	private static final String AMBER_TALK_LOST_FLOWER = "Oh zgubiłeś kwiatek? Obawiam się, że już ich nie mam. Porozmaiwaj z Jenny przy młynie. Może będzie mogła ci pomóc.";
	private static final String AMBER_TALK_SEND_TO_JEF = "Proszę daj ten kwiatek mojemu synowi i daj mu znać, że u mnie wszystko #dobrze.";
	private static final String AMBER_TALK_REJECT = "Nie ufam Ci. Twój głos drżał, gdy wymieniałeś imię mojego syna. Założę się, że ma się świetnie i jest bezpieczny.";

	private static final String JEF_TALK_QUEST_OFFER = "Tęsknię za moją mamusią! Chciała pójść na rynek i do tej pory nie wróciła. Mógłbyś jej poszukać?";
	private static final String JEF_TALK_QUEST_REJECT = "Och. Dobrze. Nie potrafię Cię zrozumieć... Wyglądasz na przepracowanego bohatera, więc nie będę Cię prosić o pomoc.";
	private static final String JEF_TALK_QUEST_ACCEPT = "Dziękuję Ci bardzo! Mam nadzieję, że #mama trzyma się świetnie i prędko wróci! Proszę, powiedz jej moje imię, #Jef, żeby udownodnić, że to ja Cię do niej wysłałem. Jeśli znajdziesz ją i wrócisz do mnie, dam Ci coś w zamian, żeby pokazać Ci, jak wdzięczny jestem.";
	private static final String JEF_TALK_QUEST_REMIND = "Mam nadzieję, że znajdziesz mamę szybko i powiesz mi, czy jest #cała i #zdrowa.";
	private static final String JEF_TALK_QUEST_OFFER_AGAIN = "Minęło już trochę czasu, odkąd rozglądałeś się za moją mamą. Czy mogę Cię prosić, abyś poszukał jej raz jeszcze i powiedział mi, czy miewa się dobrze, ok?";
	private static final String JEF_TALK_QUEST_TOO_SOON = "Nie chcę przeszkadzać mojej mamie, nim nie wróci z powrotem, więc nie musisz jej nic przekazać. Może zapytaj mnie raz jeszcze później... 2 dni.";

	private static final String HISTORY_DEFAULT = "Znalazłem Jefa w Kirdneh. Czeka tam na swoją mamę.";
	private static final String HISTORY_REJECTED = "Znalezienie jego mamy kosztuje mnie teraz zbyt wiele czasu, dlatego musiałem odrzucić jego prośbę o pomoc w znalezieniu jej.";
	private static final String HISTORY_START = "Jef poprosił mnie, żebym rozejrzał się za jego matką, Amber, która nie wróciła z zakupów na rynku. Mam nadzieję, że wysłucha mnie, gdy powiem imię jej syna - Jefa.";
	private static final String HISTORY_FOUND_MOM = "Znalazłem Amber, mamę Jefa, kiedy spacerowała gdzieś w lesie Fado. Dała mi kwiaty dla swojego syna i powiedziała mi, że te kwiaty powiedzą mu, że z nią wszystko w porządku.";
	private static final String HISTORY_COMPLETED_REPEATABLE = "Przyniosłem Jefowi bielikrasę, a on bardzo się ucieszył, gdy dowiedził się, że jego mama, Amber, trzyma się dobrze. Chociaż Jef nie chce, żebym pilnował jej znowu, tzeba zapytać go, czy nie zmienił zdania.";
	private static final String HISTORY_COMPLETED_NOT_REPEATABLE = "Przyniosłem Jefowi bielikrasę, a on bardzo się ucieszył, gdy dowiedział się, że jego mama, Amber, trzyma się dobrze. Chce zostawić mamę samą na jakiś czas.";

	private SpeakerNPC npc;
	private Engine en;

	private String questSlot;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone(CITY_ZONE_NAME);
		setupZone(FOREST_ZONE_NAME);
	}

	public FindJefsMomTest() {
		setNpcNames("Jef", "Amber");
		setZoneForPlayer(CITY_ZONE_NAME);
		addZoneConfigurator(new GossipNPC(), CITY_ZONE_NAME);
		addZoneConfigurator(new OldWomanNPC(), FOREST_ZONE_NAME);
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		quest = new FindJefsMom();
		quest.addToWorld();

		questSlot = quest.getSlotName();
	}

	@Test
	public void testRefuseQuest() {
		startTalkingToNpc("Jef");

		en.step(player, "quest");
		assertEquals(JEF_TALK_QUEST_OFFER, getReply(npc));

		en.step(player, "no");
		assertEquals(JEF_TALK_QUEST_REJECT, getReply(npc));

		assertEquals("rejected", player.getQuest(questSlot));
		assertFalse(npc.isTalking());
		assertLoseKarma(10);
		assertHistory(HISTORY_DEFAULT, HISTORY_REJECTED);
	}

	@Test
	public void testAcceptQuest() {
		startTalkingToNpc("Jef");

		en.step(player, "quest");
		assertEquals(JEF_TALK_QUEST_OFFER, getReply(npc));

		en.step(player, "yes");
		assertEquals(JEF_TALK_QUEST_ACCEPT, getReply(npc));

		assertEquals("start", player.getQuest(questSlot));
		assertHistory(HISTORY_DEFAULT, HISTORY_START);
	}

	@Test
	public void testTalkToAmber() {
		player.setQuest(questSlot, "start");

		startTalkingToNpc("Amber");

		en.step(player, "Jef");
		assertEquals(AMBER_TALK_GIVE_FLOWER, getReply(npc));

		assertEquals("found_mom", player.getQuest(questSlot));
		assertTrue(player.isEquipped("bielikrasa"));
		assertFalse(npc.isTalking());
		assertHistory(HISTORY_DEFAULT, HISTORY_FOUND_MOM);
	}

	@Test
	public void testTalkToAmberAgain() {
		player.setQuest(questSlot, "found_mom");
		PlayerTestHelper.equipWithItem(player, "bielikrasa");

		startTalkingToNpc("Amber");

		en.step(player, "Jef");
		assertEquals(AMBER_TALK_SEND_TO_JEF, getReply(npc));

		assertEquals("found_mom", player.getQuest(questSlot));
		assertFalse(npc.isTalking());
		assertHistory(HISTORY_DEFAULT, HISTORY_FOUND_MOM);
	}

	@Test
	public void testTalkToAmberAgainLostFlower() {
		player.setQuest(questSlot, "found_mom");

		startTalkingToNpc("Amber");

		en.step(player, "Jef");
		assertEquals(AMBER_TALK_LOST_FLOWER, getReply(npc));

		assertEquals("found_mom", player.getQuest(questSlot));
		assertFalse(npc.isTalking());
		assertHistory(HISTORY_DEFAULT, HISTORY_FOUND_MOM);
	}

	@Test
	public void testTalkToAmberFirst() {
		player.setQuest(questSlot, null);

		startTalkingToNpc("Amber");

		en.step(player, "Jef");
		assertEquals(AMBER_TALK_REJECT, getReply(npc));

		assertNull(player.getQuest(questSlot));
		assertFalse(player.isEquipped("bielikrasa"));
		assertFalse(npc.isTalking());
		assertNoHistory();
	}

	@Test
	public void testBackToJef() {
		long initialXp = player.getXP();
		PlayerTestHelper.equipWithItem(player, "bielikrasa");
		player.setQuest(questSlot, "found_mom");

		startTalkingToNpc("Jef");

		en.step(player, "fine");
		assertThat(getReply(npc), startsWith("Dziękuję"));

		assertFalse(player.isEquipped("bielikrasa"));
		assertTrue(player.isEquipped("skrzydlica"));
		assertGainKarma(15);
		assertEquals(800L, player.getXP() - initialXp);
		assertEquals("done", player.getQuest(questSlot, 0));

		en.step(player, "bye");
		assertEquals("Żegnaj.", getReply(npc));
		assertHistory(HISTORY_DEFAULT, HISTORY_COMPLETED_NOT_REPEATABLE);
	}

	@Test
	public void testBackToJefLostFlower() {
		player.setQuest(questSlot, "found_mom");

		startTalkingToNpc("Jef");

		en.step(player, "fine");
		assertNull(getReply(npc));

		en.step(player, "task");
		assertEquals(JEF_TALK_QUEST_REMIND, getReply(npc));

		assertEquals("found_mom", player.getQuest(questSlot));
		assertHistory(HISTORY_DEFAULT, HISTORY_FOUND_MOM);
	}

	@Test
	public void testAskQuestAgain() {
		player.setQuest(questSlot, 0, "done");
		PlayerTestHelper.setPastTime(player, questSlot, 1,
				TimeUnit.DAYS.toSeconds(5));

		startTalkingToNpc("Jef");

		en.step(player, "task");
		assertEquals(JEF_TALK_QUEST_OFFER_AGAIN, getReply(npc));

		assertTrue(quest.isRepeatable(player));
		assertEquals("done", player.getQuest(questSlot, 0));
		assertHistory(HISTORY_DEFAULT, HISTORY_COMPLETED_REPEATABLE);
	}

	@Test
	public void testAskQuestAgainTooSoon() {
		player.setQuest(questSlot, 0, "done");
		PlayerTestHelper.setPastTime(player, questSlot, 1,
				TimeUnit.DAYS.toSeconds(1));

		startTalkingToNpc("Jef");

		en.step(player, "task");
		assertThat(getReply(npc), startsWith(JEF_TALK_QUEST_TOO_SOON));

		assertFalse(quest.isRepeatable(player));
		assertEquals("done", player.getQuest(questSlot, 0));
		assertHistory(HISTORY_DEFAULT, HISTORY_COMPLETED_NOT_REPEATABLE);
	}

	private void startTalkingToNpc(String name) {
		npc = SingletonRepository.getNPCList().get(name);
		en = npc.getEngine();

		en.step(player, "hi");
		getReply(npc);
	}
}
