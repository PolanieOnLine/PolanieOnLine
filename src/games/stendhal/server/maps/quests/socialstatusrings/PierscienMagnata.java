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
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;
import games.stendhal.server.util.TimeUtil;

/** Final social-status trial: wealth understood as stewardship, not tribute. */
public class PierscienMagnata extends AbstractQuest {
	static final String QUEST_SLOT = "pierscien_magnata";
	static final String STATE_REJECTED = "rejected";
	static final String STATE_COUNCIL = "council";
	static final String STATE_STEWARDSHIP = "stewardship";
	static final String STATE_MATERIALS = "materials";
	static final String STATE_LEGACY_START = "start";
	static final String STATE_DONE = "done";
	static final String FORGING_PREFIX = "forging;";

	static final String COUNCIL_SLOT = "pierscien_magnata_council";
	static final String COUNCIL_ASKED = "asked";
	static final String COUNCIL_DONE = "done";
	static final int COUNCIL_COMMUNITY = 0;
	static final int COUNCIL_DUTY = 1;
	static final int COUNCIL_LAW = 2;

	static final String STEWARD_SLOT = "pierscien_magnata_stewardship";
	static final String STEWARD_ASKED = "asked";
	static final String STEWARD_DONE = "done";
	static final int STEWARD_FUND = 0;
	static final int STEWARD_PLEDGE = 1;
	static final int STEWARD_ACCOUNT = 2;
	static final int STEWARD_STARTED = 3;
	static final int ENDOWMENT_MONEY = 150000;
	static final int STEWARDSHIP_MINUTES = 90;

	static final String CLUB_THORNS_QUEST_SLOT = "club_thorns";
	static final String KILL_DRAGONS_QUEST_SLOT = "kill_dragons";
	static final String VAMPIRE_SWORD_QUEST_SLOT = "vs_quest";
	static final String IMMORTAL_SWORD_QUEST_SLOT = "immortalsword_quest";
	static final String FIND_RAT_KIDS_QUEST_SLOT = "find_rat_kids";
	static final String FIND_GHOSTS_QUEST_SLOT = "find_ghosts";
	static final String SAD_SCIENTIST_QUEST_SLOT = "sad_scientist";

	private static final int REQUIRED_LEVEL = 500;
	private static final int REQUIRED_SILVER = 80;
	private static final int REQUIRED_GOLD = 50;
	private static final int REQUIRED_MITHRIL = 25;
	private static final int FORGING_MINUTES = 180;
	private static final int REWARD_XP = 500000;
	private static final Logger logger = Logger.getLogger(PierscienMagnata.class);

	private final SpeakerNPC npc = npcs.get("Jubiler Zdzichu");
	private final SpeakerNPC communityWitness = npcs.get("Dobrawa");
	private final SpeakerNPC dutyWitness = npcs.get("Edgard");
	private final SpeakerNPC lawWitness = npcs.get("eDragon");

	private boolean hasLegacyPrerequisites(final Player player) {
		return player.isQuestCompleted(CLUB_THORNS_QUEST_SLOT)
				&& player.isQuestCompleted(KILL_DRAGONS_QUEST_SLOT)
				&& player.isQuestCompleted(VAMPIRE_SWORD_QUEST_SLOT)
				&& player.isQuestCompleted(IMMORTAL_SWORD_QUEST_SLOT)
				&& player.isQuestCompleted(FIND_RAT_KIDS_QUEST_SLOT)
				&& player.isQuestCompleted(FIND_GHOSTS_QUEST_SLOT)
				&& player.isQuestCompleted(SAD_SCIENTIST_QUEST_SLOT);
	}

