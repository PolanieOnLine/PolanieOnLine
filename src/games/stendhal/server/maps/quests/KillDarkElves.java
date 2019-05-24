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
package games.stendhal.server.maps.quests;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Kill Dark Elves
 * <p>
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Maerion
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Maerion asks you fix his dark elf problem
 * <li> You go kill at least a dark elf archer, captain, and thing
 * <li> The thing drops an amulet
 * <li> Maerion checks your kills, takes the amulet and gives you a ring of life
 * as reward
 * </ul>
 * REWARD: <ul><li> emerald ring <li> 100000 XP
 * <li>30 karma in total</ul>
 * 
 * REPETITIONS: - None.
 */
public class KillDarkElves extends AbstractQuest {
	private static final String QUEST_SLOT = "kill_dark_elves";
	protected final List<String> creatures = 
		Arrays.asList("elf ciemności kapitan",
				      "elf ciemności generał",
				      "elf ciemności rycerz",
				      "elf ciemności czarownik",
				      "elf ciemności czarnoksiężnik",
				      "elf ciemności królewicz",
				      "elf ciemności matrona",
					  "elf ciemności łucznik elitarny",
				      "elf ciemności łucznik");
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Maerion");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Już Cię wysłałem, abyś zabił mroczne elfy w tunelu pod ukrytym pokojem. Przynieś mi stamtąd amulet od cosia.",
				null);

		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Dziękuję za twoją pomoc. Jestem zadowolony, że odzyskałem amulet.",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Mam problem z mrocznymi elfami. Powinienem być z nimi w porozumieniu... teraz są zbyt silne. W #sekretnym #pokoju w sali na dole jest wejście do ich kryjówki.",
				null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		//actions.add(new StartRecordingKillsAction("dark elf archer", "dark elf captain", "thing"));
		actions.add(new IncreaseKarmaAction(5.0));
		actions.add(new SetQuestAction(QUEST_SLOT, "started"));
		actions.add(new ExamineChatAction("dark-elves-wanted.png", "List Gończy!", ""));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dobrze. Proszę zabij wszystkie mroczne elfy na dole i zdobądź amulet od mutanta zwanego coś.",
			new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
			ConversationPhrases.NO_MESSAGES, 
			null,
			ConversationStates.ATTENDING,
			"W takim razie boję się o bezpieczeństwo elfów w Nalwor...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("secret", "room", "sekretnym", "pokoju"),
			null,
			ConversationStates.QUEST_OFFERED,
			"To jest pokój na dole z szarym dachem z twarzą demona na drzwiach. W środku zobaczysz jak mroczne elfy robią mutanta. Pomożesz mi?",
			null);
	}

	private void step_2() {
		// Go kill the dark elves and get the amulet from the thing
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Maerion");

		// support for old-style quest
		
		// the player returns to Maerion after having started the quest.
		// Maerion checks if the player has killed one of enough dark elf types
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(new KilledCondition("elf ciemności łucznik", "elf ciemności kapitan", "coś"))),
				ConversationStates.QUEST_STARTED, 
				"Nie pamiętasz co mi obiecałeś w sprawie problemów z mrocznymi elfami? Musisz wrócić do #sekretnego #pokoju. Zabij wszystkie mroczne elfy i mutanta o imieniu coś.",
				null);
		
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new KilledCondition("elf ciemności łucznik", "elf ciemności kapitan", "coś"),
						new NotCondition(new PlayerHasItemWithHimCondition("amulet")))
				, ConversationStates.QUEST_STARTED
				, "Co się stało z amuletem? Pamiętasz, że potrzebuję go z powrotem!"
				, null);
	
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new KilledCondition("elf ciemności łucznik", "elf ciemności kapitan", "coś"),
						new PlayerHasItemWithHimCondition("amulet"))
				, ConversationStates.ATTENDING
				, "Bardzo, bardzo dziękuję. Cieszę się z oddania amuletu. Weź ten pierścień. Może on odrodzić twe moce po śmierci.",
				new MultipleActions(new DropItemAction("amulet"),
						new EquipItemAction("pierścień szmaragdowy", 1, true),
						new IncreaseXPAction(100000),
						new IncreaseKarmaAction(25.0),
						new SetQuestAction(QUEST_SLOT, "done")));
		
		
		// support for new-style quest
		
		// building string for completed quest state
		StringBuilder sb = new StringBuilder("started");
		for(int i=0;i<creatures.size();i++) {
			sb.append(";");
			sb.append(creatures.get(i));
		}
		final String completedQuestState = sb.toString();
		
		// the player returns to Maerion after having started the quest.
		// Maerion checks if the player has killed one of enough dark elf types
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT,0,"started"),
						new NotCondition(
								new QuestInStateCondition(QUEST_SLOT, completedQuestState))),
				ConversationStates.QUEST_STARTED, 
				"Nie pamiętasz? Obiecałeś mi rozwiązać problem z ciemnymi elfami"+
				" Zabij każdego ciemnego elfa w #sekretnym #pokoju w szczególności "+
				" przywódców ciemnych elfów, ich czarodziejów i wszystkich łuczników." +
				"  nie zapomnij też o matronie."+
				" I najważniejsze po zabiciu mutanta coś przynieś mi amulet z jego zwłok.",
				new ExamineChatAction("dark-elves-wanted.png", "List Gończy!!", ""));
		
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, completedQuestState),
						new NotCondition(
								new PlayerHasItemWithHimCondition("amulet")))
				, ConversationStates.QUEST_STARTED
				, "Co się stało z amuletem? Pamiętaj muszę go mieć z powrotem!"
				, null);
	
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, completedQuestState),
						new PlayerHasItemWithHimCondition("amulet"))
				, ConversationStates.ATTENDING
				, "Bardzo, bardzo dziękuję. Cieszę się z oddania amuletu. Weź ten pierścień. Może on odrodzić twe moce po śmierci.",
				new MultipleActions(new DropItemAction("amulet"),
						new EquipItemAction("pierścień szmaragdowy", 1, true),
						new IncreaseXPAction(10000),
						new IncreaseKarmaAction(5.0),
						new SetQuestAction(QUEST_SLOT, "done")));
		
		npc.add(
			ConversationStates.QUEST_STARTED,
			Arrays.asList("secret", "room", "sekretnego", "pokoju"),
			null,
			ConversationStates.ATTENDING,
			"Pokój jest pod nami. Ma szary dach i twarz demona na drzwiach. Potrzebuję Ciebie, abyś zabił mroczne elfy i przyniósł mi amulet z mutanta coś.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zabij Mroczne Elfy",
				"Maerion lider elfów chce, abyś zabił mroczne elfy w sekretnym pokoju i odzyskał jego amulet.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "KillDarkElves";
	}
	
	/**
	 * function return list of drow creatures to kill
	 * @return - list of dark elves to kill
	 */
	public List<String> getDrowCreaturesList() {
		return creatures;
	}
	
	// Killing the thing probably requires a level even higher than this - but they can get help
	@Override
	public int getMinLevel() {
		return 100;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
 		LinkedList<String> history = new LinkedList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return history;
		}
		final String questState = player.getQuest(QUEST_SLOT, 0);

		if ("rejected".equals(questState)) {
			history.add("Nie chcę pomagać Maerion.");
			return history;
		}
		if ("done".equals(questState)) {
			history.add("Zakończyłem zadanie Maeriona i dostałem emerald ring!");
			return history;
		}

		// we can be here only if player accepted this quest.
		history.add("Zgodziłem się na pomoc Maerion.");
		
		boolean ak=true;	
		
		if("started".equals(player.getQuest(QUEST_SLOT, 0))) {
			// checking which creatures player killed.		
			for(int i = 0; i<creatures.size();i++) {
				final boolean sp = creatures.get(i).equals(player.getQuest(QUEST_SLOT, i+1));
				ak = ak & sp;
				if(!sp) {
					history.add("Jeszcze nie zabiłem "+creatures.get(i)+" w sekretnym pokoju.");
				}
			}		
			for(int i = 0; i<creatures.size();i++) {
				final boolean sp = creatures.get(i).equals(player.getQuest(QUEST_SLOT, i+1));
				if(sp) {
					history.add("Zabiłem "+creatures.get(i)+" w sekretnym pokoju.");
				}
			}

			// all killed
			if (ak) {
				history.add("Zabiłem wszystkie potwory.");
			}
		}
		
		// here is support for old-style quest
		if ("start".equals(player.getQuest(QUEST_SLOT, 0))) {
			final boolean osp1 = player.hasKilled("elf ciemności kapitan");
			final boolean osp2 = player.hasKilled("elf ciemności łucznik");
			final boolean osp3 = player.hasKilled("coś");
			// first add killed creatures
			if (osp1) {
				history.add("Zabiłem elf ciemności kapitan w sekretnym pokoju.");
			}		
			if (osp2) {
				history.add("Zabiłem elf ciemności łucznik w sekretnym pokoju.");
			}
			if (osp3) {
				history.add("Zabiłem coś.");
			}
			
			// now add non-killed
			if (!osp1) {
				history.add("Jeszcze nie zabiłem elf ciemności kapitan w sekretnym pokoju.");				
			}	
			if (!osp2) {
				history.add("Jeszcze nie zabiłem elf ciemności łucznik w sekretnym pokoju.");				
			}		
			if (!osp3) {
				history.add("Jeszcze nie zabiłem cosia.");				
			}
			
			// all killed
			if (osp1 && osp2 && osp3) {
				history.add("Zabiłem wszystkie potwory.");
				ak=true;
			}	
		}
		
		// for both old- and new-style quests
		final boolean am = player.isEquipped("amulet");
		if (am) {
			history.add("Mam ze sobą amulet.");
		} else {
			history.add("Nie mam ze sobą amuletu.");
		}
		
		if (am && ak) {
			history.add("Czas wrócić do Maeriona po nagrodę.");
		}
		
		return history;		
	}
	
	@Override
	public String getRegion() {
		return Region.NALWOR_CITY;
	}

	@Override
	public String getNPCName() {
		return "Maerion";
	}
}
