package games.stendhal.server.script;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.MathHelper;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.RPObjectDAO;

/**
 * dumps characters that might be deleted
 *
 * @author hendrik
 */
public class DumpDeletionCandidates extends ScriptImpl {
	private final Logger logger = Logger.getLogger(DumpDeletionCandidates.class);

	@Override
	public void execute(Player admin, List<String> args) {
	    admin.sendPrivateText("Ważne: <musisz wyedytować skrypt, aby zdefiniować najwyższy numer RPObject.");
		for (int i = 1; i < 0; i++) {
			RPObject object = null;
			try {
				object = DAORegister.get().get(RPObjectDAO.class).loadRPObject(i, false);
			} catch (SQLException e) {
				// ignore
			} catch (IOException e) {
				// ignore
			}
			if (object != null) {
				dumpObject(object);
			}
		}
	}

	private void dumpObject(RPObject object) {
		StringBuilder items = new StringBuilder();
			final String[] slotsItems = { "bag", "rhand", "lhand", "head", "neck", "armor",
				"pas", "legs", "glove", "feet", "finger", "cloak", "fingerb", "bank", "bank_ados",
				"zaras_chest_ados", "bank_fado", "bank_nalwor", "bank_zakopane", "bank_krakow", "bank_tsoh",
				"bank_gdansk", "spells", "keyring", "money", "trade"};

			for (final String slotName : slotsItems) {
				final RPSlot slot = object.getSlot(slotName);
				if (slot != null) {
					for (RPObject item : slot) {
						items.append(MathHelper.parseIntDefault(item.get("quantity"), 0) + " " + item.get("name") + ", ");
					}
				}
			}
		if (!object.has("level") || object.getInt("level") < 5) {
			logger.info(object.get("name") + ";" + object.get("level") + ";" + object.get("age") + ";" + object.get("release") + ";" + items.toString());
		}
	}
}
