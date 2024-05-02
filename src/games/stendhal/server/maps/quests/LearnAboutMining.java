/***************************************************************************
 *                      (C) Copyright 2021 - Stendhal                      *
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;

public class LearnAboutMining extends AbstractQuest {
	private static final String QUEST_SLOT = "learn_mining";
	private final SpeakerNPC npc = npcs.get("Górnik");

	private final List<String> trigger = Arrays.asList("mining", "mines", "mine", "górnictwo", "górnik", "kopanie");

	private void step1() {
		npc.add(ConversationStates.ATTENDING,
			trigger,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Jestem dumny, że postanowiłeś się sprawdzić jako górnik! Więc mam ciebie ocenić pod względem twych umiejętności górniczych?", null);

		npc.add(ConversationStates.ATTENDING,
			trigger,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Jeśli chcesz by twój poziom górnictwa był większy to musisz ciągle zdobywać coraz większe doświadczenie podczas kopania różnych minerałów. "
			+ "Im minerał trudniejszy do wydobycia tym większe doświadczenie otrzymasz!", null);

		// player gets a little mining xp bonus
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser npc) {
					npc.say("Super! Możesz się mnie pytać o swój aktualny #poziom górnictwa, a ja cię ocenię!");
					player.incMiningXP(5000);
					player.setQuest(QUEST_SLOT, "done");
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Wiedziałam... pewnie nie masz czasu, aby dowiedzieć się na temat swojego aktualnego #poziomu.",
			new SetQuestAction(QUEST_SLOT, "done"));

		// player wants to know what karma is, and has completed the quest
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("level", "poziom"),
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.QUESTION_1,
			"Chcesz wiedzieć jakie masz teraz doświadczenie w kopaniu minerałów?",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("level", "poziom"),
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Chcesz wiedzieć jakie masz teraz doświadczenie w kopaniu minerałów?",
			null);

		// player wants to know what his own mining level is
		npc.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final int mininglevel = player.getMining();
					final String Yk = "Twoje doświadczenie w górnictwie ";
					final String rk = "#" + Integer.toString(mininglevel);
					final String canseelevel = "Na moje oko to twój poziom górnictwa wynosi " + rk + "!";
                    if (mininglevel > 99 ) {
                        npc.say(Yk + "jest niesamowite! Nie tylko posiadasz ogromne doświadczenie w kopaniu, ale masz również ogromną wiedzę na temat różnych rud oraz minerałów. " + canseelevel);
                    } else if (mininglevel > 49) {
                        npc.say(Yk + "jest w miarę dobre, posiadasz trochę już doświadczenia oraz szczątkową wiedzę na temat minerałów kopalnianych. " + canseelevel);
                    } else if (mininglevel > 14) {
                    	npc.say(Yk + "jest nieco słabe. Ważnę, że masz chęć zdobywać doświadczenie oraz wiedzę na temat rud. " + canseelevel);
                    } else {
                    	npc.say(Yk + "jest strasznie mierne... Nie posiadasz żadnych umiejętności w kopaniu oraz posiadasz znikomą widzę na temat rud i minerałów kopalnianych. " + canseelevel);
                    }
				}
			});

		// player doesn't want to know what his own karma is
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
			null, ConversationStates.ATTENDING,
			"Zatem, mogę ci jeszcze jakoś pomóc?", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Nauka o Górnictwie",
				"Górnik przekaże widzę dalej na temat umiejętności kopania rud i minerałów.",
				false);
		step1();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(player.getGenderVerb("Spotkałem") + " Górnika w kopalni Zakopane, niedaleko chatki górniczej.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("done")) {
			res.add("Górnik opowiedział mi o kopaniu i o tym, że mogę wrócić, aby przypomnieć sobie jak to działa.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Nauka o Górnictwie";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
