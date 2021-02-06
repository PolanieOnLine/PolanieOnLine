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
package games.pol.server.maps.zakopane.bank.market;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.trade.Market;
import games.stendhal.server.maps.semos.tavern.market.OfferExpirer;
import marauroa.common.game.RPObject;

/**
 * adds a market to a zone
 *
 * @author madmetzger
 *
 */
public class TradeCenterZoneConfigurator implements ZoneConfigurator {

	private static final String TRADE_ADVISOR_NAME = "Radzimir";

	private static final int COORDINATE_X = 40;
	private static final int COORDINATE_Y = 5;

	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		Market market = addShopToZone(zone);
		// start checking for expired offers
		new OfferExpirer(market);

		buildTradeCenterAdvisor(zone);
	}

	private Market addShopToZone(StendhalRPZone zone) {
		Market market = getMarketFromZone(zone);
		if (market == null) {
			market = Market.createShop();
			market.setVisibility(0);
			zone.add(market, false);
		}

		return market;
	}

	private Market getMarketFromZone(StendhalRPZone zone) {
		for (RPObject rpObject : zone) {
			if (rpObject instanceof Market) {
				return (Market) rpObject;
			}
		}
		return null;
	}

	private void buildTradeCenterAdvisor(StendhalRPZone zone) {
		SpeakerNPC speaker = new MarketManagerNPC(TRADE_ADVISOR_NAME);
		speaker.setPosition(COORDINATE_X,COORDINATE_Y);
		speaker.setEntityClass("npc_straganiarz");
		speaker.initHP(100);
		speaker.setDescription("Radzimir jest przyjaznym facetem, który czeka na utworzenie oferty od ciebie...");
		zone.add(speaker);
	}

	public static Market getShopFromZone(StendhalRPZone zone) {
		for (RPObject rpObject : zone) {
			if(rpObject.getRPClass().getName().equals(Market.MARKET_RPCLASS_NAME)) {
				return (Market) rpObject;
			}
		}
		return null;
	}
}
