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
package games.stendhal.server.maps.semos.mines;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.Map;

public class DwarfGuardianNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Phalk") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						String reply = "Tam jest coś wielkiego! Każdy jest zdenerwowany. ";
						if (player.getLevel() < 60) {
							reply += "Jesteś zbyt słaby, aby tam wejść. Gdy poczujesz się sina tyle mocny to popchnij #kamienie obok i udaj się w mroczne zakamarki kopalń...";
						} else {
							reply += "Bądź ostrożny! Aby wejść do mrocznych części kopalń popchnij #'kamienie', które leżą na przeciwko wejścia...";
						}
						raiser.say(reply);
					}
				});
				addReply(Arrays.asList("stone", "stones", "kamień", "kamienie"), "Możesz znaleść ich tony na terenie Faiumoni. Nie które nie są zbyt duże i możesz je z całą siła popchnąć...ale zgaduję, że wojownik taki jak ty nie bedzie miał z tym problemów.");
				addJob("Jestem krasnalem strażnikiem i staram się zawrócić śmiałków z drogi, jaką wybrał dla nich los.");
				addHelp("Uważaj, gdy będziesz biegł przez głębokie tunele kopalni w Semos. Czekają tam na Ciebie różne silne potwory! Potrzebujesz dobrego sprzętu, o oferty zapytaj Harolda w Tawernie w Semos, być może on będzie w stanie Ci pomóc...");
				addGoodbye();
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.LEFT);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("PhalkFirstChat")) {
					player.setQuest("PhalkFirstChat", "done");
					player.addXP(500);
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("dwarf_guardiannpc");
		npc.setDescription("Oto Phalk. Jest żołnierzem, który nie chce rezygnować z przygód, jakie zesłał mu los.");
		npc.setPosition(118, 26);
		npc.setDirection(Direction.LEFT);
		npc.initHP(25);
		zone.add(npc);
	}
}
