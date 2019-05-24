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
 * QUEST: The immortal sword forging.
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Vulcanus, son of Zeus itself, will forge for you the god's sword.
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Vulcanus tells you about the sword.
 * <li> He offers to forge a immortal sword for you if you bring him what it
 * needs.
 * <li> You give him all what he ask you.
 * <li> He tells you you must have killed a giant to get the shield
 * <li> Vulcanus forges the immortal sword for you
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> immortal sword
 * <li> 15000 XP
 * <li> some karma (25)
 * </ul>
 * 
 * 
 * REPETITIONS:
 * <ul>
 * <li> None.
 * </ul>
 */
public class StuffForVulcanus extends AbstractQuest {

	private static final String I_WILL_NEED_SEVERAL_THINGS = "Będę potrzebował kilku rzeczy: ";

	private static final String IN_EXACT_ORDER = "Wróć, gdy będziesz je miał #dokładnie w tej kolejności!";

	private static final int REQUIRED_MINUTES = 10;

	private static final String QUEST_SLOT = "immortalsword_quest";

	private final ItemCollector itemCollector = new ItemCollector();

	private final BringOrderedListOfItemsQuestLogic questLogic = new BringOrderedListOfItemsQuestLogic();

	public StuffForVulcanus() {
		itemCollector.require().item("żelazo").pieces(50).bySaying("Nie mogę #wykuć bez %s.");
		itemCollector.require().item("polano").pieces(100).bySaying("Jak możesz wymagać #wykucia skoro nie masz %s do ognia?");
		itemCollector.require().item("sztabka złota").pieces(15).bySaying("Muszę zapłacić rachunek duchom za włożenie uczuć w ten miecz. Potrzebuję %s.");
		itemCollector.require().item("serce olbrzyma").pieces(10).bySaying("To główny składnik uczuć. Potrzebuję %s.");

		questLogic.setItemCollector(itemCollector);
		questLogic.setQuest(this);
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Vulcanus");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Raz wykułem najpotężniejszy spośród mieczy. Mogę to zrobić ponownie dla Ciebie. Jesteś zainteresowany?");
					} else if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Och! Jestem bardzo zmęczony. Później do mnie zajrzyj. Potrzebuję kilku lat na relaksowanie.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Dlaczego zawracasz mi głowę skoro nie ukończyłeś zadania?");
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
					raiser.say(I_WILL_NEED_SEVERAL_THINGS + questLogic.itemsStillNeeded(player) + ". " + IN_EXACT_ORDER);
					player.setQuest(QUEST_SLOT, "start;0;0;0;0");
				}
			});

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Och, zapomnij o tym jeżeli nie potrzebujesz miecza nieśmiertelnych...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		npc.addReply("exact",
			"Ta archaiczna magia potrzebuje tych składników w dokładnie podanej kolejności.");
	}

	private void step_2() {
		/* Get the stuff. */
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Vulcanus");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					boolean missingSomething = questLogic.proceedItems(player, raiser);

					if (player.hasKilled("olbrzym") && !missingSomething) {
						raiser.say("Przyniosłeś wszystko. Muszę zrobić nieśmiertelny miecz. Poza tym jesteś wystarczająco silny, aby władać nim. Wróć za "
							+ REQUIRED_MINUTES
							+ " minutę" + ", a będzie gotowy.");
						player.setQuest(QUEST_SLOT, "forging;" + System.currentTimeMillis());
					} else {
						if (!player.hasKilled("olbrzym") && !missingSomething) {
							raiser.say("Naprawdę własnoręcznie zdobyłeś te serce olbrzyma? Nie sądzę! Ten potężny miecz może być dany tylko tym, którzy są wystarczająco silni, aby zabić #olbrzyma.");
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
						raiser.say("Jeszcze nie skończyłem wykuwania miecza. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ".");
						return;
					}

					raiser.say("Skończyłem wykuwanie nieśmiertelnika. Zasługujesz na niego. Teraz pozwolisz, że udam się na długi odpoczynek. Dowidzenia!");
					player.addXP(15000);
					player.addKarma(25);
					final Item magicSword = SingletonRepository.getEntityManager().getItem("miecz nieśmiertelnych");
					magicSword.setBoundTo(player.getName());
					player.equipOrPutOnGround(magicSword);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("forge", "missing", "wykuj", "brakuje", "lista", "przypomnij"), 
			new QuestStateStartsWithCondition(QUEST_SLOT, "start;"),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String questState = player.getQuest(QUEST_SLOT);
					if (!broughtAllItems(questState)) {
						raiser.say("Będę potrzebował " + questLogic.itemsStillNeededWithHash(player) + ".");
					}
				}
			});

		npc.add(ConversationStates.ANY,
				"żelazo",
				null,
				ConversationStates.ATTENDING,
				"Zbierz kilka rud żelaza, które są bogate w minerały.",
				null);
		npc.add(ConversationStates.ANY,
				"polano",
				null,
				ConversationStates.ATTENDING,
				"W lesie jest pełno drewna.",
				null);
		npc.add(ConversationStates.ANY,
				Arrays.asList("gold", "gold bar", "złoto", "sztabka złota"),
				null,
				ConversationStates.ATTENDING,
				"Kowal w Ados może dla Ciebie odlać bryłki złoto w sztabki złota.",
				null);
		npc.add(ConversationStates.ANY,
				Arrays.asList("giant", "giant heart", "olbrzym", "serce olbrzyma"),
				null,
				ConversationStates.ATTENDING,
				"Są starodawne legendy o olbrzymach żyjących w górach na północ od Semos i Ados.",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Rzeczy dla Vulcanusa",
				"Vulcanus syn Zeusa wykuje dla Ciebie boski miecz.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "StuffForVulcanus";
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new LinkedList<>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Spotkałem Vulcanus w Kotoch.");
		if (questState.equals("rejected")) {
			res.add("Nie potrzebny mi miecz nieśmiertelnych.");
			return res;
		}
		res.add("Aby wykuć miecz nieśmiertelnych muszę przynieść kilka rzeczy Vulkanusowi.");
		if (questState.startsWith("start") && !broughtAllItems(questState)) {
			String suffix = ".";
			if (questLogic.neededItemsWithAmounts(player).size() > 1) {
				suffix = ", w tej kolejności.";
			}
			res.add("Wciąż potrzebuję dostarczyć " + questLogic.itemsStillNeeded(player) + suffix);
		} else if (broughtAllItems(questState) || !questState.startsWith("start")) {
			res.add("Dostarczyłem wszystko co potrzebne dla Vulcanus.");
		}
		if (broughtAllItems(questState) && !player.hasKilled("giant")) {
			res.add("Aby zasłużyć na miecz muszę zabić pare gigantów i zebrać ich serca.");
		}
		if (questState.startsWith("forging")) {
			res.add("Vulcanus, syn Zeusa wykuwa mi miecz.");
		}
		if (isCompleted(player)) {
			res.add("Za sztabki złota serca olbrzymów i pare innach drobiazgów zostałem nagrodzony mieczem nieśmiertelnych.");
		}
		return res;
	}

	private boolean broughtAllItems(final String questState) {
		return "start;15;26;12;6".equals(questState);
	}

	// match to the min level of the immortal sword
	@Override
	public int getMinLevel() {
		return 80;
	}

	@Override
	public String getNPCName() {
		return "Vulcanus";
	}
	
	@Override
	public String getRegion() {
		return Region.KOTOCH;
	}
}
