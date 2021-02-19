/***************************************************************************
 *                    (C) Copyright 2009-2015 - Stendhal                    *
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Expression;

/**
 * Helper functions for producing and parsing grammatically-correct sentences.
 */
public class Grammar {
	private static final Logger logger = Logger.getLogger(Grammar.class);

	// static instance
	private static Grammar instance;

	public static Grammar get() {
		if (instance == null) {
			instance = new Grammar();
		}

		return instance;
	}

	/**
	 * "it" or "them", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "it" or "them" as appropriate
	 */
	public static String itthem(final int quantity) {
		if (quantity == 1) {
			return "to";
		} else {
			return "je";
		}
	}

	/**
	 * Modify a word to upper case notation.
	 *
	 * @param word
	 * @return word with first letter in upper case
	 */
	public static String makeUpperCaseWord(final String word) {
		final StringBuilder res = new StringBuilder();
		if (word.length() > 0) {
			res.append(Character.toUpperCase(word.charAt(0)));
			if (word.length() > 1) {
				res.append(word.substring(1));
			}
		}
		return res.toString();
	}

	/**
	 * "It" or "Them", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "It" or "Them" as appropriate
	 */
	public static String ItThem(final int quantity) {
		return makeUpperCaseWord(itthem(quantity));
	}

	/**
	 * "it" or "they", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "it" or "they" as appropriate
	 */
	public static String itthey(final int quantity) {
		if (quantity == 1) {
			return "to";
		} else {
			return "oni";
		}
	}

	/**
	 * "It" or "They", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "It" or "They" as appropriate
	 */
	public static String ItThey(final int quantity) {
		return makeUpperCaseWord(itthey(quantity));
	}

	/**
	 * "is" or "are", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "is" or "are" as appropriate
	 */
	public static String isare(final int quantity) {
		if (quantity == 1) {
			return "jest";
		} else {
			return "są";
		}
	}

	/**
	 * "Is" or "Are", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "Is" or "Are" as appropriate
	 */
	public static String IsAre(final int quantity) {
		return makeUpperCaseWord(isare(quantity));
	}

	/**
	 * "has" or "have", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "has" or "have" as appropriate
	 */
	public static String hashave(final int quantity) {
		if (quantity == 1) {
			return "ma";
		} else {
			return "mają";
		}
	}

	/**
	 * "Has" or "Have", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "Has" or "Have" as appropriate
	 */
	public static String HasHave(final int quantity) {
		return makeUpperCaseWord(hashave(quantity));
	}

	/**
	 * "that" or "those", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "that" or "those" as appropriate
	 */
	public static String thatthose(final int quantity) {
		if (quantity == 1) {
			return "ten";
		} else {
			return "te";
		}
	}

	/**
	 * "That or "Those", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "That" or "Those" as appropriate
	 */
	public static String ThatThose(final int quantity) {
		return makeUpperCaseWord(thatthose(quantity));
	}

	/**
	 * "this" or "these", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "this" or "these" as appropriate
	 */
	public static String thisthese(final int quantity) {
		if (quantity == 1) {
			return "ten";
		} else {
			return "te";
		}
	}

	/**
	 * "This or "These", depending on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @return Either "This" or "These" as appropriate
	 */
	public static String ThisThese(final int quantity) {
		return makeUpperCaseWord(thisthese(quantity));
	}


	/**
	 * Adds a prefix unless it was already added.
	 *
	 * @param noun
	 *            the noun (which may already start with the specified prefix
	 * @param prefixSingular
	 *            prefix to add
	 * @param prefixPlural
	 *            prefix, that may be present in plural form
	 * @return noun starting with prefix
	 */
	static String addPrefixIfNotAlreadyThere(final String noun,
			final String prefixSingular, final String prefixPlural) {
		if (noun.startsWith(prefixSingular)) {
			return noun;
		} else if (noun.startsWith(prefixPlural)) {
			return noun;
		} else {
			return prefixSingular + noun;
		}
	}

