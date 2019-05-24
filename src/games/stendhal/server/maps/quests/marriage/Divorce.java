/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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
import games.stendhal.server.core.engine.dbcommand.StoreMessageCommand;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.player.Player;
import marauroa.server.db.command.DBCommandQueue;

import java.util.Arrays;

class Divorce {
	private final NPCList npcs = SingletonRepository.getNPCList();
	private MarriageQuestInfo marriage;

	public Divorce(final MarriageQuestInfo marriage) {
		this.marriage = marriage;
	}

	private void divorceStep() {

		/**
		 * Creates a clerk NPC who can divorce couples.
		 * 
		 * Note: in this class, the Player variables are called husband and
		 * wife. However, the game doesn't know the concept of genders. The
		 * player who initiates the divorce is just called husband, the other
		 * wife.
		 * 
		 * @author immibis
		 * 
		 */

		SpeakerNPC clerk = npcs.get("Wilfred");

		clerk.add(ConversationStates.ATTENDING, 
				Arrays.asList("divorce", "rozwód"),
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
						return (player.isQuestCompleted(marriage.getQuestSlot()))
								&& player.isEquipped("obrączka ślubna") && player.isEquipped("money",200*player.getLevel());
					}
				}, 
				ConversationStates.QUESTION_3,
				null,
			   	new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						Player husband;
						Player wife;
						String partnerName;
						String additional = "";
						husband = player;
						partnerName = husband.getQuest(marriage.getSpouseQuestSlot());
						wife = SingletonRepository.getRuleProcessor().getPlayer(
								partnerName);
						if ((wife != null)
								&& wife.hasQuest(marriage.getQuestSlot())
								&& wife.getQuest(marriage.getSpouseQuestSlot()).equals(
										husband.getName())) {
							if (wife.isEquipped("money", 200*wife.getLevel())) {
								additional = partnerName + " posiada " + 200*wife.getLevel() + " money na opłatę.";
							} else {
								additional = partnerName + " nie posiada " + 200*wife.getLevel() + " money na opłatę i straci zamiast tego 3% doświadczenia.";
							}
						}
						npc.say("Istnieje możliwość zapłacenia za rozwód zamiast utraty doświadczenia. Będzie cię to kosztować " + 200* player.getLevel() + " money. " + additional + " Czy chcesz wziąć rozwód i zapłacić dodatkową opłatę, która uratuje Ciebie od utraty doświadczenia?");
					}
				});

		clerk.add(ConversationStates.ATTENDING, 
				  Arrays.asList("divorce", "rozwód"),
				  new ChatCondition() {
					  @Override
					  public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
						  return (player.isQuestCompleted(marriage.getQuestSlot()))
							  && player.isEquipped("obrączka ślubna") && !player.isEquipped("money",200*player.getLevel());
					}
				}, 
				ConversationStates.QUESTION_3,
				null,
			   	new ChatAction() {
					  @Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						Player husband;
						Player wife;
						String partnerName;
						String additional = "";
						husband = player;
						partnerName = husband.getQuest(marriage.getSpouseQuestSlot());
						wife = SingletonRepository.getRuleProcessor().getPlayer(partnerName);
						if ((wife != null)
							&& wife.hasQuest(marriage.getQuestSlot())
							&& wife.getQuest(marriage.getSpouseQuestSlot()).equals(
										                                           husband.getName())) {
							if (wife.isEquipped("money", 200*wife.getLevel())) {
								additional = partnerName + " posiada " + 200*wife.getLevel() + " money na opłatę.";
							} else {
								additional = partnerName + " nie posiada " + 200*wife.getLevel() + " money na opłatę i straci zamiast tego 3% doświadczenia.";
							}
						}
						npc.say("Istnieje możliwość zapłacenia za rozwód zamiast utraty doświadczenia. Będzie cię to kosztować " + 200* player.getLevel() + " money. " + additional + " Czy chcesz wziąć rozwód i zapłacić dodatkową opłatę, która uratuje Ciebie od utraty doświadczenia?");
					}
				});

		clerk.add(ConversationStates.ATTENDING,
					Arrays.asList("divorce", "rozwód"),
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
							return (player.hasQuest(marriage.getQuestSlot())
                                    && player.getQuest(marriage.getQuestSlot()).equals("just_married"))
									&& player.isEquipped("obrączka ślubna");

						}
					},
					ConversationStates.QUESTION_3,
					"Widzę, że jeszcze nie byłeś na miesiącu miodowym. Czy jesteś pewien, że chcesz wziąć rozwód tak szybko?",
					null);

		clerk.add(ConversationStates.ATTENDING,
				Arrays.asList("divorce", "rozwód"),
				new NotCondition(
					// isMarriedCondition()
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
							return (player.isQuestCompleted(marriage.getQuestSlot()) ||
									(player.hasQuest(marriage.getQuestSlot()) && player.getQuest(marriage.getQuestSlot()).equals("just_married")));
						}
					}
				), ConversationStates.ATTENDING,
				"Nie wziąłeś nawet ślubu. Przestań marnować mój czas!",
				null);

		clerk.add(ConversationStates.ATTENDING,
				Arrays.asList("divorce", "rozwód"),
				new AndCondition(
					// isMarriedCondition()
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
							return (player.isQuestCompleted(marriage.getQuestSlot()) ||
									(player.hasQuest(marriage.getQuestSlot()) && player.getQuest(marriage.getQuestSlot()).equals("just_married")));
						}
					},
					new NotCondition(new PlayerHasItemWithHimCondition("obrączka ślubna"))),
				ConversationStates.ATTENDING,
				"Przepraszam, ale potrzebuje Twojej obrączki ślubnej, aby dać Tobie rozwód.",
				null);

		// If they say no
		clerk.add(ConversationStates.QUESTION_3,
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Mam nadzieje, że miałeś szczęśliwe małżeństwo.", 
				null);

		// If they say yes
		clerk.add(ConversationStates.QUESTION_3,
				ConversationPhrases.YES_MESSAGES, 
				null,
				ConversationStates.ATTENDING, 
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						Player husband;
						Player wife;
						String partnerName;
						husband = player;
						partnerName = husband.getQuest(marriage.getSpouseQuestSlot());
						wife = SingletonRepository.getRuleProcessor().getPlayer(partnerName);
						// check wife is online and check that they're still
						// married to the current husband
						if ((wife != null)
								&& wife.hasQuest(marriage.getQuestSlot())
								&& wife.getQuest(marriage.getSpouseQuestSlot()).equals(
										husband.getName())) {
							if (wife.isEquipped("obrączka ślubna")) {
								wife.drop("obrączka ślubna");
							}
							if (wife.isEquipped("money", 200*wife.getLevel())) {
								wife.drop("money", 200*wife.getLevel());
							} else {
								final int xp = (int) (wife.getXP() * 0.03);
								wife.subXP(xp);
							}
							wife.removeQuest(marriage.getQuestSlot());
							wife.removeQuest(marriage.getSpouseQuestSlot());
							wife.sendPrivateText(husband.getName() + " rozwiódł się z tobą.");
							npc.say("Co za szkoda...co za szkoda.... byliście tak szczęśliwi...");
						} else {
							DBCommandQueue.get().enqueue(new StoreMessageCommand("Wilfred", partnerName, husband.getName() + " rozwiódł się z Tobą!" , "N"));
						}
						if (husband.isEquipped("money", 200*husband.getLevel())) {
							husband.drop("money", 200*husband.getLevel());
						} else {
							final int xp = (int) (husband.getXP() * 0.03);
							husband.subXP(xp);
						}
						husband.drop("obrączka ślubna");
						husband.removeQuest(marriage.getQuestSlot());
						husband.removeQuest(marriage.getSpouseQuestSlot());
						npc.say("Co za szkoda...co za szkoda..., a byliście tak szczęśliwi...");
					}
				});
	}

	public void addToWorld() {
		divorceStep();
	}

}
