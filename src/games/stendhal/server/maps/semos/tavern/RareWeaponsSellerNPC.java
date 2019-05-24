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
package games.stendhal.server.maps.semos.tavern;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

import java.util.Arrays;
import java.util.Map;

/*
 * Inside Semos Tavern - Level 1 (upstairs)
 */
public class RareWeaponsSellerNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMcPegleg(zone);
	}

	private void buildMcPegleg(final StendhalRPZone zone) {
		// Adding a new NPC that buys some of the stuff that Xin doesn't
		final SpeakerNPC mcpegleg = new SpeakerNPC("McPegleg") {

			@Override
			protected void createPath() {
				// McPegleg doesn't move (room too small)
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Yo man! Wyglądasz na kogoś kto potrzebuje #pomocy.");
				addJob("Jestem handlarzem ... powiedzmy ... #rzadkimi rzeczami.");
				addHelp("Nie jestem pewien czy mogę Ci ufać .... #pirat z krzywą #nogą musi mieć #oko na nowych ludzi.");
				addQuest("Jeżeli znajdziesz #rzadką #zbroję lub #broń ...");
				addGoodbye("Arrghh, do zobaczenia wkrótce!");
				add(ConversationStates.ATTENDING, Arrays.asList("weapon", "broń", "armor", "zbroję", "rare" ,"rzadką" ,"rzadkimi", "rare armor", "rzadką zbroję"),
				        ConversationStates.ATTENDING,
				        "Ciii! Okazjonalnie kupuję rzadką broń i zbroje. Masz jakieś? Może zrobimy dobry #interes?", null);
				addOffer("Spójrz na tablicę na ścianie, aby zobaczyć co oferuję.");
				add(ConversationStates.ATTENDING, Arrays.asList("eye", "oko", "leg", "nogą", "wood", "patch"),
				        ConversationStates.ATTENDING, "Nie zawsze każdy dzień jest szczęśliwy ...", null);
				add(ConversationStates.ATTENDING, Arrays.asList("pirate", "pirat"), null, ConversationStates.ATTENDING,
				        "To nie twój interes!", null);
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyrare")), false);
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		// Add some atmosphere
		mcpegleg.setDescription("Oto podejrzany facet z opaską na oku i drewnianą nogą.");

		// Add our new NPC to the game world
		mcpegleg.setEntityClass("pirate_sailornpc");
		mcpegleg.setPosition(15, 4);
		mcpegleg.initHP(100);
		zone.add(mcpegleg);
	}
}