	/**
	 * Prefix a noun with an expression like "piece of".
	 *
	 * @param noun
	 * @return noun with prefix
	 */
	public static String fullForm(final String noun) {
		final String lowString = noun.toLowerCase(Locale.ENGLISH);
		String str = lowString.replace("#", "");

		if (str.startsWith("book ")) {
			str = str.substring(5) + " book";
		} else if (str.indexOf(" armor") > -1) {
			str = addPrefixIfNotAlreadyThere(lowString, "suit of ", "suits of ");
		} else {
			str = replaceInternalByDisplayNames(PrefixManager.s_instance.fullForm(str, lowString));
		}

		return str;
	}

	/**
	 * Replace internal item names bye their display name.
	 * @param str
	 * @return fixed string
	 */
	public static String replaceInternalByDisplayNames(final String str) {
		return str.
			replace("icecream", "ice cream");
	}

	/**
	 * Merge two expressions into a compound noun.
	 * @param word1
	 * @param word2
	 * @return resulting expression: word1 or word2
	 */

	public static Expression mergeCompoundNoun(final Expression word1, Expression word2) {
		// handle special cases:
				// "ice cream" -> "ice"
		if ((word1.getMainWord().equals("ice") && word2.getMainWord().equals("cream")) ||
				// "teddy bear" -> "teddy"
				(word1.getMainWord().equals("teddy") && word2.getMainWord().equals("bear"))) {

		    // transform "ice cream" into the item name "icecream"
		    if (word1.getMainWord().equals("ice")) {
		    	word1.setNormalized("icecream");
		    }

		    return word1;
        } else {
            word2.mergeLeft(word1, true);

            return word2;
        }
	}

	/**
	 * Extracts noun from a string, that may be prefixed with a plural expression
	 * like "piece of", ... So this function is just the counter part to fullForm().
	 *
	 * @param text
	 * @return the extracted noun
	 */
	public static String extractNoun(final String text) {
		String result;

		if (text == null) {
			result = null;
		} else {
			final PrefixExtractor extractor = new PrefixExtractor(text);
			boolean changed;

			// loop until all prefix strings are removed
			do {
				changed = false;

				if (extractor.extractNounSingular()) {
					changed = true;
				}

				if (extractor.extractNounPlural()) {
					changed = true;
				}
			} while(changed);

			result = extractor.toString();
		}

		return result;
	}

	/**
	 * Check if an expression is normalized.
	 * equivalent to: {return extractNoun(text) == text}
	 *
	 * @param text
	 * @return true if the expression is already normalized
	 */
	public static boolean isNormalized(final String text) {
		boolean ret;

		if (text == null) {
			ret = true;
		} else {
			final PrefixExtractor extractor = new PrefixExtractor(text);

			// If there is detected any prefix, the reviewed text was not normalized.
			if (extractor.extractNounSingular() || extractor.extractNounPlural()) {
				ret = false;
			} else {
				ret = true;
			}
		}

		return ret;
	}

	private static final String of = " ";

