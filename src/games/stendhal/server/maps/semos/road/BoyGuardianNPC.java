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
package games.stendhal.server.maps.semos.road;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasShieldEquippedCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;

import java.util.Arrays;
import java.util.Map;

public class BoyGuardianNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Will") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				
				String greetingBasis = "Hej, ty! Trzymaj się. Właśnie opuszczasz miasto! ";
				
				// When the players level is below 15 AND (he has a shield equipped OR he completed the "meet_hayunn" quest)
				add(
						ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
								new LevelLessThanCondition(15),
								new OrCondition(
										new PlayerHasShieldEquippedCondition(),
										new QuestCompletedCondition("meet_hayunn")
								)
						),
						ConversationStates.ATTENDING,
						greetingBasis + "Uważaj na zwierzęta, które mogą cię zaatakować i innych wrogów, którzy kręcą sięw pobliżu. Lepiej weź ze sobą coś do jedzenia i picia ze sobą! ",
						null);
				// When the players level is below 15 AND he has NO shield AND he has NOT completed the "meet_hayunn" quest
				add(
						ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
								new LevelLessThanCondition(15),
								new NotCondition(new PlayerHasShieldEquippedCondition()),
								new NotCondition(new QuestCompletedCondition("meet_hayunn"))
						),
						ConversationStates.ATTENDING,
						greetingBasis + "Coo! Nie masz nawet tarczy. Wróć i porozmawiaj z Hayunn w starym domku strażników w wiosce Semos nim wpadniesz w tarapaty.",
						null);
				// When the player is above level 15
				add(
						ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new LevelGreaterThanCondition(15),
						ConversationStates.ATTENDING,
						greetingBasis + "Oh teraz widzę jesteś silny i odważny! Miłej zabawy :)",
						null);
				
				addJob("Moją pracą jest obserwowanie złych potworów! Moi rodzice przydzielili mi tą specjalną #służbę!");
				addReply(Arrays.asList("duty", "służbę"), "Tak jest specjalna i ważna!");
				addHelp("Mój ojciec zawsze mówi, abym #skradał się po lesie, którego nie znam... Mówił też, że powinienem mieć zawsze coś do #jedzenia i #picia, aby być bezpieczny!");
				addReply(Arrays.asList("sneak", "skradał", "skradał"), "Tak jeżeli chcesz być wojownikiem takim jakim ja chcę być to musisz pracować nad cichym poruszaniem!");
				addReply(Arrays.asList("eat","drink","eat and drink", "jedzenia", "picia", "jedzenia i picia"), "Leander piekarz Semos robi pyszne kanapki. Zawsze je kupuję, a jego chleb jest też pyszny! Co do najpojów to nie jestem pewien. Może zapytaj #Carmen lub #Margaret?");
				addReply("Carmen", "Jest znaną uzdrowicielką w mieście Semos. Może widziałeś ją przy drodze do wioski :)");
				addReply("Margaret", "Pracuje w tawernie, ale nie chodzę tam bez moich rodziców...");
				addQuest("Jestem na misji :) Mam oko na złe potwory, złych typków i ostrzegam ludzi, aby im #pomóc! Ale nic nie mam dla ciebie...");
				addGoodbye("Ciii nie mów zbyt głośno! Dowidzenia i trzymaj się!");
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("WillFirstChat")) {
					player.setQuest("WillFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("boyguardnpc");
		npc.setDescription("Oto Will. W przyszłości chce być zawodowym śtrażnikiem miejskim.");
		npc.setPosition(6, 43);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		npc.setPerceptionRange(4);
		zone.add(npc);
	}
}
