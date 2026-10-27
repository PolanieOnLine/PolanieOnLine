package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Actions;
import games.stendhal.common.constants.Events;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.util.Area;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

/** Real map/instance tests, including adversarial client commands and exits. */
public class ChallengeArenaViewOrbTest {
	private StendhalRPZone lobby;
	private final List<Player> players = new ArrayList<>();
	private int initialInstances;

	@BeforeClass public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }

	@Before public void setup() throws Exception {
		ChallengeArenaManager.clearForTests();
		lobby = new StendhalRPZone("arena_observation_test_lobby", 64, 64);
		final StendhalMapStructure map = TMXLoader.load("data/maps/interiors/tarnow/challenge_arena.tmx");
		lobby.addCollisionLayer(lobby.getName() + ".collision", map.getLayer("collision"));
		MockStendlRPWorld.get().addRPZone(lobby);
		ChallengeArenaManager.configureArena(new ChallengeArenaInfo(new Area(lobby,
				new Rectangle2D.Double(1, 1, 62, 39)), lobby, lobby.getName(), 32, 47, 32, 19));
		ChallengeArenaZone.configureViewOrbs(lobby);
		initialInstances = SingletonRepository.getRPWorld().getInstanceZoneManager().getInstanceCount();
	}

	@After public void cleanup() throws Exception {
		for (final Player player : players) { ChallengeArenaObservation.stopWatching(player); }
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
		assertTrue(info.isInArena(player));
		return info;
	}

	private ChallengeArenaObservationOrb orb(final int x) {
		for (final Entity entity : lobby.getEntitiesAt(x, ChallengeArenaZone.VIEW_ORB_Y)) {
			if (entity instanceof ChallengeArenaObservationOrb) { return (ChallengeArenaObservationOrb) entity; }
		}
		throw new AssertionError("Missing observation orb");
	}

	private void open(final Player viewer, final int x) {
		viewer.setPosition(x, ChallengeArenaZone.VIEW_ORB_Y + 1);
		assertTrue(orb(x).onUsed(viewer));
	}

	private void command(final Player player, final String type, final String text) {
		final RPAction action = new RPAction();
		action.put("type", type);
		if (text != null) { action.put("text", text); }
		assertTrue(CommandCenter.execute(player, action));
	}

	private RPEvent lastOptions(final Player player) {
		RPEvent found = null;
		for (final RPEvent event : player.events()) {
			if (Events.CHAT_OPTIONS.equals(event.getName()) && event.has("arena_phase")) { found = event; }
		}
		assertNotNull(found);
		return found;
	}

	@Test public void emptyArenaDoesNotOpenAChooserOrAnEmptyCameraView() {
		for (final int x : new int[] {27, 37}) {
			final Player viewer = player("EmptyArenaViewer" + x);
			open(viewer, x);
			assertSame(lobby, viewer.getZone());
			assertEquals("close", lastOptions(viewer).get("arena_phase"));
			assertEquals("", lastOptions(viewer).get("options"));
			assertFalse(viewer.isArenaSpectator());
			for (final RPEvent event : viewer.events()) { assertNotEquals(Events.VIEW_CHANGE, event.getName()); }
		}
	}

	private void observeAction(final Player player, final String operation, final String target) {
		final RPAction action = newAction("arena_observation");
		action.put("operation", operation);
		if (target != null) { action.put("target", target); }
		assertTrue(CommandCenter.execute(player, action));
		assertFalse("Observation must not become public speech", player.has("text"));
	}

	@Test public void silentButtonsRefreshChooseSwitchAndCloseWithoutPublicSpeech() {
		final Player alice = player("SilentAlice"), bob = player("SilentBob"), viewer = player("SilentViewer");
		final ChallengeArenaInfo first = start(alice), second = start(bob);
		open(viewer, 27);
		observeAction(viewer, "list", null);
		assertEquals("open", lastOptions(viewer).get("arena_phase"));
		observeAction(viewer, "watch", alice.getName());
		assertSame(first.getZone(), viewer.getZone());
		observeAction(viewer, "list", null);
		assertEquals("watch", lastOptions(viewer).get("arena_phase"));
		observeAction(viewer, "watch", bob.getName());
		assertSame(second.getZone(), viewer.getZone());
		observeAction(viewer, "close", null);
		assertSame(lobby, viewer.getZone());
		assertFalse(viewer.isArenaSpectator());
		assertEquals("close", lastOptions(viewer).get("arena_phase"));
	}

	@Test public void refreshClosesTheChooserAfterTheLastFightEnds() {
		final Player owner = player("EmptyRefreshOwner"), viewer = player("EmptyRefreshViewer");
		final ChallengeArenaInfo info = start(owner);
		open(viewer, 27);
		assertTrue(info.getEngine().forfeit(owner));
		observeAction(viewer, "list", null);
		assertEquals("close", lastOptions(viewer).get("arena_phase"));
		assertSame(lobby, viewer.getZone());
		observeAction(viewer, "watch", owner.getName());
		assertFalse(viewer.isArenaSpectator());
	}

	@Test public void desktopRPClassChatCommandsAreConsumedBeforePublicChat() {
		final Player owner = player("TypedChatOwner"), viewer = player("TypedChatViewer");
		final ChallengeArenaInfo info = start(owner);
		open(viewer, 27);
		final RPAction action = new RPAction();
		action.setRPClass(RPClass.getRPClass("chat"));
		action.put("text", "obserwuj " + owner.getName());
		assertFalse(action.has("type"));
		assertTrue(CommandCenter.execute(viewer, action));
		assertSame(info.getZone(), viewer.getZone());
		assertFalse(viewer.has("text"));
		action.put("text", "obserwuj powrot");
		assertTrue(CommandCenter.execute(viewer, action));
		assertSame(lobby, viewer.getZone());
		assertFalse(viewer.has("text"));
	}

	@Test public void silentSelectionCannotBypassOrbValidationOrTreatNamesAsCommands() {
		final Player owner = player("lista"), viewer = player("SilentValidationViewer");
		final ChallengeArenaInfo info = start(owner);
		observeAction(viewer, "watch", owner.getName());
		assertSame(lobby, viewer.getZone());
		open(viewer, 27);
		viewer.setPosition(32, 60);
		observeAction(viewer, "watch", owner.getName());
		assertSame(lobby, viewer.getZone());
		open(viewer, 27);
		observeAction(viewer, "watch", owner.getName());
		assertSame(info.getZone(), viewer.getZone());
		observeAction(viewer, "return", null);
		assertSame(lobby, viewer.getZone());
	}

	@Test public void choosesEitherPrivateFightAndSwitchesWithoutChangingTheirRuns() {
		final Player alice = player("WatchAlice"), bob = player("WatchBob"), viewer = player("WatchViewer");
		final ChallengeArenaInfo first = start(alice), second = start(bob);
		open(viewer, 27);
		assertTrue(lastOptions(viewer).get("options").contains("WatchAlice"));
		assertTrue(lastOptions(viewer).get("options").contains("WatchBob"));
		final long money = MoneyUtils.getTotalMoneyInCopper(viewer);
		command(viewer, "chat", "obserwuj watchalice");
		assertSame(first.getZone(), viewer.getZone());
		assertTrue(viewer.isArenaSpectator());
		assertFalse(first.isInArena(viewer));
		assertTrue(first.hasOnlyPlayer(alice));
		assertEquals(2, SingletonRepository.getRPWorld().getInstanceZoneManager().getMemberCount(first.getZone().getID()));
		assertTrue(viewer.events().stream().anyMatch(event -> Events.VIEW_CHANGE.equals(event.getName())
				&& event.getInt("x") == 32 && event.getInt("y") == 19));
		command(viewer, "chat", "obserwuj WatchBob");
		assertSame(second.getZone(), viewer.getZone());
		assertSame(first.getZone(), alice.getZone());
		assertSame(second.getZone(), bob.getZone());
		assertEquals(1, SingletonRepository.getRPWorld().getInstanceZoneManager().getMemberCount(first.getZone().getID()));
		assertEquals(money, MoneyUtils.getTotalMoneyInCopper(viewer));
		assertNull(viewer.getQuest(ChallengeArenaState.QUEST_SLOT));
		assertEquals(0, ChallengeArenaRewardService.getWins(viewer));
		command(viewer, "chat", "obserwuj powrot");
		assertSame(lobby, viewer.getZone());
		assertTrue(Math.abs(27 - viewer.getX()) <= 1);
		assertTrue(Math.abs(44 - viewer.getY()) <= 1);
		assertFalse(viewer.isArenaSpectator());
		assertEquals("close", lastOptions(viewer).get("arena_phase"));
	}

	@Test public void spectatorCannotAttackCastLootTradeOrExecuteUnknownCommands() {
		final Player owner = player("WatchSafeOwner"), viewer = player("WatchSafeViewer");
		viewer.setAdminLevel(5000); // The orb remains read-only even for an administrator.
		final ChallengeArenaInfo info = start(owner);
		final Item loot = SingletonRepository.getEntityManager().getItem("miecz");
		info.getZone().add(loot);
		open(viewer, 37);
		assertTrue(ChallengeArenaObservation.watch(viewer, owner.getName()));
		for (final String type : new String[] {"attack", "cast", "equip", "drop", "displace", "trade",
				"use", "item_upgrade", "summon", "ghostmode", "teleportto", "unknown_mutation", "chat"}) {
			final RPAction action = new RPAction();
			action.put("type", type);
			action.put("text", "hello");
			action.put("target", Integer.toString(owner.getID().getObjectID()));
			assertTrue("Denied before dispatch: " + type, ChallengeArenaObservation.handleAction(viewer, action));
		}
		assertTrue(info.getZone().has(loot.getID()));
		assertFalse(viewer.isAttackable());
		assertTrue(viewer.isInvisibleToCreatures());
		assertEquals(0, viewer.getResistance());
		final int hp = viewer.getHP();
		assertEquals(0, viewer.damage(100, owner));
		assertEquals(hp, viewer.getHP());
		assertFalse(viewer.isGhost());
		assertFalse(viewer.has("invisible"));
		assertFalse(viewer.teleport(info.getZone(), 32, 19, null, null));
		assertFalse(ChallengeArenaObservation.handleAction(viewer, newAction("look")));
		command(viewer, Actions.MOVE_CONTINUOUS, null);
		assertSame(lobby, viewer.getZone());
		assertTrue(viewer.isAttackable());
		assertFalse(viewer.isArenaSpectator());
	}

	private RPAction newAction(final String type) {
		final RPAction action = new RPAction(); action.put("type", type); return action;
	}

	@Test public void staleSelectionAndSelectionAwayFromTheOrbAreRejected() {
		final Player owner = player("WatchStaleOwner"), viewer = player("WatchStaleViewer");
		final ChallengeArenaInfo info = start(owner);
		assertFalse(ChallengeArenaObservation.watch(viewer, owner.getName()));
		open(viewer, 27);
		viewer.setPosition(32, 60);
		assertFalse(ChallengeArenaObservation.watch(viewer, owner.getName()));
		assertSame(lobby, viewer.getZone());
		open(viewer, 27);
		assertTrue(info.getEngine().forfeit(owner));
		assertFalse(ChallengeArenaObservation.watch(viewer, owner.getName()));
		assertSame(lobby, viewer.getZone());
		assertFalse(viewer.isArenaSpectator());
		assertEquals("close", lastOptions(viewer).get("arena_phase"));
	}

	@Test public void desktopTypedMovementReturnsInsteadOfMovingInsideThePrivateArena() {
		final Player owner = player("TypedMoveOwner"), viewer = player("TypedMoveViewer");
		start(owner);
		open(viewer, 27);
		observeAction(viewer, "watch", owner.getName());
		final RPAction action = new RPAction();
		action.setRPClass(RPClass.getRPClass("moveto"));
		action.put("x", 32); action.put("y", 19);
		assertFalse(action.has("type"));
		assertTrue(CommandCenter.execute(viewer, action));
		assertSame(lobby, viewer.getZone());
		assertFalse(viewer.isArenaSpectator());
	}

	@Test public void malformedSilentControlsCannotMoveOrEndAnObservation() {
		final Player owner = player("MalformedOwner"), viewer = player("MalformedViewer");
		final ChallengeArenaInfo info = start(owner);
		open(viewer, 27);
		observeAction(viewer, "watch", owner.getName());
		observeAction(viewer, "watch", null);
		observeAction(viewer, "teleport", owner.getName());
		final RPAction disguisedChat = newAction("look");
		disguisedChat.setRPClass(RPClass.getRPClass("chat"));
		disguisedChat.put("text", "hello");
		assertTrue(CommandCenter.execute(viewer, disguisedChat));
		assertFalse(viewer.has("text"));
		assertTrue(CommandCenter.execute(viewer, newAction("arena_observation")));
		assertTrue(viewer.isArenaSpectator());
		assertSame(info.getZone(), viewer.getZone());
		observeAction(viewer, "close", null);
		observeAction(viewer, "close", null);
		assertSame(lobby, viewer.getZone());
	}

	@Test public void multipleSpectatorsReturnWhenTheFighterWinsWithoutReceivingHisReward() {
		final Player owner = player("WatchWinOwner"), first = player("WatchWinFirst"), second = player("WatchWinSecond");
		final ChallengeArenaInfo info = start(owner);
		open(first, 27); assertTrue(ChallengeArenaObservation.watch(first, owner.getName()));
		open(second, 37); assertTrue(ChallengeArenaObservation.watch(second, owner.getName()));
		final long firstMoney = MoneyUtils.getTotalMoneyInCopper(first);
		owner.setQuest(ChallengeArenaState.QUEST_SLOT, ChallengeArenaState.start(ChallengeArenaTier.TRIAL)
				.withSpawnedCreatures(ChallengeArenaTier.TRIAL.getCreatureCount()).serialize());
		info.getEngine().onTurnReached(1);
		assertSame(lobby, first.getZone());
		assertSame(lobby, second.getZone());
		assertFalse(first.isArenaSpectator());
		assertEquals(firstMoney, MoneyUtils.getTotalMoneyInCopper(first));
		assertEquals(0, ChallengeArenaRewardService.getWins(first));
		assertEquals(1, ChallengeArenaRewardService.getWins(owner));
		assertTrue(info.isInArena(owner));
		assertEquals(1, SingletonRepository.getRPWorld().getInstanceZoneManager().getMemberCount(info.getZone().getID()));
	}

	@Test public void fighterLogoutReturnsSpectatorsAndReleasesOnlyHisInstance() throws Exception {
		final Player owner = player("WatchLogoutOwner"), viewer = player("WatchLogoutViewer"), other = player("WatchOtherFighter");
		final ChallengeArenaInfo first = start(owner), second = start(other);
		open(viewer, 27); assertTrue(ChallengeArenaObservation.watch(viewer, owner.getName()));
		Player.destroy(owner);
		SingletonRepository.getRPWorld().getInstanceZoneManager().releaseMember(owner.getName());
		assertSame(lobby, viewer.getZone());
		assertFalse(viewer.isArenaSpectator());
		assertNull(SingletonRepository.getRPWorld().getZone(first.getZone().getName()));
		assertSame(second, ChallengeArenaManager.getArenaInfo(other.getName()));
		assertEquals(initialInstances + 1, SingletonRepository.getRPWorld().getInstanceZoneManager().getInstanceCount());
	}

	@Test public void viewerLogoutSavesPermanentLobbyAndDoesNotStopTheFight() throws Exception {
		final Player owner = player("WatchPersistOwner"), viewer = player("WatchPersistViewer");
		final ChallengeArenaInfo info = start(owner);
		open(viewer, 37); assertTrue(ChallengeArenaObservation.watch(viewer, owner.getName()));
		final RPObject snapshot = ChallengeArenaObservation.persistenceSnapshot(viewer);
		assertEquals(lobby.getName(), snapshot.get("zoneid"));
		assertSame(info.getZone(), viewer.getZone());
		assertNotEquals(snapshot.get("zoneid"), viewer.get("zoneid"));
		Player.destroy(viewer);
		assertEquals(lobby.getName(), viewer.get("zoneid"));
		assertEquals(37, viewer.getInt("x"));
		assertEquals(44, viewer.getInt("y"));
		assertFalse(viewer.isArenaSpectator());
		assertNotNull(info.getEngine());
		assertTrue(info.isInArena(owner));
		assertEquals(1, SingletonRepository.getRPWorld().getInstanceZoneManager().getMemberCount(info.getZone().getID()));
	}

	@Test public void blockedReturnKeepsSpectatorProtectedAndMapAliveUntilRetry() {
		final Player owner = player("WatchBlockedOwner"), viewer = player("WatchBlockedViewer");
		final ChallengeArenaInfo info = start(owner);
		open(viewer, 27); assertTrue(ChallengeArenaObservation.watch(viewer, owner.getName()));
		final ChallengeArenaPlayerGate blocked = new ChallengeArenaPlayerGate(64, 64);
		blocked.setPosition(0, 0); lobby.add(blocked);
		assertFalse(ChallengeArenaObservation.stopWatching(viewer));
		assertTrue(viewer.isArenaSpectator());
		assertTrue(info.isInZone(viewer));
		assertEquals(2, SingletonRepository.getRPWorld().getInstanceZoneManager().getMemberCount(info.getZone().getID()));
		lobby.remove(blocked);
		assertTrue(ChallengeArenaObservation.stopWatching(viewer));
		assertSame(lobby, viewer.getZone());
		assertFalse(viewer.isArenaSpectator());
	}

	@Test public void spectatorsCompanionIsKeptSafeAndInactiveThenReturnsWithItsOwner() {
		final Player owner = player("WatchPetOwner"), viewer = player("WatchPetViewer");
		final ChallengeArenaInfo info = start(owner);
		final Sheep sheep = new Sheep(viewer);
		sheep.setPosition(26, 44); lobby.add(sheep); viewer.setSheep(sheep);
		open(viewer, 27); assertTrue(ChallengeArenaObservation.watch(viewer, owner.getName()));
		assertSame(info.getZone(), sheep.getZone());
		assertSame(sheep, viewer.getSheep());
		assertFalse(sheep.isAttackable());
		assertTrue(sheep.isInvisibleToCreatures());
		final int x = sheep.getX(), y = sheep.getY();
		sheep.setTarget(owner);
		for (int turn = 0; turn < 10; turn++) { sheep.logic(); }
		assertFalse(sheep.isAttacking());
		assertEquals(x, sheep.getX()); assertEquals(y, sheep.getY());
		assertTrue(ChallengeArenaObservation.stopWatching(viewer));
		assertSame(lobby, sheep.getZone());
		assertSame(sheep, viewer.getSheep());
		assertTrue(sheep.isAttackable());
		assertFalse(sheep.isInvisibleToCreatures());
		viewer.removeSheep(sheep); lobby.remove(sheep);
	}

	@Test public void ownerLogoutCannotDestroyAMapWhileASpectatorIsWaitingToReturn() throws Exception {
		final Player owner = player("WatchHeldOwner"), viewer = player("WatchHeldViewer");
		final ChallengeArenaInfo info = start(owner);
		open(viewer, 27); assertTrue(ChallengeArenaObservation.watch(viewer, owner.getName()));
		final ChallengeArenaPlayerGate blocked = new ChallengeArenaPlayerGate(64, 64);
		blocked.setPosition(0, 0); lobby.add(blocked);
		Player.destroy(owner);
		SingletonRepository.getRPWorld().getInstanceZoneManager().releaseMember(owner.getName());
		ChallengeArenaManager.release(owner.getName(), info);
		assertSame(info.getZone(), SingletonRepository.getRPWorld().getZone(info.getZone().getName()));
		assertTrue(viewer.isArenaSpectator());
		assertTrue(info.isInZone(viewer));
		lobby.remove(blocked);
		assertTrue(ChallengeArenaObservation.returnViewers(info));
		assertSame(lobby, viewer.getZone());
		assertFalse(viewer.isArenaSpectator());
		assertNull(SingletonRepository.getRPWorld().getZone(info.getZone().getName()));
		assertEquals(initialInstances, SingletonRepository.getRPWorld().getInstanceZoneManager().getInstanceCount());
	}
}
