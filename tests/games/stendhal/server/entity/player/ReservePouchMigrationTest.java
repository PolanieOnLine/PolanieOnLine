package games.stendhal.server.entity.player;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.slot.PlayerSlot;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import games.stendhal.server.core.engine.transformer.PlayerTransformer;
import marauroa.common.game.RPObject;

public class ReservePouchMigrationTest {
	@BeforeClass public static void setup() { MockStendlRPWorld.get(); }

	private Player player() {
		final Player player = PlayerTestHelper.createPlayer("migration-test");
		player.addSlot(new PlayerSlot("pouch_set"));
		return player;
	}
	private StackableItem money(final int quantity) {
		final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
		money.setQuantity(quantity);
		return money;
	}
	@Test public void mergesAndNeverDuplicatesOnRepeatedLogin() {
		final Player player = player();
		final StackableItem current = money(120);
		player.getSlot("pouch").add(current);
		player.getSlot("pouch_set").add(money(30));
		assertTrue(ReservePouchMigration.migrate(player));
		assertEquals(150, current.getQuantity());
		assertFalse(player.hasSlot("pouch_set"));
		assertTrue(ReservePouchMigration.migrate(player));
		assertEquals(150, current.getQuantity());
	}
	@Test public void movesMoneyToEmptyPrimaryPouchAndEnablesFeature() {
		final Player player = player();
		final StackableItem stored = money(75);
		player.getSlot("pouch_set").add(stored);
		assertTrue(ReservePouchMigration.migrate(player));
		assertSame(stored, player.getSlot("pouch").iterator().next());
		assertNotNull(player.getFeature("pouch"));
	}
	@Test public void removesEmptyLegacySlot() {
		final Player player = player();
		assertTrue(ReservePouchMigration.migrate(player));
		assertFalse(player.hasSlot("pouch_set"));
	}
	@Test public void changingEquipmentSetsNeverMovesThePrimaryPouch() {
		final Player player = player();
		final StackableItem primary = money(40);
		final StackableItem reserve = money(50);
		player.getSlot("pouch").add(primary);
		player.getSlot("pouch_set").add(reserve);
		new games.stendhal.server.actions.equip.SetChangeAction()
				.onAction(player, new marauroa.common.game.RPAction());
		assertSame(primary, player.getSlot("pouch").iterator().next());
		assertSame(reserve, player.getSlot("pouch_set").iterator().next());
		assertFalse(primary.canBeEquippedIn("pouch_set"));
	}
	@Test public void overflowPreservesBothStacksAndForbidsNewDeposits() {
		final Player player = player();
		final StackableItem primary = money(Integer.MAX_VALUE);
		player.getSlot("pouch").add(primary);
		player.getSlot("pouch_set").add(money(10));
		assertFalse(ReservePouchMigration.migrate(player));
		assertEquals(Integer.MAX_VALUE, primary.getQuantity());
		assertEquals(10, ((StackableItem) player.getSlot("pouch_set").iterator().next()).getQuantity());
		assertFalse(((PlayerSlot) player.getSlot("pouch_set")).isReachableForThrowingThingsIntoBy(player));
	}
	@Test public void unexpectedItemIsPreservedForManualRecovery() {
		final Player player = player();
		player.getSlot("pouch_set").add(SingletonRepository.getEntityManager().getItem("miecz"));
		assertFalse(ReservePouchMigration.migrate(player));
		assertEquals(1, player.getSlot("pouch_set").size());
	}
	@Test public void loginLoadsLegacySlotBeforeMigratingAndDoesNotRecreateIt() {
		final Player original = player();
		original.getSlot("pouch").add(money(25));
		original.getSlot("pouch_set").add(money(30));
		final Player restored = (Player) new PlayerTransformer().transform(new RPObject(original));
		assertFalse(restored.hasSlot("pouch_set"));
		assertEquals(55, ((StackableItem) restored.getSlot("pouch").iterator().next()).getQuantity());
		final Player restoredAgain = (Player) new PlayerTransformer().transform(new RPObject(restored));
		assertFalse(restoredAgain.hasSlot("pouch_set"));
		assertEquals(55, ((StackableItem) restoredAgain.getSlot("pouch").iterator().next()).getQuantity());
		assertFalse(PlayerTestHelper.createPlayer("new-player").hasSlot("pouch_set"));
	}
}
