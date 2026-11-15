/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.compass;

import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.navigation.NavigationGoal;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.npc.NPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.compass.CompassProgress.Stage;

/** One private, walking companion, retained across ordinary map boundaries. */
public final class CompassEscort extends NPC {
	static final String[] ZONES = { "0_koscielisko_e", "0_zakopane_w", "0_zakopane_c", "0_zakopane_s" };
	static final int[][] STOPS = { { 122, 23 }, { 105, 118 }, { 26, 112 }, { 38, 48 } };
	private static final String VERSION = "escort-v1";
	private static final Map<Player, CompassEscort> ACTIVE = new WeakHashMap<>();
	private final Player owner;
	private int turns;
	private boolean separationWarned;
	private StendhalRPZone exitedZone;
	private int exitedX, exitedY;

	private CompassEscort(final Player player, final int x, final int y) {
		owner = player;
		PlayerPrivateScene.assign(this, player);
		setName("Dobromir pod opieką");
		setEntityClass("woodcutternpc");
		setResistance(0);
		setBaseSpeed(0.35);
		setPosition(x, y);
		setDescription("Ranny Dobromir idzie powoli pod opieką " + player.getName()
				+ ". Czeka, kiedy przewodnik walczy lub zbyt daleko odchodzi.");
	}

	private static boolean active(final Player player) {
		return CompassProgress.stage(player) == Stage.ESCORT;
	}

	private static int checkpoint(final Player player) {
		return Math.max(0, Math.min(3, CompassProgress.detail(player)));
	}

	private static int index(final String name) {
		for (int i = 0; i < ZONES.length; i++) if (ZONES[i].equals(name)) return i;
		return -1;
	}

	/** Returns the actual companion even when its owner has left it on another map. */
	public static CompassEscort find(final Player player) {
		return ACTIVE.get(player);
	}

	private static String[] saved(final Player player) {
		String[] fields = player.getQuest(CompassProgress.SLOT).split(";");
		if (fields.length == 7 && VERSION.equals(fields[3])) {
			try {
				int zone = index(fields[4]);
				int x = Integer.parseInt(fields[5]), y = Integer.parseInt(fields[6]);
				StendhalRPZone loaded = SingletonRepository.getRPWorld().getZone(fields[4]);
				if (zone >= 0 && zone <= checkpoint(player) && loaded != null
						&& x >= 0 && y >= 0 && x < loaded.getWidth() && y < loaded.getHeight()
						&& !loaded.collides(x, y))
					return new String[] { fields[4], fields[5], fields[6] };
			} catch (NumberFormatException ignored) {
				// Legacy or damaged saves resume at the last completed safe stop.
			}
		}
		int step = checkpoint(player);
		return step == 0 ? new String[] { ZONES[0], "68", "86" }
				: new String[] { ZONES[step - 1], Integer.toString(STOPS[step - 1][0]),
						Integer.toString(STOPS[step - 1][1]) };
	}

	/** Called after private props refresh; it never summons the companion to its owner. */
	public static void refresh(final Player player) {
		if (!active(player) || player.isDisconnected() || player.getHP() <= 0) {
			cleanup(player);
			return;
		}
		StendhalRPZone zone = player.getZone();
		if (zone == null) return;
		CompassEscort escort = find(player);
		if (escort != null && escort.getZone() != null) {
			if (escort.getZone() != zone) escort.crossBoundary(zone);
			escort.checkSeparation();
			return;
		}
		String[] position = saved(player);
		if (!zone.getName().equals(position[0])) return;
		int x = Integer.parseInt(position[1]), y = Integer.parseInt(position[2]);
		if (x >= zone.getWidth() || y >= zone.getHeight() || zone.collides(x, y)) return;
		escort = new CompassEscort(player, x, y);
		ACTIVE.put(player, escort);
		zone.add(escort);
		escort.save();
		escort.checkSeparation();
	}

