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
package games.stendhal.server.maps.magic.house1;

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
public class ChallengerNPC implements ZoneConfigurator  {

 private static final int MINUTES_IN_DAYS = 24 * 60;
	/** how many creatures will be spawned.*/
 private static final int NUMBER_OF_CREATURES = 5;
 /** lowest level allowed to island.*/
 private static final int MIN_LEVEL = 50;
 /** Cost multiplier for getting to island. */
 private static final int COST_FACTOR = 300;
  /** How long to wait before visiting island again. */
 private static final int DAYS_BEFORE_REPEAT = 3;
 /** The name of the quest slot where we store the time last visited. */
 private static final String QUEST_SLOT = "adventure_island";
 
 private static final Logger logger = Logger.getLogger(ChallengerNPC.class);

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
					.getRPWorld().getRPZone("int_adventure_island");
			String zoneName = player.getName() + "_adventure_island";
			
			final AdventureIsland zone = new AdventureIsland(zoneName, challengezone, player);

			// add a colour to the zone
			ZoneAttributes attr = new ZoneAttributes(zone);
			attr.setBaseName("int_adventure_island");
			attr.put("color_method", "softlight");
			attr.put("color", Integer.toString(0x550088));
			zone.setAttributes(attr);
			
			SingletonRepository.getRPWorld().addRPZone(zone);

			// timestamp the quest slot so that we know if it should be repeated
			player.setQuest(QUEST_SLOT, Long.toString(System.currentTimeMillis()));
			
			// move the player
			player.teleport(zone, 4, 4, Direction.DOWN, player);
			
			// prepare the message to tell the player cost and what happened
			String message;
			int numCreatures = zone.getCreatures(); 
			if (zone.getCreatures() < AdventureIsland.NUMBER_OF_CREATURES) {
				// if we didn't manage to spawn NUMBER_OF_CREATURES they get a reduction
				cost =  (int) (cost * ((float) numCreatures / (float) NUMBER_OF_CREATURES));
				message = "Haastaja: Mogę postawić dla Ciebie tylko " + numCreatures + " potworów na wyspie. Dlatego masz mniej, a #opłata wynosi tylko " + cost + " money. Powodzenia.";
				logger.info("Tried too many times to place creatures in adventure island so less than the required number have been spawned");
			} else { 
				message = "Haastaja: Wziąłem opłatę w wysokości " + cost + " money. Powodzenia.";
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
		final SpeakerNPC npc = new SpeakerNPC("Haastaja") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			public void createDialog() {
				addGreeting("I tak przybył bohater.");
				addQuest("Powiedz #opłata i #walcz z wytrenowanymi przeze mnie magicznymi potworami na magicznej #wyspie. Będzie w sumie " + NUMBER_OF_CREATURES 
						+ " potworów odpowiednich dla Twojego poziomu.");
				addHelp("Jeżeli jesteś wystarczająco silny i zapłacisz #opłatę to możesz #walczyć z " + NUMBER_OF_CREATURES 
						+ " moimi zwierzętami na prywatnej #wyspie przygód.");
				addJob("Trenuje magiczne potwory do walki i oferuję wojownikom szansę #walki z nimi na magicznej #wyspie.");
				addOffer("Aby zostać teleportowanym na #wyspę i walczyć przeciwko " + NUMBER_OF_CREATURES + " wytrenowanym przeze mnie potworami dobranym do Twojego poziomu musisz podjąć #wyzwanie.");
				addReply(Arrays.asList("island", "wyspę", "wyspie"), "Mogę przywołać magiczną wyspę tylko dla Ciebie. Będzie potrzymywana przez twoje siły życiowe. Jeśli opuścisz ją tu musisz szybko wrócić na nią, bo inaczej " 
						+ "zniknie. Nie powinieneś próbować uciekać lub wracać więcej niż jeden raz. Jeżeli chcesz się tam dostać to po prostu zapłać #opłatę.");
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
								"Akceptuję Twoje wyzwanie. Jeżeli opłacisz #opłatę to przywołam dla Ciebie na wyspie " + NUMBER_OF_CREATURES 
								+ " niebezpiecznych potworów. Jesteś pewien, że chcesz się dostać na wyspę przygód?", 
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
						new SayTimeRemainingAction(QUEST_SLOT, DAYS_BEFORE_REPEAT * MINUTES_IN_DAYS, "Twoja siła życiowa nie wróci tak szybko od ostatniej wizyty na wyspie. Będziesz gotowy za"));

				// player returns within DAYS_BEFORE_REPEAT days, if the zone still exists that he was in before, send him straight up. 
				add(ConversationStates.ANY, 
						Arrays.asList("challenge", "fight", "battle", "fee", "wyzwanie", "walka", "walki", "opłata", "opłatę", "wyzwanie"),
						new AndCondition(
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, DAYS_BEFORE_REPEAT * MINUTES_IN_DAYS)), 
								 new AdventureZoneExistsCondition()
						),
						ConversationStates.QUESTION_1, 
						"Wyspa, która została przywołana dla Ciebie wciąż czeka na Twoją wizytę bez dodatkowych kosztów. Czy chcesz na nią powrócić?", 
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
						final String zoneName = player.getName() + "_adventure_island";
						final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
						player.teleport(zone, 4, 4, Direction.DOWN, null);
						player.notifyWorldAboutChanges();
					}
				});
				
				// player wished to return to an existing adventure island but it's now gone
				add(ConversationStates.QUESTION_1, 
						ConversationPhrases.YES_MESSAGES, 
						// check again it does exist
						new NotCondition(new AdventureZoneExistsCondition()),
						ConversationStates.ATTENDING,
						"Przepraszam, ale wyspa znikła w czasie, gdy zastanawiałeś się nad powiedzeniem 'tak'. Nie możesz jej teraz odwiedzić.",
						null);


				// player declined to return to an existing adventure island
				add(ConversationStates.QUESTION_1, 
						ConversationPhrases.NO_MESSAGES, 
						null, 
						ConversationStates.ATTENDING, 
						"Bardzo dobrze.", 
						null);
			}};
			npc.setPosition(14, 4);
			npc.setEntityClass("chaos_sorcerornpc");
			npc.setDirection(Direction.DOWN);
			npc.setDescription("Oto Haastaja Challenger. Jest potężnym czarownikiem chaosu.");
			npc.setLevel(600);
			npc.initHP(75);
			zone.add(npc);		
	}

	// Not made as an entity.npc.condition. file because the zone name depends on player here. 
	private static final class AdventureZoneExistsCondition implements ChatCondition {
		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
			final String zoneName = player.getName() + "_adventure_island";
			final IRPZone.ID zoneid = new IRPZone.ID(zoneName);
			return SingletonRepository.getRPWorld().hasRPZone(zoneid);
		}
	}
}
