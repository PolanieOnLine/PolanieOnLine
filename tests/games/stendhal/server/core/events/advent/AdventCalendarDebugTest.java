/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.events.advent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.common.Translation;
import games.stendhal.server.core.engine.db.AdventCalendarDAO;
import games.stendhal.server.core.events.advent.AdventCalendarService.AccountState;
import games.stendhal.server.core.events.advent.AdventCalendarService.Result;
import games.stendhal.server.core.events.seasonal.SeasonalEventType;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.AdventCalendarEvent;
import utilities.PlayerTestHelper;

public class AdventCalendarDebugTest {
	private static final String DEBUG = "stendhal.advent.debug";
	private static final String CHRISTMAS = SeasonalEventType.CHRISTMAS.getProperty();
	private String previousDebug;
	private String previousChristmas;

	@Before
	public void rememberProperties() {
		previousDebug = System.getProperty(DEBUG);
		previousChristmas = System.getProperty(CHRISTMAS);
		System.clearProperty(DEBUG);
		System.clearProperty(CHRISTMAS);
	}

	@After
	public void restoreProperties() {
		restore(DEBUG, previousDebug);
		restore(CHRISTMAS, previousChristmas);
	}

	private static void restore(final String key, final String value) {
		if (value == null) System.clearProperty(key);
		else System.setProperty(key, value);
	}

	private Player player(final String name, final int level) {
		final Player player = PlayerTestHelper.createPlayer(name);
		player.put("adminlevel", level);
		return player;
	}

