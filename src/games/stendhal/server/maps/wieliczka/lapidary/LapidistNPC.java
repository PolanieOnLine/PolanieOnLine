/***************************************************************************
 *                   (C) Copyright 2024 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.wieliczka.lapidary;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;

public class LapidistNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("czeladnik Jaromir") {
			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Zajmuję się głównie szlifiarstwem kamieni szlachetnych, czasem też reperuję biżuterię tutejszych.");
				addGoodbye();

				add(ConversationStates.ANY,
					ConversationPhrases.HELP_MESSAGES,
					new QuestCompletedCondition("grind_misty_gem"),
					ConversationStates.ATTENDING,
					"Mogę zająć się obróbką kryształu, aby uzyskał piękny i zjawiskowy wygląd.",
					null);

				add(ConversationStates.ANY,
					ConversationPhrases.HELP_MESSAGES,
					new NotCondition(new QuestCompletedCondition("grind_misty_gem")),
					ConversationStates.ATTENDING,
					"Żartujesz sobie? Jestem aktualnie zajęty innymi sprawami.",
					null);

				add(ConversationStates.ANY,
					ConversationPhrases.HELP_MESSAGES,
					new QuestCompletedCondition("grind_misty_gem"),
					ConversationStates.ATTENDING,
					"Znam pewną osobę, która lubi wyzwania i testowania nowych metod. Znajdź kowala #Przemysława. Na pewno znajdzie ciekawe zastosowanie tego surowca.",
					null);

				add(ConversationStates.ATTENDING,
					"przemysław",
					new QuestCompletedCondition("grind_misty_gem"),
					ConversationStates.ATTENDING,
					"Już ci mówię gdzie go znajdziesz, tylko zerknę na zwój... Ekh.. Okej, musisz iść w stronę Tarnowa. We wiosce zaraz pod tawerną znajdziesz nieco większa chatkę. Jeśli dobrze pamiętam dachówki miał w kolorze niebieskim...",
					null);

				add(ConversationStates.ANY,
					ConversationPhrases.OFFER_MESSAGES,
					new NotCondition(new QuestCompletedCondition("grind_misty_gem")),
					ConversationStates.ATTENDING,
					"Nie mam nic Tobie do zaoferowania...",
					null);
			}
		};

		npc.setDescription("Oto czeladnik Jaromir. Mistrz szlifiarstwa klejnotów z których wydobywa z nich co najpiękniejsze.");
		npc.setEntityClass("blacksmithnpc");
		npc.setGender("M");
		npc.setPosition(10, 3);
		zone.add(npc);
	}
}