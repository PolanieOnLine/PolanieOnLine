/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.ZoneEnterExitListener;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.PlayerPrivateSpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;
import marauroa.common.game.RPObject;

/**
 * First social status quest.
 *
 * The ring is not sold for a large pile of resources. Marianek only opens the
 * old custom. The actual judgement is built from what the player does for
 * ordinary people who do not know that they are taking part in a trial.
 */
public class PierscienMieszczanina extends AbstractQuest {

	static final String QUEST_SLOT = "pierscien_mieszczanina";
	static final String STATE_REJECTED = "rejected";
	static final String STATE_LEGACY_START = "start";
	static final String STATE_ROAD = "road";
	static final String STATE_MEDICINE = "medicine";
	static final String STATE_MEDICINE_FOUND = "medicine_found";
	static final String STATE_MEDICINE_TO_ZYWIA = "medicine_to_zywia";
	static final String STATE_SETTLEMENT = "settlement";
	static final String STATE_STACH = "stach";
	static final String STATE_TRACKS = "tracks";
	static final String STATE_REPAIR = "repair";
	static final String STATE_DONE = "done";

	static final String ZLOTA_CIUPAGA_QUEST_SLOT = "zlota_ciupaga";
	private static final int REQUIRED_LEVEL = 150;
	private static final Logger logger = Logger.getLogger(PierscienMieszczanina.class);

	private final SpeakerNPC npc = npcs.get("Marianek");

	private final ZoneEnterExitListener roadListener = new ZoneEnterExitListener() {
		@Override
		public void onEntered(final RPObject object, final StendhalRPZone zone) {
			if (object instanceof Player) {
				syncPrivateRoadScene((Player) object, zone);
			}
		}

		@Override
		public void onExited(final RPObject object, final StendhalRPZone zone) {
			if (object instanceof Player) {
				removePrivateRoadScene((Player) object, zone);
			}
		}
	};

