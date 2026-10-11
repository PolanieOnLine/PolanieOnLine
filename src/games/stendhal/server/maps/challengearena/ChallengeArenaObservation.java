package games.stendhal.server.maps.challengearena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
import games.stendhal.common.constants.Actions;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ChatOptionsEvent;
import games.stendhal.server.events.ViewChangeEvent;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.server.game.rp.InstanceScope;

/** Read-only spectators stay in the private map's separate, empty lobby. */
public final class ChallengeArenaObservation implements TurnListener {
	private static final Logger logger = Logger.getLogger(ChallengeArenaObservation.class);
	private static final ChallengeArenaObservation monitor = new ChallengeArenaObservation();
	private static final Map<Player, Long> selections = new ConcurrentHashMap<>();
	private static final Map<Player, Visit> viewers = new ConcurrentHashMap<>();
	private static boolean scheduled;
	private static final long SELECTION_TIMEOUT = 120000L;
	private static final int VIEWER_X = 32;
	private static final int VIEWER_Y = 47;

	private static final class Visit {
		private final String owner;
		private final ChallengeArenaInfo info;
		private final StendhalRPZone lobby;
		private final int x;
		private final int y;
		private final Direction direction;
		// Repeat after the map transfer, so neither client's zone initialization
		// can discard the initial remote-camera request.
		private int cameraRequests = 3;
		private boolean returning;

		private Visit(final String owner, final ChallengeArenaInfo info,
				final StendhalRPZone lobby, final int x, final int y, final Direction direction) {
			this.owner = owner;
			this.info = info;
			this.lobby = lobby;
			this.x = x;
			this.y = y;
			this.direction = direction;
		}
	}

	private ChallengeArenaObservation() { }

	public static boolean open(final Player player) {
		final ChallengeArenaInfo shared = ChallengeArenaManager.getArenaInfo();
		if (shared == null || player.getZone() != shared.getZone() || !nearOrb(player)) {
			return false;
		}
		selections.put(player, System.currentTimeMillis() + SELECTION_TIMEOUT);
		showChoices(player, "open");
		return true;
	}

	private static boolean nearOrb(final Player player) {
		for (final games.stendhal.server.entity.Entity entity :
				player.getZone().getEntitiesAt(ChallengeArenaZone.LEFT_VIEW_ORB_X,
						ChallengeArenaZone.VIEW_ORB_Y)) {
			if (entity instanceof ChallengeArenaObservationOrb && entity.nextTo(player)) {
				return true;
			}
		}
		for (final games.stendhal.server.entity.Entity entity :
				player.getZone().getEntitiesAt(ChallengeArenaZone.RIGHT_VIEW_ORB_X,
						ChallengeArenaZone.VIEW_ORB_Y)) {
			if (entity instanceof ChallengeArenaObservationOrb && entity.nextTo(player)) {
				return true;
			}
		}
		return false;
	}

	private static void showChoices(final Player player, final String phase) {
		final List<String> names = ChallengeArenaManager.getObservablePlayers();
		final Visit visit = viewers.get(player);
		final String title = visit == null ? "Wybierz gracza do obserwacji"
				: "Obserwujesz: " + visit.owner;
		if (names.isEmpty() && visit == null) {
			selections.remove(player);
			closeWindow(player);
			player.sendPrivateText("Nikt teraz nie walczy na arenie. Spróbuj ponownie za chwilę.");
			return;
		}
		player.addEvent(ChatOptionsEvent.arenaObservation(phase, title, names));
		if (visit == null) {
			player.sendPrivateText("Na arenie walczą: " + String.join(", ", names)
					+ ". Wybierz gracza w oknie obserwacji albo wpisz: obserwuj " + names.get(0) + ".");
		}
		player.notifyWorldAboutChanges();
	}