	/**
	 * Returns the plural form of the given noun if not already given in plural
	 * form.
	 *
	 * @param noun
	 *            The noun to examine
	 * @return An appropriate plural form
	 */
	public static String plural(final String noun) {
		if (noun == null) {
			return null;
		}

		String enoun = fullForm(noun);
		String postfix = "";

		final int position = enoun.indexOf('+');
		if (position != -1) {
			if (enoun.charAt(position - 1) == ' ') {
				postfix = enoun.substring(position - 1);
				enoun = enoun.substring(0, position - 1);
			} else {
				postfix = enoun.substring(position);
				enoun = enoun.substring(0, position);
			}
		}

		// in "of"-phrases pluralize only the first part
		if (enoun.indexOf(of) > -1) {
			return plural(enoun.substring(0, enoun.indexOf(of))) + enoun.substring(enoun.indexOf(of)) + postfix;

		} else if (enoun.equals("wojownik")) {
			return enoun + "ów" + postfix;
			
		} else if (enoun.equals("dzień")) {
			return enoun.substring(0, enoun.length() - 1) + "ni" + postfix;
		} else if (enoun.equals("tydzień")) {
			return enoun.substring(0, enoun.length() - 5) + "godnie" + postfix;

		} else if (enoun.equals("miecz")) {
			return enoun + "e" + postfix;
		} else if (enoun.equals("spodnie")) {
			return enoun.substring(0, enoun.length() - 1) + postfix;

		} else if (enoun.endsWith("dę") || enoun.endsWith("tę") || enoun.endsWith("nę")) {
			return enoun.substring(0, enoun.length() - 1) + "y" + postfix;

		} else if (enoun.endsWith("wy")) {
			return enoun.substring(0, enoun.length() - 2) + "owe" + postfix;

		} else if (enoun.endsWith("ik")) {
			return enoun.substring(0, enoun.length() - 1) + "cy" + postfix;

		} else if (enoun.endsWith("o") || enoun.endsWith("e")) {
			return enoun.substring(0, enoun.length() - 1) + "a" + postfix;
		} else if (enoun.endsWith("um")) {
			return enoun.substring(0, enoun.length() - 2) + "a" + postfix;

		} else if (enoun.endsWith("ka") || enoun.endsWith("ga")) {
			return enoun.substring(0, enoun.length() - 1) + "i" + postfix;

		} else if (enoun.endsWith("ca") || enoun.endsWith("ea") || enoun.endsWith("ia") || enoun.endsWith("ja")
				|| enoun.endsWith("la") || enoun.endsWith("ża") || enoun.endsWith("rza")) {
			return enoun.substring(0, enoun.length() - 1) + "e" + postfix;

		} else if (enoun.endsWith("a")) {
			return enoun.substring(0, enoun.length() - 1) + "y" + postfix;

		} else if (enoun.endsWith("k") || enoun.endsWith("g")) {
			return enoun + "i" + postfix;

		} else if (enoun.endsWith("c") || enoun.endsWith("j") || enoun.endsWith("ż") || enoun.endsWith("rz")) {
			return enoun + "e" + postfix;

		} else if (enoun.endsWith("ć")) {
			return enoun.substring(0, enoun.length() - 1) + "cie" + postfix;
		} else if (enoun.endsWith("ń")) {
			return enoun.substring(0, enoun.length() - 1) + "nie" + postfix;
		} else if (enoun.endsWith("ś")) {
			return enoun.substring(0, enoun.length() - 1) + "sie" + postfix;
		} else if (enoun.endsWith("ź")) {
			return enoun.substring(0, enoun.length() - 1) + "zie" + postfix;

		} else {
			// no special case matched, so use the boring default plural rule
			return enoun + "y" + postfix;
		}
	}

	/**
	 * Returns the plural form of the given noun if not already given in plural
	 * form. Method to prevent collision of items and creatures.
	 *
	 * @param noun
	 *            The noun to examine
	 * @return An appropriate plural form
	 */
	public static String pluralCreature(final String noun) {
		if (noun.equals("chicken")) {
			return "chickens";
		}
		
		return plural(noun);
	}

