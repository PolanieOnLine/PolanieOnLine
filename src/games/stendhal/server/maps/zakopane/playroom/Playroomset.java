/* $Id: Playroomset.java,v 1.6 2010/09/19 02:28:01 edi18028 Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.playroom;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.maps.zakopane.playroom.PlaySet;
import games.stendhal.server.util.Area;

import java.awt.geom.Rectangle2D;
import java.util.Map;

/**
 * Ados Wall North population - Deathmatch.
 *
 * @author hendrik
 */
public class Playroomset implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final Rectangle2D shape = new Rectangle2D.Double();
		shape.setRect(27, 19, 33 - 28 + 1, 29 - 19 + 1);
		new Area(zone, shape);
		final PlaySet playset = new PlaySet(zone);

		playset.createFiguraFioletowa(35, 3);
		playset.createFiguraFioletowa(35, 3);
		playset.createFiguraFioletowa(35, 3);
		playset.createFiguraFioletowa(35, 3);
		playset.createFiguraFioletowa(35, 3);
		playset.createFiguraFioletowa(35, 3);
		playset.createFiguraFioletowa(35, 3);
		playset.createFiguraFioletowa(35, 3);
		playset.createFiguraFioletowa(35, 3);
		playset.createFiguraZielona(35, 11);
		playset.createFiguraZielona(35, 11);
		playset.createFiguraZielona(35, 11);
		playset.createFiguraZielona(35, 11);
		playset.createFiguraZielona(35, 11);
		playset.createFiguraZielona(35, 11);
		playset.createFiguraZielona(35, 11);
		playset.createFiguraZielona(35, 11);
		playset.createFiguraZielona(35, 11);

		playset.createFiguraZielona(34, 16);
		playset.createFiguraZielona(34, 16);
		playset.createFiguraZielona(34, 16);
		playset.createFiguraZielona(34, 16);
		playset.createFiguraZielona(34, 16);
		playset.createFiguraZielona(34, 16);
		playset.createFiguraZielona(34, 16);
		playset.createFiguraZielona(34, 16);
		playset.createFiguraZielona(34, 16);
		playset.createFiguraFioletowa(34, 24);
		playset.createFiguraFioletowa(34, 24);
		playset.createFiguraFioletowa(34, 24);
		playset.createFiguraFioletowa(34, 24);
		playset.createFiguraFioletowa(34, 24);
		playset.createFiguraFioletowa(34, 24);
		playset.createFiguraFioletowa(34, 24);
		playset.createFiguraFioletowa(34, 24);
		playset.createFiguraFioletowa(34, 24);

		// first chessboard
		playset.createFiguraFioletowa(2, 3);
		playset.createFiguraFioletowa(2, 3);
		playset.createFiguraFioletowa(2, 3);
		playset.createFiguraFioletowa(2, 3);
		playset.createFiguraFioletowa(2, 3);
		playset.createFiguraFioletowa(2, 3);
		playset.createFiguraFioletowa(2, 3);
		playset.createFiguraFioletowa(2, 3);
		playset.createDamkaFioletowa(2, 6);
		playset.createDamkaFioletowa(2, 6);
		playset.createDamkaFioletowa(2, 6);
		playset.createDamkaFioletowa(2, 6);
		playset.createDamkaFioletowa(2, 6);
		playset.createDamkaFioletowa(2, 6);
		playset.createDamkaFioletowa(2, 6);
		playset.createDamkaFioletowa(2, 6);
		playset.createFiguraZielona(2, 11);
		playset.createFiguraZielona(2, 11);
		playset.createFiguraZielona(2, 11);
		playset.createFiguraZielona(2, 11);
		playset.createFiguraZielona(2, 11);
		playset.createFiguraZielona(2, 11);
		playset.createFiguraZielona(2, 11);
		playset.createFiguraZielona(2, 11);
		playset.createDamkaZielona(2, 8);
		playset.createDamkaZielona(2, 8);
		playset.createDamkaZielona(2, 8);
		playset.createDamkaZielona(2, 8);
		playset.createDamkaZielona(2, 8);
		playset.createDamkaZielona(2, 8);
		playset.createDamkaZielona(2, 8);
		playset.createDamkaZielona(2, 8);

		// second chessboard
		playset.createFiguraFioletowa(24, 3);
		playset.createFiguraFioletowa(24, 3);
		playset.createFiguraFioletowa(24, 3);
		playset.createFiguraFioletowa(24, 3);
		playset.createFiguraFioletowa(24, 3);
		playset.createFiguraFioletowa(24, 3);
		playset.createFiguraFioletowa(24, 3);
		playset.createFiguraFioletowa(24, 3);
		playset.createDamkaFioletowa(24, 6);
		playset.createDamkaFioletowa(24, 6);
		playset.createDamkaFioletowa(24, 6);
		playset.createDamkaFioletowa(24, 6);
		playset.createDamkaFioletowa(24, 6);
		playset.createDamkaFioletowa(24, 6);
		playset.createDamkaFioletowa(24, 6);
		playset.createDamkaFioletowa(24, 6);
		playset.createDamkaFioletowa(24, 6);
		playset.createFiguraZielona(24, 11);
		playset.createFiguraZielona(24, 11);
		playset.createFiguraZielona(24, 11);
		playset.createFiguraZielona(24, 11);
		playset.createFiguraZielona(24, 11);
		playset.createFiguraZielona(24, 11);
		playset.createFiguraZielona(24, 11);
		playset.createFiguraZielona(24, 11);
		playset.createDamkaZielona(24, 8);
		playset.createDamkaZielona(24, 8);
		playset.createDamkaZielona(24, 8);
		playset.createDamkaZielona(24, 8);
		playset.createDamkaZielona(24, 8);
		playset.createDamkaZielona(24, 8);
		playset.createDamkaZielona(24, 8);
		playset.createDamkaZielona(24, 8);

		// third chessboard
		playset.createFiguraFioletowa(2, 14);
		playset.createFiguraFioletowa(2, 14);
		playset.createFiguraFioletowa(2, 14);
		playset.createFiguraFioletowa(2, 14);
		playset.createFiguraFioletowa(2, 14);
		playset.createFiguraFioletowa(2, 14);
		playset.createFiguraFioletowa(2, 14);
		playset.createFiguraFioletowa(2, 14);
		playset.createDamkaFioletowa(2, 17);
		playset.createDamkaFioletowa(2, 17);
		playset.createDamkaFioletowa(2, 17);
		playset.createDamkaFioletowa(2, 17);
		playset.createDamkaFioletowa(2, 17);
		playset.createDamkaFioletowa(2, 17);
		playset.createDamkaFioletowa(2, 17);
		playset.createDamkaFioletowa(2, 17);
		playset.createFiguraZielona(2, 22);
		playset.createFiguraZielona(2, 22);
		playset.createFiguraZielona(2, 22);
		playset.createFiguraZielona(2, 22);
		playset.createFiguraZielona(2, 22);
		playset.createFiguraZielona(2, 22);
		playset.createFiguraZielona(2, 22);
		playset.createFiguraZielona(2, 22);
		playset.createDamkaZielona(2, 19);
		playset.createDamkaZielona(2, 19);
		playset.createDamkaZielona(2, 19);
		playset.createDamkaZielona(2, 19);
		playset.createDamkaZielona(2, 19);
		playset.createDamkaZielona(2, 19);
		playset.createDamkaZielona(2, 19);
		playset.createDamkaZielona(2, 19);

		// fourth chessboard
		playset.createFiguraFioletowa(24, 15);
		playset.createFiguraFioletowa(24, 15);
		playset.createFiguraFioletowa(24, 15);
		playset.createFiguraFioletowa(24, 15);
		playset.createFiguraFioletowa(24, 15);
		playset.createFiguraFioletowa(24, 15);
		playset.createFiguraFioletowa(24, 15);
		playset.createFiguraFioletowa(24, 15);
		playset.createDamkaFioletowa(24, 18);
		playset.createDamkaFioletowa(24, 18);
		playset.createDamkaFioletowa(24, 18);
		playset.createDamkaFioletowa(24, 18);
		playset.createDamkaFioletowa(24, 18);
		playset.createDamkaFioletowa(24, 18);
		playset.createDamkaFioletowa(24, 18);
		playset.createDamkaFioletowa(24, 18);
		playset.createFiguraZielona(24, 23);
		playset.createFiguraZielona(24, 23);
		playset.createFiguraZielona(24, 23);
		playset.createFiguraZielona(24, 23);
		playset.createFiguraZielona(24, 23);
		playset.createFiguraZielona(24, 23);
		playset.createFiguraZielona(24, 23);
		playset.createFiguraZielona(24, 23);
		playset.createDamkaZielona(24, 20);
		playset.createDamkaZielona(24, 20);
		playset.createDamkaZielona(24, 20);
		playset.createDamkaZielona(24, 20);
		playset.createDamkaZielona(24, 20);
		playset.createDamkaZielona(24, 20);
		playset.createDamkaZielona(24, 20);
		playset.createDamkaZielona(24, 20);

		playset.createPionekNiebieski(57, 3);
		playset.createPionekNiebieski(58, 3);
		playset.createPionekNiebieski(57, 4);
		playset.createPionekNiebieski(58, 4);
		playset.createPionekZolty(49, 4);
		playset.createPionekZolty(50, 4);
		playset.createPionekZolty(49, 5);
		playset.createPionekZolty(50, 5);
		playset.createPionekZielony(58, 11);
		playset.createPionekZielony(59, 11);
		playset.createPionekZielony(58, 12);
		playset.createPionekZielony(59, 12);
		playset.createPionekCzerwony(50, 12);
		playset.createPionekCzerwony(51, 12);
		playset.createPionekCzerwony(50, 13);
		playset.createPionekCzerwony(51, 13);
		playset.createKostka(54, 8);

		playset.createKolko(57, 16);
		playset.createKolko(57, 16);
		playset.createKolko(57, 16);
		playset.createKolko(57, 16);
		playset.createKolko(57, 16);
		playset.createKrzyzyk(57, 18);
		playset.createKrzyzyk(57, 18);
		playset.createKrzyzyk(57, 18);
		playset.createKrzyzyk(57, 18);
		playset.createKrzyzyk(57, 18);

		playset.createKrzyzyk(50, 21);
		playset.createKrzyzyk(50, 21);
		playset.createKrzyzyk(50, 21);
		playset.createKrzyzyk(50, 21);
		playset.createKrzyzyk(50, 21);
		playset.createKolko(50, 23);
		playset.createKolko(50, 23);
		playset.createKolko(50, 23);
		playset.createKolko(50, 23);
		playset.createKolko(50, 23);

		playset.createKrzyzyk(59, 21);
		playset.createKrzyzyk(59, 21);
		playset.createKrzyzyk(59, 21);
		playset.createKrzyzyk(59, 21);
		playset.createKrzyzyk(59, 21);
		playset.createKolko(59, 23);
		playset.createKolko(59, 23);
		playset.createKolko(59, 23);
		playset.createKolko(59, 23);
		playset.createKolko(59, 23);

		// first chessboard
		playset.createCzarnaWieza(4, 4);
		playset.createCzarnySkoczek(5, 4);
		playset.createCzarnyGoniec(6, 4);
		playset.createCzarnyHetman(7, 4);
		playset.createCzarnyKrol(8, 4);
		playset.createCzarnyGoniec(9, 4);
		playset.createCzarnySkoczek(10, 4);
		playset.createCzarnaWieza(11, 4);
		playset.createCzarnyPionek(4, 5);
		playset.createCzarnyPionek(5, 5);
		playset.createCzarnyPionek(6, 5);
		playset.createCzarnyPionek(7, 5);
		playset.createCzarnyPionek(8, 5);
		playset.createCzarnyPionek(9, 5);
		playset.createCzarnyPionek(10, 5);
		playset.createCzarnyPionek(11, 5);

		playset.createBialyPionek(4, 10);
		playset.createBialyPionek(5, 10);
		playset.createBialyPionek(6, 10);
		playset.createBialyPionek(7, 10);
		playset.createBialyPionek(8, 10);
		playset.createBialyPionek(9, 10);
		playset.createBialyPionek(10, 10);
		playset.createBialyPionek(11, 10);
		playset.createBialaWieza(4, 11);
		playset.createBialySkoczek(5, 11);
		playset.createBialyGoniec(6, 11);
		playset.createBialyHetman(7, 11);
		playset.createBialyKrol(8, 11);
		playset.createBialyGoniec(9, 11);
		playset.createBialySkoczek(10, 11);
		playset.createBialaWieza(11, 11);

		// second chessboard
		playset.createCzarnaWieza(15, 4);
		playset.createCzarnySkoczek(16, 4);
		playset.createCzarnyGoniec(17, 4);
		playset.createCzarnyHetman(18, 4);
		playset.createCzarnyKrol(19, 4);
		playset.createCzarnyGoniec(20, 4);
		playset.createCzarnySkoczek(21, 4);
		playset.createCzarnaWieza(22, 4);
		playset.createCzarnyPionek(15, 5);
		playset.createCzarnyPionek(16, 5);
		playset.createCzarnyPionek(17, 5);
		playset.createCzarnyPionek(18, 5);
		playset.createCzarnyPionek(19, 5);
		playset.createCzarnyPionek(20, 5);
		playset.createCzarnyPionek(21, 5);
		playset.createCzarnyPionek(22, 5);

		playset.createBialyPionek(15, 10);
		playset.createBialyPionek(16, 10);
		playset.createBialyPionek(17, 10);
		playset.createBialyPionek(18, 10);
		playset.createBialyPionek(19, 10);
		playset.createBialyPionek(20, 10);
		playset.createBialyPionek(21, 10);
		playset.createBialyPionek(22, 10);
		playset.createBialaWieza(15, 11);
		playset.createBialySkoczek(16, 11);
		playset.createBialyGoniec(17, 11);
		playset.createBialyHetman(18, 11);
		playset.createBialyKrol(19, 11);
		playset.createBialyGoniec(20, 11);
		playset.createBialySkoczek(21, 11);
		playset.createBialaWieza(22, 11);

		// third chessboard
		playset.createCzarnaWieza(4, 15);
		playset.createCzarnySkoczek(5, 15);
		playset.createCzarnyGoniec(6, 15);
		playset.createCzarnyHetman(7, 15);
		playset.createCzarnyKrol(8, 15);
		playset.createCzarnyGoniec(9, 15);
		playset.createCzarnySkoczek(10, 15);
		playset.createCzarnaWieza(11, 15);
		playset.createCzarnyPionek(4, 16);
		playset.createCzarnyPionek(5, 16);
		playset.createCzarnyPionek(6, 16);
		playset.createCzarnyPionek(7, 16);
		playset.createCzarnyPionek(8, 16);
		playset.createCzarnyPionek(9, 16);
		playset.createCzarnyPionek(10, 16);
		playset.createCzarnyPionek(11, 16);

		playset.createBialyPionek(4, 21);
		playset.createBialyPionek(5, 21);
		playset.createBialyPionek(6, 21);
		playset.createBialyPionek(7, 21);
		playset.createBialyPionek(8, 21);
		playset.createBialyPionek(9, 21);
		playset.createBialyPionek(10, 21);
		playset.createBialyPionek(11, 21);
		playset.createBialaWieza(4, 22);
		playset.createBialySkoczek(5, 22);
		playset.createBialyGoniec(6, 22);
		playset.createBialyHetman(7, 22);
		playset.createBialyKrol(8, 22);
		playset.createBialyGoniec(9, 22);
		playset.createBialySkoczek(10, 22);
		playset.createBialaWieza(11, 22);

		// fourth chessboard
		playset.createCzarnaWieza(15, 15);
		playset.createCzarnySkoczek(16, 15);
		playset.createCzarnyGoniec(17, 15);
		playset.createCzarnyHetman(18, 15);
		playset.createCzarnyKrol(19, 15);
		playset.createCzarnyGoniec(20, 15);
		playset.createCzarnySkoczek(21, 15);
		playset.createCzarnaWieza(22, 15);
		playset.createCzarnyPionek(15, 16);
		playset.createCzarnyPionek(16, 16);
		playset.createCzarnyPionek(17, 16);
		playset.createCzarnyPionek(18, 16);
		playset.createCzarnyPionek(19, 16);
		playset.createCzarnyPionek(20, 16);
		playset.createCzarnyPionek(21, 16);
		playset.createCzarnyPionek(22, 16);

		playset.createBialyPionek(15, 21);
		playset.createBialyPionek(16, 21);
		playset.createBialyPionek(17, 21);
		playset.createBialyPionek(18, 21);
		playset.createBialyPionek(19, 21);
		playset.createBialyPionek(20, 21);
		playset.createBialyPionek(21, 21);
		playset.createBialyPionek(22, 21);
		playset.createBialaWieza(15, 22);
		playset.createBialySkoczek(16, 22);
		playset.createBialyGoniec(17, 22);
		playset.createBialyHetman(18, 22);
		playset.createBialyKrol(19, 22);
		playset.createBialyGoniec(20, 22);
		playset.createBialySkoczek(21, 22);
		playset.createBialaWieza(22, 22);
	}
}
