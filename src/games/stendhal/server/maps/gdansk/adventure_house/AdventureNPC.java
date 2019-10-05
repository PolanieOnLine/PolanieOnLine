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
package games.stendhal.server.maps.pol.gdansk.adventure_house;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.ZoneAttributes;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.Map;

import marauroa.common.game.IRPZone;

import org.apache.log4j.Logger;

/**
 * The Chaos Sorcerer controlling entry to the adventure island
 *
 * @author kymara
 */
public class AdventureNPC implements ZoneConfigurator  {

 private static final int MINUTES_IN_DAYS = 24 * 60;
 private static final int NUMBER_OF_CREATURES = 40;
 private static final int MIN_LEVEL = 35;
 private static final int COST_FACTOR = 850;
 private static final int DAYS_BEFORE_REPEAT = 7;
 private static final String QUEST_SLOT = "adventure_cave";

 private static final Logger logger = Logger.getLogger(AdventureNPC.class);

	private static final class ChallengeChatAction implements ChatAction {

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			int cost = COST_FACTOR * player.getLevel();
			// check the money but take it later
			if (!player.isEquipped("money", cost)) {
				npc.say("Nie masz wystarczająco dużo money. Opłata wymagana na Twój poziom wynosi " + cost + " money.");
				npc.setCurrentState(ConversationStates.ATTENDING);
				return;
			}
			// now set up the new zone
			final StendhalRPZone challengezone = (StendhalRPZone) SingletonRepository
					.getRPWorld().getRPZone("int_adventure_cave");
			String zoneName = player.getName() + "_adventure_cave";

			final AdventureCave zone = new AdventureCave(zoneName, challengezone, player);

			// add a colour to the zone
			ZoneAttributes attr = new ZoneAttributes(zone);
			attr.setBaseName("int_adventure_cave");
			attr.put("color_method", "softlight");
			attr.put("color", Integer.toString(0x550088));
			zone.setAttributes(attr);

			SingletonRepository.getRPWorld().addRPZone(zone);

			// timestamp the quest slot so that we know if it should be repeated
			player.setQuest(QUEST_SLOT, Long.toString(System.currentTimeMillis()));

			// move the player
			player.teleport(zone, 57, 19, Direction.RIGHT, player);

			// prepare the message to tell the player cost and what happened
			String message;
			int numCreatures = zone.getCreatures();
			if (zone.getCreatures() < AdventureCave.NUMBER_OF_CREATURES) {
				// if we didn't manage to spawn NUMBER_OF_CREATURES they get a reduction
				cost =  (int) (cost * ((float) numCreatures / (float) NUMBER_OF_CREATURES));
				message = "Gereon: Mogę postawić dla Ciebie maksymalnie tylko " + numCreatures + " potworów w jaskini. Dlatego masz mniej, a #opłata wynosi tylko " + cost + " money. Powodzenia.";
				logger.info("Tried too many times to place creatures in adventure island so less than the required number have been spawned");
			} else {
				message = "Gereon: Wziąłem opłatę w wysokości " + cost + " money. Powodzenia.";
			}
		    // take the money
			player.drop("money", cost);
			// talk to the player
			player.sendPrivateText(message);

