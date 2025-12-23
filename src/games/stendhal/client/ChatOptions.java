package games.stendhal.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import games.stendhal.client.gui.chattext.ChatTextController;
import marauroa.common.game.RPAction;

/**
 * Stores the current NPC chat options for the desktop client.
 */
public final class ChatOptions {

	/**
	 * Representation of a chat option from the server.
	 */
	public static class Option {
		private final String trigger;
		private final String label;

		public Option(final String trigger, final String label) {
			this.trigger = trigger;
			this.label = label;
		}

		public String getTrigger() {
			return trigger;
		}

		public String getLabel() {
			return label;
		}
	}

	private static final List<Option> options = new ArrayList<Option>();
	private static String attendingNPC;

	private ChatOptions() {
		// utility class
	}

	public static void update(final String npcName, final String title, final List<Option> updatedOptions) {
		attendingNPC = (title != null && !title.isEmpty()) ? title : npcName;
		options.clear();
		options.addAll(updatedOptions);

		if (options.size() == 1 && "hello".equalsIgnoreCase(options.get(0).getTrigger())) {
			attendingNPC = null;
		}
	}

	public static List<Option> getOptions() {
		return Collections.unmodifiableList(options);
	}

	public static String getAttendingNPC() {
		return attendingNPC;
	}

	public static boolean isAttending(final String npcName) {
		if (attendingNPC == null) {
			return false;
		}

		final String normalized = attendingNPC.toLowerCase(Locale.ENGLISH);
		return npcName != null && npcName.toLowerCase(Locale.ENGLISH).equals(normalized);
	}

	public static String findKeyword(final Collection<String> aliases) {
		for (final Option option : options) {
			if (matches(option.getTrigger(), aliases) || matches(option.getLabel(), aliases)) {
				return option.getTrigger();
			}
		}

		return null;
	}

	private static boolean matches(final String value, final Collection<String> aliases) {
		for (final String alias : aliases) {
			if (value != null && value.equalsIgnoreCase(alias)) {
				return true;
			}
		}

		return false;
	}

	public static void sendKeyword(final String keyword) {
		if (keyword == null || keyword.isEmpty()) {
			return;
		}

		final RPAction chat = new RPAction("chat");
		chat.put("type", "chat");
		chat.put("text", keyword);
		ClientSingletonRepository.getClientFramework().send(chat);
		ChatTextController.get().setChatLine(keyword);
	}
}