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
package games.stendhal.server.actions.admin;

import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import games.stendhal.common.constants.Testing;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Banks;
import games.stendhal.server.maps.quests.IQuest;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Builds structured inspect data for administrator tooling.
 */
final class InspectDataBuilder {
	private static final List<String> EQUIPMENT_ORDER = Arrays.asList(
		"head", "neck", "cloak", "rhand", "lhand", "armor", "glove", "legs",
		"feet", "finger", "fingerb", "pas", "belt", "back");

	private static final List<String> RUNE_SLOTS = Arrays.asList(
		"offensive_rune", "defensive_rune", "resistance_rune", "utility_rune",
		"healing_rune", "control_rune", "special_rune");

	private InspectDataBuilder() {
	}

	static JSONObject build(final RPEntity inspected, final String targetId) {
		final JSONObject root = new JSONObject();

		root.put("id", targetId);
		root.put("entity", buildEntityBlock(inspected));
		root.put("location", buildLocation(inspected));
		root.put("stats", buildStats(inspected));
		root.put("resistances", buildResistances(inspected));
		root.put("outfit", buildOutfit(inspected));
		root.put("slots", buildSlots(inspected));

		if (inspected instanceof Player) {
			final Player inspectedPlayer = (Player) inspected;
			root.put("quests", buildQuestData(inspectedPlayer));
			root.put("metaSlots", buildMetaSlots(inspectedPlayer));
		}

		return root;
	}

	private static JSONObject buildEntityBlock(final RPEntity inspected) {
		final JSONObject entity = new JSONObject();
		entity.put("type", inspected.get("type"));
		entity.put("className", inspected.getClass().getName());
		entity.put("title", inspected.getTitle());
		entity.put("name", inspected.getName());
		entity.put("gender", inspected.getGender());
		return entity;
	}

	private static JSONObject buildLocation(final RPEntity inspected) {
		final JSONObject location = new JSONObject();
		if (inspected.getZone() != null) {
			location.put("zone", inspected.getZone().getName());
		}
		location.put("x", inspected.getX());
		location.put("y", inspected.getY());
		return location;
	}

	private static JSONObject buildStats(final RPEntity inspected) {
		final JSONObject stats = new JSONObject();
		stats.put("hp", inspected.getHP());
		stats.put("hpBase", inspected.getBaseHP());
		stats.put("atk", inspected.getAtk());
		stats.put("atkXp", inspected.getAtkXP());
		stats.put("def", inspected.getDef());
		stats.put("defXp", inspected.getDefXP());
		if (Testing.COMBAT) {
			stats.put("ratk", inspected.getRatk());
			stats.put("ratkXp", inspected.getRatkXP());
		}
		stats.put("mining", inspected.getMining());
		stats.put("miningXp", inspected.getMiningXP());
		stats.put("xp", inspected.getXP());
		stats.put("level", inspected.getLevel());
		stats.put("karma", inspected.getKarma());
		stats.put("mana", inspected.getMana());
		stats.put("manaBase", inspected.getBaseMana());
		if (inspected.has("age")) {
			stats.put("age", inspected.get("age"));
		}
		if (Testing.WEIGHT) {
			stats.put("capacity", inspected.getCapacity());
			stats.put("capacityBase", inspected.getBaseCapacity());
		}
		stats.put("resistance", inspected.getResistance());
		stats.put("visibility", inspected.getVisibility());
		return stats;
	}

	private static JSONObject buildResistances(final RPEntity inspected) {
		final JSONObject resistances = new JSONObject();
		for (final String key : inspected) {
			if (key.startsWith("resist_")) {
				final String value = inspected.get(key);
				final Double numeric = parseDouble(value);
				resistances.put(key, numeric != null ? numeric : value);
			}
		}
		return resistances;
	}

