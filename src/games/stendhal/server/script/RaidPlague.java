package games.stendhal.server.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.RaidCreature;
import games.stendhal.server.entity.player.Player;

/**
 * This script allows an administrator to spawn a plague of creatures around their position,
 * filtered by level and excluding special creature profiles (rare, abnormal, immortal).
 *
 * Usage (4 variants):
 *  1. /script RaidPlague.class <level>
 *	 → Spawns a random amount (10-20) of creatures with level near <level>.
 *	   Tolerance = 10 by default.
 *
 *  2. /script RaidPlague.class <level> <X>
 *	 → If X ≤ MAX_AMOUNT → X is treated as the amount, tolerance = 10
 *	   If X > MAX_AMOUNT → X is treated as the tolerance, amount = random (10-20)
 *
 *  3. /script RaidPlague.class <level> <amount> <tolerance>
 *	 → Both amount and tolerance are explicitly defined.
 *
 * Constraints:
 *  - Maximum number of spawned creatures = MAX_AMOUNT
 *  - Default tolerance = DEFAULT_TOLERANCE (10)
 *  - Rare, abnormal, or immortal creatures are ignored
 */
public class RaidPlague extends ScriptImpl {
	private static Logger logger = Logger.getLogger(RaidPlague.class);
	/** Maximum spawn radius around the admin */
	private final int RADIUS = 5;
	/** Maximum number of creatures allowed per raid */
	private final int MAX_AMOUNT = 100;
	/** Default tolerance for level matching */
	private final int DEFAULT_TOLERANCE = 10;

	/**
	 * Executes the RaidPlague script.
	 *
	 * @param admin The player (admin) executing the script.
	 * @param args  Arguments passed to the script:
	 *              <level> [amount] [tolerance]
	 *
	 * The method interprets the arguments as follows:
	 *   - Only <level> → amount = 10–20, tolerance = 10
	 *   - <level> <X>  → If X ≤ MAX_AMOUNT, amount = X, tolerance = 10
	 *                  → If X > MAX_AMOUNT, tolerance = X, amount = 10–20
	 *   - <level> <amount> <tolerance> → both explicitly given
	 */
	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args.size() < 1 || args.size() > 3 || (args.size() == 1 && "help".equalsIgnoreCase(args.get(0)))) {
			admin.sendPrivateText(
					"Użycie RaidPlague:\n" +
					"  /script RaidPlague.class <poziom>\n" +
					"	 → ilość losowa 10-20, tolerancja = 10\n\n" +
					"  /script RaidPlague.class <poziom> [X]\n" +
					"	 → jeśli X ≤ " + MAX_AMOUNT + " → ilość potworów, tolerancja = 10\n" +
					"	 → jeśli X > " + MAX_AMOUNT + "  → tolerancja, ilość losowa 10-20\n\n" +
					"  /script RaidPlague.class <poziom> [ilość] [tolerancja]\n" +
					"	 → obie wartości podane jawnie"
				);
			return;
		}

		int targetLevel;
		try {
			targetLevel = Integer.parseInt(args.get(0));
		} catch (NumberFormatException e) {
			admin.sendPrivateText("Podaj poprawny poziom (liczba).");
			return;
		}

		int amount;
		int tolerance = DEFAULT_TOLERANCE;
		Random rnd = new Random();

		if (args.size() == 1) { // Case 1: Only <level>
			amount = 10 + rnd.nextInt(11);
		} else if (args.size() == 2) { // Case 2: <level> <X>
			int val;
			try {
				val = Integer.parseInt(args.get(1));
			} catch (NumberFormatException e) {
				admin.sendPrivateText("Drugi parametr musi być liczbą (ilość lub tolerancja).");
				return;
			}

			if (val <= MAX_AMOUNT) {
				amount = val;
			} else {
				amount = 10 + rnd.nextInt(11);
				tolerance = val;
			}
		} else { // Case 3: <level> <amount> <tolerance>
			try {
				amount = Integer.parseInt(args.get(1));
				tolerance = Integer.parseInt(args.get(2));
			} catch (NumberFormatException e) {
				admin.sendPrivateText("Podaj poprawne liczby dla ilości i tolerancji.");
				return;
			}
		}

		// Enforce limits
		if (amount > MAX_AMOUNT) {
			admin.sendPrivateText("Maksymalna ilość potworów to " + MAX_AMOUNT + ". Podano: " + amount);
			amount = MAX_AMOUNT;
		}
		if (tolerance < 0) tolerance = DEFAULT_TOLERANCE;

		// Get admin zone and position
		final StendhalRPZone myZone = sandbox.getZone(admin);
		final int x = admin.getX();
		final int y = admin.getY();
		sandbox.setZone(myZone);

		// Load all defined creatures from server
		Creature[] allCreatures = sandbox.getCreatures();
		if (allCreatures == null || allCreatures.length == 0) {
			admin.sendPrivateText("Brak zdefiniowanych potworów na serwerze.");
			return;
		}

		// Filter creatures by level and profile (exclude rare/abnormal/immortal)
		List<Creature> candidates = new ArrayList<>();
		for (Creature c : allCreatures) {
			if (c == null) continue;
			try {
				// skip special profiles
				if (c.isRare() || c.isAbnormal() || c.isImmortal()) {
					continue;
				}

				if (Math.abs(c.getLevel() - targetLevel) <= tolerance) {
					candidates.add(c);
				}
			} catch (Exception e) {
				logger.warn("Nie można pobrać poziomu dla stwora: " + c + " - " + e.getMessage());
			}
		}

		if (candidates.isEmpty()) {
			admin.sendPrivateText("Brak potworów w zakresie poziomu " +
								  (targetLevel - tolerance) + " - " + (targetLevel + tolerance) + ".");
			return;
		}

		// Spawn selected creatures
		for (int i = 0; i < amount; i++) {
			Creature base = candidates.get(rnd.nextInt(candidates.size()));
			if (base == null) continue;

			RaidCreature spawn = new RaidCreature(base);
			int spawnX = x + games.stendhal.common.Rand.randUniform(-RADIUS, RADIUS);
			int spawnY = y + games.stendhal.common.Rand.randUniform(-RADIUS, RADIUS);

			sandbox.add(spawn, spawnX, spawnY);
		}

		// Inform admin and log the event
		admin.sendPrivateText("Przywołano plagę potworów | poziom ~" + targetLevel +
					" | ilość: " + amount + " | tolerancja: " + tolerance + ".");
		logger.info("RaidPlague przez " + admin.getName() + " | lvl=" + targetLevel +
					" | ilość=" + amount + " | tolerancja=" + tolerance);
	}
}