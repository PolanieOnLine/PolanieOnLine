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
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.maps.ados.abandonedkeep.OrcKillGiantDwarfNPC;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

/**
 * JUnit test for the KillDhohrNuggetcutter quest.
 * @author bluelads, M. Fuchs
 */
public class KillDhohrNuggetcutterTest extends ZonePlayerAndNPCTestImpl {

	private SpeakerNPC npc = null;
	private Engine en = null;

	private String questSlot;
	private static final String ZONE_NAME = "-1_ados_abandoned_keep";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone(ZONE_NAME);
	}

	public KillDhohrNuggetcutterTest() {
		super(ZONE_NAME, "Zogfang");
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		new OrcKillGiantDwarfNPC().configureZone(zone, null);

		quest = new KillDhohrNuggetcutter();
		quest.addToWorld();

		questSlot = quest.getSlotName();
	}

	@Test
	public void testQuest() {
		npc = SingletonRepository.getNPCList().get("Zogfang");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Cześć przyjacielu. Witaj w naszych skromnych progach.", getReply(npc));
		en.step(player, "task");
		assertEquals("Nie możemy uwolnić naszego obszaru od krasnali. Szczególnie od takiego zwanego Dhohr Nuggetcutter. Czy mógłbyś go zabić?", getReply(npc));
		en.step(player, "no");
		assertEquals("Dobrze, poczekam na kogoś odpowiedniego do tego zadania.", getReply(npc));
		assertEquals("rejected", player.getQuest(questSlot));
		en.step(player, "bye");
		assertEquals("Życzę powodzenia na wyprawach.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Cześć przyjacielu. Witaj w naszych skromnych progach.", getReply(npc));
		en.step(player, "task");
		assertEquals("Nie możemy uwolnić naszego obszaru od krasnali. Szczególnie od takiego zwanego Dhohr Nuggetcutter. Czy mógłbyś go zabić?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Wspaniale! Proszę, znajdź ich gdzieś na tym poziomie i niech zapłacą za przekroczenie granicy!", getReply(npc));
		assertEquals("start", player.getQuest(questSlot, 0));
		en.step(player, "bye");
		assertEquals("Życzę powodzenia na wyprawach.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Po prostu idź zabić Dhohra Nuggetcuttera i jego sługusów: górskiego krasnala lidera, bohatera oraz starszego krasnala. Nawet proste krasnale górskie są dla nas zagrożeniem, ich też zabij.", getReply(npc));
		en.step(player, "task");
		assertEquals("Już się Ciebie pytałem o zabicie Dhohr Nuggetcutter!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Życzę powodzenia na wyprawach.", getReply(npc));

		// kill Dhohr Nuggetcutter
		player.setSoloKill("Dhohr Nuggetcutter");
		en.step(player, "hi");
		assertEquals("Po prostu idź zabić Dhohra Nuggetcuttera i jego sługusów: górskiego krasnala lidera, bohatera oraz starszego krasnala. Nawet proste krasnale górskie są dla nas zagrożeniem, ich też zabij.", getReply(npc));
		en.step(player, "task");
		assertEquals("Już się Ciebie pytałem o zabicie Dhohr Nuggetcutter!", getReply(npc));
		en.step(player, "done");
		en.step(player, "bye");
		assertEquals("Życzę powodzenia na wyprawach.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Po prostu idź zabić Dhohra Nuggetcuttera i jego sługusów: górskiego krasnala lidera, bohatera oraz starszego krasnala. Nawet proste krasnale górskie są dla nas zagrożeniem, ich też zabij.", getReply(npc));
		en.step(player, "task");
		assertEquals("Już się Ciebie pytałem o zabicie Dhohr Nuggetcutter!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Życzę powodzenia na wyprawach.", getReply(npc));

		// kill some other of the enemies
		player.setSoloKill("górski krasnal lider");
		player.setSoloKill("górski krasnal lider");
		en.step(player, "hi");
		assertEquals("Po prostu idź zabić Dhohra Nuggetcuttera i jego sługusów: górskiego krasnala lidera, bohatera oraz starszego krasnala. Nawet proste krasnale górskie są dla nas zagrożeniem, ich też zabij.", getReply(npc));
		en.step(player, "task");
		assertEquals("Już się Ciebie pytałem o zabicie Dhohr Nuggetcutter!", getReply(npc));
		player.setSoloKill("górski ork");
		player.setSoloKill("górski ork");
		en.step(player, "bye");
		assertEquals("Życzę powodzenia na wyprawach.", getReply(npc));

		// now kill all remaining creatures
		player.setSoloKill("ork łowca");
		player.setSoloKill("górski ork łowca");
		player.setSoloKill("górski ork");
		player.setSoloKill("górski krasnal bohater");
		player.setSoloKill("górski krasnal bohater");
		player.setSoloKill("górski starszy krasnal");
		player.setSoloKill("górski starszy krasnal");
		player.setSoloKill("górski krasnal lider");
		player.setSoloKill("górski krasnal lider");
		player.setSoloKill("górski krasnal");
		player.setSoloKill("górski krasnal");

		en.step(player, "hi");
		assertEquals("Bardzo dziękuję. Rzeczywiście jesteś wojownikiem! Masz jeden z nich. Znaleźliśmy je porozrzucane wokoło. Nie mamy pojęcia, czym one są.", getReply(npc));
		assertEquals(4000L, player.getXP());
		assertEquals("killed", player.getQuest(questSlot, 0));

		en.step(player, "task");
		assertEquals("Dziękuję za pomoc. Może przyjdziesz później. Krasnale mogą wrócić. Wróć do 2 tygodni.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Życzę powodzenia na wyprawach.", getReply(npc));
	}
}
