/***************************************************************************
 *                 (C) Copyright 2020-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.warszawa.blacksmith;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.behaviour.adder.ItemUpgradeAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ItemUpgradeAdder.ItemUpgradeNPC;

/**
 * @author KarajuSs
 */
public class BlacksmithNPC implements ZoneConfigurator {
	private static final String npcName = "Kowal Tworzymir";

	private StendhalRPZone zone;

	private ItemUpgradeNPC upgradeNpc;
	private ItemUpgradeAdder itemUpgradeAdder;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		this.zone = zone;

		initNPC();
		initItemUpgrade();
	}

	private void initNPC() {
		itemUpgradeAdder = new ItemUpgradeAdder();

		upgradeNpc = new ItemUpgradeNPC(npcName) {
			@Override
			public void say(final String text) {
				// don't turn toward player
				say(text, false);
			}
		};

		upgradeNpc.setDescription("Oto " + npcName + ". Potrafi ulepszać różne wyposażenie.");
		upgradeNpc.setEntityClass("blacksmithnpc");
		upgradeNpc.setIdleDirection(Direction.DOWN);

		upgradeNpc.addGreeting();
		upgradeNpc.addGoodbye();

		upgradeNpc.addJob("Ulepszam wyposażenie. Powiedz #ulepsz, a otworzę okno ulepszania.");
		upgradeNpc.addOffer("Mogę #ulepszyć konkretny przedmiot wybrany w bezpiecznym oknie.");
		upgradeNpc.addQuest("Nie mam zadania dla Ciebie, ale mogę #ulepszyć wyposażenie.");
		upgradeNpc.addHelp("Powiedz #ulepsz, aby otworzyć okno z kosztami, materiałami i szansą powodzenia.");

		upgradeNpc.addReply("sprawdzić", "Powiedz #sprawdź <#'nazwa przedmiotu'>, aby poznać maksymalny poziom, albo #ulepsz, aby otworzyć okno.");

		upgradeNpc.setGender("M");
		upgradeNpc.setPosition(10, 4);
		zone.add(upgradeNpc);
	}

	private void initItemUpgrade() {
		itemUpgradeAdder.add(upgradeNpc);
	}
}