	private static JSONObject buildOutfit(final RPEntity inspected) {
		final JSONObject outfit = new JSONObject();
		boolean hasTemporary = false;
		if (inspected.has("outfit_ext")) {
			String current = inspected.get("outfit_ext");
			if (inspected.has("outfit_ext_orig")) {
				hasTemporary = true;
				outfit.put("temporary", current);
				current = inspected.get("outfit_ext_orig");
			}
			outfit.put("current", current);
		} else if (inspected.has("outfit")) {
			String current = inspected.get("outfit");
			if (inspected.has("outfit_org")) {
				hasTemporary = true;
				outfit.put("temporary", current);
				current = inspected.get("outfit_org");
			}
			outfit.put("current", current);
		}
		if (inspected instanceof Player) {
			final Player player = (Player) inspected;
			if (player.has("outfit_expire_age")) {
				outfit.put("expires", player.get("outfit_expire_age"));
			}
			if (hasTemporary != player.has("outfit_expire_age")) {
				outfit.put("warning", "Oryginalny strój i czas wygaśnięcia ubioru nie są zgodne.");
			}
		}
		if (inspected.has("class")) {
			outfit.put("class", inspected.get("class"));
		}
		return outfit;
	}

	private static JSONObject buildSlots(final RPEntity inspected) {
		final JSONObject slots = new JSONObject();
		final JSONArray slotList = new JSONArray();
		for (final RPSlot slot : inspected.slots()) {
			final String slotName = slot.getName();
			if (shouldSkipSlot(slotName)) {
				continue;
			}

			final JSONObject slotJson = new JSONObject();
			slotJson.put("name", slotName);
			slotJson.put("category", categorizeSlot(slotName));
			slotJson.put("label", buildSlotLabel(slotName));
			slotJson.put("size", slot.size());

			if (slotName.startsWith("!")) {
				final JSONArray values = new JSONArray();
				for (final RPObject object : slot) {
					values.add(object.toString());
				}
				slotJson.put("raw", values);
			} else {
				final JSONArray items = new JSONArray();
				for (final RPObject object : slot) {
					if (object instanceof Item) {
						items.add(buildItem((Item) object));
					}
				}
				slotJson.put("items", items);
			}

			slotList.add(slotJson);
		}
		slots.put("all", slotList);
		slots.put("equipmentOrder", toJsonArray(EQUIPMENT_ORDER));
		slots.put("runeOrder", toJsonArray(RUNE_SLOTS));
		return slots;
	}

	private static boolean shouldSkipSlot(final String slotName) {
		return "!buddy".equals(slotName)
				|| "!ignore".equals(slotName)
				|| "!visited".equals(slotName)
				|| "!tutorial".equals(slotName)
				|| "!kills".equals(slotName)
				|| "skills".equals(slotName)
				|| "spells".equals(slotName)
				|| "!quests".equals(slotName);
	}

	private static String categorizeSlot(final String slotName) {
		if (Banks.getBySlotName(slotName) != null) {
			return "bank";
		}
		if (slotName.endsWith("_set")) {
			return "reserve";
		}
		if (RUNE_SLOTS.contains(slotName)) {
			return "rune";
		}
		if ("bag".equals(slotName)) {
			return "bag";
		}
		if ("keyring".equals(slotName)
				|| "magicbag".equals(slotName)
				|| "trade".equals(slotName)
				|| "pouch".equals(slotName)
				|| "money".equals(slotName)) {
			return "container";
		}
		if (slotName.startsWith("!")) {
			return "meta";
		}
		return EQUIPMENT_ORDER.contains(slotName) ? "equipment" : "other";
	}

	private static String buildSlotLabel(final String slotName) {
		final Banks bank = Banks.getBySlotName(slotName);
		if (bank != null) {
			return formatBankLabel(bank);
		}
		switch (slotName) {
		case "rhand":
			return "Broń prawa";
		case "lhand":
			return "Broń lewa";
		case "neck":
			return "Naszyjnik";
		case "head":
			return "Hełm";
		case "armor":
			return "Zbroja";
		case "legs":
			return "Spodnie";
		case "feet":
			return "Buty";
		case "finger":
			return "Pierścień";
		case "fingerb":
			return "Pierścień (lewy)";
		case "glove":
			return "Rękawice";
		case "cloak":
			return "Płaszcz";
		case "back":
			return "Plecy";
		case "pas":
		case "belt":
			return "Pas";
		case "bag":
			return "Plecak";
		case "keyring":
			return "Rzemyk";
		case "magicbag":
			return "Magiczna torba";
		case "trade":
			return "Handel";
		case "pouch":
			return "Mieszek";
		case "money":
			return "Gotówka";
		case "offensive_rune":
			return "Glif ofensywny";
		case "defensive_rune":
			return "Glif obronny";
		case "resistance_rune":
			return "Glif odporności";
		case "utility_rune":
			return "Glif użytkowy";
		case "healing_rune":
			return "Glif leczenia";
		case "control_rune":
			return "Glif kontroli";
		case "special_rune":
			return "Glif specjalny";
		default:
			return slotName;
		}
	}

