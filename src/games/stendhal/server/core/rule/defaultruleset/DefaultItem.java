/***************************************************************************
 *                   (C) Copyright 2003-2023 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rule.defaultruleset;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.rule.defaultruleset.creator.AbstractCreator;
import games.stendhal.server.core.rule.defaultruleset.creator.AttributesItemCreator;
import games.stendhal.server.core.rule.defaultruleset.creator.DefaultItemCreator;
import games.stendhal.server.core.rule.defaultruleset.creator.FullItemCreator;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.behavior.UseBehavior;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.status.PoisonAttackerFactory;
import games.stendhal.server.entity.status.StatusAttacker;
import games.stendhal.server.entity.status.StatusAttackerFactory;
import games.stendhal.server.entity.status.StatusType;

/**
 * All default items which can be reduced to stuff that increase the attack
 * point and stuff that increase the defense points.
 *
 * @author Matthias Totz, chad3f
 */
public class DefaultItem {

	private static final Set<String> RARITY_EXCLUDED_CLASSES = Collections.unmodifiableSet(
		new HashSet<String>(Arrays.asList(
		"book",
		"box",
		"container",
		"document",
		"drink",
		"flower",
		"food",
		"glyph",
		"herb",
		"ingredient",
		"key",
		"material",
		"money",
		"potion",
		"present",
		"quest",
		"resource",
		"scroll",
		"ticket",
		"tool",
		"token")));
		
	/** Implementation creator. */
	private AbstractCreator<Item> creator;

	/** items class. */
	private String clazz;

	/** items sub class. */
	private String subclazz;

	/** items type. */
	private String name;

	/** optional item description. */
	private String description;

	/** weight of this item. */
	private double weight;

	/** slots where this item can be equipped. */
	private List<String> slots = null;

	/** Map Tile Id. */
	private int tileid;

	/** Attributes of the item.*/
	private Map<String, String> attributes = null;

	private Class< ? > implementation = null;

	private int value;

	private Nature damageType;

	private Map<Nature, Double> susceptibilities;

	/* List of status effects to be added to StatusResistantIte. */
	private Map<StatusType, Double> resistances;

	private String[] statusAttacks;

	/* Slots where SlotActivatedItem can be activated when equipped. */
	private List<String> activeSlotsList;

	private boolean unattainable = false;

	/**
	 * Use behavior of the item, or <code>null</code> if no special behaviors
	 * are attached.
	 */
	private UseBehavior useBehavior;

	public DefaultItem(final String clazz, final String subclazz, final String name, final int tileid) {
		this.clazz = clazz;
		this.subclazz = subclazz;
		this.name = name;
		this.tileid = tileid;
	}