	/**
	 * Returns the singular form of the given noun if not already given in
	 * singular form.
	 *
	 * @param enoun
	 *            The noun to examine
	 * @return An appropriate singular form
	 */
	public static String singular(String enoun) {
		if (enoun == null) {
			return null;
		}

		String postfix = "";

		final int position = enoun.indexOf('+');
		if (position != -1) {
			postfix = enoun.substring(position - 1);
			enoun = enoun.substring(0, position - 1);
		}

		// in "of"-phrases build only the singular of the first part
		if (enoun.indexOf(of) > -1) {
			return singular(enoun.substring(0, enoun.indexOf(of)))
					+ enoun.substring(enoun.indexOf(of)) + postfix;

			// first of all handle words which do not change
		} else if (enoun.endsWith("money") || enoun.endsWith("dice")
				|| enoun.endsWith("sheep") || enoun.endsWith("goat")
				|| enoun.endsWith("legs") || enoun.endsWith("boots")
				|| enoun.equals("moose") || enoun.equals("magic")
				|| enoun.endsWith("kości do gry") || enoun.equals("jeleń")) {
			return enoun + postfix;

			// now all the special cases
		} else if (enoun.endsWith("staffs") || enoun.endsWith("chiefs")) {
			return enoun.substring(0, enoun.length() - 1) + postfix;
		} else if ((enoun.length() > 4) && enoun.endsWith("ves")
				&& ("aeiourl".indexOf(enoun.charAt(enoun.length() - 4)) > -1)
				&& !enoun.endsWith("knives")) {
			return enoun.substring(0, enoun.length() - 3) + "f" + postfix;
		} else if (enoun.endsWith("ves")) {
			return enoun.substring(0, enoun.length() - 3) + "fe" + postfix;
		} else if (enoun.endsWith("houses")) {
			return enoun.substring(0, enoun.length() - 1) + postfix;
		} else if ((enoun.length() > 3) && enoun.endsWith("ice")
				&& ("mMlL".indexOf(enoun.charAt(enoun.length() - 4)) > -1)) {
			return enoun.substring(0, enoun.length() - 3) + "ouse" + postfix;
		} else if (enoun.endsWith("eese")
				&& !enoun.endsWith("cabeese") && !enoun.endsWith("cheese")) {
			return enoun.substring(0, enoun.length() - 4) + "oose" + postfix;
		} else if (enoun.endsWith("eeth")) {
			return enoun.substring(0, enoun.length() - 4) + "ooth" + postfix;
		} else if (enoun.endsWith("feet")) {
			return enoun.substring(0, enoun.length() - 4) + "foot" + postfix;
		} else if (enoun.endsWith("children")) {
			return enoun.substring(0, enoun.length() - 3) + postfix;
		} else if (enoun.endsWith("eaux")) {
			return enoun.substring(0, enoun.length() - 1) + postfix;
		} else if (enoun.endsWith("atoes")) {
			return enoun.substring(0, enoun.length() - 2) + postfix;
		// don't transform "wikipedia" to "wikipedium" -> endswith("ia") is not enough
		} else if (enoun.endsWith("helia") || enoun.endsWith("sodia")) {
			return enoun.substring(0, enoun.length() - 1) + "um" + postfix;
		} else if (enoun.endsWith("algae") || enoun.endsWith("hyphae")
				|| enoun.endsWith("larvae")) {
			return enoun.substring(0, enoun.length() - 1) + postfix;
		} else if ((enoun.length() > 2) && enoun.endsWith("ei")) {
			return enoun.substring(0, enoun.length() - 1) + "us" + postfix;
		} else if (enoun.endsWith("men")) {
			return enoun.substring(0, enoun.length() - 3) + "man" + postfix;
		} else if (enoun.endsWith("matrices")) {
			return enoun.substring(0, enoun.length() - 4) + "ix" + postfix;
		} else if (enoun.endsWith("ices")) {
			// indices, vertices, ...
			return enoun.substring(0, enoun.length() - 4) + "ex" + postfix;
		} else if (enoun.endsWith("erinyes")) {
			return enoun.substring(0, enoun.length() - 2) + "s" + postfix;
		} else if (enoun.endsWith("erinys") || enoun.endsWith("cyclops")) {
			// singular detected
			return enoun + postfix;
		} else if (enoun.endsWith("mumakil")) {
			return enoun.substring(0, enoun.length() - 2) + postfix;
		} else if (enoun.endsWith("djin")) {
			return enoun + "ni" + postfix;
		} else if (enoun.endsWith("efreet")) {
			return enoun + "i" + postfix;
		} else if (enoun.endsWith("porcini") || enoun.endsWith("porcino")) {
			return enoun.substring(0, enoun.length() - 1) + "o" + postfix;
		} else if (enoun.endsWith("lotus") || enoun.endsWith("wumpus")
				|| enoun.endsWith("deus")) {
			return enoun + postfix;
		} else if (enoun.endsWith("cabooses")) {
			return enoun.substring(0, enoun.length() - 1) + postfix;
		} else if (enoun.endsWith("yses") || enoun.endsWith("ysis")) {
			return enoun.substring(0, enoun.length() - 2) + "is" + postfix;
		} else if ((enoun.length() > 3)
				&& enoun.endsWith("es")
				&& (("zxs".indexOf(enoun.charAt(enoun.length() - 3)) > -1) || (enoun.endsWith("ches") || enoun.endsWith("shes")))
				&& !enoun.endsWith("axes") && !enoun.endsWith("bardiches")
				&& !enoun.endsWith("nooses")) {
			return enoun.substring(0, enoun.length() - 2) + postfix;
		} else if ((enoun.length() > 4) && enoun.endsWith("ies")
				&& isConsonant(enoun.charAt(enoun.length() - 4))
				&& !enoun.endsWith("zombies")) {
			return enoun.substring(0, enoun.length() - 3) + "y" + postfix;
			// no special case matched, so look for the standard "s" plural
		} else if (enoun.equals("glück") || enoun.equals("glücke")) {
			return "glück";
		} else {
			return enoun + postfix;
		}
	}

