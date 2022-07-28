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

import static org.junit.Assert.assertEquals;
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
import games.stendhal.server.maps.semos.blacksmith.BlacksmithAssistantNPC;
import games.stendhal.server.maps.semos.tavern.TraderNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class NewsFromHackimTest {

	private Player player = null;
	private SpeakerNPC npcHackim = null;
	private Engine enHackim = null;
	private SpeakerNPC npcXin = null;
	private Engine enXin = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone("admin_test");

		new BlacksmithAssistantNPC().configureZone(zone, null);
		npcHackim = SingletonRepository.getNPCList().get("Hackim Easso");

		enHackim = npcHackim.getEngine();

		final ZoneConfigurator zoneConf = new TraderNPC();
		zoneConf.configureZone(new StendhalRPZone("int_semos_tavern"), null);
		npcXin = SingletonRepository.getNPCList().get("Xin Blanca");
		enXin = npcXin.getEngine();

		final AbstractQuest quest = new NewsFromHackim();
		quest.addToWorld();

		player = PlayerTestHelper.createPlayer("player");
	}

	/**
	 * Tests for quest.
	 */
	@Test
	public void testQuest() {
		enHackim.step(player, "hi");
		assertEquals("Witaj nieznajomy. Zwą mnie Hackim Easso i jestem czeladnikiem kowala. Czy przybyłeś tu by kupić broń?", getReply(npcHackim));
		enHackim.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npcHackim));

		// -----------------------------------------------

		enHackim.step(player, "hi");
		assertEquals("Witaj ponownie player. W czym mogę #pomóc?", getReply(npcHackim));
		enHackim.step(player, "task");
		assertEquals("Pssst! Podejdź tutaj... zrób mi przysługę i powiedz #Xin #Blanca że nowa dostawa broni jest gotowa. Powiesz mu?", getReply(npcHackim));
		enHackim.step(player, "Xin");
		assertEquals("Nie wiesz kto to jest Xin? Każdy w oberży zna Xina. Jest to facet, który większości ludzi w Semos jest winny pieniądze za piwo! Zrobisz to?", getReply(npcHackim));
		enHackim.step(player, "yes");
		assertEquals("Dziękuję! Jestem pewien, że #Xin dobrze Cię wynagrodzi. Daj znać jeżeli czegoś byś potrzebował.", getReply(npcHackim));
		enHackim.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npcHackim));

		// -----------------------------------------------

		enXin.step(player, "hi");
		assertEquals("W końcu gotowe! To dobra wiadomość! Pozwól mi się odwdzięczyć za twoją pomoc... Weź te nowe skórzane spodnie! Daj mi znać jeżeli będziesz czegoś potrzebował.", getReply(npcXin));
		// [22:38] rosie earns 10 experience points.
		enXin.step(player, "task");
		assertEquals("Porozmawiaj z Hackim Easso w kuźni. Może on będzie Cię potrzebował.", getReply(npcXin));

		// -----------------------------------------------

		enHackim.step(player, "hi");
		assertEquals("Witaj ponownie player. W czym mogę #pomóc?", getReply(npcHackim));
		enHackim.step(player, "task");
		assertEquals("Dziękuję, ale nie mam wiadomości dla #Xin. Nie mogę zbyt często przemycać... sądzę, że Xoderos zaczyna coś podejrzewać. W każdym razie daj mi znać czy coś mógłbym zrobić dla Ciebie.", getReply(npcHackim));
		enHackim.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npcHackim));
	}
}
