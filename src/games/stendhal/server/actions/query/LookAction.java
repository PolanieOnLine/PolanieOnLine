/***************************************************************************
 *                   (C) Copyright 2003-2013 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions.query;

import static games.stendhal.common.constants.Actions.LOOK;
import static games.stendhal.common.constants.Actions.NAME;
import static games.stendhal.common.constants.Actions.TYPE;

import games.stendhal.common.NotificationType;
import games.stendhal.common.Translation;
import games.stendhal.common.constants.Actions;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.actions.validator.ActionData;
import games.stendhal.server.actions.validator.ActionValidation;
import games.stendhal.server.actions.validator.ExtractEntityValidator;
import games.stendhal.server.actions.validator.SlotVisibleIfEntityContained;
import games.stendhal.server.actions.validator.ZoneNotChanged;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.office.RentedSign;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.player.Player;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import marauroa.common.game.RPAction;

/**
 * Processes a look menu action.
 */
public class LookAction implements ActionListener {

	private static final ActionValidation VALIDATION = new ActionValidation();
	static {
		VALIDATION.add(new ZoneNotChanged());
		VALIDATION.add(new ExtractEntityValidator());
		VALIDATION.add(new SlotVisibleIfEntityContained());
	}

	/**
	 * registers the look action
	 */
	public static void register() {
		CommandCenter.register(LOOK, new LookAction());
	}

	/**
	 * processes the requested action.
	 *
	 * @param player the caller of the action
	 * @param action the action to be performed
	 */
	@Override
	public void onAction(final Player player, final RPAction action) {
		ActionData data = new ActionData();
		if (!VALIDATION.validateAndInformPlayer(player, action, data)) {
			return;
		}

		Entity entity = data.getEntity();

		if (entity != null) {

			String name = entity.get(TYPE);
			if (entity.has(NAME)) {
				name = entity.get(NAME);
			}
			new GameEvent(player.getName(), LOOK, name).raise();
			final String text = getLookDescription(entity, player.getLanguage());

			if (entity instanceof Player || (entity.has(Actions.ACTION) && entity.get(Actions.ACTION).equals(Actions.READ))) {
				player.sendPrivateText(NotificationType.RESPONSE, text);
			} else {
				player.sendPrivateText(text);
			}

			player.notifyWorldAboutChanges();
		}
	}

	/**
	 * Keep item look output descriptive and avoid exposing rarity, value,
	 * improvements or combat parameters. Bound items retain their ownership
	 * notice after the item's normal description.
	 */
	static String getLookDescription(final Entity entity) {
		if (entity instanceof Item) {
			final Item item = (Item) entity;
			String description = item.hasDescription()
					? item.getDescription() : "Oto " + item.getTitle() + ".";
			description = underlineItemName(description, item.getTitle());
			if (item.isBound()) {
				description += " Oto specjalna nagroda dla " + item.getBoundTo()
						+ ", która nie może być wykorzystana przez innych.";
			}
			return description;
		}
		return entity.describe();
	}