	/**
	 * Returns either the plural or singular form of the given noun, depending
	 * on the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @param noun
	 *            The noun to examine
	 * @return Either "[noun]" or plural("[noun]") as appropriate
	 */
	public static String plnoun(final int quantity, final String noun) {
		final String enoun = fullForm(noun);
		if (quantity == 1) {
			return singular(enoun);
		} else {
			return plural(noun);
		}
	}

	/**
	 * Returns either the plural or singular form of the given noun, depending
	 * on the quantity. Method to prevent collision of items and creatures.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @param noun
	 *            The noun to examine
	 * @return Either "[noun]" or plural("[noun]") as appropriate
	 */
	public static String plnounCreature(final int quantity, final String noun) {
		if (noun.equals("chicken")) {
			if (quantity == 1) {
				return "chicken";
			} else {
				return "chickens";
			}
		}

		return plnoun(quantity, noun);
	}

	/**
	 * Returns either the plural or singular form of the given noun, depending
	 * on the quantity; also prefixes the quantity.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @param noun
	 *            The noun to examine
	 * @return Either "[quantity] [noun]" or "[quantity]" + plural("[noun]") as
	 *         appropriate
	 */
	public static String quantityplnoun(final int quantity, final String noun) {
		final String end = plnoun(quantity, noun);
		return Integer.toString(quantity) + " " + end;
	}

	/**
	 * Returns either the plural or singular form of the given noun, depending
	 * on the quantity; also prefixes the quantity. Method to prevent
	 * collision of items and creatures
	 *
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @param noun
	 *            The noun to examine
	 * @return Either "[quantity] [noun]" or "[quantity]" + plural("[noun]") as
	 *         appropriate
	 */
	public static String quantityplnounCreature(final int quantity, final String noun) {
		if (noun.equals("chicken")) {
			final String end = plnounCreature(quantity, noun);
			return Integer.toString(quantity) + " " + end;
		}

		return quantityplnoun(quantity, noun);
	}

	/**
	 * Returns either the plural or singular form of the given noun, depending
	 * on the quantity; also prefixes the quantity. In case the quantity is exactly
	 * 1, the specified prefix is used. Note: There is some additional magic to convert
	 * "a" and "A" to "an" and "An" in case that is required by the noun.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @param noun
	 *            The noun to examine
	 * @param one replacement for "1".
	 * @return Either "[quantity] [noun]" or "[quantity]" + plural("[noun]") as
	 *         appropriate
	 */
	public static String quantityplnoun(final int quantity, final String noun, final String one) {
		final String word = plnoun(quantity, noun);

		if (quantity == 1) {
			if (one.equals("")) {
				return word;
			} else {
				return one + " " + word;
			}
		} else {
			return Integer.toString(quantity) + " " + plural(noun);
		}
	}

	/**
	 * Returns either the plural or singular form of the given noun, depending on
	 * the quantity; also prefixes the quantity and prints the noun with a hash prefix.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @param noun
	 *            The noun to examine
	 * @return Either "[quantity] [noun]" or "[quantity]" + plural("[noun]") as
	 *         appropriate
	 */
	public static String quantityplnounWithHash(final int quantity, final String noun) {
		return quantityplnounWithMarker(quantity, noun, '#');
	}

