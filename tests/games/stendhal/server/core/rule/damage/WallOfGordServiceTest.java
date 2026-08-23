/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class WallOfGordServiceTest {
	@BeforeClass
	public static void generateRPClasses() {
		ItemTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Before
	public void clearCooldowns() {
		WallOfGordService.clearCooldownsForTests();
	}

	@Test
	public void materializesMarkerWithoutChangingDefense() {
		final Item armor = item("armor", 100);

		assertTrue(WallOfGordService.apply(armor, new Random(7L)));
		assertTrue(armor.has(WallOfGordService.ATTRIBUTE));
		assertEquals(100, armor.getInt("def"));
		assertFalse(WallOfGordService.apply(armor, new Random(8L)));
		assertFalse(WallOfGordService.isEligible(item("ring", 3)));
	}

	@Test
	public void onlyQualifyingDirectHitIsReducedByThirtyFivePercent() {
		final Player player = playerWithWall("wall-threshold", 3600);
		final long now = 1000L;

		assertEquals(359, WallOfGordService.reduceDirectCreatureHit(
				player, 359, now));
		assertEquals(234, WallOfGordService.reduceDirectCreatureHit(
				player, 360, now));
	}

	@Test
	public void cooldownBlocksSecondProcUntilEightSeconds() {
		final Player player = playerWithWall("wall-cooldown", 3600);
		final long now = 1000L;

		assertEquals(260, WallOfGordService.reduceDirectCreatureHit(
				player, 400, now));
		assertEquals(900, WallOfGordService.reduceDirectCreatureHit(
				player, 900, now + 7999L));
		assertEquals(585, WallOfGordService.reduceDirectCreatureHit(
				player, 900, now + 8000L));
	}

	@Test
	public void usesCurrentBaseHpAsMaximumHealth() {
		final Player player = playerWithWall("wall-max-hp", 4000);
		final long now = 1000L;

		assertEquals(399, WallOfGordService.reduceDirectCreatureHit(
				player, 399, now));
		assertEquals(260, WallOfGordService.reduceDirectCreatureHit(
				player, 400, now));
	}

	@Test
	public void duplicateWallPiecesDoNotIncreaseReduction() {
		final Player player = playerWithWall("wall-duplicates", 3600);
		final Item shield = item("shield", 20);
		shield.put(WallOfGordService.ATTRIBUTE, 1.0);
		player.getSlot("lhand").add(shield);

		assertEquals(585, WallOfGordService.reduceDirectCreatureHit(
				player, 900, 1000L));
	}

	@Test
	public void playerWithoutWallKeepsIncomingDamage() {
		final Player player = PlayerTestHelper.createPlayer("without-wall");
		player.setBaseHP(3600);
		player.setHP(3600);

		assertEquals(900, WallOfGordService.reduceDirectCreatureHit(
				player, 900, 1000L));
	}

	private Player playerWithWall(final String name, final int maxHp) {
		final Player player = PlayerTestHelper.createPlayer(name);
		player.setBaseHP(maxHp);
		player.setHP(maxHp);
		final Item armor = item("armor", 50);
		armor.put(WallOfGordService.ATTRIBUTE, 1.0);
		player.getSlot("armor").add(armor);
		return player;
	}

	private Item item(final String itemClass, final int defense) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("def", Integer.toString(defense));
		return new Item("wall of the gord test", itemClass, "test", attributes);
	}
}
