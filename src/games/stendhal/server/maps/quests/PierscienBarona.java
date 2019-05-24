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
// Based on UltimateCollector
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

	public class PierscienBarona extends AbstractQuest {

	private static final String QUEST_SLOT = "pierscien_barona";

	private static final String HUNGRY_JOSHUA_QUEST_SLOT = "hungry_joshua"; 

	private static final String FISHERMANS_LICENSE2_QUEST_SLOT = "fishermans_license2";

	private static final String OBSIDIAN_KNIFE_QUEST_SLOT = "obsidian_knife";

	private static final String MITHRIL_CLOAK_QUEST_SLOT = "mithril_cloak";

	private static final String CIUPAGA_DWA_WASY_QUEST_SLOT = "ciupaga_dwa_wasy";

	private static Logger logger = Logger.getLogger(PierscienBarona.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void checkLevelHelm() {
		final SpeakerNPC npc = npcs.get("eDragon");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isBadBoy()){ 
						raiser.say("Z twej ręki zginął rycerz! Nie masz tu czego szukać, pozbądź się piętna czaszki. A teraz precz mi z oczu!");
					} else {
						if (player.isQuestCompleted(CIUPAGA_DWA_WASY_QUEST_SLOT)) {
							if (player.getLevel() >= 350) {
								if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
									raiser.say("Czyż byś chciał zdobyć pierścień barona?");
								} else if (player.isQuestCompleted(QUEST_SLOT)) {
									raiser.say("Odebrałeś już pierścień.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Twój stan społeczny jest zbyt niski aby podjąć te zadanie. Wróć gdy zdobędziesz 350 lvl.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Widzę, że nie posiadasz złotej ciupagi z dwoma wąsami. Wróć gdy ją zdobędziesz.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				}
			}); 
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Ale wpierw sprawdzę czy masz wszystkie zadania zrobione nim dostaniesz #listę.");
					player.addKarma(10);
				}
		});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Nie to nie.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void checkCollectingQuests() {
		final SpeakerNPC npc = npcs.get("eDragon");

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj przyjacielu. Czyż byś chciał zdobyć #pierścień barona?",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("listę", "liste", "pierścień"),
			new AndCondition(new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(HUNGRY_JOSHUA_QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Napraw rzemyk to porozmawiamy o pierścieniu.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("listę", "liste", "pierścień"),
			new AndCondition(new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(FISHERMANS_LICENSE2_QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Zdobądź kartę rybaka to pogadamy o pierścieniu.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("listę", "liste", "pierścień"),
			new AndCondition(new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(OBSIDIAN_KNIFE_QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Nie widzę u ciebie obsydianowego saksa, zrób na niego zadanie to porozmawiamy o pierścieniu.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("listę", "liste", "pierścień"),
			new AndCondition(new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
					 new QuestNotStartedCondition(QUEST_SLOT),
					 new QuestNotCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Najpierw zrób zadanie na płaszcz z mithrilu, wtedy porozmawiamy o pierścieniu.",
			null);

	}

	private void requestItem() {
		final SpeakerNPC npc = npcs.get("eDragon");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("listę", "liste", "pierścień"),
				new AndCondition( 
						new QuestCompletedCondition(CIUPAGA_DWA_WASY_QUEST_SLOT),
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(HUNGRY_JOSHUA_QUEST_SLOT),
						new QuestCompletedCondition(FISHERMANS_LICENSE2_QUEST_SLOT),
						new QuestCompletedCondition(OBSIDIAN_KNIFE_QUEST_SLOT),
						new QuestCompletedCondition(MITHRIL_CLOAK_QUEST_SLOT)),
				ConversationStates.ATTENDING, "Widzę, że zadania masz zrobine. Teraz tylko spiszę listę brakujących #przedmiotów.",
				new SetQuestAction(QUEST_SLOT, "lista" ));

		final List<ChatAction> amuletactions = new LinkedList<ChatAction>();
		amuletactions.add(new DropItemAction("pierścień rycerza",1));
		amuletactions.add(new DropItemAction("ruda żelaza",100));
		amuletactions.add(new DropItemAction("bryłka złota",150));
		amuletactions.add(new DropItemAction("polano",200));
		amuletactions.add(new DropItemAction("węgiel",100));
		amuletactions.add(new DropItemAction("siarka",100));
		amuletactions.add(new DropItemAction("sól",100));
		amuletactions.add(new EquipItemAction("pierścień barona", 1, true));
		amuletactions.add(new IncreaseXPAction(100000));
		amuletactions.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przedmiotów", "przedmioty"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"lista"),
				new PlayerHasItemWithHimCondition("pierścień rycerza",1),
				new PlayerHasItemWithHimCondition("ruda żelaza",100),
				new PlayerHasItemWithHimCondition("bryłka złota",150),
				new PlayerHasItemWithHimCondition("polano",200),
				new PlayerHasItemWithHimCondition("węgiel",100),
				new PlayerHasItemWithHimCondition("siarka",100),
				new PlayerHasItemWithHimCondition("sól",100)),
				ConversationStates.ATTENDING, "Widzę, że masz wszystko o co cię prosiłem. A oto twój pierścień barona.",
				new MultipleActions(amuletactions));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przedmioty","przedmiotów"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"lista"),
								 new NotCondition(
								 new AndCondition(new PlayerHasItemWithHimCondition("pierścień rycerza",1),
												  new PlayerHasItemWithHimCondition("ruda żelaza",100),
												  new PlayerHasItemWithHimCondition("bryłka złota",150),
												  new PlayerHasItemWithHimCondition("polano",200),
												  new PlayerHasItemWithHimCondition("węgiel",100),
												  new PlayerHasItemWithHimCondition("siarka",100),
												  new PlayerHasItemWithHimCondition("sól",100)))),
				ConversationStates.ATTENDING, "Przynieś mi:\n"
									+"#'1 pierścień rycerza'\n"
									+"#'100 rudy żelaza'\n"
									+"#'150 bryłek złota'\n"
									+"#'200 polan'\n"
									+"#'100 węgla'\n"
									+"#'100 siarki'\n"
									+"#'100 soli'\n"
									+" Proszę przynieś mi wszystko naraz."
									+" Słowo klucz to #'/przedmioty/'. Dziękuję!", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pierścień Barona",
				"Uporaj się z wyzwaniami, które postawił przed tobą kowal Marianek.",
				true);

		checkLevelHelm(); 
		checkCollectingQuests();
		requestItem();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem smoka eDragon.");
			res.add("Zaproponował mi pierścień barona.");
			if ("rejected".equals(questState)) {
				res.add("Nie potrzebna mi jest pierścień..");
				return res;
			} 
			if ("start".equals(questState)) {
				return res;
			} 
			res.add("Smok eDragon poprosił abym mu dostarczył: pierścień rycerza, 100 rudy żelaza, 150 bryłek złota, 200 polan, 100 węgla, 100 siarki i 100 soli. Mam mu powiedzieć przedmioty gdy będę miał wszystko.");
			if ("lista".equals(questState)) {
				return res;
			} 
			res.add("Smok eDragon był zadowolony z mojej postawy. W zamian dostałem pierścień barona.");
			if (isCompleted(player)) {
				return res;
			} 

			// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
			final List<String> debug = new ArrayList<String>();
			debug.add("Stan zadania to: " + questState);
			logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
			return debug;
	}

	@Override
	public String getName() {
		return "EdragonNPC";
	}

	@Override
	public String getNPCName() {
		return "eDragon";
	}
}