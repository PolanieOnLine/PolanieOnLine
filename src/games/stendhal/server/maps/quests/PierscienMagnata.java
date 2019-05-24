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
// Based on UltimateCollector and HelpMrsYeti.
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
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

public class PierscienMagnata extends AbstractQuest {

	private static final String QUEST_SLOT = "pierscien_magnata";

	private static final String CLUB_THORNS_QUEST_SLOT = "club_thorns";

	private static final String KILL_DRAGONS_QUEST_SLOT = "kill_dragons"; 

	private static final String VAMPIRE_SWORD_QUEST_SLOT = "vs_quest"; 

	private static final String IMMORTAL_SWORD_QUEST_SLOT = "immortalsword_quest";

	private static final String FIND_RAT_KIDS_QUEST_SLOT = "find_rat_kids";

	private static final String FIND_GHOSTS_QUEST_SLOT = "find_ghosts";

	private static final String SAD_SCIENTIST_QUEST_SLOT = "sad_scientist"; 

	private static final String PIERSCIEN_BARONA_QUEST_SLOT = "pierscien_barona";

	private static Logger logger = Logger.getLogger(PierscienMagnata.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Jubilera Zdzicha.");
		res.add("Zaproponował mi pierścień magnata.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie potrzebna mi są błyskotki..");
			return res;
		} 
		if (questState.equals("start")) {
			return res;
		} 
		res.add("Zdzichu poprosił abym mu dostarczył mu parę żeczy. po zdobyciu ich mam mu powiedzieć: pierścień.");
		if (questState.equals("start")) {
			return res;
		} 
		res.add("Jubiler Zdzichu był zadowolony z mojej postawy. W zamian dostałem pierścień magnata.");

