/***************************************************************************
 *                   (C) Copyright 2010-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
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
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;
import games.stendhal.server.util.RequiredKillsInfo;
import games.stendhal.server.util.TimeUtil;

/** Second social-status trial: the path from townsman to knight. */
public class PierscienRycerza extends AbstractQuest {
	static final String QUEST_SLOT = "pierscien_rycerza";
	static final String STATE_REJECTED = "rejected";
	static final String STATE_TRIAL = "trial";
	static final String STATE_MATERIALS = "materials";
	static final String STATE_LEGACY_ITEMS = "przedmioty";
	static final String STATE_DONE = "done";
	static final String FORGING_PREFIX = "forging;";

	static final String TRIAL_SLOT = "pierscien_rycerza_trial";
	static final String TRIAL_AWAITING = "awaiting";
	static final String TRIAL_WATCH = "watch";
	static final String TRIAL_WITNESSED = "witnessed";
	static final int TRIAL_STATE_INDEX = 0;
	static final int TRIAL_KILLS_INDEX = 1;
	static final int TRIAL_STARTED_INDEX = 2;
	static final int WATCH_MINUTES = 20;
	private static final String WATCH_SWAMP_WRAITH = "pokutnik z bagien";
	private static final String WATCH_MEADOW_WRAITH = "pokutnik z łąk";
	private static final String WATCH_MOUNTAIN_WRAITH = "pokutnik z gór";
	private static final String WATCH_GIANT = "superczłowiek olbrzym";
	private static final String WATCH_EAGLE = "orzeł gigant";
	private static final String WATCH_PEGASUS = "pegaz brązowy";
	private static final int WATCH_SWAMP_WRAITHS_REQUIRED = 2;
	private static final int WATCH_MEADOW_WRAITHS_REQUIRED = 1;
	private static final int WATCH_MOUNTAIN_WRAITHS_REQUIRED = 1;
	private static final int WATCH_GIANTS_REQUIRED = 2;
	private static final int WATCH_EAGLES_REQUIRED = 1;
	private static final int WATCH_PEGASUS_REQUIRED = 1;

	static final String MITHRILSHIELD_QUEST_SLOT = "mithrilshield_quest";
	private static final int REQUIRED_LEVEL = 250;
	private static final int REQUIRED_IRON = 30;
	private static final int REQUIRED_GOLD = 10;
	private static final int REQUIRED_MITHRIL = 5;
	private static final int FORGING_MINUTES = 90;
	private static final int REWARD_XP = 100000;
	private static final Logger logger = Logger.getLogger(PierscienRycerza.class);

	private final SpeakerNPC npc = npcs.get("Edgard");
	private final SpeakerNPC witness = npcs.get("Zakonnik");

