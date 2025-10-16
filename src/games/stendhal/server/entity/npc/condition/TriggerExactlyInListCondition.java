/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.condition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import games.stendhal.common.parser.ConvCtxForMatchingSource;
import games.stendhal.common.parser.ConversationContext;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Sentence;
import games.stendhal.common.parser.SimilarExprMatcher;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Was one of these trigger phrases said exactly ignoring case? (Use with a ""-trigger in npc.add)
 */
@Dev(category=Category.CHAT, label="\"\"?")
public class TriggerExactlyInListCondition implements ChatCondition {
	private static final ConversationContext CONVERSION_CONTEXT = new ConvCtxForMatchingSource();
	private static final Set<String> LEADING_CONNECTORS = new HashSet<String>(Arrays.asList("and", "or", "i"));

	private final List<Sentence> triggers = new LinkedList<Sentence>();
	private final List<TriggerText> triggerTexts = new LinkedList<TriggerText>();

	private static final class TriggerText {
		private final String normalized;
		private final String stripped;

		TriggerText(String normalized, String stripped) {
			this.normalized = normalized;
			this.stripped = stripped;
		}
	}

	/**
	 * Creates a new TriggerExactlyInListCondition.
	 *
	 * @param trigger list of triggers
	 */
	public TriggerExactlyInListCondition(final String... trigger) {
		this(Arrays.asList(trigger));
	}

	/**
	 * Creates a new TriggerExactlyInListCondition.
	 *
	 * @param triggers list of triggers
	 */
	@Dev()
	public TriggerExactlyInListCondition(final List<String> triggers) {
		SimilarExprMatcher matcher = new SimilarExprMatcher();
		for (String trigger : triggers) {
			final Sentence expected = ConversationParser.parse(trigger, matcher);
			this.triggers.add(expected);
			String normalized = normalizeText(trigger);
			this.triggerTexts.add(new TriggerText(normalized, stripLeadingConnector(normalized)));
		}
	}

	@Override
	public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
		final String originalText = sentence.getOriginalText();
		final String normalizedOriginal = normalizeText(originalText);
		final String strippedOriginal = stripLeadingConnector(normalizedOriginal);
		final Sentence answer = ConversationParser.parse(originalText, CONVERSION_CONTEXT);
		int index = 0;
		for (Sentence trigger : triggers) {
			if (answer.matchesFull(trigger)) {
				return true;
			}
			TriggerText expected = triggerTexts.get(index);
			if (normalizedOriginal != null && normalizedOriginal.equals(expected.normalized)) {
				return true;
			}
			if (strippedOriginal != null && strippedOriginal.equals(expected.stripped)) {
				return true;
			}
			index++;
		}
		return false;
	}

	private static String normalizeText(String text) {
		if (text == null) {
			return null;
		}
		return text.trim().toLowerCase(Locale.ENGLISH);
	}

	private static String stripLeadingConnector(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}
		for (String prefix : LEADING_CONNECTORS) {
			if (text.startsWith(prefix + " ")) {
				return text.substring(prefix.length()).trim();
			}
		}
		return text;
	}

	@Override
	public String toString() {
		return "trigger exactly <" + triggers.toString() + ">";
	}

	@Override
	public int hashCode() {
		return 5009 * triggers.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof TriggerExactlyInListCondition)) {
			return false;
		}
		TriggerExactlyInListCondition other = (TriggerExactlyInListCondition) obj;
		return triggers.equals(other.triggers);
	}
}
