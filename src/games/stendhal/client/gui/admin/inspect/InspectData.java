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
package games.stendhal.client.gui.admin.inspect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Parsed inspect data payload.
 */
public final class InspectData {
private final String id;
private final EntityInfo entity;
private final Location location;
private final Map<String, String> stats;
private final Map<String, String> resistances;
private final Outfit outfit;
private final List<Slot> slots;
private final Map<String, Slot> slotsByName;
private final List<String> equipmentOrder;
private final List<String> runeOrder;
private final List<QuestEntry> activeQuests;
private final List<QuestEntry> completedQuests;
private final List<QuestEntry> repeatableQuests;
private final List<MetaSlot> metaSlots;

private InspectData(final JSONObject source) {
id = getString(source, "id");
entity = parseEntity((JSONObject) source.get("entity"));
location = parseLocation((JSONObject) source.get("location"));
stats = parseMap((JSONObject) source.get("stats"));
resistances = parseMap((JSONObject) source.get("resistances"));
outfit = parseOutfit((JSONObject) source.get("outfit"));

final JSONObject slotsObject = (JSONObject) source.get("slots");
if (slotsObject != null) {
final JSONArray slotsArray = (JSONArray) slotsObject.get("all");
final List<Slot> parsedSlots = new ArrayList<Slot>();
final Map<String, Slot> slotMap = new HashMap<String, Slot>();
if (slotsArray != null) {
for (Object entry : slotsArray) {
if (entry instanceof JSONObject) {
final Slot slot = parseSlot((JSONObject) entry);
parsedSlots.add(slot);
slotMap.put(slot.getName(), slot);
}
}
}
slots = Collections.unmodifiableList(parsedSlots);
slotsByName = Collections.unmodifiableMap(slotMap);
equipmentOrder = parseArray((JSONArray) slotsObject.get("equipmentOrder"));
runeOrder = parseArray((JSONArray) slotsObject.get("runeOrder"));
} else {
slots = Collections.emptyList();
slotsByName = Collections.emptyMap();
equipmentOrder = Collections.emptyList();
runeOrder = Collections.emptyList();
}

final JSONObject questsObject = (JSONObject) source.get("quests");
if (questsObject != null) {
activeQuests = parseQuests((JSONArray) questsObject.get("active"));
completedQuests = parseQuests((JSONArray) questsObject.get("completed"));
repeatableQuests = parseQuests((JSONArray) questsObject.get("repeatable"));
} else {
activeQuests = Collections.emptyList();
completedQuests = Collections.emptyList();
repeatableQuests = Collections.emptyList();
}

metaSlots = parseMetaSlots((JSONArray) source.get("metaSlots"));
}

public static InspectData fromJson(final JSONObject source) {
return new InspectData(source);
}

public String getId() {
return id;
}

public EntityInfo getEntity() {
return entity;
}

public Location getLocation() {
return location;
}

public Map<String, String> getStats() {
return stats;
}

public Map<String, String> getResistances() {
return resistances;
}

public Outfit getOutfit() {
return outfit;
}

public List<Slot> getSlots() {
return slots;
}

public Slot getSlot(final String name) {
return slotsByName.get(name);
}

public List<String> getEquipmentOrder() {
return equipmentOrder;
}

public List<String> getRuneOrder() {
return runeOrder;
}

public List<Slot> getSlotsByCategory(final String category) {
if (category == null) {
return Collections.emptyList();
}
final List<Slot> filtered = new LinkedList<Slot>();
for (final Slot slot : slots) {
if (category.equals(slot.getCategory())) {
filtered.add(slot);
}
}
return filtered;
}

public List<QuestEntry> getActiveQuests() {
return activeQuests;
}

public List<QuestEntry> getCompletedQuests() {
return completedQuests;
}

public List<QuestEntry> getRepeatableQuests() {
return repeatableQuests;
}

public List<MetaSlot> getMetaSlots() {
return metaSlots;
}

private static EntityInfo parseEntity(final JSONObject entityObject) {
if (entityObject == null) {
return new EntityInfo(null, null, null, null, null);
}
return new EntityInfo(
getString(entityObject, "type"),
getString(entityObject, "className"),
getString(entityObject, "title"),
getString(entityObject, "name"),
getString(entityObject, "gender"));
}

private static Location parseLocation(final JSONObject locationObject) {
if (locationObject == null) {
return new Location(null, 0, 0);
}
return new Location(
getString(locationObject, "zone"),
toInt(locationObject.get("x")),
toInt(locationObject.get("y")));
}

private static Outfit parseOutfit(final JSONObject outfitObject) {
if (outfitObject == null) {
return new Outfit(null, null, null, null, null);
}
return new Outfit(
getString(outfitObject, "current"),
getString(outfitObject, "temporary"),
getString(outfitObject, "expires"),
getString(outfitObject, "class"),
getString(outfitObject, "warning"));
}

private static Slot parseSlot(final JSONObject object) {
final String name = getString(object, "name");
final String label = getString(object, "label");
final String category = getString(object, "category");
final List<Item> items = new ArrayList<Item>();
final JSONArray itemsArray = (JSONArray) object.get("items");
if (itemsArray != null) {
for (Object entry : itemsArray) {
if (entry instanceof JSONObject) {
items.add(parseItem((JSONObject) entry));
}
}
}
final List<String> raw = parseArray((JSONArray) object.get("raw"));
return new Slot(name, label, category, Collections.unmodifiableList(items), raw);
}

private static Item parseItem(final JSONObject object) {
final String id = getString(object, "id");
final String type = getString(object, "type");
final String name = getString(object, "name");
final String itemClass = getString(object, "class");
final String subclass = getString(object, "subclass");
final int quantity = toInt(object.get("quantity"), 1);
final String quality = getString(object, "quality");
final String description = getString(object, "description");
return new Item(id, type, name, itemClass, subclass, quantity, quality, description);
}

private static List<QuestEntry> parseQuests(final JSONArray array) {
if (array == null) {
return Collections.emptyList();
}
final List<QuestEntry> quests = new ArrayList<QuestEntry>();
for (Object entry : array) {
if (entry instanceof JSONObject) {
final JSONObject object = (JSONObject) entry;
quests.add(new QuestEntry(
getString(object, "name"),
getString(object, "slot"),
getString(object, "state"),
getString(object, "description"),
getString(object, "warning"),
parseArray((JSONArray) object.get("history"))));
}
}
return Collections.unmodifiableList(quests);
}

private static List<MetaSlot> parseMetaSlots(final JSONArray array) {
if (array == null) {
return Collections.emptyList();
}
final List<MetaSlot> result = new ArrayList<MetaSlot>();
for (Object entry : array) {
if (entry instanceof JSONObject) {
final JSONObject object = (JSONObject) entry;
result.add(new MetaSlot(getString(object, "name"), parseArray((JSONArray) object.get("values"))));
}
}
return Collections.unmodifiableList(result);
}

private static Map<String, String> parseMap(final JSONObject object) {
if (object == null) {
return Collections.emptyMap();
}
final Map<String, String> map = new HashMap<String, String>();
for (Object key : object.keySet()) {
final String name = String.valueOf(key);
map.put(name, stringify(object.get(key)));
}
return Collections.unmodifiableMap(map);
}

private static List<String> parseArray(final JSONArray array) {
if (array == null) {
return Collections.emptyList();
}
final List<String> list = new ArrayList<String>();
for (Object entry : array) {
list.add(stringify(entry));
}
return Collections.unmodifiableList(list);
}

private static String stringify(final Object value) {
if (value == null) {
return null;
}
return String.valueOf(value);
}

private static String getString(final JSONObject object, final String key) {
if (object == null) {
return null;
}
final Object value = object.get(key);
return value != null ? String.valueOf(value) : null;
}

private static int toInt(final Object value) {
return toInt(value, 0);
}

private static int toInt(final Object value, final int defaultValue) {
if (value == null) {
return defaultValue;
}
if (value instanceof Number) {
return ((Number) value).intValue();
}
try {
return Integer.parseInt(value.toString());
} catch (final NumberFormatException exception) {
return defaultValue;
}
}

public static final class EntityInfo {
private final String type;
private final String className;
private final String title;
private final String name;
private final String gender;

private EntityInfo(final String type, final String className, final String title, final String name, final String gender) {
this.type = type;
this.className = className;
this.title = title;
this.name = name;
this.gender = gender;
}

public String getType() {
return type;
}

public String getClassName() {
return className;
}

public String getTitle() {
return title;
}

public String getName() {
return name;
}

public String getGender() {
return gender;
}
}

public static final class Location {
private final String zone;
private final int x;
private final int y;

private Location(final String zone, final int x, final int y) {
this.zone = zone;
this.x = x;
this.y = y;
}

public String getZone() {
return zone;
}

public int getX() {
return x;
}

public int getY() {
return y;
}
}

public static final class Outfit {
private final String current;
private final String temporary;
private final String expires;
private final String outfitClass;
private final String warning;

private Outfit(final String current, final String temporary, final String expires, final String outfitClass, final String warning) {
this.current = current;
this.temporary = temporary;
this.expires = expires;
this.outfitClass = outfitClass;
this.warning = warning;
}

public String getCurrent() {
return current;
}

public String getTemporary() {
return temporary;
}

public String getExpires() {
return expires;
}

public String getOutfitClass() {
return outfitClass;
}

public String getWarning() {
return warning;
}
}

public static final class Slot {
private final String name;
private final String label;
private final String category;
private final List<Item> items;
private final List<String> raw;

private Slot(final String name, final String label, final String category, final List<Item> items, final List<String> raw) {
this.name = name;
this.label = label;
this.category = category;
this.items = items;
this.raw = raw;
}

public String getName() {
return name;
}

public String getLabel() {
return label;
}

public String getCategory() {
return category;
}

public List<Item> getItems() {
return items;
}

public List<String> getRaw() {
return raw;
}

public Item getFirstItem() {
if (items.isEmpty()) {
return null;
}
return items.get(0);
}
}

public static final class Item {
private final String id;
private final String type;
private final String name;
private final String itemClass;
private final String subclass;
private final int quantity;
private final String quality;
private final String description;

private Item(final String id, final String type, final String name, final String itemClass, final String subclass,
final int quantity, final String quality, final String description) {
this.id = id;
this.type = type;
this.name = name;
this.itemClass = itemClass;
this.subclass = subclass;
this.quantity = quantity;
this.quality = quality;
this.description = description;
}

public String getId() {
return id;
}

public String getType() {
return type;
}

public String getName() {
return name;
}

public String getItemClass() {
return itemClass;
}

public String getSubclass() {
return subclass;
}

public int getQuantity() {
return quantity;
}

public String getQuality() {
return quality;
}

public String getDescription() {
return description;
}
}

public static final class QuestEntry {
private final String name;
private final String slot;
private final String state;
private final String description;
private final String warning;
private final List<String> history;

private QuestEntry(final String name, final String slot, final String state, final String description, final String warning,
final List<String> history) {
this.name = name;
this.slot = slot;
this.state = state;
this.description = description;
this.warning = warning;
this.history = history;
}

public String getName() {
return name;
}

public String getSlot() {
return slot;
}

public String getState() {
return state;
}

public String getDescription() {
return description;
}

public String getWarning() {
return warning;
}

public List<String> getHistory() {
return history;
}
}

public static final class MetaSlot {
private final String name;
private final List<String> values;

private MetaSlot(final String name, final List<String> values) {
this.name = name;
this.values = values;
}

public String getName() {
return name;
}

public List<String> getValues() {
return values;
}
}
}
