package games.stendhal.server.core.events.advent;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.core.events.LoginNotifier;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.player.Player;

/** Keeps connected clients in sync across midnight and runtime event changes. */
public final class AdventCalendarAvailability implements LoginListener, TurnListener {
	private final AdventCalendarService calendar;

	public AdventCalendarAvailability(final AdventCalendarService calendar) {
		this.calendar = calendar;
	}

	public void start() {
		LoginNotifier.get().addListener(this);
		TurnNotifier.get().notifyInTurns(0, this);
	}

	@Override
	public void onLoggedIn(final Player player) {
		calendar.updateAvailability(player);
	}

	@Override
	public void onTurnReached(final int turn) {
		try {
			for (final Player player : SingletonRepository.getRuleProcessor().getOnlinePlayers().getAllPlayers()) {
				calendar.updateAvailability(player);
			}
		} finally {
			TurnNotifier.get().notifyInSeconds(1, this);
		}
	}
}
