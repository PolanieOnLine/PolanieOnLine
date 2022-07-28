/***************************************************************************
 *                   (C) Copyright 2003-2022 - Stendhal                    *
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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.semos.plains.MillerNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

/**
 * Tests for Jenny's quest to kill gnomes
 *
 * @author IschBing, hendrik
 */
public class KillGnomesTest {
	private static final String QUEST_VALUE_STARTED = "start;gnom,0,1,0,0,gnom zwiadowca,0,1,0,0,gnom kawalerzysta,0,1,0,0";

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;
	private static String questSlot;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		final StendhalRPZone zone = new StendhalRPZone("admin_test");
		new MillerNPC().configureZone(zone, null);
		AbstractQuest quest = new BuiltQuest(new KillGnomes().story());
		questSlot = quest.getSlotName();
		quest.addToWorld();
	}


	@Before
	public void setUp() {
		player = PlayerTestHelper.createPlayer("bob");
		npc = SingletonRepository.getNPCList().get("Jenny");
		en = npc.getEngine();
	}


	/**
	 * ask for the quest.
	 */
	@Test
	public void testAskForQuest() {

		// Ask for the quest
		en.step(player, "hi");
		assertEquals("Pozdrawiam! Nazywam się Jenny jestem szefową tutejszego młyna. Jeżeli przyniesiesz mi #kłosy zboża to zmielę je dla Ciebie na mąkę. Powiedz tylko #zmiel ilość #mąka.", getReply(npc));
		en.step(player, "task");
		assertEquals("Gnomy kradną marchewki z naszej farmy na północ od Semos. Potrzebują chyba dobrej lekcji. Pomożesz?", getReply(npc));

		// Accept quest
		en.step(player, "yes");
		assertEquals("Doskonale. Obozowisko gnomów znajdziesz na północny-zachód od Semos. Upewnij się, że ubiłeś kilku liderów, conajmniej jednego zwiadowcę i jednego kawalerzystę.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		assertThat(player.getQuest(questSlot, 0), equalTo("start"));
	}


	/**
	 * return without having killed the gomes but trying to cheat Jenny
	 */
	@Test
	public void returnWithoutCompleting() {
		player.setQuest(questSlot, QUEST_VALUE_STARTED);
		en.step(player, "hi");
		assertEquals("Pozdrawiam! Nazywam się Jenny jestem szefową tutejszego młyna. Jeżeli przyniesiesz mi #kłosy zboża to zmielę je dla Ciebie na mąkę. Powiedz tylko #zmiel ilość #mąka.", getReply(npc));
		en.step(player, "done");
		assertEquals("Musisz nauczyć te zuchwałe gnomy lekcji zabijając kilku dla przykładu! Upewnij się, że dostałeś kilku liderów, co najmniej jednego zwiadowcę i jednego kawalerzystę.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		assertThat(player.getQuest(questSlot), equalTo(QUEST_VALUE_STARTED));
	}


	/**
	 * Now kills the gnomes and complete the quest correctly.
	 */
	@Test
	public void returnAfterCompleting() {
		player.setQuest(questSlot, QUEST_VALUE_STARTED);

		// kill gnomes
		player.setSoloKill("gnom");
		player.setSoloKill("gnom zwiadowca");
		player.setSoloKill("gnom kawalerzysta");

		// complete quest
		en.step(player, "hi");
		assertEquals("Widzę, że ubiłeś gnomy, które okradały farmę. Mam nadzieje, że przez jakiś czas nie będą się zbliżać do marchewek! Proszę weź te mikstury w dowód uznania.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		assertThat(player.getQuest(questSlot, 0), equalTo("done"));
	}


	/**
	 * Ask again directly after the quest was completed
	 */
	@Test
	public void askForQuestAgain() {
		player.setQuest(questSlot, "done;" + System.currentTimeMillis());

		// ask for quest again
		en.step(player, "hi");
		assertEquals("Pozdrawiam! Nazywam się Jenny jestem szefową tutejszego młyna. Jeżeli przyniesiesz mi #kłosy zboża to zmielę je dla Ciebie na mąkę. Powiedz tylko #zmiel ilość #mąka.", getReply(npc));
		en.step(player, "task");
		assertEquals("Gnomy nie sprawiają problemu od momentu, gdy pokazałeś im czym jest pokora.", getReply(npc));

		// help should still work
		en.step(player, "help");
		assertEquals("Czy znasz piekarnię w Semos? Z dumą mogę powiedzieć, że używają mojej mąki. Ale ostatnio wilki znowu zjadły mojego dostawcę... albo może uciekł... hmm.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
	}
}
