package games.stendhal.server.core.economy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a calculated price adjustment for a merchant offer.
 */
public class PriceAdjustment {
	private final int basePrice;
	private final int adjustedPrice;
	private final double totalModifier;
	private final List<String> reasons;

	public PriceAdjustment(final int basePrice, final int adjustedPrice, final double totalModifier, final List<String> reasons) {
		this.basePrice = basePrice;
		this.adjustedPrice = adjustedPrice;
		this.totalModifier = totalModifier;
		this.reasons = new ArrayList<String>(reasons);
	}

	public int getBasePrice() {
		return basePrice;
	}

	public int getAdjustedPrice() {
		return adjustedPrice;
	}

	public double getTotalModifier() {
		return totalModifier;
	}

	public List<String> getReasons() {
		return Collections.unmodifiableList(reasons);
	}

	public String describeModifier() {
		if (adjustedPrice == basePrice) {
			return "";
		}
		final double diff = adjustedPrice - basePrice;
		final double percent = basePrice == 0 ? 0.0 : (diff / basePrice) * 100.0;
		final StringBuilder builder = new StringBuilder();
		if (percent > 0) {
			builder.append("+" + Math.round(percent) + "%");
		} else {
			builder.append(Math.round(percent) + "%");
		}
		if (!reasons.isEmpty()) {
			builder.append(" (");
			for (int i = 0; i < reasons.size(); i++) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append(reasons.get(i));
			}
			builder.append(")");
		}
		return builder.toString();
	}
}
