package games.stendhal.server.core.events.advent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.common.constants.Events;
import games.stendhal.server.core.events.advent.AdventCalendarService.AccountState;
import games.stendhal.server.core.events.seasonal.SeasonalEventType;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.AdventCalendarEvent;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;

public class AdventCalendarEventTest {
	private String previousChristmas;

	@Before
	public void enableChristmas() {
		previousChristmas = System.getProperty(SeasonalEventType.CHRISTMAS.getProperty());
		System.setProperty(SeasonalEventType.CHRISTMAS.getProperty(), "true");
	}

	@After
	public void restoreChristmas() {
		final String property = SeasonalEventType.CHRISTMAS.getProperty();
		if (previousChristmas == null) {
			System.clearProperty(property);
		} else {
			System.setProperty(property, previousChristmas);
		}
	}

	@Test
	public void inactiveEventDisablesAllDoorsWithoutRevealingGifts() throws Exception {
		System.clearProperty(SeasonalEventType.CHRISTMAS.getProperty());
		final AdventCalendarService calendar = new AdventCalendarService(
				Clock.fixed(Instant.parse("2026-12-10T12:00:00Z"), ZoneOffset.UTC),
				(owner, year) -> new AccountState(1, 0), day -> Collections.emptyList());
		final AdventCalendarEvent event = new AdventCalendarEvent(
				calendar.process(PlayerTestHelper.createPlayer("inactive_advent"), "open", 0), "inactive");
		assertEquals("inactive", event.get("status"));
		assertEquals(0, event.getInt("today"));
		assertEquals(Collections.nCopies(24, "inactive"), event.getList("states"));
		assertFalse(event.has("reward_names"));
	}

	@Test
	public void unopenedCalendarSerializesForOwnerWithoutRewardSpoilers() throws Exception {
		final Player player = PlayerTestHelper.createPlayer("advent_event");
		final AdventCalendarService calendar = new AdventCalendarService(
				Clock.fixed(Instant.parse("2026-12-10T12:00:00Z"), ZoneOffset.UTC),
				(owner, year) -> new AccountState(1, 0), day -> Collections.emptyList());
		player.addEvent(new AdventCalendarEvent(calendar.process(player, "open", 0), "test-open"));
		for (final DetailLevel level : new DetailLevel[] {DetailLevel.FULL, DetailLevel.PRIVATE, DetailLevel.NORMAL}) {
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			player.writeObject(new OutputSerializer(bytes), level);
			final RPObject restored = new RPObject();
			restored.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
			RPEvent calendarEvent = null;
			for (final RPEvent event : restored.events()) {
				if (Events.ADVENT_CALENDAR.equals(event.getName())) {
					calendarEvent = event;
				}
			}
			if (level == DetailLevel.NORMAL) {
				assertNull(calendarEvent);
			} else {
				assertNotNull(calendarEvent);
				assertEquals(24, calendarEvent.getList("states").size());
				assertEquals(0, calendarEvent.getInt("revealed_day"));
				for (final String key : new String[] {"reward_names", "reward_classes", "reward_subclasses", "reward_quantities"}) {
					assertFalse(calendarEvent.has(key));
				}
			}
		}
	}

	@Test
	public void configuredGiftsCanBeCreatedFromTheRealItemCatalogue() throws Exception {
		PlayerTestHelper.createPlayer("advent_catalogue");
		final List<List<AdventCalendarRewards.Reward>> days = AdventCalendarRewards.read(
				new StringReader(AdventCalendarRewardsTest.exampleCalendar()));
		for (final List<AdventCalendarRewards.Reward> rewards : days) {
			final List<Item> items = AdventCalendarRewards.createItems(rewards);
			assertEquals(rewards.size(), items.size());
			for (int index = 0; index < items.size(); index++) {
				final Item item = items.get(index);
				assertNotNull(item);
				assertEquals(rewards.get(index).name, item.getName());
				assertNotNull(item.getItemSubclass());
			}
		}
	}
}