	/**
	 * Returns either the plural or singular form of the given noun, depending on
	 * the quantity; also prefixes the quantity and prints the noun with a
	 * specifier prefix.
	 *
	 * @param quantity The quantity to examine
	 * @param noun The noun to examine
	 * @param marker The character use for the markup. '#' or '§'
	 * @return Either "[quantity] [noun]" or "[quantity]" + plural("[noun]") as
	 *         appropriate
	 */
	public static String quantityplnounWithMarker(int quantity, String noun, char marker) {
		final String fullNoun = plnoun(quantity, noun);
		String prefix = "";
		if (quantity > 0) {
			prefix = Integer.toString(quantity) + " ";
		}
		final StringBuilder sb = new StringBuilder(prefix);

		if (fullNoun.indexOf(' ') == -1) {
			sb.append(marker);
			sb.append(fullNoun);
		} else {
			sb.append(marker);
			sb.append("'" + fullNoun + "'");
		}

		return sb.toString();
	}

	/**
	 * Returns either the plural or singular form of the given noun, depending
	 * on the quantity; also prefixes the quantity as number string, if appropriate.
	 *
	 * @param quantity
	 *            The quantity to examine
	 * @param noun
	 *            The noun to examine
	 * @return Either "[quantity string] [noun]" or "[quantity string]" + plural("[noun]") as
	 *         appropriate
	 */
	public static String quantityNumberStrNoun(final int quantity, final String noun) {
		StringBuilder sb = new StringBuilder();

		switch(quantity) {
			case 0:
				sb.append("0 ");
				break;

			case 1:
				// skip quantity string
				break;

			default:
				sb.append(numberString(quantity)).append(' ');
				break;
		}

		sb.append(plnoun(quantity, noun));

		return sb.toString();
	}

	/**
	 * Is the character a vowel?
	 *
	 * @param c
	 *            The character to examine
	 * @return true if c is a vowel, false otherwise
	 */
	protected static boolean isVowel(final char c) {
		final char l = Character.toLowerCase(c);
		return ((l == 'a') || (l == 'e') || (l == 'i') || (l == 'o') || (l == 'u'));
	}

	/**
	 * Is the character a consonant?
	 *
	 * @param c
	 *            The character to examine
	 * @return true if c is a consonant, false otherwise
	 */
	protected static boolean isConsonant(final char c) {
		return !isVowel(c);
	}

	/**
	 * first, second, third, ...
	 *
	 * @param n
	 *            a number
	 * @return first, second, third, ...
	 */
	public static String ordered(final int n) {
		switch (n) {
		case 1:	return "pierwszy";
		case 2:	return "drugi";
		case 3:	return "trzeci";
		case 4:	return "czwarty";
		case 5:	return "piąty";
		case 6:	return "szósty";
		case 7:	return "siódmy";
		case 8:	return "ósmy";
		case 9:	return "dziewiąty";
		case 10:return "dziesiąty";
		default:
			if (n > 0) {
				return n + ordinalSuffix(n);
			}
			logger.error("Grammar.ordered not implemented for: " + n);
			return Integer.toString(n);
		}
	}

	/**
	 * Get ordinal suffix string corresponding to an integer.
	 *
	 * @param n integer whose ordinal's suffix is wanted
	 * @return ordinal suffix
	 */
	private static String ordinalSuffix(int n) {
		int penultimate = (n % 100) / 10;
		if (penultimate == 1) {
			return "th";
		}
		int last = n % 10;
		if (last == 1) {
			return "st";
		} else if (last == 2) {
			return "nd";
		} else if (last == 3) {
			return "rd";
		}
		return "th";
	}

	/**
	 * Helper function to nicely formulate an enumeration of a collection.
	 * <p>
	 * For example, for a collection containing the 3 elements x, y, z, returns the
	 * string "x, y, and z".
	 *
	 * @param collection
	 *            The collection whose elements should be enumerated
	 * @return A nice String representation of the collection
	 */
	public static String enumerateCollection(final Collection<String> collection) {
		return enumerateCollection(collection, "oraz");
	}