	/** Capture the departure before the engine changes the player's zone/coordinates. */
	public static void exited(final Player player, final StendhalRPZone zone) {
		CompassEscort escort = find(player);
		if (escort == null) return;
		escort.save();
		escort.clearPath();
		escort.stop();
		// Only the departure from the companion's own map can authorize a crossing.
		// Visiting another map invalidates a previously uncompleted departure.
		escort.exitedZone = zone == escort.getZone() ? zone : null;
		if (escort.exitedZone != null) {
			escort.exitedX = zone.getX() + player.getX();
			escort.exitedY = zone.getY() + player.getY();
		}
		if (player.isDisconnected() || player.getHP() <= 0) cleanup(player);
	}

	/** Persist before removal; a later login reconstructs only at this position. */
	public static void cleanup(final Player player) {
		CompassEscort escort = ACTIVE.remove(player);
		if (escort == null) return;
		escort.save();
		escort.clearPath();
		escort.stop();
		StendhalRPZone zone = escort.getZone();
		if (zone != null && zone.has(escort.getID())) zone.remove(escort);
	}

	private static int distance(final int x1, final int y1, final int x2, final int y2) {
		return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
	}

	private boolean fighting() {
		return owner.getAttackTarget() != null || !owner.getAttackingRPEntities().isEmpty();
	}

	private void crossBoundary(final StendhalRPZone destination) {
		StendhalRPZone origin = getZone();
		if (origin == null || exitedZone != origin || fighting()) return;
		int from = index(origin.getName()), to = index(destination.getName());
		if (from < 0 || to < 0 || Math.abs(from - to) != 1 || to > checkpoint(owner)
				|| origin.isInterior() || destination.isInterior() || origin.getLevel() != destination.getLevel()) return;
		boolean adjacent = (origin.getX() + origin.getWidth() == destination.getX()
				|| destination.getX() + destination.getWidth() == origin.getX()) && origin.getY() == destination.getY()
				|| (origin.getY() + origin.getHeight() == destination.getY()
				|| destination.getY() + destination.getHeight() == origin.getY()) && origin.getX() == destination.getX();
		if (!adjacent) return;
		int playerX = destination.getX() + owner.getX(), playerY = destination.getY() + owner.getY();
		int worldX = origin.getX() + getX(), worldY = origin.getY() + getY();
		if (distance(exitedX, exitedY, playerX, playerY) > 2 || distance(worldX, worldY, playerX, playerY) > 6) return;
		int x = Math.max(0, Math.min(destination.getWidth() - 1, worldX - destination.getX()));
		int y = Math.max(0, Math.min(destination.getHeight() - 1, worldY - destination.getY()));
		// Project only across the shared edge; never search for a shortcut around a wall.
		if (destination.collides(x, y) || distance(worldX, worldY, destination.getX() + x, destination.getY() + y) > 6) return;
		origin.remove(this);
		setPosition(x, y);
		destination.add(this);
		exitedZone = null;
		save();
	}

	private void save() {
		if (!active(owner) || getZone() == null) return;
		String value = Stage.ESCORT.name() + ";" + CompassProgress.detail(owner) + ";"
				+ CompassProgress.time(owner) + ";" + VERSION + ";" + getZone().getName() + ";" + getX() + ";" + getY();
		if (!value.equals(owner.getQuest(CompassProgress.SLOT))) owner.setQuest(CompassProgress.SLOT, value);
	}

	/** Stops are sequential and require both the owner and their own companion. */
	public static boolean arrive(final Player player, final Entity stop, final int step) {
		if (!active(player) || step < 0 || step > 3 || CompassProgress.detail(player) != step
				|| player.getHP() <= 0 || player.isDisconnected() || stop == null
				|| !PlayerPrivateScene.isOwnedBy(stop, player) || stop.getZone() == null
				|| stop.getZone() != player.getZone() || !ZONES[step].equals(stop.getZone().getName())
				|| stop.getX() != STOPS[step][0] || stop.getY() != STOPS[step][1] || !player.nextTo(stop)) return false;
		CompassEscort escort = find(player);
		if (escort == null || escort.getZone() != stop.getZone() || !escort.nextTo(stop, 3) || escort.fighting()) {
			player.sendPrivateText("Dobromir jeszcze nie doszedł. Zaczekaj tu na niego i osłoń go przed niebezpieczeństwem.");
			return false;
		}
		if (step == 3) {
			player.sendPrivateText("Dobromir: Przy Sabale jestem już bezpieczny. Na północnym skraju polany we wschodnim Kościelisku, tuż na wschód od rozłożystego drzewa, znalazłem stare ludzkie szczątki i rozbity kompas. Zajrzyj tam, proszę. Może ten człowiek też poszedł za światłami...");
			CompassProgress.next(player, Stage.ESCORT);
			cleanup(player);
		} else {
			CompassProgress.set(player, Stage.ESCORT, step + 1, CompassProgress.time(player) + 1);
			escort.save();
			player.sendPrivateText("Dobromir odpoczął. " + objective(player));
		}
		return true;
	}

