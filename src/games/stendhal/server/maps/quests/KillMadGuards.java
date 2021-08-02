/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.Pair;

public class KillMadGuards extends AbstractQuest {
	private static final String QUEST_SLOT = "kill_madguards";
	private final SpeakerNPC npc = npcs.get("Rycerz");

	private void step_1() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				null,
				ConversationStates.QUEST_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (!player.hasQuest(QUEST_SLOT) || player.getQuest(QUEST_SLOT).equals("rejected")) {
							raiser.say("Straż wraz co niektórymi rycerzami się zbuntowali i przejeli zamek. Przydałaby się nam każda pomoc, dasz radę pozbyć się zbuntowanych rycerzy?");
						} else if (player.getQuest(QUEST_SLOT, 0).equals("start")) {
							raiser.say("Już się Ciebie pytałem o pozbyciu się zbuntowanej straży i rycerzy!");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						} else if (player.getQuest(QUEST_SLOT).startsWith("killed;")) {
							final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
							final long delay = 2 * MathHelper.MILLISECONDS_IN_ONE_WEEK;
							final long timeRemaining = Long.parseLong(tokens[1]) + delay - System.currentTimeMillis();
							if (timeRemaining > 0) {
								raiser.say("W imieniu króla dziękuję za pomoc. Zbuntowana straż i rycerze mogą powrócić. Wróć za " + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
								raiser.setCurrentState(ConversationStates.ATTENDING);
								return;
							}
							raiser.say("Znowu potrzebujemy Twojej pomocy. Czy mógłbyś znowu nam pomóc?");
						} else {
							raiser.say("Dziękuję za pomoc w potrzebie. Teraz czujemy się bezpieczniej i pewniej.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});

		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
		toKill.put("strażnik", new Pair<Integer, Integer>(0,3));
		toKill.put("strażnik grobli", new Pair<Integer, Integer>(0,1));
		toKill.put("straznik bramy", new Pair<Integer, Integer>(0,2));
		toKill.put("rycerz na białym koniu", new Pair<Integer, Integer>(0,1));
		toKill.put("rycerz szafirowy", new Pair<Integer, Integer>(0,1));
		toKill.put("rycerz karmazynowy", new Pair<Integer, Integer>(0,1));
		toKill.put("rycerz w złotej zbroi", new Pair<Integer, Integer>(0,1));
		toKill.put("rycerz szmaragdowy", new Pair<Integer, Integer>(0,1));
		toKill.put("czarny rycerz", new Pair<Integer, Integer>(0,1));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseKarmaAction(5.0));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Wspaniale! Odkąd zajęli zamek nie mamy dostępu do odpowiedniego wyposażenia i arsenału. Kręcą się za murami tego zamku buntownicy...",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Rozumiem. Każdy się ich boi.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void step_2() {
		/* Player has to kill */
	}

	private void step_3() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Idź wypędź złych buntowników z tego zamku.");
				}
		});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jesteś godzien czci rycerskiej! Pamiętaj, że dalsza droga nie będzie łatwa, na każdym kroku musisz udowadniać swe męstwo i odwagę! Przyjmij nieco złotych dukatów na pokrycie napraw swojego wyposażenia.");
						final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
						money.setQuantity(100);
						player.equipOrPutOnGround(money);
						player.addKarma(5.0);
						player.addXP(7500);
						player.setQuest(QUEST_SLOT, "killed;" + System.currentTimeMillis());
		 			}
				}
		);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Wypędzienie Zbuntowanej Straży",
				"Pewien rycerz potrzebuje pomocy z wypędzeniem zbuntowanej straży i rycerzy.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		if (!isCompleted(player)) {
			res.add("Muszę wypędzić buntowaników z zamku.");
		} else if(isRepeatable(player)){
			res.add("Rycerz z pod zamku potrzebuje jeszcze raz pomocy i nagrodzi mnie, czy mam mu pomóc?");
		} else {
			res.add("Na tę chwilę jest spokój z buntownikami.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Wypędzienie Zbuntowanej Straży";
	}

	@Override
	public int getMinLevel() {
		return 65;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getRegion() {
		return Region.WARSZAWA;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
				new TimePassedCondition(QUEST_SLOT, 1, 2*MathHelper.MINUTES_IN_ONE_WEEK)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"killed").fire(player, null, null);
	}
}
