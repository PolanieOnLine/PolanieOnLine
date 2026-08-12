/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.Occasion;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.quests.EasterGiftsForChildren;
import games.stendhal.server.maps.quests.GoodiesForRudolph;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.MineTownRevivalWeeks;
import games.stendhal.server.maps.quests.MineTownRevivalWeeksConstruction;
import games.stendhal.server.maps.zakopane.city.MariuszekNPC;
import games.stendhal.server.util.ResetSpeakerNPC;

/**
 * Coordinates runtime seasonal event transitions.
 *
 * Expensive XML/TMX preparation runs on one dedicated worker. Applying a
 * completely prepared plan is scheduled through {@link TurnNotifier}, so live
 * world mutations happen on the RP thread.
 */
public final class SeasonalEventService {
	private static final Logger LOGGER = Logger.getLogger(SeasonalEventService.class);
	private static final SeasonalEventService INSTANCE = new SeasonalEventService();
	private static final String RUDOLPH_QUEST = "Przysmaki Rudolpha";

	private static final Transition<ChristmasEventPlan> CHRISTMAS =
			new Transition<ChristmasEventPlan>(ChristmasEventPlan.PROPERTY, "Christmas") {
				@Override
				ChristmasEventPlan prepare(final boolean enabled) throws Exception {
					return ChristmasEventPlan.prepare(enabled);
				}

				@Override
				void apply(final SeasonalEventService service, final boolean previous,
						final ChristmasEventPlan target, final ChristmasEventPlan rollback,
						final ResultListener listener) {
					service.applyChristmasTransition(previous, target, rollback, listener);
				}
			};

	private static final Transition<MineTownEventPlan> MINE_TOWN =
			new Transition<MineTownEventPlan>(MineTownEventPlan.PROPERTY, "Mine Town") {
				@Override
				MineTownEventPlan prepare(final boolean enabled) throws Exception {
					return MineTownEventPlan.prepare(enabled);
				}

				@Override
				void apply(final SeasonalEventService service, final boolean previous,
						final MineTownEventPlan target, final MineTownEventPlan rollback,
						final ResultListener listener) {
					service.applyMineTownTransition(previous, target, rollback, listener);
				}
			};

	private static final Transition<MineTownConstructionEventPlan> MINE_TOWN_CONSTRUCTION =
			new Transition<MineTownConstructionEventPlan>(MineTownConstructionEventPlan.PROPERTY,
					"Budowa Mine Town") {
				@Override
				MineTownConstructionEventPlan prepare(final boolean enabled) throws Exception {
					return MineTownConstructionEventPlan.prepare(enabled);
				}

				@Override
				void apply(final SeasonalEventService service, final boolean previous,
						final MineTownConstructionEventPlan target,
						final MineTownConstructionEventPlan rollback,
						final ResultListener listener) {
					service.applyMineTownConstructionTransition(previous, target, rollback, listener);
				}
			};

	private static final Transition<EasterEventPlan> EASTER =
			new Transition<EasterEventPlan>(EasterEventPlan.PROPERTY, "Easter") {
				@Override
				EasterEventPlan prepare(final boolean enabled) throws Exception {
					return EasterEventPlan.prepare(enabled);
				}

				@Override
				void apply(final SeasonalEventService service, final boolean previous,
						final EasterEventPlan target, final EasterEventPlan rollback,
						final ResultListener listener) {
					service.applyEasterTransition(previous, target, rollback, listener);
				}
			};

	private final ExecutorService worker;
	private final AtomicBoolean transitionInProgress = new AtomicBoolean(false);