	/**
	 * Helper function to nicely formulate an enumeration of a collection.
	 * <p>
	 * For example, for a collection containing the 3 elements x, y, z, returns the
	 * string "x, y, and z".
	 *
	 * @param collection
	 *            The collection whose elements should be enumerated
	 * @param conjunction "and" or "or"
	 * @return A nice String representation of the collection
	 */
	public static String enumerateCollection(final Collection<String> collection, String conjunction) {
		if (collection == null) {
			return "";
		}
		final String[] elements = collection.toArray(new String[collection.size()]);
		String ret;

		if (elements.length == 0) {
			ret = "";
		} else if (elements.length == 1) {
			ret = quoteHash(elements[0]);
		} else if (elements.length == 2) {
			ret = quoteHash(elements[0]) + " " + conjunction + " " + quoteHash(elements[1]);
		} else {
			final StringBuilder sb = new StringBuilder();

			for(int i = 0; i < elements.length - 1; i++) {
				sb.append(quoteHash(elements[i]) + ", ");
			}
			sb.append(conjunction + " " + quoteHash(elements[elements.length - 1]));

			ret = sb.toString();
		}

		return replaceInternalByDisplayNames(ret);
	}

	/**
	 * Helper function to nicely formulate an enumeration of a collection,
	 * with hashes to colour the words.
	 * <p>
	 * For example, for a collection containing the 3 elements x, y, z, returns the
	 * string "#x, #y, and #z".
	 *
	 * @param collection
	 *            The collection whose elements should be enumerated
	 * @return A nice String representation of the collection with hashes
	 */
	public static String enumerateCollectionWithHash(final Collection<String> collection) {
		if (collection == null) {
			return "";
		}

		final List<String> result = new ArrayList<String>(collection.size());
		for (String entry : collection) {
			result.add("#" + entry);
		}

		return enumerateCollection(result);
	}

	/**
	 * To let the client display compound words like "#battle axe" in blue, we put the whole item name in quotes.
	 *
	 * @param str
	 * @return the hashed word
	 */
	public static String quoteHash(final String str) {
		if (str != null) {
			final int idx = str.indexOf('#');

			if ((idx != -1) && (str.indexOf(' ', idx) != -1) && (str.charAt(idx + 1) != '\'')) {
				return str.substring(0, idx) + "#'" + str.substring(idx + 1) + '\'';
			}
		}

		return str;
	}

	/**
	 * Converts numbers into their textual representation for clocks.
	 *
	 * @param n
	 *            a number
	 * @return one, two, three, ...
	 */
	public static String clockNumberString(final int n) {
		switch (n) {
		case 0:
			return "zerowa";
		case 1:
			return "pierwsza";
		case 2:
			return "druga";
		case 3:
			return "trzecia";
		case 4:
			return "czwarta";
		case 5:
			return "piąta";
		case 6:
			return "szósta";
		case 7:
			return "siódma";
		case 8:
			return "ósma";
		case 9:
			return "dziewiąta";
		case 10:
			return "dziesiąta";
		case 11:
			return "jedenasta";
		case 12:
			return "dwunasta";
		default:
			return Integer.toString(n);
		}
	}

	/**
	 * Converts numbers into their textual representation.
	 *
	 * @param n
	 *            a number
	 * @return one, two, three, ...
	 */
	public static String numberString(final int n) {
		switch (n) {
		case 0:
			return "zero";
		case 1:
			return "jeden";
		case 2:
			return "dwa";
		case 3:
			return "trzy";
		case 4:
			return "cztery";
		case 5:
			return "pięć";
		case 6:
			return "sześć";
		case 7:
			return "siedem";
		case 8:
			return "osiem";
		case 9:
			return "dziewięć";
		case 10:
			return "dziesięć";
		case 11:
			return "jedenaście";
		case 12:
			return "dwanaście";
		default:
			return Integer.toString(n);
		}
	}

