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
package games.stendhal.server.events;

import static games.stendhal.common.constants.Events.DROPPEDLIST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.SyntaxException;

public class ItemLogEvent extends RPEvent {
	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(ItemLogEvent.class);

	private final List<DefaultItem> items;
	private final List<String> dropped;

	private final String[] itemClasses = { "armor", "ammunition", "axe", "belts",
			"boots", "cloak", "club", "dagger", "drink", "glove", "helmet",
			"jewellery", "legs", "magia", "missile", "money", "necklace",
			"ranged", "resource", "shield", "sword", "wand" };

	private final String[] itemExNames = { "ciupaga startowa", "złota ciupaga", "złota ciupaga z wąsem", "eliksir miłości",
			"złota ciupaga z dwoma wąsami", "złota ciupaga z trzema wąsami", "amulecik z mithrilu", "wymioty", "przędza z mithrilu",
			"ekskalibur", "strzała mroku", "strzała światła", "antyjad", "wosk pszczeli",
			"eliksir mroku", "ekstrakt litworowy", "jad kobry", "smoczy eliksir", "duży smoczy eliksir", "wełna",
			"mocna nalewka litworowa", "nalewka litworowa", "sok jabłkowy", "zupa grzybowa", "zupa rybna", "sukno z mithrilu",
			"zdobyczny hełm", "kryształ ametystu", "kryształ diamentu", "kryształ obsydianu", "kryształ rubinu",
			"dummy_ranged", "dummy_melee_8", "grudziarka", "końskie włosie", "olejek", "przędza", "przędza jedwabna",
			"puciera", "ruda cieni", "ruda miedzi", "ruda platyny", "ruda srebra", "ruda żelaza", "siarka", "płaszcz z mithrilu",
			"sztabka cieni", "sztabka miedzi", "sztabka platyny", "sztabka srebra", "sól", "trzcina cukrowa", "obsydianowy saks",
			"wypchany baran", "węgiel", "węgiel drzewny", "złote jajo", "świeca", "tarcza jaśniejąca", "miecz leworęczny",
			"miecz treningowy", "zwój czyszczący", "amulecik", "smocze pazury", "rózga GM", "filiżanka herbaty", "miecz praworęczny",
			"pas zabójcy", "rękawice zabójcy", "buty zabójcy", "maczuga cierniowa", "płaszcz czerwonego kapturka",
			"wzmocniona drewniana strzała", "zbroja akwalungowa", "mana", "sztylet praworęczny", "sztylet leworęczny" };
	private final String[] neededItemNames = { "banan", "borowik", "błazenek", "cebula", "cytryna", "czosnek", "dorsz", "duży ser", "dynia",
			"straszna dynia", "fasola pinto", "jabłko", "jabłko niezgody", "jajo", "jajo wielkanocne", "kalafior", "kanapka",
			"kapusta pekińska", "karp", "karp lustrzeń", "kiełbasa", "kiść winogron", "leszcz", "makrela", "marchew", "mięso", "szynka",
			"mufinka", "nakrapiane jajo", "okoń", "opieńka miodowa", "orzech włoski", "oscypek", "palia alpejska", "pieczarka", "pomarańcza",
			"pomidor", "por", "pstrąg", "płotka", "rzepa", "rzodkiewka", "ser", "stek", "szczupak", "szpinak", "tabliczka czekolady", "tarta",
			"tarta z rybnym nadzieniem", "truskawka", "udko", "wisienka", "zielone jabłusko", "ziemniaki", "babka lekarska", "mięta", "pokrzywa",
			"pluszowy miś", "pióro herszta hordy zbójeckiej", "róg demona", "róg jednorożca", "serce olbrzyma", "truchło nietoperza",
			"truchło wampira", "zima zaklęta w kuli", "czarny pierścień", "pierścień cesarski", "pierścień leczniczy", "pierścień niewidzialności",
			"pierścień skorupy żółwia", "pierścień ukojenia", "pierścień szmaragdowy", "pierścień z mithrilu", "pierścień zdrowia", "pierścień izolacji",
			"balonik", "biały balonik", "zwój semos", "niezapisany zwój", "zwój ados", "zwój deniran", "zwój fado", "zwój nalwor" };

	public static void generateRPClass() {
		try {
			final RPClass rpclass = new RPClass(DROPPEDLIST);
			rpclass.addAttribute("dropped_items", Type.VERY_LONG_STRING, Definition.PRIVATE);
		} catch (final SyntaxException e) {
			logger.error("cannot generateRPClass", e);
		}
	}

	public ItemLogEvent(final Player player) {
		super(DROPPEDLIST);

		items = new ArrayList<>();
		dropped = new ArrayList<>();

		final StringBuilder formatted = new StringBuilder();
		final EntityManager em = SingletonRepository.getEntityManager();

		int itemCount = 0;
		for (final DefaultItem i : em.getDefaultItems()) {
			itemCount = player.getNumberOfLootsForItem(i.getItemName());
			if (itemCount > 0) {
				dropped.add(i.getItemName());
			}

			for (final String classes : itemClasses) {
				if (i.getItemClass().equals(classes)) {
					items.add(i);
				}
			}
			for (final String name : neededItemNames) {
				if (i.getItemName().equals(name)) {
					items.add(i);
				}
			}
			for (final String exName : itemExNames) {
				if (i.getItemName().equals(exName)) {
					items.remove(i);
				}
			}
		}

		// sort alphabetically
		final Comparator<DefaultItem> sortNames = new Comparator<DefaultItem>() {
			@Override
			public int compare(final DefaultItem i1, final DefaultItem i2) {
				return (i1.getItemName().toLowerCase().compareTo(i2.getItemName().toLowerCase()));
			}
		};
		Collections.sort(items, sortNames);

		formatted.append(getFormattedString(items, player));
		put("dropped_items", formatted.toString());
	}

	private String getFormattedString(final List<DefaultItem> items, final Player player) {
		final StringBuilder sb = new StringBuilder();
		final int itemCount = items.size();
		int idx = 0;

		for (final DefaultItem item: items) {
			String name = item.getItemName();
			Boolean looted = false;

			if (dropped.contains(name)) {
				looted = true;
			}

			int itemDropCount = 0;
			itemDropCount = player.getNumberOfLootsForItem(name);
			sb.append(name + "," + looted.toString() + "," + itemDropCount + "," + item.getItemClass() + "," + item.getItemSubclass());
			if (idx != itemCount - 1) {
				sb.append(";");
			}

			idx++;
		}

		return sb.toString();
	}
}
