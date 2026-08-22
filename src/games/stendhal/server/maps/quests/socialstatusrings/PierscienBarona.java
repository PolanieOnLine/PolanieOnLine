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
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;
import games.stendhal.server.util.TimeUtil;

/** Third social-status trial: judgment followed by responsibility for its consequences. */
public class PierscienBarona extends AbstractQuest {
	static final String QUEST_SLOT = "pierscien_barona";
	static final String STATE_REJECTED = "rejected";
	static final String STATE_TRIAL_BOUNDARY = "trial_boundary";
	static final String STATE_TRIAL_GRANARY = "trial_granary";
	static final String STATE_TRIAL_JUDGMENT = "trial_judgment";
	static final String STATE_TRIAL_APPROVED = "trial_approved";
	static final String STATE_STEWARDSHIP = "stewardship";
	static final String STATE_MATERIALS = "materials";
	static final String STATE_LEGACY_LIST = "lista";
	static final String STATE_DONE = "done";
	static final String FORGING_PREFIX = "forging;";

	static final String DUTY_SLOT = "pierscien_barona_duty";
	static final int DUTY_BOUNDARY_INDEX = 0;
	static final int DUTY_GRANARY_INDEX = 1;
	static final int DUTY_JUDGMENT_INDEX = 2;
	static final int DUTY_JUDGMENT_STARTED_INDEX = 3;
	static final String DUTY_ASKED = "asked";
	static final String DUTY_STARTED = "started";
	static final String DUTY_DONE = "done";
	static final int JUDGMENT_MINUTES = 45;
	static final int REQUIRED_BREAD = 12;

	static final String HUNGRY_JOSHUA_QUEST_SLOT = "hungry_joshua";
	static final String FISHERMANS_LICENSE2_QUEST_SLOT = "fishermans_license2";
	static final String OBSIDIAN_KNIFE_QUEST_SLOT = "obsidian_knife";
	static final String MITHRIL_CLOAK_QUEST_SLOT = "mithril_cloak";
	static final String CIUPAGA_DWA_WASY_QUEST_SLOT = "ciupaga_dwa_wasy";

	private static final int REQUIRED_LEVEL = 350;
	private static final int REQUIRED_GOLD = 40;
	private static final int REQUIRED_MITHRIL = 20;
	private static final int REQUIRED_PLATINUM = 10;
	private static final int FORGING_MINUTES = 120;
	private static final int REWARD_XP = 100000;
	private static final Logger logger = Logger.getLogger(PierscienBarona.class);

	private final SpeakerNPC npc = npcs.get("eDragon");
	private final SpeakerNPC boundaryWitness = npcs.get("Edgard");
	private final SpeakerNPC granaryWitness = npcs.get("Dobrawa");
	private final SpeakerNPC judgmentWitness = npcs.get("Zakonnik");

