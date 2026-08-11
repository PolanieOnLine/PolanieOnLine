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
import games.stendhal.server.maps.quests.GoodiesForRudolph;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.MineTownRevivalWeeks;
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

	/**
	 * @return singleton event service
	 */
	public static SeasonalEventService get() {
		return INSTANCE;
	}

	/**
	 * @return whether Christmas is currently enabled
	 */
	public boolean isChristmasEnabled() {
		return System.getProperty(ChristmasEventPlan.PROPERTY) != null;
	}

	/**
	 * @return whether Mine Town Revival Weeks is currently enabled
	 */
	public boolean isMineTownEnabled() {
		return System.getProperty(MineTownEventPlan.PROPERTY) != null;
	}

	/**
	 * @return whether a seasonal transition is being prepared or applied
	 */
	public boolean isTransitionInProgress() {
		return transitionInProgress.get();
	}

	/**
	 * Requests a Christmas state change.
	 *
	 * @param enabled target state
	 * @param listener result callback executed on the RP thread
	 * @return false when another transition is already in progress
	 */
	public boolean requestChristmas(final boolean enabled,
			final ResultListener listener) {
		if (!transitionInProgress.compareAndSet(false, true)) {
			return false;
		}

		final boolean previous = isChristmasEnabled();
		if (previous == enabled) {
			finishAlreadyInState(listener, enabled,
					"Event Christmas jest już aktywny.",
					"Event Christmas jest już wyłączony.");
			return true;
		}

		worker.execute(new Runnable() {
			@Override
			public void run() {
				prepareChristmasTransition(previous, enabled, listener);
			}
		});
		return true;
	}

	/**
	 * Requests a Mine Town Revival Weeks state change.
	 *
	 * @param enabled target state
	 * @param listener result callback executed on the RP thread
	 * @return false when another transition is already in progress
	 */
	public boolean requestMineTown(final boolean enabled,
			final ResultListener listener) {
		if (!transitionInProgress.compareAndSet(false, true)) {
			return false;
		}

		final boolean previous = isMineTownEnabled();
		if (previous == enabled) {
			finishAlreadyInState(listener, enabled,
					"Event Mine Town Revival Weeks jest już aktywny.",
					"Event Mine Town Revival Weeks jest już wyłączony.");
			return true;
		}

		worker.execute(new Runnable() {
			@Override
			public void run() {
				prepareMineTownTransition(previous, enabled, listener);
			}
		});
		return true;
	}

	private void finishAlreadyInState(final ResultListener listener,
			final boolean enabled, final String enabledMessage, final String disabledMessage) {
		transitionInProgress.set(false);
		if (listener != null) {
			listener.onResult(true, enabled ? enabledMessage : disabledMessage);
		}
	}

	private void prepareChristmasTransition(final boolean previous, final boolean enabled,
			final ResultListener listener) {
		try {
			final ChristmasEventPlan target = ChristmasEventPlan.prepare(enabled);
			final ChristmasEventPlan rollback = ChristmasEventPlan.prepare(previous);
			TurnNotifier.get().notifyInTurns(0, new TurnListener() {
				@Override
				public void onTurnReached(final int currentTurn) {
					applyChristmasTransition(previous, target, rollback, listener);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Nie udało się przygotować eventu Christmas", e);
			notifyFailure(listener, "Nie udało się przygotować eventu Christmas: "
					+ readableMessage(e));
		}
	}

	private void prepareMineTownTransition(final boolean previous, final boolean enabled,
			final ResultListener listener) {
		try {
			final MineTownEventPlan target = MineTownEventPlan.prepare(enabled);
			final MineTownEventPlan rollback = MineTownEventPlan.prepare(previous);
			TurnNotifier.get().notifyInTurns(0, new TurnListener() {
				@Override
				public void onTurnReached(final int currentTurn) {
					applyMineTownTransition(previous, target, rollback, listener);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Nie udało się przygotować eventu Mine Town", e);
			notifyFailure(listener, "Nie udało się przygotować eventu Mine Town: "
					+ readableMessage(e));
		}
	}

	private void applyChristmasTransition(final boolean previous,
			final ChristmasEventPlan target,
			final ChristmasEventPlan rollback,
			final ResultListener listener) {
		String stage = "ustawienie flagi stendhal.christmas";
		try {
			setSeasonalProperty(ChristmasEventPlan.PROPERTY, target.isEnabled());
			stage = "zastosowanie przygotowanych zasobów świata";
			target.apply();
			stage = "synchronizacja questa Rudolfa";
			synchronizeRudolphQuest(target.isEnabled());
			stage = "odświeżenie NPC Mariuszek";
			refreshMariuszek();
			if (listener != null) {
				listener.onResult(true, target.isEnabled()
						? "Event Christmas został aktywowany bez restartu serwera."
						: "Event Christmas został wyłączony bez restartu serwera.");
			}
		} catch (final Exception e) {
			LOGGER.error("Nie udało się zastosować eventu Christmas na etapie: " + stage
					+ "; przywracam poprzedni stan", e);
			String rollbackFailure = null;
			String rollbackStage = "przywrócenie flagi stendhal.christmas";
			try {
				setSeasonalProperty(ChristmasEventPlan.PROPERTY, previous);
				rollbackStage = "przywrócenie zasobów świata";
				rollback.apply();
				rollbackStage = "przywrócenie questa Rudolfa";
				synchronizeRudolphQuest(previous);
				rollbackStage = "przywrócenie NPC Mariuszek";
				refreshMariuszek();
			} catch (final Exception rollbackException) {
				LOGGER.error("Nie udało się w pełni przywrócić poprzedniego stanu Christmas na etapie: "
						+ rollbackStage, rollbackException);
				rollbackFailure = "; dodatkowo rollback na etapie '" + rollbackStage
						+ "' zgłosił: " + readableMessage(rollbackException);
			}
			if (listener != null) {
				listener.onResult(false, "Nie udało się przełączyć eventu Christmas na etapie '"
						+ stage + "': " + readableMessage(e)
						+ (rollbackFailure == null ? "" : rollbackFailure));
			}
		} finally {
			transitionInProgress.set(false);
		}
	}

	private void applyMineTownTransition(final boolean previous,
			final MineTownEventPlan target,
			final MineTownEventPlan rollback,
			final ResultListener listener) {
		String stage = "ustawienie flagi stendhal.minetown";
		try {
			setSeasonalProperty(MineTownEventPlan.PROPERTY, target.isEnabled());
			if (!target.isEnabled()) {
				stage = "odłączenie Mine Town Revival Weeks";
				synchronizeMineTownQuest(false);
			}
			stage = "zastosowanie przygotowanych zasobów Mine Town";
			target.apply();
			if (target.isEnabled()) {
				stage = "uruchomienie Mine Town Revival Weeks";
				synchronizeMineTownQuest(true);
			}
			if (listener != null) {
				listener.onResult(true, target.isEnabled()
						? "Event Mine Town Revival Weeks został aktywowany bez restartu serwera."
						: "Event Mine Town Revival Weeks został wyłączony bez restartu serwera.");
			}
		} catch (final Exception e) {
			LOGGER.error("Nie udało się zastosować eventu Mine Town na etapie: " + stage
					+ "; przywracam poprzedni stan", e);
			String rollbackFailure = null;
			String rollbackStage = "przywrócenie flagi stendhal.minetown";
			try {
				setSeasonalProperty(MineTownEventPlan.PROPERTY, previous);
				if (!previous) {
					rollbackStage = "usunięcie częściowo uruchomionego questa Mine Town";
					synchronizeMineTownQuest(false);
				}
				rollbackStage = "przywrócenie zasobów Mine Town";
				rollback.apply();
				if (previous) {
					rollbackStage = "przywrócenie questa Mine Town";
					synchronizeMineTownQuest(true);
				}
			} catch (final Exception rollbackException) {
				LOGGER.error("Nie udało się w pełni przywrócić poprzedniego stanu Mine Town na etapie: "
						+ rollbackStage, rollbackException);
				rollbackFailure = "; dodatkowo rollback na etapie '" + rollbackStage
						+ "' zgłosił: " + readableMessage(rollbackException);
			}
			if (listener != null) {
				listener.onResult(false, "Nie udało się przełączyć eventu Mine Town na etapie '"
						+ stage + "': " + readableMessage(e)
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

	/**
	 * Callback for an asynchronous event transition result.
	 */
	public interface ResultListener {
		/**
		 * @param success whether the requested transition succeeded
		 * @param message human readable result
		 */
		void onResult(boolean success, String message);
	}
}
