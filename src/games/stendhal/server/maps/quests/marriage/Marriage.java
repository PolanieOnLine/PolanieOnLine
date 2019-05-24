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
package games.stendhal.server.maps.quests.marriage;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.Area;

import java.awt.Rectangle;
import java.util.Arrays;

class Marriage {
	private final NPCList npcs = SingletonRepository.getNPCList();
	private MarriageQuestInfo marriage;

	private SpeakerNPC priest;

	private Player groom;
	private Player bride;

	public Marriage(final MarriageQuestInfo marriage) {
		this.marriage = marriage;
	}

	private void marriageStep() {

		/**
		 * Creates a priest NPC who can celebrate marriages between two players.
		 * 
		 * Note: in this class, the Player variables are called groom and bride.
		 * However, the game doesn't know the concept of genders. The player who
		 * initiates the wedding is just called groom, the other bride.
		 * 
		 * @author daniel
		 * 
		 */

		priest = npcs.get("Priest");
		priest.add(ConversationStates.ATTENDING,
					Arrays.asList("marry", "poślub"),
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence,
								final Entity npc) {
							return player.hasQuest(marriage.getQuestSlot())
									&& player.getQuest(marriage.getQuestSlot()).startsWith(
											"engaged")
									&& player.isEquipped("obrączka ślubna");
						}
					},
					// TODO: make sure the pair getting married are engaged to each
					// other, if this is desired.
					ConversationStates.ATTENDING, 
					null,
					new ChatAction() {

						@Override
						public void fire(final Player player, final Sentence sentence,
								final EventRaiser npc) {
							// find out whom the player wants to marry.
							final String brideName = sentence.getSubjectName();
	
							if (brideName == null) {
								npc.say("Powinieneś mi powiedzieć kogo chcesz poślubić.");
							} else {
								startMarriage((SpeakerNPC) npc.getEntity(), player, brideName);
							}
						}
					});

		priest.add(ConversationStates.QUESTION_1,
					ConversationPhrases.YES_MESSAGES, 
					null,
					ConversationStates.QUESTION_2, 
					null,
					new ChatAction() {

						@Override
						public void fire(final Player player, final Sentence sentence,
								final EventRaiser npc) {
							askBride();
						}
					});

		priest.add(ConversationStates.QUESTION_1,
					ConversationPhrases.NO_MESSAGES, 
					null, 
					ConversationStates.IDLE,
					"Co za szkoda! Dowidzenia!", 
					null);

		priest.add(ConversationStates.QUESTION_2,
					ConversationPhrases.YES_MESSAGES, 
					null,
					ConversationStates.ATTENDING, 
					null,
					new ChatAction() {

						@Override
						public void fire(final Player player, final Sentence sentence,
								final EventRaiser npc) {
							finishMarriage();
						}
					});

		priest.add(ConversationStates.QUESTION_2,
					ConversationPhrases.NO_MESSAGES, 
					null, 
					ConversationStates.IDLE,
					"Co za szkoda! Dowidzenia!", 
					null);

		// What he responds to marry if you haven't fulfilled all objectives
		// before hand
		priest.add(ConversationStates.ATTENDING,
					Arrays.asList("marry", "poślub"),
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence,
								final Entity npc) {
							return (!player.hasQuest(marriage.getQuestSlot()) 
									|| (player.hasQuest(marriage.getQuestSlot())	&& player.getQuest(marriage.getQuestSlot()).startsWith("engaged") && !player.isEquipped("obrączka ślubna")));
						}
					},
					ConversationStates.ATTENDING,
					"Nie jesteś jeszcze gotowy, aby wziąć ślub. Wróć, gdy się zaręczysz i przyniesiesz obrączkę ślubną. Spróbuj nie zostawiać swojego partnera z tyłu ...",
					null);

		// What he responds to marry if you are already married
		priest.add(ConversationStates.ATTENDING, 
				Arrays.asList("marry", "poślub"),
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence,
							final Entity npc) {
						return (player.isQuestCompleted(marriage.getQuestSlot()));
					}
				}, 
				ConversationStates.ATTENDING,
				"Jesteś już żonaty i nie możesz wziąć ślubu jeszcze raz.", 
				null);
	}
	
	private void startMarriage(final SpeakerNPC priest, final Player player,
			final String partnerName) {
		final StendhalRPZone churchZone = priest.getZone();
		final Area inFrontOfAltar = new Area(churchZone, new Rectangle(10, 9, 4, 1));

		groom = player;
		bride = SingletonRepository.getRuleProcessor().getPlayer(partnerName);

		if (!inFrontOfAltar.contains(groom)) {
			priest.say("Musisz stanąć przed ołtarzem jeżeli chcesz wziąć ślub.");
		} else if (marriage.isMarried(groom)) {
			priest.say("Jesteś już żonaty z " + groom.getName()
					+ "! Nie możesz jeszcze raz wziąć ślubu.");
		} else if ((bride == null) || !inFrontOfAltar.contains(bride)) {
			priest.say("Musisz przyprowadzić swojego partnera przed ołtarz, aby go poślubić.");
		} else if (bride.getName().equals(groom.getName())) {
			priest.say("Nie możesz wziąć ślubu ze sobą!");
		} else if (marriage.isMarried(bride)) {
			priest.say("Jesteś już żonaty " + bride.getName()
					+ "! Nie możesz jeszcze raz wziąć ślubu.");
		} else if (!bride.hasQuest(marriage.getQuestSlot())) {
			priest.say(bride.getName() + " nie jest zaręczony.");
		} else if (bride.hasQuest(marriage.getQuestSlot())
				&& !bride.getQuest(marriage.getQuestSlot()).startsWith("engaged")) {
			priest.say(bride.getName() + " nie jest zaręczony.");
		}  else if (!bride.isEquipped("obrączka ślubna")) {
			priest.say(bride.getName()
					+ " nie może dać tobie obrączki ślubnej.");
		} else {
			askGroom();
		}
	}

	private void askGroom() {
		priest.say(groom.getName() + " czy chcesz poślubić "
				+ bride.getName() + "?");
		priest.setCurrentState(ConversationStates.QUESTION_1);
	}

	private void askBride() {
		priest.say(bride.getName() + " czy chcesz poślubić "
				+ groom.getName() + "?");
		priest.setCurrentState(ConversationStates.QUESTION_2);
		priest.setAttending(bride);
	}

	private void finishMarriage() {
		exchangeRings();
		priest.say("Gratulacje "
				+ groom.getName()
				+ " i "
				+ bride.getName()
				+ " jesteście związani świętym węzłem małżeńskim! Nie popieram tego, ale jeżeli chcecie spędzić noc poślubną, to zapytajcie Lindy w hotelu. Powiedzcie jej 'honeymoon', a ona zrozumie.");
		// Memorize that the two married so that they can't just marry other
		// persons
		groom.setQuest(marriage.getSpouseQuestSlot(), bride.getName());
		bride.setQuest(marriage.getSpouseQuestSlot(), groom.getName());
		groom.setQuest(marriage.getQuestSlot(), "just_married");
		bride.setQuest(marriage.getQuestSlot(), "just_married");
		// Clear the variables so that other players can become groom and bride
		// later
		groom = null;
		bride = null;
	}

	private void giveRing(final Player player, final Player partner) {
		// players bring their own golden rings
		player.drop("obrączka ślubna");
		final Item ring = SingletonRepository.getEntityManager().getItem(
				"obrączka ślubna");
		ring.setInfoString(partner.getName());
		ring.setBoundTo(player.getName());
		player.equipOrPutOnGround(ring);
	}

	private void exchangeRings() {
		giveRing(groom, bride);
		giveRing(bride, groom);
	}

	public void addToWorld() {
		marriageStep();
	}

}
