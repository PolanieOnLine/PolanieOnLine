/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
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

import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Kill Spiders
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Morgrin
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Groundskeeper Morgrin ask you to clean up the school basement
 * <li> You go kill the spiders in the basement and you get the reward from Morgrin
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> magical egg
 * <li> 5000 XP
 * <li> 10 karma in total
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> after 7 days.
 * </ul>
 */

public class KillSpiders extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_all_spiders";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Morgrin");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (!player.hasQuest(QUEST_SLOT) || player.getQuest(QUEST_SLOT).equals("rejected")) {
							raiser.say("Byłeś kiedyś w szkolnej piwnicy? Odkąd studenci eksperymentowali to pokój jest pełen pająków i niektóre mogą być niebezpieczne! Czy mógłbyś mi pomóc z tym 'małym' problemem?");
							raiser.setCurrentState(ConversationStates.QUEST_OFFERED);
						}  else if (player.isQuestCompleted(QUEST_SLOT)) {
							raiser.say("Już Cię wysłałem, abyś zabił wszystkie potwory w piwnicy!");
						}  else if (player.getQuest(QUEST_SLOT).startsWith("killed;")) {
							final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
							final long delay = MathHelper.MILLISECONDS_IN_ONE_WEEK;
							final long timeRemaining = Long.parseLong(tokens[1]) + delay - System.currentTimeMillis();
							if (timeRemaining > 0) {
								raiser.say("Przepraszam, ale nic dla Ciebie nie mam. Może mógłbyś wrócić później. Muszę posprzątać szkołę raz w tygodniu.");
								return;
							}
							raiser.say("Czy mógłbyś mi znowu pomóc?");
							raiser.setCurrentState(ConversationStates.QUEST_OFFERED);
						} else {
							raiser.say("Dziękuję za pomoc. Teraz mogę spać spokojnie.");
						}
					}
				});

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "started", 5.0));
		//actions.add(new StartRecordingKillsAction(QUEST_SLOT,1,"spider", "poisonous spider", "giant spider"));


		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dobrze. Zejdź na dół do piwnicy i zabij tam wszystkie potwory!",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dobrze, muszę znaleźć kogoś innego kto mi pomoże w tej 'małej' robótce!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Morgrin");
		// support for old-style quests
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start")),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.hasKilled("pająk")
								&& player.hasKilled("pająk ptasznik")
								&& player.hasKilled("królowa pająków")) {
							raiser.say("Och dziękuję mój przyjacielu. Masz tutaj coś specjalnego mam to od Magów. Kto to był to nie wiem. Dla kogo to jajko było też nie wiem. Wiem tylko tyle, że Tobie może się przydać.");
							final Item mythegg = SingletonRepository.getEntityManager()
									.getItem("mityczne jajo");
							mythegg.setBoundTo(player.getName());
							player.equipOrPutOnGround(mythegg);
							player.addKarma(5.0);
							player.addXP(5000);
							player.setQuest(QUEST_SLOT, "killed;" + System.currentTimeMillis());
						} else {
							raiser.say("Idź na dół i zabij wszystkie potwory, nie zostało Tobie zbyt wiele czasu.");
						}
		 			}
				});

		// support for new quests.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "started")),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.getQuest(QUEST_SLOT, 1).equals("pająk") &&
							player.getQuest(QUEST_SLOT, 2).equals("pająk ptasznik") &&
							player.getQuest(QUEST_SLOT, 3).equals("królowa pająków")
							) {
							raiser.say("Och dziękuję mój przyjacielu. Masz tutaj coś specjalnego mam to od Magów. Kto to był to nie wiem. Dla kogo to jajko było też nie wiem. Wiem tylko tyle, że Tobie może się przydać.");
							final Item mythegg = SingletonRepository.getEntityManager()
									.getItem("mityczne jajo");
							mythegg.setBoundTo(player.getName());
							player.equipOrPutOnGround(mythegg);
							player.addKarma(5.0);
							player.addXP(5000);
							player.setQuest(QUEST_SLOT, "killed;" + System.currentTimeMillis());
						} else {
							raiser.say("Idź na dół i zabij wszystkie potwory, nie zostało Tobie zbyt wiele czasu.");
						}
		 			}
				});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zabij Pająki",
				"Dozorca Morgrin ze szkoły magów chce, abym wyczyścił piwnicę szkolną z pająków.",
				true);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "KillSpiders";
	}

	@Override
	public int getMinLevel() {
		return 70;
	}

	@Override
	public List<String> getHistory(final Player player) {
 		LinkedList<String> history = new LinkedList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return history;
		}
		final String questState = player.getQuest(QUEST_SLOT, 0);

		if ("rejected".equals(questState)) {
			history.add("Nie zgadzam się, aby pomóc Morgrin.");
			return history;
		}
		if ("killed".equals(questState)) {
			history.add("Zabiłem wszystkie pająki w piwnicy w szkole magów i dostałem mythical egg.");
			return history;
		}

		// we can be here only if player accepted this quest.
		history.add("Zgadzam się, aby pomóc Morgrin.");
		// checking which spiders player killed.
		final boolean sp1 = "pająk".equals(player.getQuest(QUEST_SLOT, 1));
		final boolean sp2 = "pająk ptasznik".equals(player.getQuest(QUEST_SLOT, 2));
		final boolean sp3 = "królowa pająków".equals(player.getQuest(QUEST_SLOT, 3));
		final boolean sp = "start".equals(player.getQuest(QUEST_SLOT, 0));
		if (sp1) {
			history.add("Zabiłem pająka w piwnicy.");
		}
		if (sp2) {
			history.add("Zabiłem pająka ptasznika w piwnicy.");
		}
		if (sp3) {
			history.add("Zabiłem królową pająków w piwnicy.");
		}
		if (sp1 && sp2 && sp3) {
			history.add("Zabiłem wszystkie 3 pająki w piwnicy. Teraz wracam do Morgrin, aby odebrać moją nagrodę.");
		}

		// here is support for old-style quest
		if (sp) {
			final boolean osp1 = player.hasKilled("pająk");
			final boolean osp2 = player.hasKilled("pająk ptasznik");
			final boolean osp3 = player.hasKilled("królowa pająków");
			if (osp1) {
				history.add("Zabiłem pająka w piwnicy.");
			}
			if (osp2) {
				history.add("Zabiłem pająka ptasznika w piwnicy.");
			}
			if (osp3) {
				history.add("Zabiłem królową pająków w piwnicy.");
			}
			if (osp1 && osp2 && osp3) {
				history.add("Zabiłem wszystkie 3 pająki w piwnicy. Teraz wracam do Morgrin, aby odebra moją nagrodę.");
			}
		}

		return history;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed;"),
				 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"killed;").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Morgrin";
	}

	@Override
	public String getRegion() {
		return Region.FADO_CAVES;
	}
}
