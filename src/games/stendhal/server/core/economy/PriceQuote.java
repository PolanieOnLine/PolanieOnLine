package games.stendhal.server.core.economy;

import java.util.List;

/**
 * Represents a price quote for a transaction including modifier descriptions.
 */
public class PriceQuote {
	private final int amount;
	private final PriceAdjustment adjustment;
	private final CommerceType commerceType;

	public PriceQuote(final int amount, final PriceAdjustment adjustment, final CommerceType commerceType) {
		this.amount = amount;
		this.adjustment = adjustment;
		this.commerceType = commerceType;
	}

	public int getAmount() {
		return amount;
	}

	public PriceAdjustment getAdjustment() {
		return adjustment;
	}

	public CommerceType getCommerceType() {
		return commerceType;
	}

	public int getTotalPrice() {
		return adjustment.getAdjustedPrice() * amount;
	}

	public String describeAdjustment() {
		final String modifierDescription = adjustment.describeModifier();
		if (modifierDescription.isEmpty()) {
			return "";
		}
		return modifierDescription;
	}

	public boolean hasReasons() {
		final java.util.List<String> reasons = adjustment.getReasons();
		return reasons != null && !reasons.isEmpty();
	}
}
