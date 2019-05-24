/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.List;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPRuleProcessor;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * Alters the state of a quest of a player.
 *
 * @author hendrik
 */
public class AlterQuest extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {

		// help text
		if (args.size() < 2) {
			admin.sendPrivateText("Użyj /script AlterQuest.class <wojownik> <nazwa_zadania> <stan>. Pomiń <stan>, aby usunąć zadanie.");
			return;
		}

		// find player
		final StendhalRPRuleProcessor rules = SingletonRepository.getRuleProcessor();
		final Player target = rules.getPlayer(args.get(0));
		if (target != null) {

			// old state
			final String questName = args.get(1);
			String oldQuestState = null;
			if (target.hasQuest(questName)) {
				oldQuestState = target.getQuest(questName);
			}

			// new state (or null to remove the quest)
			String newQuestState = null;
			if (args.size() > 2) {
				newQuestState = args.get(2);
			}

			// set the quest
			target.setQuest(questName, newQuestState);

			// notify admin and altered player
			target.sendPrivateText(NotificationType.SUPPORT, "Administrator " + admin.getTitle()
					+ " zmienił stan twojego zadania '" + questName
					+ "' z '" + oldQuestState + "' na '" + newQuestState
					+ "'");
			admin.sendPrivateText("Zmieniono stan zadania '" + questName
					+ "' z '" + oldQuestState + "' na '" + newQuestState
					+ "'");
		} else {
			admin.sendPrivateText(args.get(0) + " nie jest zalogowany");
		}
	}

}
