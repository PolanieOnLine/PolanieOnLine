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
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.Area;

import java.awt.Rectangle;
import java.util.Arrays;

class Engagement {
	private MarriageQuestInfo marriage;
	
	private final NPCList npcs = SingletonRepository.getNPCList();
	private SpeakerNPC nun;

	private Player groom;
	private Player bride;
	
	public Engagement(final MarriageQuestInfo marriage) {
		this.marriage = marriage;
	}
	
	private void engagementStep() {
		nun = npcs.get("Sister Benedicta");
		nun.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				null,
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
							final EventRaiser raiser) {
						if (!player.hasQuest(marriage.getQuestSlot())) {
							raiser.say("Największe zadanie w ciągu całego życia to #małżeństwo.");
						} else if (player.isQuestCompleted(marriage.getQuestSlot())) {
							raiser.say("Mam nadzieje, że jesteś zadowolony z małżeństwa.");
						} else {
							raiser.say("Nie zorganizowałeś jeszcze ślubu?");
						}
					}
				});

		nun.add(ConversationStates.ATTENDING,
				Arrays.asList("married", "małżeństwo"),
				null,
				ConversationStates.ATTENDING,
				"Jeżeli masz partnera to możesz z nim wziąć #ślub. Gdy będziecie małżeństwem "
				+ "to będziecie mogli używać wedding ring, aby być razem. Moc pierścienia bierze się z "
				+ "więzi pomiędzy parą i działa najlepiej, gdy para jest #razem.",
				null);

		nun.add(ConversationStates.ATTENDING,
				Arrays.asList("equals", "razem"),
				null,
				ConversationStates.ATTENDING,
				"#Obrączka ślubna potrzebuje czasu, aby uzupełnić moc po każdym użyciu i potrzebuje mniej mocy, aby połączyć parę razem. "
				+ "Dla par z podobnym poziomem czas odzyskiwania mocy jest krótszy niż 10 minut i może być krótszy niż 5 minut.",
				null);

		nun.add(ConversationStates.ATTENDING,
				Arrays.asList("wedding","obrączka", "ślub"),
				null,
				ConversationStates.ATTENDING,
				"Ślub możesz wziąć tutaj w tym kościele. Jeżeli chcesz się #zaręczyć to przyprowadź tutaj swojego partnera "
				+ "i powiedz mi #zaręcz #imię.",
				null);

		nun.add(ConversationStates.ATTENDING, 
				Arrays.asList("engage", "zaręcz"), 
				null,
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
							final EventRaiser npc) {
						// find out whom the player wants to marry.
						final String brideName = sentence.getSubjectName();

						if (brideName == null) {
							npc.say("Powiedz mi kogo chcesz poślubić.");
						} else {
							startEngagement((SpeakerNPC) npc.getEntity(), player, brideName);
						}
					}
				});

		nun.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, 
				null,
				ConversationStates.QUESTION_2, 
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
							final EventRaiser npc) {
						askBrideE();
					}
				});

		nun.add(ConversationStates.QUESTION_1, 
				ConversationPhrases.NO_MESSAGES,
				null, 
				ConversationStates.IDLE, 
				"Co za szkoda! Dowidzenia!", 
				null);

		nun.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, 
				null,
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
							final EventRaiser npc) {
						finishEngagement();
					}
				});

		nun.add(ConversationStates.QUESTION_2, 
				ConversationPhrases.NO_MESSAGES,
				null, 
				ConversationStates.IDLE, 
				"Co za szkoda! Dowidzenia!", 
				null);
	}

	private void startEngagement(final SpeakerNPC nun, final Player player,
			final String partnerName) {
		final StendhalRPZone outsideChurchZone = nun.getZone();
		final Area inFrontOfNun = new Area(outsideChurchZone, new Rectangle(51, 52, 6, 5));
		groom = player;
		bride = SingletonRepository.getRuleProcessor().getPlayer(partnerName);

		if (!inFrontOfNun.contains(groom)) {
			nun.say("Mój słuch nie jest zbyt dobry. Proszę podejdźcie oboje bliżej i powiedzcie mi z kim chcecie się zaręczyć.");
		} else if (marriage.isMarried(groom)) {
			nun.say("Jesteś już żonaty z " 
					+ groom.getName()
					+ "! Nie możesz się znowu ożenić.");
		} else if ((bride == null) || !inFrontOfNun.contains(bride)) {
			nun.say("Mój słuch nie jest zbyt dobry. Proszę podejdźcie oboje bliżej i powiedzcie mi z kim chcecie się zaręczyć.");
		} else if (bride.getName().equals(groom.getName())) {
			nun.say("Nie możesz wziąć ślubu ze sobą!");
		} else if (marriage.isMarried(bride)) {
			nun.say("Jesteś już żonaty z " 
					+ bride.getName()
					+ "! Nie możesz się ponownie ożenić.");
		} else {
			askGroomE();
		}

	}

	private void askGroomE() {
		nun.say(groom.getName() + ", czy chcesz się zaręczyć z "
				+ bride.getName() + "?");
		nun.setCurrentState(ConversationStates.QUESTION_1);
	}

	private void askBrideE() {
		nun.say(bride.getName() + ", czy chcesz się zaręczyć z "
				+ groom.getName() + "?");
		nun.setCurrentState(ConversationStates.QUESTION_2);
		nun.setAttending(bride);
	}

	private void giveInvite(final Player player) {
		final StackableItem invite = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"zwój weselny");
		invite.setQuantity(4);
		// location of church
		invite.setInfoString("marriage," + player.getName());

		// perhaps change this to a hotel room where they can get dressed into
		// wedding outfits?
		// then they walk to the church?
		player.equipOrPutOnGround(invite);
	}

	private void finishEngagement() {
		// we check if each of the bride and groom are engaged, or both, and only give invites 
		// if they were not already engaged.
		String additional;
		if (!marriage.isEngaged(groom)) {
			giveInvite(groom);
			if (!marriage.isEngaged(bride)) {
				giveInvite(bride);
				additional = "A tutaj mam trochę zaproszeń, które możecie rozdać gościom.";
			} else {
				additional = "Dałam zaproszenia dla gości " + groom.getName() + ". " + bride.getName() 
					+ ", jeżeli Ognir już wyrabia tobie pierścień to powinieneś pójść do niego i zapytać czy nie zrobiłby następnego.";
				}
		} else if (!marriage.isEngaged(bride)) {
			giveInvite(bride);
			additional = "Dałam zaproszenia dla gości " + bride.getName() + ". " + groom.getName() 
					+ ", jeżeli Ognir już wyrabia tobie pierścień to powinieneś pójść do niego i zapytać czy nie zrobiłby następnego.";
		} else {
			additional = "Nie dam wam więcej zaproszeń skoro jesteście już zaręczeni i dostaliście je wcześniej."
				+ " Jeżeli posiadacie już pierścienie to musicie zrobić je jeszcze raz.";
		}		
		nun.say("Gratulacje "
				+ groom.getName()
				+ " i tobie "
				+ bride.getName()
				+ " jesteście teraz zaręczeni! Przed wzięciem ślubu w kościele upewnijcie się, że byliście u Ognira po wykonane pierścionków ślubnych przed "
				+ "pójściem do kościoła. " + additional);
		// Memorize that the two engaged so that the priest knows
		groom.setQuest(marriage.getQuestSlot(), "engaged");
		bride.setQuest(marriage.getQuestSlot(), "engaged");
		// Clear the variables so that other players can become groom and bride
		// later
		groom = null;
		bride = null;
	}

	public void addToWorld() {
		engagementStep();
	}
}
