package games.stendhal.server.actions;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;
import static utilities.SpeakerNPCTestHelper.getReplies;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Actions;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPC;
import games.stendhal.server.entity.npc.PlayerPrivateSpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;

public class TalkActionTest {
	private static final AtomicInteger ZONE_NUMBER = new AtomicInteger();
	private StendhalRPZone zone;
	private Player player;
	private SpeakerNPC npc;
	private final TalkAction action = new TalkAction();

	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Before
	public void setUp() {
		zone = new StendhalRPZone("test_talk_" + ZONE_NUMBER.incrementAndGet(), 40, 40);
		MockStendlRPWorld.get().addRPZone(zone);
		player = PlayerTestHelper.createPlayer("TalkPlayer");
		player.setPosition(10, 10);
		zone.add(player);
		npc = speaker("Rozmówca", 12, 10);
	}

	private SpeakerNPC speaker(final String name, final int x, final int y) {
		final SpeakerNPC speaker = new SpeakerNPC(name + " " + ZONE_NUMBER.get());
		speaker.setPosition(x, y);
		speaker.addGreeting("Witaj.");
		zone.add(speaker);
		return speaker;
	}

	private RPAction request(final NPC target) {
		final RPAction request = new RPAction();
		request.put(Actions.TYPE, Actions.TALK);
		request.put(Actions.ZONE, zone.getName());
		request.put(Actions.TARGET, "#" + target.getID().getObjectID());
		return request;
	}

	@Test
	public void greetsOnlySelectedNpcWithoutPublicSpeech() {
		final SpeakerNPC other = speaker("Drugi rozmówca", 11, 10);
		final RPAction request = request(npc);
		request.put(Actions.TEXT, "zadanie"); // Untrusted text cannot change the greeting.
		assertTrue(CommandCenter.execute(player, request));
		assertNull(npc.getAttending());
		npc.preLogic();
		other.preLogic();
		assertSame(player, npc.getAttending());
		assertEquals("Witaj.", getReply(npc));
		assertFalse(player.has("text"));
		assertNull(other.getAttending());
		assertNull(getReply(other));
		assertTrue(npc.has("talkable"));
	}

	@Test
	public void duplicateClicksProduceOneGreetingAndDoNotRestartAnActiveDialog() {
		for (int i = 0; i < 10; i++) action.onAction(player, request(npc));
		npc.preLogic();
		assertEquals(java.util.Collections.singletonList("Witaj."), getReplies(npc));
		npc.setCurrentState(ConversationStates.QUEST_OFFERED);
		action.onAction(player, request(npc));
		npc.preLogic();
		assertSame(player, npc.getAttending());
		assertEquals(ConversationStates.QUEST_OFFERED, npc.getEngine().getCurrentState());
		assertNull(getReply(npc));
	}

	@Test
	public void busyNpcKeepsItsCurrentPlayer() {
		final Player other = PlayerTestHelper.createPlayer("OtherTalkPlayer");
		other.setPosition(11, 10);
		zone.add(other);
		npc.listenTo(other, "hi");
		getReply(npc);
		action.onAction(player, request(npc));
		npc.preLogic();
		assertSame(other, npc.getAttending());
		assertNotNull(getReply(npc));
		assertFalse(player.has("text"));
	}

	@Test
	public void respectsNormalRectangularConversationRange() {
		npc.setPosition(14, 14);
		action.onAction(player, request(npc));
		npc.preLogic();
		assertSame(player, npc.getAttending());
		npc.endConversation();
		npc.setPosition(15, 10);
		action.onAction(player, request(npc));
		npc.preLogic();
		assertNull(npc.getAttending());
		assertFalse(player.has("text"));
	}

	@Test
	public void rejectsMalformedTargetsStaleZonesAndNonSpeakers() {
		for (final String target : new String[] {"#abc", "#", "#99999999999999", "#-1", "#999999", "Rozmówca", "#" + player.getID().getObjectID()}) {
			final RPAction request = request(npc);
			request.put(Actions.TARGET, target);
			action.onAction(player, request);
		}
		final RPAction wrongZone = request(npc);
		wrongZone.put(Actions.ZONE, "old_zone");
		action.onAction(player, wrongZone);
		wrongZone.remove(Actions.ZONE);
		action.onAction(player, wrongZone);
		action.onAction(player, new RPAction());
		final NPC silent = new NPC() {
			@Override
			public void preLogic() { }
		};
		silent.setPosition(11, 11);
		zone.add(silent);
		assertFalse(silent.has("talkable"));
		action.onAction(player, request(silent));
		npc.preLogic();
		assertNull(npc.getAttending());
	}

	@Test
	public void queuedGreetingIsDiscardedAfterPlayerLeavesRange() {
		action.onAction(player, request(npc));
		player.setPosition(30, 30);
		npc.preLogic();
		player.setPosition(10, 10);
		npc.preLogic();
		assertNull(npc.getAttending());
		assertFalse(npc.has("text"));
	}

	@Test
	public void privateNpcOnlyTalksToItsOwner() {
		final Player owner = PlayerTestHelper.createPlayer("PrivateOwner");
		owner.setPosition(10, 11);
		zone.add(owner);
		final PlayerPrivateSpeakerNPC privateNpc = new PlayerPrivateSpeakerNPC(owner, "Prywatny rozmówca");
		privateNpc.setPosition(11, 11);
		privateNpc.addGreeting("Witaj właścicielu.");
		zone.add(privateNpc);
		action.onAction(player, request(privateNpc));
		privateNpc.preLogic();
		assertNull(privateNpc.getAttending());
		action.onAction(owner, request(privateNpc));
		privateNpc.preLogic();
		assertSame(owner, privateNpc.getAttending());
	}

	@Test
	public void respectsTheChatSpamLimit() {
		for (int i = 0; i < 10000; i++) player.getChatBucket().checkAndAdd(1);
		action.onAction(player, request(npc));
		npc.preLogic();
		assertNull(npc.getAttending());
		assertFalse(player.has("text"));
	}

	@Test
	public void mutedPlayerCannotUseTalkToBypassChatRestrictions() {
		player.setQuest("gag", Long.toString(System.currentTimeMillis() + 60000));
		action.onAction(player, request(npc));
		npc.preLogic();
		assertNull(npc.getAttending());
		assertNull(getReply(npc));
	}

	@Test
	public void leavingTheZoneCancelsTheQueuedGreeting() {
		action.onAction(player, request(npc));
		zone.remove(player);
		npc.preLogic();
		zone.add(player);
		npc.preLogic();
		assertNull(npc.getAttending());
		assertNull(getReply(npc));
	}

	@Test
	public void walkingPrivateNpcCannotBeStoppedByTheTalkMenu() {
		final PlayerPrivateSpeakerNPC privateNpc = new PlayerPrivateSpeakerNPC(player, "Wędrowiec");
		privateNpc.setPosition(11, 11);
		privateNpc.setBaseSpeed(1.0);
		privateNpc.addGreeting("Witaj.");
		zone.add(privateNpc);
		action.onAction(player, request(privateNpc));
		privateNpc.setPath(new games.stendhal.server.core.pathfinder.FixedPath(java.util.Arrays.asList(
				new games.stendhal.server.core.pathfinder.Node(11, 11),
				new games.stendhal.server.core.pathfinder.Node(12, 11)), false));
		privateNpc.preLogic();
		assertNull(privateNpc.getAttending());
		privateNpc.clearPath();
		privateNpc.preLogic();
		assertNull(privateNpc.getAttending());
		assertNull(getReply(privateNpc));
	}
}
