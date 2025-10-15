/***************************************************************************
 *                   (C) Copyright 2003-2022 - Stendhal                    *
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

import games.stendhal.client.entity.Entity;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.sound.facade.SoundSystemFacade;
import games.stendhal.common.NotificationType;

/**
 * @author hendrik
 *
 */
public interface UserInterface {
	/**
	 * Add an event line to the chat log.
	 *
	 * @param line event line
	 */
	public void addEventLine(final EventLine line);

	/**
	 * Adds a text box on the screen
	 *
	 * @param x  x
	 * @param y  y
	 * @param text
	 *     Text to display.
	 * @param type
	 *     Type of text.
	 * @param isTalking
	 *     Chat?
	 */
	@Deprecated
	public void addGameScreenText(final double x, final double y,
			final String text, final NotificationType type,
			final boolean isTalking);

	/**
	 * Adds a text box on the screen.
	 *
	 * @param entity
	 *     Entity that text bubble will follow.
	 * @param text
	 *     Text to display.
	 * @param type
	 *     Type of text.
	 * @param isTalking
	 *     Chat?
	 */
	@Deprecated
	public void addGameScreenText(final Entity entity,
			final String text, final NotificationType type,
			final boolean isTalking);

	/**
	 * Display a box for a reached achievement
	 *
	 * @param title the title of the achievement
	 * @param description the description of the achievement
	 * @param category the category of the achievement
	 */
	public void addAchievementBox(final String title, final String description, final String category);

	/**
	 * gets the sound system
	 *
	 * @return SoundSystemFacade
	 */
	public SoundSystemFacade getSoundSystemFacade();

	/**
	 * are debug messages enabled?
	 *
	 * @return true, if debug messages are enabled; false otherwise
	 */
	public boolean isDebugEnabled();

	/**
	 * toggle debug messages
	 */
	public void toggleDebugEnabled();

	/**
	 * Requests the producer window to open for a specific NPC.
	 *
	 * @param npcName
	 *	    internal NPC name.
	 * @param npcTitle
	 *	    display title of the NPC.
	 */
	public void showProducerWindow(String npcName, String npcTitle);
}