	/** Runs before ALL client commands, including forged inventory/admin actions. */
	public static boolean handleAction(final Player player, final RPAction action) {
		// Desktop clients encode known commands using RPClass instead of a type attribute.
		final String encodedType = action.getRPClass().getName();
		final String type = !encodedType.isEmpty() ? encodedType
				: action.has(Actions.TYPE) ? action.get(Actions.TYPE) : "";
		if (Actions.ARENA_OBSERVATION.equals(type)) {
			final String operation = action.has("operation") ? action.get("operation") : "";
			if ("close".equals(operation) || "return".equals(operation)) {
				if (viewers.containsKey(player) || selections.containsKey(player)) { stopWatching(player); }
			} else if ("list".equals(operation)) {
				if (canChoose(player)) { showChoices(player, viewers.containsKey(player) ? "watch" : "open"); }
			} else if ("watch".equals(operation) && action.has("target")) {
				watch(player, action.get("target"));
			}
			// UI controls never pass through the public-chat or general action handlers.
			return true;
		}
		if (Actions.CHAT.equals(type) && action.has(Actions.TEXT)) {
			final String text = action.get(Actions.TEXT).trim();
			if (text.toLowerCase(Locale.ROOT).startsWith("obserwuj ")
					&& (viewers.containsKey(player) || selections.containsKey(player))) {
				final String choice = text.substring("obserwuj ".length()).trim();
				if ("powrot".equalsIgnoreCase(choice) || "powrót".equalsIgnoreCase(choice)
						|| "zamknij".equalsIgnoreCase(choice)) {
					stopWatching(player);
				} else if ("lista".equalsIgnoreCase(choice)) {
					if (canChoose(player)) {
						showChoices(player, viewers.containsKey(player) ? "watch" : "open");
					}
				} else {
					watch(player, choice);
				}
				return true;
			}
		}
		if (!viewers.containsKey(player)) {
			return false;
		}
		if (Actions.MOVE.equals(type) || Actions.MOVETO.equals(type) || Actions.MOVE_CONTINUOUS.equals(type)
				|| "autowalk".equals(type)) {
			stopWatching(player); // consume movement: never move on the fighter's map
			return true;
		}
		if (isReadOnlyAction(type)) {
			return false;
		}
		player.sendPrivateText("Tylko oglądasz walkę. Aby wrócić do lobby, naciśnij kierunek ruchu"
				+ " lub wybierz Powrót do lobby w oknie obserwacji.");
		return true;
	}

	static boolean isReadOnlyAction(final String type) {
		// Deliberately deny everything else, including future command types.
		return "look".equals(type) || "info".equals(type) || "who".equals(type)
				|| "where".equals(type) || "tell".equals(type) || "answer".equals(type)
				|| "group_message".equals(type) || "support".equals(type)
				|| "report_error".equals(type) || "listquests".equals(type);
	}

	private static boolean canChoose(final Player player) {
		final Visit visit = viewers.get(player);
		if (visit != null) {
			return !visit.returning && visit.info.isInZone(player);
		}
		final Long expires = selections.get(player);
		final ChallengeArenaInfo shared = ChallengeArenaManager.getArenaInfo();
		if (expires != null && expires >= System.currentTimeMillis() && shared != null
				&& player.getZone() == shared.getZone() && nearOrb(player)) {
			return true;
		}
		selections.remove(player);
		closeWindow(player);
		player.sendPrivateText("Podejdź do kuli i ponownie wybierz gracza.");
		return false;
	}

	static boolean watch(final Player player, final String selectedName) {
		if (!canChoose(player) || player.isDisconnected() || player.getHP() <= 0
				|| ChallengeArenaManager.isReservedBy(player.getName()) || player.isAttacking()) {
			return false;
		}
		String owner = null;
		for (final String name : ChallengeArenaManager.getObservablePlayers()) {
			if (name.equalsIgnoreCase(selectedName)) { owner = name; break; }
		}
		if (owner == null) {
			player.sendPrivateText("Ten gracz już nie walczy. Wybierz inną walkę.");
			showChoices(player, viewers.containsKey(player) ? "watch" : "open");
			return false;
		}
		final ChallengeArenaInfo info = ChallengeArenaManager.getArenaInfo(owner);
		final Visit previous = viewers.get(player);
		if (previous != null && previous.info == info) {
			previous.cameraRequests = 3;
			showChoices(player, "watch");
			return true;
		}
		final Visit visit = previous == null
				? new Visit(owner, info, player.getZone(), player.getX(), player.getY(), player.getDirection())
				: new Visit(owner, info, previous.lobby, previous.x, previous.y, previous.direction);
		try {
			// Keep the instance alive if its fighter disconnects while spectators
			// are waiting for a temporarily blocked return to the permanent lobby.
			final ChallengeArenaInfo shared = ChallengeArenaManager.getArenaInfo();
			final String selectedOwner = owner;
			if (SingletonRepository.getRPWorld().getInstanceZoneManager().acquire(
					shared.getZone().getName(), "challenge_arena", InstanceScope.player(selectedOwner),
					player.getName(), descriptor -> {
						throw new IllegalStateException("Observation cannot create a new arena");
					}) != info.getZone()) {
				throw new IllegalStateException("Arena changed while selecting a fight");
			}
		} catch (final Exception e) {
			logger.warn("Unable to acquire arena observation for " + player.getName(), e);
			player.sendPrivateText("Nie udało się otworzyć podglądu. Spróbuj ponownie.");
			return false;
		}
		viewers.put(player, visit);
		player.setArenaSpectator(true);
		player.forceStop();
		player.stopAttack();
		player.clearPath();
		if (player.has(Actions.MOVE_CONTINUOUS)) { player.remove(Actions.MOVE_CONTINUOUS); }
		if (!player.teleport(info.getZone(), VIEWER_X, VIEWER_Y, Direction.UP, null)) {
			if (previous == null) { viewers.remove(player); player.setArenaSpectator(false); }
			else { viewers.put(player, previous); }
			releaseMembership(player, visit);
			return false;
		}
		selections.remove(player);
		if (previous != null) { releaseMembership(player, previous); }
		requestCamera(player, visit);
		showChoices(player, "watch");
		player.sendPrivateText("Obserwujesz walkę gracza " + owner
				+ ". Możesz wybrać inną walkę albo wrócić do lobby. Nie bierzesz udziału w walce.");
		schedule();
		return true;
	}

