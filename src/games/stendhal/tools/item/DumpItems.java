/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.tools.item;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.LinkedHashMultimap;

import games.stendhal.server.core.config.CreatureGroupsXMLLoader;
import games.stendhal.server.core.config.ItemGroupsXMLLoader;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import games.stendhal.server.core.rule.defaultruleset.LowerCaseMap;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.creature.impl.DropItem;

/**
 * Alters number of loots a player has for an item.
 */
public class DumpItems extends ScriptImpl {

	private static Logger LOGGER = Logger.getLogger(DumpItems.class);
	private final Map<String, DefaultItem> classToItem = new HashMap<>();
	private LowerCaseMap<DefaultCreature> classToCreature = new LowerCaseMap<>();
	private final LinkedHashMultimap<String, DefaultCreature> droppedBy = LinkedHashMultimap.create();

	private void dump() {
		buildItemTables();
		buildCreatureTables();
		Map<String, String> overwrite = new HashMap<>();
		overwrite.put("wymioty", "0");
		overwrite.put("zdobyczny hełm", "0");
		overwrite.put("sierp", "0");

		overwrite.put("strzała", "0");
		overwrite.put("drewniany łuk", "0");
		overwrite.put("drewniana tarcza", "0");
		overwrite.put("włócznia", "0");
		overwrite.put("śnieżka", "0");

		overwrite.put("strzała żelazna", "5");
		overwrite.put("strzała złota", "20");
		overwrite.put("strzała płonąca", "50");
		overwrite.put("strzała lodowa", "60");
		overwrite.put("strzała ognia", "60");
		overwrite.put("strzała światła", "60");
		overwrite.put("strzała mroku", "60");

		overwrite.put("buty zbójcy", "300");

		overwrite.put("płaszcz z otchłani", "0");
		overwrite.put("płaszcz z mithrilu", "330");

		overwrite.put("maczuga cierniowa", "40");
		overwrite.put("kij przywoływania nieumarłych", "9999");
		overwrite.put("wzmocniony pierścień imperialny", "0");

		overwrite.put("miecz treningowy", "10");
		overwrite.put("dummy_melee_8", "9999");
		overwrite.put("dummy_ranged", "9999");
		overwrite.put("rózga GM", "0");

		overwrite.put("tarcza z mithrilu", "330");
		overwrite.put("hełm z mithrilu", "330");

		System.out.println("class; item; min; ; old; creature min; ; atk/rate; atk; rate; def; ; all creatures");
		for (DefaultItem item : classToItem.values()) {
			if ((item.getAttributes().get("atk") == null) && (item.getAttributes().get("def") == null)) {
				continue;
			}
			String oldMinLevel = item.getAttributes().get("min_level");
			if (oldMinLevel == null) {
				oldMinLevel = "0";
			}
			int oldMinLevelInt = Integer.parseInt(oldMinLevel);
			int minLevel = 9999;

			StringBuilder creatureLevels = new StringBuilder();
			for (DefaultCreature creature : droppedBy.get(item.getItemName())) {
				creatureLevels.append("; ");
				creatureLevels.append(creature.getLevel());
				if (minLevel > creature.getLevel()) {
					minLevel = creature.getLevel();
				}
			}
			int creatureMinLevel = minLevel;
			if (minLevel <= 5) {
				minLevel = 0;
			} else if (minLevel <= 10) {
				minLevel = 5;
			} else if (minLevel <= 20) {
				minLevel = 10;
			} else {
				minLevel = (int) Math.round(minLevel * 0.9);
			}

			if (oldMinLevelInt > minLevel  || (oldMinLevelInt > 0 && creatureMinLevel == 9999)) {
				minLevel = oldMinLevelInt;
			}
			if (overwrite.containsKey(item.getItemName())) {
				minLevel = Integer.parseInt(overwrite.get(item.getItemName()));
			}

			StringBuilder sb = new StringBuilder();
			sb.append(item.getItemClass());
			sb.append("; ");
			sb.append(item.getItemName());
			sb.append("; ");
			sb.append(minLevel);
			sb.append("; ");
			sb.append("; ");
			sb.append(oldMinLevel);
			sb.append("; ");
			sb.append(creatureMinLevel);
			sb.append("; ");
			sb.append("; ");
			sb.append(devide(item.getAttributes().get("atk"), item.getAttributes().get("rate")));
			sb.append("; ");
			sb.append(nullTrim(item.getAttributes().get("atk")));
			sb.append("; ");
			sb.append(nullTrim(item.getAttributes().get("rate")));
			sb.append("; ");
			sb.append(nullTrim(item.getAttributes().get("def")));
			sb.append("; ");
			sb.append(creatureLevels);

			System.out.println(sb);
		}

	}

	public static void main(String[] args) {
		DumpItems dumpItems = new DumpItems();
		// dumpItems.dump();
		dumpItems.dumpHealing();
	}

	private void dumpHealing() {
		buildItemTables();
		for (DefaultItem item : classToItem.values()) {
			if ((item.getAttributes().get("amount") == null) && (item.getAttributes().get("regen") == null)) {
				continue;
			}

			StringBuilder sb = new StringBuilder();
			sb.append(item.getItemClass());
			sb.append("; ");
			sb.append(item.getItemName());
			sb.append("; ");
			sb.append(nullTrim(item.getAttributes().get("amount")));
			sb.append("; ");
			sb.append(nullTrim(item.getAttributes().get("frequency")));
			sb.append("; ");
			sb.append(nullTrim(item.getAttributes().get("regen")));
			sb.append("; ");
			sb.append(nullTrim(item.getAttributes().get("immunization")));

			System.out.println(sb);
		}
	}

	private void buildItemTables() {
		try {
			final ItemGroupsXMLLoader loader = new ItemGroupsXMLLoader(new URI("/data/conf/items.xml"));
			final List<DefaultItem> items = loader.load();

			for (final DefaultItem item : items) {
				final String clazz = item.getItemName();

				if (classToItem.containsKey(clazz)) {
					LOGGER.warn("Repeated item name: " + clazz);
				}

				classToItem.put(clazz, item);
			}
		} catch (

		final Exception e) {
			LOGGER.error("items.xml could not be loaded", e);
		}
	}

	/**
	 * Build the creatures tables
	 */
	private void buildCreatureTables() {
		classToCreature = new LowerCaseMap<DefaultCreature>();

		final CreatureGroupsXMLLoader loader = new CreatureGroupsXMLLoader("/data/conf/creatures.xml");
		final List<DefaultCreature> creatures = loader.load();

		for (final DefaultCreature creature : creatures) {
			final String clazz = creature.getCreatureName();
			classToCreature.put(clazz, creature);
			for (DropItem drop : creature.getDropItems()) {
				if (drop.probability < 0.01) {
					System.out.println(drop.name + " " + creature.getLevel() + " " + drop.probability);
				}
				droppedBy.put(drop.name, creature);
			}
		}
	}

	private String devide(String atk, String rate) {
		if (atk == null || rate == null) {
			return "";
		}
		return String.format(Locale.GERMAN, "%.1f", ((float) Math.round(Float.parseFloat(atk) / Float.parseFloat(rate) * 10)) / 10);
	}

	private String nullTrim(String s) {
		if (s == null) {
			return "";
		}
		return s;
	}
}