	private SeasonalEventService() {
		worker = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(final Runnable runnable) {
				final Thread thread = new Thread(runnable, "SeasonalEventPrepare");
				thread.setDaemon(true);
				return thread;
			}
		});
	}

	public static SeasonalEventService get() {
		return INSTANCE;
	}

	public boolean isChristmasEnabled() {
		return CHRISTMAS.isEnabled();
	}

	public boolean isMineTownEnabled() {
		return MINE_TOWN.isEnabled();
	}

	public boolean isMineTownConstructionEnabled() {
		return MINE_TOWN_CONSTRUCTION.isEnabled();
	}

	public boolean isEasterEnabled() {
		return EASTER.isEnabled();
	}

	public boolean isTransitionInProgress() {
		return transitionInProgress.get();
	}

	public boolean requestChristmas(final boolean enabled,
			final ResultListener listener) {
		return request(CHRISTMAS, enabled, listener);
	}

	public boolean requestMineTown(final boolean enabled,
			final ResultListener listener) {
		return request(MINE_TOWN, enabled, listener);
	}

	public boolean requestMineTownConstruction(final boolean enabled,
			final ResultListener listener) {
		return request(MINE_TOWN_CONSTRUCTION, enabled, listener);
	}

	public boolean requestEaster(final boolean enabled,
			final ResultListener listener) {
		return request(EASTER, enabled, listener);
	}

	private <P extends PreparedSeasonalEventPlan> boolean request(
			final Transition<P> transition, final boolean enabled,
			final ResultListener listener) {
		if (!transitionInProgress.compareAndSet(false, true)) {
			return false;
		}

		final boolean previous = transition.isEnabled();
		if (previous == enabled) {
			finishAlreadyInState(listener, enabled,
					"Event " + transition.displayName + " jest już aktywny.",
					"Event " + transition.displayName + " jest już wyłączony.");
			return true;
		}

		worker.execute(new Runnable() {
			@Override
			public void run() {
				prepareTransition(transition, previous, enabled, listener);
			}
		});
		return true;
	}

	private <P extends PreparedSeasonalEventPlan> void prepareTransition(
			final Transition<P> transition, final boolean previous,
			final boolean enabled, final ResultListener listener) {
		try {
			final P target = transition.prepare(enabled);
			final P rollback = transition.prepare(previous);
			TurnNotifier.get().notifyInTurns(0, new TurnListener() {
				@Override
				public void onTurnReached(final int currentTurn) {
					transition.apply(SeasonalEventService.this, previous,
							target, rollback, listener);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Nie udało się przygotować eventu " + transition.displayName, e);
			notifyFailure(listener, "Nie udało się przygotować eventu "
					+ transition.displayName + ": " + readableMessage(e));
		}
	}

	private void finishAlreadyInState(final ResultListener listener,
			final boolean enabled, final String enabledMessage, final String disabledMessage) {
		transitionInProgress.set(false);
		if (listener != null) {
			listener.onResult(true, enabled ? enabledMessage : disabledMessage);
		}
	}

	private void applyChristmasTransition(final boolean previous,
			final ChristmasEventPlan target,
			final ChristmasEventPlan rollback,
			final ResultListener listener) {
		executeTransition("Christmas", "Christmas", target.isEnabled(), listener,
				new TransitionWork() {
					@Override
					public void run(final StageTracker stage) throws Exception {
						stage.set("ustawienie flagi stendhal.christmas");
						setSeasonalProperty(ChristmasEventPlan.PROPERTY, target.isEnabled());
						stage.set("zastosowanie przygotowanych zasobów świata");
						target.apply();
						stage.set("synchronizacja questa Rudolfa");
						synchronizeRudolphQuest(target.isEnabled());
						stage.set("odświeżenie NPC Mariuszek");
						refreshMariuszek();
					}
				},
				new TransitionWork() {
					@Override
					public void run(final StageTracker stage) throws Exception {
						stage.set("przywrócenie flagi stendhal.christmas");
						setSeasonalProperty(ChristmasEventPlan.PROPERTY, previous);
						stage.set("przywrócenie zasobów świata");
						rollback.apply();
						stage.set("przywrócenie questa Rudolfa");
						synchronizeRudolphQuest(previous);
						stage.set("przywrócenie NPC Mariuszek");
						refreshMariuszek();
					}
				});
	}

	private void applyMineTownTransition(final boolean previous,
			final MineTownEventPlan target,
			final MineTownEventPlan rollback,
			final ResultListener listener) {
		executeTransition("Mine Town", "Mine Town Revival Weeks", target.isEnabled(), listener,
				new TransitionWork() {
					@Override
					public void run(final StageTracker stage) throws Exception {
						stage.set("ustawienie flagi stendhal.minetown");
						setSeasonalProperty(MineTownEventPlan.PROPERTY, target.isEnabled());
						if (!target.isEnabled()) {
							stage.set("odłączenie Mine Town Revival Weeks");
							synchronizeMineTownQuest(false);
						}
						stage.set("zastosowanie przygotowanych zasobów Mine Town");
						target.apply();
						if (target.isEnabled()) {
							stage.set("uruchomienie Mine Town Revival Weeks");
							synchronizeMineTownQuest(true);
						}
						stage.set("ponowne podpięcie dialogu Easter do Caroline");
						reattachEasterQuestToCurrentCaroline();
					}
				},
				new TransitionWork() {
					@Override
					public void run(final StageTracker stage) throws Exception {
						stage.set("przywrócenie flagi stendhal.minetown");
						setSeasonalProperty(MineTownEventPlan.PROPERTY, previous);
						if (!previous) {
							stage.set("usunięcie częściowo uruchomionego questa Mine Town");
							synchronizeMineTownQuest(false);
						}
						stage.set("przywrócenie zasobów Mine Town");
						rollback.apply();
						if (previous) {
							stage.set("przywrócenie questa Mine Town");
							synchronizeMineTownQuest(true);
						}
						stage.set("ponowne podpięcie dialogu Easter do Caroline");
						reattachEasterQuestToCurrentCaroline();
					}
				});
	}

	private void applyMineTownConstructionTransition(final boolean previous,
			final MineTownConstructionEventPlan target,
			final MineTownConstructionEventPlan rollback,
			final ResultListener listener) {
		executeTransition("budowa Mine Town", "Budowa Mine Town Revival Weeks",
				target.isEnabled(), listener,
				new TransitionWork() {
					@Override
					public void run(final StageTracker stage) throws Exception {
						stage.set("ustawienie flagi stendhal.minetownconstruction");
						setSeasonalProperty(MineTownConstructionEventPlan.PROPERTY,
								target.isEnabled());
						if (!target.isEnabled()) {
							stage.set("odłączenie questa budowy Mine Town");
							synchronizeMineTownConstructionQuest(false);
						}
						stage.set("zastosowanie przygotowanych zasobów budowy Mine Town");
						target.apply();
						if (target.isEnabled()) {
							stage.set("uruchomienie questa budowy Mine Town");
							synchronizeMineTownConstructionQuest(true);
						}
					}
				},
				new TransitionWork() {
					@Override
					public void run(final StageTracker stage) throws Exception {
						stage.set("przywrócenie flagi stendhal.minetownconstruction");
						setSeasonalProperty(MineTownConstructionEventPlan.PROPERTY, previous);
						if (!previous) {
							stage.set("usunięcie częściowo uruchomionego questa budowy Mine Town");
							synchronizeMineTownConstructionQuest(false);
						}
						stage.set("przywrócenie zasobów budowy Mine Town");
						rollback.apply();
						if (previous) {
							stage.set("przywrócenie questa budowy Mine Town");
							synchronizeMineTownConstructionQuest(true);
						}
					}
				});
	}

	private void applyEasterTransition(final boolean previous,
			final EasterEventPlan target,
			final EasterEventPlan rollback,
			final ResultListener listener) {
		executeTransition("Easter", "Easter", target.isEnabled(), listener,
				new TransitionWork() {
					@Override
					public void run(final StageTracker stage) throws Exception {
						stage.set("ustawienie flagi stendhal.easter");
						setSeasonalProperty(EasterEventPlan.PROPERTY, target.isEnabled());
						stage.set("zastosowanie przygotowanych zasobów Easter");
						target.apply();
					}
				},
				new TransitionWork() {
					@Override
					public void run(final StageTracker stage) throws Exception {
						stage.set("przywrócenie flagi stendhal.easter");
						setSeasonalProperty(EasterEventPlan.PROPERTY, previous);
						stage.set("przywrócenie zasobów Easter");
						rollback.apply();
					}
				});
	}

	private void executeTransition(final String logName, final String resultName,
			final boolean enabled, final ResultListener listener,
			final TransitionWork apply, final TransitionWork rollback) {
		final StageTracker stage = new StageTracker("rozpoczęcie przełączenia");
		try {
			apply.run(stage);
			if (listener != null) {
				listener.onResult(true, enabled
						? "Event " + resultName + " został aktywowany bez restartu serwera."
						: "Event " + resultName + " został wyłączony bez restartu serwera.");
			}
		} catch (final Exception e) {
			LOGGER.error("Nie udało się zastosować eventu " + logName + " na etapie: "
					+ stage.get() + "; przywracam poprzedni stan", e);
			String rollbackFailure = null;
			final StageTracker rollbackStage = new StageTracker("rozpoczęcie rollbacku");
			try {
				rollback.run(rollbackStage);
			} catch (final Exception rollbackException) {
				LOGGER.error("Nie udało się w pełni przywrócić poprzedniego stanu " + logName
						+ " na etapie: " + rollbackStage.get(), rollbackException);
				rollbackFailure = "; dodatkowo rollback na etapie '" + rollbackStage.get()
						+ "' zgłosił: " + readableMessage(rollbackException);
			}
			if (listener != null) {
				listener.onResult(false, "Nie udało się przełączyć eventu " + logName
						+ " na etapie '" + stage.get() + "': " + readableMessage(e)
						+ (rollbackFailure == null ? "" : rollbackFailure));
			}
		} finally {
			transitionInProgress.set(false);
		}
	}

	private void notifyFailure(final ResultListener listener, final String message) {
		TurnNotifier.get().notifyInTurns(0, new TurnListener() {
			@Override
			public void onTurnReached(final int currentTurn) {
				try {
					if (listener != null) {
						listener.onResult(false, message);
					}
				} finally {
					transitionInProgress.set(false);
				}
			}
		});
	}

	private static void setSeasonalProperty(final String property, final boolean enabled) {
		if (enabled) {
			System.setProperty(property, "true");
		} else {
			System.clearProperty(property);
		}
		Occasion.refresh();
	}

	private static void synchronizeRudolphQuest(final boolean enabled) {
		if (enabled) {
			unloadRudolphQuestIfPresent();
			loadFreshRudolphQuest();
		} else {
			ensureRudolphQuestLoaded();
		}
	}

	private static void unloadRudolphQuestIfPresent() {
		final StendhalQuestSystem quests = StendhalQuestSystem.get();
		final IQuest quest = quests.getQuest(RUDOLPH_QUEST);
		if (quest != null && !quests.unloadQuest(quest)) {
			throw new IllegalStateException("Nie udało się odłączyć questa Rudolfa");
		}
	}

	private static void ensureRudolphQuestLoaded() {
		final StendhalQuestSystem quests = StendhalQuestSystem.get();
		if (quests.getQuest(RUDOLPH_QUEST) == null) {
			loadFreshRudolphQuest();
		}
	}

	private static void loadFreshRudolphQuest() {
		final StendhalQuestSystem quests = StendhalQuestSystem.get();
		quests.loadQuest(new GoodiesForRudolph());
		if (quests.getQuest(RUDOLPH_QUEST) == null) {
			throw new IllegalStateException("Nie udało się ponownie załadować questa Rudolfa");
		}
	}

	private static void synchronizeMineTownQuest(final boolean enabled) {
		final StendhalQuestSystem quests = StendhalQuestSystem.get();
		final IQuest current = quests.getQuest(MineTownRevivalWeeks.QUEST_NAME);
		if (current != null && !quests.unloadQuest(current)) {
			throw new IllegalStateException("Nie udało się odłączyć questa Mine Town Revival Weeks");
		}
		if (enabled) {
			quests.loadQuest(new MineTownRevivalWeeks());
			if (quests.getQuest(MineTownRevivalWeeks.QUEST_NAME) == null) {
				throw new IllegalStateException("Nie udało się załadować questa Mine Town Revival Weeks");
			}
		}
	}

	private static void synchronizeMineTownConstructionQuest(final boolean enabled) {
		final StendhalQuestSystem quests = StendhalQuestSystem.get();
		final IQuest current = quests.getQuest(MineTownRevivalWeeksConstruction.QUEST_NAME);
		if (current != null && !quests.unloadQuest(current)) {
			throw new IllegalStateException("Nie udało się odłączyć questa budowy Mine Town");
		}
		if (enabled) {
			quests.loadQuest(new MineTownRevivalWeeksConstruction());
			if (quests.getQuest(MineTownRevivalWeeksConstruction.QUEST_NAME) == null) {
				throw new IllegalStateException("Nie udało się załadować questa budowy Mine Town");
			}
		}
	}

	/**
	 * Mine Town replaces the Caroline object. Easter dialogue is dynamically
	 * gated by Occasion.EASTER, but must be attached again to the newly created
	 * NPC object after such a replacement. No NPC reset happens here.
	 */
	private static void reattachEasterQuestToCurrentCaroline() {
		final StendhalQuestSystem quests = StendhalQuestSystem.get();
		final IQuest current = quests.getQuest(EasterGiftsForChildren.QUEST_NAME);
		if (current != null && !quests.unloadQuest(current)) {
			throw new IllegalStateException("Nie udało się odłączyć definicji questa Easter");
		}
		quests.loadQuest(new EasterGiftsForChildren());
		if (quests.getQuest(EasterGiftsForChildren.QUEST_NAME) == null) {
			throw new IllegalStateException("Nie udało się ponownie podpiąć questa Easter do Caroline");
		}
	}

	private static void refreshMariuszek() {
		final SpeakerNPC mariuszek = SingletonRepository.getNPCList().get("Mariuszek");
		if (mariuszek != null
				&& !ResetSpeakerNPC.reload(new MariuszekNPC(), "Mariuszek")) {
			throw new IllegalStateException("Nie udało się odświeżyć NPC Mariuszek");
		}
	}

	private static String readableMessage(final Exception exception) {
		final StringBuilder result = new StringBuilder();
		final String outerMessage = exception.getMessage();
		if (outerMessage == null || outerMessage.trim().isEmpty()) {
			result.append(exception.getClass().getSimpleName());
		} else {
			result.append(outerMessage);
		}

		Throwable root = exception;
		while (root.getCause() != null && root.getCause() != root) {
			root = root.getCause();
		}
		if (root != exception) {
			result.append("; przyczyna: ").append(root.getClass().getSimpleName());
			final String rootMessage = root.getMessage();
			if (rootMessage != null && !rootMessage.trim().isEmpty()) {
				result.append(": ").append(rootMessage);
			}
		}

		for (final StackTraceElement element : root.getStackTrace()) {
			if (element.getClassName().startsWith("games.stendhal.")) {
				result.append(" [").append(element.getClassName())
						.append(':').append(element.getLineNumber()).append(']');
				break;
			}
		}
		return result.toString();
	}

	private abstract static class Transition<P extends PreparedSeasonalEventPlan> {
		private final String property;
		private final String displayName;

		Transition(final String property, final String displayName) {
			this.property = property;
			this.displayName = displayName;
		}

		final boolean isEnabled() {
			return System.getProperty(property) != null;
		}

		abstract P prepare(boolean enabled) throws Exception;

		abstract void apply(SeasonalEventService service, boolean previous,
				P target, P rollback, ResultListener listener);
	}

	private interface TransitionWork {
		void run(StageTracker stage) throws Exception;
	}

	private static final class StageTracker {
		private String stage;

		StageTracker(final String initialStage) {
			stage = initialStage;
		}

		void set(final String newStage) {
			stage = newStage;
		}

		String get() {
			return stage;
		}
	}

	public interface ResultListener {
		void onResult(boolean success, String message);
	}
}