	private AdventCalendarService calendar(final String instant) {
		return new AdventCalendarService(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC),
				(owner, year) -> new AccountState(42, 0), day -> {
					final StackableItem coins = new StackableItem("money", "money", "money", Collections.emptyMap());
					coins.setQuantity(5000);
					return Collections.singletonList(coins);
				});
	}

	@Test
	public void explicitlyEnabledAdminCanOpenAllDoorsOutsideChristmasWithoutChangingRealDate() throws Exception {
		System.setProperty(DEBUG, "true");
		final Player admin = player("advent_debug_admin", 35);
		final AdventCalendarService service = calendar("2026-11-15T12:00:00Z");
		final Result opened = service.process(admin, "open", 0);
		assertEquals("ok", opened.status);
		assertEquals("2026-11-15", opened.date.toString());
		assertTrue(opened.message, opened.message.contains("Tryb testowy"));
		final AdventCalendarEvent event = new AdventCalendarEvent(opened, "debug-open");
		assertEquals(Collections.nCopies(24, "available"), event.getList("states"));
		assertEquals(0, event.getInt("today"));
		assertFalse(event.has("reward_names"));
		assertEquals(0, service.process(admin, "view", 24).revealedDay);
		assertEquals("received", service.process(admin, "claim", 1).status);
		assertEquals("received", service.process(admin, "claim", 24).status);
		assertEquals(10000, admin.getNumberOfEquipped("money"));
		final AdventCalendarEvent refreshed = new AdventCalendarEvent(service.process(admin, "refresh", 0), "debug-refresh");
		assertEquals("claimed", refreshed.getList("states").get(0));
		assertEquals("claimed", refreshed.getList("states").get(23));
		assertEquals("available", refreshed.getList("states").get(12));
	}

	@Test
	public void adminStillNeedsAnExplicitTrueFlag() throws Exception {
		final Player admin = player("advent_debug_off", 5000);
		final AdventCalendarService service = calendar("2026-11-15T12:00:00Z");
		for (final String value : new String[] { null, "false", "1", "" }) {
			restore(DEBUG, value);
			System.clearProperty(CHRISTMAS);
			assertEquals("inactive", service.process(admin, "claim", 24).status);
			System.setProperty(CHRISTMAS, "true");
			assertEquals("inactive", service.process(admin, "claim", 24).status);
		}
		assertEquals(0, admin.getNumberOfEquipped("money"));
	}

	@Test
	public void debugDoesNotUnlockDatesOrSeasonForOrdinaryPlayersOrSupportStaff() throws Exception {
		System.setProperty(DEBUG, "true");
		for (final int level : new int[] {0, 1, 34}) {
			final Player player = player("advent_debug_player_" + level, level);
			final AdventCalendarService november = calendar("2026-11-15T12:00:00Z");
			System.clearProperty(CHRISTMAS);
			assertEquals("inactive", november.process(player, "claim", 24).status);
			System.setProperty(CHRISTMAS, "true");
			final Result opened = november.process(player, "open", 0);
			assertFalse(opened.message.contains("Tryb testowy"));
			assertEquals(Collections.nCopies(24, "inactive"), new AdventCalendarEvent(opened, "normal-open").getList("states"));
			assertEquals("inactive", november.process(player, "claim", 24).status);
			final AdventCalendarService december = calendar("2026-12-10T12:00:00Z");
			assertEquals("expired", december.process(player, "claim", 6).status);
			assertEquals("future", december.process(player, "claim", 11).status);
			assertEquals(0, player.getNumberOfEquipped("money"));
			assertFalse(player.hasQuest(AdventCalendarDAO.QUEST_SLOT));
		}
	}

	@Test
	public void adminCanAlsoTestExpiredAndFutureDecemberDoors() throws Exception {
		System.setProperty(DEBUG, "true");
		final Player admin = player("advent_debug_december", 35);
		final AdventCalendarService service = calendar("2026-12-10T12:00:00Z");
		assertEquals("received", service.process(admin, "claim", 1).status);
		assertEquals("received", service.process(admin, "claim", 24).status);
	}

	@Test
	public void debugDoesNotBypassAccountClaimsOrResetThemWhenDisabled() throws Exception {
		System.setProperty(DEBUG, "true");
		final AdventCalendarService service = calendar("2026-12-24T12:00:00Z");
		final Player first = player("advent_debug_first", 35);
		final Player second = player("advent_debug_second", 5000);
		assertEquals("received", service.process(first, "claim", 24).status);
		assertEquals("claimed", service.process(first, "claim", 24).status);
		assertEquals("claimed", service.process(second, "claim", 24).status);
		System.clearProperty(DEBUG);
		System.setProperty(CHRISTMAS, "true");
		assertEquals("claimed", service.process(first, "claim", 24).status);
		assertEquals("claimed", service.process(second, "claim", 24).status);
		assertEquals(5000, first.getNumberOfEquipped("money"));
		assertEquals(0, second.getNumberOfEquipped("money"));
	}

	@Test
	public void invalidAndFullBagClaimsStayBlockedInDebug() throws Exception {
		System.setProperty(DEBUG, "true");
		final Player admin = player("advent_debug_full", 35);
		final AdventCalendarService service = calendar("2026-11-15T12:00:00Z");
		for (final int day : new int[] {Integer.MIN_VALUE, -1, 0, 25, Integer.MAX_VALUE}) {
			final Result invalid = service.process(admin, "claim", day);
			assertEquals("invalid", invalid.status);
			assertEquals(0, invalid.revealedDay);
		}
		while (!admin.getSlot("bag").isFull()) {
			admin.getSlot("bag").add(new Item("debug bag filler", "cloak", "test", Collections.emptyMap()));
		}
		final Result full = service.process(admin, "claim", 24);
		assertEquals("no_space", full.status);
		assertEquals(0, full.revealedDay);
		assertEquals(0, full.mask);
		assertFalse(admin.hasQuest(AdventCalendarDAO.QUEST_SLOT));
		admin.getSlot("bag").remove(admin.getSlot("bag").getFirst().getID());
		assertEquals("received", service.process(admin, "claim", 24).status);
	}

	@Test
	public void debugMessagesTranslateBothTheNoticeAndNormalCalendarMessage() throws Exception {
		System.setProperty(DEBUG, "true");
		final Player admin = player("advent_debug_translated", 35);
		final AdventCalendarService service = calendar("2026-11-15T12:00:00Z");
		final Result opened = service.process(admin, "open", 0);
		assertEquals("Administrator test mode. You can open any door. Gifts can only be collected once per account.",
				Translation.getText("en", opened.message));
		assertEquals("Modo de prueba del administrador. Puedes abrir cualquier casilla. Los regalos solo se pueden recoger una vez por cuenta.",
				Translation.getText("es", opened.message));
		final Result claimed = service.process(admin, "claim", 24);
		assertEquals("Administrator test mode. Your Christmas gift has been added to your backpack.",
				Translation.getText("en", claimed.message));
	}
}
