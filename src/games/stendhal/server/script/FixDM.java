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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPRuleProcessor;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * Puts the Deathmatch slot of a player into victory format.
 *
 * @author kymara
 */
public class FixDM extends ScriptImpl {

	private static final String questName = "deathmatch";

	@Override
	public void execute(final Player admin, final List<String> args) {

		// help text
		if (args.size() < 1) {
			admin.sendPrivateText("Użyj /script FixDM.class <wojownik>. Sprawdza czy zabił wszystkie potwory!");
			return;
		}

		// find player
		final StendhalRPRuleProcessor rules = SingletonRepository.getRuleProcessor();
		final Player target = rules.getPlayer(args.get(0));
		if (target != null) {

			// old state
			if (!target.hasQuest(questName)) {
				admin.sendPrivateText(target.getTitle() + " nigdy nie zrobił deathmatcha.");
				return;
			}
			String oldQuestState = target.getQuest(questName);
			if (oldQuestState.equals("done")) {
				admin.sendPrivateText(target.getTitle() + " ostatnio pomyślnie zaliczył deathmatcha. Nie wykonuje teraz DMa.");
				return;
			}
			final String[] questpieces = oldQuestState.split(";");
			if (questpieces.length < 2) {
				admin.sendPrivateText(target.getTitle() + " ukończył deathmatch ze stanem " + oldQuestState + "  ,np. czy nie uciekł z deathmatcha lub przerwał go? Musisz naprawić jego hełm ręcznie o ile powinienen być naprawiony. Zadanie powinno być ustawione na #done.");
				target.setQuest(questName, "done");
				target.sendPrivateText("Administrator " + admin.getTitle()
								   + " zmienił stan Twojego zadania '" + questName
									   + "' z '" + oldQuestState + "' na 'done'. Wciąż musi naprawić Twój hełm.");
				return;
			} else {
				String newQuestState = "victory;" + questpieces[1] + ";" + questpieces[2];
				// set the quest
				target.setQuest(questName, newQuestState);

				// notify admin and altered player
				target.sendPrivateText("Spróbuj ponownie zawołać victory, a " + admin.getTitle()
									   + " uderzy magiczną różdżką.");
				admin.sendPrivateText("Zmienił stan zadania '" + questName
									  + "' z '" + oldQuestState + "' na '" + newQuestState
									  + "'. Powiedz " + target.getTitle() + ", aby znowu zawołał victory.");
			}
		} else {
				admin.sendPrivateText(args.get(0) + " nie jest zalogowany");
		}
	}
}
