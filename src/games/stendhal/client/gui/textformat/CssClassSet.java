/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.textformat;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A {\@link FormatSet} implementation storing CSS class names.
 */
public class CssClassSet implements FormatSet<Set<String>, CssClassSet> {
	private final LinkedHashSet<String> classes;

	public CssClassSet() {
		this.classes = new LinkedHashSet<String>();
	}

	private CssClassSet(final Set<String> source) {
		this.classes = new LinkedHashSet<String>(source);
	}

	@Override
	public CssClassSet union(final CssClassSet additional) {
		final CssClassSet copy = copy();
		if ((additional != null) && (additional.classes != null)) {
			copy.classes.addAll(additional.classes);
		}
		return copy;
	}

	@Override
	public CssClassSet copy() {
		return new CssClassSet(classes);
	}

	@Override
	public Set<String> contents() {
		return classes;
	}

	/**
	 * Add a CSS class name to this set.
	 *
	 * @param cssClass
	 *            class name
	 * @return this instance for chaining
	 */
	public CssClassSet addClass(final String cssClass) {
		if ((cssClass != null) && !cssClass.isEmpty()) {
			classes.add(cssClass);
		}
		return this;
	}

	/**
	 * Determine whether the set contains any classes.
	 *
	 * @return {\@code true} if no classes are defined
	 */
	public boolean isEmpty() {
		return classes.isEmpty();
	}

	/**
	 * Concatenate the stored class names into a single string suitable for an
	 * HTML {\@code class} attribute.
	 *
	 * @return space separated class list
	 */
	public String classString() {
		if (classes.isEmpty()) {
			return "";
		}
		final StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (final String cssClass : classes) {
			if (!first) {
				builder.append(' ');
			}
			builder.append(cssClass);
			first = false;
		}
		return builder.toString();
	}
}
