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

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.Arrays;

class MakeRings {
	private static final int REQUIRED_GOLD = 10;

	private static final int REQUIRED_MONEY = 500;

	private static final int REQUIRED_MINUTES = 10;

	private final NPCList npcs = SingletonRepository.getNPCList();
	private MarriageQuestInfo marriage;

	public MakeRings(final MarriageQuestInfo marriage) {
		this.marriage = marriage;
	}

	private void makeRingsStep() {
		final SpeakerNPC npc = npcs.get("Ognir");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("obrączka ślubna","obrączka", "wedding", "pierścionek ślubny", "ślub"),
				new QuestStateStartsWithCondition(marriage.getQuestSlot(), "engaged"),
				ConversationStates.QUEST_ITEM_QUESTION, 
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						if (player.isQuestInState(marriage.getQuestSlot(), "engaged_with_ring")) {
							// player has wedding ring already. just remind to
							// get spouse to get one and hint to get dressed.
							npc.say("Planujesz ślub? Upewnij się, że twoja narzeczona zdobyła obrączkę ślubną także dla Ciebie! Och i pamiętaj, aby się ubrać ( #dressed ) na wielki dzień.");
							npc.setCurrentState(ConversationStates.INFORMATION_2);
						} else {
							// says you'll need a ring
							npc.say("Potrzebuję "
									+ REQUIRED_GOLD
									+ " szatabek złota i "
									+ REQUIRED_MONEY
									+ " money, aby wykonać obrączkę ślubną dla twojej narzeczonej. Czy masz to?");
						}
					}
				});

		// response to wedding ring enquiry if you are not engaged
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("obrączka ślubna","obrączka", "wedding", "pierścionek ślubny", "ślub"),
				new QuestNotStartedCondition(marriage.getQuestSlot()),
				ConversationStates.ATTENDING,
				"Wyrabiam obrączkę ślubną dla Ciebie, abyś dał swojemu partnerowi o ile jesteś z kimś zaręczony. Jeżeli chcesz się zaręczyć to porozmawiaj z zakonnicą, która stoi przed kościołem.",
				null);

		// response to wedding ring enquiry when you're already married
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("obrączka ślubna", "wedding", "pierścionek ślubny", "ślub"),
				new AndCondition(new QuestCompletedCondition(marriage.getQuestSlot()), new PlayerHasItemWithHimCondition("obrączka ślubna")),
				ConversationStates.ATTENDING,
				"Mam nadzieję, że wciąż jesteście szczęśliwym małżeństwem inaczej nie rozumiem po co Tobie następny pierścionek... Jeżeli macie problemy i chcecie rozwodu to porozmawiajcie z urzędnikiem ratuszu w Ados.",
				null);

		// response to wedding ring enquiry when you're already married and not wearing ring
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("obrączka ślubna", "wedding", "pierścionek ślubny", "ślub"),
				new AndCondition(new QuestCompletedCondition(marriage.getQuestSlot()), new NotCondition(new PlayerHasItemWithHimCondition("obrączka ślubna"))),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Nie masz swojej obrączki ślubnej! Mogę zrobić następną za " + REQUIRED_GOLD
									+ " sztabek złota i "
									+ REQUIRED_MONEY
									+ " money, czy potrzebujesz następną?",
				null);

		// response to wedding ring enquiry when you're married but not taken honeymoon
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("obrączka ślubna", "wedding", "pierścionek ślubny", "ślub"),
				new QuestInStateCondition(marriage.getQuestSlot(), "just_married"),
				ConversationStates.ATTENDING,
				"Gratulacje z okazji ślubu! Nie zapomnijcie zapytać Lindy w Fado Hotel o wasz miesiąc miodowy.",
				null);

		// Here the behaviour is defined for if you make a wedding ring enquiry to Ognir and your
		// ring is being made
	 	npc.add(ConversationStates.ATTENDING, 
				Arrays.asList("obrączka ślubna", "wedding", "pierścionek ślubny", "ślub"),
		 		new QuestStateStartsWithCondition(marriage.getQuestSlot(), "forging"),
				ConversationStates.IDLE, 
		 		null, 
				new ChatAction() {
	 				@Override
	 				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
	 					final String[] tokens = player.getQuest(marriage.getQuestSlot()).split(";");
						final long delayInMIlliSeconds = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE; 
						final long timeRemaining = (Long.parseLong(tokens[1]) + delayInMIlliSeconds)
								- System.currentTimeMillis();
						// ring is not ready yet
						if (timeRemaining > 0L) {
							npc.say("Jeszcze nie zrobiłem obrączki ślubnej. Wróć za "
									+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
									+ ". Dowidzenia.");
							return;
						}
						/*The ring is ready now. It was either forging ready for a wedding or 
						 * forging again because a married player lost theirs.
						 * In each case we bind to the player. If player is engaged the rings get swapped at marriage ceremony
						 * If this is a forgingagain we must set the infostring to spouse name so the ring works
						 * We don't give them any XP if it is to replace a lost ring. (fools.)
						 * If this is for an engaged player, npc gives a hitn about getting dressed for big day
						 */
						final Item weddingRing = SingletonRepository.getEntityManager().getItem(
								"obrączka ślubna");
						weddingRing.setBoundTo(player.getName());
						if (player.getQuest(marriage.getQuestSlot()).startsWith("forgingagain")) {
							npc.say("Skończyłem pracę nad następną obrączką ślubną. Następnym razem bądź ostrożniejszy!");
							weddingRing.setInfoString(player.getQuest(marriage.getSpouseQuestSlot()));
							player.setQuest(marriage.getQuestSlot(), "done");
						} else {							
							npc.say("Zostałem poproszony, aby przekazać, że obrączka ślubna dla twojej narzeczonej jest już skończona! Upewnij się, że jedna jest dla Ciebie! *psst* jeszcze mała rada ( #hint ) na dzień ślubu ...");
							player.setQuest(marriage.getQuestSlot(), "engaged_with_ring");
							player.addXP(500);
						}
						player.equipOrPutOnGround(weddingRing);
						player.notifyWorldAboutChanges();
						npc.setCurrentState(ConversationStates.INFORMATION_2);
					}
				});


		// player says yes, they want a wedding ring made
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES, 
				null,
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						if ((player.isEquipped("sztabka złota", REQUIRED_GOLD))
								&& (player.isEquipped("money", REQUIRED_MONEY))) {
							player.drop("sztabka złota", REQUIRED_GOLD);
							player.drop("money", REQUIRED_MONEY);
							npc.say("Dobrze wróć za "
									+ REQUIRED_MINUTES
									+ " minutę" + ", a będzie gotowa. Dowidzenia.");
							if (player.isQuestCompleted(marriage.getQuestSlot())) {
								player.setQuest(marriage.getQuestSlot(), "forgingagain;"	+ System.currentTimeMillis());
							} else {
								player.setQuest(marriage.getQuestSlot(), "forging;"
												+ System.currentTimeMillis());
							}
							npc.setCurrentState(ConversationStates.IDLE);
						} else {
							// player said they had the money and/or gold but they lied
							npc.say("Wróć, gdy będziesz miał pieniądze i złoto.");
						}
					}
				});

		// player says (s)he doesn't have the money and/or gold
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Nie ma problemu, wróć, gdy będziesz miał pieniądze i złoto.",
				null);

		// Just a little hint about getting dressed for the wedding.
		npc.add(ConversationStates.INFORMATION_2,
				Arrays.asList("dressed", "hint", "dress", "ubrana", "podpowiedź", "sukienka"),
				null,
				ConversationStates.ATTENDING,
				"Gdy moja żona i ja mieliśmy wziąć ślub to poszliśmy do hotelu w Fado i wypożyczyliśmy specjalne ubrania. Przebieralnia jest po prawej stronie od wejścia. Idź tam i szukaj drewnianych drzwi. Powodzenia!",
				null);

	}

	public void addToWorld() {
		makeRingsStep();
	}

}
