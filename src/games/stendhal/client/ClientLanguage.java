/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client;

import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.Translation;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

/** Connects client language selection to the existing server language action. */
public final class ClientLanguage {
	public static final String PROPERTY = "ui.language";
	private static String pendingStartupLanguage;
	private static RPObject startupLanguageSubmittedTo;
	private ClientLanguage() { }

	public static String getLanguage() {
		return Translation.normalize(WtWindowManager.getInstance().getProperty(PROPERTY, "pl"));
	}

	public static String getText(String text) {
		return Translation.getText(getLanguage(), text);
	}

	/** The initial welcome can arrive before the startup language action is acknowledged. */
	public static String getWelcomeText(String text) {
		return text != null && text.startsWith("Witaj w ") && text.contains("! Odwiedź stronę - ")
				? getText(text) : text;
	}

	public static synchronized void choose(String language) {
		if (!Translation.isSupported(language)) return;
		if (UserContext.get().getPlayer() == null) {
			pendingStartupLanguage = Translation.normalize(language);
			startupLanguageSubmittedTo = null;
			WtWindowManager.getInstance().setProperty(PROPERTY, pendingStartupLanguage);
			WtWindowManager.getInstance().save();
			return;
		}
		pendingStartupLanguage = null;
		startupLanguageSubmittedTo = null;
		RPAction action = new RPAction();
		action.put("type", "language");
		action.put("target", Translation.normalize(language));
		ClientSingletonRepository.getClientFramework().send(action);
	}

	public static synchronized void synchronize(RPObject player) {
		if (player == null) return;
		String language = Translation.normalize(player.has("language") ? player.get("language") : null);
		if (pendingStartupLanguage != null) {
			if (player.has("language") && pendingStartupLanguage.equals(language)) {
				pendingStartupLanguage = null;
				startupLanguageSubmittedTo = null;
			} else {
				if (startupLanguageSubmittedTo != player && ClientSingletonRepository.getClientFramework() != null) {
					RPAction action = new RPAction();
					action.put("type", "language");
					action.put("target", pendingStartupLanguage);
					ClientSingletonRepository.getClientFramework().send(action);
					startupLanguageSubmittedTo = player;
				}
				return;
			}
		}
		if (!language.equals(getLanguage())) WtWindowManager.getInstance().setProperty(PROPERTY, language);
	}
}
