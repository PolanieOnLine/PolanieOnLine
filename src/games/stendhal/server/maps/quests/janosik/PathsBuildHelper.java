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
package games.stendhal.server.maps.quests.janosik;

import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.RPZonePath;

public class PathsBuildHelper {

	/**
	 * route for pied piper incoming
	 * @return - incoming path
	 */
	public static List<RPZonePath> getZakopaneIncomingPath() {
		final List<RPZonePath> fullPath =
			new LinkedList<RPZonePath>();

		final List<Node> localroute = new LinkedList<Node>();

		localroute.add(new Node(26,0));
		localroute.add(new Node(26,4));
		localroute.add(new Node(38,4));
		localroute.add(new Node(38,1));
		localroute.add(new Node(43,1));
		localroute.add(new Node(43,0));
		fullPath.add(
				new RPZonePath("0_zakopane_s",
				new LinkedList<Node>(localroute)));

		localroute.clear();
		localroute.add(new Node(43,127));
		localroute.add(new Node(43,125));
		localroute.add(new Node(50,125));
		localroute.add(new Node(50,122));
		localroute.add(new Node(58,122));
		localroute.add(new Node(58,118));
		localroute.add(new Node(62,118));
		localroute.add(new Node(62,116));
		localroute.add(new Node(66,116));
		localroute.add(new Node(66,122));
		localroute.add(new Node(76,122));
		localroute.add(new Node(76,119));
		fullPath.add(
				new RPZonePath("0_zakopane_c",
				new LinkedList<Node>(localroute)));

		// town hall
		localroute.clear();
		localroute.add(new Node(15,45));
		localroute.add(new Node(15,7));
		fullPath.add(
				new RPZonePath("int_zakopane_townhall",
				new LinkedList<Node>(localroute)));

		return fullPath;
	}


	/**
	 * route for pied piper outgoing
	 * @return - outgoing path
	 */
	public static List<RPZonePath> getZakopaneTownHallBackwardPath() {
		final List<RPZonePath> fullPath =
			new LinkedList<RPZonePath>();

		final List<Node> localroute = new LinkedList<Node>();

		// town hall
		localroute.clear();
		localroute.add(new Node(15,7));
		localroute.add(new Node(15,45));
		fullPath.add(
				new RPZonePath("int_zakopane_townhall",
				new LinkedList<Node>(localroute)));

		return fullPath;
	}

	/**
	 * it is a point where piper should go after speaking with mayor.
	 * @return - return point where pied piper can go through his multi zones path.
	 */
	public static Node getZakopaneTownHallMiddlePoint() {
		return new Node(15,7);
	}

	/**
	 * route for pied piper outgoing event
	 * @return - outgoing path
	 */
	public static List<List<RPZonePath>> getZakopaneCollectingMonstersPaths() {

		final List<Node> localroute = new LinkedList<Node>();
		final List<RPZonePath> globalroute = new LinkedList<RPZonePath>();
		final List<List<RPZonePath>> fullPath = new LinkedList<List<RPZonePath>>();

		/*
		 * Home
		 */
		localroute.clear();
		localroute.add(new Node(25,31));
		localroute.add(new Node(30,31));

		globalroute.clear();
		globalroute.add(
				new RPZonePath("0_zakopane_s",
				new LinkedList<Node>(localroute)));
		fullPath.add(new LinkedList<RPZonePath>(globalroute));

		/*
		 * Hospital
		 */
		localroute.clear();
		localroute.add(new Node(61,51));
		localroute.add(new Node(52,51));

		globalroute.clear();
		globalroute.add(
				new RPZonePath("0_zakopane_s",
				new LinkedList<Node>(localroute)));
		fullPath.add(new LinkedList<RPZonePath>(globalroute));

		/*
		 * Bank
		 */
		localroute.clear();
		localroute.add(new Node(102,44));
		localroute.add(new Node(103,44));

		globalroute.clear();
		globalroute.add(
				new RPZonePath("0_zakopane_s",
				new LinkedList<Node>(localroute)));
		fullPath.add(new LinkedList<RPZonePath>(globalroute));

		/*
		 * Sewing
		 */
		localroute.clear();
		localroute.add(new Node(125,96));
		localroute.add(new Node(121,96));

		globalroute.clear();
		globalroute.add(
				new RPZonePath("0_zakopane_s",
				new LinkedList<Node>(localroute)));

		/*
		 * thats all :)
		 */
		return fullPath;
	}

}
