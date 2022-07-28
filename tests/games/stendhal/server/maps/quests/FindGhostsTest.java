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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.ados.city.KidGhostNPC;
import games.stendhal.server.maps.ados.hauntedhouse.WomanGhostNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class FindGhostsTest {

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;
	private SpeakerNPC npcGhost = null;
	private Engine enGhost = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone("admin_test");
		ZoneConfigurator zoneConf = new WomanGhostNPC();
		zoneConf.configureZone(zone, null);
		npc = SingletonRepository.getNPCList().get("Carena");
		en = npc.getEngine();

		zoneConf = new KidGhostNPC();
		zoneConf.configureZone(zone, null);

		zoneConf = new games.stendhal.server.maps.athor.cave.GhostNPC();
		zoneConf.configureZone(zone, null);

		zoneConf = new games.stendhal.server.maps.orril.dungeon.GhostNPC();
		zoneConf.configureZone(zone, null);

		zoneConf = new games.stendhal.server.maps.wofol.house5.GhostNPC();
		zoneConf.configureZone(zone, null);

		final AbstractQuest quest = new FindGhosts();
		quest.addToWorld();
		en = npc.getEngine();

		player = PlayerTestHelper.createPlayer("player");
	}

	@org.junit.After
	public void tearDown() throws Exception {
		SingletonRepository.getNPCList().clear();
	}

	/**
	 * Tests for rejectQuest.
	 */
	@Test
	public void testRejectQuest() {
		en.step(player, "hi");
		assertEquals("Ałłłuuuuuuu!", getReply(npc));
		en.step(player, "help");
		assertEquals(
				"Tutaj jest ostrzeżenie: Jeżeli zginiesz to staniesz się duchem jak ja częściowo widocznym i niematerialnym. Jeżeli znajdziesz swoją drogę wyjścia z afterlife to narodzisz się na nowo.",
				getReply(npc));
		en.step(player, "task");
		assertEquals(
				"Czuję się taka samotna. Spotykam tylko potwory i żywych ludzi. Jeżeli wiedziałabym o innych #duchach to poczułabym się lepiej.",
				getReply(npc));
		en.step(player, "spirits");
		assertEquals(
				"Czuję, że są 4 inne duchy. Gdybym znała ich imiona to mogłabym się z nimi skontaktować. Znajdziesz je i powiesz mi ich imiona?",
				getReply(npc));

		// test case insensitive recognition of "no" and see if "0" is correctly handled as distinct
		assertTrue(en.step(player, "NO"));

		assertEquals("rejected", player.getQuest("find_ghosts"));
		assertEquals("Och nieważne. Może dlatego, że jestem duchem to nie mogę zaoferować Ci lepszej nagrody.",
				getReply(npc));
		en.step(player, "bye");
		assertEquals("Żegnaj.", getReply(npc));
	}

	/**
	 * Tests for acceptQuest.
	 */
	@Test
	public void testAcceptQuest() {
		en.step(player, "hi");
		assertEquals("Ałłłuuuuuuu!", getReply(npc));
		en.step(player, "task");
		assertEquals(
				"Czuję się taka samotna. Spotykam tylko potwory i żywych ludzi. Jeżeli wiedziałabym o innych #duchach to poczułabym się lepiej.",
				getReply(npc));
		en.step(player, "yes");
		assertEquals("To wspaniale z twojej strony. Powodzenia w szukaniu ich.", getReply(npc));
		en.step(player, "hi");
		en.step(player, "bye");
		assertEquals("looking:said", player.getQuest("find_ghosts"));
		assertEquals("Żegnaj.", getReply(npc));
	}

	/**
	 * Tests for rejectthenAcceptQuest.
	 */
	@Test
	public void testRejectthenAcceptQuest() {
		en.step(player, "hi");
		assertEquals("Ałłłuuuuuuu!", getReply(npc));
		en.step(player, "help");
		assertEquals(
				"Tutaj jest ostrzeżenie: Jeżeli zginiesz to staniesz się duchem jak ja częściowo widocznym i niematerialnym. Jeżeli znajdziesz swoją drogę wyjścia z afterlife to narodzisz się na nowo.",
				getReply(npc));
		en.step(player, "task");
		assertEquals(
				"Czuję się taka samotna. Spotykam tylko potwory i żywych ludzi. Jeżeli wiedziałabym o innych #duchach to poczułabym się lepiej.",
				getReply(npc));
		en.step(player, "spirits");
		assertEquals(
				"Czuję, że są 4 inne duchy. Gdybym znała ich imiona to mogłabym się z nimi skontaktować. Znajdziesz je i powiesz mi ich imiona?",
				getReply(npc));
		assertTrue(en.step(player, "no"));
		assertEquals("rejected", player.getQuest("find_ghosts"));
		assertEquals("Och nieważne. Może dlatego, że jestem duchem to nie mogę zaoferować Ci lepszej nagrody.",
				getReply(npc));
		en.step(player, "bye");
		assertEquals("Żegnaj.", getReply(npc));

		en.step(player, "hi");
		assertEquals("Ałłłuuuuuuu!", getReply(npc));
		assertTrue(en.step(player, "task"));
		assertEquals(
				"Czuję się taka samotna. Spotykam tylko potwory i żywych ludzi. Jeżeli wiedziałabym o innych #duchach to poczułabym się lepiej.",
				getReply(npc));
		en.step(player, "yes");
		assertEquals("To wspaniale z twojej strony. Powodzenia w szukaniu ich.", getReply(npc));
		en.step(player, "hi");
		en.step(player, "bye");
		assertEquals("looking:said", player.getQuest("find_ghosts"));
		assertEquals("Żegnaj.", getReply(npc));
	}

	/**
	 * Tests for quest.
	 */
	@Test
	public void testQuest() {
		player.setXP(28900);
		player.setQuest("find_ghosts", "looking:said");
		assertEquals("looking:said", player.getQuest("find_ghosts"));

		int oldxp = player.getXP();
		npcGhost = SingletonRepository.getNPCList().get("Mary");
		enGhost = npcGhost.getEngine();
		enGhost.step(player, "hi");
		assertEquals("Pamiętasz moje imię ... Mary ... Mary ...", getReply(npcGhost));
		assertEquals(oldxp + 100, player.getXP());
		assertThat(player.getQuest("find_ghosts") , containsString("mary"));

		// [22:26] superkym earns 100 experience points.

		// -----------------------------------------------
		assertThat(player.getQuest("find_ghosts") , not(containsString("Ben")));

		oldxp = player.getXP();
		npcGhost = SingletonRepository.getNPCList().get("Ben");
		enGhost = npcGhost.getEngine();
		enGhost.step(player, "hi");
		assertEquals(
				"Cześć! Każdemu jest ciężko ze mną rozmawiać. Inne dzieci udają, że nie istnieję. Mam nadzieję, że mnie pamiętasz.",
				getReply(npcGhost));
		// [22:26] superkym earns 100 experience points.
		assertEquals(oldxp + 100, player.getXP());
		assertThat(player.getQuest("find_ghosts").toLowerCase() , containsString("ben"));

		// -----------------------------------------------
		assertThat(player.getQuest("find_ghosts") , not(containsString("goran")));

		oldxp = player.getXP();
		npcGhost = SingletonRepository.getNPCList().get("Goran");
		enGhost = npcGhost.getEngine();
		enGhost.step(player, "hi");
		assertEquals("Pamiętasz moje imię ... Goran ... Goran ...", getReply(npcGhost));
		// [22:26] superkym earns 100 experience points.
		assertEquals(oldxp + 100, player.getXP());
		assertThat(player.getQuest("find_ghosts") , containsString("goran"));

		// -----------------------------------------------
		oldxp = player.getXP();
		assertThat(player.getQuest("find_ghosts") , not(containsString("zak")));

		npcGhost = SingletonRepository.getNPCList().get("Zak");
		enGhost = npcGhost.getEngine();
		enGhost.step(player, "hi");
		assertEquals("Pamiętasz moje imię ... Zak ... Zak ...", getReply(npcGhost));
		// [22:26] superkym earns 100 experience points.
		assertEquals(oldxp + 100, player.getXP());
		assertThat(player.getQuest("find_ghosts") , containsString("zak"));

		// -----------------------------------------------
		oldxp = player.getXP();
		final int oldHP = player.getBaseHP();
		en.step(player, "hi");
		assertEquals("Jeżeli znajdziesz #duchy to proszę wyjaw mi ich imiona.", getReply(npc));
		en.step(player, "yes");
		assertEquals("Przepraszam, ale nie rozumiem Ciebie. Jakie to było imię?", getReply(npc));

		en.step(player, "spirits");
		assertEquals(
				"Chcę dowiedzieć się więcej o innych duchach, które wędrują po ziemskim świecie jako duchy tak jak ja. Proszę, wyjaw mi ich imiona.",
				getReply(npc));

		assertThat(player.getQuest("find_ghosts").split(":")[0], containsString("mary"));
		assertThat(player.getQuest("find_ghosts").split(":")[1], not(containsString("mary")));
		en.step(player, "Mary");
		assertEquals("Dziękuje. Jeżeli spotkasz inne duchy to proszę powiedz mi ich imiona.", getReply(npc));
		assertThat(player.getQuest("find_ghosts").split(":")[1], containsString("mary"));
		assertThat(player.getQuest("find_ghosts").split(":")[0], not(containsString("Mary")));

		en.step(player, "Mary");
		assertEquals("Już mi wyjawiłeś imię tego ducha, dziękuję. Jeżeli spotkasz inne duchy to proszę powiedz mi ich imiona.", getReply(npc));

		en.step(player, "Brandy");
		assertEquals("Przepraszam, ale nie rozumiem Ciebie. Jakie to było imię?", getReply(npc));

		en.step(player, "spirits");
		assertEquals(
				"Chcę dowiedzieć się więcej o innych duchach, które wędrują po ziemskim świecie jako duchy tak jak ja. Proszę, wyjaw mi ich imiona.",
				getReply(npc));

		assertThat(player.getQuest("find_ghosts").split(":")[1], not(containsString("ben")));
		en.step(player, "Ben");
		assertEquals("Dziękuje. Jeżeli spotkasz inne duchy to proszę powiedz mi ich imiona.", getReply(npc));
		assertThat(player.getQuest("find_ghosts").split(":")[1], containsString("ben"));

		en.step(player, "spirits");
		assertEquals(
				"Chcę dowiedzieć się więcej o innych duchach, które wędrują po ziemskim świecie jako duchy tak jak ja. Proszę, wyjaw mi ich imiona.",
				getReply(npc));

		assertThat(player.getQuest("find_ghosts").split(":")[1], not(containsString("zak")));
		en.step(player, "Zak");
		assertEquals("Dziękuje. Jeżeli spotkasz inne duchy to proszę powiedz mi ich imiona.", getReply(npc));
		assertThat(player.getQuest("find_ghosts").split(":")[1], containsString("zak"));

		en.step(player, "spirits");
		assertEquals(
				"Chcę dowiedzieć się więcej o innych duchach, które wędrują po ziemskim świecie jako duchy tak jak ja. Proszę, wyjaw mi ich imiona.",
				getReply(npc));

		assertThat(player.getQuest("find_ghosts"), not(containsString("Goran")));
		en.step(player, "Goran");
		assertEquals(
				"Dziękuje. Znam teraz 4 inne duchy. Może teraz będę mogła się skontaktować z nimi za pomocą telepatii. Nie mogłam dać Ci nic z materialnych rzeczy i dlatego zwiększyłam twoją żywotność. Będziesz mógł żyć dłużej.",
				getReply(npc));
		assertThat(player.getQuest("find_ghosts"), is("done"));
		// [22:27] superkym heals 50 health points.
		// [22:27] superkym earns 5000 experience points.
		en.step(player, "bye");
		assertEquals("Żegnaj.", getReply(npc));
		assertEquals(oldxp + 5000, player.getXP());
		assertEquals(oldHP + 100, player.getBaseHP());
		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Ałłłuuuuuuu!", getReply(npc));
		en.step(player, "task");
		assertEquals("Dziękuję! Czuję się teraz lepiej znając imiona innych duchów w Faiumoni.",
				getReply(npc));
		en.step(player, "bye");
		assertEquals("Żegnaj.", getReply(npc));
	}
}
