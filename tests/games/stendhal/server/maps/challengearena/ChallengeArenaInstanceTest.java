/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.actions.CommandCenter;
import marauroa.common.game.RPAction;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.util.Area;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class ChallengeArenaInstanceTest {
	private StendhalRPZone lobby;
	private final List<Player> players = new ArrayList<>();
	private int initialInstances;

	@BeforeClass public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }

	@Before public void setup() throws Exception {
		ChallengeArenaManager.clearForTests();
		lobby = new StendhalRPZone("arena_private_test_lobby", 64, 64);
		final StendhalMapStructure map = TMXLoader.load("data/maps/interiors/tarnow/challenge_arena.tmx");
		lobby.addCollisionLayer(lobby.getName() + ".collision", map.getLayer("collision"));
		MockStendlRPWorld.get().addRPZone(lobby);
		ChallengeArenaManager.configureArena(new ChallengeArenaInfo(new Area(lobby,
				new Rectangle2D.Double(1, 1, 62, 39)), lobby, lobby.getName(), 32, 47, 32, 19));
		initialInstances = SingletonRepository.getRPWorld().getInstanceZoneManager().getInstanceCount();
	}

	@After public void cleanup() throws Exception {
		for (final Player player : players) {
			final ChallengeArenaInfo info = ChallengeArenaManager.getArenaInfo(player.getName());
			if (info != null) {
				if (info.getEngine() != null) { info.getEngine().forfeit(player); }
				if (info.isInZone(player)) { info.teleportToLobby(player); }
				ChallengeArenaManager.release(player.getName(), info);
			}
			SingletonRepository.getRPWorld().getInstanceZoneManager().releaseMember(player.getName());
			if (player.getZone().has(player.getID())) { player.getZone().remove(player); }
		}
		ChallengeArenaManager.clearForTests();
	}

	private Player player(final String name) {
		final Player player = PlayerTestHelper.createPlayer(name);
		lobby.add(player);
		player.setPosition(32, 47);
		players.add(player);
		MoneyUtils.giveMoney(player, ChallengeArenaTier.TRIAL.getStake() * 2);
		return player;
	}

	private ChallengeArenaInfo start(final Player player) {
		new StartChallengeArenaAction(ChallengeArenaTier.TRIAL).fire(player, null, new EventRaiser(player));
		final ChallengeArenaInfo info = ChallengeArenaManager.getArenaInfo(player.getName());
		assertNotNull(info);
		assertEquals(info.getZone().getName(), info.getZone().getAttributes().get("runtime_zone"));
		assertTrue(info.isInArena(player));
		assertNotNull(info.getEngine());
		for (int x = 31; x <= 33; x++) {
			assertFalse("Exit must be on a walkable tile of the real arena map",
					info.getZone().collisionMap.collides(x, ChallengeArenaInstanceFactory.EXIT_Y));
		}
		return info;
	}

	@Test public void concurrentRunsIsolatePlayersLootAndCharges() {
		final Player alice = player("PrivateArenaAlice"), bob = player("PrivateArenaBob");
		final ChallengeArenaInfo first = start(alice), second = start(bob);
		assertNotSame(first.getZone(), second.getZone());
		assertNotSame(lobby, first.getZone());
		assertEquals(initialInstances + 2,
				SingletonRepository.getRPWorld().getInstanceZoneManager().getInstanceCount());
		assertFalse(first.getZone().has(bob.getID()));
		assertEquals(ChallengeArenaTier.TRIAL.getStake(), MoneyUtils.getTotalMoneyInCopper(alice));
		new StartChallengeArenaAction(ChallengeArenaTier.TRIAL).fire(alice, null, new EventRaiser(alice));
		assertSame(first, ChallengeArenaManager.getArenaInfo(alice.getName()));
		assertEquals(ChallengeArenaTier.TRIAL.getStake(), MoneyUtils.getTotalMoneyInCopper(alice));
		final Item loot = SingletonRepository.getEntityManager().getItem("miecz");
		first.getZone().add(loot);
		assertFalse(second.getZone().has(loot.getID()));
		assertTrue(first.getEngine().forfeit(alice));
		assertSame(lobby, alice.getZone());
		assertNull(ChallengeArenaManager.getArenaInfo(alice.getName()));
		assertSame(second.getZone(), bob.getZone());
		assertNotNull(second.getEngine());
		assertEquals(initialInstances + 1,
				SingletonRepository.getRPWorld().getInstanceZoneManager().getInstanceCount());
	}

	@Test public void cannotTeleportAnotherPlayerAnywhereIntoPrivateMap() {
		final Player owner = player("PrivateArenaOwner"), stranger = player("PrivateArenaStranger");
		final ChallengeArenaInfo info = start(owner);
		assertFalse(stranger.teleport(info.getZone(), 32, 19, null, null));
		assertSame(lobby, stranger.getZone());
		assertFalse(info.getZone().isPublicAccessible());
		assertFalse(info.getZone().isTeleportInAllowed(32, 19));
	}

	@Test public void administratorCanTeleportWalkAndUseExitWithoutGhostMode() {
		final Player owner = player("AdminVisitOwner"), admin = player("AdminArenaVisitor");
		admin.setAdminLevel(8);
		final ChallengeArenaInfo info = start(owner);
		final String ownerState = owner.getQuest(ChallengeArenaState.QUEST_SLOT);
		final int money = MoneyUtils.getTotalMoneyInCopper(admin);
		MockStendhalRPRuleProcessor.get().addPlayer(owner);
		final RPAction teleport = new RPAction();
		teleport.put("type", "teleportto"); teleport.put("target", owner.getName());
		assertTrue(CommandCenter.execute(admin, teleport));
		assertSame(info.getZone(), admin.getZone());
		assertFalse(admin.isGhost());
		assertFalse(admin.isArenaSpectator());
		assertTrue("The arena gate must allow administrative movement", admin.canMoveTo(34, 19));
		for (final games.stendhal.server.entity.Entity entity : info.getZone().getEntitiesAt(0, 0)) {
			if (entity instanceof ChallengeArenaPlayerGate) {
				final ChallengeArenaPlayerGate gate = (ChallengeArenaPlayerGate) entity;
				gate.onEntered(admin, info.getZone(), admin.getX(), admin.getY());
				assertFalse(gate.isObstacle(admin));
			}
		}
		admin.setPosition(32, ChallengeArenaInstanceFactory.EXIT_Y - 1);
		assertTrue(info.getZone().getPortal(32, ChallengeArenaInstanceFactory.EXIT_Y).onUsed(admin));
		assertSame(lobby, admin.getZone());
		assertSame(info.getZone(), owner.getZone());
		assertNotNull(info.getEngine());
		assertEquals(ownerState, owner.getQuest(ChallengeArenaState.QUEST_SLOT));
		assertEquals(money, MoneyUtils.getTotalMoneyInCopper(admin));
		assertEquals(0, ChallengeArenaRewardService.getWins(admin));
	}

	@Test public void supportPrivilegesDoNotGrantPrivateArenaEntry() {
		final Player owner = player("SupportVisitOwner"), support = player("SupportArenaVisitor");
		support.setAdminLevel(1);
		final ChallengeArenaInfo info = start(owner);
		assertFalse(support.teleport(info.getZone(), 32, 19, null, null));
		assertSame(lobby, support.getZone());
	}

	@Test public void anAdministratorFightingHisOwnTrialStillForfeitsThroughTheExit() {
		final Player admin = player("AdminArenaFighter");
		admin.setAdminLevel(8);
		final ChallengeArenaInfo info = start(admin);
		admin.setPosition(32, ChallengeArenaInstanceFactory.EXIT_Y - 1);
		assertTrue(info.getZone().getPortal(32, ChallengeArenaInstanceFactory.EXIT_Y).onUsed(admin));
		assertSame(lobby, admin.getZone());
		assertEquals(ChallengeArenaState.Lifecycle.FAILED,
				ChallengeArenaState.parse(admin.getQuest(ChallengeArenaState.QUEST_SLOT)).getLifecycle());
		assertNull(ChallengeArenaManager.getArenaInfo(admin.getName()));
		assertEquals(0, ChallengeArenaRewardService.getWins(admin));
	}

	@Test public void administrativeVisitorsReturnToLobbyWhenThePrivateArenaCloses() {
		final Player owner = player("AdminReturnOwner"), admin = player("AdminReturnVisitor");
		admin.setAdminLevel(8);
		final ChallengeArenaInfo info = start(owner);
		assertTrue(admin.teleport(info.getZone(), 34, 19, null, admin));
		assertTrue(info.getEngine().forfeit(owner));
		assertSame(lobby, admin.getZone());
		assertSame(lobby, owner.getZone());
		assertFalse(admin.isGhost());
		assertEquals(0, ChallengeArenaRewardService.getWins(admin));
	}

	@Test public void logoutPersistsLobbyAndOnlyDestroysOwnArena() throws Exception {
		final Player alice = player("PrivateArenaLogout"), bob = player("PrivateArenaRemaining");
		final ChallengeArenaInfo first = start(alice), second = start(bob);
		Player.destroy(alice);
		assertEquals(lobby.getName(), alice.get("zoneid"));
		assertEquals(32, alice.getInt("x"));
		assertEquals(47, alice.getInt("y"));
		assertEquals(ChallengeArenaState.Lifecycle.FAILED,
				ChallengeArenaState.parse(alice.getQuest(ChallengeArenaState.QUEST_SLOT)).getLifecycle());
		SingletonRepository.getRPWorld().getInstanceZoneManager().releaseMember(alice.getName());
		assertNull(ChallengeArenaManager.getArenaInfo(alice.getName()));
		assertNull(SingletonRepository.getRPWorld().getZone(first.getZone().getName()));
		assertSame(second, ChallengeArenaManager.getArenaInfo(bob.getName()));
	}

	@Test public void victoryExitKeepsRewardAndDestroysMap() {
		final Player owner = player("PrivateArenaWinner");
		final ChallengeArenaInfo info = start(owner);
		owner.setQuest(ChallengeArenaState.QUEST_SLOT, ChallengeArenaState.start(ChallengeArenaTier.TRIAL)
				.withSpawnedCreatures(ChallengeArenaTier.TRIAL.getCreatureCount()).serialize());
		final ChallengeArenaEngine engine = info.getEngine();
		engine.onTurnReached(1);
		assertEquals(1, ChallengeArenaRewardService.getWins(owner));
		owner.setPosition(32, ChallengeArenaInstanceFactory.EXIT_Y - 1);
		assertTrue(info.getZone().getPortal(32, ChallengeArenaInstanceFactory.EXIT_Y).onUsed(owner));
		assertSame(lobby, owner.getZone());
		assertEquals(ChallengeArenaState.Lifecycle.DONE,
				ChallengeArenaState.parse(owner.getQuest(ChallengeArenaState.QUEST_SLOT)).getLifecycle());
		engine.onTurnReached(2);
		assertEquals(1, ChallengeArenaRewardService.getWins(owner));
		assertNull(ChallengeArenaManager.getArenaInfo(owner.getName()));
	}

	@Test public void unfinishedExitAndDeathDoNotAwardVictory() {
		final Player owner = player("PrivateArenaForfeit"), dead = player("PrivateArenaDead");
		final ChallengeArenaInfo first = start(owner), second = start(dead);
		owner.setPosition(32, ChallengeArenaInstanceFactory.EXIT_Y - 1);
		assertTrue(first.getZone().getPortal(32, ChallengeArenaInstanceFactory.EXIT_Y).onUsed(owner));
		dead.setHP(0);
		second.getEngine().onTurnReached(1);
		assertSame(lobby, owner.getZone());
		assertSame(lobby, dead.getZone());
		assertFalse(ChallengeArenaManager.isReserved());
		assertEquals(0, ChallengeArenaRewardService.getWins(owner));
		assertEquals(0, ChallengeArenaRewardService.getWins(dead));
	}

	@Test public void insufficientMoneyDoesNotCreateOrReserveMap() {
		final Player owner = player("PrivateArenaNoMoney");
		MoneyUtils.removeMoney(owner, MoneyUtils.getTotalMoneyInCopper(owner));
		new StartChallengeArenaAction(ChallengeArenaTier.TRIAL).fire(owner, null, new EventRaiser(owner));
		assertFalse(ChallengeArenaManager.isReservedBy(owner.getName()));
		assertNull(ChallengeArenaManager.getArenaInfo(owner.getName()));
		assertSame(lobby, owner.getZone());
		assertEquals(initialInstances, SingletonRepository.getRPWorld().getInstanceZoneManager().getInstanceCount());
	}

	@Test public void failedEntryRefundsStakeAndDestroysEmptyInstance() {
		final Player owner = player("PrivateArenaFailedEntry");
		final ChallengeArenaInfo unreachable = new ChallengeArenaInfo(new Area(lobby,
				new Rectangle2D.Double(1000, 1000, 1, 1)), lobby, lobby.getName(), 32, 47, 32, 19);
		ChallengeArenaManager.configureArena(unreachable);
		new StartChallengeArenaAction(ChallengeArenaTier.TRIAL).fire(owner, null, new EventRaiser(owner));
		assertEquals(ChallengeArenaTier.TRIAL.getStake() * 2, MoneyUtils.getTotalMoneyInCopper(owner));
		assertSame(lobby, owner.getZone());
		assertFalse(ChallengeArenaManager.isReserved());
		assertEquals(initialInstances, SingletonRepository.getRPWorld().getInstanceZoneManager().getInstanceCount());
	}

	@Test public void delayedExitDoesNotCancelNewRun() {
		final Player owner = player("PrivateArenaRepeat");
		final ChallengeArenaInfo old = start(owner);
		assertTrue(old.getEngine().forfeit(owner));
		final ChallengeArenaInfo current = start(owner);
		ChallengeArenaManager.release(owner.getName(), old);
		assertSame(current, ChallengeArenaManager.getArenaInfo(owner.getName()));
		assertTrue(current.isInArena(owner));
		assertNotNull(current.getEngine());
	}

	@Test public void blockedLobbyKeepsMapAliveAndRetriesReturnWithoutDuplicatingReward() {
		final Player owner = player("PrivateArenaBlockedReturn");
		final ChallengeArenaInfo info = start(owner);
		owner.setQuest(ChallengeArenaState.QUEST_SLOT, ChallengeArenaState.start(ChallengeArenaTier.TRIAL)
				.withSpawnedCreatures(ChallengeArenaTier.TRIAL.getCreatureCount()).serialize());
		final ChallengeArenaEngine engine = info.getEngine();
		engine.onTurnReached(1);
		final ChallengeArenaPlayerGate blocked = new ChallengeArenaPlayerGate(64, 64);
		blocked.setPosition(0, 0);
		lobby.add(blocked);
		assertTrue(engine.forfeit(owner));
		assertTrue(info.isInZone(owner));
		assertSame(info, ChallengeArenaManager.getArenaInfo(owner.getName()));
		assertSame(info.getZone(), SingletonRepository.getRPWorld().getZone(info.getZone().getName()));
		lobby.remove(blocked);
		engine.onTurnReached(2);
		assertSame(lobby, owner.getZone());
		assertEquals(1, ChallengeArenaRewardService.getWins(owner));
		assertNull(ChallengeArenaManager.getArenaInfo(owner.getName()));
	}

	@Test public void concurrentRunsSpawnIndependentCreatures() {
		final Player alice = player("PrivateArenaWaveAlice"), bob = player("PrivateArenaWaveBob");
		final ChallengeArenaInfo first = start(alice), second = start(bob);
		first.getEngine().onTurnReached(1);
		second.getEngine().onTurnReached(1);
		assertFalse(first.getZone().getNPCList().isEmpty());
		assertFalse(second.getZone().getNPCList().isEmpty());
		for (final games.stendhal.server.entity.npc.NPC creature : first.getZone().getNPCList()) {
			assertSame(first.getZone(), creature.getZone());
			assertFalse(second.getZone().has(creature.getID()));
		}
		assertTrue(first.getEngine().forfeit(alice));
		assertFalse(second.getZone().getNPCList().isEmpty());
	}
}