	private ChatCondition newTrialCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				final boolean notStarted = !player.hasQuest(QUEST_SLOT)
						|| STATE_REJECTED.equals(player.getQuest(QUEST_SLOT));
				return notStarted
						&& !player.isBadBoy()
						&& player.getLevel() >= REQUIRED_LEVEL
						&& player.isQuestCompleted(PierscienBarona.QUEST_SLOT)
						&& player.isEquipped("pierścień barona")
						&& hasLegacyPrerequisites(player);
			}
		};
	}

	private ChatCondition blockedNewTrialCondition() {
		return new AndCondition(
				new OrCondition(new QuestNotStartedCondition(QUEST_SLOT),
						new QuestInStateCondition(QUEST_SLOT, STATE_REJECTED)),
				new NotCondition(newTrialCondition()));
	}

	private OrCondition materialsStateCondition() {
		return new OrCondition(new QuestInStateCondition(QUEST_SLOT, STATE_MATERIALS),
				new QuestInStateCondition(QUEST_SLOT, STATE_LEGACY_START));
	}

	private ChatCondition councilEntryCondition(final int index, final boolean completed) {
		final ChatCondition done = new QuestInStateCondition(COUNCIL_SLOT, index, COUNCIL_DONE);
		if (completed) {
			return new AndCondition(new QuestInStateCondition(QUEST_SLOT, STATE_COUNCIL), done);
		}
		return new AndCondition(new QuestInStateCondition(QUEST_SLOT, STATE_COUNCIL), new NotCondition(done));
	}

	private AndCondition allCouncilApprovals() {
		return new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, STATE_COUNCIL),
				new QuestInStateCondition(COUNCIL_SLOT, COUNCIL_COMMUNITY, COUNCIL_DONE),
				new QuestInStateCondition(COUNCIL_SLOT, COUNCIL_DUTY, COUNCIL_DONE),
				new QuestInStateCondition(COUNCIL_SLOT, COUNCIL_LAW, COUNCIL_DONE));
	}

	private ChatCondition councilStillPending() {
		return new AndCondition(new QuestInStateCondition(QUEST_SLOT, STATE_COUNCIL),
				new NotCondition(allCouncilApprovals()));
	}

	private boolean isCouncilApproved(final Player player, final int index) {
		return player.hasQuest(COUNCIL_SLOT) && COUNCIL_DONE.equals(player.getQuest(COUNCIL_SLOT, index));
	}

	private ChatCondition stewardStateCondition(final int index, final String state) {
		return new AndCondition(new QuestInStateCondition(QUEST_SLOT, STATE_STEWARDSHIP),
				new QuestInStateCondition(STEWARD_SLOT, index, state));
	}

	private ChatCondition stewardNotDoneCondition(final int index) {
		return new AndCondition(new QuestInStateCondition(QUEST_SLOT, STATE_STEWARDSHIP),
				new NotCondition(new QuestInStateCondition(STEWARD_SLOT, index, STEWARD_DONE)));
	}

	private boolean isStewardDone(final Player player, final int index) {
		return player.hasQuest(STEWARD_SLOT) && STEWARD_DONE.equals(player.getQuest(STEWARD_SLOT, index));
	}

	private boolean isStewardshipTimeReady(final Player player) {
		if (!isStewardDone(player, STEWARD_FUND)) {
			return false;
		}
		try {
			final String startedValue = player.getQuest(STEWARD_SLOT, STEWARD_STARTED);
			if (startedValue == null || startedValue.length() == 0) {
				return true;
			}
			final long started = Long.parseLong(startedValue);
			return System.currentTimeMillis() >= started + STEWARDSHIP_MINUTES * TimeUtil.MILLISECONDS_IN_MINUTE;
		} catch (final RuntimeException e) {
			return true;
		}
	}

	private long remainingStewardshipMillis(final Player player) {
		try {
			final long started = Long.parseLong(player.getQuest(STEWARD_SLOT, STEWARD_STARTED));
			return Math.max(0L,
					started + STEWARDSHIP_MINUTES * TimeUtil.MILLISECONDS_IN_MINUTE - System.currentTimeMillis());
		} catch (final RuntimeException e) {
			return 0L;
		}
	}

	private ChatCondition allStewardshipRequirements() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				return player.isQuestInState(QUEST_SLOT, STATE_STEWARDSHIP)
						&& isStewardDone(player, STEWARD_FUND)
						&& isStewardDone(player, STEWARD_PLEDGE)
						&& isStewardDone(player, STEWARD_ACCOUNT)
						&& isStewardshipTimeReady(player);
			}
		};
	}

	private ChatCondition pendingStewardshipCondition() {
		return new AndCondition(new QuestInStateCondition(QUEST_SLOT, STATE_STEWARDSHIP),
				new NotCondition(allStewardshipRequirements()));
	}

	private AndCondition hasForgingMaterials() {
		return new AndCondition(
				new PlayerHasItemWithHimCondition("pierścień barona", 1),
				new PlayerHasItemWithHimCondition("sztabka srebra", REQUIRED_SILVER),
				new PlayerHasItemWithHimCondition("sztabka złota", REQUIRED_GOLD),
				new PlayerHasItemWithHimCondition("sztabka mithrilu", REQUIRED_MITHRIL));
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
			return Math.max(0L, started + FORGING_MINUTES * TimeUtil.MILLISECONDS_IN_MINUTE - System.currentTimeMillis());
		} catch (final RuntimeException e) {
			return 0L;
		}
	}

	private ChatAction startForgingAction() {
		return new MultipleActions(
				new DropItemAction("pierścień barona", 1),
				new DropItemAction("sztabka srebra", REQUIRED_SILVER),
				new DropItemAction("sztabka złota", REQUIRED_GOLD),
				new DropItemAction("sztabka mithrilu", REQUIRED_MITHRIL),
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						player.setQuest(QUEST_SLOT, FORGING_PREFIX + System.currentTimeMillis());
					}
				});
	}

	private ChatAction finishForgingAction() {
		return new MultipleActions(new EquipItemAction("pierścień magnata", 1, true),
				new IncreaseXPAction(REWARD_XP), new SetQuestAction(QUEST_SLOT, STATE_DONE));
	}

	private void prepareOffer() {
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_DONE), ConversationStates.ATTENDING,
				"Znak magnata już nosisz. Pamiętaj drogę, która do niego prowadziła. Zaufanie ludzi, rycerska służba i odpowiedzialność barona nie znikają pod szerszą oprawą.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, blockedNewTrialCondition(),
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.isBadBoy()) {
							raiser.say("Bogactwo w dłoniach człowieka, który nie panuje nad własnym gniewem, staje się tylko większym narzędziem krzywdy. Oczyść imię i wróć.");
						} else if (player.getLevel() < REQUIRED_LEVEL) {
							raiser.say("Magnat rozporządza losem większym niż własna sakwa. Wróć, gdy osiągniesz poziom 500 i będziesz miał dość drogi za sobą.");
						} else if (!player.isQuestCompleted(PierscienBarona.QUEST_SLOT)) {
							raiser.say("Najpierw naucz się ciężaru baroniego znaku. Kto nie umie odpowiadać za kilku, nie powinien odpowiadać za wielu.");
						} else if (!player.isEquipped("pierścień barona")) {
							raiser.say("Przynieś pierścień barona. Nie będę tworzył nowego znaku z próżni. Przekuję to, czym już zasłużyłeś na poprzedni tytuł.");
						} else if (!hasLegacyPrerequisites(player)) {
							raiser.say("Masz jeszcze niedokończone dawne powinności. Wróć po cierniowej maczudze i nieśmiertelnym mieczu, po próbach smoków i wampira, po odnalezieniu dzieci i duchów oraz po pomocy zasmuconemu uczonemu. Magnat nie może wybierać tylko wygodnych obowiązków.");
						}
					}
				});

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, newTrialCondition(),
				ConversationStates.INFORMATION_7,
				"Masz za sobą trzy różne lekcje. Jako mieszczanin zdobywałeś zaufanie czynem. Jako rycerz brałeś służbę mimo ryzyka. Jako baron odpowiadałeś za prawo i jego skutki. Magnat ma utrzymać je wszystkie naraz, gdy zaczynasz rozporządzać #dostatkiem większym niż własna sakwa.", null);
		npc.add(ConversationStates.INFORMATION_7, Arrays.asList("dostatek", "dostatkiem", "bogactwo"),
				newTrialCondition(), ConversationStates.INFORMATION_8,
				"Dostatek nie polega na tym, że jedna skrzynia pęka od monet. Prawdziwy zapas pozwala przeżyć zimę, naprawić most po roztopach i nakarmić ludzi, zanim zaczną sprzedawać ostatnie narzędzia. Zaufanie bez środków bywa bezsilne, środki bez słowa stają się kaprysem, a oba bez prawa łatwo zamienić we władzę dla własnej korzyści. Dlatego wszystko spina #piecza.", null);
		npc.add(ConversationStates.INFORMATION_8, Arrays.asList("piecza", "pieczę", "piecze"), newTrialCondition(),
				ConversationStates.QUEST_OFFERED,
				"Jeśli przyjmiesz znak magnata, zobowiązujesz się utrzymać razem to, czego nauczyły cię poprzednie znaki. Chodzi o zaufanie wspólnoty, dane słowo i jedną miarę prawa. Podejmujesz się tej pieczy?", null);
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.YES_MESSAGES, newTrialCondition(),
				ConversationStates.ATTENDING,
				"Dobrze. Ja jestem jubilerem, więc nie będę udawał jedynego sędziego całej twojej drogi. Dobrawa pamięta, czy zasłużyłeś na zaufanie #wspólnoty jako mieszczanin. Edgard pamięta dane #słowo i rycerską wartę. eDragon sprawdzi, czy #prawo z próby barona nadal ma dla ciebie jedną miarę. Ich trzy głosy otworzą próbę, lecz jej za ciebie nie wykonają.",
				new MultipleActions(new SetQuestAction(QUEST_SLOT, STATE_COUNCIL), new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						player.addKarma(10);
					}
				}));
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, newTrialCondition(),
				ConversationStates.IDLE,
				"Dobrze, że nie składasz słowa bez przekonania. Wróć, gdy dostatek przestanie ci się kojarzyć wyłącznie z pełną sakwą.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, STATE_REJECTED, -10.0));
	}

	private void prepareCouncil() {
		communityWitness.add(ConversationStates.ATTENDING, Arrays.asList("wspólnota", "wspólnotę", "wspolnota", "wspolnote"),
				councilEntryCondition(COUNCIL_COMMUNITY, false), ConversationStates.ATTENDING,
				"Pamiętam cię jeszcze z próby mieszczanina. Wtedy nie chodziło o tytuł, tylko o rannych, odzyskane narzędzia i drogę, którą ktoś musiał naprawić. Ludzie zaczęli na tobie polegać, zanim dostałeś kolejny pierścień. Przy większym dostatku ta zasada nie znika. Bez #zaufania wspólnoty pełny spichlerz staje się tylko zapasem pilnowanym przed ludźmi, którym miał służyć.",
				new SetQuestAction(COUNCIL_SLOT, COUNCIL_COMMUNITY, COUNCIL_ASKED));
		communityWitness.add(ConversationStates.ATTENDING, Arrays.asList("zaufanie", "zaufania"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, STATE_COUNCIL),
						new QuestInStateCondition(COUNCIL_SLOT, COUNCIL_COMMUNITY, COUNCIL_ASKED)),
				ConversationStates.ATTENDING,
				"Moje świadectwo dostaniesz. Znak mieszczanina miał sens dlatego, że ludzie mogli na tobie polegać bez obietnicy nagrody. Jeśli o tym pamiętasz także przy wielkim majątku, zaufanie nie stanie się towarem.",
				new SetQuestAction(COUNCIL_SLOT, COUNCIL_COMMUNITY, COUNCIL_DONE));
		communityWitness.add(ConversationStates.ATTENDING,
				Arrays.asList("wspólnota", "wspólnotę", "zaufanie", "zaufania"),
				councilEntryCondition(COUNCIL_COMMUNITY, true), ConversationStates.ATTENDING,
				"Moje świadectwo już masz. Pamiętaj tylko, że magnat nie przestaje być człowiekiem, na którym wspólnota ma móc polegać.", null);

		dutyWitness.add(ConversationStates.ATTENDING, Arrays.asList("słowo", "slowo", "przysięga", "przysiega"),
				councilEntryCondition(COUNCIL_DUTY, false), ConversationStates.ATTENDING,
				"Pamiętam twoją rycerską wartę. Nie wystarczyło pokonać zagrożeń. Miałeś zostać na powierzonym miejscu także wtedy, gdy po walce zapadła cisza. Magnat ma tę samą próbę w większej skali. Dotrzymanie umowy może kosztować więcej niż bitwa, a jej złamanie może przynieść zysk. Dlatego nadal liczy się #obowiązek.",
				new SetQuestAction(COUNCIL_SLOT, COUNCIL_DUTY, COUNCIL_ASKED));
		dutyWitness.add(ConversationStates.ATTENDING, Arrays.asList("obowiązek", "obowiazek"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, STATE_COUNCIL),
						new QuestInStateCondition(COUNCIL_SLOT, COUNCIL_DUTY, COUNCIL_ASKED)),
				ConversationStates.ATTENDING,
				"Moje świadectwo dla Zdzicha brzmi prosto. Rycerskie słowo nie skończyło się wraz z wartą. Umiesz postawić obowiązek przed wygodą i zostać przy nim, gdy nikt nie patrzy.",
				new SetQuestAction(COUNCIL_SLOT, COUNCIL_DUTY, COUNCIL_DONE));
		dutyWitness.add(ConversationStates.ATTENDING, Arrays.asList("słowo", "slowo", "obowiązek", "obowiazek"),
				councilEntryCondition(COUNCIL_DUTY, true), ConversationStates.ATTENDING,
				"Moje słowo już poszło do Zdzicha. Jedna przysięga wystarczy, jeśli naprawdę wiąże cię także później.", null);

		lawWitness.add(ConversationStates.ATTENDING, Arrays.asList("prawo", "prawa", "prawie"),
				councilEntryCondition(COUNCIL_LAW, false), ConversationStates.ATTENDING,
				"Przy baroniej próbie musiałeś oprzeć granicę na świadectwie, spichlerz na potrzebie wspólnoty, a wyrok na dowodzie zamiast gniewu. Teraz twoja sakwa jest cięższa i łatwiej nią nagiąć cudzy wybór bez jednego rozkazu. Dlatego pytam o prawo jednej #miary dla biednego i bogatego.",
				new SetQuestAction(COUNCIL_SLOT, COUNCIL_LAW, COUNCIL_ASKED));
		lawWitness.add(ConversationStates.ATTENDING, Arrays.asList("miara", "miary", "miarę", "miare"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, STATE_COUNCIL),
						new QuestInStateCondition(COUNCIL_SLOT, COUNCIL_LAW, COUNCIL_ASKED)),
				ConversationStates.ATTENDING,
				"Taką odpowiedź uznaję. Baron miał stosować prawo również wtedy, gdy wyrok był niewygodny. Magnat musi przyjąć tę samą miarę także dla własnego majątku i własnych wpływów.",
				new SetQuestAction(COUNCIL_SLOT, COUNCIL_LAW, COUNCIL_DONE));
		lawWitness.add(ConversationStates.ATTENDING, Arrays.asList("prawo", "miara", "miary", "miarę", "miare"),
				councilEntryCondition(COUNCIL_LAW, true), ConversationStates.ATTENDING,
				"Moje świadectwo już zostało dane. Znak barona nauczył cię prawa. Znak magnata ma sprawdzić, czy nie kupisz sobie od niego wyjątku.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, councilStillPending(),
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final List<String> missing = new ArrayList<>();
						if (!isCouncilApproved(player, COUNCIL_COMMUNITY)) {
							missing.add("Dobrawa i jej #wspólnota");
						}
						if (!isCouncilApproved(player, COUNCIL_DUTY)) {
							missing.add("Edgard i dane #słowo");
						}
						if (!isCouncilApproved(player, COUNCIL_LAW)) {
							missing.add("eDragon i #prawo");
						}
						raiser.say("Brakuje jeszcze " + String.join(", ", missing)
								+ ". Nie chodzi o samą formalność. Każdy głos przypomina jedną część drogi, której magnat nie może porzucić.");
					}
				});

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, allCouncilApprovals(),
				ConversationStates.ATTENDING,
				"Trzy głosy dotarły. Dobrawa przyniosła zaufanie mieszczanina, Edgard służbę i słowo rycerza, eDragon odpowiedzialność barona za prawo. Magnat nie dostaje czwartej lekcji obok nich. Ma utrzymać wszystkie trzy naraz, gdy majątek może zmieniać cudze życie. Jeśli jesteś gotów, podejmij #pieczę nad żelaznym funduszem wspólnoty.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("piecza", "pieczę", "piecze"), allCouncilApprovals(),
				ConversationStates.ATTENDING,
				"Idź do Dobrawy i wspomnij o #funduszu. Nie oddasz pieniędzy jubilerowi ani nie kupisz tytułu. Wydzielisz część dostatku poza własną sakwę, a potem pokażesz, że zaufanie, słowo i prawo potrafią pilnować jednego zobowiązania jednocześnie.",
				new SetQuestAction(QUEST_SLOT, STATE_STEWARDSHIP));
	}

	private void prepareStewardship() {
		communityWitness.add(ConversationStates.ATTENDING, Arrays.asList("fundusz", "funduszu"),
				stewardNotDoneCondition(STEWARD_FUND), ConversationStates.ATTENDING,
				"Jako mieszczanin budowałeś zaufanie pracą, której nie dało się zastąpić monetą. Teraz masz sprawdzić odwrotną rzecz. Czy potrafisz odłożyć wielką sumę tak, by nie stała się narzędziem kupowania wdzięczności? Żelazny fundusz ma należeć do wspólnoty. Wydziel 150000 monet na wypadek głodu, naprawę drogi, leczenie albo inne wspólne nieszczęście. Gdy będziesz mieć całość, powierz mi #zapas.",
				new SetQuestAction(STEWARD_SLOT, STEWARD_FUND, STEWARD_ASKED));

		communityWitness.add(ConversationStates.ATTENDING, Arrays.asList("zapas"),
				new AndCondition(stewardStateCondition(STEWARD_FUND, STEWARD_ASKED),
						new PlayerHasItemWithHimCondition("money", ENDOWMENT_MONEY)),
				ConversationStates.ATTENDING,
				"Przyjmuję fundusz w imieniu wspólnoty. Te monety nie kupują ci tytułu, głosu ani wdzięczności i nie wrócą do twojej sakwy. Od tej chwili mają służyć ludziom, a ty przez dziewięćdziesiąt minut odpowiadasz za pieczę nad zobowiązaniem. Edgard sprawdzi rycerskie #poręczenie. eDragon sprawdzi #rachunek funduszu.",
				new MultipleActions(new DropItemAction("money", ENDOWMENT_MONEY),
						new SetQuestAction(STEWARD_SLOT, STEWARD_FUND, STEWARD_DONE), new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								player.setQuest(STEWARD_SLOT, STEWARD_STARTED, Long.toString(System.currentTimeMillis()));
							}
						}));

		communityWitness.add(ConversationStates.ATTENDING, Arrays.asList("zapas"),
				new AndCondition(stewardStateCondition(STEWARD_FUND, STEWARD_ASKED),
						new NotCondition(new PlayerHasItemWithHimCondition("money", ENDOWMENT_MONEY))),
				ConversationStates.ATTENDING,
				"Fundusz musi powstać w całości. Potrzeba 150000 monet razem. Nie zabieram części. Wróć, gdy naprawdę możesz odłożyć tę sumę poza własny majątek.", null);

		communityWitness.add(ConversationStates.ATTENDING, Arrays.asList("fundusz", "funduszu", "zapas"),
				stewardStateCondition(STEWARD_FUND, STEWARD_DONE), ConversationStates.ATTENDING,
				"Fundusz już jest oddzielony od twojej sakwy. Zaufanie wspólnoty nie polega teraz na tym, że jesteś hojny, tylko że tych środków nie cofniesz, gdy staną się potrzebne. Edgard i eDragon mają jeszcze związać je słowem i prawem.", null);

		dutyWitness.add(ConversationStates.ATTENDING, Arrays.asList("poręczenie", "poreczenie"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, STATE_STEWARDSHIP),
						new QuestInStateCondition(STEWARD_SLOT, STEWARD_FUND, STEWARD_DONE),
						new NotCondition(new QuestInStateCondition(STEWARD_SLOT, STEWARD_PLEDGE, STEWARD_DONE))),
				ConversationStates.ATTENDING,
				"Na rycerskiej warcie twoje słowo znaczyło, że nie schodzisz ze służby po ostatnim ciosie. Tu nie ma miecza do wyciągnięcia. Fundusz może szybko stopnieć podczas usuwania skutków pożaru albo nieurodzaju, a wtedy najłatwiej powiedzieć, że to już cudzy kłopot. Jeśli poręczasz jego cel własnym imieniem, powiedz jeszcze raz #słowo.",
				new SetQuestAction(STEWARD_SLOT, STEWARD_PLEDGE, STEWARD_ASKED));

		dutyWitness.add(ConversationStates.ATTENDING, Arrays.asList("słowo", "slowo"),
				stewardStateCondition(STEWARD_PLEDGE, STEWARD_ASKED), ConversationStates.ATTENDING,
				"Przyjmuję poręczenie. Rycerska służba była próbą twojego czasu i bezpieczeństwa. To słowo obejmuje teraz majątek, z którego skorzystają inni. Oba znaczą tyle samo. Nie odchodzisz od obowiązku, gdy przestaje być wygodny.",
				new SetQuestAction(STEWARD_SLOT, STEWARD_PLEDGE, STEWARD_DONE));

		dutyWitness.add(ConversationStates.ATTENDING, Arrays.asList("poręczenie", "poreczenie", "słowo", "slowo"),
				stewardStateCondition(STEWARD_PLEDGE, STEWARD_DONE), ConversationStates.ATTENDING,
				"Poręczenie już zapisałem. Nie składaj go drugi raz. Ważniejsze, by pierwsze pozostało tak samo wiążące jak rycerskie słowo.", null);

		lawWitness.add(ConversationStates.ATTENDING, Arrays.asList("rachunek", "rachunku"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, STATE_STEWARDSHIP),
						new QuestInStateCondition(STEWARD_SLOT, STEWARD_FUND, STEWARD_DONE),
						new NotCondition(new QuestInStateCondition(STEWARD_SLOT, STEWARD_ACCOUNT, STEWARD_DONE))),
				ConversationStates.ATTENDING,
				"Jako baron żądałeś świadectwa przy granicy i dowodu przed wyrokiem, choć łatwiej było poprzeć silniejszego albo głośniejszego. Teraz twój własny majątek ma stanąć przed tą samą kontrolą. Bez rachunku hojność może stać się sposobem kupowania wpływu. Czy przyjmujesz tę samą #miarę dla własnych monet, jakiej żądałeś od innych?",
				new SetQuestAction(STEWARD_SLOT, STEWARD_ACCOUNT, STEWARD_ASKED));

		lawWitness.add(ConversationStates.ATTENDING, Arrays.asList("miara", "miary", "miarę", "miare"),
				stewardStateCondition(STEWARD_ACCOUNT, STEWARD_ASKED), ConversationStates.ATTENDING,
				"Przyjmuję rachunek. Odpowiedzialność barona nie kończy się na cudzych sporach. Fundusz ma cel, poręczyciela i prawo, przed którym można rozliczyć również ciebie. To odróżnia pieczę nad majątkiem od hojnego kaprysu.",
				new SetQuestAction(STEWARD_SLOT, STEWARD_ACCOUNT, STEWARD_DONE));

		lawWitness.add(ConversationStates.ATTENDING, Arrays.asList("rachunek", "rachunku", "miara", "miary", "miarę", "miare"),
				stewardStateCondition(STEWARD_ACCOUNT, STEWARD_DONE), ConversationStates.ATTENDING,
				"Rachunek został przyjęty. Prawo pamięta o funduszu także wtedy, gdy jego twórca przestanie o nim mówić. Taka sama miara dla własnego skarbca jest trudniejsza niż dla cudzej sprawy.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, pendingStewardshipCondition(),
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final List<String> missing = new ArrayList<>();
						if (!isStewardDone(player, STEWARD_FUND)) {
							missing.add("żelazny #fundusz u Dobrawy");
						} else {
							if (!isStewardDone(player, STEWARD_PLEDGE)) {
								missing.add("#poręczenie u Edgarda");
							}
							if (!isStewardDone(player, STEWARD_ACCOUNT)) {
								missing.add("#rachunek u eDragona");
							}
							if (!isStewardshipTimeReady(player)) {
								missing.add("jeszcze około "
										+ TimeUtil.approxTimeUntil((int) (remainingStewardshipMillis(player) / 1000L))
										+ " trwałej pieczy");
							}
						}
						raiser.say("Trzy lekcje już znasz, ale magnacka piecza wymaga, by zaufanie, słowo i prawo działały razem przez cały czas. Brakuje jeszcze "
								+ String.join(", ", missing) + ".");
					}
				});

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, allStewardshipRequirements(),
				ConversationStates.ATTENDING,
				"Fundusz opuścił twoją sakwę i przetrwał próbę. Dobrawa ma zaufanie wspólnoty, Edgard twoje słowo, eDragon rachunek pod prawem. To pierwszy raz, gdy wszystkie trzy wcześniejsze próby pilnowały jednego zobowiązania naraz. Teraz mogę wykonać znak, ale dopiero gdy nazwiesz #oprawę.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("oprawa", "oprawy", "oprawę", "oprawe"), allStewardshipRequirements(),
				ConversationStates.ATTENDING,
				"Teraz moja część. Pierścień barona będzie rdzeniem, ale nie dlatego, że magnat wyrasta ponad dawne znaki. Mieszczanin dał mu zaufanie, rycerz słowo, a baron prawo. Szeroka oprawa ma tylko objąć te trzy rzeczy pieczą nad większym dostatkiem. Fundusz wspólnoty nie jest zapłatą za moją pracę. Przejdźmy do #materiałów.",
				new SetQuestAction(QUEST_SLOT, STATE_MATERIALS));
	}

	private void prepareForging() {
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("materiały", "materiałów", "materialy", "materialow", "dary", "przypomnij", "pierścień", "pierscien"),
				materialsStateCondition(), ConversationStates.QUEST_ITEM_QUESTION,
				"Przynieś pierścień barona, 80 sztabek srebra, 50 sztabek złota i 25 sztabek mithrilu. Srebro pójdzie w szeroką oprawę i próby formy, złoto w znak rodu i wiązania, a mithril w rdzeń oraz wzmocnienia starego baroniego znaku. Masz pełny zapas i oddajesz go pod moje narzędzia?", null);
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(materialsStateCondition(), hasForgingMaterials()), ConversationStates.IDLE,
				"Dobrze. Rozkuję starą oprawę, zachowam rdzeń i osadzę go na nowo. Daj mi około trzech godzin. Potem wróć po #pierścień.", startForgingAction());
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(materialsStateCondition(), new NotCondition(hasForgingMaterials())), ConversationStates.ATTENDING,
				"Nie rozbiorę baroniego znaku, dopóki nie mam wszystkiego pod ręką. Brakuje któregoś z elementów. Potrzebny jest pierścień barona, 80 sztabek srebra, 50 sztabek złota oraz 25 sztabek mithrilu. Gdy zbierzesz całość, wrócimy do #materiałów.", null);
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.NO_MESSAGES, materialsStateCondition(),
				ConversationStates.ATTENDING,
				"Nie ma pośpiechu. Dobry jubiler częściej czeka na właściwy metal niż poprawia robotę zrobioną z byle czego. Gdy będziesz gotów, wrócimy do #materiałów.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, forgingCondition(false),
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final long remaining = remainingForgingMillis(player);
						raiser.say("Jeszcze nie. Oprawa musi związać się z dawnym rdzeniem bez naprężeń. Daj mi około "
								+ TimeUtil.approxTimeUntil((int) (remaining / 1000L)) + ". Dostatku też nie buduje się jednym dniem.");
					}
				});
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pierścień", "pierscien"), forgingCondition(false),
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final long remaining = remainingForgingMillis(player);
						raiser.say("Jeszcze pracuję nad oprawą. Wróć za około "
								+ TimeUtil.approxTimeUntil((int) (remaining / 1000L))
								+ ". Nie każ metalowi udawać, że dojrzał szybciej.");
					}
				});
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, forgingCondition(true),
				ConversationStates.INFORMATION_9,
				"Praca skończona. Rdzeń baroniego znaku jest mocno osadzony w nowej oprawie. Twój #pierścień magnata jest gotowy.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pierścień", "pierscien"), forgingCondition(true),
				ConversationStates.ATTENDING,
				"Weź go. Mieszczanin nauczył cię być człowiekiem, na którym można polegać. Rycerz nauczył cię zostać przy obowiązku, gdy robi się niebezpiecznie albo cicho. Baron nauczył cię odpowiadać za prawo i jego skutki. Magnat nie unieważnia żadnej z tych prób. Ma je wszystkie utrzymać, gdy pod jego pieczą jest majątek i los wielu ludzi.", finishForgingAction());
		npc.add(ConversationStates.INFORMATION_9, Arrays.asList("pierścień", "pierscien"), forgingCondition(true),
				ConversationStates.ATTENDING,
				"Weź go. Mieszczanin nauczył cię być człowiekiem, na którym można polegać. Rycerz nauczył cię zostać przy obowiązku, gdy robi się niebezpiecznie albo cicho. Baron nauczył cię odpowiadać za prawo i jego skutki. Magnat nie unieważnia żadnej z tych prób. Ma je wszystkie utrzymać, gdy pod jego pieczą jest majątek i los wielu ludzi.", finishForgingAction());
	}

	@Override
	public void addToWorld() {
		fillQuestInfo("Status Społeczny: Magnat",
				"Połącz zaufanie zdobyte jako mieszczanin, rycerskie słowo i odpowiedzialność barona: zdobądź trzy świadectwa, wydziel fundusz wspólnoty, poręcz go słowem i prawem, a dopiero potem pozwól Zdzichowi przekuć znak barona.", true);
		prepareOffer();
		prepareCouncil();
		prepareStewardship();
		prepareForging();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String state = player.getQuest(QUEST_SLOT);
		res.add(player.getGenderVerb("Spotkałem") + " Zdzicha, jubilera, który potrafi wykonać znak magnata, ale uważa, że o gotowości do tego tytułu powinni zaświadczyć także inni.");
		if (STATE_REJECTED.equals(state)) {
			res.add("Nie przyjąłem jeszcze pieczy związanej ze znakiem magnata.");
			return res;
		}
		res.add("Zdzichu przypomniał mi całą drogę: zaufanie mieszczanina, służbę rycerza i odpowiedzialność barona za prawo. Magnacka piecza ma utrzymać te trzy rzeczy naraz przy większym dostatku.");
		if (STATE_COUNCIL.equals(state)) {
			res.add("Zdzichu odmówił bycia jedynym sędzią. Dobrawa ma potwierdzić zaufanie wyniesione z próby mieszczanina, Edgard rycerskie słowo i obowiązek, a eDragon zasadę jednej miary prawa wyniesioną z próby barona.");
			if (isCouncilApproved(player, COUNCIL_COMMUNITY)) {
				res.add("Dobrawa potwierdziła, że wraz z większym majątkiem nie zgubiłem zaufania wspólnoty.");
			}
			if (isCouncilApproved(player, COUNCIL_DUTY)) {
				res.add("Edgard potwierdził, że rycerskie słowo nadal wiąże mnie po zakończonej warcie.");
			}
			if (isCouncilApproved(player, COUNCIL_LAW)) {
				res.add("eDragon uznał, że baronowska miara prawa ma obejmować także mój własny majątek i wpływy.");
			}
			return res;
		}
		if (STATE_STEWARDSHIP.equals(state)) {
			res.add("Rada dopuściła mnie do właściwej próby magnata: jeden fundusz ma być jednocześnie chroniony zaufaniem wspólnoty, rycerskim słowem i odpowiedzialnością barona przed prawem.");
			if (isStewardDone(player, STEWARD_FUND)) {
				res.add("Dobrawa przyjęła 150000 monet do żelaznego funduszu wspólnoty; pieniądze nie są opłatą za tytuł ani sposobem kupowania wdzięczności.");
			}
			if (isStewardDone(player, STEWARD_PLEDGE)) {
				res.add("Złożyłem u Edgarda rycerskie poręczenie, że fundusz będzie służył wspólnocie.");
			}
			if (isStewardDone(player, STEWARD_ACCOUNT)) {
				res.add("eDragon przyjął jawny rachunek funduszu według tej samej miary prawa, której wymagałem jako baron.");
			}
			if (isStewardDone(player, STEWARD_FUND) && !isStewardshipTimeReady(player)) {
				res.add("Fundusz pozostaje pod próbą pieczy; zobowiązanie musi trwać co najmniej dziewięćdziesiąt minut.");
			}
			return res;
		}
		if (STATE_MATERIALS.equals(state) || STATE_LEGACY_START.equals(state)) {
			res.add("Próba pieczy została uznana. Zaufanie, słowo i prawo utrzymały jedno zobowiązanie. Mam oddać pierścień barona, 80 sztabek srebra, 50 sztabek złota i 25 sztabek mithrilu, aby Zdzichu przekuł poprzedni znak w nowy.");
			return res;
		}
		if (state != null && state.startsWith(FORGING_PREFIX)) {
			res.add("Zdzichu rozkuwa starą oprawę i osadza rdzeń baroniego znaku w nowym pierścieniu. Mam wrócić po zakończeniu pracy.");
			return res;
		}
		if (STATE_DONE.equals(state)) {
			res.add("Odebrałem pierścień magnata z zachowanym rdzeniem baroniego znaku.");
			res.add("Zadanie zakończone. Znak magnata nie zastąpił wcześniejszych rang: łączy zaufanie mieszczanina, służbę rycerza i odpowiedzialność barona w obowiązku pieczy nad majątkiem służącym wielu ludziom.");
			return res;
		}
		final List<String> debug = new ArrayList<>();
		debug.add("Stan zadania to: " + state);
		logger.error("Historia Pierścienia Magnata nie pasuje do stanu zadania: " + state);
		return debug;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Pierścień Magnata";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
