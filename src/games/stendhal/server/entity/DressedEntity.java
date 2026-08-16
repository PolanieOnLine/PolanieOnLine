/***************************************************************************
 *                    (C) Copyright 2018-2023 - Arianne                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity;

import static games.stendhal.common.Outfits.RECOLORABLE_OUTFIT_PARTS;
import static games.stendhal.common.Outfits.SKIN_LAYERS;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.Testing;
import games.stendhal.server.core.rule.glyph.GlyphEffectService;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import marauroa.common.game.RPObject;
import marauroa.common.game.SyntaxException;

/**
 * Defines an entity whose appearance (outfit) can be changed.
 */
public abstract class DressedEntity extends RPEntity {

	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(DressedEntity.class);

	public DressedEntity() {
		super();
	}

	public DressedEntity(RPObject object) {
		super(object);
	}

	/**
	 * Returns attack skill including the flat bonus from currently equipped
	 * glyphs. The stored/base ATK remains untouched so ATK XP calculations always
	 * operate on the real trained value.
	 */
	@Override
	public int getAtk() {
		return applyGlyphSkillAttackBonus(super.getAtk());
	}

	/**
	 * Returns capped attack skill including the same dynamic glyph bonus used by
	 * {@link #getAtk()}.
	 */
	@Override
	public int getCappedAtk() {
		return applyGlyphSkillAttackBonus(super.getCappedAtk());
	}

	private int applyGlyphSkillAttackBonus(final int baseAttack) {
		final long effectiveAttack = (long) baseAttack
				+ GlyphEffectService.getSkillAttackBonus(this);
		return (int) Math.max(Short.MIN_VALUE,
				Math.min(Short.MAX_VALUE, effectiveAttack));
	}

	/**
	 * Keeps the legacy Strzybog rate reduction from creating a synthetic rate-1
	 * weapon. Authored rate-1 weapons stay untouched, while weapons whose rate
	 * only fell from 2 to 1 because of an equipped glyph are floored at 2.
	 */
	@Override
	public int getAttackRate() {
		final int attackRate = super.getAttackRate();
		final double rateReduction =
				GlyphEffectService.getAttackRateReduction(this);
		if (attackRate != 1 || rateReduction <= 0.0) {
			return attackRate;
		}

		final List<Item> weapons = getWeapons();
		if (weapons.isEmpty()) {
			return attackRate;
		}

		final boolean meleeDistance = isAttacking()
				&& nextTo(getAttackTarget());
		int naturalBest = weapons.get(0).getAttackRate(meleeDistance);
		for (final Item weapon : weapons) {
			naturalBest = Math.min(naturalBest,
					weapon.getAttackRate(meleeDistance));
		}

		return naturalBest <= 1 ? 1 : 2;
	}

	/**
	 * Resolves a real weapon roll while keeping percentage attack bonuses from
	 * equipped glyphs attached to that real roll rather than to the weapon's
	 * stable average damage.
	 *
	 * The inherited implementation first calculates the percentage-adjusted
	 * stable equipment attack and then replaces only the unmodified stable weapon
	 * component with the rolled one. With an attack glyph this leaves part of the
	 * percentage bonus tied to the average weapon damage. When no percentage
	 * attack glyph is equipped, the inherited path is used unchanged.
	 */
	@Override
	public float getItemAtkForAttack(
			final java.util.function.ToDoubleFunction<Item> damageMultiplier) {
		final double glyphAttackBonus =
				GlyphEffectService.getAttackPercentBonusFraction(this);
		if (Double.compare(glyphAttackBonus, 0.0) == 0) {
			return super.getItemAtkForAttack(damageMultiplier);
		}

		final List<Item> weapons = getWeapons();
		double weaponRollDelta = 0.0;
		for (final Item weapon : weapons) {
			double rolledDamage = weapon.rollDamage();
			if (damageMultiplier != null) {
				rolledDamage *= damageMultiplier.applyAsDouble(weapon);
			}
			weaponRollDelta += rolledDamage - weapon.getAverageDamage();
		}

		/* In legacy ATK combat, a wand without spell ammunition contributes only
		 * 10% of its weapon value. The stable and rolled weapon components are both
		 * scaled in the inherited calculation, so their delta must be scaled too. */
		if (!Testing.COMBAT && !weapons.isEmpty()
				&& weapons.get(0).isOfClass("wand")) {
			final Item magic = getMagicSpells();
			if (magic == null || magic.getAttack() == 0) {
				weaponRollDelta *= 0.1;
			}
		}

		final double glyphAttackMultiplier = 1.0 + glyphAttackBonus;
		return Math.max(0.0f,
				(float) (getItemAtk()
						+ weaponRollDelta * glyphAttackMultiplier));
	}

