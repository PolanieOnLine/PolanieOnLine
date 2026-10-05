/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.engine.transformer;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.seasonal.MineTownLanternSpawns;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class MineTownLanternRestoreTest {
	@BeforeClass
	public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@After
	public void cleanup() {
		PlayerTestHelper.removeAllPlayers();
	}

	private Item ordinary() {
		return SingletonRepository.getEntityManager().getItem("latarenka");
	}

	private StackableItem festival(final String marker, final int quantity) {
		final StackableItem item = (StackableItem) SingletonRepository.getEntityManager()
				.getItem(MineTownLanternSpawns.LANTERN_NAME);
		item.setItemData(marker);
		item.setQuantity(quantity);
		return item;
	}

	private Item restore(final Item source) throws IOException {
		source.setID(new RPObject.ID(301, "lantern_restore_test"));
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		source.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject copy = new RPObject();
		copy.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
		return new ItemTransformer().transform(copy);
	}

	@Test
	public void markedLegacyLanternMigratesAndKeepsInstanceData() throws IOException {
		for (final boolean persistent : new boolean[] { false, true }) {
			final Item old = ordinary();
			old.setItemData(MineTownLanternSpawns.itemData());
			old.setDescription("Oto zguba Wolrada.");
			old.setBoundTo("collector");
			if (persistent) { old.put("persistent", 1); }
			final Item restored = restore(old);
			assertTrue(restored instanceof StackableItem);
			assertEquals(MineTownLanternSpawns.LANTERN_NAME, restored.getName());
			assertEquals(1, restored.getQuantity());
			assertEquals(Integer.MAX_VALUE, ((StackableItem) restored).getCapacity());
			assertEquals(old.getItemData(), restored.getItemData());
			assertEquals(old.getDescription(), restored.getDescription());
			assertEquals(old.getBoundTo(), restored.getBoundTo());
			assertEquals(old.getItemSubclass(), restored.getItemSubclass());
			assertEquals(restored.getName(), restore(restored).getName());
		}
	}

	@Test
	public void ordinaryAndUnrelatedMarkedLanternsRemainNonStackable() throws IOException {
		assertFalse(ordinary() instanceof StackableItem);
		assertFalse(restore(ordinary()) instanceof StackableItem);
		final Item unrelated = ordinary();
		unrelated.setItemData("another_quest");
		assertFalse(restore(unrelated) instanceof StackableItem);
	}

	@Test
	public void restoredStackPreservesQuantityAndSplittingPreservesEdition() throws IOException {
		final StackableItem original = festival(MineTownLanternSpawns.itemData(), 8);
		original.setDescription("Oto zguba Wolrada.");
		final StackableItem restored = (StackableItem) restore(original);
		final Player player = PlayerTestHelper.createPlayer("lantern_split_collector");
		assertTrue(player.equipToInventoryOnly(restored));
		final StackableItem split = restored.splitOff(3);
		assertEquals(5, restored.getQuantity());
		assertEquals(3, split.getQuantity());
		assertEquals(original.getItemData(), split.getItemData());
		assertEquals(original.getDescription(), split.getDescription());
		assertTrue(restored.isStackable(split));
		assertFalse(restored.isStackable(festival("minetown_lantern_old_edition", 1)));
		final StackableItem unmarked = (StackableItem) SingletonRepository.getEntityManager()
				.getItem(MineTownLanternSpawns.LANTERN_NAME);
		assertFalse(restored.isStackable(unmarked));
	}

	@Test
	public void pickedLanternsMergeAndQuestCountsUnitsNotSlots() {
		final Player player = PlayerTestHelper.createPlayer("lantern_stack_collector");
		for (int i = 0; i < 5; i++) {
			assertTrue(player.equipToInventoryOnly(festival(MineTownLanternSpawns.itemData(), 1)));
		}
		assertEquals(1, player.getAllEquipped(MineTownLanternSpawns.LANTERN_NAME).size());
		assertTrue(player.isEquippedWithItemdata(MineTownLanternSpawns.LANTERN_NAME,
				MineTownLanternSpawns.itemData(), 5));
		assertFalse(player.isEquippedWithItemdata(MineTownLanternSpawns.LANTERN_NAME,
				MineTownLanternSpawns.itemData(), 6));
	}
}
