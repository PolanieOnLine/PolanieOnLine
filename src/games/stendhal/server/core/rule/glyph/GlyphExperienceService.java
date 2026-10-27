package games.stendhal.server.core.rule.glyph;

import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

/** The permanent knowledge glyph modifies personal creature XP, never addXP. */
public final class GlyphExperienceService {
	public static final String ITEM_NAME = "glif wiedzy";
	public static final int BONUS_PERCENT = 10;

	private GlyphExperienceService() { }

	public static boolean isKnowledgeGlyph(final Item item) {
		return item instanceof Glyph && ITEM_NAME.equals(item.getName());
	}

	/** Called once after group splitting and the recipient's reborn modifier. */
	public static int applyCombatBonus(final Player player, final int experience) {
		if (player == null || experience <= 0) return experience;
		for (final RPObject object : player.getSlot("utility_rune")) {
			if (object instanceof Item && isKnowledgeGlyph((Item) object)
					&& player.getName().equals(((Item) object).getBoundTo())) {
				final long reward = ((long) experience * (100 + BONUS_PERCENT) + 50) / 100;
				return (int) Math.min(Integer.MAX_VALUE, reward);
			}
		}
		return experience;
	}
}
