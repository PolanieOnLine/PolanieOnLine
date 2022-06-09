/***************************************************************************
 *                 (C) Copyright 2019-2022 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.tarnow.city;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.PlaySoundAction;
import games.stendhal.server.entity.npc.behaviour.impl.MonologueBehaviour;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.player.Player;

/**
 * @author KarajuSs
 */
public class MageNPC implements ZoneConfigurator {
	private static int currentFee = 1000;
	private static final int exchangeValue = currentFee / 2;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String[] yells = {
			"Halooo!! Jest tu ktoś?!",
			"Hej!!! Halo!!! Moja oferta może zainteresować!"
		};
		new MonologueBehaviour(buildNPC(zone), yells, 3);
	}

	private SpeakerNPC buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Częstogoj") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj! Może zainteresuje cię pewna #wymiana?");
				addJob("Nie mam aktualnie żadnego wyzwania dla Ciebie.");
				addHelp("Nie potrzebuję pomocy. Możemy jedynie dokonać pewnej #wymiany.");
				addOffer("Możemy wykonać #wymianę na magię lodu.");
				addReply(Arrays.asList("exchange","wymiana"),
						"Wymienię się magią lodu w zamian za magię ziemi. Zainteresowany?");
				addGoodbye();

				add(ConversationStates.ATTENDING,
					ConversationPhrases.YES_MESSAGES,
					new NotCondition(hasFeeCondition),
					ConversationStates.IDLE,
					"Minimalnie mogę wymienić się za 1000 magii ziemi!",
					null
				);

				add(ConversationStates.ATTENDING,
					ConversationPhrases.YES_MESSAGES,
					hasFeeCondition,
					ConversationStates.ATTENDING,
					"Dobrze! Proszę... oto twoje zaklęcia lodu, połowa wartości zaklęć ziemi!",
                	new MultipleActions(
                		new DropItemAction("magia ziemi", currentFee),
                		new EquipItemAction("magia lodu", exchangeValue),
                		new PlaySoundAction("coins-01")
                	)
			 	);

				add(ConversationStates.ATTENDING,
					ConversationPhrases.NO_MESSAGES,
					ConversationStates.IDLE,
					"To może innym razem się wymienimy!",
					null
				);
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("Oto Częstogoj. Może ma coś do zaoferowania.");
		npc.setEntityClass("wisemannpc");
		npc.setGender("M");
		npc.setPosition(117, 68);

		final Sign sign = new Sign();
		sign.setPosition(114, 68);
		sign.setText(" -- WYMIANIA -- \n 1000 magia ziemi —— 500 magia lodu\n\nCzęstogoj napisał na koniec:\nPolecam! To jest bardzo dobra oferta!!");
		sign.setEntityClass("default");

		zone.add(sign);
		zone.add(npc);

		return npc;
	}

	final ChatCondition hasFeeCondition = new ChatCondition() {
		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			return player.isEquipped("magia ziemi", currentFee);
		}
	};
}
