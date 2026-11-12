/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.maps.tarnow.blacksmith.forge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityModifiers;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.behaviour.impl.MultiProducerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.UpdateConverter;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class ForgeItemsTest {
	private static final String QUEST_SLOT = "przemyslaw_newarms";
	private static final String SOURCE = "spodnie z mithrilu";
	private static final String PRODUCT = "spodnie z hartowanego mithrilu";

	@BeforeClass
	public static void setUpWorld() {
		MockStendlRPWorld.get();
	}

	@Test
	public void productsAcceptCurrentLegacyAndTranslatedNames() {
		final String[][] names = {
				{"hełm z hartowanego mithrilu", "hełm ciemnomithrilowy", "hardened mithril helmet"},
				{"tarcza z hartowanego mithrilu", "tarcza ciemnomithrilowa", "hardened mithril shield"},
				{"buty z hartowanego mithrilu", "buty ciemnomithrilowe", "hardened mithril boots"}
		};
		final MultiProducerBehaviour behaviour = ForgeItems.getBehaviour();
		for (final String[] item : names) {
			for (final String name : item) {
				final ItemParserResult result = behaviour.parse(ConversationParser.parse("forge " + name));
				assertTrue(name, result.wasFound());
				assertEquals(item[0], result.getChosenItemName());
			}
		}
	}

	@Test
	public void hardenedMithrilProductInheritsLegendaryRollAndAffixes() {
		final Player player = PlayerTestHelper.createPlayer("rarity_forge_player");
		final Item source = SingletonRepository.getEntityManager().getItem(SOURCE,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withRarity(ItemRarity.LEGENDARY)
						.withModifiers(ItemRarityModifiers.builder()
								.defenseMultiplier(1.25)
								.valueMultiplier(2.0).build())
						.withAffixSeed(123456L).build());
		source.setUpgradeLevel(1);
		assertTrue(player.equipToInventoryOnly(source));
		assertTrue(PlayerTestHelper.equipWithStackableItem(player,
				"klejnot ciemnolitu", 3));
		assertTrue(PlayerTestHelper.equipWithStackableItem(player,
				"sztabka platyny", 7));
		assertTrue(PlayerTestHelper.equipWithStackableItem(player,
				"bryłka mithrilu", 15));

		final MultiProducerBehaviour behaviour = ForgeItems.getBehaviour();
		assertTrue(behaviour.transactAgreedDeal(
				new ItemParserResult(true, PRODUCT, 1, null),
				new EventRaiser(null), player));
		final String[] order = player.getQuest(QUEST_SLOT).split(";");
		assertEquals(4, order.length);
		assertTrue(order[3].startsWith("rarity-v1:"));

		player.setQuest(QUEST_SLOT,
				order[0] + ";spodnie ciemnomithrilowe;0;" + order[3]);
		UpdateConverter.updateQuests(player);
		assertEquals(order[0] + ";" + PRODUCT + ";0;" + order[3], player.getQuest(QUEST_SLOT));
		behaviour.giveProduct(new EventRaiser(null), player);

		final Item product = player.getFirstEquipped(PRODUCT);
		assertNotNull(product);
		assertSame(ItemRarity.LEGENDARY, product.getRarity());
		assertEquals(Double.valueOf(1.25), product.getRarityModifier("def"));
		assertEquals(45, product.getInt("def"));
		assertEquals(0, product.getUpgradeLevel());
		assertEquals(ItemAffixState.getValues(source),
				ItemAffixState.getValues(product));
		assertEquals(ItemAffixState.getSeed(source), ItemAffixState.getSeed(product));
		assertEquals(player.getName(), product.getBoundTo());
	}

	@Test
	public void legacyThreeFieldOrderStillReturnsDeterministicCommonProduct() {
		final Player player = PlayerTestHelper.createPlayer("legacy_forge_player");
		player.setQuest(QUEST_SLOT, "1;spodnie ciemnomithrilowe;0");
		UpdateConverter.updateQuests(player);
		assertEquals("1;" + PRODUCT + ";0", player.getQuest(QUEST_SLOT));

		ForgeItems.getBehaviour().giveProduct(new EventRaiser(null), player);

		final Item product = player.getFirstEquipped(PRODUCT);
		assertNotNull(product);
		assertSame(ItemRarity.COMMON, product.getRarity());
		assertEquals(36, product.getInt("def"));
	}
}
