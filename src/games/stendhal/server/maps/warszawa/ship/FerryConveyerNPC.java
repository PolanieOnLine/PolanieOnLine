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
package games.stendhal.server.maps.warszawa.ship;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.krakow.ship.KrakowFerry;
import games.stendhal.server.maps.krakow.ship.KrakowFerry.Status;

import java.util.Arrays;
import java.util.Map;

/**
 * Factory for an NPC who brings players from the docks to Krakow Ferry
 * in a rowing boat.
 */
public class FerryConveyerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	protected Status ferrystate;
	private static StendhalRPZone shipZone;

	public static StendhalRPZone getShipZone() {
		if (shipZone == null) {
			shipZone = SingletonRepository.getRPWorld().getZone("int_polish_ship");
		}
		return shipZone;
	}
	
	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Franek") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			public void createDialog() {
				addGoodbye("Dowidzenia!");
				addGreeting("Witam w Warszawskim #ferry service! W czym mogę #pomóc?");
				addHelp("Możesz wejść na #prom tylko za "
						+ KrakowFerry.PRICE
						+ " złota, ale tylko wtedy, kiedy jest zacumowany przy przystani. Zapytaj mnie o #status jeżeli chcesz wiedzieć gdzie jest prom.");
				addJob("Jeżeli pasażerowie chcą #wejść na #prom do Warszawy to ja ich zabieram na statek.");
				addReply(Arrays.asList("ferry", "prom", "promu"), "Prom żegluje regularnie pomiędzy Warszawą, a Krakowem. Możesz #wejść na statek tylko kiedy jest tutaj. Zapytaj mnie o #status jeżeli chcesz sprawdzić gdzie aktualnie się znajduje.");
				addReply(Arrays.asList("Kraków", "Krakowem"), "Kraków to miasto leżące na południu Polski posiadające wiele zabytków oraz świetne miasto wypoczynku.");
				add(ConversationStates.ATTENDING, "status",
						null,
						ConversationStates.ATTENDING,
						null,
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
								npc.say(ferrystate.toString());
							}
						});

				add(ConversationStates.ATTENDING,
						Arrays.asList("board", "wejdź", "wejść"),
						null,
						ConversationStates.ATTENDING,
						null,
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
								if (ferrystate == Status.ANCHORED_AT_WARSZAWA) {
									npc.say("Aby wejść na prom musisz zapłacić " + KrakowFerry.PRICE
											+ " złota. Czy chcesz zapłacić?");
									npc.setCurrentState(ConversationStates.SERVICE_OFFERED);
								} else {
									npc.say(ferrystate.toString()
											+ " Możesz wejść na prom wtedy, kiedy prom jest zacumowany.");
								}
							}
						});

				add(ConversationStates.SERVICE_OFFERED,
						ConversationPhrases.YES_MESSAGES,
						null,
						ConversationStates.ATTENDING, null,
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
								if (player.drop("money", KrakowFerry.PRICE)) {
									player.teleport(getShipZone(), 27, 33, Direction.LEFT, null);
								} else {
									npc.say("Hej! Nie masz tyle pieniędzy!");
								}
							}
						});

				add(ConversationStates.SERVICE_OFFERED,
						ConversationPhrases.NO_MESSAGES,
						null,
						ConversationStates.ATTENDING,
						"Nie wiesz co tracisz, szczurze lądowy!",
						null);
			}
		};

		new KrakowFerry.FerryListener() {
			@Override
			public void onNewFerryState(final Status status) {
				ferrystate = status;
				switch (status) {
					case ANCHORED_AT_WARSZAWA:
						npc.say("UWAGA: Prom przybył do wybrzeża! Można wejść na statek mówiąc #wejdź.");
						break;
					case DRIVING_TO_KRAKOW:
						npc.say("UWAGA: Prom odpłynął. Nie można się już dostać na statek.");
						break;
					default:
						break;
				}
			}
		};

		npc.setPosition(6, 62);
		npc.setEntityClass("npcflisak1");
		npc.setDirection(Direction.LEFT);
		zone.add(npc);
	}
}
