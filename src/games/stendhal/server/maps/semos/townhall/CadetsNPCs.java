/* $Id$ */
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
package games.stendhal.server.maps.semos.townhall;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * Creates the cadet npcs in townhall.
 *
 * @author kymara
 */
public class CadetsNPCs implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPCs(zone);
	}

	private void buildNPCs(final StendhalRPZone zone) {
		final String[] names = {"Super Trainer", "XP Hunter", "Well Rounded"};
		final String[] images = {"supertrainedguynpc", "xpphunternpc", "wellroundedguynpc"};
		final Direction[] directions = {Direction.RIGHT, Direction.UP, Direction.LEFT};
		final String[] descriptions = {"Oto żołnierz, który rzadko idzie na walkę, a sporo czasu spędza na treningu. Posiada: atk 40, obr 40.","Oto tchórzliwy żołnierz, który liczy na innych podczas obrony przed wrogami, a który zbiera nagrody za atakowanie ich. Posiada: atk 20, obr 20.","Oto odpowiedni żołnierz, który nie obawia się stawić czoła atakującym wrogom i który uczy się od nich nowych zdolności. Posiada: atk 30, obr 30."};
		final int[] levels = {20, 60, 40};
		final int[] xposition = {21, 24, 26};
		final int[] yposition = {17, 18, 16};
		for (int i = 0; i < 3; i++) {
			final SpeakerNPC npc = new SpeakerNPC(names[i]) {

				@Override
				protected void createPath() {
					setPath(null);
				}

				@Override
				protected void createDialog() {
					// no dialog
				}
			};
			npc.setEntityClass(images[i]);
			npc.setPosition(xposition[i], yposition[i]);
			npc.setDirection(directions[i]);
			npc.initHP(100);
			npc.setDescription(descriptions[i]);
			npc.setLevel(levels[i]);
			zone.add(npc);
		}
	}
}