	private boolean hasLegacyPrerequisites(final Player player) {
		return player.isQuestCompleted(CIUPAGA_DWA_WASY_QUEST_SLOT)
				&& player.isQuestCompleted(HUNGRY_JOSHUA_QUEST_SLOT)
				&& player.isQuestCompleted(FISHERMANS_LICENSE2_QUEST_SLOT)
				&& player.isQuestCompleted(OBSIDIAN_KNIFE_QUEST_SLOT)
				&& player.isQuestCompleted(MITHRIL_CLOAK_QUEST_SLOT);
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
						&& player.isQuestCompleted(PierscienRycerza.QUEST_SLOT)
						&& player.isEquipped("pierścień rycerza")
						&& hasLegacyPrerequisites(player);
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
				new QuestInStateCondition(QUEST_SLOT, STATE_LEGACY_LIST));
	}

	private ChatCondition dutyStateCondition(final int index, final String state) {
		return new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, STATE_STEWARDSHIP),
				new QuestInStateCondition(DUTY_SLOT, index, state));
	}

	private ChatCondition dutyNotDoneCondition(final int index) {
		return new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, STATE_STEWARDSHIP),
				new NotCondition(new QuestInStateCondition(DUTY_SLOT, index, DUTY_DONE)));
	}

	private AndCondition allDutiesDoneCondition() {
		return new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, STATE_STEWARDSHIP),
				new QuestInStateCondition(DUTY_SLOT, DUTY_BOUNDARY_INDEX, DUTY_DONE),
				new QuestInStateCondition(DUTY_SLOT, DUTY_GRANARY_INDEX, DUTY_DONE),
				new QuestInStateCondition(DUTY_SLOT, DUTY_JUDGMENT_INDEX, DUTY_DONE));
	}

	private ChatCondition pendingDutiesCondition() {
		return new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, STATE_STEWARDSHIP),
				new NotCondition(allDutiesDoneCondition()));
	}

	private boolean isDutyDone(final Player player, final int index) {
		return player.hasQuest(DUTY_SLOT) && DUTY_DONE.equals(player.getQuest(DUTY_SLOT, index));
	}

	private boolean isJudgmentDelayReady(final Player player) {
		if (!player.hasQuest(DUTY_SLOT)
				|| !DUTY_STARTED.equals(player.getQuest(DUTY_SLOT, DUTY_JUDGMENT_INDEX))) {
			return false;
		}
		try {
			final String startedValue = player.getQuest(DUTY_SLOT, DUTY_JUDGMENT_STARTED_INDEX);
			if (startedValue == null || startedValue.length() == 0) {
				return true;
			}
			final long started = Long.parseLong(startedValue);
			return System.currentTimeMillis() >= started + JUDGMENT_MINUTES * TimeUtil.MILLISECONDS_IN_MINUTE;
		} catch (final RuntimeException e) {
			return true;
		}
	}

	private ChatCondition judgmentDelayCondition(final boolean ready) {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				return player.isQuestInState(QUEST_SLOT, STATE_STEWARDSHIP)
						&& player.hasQuest(DUTY_SLOT)
						&& DUTY_STARTED.equals(player.getQuest(DUTY_SLOT, DUTY_JUDGMENT_INDEX))
						&& isJudgmentDelayReady(player) == ready;
			}
		};
	}

	private long remainingJudgmentMillis(final Player player) {
		try {
			final long started = Long.parseLong(player.getQuest(DUTY_SLOT, DUTY_JUDGMENT_STARTED_INDEX));
			return Math.max(0L,
					started + JUDGMENT_MINUTES * TimeUtil.MILLISECONDS_IN_MINUTE - System.currentTimeMillis());
		} catch (final RuntimeException e) {
			return 0L;
		}
	}

	private AndCondition hasForgingMaterials() {
		return new AndCondition(
				new PlayerHasItemWithHimCondition("pierścień rycerza", 1),
				new PlayerHasItemWithHimCondition("sztabka złota", REQUIRED_GOLD),
				new PlayerHasItemWithHimCondition("sztabka mithrilu", REQUIRED_MITHRIL),
				new PlayerHasItemWithHimCondition("sztabka platyny", REQUIRED_PLATINUM));
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
				new DropItemAction("pierścień rycerza", 1),
				new DropItemAction("sztabka złota", REQUIRED_GOLD),
				new DropItemAction("sztabka mithrilu", REQUIRED_MITHRIL),
				new DropItemAction("sztabka platyny", REQUIRED_PLATINUM),
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						player.setQuest(QUEST_SLOT, FORGING_PREFIX + System.currentTimeMillis());
					}
				});
	}

	private ChatAction finishForgingAction() {
		return new MultipleActions(
				new EquipItemAction("pierścień barona", 1, true,
						ItemCreationContext.questReward()),
				new IncreaseXPAction(REWARD_XP),
				new SetQuestAction(QUEST_SLOT, STATE_DONE));
	}

	private void prepareOffer() {
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_DONE), ConversationStates.ATTENDING,
				"Znak barona już otrzymałeś. Pamiętaj, ziemia może należeć do władcy na pergaminie, lecz ludzie zapamiętują przede wszystkim jego czyny.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, blockedNewTrialCondition(),
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.isBadBoy()) {
							raiser.say("Czuję na tobie krew rycerza. Kto nie potrafi panować nad własnym gniewem, nie powinien panować nad cudzym losem.");
						} else if (player.getLevel() < REQUIRED_LEVEL) {
							raiser.say("Za mało drogi masz jeszcze za sobą. Wróć, gdy osiągniesz poziom 350.");
						} else if (!player.isQuestCompleted(PierscienRycerza.QUEST_SLOT)) {
							raiser.say("Najpierw dotrzymaj rycerskiego słowa i zdobądź znak Zakonu. Baron, który nie zna obowiązku rycerza, byłby tylko bogatym próżniakiem.");
						} else if (!player.isEquipped("pierścień rycerza")) {
							raiser.say("Przynieś pierścień rycerza. Nowy znak ma wyrastać z poprzedniego, nie pojawiać się z niczego.");
						} else if (!hasLegacyPrerequisites(player)) {
							raiser.say("Zanim porozmawiamy o władaniu, dokończ dawne próby. Nakarm kowala, zdobądź drugą licencję rybaka, obsydianowy nóż, płaszcz z mithrilu i złotą ciupagę z dwoma wąsami. Każda uczy innego rodzaju odpowiedzialności.");
						}
					}
				});

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, newTrialCondition(),
				ConversationStates.INFORMATION_7,
				"Rycerski znak mówi, że potrafisz sam stanąć przed niebezpieczeństwem. Baron ponosi cięższy rodzaj odpowiedzialności. Jego słowo wysyła innych ludzi do pracy, na straż i przed sąd. Musisz więc zdecydować, gdy każda strona ma własną rację i każda decyzja ma cenę. To jest #władza.", null);

		npc.add(ConversationStates.INFORMATION_7, Arrays.asList("władza", "wladza"), newTrialCondition(),
				ConversationStates.INFORMATION_8,
				"Władza nie jest prawem do pierwszej misy i najcieplejszej izby. Jest obowiązkiem pilnowania granic, zapasów i ludzi, kiedy zima albo gniew zabiera łatwy wybór. Dlatego ważniejsza od rozkazu jest #odpowiedzialność.", null);

		npc.add(ConversationStates.INFORMATION_8, Arrays.asList("odpowiedzialność", "odpowiedzialnosc"), newTrialCondition(),
				ConversationStates.QUEST_OFFERED,
				"Jeśli przyjmiesz znak barona, nie wystarczy wskazać właściwej odpowiedzi. Najpierw wysłuchasz, dlaczego każda strona uważa się za pokrzywdzoną, a potem sam dopilnujesz skutków prawa. Bierzesz ten ciężar?", null);

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.YES_MESSAGES, newTrialCondition(),
				ConversationStates.ATTENDING,
				"Dobrze. Osądzisz trzy sprawy, ale nie dam ci ich jako zagadek z jedną wygodną odpowiedzią. Najpierw poznasz ludzi i ich racje. Pierwsza dotyczy starej #granicy między rodami.",
				new MultipleActions(new SetQuestAction(QUEST_SLOT, STATE_TRIAL_BOUNDARY), new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						player.addKarma(10);
					}
				}));

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, newTrialCondition(),
				ConversationStates.IDLE,
				"Rozsądna odmowa jest lepsza niż lekkomyślna przysięga. Wróć, gdy będziesz gotowy odpowiadać za więcej niż własny miecz.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, STATE_REJECTED, -10.0));
	}

	private void prepareJudgmentTrial() {
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_BOUNDARY), ConversationStates.ATTENDING,
				"Pierwszy spór nadal leży przed tobą. Dwa rody przesunęły płoty, a między nimi stoi stary kamień graniczny. Wróćmy do #granicy.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("granica", "granicy"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_BOUNDARY), ConversationStates.ATTENDING,
				"Nie rozstrzygaj jeszcze. Najpierw poznaj #rody. Oba przyszły z argumentami, które ich zdaniem usprawiedliwiają przesunięcie granicy.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("rody", "rodach"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_BOUNDARY), ConversationStates.ATTENDING,
				"Liczniejszy ród po ostatniej wichurze oczyścił zawalony trakt i obsiał skrawek ziemi, którego sąsiedzi nie byli w stanie uprawić. Twierdzi, że nie pozwolił gruntowi leżeć odłogiem i dlatego powinien go zatrzymać. Mniejszy ród przyprowadził starców pamiętających dawny podział i wskazuje stary kamień, choć sam przez kilka sezonów nie miał ludzi do pracy. Komu dasz pierwszeństwo przy ustalaniu prawa do granicy, #świadkom i dawnemu znakowi czy #rodowi, który ma dziś więcej ludzi i wykonał więcej pracy?", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("świadkom", "świadkowie", "swiadkom", "swiadkowie"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_BOUNDARY), ConversationStates.ATTENDING,
				"Dobrze. Praca silniejszego rodu może zasługiwać na wynagrodzenie, lecz sama nie przenosi starego prawa własności. Wysłuchujesz świadków, sprawdzasz znak i dopiero potem rozstrzygasz. Druga sprawa czeka przy zimowym #spichlerzu.",
				new SetQuestAction(QUEST_SLOT, STATE_TRIAL_GRANARY));
		npc.add(ConversationStates.ATTENDING, Arrays.asList("rodowi", "ród", "rod", "silniejszy"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_BOUNDARY), ConversationStates.ATTENDING,
				"Rozumiem pokusę. Silniejszy ród naprawdę wykonał pracę, której inni nie mogli. Ale jeśli sama przewaga ludzi i siły pozwala zmieniać granice, jutro każdy słabszy straci ziemię, gdy tylko zachoruje albo pójdzie na wojnę. Najpierw #świadkowie i dawny znak. Za wykonaną pracę można rozliczyć się osobno.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_GRANARY), ConversationStates.ATTENDING,
				"Druga sprawa dotyczy zimowych zapasów. Wróćmy do #spichlerza. Tu druga strona również nie przychodzi z głupim argumentem.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("spichlerz", "spichlerza", "spichlerzu"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_GRANARY), ConversationStates.ATTENDING,
				"Po przednówku zostało zboża na jedną osadę. Zanim zdecydujesz, wysłuchaj #kupca. Jego oferta może rozwiązać inny prawdziwy problem.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("kupiec", "kupca", "kupcowi"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_GRANARY), ConversationStates.ATTENDING,
				"Kupiec zapłaci potrójnie za całe zboże. Za tę sumę można jeszcze przed roztopami naprawić most, od którego zależy handel kilku wsi. Nie kłamie i nie kradnie. Daje uczciwą cenę. Tyle że jeśli wywiezie zapas za góry, miejscowi przejdą przednówek z pustymi garnkami. Co ma pierwszeństwo, bezpieczeństwo #wspólnoty czy #zysk, który pozwoli naprawić most?", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("wspólnota", "wspólnotę", "wspólnoty", "wspolnota", "wspolnote", "wspolnoty"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_GRANARY), ConversationStates.ATTENDING,
				"Tak. Most trzeba naprawić, ale nie ceną głodu tych, za których odpowiadasz. Spichlerz barona ma najpierw przeprowadzić ludzi przez niedostatek. Dopiero nadwyżkę można sprzedać i przeznaczyć na inne potrzeby. Ostatnia sprawa dotyczy #wyroku.",
				new SetQuestAction(QUEST_SLOT, STATE_TRIAL_JUDGMENT));
		npc.add(ConversationStates.ATTENDING, Arrays.asList("zysk", "pieniądze", "pieniadze"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_GRANARY), ConversationStates.ATTENDING,
				"To nie jest chciwa odpowiedź bez sensu. Most naprawdę wymaga naprawy. Ale pełna skrzynia srebra nie wykarmi ludzi, których skazałbyś na głód w imię przyszłego handlu. Najpierw chronisz #wspólnotę. Dla mostu musisz znaleźć rozwiązanie, które nie odbierze ludziom ostatniego zapasu.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_JUDGMENT), ConversationStates.ATTENDING,
				"Została trzecia sprawa. Tym razem nie chodzi o ziemię ani zapasy, tylko o człowieka, na którego czeka #wyrok.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("wyrok", "wyroku"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_JUDGMENT), ConversationStates.ATTENDING,
				"Straż przyprowadziła człowieka oskarżonego o podpalenie stodoły. Zanim odpowiesz, posłuchaj, dlaczego #tłum jest tak pewny winy. Gniew też ma swoje źródło.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("tłum", "tlum", "tłumu", "tlumu"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_JUDGMENT), ConversationStates.ATTENDING,
				"Właściciel stodoły stracił zapas na zimę, a kilka dni wcześniej oskarżony groził mu przy świadkach po sporze o dług. Ludzie widzą motyw, pamiętają groźbę i boją się kolejnego pożaru. Ale samego ognia nikt nie widział w jego ręku, a nocą przez drogę przechodzili też obcy. Czy przed karą żądasz #dowodu, czy pozwalasz, by o winie rozstrzygnął #gniew tłumu?", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("dowód", "dowodu", "dowod"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_JUDGMENT), ConversationStates.ATTENDING,
				"Właśnie tak. Groźba i motyw są powodem do śledztwa, nie gotowym wyrokiem. Tłum może być pewny i nadal się mylić. Władca, który karze bez dowodu, uczy ludzi bać się jego humoru zamiast szanować prawo. Znasz zasadę. Zostało nazwać #prawo, któremu sam też podlegasz.",
				new SetQuestAction(QUEST_SLOT, STATE_TRIAL_APPROVED));
		npc.add(ConversationStates.ATTENDING, Arrays.asList("gniew", "kara"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_JUDGMENT), ConversationStates.ATTENDING,
				"Rozumiem, dlaczego tłum chce szybkiej kary. Strata jest prawdziwa, a groźba naprawdę padła. Ale prawo nie może zamienić podejrzenia w pewność tylko dlatego, że ludzie się boją. Bez #dowodu skazałbyś człowieka za to, że pasuje do opowieści o winie, nie za udowodniony czyn.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_APPROVED), ConversationStates.ATTENDING,
				"Trzy zasady nazwałeś poprawnie. W każdej odrzuciłeś łatwiejszy nacisk, choć miał za sobą jakiś rozsądny argument. Teraz powiedz o #prawie. Jeśli naprawdę ma wiązać władcę, wyślę cię dopilnować skutków własnych rozstrzygnięć.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("prawo", "prawie", "prawa"),
				new QuestInStateCondition(QUEST_SLOT, STATE_TRIAL_APPROVED), ConversationStates.ATTENDING,
				"Słowa przyjęte, lecz próba dopiero teraz staje się trudna. Edgard ma ogłosić, że liczebna przewaga silniejszego rodu nie zmienia #granicy. Dobrawa odłoży pierwszy #zapas dla wspólnoty. Zakonnik dopilnuje, by bez dowodu nie zapadł #wyrok. Wróć, gdy każde z nich potwierdzi wykonanie prawa.",
				new SetQuestAction(QUEST_SLOT, STATE_STEWARDSHIP));
	}

	private void prepareStewardship() {
		boundaryWitness.add(ConversationStates.ATTENDING, Arrays.asList("granica", "granicy"),
				dutyNotDoneCondition(DUTY_BOUNDARY_INDEX), ConversationStates.ATTENDING,
				"eDragon przysłał wieść. Dwa rody już czekają, a silniejszy chce, bym ogłosił jego wersję jako pierwszą. Jeśli mam zanieść twoje rozstrzygnięcie, potwierdź mi #równość obu rodów wobec świadectwa i starego znaku.",
				new SetQuestAction(DUTY_SLOT, DUTY_BOUNDARY_INDEX, DUTY_ASKED));
		boundaryWitness.add(ConversationStates.ATTENDING, Arrays.asList("równość", "rownosc", "równo", "rowno"),
				dutyStateCondition(DUTY_BOUNDARY_INDEX, DUTY_ASKED), ConversationStates.ATTENDING,
				"Tak ogłoszę obu rodom. Żaden z nich nie przesunie granicy samą liczbą ludzi. Najpierw świadkowie i dawny znak. Od tej chwili nie możesz udawać, że rozstrzygnięcie było tylko ćwiczeniem.",
				new SetQuestAction(DUTY_SLOT, DUTY_BOUNDARY_INDEX, DUTY_DONE));
		boundaryWitness.add(ConversationStates.ATTENDING, Arrays.asList("granica", "granicy", "równość", "rownosc"),
				dutyStateCondition(DUTY_BOUNDARY_INDEX, DUTY_DONE), ConversationStates.ATTENDING,
				"Rozstrzygnięcie granicy już ogłosiłem w twoim imieniu. Teraz odpowiedzialność za nie jest publiczna, nie tylko wypowiedziana smokowi.", null);

		granaryWitness.add(ConversationStates.ATTENDING, Arrays.asList("spichlerz", "spichlerza", "spichlerzu"),
				dutyNotDoneCondition(DUTY_GRANARY_INDEX), ConversationStates.ATTENDING,
				"Jeśli spichlerz ma służyć ludziom przed zyskiem, pokaż to czymś prostym. Nie chcę skarbca ani dziesięciu rodzajów darów. Odłóż dwanaście chlebów jako pierwszy #zapas dla tych, którzy przyjdą bez monety.",
				new SetQuestAction(DUTY_SLOT, DUTY_GRANARY_INDEX, DUTY_ASKED));
		granaryWitness.add(ConversationStates.ATTENDING, Arrays.asList("zapas", "zapasy"),
				new AndCondition(dutyStateCondition(DUTY_GRANARY_INDEX, DUTY_ASKED),
						new PlayerHasItemWithHimCondition("chleb", REQUIRED_BREAD)), ConversationStates.ATTENDING,
				"Dwanaście chlebów zostaje dla wspólnoty. To nie danina dla mnie. To znak, że przy pierwszym niedostatku umiesz odjąć od własnego zapasu, zanim każesz ludziom zaciskać pasa.",
				new MultipleActions(new DropItemAction("chleb", REQUIRED_BREAD),
						new SetQuestAction(DUTY_SLOT, DUTY_GRANARY_INDEX, DUTY_DONE)));
		granaryWitness.add(ConversationStates.ATTENDING, Arrays.asList("zapas", "zapasy"),
				new AndCondition(dutyStateCondition(DUTY_GRANARY_INDEX, DUTY_ASKED),
						new NotCondition(new PlayerHasItemWithHimCondition("chleb", REQUIRED_BREAD))), ConversationStates.ATTENDING,
				"Nie ma jeszcze pełnego zapasu. Potrzeba dwunastu chlebów razem, żebym mogła odłożyć je dla wspólnoty. Nie oddawaj mi po jednym. Wróć z całością.", null);
		granaryWitness.add(ConversationStates.ATTENDING, Arrays.asList("spichlerz", "zapas", "zapasy"),
				dutyStateCondition(DUTY_GRANARY_INDEX, DUTY_DONE), ConversationStates.ATTENDING,
				"Zapas jest odłożony. Nie jest wielki, ale wystarczył, by twoja decyzja kosztowała coś więcej niż jedno poprawne słowo.", null);

		judgmentWitness.add(ConversationStates.ATTENDING, Arrays.asList("wyrok", "wyroku"),
				new AndCondition(dutyNotDoneCondition(DUTY_JUDGMENT_INDEX),
						new NotCondition(new QuestInStateCondition(DUTY_SLOT, DUTY_JUDGMENT_INDEX, DUTY_STARTED))),
				ConversationStates.ATTENDING,
				"Straż trzyma oskarżonego, a pod bramą zbiera się tłum. Skoro zażądałeś dowodu, przez czterdzieści pięć minut nie będzie kary ani samosądu. To czas na ostudzenie gniewu i sprawdzenie zeznań. Wróć potem po #dowód. Wcześniej nie potwierdzę, że dotrzymałeś własnej zasady.",
				new MultipleActions(new SetQuestAction(DUTY_SLOT, DUTY_JUDGMENT_INDEX, DUTY_STARTED), new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						player.setQuest(DUTY_SLOT, DUTY_JUDGMENT_STARTED_INDEX, Long.toString(System.currentTimeMillis()));
					}
				}));
		judgmentWitness.add(ConversationStates.ATTENDING, Arrays.asList("wyrok", "wyroku", "dowód", "dowodu", "dowodzie", "dowod"),
				judgmentDelayCondition(false), ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final long remaining = remainingJudgmentMillis(player);
						raiser.say("Jeszcze nie. Tłum nadal krzyczy, ale właśnie wtedy prawo ma być cięższe od gniewu. Zostało około "
								+ TimeUtil.approxTimeUntil((int) (remaining / 1000L)) + ". Potem wróć po #dowód.");
					}
				});
		judgmentWitness.add(ConversationStates.ATTENDING, Arrays.asList("wyrok", "wyroku"),
				judgmentDelayCondition(true), ConversationStates.ATTENDING,
				"Czas minął, tłum się rozszedł, a zeznania można było sprawdzić bez wrzasku pod oknem. Teraz możesz oprzeć decyzję na #dowodzie, nie na pośpiechu.", null);
		judgmentWitness.add(ConversationStates.ATTENDING, Arrays.asList("dowód", "dowodu", "dowodzie", "dowod"),
				judgmentDelayCondition(true), ConversationStates.ATTENDING,
				"Potwierdzę eDragonowi, że nie złamałeś własnej zasady pod naciskiem tłumu. Odroczyłeś karę i dałeś czas na sprawdzenie zeznań, zanim zapadł wyrok.",
				new SetQuestAction(DUTY_SLOT, DUTY_JUDGMENT_INDEX, DUTY_DONE));
		judgmentWitness.add(ConversationStates.ATTENDING, Arrays.asList("wyrok", "wyroku", "dowód", "dowodu", "dowodzie", "dowod"),
				dutyStateCondition(DUTY_JUDGMENT_INDEX, DUTY_DONE), ConversationStates.ATTENDING,
				"Moje potwierdzenie już masz. Tłum chciał szybkości, a ty wybrałeś prawo. Teraz odpowiadaj za ten wybór także wtedy, gdy nie ma mnie obok.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, pendingDutiesCondition(),
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final List<String> missing = new ArrayList<>();
						if (!isDutyDone(player, DUTY_BOUNDARY_INDEX)) {
							missing.add("ogłoszenie #granicy u Edgarda");
						}
						if (!isDutyDone(player, DUTY_GRANARY_INDEX)) {
							missing.add("#zapas u Dobrawy");
						}
						if (!isDutyDone(player, DUTY_JUDGMENT_INDEX)) {
							missing.add("odroczenie #wyroku u Zakonnika");
						}
						raiser.say("Zasady już znasz, ale baron odpowiada za wykonanie. Brakuje jeszcze "
								+ String.join(", ", missing) + ". Wróć, gdy skutki prawa będą równie prawdziwe jak twoje słowa.");
					}
				});
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, allDutiesDoneCondition(),
				ConversationStates.ATTENDING,
				"Teraz widzę różnicę między człowiekiem, który zna odpowiedzi, a tym, który bierze za nie odpowiedzialność. Granica została ogłoszona, wspólnota dostała zapas, a wyrok poczekał na dowód. Nazwij jeszcze raz #prawo, a uznam próbę.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("prawo", "prawie", "prawa"), allDutiesDoneCondition(),
				ConversationStates.ATTENDING,
				"Granicy nie przesunąłeś na korzyść silniejszego rodu tylko dlatego, że miał więcej ludzi. Zboża nie sprzedałeś kupcowi tylko dlatego, że dawał więcej srebra. Oskarżonego nie skazałeś pod naciskiem rozgniewanego tłumu. A potem sam wykonałeś te rozstrzygnięcia. Teraz mogę uznać próbę. Twój pierścień rycerza posłuży za rdzeń nowego znaku. Mój ogień potrzebuje jeszcze przygotowanego zapasu #materiałów do kilku prób stopu i hartowania.",
				new SetQuestAction(QUEST_SLOT, STATE_MATERIALS));
	}

	private void prepareForging() {
		npc.add(ConversationStates.ATTENDING, Arrays.asList("materiały", "materiałów", "materialy", "materialow", "dary", "przypomnij"),
				materialsStateCondition(), ConversationStates.QUEST_ITEM_QUESTION,
				"Przynieś pierścień rycerza, 40 sztabek złota, 20 sztabek mithrilu i 10 sztabek platyny. Złoto pójdzie nie tylko w widoczny znak urzędu, ale też w próby smoczego stopu. Mithril utrzyma i wzmocni stary rdzeń, a platyna przyjmie najgorętszą część żaru. Masz pełny zapas i oddajesz go pod mój ogień?", null);
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(materialsStateCondition(), hasForgingMaterials()), ConversationStates.IDLE,
				"Dobrze. To nie będzie zwykłe kowalskie grzanie. Mój ogień potrafi stopić to, co kowal ledwie rozgrzeje. Przekucie starego znaku i spokojne studzenie potrwa około dwóch godzin. Potem wróć po #pierścień.", startForgingAction());
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(materialsStateCondition(), new NotCondition(hasForgingMaterials())), ConversationStates.ATTENDING,
				"Nie będę przypalał połowy roboty. Brakuje któregoś z elementów. Potrzebny jest pierścień rycerza, 40 sztabek złota, 20 sztabek mithrilu oraz 10 sztabek platyny. Gdy zbierzesz całość, wrócimy do #materiałów.", null);
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.NO_MESSAGES, materialsStateCondition(),
				ConversationStates.ATTENDING, "Metal nie ucieknie. Kiedy będziesz gotów oddać pełne tworzywo, wrócimy do #materiałów.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, forgingCondition(false),
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final long remaining = remainingForgingMillis(player);
						raiser.say("Jeszcze nie. Smoczy ogień zrobił swoje, ale gwałtownie schłodzony metal pęka tak samo jak źle wydany rozkaz. Daj mu około "
								+ TimeUtil.approxTimeUntil((int) (remaining / 1000L)) + ".");
					}
				});
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pierścień", "pierscien"), forgingCondition(false),
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final long remaining = remainingForgingMillis(player);
						raiser.say("Jeszcze stygnie. Wróć za około " + TimeUtil.approxTimeUntil((int) (remaining / 1000L))
								+ ". Cierpliwość też jest częścią władzy.");
					}
				});
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, forgingCondition(true),
				ConversationStates.INFORMATION_9, "Metal ostygł i nie pękł pod próbą pazura. Twój #pierścień barona jest gotowy.", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pierścień", "pierscien"), forgingCondition(true),
				ConversationStates.ATTENDING,
				"Weź go. Stary rycerski rdzeń pozostał wewnątrz, bo władza bez obowiązku byłaby tylko ozdobą. Noś znak barona tak, by ludzie widzieli w nim odpowiedzialność, nie prawo do rozkazywania.", finishForgingAction());
		npc.add(ConversationStates.INFORMATION_9, Arrays.asList("pierścień", "pierscien"), forgingCondition(true),
				ConversationStates.ATTENDING,
				"Weź go. Stary rycerski rdzeń pozostał wewnątrz, bo władza bez obowiązku byłaby tylko ozdobą. Noś znak barona tak, by ludzie widzieli w nim odpowiedzialność, nie prawo do rozkazywania.", finishForgingAction());
	}

	@Override
	public void addToWorld() {
		fillQuestInfo("Status Społeczny: Baron",
				"Wysłuchaj stron trzech trudnych sporów, rozstrzygnij je według prawa, wykonaj ich skutki wobec rodów i wspólnoty, a dopiero potem pozwól eDragonowi przekuć rycerski znak.", true);
		prepareOffer();
		prepareJudgmentTrial();
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
		res.add(player.getGenderVerb("Spotkałem") + " eDragona, starego strażnika zwyczaju baronów.");
		if (STATE_REJECTED.equals(state)) {
			res.add("Nie przyjąłem jeszcze odpowiedzialności związanej ze znakiem barona.");
			return res;
		}
		res.add("eDragon przypomniał mi, że rycerz odpowiada głównie za własne czyny, a baron także za decyzje wykonywane przez innych i za ich skutki.");
		if (STATE_TRIAL_BOUNDARY.equals(state)) {
			res.add("Przechodzę pierwszą część smoczej próby. Dwa rody mają rozsądne argumenty w sporze o ziemię, ale muszę oddzielić wynagrodzenie za pracę od prawa do przesuwania dawnej granicy.");
			return res;
		}
		if (STATE_TRIAL_GRANARY.equals(state)) {
			res.add("W sporze o granicę postawiłem świadectwo i dawny znak ponad przewagą silniejszego rodu. Teraz ważę zimowy zapas przeciw uczciwej ofercie kupca, która mogłaby sfinansować naprawę mostu.");
			return res;
		}
		if (STATE_TRIAL_JUDGMENT.equals(state)) {
			res.add("Uznałem, że zapasy mają najpierw chronić wspólnotę przed głodem, choć sprzedaż mogłaby naprawić most. Została sprawa podejrzanego o podpalenie i tłumu, który ma prawdziwy powód do strachu, ale nie ma jeszcze dowodu winy.");
			return res;
		}
		if (STATE_TRIAL_APPROVED.equals(state)) {
			res.add("Zażądałem dowodu przed karą, odróżniając motyw i podejrzenie od udowodnionego czynu. eDragon oczekuje, że nazwę prawo i przyjmę obowiązek wykonania trzech rozstrzygnięć.");
			return res;
		}
		if (STATE_STEWARDSHIP.equals(state)) {
			res.add("Nie wystarczyło poprawnie osądzić spraw. Mam publicznie wykonać własne rozstrzygnięcia i zebrać niezależne potwierdzenia ich skutków.");
			if (isDutyDone(player, DUTY_BOUNDARY_INDEX)) {
				res.add("Edgard ogłosił obu rodom, że granicy nie przesuwa przewaga siły.");
			}
			if (isDutyDone(player, DUTY_GRANARY_INDEX)) {
				res.add("Dobrawa odłożyła dwanaście chlebów jako pierwszy zapas wspólnoty.");
			}
			if (isDutyDone(player, DUTY_JUDGMENT_INDEX)) {
				res.add("Zakonnik potwierdził, że wyrok został odroczony, aż gniew opadł i był czas na dowód.");
			}
			return res;
		}
		if (STATE_MATERIALS.equals(state) || STATE_LEGACY_LIST.equals(state)) {
			res.add("eDragon uznał próbę prawa dopiero po wykonaniu jej skutków. Mam oddać pierścień rycerza, 40 sztabek złota, 20 sztabek mithrilu i 10 sztabek platyny, aby przekuł stary znak w nowy.");
			return res;
		}
		if (state != null && state.startsWith(FORGING_PREFIX)) {
			res.add("eDragon przekuwa mój poprzedni znak w smoczym ogniu. Mam wrócić po spokojnym ostygnięciu metalu.");
			return res;
		}
		if (STATE_DONE.equals(state)) {
			res.add("Odebrałem pierścień barona z zachowanym rycerskim rdzeniem.");
			res.add("Zadanie zakończone. Znak przypomina, że władza wymaga wysłuchania racji, odparcia nacisku oraz odpowiedzialności za wykonanie prawa, nie tylko za jego wypowiedzenie.");
			return res;
		}
		final List<String> debug = new ArrayList<>();
		debug.add("Stan zadania to: " + state);
		logger.error("Historia Pierścienia Barona nie pasuje do stanu zadania: " + state);
		return debug;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Pierścień Barona";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
