/***************************************************************************
 *                   (C) Copyright 2024 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.tarnow.blacksmith.forge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.server.entity.npc.behaviour.impl.MultiProducerBehaviour;

public class ForgeItems {
	private final static String helmet = "hełm ciemnomithrilowy";
	private final static String armor = "zbroja ciemnomithrilowa";
	private final static String legs = "spodnie ciemnomithrilowe";
	private final static String boots = "buty ciemnomithrilowe";
	private final static String shield = "tarcza ciemnomithrilowa";
	private final static String cloak = "płaszcz ciemnomithrilowy";

	private static HashSet<String> getNewItems() {
		final HashSet<String> productionItems = new HashSet<String>();
		productionItems.add(helmet);
		productionItems.add(armor);
		productionItems.add(legs);
		productionItems.add(boots);
		productionItems.add(shield);
		productionItems.add(cloak);

		return productionItems;
	}

	private static HashMap<String, Map<String, Integer>> getItemResources() {
		final Map<String, Integer> productionHelmet = new TreeMap<String, Integer>();
		productionHelmet.put("klejnot ciemnolitu", 4);
		productionHelmet.put("sztabka platyny", 9);
		productionHelmet.put("bryłka mithrilu", 16);
		productionHelmet.put("hełm z mithrilu", 1);

		final Map<String, Integer> productionArmor = new TreeMap<String, Integer>();
		productionArmor.put("klejnot ciemnolitu", 9);
		productionArmor.put("sztabka platyny", 10);
		productionArmor.put("bryłka mithrilu", 19);
		productionArmor.put("zbroja z mithrilu", 1);

		final Map<String, Integer> productionLegs = new TreeMap<String, Integer>();
		productionLegs.put("klejnot ciemnolitu", 3);
		productionLegs.put("sztabka platyny", 7);
		productionLegs.put("bryłka mithrilu", 15);
		productionLegs.put("spodnie z mithrilu", 1);

		final Map<String, Integer> productionBoots = new TreeMap<String, Integer>();
		productionBoots.put("klejnot ciemnolitu", 2);
		productionBoots.put("sztabka platyny", 8);
		productionBoots.put("bryłka mithrilu", 9);
		productionBoots.put("buty z mithrilu", 1);

		final Map<String, Integer> productionShield = new TreeMap<String, Integer>();
		productionShield.put("klejnot ciemnolitu", 5);
		productionShield.put("sztabka platyny", 14);
		productionShield.put("bryłka mithrilu", 24);
		productionShield.put("tarcza z mithrilu", 1);

		final Map<String, Integer> productionCloak = new TreeMap<String, Integer>();
		productionCloak.put("klejnot ciemnolitu", 7);
		productionCloak.put("sztabka platyny", 12);
		productionCloak.put("bryłka mithrilu", 17);
		productionCloak.put("płaszcz z mithrilu", 1);

		final HashMap<String, Map<String, Integer>> requiredResources = new HashMap<String, Map<String, Integer>>();
		requiredResources.put(helmet, productionHelmet);
		requiredResources.put(armor, productionArmor);
		requiredResources.put(legs, productionLegs);
		requiredResources.put(boots, productionBoots);
		requiredResources.put(shield, productionShield);
		requiredResources.put(cloak, productionCloak);

		return requiredResources;
	}
	
	private static HashMap<String, Integer> getProductionTime() {
		final HashMap<String, Integer> productionTime = new HashMap<String, Integer>();
		productionTime.put(helmet, 14 * 60);
		productionTime.put(armor, 14 * 60);
		productionTime.put(legs, 14 * 60);
		productionTime.put(boots, 14 * 60);
		productionTime.put(shield, 14 * 60);
		productionTime.put(cloak, 14 * 60);

		return productionTime;
	}
	
	private static HashMap<String, Boolean> getItemsBound() {
		final HashMap<String, Boolean> itemsBound = new HashMap<String, Boolean>();
		itemsBound.put(helmet, false);
		itemsBound.put(armor, false);
		itemsBound.put(legs, false);
		itemsBound.put(boots, false);
		itemsBound.put(shield, false);
		itemsBound.put(cloak, false);

		return itemsBound;
	}

	public static MultiProducerBehaviour getBehaviour() {
		final MultiProducerBehaviour behaviour = new MultiProducerBehaviour(
				"przemyslaw_newarms",
				Arrays.asList("forge", "make", "stwórz"),
				getNewItems(),
				getItemResources(),
				getProductionTime(),
				getItemsBound());

		return behaviour;
	}
}
