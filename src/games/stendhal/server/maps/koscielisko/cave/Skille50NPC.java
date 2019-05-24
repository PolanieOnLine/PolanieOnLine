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
package games.stendhal.server.maps.koscielisko.cave;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

public class Skille50NPC implements ZoneConfigurator {
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
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Choros") {

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						String reply = "Witaj, ";
						if (player.getLevel() < 50) {
							reply += "po co do mnie tutaj przybyłeś? Jeszcze nie jesteś godzien, abym mógł Ciebie #'nauczyć' prawdziwej walki z potworami!";
						} else {
							reply += "widzę, że już zdobyłeś wystarczającą ilość doświadczenia poprzez walki z potworami, a zatem jesteś godzien przyjąć me nauki!";
						}
						raiser.say(reply);
					}
				});
				addJob("Jestem kapłanem, który chroni tę krainę przed złem.");
				addOffer("Moja jedyna oferta to gdy osiągniesz 50 poziom to nauczę Cię lepiej walczyć z potworami.");
				addReply("nauczyć",
						"Moje nauki to pradawna magia, która wspomaga podczas walk z potworami. Gdy osiągniesz 50 poziom, nauczę Cię jej.");
				addGoodbye();
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("50LVLReward")
						&& (player.getLevel() >= 50)) {
					player.setQuest("50LVLReward", "done");

					player.setAtkXP(45000 + player.getAtkXP());
					player.setDefXP(90000 + player.getDefXP());
					player.addXP(50000);

					player.incAtkXP();
					player.incDefXP();
				}

				if (!player.hasQuest("50LVLFirstChat")) {
					player.setQuest("50LVLFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
				
			}
			
		});

		npc.setEntityClass("blackwizardpriestnpc");
		npc.setDescription("Oto Choros. Jest Wysokim Kapłanem, który może Cię czegoś nauczyć.");
		npc.setPosition(38, 3);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
