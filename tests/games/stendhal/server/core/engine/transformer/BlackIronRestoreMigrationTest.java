/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.engine.transformer;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.PlayerLootedItemsHandler;
import games.stendhal.server.entity.player.UpdateConverter;
import games.stendhal.server.entity.slot.Banks;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class BlackIronRestoreMigrationTest {
	private static final String[] OLD = { "ruda cieni", "sztabka cieni" };
	private static final String[] NEW = { "ruda czarnego żelaza", "sztabka czarnego żelaza" };
	private static final String[] OLD_DESCRIPTIONS = {
		"Oto ruda cieni. Niczym nie połyskuje.",
		"Oto sztabka cieni. Posiada specyficzne właściwości, potrafi powstrzymywać światło."
	};

	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	private Item definition(final int material) {
		return SingletonRepository.getEntityManager().getItem(NEW[material], ItemCreationContext.restore());
	}

	private StackableItem oldStack(final int material, final int quantity, final boolean persistent) {
		final StackableItem item = (StackableItem) definition(material);
		item.put("name", OLD[material]);
		item.setDescription(OLD_DESCRIPTIONS[material]);
		item.put(Item.DEFINITION_DESCRIPTION, OLD_DESCRIPTIONS[material]);
		item.setQuantity(quantity);
		item.setPersistent(persistent);
		return item;
	}

	private Item restore(final Item source) throws Exception {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		source.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject copy = new RPObject();
		copy.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
		return new ItemTransformer().transform(copy);
	}

	@Test
	public void oldNamesAndUnderscoresMapToBlackIronWithoutChangingRecordedAmounts() {
		for (int material = 0; material < OLD.length; material++) {
			assertEquals(NEW[material], UpdateConverter.updateItemName(OLD[material]));
			assertEquals(NEW[material], UpdateConverter.updateItemName(OLD[material].replace(' ', '_')));
			assertEquals(NEW[material] + "=12", UpdateConverter.updateItemName(OLD[material] + "=12"));
			assertEquals(NEW[material], UpdateConverter.updateItemName(NEW[material]));
		}
		assertEquals("zbroja cieni", UpdateConverter.updateItemName("zbroja cieni"));
	}

	@Test
	public void savedStacksKeepTheirQuantityIdentityAndMetadataAcrossRepeatedLogins() throws Exception {
		for (int material = 0; material < OLD.length; material++) {
			for (boolean persistent : new boolean[] { false, true }) {
				Item saved = oldStack(material, 37, persistent);
				final ID id = new ID(850 + material, "black-iron-restore");
				saved.setID(id);
				saved.setBoundTo("black-iron-test");
				saved.setItemData("legacy-material-note");
				saved.put("logid", 12345);
				saved.put("improve", 3);
				saved.put("uses", 7);
				for (int login = 0; login < 3; login++) {
					saved = restore(saved);
					assertNotNull(saved);
					assertEquals(NEW[material], saved.getName());
					assertEquals(definition(material).getDescription(), saved.getDescription());
					assertEquals(definition(material).getDescription(), saved.get(Item.DEFINITION_DESCRIPTION));
					assertEquals(37, ((StackableItem) saved).getQuantity());
					assertEquals(id, saved.getID());
					assertEquals("black-iron-test", saved.get("bound"));
					assertEquals("legacy-material-note", saved.getItemData());
					assertEquals(12345, saved.getInt("logid"));
					assertEquals(3, saved.getUpgradeLevel());
					assertEquals(7, saved.getInt("uses"));
					assertEquals(definition(material).get("subclass"), saved.get("subclass"));
				}
			}
		}
	}

	@Test
	public void legacyDescriptionsAlsoRefreshWhenPersonalMetadataHasNoDefinitionMarker() throws Exception {
		for (int material = 0; material < OLD.length; material++) {
			final Item saved = oldStack(material, 5, true);
			saved.setID(new ID(860 + material, "black-iron-restore"));
			saved.remove(Item.DEFINITION_DESCRIPTION);
			saved.setItemData("keep-this-data");
			final Item restored = restore(saved);
			assertEquals(definition(material).getDescription(), restored.getDescription());
			assertEquals("keep-this-data", restored.getItemData());
		}
	}

	@Test
	public void everyBankReserveHandsAndNestedContainersReceiveTheRenamedStacks() {
		for (int material = 0; material < OLD.length; material++) {
			assertInventoryMigration(material);
		}
	}

	private void assertInventoryMigration(final int material) {
		final Player player = PlayerTestHelper.createPlayer("black-iron-inventory");
		final List<String> slots = new ArrayList<String>(Arrays.asList("bag", "magicbag", "lhand", "rhand", "lhand_set", "rhand_set"));
		for (final Banks bank : Banks.values()) slots.add(bank.getSlotName());
		for (final String slot : slots) {
			player.getSlot(slot).add(oldStack(material, 11, material == 1));
		}
		final Item container = SingletonRepository.getEntityManager().getItem("rzemyk", ItemCreationContext.restore());
		container.setPersistent(true);
		container.getSlot("content").add(oldStack(1, 19, true));
		player.getSlot("bag").add(container);
		for (int login = 0; login < 2; login++) {
			new PlayerTransformer().loadItemsIntoSlots(player);
			for (final String slot : slots) {
				int ore = 0;
				int bars = 0;
				for (final RPObject object : player.getSlot(slot)) {
					assertFalse(OLD[0].equals(object.get("name")) || OLD[1].equals(object.get("name")));
					if (NEW[0].equals(object.get("name"))) ore += object.getInt("quantity");
					if (NEW[1].equals(object.get("name"))) bars += object.getInt("quantity");
					if ("rzemyk".equals(object.get("name"))) {
						assertEquals(1, object.getSlot("content").size());
						assertEquals(NEW[1], object.getSlot("content").getFirst().get("name"));
						assertEquals(19, object.getSlot("content").getFirst().getInt("quantity"));
					}
				}
				assertEquals(slot, material == 0 ? 11 : 0, ore);
				assertEquals(slot, material == 1 ? 11 : 0, bars);
			}
		}
	}

	@Test
	public void activeDeliveryTasksAndSmithOrdersKeepTheirQuantitiesTimesAndProgress() {
		final Player player = PlayerTestHelper.createPlayer("black-iron-quests");
		player.setQuest("daily_museum_gdansk_quest", "ruda cieni=8;1700000000000;3");
		player.setQuest("zygfryd_cast_shadow", "4;sztabka cieni;1700000000001");
		player.setQuest("gornictwo", "dig_shadow");
		for (int login = 0; login < 3; login++) {
			UpdateConverter.updateQuests(player);
			assertEquals("ruda czarnego żelaza=8;1700000000000;3", player.getQuest("daily_museum_gdansk_quest"));
			assertEquals("4;sztabka czarnego żelaza;1700000000001", player.getQuest("zygfryd_cast_shadow"));
			assertEquals("dig_shadow", player.getQuest("gornictwo"));
		}
	}

	@Test
	public void productionMiningAndTradeCountersAreMergedAndNotReset() {
		final RPObject saved = new RPObject(PlayerTestHelper.createPlayer("black-iron-counters"));
		final String map = PlayerLootedItemsHandler.LOOTED_ITEMS;
		for (final String prefix : new String[] { "", "produced.", "mined.", "obtained.", "bought.", "sold.", "improved." }) {
			saved.put(map, prefix + OLD[0], 13);
			saved.put(map, prefix + NEW[0], 2);
			saved.put(map, prefix + OLD[1], 7);
		}
		saved.put(map, "produced.sztabka żelaza", 42);
		for (int login = 0; login < 3; login++) {
			UpdateConverter.updatePlayerRPObject(saved);
			for (final String prefix : new String[] { "", "produced.", "mined.", "obtained.", "bought.", "sold.", "improved." }) {
				assertFalse(saved.has(map, prefix + OLD[0]));
				assertFalse(saved.has(map, prefix + OLD[1]));
				assertEquals(15, saved.getInt(map, prefix + NEW[0]));
				assertEquals(7, saved.getInt(map, prefix + NEW[1]));
			}
			assertEquals(42, saved.getInt(map, "produced.sztabka żelaza"));
		}
		final Player loaded = new Player(saved);
		assertEquals(15, loaded.getQuantityOfMinedItems(NEW[0]));
		assertEquals(7, loaded.getQuantityOfProducedItems(NEW[1]));
		assertEquals(42, loaded.getQuantityOfProducedItems("sztabka żelaza"));
	}
}
