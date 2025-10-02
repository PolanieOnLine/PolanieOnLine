package games.stendhal.server.script;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * This script allows administrators to heal players around them or across the entire zone.
 * Usage:
 *   /script MassHeal.class			 → heal players within default radius
 *   /script MassHeal.class <radius>	→ heal players within given radius
 *   /script MassHeal.class all		 → heal all players in the same zone
 *
 * Features:
 * - Heals only non-ghost players.
 * - "all" parameter heals every player in the same zone as the admin.
 * - If radius is invalid, default radius will be used.
 */
public class MassHeal extends ScriptImpl {
	private static Logger logger = Logger.getLogger(MassHeal.class);
	private final int DEFAULT_RADIUS = 10;

	/**
	 * Executes the MassHeal command.
	 * 
	 * @param admin  The player (administrator) executing the script
	 * @param args   Command arguments (none, radius, or "all")
	 */
	@Override
	public void execute(final Player admin, final List<String> args) {
		int radius = DEFAULT_RADIUS;
		boolean healAll = false;

		// usage check
		if (args.size() == 1 && "help".equalsIgnoreCase(args.get(0))) {
			admin.sendPrivateText(
				"Użycie: /script MassHeal.class [promień | all]\n" +
				"  → bez argumentu: leczy graczy w domyślnym promieniu " + DEFAULT_RADIUS + "\n" +
				"  → [promień]: leczy graczy w podanym promieniu\n" +
				"  → all: leczy wszystkich graczy w tej samej strefie"
			);
			return;
		}

		// parse argument
		if (args.size() == 1) {
			String arg = args.get(0);
			if ("all".equalsIgnoreCase(arg)) {
				healAll = true;
			} else {
				try {
					radius = Integer.parseInt(arg);
					if (radius < 1) {
						admin.sendPrivateText("Promień musi być dodatni. Użyto wartości domyślnej " + DEFAULT_RADIUS + ".");
						radius = DEFAULT_RADIUS;
					}
				} catch (NumberFormatException e) {
					admin.sendPrivateText("Promień musi być liczbą lub słowem 'all'.");
					return;
				}
			}
		}

		// current zone
		final StendhalRPZone myZone = sandbox.getZone(admin);
		sandbox.setZone(myZone);

		List<Player> healedPlayers = new ArrayList<>();

		// iterate players in zone
		for (Player p : myZone.getPlayers()) {
			if (p == null || p.isGhost()) continue;

			p.sendPrivateText(NotificationType.POSITIVE, "#'✨' Boska moc uleczyła Twoje rany! #'✨'");
			if (healAll) {
				p.setHP(p.getBaseHP());
				healedPlayers.add(p);
			} else {
				int dx = Math.abs(p.getX() - admin.getX());
				int dy = Math.abs(p.getY() - admin.getY());

				if (dx <= radius && dy <= radius) {
					p.setHP(p.getBaseHP());
					healedPlayers.add(p);
				}
			}
		}

		// summary to admin
		if (healAll) {
			admin.sendPrivateText("MassHeal: Uleczono #" + healedPlayers.size() + " graczy w strefie #'" + myZone.getName() + "'.");
			logger.info("MassHeal executed by " + admin.getName() + 
						" | healed=" + healedPlayers.size() + 
						" | mode=ALL | zone=" + myZone.getName());
		} else {
			admin.sendPrivateText("MassHeal: Uleczono #" + healedPlayers.size() + " graczy w promieniu #" + radius + " od ciebie.");
			logger.info("MassHeal executed by " + admin.getName() + 
						" | healed=" + healedPlayers.size() + 
						" | radius=" + radius);
		}
	}
}