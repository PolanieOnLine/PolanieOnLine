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
package games.stendhal.server.maps.semos.city;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.actions.admin.AdministrationAction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.StoreMessageAction;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A crazy old man (original name: Diogenes) who walks around the city.
 */ 
public class RetireeNPC implements ZoneConfigurator {
	
	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Diogenes") {
			
			@Override
			public void createDialog() {
				addGreeting();
				addJob("Ha ha! Praca? Od dawna jestem na emeryturze po tym jak byłem #listonoszem! Ha ha!");
				addHelp("Nie mogę Ci pomóc, ale ty możesz pomóc PolskaGra mówiąc znajomym i pomagając w rozwoju gry! Wejdź na http://www.polskagra.net i zobacz jak możesz nam pomóc!");
				addGoodbye();
				addReply("listonoszem", "Powinienem dostarczyć wiadomości. Ale teraz jest nowy dzieciak, który się tym zajmuje. Wiesz co wyślę mu wiadomość, aby dostarcył ją tobie.", new StoreMessageAction("Diogenes", "Cześć to była miła rozmowa z tobą w Semos. Jeżeli chcesz skorzystać z listonosza, aby wysłać wiadomość innym, których teraz nie ma to powiedz /msg postman"));
				addOffer("Cóż cóż... Wciąż mógłbym zaopiekować się twoimi listami, ale jestem teraz na emeryturze i ktoś inny ma teraz moją posadę. Możesz odwiedzić tą osobę, nowego #listonosza na równinach Semos na północ stąd.");
				add(ConversationStates.ATTENDING,
						ConversationPhrases.QUEST_MESSAGES,
						null,
				        ConversationStates.ATTENDING,
				        null,
				        new ChatAction() {
					        @Override
					        public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						        if (Rand.throwCoin() == 1) {
					                npc.say("Ach, zadania... jak za starych dobrych czasów kiedy byłem młody! Pamiętam jedno zadanie było o... O zobacz, co tam leci? Hmm, co? Aha, zadania... jak za starych dobrych czasów kiedy byłem młody!");
				                } else {
				                 	npc.say("Wiesz, że Sato kupuje owce? Plotki mówią, że głęboko w podziemiach jest potwór, który także kupuje owce... w dodatku płaci lepiej niż Sato!");
				                }
			                }
		                });

		// A convenience function to make it easier for admins to test quests.
		add(ConversationStates.ATTENDING, Arrays.asList("cleanme!", "wyczyść mnie!"), null, ConversationStates.ATTENDING, "Co?",
		        new ChatAction() {
					        @Override
			        public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				        if (AdministrationAction.isPlayerAllowedToExecuteAdminCommand(player, "alter", false)) {
					        for (final String quest : player.getQuests()) {
						        player.removeQuest(quest);
					        }
				        } else {
					        npc.say("Co? Nie. Wyczyściłeś mnie! Dzięki.");
					        player.damage(5, npc.getEntity());
					        player.notifyWorldAboutChanges();
				        }
			        }
		        });
	}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(22, 43));
				nodes.add(new Node(25, 43));
				nodes.add(new Node(25, 45));
				nodes.add(new Node(31, 45));
				nodes.add(new Node(31, 43));
				nodes.add(new Node(35, 43));
				nodes.add(new Node(35, 29));
				nodes.add(new Node(22, 29));
				setPath(new FixedPath(nodes, true));
			}
			
		};
		npc.setPosition(24, 43);
		npc.setEntityClass("beggarnpc");
		npc.setDescription("Diogenes jest starszym mężczyzną, ale żwawym jak na swój wiek. Wygląda na przyjaznego i pomocnego.");
		npc.setSounds(Arrays.asList("laugh-1", "laugh-2"));
		zone.add(npc);
	}

}