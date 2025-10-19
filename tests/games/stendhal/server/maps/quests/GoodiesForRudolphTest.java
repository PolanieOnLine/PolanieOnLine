/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.maps.semos.city.RudolphNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

public class GoodiesForRudolphTest extends ZonePlayerAndNPCTestImpl {

	private static final String ZONE_SEMOS = "0_semos_city";

	private static final String NPC_RUDOLPH = "Rudolph";

	private static final String ITEM_APPLE = "jabłko";
	private static final String ITEM_CARROT = "marchew";
	private static final String ITEM_MONEY = "money";
	private static final String ITEM_REINDEER_MOSS = "mech renifera";
	private static final String ITEM_SNOWGLOBE = "zima zaklęta w kuli";

	private static final String RUDOLPH_TALK_GREETING_DEFAULT = "Cześć jestem wesołym przyjacielem. Co za wspaniała pora roku!";
	private static final String RUDOLPH_TALK_GREETING_WITHOUT_GOODIES = "Oh nie mogę się doczekać tych przysmaków, o które cię prosiłem. Mam nadzieję, że nie będę czekał długo nim mi je przyniesiesz.";
	private static final String RUDOLPH_TALK_GREETING_WITH_GOODIES = "Przepraszam! Widzę, że masz pyszne smakołyki. Są one dla mnie?";
	private static final String RUDOLPH_TALK_QUEST_OFFER = "Chcę pyszne przysmaki tylko ty możesz mi pomóc je zdobyć. Czy możesz mi pomóc?";
	private static final String RUDOLPH_TALK_QUEST_REJECT = "W takim razie zapytam kogoś innego. Biada mi.";
	private static final String RUDOLPH_TALK_QUEST_ACCEPT = "Słyszałem o wspaniałych #przysmakach, które masz tutaj w Semos. Jeśli zdobędziesz 5 mchów renifera, 10 jabłek i 10 marchew to dam ci nagrodę.";
	private static final String RUDOLPH_TALK_QUEST_GOODIES_REFUSED = "Cóż mam nazieję, że znajdziesz jakieś przysmaki nim padnę z głodu.";
	private static final String RUDOLPH_TALK_QUEST_GOODIES_OFFERED = "Jestem tak podekscytowany! Tak chciałem je zjeść od dłuższego czasu. Dziękuję bardzo. Przytoczę powiedzenie Ho Ho Ho, Wesołych Świąt.";
	private static final String RUDOLPH_TALK_QUEST_TOO_SOON = "Dziękuję bardzo za przysmaki, ale nie mam dla ciebie w tym roku żadnych zadań. Korzystaj ze wspaniałego sezonu wakacyjnego.";

	private static final String HISTORY_DEFAULT = "Spotkałem Rudolpha. Jest on Czerwononosym Reniferem biegającym w pobliżu Semos.";
	private static final String HISTORY_REJECTED = "Zapytał mnie o znalezienie przysmaków dla niego, ale odrzuciłem jego prośbę.";
	private static final String HISTORY_START = "Przyrzekłem, że znajdę dla niego przysmaki ponieważ jest miłym reniferem.";
	private static final String HISTORY_GOT_GOODIES = "Mam wszystkie przysmaki, które zabiorę do Rudolpha.";
	private static final String HISTORY_COMPLETED_REPEATABLE = "Minął rok kiedy ostatnio pomogłem Rudolphowi. Powinienem go zapytać czy znów nie potrzebuje mojej pomocy.";
	private static final String HISTORY_COMPLETED_NOT_REPEATABLE = "Wziąłem przysmaki do Rudolpha. Jako podziękowanie otrzymałem trochę przysmaków. :)";

	private SpeakerNPC npc;
	private Engine en;