	/**
	 * Applies glyph-granted lifesteal even when the hit was made without a held
	 * weapon. The blood glyph is described as granting a percentage of lifesteal,
	 * while the inherited implementation weights all player lifesteal against
	 * held weapon ATK and therefore gives a glyph no effect when that sum is zero.
	 *
	 * Armed attacks keep using the inherited implementation unchanged so weapon,
	 * glove and ring lifesteal weighting is not altered.
	 */
	@Override
	public void handleLifesteal(final RPEntity attacker,
			final List<Item> attackerWeapons, final int damage) {
		final double glyphLifesteal =
				GlyphEffectService.getLifestealBonusFraction(attacker);
		if (!attackerWeapons.isEmpty()
				|| Double.compare(glyphLifesteal, 0.0) == 0) {
			super.handleLifesteal(attacker, attackerWeapons, damage);
			return;
		}

		// Match the inherited lifesteal rounding and negative-value semantics.
		final int lifesteal = (int) (damage * glyphLifesteal + 0.5f);
		if (lifesteal >= 0) {
			attacker.heal(lifesteal, true);
		} else {
			attacker.damage(-lifesteal, attacker);
		}
		attacker.notifyWorldAboutChanges();
	}

	public static void generateRPClass() {
		try {
			DressedEntityRPClass.generateRPClass();
		} catch (final SyntaxException e) {
			logger.error("cannot generateRPClass", e);
		}
	}

	/**
	 * This is simply for backwards compatibility to update a user's outfit
	 * with the "outfit" attribute.
	 */
	@Override
	public void put(final String attr, final String value) {
		if (attr.equals("outfit")) {
			final StringBuilder sb = new StringBuilder();
			final int code = Integer.parseInt(value);

			sb.append("body=" + code % 100);
			sb.append(",dress=" + code / 100 % 100);
			sb.append(",head=" + (int) (code / Math.pow(100, 2) % 100));
			sb.append(",hair=" + (int) (code / Math.pow(100, 3) % 100));
			sb.append(",detail=" + (int) (code / Math.pow(100, 4) % 100));

			// "outfit_ext" actually manages the entity's outfit
			super.put("outfit_ext", sb.toString());
		}

		super.put(attr, value);
	}

	/**
	 * Gets this entity's outfit.
	 *
	 * Note: some entities (e.g. sheep, many NPC's, all monsters) don't use
	 * the outfit system.
	 *
	 * @return The outfit, or null if this RPEntity is represented as a single
	 *         sprite rather than an outfit combination.
	 */
	public Outfit getOutfit() {
		if (has("outfit_ext")) {
			return new Outfit(get("outfit_ext"));
		} else if (has("outfit")) {
			return new Outfit(Integer.toString(getInt("outfit")));
		}
		return null;
	}

	/**
	 * Retrieves the entity's original outfit.
	 *
	 * @return
	 *     Original outfit if entity is currently wearing a temporary one.
	 */
	public Outfit getOriginalOutfit() {
		if (has("outfit_ext_orig")) {
			return new Outfit(get("outfit_ext_orig"));
		} else if (has("outfit_org")) {
			return new Outfit(Integer.toString(getInt("outfit_org")));
		}

		return new Outfit(get("outfit_ext"));
	}

	/**
	 * gets the color map
	 *
	 * @return color map
	 */
	public Map<String, String> getOutfitColors() {
		return getMap("outfit_colors");
	}

	/**
	 * Retrieves color info for a single layer.
	 *
	 * @param layer
	 *     Layer name.
	 */
	public String getOutfitColor(final String layer) {
		final Map<String, String> ocolors = getOutfitColors();
		if (ocolors != null && ocolors.containsKey(layer)) {
			return ocolors.get(layer);
		}
		return null;
	}

