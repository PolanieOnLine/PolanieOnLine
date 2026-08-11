/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.item.upgrade;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Server-calculated money and material requirements for one upgrade. */
public final class ItemUpgradeRequirements {
	private final int fee;
	private final String formattedFee;
	private final int ownedMoney;
	private final Map<String, Integer> materials;
	private final Map<String, Integer> ownedMaterials;

	ItemUpgradeRequirements(final int fee, final String formattedFee,
			final int ownedMoney, final Map<String, Integer> materials,
			final Map<String, Integer> ownedMaterials) {
		this.fee = fee;
		this.formattedFee = formattedFee;
		this.ownedMoney = ownedMoney;
		this.materials = immutableCopy(materials);
		this.ownedMaterials = immutableCopy(ownedMaterials);
	}

	private Map<String, Integer> immutableCopy(
			final Map<String, Integer> source) {
		return Collections.unmodifiableMap(
				new LinkedHashMap<String, Integer>(source));
	}

	public int getFee() {
		return fee;
	}

	public String getFormattedFee() {
		return formattedFee;
	}

	public int getOwnedMoney() {
		return ownedMoney;
	}

	public Map<String, Integer> getMaterials() {
		return materials;
	}

	public Map<String, Integer> getOwnedMaterials() {
		return ownedMaterials;
	}

	public boolean hasEnoughMoney() {
		return ownedMoney >= fee;
	}

	public boolean hasAllMaterials() {
		for (final Map.Entry<String, Integer> entry : materials.entrySet()) {
			final Integer owned = ownedMaterials.get(entry.getKey());
			if (owned == null || owned.intValue() < entry.getValue().intValue()) {
				return false;
			}
		}
		return true;
	}
}
