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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.ados.market.FishSoupMakerNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class FishSoupTest {

	private static final String QUEST_SLOT = "fishsoup_maker";

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		ItemTestHelper.generateRPClasses();
	}

	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone("0_ados_city_n2");
		MockStendlRPWorld.get().addRPZone(zone);
		new FishSoupMakerNPC().configureZone(zone, null);

		AbstractQuest quest = new FishSoup();
		quest.addToWorld();

		player = PlayerTestHelper.createPlayer("bob");
	}

	@Test
	public void testQuest() {

		npc = SingletonRepository.getNPCList().get("Florence Boullabaisse");
		en = npc.getEngine();
		player.setXP(100);
		en.step(player, "hi");
		assertEquals("Część i witam na rynku w Ados! Mam coś naprawdę smacznego dla Ciebie. Chciałbyś #zobaczyć?", getReply(npc));
		en.step(player, "revive");
		assertEquals("Moja specjalna zupa rybna ma magiczny smak. Chcę, abyś przyniósł mi potrzebne #składniki.", getReply(npc));
		en.step(player, "ingredients");
		assertEquals("Potrzebuję 11 składników nim ugotuję zupę: #pokolec, #dorsz, #'palia alpejska', #płotka, #błazenek, #cebula, #makrela, #czosnek, #por, #okoń, oraz #pomidor. Zbierzesz je?", getReply(npc));
		en.step(player, "no");
		assertEquals("Mam nadzieję, że kiedyś zmienisz zdanie. Tracisz to co najlepsze!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Życzę miłego pobytu na rynku w Ados!", getReply(npc));
		en.step(player, "hi");
		assertEquals("Część i witam na rynku w Ados! Mam coś naprawdę smacznego dla Ciebie. Chciałbyś #zobaczyć?", getReply(npc));
		en.step(player, "revive");
		assertEquals("Moja specjalna zupa rybna ma magiczny smak. Chcę, abyś przyniósł mi potrzebne #składniki.", getReply(npc));
		en.step(player, "ingredients");
		assertEquals("Potrzebuję 11 składników nim ugotuję zupę: #pokolec, #dorsz, #'palia alpejska', #płotka, #błazenek, #cebula, #makrela, #czosnek, #por, #okoń, oraz #pomidor. Zbierzesz je?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dokonałeś dobrego wyboru i założe się, że nie będziesz rozczarowany. Masz wszystko czego potrzebuję?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Co przyniosłeś?", getReply(npc));
		en.step(player, "por");
		assertEquals("No nie. Nie mam czasu na żarty! Nie masz por ze sobą.", getReply(npc));
		en.step(player, "cebula");
		assertEquals("No nie. Nie mam czasu na żarty! Nie masz cebula ze sobą.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		PlayerTestHelper.equipWithItem(player, "por");
		PlayerTestHelper.equipWithItem(player, "pokolec");
		PlayerTestHelper.equipWithItem(player, "dorsz");
		PlayerTestHelper.equipWithItem(player, "okoń");
		PlayerTestHelper.equipWithItem(player, "makrela");
		PlayerTestHelper.equipWithItem(player, "błazenek");
		PlayerTestHelper.equipWithItem(player, "pomidor");
		PlayerTestHelper.equipWithItem(player, "czosnek");
		PlayerTestHelper.equipWithItem(player, "palia alpejska");
		en.step(player, "hi");
		assertEquals("Witaj z powrotem! Mam nadzieje, że zdobyłeś jakieś #składniki na zupę rybną lub #wszystkie.", getReply(npc));
		en.step(player, "everything");
		assertEquals("Nie miałeś wszystkich składników, które potrzebuje. Wciąż potrzebuję 2 składników: #płotka oraz #cebula. Dostaniesz złą karmę jeżeli nadal będziesz popełniał błędy takie jak ten!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		en.step(player, "hi");
		assertEquals("Witaj z powrotem! Mam nadzieje, że zdobyłeś jakieś #składniki na zupę rybną lub #wszystkie.", getReply(npc));
		en.step(player, "ingredients");
		assertEquals("Wciąż potrzebuję 2 składniki: #płotka oraz #cebula. Przyniosłeś coś może?", getReply(npc));
		PlayerTestHelper.equipWithItem(player, "płotka");
		PlayerTestHelper.equipWithItem(player, "cebula");
		en.step(player, "płotka");
		assertEquals("Dziękuję bardzo! Co jeszcze przyniosłeś?", getReply(npc));
		en.step(player, "cebula");
		assertEquals("Zupa dla Ciebie jest na stole na rynku. Uzdrowi Ciebie. Mój magiczny sposób gotowania zupy daje też trochę karmy.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Życzę miłego pobytu na rynku w Ados!", getReply(npc));
		assertEquals(player.getXP(), 150L);
		en.step(player, "hi");
		assertEquals("Przykro mi, ale muszę umyć mój garnek nim zacznę gotować moją zupę dla Ciebie. Wróć za 20 minut.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Życzę miłego pobytu na rynku w Ados!", getReply(npc));

		//Test when player has everything
		player.setQuest(QUEST_SLOT, "done;0");
		en.step(player, "hi");
		assertEquals("Witaj ponownie. Wróciłeś po mój specjał, zupę rybną?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dokonałeś dobrego wyboru i założe się, że nie będziesz rozczarowany. Masz wszystko czego potrzebuję?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Co przyniosłeś?", getReply(npc));
		en.step(player, "por");
		assertEquals("No nie. Nie mam czasu na żarty! Nie masz por ze sobą.", getReply(npc));
		en.step(player, "cebula");
		assertEquals("No nie. Nie mam czasu na żarty! Nie masz cebula ze sobą.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		PlayerTestHelper.equipWithItem(player, "por");
		PlayerTestHelper.equipWithItem(player, "pokolec");
		PlayerTestHelper.equipWithItem(player, "dorsz");
		PlayerTestHelper.equipWithItem(player, "okoń");
		PlayerTestHelper.equipWithItem(player, "makrela");
		PlayerTestHelper.equipWithItem(player, "błazenek");
		PlayerTestHelper.equipWithItem(player, "pomidor");
		PlayerTestHelper.equipWithItem(player, "czosnek");
		PlayerTestHelper.equipWithItem(player, "palia alpejska");
		PlayerTestHelper.equipWithItem(player, "cebula");
		PlayerTestHelper.equipWithItem(player, "płotka");
		en.step(player, "hi");
		assertEquals("Witaj z powrotem! Mam nadzieje, że zdobyłeś jakieś #składniki na zupę rybną lub #wszystkie.", getReply(npc));
		en.step(player, "everything");
		assertEquals("Zupa dla Ciebie jest na stole na rynku. Uzdrowi cię. Powiedz mi jeżeli mógłbym jeszcze w czymś pomóc.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Życzę miłego pobytu na rynku w Ados!", getReply(npc));
		assertEquals(player.getXP(), 180L);
		en.step(player, "hi");
		assertEquals("Przykro mi, ale muszę umyć mój garnek nim zacznę gotować moją zupę dla Ciebie. Wróć za 20 minut.", getReply(npc));
		en.step(player, "job");
		assertEquals("Jestem wyspecjalizowany w zupach. Moją ulubioną zupą jest fish soup, ale lubię także inne...", getReply(npc));
		en.step(player, "offer");
		assertEquals("Jeżeli jesteś naprawdę głodny lub potrzebyjesz jedzenia na swoje podróże to mogę ugotować tobie smaczną zupę rybną, ale dopiero, gdy przyniesze potrzebne produkty wg receptury.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Życzę miłego pobytu na rynku w Ados!", getReply(npc));

	}
}
