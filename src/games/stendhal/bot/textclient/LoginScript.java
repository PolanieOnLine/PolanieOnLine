/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.bot.textclient;

import marauroa.client.ClientFramework;
import marauroa.common.game.RPAction;

/**
 * actions to execute on login
 *
 * @author hendrik
 */
public class LoginScript {
	private ClientFramework client;

	/**
	 * creates a new LoginScript
	 *
	 * @param client ClientFramework
	 */
	public LoginScript(ClientFramework client) {
		this.client = client;
	}

	/**
	 * performs some steps after an admin login
	 */
	public void adminLogin() {
		teleportToPostofficeLocation();
	}

	/**
	 * teleports to the admin house
	 */
	private void teleportToPostofficeLocation() {
		RPAction action = new RPAction();
		action.put("type", "teleport");
		action.put("target", "postman");
		action.put("zone", "int_zakopane_postoffice");
		action.put("x", "4");
		action.put("y", "5");
		client.send(action);
	}
}
