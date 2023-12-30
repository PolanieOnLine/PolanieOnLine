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
		} else if (str.startsWith("rękawic") || str.startsWith("spodni")) {
			str = addPrefixIfNotAlreadyThere(lowString, "parę ", "par ");
		} else if (str.startsWith("szynki") || str.startsWith("mięsa")) {
			str = addPrefixIfNotAlreadyThere(lowString, "kawałek ", "kawałków ");
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
		return str.replace("icecream", "ice cream");
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

		if (enoun.split(" ").length > 1) {
			return plural(enoun.split(" "));
		}

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

		} else if (enoun.equals("money") || enoun.equals("kierpce") || enoun.equals("korale") || enoun.endsWith("ów") || enoun.endsWith("ami")) {
			return enoun;
		} else if (enoun.startsWith("rękawice") || enoun.equals("spodnie")) {
			return enoun.substring(0, enoun.length() - 1) + postfix;
		} else if (enoun.startsWith("magia")) {
			return enoun.substring(0, enoun.length() - 1) + "i" + postfix;
		} else if (enoun.startsWith("broń")) {
			return enoun.substring(0, enoun.length() - 1) + "ni" + postfix;
		} else if (enoun.startsWith("płaszcz")) {
			return enoun + "y" + postfix;
		} else if (enoun.equals("wojownik") || enoun.equals("przedmiot")) {
			return enoun + "ów" + postfix;
		} else if (enoun.startsWith("sok")) {
			return enoun + "i" + postfix;
		} else if (enoun.equals("miecz")) {
			return enoun + "e" + postfix;

		} else if (enoun.equals("dzień")) {
			return enoun.substring(0, enoun.length() - 4) + "ni" + postfix;
		} else if (enoun.equals("tydzień")) {
			return enoun.substring(0, enoun.length() - 5) + "godni" + postfix;

		} else if (enoun.endsWith("dę") || enoun.endsWith("tę") || enoun.endsWith("nę")) {
			return enoun.substring(0, enoun.length() - 1) + "y" + postfix;

		} else if (enoun.endsWith("owy") || enoun.endsWith("ki")) {
			return enoun + "ch" + postfix;

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
				|| enoun.endsWith("la") || enoun.endsWith("ża") || enoun.endsWith("rza") || enoun.endsWith("ta")) {
			return enoun.substring(0, enoun.length() - 1) + "e" + postfix;

		} else if (enoun.endsWith("a")) {
			return enoun.substring(0, enoun.length() - 1) + "y" + postfix;

		} else if (enoun.endsWith("ek")) {
			return enoun.substring(0, enoun.length() - 2) + "ki" + postfix;

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

	public static String plural(String[] noun) {
		if (noun == null) {
			return null;
		}

		if (noun.length == 2) {
			if (noun[0].startsWith("zatruta")) {
				noun[0] = noun[0].substring(0, noun[0].length() - 1) + "e";
			}
			if (noun[1].startsWith("strzała")) {
				noun[1] = noun[1].substring(0, noun[1].length() - 1) + "y";
			}
			return noun[0] + " " + noun[1];
		} else if (noun.length == 3) {
			if (noun[0].startsWith("napój")) {
				noun[0] = noun[0].substring(0, noun[0].length() - 2) + "ojów";
			}
			return noun[0] + " " + noun[1] + " " + noun[2];
		} else if (noun.length == 4) {
			return noun[0] + " " + noun[1] + " " + noun[2] + " " + noun[3];
		} else {
			return plural(noun[0]);
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
		} else if (enoun.equals("mnich")) {
			return enoun + "a";

		} else if (enoun.endsWith("iki")) {
			return enoun.substring(0, enoun.length() - 1) + postfix;

		} else if (enoun.startsWith("rękawice") || enoun.endsWith("czy")) {
			return enoun.substring(0, enoun.length() - 1) + postfix;
		} else if (enoun.endsWith("ów")) {
			return enoun.substring(0, enoun.length() - 2) + postfix;
		} else if (enoun.endsWith("tę") || enoun.endsWith("ło")) {
			return enoun.substring(0, enoun.length() - 1) + "a" + postfix;
		} else if (enoun.endsWith("na")) {
			return enoun.substring(0, enoun.length() - 1) + "e" + postfix;
		} else {
			return enoun + postfix;
		}
	}

	public static String youryour(int quantity, String noun) {
		if (noun.equals("pączek") || noun.equals("chleb")) {
			return plnoun(quantity, "twój");
		} else {
			return plnoun(quantity, "twoją");
		}
	}

	public static String singular(int quantity, String enoun) {
		if (enoun == null) {
			return null;
		}
		String postfix = "";

		final int position = enoun.indexOf('+');
		if (position != -1) {
			postfix = enoun.substring(position - 1);
			enoun = enoun.substring(0, position - 1);
		}

		String[] noun = enoun.split(" ");
		if (noun.length == 2) {
			if (quantity > 4) {
				if (noun[0].startsWith("zatruta")) {
					noun[0] = noun[0].substring(0, noun[0].length() - 1) + "ych";
				}
				if (noun[1].startsWith("strzała")) {
					noun[1] = noun[1].substring(0, noun[1].length() - 1);
				}
			}
			return noun[0] + " " + noun[1];
		}

		// in "of"-phrases build only the singular of the first part
		if (enoun.indexOf(of) > -1) {
			return singular(enoun.substring(0, enoun.indexOf(of)))
					+ enoun.substring(enoun.indexOf(of)) + postfix;

		} else if (enoun.equals("będzie")) {
			if (quantity > 1) {
				return enoun = "będą";
			} else {
				return enoun;
			}
		} else if (enoun.equals("płaszcz")) {
			if (quantity <= 4) {
				return enoun + "e" + postfix;
			} else {
				return enoun + "y" + postfix;
			}
		} else if (enoun.equals("lazurowy")) {
			if (quantity <= 4) {
				return enoun.substring(0, enoun.length() - 1) + "e" + postfix;
			} else {
				return enoun + "ch" + postfix;
			}
		} else if (enoun.equals("borowik")) {
			if (quantity <= 4) {
				return enoun + "i" + postfix;
			} else {
				return enoun + "ów" + postfix;
			}
		} else if (enoun.equals("lody") || enoun.equals("składniki")) {
			if (quantity > 4 || quantity == 0) {
				return enoun.substring(0, enoun.length() - 1) + "ów" + postfix;
			} else {
				return enoun + postfix;
			}
		} else if (enoun.equals("mnich")) {
			if (quantity > 1 || quantity == 0) {
				return enoun + "ów" + postfix;
			} else {
				return enoun;
			}
		} else if (enoun.equals("strzała") || enoun.equals("trucizna")) {
			if (quantity > 4) {
				return enoun.substring(0, enoun.length() - 1) + postfix;
			} else {
				return plural(enoun);
			}

		} else if (enoun.endsWith("ty") || enoun.endsWith("ry") || enoun.endsWith("wy")) {
			if (quantity > 1) {
				return enoun.substring(0, enoun.length() - 1) + "e" + postfix;
			} else {
				return enoun;
			}
		} else if (enoun.endsWith("ój")) {
			if (quantity > 1) {
				return enoun.substring(0, enoun.length() - 2) + "oje" + postfix;
			} else {
				return enoun;
			}
		} else if (enoun.endsWith("oją")) {
			if (quantity > 1) {
				return enoun.substring(0, enoun.length() - 2) + "je" + postfix;
			} else {
				return enoun;
			}

		} else if (enoun.endsWith("tę")) {
			if (quantity >= 5) {
				return enoun.substring(0, enoun.length() - 1) + postfix;
			} else if (quantity <= 4) {
				return enoun.substring(0, enoun.length() - 1) + "y" + postfix;
			} else {
				return enoun;
			}

		} else if (enoun.endsWith("ło")) {
			if (quantity >= 5) {
				return enoun + postfix;
			} else if (quantity <= 4) {
				return enoun.substring(0, enoun.length() - 1) + "y" + postfix;
			} else {
				return enoun;
			}

		} else if (enoun.endsWith("czy")) {
			if (quantity <= 4) {
				return enoun.substring(0, enoun.length() - 1) + "e" + postfix;
			} else {
				return enoun;
			}

		} else {
			return plural(enoun);
		}
	}

	public static String alternativeSingular(int quantity, String enoun) {
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

		} else if (enoun.equals("pączek")) {
			if (quantity > 1) {
				return enoun.substring(0, enoun.length() - 2) + "kami" + postfix;
			} else {
				return enoun.substring(0, enoun.length() - 2) + "kiem" + postfix;
			}
		} else if (enoun.equals("lina")) {
			if (quantity == 1) {
				return enoun.substring(0, enoun.length() - 1) + "ą" + postfix;
			} else {
				return singular(quantity, enoun);
			}
		} else if (enoun.equals("chleb") || enoun.equals("ametyst")) {
			if (quantity > 1) {
				return enoun + "ami" + postfix;
			} else {
				return enoun + "em" + postfix;
			}

		} else {
			return singular(quantity, enoun);
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
			return singular(quantity, enoun);
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

		if (quantity == 1) {
			return end;
		} else {
			return Integer.toString(quantity) + " " + end;
		}
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
			pluralCollection.add(entry); // plural(entry)
		}
		return enumerateCollection(pluralCollection);
	}

	public static String genderVerb(String gender, final String word) {
		if (gender.equals("F")) {
			if (word.equals("mógł")) {
				return "mogła";
			}
			if (word.equals("mógłbym")) {
				return "mogłabym";
			}
			if (word.equals("powinieneś")) {
				return "powinnaś";
			}
			if (word.equals("powinienem")) {
				return "powinnam";
			}
			if (word.equals("go")) {
				return "jej";
			}
			if (word.equals("narzeczonego")) {
				return "narzeczonej";
			}
			if (word.equals("chłop")) {
				return "chłopka";
			}
			if (word.equals("kmieć")) {
				return "kmiotka";
			}
			if (word.equals("mieszczanin")) {
				return "miszczanka";
			}
			if (word.equals("szlachcic")) {
				return "szlachcianka";
			}
			if (word.equals("rycerz") || word.equals("baronet")) {
				return word;
			}
			if (word.equals("baron")) {
				return "baronessa";
			}
			if (word.equals("wicehrabia")) {
				return "wicehrabina";
			}
			if (word.equals("hrabia")) {
				return "hrabina";
			}
			if (word.equals("magnat")) {
				return "magnatka";
			}
			if (word.equals("książe")) {
				return "księżniczka";
			}
			if (word.equals("król")) {
				return "królowa";
			}

			if (word.endsWith("ąłeś")) {
				return word.substring(0, word.length() - 4) + "ęłaś";
			}
			if (word.endsWith("eś")) {
				return word.substring(0, word.length() - 2) + "aś";
			}
			if (word.endsWith("ósł")) {
				return word.substring(0, word.length() - 3) + "osła";
			}
			if (word.endsWith("ódł")) {
				return word.substring(0, word.length() - 3) + "odła";
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
			if (word.endsWith("łem")) {
				return word.substring(0, word.length() - 2) + "am";
			}

			return word + "a";
		}
		return word;
	}

	public static String genderNouns(String noun, final String word) {
		if (noun.endsWith("zwiadowca") || noun.endsWith("kawalerzysta") || noun.endsWith("morderca")
				|| noun.equals("mefisto") || noun.startsWith("szkielet") || noun.startsWith("zgniły")) {
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

	/**
	 * Transforms the first letter of string to uppercase
	 * and leaves the rest of the string the same.
	 *
	 * @param str
	 * 		A string to capitalize.
	 * @return
	 * 		If <code>true</code> return capitalized word.
	 * 		If <code>false</code> if string is null or empty, the above code throws an exception.
	 */
	public static String capitalize(String str) {
	    if(str == null || str.isEmpty()) {
	        return str;
	    }

	    return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	public static String variation(String word) {
		if (word.equals("stokrotki")) {
			return word.substring(0, word.length() - 2) + "ek";
		}

		if (word.equals("bratek")) {
			return word.substring(0, word.length() - 2) + "ów";
		}

		if (word.equals("lilia")) {
			return word.substring(0, word.length() - 1) + "i";
		}

		if (word.equals("bielikrasa")) {
			return word.substring(0, word.length() - 1) + "y";
		}

		return word;
	}
}
