/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.awt.Color;

import javax.swing.JComponent;

import games.stendhal.client.gui.chatlog.EventLine;

/**
 * Abstraction for chat log components.
 */
public interface ChatLogView {
/**
 * @return Swing component hosting the chat log UI.
 */
JComponent getComponent();

/**
 * Append an event line to the chat log.
 *
 * @param line event to display
 */
void addLine(EventLine line);

/**
 * Clear all messages from the chat log.
 */
void clear();

/**
 * Configure the default background color used when no unread highlight is
 * active.
 *
 * @param color background color
 */
void setDefaultBackground(Color color);

/**
 * Set the associated channel name for persistence and logging.
 *
 * @param name channel name
 */
void setChannelName(String name);
}
