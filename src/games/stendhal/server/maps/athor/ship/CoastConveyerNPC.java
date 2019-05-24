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
package games.stendhal.server.maps.athor.ship;

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
import games.stendhal.server.maps.athor.ship.AthorFerry.Status;

import java.util.Arrays;
import java.util.Map;

/** Factory for cargo worker on Athor Ferry. */

public class CoastConveyerNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private static StendhalRPZone islandDocksZone;
	private static StendhalRPZone mainlandDocksZone;

	private StendhalRPZone getIslandDockZone() {
		if (islandDocksZone == null) {

			islandDocksZone = SingletonRepository.getRPWorld().getZone("0_athor_island");
		}

		return islandDocksZone;
	}


	private Status ferryState;

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Jackie") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			public void createDialog() {

				addGoodbye("Dowidzenia!");
				addGreeting("Ahoj, Przyjacielu! W czym mogę #pomóc?");
				addHelp("Tak, możesz zejść mówiąc #zejdź, ale wtedy kiedy zacumujemy na przystani. Zapytaj mnie o #status jeżeli nie masz pojęcia gdzie jesteśmy.");
				addJob("Zabieram pasażerów, którzy chcą zejść na ląd.");

				add(ConversationStates.ATTENDING, "status",
						null,
						ConversationStates.ATTENDING,
						null,
						new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						npc.say(ferryState.toString());
					}
				});

				add(ConversationStates.ATTENDING,
						Arrays.asList("disembark", "leave", "zejdź", "opuść"),
						null,
						ConversationStates.ATTENDING,
						null,
						new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						switch (ferryState) {
						case ANCHORED_AT_MAINLAND:
							npc.say("Czy chcesz, abym zabrał Ciebie na stały ląd?");
							npc.setCurrentState(ConversationStates.SERVICE_OFFERED);
							break;
						case ANCHORED_AT_ISLAND:
							npc.say("Czy chcesz, abym zabrał Ciebie na wyspę?");
							npc.setCurrentState(ConversationStates.SERVICE_OFFERED);
							break;

						default:
							npc.say(ferryState.toString()
								+ " Możesz zejść na ląd kiedy jesteśmy zacumowani na przystani.");

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
						switch (ferryState) {
						case ANCHORED_AT_MAINLAND:
							player.teleport(getMainlandDocksZone(), 100, 100, Direction.LEFT, null);
							npc.setCurrentState(ConversationStates.IDLE);
							break;
						case ANCHORED_AT_ISLAND:
							player.teleport(getIslandDockZone(), 16, 89, Direction.LEFT, null);
							npc.setCurrentState(ConversationStates.IDLE);
							break;

						default:
							npc.say("Niedobrze! Statek już wypłynął.");

						}

					}
				});

				add(ConversationStates.SERVICE_OFFERED,
						ConversationPhrases.NO_MESSAGES,
						null,
						ConversationStates.ATTENDING,
						"Aye, przyjacielu!", null);

			}};
			new AthorFerry.FerryListener() {

			
				@Override
				public void onNewFerryState(final Status status) {
					ferryState = status;
					switch (status) {
					case ANCHORED_AT_MAINLAND:
						npc.say("UWAGA: Prom dobił do stałego lądu! Możesz teraz zejść mówiąc #zejdź.");
						break;
					case ANCHORED_AT_ISLAND:
						npc.say("UWAGA: Prom dobił do wyspy! Możesz teraz zejść mówiąc #zejdź.");
						break;
					default:
						npc.say("UWAGA: Prom wypłynął.");
						break;
					}

				}
			};

			npc.setPosition(29, 34);
			npc.setEntityClass("pirate_sailor2npc");
			npc.setDescription ("Jackie pomaga pasażerom zejść ze statku na ląd. Jest prawdziwą piratką!");
			npc.setDirection(Direction.LEFT);
			zone.add(npc);	
	}

	private static StendhalRPZone getMainlandDocksZone() {
		if (mainlandDocksZone == null) {
			mainlandDocksZone = SingletonRepository.getRPWorld().getZone("0_ados_coast_s_w2");
		}
		return mainlandDocksZone;
	}
}
