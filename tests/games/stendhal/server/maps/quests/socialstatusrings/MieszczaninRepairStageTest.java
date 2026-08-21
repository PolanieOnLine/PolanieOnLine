/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class MieszczaninRepairStageTest {

	@BeforeClass
	public static void beforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Test
	public void toolsLeadThroughInteractiveRepairAndCommunityApproval() {
		final StendhalRPZone zone = new StendhalRPZone(MieszczaninRoadScene.ZONE_NAME, 128, 128);
		final SpeakerNPC dobrawa = new SpeakerNPC("Dobrawa test");
		final SpeakerNPC stach = new SpeakerNPC("Stach test");
		final SpeakerNPC zywia = new SpeakerNPC("Żywia test");
		final SpeakerNPC milost = new SpeakerNPC("Miłost test");
		MieszczaninRepairStage.attach(zone, dobrawa, stach, zywia, milost);

		// Simulate the older main-quest transition being registered after the
		// settlement configurator but before the first player enters the zone.
		stach.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_TRACKS),
						new QuestInStateCondition(MieszczaninHideoutProgress.SLOT,
								MieszczaninHideoutProgress.TOOLS_RECOVERED)),
				ConversationStates.ATTENDING,
				"Stary, niejednoznaczny tekst.",
				new SetQuestAction(PierscienMieszczanina.QUEST_SLOT,
						PierscienMieszczanina.STATE_REPAIR));

		final Player player = PlayerTestHelper.createPlayer("Alice");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_TRACKS);
		MieszczaninHideoutProgress.markToolsRecovered(player);
		player.setPosition(70, 67);
		zone.add(player);

		greet(stach, player);
		final String stachReply = getReply(stach);
		assertEquals(ConversationStates.INFORMATION_9, stach.getEngine().getCurrentState());
		assertTrue(stachReply.contains("#naprawa"));
		assertFalse(stachReply.contains("niejednoznaczny"));
		assertTrue(player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_TRACKS));
		assertTrue(player.hasQuest(MieszczaninHideoutProgress.SLOT));
		assertFalse(hasRepairSite(zone, player));

		step(stach, player, "naprawa");
		assertTrue(player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_REPAIR));
		assertFalse(player.hasQuest(MieszczaninHideoutProgress.SLOT));
		assertTrue(hasRepairSite(zone, player));
		assertTrue(hasRepairBeam(zone, player));

		MieszczaninRepairProgress.markRepaired(player);
		MieszczaninRepairStage.syncRepairSite(zone, player);
		assertFalse(hasRepairSite(zone, player));
		assertFalse(hasRepairBeam(zone, player));

		greet(stach, player);
		assertEquals(ConversationStates.INFORMATION_9, stach.getEngine().getCurrentState());
		assertTrue(getReply(stach).contains("#belki"));
		assertFalse(MieszczaninRepairProgress.isStachConfirmed(player));
		step(stach, player, "belki");
		assertTrue(MieszczaninRepairProgress.isStachConfirmed(player));

		greet(dobrawa, player);
		assertEquals(ConversationStates.INFORMATION_8, dobrawa.getEngine().getCurrentState());
		assertTrue(getReply(dobrawa).contains("#pomoc"));
		assertFalse(MieszczaninRepairProgress.isCommunityApproved(player));
		step(dobrawa, player, "pomoc");
		assertEquals(ConversationStates.INFORMATION_9, dobrawa.getEngine().getCurrentState());
		assertTrue(getReply(dobrawa).contains("#Marianek"));
		assertFalse(MieszczaninRepairProgress.isCommunityApproved(player));
		step(dobrawa, player, "Marianek");
		assertTrue(MieszczaninRepairProgress.isCommunityApproved(player));
	}

	private static void greet(final SpeakerNPC npc, final Player player) {
		final Engine engine = npc.getEngine();
		engine.setCurrentState(ConversationStates.IDLE);
		engine.step(player, "hi");
	}

	private static void step(final SpeakerNPC npc, final Player player, final String text) {
		npc.getEngine().step(player, text);
	}

	private static boolean hasRepairSite(final StendhalRPZone zone, final Player owner) {
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninRepairSite.class)) {
			if (((MieszczaninRepairSite) entity).isOwnedBy(owner)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasRepairBeam(final StendhalRPZone zone, final Player owner) {
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninRepairBeam.class)) {
			if (((MieszczaninRepairBeam) entity).isOwnedBy(owner)) {
				return true;
			}
		}
		return false;
	}
}
