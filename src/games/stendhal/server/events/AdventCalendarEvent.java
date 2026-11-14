package games.stendhal.server.events;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.common.AdventCalendarLayout;
import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.advent.AdventCalendarRewards;
import games.stendhal.server.core.events.advent.AdventCalendarRewards.Reward;
import games.stendhal.server.core.events.advent.AdventCalendarRules;
import games.stendhal.server.core.events.advent.AdventCalendarService.Result;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;

/** Private states and only the gift the account has successfully opened. */
public final class AdventCalendarEvent extends RPEvent {

	public static void generateRPClass() {
		final RPClass rpclass = new RPClass(Events.ADVENT_CALENDAR);
		for (final String key : new String[] {"request_id", "status", "date"}) {
			rpclass.addAttribute(key, Type.STRING, Definition.PRIVATE);
		}
		rpclass.addAttribute("message", Type.LONG_STRING, Definition.PRIVATE);
		for (final String key : new String[] {"year", "today", "revealed_day"}) {
			rpclass.addAttribute(key, Type.INT, Definition.PRIVATE);
		}
		for (final String key : new String[] {"states", "layout", "reward_names", "reward_classes",
				"reward_subclasses", "reward_quantities"}) {
			rpclass.addAttribute(key, Type.VERY_LONG_STRING, Definition.PRIVATE);
		}
	}

	public AdventCalendarEvent(final Result result, final String request) {
		super(Events.ADVENT_CALENDAR);
		put("request_id", request);
		put("status", result.status);
		put("message", result.message);
		put("date", result.date.toString());
		put("year", result.date.getYear());
		final boolean inactive = "inactive".equals(result.status);
		put("today", !inactive && result.date.getMonthValue() == 12 ? result.date.getDayOfMonth() : 0);
		put("revealed_day", result.revealedDay);
		final List<String> states = new ArrayList<>();
		for (int day = 1; day <= 24; day++) {
			states.add(inactive ? "inactive" : AdventCalendarRules.state(result.date, day, result.mask));
		}
		put("states", states);
		final List<String> layout = new ArrayList<>();
		for (final int[] door : AdventCalendarLayout.doors()) {
			layout.add(door[0] + "," + door[1] + "," + door[2] + "," + door[3] + "," + door[4]);
		}
		put("layout", layout);
		if (result.revealedDay == 0) {
			return;
		}
		final List<String> names = new ArrayList<>();
		final List<String> classes = new ArrayList<>();
		final List<String> subclasses = new ArrayList<>();
		final List<String> quantities = new ArrayList<>();
		for (final Reward reward : AdventCalendarRewards.forDay(result.revealedDay)) {
			final Item prototype = SingletonRepository.getEntityManager().getItem(reward.name, ItemCreationContext.restore());
			names.add(reward.name);
			classes.add(prototype.getItemClass());
			subclasses.add(prototype.getItemSubclass());
			quantities.add(Integer.toString(reward.quantity));
		}
		put("reward_names", names);
		put("reward_classes", classes);
		put("reward_subclasses", subclasses);
		put("reward_quantities", quantities);
	}
}
