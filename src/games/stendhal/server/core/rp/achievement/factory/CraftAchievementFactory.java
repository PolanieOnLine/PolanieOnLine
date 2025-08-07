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
package games.stendhal.server.core.rp.achievement.factory;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerProducedNumberOfItemsCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestsCountCompletedCondition;
import games.stendhal.server.entity.player.Player;

public class CraftAchievementFactory extends AbstractAchievementFactory {
	private static final String[][] CIUPAGA_DATA = {
		{ "craft.zlota.ciupaga", "Złota Ciupaga", "Wykonano złotą ciupagę u Kowala Andrzeja", "zlota_ciupaga", Integer.toString(Achievement.EASY_BASE_SCORE) },
		{ "craft.zlota.ciupaga.1", "Złota Ciupaga z Wąsem", "Wykonano złotą ciupagę z wąsem u Józka", "zlota_ciupaga_was", Integer.toString(Achievement.MEDIUM_BASE_SCORE) },
		{ "craft.zlota.ciupaga.2", "Złota Ciupaga z Dwoma Wąsami", "Wykonano złotą ciupagę z dwoma wąsami u Krasnoluda", "ciupaga_dwa_wasy", Integer.toString(Achievement.HARD_BASE_SCORE) },
		{ "craft.zlota.ciupaga.3", "Złota Ciupaga z Trzema Wąsami", "Wykonano złotą ciupagę z trzema wąsami u Hadrina", "ciupaga_trzy_wasy", Integer.toString(Achievement.EXTREME_BASE_SCORE) }
	};

	private static final List<String> uniqueItemQuests = Arrays.asList(
		"zlota_ciupaga",
		"zlota_ciupaga_was",
		"ciupaga_dwa_wasy",
		"ciupaga_trzy_wasy",
		"zamowienie_strazy",
		"zloty_pierscien",
		"obsidian_knife",
		"mithrilshield_quest",
		"mithril_cloak",
		"immortalsword_quest"
	);

	private ChatCondition firstCraft() {
		return new OrCondition(
			uniqueItemQuests.stream()
				.map(QuestCompletedCondition::new)
				.toArray(ChatCondition[]::new)
		);
	}

	private ChatCondition countCraftedItems(int count) {
		return new QuestsCountCompletedCondition(uniqueItemQuests, count);
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(
			"craft.novice", "Rzemieślnik Polany",
			"Wykonaj swój pierwszy unikalny przedmiot u NPC",
			Achievement.EASY_BASE_SCORE, true,
			firstCraft()
		));

		achievements.add(createAchievement(
			"craft.masterartisan", "Twórca Artefaktów",
			"Wykonaj 10 różnych unikalnych przedmiotów u NPC",
			Achievement.MEDIUM_BASE_SCORE, true,
			countCraftedItems(10)
		));

		for (String[] data : CIUPAGA_DATA) {
			String id = data[0];
			String title = data[1];
			String description = data[2];
			String questId = data[3];
			int baseScore = Integer.parseInt(data[4]);

			achievements.add(createAchievement(
				id,
				title,
				description,
				baseScore,
				true,
				new QuestCompletedCondition(questId)
			));
		}

		achievements.add(createAchievement(
			"craft.glyph.creator", "Rzeźbiarz Mocy",
			"Stwórz jeden z pradawnych glifów u mistrza run Zoryka",
			Achievement.MEDIUM_BASE_SCORE, true,
			new ChatCondition() {
				@Override
				public boolean fire(Player player, Sentence sentence, Entity npc) {
					for (final String glyphs: Arrays.asList(
							"glif daru mokoszy",
							"glif siły",
							"glif peruna",
							"glif czaszy",
							"glif tarczy",
							"glif swaroga",
							"glif tytana",
							"glif strzyboga",
							"glif jarowita",
							"glif kryzysu",
							"glif krwi")) {
						if (player.getQuantityOfProducedItems(glyphs) >= 1) {
							return true;
						}
					}
					return false;
				}
			}
		));

		achievements.add(createAchievement(
			"craft.smith.try", "Szept Stali",
			"Stwórz swoją pierwszą część wyposażenia ciemnomithrilowego",
			Achievement.HARD_BASE_SCORE, true,
			new QuestCompletedCondition("forge_newarms")
		));

		achievements.add(createAchievement(
			"craft.smith.perfection", "Ręka Swaroga",
			"Stwórz pełne wyposażenie ciemnomithrilowe",
			Achievement.EXTREME_BASE_SCORE, true,
			new PlayerProducedNumberOfItemsCondition(1, new String[] {
				"hełm ciemnomithrilowy",
				"zbroja ciemnomithrilowa",
				"pas ciemnomithrilowy",
				"spodnie ciemnomithrilowe",
				"buty ciemnomithrilowe",
				"tarcza ciemnomithrilowa",
				"płaszcz ciemnomithrilowy"})
		));

		achievements.add(createAchievement(
			"craft.jeweller.magnatering", "Znak Magnata",
			"Stwórz pierścień przeznaczony dla najwyższych warstw społecznych",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestCompletedCondition("pierscien_magnata")
		));

		return achievements;
	}

	@Override
	protected Category getCategory() {
		return Category.CRAFT;
	}
}
