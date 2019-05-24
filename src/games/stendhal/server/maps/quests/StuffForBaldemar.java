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
package games.stendhal.server.maps.quests;

import java.util.Arrays;
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
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.logic.BringOrderedListOfItemsQuestLogic;
import games.stendhal.server.maps.quests.logic.ItemCollector;
import games.stendhal.server.util.TimeUtil;

/**
 * QUEST: The mithril shield forging.
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Baldemar, mithrilbourgh elite wizard, will forge a mithril shield.
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Baldemar tells you about shield.
 * <li> He offers to forge a mithril shield for you if you bring him what he
 * needs.
 * <li> You give him all he asks for.
 * <li> Baldemar checks if you have ever killed a black giant alone, or not
 * <li> Baldemar forges the shield for you
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> mithril shield
 * <li> 95000 XP
 * <li> some karma (25)
 * </ul>
 *
 *
 * REPETITIONS:
 * <ul>
 * <li> None.
 * </ul>
 */
public class StuffForBaldemar extends AbstractQuest {

	static final String TALK_NEED_KILL_GIANT = "Tą tarczę mogą otrzymać ci co zabili czarnego giganta bez pomocy innych osób.";

	private static final String I_WILL_NEED_MANY_THINGS = "Będę potrzebował wiele, wiele rzeczy: ";

	private static final String IN_EXACT_ORDER = "Wróć, gdy będziesz miał wszystko #dokładnie w tej kolejności!";

	private static final int REQUIRED_MINUTES = 10;

	private static final String QUEST_SLOT = "mithrilshield_quest";

	private final ItemCollector itemCollector = new ItemCollector();

	private final BringOrderedListOfItemsQuestLogic questLogic = new BringOrderedListOfItemsQuestLogic();