	/**
	 * Removes layer color information.
	 */
	protected void clearColors() {
		for (final String part : getColorableLayers()) {
			remove("outfit_colors", part);
		}
	}

	/**
	 * Sets this entity's outfit.
	 *
	 * Note: some entities (e.g. sheep, many NPC's, all monsters) don't use
	 * the outfit system.
	 *
	 * @param outfit
	 *            The new outfit.
	 */
	public void setOutfit(final Outfit outfit) {
		setOutfit(outfit, false);
	}

	/**
	 * Makes this player wear the given outfit. If the given outfit contains
	 * null parts, the current outfit will be kept for these parts. If the
	 * outfit change includes any colors, they should be changed <b>after</b>
	 * calling this.
	 *
	 * @param outfit
	 *            The new outfit.
	 * @param temporary
	 *            If true, the original outfit will be stored so that it can be
	 *            restored later.
	 */
	public void setOutfit(final Outfit outfit, final boolean temporary) {
		// if the new outfit is temporary and the player is not wearing
		// a temporary outfit already, store the current outfit in a
		// second slot so that we can return to it later.
		if (temporary) {
			// remember original outfit & colors
			storeOriginalOutfit();

			// backward compatibility
			if (has("outfit") && !has("outfit_org")) {
				put("outfit_org", get("outfit"));
			}

			if (has("outfit_ext") || has("outfit")) {
				// remember the old color selections.
				for (final String part : getColorableLayers()) {
					String tmp = part + "_orig";
					String color = get("outfit_colors", part);
					if (color != null) {
						put("outfit_colors", tmp, color);
						if (!"hair".equals(part)) {
							remove("outfit_colors", part);
						}
					} else if (has("outfit_colors", tmp)) {
						// old saved colors need to be cleared in any case
						remove("outfit_colors", tmp);
					}
				}
			}
		} else {
			if (has("outfit_ext_orig")) {
				remove("outfit_ext_orig");
			}
			if (has("outfit_org")) {
				remove("outfit_org");
			}

			if (has("outfit_ext_orig") || has("outfit_org")) {
				// clear colors
				for (final String part : getColorableLayers()) {
					if (has("outfit_colors", part)) {
						remove("outfit_colors", part);
					}
				}
			}
		}

		// combine the old outfit with the new one, as the new one might
		// contain null parts.
		final Outfit newOutfit = outfit.putOver(getOutfit());

		final StringBuilder sb = new StringBuilder();
		sb.append("body=" + newOutfit.getLayer("body") + ",");
		sb.append("dress=" + newOutfit.getLayer("dress") + ",");
		sb.append("head=" + newOutfit.getLayer("head") + ",");
		sb.append("mouth=" + newOutfit.getLayer("mouth") + ",");
		sb.append("eyes=" + newOutfit.getLayer("eyes") + ",");
		sb.append("mask=" + newOutfit.getLayer("mask") + ",");
		sb.append("hair=" + newOutfit.getLayer("hair") + ",");
		sb.append("hat=" + newOutfit.getLayer("hat") + ",");
		sb.append("detail=" + newOutfit.getLayer("detail"));

		put("outfit_ext", sb.toString());
		notifyWorldAboutChanges();
	}

	/**
	 * Makes this player wear the given outfit. If the given outfit contains
	 * null parts, the current outfit will be kept for these parts. If the
	 * outfit change includes any colors, they should be changed <b>after</b>
	 * calling this.
	 *
	 * Currently supported layers should be in this order:
	 * 		body, dress, head, mouth, eyes, mask, hair, hat, detail
	 *
	 * @param layers
	 *            Integer indexes of each outfit layer or null.
	 */
	public void setOutfit(final Integer... layers) {
		setOutfit(new Outfit(layers), false);
	}

	/**
	 * Makes this player wear the given outfit. If the given outfit contains
	 * null parts, the current outfit will be kept for these parts. If the
	 * outfit change includes any colors, they should be changed <b>after</b>
	 * calling this.
	 *
	 * Currently supported layers should be in this order:
	 * 		body, dress, head, mouth, eyes, mask, hair, hat, detail
	 *
	 * @param temporary
	 *            If true, the original outfit will be stored so that it can be
	 *            restored later.
	 * @param layers
	 *            Integer indexes of each outfit layer or null.
	 */
	public void setOutfit(final boolean temporary, final Integer... layers) {
		setOutfit(new Outfit(layers), temporary);
	}

