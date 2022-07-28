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
package games.stendhal.server.maps.quests;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.orril.dungeon.RatChild1NPC;
import games.stendhal.server.maps.orril.dungeon.RatChild2NPC;
import games.stendhal.server.maps.orril.dungeon.RatChildBoy1NPC;
import games.stendhal.server.maps.orril.dungeon.RatChildBoy2NPC;
import games.stendhal.server.maps.ratcity.house1.OldRatWomanNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class FindRatChildrenTest {

	private static final String QUEST_SLOT = "find_rat_kids";

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {

		final StendhalRPZone zone = new StendhalRPZone("admin_test");

		new OldRatWomanNPC().configureZone(zone, null);
		new RatChildBoy1NPC().configureZone(zone, null);
		new RatChildBoy2NPC().configureZone(zone, null);
		new RatChild1NPC().configureZone(zone, null);
		new RatChild2NPC().configureZone(zone, null);

		AbstractQuest quest = new FindRatChildren();
		quest.addToWorld();

		player = PlayerTestHelper.createPlayer("bob");
	}

	@After
	public void tearDown() {
		PlayerTestHelper.removeNPC("Agnus");
		PlayerTestHelper.removeNPC("Avalon");
		PlayerTestHelper.removeNPC("Cody");
		PlayerTestHelper.removeNPC("Mariel");
		PlayerTestHelper.removeNPC("Opal");
	}

	@Test
	public void testMeetingKidBeforeQuestStarted() {

		// haven't started quest yet
		assertNull(player.getQuest(QUEST_SLOT));

		npc = SingletonRepository.getNPCList().get("Cody");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Moja mama mówi mi, że nie powinienem rozmawiać z obcymi.", getReply(npc));
		en.step(player, "bye");
	}
		// -----------------------------------------------

	@Test
	public void testStartQuest() {

		npc = SingletonRepository.getNPCList().get("Agnus");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Witaj.", getReply(npc));
		en.step(player, "help");
		assertEquals("Nie mogę Ci w niczym pomóc.", getReply(npc));
		en.step(player, "job");
		assertEquals("Nie zostawiaj moich dzieci na pastwę losu. Popatrz za nimi co jakiś czas.", getReply(npc));
		en.step(player, "task");
		assertEquals("Jestem bardzo zmartwiona. Gdybym tylko wiedziała, że moje #dzieci są bezpieczne, czułabym się lepiej.", getReply(npc));
		en.step(player, "children");
		assertEquals("Moje dzieci poszły bawić się gdzieś w kanałach. Minęło już sporo czasu od tego momentu. Znajdziesz je i sprawdzisz czy u nich jest wszystko w porządku?", getReply(npc));
		en.step(player, "no");
		assertEquals("Hmmm... Nic nie szkodzi. Jestem pewna, że znajdzie się ktoś kto mi pomoże.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// check quest slot
		assertEquals(player.getQuest(QUEST_SLOT),"rejected");

		en.step(player, "hi");
		assertEquals("Witaj.", getReply(npc));
		en.step(player, "task");
		assertEquals("Jestem bardzo zmartwiona. Gdybym tylko wiedziała, że moje #dzieci są bezpieczne, czułabym się lepiej.", getReply(npc));
		en.step(player, "children");
		assertEquals("Moje dzieci poszły bawić się gdzieś w kanałach. Minęło już sporo czasu od tego momentu. Znajdziesz je i sprawdzisz czy u nich jest wszystko w porządku?", getReply(npc));
		en.step(player, "yes");
		assertEquals("To bardzo miłe z twojej strony. Powodzenia w poszukiwaniach.", getReply(npc));
		en.step(player, "task");
		assertEquals("Dlaczego moje dzieci są tak długo poza domem? Poszukaj je i sprawdź czy wszystko z nimi w porządku.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// check quest slot
		assertEquals(player.getQuest(QUEST_SLOT),"looking:said");
	}

	@Test
	public void testNamingKidsThatDontExistOrNotMet() {

		npc = SingletonRepository.getNPCList().get("Agnus");
		en = npc.getEngine();

		// the state wasn't remembered across the new test method so we need to set it to what it was when we ended the last
		player.setQuest(QUEST_SLOT, "looking:said");

		en.step(player, "hi");
		assertEquals("Jeżeli znajdziesz moje #dziecko to podaj mi jego imię.", getReply(npc));
		en.step(player, "children");
		assertEquals("Przepraszam, ale nie zrozumiałam ciebie. Jakie imię powiedziałeś?", getReply(npc));
		en.step(player, "unknownchild");
		assertEquals("Przepraszam, ale nie zrozumiałam ciebie. Jakie imię powiedziałeś?", getReply(npc));
		en.step(player, "banana");
		assertEquals("Przepraszam, ale nie zrozumiałam ciebie. Jakie imię powiedziałeś?", getReply(npc));
		en.step(player, "Cody");
		assertEquals("Czy aby na pewno widziałeś to dziecko, chyba mnie oszukujesz. Jeżeli widziałeś inne z moich dzieci to proszę powiedz mi które.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Nie ma problemu, wróć później.", getReply(npc));

		// check quest slot
		assertEquals(player.getQuest(QUEST_SLOT),"looking:said");
	}

	@Test
	public void testMeetingCodyAfterQuestStarted() {
		npc = SingletonRepository.getNPCList().get("Cody");
		en = npc.getEngine();

		// the state wasn't remembered across the new test method so we need to set it to what it was when we ended the last
		player.setQuest(QUEST_SLOT, "looking:said");

		// remember the xp and karma, did it go up?
		final int xp = player.getXP();

		en.step(player, "hi");
		assertEquals("Cześć nazywam się Cody. Proszę powiedz mojej mamie, że ze mną jest wszystko w porządku.", getReply(npc));
		// [11:49] kymara earns 500 experience points.

		assertThat(player.getXP(), greaterThan(xp));
		// check quest slot
		assertEquals(player.getQuest(QUEST_SLOT),"looking;cody:said");

		// return after having met in this quest run
		en.step(player, "hi");
		assertEquals("Witaj ponownie.", getReply(npc));

		// check quest slot
		assertEquals(player.getQuest(QUEST_SLOT),"looking;cody:said");
	}

	@Test
	public void testNamingKidsMet() {

		npc = SingletonRepository.getNPCList().get("Agnus");
		en = npc.getEngine();

		// the state wasn't remembered across the new test method so we need to set it to what it was when we ended the last
		player.setQuest(QUEST_SLOT, "looking;cody:said");

		en.step(player, "hi");
		assertEquals("Jeżeli znajdziesz moje #dziecko to podaj mi jego imię.", getReply(npc));
		en.step(player, "children");
		assertEquals("Przepraszam, ale nie zrozumiałam ciebie. Jakie imię powiedziałeś?", getReply(npc));
		en.step(player, "CODY");
		assertEquals("Dziękuję. Jeżeli widziałeś inne z moich dzieci to proszę powiedz mi które.", getReply(npc));
		en.step(player, "mariel");
		assertEquals("Czy aby na pewno widziałeś to dziecko, chyba mnie oszukujesz. Jeżeli widziałeś inne z moich dzieci to proszę powiedz mi które.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Nie ma problemu, wróć później.", getReply(npc));

		// check quest slot
		assertEquals(player.getQuest(QUEST_SLOT),"looking;cody:said;cody");
	}

	@Test
	public void testMeetingRemainingKids() {
		npc = SingletonRepository.getNPCList().get("Mariel");
		en = npc.getEngine();

		// the state wasn't remembered across the new test method so we need to set it to what it was when we ended the last
		player.setQuest(QUEST_SLOT, "looking;cody:said;cody");

		en.step(player, "hi");
		assertEquals("Cześć nazywam się Mariel. Proszę powiedz mojej mamie, że ze mną jest wszystko w porządku.", getReply(npc));
		// [11:49] kymara earns 500 experience points.

		// -----------------------------------------------

		npc = SingletonRepository.getNPCList().get("Opal");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Cześć nazywam się Opal. Proszę powiedz mojej mamie, że ze mną jest wszystko w porządku.", getReply(npc));
		// [11:50] kymara earns 500 experience points.

		// -----------------------------------------------
		npc = SingletonRepository.getNPCList().get("Avalon");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Cześć nazywam się Avalon. Proszę powiedz mojej mamie, że ze mną jest wszystko w porządku.", getReply(npc));
		// [11:50] kymara earns 500 experience points.

		// check quest slot
		assertEquals(player.getQuest(QUEST_SLOT),"looking;cody;mariel;opal;avalon:said;cody");

	}
	@Test
	public void testNamingRemainingKids() {

		npc = SingletonRepository.getNPCList().get("Agnus");
		en = npc.getEngine();

		// the state wasn't remembered across the new test method so we need to set it to what it was when we ended the last
		player.setQuest(QUEST_SLOT, "looking;cody;mariel;opal;avalon:said;cody");

		en.step(player, "hi");
		assertEquals("Jeżeli znajdziesz moje #dziecko to podaj mi jego imię.", getReply(npc));
		en.step(player, "mariel");
		assertEquals("Dziękuję. Jeżeli widziałeś inne z moich dzieci to proszę powiedz mi które.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Nie ma problemu, wróć później.", getReply(npc));

		// check quest slot
		assertEquals(player.getQuest(QUEST_SLOT),"looking;cody;mariel;opal;avalon:said;cody;mariel");

		// remember the xp and karma, did it go up?
		final int xp = player.getXP();
		final double karma = player.getKarma();

		en.step(player, "hi");
		assertEquals("Jeżeli znajdziesz moje #dziecko to podaj mi jego imię.", getReply(npc));
		en.step(player, "avalon");
		assertEquals("Dziękuję. Jeżeli widziałeś inne z moich dzieci to proszę powiedz mi które.", getReply(npc));
		// test saying a name we already had given
		en.step(player, "Cody");
		assertEquals("Już mi mówiłeś, że z tym dzieckiem jest wszystko dobrze. Jeżeli widziałeś inne z moich dzieci to proszę powiedz mi które.", getReply(npc));
		en.step(player, "Opal");
		assertEquals("Dziękuję. Uff... teraz mogę odsapnąć wiedząc, że z dziećmi jest wszystko w porządku.", getReply(npc));
		// [11:50] kymara earns 5000 experience points.
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		assertThat(player.getXP(), greaterThan(xp));
		assertThat(player.getKarma(), greaterThan(karma));

		// check quest slot
		assertTrue(player.isQuestCompleted(QUEST_SLOT));
	}

	@Test
	public void testReturningBeforeTimePassed() {

		npc = SingletonRepository.getNPCList().get("Agnus");
		en = npc.getEngine();

		// the state wasn't remembered across the new test method so we need to set it to what it was when we ended the last
		player.setQuest(QUEST_SLOT, "done;"+System.currentTimeMillis());

		en.step(player, "hi");
		assertEquals("Witaj.", getReply(npc));
		en.step(player, "task");
		assertEquals("Dziękuję! Czuję się lepiej wiedząc, że są bezpieczne.", getReply(npc));
		en.step(player, "yes");
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
	}

    @Test
	public void testReturningAfterTimePassed() {

		// [11:51] Admin kymara changed your state of the quest 'find_rat_kids' from 'done;1270205441630' to 'done;1'
		// [11:51] Changed the state of quest 'find_rat_kids' from 'done;1270205441630' to 'done;1'

		// the state wasn't remembered across the new test method so we need to set it to what it was when we ended the last
		player.setQuest(QUEST_SLOT, "done;1");

		npc = SingletonRepository.getNPCList().get("Agnus");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Pomożesz mi jeszcze raz odszukać moje dzieci?", getReply(npc));
		en.step(player, "yes");
		assertEquals("To bardzo miłe z twojej strony. Powodzenia w poszukiwaniach.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		npc = SingletonRepository.getNPCList().get("Cody");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Cześć nazywam się Cody. Proszę powiedz mojej mamie, że ze mną jest wszystko w porządku.", getReply(npc));
		// [11:51] kymara earns 500 experience points.
		en.step(player, "bye");
	}
}