	public static boolean isWatchingZone(final Player player, final StendhalRPZone zone) {
		final Visit visit = viewers.get(player);
		return visit != null && visit.info.getZone() == zone;
	}

	/** No teleport, including a forged or indirect request, may enter the ring. */
	public static boolean mayTeleport(final Player player, final StendhalRPZone zone,
			final int x, final int y) {
		final Visit visit = viewers.get(player);
		return visit != null && (zone == visit.lobby || (zone == visit.info.getZone()
				&& !visit.info.getArena().getShape().contains(x + 0.5, y + 0.5)));
	}

	private static void requestCamera(final Player player, final Visit visit) {
		player.addEvent(new ViewChangeEvent(ChallengeArenaZone.COMBAT_X, ChallengeArenaZone.COMBAT_Y));
		player.notifyWorldAboutChanges();
		visit.cameraRequests--;
	}

	private static void closeWindow(final Player player) {
		player.addEvent(ChatOptionsEvent.arenaObservation("close", "", Collections.emptyList()));
		player.notifyWorldAboutChanges();
	}

	static boolean stopWatching(final Player player) {
		selections.remove(player);
		final Visit visit = viewers.get(player);
		if (visit == null) { closeWindow(player); return true; }
		visit.returning = true;
		if (!player.isDisconnected() && visit.info.isInZone(player)
				&& !player.teleport(visit.lobby, visit.x, visit.y, visit.direction, null)) {
			// Do not clear protection or membership until the move succeeds.
			schedule();
			return false;
		}
		viewers.remove(player);
		player.setArenaSpectator(false);
		if (player.isDisconnected()) { saveLobbyPosition(player, visit); }
		else {
			closeWindow(player);
			player.sendPrivateText("Kończysz obserwację i wracasz do lobby areny.");
		}
		releaseMembership(player, visit);
		return true;
	}

	static boolean returnViewers(final ChallengeArenaInfo info) {
		boolean returned = true;
		for (final Map.Entry<Player, Visit> entry : new ArrayList<>(viewers.entrySet())) {
			if (entry.getValue().info == info) {
				returned &= stopWatching(entry.getKey());
			}
		}
		return returned;
	}

	/** Called after removal but before the disconnected object can be saved. */
	public static void onLogout(final Player player) {
		selections.remove(player);
		final Visit visit = viewers.remove(player);
		player.setArenaSpectator(false);
		if (visit != null) { saveLobbyPosition(player, visit); releaseMembership(player, visit); }
	}

	/** Periodic saves also restore a permanent location after a server crash. */
	public static RPObject persistenceSnapshot(final RPObject object) {
		final Visit visit = object instanceof Player ? viewers.get((Player) object) : null;
		if (visit == null) { return object; }
		final RPObject snapshot = new RPObject(object);
		snapshot.put("zoneid", visit.lobby.getName());
		snapshot.put("x", visit.x);
		snapshot.put("y", visit.y);
		return snapshot;
	}

	private static void saveLobbyPosition(final Player player, final Visit visit) {
		player.put("zoneid", visit.lobby.getName());
		player.put("x", visit.x);
		player.put("y", visit.y);
	}

	private static void releaseMembership(final Player player, final Visit visit) {
		try {
			SingletonRepository.getRPWorld().getInstanceZoneManager().release(
					visit.info.getZone().getID(), player.getName());
		} catch (final Exception e) {
			logger.error("Unable to release arena observation for " + player.getName(), e);
		}
	}

	private static void schedule() {
		if (!scheduled && !viewers.isEmpty()) {
			scheduled = true;
			SingletonRepository.getTurnNotifier().notifyInTurns(1, monitor);
		}
	}

	@Override public void onTurnReached(final int turn) {
		scheduled = false;
		for (final Map.Entry<Player, Visit> entry : new ArrayList<>(viewers.entrySet())) {
			final Player player = entry.getKey();
			final Visit visit = entry.getValue();
			if (visit.returning || player.isDisconnected() || !visit.info.isInZone(player)
					|| !ChallengeArenaManager.isObservable(visit.owner, visit.info)) {
				stopWatching(player);
			} else if (visit.cameraRequests > 0) {
				requestCamera(player, visit);
			}
		}
		schedule();
	}

	static void clearForTests() {
		SingletonRepository.getTurnNotifier().dontNotify(monitor);
		scheduled = false;
		selections.clear();
		for (final Player player : viewers.keySet()) { player.setArenaSpectator(false); }
		viewers.clear();
	}
}
