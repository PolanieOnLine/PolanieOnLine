/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableSet;

/**
 * Common phrases used by players to interact with a SpeakerNPC.
 *
 * @author hendrik
 */
public class ConversationPhrases {
	// define "no" trigger to be exactly matched while ignoring case
	// (for available matching option strings see ExpressionMatcher)
	public static final String NO_EXPRESSION = "|EXACT|ICASE|no";

	// do not use a mutable list here
	public static final List<String> EMPTY = Arrays.asList(new String[0]);

	private static final List<String> EN_GREETING = Arrays.asList(
			"hello", "hallo", "hi", "greetings", "hola", "👋");
	private static final List<String> PL_GREETING = Arrays.asList(
			"witaj", "witam", "cześć", "dzień dobry", "dobry wieczór",
			"siema", "siemaneczko", "elo", "eluwina");
	public static final List<String> GREETING_MESSAGES = combine(EN_GREETING, PL_GREETING);

	private static final List<String> EN_JOB = Arrays.asList(
			"job", "work", "occupation", "🧹");
	private static final List<String> PL_JOB = Arrays.asList(
			"praca", "zajęcie", "zawód");
	public static final List<String> JOB_MESSAGES = combine(EN_JOB, PL_JOB);

	private static final List<String> EN_HELP = Arrays.asList(
			"help", "ayuda", "❓");
	private static final List<String> PL_HELP = Arrays.asList(
			"pomoc", "pomocy", "pomóc", "pomagam", "pomożesz");
	public static final List<String> HELP_MESSAGES = combine(EN_HELP, PL_HELP);

	private static final List<String> EN_QUEST = Arrays.asList(
			"task", "quest", "favor", "favour", "❗️");
	private static final List<String> PL_QUEST = Arrays.asList(
			"zadanie", "misja", "przysługa");
	public static final List<String> QUEST_MESSAGES = combine(EN_QUEST, PL_QUEST);

	public static final List<String> BEGIN_MESSAGES = Arrays.asList("begin", "start", "go", "zacznij", "rozpocznij");

	private static final List<String> EN_FINISH = Arrays.asList(
			"done", "finish", "complete", "‼️");
	private static final List<String> PL_FINISH = Arrays.asList(
			"zrobione", "skończone", "zakończone", "ukończone", "załatwione");
	public static final List<String> FINISH_MESSAGES = combine(EN_FINISH, PL_FINISH);

	public static final List<String> QUEST_FINISH_MESSAGES = combine(QUEST_MESSAGES, FINISH_MESSAGES);

	private static final List<String> EN_ABORT = Arrays.asList(
			"another", "abort");
	private static final List<String> PL_ABORT = Arrays.asList(
			"inny", "przerwij");
	public static final List<String> ABORT_MESSAGES = combine(EN_ABORT, PL_ABORT);

	private static final List<String> EN_OFFER = Arrays.asList(
			"offer", "deal", "trade", "🪙");
	private static final List<String> PL_OFFER = Arrays.asList(
			"oferta", "transakcja", "interes", "handel");
	public static final List<String> OFFER_MESSAGES = combine(EN_OFFER, PL_OFFER);

	public static final List<String> YES_MESSAGES = Arrays.asList("yes", "ok", "yep", "sure",
			"tak", "dobrze", "oczywiście", "oczywiscie", "👍");

	public static final List<String> NO_MESSAGES = Arrays.asList(NO_EXPRESSION, "nope",
			"nothing", "none", "no", "nie", "nic", "👎");

	public static final List<String> GOODBYE_MESSAGES = Arrays.asList("bye", "goodbye",
			"farewell", "cya", "adios", "do widzenia", "żegnaj", "zegnaj", "bywaj",
			"nara", "tymczasem", "dobranoc", "na razie", "do jutra", "👋");

	public static final List<String> PURCHASE_MESSAGES = Arrays.asList("buy", "purchase", "kup",
			"kupię", "kupie", "kupno", "kupić", "kupic", "kupować", "kupowac", "zakup",
			"zakupię", "zakupie", "zakupić", "zakupic");

	public static final List<String> SALES_MESSAGES = Arrays.asList("sell", "sales", "sprzedaż",
			"sprzedaz", "sprzedam");

	public static final ImmutableSet<String> KNOWN = ImmutableSet.of("witaj", "pomocy", "praca", 
			"zadanie", "zrobione", "ukończone", "inne", "oferta", "tak", "nie", "bywaj", "kupię",
			"sprzedam", "ulecz", "wyzwanie");

	/**
	 * Combine a string collection (list) with additional strings.
	 *
	 * @param list first collection of strings
	 * @param args additional strings
	 * @return new list with the contents of the list and all the additional
	 * 	strings
	 */
	public static final List<String> combine(Collection<String> list, String ...args) {
		List<String> ret = new ArrayList<String>(list);

		for(String s : args) {
			ret.add(s);
		}

		return ret;
	}

	/**
	 * Combine a string collection with other collections.
	 *
	 * @param list1 first collection
	 * @param lists additional collections
	 * @return a new list with contents of all the collections
	 */
	@SafeVarargs
	public static final List<String> combine(Collection<String> list1, Collection<String>... lists) {
		List<String> ret = new LinkedList<String>(list1);
		for (Collection<String> list : lists) {
			ret.addAll(list);
		}
		Collections.sort(ret);
		return ret;
	}

}