	public static NavigationGoal target(final Player player) {
		if (!active(player)) return null;
		CompassEscort escort = find(player);
		String[] position = saved(player);
		String zone = escort == null || escort.getZone() == null ? position[0] : escort.getZone().getName();
		int x = escort == null ? Integer.parseInt(position[1]) : escort.getX();
		int y = escort == null ? Integer.parseInt(position[2]) : escort.getY();
		if (player.getZone() == null || !zone.equals(player.getZone().getName())
				|| distance(player.getX(), player.getY(), x, y) > 10)
			return new NavigationGoal(zone, x, y, "Wróć po Dobromira", "ESCORT", "dobromir");
		int step = checkpoint(player);
		return new NavigationGoal(ZONES[step], STOPS[step][0], STOPS[step][1], objective(player), "ESCORT;" + step);
	}

	public static String objective(final Player player) {
		String waiting = waitingObjective(player);
		if (waiting != null) return waiting;
		switch (checkpoint(player)) {
		case 0: return "Odprowadź Dobromira do postoju przy wschodnim wyjściu z lasu w Kościelisku. Idź powoli i zaczekaj na niego.";
		case 1: return "Przejdźcie razem na wschód do zachodniej części Zakopanego. Odpocznijcie przy drewnie obok drogi na południowym wschodzie.";
		case 2: return "Idźcie razem dalej na wschód, przez centrum Zakopanego. Doprowadź Dobromira do osłoniętego postoju przy drodze na południu.";
		default: return "Odprowadź Dobromira na południe, do Sabały w południowej części Zakopanego. Zaczekaj na niego przy bezpiecznym postoju.";
		}
	}

	/** The journal remains useful before this quest unlocks the compass display. */
	private static String waitingObjective(final Player player) {
		if (!active(player)) return null;
		CompassEscort escort = find(player);
		String zone;
		int x, y;
		if (escort != null && escort.getZone() != null) {
			zone = escort.getZone().getName();
			x = escort.getX();
			y = escort.getY();
		} else {
			String[] position = saved(player);
			zone = position[0];
			x = Integer.parseInt(position[1]);
			y = Integer.parseInt(position[2]);
		}
		if (player.getZone() != null && zone.equals(player.getZone().getName())
				&& distance(player.getX(), player.getY(), x, y) <= 10) return null;
		String place;
		switch (index(zone)) {
		case 1: place = "w zachodniej części Zakopanego"; break;
		case 2: place = "w centrum Zakopanego"; break;
		case 3: place = "w południowej części Zakopanego"; break;
		default: place = "we wschodnim lesie Kościeliska"; break;
		}
		return "Dobromir czeka " + place + ". Wróć po niego i idź wolniej, czekając, aż nadąży.";
	}

	private void checkSeparation() {
		if (owner.getZone() == getZone() && distance(owner.getX(), owner.getY(), getX(), getY()) <= 10) {
			separationWarned = false;
		} else if (!separationWarned) {
			owner.sendPrivateText(waitingObjective(owner));
			separationWarned = true;
		}
	}

	@Override
	public void logic() {
		if (getZone() == null) return;
		if (!active(owner) || owner.isDisconnected() || owner.getHP() <= 0) {
			cleanup(owner);
			return;
		}
		checkSeparation();
		if (owner.getZone() != getZone() || !getZone().has(owner.getID()) || fighting() || !nextTo(owner, 10)) {
			clearPath();
			stop();
			return;
		}
		if (turns++ % 4 == 0) setMovement(owner, 1, 3, 10);
		super.logic();
		if (turns % 20 == 0) save();
	}
}
