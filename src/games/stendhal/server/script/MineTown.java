/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.List;

import games.stendhal.server.core.events.seasonal.SeasonalEventService;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * Backwards compatible entry point for Mine Town Revival Weeks.
 *
 * New administration should use:
 * <pre>
 * /script SeasonalEvent.class minetown on
 * /script SeasonalEvent.class minetown off
 * </pre>
 */
public class MineTown extends ScriptImpl {
	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args.size() != 1) {
			admin.sendPrivateText("/script MineTown.class {true|false}");
			return;
		}

		final String value = args.get(0).trim().toLowerCase();
		if (!("true".equals(value) || "false".equals(value))) {
			admin.sendPrivateText("/script MineTown.class {true|false}");
			return;
		}

		final boolean enabled = Boolean.parseBoolean(value);
		final SeasonalEventService service = SeasonalEventService.get();
		final boolean accepted = service.requestMineTown(enabled,
				new SeasonalEventService.ResultListener() {
					@Override
					public void onResult(final boolean success, final String message) {
						admin.sendPrivateText((success ? "[OK] " : "[BŁĄD] ") + message);
					}
				});
		if (!accepted) {
			admin.sendPrivateText("Inny event sezonowy jest właśnie przełączany. Spróbuj ponownie po zakończeniu operacji.");
			return;
		}
		if (service.isTransitionInProgress()) {
			admin.sendPrivateText("Przygotowuję Mine Town=" + enabled
					+ " poza wątkiem RP. Zmiana zostanie zastosowana po walidacji zasobów.");
		}
	}
}
