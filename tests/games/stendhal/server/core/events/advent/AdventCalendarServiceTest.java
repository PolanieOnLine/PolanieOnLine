package games.stendhal.server.core.events.advent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.common.AdventCalendarLayout;
import games.stendhal.server.core.engine.db.AdventCalendarDAO;
import games.stendhal.server.core.events.advent.AdventCalendarService.AccountState;
import games.stendhal.server.core.events.advent.AdventCalendarService.Result;
import games.stendhal.server.core.events.seasonal.SeasonalEventType;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class AdventCalendarServiceTest {
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
	public void followsServerChristmasToggleWithoutRestartAndRejectsDirectClaims() throws Exception {
		final AdventCalendarService service = new AdventCalendarService(
				Clock.fixed(Instant.parse("2026-12-10T12:00:00Z"), ZoneOffset.UTC),
				(player, year) -> new AccountState(42, 0), day -> Collections.singletonList(coins(5000)));
		final Player player = PlayerTestHelper.createPlayer("advent_xmas");
		final String property = SeasonalEventType.CHRISTMAS.getProperty();
		System.clearProperty(property);
		for (final String command : new String[] {"open", "refresh", "claim", "view"}) {
			final Result result = service.process(player, command, 10);
			assertEquals("inactive", result.status);
			assertEquals(0, result.revealedDay);
			assertTrue(result.items.isEmpty());
		}
		assertEquals(0, player.getNumberOfEquipped("money"));
		assertFalse(player.hasQuest(AdventCalendarDAO.QUEST_SLOT));
		System.setProperty(property, "true");
		assertEquals("received", service.process(player, "claim", 10).status);
		System.clearProperty(property);
		assertEquals("inactive", service.process(player, "claim", 9).status);
		assertEquals(5000, player.getNumberOfEquipped("money"));
		System.setProperty(property, "true");
		assertEquals("claimed", service.process(player, "claim", 10).status);
	}

	private StackableItem coins(final int quantity) {
		ItemTestHelper.generateRPClasses();
		final StackableItem item = new StackableItem("money", "money", "money", Collections.emptyMap());
		item.setQuantity(quantity);
		return item;
	}

	private void fillBackpack(final Player player) {
		ItemTestHelper.generateRPClasses();
		for (int count = 0; count < 100 && !player.getSlot("bag").isFull(); count++) {
			player.getSlot("bag").add(new Item("advent filler", "cloak", "test", Collections.emptyMap()));
		}
		assertTrue(player.getSlot("bag").isFull());
	}

	@Test
	public void twoCharactersCannotClaimTheSameDayBeforeTheirSaveFinishes() throws Exception {
		final AdventCalendarService service = new AdventCalendarService(
				Clock.fixed(Instant.parse("2026-12-10T12:00:00Z"), ZoneOffset.UTC),
				(player, year) -> new AccountState(17, 0), day -> Collections.singletonList(coins(5000)));
		final Player first = PlayerTestHelper.createPlayer("advent_first");
		final Player second = PlayerTestHelper.createPlayer("advent_second");
		assertEquals("received", service.process(first, "claim", 10).status);
		assertEquals("claimed", service.process(second, "claim", 10).status);
		assertEquals(5000, first.getNumberOfEquipped("money"));
		assertEquals(0, second.getNumberOfEquipped("money"));
		assertTrue(first.hasQuest(AdventCalendarDAO.QUEST_SLOT));
	}

	@Test
	public void anotherAccountCanReceiveItsOwnGift() throws Exception {
		final AdventCalendarService service = new AdventCalendarService(
				Clock.fixed(Instant.parse("2026-12-10T12:00:00Z"), ZoneOffset.UTC),
				(player, year) -> new AccountState(player.getName().endsWith("a") ? 1 : 2, 0),
				day -> Collections.singletonList(coins(5000)));
		assertEquals("received", service.process(PlayerTestHelper.createPlayer("advent_a"), "claim", 10).status);
		assertEquals("received", service.process(PlayerTestHelper.createPlayer("advent_b"), "claim", 10).status);
	}

	@Test
	public void fullBackpackKeepsGiftHiddenAndAllowsRetryAfterSpaceIsFreed() throws Exception {
		final AdventCalendarService service = new AdventCalendarService(
				Clock.fixed(Instant.parse("2026-12-10T12:00:00Z"), ZoneOffset.UTC),
				(player, year) -> new AccountState(21, 0), day -> Collections.singletonList(coins(5000)));
		final Player player = PlayerTestHelper.createPlayer("advent_full");
		fillBackpack(player);
		final Result blocked = service.process(player, "claim", 10);
		assertEquals("no_space", blocked.status);
		assertEquals(0, blocked.revealedDay);
		assertTrue(blocked.items.isEmpty());
		assertFalse(player.hasQuest(AdventCalendarDAO.QUEST_SLOT));
		assertEquals(0, blocked.mask);
		player.getSlot("bag").remove(player.getSlot("bag").getFirst().getID());
		assertEquals("received", service.process(player, "claim", 10).status);
	}

	@Test
	public void failedBundleRestoresMergedCoinsAndDoesNotDropAnything() {
		final Player player = PlayerTestHelper.createPlayer("advent_bundle");
		final StackableItem original = coins(100);
		player.getSlot("bag").add(original);
		fillBackpack(player);
		final int originalSize = player.getSlot("bag").size();
		final Item second = new Item("advent test cloak", "cloak", "test", Collections.emptyMap());
		assertFalse(AdventInventoryGrant.give(player, Arrays.asList(coins(5000), second)));
		assertEquals(100, original.getQuantity());
		assertEquals(originalSize, player.getSlot("bag").size());
	}

	@Test
	public void closedAndExpiredDaysNeverRevealTheirContents() throws Exception {
		final AdventCalendarService service = new AdventCalendarService(
				Clock.fixed(Instant.parse("2026-12-10T12:00:00Z"), ZoneOffset.UTC),
				(player, year) -> new AccountState(31, 0), day -> {
					throw new AssertionError("Closed rewards must not be created");
				});
		final Player player = PlayerTestHelper.createPlayer("advent_hidden");
		for (final int day : new int[] {0, 6, 11, 24, 25}) {
			final Result result = service.process(player, "claim", day);
			assertEquals(0, result.revealedDay);
			assertTrue(result.items.isEmpty());
		}
		assertEquals(0, service.process(player, "view", 10).revealedDay);
	}

	@Test
	public void layoutCoversThirtyCellsWithTwentyFourUniqueDays() {
		final boolean[][] occupied = new boolean[5][6];
		final boolean[] days = new boolean[25];
		for (final int[] door : AdventCalendarLayout.doors()) {
			assertFalse(days[door[0]]);
			days[door[0]] = true;
			for (int row = door[2]; row < door[2] + door[4]; row++) {
				for (int column = door[1]; column < door[1] + door[3]; column++) {
					assertFalse(occupied[row][column]);
					occupied[row][column] = true;
				}
			}
		}
		for (int day = 1; day <= 24; day++) {
			assertTrue(days[day]);
		}
		for (final boolean[] row : occupied) {
			for (final boolean cell : row) {
				assertTrue(cell);
			}
		}
	}

}
