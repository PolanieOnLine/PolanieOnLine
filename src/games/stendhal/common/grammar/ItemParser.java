/***************************************************************************
 *                    Copyright © 2003-2023 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.common.grammar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import games.stendhal.common.Translation;

import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.NameSearch;
import games.stendhal.common.parser.Sentence;

public class ItemParser {
	private static final Map<String, Set<String>> TRANSLATED_ITEM_ALIASES = new ConcurrentHashMap<String, Set<String>>();

	/** ItemNames contains all valid item names. */
	protected Set<String> itemNames;

	public ItemParser() {
		this.itemNames = new HashSet<String>();
	}

	public ItemParser(final Set<String> itemNames) {
		this.itemNames = itemNames;
	}

	public ItemParser(final String itemName) {
		itemNames = new HashSet<String>();
		itemNames.add(itemName);
	}

	/**
	 * @return the recognized item names
	 */
	public Set<String> getItemNames() {
		return itemNames;
	}

	/**
	 * Search for a matching name in the available names.
	 *
	 * @param sentence
	 * @return parsing result
	 */
	public ItemParserResult parse(final Sentence sentence) {
		// get names from overriden method
		final Set<String> iNames = getItemNames();

		if (iNames.isEmpty()) {
			List<Expression> expressions = sentence.getExpressions();
			String chosenName;
			int amount;

			if (expressions.size() == 1) {
				Expression expr = expressions.get(0);
				amount = expr.getAmount();
				chosenName = expr.getNormalized();
			} else {
				final Expression numeral = sentence.getNumeral();
				amount = numeral!=null? numeral.getAmount(): 1;
				chosenName = sentence.getExpressionStringAfterVerb();
			}

			return new ItemParserResult(false, chosenName, amount, null);
		}

		NameSearch search = sentence.findMatchingName(iNames);
		// Display names are aliases only within this NPC's permitted wares.
		// Saved item IDs and the player's original chat message stay unchanged.
		Map<String, String> aliases = new HashMap<String, String>();
		if (!search.found()) {
			Set<String> ambiguous = new HashSet<String>();
			for (String name : iNames) {
				for (String alias : translatedAliases(name)) {
					if (aliases.containsKey(alias) && !aliases.get(alias).equals(name)) ambiguous.add(alias);
					else aliases.put(alias, name);
				}
			}
			for (String alias : ambiguous) aliases.remove(alias);
			if (!aliases.isEmpty()) search = sentence.findMatchingName(aliases.keySet());
		}

		boolean found = search.found();

		// Store found name.
		String chosenName = search.getName();
		if (chosenName != null && aliases.containsKey(chosenName)) chosenName = aliases.get(chosenName);
		int amount = search.getAmount();
		Set<String> mayBeItems = null;

		if (!found) {
			if ((sentence.getNumeralCount() == 1)
					&& (sentence.getUnknownTypeCount() == 0)
					&& (sentence.getObjectCount() == 0)) {
				final Expression number = sentence.getNumeral();

				// If there is given only a number, return this as amount.
				amount = number.getAmount();
			} else {
				// If there was no match, return the given object name instead
				// and set amount to 1.
				chosenName = sentence.getExpressionStringAfterVerb();
				amount = 1;
			}

			if (chosenName == null && iNames.size() == 1) {
				// The NPC only offers one type of ware, so
				// it's clear what the player wants.
				chosenName = iNames.iterator().next();
				found = true;
			} else if (chosenName != null) {
				mayBeItems = new HashSet<String>();

				// search for items to sell with compound names, ending with the given expression
				for(String name : iNames) {
					if (name.endsWith(" "+chosenName)||(name.startsWith(chosenName+" "))) {
						mayBeItems.add(name);
					}
				}
			}
		}

		return new ItemParserResult(found, chosenName, amount, mayBeItems);
	}

	private static Set<String> translatedAliases(final String name) {
		return TRANSLATED_ITEM_ALIASES.computeIfAbsent(name, canonical -> {
			Set<String> result = new HashSet<String>();
			String singular = Translation.getText("en", canonical);
			if (!singular.equals(canonical)) result.add(singular);
			// A displayed plural or mass noun, such as "pieces of wood", is still the same ware.
			for (int amount : new int[]{2, 5}) {
				String source = Grammar.quantityplnoun(amount, canonical);
				String translated = Translation.getText("en", source);
				String prefix = amount + " ";
				if (!source.equals(translated) && translated.startsWith(prefix)) result.add(translated.substring(prefix.length()));
			}
			return Collections.unmodifiableSet(result);
		});
	}

	/**
	 * Answer with an error message in case the request could not be fulfilled.
	 *
	 * @param res
	 * @param userAction
	 * @param npcAction
	 * @return error message
	 */
	public String getErrormessage(final ItemParserResult res, final List<String> userAction, String npcAction) {
		String chosenItemName = res.getChosenItemName();
		Set<String> mayBeItems = res.getMayBeItems();

		if (npcAction.equals("produce")) {
			npcAction = "produkuję";
		} else if (npcAction.equals("buy")) {
			npcAction = "skupuję";
		} else if (npcAction.equals("sell")) {
			npcAction = "sprzedaję";
		} else if (npcAction.equals("repair")) {
			npcAction = "naprawiam";
		} else if (npcAction.equals("heal")) {
			npcAction = "leczę";
		}

		if (chosenItemName == null) {
			return "Powiedz mi co chcesz zrobić.";
		} else if (mayBeItems!=null && mayBeItems.size()>1) {
			return "Jest więcej niż jeden " + chosenItemName + ". " +
					"Powiedz mi jaki rodzaj "
					+ chosenItemName + " chcesz użyć.";
		} else if (mayBeItems!=null && !mayBeItems.isEmpty()) {
			return "Powiedz mi jaki rodzaj "
					+ chosenItemName + " chcesz użyć.";
		} else {
			return "Nie " + npcAction + " "
					+ Grammar.plural(chosenItemName) + ".";
		}
	}
}
