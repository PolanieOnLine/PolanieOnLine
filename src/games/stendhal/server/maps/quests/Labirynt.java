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
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.scroll.MagicznyScroll;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * QUEST: Labirynt
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Ozo, a seller in magiczny bilet
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>The NPC sells magiczny bilet to players above level 30</li>
 * <li>When used, magiczny bilet teleport you to a dreamworld full of strange
 * sights, hallucinations and the creatures of your nightmares</li>
 * <li>You can remain there for up to 30 minutes</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>The dream world is really cool!</li>
 * <li>XP from creatures you kill there</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>No more than once every 6 hours</li>
 * </ul>
 *
 * NOTES:
 * <ul>
 * <li>The area of the dreamworld will be a no teleport zone</li>
 * <li>You can exit via a portal if you want to exit before the 30 minutes is
 * up</li>
 * </ul>
 */
public class Labirynt extends AbstractQuest {
	private static final String QUEST_SLOT = "ozo";
	private final SpeakerNPC npc = npcs.get("Ozo");

	private static final int REQUIRED_LEVEL = 250;
	private static final int REQUIRED_MONEY = 20000;

	private static final int REQUIRED_WEEK = 2 * 7;

	private ChatCondition completedTouristTripCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				return BiletTurystyczny.hasCompletedTrip(player);
			}
		};
	}

	private void step_1() {
		final ChatCondition completedTouristTrip = completedTouristTripCondition();

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(completedTouristTrip)),
			ConversationStates.ATTENDING,
			"Jeśli szukasz magicznego biletu, zaczynasz od złej strony. Najpierw znajdź Juhasa w tawernie w Semos, użyj jego biletu turystycznego i wróć z pustyni. Jego przejście jest stabilne. Jeśli nie poradzisz sobie z nim, nie powierzam ci mojego.", null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotStartedCondition(QUEST_SLOT),
					completedTouristTrip),
			ConversationStates.INFORMATION_1,
			"A więc wróciłeś z pustyni po użyciu biletu Juhasa. Dobrze. Skoro stabilne przejście cię nie złamało, mogę powiedzieć ci o czymś mniej przewidywalnym. Zapytaj, czym #handluję.", null);

		// player returns after finishing the quest (it is repeatable) after the
		// time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStartedCondition(QUEST_SLOT),
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_WEEK)),
			ConversationStates.QUEST_OFFERED,
			"Wróciłeś. Chcesz ponownie otworzyć przejście do labiryntu?", null);

		// player returns after finishing the quest (it is repeatable) before
		// the time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStartedCondition(QUEST_SLOT),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_WEEK))),
			ConversationStates.ATTENDING,
			null,
			new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_WEEK,
					"Przejście jeszcze się nie uspokoiło. Następny bilet mogę przygotować za co najmniej "));

		// player responds to word 'deal' - enough level
		npc.add(ConversationStates.INFORMATION_1,
			Arrays.asList("deal", "handluję", "bilet", "magiczny bilet"),
			new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					completedTouristTrip,
					new LevelGreaterThanCondition(REQUIRED_LEVEL-1)),
			ConversationStates.QUEST_OFFERED,
			"Juhas używa przejścia związanego z jednym znanym miejscem. Mój #'magiczny bilet' otwiera drogę do starego labiryntu, który nie zachowuje się jak zwykła mapa. Nie obiecam ci, co spotkasz po drugiej stronie, ale znak powrotu jest prawdziwy. Bilet kosztuje "
								+ REQUIRED_MONEY
								+ " monet. Chcesz go kupić?",
			null);

		// player responds to word 'deal' - low level
		npc.add(ConversationStates.INFORMATION_1,
			Arrays.asList("deal", "handluję", "bilet", "magiczny bilet"),
			new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					completedTouristTrip,
					new LevelLessThanCondition(REQUIRED_LEVEL)),
			ConversationStates.ATTENDING,
			"Bilet Juhasa wystarczył, żebym uwierzył, że rozumiesz działanie przejść. To jednak nie znaczy, że poradzisz sobie w moim labiryncie. Wróć, gdy osiągniesz poziom 250.",
			null);

		// player wants to take the beans but hasn't the money
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemWithHimCondition("money", REQUIRED_MONEY)),
			ConversationStates.ATTENDING,
			"Nie masz wystarczająco dużo pieniędzy. Wróć, gdy zbierzesz potrzebną kwotę.",
			null);

		// player wants to take the beans
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasItemWithHimCondition("money", REQUIRED_MONEY),
				ConversationStates.ATTENDING,
				"Masz bilet. To nie jest wycieczka. Po użyciu znak otworzy przejście do labiryntu. Powrót nastąpi po około pięciu godzinach. Jeśli zechcesz wrócić wcześniej, poszukaj wyjścia wewnątrz labiryntu.",
				new MultipleActions(
						new DropItemAction("money", REQUIRED_MONEY),
						new EquipItemAction("magiczny bilet", 1, true),
						// this is still complicated and could probably be split out further
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
								if (player.hasQuest(QUEST_SLOT)) {
									final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
									if (tokens.length == 4) {
										// we stored a last time (or set it to -1), either way, remember this.
										player.setQuest(QUEST_SLOT, "bought;"
												+ System.currentTimeMillis() + ";taken;" + tokens[3]);
									} else {
										// old quest state without a recorded use time
										player.setQuest(QUEST_SLOT, "bought;"
												+ System.currentTimeMillis() + ";taken;-1");
									}
								} else {
									player.setQuest(QUEST_SLOT, "bought;"
											+ System.currentTimeMillis() + ";taken;-1");
								}
							}
						}));

		// player is not willing to experiment
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Rozsądnie. Do takiego przejścia nie powinno się wchodzić bez przekonania.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("deal", "handluję", "bilet", "magiczny bilet", "yes", "tak"),
			new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(completedTouristTrip)),
			ConversationStates.ATTENDING,
			"Najpierw wróć z pustyni po użyciu biletu Juhasa. Dopiero wtedy porozmawiamy o moim przejściu.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("deal", "handluję", "bilet", "magiczny bilet", "yes", "tak"),
			new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					completedTouristTrip,
					new LevelGreaterThanCondition(REQUIRED_LEVEL-1)),
			ConversationStates.ATTENDING,
			"Już powiedziałem ci, czym różni się mój bilet od przejścia Juhasa. Jeśli nadal chcesz go kupić, wróć do naszej rozmowy od początku.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("deal", "handluję", "bilet", "magiczny bilet", "yes", "tak"),
			new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					completedTouristTrip,
					new LevelLessThanCondition(REQUIRED_LEVEL)),
			ConversationStates.ATTENDING,
			"Wiesz już, jak działa stabilne przejście, ale na mój labirynt jest jeszcze za wcześnie. Wróć na poziomie 250.",
			null);
	}

	@Override
	public void addToWorld() {
		/* login notifier to teleport away players logging into the dream world.
		 * there is a note in TimedTeleportScroll that it should be done there or its subclass.
		 */
		SingletonRepository.getLoginNotifier().addListener(new LoginListener() {
			@Override
			public void onLoggedIn(final Player player) {
			    MagicznyScroll scroll = (MagicznyScroll) SingletonRepository.getEntityManager().getItem("magiczny bilet");
				scroll.teleportBack(player);
			}

		});
		fillQuestInfo(
				"Magiczny Bilet",
				"Po bezpieczniejszej podróży biletem Juhasa Ozo może dopuścić do znacznie mniej przewidywalnego przejścia prowadzącego do starego labiryntu.",
				false);
		step_1();
	}

	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Magiczny Bilet";
	}

	@Override
	public int getMinLevel() {
		return REQUIRED_LEVEL;
	}

	@Override
	public boolean isCompleted(final Player player) {
		if(!player.hasQuest(QUEST_SLOT)) {
			return false;
		}
		String[] tokens = player.getQuest(QUEST_SLOT).split(";");
		if (tokens.length < 4) {
			return false;
		}
		return MathHelper.parseLongDefault(tokens[3],-1)>0;
	}

	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
