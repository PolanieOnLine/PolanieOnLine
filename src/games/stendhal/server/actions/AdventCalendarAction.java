package games.stendhal.server.actions;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.Actions;
import games.stendhal.server.core.events.advent.AdventCalendarService;
import games.stendhal.server.core.events.advent.AdventCalendarService.Result;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.AdventCalendarEvent;
import marauroa.common.game.RPAction;

/** All dates, reward contents and account claim checks are server-owned. */
public final class AdventCalendarAction implements ActionListener {

	private static final Logger LOGGER = Logger.getLogger(AdventCalendarAction.class);
	private final AdventCalendarService calendar = new AdventCalendarService();

	public static void register() {
		CommandCenter.register(Actions.ADVENT_CALENDAR, new AdventCalendarAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		final String command = action.has("command") ? action.get("command") : "open";
		if (!"open".equals(command) && !"refresh".equals(command)
				&& !"claim".equals(command) && !"view".equals(command)) {
			return;
		}
		final String request = action.has("request_id") ? action.get("request_id") : "";
		if (request.length() > 80) {
			return;
		}
		try {
			final int day = action.has("day") ? Integer.parseInt(action.get("day")) : 0;
			final Result result = calendar.process(player, command, day);
			player.addEvent(new AdventCalendarEvent(result, request));
			if ("no_space".equals(result.status)) {
				player.sendPrivateText(result.message);
			}
		} catch (final Exception exception) {
			LOGGER.error("Unable to process advent calendar for " + player.getName(), exception);
			player.sendPrivateText("Nie udało się otworzyć kalendarza. Spróbuj ponownie za chwilę.");
		}
	}
}