		if (isCompleted(player)) {
			return res;
		} 

		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}

	private void checkLevelHelm() {
		final SpeakerNPC npc = npcs.get("Jubiler Zdzichu");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isBadBoy()){ 
						raiser.say("Z twej ręki zginął rycerz! Nie masz tu czego szukać, pozbądź się piętna czaszki. A teraz precz mi z oczu!");
					} else {
						if (player.isQuestCompleted(PIERSCIEN_BARONA_QUEST_SLOT)) {
							if (player.getLevel() >= 500) {
								if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
									raiser.say("Czy chcesz zdobyć pierścień magnata?.");
								} else if (player.isQuestCompleted(QUEST_SLOT)) {
									raiser.say("Odebrałeś już swój pierścień, żegnaj.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Twój stan społeczny jest zbyt niski aby podjąć te zadanie. Wróć gdy zdobędziesz 500 lvl.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Widzę, że nie posiadasz pierścienia barona, wróć gdy zdobędziesz go.");
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
					raiser.say("Ale wpierw sprawdzę czy masz wszystkie zadania zrobione nim dostaniesz #pierścień.");
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
		final SpeakerNPC npc = npcs.get("Jubiler Zdzichu");

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
							 new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
							 new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj przyjacielu. Mam dla Ciebie wyzwanie dzięki, któremu zdobędziesz #pierścień magnata.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "pierścień", "pierscien"), 
			new AndCondition(new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
							 new QuestNotStartedCondition(QUEST_SLOT),
							 new OrCondition(new QuestNotCompletedCondition(CLUB_THORNS_QUEST_SLOT),
							 new QuestNotCompletedCondition(IMMORTAL_SWORD_QUEST_SLOT))),
			ConversationStates.ATTENDING, 
			"Wciąż masz zadanie do wykonania w Kotoch. Szukaj dokładnie, a zdobędziesz pierścień!",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "pierścień", "pierscien"), 
			new AndCondition(new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
							 new QuestNotStartedCondition(QUEST_SLOT),
							 new OrCondition(new QuestNotCompletedCondition(KILL_DRAGONS_QUEST_SLOT),
							 new QuestNotCompletedCondition(VAMPIRE_SWORD_QUEST_SLOT))),
			ConversationStates.ATTENDING, 
			"Nie pomogłeś małej Alicji lub brakuje tobie miecza wampira.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "pierścień", "pierscien"),
			new AndCondition(new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
							 new QuestNotStartedCondition(QUEST_SLOT),
							 new OrCondition(new QuestNotCompletedCondition(FIND_RAT_KIDS_QUEST_SLOT),
							 	 	 	 	 new QuestNotCompletedCondition(FIND_GHOSTS_QUEST_SLOT))),
			ConversationStates.ATTENDING, 
			"Musisz odnaleść błąkające się duchy lub dzieci Agnus.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "pierścień", "pierscien"),
			new AndCondition(new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
							 new QuestNotStartedCondition(QUEST_SLOT),
							 new QuestNotCompletedCondition(SAD_SCIENTIST_QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Uzbierałeś sporo specjalnych przedmiotów, ale nie zdobyłeś czarnych spodni.",
			null);
	}

	private void requestItem() {
		final SpeakerNPC npc = npcs.get("Jubiler Zdzichu");

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "pierścień", "pierscien"),
			new AndCondition(
					new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT),
					new QuestCompletedCondition(SAD_SCIENTIST_QUEST_SLOT),
					new QuestCompletedCondition(FIND_GHOSTS_QUEST_SLOT),
					new QuestCompletedCondition(FIND_RAT_KIDS_QUEST_SLOT),
					new QuestCompletedCondition(VAMPIRE_SWORD_QUEST_SLOT),
					new QuestCompletedCondition(KILL_DRAGONS_QUEST_SLOT),
					new QuestCompletedCondition(CLUB_THORNS_QUEST_SLOT),
					new QuestCompletedCondition(IMMORTAL_SWORD_QUEST_SLOT)),
			ConversationStates.ATTENDING, "Aby zdobyć #pierścień musisz przynieść kilka przedmiotów.",
			new SetQuestAction(QUEST_SLOT, "start" ));

		final List<ChatAction> amuletactions = new LinkedList<ChatAction>();
		amuletactions.add(new DropItemAction("sztabka srebra",70));
		amuletactions.add(new DropItemAction("sztabka mithrilu",70));
		amuletactions.add(new DropItemAction("sztabka złota",250)); 
		amuletactions.add(new DropItemAction("bryłka mithrilu",100));
		amuletactions.add(new DropItemAction("bryłka złota",200));
		amuletactions.add(new DropItemAction("money",500000));
		amuletactions.add(new DropItemAction("pierścień barona",1));
		amuletactions.add(new EquipItemAction("pierścień magnata", 1, true));
		amuletactions.add(new IncreaseXPAction(500000));
		amuletactions.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("challenge", "pierścień", "pierscien"),
			new AndCondition(
					new QuestInStateCondition(QUEST_SLOT,"start"),
					new PlayerHasItemWithHimCondition("sztabka srebra",70),
					new PlayerHasItemWithHimCondition("sztabka mithrilu",70),
					new PlayerHasItemWithHimCondition("sztabka złota",250),
					new PlayerHasItemWithHimCondition("bryłka mithrilu",100),
					new PlayerHasItemWithHimCondition("bryłka złota",200),
					new PlayerHasItemWithHimCondition("money",500000),
					new PlayerHasItemWithHimCondition("pierścień barona",1)),
			ConversationStates.ATTENDING, "Widzę, że masz wszystko o co cię prosiłem. A oto nagroda pierścień magnata.",
			new MultipleActions(amuletactions));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("challenge", "pierścień", "pierscien"),
			new AndCondition(
					new QuestInStateCondition(QUEST_SLOT,"start"),
					new NotCondition(
							new AndCondition(new PlayerHasItemWithHimCondition("sztabka srebra",70),
							new PlayerHasItemWithHimCondition("sztabka mithrilu",70),
							new PlayerHasItemWithHimCondition("sztabka złota",250),
							new PlayerHasItemWithHimCondition("bryłka mithrilu",100),
							new PlayerHasItemWithHimCondition("bryłka złota",200),
							new PlayerHasItemWithHimCondition("money",500000),
							new PlayerHasItemWithHimCondition("pierścień barona",1)))),
			ConversationStates.ATTENDING, "Potrzebuję:\n"
									+"#'70 sztabka srebra'\n" 
									+"#'70 sztabek mithrilu'\n"
									+"#'250 sztabek złota'\n"
									+"#'100 bryłek mithrilu'\n"
									+"#'200 bryłek złota'\n"
									+"#'500000 money'\n"
									+"#'1 pierścień barona'\n"
									+" Proszę przynieś mi to wszystko naraz. Słowo klucz to #'/pierścień/'. Dziękuję!", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Pierścień Magnata",
			"Uporaj się z wyzwaniami, które postawił przed tobą jubiler Zdzichu.",
			true);

		checkLevelHelm(); 
		checkCollectingQuests();
		requestItem();
	}

	@Override
	public String getName() {
		return "PierscienMagnata";
	}
	@Override
	public String getNPCName() {
		return "Jubiler Zdzichu";
	}
}
