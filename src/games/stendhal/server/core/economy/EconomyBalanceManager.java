package games.stendhal.server.core.economy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;

/**
 * Balances global economic activity such as loot drops and merchant pricing.
 */
public final class EconomyBalanceManager {
	private static final EconomyBalanceManager INSTANCE = new EconomyBalanceManager();

	private static final double MERCHANT_MARGIN_MIN = 0.85;
	private static final int HAPPY_HOUR_DURATION = 2;
	private static final ZoneId ECONOMY_ZONE = ZoneId.of("Europe/Warsaw");

	private final AtomicLong totalCurrencySources = new AtomicLong();
	private final AtomicLong totalCurrencySinks = new AtomicLong();

	private EconomyBalanceManager() {
	}

	public static EconomyBalanceManager getInstance() {
		return INSTANCE;
	}

	public int balanceCurrencyDrop(final Creature creature, final String itemName, final int baseQuantity) {
		if (itemName == null) {
			return baseQuantity;
		}
		if (!"money".equalsIgnoreCase(itemName)) {
			return baseQuantity;
		}
		final int level = creature == null ? 1 : Math.max(1, creature.getLevel());
		final double levelModifier = 0.6 + Math.min(2.5, level / 25.0);
		final double economyModifier = computeDropEconomyModifier();
		final int adjusted = (int) Math.max(1, Math.round(baseQuantity * levelModifier * economyModifier));
		totalCurrencySources.addAndGet(adjusted);
		return adjusted;
	}

	public PriceAdjustment adjustMerchantPrice(final String merchantName, final Player player, final String itemName,
		final int basePrice, final CommerceType type) {
		if (basePrice <= 0) {
			return new PriceAdjustment(basePrice, basePrice, 1.0, java.util.Collections.<String>emptyList());
		}
		double modifier = 1.0;
		final List<String> reasons = new ArrayList<String>();

		final double merchantModifier = computeMerchantSpecificModifier(merchantName);
		modifier *= merchantModifier;
		if (merchantModifier > 1.05) {
			reasons.add("marża kupca");
		} else if (merchantModifier < 0.95) {
			reasons.add("promocja kupca");
		}

		final double reputationModifier = computePlayerReputationModifier(player, type);
		modifier *= reputationModifier;
		if (reputationModifier > 1.01) {
			reasons.add("zła reputacja");
		} else if (reputationModifier < 0.99) {
			reasons.add("dobra reputacja");
		}

		final double economyModifier = computeCommerceEconomyModifier(type);
		modifier *= economyModifier;
		if (economyModifier > 1.01) {
			reasons.add("kontrola inflacji");
		} else if (economyModifier < 0.99) {
			reasons.add("stymulacja rynku");
		}

		final double timeModifier = computeTimeModifier(merchantName, type);
		modifier *= timeModifier;
		if (timeModifier < 0.999) {
			reasons.add("happy hour");
		} else if (timeModifier > 1.001) {
			reasons.add("popyt okresowy");
		}

		final int adjusted = (int) Math.max(1, Math.round(basePrice * modifier));
		return new PriceAdjustment(basePrice, adjusted, modifier, reasons);
	}

	public void recordMerchantTransaction(final String merchantName, final int amount, final CommerceType type) {
		if (amount <= 0) {
			return;
		}
		if (type == CommerceType.NPC_SELLING) {
			totalCurrencySinks.addAndGet(amount);
		} else {
			totalCurrencySources.addAndGet(amount);
		}
	}

	private double computeMerchantSpecificModifier(final String merchantName) {
		if (merchantName == null || merchantName.isEmpty()) {
			return 1.0;
		}
		final int hash = Math.abs(merchantName.hashCode());
		final double normalized = (hash % 31) / 100.0;
		return MERCHANT_MARGIN_MIN + normalized;
	}

	private double computePlayerReputationModifier(final Player player, final CommerceType type) {
		if (player == null) {
			return 1.0;
		}
		final double karma = player.getKarma();
		if (type == CommerceType.NPC_SELLING) {
			if (karma > 150.0) {
				return 0.9;
			}
			if (karma > 50.0) {
				return 0.95;
			}
			if (karma < -100.0) {
				return 1.15;
			}
			if (karma < -25.0) {
				return 1.05;
			}
		} else {
			if (karma > 150.0) {
				return 1.15;
			}
			if (karma > 50.0) {
				return 1.05;
			}
			if (karma < -100.0) {
				return 0.85;
			}
			if (karma < -25.0) {
				return 0.95;
			}
		}
		return 1.0;
	}

	private double computeDropEconomyModifier() {
		final double ratio = getCurrencyRatio();
		final double adjustment = (ratio - 1.0) * 0.25;
		return clamp(1.0 - adjustment, 0.55, 1.45);
	}

	private double computeCommerceEconomyModifier(final CommerceType type) {
		final double ratio = getCurrencyRatio();
		final double adjustment = (ratio - 1.0) * 0.35;
		if (type == CommerceType.NPC_SELLING) {
			return clamp(1.0 + adjustment, 0.7, 1.6);
		}
		return clamp(1.0 - adjustment, 0.6, 1.4);
	}

	private double computeTimeModifier(final String merchantName, final CommerceType type) {
		if (merchantName == null) {
			return 1.0;
		}
		final int hash = Math.abs(merchantName.hashCode());
		final int happyHourStart = hash % 24;
		final LocalDateTime now = LocalDateTime.now(ECONOMY_ZONE);
		final int hour = now.getHour();
		if (isWithinWindow(hour, happyHourStart, HAPPY_HOUR_DURATION)) {
			return type == CommerceType.NPC_SELLING ? 0.75 : 1.25;
		}
		if (hour >= 22 || hour < 6) {
			return type == CommerceType.NPC_SELLING ? 0.9 : 1.1;
		}
		if (hour >= 6 && hour < 9) {
			return type == CommerceType.NPC_SELLING ? 0.95 : 1.05;
		}
		if (hour >= 17 && hour < 20) {
			return type == CommerceType.NPC_SELLING ? 1.1 : 0.9;
		}
		return 1.0;
	}

	private boolean isWithinWindow(final int value, final int start, final int duration) {
		int normalizedStart = start;
		while (normalizedStart < 0) {
			normalizedStart += 24;
		}
		final int end = (normalizedStart + duration) % 24;
		if (duration >= 24) {
			return true;
		}
		if (normalizedStart <= end) {
			return value >= normalizedStart && value < end;
		}
		return value >= normalizedStart || value < end;
	}

	private double getCurrencyRatio() {
		final double sources = Math.max(1.0, totalCurrencySources.doubleValue());
		final double sinks = Math.max(1.0, totalCurrencySinks.doubleValue());
		return sources / sinks;
	}

	private double clamp(final double value, final double min, final double max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}
}
