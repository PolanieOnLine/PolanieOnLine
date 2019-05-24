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

import games.stendhal.common.Direction;
import games.stendhal.common.NotificationType;
import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.TextHasNumberCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.Area;

import java.awt.Rectangle;
import java.util.Arrays;

class Honeymoon {
	private final NPCList npcs = SingletonRepository.getNPCList();
	private MarriageQuestInfo marriage;

	public Honeymoon(final MarriageQuestInfo marriage) {
		this.marriage = marriage;
	}

	private void honeymoonStep() {
		final SpeakerNPC linda = npcs.get("Linda");
		// tell her you want a honeymoon
		linda.add(
				ConversationStates.ATTENDING,
				Arrays.asList("honeymoon", "miesiąc miodowy"),
				null,
				ConversationStates.QUESTION_1, null,
				new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
                        final StendhalRPZone fadoHotel = npc.getZone();
                        final Area hotelReception = new Area(fadoHotel, new Rectangle(11, 46, 19, 10));

                        Player husband;
                        Player wife;
                        String partnerName;
                        husband = player;
                        partnerName = husband.getQuest(marriage.getSpouseQuestSlot());
                        wife = SingletonRepository.getRuleProcessor().getPlayer(partnerName);
                        
						if (!(player.hasQuest(marriage.getQuestSlot())) || !("just_married".equals(player.getQuest(marriage.getQuestSlot())))) {
							// person is not just married
							npc.say("Przepraszam, ale nasze apartamenty na miesiąc miodowy są dostępne tylko dla klientów po ślubie.");
							npc.setCurrentState(ConversationStates.ATTENDING);
						} else if (wife == null) { 
							//wife is not online
                            npc.say("Wróć, gdy " + partnerName + " będzie z tobą - powinniście być razem podczas miesiąca miodowego!");
                            npc.setCurrentState(ConversationStates.IDLE);
                        } else if (!(wife.hasQuest(marriage.getQuestSlot())
                                     && wife.getQuest(marriage.getSpouseQuestSlot()).equals(husband.getName()))) {
                        	//wife is not married to this husband
                            npc.say("Och, ale kłopotliwa sytuacja. Powinniście być małżeństwem, ale " + partnerName + " nie wziął ślubu z tobą.");
                            npc.setCurrentState(ConversationStates.ATTENDING);
                        } else if (!hotelReception.contains(wife)) {
                        	//  wife has not bothered to come to reception desk
                            npc.say("Czy mógłbyś poprosić " + partnerName + ", aby podeszła do recepcji i zapoznała się z naszym katalogiem. Później powiedz, który pokój wybraliście.");
                        }  else { 
                        	//wife and husband fulfill all conditions
							npc.say("Jak słodko! Proszę zobaczcie nasz katalog i powiedzcie mi numer pokoju, który chcecie.");
						}
					}
				});
		// player says room number
		linda.addMatching(ConversationStates.QUESTION_1,
				// match for all numbers as trigger expression
				ExpressionType.NUMERAL, new JokerExprMatcher(),
				new TextHasNumberCondition(1, 15),
				ConversationStates.IDLE, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {

                        final String room = Integer.toString(sentence.getNumeral().getAmount());
                        final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(
                                                                                       "int_fado_lovers_room_" + room);
						if (zone.getPlayers().size() > 0) {
							npc.say("Przykro mi, ale ten pokój jest teraz zajęty. Moglibyście podać inny numer?");
							npc.setCurrentState(ConversationStates.QUESTION_1);
						} else {

							Player husband;
							Player wife;
							String partnerName;
							husband = player;
							partnerName = husband.getQuest(marriage.getSpouseQuestSlot());
							wife = SingletonRepository.getRuleProcessor().getPlayer(
                                                                                partnerName);
							final StackableItem invite1 = (StackableItem) SingletonRepository.getEntityManager().getItem(
																												  "zwój weselny");
							invite1.setQuantity(1);
                            final StackableItem invite2 = (StackableItem) SingletonRepository.getEntityManager().getItem(
                                                                                                                  "zwój weselny");
                            invite2.setQuantity(1);
                            // 
							invite1.setInfoString("honeymoon," + partnerName);
							invite2.setInfoString("honeymoon," + husband.getTitle());
							if (wife.equipToInventoryOnly(invite1) &&  husband.equipToInventoryOnly(invite2)) {
								npc.say("Dobry wybór! Teraz zorganizuje to."); 
								husband.setQuest(marriage.getQuestSlot(), "done");
								wife.setQuest(marriage.getQuestSlot(), "done");
								wife.teleport(zone, 5, 5, Direction.DOWN, player);
								husband.teleport(zone, 6, 5, Direction.DOWN, player);
								final String scrollmessage = "Linda powiedziała Tobie: Użyj zwoju w plecaku, aby powrócić do hotelu. Nasze specjalne apartamenty na miesiąc miodowy są prywatne i nie ma w nich normalnych wejść i wyjść!";
								wife.sendPrivateText(NotificationType.PRIVMSG, scrollmessage);
                                husband.sendPrivateText(NotificationType.PRIVMSG, scrollmessage);
								wife.notifyWorldAboutChanges();
								husband.notifyWorldAboutChanges();
								npc.setCurrentState(ConversationStates.IDLE);
							} else {
								npc.say("Każdy z was potrzebuje jednego miejsca w plecaku, aby wziąć zwój. Proszę zróbcie miejsce i zapytajcie mnie ponownie. Dziękuję.");
							}
						}
					}
				});

		// player says something which isn't a room number
//		npc.add(ConversationStates.QUESTION_1, "",
//			new SpeakerNPC.ChatCondition() {
//				@Override public boolean fire(Player player, Sentence sentence, SpeakerNPC npc) {
//					return !ROOMS.contains(text);
//				}
//			}, ConversationStates.QUESTION_1,
//			"Sorry, that's not a room number we have available.", null
//		);

	}

	public void addToWorld() {
		honeymoonStep();
	}

}
