package games.stendhal.server.actions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.group.Group;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;

/** Regression tests for group actions targeting members who have disconnected. */
public class GroupManagementActionTest {
	private static final String LEADER = "groupActionLeader";
	private static final String OFFLINE_MEMBER = "lastochaos";
	private static final String OTHER_MEMBER = "otherGroupMember";

	private final MockStendhalRPRuleProcessor ruleProcessor = MockStendhalRPRuleProcessor.get();
	private final GroupManagementAction action = new GroupManagementAction();

	private Group group;
	private Player leader;

	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
	}

	@Before
	public void setUp() {
		ruleProcessor.clearPlayers();
		SingletonRepository.getGroupManager().destroyGroup(LEADER);
		leader = PlayerTestHelper.createPlayer(LEADER);
		ruleProcessor.addPlayer(leader);
		group = SingletonRepository.getGroupManager().createGroup(LEADER);
		group.addMember(OFFLINE_MEMBER);
		group.addMember(OTHER_MEMBER);
		assertNull(ruleProcessor.getPlayer(OFFLINE_MEMBER));
	}

	@After
	public void tearDown() {
		SingletonRepository.getGroupManager().destroyGroup(LEADER);
		ruleProcessor.clearPlayers();
	}

	private RPAction request(String subAction, String target) {
		final RPAction request = new RPAction();
		request.put("action", subAction);
		request.put("params", target);
		return request;
	}

	@Test
	public void leaderCanKickDisconnectedMemberWithoutNullPointerException() {
		action.onAction(leader, request("kick", OFFLINE_MEMBER));
		assertFalse(group.hasMember(OFFLINE_MEMBER));
		assertTrue(group.hasMember(LEADER));
		assertTrue(group.hasMember(OTHER_MEMBER));
	}

	@Test
	public void offlineInviteTargetGetsErrorInsteadOfNullPointerException() {
		action.onAction(leader, request("invite", OFFLINE_MEMBER));
		assertTrue(group.hasMember(OFFLINE_MEMBER));
	}

	@Test
	public void offlineLeadershipTargetGetsErrorInsteadOfNullPointerException() {
		action.onAction(leader, request("leader", OFFLINE_MEMBER));
		assertTrue(group.hasLeader(LEADER));
		assertTrue(group.hasMember(OFFLINE_MEMBER));
	}

	@Test
	public void nonLeaderCannotKickDisconnectedMember() {
		final Player other = PlayerTestHelper.createPlayer(OTHER_MEMBER);
		ruleProcessor.addPlayer(other);
		action.onAction(other, request("kick", OFFLINE_MEMBER));
		assertTrue(group.hasMember(OFFLINE_MEMBER));
	}
}
