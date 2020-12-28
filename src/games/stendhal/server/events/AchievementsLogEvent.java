package games.stendhal.server.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import marauroa.server.db.command.ResultHandle;

/**
 * @author KarajuSs
 */
public class AchievementsLogEvent extends RPEvent {
	/** The logger instance. */
	private static final Logger logger = Logger.getLogger(AchievementsLogEvent.class);

	private final List<Achievement> achievementsArray = new ArrayList<>();;
	private ResultHandle handle = new ResultHandle();

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

		AchievementNotifier achievements = AchievementNotifier.get();
		for (final Achievement a: achievements.getAchievements()) {
			achievementsArray.add(a);
		}

		final StringBuilder formatted = new StringBuilder();
		SortArray(achievementsArray);
		formatted.append(getFormattedString(player, achievementsArray));

		put("achievements", formatted.toString());
	}

	private void SortArray(List<Achievement> array) {
		final Comparator<Achievement> sorter_id = new Comparator<Achievement>() {
			@Override
			public int compare(final Achievement a1, final Achievement a2) {
				return (a1.getIdentifier().toLowerCase().compareTo(a2.getIdentifier().toLowerCase()));
			}
		};
		Collections.sort(array, sorter_id);

		final Comparator<Achievement> sorter_cat = new Comparator<Achievement>() {
			@Override
			public int compare(final Achievement a1, final Achievement a2) {
				return (a1.getCategory().toString().toLowerCase().compareTo(a2.getCategory().toString().toLowerCase()));
			}
		};
		Collections.sort(array, sorter_cat);
	}

	private String getFormattedString(final Player player, final List<Achievement> achievements) {
		final StringBuilder sb = new StringBuilder();
		final int achievementCount = AchievementNotifier.get().getAchievements().size();

		int idx = 0;
		for (final Achievement a : achievements) {
			if (!a.isActive()) {
				continue;
			}

			Boolean reached = player.hasReachedAchievement(a.getIdentifier());
			sb.append(a.getCategory() + ":" + a.getTitle() + ":" + a.getDescription() + ":" + reached.toString());

			if (idx != achievementCount - 1) {
				sb.append(";");
			}

			idx++;
		}

		return sb.toString();
	}
}
