/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.pirate_island.ship;

import java.util.Arrays;
import java.util.Map;

import games.pol.server.maps.pirate_island.ship.PirateFerry.Status;
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
			islandDocksZone = SingletonRepository.getRPWorld().getZone("0_gdansk_city_n");
		}

		return islandDocksZone;
	}

	private Status ferryState;

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Fionella") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			public void createDialog() {
				addGoodbye("Do widzenia!");
				addGreeting("Ahoj, Przyjacielu! W czym mogę #pomóc?");
				addHelp("Tak, możesz zejść mówiąc #zejdź, ale wtedy kiedy zacumujemy na przystani. Zapytaj mnie o #status jeżeli nie masz pojęcia gdzie jesteśmy.");
				addJob("Zabieram kamratów, którzy chcą zejść na ląd.");

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
									case ANCHORED_AT_ISLAND:
										npc.say("Czy chcesz, abym zabrała Ciebie na wyspę?");
										npc.setCurrentState(ConversationStates.SERVICE_OFFERED);
										break;
									case ANCHORED_AT_GDANSK:
										npc.say("Czy chcesz, abym zabrała Ciebie do Gdańska?");
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
									case ANCHORED_AT_ISLAND:
										player.teleport(getMainlandDocksZone(), 9, 87, Direction.UP, null);
										npc.setCurrentState(ConversationStates.IDLE);
										break;
									case ANCHORED_AT_GDANSK:
										player.teleport(getIslandDockZone(), 96, 27, Direction.DOWN, null);
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
			}
		};

		new PirateFerry.FerryListener() {

			@Override
			public void onNewFerryState(final Status status) {
				ferryState = status;
				switch (status) {
					case ANCHORED_AT_ISLAND:
						npc.say("UWAGA: Prom dobił na piracką wyspę! Możesz teraz zejść mówiąc #zejdź.");
						break;
					case ANCHORED_AT_GDANSK:
						npc.say("UWAGA: Prom dobił do Gdańska! Możesz teraz zejść mówiąc #zejdź.");
						break;
					default:
						npc.say("UWAGA: Prom wypłynął.");
						break;
				}
			}
		};

		npc.setDescription("Oto Fionella, która zajmuje się pasażeramy, aby bezpiecznie zeszli z łajby na prom.");
		npc.setEntityClass("pirate_sailor2npc");
		npc.setGender("F");
		npc.setPosition(28, 34);
		npc.setDirection(Direction.LEFT);
		zone.add(npc);
	}

	private static StendhalRPZone getMainlandDocksZone() {
		if (mainlandDocksZone == null) {
			mainlandDocksZone = SingletonRepository.getRPWorld().getZone("0_pirates_island_e");
		}

		return mainlandDocksZone;
	}
}
