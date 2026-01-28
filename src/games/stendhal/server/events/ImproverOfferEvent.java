/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2024 - Stendhal                        *
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

import org.apache.log4j.Logger;

import marauroa.common.game.Definition;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.SyntaxException;

/**
 * Sends structured improver offer data to the client UI.
 */
public class ImproverOfferEvent extends RPEvent {
	private static final String RPCLASS_NAME = "improver_offer";
	private static final String PAYLOAD = "payload";

	private static final Logger logger = Logger.getLogger(ImproverOfferEvent.class);

	/**
	 * Creates the rpclass.
	 */
	public static void generateRPClass() {
		try {
			final RPClass rpclass = new RPClass(RPCLASS_NAME);
			rpclass.add(DefinitionClass.ATTRIBUTE, PAYLOAD, Type.STRING, Definition.PRIVATE);
		} catch (final SyntaxException e) {
			logger.error("cannot generateRPClass", e);
		}
	}

	public ImproverOfferEvent(final String payload) {
		super(RPCLASS_NAME);
		if (payload != null) {
			super.put(PAYLOAD, payload);
		}
	}
}
