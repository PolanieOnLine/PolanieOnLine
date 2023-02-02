/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import java.util.Collection;
import java.util.LinkedList;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.core.rp.achievement.condition.QuestWithPrefixCompletedCondition;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AGrandfathersWish;

/**
 * Factory for quest achievements
 *
 * @author kymara
 */
public class FriendAchievementFactory extends AbstractAchievementFactory {
	public static final String ID_CHILD_FRIEND = "friend.quests.children";
	public static final String ID_PRIVATE_DETECTIVE = "friend.quests.find";
	public static final String ID_DRAGONS = "friend.quests.dragons";
	public static final String ID_BAD_DREAMS = "friend.quest.dreams";
	public static final String ID_PIZZA_DELIVERY = "friend.quest.pizza";
	public static final String ID_GOOD_SAMARITAN = "friend.karma.250";
	public static final String ID_MERCIFUL = "friend.karma.2000";
	public static final String ID_KILLER = "friend.playerkiller";
	public static final String ID_STILL_BELIEVING = "friend.meet.seasonal";
	public static final String ID_PET_FRIEND = "friend.pet.condition";
	public static final String ID_MARRIAGE = "friend.marriage";

	@Override
	protected Category getCategory() {
		return Category.FRIEND;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

	    // TODO: add Pacifist achievement for not participating in pvp for 6 months or more (last_pvp_action_time)

		// Befriend Susi and complete quests for all children
		achievements.add(createAchievement(
				ID_CHILD_FRIEND, "Przyjaciel Dzieci", "Ukończył zadania wszystkich dzieci",
				Achievement.MEDIUM_BASE_SCORE, true,
				new AndCondition(
						// Susi Quest is never set to done, therefore we check just if the quest has been started (condition "anyFriends" from FoundGirl.java)
						new QuestStartedCondition("susi"),
						// Help Tad, Semos Town Hall (Medicine for Tad)
						new QuestCompletedCondition("introduce_players"),
						// Plink, Semos Plains North
						new QuestCompletedCondition("plinks_toy"),
						// Anna, in Ados
						new QuestCompletedCondition("toys_collector"),
						// Sally, Orril River
						// 'completed' doesn't work for Sally - return player.hasQuest(QUEST_SLOT) && !"start".equals(player.getQuest(QUEST_SLOT)) && !"rejected".equals(player.getQuest(QUEST_SLOT));
						new AndCondition(new QuestActiveCondition("campfire"), new QuestNotInStateCondition("campfire", "start")),
						// Annie, Kalavan city gardens
						new QuestStateStartsWithCondition("icecream_for_annie","eating;"),
						// Elisabeth, Kirdneh
						new QuestStateStartsWithCondition("chocolate_for_elisabeth","eating;"),
						// Jef, Kirdneh
						new QuestCompletedCondition("find_jefs_mom"),
						// Hughie, Ados farmhouse
						new AndCondition(new QuestActiveCondition("fishsoup_for_hughie"), new QuestNotInStateCondition("fishsoup_for_hughie", "start")),
						// Finn Farmer, George
						new QuestCompletedCondition("coded_message"),
						// Marianne, Deniran City S
						new AndCondition(
								new QuestActiveCondition("eggs_for_marianne"),
								new QuestNotInStateCondition("eggs_for_marianne", "start")
						)
				)));

		achievements.add(createAchievement(
				ID_BAD_DREAMS, "Spokojny Sen", "Pomógł pozbyć się złych koszmarów małej dziewczynki Alicji",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new QuestCompletedCondition("kill_dragons")));

		// quests about finding people
		achievements.add(createAchievement(
				ID_PRIVATE_DETECTIVE, "Prywatny Detektyw", "Odnalazł wszystkie zagubione i ukrywające się aniołki oraz osoby",
				Achievement.HARD_BASE_SCORE, true,
				new AndCondition(
						// Rat Children (Agnus)
						new QuestCompletedCondition("find_rat_kids"),
						// Find Ghosts (Carena)
						new QuestCompletedCondition("find_ghosts"),
						// Meet Angels (any of the cherubs)
						new ChatCondition() {
							@Override
							public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
								if (!player.hasQuest("seven_cherubs")) {
									return false;
								}
								final String npcDoneText = player.getQuest("seven_cherubs");
								final String[] done = npcDoneText.split(";");
								final int left = 7 - done.length;
								return left < 0;
							}
						},
						// Jef, Kirdneh
						new QuestCompletedCondition("find_jefs_mom"),
						// Elias Breland, Deniran
						new QuestCompletedCondition(AGrandfathersWish.QUEST_SLOT)
					)));

		// quests about finding dragons
		achievements.add(createAchievement(
				ID_DRAGONS, "Przyjaciel Smoków", "Odnalazł wszystkie ukrywające się smoki",
				Achievement.HARD_BASE_SCORE, true,
				new AndCondition(
						// Meet Dragons
						new ChatCondition() {
							@Override
							public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
								if (!player.hasQuest("where_dragon")) {
									return false;
								}
								final String npcDoneText = player.getQuest("where_dragon");
								final String[] done = npcDoneText.split(";");
								final int left = 13 - done.length;
								return left < 0;
							}
						})));

		// earn over 250 karma
		achievements.add(createAchievement(
				ID_GOOD_SAMARITAN, "Dobry Samarytanin", "Zdobył 250 karmy",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
						return player.getKarma()>250;
					}
				}
		));

		achievements.add(createAchievement(
				ID_MERCIFUL, "Człowiek Miłosierny", "Zdobył 2,000 karmy",
				Achievement.HARD_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
						return player.getKarma()>2000;
					}
				}
		));

		achievements.add(createAchievement(
				ID_KILLER, "Aberratio Ictus", "Zabił innego gracza",
				Achievement.EASY_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
						return player.isBadBoy();
					}
				}
		));

		// meet Santa Claus, Easter Bunny and Guslarz
		achievements.add(createAchievement(
				ID_STILL_BELIEVING, "Wciąż Wierzy", "Spotkał Świętego Mikołaja, zajączka Wielkanocnego i Guślarza",
				Achievement.EASY_BASE_SCORE, true,
				new AndCondition(
							new QuestWithPrefixCompletedCondition("meet_santa_"),
							new QuestWithPrefixCompletedCondition("meet_bunny_"),
							new QuestWithPrefixCompletedCondition("meet_guslarz_"))));

		achievements.add(createAchievement(
				ID_PIZZA_DELIVERY, "Dostawca Pizzy", "Rozwiózł pizze w krainie Faiumoni lub Prasłowiańskiej",
				Achievement.EASY_BASE_SCORE, true,
				new OrCondition(
						new QuestCompletedCondition("pizza_delivery"),
						new QuestCompletedCondition("dostawca_pizzy2"))));

		achievements.add(createAchievement(
				ID_MARRIAGE, "Współmałżonkowie", "Zawarto więzy małżeńskie",
				Achievement.EASY_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
						return player.hasQuest("spouse");
					}
				}));

		return achievements;
	}
}
