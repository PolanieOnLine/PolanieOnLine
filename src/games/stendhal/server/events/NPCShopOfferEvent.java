/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ItemInformation;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.MerchantBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPSlot;
import marauroa.common.game.SyntaxException;

public class NPCShopOfferEvent extends RPEvent {
	private static final Logger LOGGER = Logger.getLogger(NPCShopOfferEvent.class);

	private static final String STATUS = "status";
	private static final String NPC = "npc";
	private static final String TITLE = "title";
	private static final String BACKGROUND = "background";
	private static final String SELL_SLOT = "sell";
	private static final String BUY_SLOT = "buy";
	private static final String PRICE_ATTRIBUTE = "price";
	private static final String PRICE_COPPER_ATTRIBUTE = "price_copper";
	private static final String STACKABLE_ATTRIBUTE = "stackable";
	private static final String FLAVOUR_ATTRIBUTE = "flavor_text";

	private NPCShopOfferEvent() {
		super(Events.NPC_SHOP_OFFER);
	}

	public static void generateRPClass() {
		try {
			final RPClass rpclass = new RPClass(Events.NPC_SHOP_OFFER);
			rpclass.add(DefinitionClass.ATTRIBUTE, STATUS, Type.STRING, Definition.PRIVATE);
			rpclass.add(DefinitionClass.ATTRIBUTE, NPC, Type.STRING, Definition.PRIVATE);
			rpclass.add(DefinitionClass.ATTRIBUTE, TITLE, Type.STRING, Definition.PRIVATE);
			rpclass.add(DefinitionClass.ATTRIBUTE, BACKGROUND, Type.STRING, Definition.PRIVATE);
			rpclass.addRPSlot(SELL_SLOT, 999);
			rpclass.addRPSlot(BUY_SLOT, 999);
		} catch (final SyntaxException e) {
			LOGGER.error("cannot generateRPClass", e);
		}
	}

	public static NPCShopOfferEvent open(final SpeakerNPC npc, final Player player,
			final SellerBehaviour seller, final BuyerBehaviour buyer) {
		if ((seller == null || seller.dealtItems().isEmpty())
				&& (buyer == null || buyer.dealtItems().isEmpty())) {
			return null;
		}

		final NPCShopOfferEvent event = new NPCShopOfferEvent();
		event.put(STATUS, "open");
		event.put(NPC, npc.getName());

		if (npc.has("shop_title")) {
			event.put(TITLE, npc.get("shop_title"));
		} else {
			event.put(TITLE, npc.getName() + " - oferta");
		}

		if (npc.has("shop_background")) {
			event.put(BACKGROUND, npc.get("shop_background"));
		}

		event.fillSlot(SELL_SLOT, seller, true);
		event.fillSlot(BUY_SLOT, buyer, false);

		return event;
	}

	public static NPCShopOfferEvent close(final String npcName) {
		final NPCShopOfferEvent event = new NPCShopOfferEvent();
		event.put(STATUS, "close");
		event.put(NPC, npcName);
		return event;
	}

	private void fillSlot(final String slotName, final MerchantBehaviour behaviour,
			final boolean selling) {
		if (behaviour == null) {
			return;
		}

		final List<String> items = new ArrayList<>(behaviour.dealtItems());
		if (items.isEmpty()) {
			return;
		}
		Collections.sort(items, String.CASE_INSENSITIVE_ORDER);

		addSlot(slotName);
		final RPSlot slot = getSlot(slotName);
		for (final String itemName : items) {
			final int unitPrice = behaviour.getUnitPrice(itemName);
			final Item information = buildInformation(itemName, unitPrice, selling);
			if (information != null) {
				slot.add(information);
			}
		}
	}

	private Item buildInformation(final String itemName, final int unitPrice,
			final boolean selling) {
		final Item prototype = SingletonRepository.getEntityManager().getItem(itemName);
		if (prototype == null) {
			LOGGER.warn("Unknown item in shop: " + itemName);
			return null;
		}

		final ItemInformation info = new ItemInformation(prototype);
		info.put(PRICE_ATTRIBUTE, unitPrice);
		info.put(PRICE_COPPER_ATTRIBUTE, unitPrice);
		info.put("description_info", info.describe());

		if (prototype instanceof StackableItem) {
			info.put(STACKABLE_ATTRIBUTE, 1);
		} else {
			info.put(STACKABLE_ATTRIBUTE, 0);
		}

		if (prototype.has("flavor")) {
			info.put(FLAVOUR_ATTRIBUTE, prototype.get("flavor"));
		} else if (prototype.has("flavour")) {
			info.put(FLAVOUR_ATTRIBUTE, prototype.get("flavour"));
		}

		if (selling && info.has("price") && info.getInt("price") < 0) {
			info.put(PRICE_ATTRIBUTE, Math.abs(info.getInt("price")));
			info.put(PRICE_COPPER_ATTRIBUTE, Math.abs(info.getInt("price")));
		}

		return info;
	}
}
