/***************************************************************************
 *                   (C) Copyright 2003-2016 - Stendhal                    *
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
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.maps.ados.city.ManWithHatNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

public class KillMonksTest extends ZonePlayerAndNPCTestImpl {

	private static final String CITY_ZONE_NAME = "0_ados_city_s";

	private static final String NPC_TALK_QUEST_OFFER = "Moja kochana żona została zamordowana, gdy szła do Wo'fol, aby zamówić pizzę u Kroipa. Jacyś mnichowie ją napadli i nie miała szansy. Teraz chcę się zemścić! Może mi pomożesz?";
	private static final String NPC_TALK_QUEST_REJECT = "Co za szkoda... Może kiedyś zmienisz zdanie i pomożesz smutnemu człowiekowi.";
	private static final String NPC_TALK_QUEST_ACCEPT = "Dziękuję! Zabij 25 mnichów i 25 mnichów ciemności w imię mojej ukochanej żony.";
	private static final String NPC_TALK_QUEST_REMIND = "Proszę pomóż mi w dokonaniu zemsty!";
	private static final String NPC_TALK_QUEST_OFFER_AGAIN = "Ci mnichowie są okrutni, a ja wciąż nie mogę dokonać mojej zemsty. Może znów mi pomożesz?";
	private static final String NPC_TALK_QUEST_TOO_SOON = "Ci mnichowie dostali lekcje, ale możliwe, że znów będę potrzebował twojej pomocy za";

	private static final String NPC_TALK_BYE = "Do widzenia i dziękuję za rozmowę.";

	private static final String HISTORY_DEFAULT = "Spotkałem Andiego w mieście Ados. Poprosił mnie o pomszczenie jego żony.";
	private static final String HISTORY_REJECTED = "Odrzuciłem prośbę.";
	private static final String HISTORY_START = "Obiecałem zabić 25 mnichów i 25 mnichów ciemności, aby dokonać zemsty za żone Andiego.";
	private static final String HISTORY_STATUS = "Wciąż muszę zabić ";
	private static final String HISTORY_COMPLETED_REPEATABLE = "Po dwóch tygodniach powinienem może odwiedzić Andiego. Być może potrzebuje ponownie mojej pomocy!";
	private static final String HISTORY_COMPLETED_NOT_REPEATABLE = "Zabiłem paru mnichów, a Andi może teraz spać trochę spokojnie!";
	private static final String HISTORY_COMPLETED_ONCE = "Zemściłem się dla Andiego razy.";

	private static final String QUEST_STATE_JUST_STARTED = "start;mnich ciemności,0,25,0,0,mnich,0,25,0,0";

	private SpeakerNPC npc;
	private Engine en;

	private String questSlot;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone(CITY_ZONE_NAME);
	}

	public KillMonksTest() {
		setNpcNames("Andy");
		setZoneForPlayer(CITY_ZONE_NAME);
		addZoneConfigurator(new ManWithHatNPC(), CITY_ZONE_NAME);
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		quest = new KillMonks();
		quest.addToWorld();

		questSlot = quest.getSlotName();
	}

	@Test
	public void testRefuseQuest() {
		startTalkingToNpc("Andy");

		en.step(player, "quest");
		assertEquals(NPC_TALK_QUEST_OFFER, getReply(npc));

		en.step(player, "no");
		assertEquals(NPC_TALK_QUEST_REJECT, getReply(npc));

		assertEquals("rejected", player.getQuest(questSlot));
		assertTrue(npc.isTalking());
		assertLoseKarma(5);
		assertHistory(HISTORY_DEFAULT, HISTORY_REJECTED);
	}

	@Test
	public void testAcceptQuest() {
		startTalkingToNpc("Andy");

		en.step(player, "quest");
		assertEquals(NPC_TALK_QUEST_OFFER, getReply(npc));

		en.step(player, "yes");
		assertEquals(NPC_TALK_QUEST_ACCEPT, getReply(npc));

		assertEquals("start", player.getQuest(questSlot, 0));
		assertGainKarma(5);
		assertHistory(HISTORY_DEFAULT, HISTORY_START, HISTORY_STATUS + "0 mnichów i 0 mnichów ciemności.");
	}

	@Test
	public void testBackToNpc() {
		long initialXp = player.getXP();

		player.setQuest(questSlot, QUEST_STATE_JUST_STARTED);
		killCreatureShared("mnich", 25);
		killCreatureShared("mnich ciemności", 25);

		startTalkingToNpc("Andy");

		en.step(player, "task");
		assertThat(getReply(npc), startsWith("Bardzo dziękuję!"));

		assertTrue(player.isEquipped("zupa"));
		assertGainKarma(0);
		assertEquals(15000L, player.getXP() - initialXp);
		assertEquals("killed", player.getQuest(questSlot, 0));

		en.step(player, "bye");
		assertEquals(NPC_TALK_BYE, getReply(npc));
		assertHistory(HISTORY_DEFAULT, HISTORY_COMPLETED_NOT_REPEATABLE, HISTORY_COMPLETED_ONCE);
	}

	@Test
	public void testBackToNpcTooSoon() {
		player.setQuest(questSlot, QUEST_STATE_JUST_STARTED);
		killCreatureShared("mnich", 1);
		killCreatureShared("mnich ciemności", 2);

		startTalkingToNpc("Andy");

		en.step(player, "task");
		assertEquals(NPC_TALK_QUEST_REMIND, getReply(npc));

		assertEquals(QUEST_STATE_JUST_STARTED, player.getQuest(questSlot));
		assertHistory(HISTORY_DEFAULT, HISTORY_START, HISTORY_STATUS + "mnicha i 2 mnichów ciemności.");
	}

	@Test
	public void testAskQuestAgain() {
		player.setQuest(questSlot, 0, "killed");
		PlayerTestHelper.setPastTime(player, questSlot, 1, TimeUnit.DAYS.toSeconds(14));

		startTalkingToNpc("Andy");

		en.step(player, "task");
		assertEquals(NPC_TALK_QUEST_OFFER_AGAIN, getReply(npc));

		assertTrue(quest.isRepeatable(player));
		assertEquals("killed", player.getQuest(questSlot, 0));
		assertHistory(HISTORY_DEFAULT, HISTORY_COMPLETED_REPEATABLE);
	}

	@Test
	public void testAskQuestAgainTooSoon() {
		player.setQuest(questSlot, 0, "killed");
		PlayerTestHelper.setPastTime(player, questSlot, 1, TimeUnit.DAYS.toSeconds(1));

		startTalkingToNpc("Andy");

		en.step(player, "task");
		assertThat(getReply(npc), startsWith(NPC_TALK_QUEST_TOO_SOON));

		assertFalse(quest.isRepeatable(player));
		assertEquals("killed", player.getQuest(questSlot, 0));
		assertHistory(HISTORY_DEFAULT, HISTORY_COMPLETED_NOT_REPEATABLE);
	}

	private void startTalkingToNpc(String name) {
		npc = SingletonRepository.getNPCList().get(name);
		en = npc.getEngine();

		en.step(player, "hi");
		getReply(npc);
	}

	private void killCreatureShared(String creatureName, int count) {
		for (int i = 0; i < count; i++) {
			player.setSharedKill(creatureName);
		}
	}
}
