/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.lang.reflect.Method;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.PlayerPrivateSpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class PierscienMieszczaninaTest {

	private static SpeakerNPC marianek;
	private static SpeakerNPC dobrawa;
	private static SpeakerNPC zywia;
	private static SpeakerNPC stach;
	private static SpeakerNPC milost;

	@BeforeClass
	public static void beforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		marianek = addNpc("Marianek");
		dobrawa = addNpc("Dobrawa");
		zywia = addNpc("Żywia");
		stach = addNpc("Stach");
		milost = addNpc("Miłost");

		new PierscienMieszczanina().addToWorld();
	}

	@AfterClass
	public static void afterClass() {
		SingletonRepository.getNPCList().clear();
	}

	private static SpeakerNPC addNpc(final String name) {
		final SpeakerNPC npc = new SpeakerNPC(name);
		SingletonRepository.getNPCList().add(npc);
		return npc;
	}

	@Test
	public void newTrialRequiresCompletedGoldenCiupagaQuestAndInteractiveIntroduction() {
		final Player player = PlayerTestHelper.createPlayer("CraftGate");
		player.setLevel(150);
		final Engine engine = marianek.getEngine();

		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, "zadanie");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertFalse(player.hasQuest(PierscienMieszczanina.QUEST_SLOT));
		assertTrue(getReply(marianek).contains("Andrzeja"));

		player.setQuest(PierscienMieszczanina.ZLOTA_CIUPAGA_QUEST_SLOT, "done");
		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, "zadanie");
		assertEquals(ConversationStates.INFORMATION_7, engine.getCurrentState());
		assertFalse(player.hasQuest(PierscienMieszczanina.QUEST_SLOT));
		assertTrue(getReply(marianek).contains("#rzemiosła"));

		engine.step(player, "rzemiosła");
		assertEquals(ConversationStates.INFORMATION_8, engine.getCurrentState());
		assertTrue(getReply(marianek).contains("#zwyczaj"));

		engine.step(player, "zwyczaj");
		assertEquals(ConversationStates.INFORMATION_9, engine.getCurrentState());
		assertTrue(getReply(marianek).contains("#Witomir"));

		engine.step(player, "Witomir");
		assertEquals(ConversationStates.QUEST_OFFERED, engine.getCurrentState());
		assertFalse(player.hasQuest(PierscienMieszczanina.QUEST_SLOT));
	}

	@Test
	public void settlementConversationsRequireFollowUpKeywordsBeforeAdvancing() {
		final Player player = PlayerTestHelper.createPlayer("Alice");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_MEDICINE_FOUND);

		stepGreeting(dobrawa, player);
		assertEquals(PierscienMieszczanina.STATE_MEDICINE_FOUND,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
		assertEquals(ConversationStates.INFORMATION_6, dobrawa.getEngine().getCurrentState());
		assertTrue(getReply(dobrawa).contains("#ranny"));
		step(dobrawa, player, "ranny");
		assertEquals(PierscienMieszczanina.STATE_MEDICINE_TO_ZYWIA,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));

		stepGreeting(zywia, player);
		assertEquals(PierscienMieszczanina.STATE_MEDICINE_TO_ZYWIA,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
		assertTrue(getReply(zywia).contains("#ranę"));
		step(zywia, player, "ranę");
		assertEquals(PierscienMieszczanina.STATE_SETTLEMENT,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));

		stepGreeting(dobrawa, player);
		assertEquals(PierscienMieszczanina.STATE_SETTLEMENT,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
		assertTrue(getReply(dobrawa).contains("#dostawy"));
		step(dobrawa, player, "dostawy");
		assertEquals(PierscienMieszczanina.STATE_SETTLEMENT,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
		assertTrue(getReply(dobrawa).contains("#objazd"));
		step(dobrawa, player, "objazd");
		assertEquals(PierscienMieszczanina.STATE_STACH,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));

		stepGreeting(stach, player);
		assertEquals(PierscienMieszczanina.STATE_STACH,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
		assertTrue(getReply(stach).contains("#objazd"));
		step(stach, player, "objazd");
		assertTrue(getReply(stach).contains("#narzędzia"));
		step(stach, player, "narzędzia");
		assertTrue(getReply(stach).contains("#Radomir"));
		step(stach, player, "Radomir");
		assertEquals(PierscienMieszczanina.STATE_TRACKS,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
	}

	@Test
	public void witomirExplainsAttackInStepsBeforeStartingJourney() throws Exception {
		final Player player = PlayerTestHelper.createPlayer("WitomirTester");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_ROAD);
		final StendhalRPZone zone = new StendhalRPZone(MieszczaninRoadScene.ZONE_NAME, 128, 128);
		player.setPosition(MieszczaninRoadScene.WITOMIR_START_X,
				MieszczaninRoadScene.WITOMIR_START_Y + 1);
		zone.add(player);
		final PierscienMieszczanina quest = new PierscienMieszczanina();

		final Method sync = PierscienMieszczanina.class.getDeclaredMethod(
				"syncPrivateRoadScene", Player.class, StendhalRPZone.class);
		sync.setAccessible(true);
		sync.invoke(quest, player, zone);

		PlayerPrivateSpeakerNPC witomir = null;
		for (final Entity entity : zone.getEntitiesOfClass(PlayerPrivateSpeakerNPC.class)) {
			final PlayerPrivateSpeakerNPC candidate = (PlayerPrivateSpeakerNPC) entity;
			if (candidate.isOwnedBy(player) && "Witomir".equals(candidate.getTitle())) {
				witomir = candidate;
				break;
			}
		}
		assertTrue(witomir != null);

		final Engine engine = witomir.getEngine();
		engine.setCurrentState(ConversationStates.IDLE);
		engine.step(player, "hi");
		assertEquals(PierscienMieszczanina.STATE_ROAD,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
		assertFalse(witomir.hasPath());
		assertTrue(getReply(witomir).contains("#napad"));

		engine.step(player, "napad");
		assertEquals(ConversationStates.INFORMATION_1, engine.getCurrentState());
		assertTrue(getReply(witomir).contains("#ładunek"));
		assertFalse(witomir.hasPath());

		engine.step(player, "ładunek");
		assertEquals(ConversationStates.INFORMATION_2, engine.getCurrentState());
		assertTrue(getReply(witomir).contains("#lekarstwo"));
		assertFalse(witomir.hasPath());

		engine.step(player, "lekarstwo");
		assertEquals(ConversationStates.QUEST_OFFERED, engine.getCurrentState());
		assertEquals(PierscienMieszczanina.STATE_ROAD,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
		assertFalse(witomir.hasPath());

		engine.step(player, "yes");
		assertEquals(PierscienMieszczanina.STATE_MEDICINE,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
		assertEquals(ConversationStates.IDLE, engine.getCurrentState());
		assertTrue(witomir.hasPath());

		int crates = 0;
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninMedicineCrate.class)) {
			if (((MieszczaninMedicineCrate) entity).isOwnedBy(player)) {
				crates++;
			}
		}
		assertEquals(1, crates);
	}

	@Test
	public void recoveredToolsMoveQuestToRepairStage() {
		final Player player = PlayerTestHelper.createPlayer("Repairer");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_TRACKS);
		MieszczaninHideoutProgress.markToolsRecovered(player);

		stepGreeting(stach, player);

		assertEquals(PierscienMieszczanina.STATE_REPAIR,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
		assertFalse(player.hasQuest(MieszczaninHideoutProgress.SLOT));
	}

	@Test
	public void legacyStartMovesToNewRoadStoryWithoutGoldenCiupagaGate() {
		final Player player = PlayerTestHelper.createPlayer("Legacy");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_LEGACY_START);

		final Engine engine = marianek.getEngine();
		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, "zadanie");

		assertEquals(PierscienMieszczanina.STATE_ROAD,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
	}

	@Test
	public void milostDoesNotAdvanceQuest() {
		final Player player = PlayerTestHelper.createPlayer("Listener");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_SETTLEMENT);

		stepGreeting(milost, player);
		assertEquals(PierscienMieszczanina.STATE_SETTLEMENT,
				player.getQuest(PierscienMieszczanina.QUEST_SLOT));
	}

	private static void stepGreeting(final SpeakerNPC npc, final Player player) {
		final Engine engine = npc.getEngine();
		engine.setCurrentState(ConversationStates.IDLE);
		engine.step(player, "hi");
	}

	private static void step(final SpeakerNPC npc, final Player player, final String text) {
		npc.getEngine().step(player, text);
	}
}
