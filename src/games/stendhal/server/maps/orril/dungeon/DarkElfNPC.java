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
package games.stendhal.server.maps.orril.dungeon;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

public class DarkElfNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildTunnelArea(zone);
	}

	private void buildTunnelArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Waerryna") {
			// name means deep and hidden hired mercenary according to http://www.angelfire.com/rpg2/vortexshadow/drownames.html
			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						String reply = "Jeżeli wybierasz się do tego ohydnego miasta szczurów to nie przychodź obok mnie. ";
						if (player.getLevel() < 60) {
							reply += " Po prostu nie idź tą drogą, bo nie przetrwasz mrocznych elfów... ";
						} else {
							reply += " Wschodnie przejście prowadzi do drow tunnels.";
						}
						raiser.say(reply);
					}
				});

				addJob("Pilnuję tych szczurów. My mroczne elfy nie lubimy, gdy ludzie szczury wtrącają się w nasze sprawy.");
				addHelp("Jeżeli chcesz zabić kilka tych wstrętnych ludzi szczurów to trzymaj się tej drogi aż do następnej dużej jaskini. Przejdź przez jaskinię i idź pomiędzy posągami czaszek, a znajdziesz miasto szczurów. Kieruj się tymi zwłokami, a będziesz wiedział, że jesteś na dobrej drodze.");
				addQuest("Jeżeli chcesz #pomóc ... to powiedz.");
				addGoodbye("Tak długo!");
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("WaerrynaFirstChat")) {
					player.setQuest("WaerrynaFirstChat", "done");
					player.addXP(300);
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setDescription("Oto potężna mroczna elfka Waerryna. Nie przechodź obok niej.");
		npc.setEntityClass("blackwizardpriestnpc");
		npc.setGender("F");
		npc.setPosition(49, 105);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(25);
		zone.add(npc);
	}
}
