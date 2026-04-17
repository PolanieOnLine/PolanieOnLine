/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.events;

import games.stendhal.common.constants.Events;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;

/**
 * Screen-wide banner announcement rendered near the top-center of the game screen.
 */
public class ScreenAnnouncementEvent extends RPEvent {

	public static void generateRPClass() {
		final RPClass rpclass = new RPClass(Events.SCREEN_ANNOUNCEMENT);
		rpclass.add(DefinitionClass.ATTRIBUTE, "title", Type.STRING);
		rpclass.add(DefinitionClass.ATTRIBUTE, "text", Type.VERY_LONG_STRING);
		rpclass.add(DefinitionClass.ATTRIBUTE, "category", Type.STRING);
	}

	public ScreenAnnouncementEvent(final String title, final String text, final String category) {
		super(Events.SCREEN_ANNOUNCEMENT);
		put("title", title == null ? "" : title);
		put("text", text == null ? "" : text);
		put("category", category == null ? "default" : category);
	}
}