	private String questSlot;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone(ZONE_SEMOS);
	}

	public GoodiesForRudolphTest() {
		super(ZONE_SEMOS, NPC_RUDOLPH);
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		final StendhalRPZone cityZone = new StendhalRPZone(ZONE_SEMOS);
		new RudolphNPC().configureZone(cityZone, null);

		quest = new GoodiesForRudolph();
		quest.addToWorld();
		if (System.getProperty("stendhal.christmas") == null) {
			((GoodiesForRudolph) quest).addStepsToWorld();
		}

		questSlot = quest.getSlotName();
	}

	@Test
	public void testDidNotAskForQuest() {
		String responseToGreeting = startTalkingToNpc(NPC_RUDOLPH);
		assertEquals(RUDOLPH_TALK_GREETING_DEFAULT, responseToGreeting);

		assertNoHistory();
	}

	@Test
	public void testRefuseQuest() {
		String responseToGreeting = startTalkingToNpc(NPC_RUDOLPH);
		assertEquals(RUDOLPH_TALK_GREETING_DEFAULT, responseToGreeting);

		en.step(player, "quest");
		assertEquals(RUDOLPH_TALK_QUEST_OFFER, getReply(npc));

		en.step(player, "no");
		assertEquals(RUDOLPH_TALK_QUEST_REJECT, getReply(npc));

		assertEquals("rejected", player.getQuest(questSlot));
		assertTrue(npc.isTalking());
		assertLoseKarma(5);
		assertHistory(HISTORY_DEFAULT, HISTORY_REJECTED);
	}

	@Test
	public void testAcceptQuest() {
		String responseToGreeting = startTalkingToNpc(NPC_RUDOLPH);
		assertEquals(RUDOLPH_TALK_GREETING_DEFAULT, responseToGreeting);

		en.step(player, "quest");
		assertEquals(RUDOLPH_TALK_QUEST_OFFER, getReply(npc));

		en.step(player, "yes");
		assertEquals(RUDOLPH_TALK_QUEST_ACCEPT, getReply(npc));

		assertEquals("start", player.getQuest(questSlot));
		assertHistory(HISTORY_DEFAULT, HISTORY_START);
	}

	@Test
	public void testBackToRudolphWithoutGoodies() {
		player.setQuest(questSlot, "start");

		String responseToGreeting = startTalkingToNpc(NPC_RUDOLPH);
		assertEquals(RUDOLPH_TALK_GREETING_WITHOUT_GOODIES, responseToGreeting);

		en.step(player, "quest");
		assertEquals(RUDOLPH_TALK_QUEST_OFFER, getReply(npc));

		en.step(player, "yes");
		assertEquals(RUDOLPH_TALK_QUEST_ACCEPT, getReply(npc));

		prepareGoodies();

		assertEquals("start", player.getQuest(questSlot));
		assertHistory(HISTORY_DEFAULT, HISTORY_START, HISTORY_GOT_GOODIES);
	}

	@Test
	public void testPrepareGoodies() {
		String responseToGreeting = startTalkingToNpc(NPC_RUDOLPH);
		assertEquals(RUDOLPH_TALK_GREETING_DEFAULT, responseToGreeting);

		en.step(player, "quest");
		assertEquals(RUDOLPH_TALK_QUEST_OFFER, getReply(npc));

		en.step(player, "yes");
		assertEquals(RUDOLPH_TALK_QUEST_ACCEPT, getReply(npc));

		prepareGoodies();

		assertEquals("start", player.getQuest(questSlot));
		assertHistory(HISTORY_DEFAULT, HISTORY_START, HISTORY_GOT_GOODIES);
	}

	@Test
	public void testBackToRudolphRefuseGoodies() {
		player.setQuest(questSlot, "start");
		prepareGoodies();

		String responseToGreeting = startTalkingToNpc(NPC_RUDOLPH);
		assertEquals(RUDOLPH_TALK_GREETING_WITH_GOODIES, responseToGreeting);

		en.step(player, "no");
		assertEquals(RUDOLPH_TALK_QUEST_GOODIES_REFUSED, getReply(npc));

		assertTrue(npc.isTalking());

		assertEquals("start", player.getQuest(questSlot));
		assertHistory(HISTORY_DEFAULT, HISTORY_START, HISTORY_GOT_GOODIES);
	}

	@Test
	public void testBackToRudolphOfferGoodies() {
		long initialXp = player.getXP();
		player.setQuest(questSlot, "start");
		prepareGoodies();

		String responseToGreeting = startTalkingToNpc(NPC_RUDOLPH);
		assertEquals(RUDOLPH_TALK_GREETING_WITH_GOODIES, responseToGreeting);

		en.step(player, "yes");
		assertEquals(RUDOLPH_TALK_QUEST_GOODIES_OFFERED, getReply(npc));

		assertTrue(npc.isTalking());

		assertFalse(player.isEquipped(ITEM_APPLE));
		assertFalse(player.isEquipped(ITEM_CARROT));
		assertFalse(player.isEquipped(ITEM_REINDEER_MOSS));
		assertTrue(player.isEquipped(ITEM_SNOWGLOBE));
		assertTrue(player.isEquipped(ITEM_MONEY, 50));
		assertGainKarma(60);
		assertEquals(100L, player.getXP() - initialXp);
		assertEquals("done", player.getQuest(questSlot, 0));

		assertHistory(HISTORY_DEFAULT, HISTORY_START, HISTORY_GOT_GOODIES,
				HISTORY_COMPLETED_NOT_REPEATABLE);
	}

	@Test
	public void testBackToRudolphOfferThenHideGoodies() {
		player.setQuest(questSlot, "start");
		prepareGoodies();

		String responseToGreeting = startTalkingToNpc(NPC_RUDOLPH);
		assertEquals(RUDOLPH_TALK_GREETING_WITH_GOODIES, responseToGreeting);

		hideGoodies();

		en.step(player, "yes");
		assertEquals(null, getReply(npc));
		assertTrue(npc.isTalking());
	}

	@Test
	public void testAskQuestAgain() {
		player.setQuest(questSlot, 0, "done");
		PlayerTestHelper.setPastTime(player, questSlot, 1,
				TimeUnit.DAYS.toSeconds(365));

		String responseToGreeting = startTalkingToNpc(NPC_RUDOLPH);
		assertEquals(RUDOLPH_TALK_GREETING_DEFAULT, responseToGreeting);

		en.step(player, "task");
		assertEquals(RUDOLPH_TALK_QUEST_OFFER, getReply(npc));

		assertTrue(quest.isRepeatable(player));
		assertEquals("done", player.getQuest(questSlot, 0));
		assertHistory(HISTORY_DEFAULT, HISTORY_START, HISTORY_GOT_GOODIES,
				HISTORY_COMPLETED_REPEATABLE);
	}

	@Test
	public void testAskQuestAgainAndAccept() {
		player.setQuest(questSlot, 0, "done");
		PlayerTestHelper.setPastTime(player, questSlot, 1,
				TimeUnit.DAYS.toSeconds(365));

		String responseToGreeting = startTalkingToNpc(NPC_RUDOLPH);
		assertEquals(RUDOLPH_TALK_GREETING_DEFAULT, responseToGreeting);

		en.step(player, "task");
		assertEquals(RUDOLPH_TALK_QUEST_OFFER, getReply(npc));

		en.step(player, "yes");
		assertEquals(RUDOLPH_TALK_QUEST_ACCEPT, getReply(npc));

		assertEquals("start", player.getQuest(questSlot));
		assertHistory(HISTORY_DEFAULT, HISTORY_START);
	}

	@Test
	public void testAskQuestAgainTooSoon() {
		player.setQuest(questSlot, 0, "done");
		PlayerTestHelper.setPastTime(player, questSlot, 1,
				TimeUnit.DAYS.toSeconds(1));

		String responseToGreeting = startTalkingToNpc(NPC_RUDOLPH);
		assertEquals(RUDOLPH_TALK_GREETING_DEFAULT, responseToGreeting);

		en.step(player, "task");
		assertThat(getReply(npc), startsWith(RUDOLPH_TALK_QUEST_TOO_SOON));

		assertFalse(quest.isRepeatable(player));
		assertEquals("done", player.getQuest(questSlot, 0));
		assertHistory(HISTORY_DEFAULT, HISTORY_START, HISTORY_GOT_GOODIES,
				HISTORY_COMPLETED_NOT_REPEATABLE);
	}

	private String startTalkingToNpc(String name) {
		npc = SingletonRepository.getNPCList().get(name);
		en = npc.getEngine();

		en.step(player, "hi");
		return getReply(npc);
	}

	private void prepareGoodies() {
		PlayerTestHelper.equipWithStackableItem(player, ITEM_APPLE, 10);
		PlayerTestHelper.equipWithStackableItem(player, ITEM_CARROT, 10);
		for (int i = 0; i < 5; i++) {
			PlayerTestHelper.equipWithItem(player, ITEM_REINDEER_MOSS);
		}
	}

	private void hideGoodies() {
		player.drop(ITEM_APPLE, 10);
		player.drop(ITEM_CARROT, 10);
		for (int i = 0; i < 5; i++) {
			player.drop(ITEM_REINDEER_MOSS);
		}
	}
}
