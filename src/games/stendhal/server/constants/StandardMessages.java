/***************************************************************************
 *                 Copyright © 2003-2024 - Faiumoni e. V.                  *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.constants;

import games.stendhal.common.NotificationType;
import games.stendhal.server.entity.player.Player;

public abstract class StandardMessages {
	/**
	 * Processes a message.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @param _type
	 *   Message type or {@code null} for default type.
	 * @param msg
	 *   Message text.
	 * @return
	 *   Message text.
	 */
	private static String process(final Player target, final NotificationType _type,
			final String msg) {
		if (target != null) {
			if (_type != null) {
				target.sendPrivateText(_type, msg);
			} else {
				target.sendPrivateText(msg);
			}
		}
		return msg;
	}

	/**
	 * Processes a message.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @param msg
	 *   Message text.
	 * @return
	 *   Message text.
	 */
	private static String process(final Player target, final String msg) {
		return process(target, null, msg);
	}

	/**
	 * Processes an error type message.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @param msg
	 *   Message text.
	 * @return
	 *   Message text
	 */
	private static String processError(final Player target, final String msg) {
		return process(target, NotificationType.ERROR, msg);
	}

	/**
	 * Specified player was not found online.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @param subject
	 *   Name of the subject player.
	 * @return
	 *   Message text.
	 */
	public static String playerNotOnline(final Player target, final String subject) {
		return process(target, "Żaden wojownik o imieniu \"" + subject + "\" nie jest aktualnie zalogowany.");
	}

	/**
	 * Script or command executed with too few parameters.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @return
	 *   Message text.
	 */
	public static String missingParameter(final Player target) {
		return processError(target, "Brakuje parametru.");
	}

	/**
	 * Script or command executed with too few parameters.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @param cmd
	 *   Name of command that was to be executed.
	 * @return
	 *   Message text.
	 */
	public static String missingParameter(final Player target, final String cmd) {
		return processError(target, "Brakuje parametru dla komendy \"" + cmd + "\".");
	}

	/**
	 * Command parameter requires an additional value parameter.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @param param
	 *   Name of parameter.
	 * @return
	 *   Message text.
	 */
	public static String missingParamValue(final Player target, final String param) {
		return processError(target, "Brakuje wartości dla parametru \"" + param + "\".");
	}

	/**
	 * Script or command executed with too many parameters.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @param cmd
	 *   Name of command that was to be executed.
	 * @return
	 *   Message text.
	 */
	public static void excessParameter(final Player target) {
		target.sendPrivateText(NotificationType.ERROR, "Zbyt wiele argumentów.");
	}

	/**
	 * Script or command executed with too many parameters.
	 */
	public static String excessParameter(final Player target, final String cmd) {
		return processError(target, "Zbyt wiele argumentów dla komendy \"" + cmd + "\".");
	}

	/**
	 * Script or command executed with unacceptable parameter.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @param cmd
	 *   Name of command that was to be executed.
	 * @return
	 *   Message text.
	 */
	public static String unknownCommand(final Player target, final String cmd) {
		return processError(target, "Nieznana komenda: " + cmd);
	}

	/**
	 * Script or command executed with unacceptable parameter.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @param param
	 *   Name of parameter.
	 * @return
	 *   Message text.
	 */
	public static String unknownParameter(final Player target, final String param) {
		return processError(target, "Nieznany parametr: " + param);
	}

	/**
	 * Script or command executed with unacceptable parameter type.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @param param
	 *   Name of parameter.
	 * @return
	 *   Message text.
	 */
	public static String paramMustBeNumber(final Player target, final String param) {
		return processError(target, param + " musi być liczbą.");
	}

	/**
	 * Script or command executed with unacceptable parameter type.
	 *
	 * @param target
	 *   Player receiving message or {@code null}.
	 * @return
	 *   Message text.
	 */
	public static String paramMustBeNumber(final Player target) {
		return paramMustBeNumber(target, "Parametr");
	}

	/**
	 * Messages admin and player when admin makes a change to player's quest state.
	 *
	 * @param admin
	 *   Admin making change.
	 * @param player
	 *   Player being update.
	 * @param questName
	 *   Quest ID/name.
	 * @param oldState
	 *   Quest state before change.
	 * @param newState
	 *   Quest state after change.
	 */
	public static void changedQuestState(final Player admin, final Player player,
			final String questName, final String oldState, final String newState) {
		player.sendPrivateText(NotificationType.SUPPORT, "Administrator " + admin.getTitle()
				+ " zmienił twój stan zadania '" + questName + "' z '" + oldState + "' na '"
				+ newState + "'");
		admin.sendPrivateText("Zmieniono stan zadania '" + questName + "' z '" + oldState
				+ "' na '" + newState + "'");
	}
}