	private ChatCondition newTrialCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				final boolean notStarted = !player.hasQuest(QUEST_SLOT)
						|| STATE_REJECTED.equals(player.getQuest(QUEST_SLOT));
				return notStarted
						&& !player.isBadBoy()
						&& player.getLevel() >= REQUIRED_LEVEL
						&& player.isQuestCompleted(PierscienMieszczanina.QUEST_SLOT)
						&& player.isQuestCompleted(MITHRILSHIELD_QUEST_SLOT)
						&& player.isEquipped("pierścień mieszczanina");
			}
		};
	}

	private ChatCondition blockedNewTrialCondition() {
		return new AndCondition(
				new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestInStateCondition(QUEST_SLOT, STATE_REJECTED)),
				new NotCondition(newTrialCondition()));
	}

	private OrCondition materialsStateCondition() {
		return new OrCondition(
				new QuestInStateCondition(QUEST_SLOT, STATE_MATERIALS),
				new QuestInStateCondition(QUEST_SLOT, STATE_LEGACY_ITEMS));
	}

	private ChatCondition trialPhaseCondition(final String phase) {
		return new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL),
				new QuestInStateCondition(TRIAL_SLOT, TRIAL_STATE_INDEX, phase));
	}

	private ChatCondition watchDurationElapsedCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				return isWatchDurationElapsed(player);
			}
		};
	}

	private boolean isWatchDurationElapsed(final Player player) {
		if (!player.hasQuest(TRIAL_SLOT)) {
			return false;
		}
		try {
			final String startedValue = player.getQuest(TRIAL_SLOT, TRIAL_STARTED_INDEX);
			if (startedValue == null || startedValue.length() == 0) {
				return true;
			}
			final long started = Long.parseLong(startedValue);
			return System.currentTimeMillis() >= started + WATCH_MINUTES * TimeUtil.MILLISECONDS_IN_MINUTE;
		} catch (final RuntimeException e) {
			return true;
		}
	}

	private long remainingWatchMillis(final Player player) {
		try {
			final String startedValue = player.getQuest(TRIAL_SLOT, TRIAL_STARTED_INDEX);
			final long started = Long.parseLong(startedValue);
			return Math.max(0L,
					started + WATCH_MINUTES * TimeUtil.MILLISECONDS_IN_MINUTE - System.currentTimeMillis());
		} catch (final RuntimeException e) {
			return 0L;
		}
	}

	private ChatCondition finishedWatchCondition() {
		return new AndCondition(
				trialPhaseCondition(TRIAL_WATCH),
				new KilledForQuestCondition(TRIAL_SLOT, TRIAL_KILLS_INDEX),
				watchDurationElapsedCondition());
	}

	private ChatCondition unfinishedWatchCondition() {
		return new AndCondition(
				trialPhaseCondition(TRIAL_WATCH),
				new NotCondition(new AndCondition(
						new KilledForQuestCondition(TRIAL_SLOT, TRIAL_KILLS_INDEX),
						watchDurationElapsedCondition())));
	}

	private ChatCondition trialWithoutWitnessCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				if (!player.isQuestInState(QUEST_SLOT, STATE_TRIAL)) {
					return false;
				}
				return !player.hasQuest(TRIAL_SLOT)
						|| !TRIAL_WITNESSED.equals(player.getQuest(TRIAL_SLOT, TRIAL_STATE_INDEX));
			}
		};
	}

	private AndCondition hasForgingMaterials() {
		return new AndCondition(
				new PlayerHasItemWithHimCondition("pierścień mieszczanina", 1),
				new PlayerHasItemWithHimCondition("sztabka żelaza", REQUIRED_IRON),
				new PlayerHasItemWithHimCondition("sztabka złota", REQUIRED_GOLD),
				new PlayerHasItemWithHimCondition("bryłka mithrilu", REQUIRED_MITHRIL));
	}

	private ChatCondition forgingCondition(final boolean ready) {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				if (!player.hasQuest(QUEST_SLOT)) {
					return false;
				}
				final String state = player.getQuest(QUEST_SLOT);
				if (state == null || !state.startsWith(FORGING_PREFIX)) {
					return false;
				}
				return isForgingReady(state) == ready;
			}
		};
	}

	private boolean isForgingReady(final String state) {
		try {
			final long started = Long.parseLong(state.substring(FORGING_PREFIX.length()));
			return System.currentTimeMillis() >= started + FORGING_MINUTES * TimeUtil.MILLISECONDS_IN_MINUTE;
		} catch (final RuntimeException e) {
			return true;
		}
	}

	private long remainingForgingMillis(final Player player) {
		try {
			final String state = player.getQuest(QUEST_SLOT);
			final long started = Long.parseLong(state.substring(FORGING_PREFIX.length()));
			return Math.max(0L,
					started + FORGING_MINUTES * TimeUtil.MILLISECONDS_IN_MINUTE - System.currentTimeMillis());
		} catch (final RuntimeException e) {
			return 0L;
		}
	}

	private ChatAction startForgingAction() {
		return new MultipleActions(
				new DropItemAction("pierścień mieszczanina", 1),
				new DropItemAction("sztabka żelaza", REQUIRED_IRON),
				new DropItemAction("sztabka złota", REQUIRED_GOLD),
				new DropItemAction("bryłka mithrilu", REQUIRED_MITHRIL),
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						player.setQuest(QUEST_SLOT, FORGING_PREFIX + System.currentTimeMillis());
					}
				});
	}

	private ChatAction finishForgingAction() {
		return new MultipleActions(
				new EquipItemAction("pierścień rycerza", 1, true,
						ItemCreationContext.questReward()),
				new IncreaseXPAction(REWARD_XP),
				new SetQuestAction(QUEST_SLOT, STATE_DONE));
	}

	private void prepareOffer() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_DONE),
				ConversationStates.ATTENDING,
				"Przysięga została już złożona, a znak Zakonu nosisz przy sobie. Rycerstwo zaczyna się po ceremonii, nie kończy na niej.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				blockedNewTrialCondition(),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.isBadBoy()) {
							raiser.say("Na twoich dłoniach ciąży jeszcze piętno zabitego rycerza. Zakon nie przyjmie przysięgi, dopóki go nie zmyjesz.");
						} else if (player.getLevel() < REQUIRED_LEVEL) {
							raiser.say("Do tej przysięgi trzeba nie tylko odwagi, lecz doświadczenia. Wróć, gdy osiągniesz poziom 250.");
						} else if (!player.isQuestCompleted(PierscienMieszczanina.QUEST_SLOT)) {
							raiser.say("Najpierw naucz się być człowiekiem, na którym może polegać wspólnota. Bez próby mieszczanina nie ma drogi do rycerskiego słowa.");
						} else if (!player.isQuestCompleted(MITHRILSHIELD_QUEST_SLOT)) {
							raiser.say("Mistrz chce najpierw zobaczyć, czy umiesz doprowadzić próbę tarczy z mithrilu do końca. Wróć po niej.");
						} else if (!player.isEquipped("pierścień mieszczanina")) {
							raiser.say("Przynieś swój pierścień mieszczanina. Nowy znak nie ma wymazać starej drogi. Zostanie z niej przekuty.");
						}
					}
				});

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				newTrialCondition(),
				ConversationStates.INFORMATION_7,
				"Pierścień mieszczanina mówi, że wspólnota nauczyła się na tobie polegać. Rycerz ma zrobić krok dalej. Gdy ci sami ludzie nie mogą obronić drogi, to ty masz stanąć przed nimi. Takie zobowiązanie zaczyna #przysięga.",
				null);

		npc.add(ConversationStates.INFORMATION_7,
				Arrays.asList("przysięga", "przysiega"),
				newTrialCondition(),
				ConversationStates.INFORMATION_8,
				"Nie pytamy, czy umiesz zwyciężać. Tarcza z mithrilu już dowiodła wytrwałości. Pytamy, czy staniesz między niebezpieczeństwem a słabszym nawet wtedy, gdy nie czeka cię za to łup. To jest #obowiązek.",
				null);

		npc.add(ConversationStates.INFORMATION_8,
				Arrays.asList("obowiązek", "obowiazek"),
				newTrialCondition(),
				ConversationStates.QUEST_OFFERED,
				"Jeśli złożysz słowo, Zakon sprawdzi je na pełnej warcie. Bierzesz na siebie obowiązek bronić drogi, wspólnoty i tych, którzy sami nie utrzymają tarczy?",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				newTrialCondition(),
				ConversationStates.ATTENDING,
				"Słowo przyjęte. Z kilku stron przyszły meldunki o zagrożeniach na szlaku wokół zamku. Prowadzą od bagien aż po górski trakt. Zakonnik zbiera te wieści na murze w północnej części zamku od strony wschodniej. Powiedz mu o #warcie. On sprawdzi, czy potraktujesz ją jak obowiązek, a nie polowanie po łup.",
				new MultipleActions(
						new SetQuestAction(QUEST_SLOT, STATE_TRIAL),
						new SetQuestAction(TRIAL_SLOT, TRIAL_STATE_INDEX, TRIAL_AWAITING),
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								player.addKarma(10);
							}
						}));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				newTrialCondition(),
				ConversationStates.IDLE,
				"Dobrze. Przysięgi wypowiedzianej bez przekonania lepiej nie składać. Wróć, gdy słowo będzie ważyć więcej niż strach.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, STATE_REJECTED, -10.0));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				trialWithoutWitnessCondition(),
				ConversationStates.ATTENDING,
				"Słowo już padło. Teraz nie mnie masz przekonywać. Zakonnik czeka na murze w północnej części zamku od strony wschodniej, by przyjąć twoją #wartę. To on ma zobaczyć, czy wytrwasz przy obowiązku.",
				null);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("warta", "wartę", "warte", "warcie", "zakonnik"),
				trialWithoutWitnessCondition(),
				ConversationStates.ATTENDING,
				"Znajdziesz Zakonnika na murze w północnej części zamku od strony wschodniej. Pilnuje szlaku wokół zamku, na którym zagrożenia wracają falami. Tam liczy się nie jeden pojedynek, lecz wytrwanie na powierzonym miejscu.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				trialPhaseCondition(TRIAL_WITNESSED),
				ConversationStates.ATTENDING,
				"Zakonnik przekazał mi swoje słowo. Nie pytał, ile krwi zostało na twoim mieczu. Powiedział tylko, że droga była bezpieczna, gdy inni musieli nią przejść, a ty nie porzuciłeś służby. Takie #świadectwo ma dla Zakonu wagę.",
				null);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("świadectwo", "swiadectwo"),
				trialPhaseCondition(TRIAL_WITNESSED),
				ConversationStates.ATTENDING,
				"Świadek potwierdził czyn, więc słowo może dostać własny znak. Twój pierścień mieszczanina stanie się rdzeniem nowej obręczy. Kuźnia potrzebuje jeszcze kilku #materiałów.",
				new SetQuestAction(QUEST_SLOT, STATE_MATERIALS));
	}

	private void prepareWatch() {
		witness.add(ConversationStates.ATTENDING,
				Arrays.asList("warta", "wartę", "warte", "warcie"),
				new OrCondition(
						trialPhaseCondition(TRIAL_AWAITING),
						new AndCondition(
								new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL),
								new QuestNotStartedCondition(TRIAL_SLOT))),
				ConversationStates.ATTENDING,
				"Edgard uprzedził mnie o twoim słowie. Nie wyznaczę ci jednej bitwy, po której od razu wrócisz po nagrodę. Meldunki z różnych stron wskazują, że cały szlak wokół zamku staje się niebezpieczny. Na #bagnach widziano dwóch pokutników, na #łąkach jednego, a w #górach kolejnego. Dalej dwóch #olbrzymów spycha wszystko ku drodze, a nad traktem krążą zagrożenia z #nieba. Od tej chwili przez co najmniej dwadzieścia minut odpowiadasz za ten szlak. Sprawdź każdy meldunek i wróć, gdy warta naprawdę będzie odsłużona.",
				new MultipleActions(
						new SetQuestAction(TRIAL_SLOT, TRIAL_STATE_INDEX, TRIAL_WATCH),
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								player.setQuest(TRIAL_SLOT, TRIAL_STARTED_INDEX,
										Long.toString(System.currentTimeMillis()));
							}
						},
						new StartRecordingKillsAction(TRIAL_SLOT, TRIAL_KILLS_INDEX,
								new RequiredKillsInfo(WATCH_SWAMP_WRAITH, 0, WATCH_SWAMP_WRAITHS_REQUIRED),
								new RequiredKillsInfo(WATCH_MEADOW_WRAITH, 0, WATCH_MEADOW_WRAITHS_REQUIRED),
								new RequiredKillsInfo(WATCH_MOUNTAIN_WRAITH, 0, WATCH_MOUNTAIN_WRAITHS_REQUIRED),
								new RequiredKillsInfo(WATCH_GIANT, 0, WATCH_GIANTS_REQUIRED),
								new RequiredKillsInfo(WATCH_EAGLE, 0, WATCH_EAGLES_REQUIRED),
								new RequiredKillsInfo(WATCH_PEGASUS, 0, WATCH_PEGASUS_REQUIRED))));

		witness.add(ConversationStates.ATTENDING,
				Arrays.asList("bagna", "bagnach"),
				trialPhaseCondition(TRIAL_WATCH), ConversationStates.ATTENDING,
				"Pierwszy meldunek przyszedł od ludzi omijających mokradła. Dwa pokutniki z bagien podeszły pod drogę bliżej niż zwykle. Nie szukaj ich dla trofeum. Usuń je, bo tędy wracają zielarze i drwale. Potem sprawdź #łąki.", null);

		witness.add(ConversationStates.ATTENDING,
				Arrays.asList("łąki", "laki", "łąkach", "lakach"),
				trialPhaseCondition(TRIAL_WATCH), ConversationStates.ATTENDING,
				"Na łąkach widziano jednego pokutnika z łąk. Sam nie byłby powodem do alarmu, gdyby nie to, że podobne zjawy wyszły jednocześnie z bagien i gór. To wygląda, jakby coś wypychało je ku ludziom. Dalej prowadzą ślady w #góry.", null);

		witness.add(ConversationStates.ATTENDING,
				Arrays.asList("góry", "gory", "górach", "gorach"),
				trialPhaseCondition(TRIAL_WATCH), ConversationStates.ATTENDING,
				"W górach trzeba usunąć jednego pokutnika z gór. Zwiadowcy znaleźli tam też połamane drzewa i kamienie zepchnięte z traktu. To robota czegoś cięższego niż zjawa. Sprawdź #olbrzymy.", null);

		witness.add(ConversationStates.ATTENDING,
				Arrays.asList("olbrzymy", "olbrzymów", "olbrzymow"),
				trialPhaseCondition(TRIAL_WATCH), ConversationStates.ATTENDING,
				"Dwóch superczłowieków olbrzymów schodzi w stronę obejścia. To już nie płoszenie podróżnych, tylko zagrożenie, którego zwykła straż może nie zatrzymać. Ich ruch spłoszył także stworzenia z #nieba.", null);

		witness.add(ConversationStates.ATTENDING,
				Arrays.asList("niebo", "nieba", "niebie"),
				trialPhaseCondition(TRIAL_WATCH), ConversationStates.ATTENDING,
				"Nad traktem widziano orła giganta, a dalej pegaza brązowego. Oba są dość silne, by rozbić słabszy patrol. Jeśli po wszystkim droga ucichnie, nie schodź od razu ze służby. Pełna #warta trwa co najmniej dwadzieścia minut.", null);

		witness.add(ConversationStates.ATTENDING,
				Arrays.asList("warta", "wartę", "warte"),
				unfinishedWatchCondition(),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final boolean killsDone = new KilledForQuestCondition(TRIAL_SLOT, TRIAL_KILLS_INDEX)
								.fire(player, sentence, raiser.getEntity());
						final long remaining = remainingWatchMillis(player);
						if (killsDone && remaining > 0L) {
							raiser.say("Zagrożenia ucichły, ale warta nie kończy się ostatnim ciosem. Zostań odpowiedzialny za obejście jeszcze około "
									+ TimeUtil.approxTimeUntil((int) (remaining / 1000L)) + ". Rycerz nie schodzi ze służby tylko dlatego, że zrobiło się cicho.");
						} else {
							raiser.say("Warta trwa. Któryś z meldunków nadal nie został domknięty. Przejdź jeszcze przez #bagna, #łąki i #góry, sprawdź #olbrzymy oraz #niebo. Sam upływ czasu nie zastąpi obowiązku, ale nie będę też liczył rycerstwa samą liczbą ciosów.");
						}
					}
				});

		witness.add(ConversationStates.ATTENDING,
				Arrays.asList("warta", "wartę", "warte"),
				finishedWatchCondition(),
				ConversationStates.INFORMATION_7,
				"Teraz mogę mówić jako świadek. Nie pytam, ile bestii padło dla twojej chwały. Wiem, że każde zagrożenie z meldunków zostało usunięte, droga znów jest otwarta dla ludzi, a ty pozostałeś na służbie także wtedy, gdy po walce zrobiło się cicho. To jest #świadectwo, którego potrzebuje Edgard.",
				null);

		witness.add(ConversationStates.INFORMATION_7,
				Arrays.asList("świadectwo", "swiadectwo"),
				finishedWatchCondition(),
				ConversationStates.ATTENDING,
				"Powiem Edgardowi tyle, ile trzeba. Przyjąłeś wartę, sprawdziłeś zagrożenie od bagien po górski trakt, wytrwałeś na miejscu i zostawiłeś drogę bezpieczniejszą, niż ją zastałeś. Reszta należy do twojej przysięgi.",
				new SetQuestAction(TRIAL_SLOT, TRIAL_STATE_INDEX, TRIAL_WITNESSED));

		witness.add(ConversationStates.ATTENDING,
				Arrays.asList("warta", "wartę", "warte", "świadectwo", "swiadectwo"),
				trialPhaseCondition(TRIAL_WITNESSED),
				ConversationStates.ATTENDING,
				"Moje świadectwo już poszło do Edgarda. Teraz wróć do niego. Ja zostaję na murze, bo warta nie kończy się wraz z twoją próbą.",
				null);
	}

	private void prepareForging() {
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("materiały", "materiałów", "materialy", "materialow", "pierścień", "pierscien", "przypomnij"),
				materialsStateCondition(),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Do przekucia znaku oddasz swój pierścień mieszczanina, 30 sztabek żelaza na serię próbnych prętów i mocną obręcz, 10 sztabek złota na pieczęć Zakonu oraz 5 bryłek mithrilu na rdzeń i wzmocnienia. Kuźnia odrzuci część stopu podczas prób i hartowania, dlatego to już pełne przygotowanie rzemieślnicze, nie symboliczny dar. Masz wszystko i przekazujesz je kuźni Zakonu?",
				null);

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(materialsStateCondition(), hasForgingMaterials()),
				ConversationStates.IDLE,
				"Przyjmuję. Jeszcze tej nocy posłaniec zaniesie metal do naszej kuźni. Przekucie starego znaku, hartowanie i nałożenie pieczęci potrwa około półtorej godziny. Potem wróć, gdy będziesz gotów odebrać #pierścień.",
				startForgingAction());

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(materialsStateCondition(), new NotCondition(hasForgingMaterials())),
				ConversationStates.ATTENDING,
				"Kuźnia nie rozpocznie pracy z niepełnym tworzywem. Potrzebny jest pierścień mieszczanina, 30 sztabek żelaza, 10 sztabek złota i 5 bryłek mithrilu. Gdy zbierzesz całość, #materiały będą mogły trafić pod młot.",
				null);

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES,
				materialsStateCondition(),
				ConversationStates.ATTENDING,
				"Nie ma pośpiechu. Metal może poczekać. Kiedy będziesz gotów oddać pełne tworzywo, wrócimy do #materiałów.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				forgingCondition(false),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final long remaining = remainingForgingMillis(player);
						raiser.say("Jeszcze nie. W kuźni Zakonu znak jest hartowany, a pieczęci nie kładzie się na gorący metal. Żar potrzebuje jeszcze około "
								+ TimeUtil.approxTimeUntil((int) (remaining / 1000L)) + ".");
					}
				});

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("pierścień", "pierscien"),
				forgingCondition(false),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final long remaining = remainingForgingMillis(player);
						raiser.say("Posłaniec jeszcze nie wrócił z kuźni. Daj im około "
								+ TimeUtil.approxTimeUntil((int) (remaining / 1000L)) + ". Dobry znak nie rodzi się z pośpiechu.");
					}
				});

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				forgingCondition(true),
				ConversationStates.INFORMATION_9,
				"Posłaniec wrócił przed chwilą. Stary znak został przekuty, a pieczęć Zakonu wybita w metalu. Twój #pierścień czeka.",
				null);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("pierścień", "pierscien"),
				forgingCondition(true),
				ConversationStates.ATTENDING,
				"Przyjmij go. Żelazo przypomina ciężar tarczy. Złoto przypomina o złożonym słowie. Mithril przypomina, że obowiązek ma trwać dłużej niż chwila odwagi. Noś znak rycerza tak, by nie trzeba było pytać, komu służysz.",
				finishForgingAction());

		npc.add(ConversationStates.INFORMATION_9,
				Arrays.asList("pierścień", "pierscien"),
				forgingCondition(true),
				ConversationStates.ATTENDING,
				"Przyjmij go. Żelazo przypomina ciężar tarczy. Złoto przypomina o złożonym słowie. Mithril przypomina, że obowiązek ma trwać dłużej niż chwila odwagi. Noś znak rycerza tak, by nie trzeba było pytać, komu służysz.",
				finishForgingAction());
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Status Społeczny: Rycerz",
				"Złóż rycerskie słowo, odsłuż pełną wartę pod okiem świadka i pozwól Zakonowi przekuć znak mieszczanina w pierścień rycerza.",
				true);
		prepareOffer();
		prepareWatch();
		prepareForging();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String state = player.getQuest(QUEST_SLOT);
		res.add(player.getGenderVerb("Spotkałem") + " Edgarda, posłańca Zakonu Cieni.");

		if (STATE_REJECTED.equals(state)) {
			res.add("Nie złożyłem jeszcze rycerskiej przysięgi. Edgard pozwolił mi wrócić, gdy będę gotowy wziąć odpowiedzialność za innych.");
			return res;
		}

		res.add("Edgard przypomniał mi, że znak mieszczanina oznacza zaufanie wspólnoty, a rycerska przysięga ma zamienić to zaufanie w obowiązek jej obrony.");
		res.add("Złożyłem słowo, że rycerski obowiązek będzie ważniejszy niż łup i własna wygoda.");

		if (STATE_TRIAL.equals(state)) {
			if (!player.hasQuest(TRIAL_SLOT)
					|| TRIAL_AWAITING.equals(player.getQuest(TRIAL_SLOT, TRIAL_STATE_INDEX))) {
				res.add("Mam odnaleźć Zakonnika na północno-wschodnim murze zamku i zgłosić mu się na wartę.");
			} else if (TRIAL_WATCH.equals(player.getQuest(TRIAL_SLOT, TRIAL_STATE_INDEX))) {
				res.add("Zakonnik powierzył mi wartę na szlaku wokół zamku. Meldunki prowadzą od bagien przez łąki i góry aż do silniejszych zagrożeń: mam usunąć dwa pokutniki z bagien, jednego pokutnika z łąk, jednego pokutnika z gór, dwóch superczłowieków olbrzymów, jednego orła giganta i jednego pegaza brązowego oraz wytrwać w służbie co najmniej dwadzieścia minut.");
			} else if (TRIAL_WITNESSED.equals(player.getQuest(TRIAL_SLOT, TRIAL_STATE_INDEX))) {
				res.add("Zakonnik potwierdził, że sprawdziłem wszystkie meldunki, zabezpieczyłem drogę i wytrwałem na pełnej warcie. Mam wrócić do Edgarda.");
			}
			return res;
		}

		res.add("Zakonnik dał świadectwo, że przyjąłem wartę, sprawdziłem zagrożenia od bagien po górski trakt i nie opuściłem powierzonej drogi.");

		if (STATE_MATERIALS.equals(state) || STATE_LEGACY_ITEMS.equals(state)) {
			res.add("Mam przekazać kuźni Zakonu pierścień mieszczanina, 30 sztabek żelaza, 10 sztabek złota i 5 bryłek mithrilu, aby stary znak został przekuty w nowy.");
			return res;
		}

		if (state != null && state.startsWith(FORGING_PREFIX)) {
			res.add("Oddałem stary pierścień i tworzywo. Kuźnia Zakonu pracuje nad pierścieniem rycerza; mam wrócić po zakończeniu hartowania i pieczętowania.");
			return res;
		}

		if (STATE_DONE.equals(state)) {
			res.add("Odebrałem pierścień rycerza, przekuty z poprzedniego znaku i opatrzony pieczęcią Zakonu.");
			res.add("Zadanie zakończone. Znak mieszczanina oznaczał, że ludzie mogą na mnie polegać; znak rycerza przypomina, że mam stanąć w ich obronie, gdy przyjdzie zagrożenie.");
			return res;
		}

		final List<String> debug = new ArrayList<>();
		debug.add("Stan zadania to: " + state);
		logger.error("Historia Pierścienia Rycerza nie pasuje do stanu zadania: " + state);
		return debug;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Pierścień Rycerza";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
