/***************************************************************************
 *                    (C) Copyright 2026 - PolanieOnLine                  *
 ***************************************************************************/
package games.stendhal.server.maps.deathmatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Level;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.RebornSystem;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class DeathmatchHelmetRecoveryTest {
	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
	}

	private Player player(final String name, final int level, final int wins,
			final int reborns) {
		final Player player = PlayerTestHelper.createPlayer(name);
		// Simulate an old save before the login migration, not a new character.
		if (player.has(DeathmatchHelmetRecovery.MIGRATION_MARKER)) {
			player.remove(DeathmatchHelmetRecovery.MIGRATION_MARKER);
		}
		player.setLevel(level);
		player.setQuest("deathmatch_wins", Integer.toString(wins));
		player.put(RebornSystem.ATTR_REBORNS, reborns);
		return player;
	}

	private Item addHelmet(final Player player, final String slot, final int defense) {
		final Item helmet = SingletonRepository.getEntityManager().getItem("zdobyczny hełm");
		helmet.setBoundTo(player.getName());
		helmet.put("def", defense);
		player.getSlot(slot).add(helmet);
		return helmet;
	}

	@Test
	public void standardPlayerReceivesWinsCappedByCurrentLevel() {
		final Player player = player("helmet-standard", 100, 80, 0);
		final Item helmet = addHelmet(player, "bag", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(25, helmet.getInt("def"));
		assertTrue(player.has(DeathmatchHelmetRecovery.MIGRATION_MARKER));
		assertEquals(1, player.getInt(DeathmatchHelmetRecovery.MIGRATION_MARKER));
	}

	@Test
	public void rebornPlayerUsesRecordedWinsRatherThanCurrentLevelCap() {
		final Player player = player("helmet-reborn", 25, 100, 2);
		final Item helmet = addHelmet(player, "bag", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(100, helmet.getInt("def"));
	}

	@Test
	public void rebornWithManyWinsAndLowCurrentLevelStillGets124Defense() {
		final Player player = player("helmet-reborn-many-wins", 50, 200, 1);
		final Item helmet = addHelmet(player, "bag", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(124, helmet.getInt("def"));
		assertEquals("124", helmet.get("tooltip_stats", "def"));
	}

	@Test
	public void rebornWithOnlyTwoWinsGetsTwoDefense() {
		final Player player = player("helmet-reborn-two-wins", 50, 2, 1);
		final Item helmet = addHelmet(player, "bag", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(2, helmet.getInt("def"));
	}

	@Test
	public void rebornWith123WinsGets123Defense() {
		final Player player = player("helmet-reborn-123-wins", 10, 123, 1);
		final Item helmet = addHelmet(player, "bag", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(123, helmet.getInt("def"));
	}

	@Test
	public void rebornNeverExceedsHistoricalMaximumOf124Defense() {
		final Player player = player("helmet-max", 1, 900, 3);
		final Item helmet = addHelmet(player, "head", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(5 + Level.maxLevel() / 5, helmet.getInt("def"));
		assertEquals(124, helmet.getInt("def"));
	}

	@Test
	public void aSmallNumberOfWinsCapsTheEstimateBelowLevelMaximum() {
		final Player player = player("helmet-10-wins", Level.maxLevel(), 10, 0);
		final Item helmet = addHelmet(player, "bag", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(10, helmet.getInt("def"));
	}

	@Test
	public void recoveryIsNeverRepeatedEvenIfTheHelmetLaterDropsToOne() {
		final Player player = player("helmet-once", 100, 60, 0);
		final Item helmet = addHelmet(player, "bag", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(25, helmet.getInt("def"));
		helmet.put("def", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(1, helmet.getInt("def"));
	}

	@Test
	public void newHelmetsReceivedAfterFirstLoginAreNotArtificiallyBoosted() {
		final Player player = player("helmet-new", 597, 100, 2);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		final Item helmet = addHelmet(player, "bag", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(1, helmet.getInt("def"));
	}

	@Test
	public void validHelmetDefenseIsPreserved() {
		final Player player = player("helmet-correct", 597, 100, 0);
		final Item helmet = addHelmet(player, "bag", 77);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(77, helmet.getInt("def"));
		assertTrue(player.has(DeathmatchHelmetRecovery.MIGRATION_MARKER));
	}

	@Test
	public void noRecordedWinsAndOnlyOneWinMustNotInventProgress() {
		final Player none = player("helmet-no-wins", 597, 0, 1);
		final Item noneHelmet = addHelmet(none, "bag", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(none);
		assertEquals(1, noneHelmet.getInt("def"));
		final Player once = player("helmet-one-win", 597, 1, 2);
		final Item onceHelmet = addHelmet(once, "bag", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(once);
		assertEquals(1, onceHelmet.getInt("def"));
	}

	@Test
	public void recoversTrophyStoredInBank() {
		final Player player = player("helmet-in-bank", 300, 60, 0);
		final Item helmet = addHelmet(player, "bank", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertEquals(60, helmet.getInt("def"));
	}

	@Test
	public void multipleHelmetsAndHelmetsBoundToSomeoneElseAreLeftAlone() {
		final Player multi = player("helmet-multi", 597, 100, 0);
		final Item first = addHelmet(multi, "bag", 1);
		final Item second = addHelmet(multi, "bank", 1);
		DeathmatchHelmetRecovery.migrateOnLogin(multi);
		assertEquals(1, first.getInt("def"));
		assertEquals(1, second.getInt("def"));

		final Player stranger = player("helmet-foreign", 597, 100, 0);
		final Item foreign = addHelmet(stranger, "bag", 1);
		foreign.setBoundTo("someone-else");
		DeathmatchHelmetRecovery.migrateOnLogin(stranger);
		assertEquals(1, foreign.getInt("def"));
	}

	@Test
	public void alwaysMarksCharacterEvenIfOnlyBankWasEmpty() {
		final Player player = player("helmet-empty", 597, 100, 0);
		DeathmatchHelmetRecovery.migrateOnLogin(player);
		assertTrue(player.has(DeathmatchHelmetRecovery.MIGRATION_MARKER));
		assertFalse(player.getAllEquipped("zdobyczny hełm").size() > 0);
	}
}
