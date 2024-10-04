/***************************************************************************
 *                   (C) Copyright 2023-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.events;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import games.stendhal.common.constants.Events;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Transition;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;

public class ChatOptionsEvent extends RPEvent {

	private static final String NPC = "npc";
	private static final String TITLE = "title";
	private static final String OPTIONS = "options";

	private static class ChatOption implements Comparable<ChatOption> {
		private static final Map<String, Integer> SORT_INDEX = new HashMap<>();
		static {
			SORT_INDEX.put("Witaj", 1);
			SORT_INDEX.put("Tak", 11);
			SORT_INDEX.put("Nie", 12);
			SORT_INDEX.put("Inne", 21);
			SORT_INDEX.put("Zrobione", 22);
			SORT_INDEX.put("Ukończone", 23);
			SORT_INDEX.put("Zadanie", 24);
			SORT_INDEX.put("Wyzwanie", 25);
			SORT_INDEX.put("Pomocy", 31);
			SORT_INDEX.put("Praca", 32);
			SORT_INDEX.put("Oferta", 33);
			SORT_INDEX.put("Kupię ...", 34);
			SORT_INDEX.put("Sprzedam ...", 35);
			SORT_INDEX.put("Ulecz", 41);
			SORT_INDEX.put("Bywaj", 9999);
		}

		// common triggers for merchants & producers
		private static final List<String> merchantActivities = new LinkedList<>(Arrays.asList("buy", "sell", "repair"));

		private String trigger;
		private String label;
		private String options = "";

		ChatOption(String trigger) {
			this.trigger = trigger.toLowerCase(Locale.ENGLISH);

			String label = Grammar.makeUpperCaseWord(trigger);
			String options = "";

			if (merchantActivities.contains(trigger) || trigger.equals("")) {
				label = label + " ...";
				options = "params";
			}

			this.label = label;
			this.options = options;
		}

		String serialize() {
			return trigger + "|~|" + label + "|~|" + options;
		}

		@Override
		public int compareTo(ChatOption o) {
			Integer value1 = SORT_INDEX.get(label);
			Integer value2 = SORT_INDEX.get(o.label);
			if (value1 == null) {
				value1 = Integer.valueOf(1000);
			}
			if (value2 == null) {
				value2 = Integer.valueOf(1000);
			}
			if (value1.equals(value2)) {
				return this.label.compareTo(o.label);
			}
			return value1 - value2;
		}

	}

	public ChatOptionsEvent(SpeakerNPC npc, Player player, ConversationStates currentState) {
		super(Events.CHAT_OPTIONS);
		put(NPC, npc.getName());
		final String title = npc.getTitle();
		if (title != null) {
			put(TITLE, title);
		}

		TreeSet<ChatOption> chatOptions = buildChatOptions(npc, player, currentState);
		put(OPTIONS, Joiner.on("\t").join(Iterables.transform(chatOptions, new Function<ChatOption, String>() {

			@Override
			public String apply(ChatOption arg) {
				return arg.serialize();
			}
		}
		)));
	}

	/**
	 * builds a list of the available chat options
	 *
	 * @param npc SpeakerNPC the player is talking to
	 * @param player player (to check conditions)
	 * @param currentState current state of the SpeakerNPC's state machine
	 * @return list of chat options
	 */
	private TreeSet<ChatOption> buildChatOptions(SpeakerNPC npc, Player player, ConversationStates currentState) {
		TreeSet<ChatOption> res = new TreeSet<>();
		Sentence sentence = ConversationParser.parse("");

		final List<Transition> transitions = npc.getTransitions();
		for (final Transition transition : transitions) {
			if (transition.getState() != currentState) {
				continue;
			}
			processTransition(npc, player, res, sentence, transition);
		}

		if (npc.getAttending() instanceof Player) {
			final Set<String> temp = new HashSet<>();
			temp.addAll(npc.getForcedWordsInCurrentConversation());
			temp.addAll(npc.getKnownChatOptions());
			for (final String trigger: temp) {
				final ChatOption copt = new ChatOption(trigger);
				if (!res.contains(copt)) {
					res.add(copt);
				}
			}
		}

		if (currentState != ConversationStates.IDLE) {
			for (final Transition transition : transitions) {
				if (transition.getState() != ConversationStates.ANY) {
					continue;
				}
				processTransition(npc, player, res, sentence, transition);
			}
		}
		return res;
	}

	private void processTransition(SpeakerNPC npc, Player player, TreeSet<ChatOption> res, Sentence sentence,
			final Transition transition) {
		for(Expression expr : transition.getTriggers()) {
			if (transition.getCondition() != null) {
				if (!transition.getCondition().fire(player, sentence, npc)) {
					continue;
				}
			}

			String trigger = expr.getNormalized().toLowerCase(Locale.ENGLISH);
			ChatOption option = new ChatOption(trigger);
			if (ConversationPhrases.KNOWN.contains(trigger)
					|| npc.hasLearnedWordInCurrentConversation(trigger)
					|| npc.hasLearnedWordInCurrentConversation(Grammar.plural(trigger))) {
				res.add(option);
			}
		}
	}

	/**
	 * Creates the rpclass.
	 */
	public static void generateRPClass() {
		final RPClass rpclass = new RPClass(Events.CHAT_OPTIONS);
		rpclass.addAttribute(NPC, Type.STRING);
		rpclass.addAttribute(TITLE, Type.STRING);
		rpclass.addAttribute(OPTIONS, Type.VERY_LONG_STRING);
	}

	/**
	 * Add a word or phrase to list of known merchant/producer activities.
	 */
	public static void addMerchantActivity(final String activity) {
		if (ChatOption.merchantActivities.contains(activity)) {
			return;
		}
		ChatOption.merchantActivities.add(activity);
		// XXX: should it also be added to ChatOption.SORT_INDEX?
	}
}