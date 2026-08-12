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
			stage = "ponowne podpięcie dialogu Easter do Caroline";
			reattachEasterQuestToCurrentCaroline();
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
				rollbackStage = "ponowne podpięcie dialogu Easter do Caroline";
				reattachEasterQuestToCurrentCaroline();
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

	private void applyEasterTransition(final boolean previous,
			final EasterEventPlan target,
			final EasterEventPlan rollback,
			final ResultListener listener) {
		String stage = "ustawienie flagi stendhal.easter";
		try {
			setSeasonalProperty(EasterEventPlan.PROPERTY, target.isEnabled());
			stage = "zastosowanie przygotowanych zasobów Easter";
			target.apply();
			if (listener != null) {
				listener.onResult(true, target.isEnabled()
						? "Event Easter został aktywowany bez restartu serwera."
						: "Event Easter został wyłączony bez restartu serwera.");
			}
		} catch (final Exception e) {
			LOGGER.error("Nie udało się zastosować eventu Easter na etapie: " + stage
					+ "; przywracam poprzedni stan", e);
			String rollbackFailure = null;
			String rollbackStage = "przywrócenie flagi stendhal.easter";
			try {
				setSeasonalProperty(EasterEventPlan.PROPERTY, previous);
				rollbackStage = "przywrócenie zasobów Easter";
				rollback.apply();
			} catch (final Exception rollbackException) {
				LOGGER.error("Nie udało się w pełni przywrócić poprzedniego stanu Easter na etapie: "
						+ rollbackStage, rollbackException);
				rollbackFailure = "; dodatkowo rollback na etapie '" + rollbackStage
						+ "' zgłosił: " + readableMessage(rollbackException);
			}
			if (listener != null) {
				listener.onResult(false, "Nie udało się przełączyć eventu Easter na etapie '"
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

	public interface ResultListener {
		void onResult(boolean success, String message);
	}
}