	public StuffForBaldemar() {
		itemCollector.require().item("sztabka mithrilu").pieces(20)
				.bySaying("Nie mogę #wykuć bez brakujących %s. Po wszystkim będzie TO tarcza z mithrilu.");
		itemCollector.require().item("obsydian")
				.bySaying("Potrzebuję kilku kamieni, aby zmiażdżyć je na proszek i wymieszać z mithrilem. Wciąż potrzebuję %s.");
		itemCollector.require().item("diament")
				.bySaying("Potrzebuję kilku kamieni, aby zmiażdżyć je na proszek i wymieszać z mithrilem. Wciąż potrzebuję %s.");
		itemCollector.require().item("szmaragd").pieces(5)
				.bySaying("Potrzebuję kilku kamieni, aby zmiażdżyć je na proszek i wymieszać z mithrilem. Wciąż potrzebuję %s.");
		itemCollector.require().item("rubin").pieces(10)
				.bySaying("Potrzebuję kilku kamieni, aby zmiażdżyć je na proszek i wymieszać z mithrilem. Wciąż potrzebuję %s.");
		itemCollector.require().item("szafir").pieces(10)
				.bySaying("Potrzebuję kilku kamieni, aby zmiażdżyć je na proszek i wymieszać z mithrilem. Wciąż potrzebuję %s.");
		itemCollector.require().item("czarna tarcza").bySaying("Potrzebuję %s, aby uformować konstrukcję dla twojej nowej tarczy.");
		itemCollector.require().item("magiczna tarcza płytowa")
				.bySaying("Potrzebuję %s na kawałki i części do twojej nowej tarczy.");
		itemCollector.require().item("sztabka złota").pieces(10)
				.bySaying("Potrzebuję %s, aby połączyć z mithrilem i żelazem.");
		itemCollector.require().item("żelazo").pieces(20).bySaying("Potrzebuję %s, aby połączyć z mithrilem i złotem.");
		itemCollector.require().item("czarna perła").pieces(10)
				.bySaying("Potrzebuję %s, aby zmielić na proszek do posypania na tarczę, aby dawała ładny połysk.");
		itemCollector.require().item("shuriken").pieces(20).bySaying(
				"Potrzebuję %s, aby przetopić z mithrilem, złotem i żelazem. To 'tajny' składnik, o którym wie tylko ty i ja. ;)");
		itemCollector.require().item("kolorowe kulki").pieces(15).bySaying("Mój syn potrzebuje nowych zabawek. Wciąż potrzebuję %s.");
		itemCollector.require().item("zima zaklęta w kuli").bySaying("KOCHAM te błyskotki z Athor. Wciąż potrzebuję %s.");

		questLogic.setItemCollector(itemCollector);
		questLogic.setQuest(this);
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Baldemar");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Mogę wykuć tarczę zrobioną z mithrilu z kilkoma innymi rzeczami. Czy chciałbyś, abym ją wykonał?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Wolałbym, abyś pozwolił mi się trochę rozerwać.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Dlaczego mi przeszkadzasz skoro jeszcze nie ukończyłeś swojego zadania?");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					String need = I_WILL_NEED_MANY_THINGS + questLogic.itemsStillNeeded(player) + ". " + IN_EXACT_ORDER;
					raiser.say(need);
					player.setQuest(QUEST_SLOT, "start;0;0;0;0;0;0;0;0;0;0;0;0;0;0");

				}
			});

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Nie mogę uwierzyć, że nie chcesz skorzystać z tej okazji! Musisz być szalony!!!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		npc.addReply(Arrays.asList("exact", "dokładnie"),
			"Jak już powiedziałem. Musisz mi dać dokładnie w tej kolejności.");
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Baldemar");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					boolean missingSomething = questLogic.proceedItems(player, raiser);

					if (player.hasKilledSolo("czarny olbrzym") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Teraz wykuję tarczę. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowa.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilledSolo("czarny olbrzym") && !missingSomething) {
							raiser.say(TALK_NEED_KILL_GIANT);
						}

						questLogic.updateQuantitiesInQuestStatus(player);
					}
				}
			});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;")),
				ConversationStates.IDLE, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					final long delay = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE;
					final long timeRemaining = Long.parseLong(tokens[1]) + delay
							- System.currentTimeMillis();

					if (timeRemaining > 0L) {
						raiser.say("Jeszcze nie wykułem twojej tarczy. Sprawdź za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					raiser.say("Skończyłem wykuwanie twojej nowej tarczy z mithrilu. Ciesz się. Teraz pójdę sprawdzić co Trillium położyła za ladą dla mnie. ;)");
					player.addXP(95000);
					player.addKarma(25);
					final Item mithrilshield = SingletonRepository.getEntityManager().getItem("tarcza z mithrilu");
					mithrilshield.setBoundTo(player.getName());
					player.equipOrPutOnGround(mithrilshield);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("forge", "missing", "wykuć", "brakuje"),
			new QuestStateStartsWithCondition(QUEST_SLOT, "start;"),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String questState = player.getQuest(QUEST_SLOT);
					if (!broughtAllItems(questState)) {
						raiser.say("Będę potrzebował " + questLogic.itemsStillNeeded(player) + ".");
					} else {
						if(!player.hasKilledSolo("czarny olbrzym")) {
							raiser.say(TALK_NEED_KILL_GIANT);
						}
					}
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Rzeczy dla Baldemara",
				"Baldemar przyjazny, elitarny czarodziej mithrilbourghtó wykuje dla ciebie specjalną tarczę.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "StuffForBaldemar";
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new LinkedList<>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Spotkałem Baldemar w  magic theater.");
		if (questState.equals("rejected")) {
			res.add("Nie jestem zainteresowany tarczą wykonaną z mithrilu.");
			return res;
		}
		res.add("Baldemar zapytał mnie o przyniesienie wielu rzeczy.");
		if (questState.startsWith("start") && !broughtAllItems(questState)) {
			String suffix = ".";
			if (questLogic.neededItemsWithAmounts(player).size() > 1) {
				suffix = " w tej kolejności.";
			}
			res.add("Wciąż potrzebuję przynieść " + questLogic.itemsStillNeeded(player) + suffix);
		} else if (broughtAllItems(questState) || !questState.startsWith("start")) {
			res.add("Zaniosłem wszystkie specjalne przedmioty do Baldemara.");
		}
		if (broughtAllItems(questState) && !player.hasKilledSolo("black giant")) {
			res.add("I will need to bravely face a black giant alone, before I am worthy of this shield.");
		}
		if (questState.startsWith("forging")) {
			res.add("Baldemar wykuwa dla mnie tarczę z mithrilu!");
		}
		if (isCompleted(player)) {
			res.add("Dostarczyłem Baldemarowi potrzebne surowce, zabiłem sam czarnego olbrzyma. W nagrodę dostałem tarcze z mithrilu.");
		}
		return res;
	}

	private boolean broughtAllItems(final String questState) {
		return "start;20;1;1;5;10;10;1;1;10;20;10;20;15;1".equals(questState);
	}

	@Override
	public int getMinLevel() {
		return 100;
	}

	@Override
	public String getNPCName() {
		return "Baldemar";
	}

	@Override
	public String getRegion() {
		return Region.FADO_CAVES;
	}
}
