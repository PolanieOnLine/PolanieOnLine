/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client;

import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.Translation;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

/** Connects client language selection to the existing server language action. */
public final class ClientLanguage {
	public static final String PROPERTY = "ui.language";
	private ClientLanguage() { }

	public static String getLanguage() {
		return Translation.normalize(WtWindowManager.getInstance().getProperty(PROPERTY, "pl"));
	}

	public static String getText(String text) {
		return Translation.getText(getLanguage(), text);
	}

	public static void choose(String language) {
		if (!Translation.isSupported(language)) return;
		if (UserContext.get().getPlayer() == null) {
			WtWindowManager.getInstance().setProperty(PROPERTY, Translation.normalize(language));
			return;
		}
		RPAction action = new RPAction();
		action.put("type", "language");
		action.put("target", Translation.normalize(language));
		ClientSingletonRepository.getClientFramework().send(action);
	}

	public static void synchronize(RPObject player) {
		if (player == null) return;
		String language = Translation.normalize(player.has("language") ? player.get("language") : null);
		if (!language.equals(getLanguage())) WtWindowManager.getInstance().setProperty(PROPERTY, language);
	}
}
