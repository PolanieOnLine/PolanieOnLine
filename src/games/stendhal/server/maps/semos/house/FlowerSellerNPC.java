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
package games.stendhal.server.maps.semos.house;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.TeleporterBehaviour;

/**
 * Builds a Flower Seller NPC for the Elf Princess quest.
 *
 * @author kymara
 */
public class FlowerSellerNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		new TeleporterBehaviour(buildSemosHouseArea(), null, "0", "Kwiatki! Świeże kwiatki!");
	}

	private SpeakerNPC buildSemosHouseArea() {
	    final SpeakerNPC rose = new SpeakerNPC("Róża Kwiaciarka") {
	    	@Override
	    	protected void createDialog() {
	    		addJob("Jestem wędrowną kwiaciarką z dalekiego Grodu Kraka.");
	    		addGoodbye("Wszystko zaczyna się od róż... Do widzenia...");
			}// the rest is in the ElfPrincess quest
		};

		rose.setDescription("Oto Róża Kwiaciarka. Skacze z miejsca na miejsce z koszykiem pełnym pięknych róż.");
		rose.setEntityClass("gypsywomannpc");
		rose.setGender("F");
		rose.setCollisionAction(CollisionAction.REVERSE);

		// start in int_semos_house
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_semos_house");
		rose.setPosition(5, 6);
		zone.add(rose);

		return rose;
	}
}
