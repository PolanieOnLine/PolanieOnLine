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
import games.stendhal.server.entity.item.scroll.LastMinuteScroll;
import games.stendhal.server.entity.npc.ChatAction;
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
import games.stendhal.server.maps.Region;

public class BiletTurystyczny extends AbstractQuest {
	private static final String QUEST_SLOT = "bilet_turystyczny";
	private final SpeakerNPC npc = npcs.get("Juhas");

	// Level needed
	private static final int REQUIRED_LEVEL = 100;
	// Money
	private static final int REQUIRED_MONEY = 5000;
	private static final String SCROLL = "bilet turystyczny";
	// Time
	private static final int REQUIRED_MINUTES = 60 * 12;

	static boolean hasCompletedTrip(final Player player) {
		if (!player.hasQuest(QUEST_SLOT)) {
			return false;
		}
		final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
		if (tokens.length < 4) {
			return false;
		}
		return MathHelper.parseLongDefault(tokens[3], -1) > 0;
	}

	private void step_1() {
		// player says hi before starting the quest
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("bilet", "bilet turystyczny", "bilety"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT),
						new LevelGreaterThanCondition(REQUIRED_LEVEL-1)),
			ConversationStates.INFORMATION_1,
			"Nie wszystkie moje zwoje działają tak samo. Bilet turystyczny dostał taką nazwę celowo, bo brzmi niewinnie. Jego wzór pochodzi od Ozo z Zakopanego i otwiera czasowe przejście na pustynię. Ten wariant jest stabilny i potrafi sprowadzić podróżnika z powrotem. Jeśli chcesz wiedzieć więcej, zapytaj czym #handluję.", null);

		// player says hi before starting the quest
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("bilet", "bilet turystyczny", "bilety"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT),
						new LevelLessThanCondition(REQUIRED_LEVEL)),
			ConversationStates.ATTENDING,
			"To nie jest zwykły zwój podróżny. Przejście prowadzi na niebezpieczną pustynię, dlatego nie sprzedam ci biletu, dopóki nie zdobędziesz większego doświadczenia. Wróć, gdy osiągniesz poziom 100.", null);

		// player returns after finishing the quest (it is repeatable) after the
		// time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStartedCondition(QUEST_SLOT),
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES),
					new LevelGreaterThanCondition(REQUIRED_LEVEL-1)),
			ConversationStates.QUEST_OFFERED,
			"Widzę, że przejście cię nie zniechęciło. Chcesz kolejny bilet turystyczny?", null);

		// player returns after finishing the quest (it is repeatable) before
		// the time as finished
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("bilet", "bilet turystyczny", "bilety"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStartedCondition(QUEST_SLOT),
					new LevelGreaterThanCondition(REQUIRED_LEVEL-1),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
			ConversationStates.ATTENDING,
			null,
			new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Nie mam jeszcze gotowego następnego biletu. Wróć za co najmniej"));

		// player responds to word 'deal' - enough level
		npc.add(ConversationStates.INFORMATION_1,
			Arrays.asList("deal", "handluję", "umowa", "biletem", "bilety"),
			new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					new LevelGreaterThanCondition(REQUIRED_LEVEL-1)),
			ConversationStates.QUEST_OFFERED,
			"Zwykły zwój prowadzi do jednego znanego miejsca. Ten bilet na chwilę wiąże podróżnika z pustynią i później sprowadza go z powrotem. Ozo zachował dla siebie znacznie mniej stabilne wzory, dlatego dobrze najpierw poznać bezpieczniejszą odmianę przejścia. Bilet kosztuje "
								+ REQUIRED_MONEY
								+ " monet. Chcesz go kupić?",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
				Arrays.asList("bilety turystyczne"),
				null,
				ConversationStates.QUEST_OFFERED,
				"To ustabilizowane znaki przejścia prowadzące na pustynię. Nazwa bilet turystyczny ma po prostu nie zwracać uwagi na to, z jaką magią naprawdę mamy do czynienia.",
				null);

		// player responds to word 'deal' - low level
		npc.add(ConversationStates.INFORMATION_1,
			Arrays.asList("deal", "handluję", "umowa", "biletem", "bilety"),
			new AndCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					new LevelLessThanCondition(REQUIRED_LEVEL)),
			ConversationStates.ATTENDING,
			"Nie jesteś jeszcze gotowy na takie przejście. Wróć, gdy zdobędziesz więcej doświadczenia.",
			null);

		// player wants to take the beans but hasn't the money
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemWithHimCondition("money", REQUIRED_MONEY)),
			ConversationStates.ATTENDING,
			"Nie masz wystarczająco dużo pieniędzy. Wróć, gdy będziesz miał.",
			null);

		// player wants to take the beans
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasItemWithHimCondition("money", REQUIRED_MONEY),
				ConversationStates.ATTENDING,
				"Oto twój bilet. Użyj go, gdy będziesz gotowy. Po mniej więcej trzech godzinach znak sam sprowadzi cię z pustyni. Jeśli zechcesz wrócić wcześniej, poszukaj tam znaku z herbem Zakopanego, który zamknie przejście i odeśle cię z powrotem.",
				new MultipleActions(
						new DropItemAction("money", REQUIRED_MONEY),
						new EquipItemAction(SCROLL, 1, true),
						// this is still complicated and could probably be split out further
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
								player.incBoughtForItem(SCROLL, 1);

								if (player.hasQuest(QUEST_SLOT)) {
									final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
									if (tokens.length == 4) {
										// we stored an old time taken or set it to -1 (never taken), either way, remember this.
										player.setQuest(QUEST_SLOT, "bought;"
												+ System.currentTimeMillis() + ";taken;" + tokens[3]);
									} else {
										// it must have started with "done" (old quest slot status was done;timestamp), but now we store when the beans were taken.
										// And they haven't taken beans since
										player.setQuest(QUEST_SLOT, "bought;"
												+ System.currentTimeMillis() + ";taken;-1");
									}
								} else {
									// first time they bought beans here
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
			"Rozumiem. Takiej podróży nie warto zaczynać bez przekonania.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("Ozo", "ozo"),
			null,
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (hasCompletedTrip(player)) {
						npc.say("Skoro wróciłeś już z pustyni, Ozo powinien potraktować cię poważniej. Znajdziesz go w Zakopanem w okolicach Góry Smoka. Pamiętaj tylko, że jego bilety nie są tak przewidywalne jak moje.");
					} else {
						npc.say("Ozo nie ufa ludziom, którzy nie przeszli nawet przez stabilne przejście. Najpierw użyj mojego biletu turystycznego i wróć z pustyni. Dopiero wtedy warto go szukać.");
					}
				}
			});
	}

	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}
	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}

	@Override
	public void addToWorld() {
		/* login notifier to teleport away players logging into the dream world.
		 * there is a note in TimedTeleportScroll that it should be done there or its subclass.
		 */
		SingletonRepository.getLoginNotifier().addListener(new LoginListener() {
			@Override
			public void onLoggedIn(final Player player) {
				LastMinuteScroll scroll = (LastMinuteScroll) SingletonRepository.getEntityManager().getItem("bilet turystyczny");
				scroll.teleportBack(player);
			}

		});
		fillQuestInfo(
				"Bilet Turystyczny",
				"Juhas sprzedaje ustabilizowany bilet otwierający czasowe przejście na pustynię. Udany powrót może przekonać Ozo, że znasz już podstawy tej magii.",
				false);
		step_1();

	}

	@Override
	public int getMinLevel() {
		return REQUIRED_LEVEL;
	}

	@Override
	public boolean isCompleted(final Player player) {
		return hasCompletedTrip(player);
	}

	@Override
	public String getName() {
		return "Bilet Turystyczny";
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
