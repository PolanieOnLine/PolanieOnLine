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
package games.stendhal.server.maps.zakopane.bank.market;

import java.util.Map;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.semos.tavern.market.MarketManagerNPC;
import games.stendhal.server.maps.semos.tavern.market.TradeCenterZoneConfigurator;

/**
 * adds a market to a zone
 *
 * @author madmetzger
 */
public class TraderNPC extends TradeCenterZoneConfigurator {
	private static final String TRADE_ADVISOR_NAME = "Radzimir";

	private static final int COORDINATE_X = 40;
	private static final int COORDINATE_Y = 5;

	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildTradeCenterAdvisor(zone);
	}

	private void buildTradeCenterAdvisor(StendhalRPZone zone) {
		SpeakerNPC speaker = new MarketManagerNPC(TRADE_ADVISOR_NAME, 5);
		speaker.setPosition(COORDINATE_X,COORDINATE_Y);
		speaker.setDescription("Oto Radzimir. Wyglądający na przyjaznego faceta, który czeka na utworzenie oferty od ciebie...");
		speaker.setEntityClass("npcstraganiarz");
		speaker.setGender("M");
		zone.add(speaker);
	}
}
