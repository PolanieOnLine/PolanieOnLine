package games.stendhal.server.entity.item.money;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.CurrencyReform;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;

public final class MoneyUtils {
	private static final Logger logger = Logger.getLogger(MoneyUtils.class);

	public static final int COPPER_VALUE = 1;
	public static final int SILVER_VALUE = 100;
	public static final int GOLD_VALUE = 10000;

	private static final String MONEY = "money";
	private static final String COPPER = "miedziak";
	private static final String SILVER = "talar";
	private static final String GOLD = "dukat";

	private MoneyUtils() {}

	public static long toCopper(String coinName, int quantity) {
		if (coinName == null) return 0;
		switch (coinName.toLowerCase()) {
			case MONEY:
			case COPPER:
				return (long) quantity * COPPER_VALUE;
			case SILVER:
				return (long) quantity * SILVER_VALUE;
			case GOLD:
				return (long) quantity * GOLD_VALUE;
			default:
				return 0;
		}
	}

	public static Map<String, Integer> fromCopper(int totalCopper) {
		Map<String, Integer> result = new LinkedHashMap<>();
		if (CurrencyReform.useReformedCurrency()) {
			int dukaty = totalCopper / GOLD_VALUE;
			totalCopper %= GOLD_VALUE;
			int talary = totalCopper / SILVER_VALUE;
			int miedziaki = totalCopper % SILVER_VALUE;

			result.put(GOLD, dukaty);
			result.put(SILVER, talary);
			result.put(COPPER, miedziaki);
			result.put(MONEY, 0);
		} else {
			result.put(GOLD, 0);
			result.put(SILVER, 0);
			result.put(COPPER, 0);
			result.put(MONEY, totalCopper);
		}
		return result;
	}

	/**
	 * Zwraca łączną wartość wszystkich monet gracza w miedziakach.
	 */
	public static long getTotalMoneyInCopper(final Player player) {
		long totalCopper = 0;
		String[] coins = getKnownCoinNames();

		for (String name : coins) {
			List<Item> items = player.getAllEquipped(name);
			if (items == null) continue;
			for (Item item : items) {
				if (item instanceof StackableItem) {
					StackableItem coin = (StackableItem) item;
					totalCopper += toCopper(name, coin.getQuantity());
				}
			}
		}
		return totalCopper;
	}

	/**
	 * Usuwa określoną kwotę (w miedziakach) z ekwipunku gracza.
	 * Pobiera monety z istniejących stosów, wydając resztę tylko przy rozmienianiu.
	 */
	public static boolean removeMoney(final Player player, long amountToRemove) {
		if (amountToRemove < 0 || getTotalMoneyInCopper(player) < amountToRemove) {
			return false;
		}
		if (amountToRemove == 0) {
			return true;
		}
		long remaining = amountToRemove;

		String[] coins = getKnownCoinNames();
		for (String coinName : coins) {
			List<Item> items = new ArrayList<>(player.getAllEquipped(coinName));
			for (Item item : items) {
				if (item instanceof StackableItem) {
					StackableItem s = (StackableItem) item;
					if (s.getQuantity() > 0) {
						final long unitValue = toCopper(coinName, 1);
						final int quantity = (int) Math.min(s.getQuantity(), 1 + (remaining - 1) / unitValue);
						if (!player.drop(coinName, quantity)) {
							return false;
						}
						remaining -= quantity * unitValue;
						if (remaining <= 0) {
							giveMoney(player, (int) -remaining);
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static String formatPrice(int totalCopper) {
		return formatPrice(totalCopper, false);
	}

	/** Currency names in the case needed after "dam ci" or "zapłacę". */
	public static String formatPayment(int totalCopper) {
		return formatPrice(totalCopper, true);
	}

	private static String quantityForm(String coin, int quantity, boolean payment) {
		if (payment && quantity == 1) {
			switch (coin) {
				case MONEY: return "monetę";
				case GOLD: return "dukata";
				case SILVER: return "talara";
				case COPPER: return "miedziaka";
				default: break;
			}
		}
		return Grammar.polishQuantity(coin, quantity);
	}

	private static String formatPrice(int totalCopper, boolean payment) {
		if (!CurrencyReform.useReformedCurrency()) {
			return totalCopper + " " + quantityForm(MONEY, totalCopper, payment);
		}

		Map<String, Integer> map = fromCopper(totalCopper);
		int dukaty = map.get(GOLD);
		int talary = map.get(SILVER);
		int miedziaki = map.get(COPPER);

		List<String> parts = new ArrayList<>();

		if (dukaty > 0) parts.add(dukaty + " " + quantityForm(GOLD, dukaty, payment));
		if (talary > 0) parts.add(talary + " " + quantityForm(SILVER, talary, payment));
		if (miedziaki > 0 || parts.isEmpty()) parts.add(miedziaki + " " + quantityForm(COPPER, miedziaki, payment));

		if (parts.size() == 1) {
			return parts.get(0);
		} else if (parts.size() == 2) {
			return parts.get(0) + " i " + parts.get(1);
		} else {
			// 3 elementy: 1 dukat, 2 talary i 50 miedziaków
			return parts.get(0) + ", " + parts.get(1) + " i " + parts.get(2);
		}
	}

	/**
	 * Sprawdza, czy gracz ma wystarczająco pieniędzy (w przeliczeniu na miedziaki).
	 */
	public static boolean hasEnoughMoney(final Player player, int priceInCopper) {
		return getTotalMoneyInCopper(player) >= priceInCopper;
	}

	/**
	 * Adds money to player's inventory according to the active currency model.
	 *
	 * @param player
	 *            player who will receive the money
	 * @param totalCopper
	 *            amount expressed in copper units
	 */
	public static void giveMoney(final Player player, int totalCopper) {
		Map<String, Integer> newCoins = fromCopper(totalCopper);

		for (Map.Entry<String, Integer> entry : newCoins.entrySet()) {
			String coinName = entry.getKey();
			int qty = entry.getValue();
			if (qty <= 0) {
				continue;
			}

			try {
				StackableItem coin = (StackableItem)
					SingletonRepository.getEntityManager().getItem(coinName);
				coin.setQuantity(qty);
				player.equipOrPutOnGround(coin);
			} catch (Exception e) {
				logger.error("Błąd przy dodawaniu monet: " + coinName + " x" + qty, e);
			}
		}
	}

	private static String[] getKnownCoinNames() {
		if (CurrencyReform.useReformedCurrency()) {
			return new String[] {COPPER, SILVER, GOLD};
		}
		return new String[] {MONEY, COPPER, SILVER, GOLD};
	}
}