	/**
	 * Interprets number texts.
	 *
	 * @param text
	 *            a number
	 * @return one, two, three, ...
	 */
	public static Integer number(final String text) {
		if (text.equals("no") || text.equals("zero")) {
			return 0;
		} else if (text.equals("one") || text.equals("jeden")) {
			return 1;
		} else if (text.equals("two") || text.equals("dwa")) {
			return 2;
		} else if (text.equals("three") || text.equals("trzy")) {
			return 3;
		} else if (text.equals("four") || text.equals("cztery")) {
			return 4;
		} else if (text.equals("five") || text.equals("pięć")) {
			return 5;
		} else if (text.equals("six") || text.equals("sześć")) {
			return 6;
		} else if (text.equals("seven") || text.equals("siedem")) {
			return 7;
		} else if (text.equals("eight") || text.equals("osiem")) {
			return 8;
		} else if (text.equals("nine") || text.equals("dziewięć")) {
			return 9;
		} else if (text.equals("ten") || text.equals("dziesięć")) {
			return 10;
		} else if (text.equals("eleven") || text.equals("jedenaście")) {
			return 11;
		} else if (text.equals("twelve") || text.equals("dwanaście")) {
			return 12;
		} else {
			// also handle "a dozen", ...
			return null;
		}
	}

	/**
	 * enumerates a collections using the plural forms.
	 *
	 * @param collection Collection
	 * @return enumeration using plural forms
	 */
	public static String enumerateCollectionPlural(Collection<String> collection) {
		Collection<String> pluralCollection = new ArrayList<String>(collection.size());
		for (String entry : collection) {
			pluralCollection.add(plural(entry));
		}
		return enumerateCollection(pluralCollection);
	}

	public static String genderVerb(String gender, final String word) {
		if (gender.equals("F")) {
			if (word.equals("mógł")) {
				return "mogła";
			}
			if (word.equals("powinieneś")) {
				return "powinnaś";
			}
			if (word.equals("go")) {
				return "jej";
			}

			if (word.endsWith("eś")) {
				return word.substring(0, word.length() - 2) + "aś";
			}
			if (word.endsWith("ósł")) {
				return word.substring(0, word.length() - 3) + "osła";
			}
			if (word.endsWith("ego")) {
				return word.substring(0, word.length() - 3) + "ą";
			}
			if (word.endsWith("y")) {
				return word.substring(0, word.length() - 1) + "a";
			}
			if (word.endsWith("łbyś")) {
				return word.substring(0, word.length() - 4) + "łabyś";
			}
			if (word.endsWith("ął")) {
				return word.substring(0, word.length() - 2) + "ęła";
			}

			return word + "a";
		}
		return word;
	}

	public static String genderNouns(String noun, final String word) {
		if (noun.endsWith("zwiadowca") || noun.endsWith("kawalerzysta") || noun.endsWith("morderca")
				|| noun.equals("mefisto") || noun.startsWith("szkielet")) {
			return word;
		}

		if (noun.endsWith("a") || noun.startsWith("sztabka") || noun.startsWith("bryłka") || noun.startsWith("złota") || noun.startsWith("złote") || noun.startsWith("czarna")
				|| noun.startsWith("chmara") || noun.startsWith("panna") || noun.startsWith("mysz") || noun.startsWith("królowa") || noun.startsWith("gigantyczna")) {
			if (word.endsWith("ój")) {
				return word.substring(0, word.length() - 2) + "oja";
			}
			if (word.endsWith("y")) {
				return word.substring(0, word.length() - 1) + "a";
			}

			return word + "a";
		}

		if (noun.endsWith("e")) {
			if (word.endsWith("ój")) {
				return word.substring(0, word.length() - 2) + "oje";
			}
			if (word.endsWith("y")) {
				return word.substring(0, word.length() - 1) + "e";
			}

			return word + "y";
		}

		if (noun.endsWith("o") || noun.equals("oczko") || noun.equals("oko starsze") || noun.equals("coś")) {
			if (word.endsWith("ój")) {
				return word.substring(0, word.length() - 2) + "oje";
			}
			if (word.endsWith("y")) {
				return word.substring(0, word.length() - 1) + "e";
			}

			return word + "o";
		}

		return word;
	}
}
