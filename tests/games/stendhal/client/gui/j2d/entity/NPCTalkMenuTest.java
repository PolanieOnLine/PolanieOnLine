package games.stendhal.client.gui.j2d.entity;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.awt.GraphicsEnvironment;

import org.junit.Assume;
import org.junit.Test;

import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.NPC;
import games.stendhal.client.entity.User;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

public class NPCTalkMenuTest {
	private NPC npc(final boolean talkable) {
		final RPObject object = new RPObject();
		object.setID(new RPObject.ID(17, "talk_zone"));
		object.put("zoneid", "talk_zone");
		object.put("name", "Rozmówca");
		object.put("type", "npc");
		object.put("no_attack", "");
		if (talkable) object.put("talkable", "");
		final NPC npc = new NPC() {
			{ rpObject = object; }
		};
		return npc;
	}

	@Test
	public void speakingNpcOffersTalkWithoutRemovingLook() {
		Assume.assumeFalse("Client views require a display", GraphicsEnvironment.isHeadless());
		final NPC2DView<NPC> view = new NPC2DView<NPC>();
		view.entity = npc(true);
		final List<String> actions = new ArrayList<String>();
		view.buildActions(actions);
		assertEquals("Porozmawiaj", actions.get(0));
		assertTrue(actions.contains("Zobacz"));
		assertFalse(actions.contains("Popchnij"));
	}

	@Test
	public void silentNpcDoesNotOfferTalk() {
		Assume.assumeFalse("Client views require a display", GraphicsEnvironment.isHeadless());
		final NPC2DView<NPC> view = new NPC2DView<NPC>();
		view.entity = npc(false);
		final List<String> actions = new ArrayList<String>();
		view.buildActions(actions);
		assertFalse(actions.contains("Porozmawiaj"));
	}

	@Test
	public void talkActionTargetsOneNpcInItsZoneWithoutPlayerSpeech() {
		final RPAction action = ActionType.TALK.fillTargetInfo(npc(true));
		assertEquals("talk", action.get("type"));
		assertEquals("#17", action.get("target"));
		assertEquals("talk_zone", action.get("zone"));
		assertFalse(action.has("text"));
	}

	@Test
	public void conversationStateMatchesOnlyTheCurrentPlayerAndCanBeRemoved() {
		final NPC npc = npc(true);
		assertFalse(npc.isTalkingTo("MenuPlayer"));
		npc.getRPObject().put("conversation_player", "MenuPlayer");
		assertTrue(npc.isTalkingTo("MenuPlayer"));
		assertFalse(npc.isTalkingTo("OtherPlayer"));
		assertFalse(npc.isTalkingTo(null));
		npc.getRPObject().remove("conversation_player");
		assertFalse(npc.isTalkingTo("MenuPlayer"));
	}

	@Test
	public void activeConversationHidesTalkOnlyFromItsPlayer() throws Exception {
		Assume.assumeFalse("Client views require a display", GraphicsEnvironment.isHeadless());
		final java.lang.reflect.Field proxy = User.class.getDeclaredField("userProxy");
		proxy.setAccessible(true);
		final Object originalProxy = proxy.get(null);
		try {
			new User() {
				@Override public String getName() { return "MenuPlayer"; }
			};
			final NPC2DView<NPC> view = new NPC2DView<NPC>();
			view.entity = npc(true);
			view.entity.getRPObject().put("conversation_player", "MenuPlayer");
			final List<String> actions = new ArrayList<String>();
			view.buildActions(actions);
			assertFalse(actions.contains("Porozmawiaj"));
			assertTrue(actions.contains("Zobacz"));
			view.entity.getRPObject().put("conversation_player", "OtherPlayer");
			actions.clear();
			view.buildActions(actions);
			assertTrue(actions.contains("Porozmawiaj"));
			view.entity.getRPObject().remove("conversation_player");
			actions.clear();
			view.buildActions(actions);
			assertTrue(actions.contains("Porozmawiaj"));
		} finally {
			proxy.set(null, originalProxy);
		}
	}
}
