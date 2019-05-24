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
package games.stendhal.server.maps.orril.river;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Configure Orril River South Campfire (Outside/Level 0).
 */
public class GoldProspectorNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildGoldSourceArea(zone);
	}

	private void buildGoldSourceArea(final StendhalRPZone zone) {

		final SpeakerNPC bill = new SpeakerNPC("Bill") {

			@Override
			protected void createDialog() {
				addGreeting("Czołem partnerze!");
				addJob("Kiedyś byłem najlepszym poszukiwaczem złota, ale z biegiem czasu nabawiłem się bólu krzyża. Teraz jestem emerytem. Jednakże wciąż mogę dawać rady nowicjuszom!");
				add(ConversationStates.ATTENDING, ConversationPhrases.HELP_MESSAGES, null,
				        ConversationStates.INFORMATION_1,
				        "Wyjawię Tobie sekret o wydobywaniu złota. Jeżeli jesteś zainteresowany?", null);

				add(
				        ConversationStates.INFORMATION_1,
				        ConversationPhrases.YES_MESSAGES,
				        null,
				        ConversationStates.ATTENDING,
				        "Po pierwsze potrzebujesz #'misy do płukania złota' aby oddzielić złoto od błota. Potem musisz szukać w odpowiednich miejscach w wodzie. Gładka woda w tym obszarze jest bardzo bogata w złoto. Kiedy widzisz coś świecącego w jasnoniebieskiej wodzie po prostu naciśnij dwukrotnie przycisk. Nie poddawaj się zbyt szybko. Potrzebujesz szczęścia i cierpliwości.",
				        null);

				add(ConversationStates.INFORMATION_1, ConversationPhrases.NO_MESSAGES, null,
				        ConversationStates.ATTENDING,
				        "To nie ma znaczenia. Mało ludzi zna sekrety lepszego poszukiwania!", null);

				add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, null,
				        ConversationStates.ATTENDING,
				        "Nie mam zadania dla Ciebie. Jestem tutaj, aby pomóc nowym poszukiwaczom.", null);

				add(ConversationStates.ATTENDING, Arrays.asList("gold", "pan", "gold pan", "misy do płukania złota", "misa", "złota"), null,
				        ConversationStates.ATTENDING,
				        "Nie posiadam misy do płukania złota, ale możesz się zapytać kowala czy nie sprzedałby Tobie jednej.", null);

				addGoodbye("Dowidzenia!");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
			
		};

		bill.setEntityClass("oldcowboynpc");
		bill.setPosition(105, 58);
		bill.setDirection(Direction.DOWN);
		bill.initHP(100);
		bill.setDescription("Bill jest emerytowanym poszukiwaczem złota. Teraz czeka na swoich następców w tym interesie.");
		zone.add(bill);
	}
}
