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

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.Events;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ItemInformation;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
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

	private static final String ACTION_OPEN = "open";
	private static final String ACTION_CLOSE = "close";

	private static final Set<String> WEAPON_TYPES = Arrays.stream(new String[] {
		"weapon", "sword", "axe", "club", "dagger", "bow", "crossbow", "spear", "polearm",
		"hammer", "staff", "wand"
	}).collect(Collectors.toSet());

	private static final Set<String> POTION_TYPES = Arrays.stream(new String[] {
		"drink", "potion", "elixir"
	}).collect(Collectors.toSet());

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
	 * @param behaviour
	 *            behaviour providing the offers
	 * @param player
	 *            player in the conversation
	 * @return event describing shop inventory
	 */
	public static NpcShopWindowEvent open(final SpeakerNPC npc, final SellerBehaviour behaviour, final Player player) {
		final NpcShopWindowEvent event = new NpcShopWindowEvent(ACTION_OPEN, npc.getName());
		event.put(ATTR_TITLE, npc.getName() + " - Sklep");

		if (npc.has("shop_title")) {
			event.put(ATTR_TITLE, npc.get("shop_title"));
		}
		if (npc.has("shop_background")) {
			event.put(ATTR_BACKGROUND, npc.get("shop_background"));
		}

		event.addSlot(SLOT_OFFERS);
		final RPSlot slot = event.getSlot(SLOT_OFFERS);

		for (final String itemName : behaviour.dealtItems()) {
			final Item item = behaviour.getAskedItem(itemName, player);
			if (item == null) {
				logger.warn("Skipping null offer for item '" + itemName + "' from NPC " + npc.getName());
				continue;
			}
			final ItemInformation info = new ItemInformation(item);
			final int price = behaviour.getUnitPrice(itemName);
			info.put("price", price);
			info.put("description_info", info.describe());
			info.put(ATTR_ITEM_KEY, itemName);
			info.put(ATTR_CATEGORY, determineCategory(info));
			info.put(ATTR_FLAVOR, extractFlavor(item));

			slot.add(info);
		}

		return event;
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

	private static String determineCategory(final Item item) {
		final String type = lowercase(item.get("class"));
		final String subclass = lowercase(item.get("subclass"));
		final String name = lowercase(item.getName());

		if (matchesAny(type, subclass, name, WEAPON_TYPES)) {
			return "weapon";
		}
		if (matchesAny(type, subclass, name, POTION_TYPES)) {
			return "potion";
		}
		return "misc";
	}

	private static boolean matchesAny(final String type, final String subclass, final String name, final Set<String> keywords) {
		if (type != null && keywords.contains(type)) {
			return true;
		}
		if (subclass != null && keywords.contains(subclass)) {
			return true;
		}
		if (name != null) {
			for (final String keyword : keywords) {
				if (name.contains(keyword)) {
					return true;
				}
			}
		}
		return false;
	}

	private static String lowercase(final String value) {
		return value == null ? null : value.toLowerCase(Locale.ROOT);
	}
}
