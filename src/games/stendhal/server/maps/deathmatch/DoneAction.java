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
package games.stendhal.server.maps.deathmatch;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.dbcommand.WriteHallOfFamePointsCommand;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.player.Player;
import marauroa.server.db.command.DBCommandQueue;

/**
 * Handles player claim of victory by giving reward after verifying the winning.
 */
public class DoneAction implements ChatAction {

	/**
	 * Creates the player bound special trophy helmet and equips it.
	 *
	 * @param player Player object
	 * @return Helmet
	 */
	private Item createTrophyHelmet(final Player player) {
		final Item helmet = SingletonRepository.getEntityManager().getItem("zdobyczny hełm");
		helmet.setBoundTo(player.getName());
		helmet.put("def", 1);
		helmet.setInfoString(player.getName());
		helmet.setPersistent(true);
		helmet.setDescription("Oto główna nagroda dla wojownika " + player.getName()
		        + " za wygranie Deathmatchu. Noś ją z dumą.");
		player.equipOrPutOnGround(helmet);
		return helmet;
	}

	/**
	 * Updates the player's points in the hall of fame for deathmatch.
	 *
	 * @param player Player
	 */
	private void updatePoints(final Player player) {
		final DeathmatchState deathmatchState = DeathmatchState.createFromQuestString(player.getQuest("deathmatch"));
		DBCommandQueue.get().enqueue(new WriteHallOfFamePointsCommand(player.getName(), "D", deathmatchState.getPoints(), true));
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		final DeathmatchState deathmatchState = DeathmatchState.createFromQuestString(player.getQuest("deathmatch"));
		if (deathmatchState.getLifecycleState() != DeathmatchLifecycle.VICTORY) {
			raiser.say("Nie oszukuj mnie! Jedyne co możesz zrobić to #wycofać się lub wygrać.");
			return;
		}

		updatePoints(player);

		// We assume that the player only carries one trophy helmet.
		final Item helmet = player.getFirstEquipped("zdobyczny hełm");
		if (helmet == null) {
			createTrophyHelmet(player);
			raiser.say("Oto twój specjalny zdobyczny hełm. Trzymaj tak dalej, a z każdym ukończonym deathmatchem "
				+ " jego obrona będzie się zwiększać o 1. Teraz powiedz mi czy chcesz wyjść, mówiąc #wyjdź.");
		} else {
			int defense = 1;
			if (helmet.has("def")) {
				defense = helmet.getInt("def");
			}
			defense++;
			final int maxdefense = 5 + (player.getLevel() / 5);
			if (defense > maxdefense) {
				helmet.put("def", maxdefense);
				raiser.say("Z przykrością oznajmiam, że osiągnąłeś szczyt obrony dla swojego hełmu, który wynosi "
				                + maxdefense);
			} else {
				helmet.put("def", defense);
				String message;
				if (defense == maxdefense) {
					message = "Twój hełm został magicznie wzmocniony do maksimum jak na twój poziom czyli " + defense;
				} else {
					message = "Twój hełm został magicznie wzmocniony do obrony " + defense;
				}
				raiser.say(message + ". Powiedz mi #wyjdź, kiedy będziesz chciał opuścić arenę.");
			}
		}
		player.updateItemAtkDef();
		TurnNotifier.get().notifyInTurns(0, new NotifyPlayerAboutHallOfFamePoints((SpeakerNPC) raiser.getEntity(), player.getName(), "D", "deathmatch_score"));
		
		new SetQuestAction("deathmatch", 0, "done").fire(player, sentence, raiser);
		// Track the number of wins.
		new IncrementQuestAction("deathmatch", 6, 1).fire(player, sentence, raiser);
		SingletonRepository.getAchievementNotifier().onFinishQuest(player);
	}

}
