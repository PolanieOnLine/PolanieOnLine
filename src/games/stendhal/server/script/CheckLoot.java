package games.stendhal.server.script;

import java.util.List;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

public class CheckLoot extends ScriptImpl {
	@Override
	public void execute(final Player admin, final List<String> args) {
		//super.execute(admin, args);
		if (args == null || args.size() != 2) {
			admin.sendPrivateText("Użycie CheckLoot: <gracz> <przedmiot>");
			return;
		}

		final String player_name = args.get(0);
		final Player target = SingletonRepository.getRuleProcessor().getPlayer(player_name);
		// All remaining arguments are the item's name
		final String item = String.join(" ", args.subList(1, args.size()));

		// Player does not exist
		if (target == null) {
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Nie ma takiego gracza: " + player_name + ".");
			return;
		} if (item == null) {
			admin.sendPrivateText(NotificationType.ERROR, "BŁĄD: Nie ma takiego przedmiotu #'" + item + "'.");
			return;
		}

		final int actual_count = target.getNumberOfLootsForItem(item);
		admin.sendPrivateText(NotificationType.CLIENT, "Gracz \"" + target.getName() + "\" zdobył łączenie:\n" + Integer.toString(actual_count) + " #'" + item + "'.");
	}
}
