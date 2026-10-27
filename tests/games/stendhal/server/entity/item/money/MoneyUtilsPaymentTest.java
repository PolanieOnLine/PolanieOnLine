package games.stendhal.server.entity.item.money;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.CurrencyReform;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import marauroa.server.game.db.DatabaseFactory;
import utilities.PlayerTestHelper;

public class MoneyUtilsPaymentTest {
	@BeforeClass
	public static void initialize() throws Exception {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
		new DatabaseFactory().initializeDatabase();
	}

	@Test
	public void singularPaymentUsesTheAccusativeButExistingPricesRemainUnchanged() {
		if (CurrencyReform.useReformedCurrency()) {
			assertEquals("1 miedziaka", MoneyUtils.formatPayment(1));
			assertEquals("1 talara", MoneyUtils.formatPayment(100));
			assertEquals("1 dukata", MoneyUtils.formatPayment(10000));
			assertEquals("1 dukata, 1 talara i 1 miedziaka", MoneyUtils.formatPayment(10101));
			assertEquals("1 dukat, 1 talar i 1 miedziak", MoneyUtils.formatPrice(10101));
		} else {
			assertEquals("1 monetę", MoneyUtils.formatPayment(1));
			assertEquals("1 moneta", MoneyUtils.formatPrice(1));
		}
	}

	@Test
	public void pluralCoinEndingsHandleTeenNumbersAndNumbersEndingInTwo() {
		if (CurrencyReform.useReformedCurrency()) {
			assertEquals("2 miedziaki", MoneyUtils.formatPayment(2));
			assertEquals("5 miedziaków", MoneyUtils.formatPayment(5));
			assertEquals("12 miedziaków", MoneyUtils.formatPayment(12));
			assertEquals("22 miedziaki", MoneyUtils.formatPayment(22));
			assertEquals("11 dukatów, 12 talarów i 14 miedziaków", MoneyUtils.formatPayment(111214));
		} else {
			assertEquals("2 monety", MoneyUtils.formatPayment(2));
			assertEquals("5 monet", MoneyUtils.formatPayment(5));
			assertEquals("12 monet", MoneyUtils.formatPayment(12));
			assertEquals("22 monety", MoneyUtils.formatPayment(22));
		}

		final Player player = PlayerTestHelper.createPlayer("large_wallet");
		final String coinName = CurrencyReform.useReformedCurrency() ? "miedziak" : "money";
		for (int i = 0; i < 2; i++) {
			final StackableItem coins = (StackableItem) SingletonRepository.getEntityManager().getItem(coinName);
			coins.setQuantity(1500000000);
			player.getSlot("bag").add(coins);
		}
		assertEquals(3000000000L, MoneyUtils.toCopper("dukat", 300000));
		assertEquals(3000000000L, MoneyUtils.getTotalMoneyInCopper(player));
		assertTrue(MoneyUtils.hasEnoughMoney(player, 1));
		assertTrue(MoneyUtils.removeMoney(player, 10));
		assertEquals(2999999990L, MoneyUtils.getTotalMoneyInCopper(player));
		assertFalse(MoneyUtils.removeMoney(player, -1));
		assertFalse(MoneyUtils.removeMoney(player, 3000000000L));
		assertEquals(2999999990L, MoneyUtils.getTotalMoneyInCopper(player));
		MoneyUtils.giveMoney(player, 1000000000);
		assertEquals(3999999990L, MoneyUtils.getTotalMoneyInCopper(player));
		assertTrue(MoneyUtils.removeMoney(player, 3999999990L));
		assertEquals(0L, MoneyUtils.getTotalMoneyInCopper(player));
		final StackableItem gold = (StackableItem) SingletonRepository.getEntityManager().getItem("dukat");
		gold.setQuantity(300001);
		player.getSlot("bag").add(gold);
		assertTrue(MoneyUtils.removeMoney(player, 13));
		assertEquals(3000009987L, MoneyUtils.getTotalMoneyInCopper(player));
	}
}
