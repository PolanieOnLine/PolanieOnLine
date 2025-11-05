package games.stendhal.server.entity.item.money;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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

	private MoneyUtils() {}

	public static int toCopper(String coinName, int quantity) {
		if (coinName == null) return 0;
		switch (coinName.toLowerCase()) {
			case "miedziak": return quantity * COPPER_VALUE;
			case "talar":	return quantity * SILVER_VALUE;
			case "dukat":	return quantity * GOLD_VALUE;
			default: return 0;
		}
	}

	public static Map<String, Integer> fromCopper(int totalCopper) {
		Map<String, Integer> result = new HashMap<>();
		int dukaty = totalCopper / GOLD_VALUE;
		totalCopper %= GOLD_VALUE;
		int talary = totalCopper / SILVER_VALUE;
		int miedziaki = totalCopper % SILVER_VALUE;

		result.put("dukat", dukaty);
		result.put("talar", talary);
		result.put("miedziak", miedziaki);
		return result;
	}

	/**
	 * Zwraca łączną wartość wszystkich monet gracza w miedziakach.
	 */
	public static int getTotalMoneyInCopper(final Player player) {
		int totalCopper = 0;
		String[] coins = {"miedziak", "talar", "dukat"};

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
	 * Robi to w sposób deterministyczny:
	 *  - oblicza łączną wartość posiadanych monet,
	 *  - odejmuje koszt,
	 *  - usuwa stare monety i tworzy nowe z reszty.
	 */
	public static boolean removeMoney(final Player player, int amountToRemove) {
		int total = getTotalMoneyInCopper(player);
		if (total < amountToRemove) {
			return false;
		}

		int remaining = total - amountToRemove;

		String[] coins = {"miedziak", "talar", "dukat"};
		for (String coinName : coins) {
			List<Item> items = new ArrayList<>(player.getAllEquipped(coinName));
			for (Item item : items) {
				if (item instanceof StackableItem) {
					StackableItem s = (StackableItem) item;
					if (s.getQuantity() > 0) {
						player.drop(coinName, s.getQuantity());
					}
				}
			}
		}

		Map<String, Integer> newCoins = fromCopper(remaining);

		for (Map.Entry<String, Integer> entry : newCoins.entrySet()) {
			String coinName = entry.getKey();
			int qty = entry.getValue();
			if (qty <= 0) continue;

			try {
				StackableItem coin = (StackableItem)
						SingletonRepository.getEntityManager().getItem(coinName);
				coin.setQuantity(qty);
				player.equipOrPutOnGround(coin);
			} catch (Exception e) {
				logger.error("Błąd przy dodawaniu monet: " + coinName + " x" + qty);
			}
		}

		return true;
	}

	public static String formatPrice(int totalCopper) {
		Map<String, Integer> map = fromCopper(totalCopper);
		int dukaty = map.get("dukat");
		int talary = map.get("talar");
		int miedziaki = map.get("miedziak");

		List<String> parts = new ArrayList<>();

		if (dukaty > 0) parts.add(dukaty + " " + Grammar.polishQuantity("dukat", dukaty));
		if (talary > 0) parts.add(talary + " " + Grammar.polishQuantity("talar", talary));
		if (miedziaki > 0 || parts.isEmpty()) parts.add(miedziaki + " " + Grammar.polishQuantity("miedziak", miedziaki));

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
}
