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
package games.stendhal.server.maps.nalwor.hell;

import games.stendhal.common.Direction;
import games.stendhal.common.NotificationType;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the 2nd reaper in hell.
 * @author kymara
 */
public class Reaper2NPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

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
		SpeakerNPC npc = createNPC("repaeR mirG");
		npc.setPosition(68, 76);
		zone.add(npc);
	}
	
	static SpeakerNPC createNPC(String name) {
		final SpeakerNPC npc = new SpeakerNPC(name) {

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("#ękdagaz ćaząiwzor zsisum ot ecsjeim ot ćicśupo zsechc ileżeJ");
				add(ConversationStates.ATTENDING, Arrays.asList("evael", "ćicśupo", "ćśjyw"), null, ConversationStates.QUESTION_1, "?neiwep śetseJ .adokzs eizdęb oT", null);
				final List<ChatAction> processStep = new LinkedList<ChatAction>();
				processStep.add(new TeleportAction("int_afterlife", 31, 23, Direction.UP));
				processStep.add(new DecreaseKarmaAction(100.0));
				processStep.add(new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						player.subXP(10000);
						// wipe riddle slot if player decided they couldnt answer it and would take the karma hit
						if (player.hasQuest("solve_riddles")) {
							player.removeQuest("solve_riddles");
						}
						player.sendPrivateText(NotificationType.NEGATIVE, "Reaper zabrał Tobie 10000 punktów doświadczenia oraz obdarzył Cię złą karmą.");
					}
				});
				add(ConversationStates.QUESTION_1, Arrays.asList("yes", "sey", "ok", "ko", "tak", "kat", "dobrze", "ezrbod"), null, ConversationStates.IDLE, "!ahahahaH", new MultipleActions(processStep));
				add(ConversationStates.QUESTION_1, Arrays.asList(ConversationPhrases.NO_EXPRESSION, "on", "ein"), null, ConversationStates.ATTENDING, ".ezrboD", null);
				addReply(Arrays.asList("ękdagaz", "elddir"), ".ortsul ejom jatypaZ");
				addJob(".hcycąjyż ezsud mareibaZ");
				addHelp("ćśjyw# eibos zsyzcyŻ .ałkeip marb od ezculk mamyzrT");
				addOffer("... ęzsud ąjowt łąizw myba ,eibos zsyzcyżaz ein ikópoD");
				addGoodbye("... ćęimapein w łdezsdo yzcezr kedązrop yratS");
			}
		};
		npc.setEntityClass("grim_reaper2_npc");
		npc.setPosition(68, 76);
		npc.initHP(100);
		npc.setDescription("Oto repaeR mirG. Jego lustro daje wolność.");
		return npc;
	}
}
