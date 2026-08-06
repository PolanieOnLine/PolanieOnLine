/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Item;
import games.stendhal.common.constants.ItemRarity;
import marauroa.common.game.RPObject;

/** Builds text rarity presentation without depending on graphics setup. */
final class ItemRarityPresentation {
	private ItemRarityPresentation() {
		// utility class
	}

	static String buildItemToolTip(final IEntity entity) {
		if (entity == null) {
			return null;
		}

		final String scrollDestination = getScrollDestination(entity);
		if (!(entity instanceof Item)) {
			return scrollDestination;
		}

		final ItemRarity rarity = ((Item) entity).getRarity();
		if (rarity == null) {
			return scrollDestination;
		}

		final StringBuilder tooltip = new StringBuilder("<html>");
		final String title = entity.getTitle();
		if (title != null) {
			tooltip.append("<b><font color=\"");
			tooltip.append(escapeHtml(rarity.getColorHex()));
			tooltip.append("\">");
			tooltip.append(escapeHtml(title));
			tooltip.append("</font></b><br>");
		}
		tooltip.append("Rzadkość: ");
		tooltip.append(escapeHtml(rarity.getPolishDisplayName()));
		if (scrollDestination != null) {
			tooltip.append("<br>");
			tooltip.append(escapeHtml(scrollDestination));
		}
		tooltip.append("</html>");
		return tooltip.toString();
	}

	private static String getScrollDestination(final IEntity entity) {
		final RPObject object = entity.getRPObject();
		if ("scroll".equals(entity.getEntityClass()) && object.has("dest")) {
			return object.get("dest").replaceFirst(",", " ");
		}
		return null;
	}

	private static String escapeHtml(final String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}
}
