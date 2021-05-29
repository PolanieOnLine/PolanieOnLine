/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.core.rule.defaultruleset.DefaultAchievement;

public class AchievementsXMLLoader extends DefaultHandler {
	/** the logger instance. */
	private static final Logger LOGGER = Logger.getLogger(AchievementsXMLLoader.class);

	private String clazzname;

	private String subclass;

	private String identifier;
	private String title;
	private String description;
	private boolean active;

	private Category category;

	private String text;

	private boolean attributesTag;

	private int baseScore;
	private String condition;

	private List<DefaultAchievement> list;

	/** Attributes of the achievement */
	private Map<String, String> attributes;

	public List<DefaultAchievement> load(final URI uri) throws SAXException {
		list = new LinkedList<DefaultAchievement>();
		// Use the default (non-validating) parser
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			// Parse the input
			final SAXParser saxParser = factory.newSAXParser();

			final InputStream is = AchievementsXMLLoader.class.getResourceAsStream(uri.getPath());

			if (is == null) {
				throw new FileNotFoundException("cannot find resource '" + uri
						+ "' in classpath");
			}
			try {
				saxParser.parse(is, this);
			} finally {
				is.close();
			}
		} catch (final ParserConfigurationException t) {
			LOGGER.error(t);
		} catch (final IOException e) {
			LOGGER.error(e);
			throw new SAXException(e);
		}

		return list;
	}

	@Override
	public void startDocument() {
		// do nothing
	}

	@Override
	public void endDocument() {
		// do nothing
	}

	@Override
	public void startElement(final String namespaceURI, final String lName, final String qName,
			final Attributes attrs) {
		text = "";
		if (qName.equals("type")) {
			subclass = attrs.getValue("subclass");
		}

		if (qName.equals("implementation")) {
			clazzname = attrs.getValue("class-name");
		}

		if (qName.equals("achievement")) {
			identifier = attrs.getValue("id");
			title = attrs.getValue("title");
			description = attrs.getValue("description");
			active = Boolean.parseBoolean(attrs.getValue("active"));

			attributes = new LinkedHashMap<String, String>();
		} else if (qName.equals("attributes")) {
			attributesTag = true;
		} else if (attributesTag && qName.equals("score")) {
			baseScore = Integer.parseInt(attrs.getValue("value"));
		} else if (attributesTag && qName.equals("condition")) {
			condition = attrs.getValue("value");
		}
	}

	@Override
	public void endElement(final String namespaceURI, final String sName, final String qName) {
		if (qName.equals("achievement")) {
			final DefaultAchievement achievement = new DefaultAchievement(identifier, title, category, description, baseScore, active, condition);
//			achievement.setClassName(clazzname);
//			achievement.setTitle(title);
//			achievement.setDescription(description);
//			achievement.setAttributes(attributes);
//			achievement.setCondition(condition);
//			achievement.setScore(score);
			list.add(achievement);
		} else if (qName.equals("attributes")) {
			attributesTag = false;
		}
	}

	@Override
	public void characters(final char[] buf, final int offset, final int len) {
		text = text + (new String(buf, offset, len)).trim();
	}
}