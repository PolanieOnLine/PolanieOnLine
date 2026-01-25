package games.stendhal.server.script;

import java.util.List;

import games.stendhal.server.core.rp.DragonMistEventManager;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * Forces the Dragon Mist event to trigger immediately.
 */
public class ForceDragonMistEvent extends ScriptImpl {
	@Override
	public void execute(final Player admin, final List<String> args) {
		if (!args.isEmpty() && "help".equalsIgnoreCase(args.get(0))) {
			admin.sendPrivateText("Użycie: /script ForceDragonMistEvent.class");
			return;
		}

		DragonMistEventManager.get().forceEvent();
		admin.sendPrivateText("Smocza mgła została wymuszona.");
	}
}
