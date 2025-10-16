/***************************************************************************
 *                   (C) Copyright 2006-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.extension;

import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.actions.admin.AdministrationAction;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

import org.apache.log4j.Logger;

/**
 * This extension adds marriage to the game world. there are 2 commands:
 * <li>/marry &lt;Player1&gt; &lt;Player2&gt;
 * <p>
 * which will create a bond between those players This command is an admin
 * command of the same access level as /jail ;)
 * <li>/spouse which will teleport a married player to his spouse
 * <p>
 * To enable this extension, add it to the marauroa.int file:
 * <p>
 * <tt>#</tt> load StendhalServerExtension(s)
 * <p>
 * groovy=games.stendhal.server.scripting.StendhalGroovyRunner <p>
 * http=games.stendhal.server.StendhalHttpServer <p>
 * spouse=games.stendhal.server.extension.SpouseExtension <p>
 * server_extension=groovy,http,spouse <p>
 * 
 * @author intensifly
 */
public class SpouseExtension extends StendhalServerExtension implements
		ActionListener {

	private final String SPOUSE = "spouse";

	private static final Logger logger = Logger.getLogger(SpouseExtension.class);

	/**
	 * 
	 */
	public SpouseExtension() {
		super();
		logger.info("SpouseExtension starting...");
		CommandCenter.register("marry", this, 400);
		CommandCenter.register("spouse", this);
	}

	/*
	 * @see games.stendhal.server.StendhalServerExtension#init()
	 */
	@Override
	public void init() {
		// this extension has no specific init code, everything is
		// implemented as /commands that are handled onAction
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		final String type = action.get("type");

		if (type.equals("marry")) {
			onMarry(player, action);
		} else if (type.equals("spouse")) {
			onSpouse(player, action);
		}
	}

	@SuppressWarnings("null")
	private void onMarry(final Player player, final RPAction action) {
		final String usage = "Użyj: #/marry <wojownik1> <wojownik2>";
		String text = "";

		Player player1 = null;
		String name1 = null;
		Player player2 = null;
		String name2 = null;
		boolean canMarry = true;

		if (!AdministrationAction.isPlayerAllowedToExecuteAdminCommand(player,
				"marry", true)) {
			return;
		}

		if (action.has("target")) {
			name1 = action.get("target");
			player1 = SingletonRepository.getRuleProcessor().getPlayer(name1);
			if (player1 == null) {
				canMarry = false;
				text += "Wojownik \"" + name1 + "\" nie został znaleziony. ";
			}
		} else {
			canMarry = false;
			text = usage;
		}

		if (action.has("args")) {
			name2 = action.get("args");
			player2 = SingletonRepository.getRuleProcessor().getPlayer(name2);
			if (player2 == null) {
				canMarry = false;
				text += "Wojownik \"" + name2 + "\" nie został znaleziony. ";
			}
		} else {
			canMarry = false;
			text = usage;
		}

		if (canMarry) {
			if (name1.equals(name2)) {
				text += "Wojownik nie może wziąć ślubu ze sobą!";
				canMarry = false;
			}
		}

		if (canMarry) {
			if (player1.hasQuest(SPOUSE)) {
				text += name1 + " poślubił już "
						+ player1.getQuest(SPOUSE) + ". ";
				canMarry = false;
			}
			if (player2.hasQuest(SPOUSE)) {
				text += name2 + " poślubił już "
						+ player2.getQuest(SPOUSE) + ". ";
				canMarry = false;
			}
		}

		if (canMarry) {
			player1.setQuest(SPOUSE, name2);
			player1.sendPrivateText("Gratulacje! Wziąłeś ślub z \""
					+ name2
					+ "\". Możesz użyć #/spouse jeżeli chcecie być razem.");
			player2.setQuest(SPOUSE, name1);
			player2.sendPrivateText("Gratulacje! Wziąłeś ślub z \""
					+ name1
					+ "\". Możesz użyć #/spouse jeżeli chcecie być razem.");
			text = "\"" + name1 + "\" i ty \""
					+ name2 + "\" jesteście teraz małżeństwem.";
			new GameEvent(player.getName(), "marry",  name1 + " + " + name2).raise();
		}

		player.sendPrivateText(text.trim());
	}

	private void onSpouse(final Player player, final RPAction action) {
		if (player.hasQuest(SPOUSE)) {
			Player teleported = null;

			final String name = player.getQuest(SPOUSE);
			teleported = SingletonRepository.getRuleProcessor().getPlayer(name);

			if (teleported == null) {
				final String text = "Twojego małżonka (\"" + name + "\") nie ma teraz w grze.";
				player.sendPrivateText(text);
				logger.debug(text);
				return;
			}

			final StendhalRPZone zone = teleported.getZone();
			final int x = teleported.getX();
			final int y = teleported.getY();

			if (player.teleport(zone, x, y, teleported.getDirection(), null)) {
				new GameEvent(player.getName(), "teleportto", teleported.getName() + "(spouse)").raise();
			}
		}
	}

}