	private ChatCondition canStartStatusQuest() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				return !player.isBadBoy()
						&& player.getLevel() >= REQUIRED_LEVEL
						&& player.isQuestCompleted(ZLOTA_CIUPAGA_QUEST_SLOT);
			}
		};
	}

	private ChatCondition belowRequiredLevel() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				return !player.isBadBoy() && player.getLevel() < REQUIRED_LEVEL;
			}
		};
	}

	private ChatCondition missingCraftPrerequisite() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				return !player.isBadBoy()
						&& player.getLevel() >= REQUIRED_LEVEL
						&& !player.isQuestCompleted(ZLOTA_CIUPAGA_QUEST_SLOT);
			}
		};
	}

	private ChatCondition isBadBoy() {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				return player.isBadBoy();
			}
		};
	}

	private OrCondition questNotStartedOrRejected() {
		return new OrCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new QuestInStateCondition(QUEST_SLOT, STATE_REJECTED));
	}

	private OrCondition questInAnyState(final String... states) {
		final ChatCondition[] conditions = new ChatCondition[states.length];
		for (int i = 0; i < states.length; i++) {
			conditions[i] = new QuestInStateCondition(QUEST_SLOT, states[i]);
		}
		return new OrCondition(conditions);
	}

	private AndCondition newTrialCondition() {
		return new AndCondition(questNotStartedOrRejected(), canStartStatusQuest());
	}

	private void prepareMarianekOffer() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_DONE),
				ConversationStates.ATTENDING,
				"Pierwszy znak już nosisz. Pamiętaj tylko, że jego wartość zależy od twojego słowa wobec innych ludzi.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_LEGACY_START),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						player.setQuest(QUEST_SLOT, STATE_ROAD);
						raiser.say("Dawny zwyczaj z monetami odrzucamy. Pierwszego znaku nie kupuje się kruszcem. Odszukaj na wschodnim trakcie mojego stryja Witomira.");
					}
				});

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_ROAD),
				ConversationStates.ATTENDING,
				"Odszukaj Witomira na wschodnim trakcie. Nie wspominaj mu o pierścieniu ani o formalnej próbie.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_MEDICINE),
				ConversationStates.ATTENDING,
				"Najpierw lekarstwo. Pierścień może poczekać, człowiek potrzebujący pomocy nie powinien.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_MEDICINE_FOUND),
				ConversationStates.ATTENDING,
				"Masz lekarstwo. Zanieś je ludziom, którzy na nie czekają, zamiast wracać teraz do mnie.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(questNotStartedOrRejected(), isBadBoy()),
				ConversationStates.ATTENDING,
				"Nosząc piętno zabitego rycerza nie dostaniesz dziś próby znaku zaufania. Najpierw uporządkuj własne sprawy.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(questNotStartedOrRejected(), belowRequiredLevel()),
				ConversationStates.ATTENDING,
				"Ta droga wymaga doświadczenia. Wróć, gdy osiągniesz poziom 150.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(questNotStartedOrRejected(), missingCraftPrerequisite()),
				ConversationStates.ATTENDING,
				"Najpierw dokończ u Andrzeja porządną złotą #ciupagę. Kiedy uzna pracę za skończoną, wróć do mnie.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				newTrialCondition(),
				ConversationStates.INFORMATION_7,
				"Andrzej nauczył cię, że porządnej rzeczy nie robi się na pół. Pierwszy pierścień wymaga jednak czegoś więcej niż samego #rzemiosła.",
				null);

		npc.add(ConversationStates.INFORMATION_7,
				"rzemiosło",
				newTrialCondition(),
				ConversationStates.INFORMATION_8,
				"Złota ciupaga pokazała, że potrafisz doprowadzić własną pracę do końca. Dla pierwszego pierścienia ważniejszy jest stary #zwyczaj.",
				null);

		npc.add(ConversationStates.INFORMATION_8,
				"zwyczaj",
				newTrialCondition(),
				ConversationStates.INFORMATION_9,
				"Pierwszego znaku nie wykuwa się dla człowieka, który sam uważa się za godnego. Muszą powiedzieć to inni. W tej sprawie ważny będzie #Witomir.",
				null);

		npc.add(ConversationStates.INFORMATION_9,
				"Witomir",
				newTrialCondition(),
				ConversationStates.QUEST_OFFERED,
				"Mój stryj wozi zaopatrzenie do przysiółków na wschód stąd. Odszukasz go i zobaczysz, czego naprawdę potrzebują ludzie na trakcie?",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dobrze. Jedź wschodnim traktem i znajdź Witomira. Nie proś go o dobre słowo, po prostu zobacz, czego potrzebuje.",
				new SetQuestAction(QUEST_SLOT, STATE_ROAD));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Jak chcesz. Do próby możesz wrócić, kiedy będziesz gotowy sprawdzić coś więcej niż własne ręce.",
				new SetQuestAction(QUEST_SLOT, STATE_REJECTED));
	}

	private void prepareSettlementDialogs() {
		final SpeakerNPC dobrawa = npcs.get("Dobrawa");
		final SpeakerNPC zywia = npcs.get("Żywia");
		final SpeakerNPC stach = npcs.get("Stach");
		final SpeakerNPC milost = npcs.get("Miłost");

		if (dobrawa == null || zywia == null || stach == null || milost == null) {
			logger.error("Nie można podłączyć dialogów Mieszczanina do mieszkańców przysiółka.");
			return;
		}

		dobrawa.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_MEDICINE_FOUND),
				ConversationStates.INFORMATION_6,
				"Poznaję zielony liść na wieku. To lekarstwo Witomira. Najpilniejszy jest teraz #ranny.",
				null);

		dobrawa.add(ConversationStates.INFORMATION_6,
				"ranny",
				new QuestInStateCondition(QUEST_SLOT, STATE_MEDICINE_FOUND),
				ConversationStates.ATTENDING,
				"Żywia czeka przy swojej chacie. Zanieś jej skrzynkę od razu, potem wróć do mnie.",
				new SetQuestAction(QUEST_SLOT, STATE_MEDICINE_TO_ZYWIA));

		dobrawa.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_MEDICINE_TO_ZYWIA),
				ConversationStates.ATTENDING,
				"Żywia czeka na skrzynkę. Najpierw człowiek, potem towar.",
				null);

		dobrawa.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_SETTLEMENT),
				ConversationStates.INFORMATION_6,
				"Ranny jest już pod opieką Żywii. Z wozu Witomira zniknęła jednak reszta #dostawy.",
				null);

		dobrawa.add(ConversationStates.INFORMATION_6,
				"dostawa",
				new QuestInStateCondition(QUEST_SLOT, STATE_SETTLEMENT),
				ConversationStates.INFORMATION_7,
				"Zabrali zapasy i narzędzia Stacha. To nie pierwszy wóz, który miał kłopoty. Wspólny jest leśny #objazd.",
				null);

		dobrawa.add(ConversationStates.INFORMATION_7,
				"objazd",
				new QuestInStateCondition(QUEST_SLOT, STATE_SETTLEMENT),
				ConversationStates.ATTENDING,
				"Porozmawiaj ze Stachem. Od kilku dni twierdzi, że przejazd przy rzece psuje się zbyt podejrzanie.",
				new SetQuestAction(QUEST_SLOT, STATE_STACH));

		zywia.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_MEDICINE_TO_ZYWIA),
				ConversationStates.INFORMATION_6,
				"Tak, to moja skrzynka. Ranny ma głęboką #ranę i same zioła już nie wystarczają.",
				null);

		zywia.add(ConversationStates.INFORMATION_6,
				"rana",
				new QuestInStateCondition(QUEST_SLOT, STATE_MEDICINE_TO_ZYWIA),
				ConversationStates.ATTENDING,
				"Teraz mogę go opatrzyć jak należy. Powiedz Dobrawie, że z tej strony sytuacja jest pod kontrolą.",
				new SetQuestAction(QUEST_SLOT, STATE_SETTLEMENT));

		zywia.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_SETTLEMENT),
				ConversationStates.ATTENDING,
				"Lekarstwo dotarło w porę. Ja zostaję przy rannym, a Dobrawa wie, co jeszcze zginęło z dostawy.",
				null);

		stach.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, STATE_STACH),
				ConversationStates.INFORMATION_6,
				"Przejazd przy rzece nie rozsypał się sam. Ktoś poluzował podpory, żeby cięższe wozy wybierały #objazd.",
				null);

		stach.add(ConversationStates.INFORMATION_6,
				"objazd",
				new QuestInStateCondition(QUEST_SLOT, STATE_STACH),
				ConversationStates.INFORMATION_7,
				"W lesie łatwo urządzić zasadzkę. Chciałem naprawić przejazd, ale z wozu Witomira zniknęły moje #narzędzia.",
				null);

		stach.add(ConversationStates.INFORMATION_7,
				"narzędzia",
				new QuestInStateCondition(QUEST_SLOT, STATE_STACH),
				ConversationStates.INFORMATION_8,
				"Bez nich niewiele zrobię. Wysłałem po pomoc posłańca, ale #Radomir nie wrócił.",
				null);

		stach.add(ConversationStates.INFORMATION_8,
				"Radomir",
				new QuestInStateCondition(QUEST_SLOT, STATE_STACH),
				ConversationStates.ATTENDING,
				"Przy skraju lasu zostały ślady butów i rysy po ciągniętym ciężarze. Zacznij od nich, mogą prowadzić do sprawców.",
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						player.setQuest(QUEST_SLOT, STATE_TRACKS);
						final StendhalRPZone zone = player.getZone();
						if (zone != null) {
							syncPrivateRoadScene(player, zone);
						}
					}
				});

		stach.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, STATE_TRACKS),
						new QuestInStateCondition(MieszczaninHideoutProgress.SLOT,
								MieszczaninHideoutProgress.TOOLS_RECOVERED)),
				ConversationStates.ATTENDING,
				"To moje narzędzia. Dobrze, że Radomir żyje. Napastnicy stracili kryjówkę, więc teraz została naprawa przejazdu.",
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						player.setQuest(QUEST_SLOT, STATE_REPAIR);
						MieszczaninHideoutProgress.clear(player);
						final StendhalRPZone zone = player.getZone();
						if (zone != null) {
							syncPrivateRoadScene(player, zone);
						}
					}
				});

		stach.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, STATE_TRACKS),
						new NotCondition(new QuestInStateCondition(MieszczaninHideoutProgress.SLOT,
								MieszczaninHideoutProgress.TOOLS_RECOVERED))),
				ConversationStates.ATTENDING,
				"Ślady zaczynają się przy skraju lasu. Szukaj odcisków butów, zgniecionej trawy i rysy po czymś ciężkim.",
				null);

		milost.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				questInAnyState(STATE_MEDICINE, STATE_MEDICINE_FOUND, STATE_MEDICINE_TO_ZYWIA),
				ConversationStates.ATTENDING,
				"Witomir ledwo doszedł do studni. Jeśli odzyskujesz jego ładunek, zacznij od lekarstwa.",
				null);

		milost.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				questInAnyState(STATE_SETTLEMENT, STATE_STACH, STATE_TRACKS),
				ConversationStates.ATTENDING,
				"Dobrze, że lekarstwo wróciło. Stach od dawna mówił, że ktoś korzysta na niszczejącym przejeździe.",
				null);
	}

	private boolean shouldShowWreck(final Player player) {
		return player.isQuestInState(QUEST_SLOT,
				STATE_ROAD,
				STATE_MEDICINE,
				STATE_MEDICINE_FOUND,
				STATE_MEDICINE_TO_ZYWIA);
	}

	private boolean shouldShowTracks(final Player player) {
		return player.isQuestInState(QUEST_SLOT, STATE_TRACKS);
	}

	private boolean shouldShowWitomir(final Player player) {
		return player.isQuestInState(QUEST_SLOT,
				STATE_ROAD,
				STATE_MEDICINE,
				STATE_MEDICINE_FOUND,
				STATE_MEDICINE_TO_ZYWIA,
				STATE_SETTLEMENT,
				STATE_STACH,
				STATE_TRACKS,
				STATE_REPAIR);
	}

	private void syncPrivateRoadScene(final Player player, final StendhalRPZone zone) {
		if (zone == null || !MieszczaninRoadScene.ZONE_NAME.equals(zone.getName())) {
			return;
		}

		if (shouldShowWreck(player)) {
			MieszczaninRoadScene.ensureWreckProps(zone, player);
		} else {
			MieszczaninRoadScene.removeWreckProps(zone, player);
		}

		if (shouldShowTracks(player)) {
			MieszczaninRoadScene.ensureTrackProps(zone, player);
		} else {
			MieszczaninRoadScene.removeTrackProps(zone, player);
		}

		if (!shouldShowWitomir(player)) {
			final WitomirNPC existing = findWitomir(zone, player);
			if (existing != null) {
				zone.remove(existing.getID());
			}
			return;
		}

		if (findWitomir(zone, player) != null) {
			return;
		}

		final WitomirNPC witomir = new WitomirNPC(player);
		if (player.isQuestInState(QUEST_SLOT, STATE_ROAD)) {
			witomir.setPosition(MieszczaninRoadScene.WITOMIR_START_X,
					MieszczaninRoadScene.WITOMIR_START_Y);
		} else {
			witomir.setPosition(MieszczaninRoadScene.WITOMIR_END_X,
					MieszczaninRoadScene.WITOMIR_END_Y);
			witomir.setDirection(MieszczaninRoadScene.finalDirection());
		}
		zone.add(witomir);
	}

	private void removePrivateRoadScene(final Player player, final StendhalRPZone zone) {
		if (!MieszczaninRoadScene.ZONE_NAME.equals(zone.getName())) {
			return;
		}
		MieszczaninRoadScene.removeWreckProps(zone, player);
		MieszczaninRoadScene.removeTrackProps(zone, player);
		final WitomirNPC witomir = findWitomir(zone, player);
		if (witomir != null) {
			zone.remove(witomir.getID());
		}
	}

	private WitomirNPC findWitomir(final StendhalRPZone zone, final Player owner) {
		for (final Entity entity : zone.getEntitiesOfClass(WitomirNPC.class)) {
			final WitomirNPC witomir = (WitomirNPC) entity;
			if (witomir.isOwnedBy(owner)) {
				return witomir;
			}
		}
		return null;
	}

	private final class WitomirNPC extends PlayerPrivateSpeakerNPC {
		WitomirNPC(final Player owner) {
			super(owner, "Witomir");
			setDescription("Oto Witomir, starszy kupiec i dostawca. Jego wóz wygląda, jakby niedawno spotkało go nieszczęście.");
			setEntityClass("oldmannpc");
			setGender("M");
			setIdleDirection(Direction.DOWN);
		}

		private ChatCondition roadOwnerCondition() {
			return new ChatCondition() {
				@Override
				public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
					return isOwnedBy(player) && player.isQuestInState(QUEST_SLOT, STATE_ROAD);
				}
			};
		}

		@Override
		protected void createDialog() {
			add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					roadOwnerCondition(),
					ConversationStates.ATTENDING,
					"Dobrze, że ktoś wreszcie idzie traktem. To nie był zwykły wypadek, tylko #napad.",
					null);

			add(ConversationStates.ATTENDING,
					"napad",
					roadOwnerCondition(),
					ConversationStates.INFORMATION_1,
					"Zwolniłem przy uszkodzonym przejeździe i wtedy wyszli z lasu zamaskowani ludzie. Nie rabowali na oślep, interesował ich mój #ładunek.",
					null);

			add(ConversationStates.INFORMATION_1,
					"ładunek",
					roadOwnerCondition(),
					ConversationStates.INFORMATION_2,
					"Zabrali żywność, narzędzia i lżejsze skrzynie. Najbardziej martwi mnie #lekarstwo dla przysiółka.",
					null);

			add(ConversationStates.INFORMATION_2,
					"lekarstwo",
					roadOwnerCondition(),
					ConversationStates.QUEST_OFFERED,
					"Mała skrzynka ma zielony liść na wieku. Jeden napastnik niósł ją na zachód i ledwo dawał radę. Pomożesz mi jej poszukać?",
					null);

			add(ConversationStates.QUEST_OFFERED,
					ConversationPhrases.YES_MESSAGES,
					roadOwnerCondition(),
					ConversationStates.IDLE,
					"Dziękuję. Szukaj na zachód od wozu, przy skraju lasu, wypatruj zielonego liścia. Skrzynkę zanieś Dobrawie przy studni, ja ruszę ostrzec mieszkańców.",
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							player.setQuest(QUEST_SLOT, STATE_MEDICINE);
							final StendhalRPZone zone = player.getZone();
							if (zone != null) {
								syncPrivateRoadScene(player, zone);
							}
							setPath(new FixedPath(MieszczaninRoadScene.createWitomirPath(), false));
						}
					});

			add(ConversationStates.QUEST_OFFERED,
					ConversationPhrases.NO_MESSAGES,
					roadOwnerCondition(),
					ConversationStates.ATTENDING,
					"Rozumiem. Jeśli jednak zmienisz zdanie, ten #napad nadal nie daje mi spokoju.",
					null);

			add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
							return isOwnedBy(player) && player.isQuestInState(QUEST_SLOT, STATE_MEDICINE);
						}
					},
					ConversationStates.ATTENDING,
					"Najważniejsze jest teraz lekarstwo. Szukaj skrzynki z zielonym liściem na zachód od rozbitego wozu.",
					null);

			add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
							return isOwnedBy(player) && player.isQuestInState(QUEST_SLOT, STATE_MEDICINE_FOUND);
						}
					},
					ConversationStates.ATTENDING,
					"Masz ją. Dobrze. Zanieś skrzynkę Dobrawie, ona wie, komu trzeba ją oddać.",
					null);

			add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
							return isOwnedBy(player) && player.isQuestInState(QUEST_SLOT,
									STATE_MEDICINE_TO_ZYWIA, STATE_SETTLEMENT, STATE_STACH,
									STATE_TRACKS, STATE_REPAIR);
						}
					},
					ConversationStates.ATTENDING,
					"Ja zostanę przy studni. Dobrawa, Żywia i Stach znają tę okolicę lepiej ode mnie.",
					null);

			addGoodbye("Obyśmy następnym razem spotkali się przy całym wozie.");
		}
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Status Społeczny: Mieszczanin",
				"Po ukończeniu nauki złotej ciupagi u Andrzeja Marianek otwiera starą próbę pierwszego pierścienia. Tym razem nie chodzi o surowce, tylko o to, czy zwykli ludzie mogą na tobie polegać.",
				true);

		prepareMarianekOffer();
		prepareSettlementDialogs();

		final StendhalRPZone road = SingletonRepository.getRPWorld().getZone(MieszczaninRoadScene.ZONE_NAME);
		if (road != null) {
			road.addZoneEnterExitListener(roadListener);
		}
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}

		final String state = player.getQuest(QUEST_SLOT);
		if (STATE_REJECTED.equals(state)) {
			res.add("Marianek opowiedział mi o starym zwyczaju pierwszego pierścienia, ale na razie nie chcę iść tą drogą.");
			return res;
		}

		if (STATE_LEGACY_START.equals(state)) {
			res.add("Mam wrócić do Marianka. Dawna wersja próby mieszczanina została zastąpiona próbą opartą na zaufaniu ludzi.");
			return res;
		}

		res.add("Po nauce rzemiosła u Andrzeja Marianek opowiedział mi o drugim rodzaju próby. Pierwszego pierścienia nie przyznaje sobie sam zainteresowany. O jego wartości mają świadczyć ludzie, którym rzeczywiście pomógł.");

		if (STATE_ROAD.equals(state)) {
			res.add("Mam odszukać Witomira na wschodnim trakcie. Marianek wyraźnie zabronił mi wspominać mu o pierścieniu i formalnej próbie.");
			return res;
		}

		if (STATE_MEDICINE.equals(state)) {
			res.add("Witomir został napadnięty podczas dostawy do małego przysiółka. Najpilniejsza jest skradziona skrzynka z lekarstwem oznaczona zielonym liściem. Widział napastnika niosącego ją na zachód od rozbitego wozu, ku skrajowi lasu.");
			return res;
		}

		if (STATE_MEDICINE_FOUND.equals(state)) {
			res.add("Odzyskałem skrzynkę z lekarstwem. Powinienem zanieść ją Dobrawie w przysiółku.");
			return res;
		}

		if (STATE_MEDICINE_TO_ZYWIA.equals(state)) {
			res.add("Dobrawa kazała mi jak najszybciej zanieść skrzynkę Żywii przy małej chacie.");
			return res;
		}

		if (STATE_SETTLEMENT.equals(state)) {
			res.add("Żywia dostała lekarstwo i może zająć się rannym. Dobrawa chce porozmawiać o reszcie skradzionej dostawy.");
			return res;
		}

		if (STATE_STACH.equals(state)) {
			res.add("Dobrawa powiedziała, że napady nie są przypadkowe. Mam porozmawiać ze Stachem o uszkodzonym przejeździe przy rzece.");
			return res;
		}

		if (STATE_TRACKS.equals(state)) {
			if (MieszczaninHideoutProgress.areToolsRecovered(player)) {
				res.add("Oczyściłem kryjówkę, uwolniłem Radomira i odzyskałem narzędzia Stacha. Powinienem wrócić do Stacha.");
			} else if (MieszczaninHideoutProgress.isMessengerFreed(player)) {
				res.add("Pokonałem napastników i uwolniłem Radomira. Narzędzia Stacha leżą po zachodniej stronie kryjówki, odsunięte od stosu skradzionych skrzyń.");
			} else if (MieszczaninHideoutProgress.isCleared(player)) {
				res.add("Pokonałem napastników w leśnej kryjówce. Powinienem porozmawiać z uwięzionym posłańcem.");
			} else {
				res.add("Stach uważa, że ktoś celowo niszczy przejazd i zmusza wozy do leśnego objazdu. Zaginął też wysłany po pomoc Radomir. Mam iść za śladami butów i rysą po ciągniętym ładunku przy skraju lasu.");
			}
			return res;
		}

		if (STATE_REPAIR.equals(state)) {
			if (MieszczaninRepairProgress.isCommunityApproved(player)) {
				res.add("Dobrawa potwierdziła, że przysiółek znów może korzystać z traktu i wysłała wiadomość do Marianka. Powinienem wrócić do niego po wynik próby.");
			} else if (MieszczaninRepairProgress.isStachConfirmed(player)) {
				res.add("Stach sprawdził naprawę i uznał przejazd za bezpieczny. Mam porozmawiać z Dobrawą.");
			} else if (MieszczaninRepairProgress.isRepaired(player)) {
				res.add("Naprawiłem uszkodzony przejazd dwiema belkami i klamrami przygotowanymi przez Stacha. Powinien sprawdzić, czy naprawa wytrzyma pod pełnym wozem.");
			} else {
				res.add("Radomir jest bezpieczny, a Stach odzyskał narzędzia. Mam naprawić uszkodzony przejazd na północny wschód od przysiółka.");
			}
			return res;
		}

		if (STATE_DONE.equals(state)) {
			res.add("Ludzie z przysiółka uznali, że można na mnie polegać. Marianek wykuł dla mnie pierścień mieszczanina.");
			return res;
		}

		logger.error("Historia nie pasuje do stanu zadania: " + state);
		res.add("Stan zadania wymaga sprawdzenia przez administratora.");
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Pierścień Mieszczanina";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
