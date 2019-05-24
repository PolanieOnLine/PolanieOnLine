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
package games.stendhal.server.maps.wieliczka.mines;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

public class Skille550NPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildMineArea(zone);
	}

	private void buildMineArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Deviotis") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(118, 24));
				nodes.add(new Node(118, 20));
				nodes.add(new Node(117, 20));
				nodes.add(new Node(117, 19));
				setPath(new FixedPath(nodes, true));
			}
			
			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						String reply = "Witaj, ";
						if (player.getLevel() < 550) {
							reply += "po co do mnie tutaj przybyłeś? Jeszcze nie jesteś godzien, abym mógł Ciebie #'nauczyć' prawdziwej walki z potworami!";
						} else {
							reply += "widzę, że już zdobyłeś wystarczającą ilość doświadczenia poprzez walki z potworami, a zatem jesteś godzien przyjąć me nauki!";
						}
						raiser.say(reply);
					}
				});
				addJob("Jestem kapłanem, który chroni tę krainę przed złem.");
				addOffer("Moja jedyna oferta to gdy osiągniesz 550 poziom to nauczę Cię lepiej walczyć z potworami.");
				addReply("nauczyć",
						"Moje nauki to pradawna magia, która wspomaga podczas walk z potworami. Gdy osiągniesz 550 poziom, nauczę Cię jej.");
				addGoodbye();
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("DeviotisReward")
						&& (player.getLevel() >= 550)) {
					player.setQuest("DeviotisReward", "done");

					player.setAtkXP(1750000 + player.getAtkXP());
					player.setDefXP(2750000 + player.getDefXP());
					player.addXP(550000);

					player.incAtkXP();
					player.incDefXP();
				}

				if (!player.hasQuest("DeviotisFirstChat")) {
					player.setQuest("DeviotisFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
				
			}
			
		});

		npc.setEntityClass("blackwizardpriestnpc");
		npc.setPosition(118, 24);
		zone.add(npc);
	}
}
