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
package games.stendhal.server.maps.quests.mithrilcloak;

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
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.Arrays;


/**
 * @author kymara
*/

class MakingClasp {


	private static final int REQUIRED_MINUTES_CLASP = 60;

	private MithrilCloakQuestInfo mithrilcloak;
	
	private final NPCList npcs = SingletonRepository.getNPCList();

	public MakingClasp(final MithrilCloakQuestInfo mithrilcloak) {
		this.mithrilcloak = mithrilcloak;
	}
	
	private void getClaspStep() {

		// don't overlap with any states from producer adder since he is a mithril bar producer
		
		final SpeakerNPC npc = npcs.get("Pedinghaus");

		// offer the clasp when prompted
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("clasp", "mithril clasp","brosza", "ida", "cloak", "mithril cloak"),
			new QuestInStateCondition(mithrilcloak.getQuestSlot(), "need_clasp"),
			ConversationStates.SERVICE_OFFERED,
			"Brosza? Cokolwiek powiesz! Wciąż się cieszę z listu, który mi przyniosłeś. Z przyjemnością zrobię coś dla Ciebie. Potrzebuję tylko sztabkę mithrilu. Masz ją?",
			null);

		// player says yes they want a clasp made and claim they have the mithril
		npc.add(
			ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.YES_MESSAGES, 
			new QuestInStateCondition(mithrilcloak.getQuestSlot(), "need_clasp"),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (player.isEquipped("sztabka mithrilu")) {
						player.drop("sztabka mithrilu");
							npc.say("Co za piękny kawałek mithrilu ... Dobrze. Wróć za " 
									   + REQUIRED_MINUTES_CLASP + " minutę" + ", a Twója brosza będzie gotowa!");
							player.setQuest(mithrilcloak.getQuestSlot(), "forgingclasp;" + System.currentTimeMillis());
							player.notifyWorldAboutChanges();
						} else {
							npc.say("Nie możesz oszukać starego czarodzieja. Wiem jak wygląda mithril. Wróć, gdy będziesz miał co najmniej jedną sztabkę.");
						}
				}
			});

		// player says they don't have any mithril yet
		npc.add(
			ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.NO_MESSAGES, 
			null,
			ConversationStates.ATTENDING,
			"Jeżeli chciałbyś, abym odlał sztabkę mithrilu to powiedz.",
			null);

		npc.add(ConversationStates.ATTENDING, 
			Arrays.asList("clasp", "mithril clasp", "ida", "cloak","brosza", "mithril cloak"),
			new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "forgingclasp;"),
			ConversationStates.ATTENDING, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String[] tokens = player.getQuest(mithrilcloak.getQuestSlot()).split(";");
					// minutes -> milliseconds
					final long delay = REQUIRED_MINUTES_CLASP * MathHelper.MILLISECONDS_IN_ONE_MINUTE;
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();
					if (timeRemaining > 0L) {
						npc.say("Nie skończyłem jeszcze. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
						return;
					}
					npc.say("Oto Twója brosza!");
					player.addXP(100);
					player.addKarma(15);
					final Item clasp = SingletonRepository.getEntityManager().getItem(
									"brosza z mithrilu");
					clasp.setBoundTo(player.getName());
					player.equipOrPutOnGround(clasp);
					player.setQuest(mithrilcloak.getQuestSlot(), "got_clasp");
					player.notifyWorldAboutChanges();
				}
			});

	}


	private void giveClaspStep() {

		final SpeakerNPC npc = npcs.get("Ida");
		
		// Player brought the clasp, don't make them wait any longer for the cloak
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("clasp", "brosza z mithrilu", "cloak","brosza", "mithril cloak", "task", "quest", "zadanie", "misja"),
				new AndCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_clasp"), new PlayerHasItemWithHimCondition("brosza z mithrilu")),
				ConversationStates.ATTENDING,
				"Łał Pedinghaus naprawdę się postarał. Bezie wspaniale wyglądać na Twoim płaszczu! Ubieraj to z dumą.",
				new MultipleActions(
									 new DropItemAction("brosza z mithrilu"), 
									 new SetQuestAndModifyKarmaAction(mithrilcloak.getQuestSlot(), "done", 10.0),
									 new EquipItemAction("płaszcz z mithrilu", 1, true), 
									 new IncreaseXPAction(1000)
									 )
				);

		// remind about getting clasp
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("clasp", "brosza z mithrilu", "cloak", "mithril cloak", "task", "quest", "zadanie", "misja"),
				new OrCondition(
								new QuestInStateCondition(mithrilcloak.getQuestSlot(), "need_clasp"),
								new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "forgingclasp;"),
								new AndCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_clasp"),
												 new NotCondition(new PlayerHasItemWithHimCondition("brosza z mithrilu")))
								),
				ConversationStates.ATTENDING,
				"Nie masz broszy od #Pedinghaus. Jeżeli mi ją przyniesiesz to skończę Twój płaszcz!",				
				null);
	}

	public void addToWorld() {
		getClaspStep();
		giveClaspStep();
		
	}

}
