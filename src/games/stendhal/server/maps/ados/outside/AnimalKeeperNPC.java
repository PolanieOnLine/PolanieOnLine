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
package games.stendhal.server.maps.ados.outside;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Pet;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

public class AnimalKeeperNPC implements ZoneConfigurator {

	private static final String ZONE_NAME = "int_ados_pet_sanctuary";

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildZooArea(zone);
	}

	private void buildZooArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Katinka") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(41, 40));
				nodes.add(new Node(51, 40));
				nodes.add(new Node(51, 46));
				nodes.add(new Node(58, 46));
				nodes.add(new Node(58, 42));
				nodes.add(new Node(51, 42));
				nodes.add(new Node(51, 40));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addOffer("Czy możesz zachować to w tajemnicy? Dr. Feelgood nasz weterynarz, może Tobie sprzedać leki, których nie potrzebuje dla zwierząt.");
				addJob("Opiekuję się schroniskiem i zaopiekuje się każdym porzuconym zwierzątkiem.");
				add(ConversationStates.ATTENDING,
					ConversationPhrases.HELP_MESSAGES,
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence,
											final Entity engine) {
							return player.hasPet();
						}
					}, 
					ConversationStates.SERVICE_OFFERED, "Czy przyprowadziłeś zwierzątko, które wymaga naszej opieki?",
					null);
				
				add(ConversationStates.SERVICE_OFFERED,
					ConversationPhrases.YES_MESSAGES, null,
					ConversationStates.ATTENDING, null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence,
										 final EventRaiser npc) {
							Pet pet = player.getPet();
							String petName = pet.getTitle();
							// these numbers are hardcoded, they're the area in the pet sanctuary which is for pets. It has food spawners.
							int x = Rand.randUniform(2, 12);
							int y = Rand.randUniform(7, 29);
							StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(ZONE_NAME);
							if (StendhalRPAction.placeat(zone, pet, x, y)) {
								player.removePet(pet);
								// reward with some karma but limit abuse
								if (player.getKarma() < 60.0) {
									player.addKarma(30.0);
								}
								npc.say("Dziękuję za uratowanie " + petName + ". Dobrze się nim zaopiekuję. Pamiętaj, że zawsze możesz wrócić "
								+ "i odwiedzić sanktuarium dla zwierząt kiedy zechcesz!");
								notifyWorldAboutChanges();
							} else {
								// there was no room for the pet
								npc.say("Wygląda na to, że nie mamy wolnego miejsca w naszym sanktuarium dla zwierząt! Mam nadzieję, że jeszcze możesz się rozglądnąć za " + petName + ".");  
							}
						}
					});

				add(ConversationStates.SERVICE_OFFERED,
					ConversationPhrases.NO_MESSAGES, null,
					ConversationStates.ATTENDING, "Och jak miło zobaczyć razem właściciela i jego szczęśliwe zwierzątko. Życzę wam powodzenia.", null);
				
				add(ConversationStates.ATTENDING,
					ConversationPhrases.HELP_MESSAGES,
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence,
											final Entity engine) {
							return !player.hasPet();
						}
					}, 
					ConversationStates.ATTENDING, "Jeżeli spotkasz spotkasz porzucone zwierzątko to proszę przyprowadź je do mnie.",
					null);

				addGoodbye("Dowidzenia!");
				
			}
			// remaining behaviour is defined in maps.quests.ZooFood.
		};

		npc.setEntityClass("woman_007_npc");
		npc.setPosition(41, 40);
		//npc.setDirection(Direction.DOWN);
		npc.setCollisionAction(CollisionAction.STOP);
		npc.initHP(100);
		npc.setDescription("Oto Katinka. Opiekuje się zwierzętami w tym Zoo.");
		zone.add(npc);
	}
}
