/***************************************************************************
 *                    (C) Copyright 2007-2010 - Stendhal                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************
 */
package games.stendhal.server.entity.mapstuff.sign;

import java.util.List;

import games.stendhal.server.core.engine.dbcommand.ReadCharactersFromHallOfFameCommand;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.db.command.ResultHandle;

/**
 * loads the content of a sign from the hall of fame.
 *
 * @author hendrik
 */
public class SignFromHallOfFameLoader implements TurnListener {
	private Sign sign;
	private String introduction;
	private ResultHandle handle;
	private boolean lineBreaks;
	private boolean numbered;


	/**
	 * creates a new SignFromHallOfFameLoader
	 *
	 * @param sign the sign to modify
	 * @param introduction introduction for the sign
	 * @param fametype type of fame
	 * @param max maximum number of returned characters
	 * @param ascending sort ascending or descending
	 * @param lineBreaks should each player be put on a line by itself?
	 */
	public SignFromHallOfFameLoader(Sign sign, String introduction, String fametype, int max, boolean ascending, boolean lineBreaks) {
		this(sign, introduction, fametype, max, ascending, lineBreaks, false);
	}

	/**
	 * Creates a loader with optional place numbers on line-separated rankings.
	 * Existing signs retain their original format unless numbering is requested.
	 *
	 * @param numbered prefix each line with its place, starting at one
	 */
	public SignFromHallOfFameLoader(Sign sign, String introduction, String fametype,
			int max, boolean ascending, boolean lineBreaks, boolean numbered) {
		this.sign = sign;
		this.introduction = introduction;
		this.handle = new ResultHandle();
		this.lineBreaks = lineBreaks;
		this.numbered = numbered;
		DBCommand command = new ReadCharactersFromHallOfFameCommand(fametype, max, ascending);
		DBCommandQueue.get().enqueueAndAwaitResult(command, handle);
	}

	@Override
	public void onTurnReached(int currentTurn) {
		// if there is no result, wait some more
		ReadCharactersFromHallOfFameCommand command = DBCommandQueue.get().getOneResult(ReadCharactersFromHallOfFameCommand.class, handle);
		if (command == null) {
			TurnNotifier.get().notifyInTurns(0, this);
			return;
		}

		// update the sign
		List<String> players = command.getNames();

		sign.setText(introduction + formatPlayers(players, lineBreaks, numbered));
		sign.notifyWorldAboutChanges();
	}

	static String formatPlayers(final List<String> players, final boolean lineBreaks,
			final boolean numbered) {
		if (!lineBreaks) {
			return players.toString();
		}
		final StringBuilder builder = new StringBuilder();
		for (int index = 0; index < players.size(); index++) {
			if (index > 0) {
				builder.append('\n');
			}
			if (numbered) {
				builder.append(index + 1).append(". ");
			}
			builder.append(players.get(index));
		}
		return builder.toString();
	}

}