	/** Localize the final display only; entity descriptions and names remain canonical. */
	static String getLookDescription(final Entity entity, final String language) {
		if ("pl".equals(Translation.normalize(language))) {
			return getLookDescription(entity);
		}
		if (entity instanceof Player) {
			return getPlayerLookDescription((Player) entity, language);
		}
		if (entity instanceof Item) {
			final Item item = (Item) entity;
			String text = item.hasDescription() ? Translation.getText(language, item.getDescription())
					: Translation.getText(language, "Oto {0}.").replace("{0}", Translation.getText(language, item.getTitle()));
			text = underlineItemName(text, Translation.getText(language, item.getTitle()));
			if (item.isBound()) {
				text += " " + Translation.getText(language,
						"Oto specjalna nagroda dla {0}, która nie może być wykorzystana przez innych.")
						.replace("{0}", item.getBoundTo());
			}
			return text;
		}
		if (entity instanceof Sign) {
			final String raw = entity.describe();
			// Rented notices contain player-written text; ordinary signs contain authored game text.
			if (entity.has("text")) {
				final String prefix = raw.startsWith("Czytasz:\n\"") ? "Czytasz:\n\"" : "Czytasz: \"";
				final String original = prefix + ((Sign) entity).getText() + "\"";
				if (raw.startsWith(original)) {
					final String body = entity instanceof RentedSign ? entity.get("text")
							: ((Sign) entity).getText(language);
					return format(language, prefix + "{0}\"", body)
							+ Translation.getText(language, raw.substring(original.length()));
				}
			}
			return Translation.getText(language, raw);
		}
		final String raw = entity.describe();
		final String base = entity.hasDescription() ? entity.getDescription()
				: "Oto " + entity.getDescriptionName() + ".";
		if (!raw.startsWith(base)) {
			return Translation.getText(language, raw);
		}
		String text;
		if (entity.hasDescription()) {
			text = Translation.getText(language, base);
		} else {
			String name = entity.getDescriptionName();
			if (entity instanceof Creature) name = Translation.getText(language, name);
			text = Translation.getText(language, "Oto {0}.").replace("{0}", name);
		}
		// RPEntity appends level; Creature appends armor. Translate each complete field.
		String[] fields = raw.substring(base.length()).split("(?<=\\.)(?=\\s)", -1);
		for (String field : fields) text += Translation.getText(language, field);
		return text;
	}

	private static String underlineItemName(final String description, final String title) {
		return title.isEmpty() ? description : description.replace(title, "§'" + title + "'");
	}

	private static String getPlayerLookDescription(final Player player, final String language) {
		if (player.hasDescription()) return player.getDescription();
		final String name = player.getTitle();
		final String time = format(language, "{0} i {1}",
				Translation.getText(language, player.getAge() / 60 + " godzinę"),
				Translation.getText(language, player.getAge() % 60 + " minutę"));
		String text;
		if ("postman".equals(name)) {
			text = format(language, "Oto {0}. Listonosz, który stara się, aby wszelkie listy dotarły do ich odbiorców. Wiek {1}.", name, time);
		} else {
			// Use the rank already selected by Player.describe(), without copying its level rules.
			final String firstLine = player.describe().split("\\n", 2)[0];
			final String suffix = " " + name + ".";
			if (!firstLine.startsWith("Oto ") || !firstLine.endsWith(suffix)) return player.describe();
			final String rank = firstLine.substring(4, firstLine.length() - suffix.length());
			text = format(language, "Oto {0} {1}.", Translation.getText(language, rank), name)
					+ "\n" + format(language, "{0} posiada poziom {1}. Wiek {2}.", name, Integer.toString(player.getLevel()), time);
		}
		final String away = player.getAwayMessage(), grumpy = player.getGrumpyMessage();
		if (away != null) text += "\n" + format(language,
				"{0} nie ma " + Grammar.genderVerb(player.getGender(), "go") + " teraz, ale "
				+ Grammar.genderVerb(player.getGender(), "zostawił") + " wiadomość: {1}", name, away);
		if (grumpy != null) text += "\n" + format(language,
				"{0} ukrywa się i " + Grammar.genderVerb(player.getGender(), "zostawił") + " wiadomość: {1}", name, grumpy);
		if (away == null && grumpy == null && !player.getSentence().isEmpty()) {
			text += "\n" + format(language, "Opis {0} jest: \"{1}\"", name, player.getSentence());
		}
		return text;
	}

	/** Substitute once, without interpreting nicknames or player-written text as dictionary keys. */
	private static String format(final String language, final String source, final String... values) {
		final Matcher slots = Pattern.compile("\\{(\\d+)\\}").matcher(Translation.getText(language, source));
		final StringBuffer result = new StringBuffer();
		while (slots.find()) slots.appendReplacement(result, Matcher.quoteReplacement(values[Integer.parseInt(slots.group(1))]));
		return slots.appendTail(result).toString();
	}

}
