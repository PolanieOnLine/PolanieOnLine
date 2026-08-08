/***************************************************************************
 *                   (C) Copyright 2003-2026 - Marauroa                    *
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.rule.defaultruleset.creator.AbstractCreator;
import games.stendhal.server.core.rule.defaultruleset.creator.AttributesItemCreator;
import games.stendhal.server.core.rule.defaultruleset.creator.DefaultItemCreator;
import games.stendhal.server.core.rule.defaultruleset.creator.FullItemCreator;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityProfile;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.behavior.UseBehavior;
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

	/** Nullable XML override. Null means automatic eligibility. */
	private Boolean rarityEnabled;

	/** Named selection/modifier profile, ready for future drop profiles. */
	private String rarityProfile = ItemRarityProfile.DEFAULT_ID;

	/** Optional structured tooltip category override from XML. */
	private String tooltipCategory;

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
	 * value of that type. The content of the mapping is copied, so
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
	 * @param slots String list of slots separated by semicolon
	 */
	public void initializeActiveSlotsList(String slots) {
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
	 * @param res The status type and the resistance value
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

	/** Set the use behavior. */
	public void setBehavior(UseBehavior behavior) {
		this.useBehavior = behavior;
	}

	public Class< ? > getImplementation() {
		return implementation;
	}

	protected AbstractCreator<Item> buildCreator(final Class< ? > implementation) {
		Constructor< ? > construct;

		try {
			construct = implementation.getConstructor(new Class[] {
					String.class, String.class, String.class, Map.class });
			return new FullItemCreator(this, construct);
		} catch (final NoSuchMethodException ex) {
			// ignore and continue
		}

		try {
			construct = implementation.getConstructor(new Class[] { Map.class });
			return new AttributesItemCreator(this, construct);
		} catch (final NoSuchMethodException ex) {
			// ignore and continue
		}

		try {
			construct = implementation.getConstructor(new Class[] {});
			return new DefaultItemCreator(this, construct);
		} catch (final NoSuchMethodException ex) {
			// ignore and continue
		}

		return null;
	}

	public Item getItem() {
		return getItem(ItemCreationContext.defaultCreation());
	}

	public Item getItem(final ItemCreationContext creationContext) {
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

			if ((this.resistances != null) && (!this.resistances.isEmpty())) {
				item.initializeStatusResistancesList(resistances);
			}

			if ((this.activeSlotsList != null)
					&& (!this.activeSlotsList.isEmpty())) {
				item.initializeActiveSlotsList(this.activeSlotsList);
			}

			item.setUseBehavior(useBehavior);
			if (tooltipCategory != null) {
				item.put(ItemTooltip.CATEGORY_OVERRIDE, tooltipCategory);
			}
			item.configureRarity(rarityEnabled, rarityProfile, value);
			ItemRarityService.getInstance().initialize(item, creationContext);
		}

		return item;
	}

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

	public void setRarityEnabled(final Boolean rarityEnabled) {
		this.rarityEnabled = rarityEnabled;
	}

	public Boolean getRarityEnabled() {
		return rarityEnabled;
	}

	public void setRarityProfile(final String rarityProfile) {
		this.rarityProfile = rarityProfile == null
				? ItemRarityProfile.DEFAULT_ID : rarityProfile;
	}

	public String getRarityProfile() {
		return rarityProfile;
	}

	public void setTooltipCategory(final String tooltipCategory) {
		this.tooltipCategory = ItemTooltip.isValidCategory(tooltipCategory)
				? tooltipCategory : null;
	}

	public String getTooltipCategory() {
		return tooltipCategory;
	}

	public String getItemClass() {
		return clazz;
	}

	public void setItemClass(final String val) {
		clazz = val;
	}

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
		os.append("  <item name=\"" + name + "\"");
		if (rarityEnabled != null) {
			os.append(" rarity-enabled=\"" + rarityEnabled + "\"");
		}
		if (!ItemRarityProfile.DEFAULT_ID.equals(rarityProfile)) {
			os.append(" rarity-profile=\"" + rarityProfile + "\"");
		}
		if (tooltipCategory != null) {
			os.append(" tooltip-category=\"" + tooltipCategory + "\"");
		}
		os.append(">\n");
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