	private static String formatBankLabel(final Banks bank) {
		switch (bank) {
		case VAULT:
			return "Skrytka";
		case SEMOS:
			return "Bank Semos";
		case ADOS:
			return "Bank Ados";
		case DENIRAN:
			return "Bank Deniran";
		case FADO:
			return "Bank Fado";
		case KIRDNEH:
			return "Bank Kirdneh";
		case MAGIC:
			return "Bank Magic";
		case NALWOR:
			return "Bank Nalwor";
		case ZAKOPANE:
			return "Bank Zakopane";
		case KRAKOW:
			return "Bank Kraków";
		case GDANSK:
			return "Bank Gdańsk";
		case ZARAS:
			return "Skrzynia Zara";
		default:
			return bank.name();
		}
	}

	private static JSONObject buildItem(final Item item) {
		final JSONObject json = new JSONObject();
		json.put("id", item.getID().toString());
		json.put("type", item.get("type"));
		json.put("name", item.getDescriptionName());
		if (item.has("class")) {
			json.put("class", item.get("class"));
		}
		if (item.has("subclass")) {
			json.put("subclass", item.get("subclass"));
		}
		if (item instanceof StackableItem && item.has("quantity")) {
			json.put("quantity", item.get("quantity"));
		}
		if (item.has("quality")) {
			json.put("quality", item.get("quality"));
		}
		json.put("description", item.describe());
		return json;
	}

	private static JSONObject buildQuestData(final Player player) {
		final JSONObject quests = new JSONObject();
		final StendhalQuestSystem questSystem = SingletonRepository.getStendhalQuestSystem();

		quests.put("active", buildQuestEntries(player, questSystem.getOpenQuests(player), questSystem));
		quests.put("completed", buildQuestEntries(player, questSystem.getCompletedQuests(player), questSystem));
		quests.put("repeatable", buildQuestEntries(player, questSystem.getRepeatableQuests(player), questSystem));

		return quests;
	}

	private static JSONArray buildQuestEntries(final Player player, final List<String> questNames,
		final StendhalQuestSystem questSystem) {
		final JSONArray array = new JSONArray();
		for (final String questName : questNames) {
			final IQuest quest = questSystem.getQuest(questName);
			if (quest == null) {
				continue;
			}
			final JSONObject entry = new JSONObject();
			entry.put("name", questName);
			entry.put("slot", quest.getSlotName());
			entry.put("state", player.getQuest(quest.getSlotName()));
			entry.put("description", questSystem.getQuestDescription(player, questName));
			entry.put("warning", questSystem.getQuestLevelWarning(player, questName));
			entry.put("history", questSystem.getQuestProgressDetails(player, questName));
			array.add(entry);
		}
		return array;
	}

	private static JSONArray buildMetaSlots(final Player player) {
		final JSONArray meta = new JSONArray();
		for (final RPSlot slot : player.slots()) {
			final String name = slot.getName();
			if (!name.startsWith("!") || shouldSkipSlot(name) || "!quests".equals(name)) {
				continue;
			}
			final JSONObject entry = new JSONObject();
			entry.put("name", name);
			final JSONArray values = new JSONArray();
			for (final RPObject object : slot) {
				values.add(object.toString());
			}
			entry.put("values", values);
			meta.add(entry);
		}
		return meta;
	}

	private static JSONArray toJsonArray(final List<String> values) {
		final JSONArray array = new JSONArray();
		array.addAll(values);
		return array;
	}

	private static Double parseDouble(final String value) {
		if (value == null) {
			return null;
		}
		try {
			return Double.valueOf(value);
		} catch (final NumberFormatException e) {
			return null;
		}
	}
}
