package games.stendhal.server.events;

import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.Events;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.AchievementNotifier;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.SyntaxException;

public class AchievementsLogEvent extends RPEvent {
	/** The logger instance. */
	private static final Logger logger = Logger.getLogger(AchievementsLogEvent.class);

	/**
	 * Creates the rpclass.
	 */
	public static void generateRPClass() {
		try {
			final RPClass rpclass = new RPClass(Events.ACHIEVEMENTS_LOG);
			rpclass.addAttribute("achievements", Type.VERY_LONG_STRING, Definition.PRIVATE);
		} catch (final SyntaxException e) {
			logger.error("cannot generateRPClass", e);
		}
	}

	public AchievementsLogEvent(final Player player) {
		super(Events.ACHIEVEMENTS_LOG);

		final int achievementCount = AchievementNotifier.get().getAchievements().size();
		final StringBuilder sb = new StringBuilder();

		int idx = 0;
		for (Achievement a : AchievementNotifier.get().getAchievements()) {
			if (!a.isActive()) {
				continue;
			}

			Boolean did = false;
			if (a.isFulfilled(player)) {
				did = true;
			}

			sb.append(a.getTitle() + "," + a.getDescription() + "," + did.toString());

			if (idx != achievementCount - 1) {
				sb.append(";");
			}

			idx++;
		}

		put("achievements", sb.toString());
	}
}
