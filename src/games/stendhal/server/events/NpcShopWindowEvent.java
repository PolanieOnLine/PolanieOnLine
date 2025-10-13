/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.events;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ItemInformation;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.shop.ShopType;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPSlot;
import marauroa.common.game.SyntaxException;

/**
 * Event sending a graphical NPC shop window to the client.
 */
public class NpcShopWindowEvent extends RPEvent {
	private static final Logger logger = Logger.getLogger(NpcShopWindowEvent.class);

	private static final String SLOT_OFFERS = "offers";
	private static final String ATTR_ACTION = "action";
	private static final String ATTR_NPC = "npc";
	private static final String ATTR_TITLE = "title";
	private static final String ATTR_BACKGROUND = "background";
	private static final String ATTR_FLAVOR = "shop_flavor";
	private static final String ATTR_CATEGORY = "shop_category";
	private static final String ATTR_ITEM_KEY = "shop_item_key";
	private static final String ATTR_MODE = "shop_mode";
	private static final String ATTR_OFFER_TYPE = "shop_offer_type";
	private static final String MODE_BUY_SELL = "buy_sell";

	private static final String ACTION_OPEN = "open";
	private static final String ACTION_CLOSE = "close";

	private NpcShopWindowEvent(final String action, final String npcName) {
		super(Events.NPC_SHOP);
		put(ATTR_ACTION, action);
		put(ATTR_NPC, npcName);
	}

	/**
	 * Registers the RPClass for NPC shop events.
	*/
	public static void generateRPClass() {
		try {
			final RPClass rpclass = new RPClass(Events.NPC_SHOP);
			rpclass.add(DefinitionClass.ATTRIBUTE, ATTR_ACTION, Type.STRING, Definition.PRIVATE);
			rpclass.add(DefinitionClass.ATTRIBUTE, ATTR_NPC, Type.STRING, Definition.PRIVATE);
			rpclass.add(DefinitionClass.ATTRIBUTE, ATTR_TITLE, Type.STRING, Definition.PRIVATE);
			rpclass.add(DefinitionClass.ATTRIBUTE, ATTR_BACKGROUND, Type.STRING, Definition.PRIVATE);
			rpclass.add(DefinitionClass.ATTRIBUTE, ATTR_MODE, Type.STRING, Definition.PRIVATE);
			rpclass.addRPSlot(SLOT_OFFERS, 999);
		} catch (final SyntaxException e) {
			logger.error("cannot generateRPClass", e);
		}
	}

	/**
	 * Creates an event opening the shop window for the given NPC.
	 *
	 * @param npc
	 *            merchant speaking to the player
	 * @param sellerBehaviour
	 *            behaviour providing the sell offers
	 * @param buyerBehaviour
	 *            behaviour providing the buy offers
	 * @param player
	 *            player in the conversation
	 * @return event describing shop inventory
	*/
	public static NpcShopWindowEvent open(final SpeakerNPC npc, final SellerBehaviour sellerBehaviour,
	final BuyerBehaviour buyerBehaviour, final Player player) {
		final NpcShopWindowEvent event = new NpcShopWindowEvent(ACTION_OPEN, npc.getName());
		event.put(ATTR_TITLE, npc.getName() + " - Sklep");

		if (npc.has("shop_title")) {
			event.put(ATTR_TITLE, npc.get("shop_title"));
		}
		if (npc.has("shop_background")) {
			event.put(ATTR_BACKGROUND, npc.get("shop_background"));
		}

		final boolean hasSeller = sellerBehaviour != null;
		final boolean hasBuyer = buyerBehaviour != null;
		event.put(ATTR_MODE, determineMode(hasSeller, hasBuyer, sellerBehaviour, buyerBehaviour));
		event.addSlot(SLOT_OFFERS);
		final RPSlot slot = event.getSlot(SLOT_OFFERS);

		if (hasSeller) {
			appendSellerOffers(slot, npc, sellerBehaviour, player);
		}
		if (hasBuyer) {
			appendBuyerOffers(slot, npc, buyerBehaviour);
		}

		return event;
	}

	private static void appendSellerOffers(final RPSlot slot, final SpeakerNPC npc, final SellerBehaviour behaviour,
	final Player player) {
		for (final String itemName : behaviour.dealtItems()) {
			final Item item = behaviour.getAskedItem(itemName, player);
			if (item == null) {
				logger.warn("Skipping null offer for item '" + itemName + "' from NPC " + npc.getName());
				continue;
			}
			final ItemInformation info = new ItemInformation(item);
			populateOfferInfo(info, item, itemName, behaviour.getUnitPrice(itemName), ShopType.ITEM_SELL);
			slot.add(info);
		}
	}

	private static void appendBuyerOffers(final RPSlot slot, final SpeakerNPC npc, final BuyerBehaviour behaviour) {
		for (final String itemName : behaviour.dealtItems()) {
			final Item item = SingletonRepository.getEntityManager().getItem(itemName);
			if (item == null) {
				logger.warn("Skipping null buy offer for item '" + itemName + "' from NPC " + npc.getName());
				continue;
			}
			final ItemInformation info = new ItemInformation(item);
			populateOfferInfo(info, item, itemName, behaviour.getUnitPrice(itemName), ShopType.ITEM_BUY);
			slot.add(info);
		}
	}

	private static void populateOfferInfo(final ItemInformation info, final Item item, final String itemName,
	final int price, final ShopType offerType) {
		info.put("price", price);
		info.put("description_info", info.describe());
		info.put(ATTR_ITEM_KEY, itemName);
		if ((item != null) && item.has("class")) {
			info.put(ATTR_CATEGORY, item.get("class"));
		}
		info.put(ATTR_FLAVOR, extractFlavor(item));
		info.put(ATTR_OFFER_TYPE, offerType.toString());
	}

	private static String determineMode(final boolean hasSeller, final boolean hasBuyer,
	final SellerBehaviour sellerBehaviour, final BuyerBehaviour buyerBehaviour) {
		if (hasSeller && hasBuyer) {
			return MODE_BUY_SELL;
		}
		if (hasSeller) {
			return extractTypeOrDefault(sellerBehaviour, ShopType.ITEM_SELL).toString();
		}
		if (hasBuyer) {
			return extractTypeOrDefault(buyerBehaviour, ShopType.ITEM_BUY).toString();
		}
		return ShopType.ITEM_SELL.toString();
	}

	private static ShopType extractTypeOrDefault(final SellerBehaviour behaviour, final ShopType fallback) {
		if (behaviour == null) {
			return fallback;
		}
		final ShopType type = behaviour.getShopType();
		return type == null ? fallback : type;
	}

	private static ShopType extractTypeOrDefault(final BuyerBehaviour behaviour, final ShopType fallback) {
		if (behaviour == null) {
			return fallback;
		}
		final ShopType type = behaviour.getShopType();
		return type == null ? fallback : type;
	}

	/**
	 * Creates an event closing any open shop window for the NPC.
	 *
	 * @param npc
	 *            merchant finishing the conversation
	 * @return closing event
	*/
	public static NpcShopWindowEvent close(final SpeakerNPC npc) {
		return new NpcShopWindowEvent(ACTION_CLOSE, npc.getName());
	}

	private static String extractFlavor(final Item item) {
		if (item == null) {
			return "";
		}
		if (item.has("shop_flavor")) {
			return item.get("shop_flavor");
		}
		if (item.has("flavor_text")) {
			return item.get("flavor_text");
		}
		if (item.has("flavour_text")) {
			return item.get("flavour_text");
		}
		return "";
	}

}
