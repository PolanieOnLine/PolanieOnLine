/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityModifiers;
import games.stendhal.server.core.rule.rarity.ItemRarityTransferSnapshot;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class ForgeNewArmsTest {
	@BeforeClass
	public static void setUpWorld() {
		MockStendlRPWorld.get();
	}

	@Test
	public void restoredOrderPreservesLegendaryShieldRarity() {
		final Player player = PlayerTestHelper.createPlayer("old_shield_forge");
		final Item source = SingletonRepository.getEntityManager().getItem(
				"tarcza z mithrilu", ItemCreationContext
						.builder(ItemCreationContext.Source.ADMIN)
						.withRarity(ItemRarity.LEGENDARY)
						.withModifiers(ItemRarityModifiers.builder()
								.defenseMultiplier(1.24)
								.valueMultiplier(2.0).build())
						.withAffixSeed(9123L).build());
		player.setQuest("forge_newarms", "forging;0;"
				+ ItemRarityTransferSnapshot.encode(source));

		final Item result = ForgeNewArms.createTransferredItem(player,
				"tarcza ciemnomithrilowa");

		assertSame(ItemRarity.LEGENDARY, result.getRarity());
		assertEquals(Double.valueOf(1.24), result.getRarityModifier("def"));
		assertEquals(223, result.getInt("def"));
		assertEquals(player.getName(), result.getBoundTo());
	}

	@Test
	public void legacyOrderWithoutSnapshotReturnsCommonShield() {
		final Player player = PlayerTestHelper.createPlayer("legacy_shield_forge");
		player.setQuest("forge_newarms", "forging;0");

		final Item result = ForgeNewArms.createTransferredItem(player,
				"tarcza z mithrilu");

		assertSame(ItemRarity.COMMON, result.getRarity());
		assertEquals(player.getName(), result.getBoundTo());
	}

	@Test
	public void damagedSnapshotFallsBackToCommonShield() {
		final Player player = PlayerTestHelper.createPlayer("damaged_shield_forge");
		player.setQuest("forge_newarms", "forging;0;not-a-snapshot");

		final Item result = ForgeNewArms.createTransferredItem(player,
				"tarcza z mithrilu");

		assertSame(ItemRarity.COMMON, result.getRarity());
		assertEquals(player.getName(), result.getBoundTo());
	}
}
