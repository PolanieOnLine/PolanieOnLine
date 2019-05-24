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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.EnableFeatureAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * QUEST: Learn about Karma
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Sarzina, the friendly wizardess who also sells potions in Fado</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Sarzina asks if you are a helpful person</li>
 * <li>You get good or bad karma depending on what you say</li>
 * <li>You get the chance to learn about karma and find out what yours is.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>Some Karma</li>
 * <li>Knowledge</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>Can always learn about karma but not get the bonus each time</li>
 * </ul>
 */
public class LearnAboutKarma extends AbstractQuest {

	private static final String QUEST_SLOT = "learn_karma";


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
		res.add("Spotkałem Sarzina w domku w Fado i zapytałem ją o zadanie.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("done")) {
			res.add("Sarzina powiedziała mi o karmie i o tym, że mogę wrócić, aby przypomnieć sobie jak to działa.");
		}
		return res;
	}

	private void step1() {
		final SpeakerNPC npc = npcs.get("Sarzina");
		
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Czy jesteś tym, który lubi pomagać innym?", null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Jeśli chcesz by towarzyszyła ci dobra #karma jedyne co musisz robić, to pomagać innym. Znam dziewczę o imieniu Sally, która potrzebuje drewna, " 
			+ "i znam inne dziewczę co zwie się Annie, która uwielbia lody. Cóż, znam wielu mieszkańców tej krainy, którzy stale potrzebować będą pomocy. Jestem pewna, że jeśli im pomożesz czeka cię sowita zapłata.", null);

		// player is willing to help other people
		// player gets a little karma bonus
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Wyśmienicie! Musi cię otaczać dobra #karma.",
			new MultipleActions(
					new SetQuestAction(QUEST_SLOT, "done"),
					new EnableFeatureAction("karma_indicator")));

		// player is not willing to help other people
		// player gets a little karma removed
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Wiedziałam ... pewnie otacza cię zła #karma.",
			new MultipleActions(
					new DecreaseKarmaAction(10.0), 
					new SetQuestAction(QUEST_SLOT, "done"),
					new EnableFeatureAction("karma_indicator")));

		// player wants to know what karma is, and has completed the quest
		npc.add(
			ConversationStates.ATTENDING,
			"karma",
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.QUESTION_1,
			"Gdy robisz dobre rzeczy dla innych takie jak #zadania dostajesz dobrą karmę. Dobra karma oznacza, że " 
			+ " będzie ci się powodzić w bitwach, w łowieniu ryb, poszukiwaniu złota i drogocennych kamieni. " 
			+ " Chcesz wiedzieć jaką masz teraz karmę?",
			null);

		// Player wants to know what karma is, but has not yet completed the
		// quest. Act like the player asked about for a task.
		npc.add(ConversationStates.ATTENDING,
				"karma",
				new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Jesteś tym, który lubi pomagać innym?", null);

		// player wants to know what his own karma is
		npc.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final long roundedkarma = Math.round(player.getKarma());
					final String Yk = "Twoja karma ";
					final String canseekarma = "Teraz zobaczymy jakiego koloru jest twoja karma. ";
					final String rk = Long.toString(roundedkarma);
                    if (roundedkarma > 499 ) {
                        npc.say(Yk+"jest niesamowicie wysoka, "+rk+"! Musiałeś zrobić bardzo dużo dobrych uczynków. " + canseekarma + " Jest na niebiesko." );
                    } else if (roundedkarma > 99) {
                        npc.say(Yk+"jest wysoka, "+rk+". " + canseekarma + " Jest jeszcze na niebiesko.");
                    } else if (roundedkarma > 5) {
                    	npc.say(Yk+"wynosi "+rk+" jest dobra. " + canseekarma + " Musisz uważać zbliża się do koloru czerwonego.");
                    } else if (roundedkarma > -5) {
                        npc.say(Yk+"wynosi "+rk+". " + canseekarma + " Znajduje się w połowie skali.");
                    } else if (roundedkarma > -99) {
                        npc.say(Yk+"to "+rk+" jest zła. " + canseekarma + " Postaraj się aby była na niebiesko.");
                    } else if (roundedkarma > -499) {
                        npc.say(Yk+"jest straszna, "+rk+"! " + canseekarma + " Jest na czerwono.");
                    } else {
                    	npc.say(Yk+"jest katasrofalna, "+rk+"!!! " + canseekarma + " Zabrakło skali dla niej. Musisz być bardzo złym człowiekiem... ");
                    }
				}
			});

		// player doesn't want to know what his own karma is
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
			null, ConversationStates.ATTENDING,
			"Zatem, mogę ci jeszcze jakoś pomóc?", null);
		
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.QUEST_MESSAGES,
				null, ConversationStates.QUESTION_1,
				"Jeśli popytasz o zadania tu i tam, wypełnisz je, wówczas twoja karma urośnie. Chcesz wiedzieć jaką masz teraz karmę?", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Nauka o Karmie", 
				"Sarzina nauczy mnie o Karmie.", 
				false);
		step1();
	}
	
	@Override
	public String getName() {
		return "LearnAboutKarma";
	}
	
	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}
	@Override
	public String getNPCName() {
		return "Sarzina";
	}
}