	public void setWeight(final double weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * Returns a shallow copy of the configured attribute map. This allows callers to
	 * adjust values without mutating the baseline configured in XML.
	 *
	 * @return copy of the attributes or {@code null}
	 */
	public Map<String, String> copyAttributes() {
		if (attributes == null) {
			return null;
		}

		return new LinkedHashMap<String, String>(attributes);
	}

	public boolean isRarityEligible() {
		return isRarityEligible(slots, clazz);
	}

	private boolean isRarityEligible(List<String> equipSlots, String itemClass) {
		if ((itemClass != null) && RARITY_EXCLUDED_CLASSES.contains(itemClass)) {
			return false;
		}
		if (RPEntity.isWeaponClass(itemClass)) {
			return true;
		}
		if (equipSlots != null) {
			for (String slot : equipSlots) {
				if (RPEntity.isEquipmentSlot(slot)) {
					return true;
				}
			}
		}
		return false;
	}

	public void setAttributes(final Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public void setEquipableSlots(final List<String> slots) {
		this.slots = slots;
	}

	public List<String> getEquipableSlots() {
		return slots;
	}

	public void setDescription(final String text) {
		this.description = text;
	}

	public String getDescription() {
		return description;
	}

	public void setDamageType(String type) {
		damageType = Nature.parse(type);
	}

	/**
	 * Set the susceptibilities. The key of each map entry should be a
	 * string corresponding to a damage type. The value is the susceptibility
	 * value of that damage type. The content of the mapping is copied, so
	 * it can be safely modified afterwards.
	 *
	 * @param sus susceptibility mapping
	 */
	public void setSusceptibilities(Map<String, Double> sus) {
		susceptibilities = new EnumMap<Nature, Double>(Nature.class);

		for (Entry<String, Double> entry : sus.entrySet()) {
			susceptibilities.put(Nature.parse(entry.getKey()), entry.getValue());
		}
	}

	public void setStatusAttacks(final String statusAttacks) {
		this.statusAttacks = statusAttacks.split(";");
	}

	/**
	 * Add slots to list where SlotActivatedItem can be activated when
	 * equipped.
	 *
	 * @param slots
	 * 		String list of slots separated by semicolon
	 */
	public void initializeActiveSlotsList(String slots) {
		// Make sure the list is initialized
		if (activeSlotsList == null) {
			activeSlotsList = new ArrayList<String>();
		}

		for (String s : slots.split(";")) {
			activeSlotsList.add(s);
		}
	}

	/**
	 * Set the types of status attacks that this StatusResistantItem can resist.
	 *
	 * @param res
	 * 		The status type and the resistance value
	 */
	public void initializeStatusResistancesList(Map<String, Double> res) {
		resistances = new EnumMap<StatusType, Double>(StatusType.class);

		for (Entry<String, Double> entry : res.entrySet()) {
			resistances.put(StatusType.parse(entry.getKey()), entry.getValue());
		}
	}

	public void setImplementation(final Class< ? > implementation) {
		this.implementation = implementation;
		creator = buildCreator(implementation);
	}

	/**
	 * Set the use behavior.
	 *
	 * @param behavior new behavior
	 */
	public void setBehavior(UseBehavior behavior) {
		this.useBehavior = behavior;
	}

	public Class< ? > getImplementation() {
		return implementation;
	}

	/**
	 * Build a creator for the class. It uses the following constructor search
	 * order:<br>
	 *
	 * <ul>
	 * <li><em>Class</em>(<em>name</em>, <em>clazz</em>,
	 * <em>subclazz</em>, <em>attributes</em>)
	 * <li><em>Class</em>(<em>attributes</em>)
	 * <li><em>Class</em>()
	 * </ul>
	 *
	 * @param implementation
	 *            The implementation class.
	 *
	 * @return A creator, or <code>null</code> if none found.
	 */
	protected AbstractCreator<Item> buildCreator(final Class< ? > implementation) {
		Constructor< ? > construct;

		/*
		 * <Class>(name, clazz, subclazz, attributes)
		 */
		try {
			construct = implementation.getConstructor(new Class[] {
					String.class, String.class, String.class, Map.class });

			return new FullItemCreator(this, construct);
		} catch (final NoSuchMethodException ex) {
			// ignore and continue
		}

		/*
		 * <Class>(attributes)
		 */
		try {
			construct = implementation.getConstructor(new Class[] { Map.class });

			return new AttributesItemCreator(this, construct);
		} catch (final NoSuchMethodException ex) {
			// ignore and continue
		}

		/*
		 * <Class>()
		 */
		try {
			construct = implementation.getConstructor(new Class[] {});

			return new DefaultItemCreator(this, construct);
		} catch (final NoSuchMethodException ex) {
			// ignore and continue
		}

		return null;
	}

	/**
	 * Returns an item-instance.
	 *
	 * @return An item, or <code>null</code> on error.
	 */
	public Item createItem() {
		return createItem(null);
	}

	public Item createItem(ItemRarity forcedRarity) {
		/*
		 * Just in case - Really should generate fatal error up front (in
		 * ItemXMLLoader).
		 */
		if (creator == null) {
			return null;
		}
		final Item item = creator.create();
		if (item != null) {
			item.setEquipableSlots(slots);
			item.setDescription(description);
			if (damageType != null) {
				item.setDamageType(damageType);
			}
			item.setWeight(weight);
			item.setSusceptibilities(susceptibilities);

			// status attackers
			if (statusAttacks != null) {
				for (final String statk: statusAttacks) {
					StatusAttacker statusAttacker;
					if (statk.contains("trucizna") || statk.contains("jad kobry") || statk.contains("ekstrakt litworowy")) {
						statusAttacker = PoisonAttackerFactory.get(statk);
					} else {
						statusAttacker = StatusAttackerFactory.get(statk);
					}
					if (statusAttacker != null) {
						item.addStatusAttacker(statusAttacker);
					}
				}
			}

			/* Set a list of status resistances for StatusResistantItem. */
			if ((this.resistances != null) && (!this.resistances.isEmpty())) {
				item.initializeStatusResistancesList(resistances);
			}

			/* Set a list of active slots for SlotActivatedItem. */
			if ((this.activeSlotsList != null)
					&& (!this.activeSlotsList.isEmpty())) {
				item.initializeActiveSlotsList(this.activeSlotsList);
			}

			item.setUseBehavior(useBehavior);

		Map<String, String> baseAttributes = copyAttributes();
		boolean rarityEligible = isRarityEligible(slots, clazz);
		item.setRarityBadgeVisible(rarityEligible);
		ItemRarity rolledRarity = (forcedRarity != null) ? forcedRarity : ItemRarity.rollRandom();
		if (!rarityEligible) {
			ItemRarity appliedRarity = (forcedRarity != null) ? forcedRarity : ItemRarity.COMMON;
			item.applyRarity(appliedRarity, baseAttributes, value, false);
		} else {
			item.applyRarity(rolledRarity, baseAttributes, value);
		}
		}

		return item;
	}

	/** @return the tile id .*/
	public int getTileId() {
		return tileid;
	}

	public void setTileId(final int val) {
		tileid = val;
	}

	public void setValue(final int val) {
		value = val;
	}

	public int getValue() {
		return value;
	}

	/** @return the class. */
	public String getItemClass() {
		return clazz;
	}

	public void setItemClass(final String val) {
		clazz = val;
	}

	/** @return the subclass. */
	public String getItemSubclass() {
		return subclazz;
	}

	public void setItemSubclass(final String val) {
		subclazz = val;
	}

	public String getItemName() {
		return name;
	}

	public void setItemName(final String val) {
		name = val;
	}

	public AbstractCreator<Item> getCreator() {
		return creator;
	}

	public List<String> getSlots() {
		return slots;
	}

	public Nature getDamageType() {
		return damageType;
	}

	public Map<Nature, Double> getSusceptibilities() {
		return susceptibilities;
	}

	public Map<StatusType, Double> getResistances() {
		return resistances;
	}

	public String[] getStatusAttacks() {
		return statusAttacks;
	}

	public List<String> getActiveSlotsList() {
		return activeSlotsList;
	}

	public UseBehavior getUseBehavior() {
		return useBehavior;
	}

	public boolean isUnattainable() {
		return unattainable;
	}

	public void setUnattainable(boolean unattainable) {
		this.unattainable = unattainable;
	}

	public String toXML() {
		final StringBuilder os = new StringBuilder();
		os.append("  <item name=\"" + name + "\">\n");
		os.append("    <type class=\"" + clazz + "\" subclass=\"" + subclazz
				+ "\" tileid=\"" + tileid + "\"/>\n");
		if (description != null) {
			os.append("    <description>" + description + "</description>\n");
		}
		os.append("    <implementation class-name=\""
				+ implementation.getCanonicalName() + "\"/>");
		os.append("    <attributes>\n");
		for (final Map.Entry<String, String> entry : attributes.entrySet()) {
			os.append("      <" + entry.getKey() + " value=\""
					+ entry.getValue() + "\"/>\n");
		}

		os.append("    </attributes>\n");
		os.append("    <weight value=\"" + weight + "\"/>\n");
		os.append("    <value value=\"" + value + "\"/>\n");
		os.append("    <equipable>\n");
		for (final String slot : slots) {
			os.append("      <slot name=\"" + slot + "\"/>\n");
		}
		os.append("    </equipable>\n");
		os.append("  </item>\n");
		return os.toString();
	}
}