	/**
	 * Sets the entity's outfit using a string code. E.g.:
	 * 		body=1,hair=5,dress=13
	 *
	 * @param strcode
	 */
	public void setOutfit(final String strcode) {
		setOutfit(new Outfit(strcode), false);
	}

	/**
	 * Sets the entity's outfit using a string code. E.g.:
	 * 		body=1,hair=5,dress=13
	 *
	 * @param strcode
	 * 		String code representing outfit.
	 * @param temporary
	 * 		If true, the original outfit will be stored so that it can be
	 * 		restored later.
	 */
	public void setOutfit(final String strcode, final boolean temporary) {
		setOutfit(new Outfit(strcode), temporary);
	}

	// Hack to preserve detail layer
	public void setOutfitWithDetail(final Outfit outfit, final boolean temporary) {
		// preserve detail layer
		int oldDetailCode = getOutfit().getLayer("detail");
		int newDetailCode = outfit.getLayer("detail");
		if (oldDetailCode > 0 && newDetailCode == 0) {
			outfit.setLayer("detail", oldDetailCode);
		}
		setOutfit(outfit, temporary);
	}

	/**
	 * Set color for single outfit layer.
	 *
	 * @param part
	 * 		Layer to be colored.
	 * @param color
	 * 		<code>Integer</code> value color to use.
	 */
	public void setOutfitColor(final String part, final int color) {
		put("outfit_colors", part, color);
	}

	/**
	 * Set color for single outfit layer.
	 *
	 * @param part
	 * 		Layer to be colored.
	 * @param color
	 * 		<code>Color</code> value color to use.
	 */
	public void setOutfitColor(final String part, final Color color) {
		setOutfitColor(part, color.getRGB());
	}

	public void setOutfitColor(final String part, final String color) {
		put("outfit_colors", part, color);
	}

	/**
	 * Set colors for the entire outfit.
	 *
	 * @param parts
	 * 		<code>Map</code> of layers & colors (<code>Integer</code>).
	 */
	public void setOutfitColors(final Map<String, Integer> parts) {
		remove("outfit_colors"); // clear old colors
		for (final String key: parts.keySet()) {
			put("outfit_colors", key, parts.get(key));
		}
	}

	/**
	 * Checks if the entity is not wearing clothes.
	 */
	public boolean isNaked() {
		return getOutfit().isNaked();
	}

	/**
	 * Unset color of a single layer.
	 *
	 * @param part
	 * 		Layer to be unset.
	 */
	public void unsetOutfitColor(final String part) {
		remove("outfit_colors", part);
	}

	protected List<String> getColorableLayers() {
		final List<String> new_list = new ArrayList<>();
		for (final String part : RECOLORABLE_OUTFIT_PARTS) {
			if (!SKIN_LAYERS.contains(part)) {
				new_list.add(part);
			}
		}

		new_list.add("skin");
		return new_list;
	}

	protected void storeOriginalOutfit() {
		if (has("outfit_ext") && !has("outfit_ext_orig")) {
			put("outfit_ext_orig", get("outfit_ext"));
		}

		for (final String part : getColorableLayers()) {
			final String color_orig = get("outfit_colors", part + "_orig");
			if (color_orig == null) {
				final String color = get("outfit_colors", part);
				if (color != null) {
					put("outfit_colors", part + "_orig", color);
				}
			}
		}
	}

	public void restoreOriginalOutfit() {
		if (has("outfit_ext_orig")) {
			setOutfitWithDetail(new Outfit(get("outfit_ext_orig")), false);

			for (final String part : getColorableLayers()) {
				final String color_orig = get("outfit_colors", part + "_orig");
				if (color_orig != null) {
					put("outfit_colors", part, color_orig);
					remove("outfit_colors", part + "_orig");
				}
			}
		}
	}

	@Override
	protected abstract void dropItemsOn(Corpse corpse);

	@Override
	public abstract void logic();
}
