/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.tavern.market;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.trade.Market;
import games.stendhal.server.events.ChatOptionsEvent;
import marauroa.common.game.RPObject;

/**
 * adds a market to a zone
 *
 * @author madmetzger
 */
public class TradeCenterZoneConfigurator implements ZoneConfigurator {
	private static final String TRADE_ADVISOR_NAME = "Harold";
	private static final int COORDINATE_Y = 13;
	private static final int COORDINATE_X = 10;

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
			/*if (rpObject.getRPClass().getName().equals(Market.MARKET_RPCLASS_NAME)) {
				return (Market) rpObject;
			}
			*/
			if (rpObject instanceof Market) {
				return (Market) rpObject;
			}
		}
		return null;
	}

	private void buildTradeCenterAdvisor(StendhalRPZone zone) {
		SpeakerNPC speaker = new MarketManagerNPC(TRADE_ADVISOR_NAME, 3);
		speaker.setPosition(COORDINATE_X,COORDINATE_Y);
		speaker.setDescription("Oto Harold. Wyglądający na przyjaznego faceta, który czeka na utworzenie oferty od ciebie...");
		speaker.setEntityClass("tradecenteradvisornpc");
		speaker.setOutfit(3, 34, 1, null, 0, null, 5, null, 0);
		speaker.setGender("M");
		// add icon denoting merchant
		speaker.put("job_merchant", "");

		// chat options
		speaker.addKnownChatOptions("pokaż");
		ChatOptionsEvent.addMerchantActivity("pokaż");

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
