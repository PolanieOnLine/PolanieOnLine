package games.stendhal.server.entity.item.money;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import games.stendhal.common.constants.CurrencyReform;

public class MoneyUtilsPaymentTest {
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
	}
}
