/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityinterior.psychicparlor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.constants.SoundID;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.NPCEmoteAction;
import games.stendhal.server.entity.npc.action.PlaySoundAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
//import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.SoundEvent;

public class PsychicNPC implements ZoneConfigurator {
	private static SpeakerNPC psychic;

	private Creature requestedEnemy = null;
	private Integer currentFee = null;

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		initNPC(zone);
		initReading();
	}

	private void initNPC(final StendhalRPZone zone) {
		psychic = new SpeakerNPC("Lovena") {
			@Override
			protected void onGoodbye(final RPEntity attending) {
				say("Wróć ponownie " + attending.getName() + ".");
				addEvent(new SoundEvent("npc/goodbye_female-01", SoundLayer.CREATURE_NOISE));
			}
		};
		psychic.setEntityClass("wizardwomannpc");
		psychic.setGender("F");

		psychic.setPosition(26, 11);
		psychic.setIdleDirection(Direction.LEFT);

		psychic.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
						new PlaySoundAction("npc/hello_female-01"),
						new SayTextAction("Witaj w mojej sali medium. W czym mogę #pomóc?")));

		psychic.add(ConversationStates.ANY,
				ConversationPhrases.GOODBYE_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Wiedziałam, że zaraz pójdziesz. Do zobaczenia.",
				new PlaySoundAction("npc/goodbye_female-01"));

		psychic.addJob("Jestem medium. Chciałbyś #poczytać?");
		final String helpReply = "Mogę #odczytać twoją przeszłość bojową, o ile #zarejestrowali wrogów, których pokonałeś.";
		psychic.addHelp(helpReply);
		psychic.addOffer(helpReply);
		psychic.addQuest("Jedyne zadanie, jakie mam dla ciebie, to poszukiwanie wiedzy. Mogę zaoferować ci #czytanie twojej przeszłości.");
		psychic.addReply(
				Arrays.asList("record", "recorded", "recording", "zarejestrowali", "zarejestrowano", "zarejestrowanie"),
				"Niektórzy poszukiwacze przygód są na tyle mądrzy, aby prowadzić rejestr wrogów napotkanych w #bestiariuszu.");
		psychic.addReply(
				Arrays.asList("bestiary", "bestiariuszu"),
				"Jeśli nie jesteś zaznajomiony, poszukaj doświadczonego poszukiwacza przygód, który pokaże ci, jak prowadzić dziennik swoich wrogów.");

		zone.add(psychic);
	}

	private void initReading() {
		final EntityManager em = SingletonRepository.getEntityManager();

		final ChatCondition isValidCreatureCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				final String requested = sentence.getTrimmedText();
				if (em.isCreature(requested)) {
					requestedEnemy = em.getCreature(requested);
					return true;
				}

				return false;
			}
		};

		final ChatCondition hasKilledCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				if (requestedEnemy == null) {
					return false;
				}

				return player.hasKilled(requestedEnemy.getName());
			}
		};

		final ChatCondition hasFeeCondition = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
				if (requestedEnemy == null) {
					return false;
				}

				if (currentFee == null) {
					calculateFee(player);
				}

				return player.isEquipped("money", currentFee);
			}
		};

		final ChatAction sayFeeAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				if (requestedEnemy == null) {
					npc.say("Hmmm, moje umiejętności wróżenia są pochmurne. Nie mogę ustalić informacji na temat tego wroga.");
					npc.setCurrentState(ConversationStates.ATTENDING);
					return;
				}

				if (currentFee == null) {
					calculateFee(player);
				}

				npc.say("Informacja o " + requestedEnemy.getName() + " będzie kosztować " + currentFee + ". Czy " + player.getGenderVerb("chciałbyś") + " zapłacić?");
			}
		};

		final ChatAction sayKillsAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (requestedEnemy == null) {
					psychic.say("Hmmm, moje umiejętności wróżenia są pochmurne. Nie mogę ustalić informacji na temat tego wroga.");
					psychic.setCurrentState(ConversationStates.ATTENDING);
					return;
				}

				if (currentFee == null) {
					calculateFee(player);
				}

				player.drop("money", currentFee);
				player.addEvent(new SoundEvent(SoundID.COMMERCE, SoundLayer.CREATURE_NOISE));

				final String enemyName = requestedEnemy.getName();
				final String enemyNamePlural = Grammar.pluralCreature(enemyName);
				final int soloKills = player.getSoloKill(enemyName);
				final int sharedKills = player.getSharedKill(enemyName);
				final int totalKills = soloKills + sharedKills;

				final StringBuilder sb = new StringBuilder(player.getName() + ", " + player.getGenderVerb("zabiłeś") + " w sumie " + totalKills + " ");

				if (totalKills == 1) {
					sb.append(enemyName);
				} else {
					sb.append(enemyNamePlural);
				}

				sb.append(". ");
				if (soloKills == 0) {
					sb.append("Nie " + player.getGenderVerb("pokonałeś") + " ani jednego");
				} else {
					sb.append(player.getGenderVerb("Pokonałeś") + " " + soloKills);
				}
				sb.append(" w pojedynkę oraz ");

				if (sharedKills == 0) {
					sb.append("ani jednego");
				} else {
					sb.append(sharedKills);
				}
				sb.append(" w grupie.");

				psychic.say("Dziękuję.");
				new NPCEmoteAction("kładzie rękę na bestiariuszu i wpatruje się w kryształową kulę.", false).fire(null, null, raiser);
				psychic.setCurrentState(ConversationStates.BUSY);
				new PlaySoundAction("npc/mm_hmm_female-01").fire(null, null, raiser);
				psychic.setDirection(Direction.LEFT); // face crystal ball

				// create a pause before the NPC replies
				TurnNotifier.get().notifyInSeconds(6, new TurnListener() {
					@Override
					public void onTurnReached(final int currentTurn) {
						/* FIXME: There is a bit of strange behavior if player walks away before receiving
						 *        response as another player can request reading.
						 */
						if (psychic.inConversationRange()) {
							psychic.setCurrentState(ConversationStates.ATTENDING);
						}
						psychic.say(sb.toString());
					}
				});

				// reset requested enemy & fee
				requestedEnemy = null;
				currentFee = null;
			}
		};

		final List<String> readPhrases = Arrays.asList("read", "reading", "odczytać", "czytać", "czytanie", "poczytać", "przeczytać");
		/**
		// player is not carrying a bestiary
		psychic.add(ConversationStates.ATTENDING,
				readPhrases,
				new NotCondition(new PlayerHasItemWithHimCondition("bestiariusz")),
				ConversationStates.ATTENDING,
				"Nie mogę odczytać twojej przeszłości, chyba że masz nagranie # wrogów, z którymi się spotkałeś.",
				null);
		*/

		psychic.add(ConversationStates.ATTENDING,
				readPhrases,
				//new PlayerHasItemWithHimCondition("bestiariusz"),
				ConversationStates.QUESTION_1,
				"Widzę, że nosisz bestiariusz. O którym wrogu chciałbyś uzyskać informacje?",
				null);

		psychic.add(ConversationStates.QUESTION_1,
				"",
				new NotCondition(isValidCreatureCondition),
				ConversationStates.ATTENDING,
				"Przepraszam, nie znam tego stworzenia. Czy mogę jeszcze w czymś pomóc?",
				null);

		psychic.add(ConversationStates.QUESTION_1,
				"",
				new AndCondition(
						isValidCreatureCondition,
						new NotCondition(hasKilledCondition)),
				ConversationStates.ATTENDING,
				"Wygląda na to, że jeszcze nie spotkałeś takiego stworzenia.",
				null);

		psychic.add(ConversationStates.QUESTION_1,
				"",
				new AndCondition(
						isValidCreatureCondition,
						hasKilledCondition),
				ConversationStates.QUESTION_2,
				null,
				sayFeeAction);

		psychic.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"W porządku. Czy mogę jeszcze w czymś pomóc?",
				null);

		psychic.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(hasFeeCondition),
				ConversationStates.ATTENDING,
				"Przepraszam, nie masz dość pieniędzy, aby uiścić opłatę.",
				null);

		psychic.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				hasFeeCondition,
				null,
				null,
				sayKillsAction);
	}

	/**
	 * Calculates the fee for receiving a reading based on player & requested
	 * creature levels.
	 *
	 * @param player
	 * 		The player requesting the reading.
	 */
	private void calculateFee(final Player player) {
		currentFee = player.getLevel() * 10 + requestedEnemy.getLevel() * 15;
	}
}