			player.notifyWorldAboutChanges();
		}
	}

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Gereon") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			public void createDialog() {
				addGreeting("Witaj młody podróżniku.");
				addQuest("Powiedz #opłata i #walcz z strasznymi potworami na równym sobie poziomie w ciemnej #jaskini.");
				addHelp("Jeżeli jesteś wystarczająco silny i zapłacisz #opłatę to możesz #walczyć maksymalnie z " + NUMBER_OF_CREATURES
						+ " potworami w swej prywatnej #jaskini.");
				addJob("Szukamy dzielnych wojowników oraz dajemy im szansę #walki z potworami równych sobie w #jaskini.");
				addOffer("Aby zostać teleportowanym do #jaskini i walczyć przeciwko " + NUMBER_OF_CREATURES + " strasznych potworów dobranym do Twojego poziomu musisz podjąć #wyzwanie.");
				addReply(Arrays.asList("cave", "jakinię", "jaskinia", "jaskini"), "W jaskini znajdują się odpowiednio dobrane potwory do Twojego aktualnego poziomu."
						+ "Nie powinieneś próbować uciekać lub wracać więcej niż jeden raz. Jeżeli chcesz się tam dostać to po prostu zapłać #opłatę.");
				addGoodbye("Dowidzenia.");
				// fee depends on the level
				// but there is a min level to play
				add(ConversationStates.ANY,
						Arrays.asList("fee", "opłata", "opłatę"),
						new AndCondition(new LevelGreaterThanCondition(MIN_LEVEL - 1),
								new TimePassedCondition(QUEST_SLOT, DAYS_BEFORE_REPEAT * MINUTES_IN_DAYS)),
								ConversationStates.QUEST_OFFERED,
								null,
								new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						npc.say("Opłatę oblicza się przez pomnożenie Twojego aktualnego poziomu przez " + COST_FACTOR + ", płacona jest w gotówce. Według Twojego poziomu "
								+ player.getLevel() + " opłata będzie wynosić " + COST_FACTOR * player.getLevel() + " money. Czy chcesz walczyć?");
					}
				});

				// player meets conditions, first remind them of the dangers and wait for a 'yes'
				add(ConversationStates.ANY,
						Arrays.asList("challenge", "fight", "battle", "walka", "walki", "wyzwanie"),
						new AndCondition(new LevelGreaterThanCondition(MIN_LEVEL - 1),
								 new TimePassedCondition(QUEST_SLOT, DAYS_BEFORE_REPEAT * MINUTES_IN_DAYS)),
								ConversationStates.QUEST_OFFERED,
								"Akceptuję Twoje wyzwanie. Jeżeli opłacisz #opłatę to przeteleportuję Ciebie do jaskini z " + NUMBER_OF_CREATURES
								+ " niebezpiecznymi potworami. Jesteś pewien, że chcesz się dostać do jaskini?",
								null);
				// player returns within DAYS_BEFORE_REPEAT days, and his island has expired
				add(ConversationStates.ANY,
						Arrays.asList("challenge", "fight", "battle", "fee", "walka", "walki", "opłata", "opłatę", "wyzwanie"),
						new AndCondition(
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, DAYS_BEFORE_REPEAT * MINUTES_IN_DAYS)),
								 new NotCondition(new AdventureZoneExistsCondition())
						),
						ConversationStates.ATTENDING,
						null,
						new SayTimeRemainingAction(QUEST_SLOT, DAYS_BEFORE_REPEAT * MINUTES_IN_DAYS, "Nie możemy aktualnie znaleźć odpowiednich potworów na Twój poziom. Proszę wróć za"));

				// player returns within DAYS_BEFORE_REPEAT days, if the zone still exists that he was in before, send him straight up.
				add(ConversationStates.ANY,
						Arrays.asList("challenge", "fight", "battle", "fee", "wyzwanie", "walka", "walki", "opłata", "opłatę", "wyzwanie"),
						new AndCondition(
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, DAYS_BEFORE_REPEAT * MINUTES_IN_DAYS)),
								 new AdventureZoneExistsCondition()
						),
						ConversationStates.QUESTION_1,
						"Jaskinia wciąż czeka na Twoją wizytę bez dodatkowych kosztów. Czy chcesz do niej powrócić?",
						null);

				// player below MIN_LEVEL
				add(ConversationStates.ANY,
						Arrays.asList("challenge", "fight", "battle", "fee", "wyzwanie", "walka", "walki", "opłata", "opłatę", "wyzwanie"),
						new LevelLessThanCondition(MIN_LEVEL),
						ConversationStates.ATTENDING,
						"Jesteś zbyt słaby, aby walczyć przeciwko " + NUMBER_OF_CREATURES  + " na raz. Wróć, gdy osiągniesz co najmniej " + MIN_LEVEL + " poziom.",
						null);
				// all conditions are met and player says yes he wants to fight
				add(ConversationStates.QUEST_OFFERED,
						ConversationPhrases.YES_MESSAGES,
						new LevelGreaterThanCondition(MIN_LEVEL - 1),
						ConversationStates.IDLE,
						null,
						new ChallengeChatAction());
				// player was reminded of dangers and he doesn't want to fight
				add(ConversationStates.QUEST_OFFERED,
						ConversationPhrases.NO_MESSAGES,
						null,
						ConversationStates.ATTENDING,
						"W porządku.",
						null);

				// player wishes to return to an existing adventure island
				add(ConversationStates.QUESTION_1,
						ConversationPhrases.YES_MESSAGES,
						// check again it does exist
						new AdventureZoneExistsCondition(),
						ConversationStates.IDLE,
						null,
						new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String zoneName = player.getName() + "_adventure_cave";
						final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
						player.teleport(zone, 58, 19, Direction.LEFT, null);
						player.notifyWorldAboutChanges();
					}
				});

				// player wished to return to an existing adventure island but it's now gone
				add(ConversationStates.QUESTION_1,
						ConversationPhrases.YES_MESSAGES,
						// check again it does exist
						new NotCondition(new AdventureZoneExistsCondition()),
						ConversationStates.ATTENDING,
						"Przepraszam, ale nie mogę ponownie teleportować Ciebie do jaskini.",
						null);


				// player declined to return to an existing adventure island
				add(ConversationStates.QUESTION_1,
						ConversationPhrases.NO_MESSAGES,
						null,
						ConversationStates.ATTENDING,
						"Bardzo dobrze.",
						null);
			}};
			npc.setPosition(5, 14);
			npc.setEntityClass("brownwizardnpc");
			npc.setDirection(Direction.DOWN);
			npc.setDescription("Oto Gereon. Pilnuje razem ze strażnikami wyjścia, aby potwory nie wyszły na zewnątrz.");
			npc.setLevel(600);
			npc.initHP(75);
			zone.add(npc);
	}

	// Not made as an entity.npc.condition. file because the zone name depends on player here.
	private static final class AdventureZoneExistsCondition implements ChatCondition {
		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
			final String zoneName = player.getName() + "_adventure_cave";
			final IRPZone.ID zoneid = new IRPZone.ID(zoneName);
			return SingletonRepository.getRPWorld().hasRPZone(zoneid);
		}
	}
}
