/***************************************************************************
 *                 (C) Copyright 2019-2024 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemdataItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class GlifFragments extends AbstractQuest {
	private static final String QUEST_SLOT = "glif_fragments";
	private final SpeakerNPC npc = npcs.get("Omar");
	private final String itemName = "fragment glifu";

	private final Random random = new Random();

	private static final String[] maps = {
		"0_desert_pyramid_nw",
		"0_desert_pyramid_ne",
		"0_desert_pyramid_sw",
		"0_desert_pyramid_se"
	};

	@Override
	public List<String> getHistory(Player player) {
		final List<String> res = new LinkedList<>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Omara, tajemniczego wędrowca na pustyni, i otrzymałem zadanie odnalezienia fragmentów glifów.");
		if ("start".equals(player.getQuest(QUEST_SLOT, 0))) {
			String questMap = player.getQuest(QUEST_SLOT, 2);
			int x = Integer.parseInt(player.getQuest(QUEST_SLOT, 3));
			int y = Integer.parseInt(player.getQuest(QUEST_SLOT, 4));
			int[] cords = sendApproximateCoordinates(player, x, y);
			res.add("Muszę poszukać fragmentu w regionie " + getQuestMapName(questMap) + ". Gdzieś w pobliżu (#'" + cords[0] + "', #'" + cords[1] + "').");
		}
		if ("found_fragment".equals(player.getQuest(QUEST_SLOT, 0))) {
			String fragment = player.getQuest(QUEST_SLOT, 1);
			res.add("Udało się wykopać " + fragment + " " + itemName + ".");
		}
		if ("done".equals(player.getQuest(QUEST_SLOT, 0))) {
			res.add("Dostarczyłem Omarowi fragmenty glifów i otrzymałem nagrodę.");
		}
		return res;
	}

	private void step_1() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"W tejże pustyni możesz odnaleźć wiele skarbów zasypanych w piasku... Mnie interesują"
				+ " jedynie fragmenty zapomnianych glifów. Zechcesz mi pomóc w ich odnalezieniu?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, 0, "start"),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser npc) {
					String questMap = player.getQuest(QUEST_SLOT, 2);
					int x = Integer.parseInt(player.getQuest(QUEST_SLOT, 3));
					int y = Integer.parseInt(player.getQuest(QUEST_SLOT, 4));
					int[] cords = sendApproximateCoordinates(player, x, y);
					npc.say("Udaj się proszę w taki region jak " + getQuestMapName(questMap) + ". Przybliżone miejsce"
						+ " gdzie mogą się znajdować fragmenty to (#'" + cords[0] + "', #'" + cords[1] + "').");
				}
			});

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Rozumiem, że jesteś zainteresowany ponownym szukaniem fragmentu?",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser npc) {
					String questMap = getRandomMap();
					int[] cords = getRandomCoordinates();
					int[] similiarCords = sendApproximateCoordinates(player, cords[0], cords[1]);
					npc.say("Świetnie, będziesz potrzebował do tego zadania łopaty, aby odnaleźć fragmenty."
						+ " Mogą one delikatnie różnić się w zależności w jakim stanie go wykopiesz."
						+ " Pamiętam, że ostatnio zbadałem prawie wszystkie regiony prócz regionu " + getQuestMapName(questMap) + "."
						+ " Spróbuj tam poszukać! Przybliżone miejsce gdzie mogą się znajdować fragmenty to"
						+ " (#'" + similiarCords[0] + "', #'" + similiarCords[1] + "').");
					setStartQuestAction(player, questMap, cords);
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Rozumiem, nie każdy podejmuje się tak niebezpiecznych zadań.",
			new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, 0, "found_fragment"),
				new NotCondition(getItemWithItemdataCondition())),
			ConversationStates.ATTENDING,
			"A więc zgubiłeś swój wydobyty fragment? Może ci wypadł po drodze. Wróć tam gdzie został znaleziony i poszukaj go!",
			resetQuestAction());

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, 0, "found_fragment"),
				getItemWithItemdataCondition()),
			ConversationStates.ATTENDING,
			"Widzę, że odnalazłeś fragment glifu. Zechcesz mi go pokazać, abym mógł ocenić jego aktualny stan?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, 0, "found_fragment"),
				getItemWithItemdataCondition()),
			ConversationStates.ATTENDING,
			null,
			attemptToFixFragment());

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.NO_MESSAGES,
			new AndCondition(
				new QuestInStateCondition(QUEST_SLOT, 0, "found_fragment"),
				getItemWithItemdataCondition()),
			ConversationStates.ATTENDING,
			"Gdy zmienisz zdanie to wróć.",
			null);
	}

	/**
	 * Sends an approximate location of the buried item to the player.
	 *
	 * @param player
	 *	 The player to send the message to.
	 * @param fragmentX
	 *	 The exact X coordinate of the fragment.
	 * @param fragmentY
	 *	 The exact Y coordinate of the fragment.
	 */
	private int[] sendApproximateCoordinates(Player player, int fragmentX, int fragmentY) {
		Random random = new Random();
		// Generate random offsets within a small range (e.g., -8 to 8) for both X and Y.
		int approxX = Math.max(0, fragmentX + random.nextInt(11) - 8);
		int approxY = Math.max(0, fragmentY + random.nextInt(11) - 8);

		// Send a message to the player with the approximate coordinates.
		return new int[] { approxX, approxY };
	}

	private int[] getRandomCoordinates() {
		Random random = new Random();
		int x = random.nextInt(128);
		int y = random.nextInt(128);

		return new int[] { x, y };
	}

	private String getRandomMap() {
		return maps[random.nextInt(maps.length)];
	}

	private void setStartQuestAction(Player player, String mapName, int[] cords) {
		player.setQuest(QUEST_SLOT, 0, "start");
		player.setQuest(QUEST_SLOT, 1, "");
		player.setQuest(QUEST_SLOT, 2, mapName);
		player.setQuest(QUEST_SLOT, 3, Integer.toString(cords[0]));
		player.setQuest(QUEST_SLOT, 4, Integer.toString(cords[1]));
		player.setQuest(QUEST_SLOT, 5, "0");
	}

	private ChatCondition getItemWithItemdataCondition() {
		return new ChatCondition() {
			@Override
			public boolean fire(Player player, Sentence sentence, Entity npc) {
				if ("found_fragment".equals(player.getQuest(QUEST_SLOT, 0))) {
					if ("zniszczony".equals(player.getQuest(QUEST_SLOT, 1))) {
						return new PlayerHasItemdataItemWithHimCondition(itemName, "zniszczony" + " " + itemName).fire(player, sentence, npc);
					} else if ("spękany".equals(player.getQuest(QUEST_SLOT, 1))) {
						return new PlayerHasItemdataItemWithHimCondition(itemName, "spękany" + " " + itemName).fire(player, sentence, npc);
					} else if ("nadkruszony".equals(player.getQuest(QUEST_SLOT, 1))) {
						return new PlayerHasItemdataItemWithHimCondition(itemName, "nadkruszony" + " " + itemName).fire(player, sentence, npc);
					}
				}
				return false;
			}
		};
	}

	private String getQuestMapName(String map) {
		String direction = map.substring(map.length() - 2);

		switch (direction) {
			case "nw":
				return "#'północno-zachodniej' części pustyni";
			case "ne":
				return "#'północno-wschodniej' części pustyni";
			case "sw":
				return "#'południowo-zachodniej' części pustyni";
			case "se":
				return "#'południowo-wschodniej' części pustyni";
			default:
				return "pustyni";
		}
	}

	private ChatAction resetQuestAction() {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				int[] cords = new int[] {
					Integer.parseInt(player.getQuest(QUEST_SLOT, 3)),
					Integer.parseInt(player.getQuest(QUEST_SLOT, 4))
				};
				setStartQuestAction(player, player.getQuest(QUEST_SLOT, 2), cords);
			}
		};
	}

	private ChatAction attemptToFixFragment() {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				final String fragmentType = player.getQuest(QUEST_SLOT, 1);
				double repairChance = getRepairChance(fragmentType);

				if (repairChance > 0) {
					player.dropWithItemdata(itemName, fragmentType + " " + itemName);
					Random random = new Random();
					if (random.nextDouble() * 100 <= repairChance) {
						new MultipleActions(
							new IncreaseXPAction(5000),
							new IncreaseKarmaAction(5),
							new EquipItemAction(itemName),
							new SetQuestAction(QUEST_SLOT, "done;;;;;"),
							new IncrementQuestAction(QUEST_SLOT, 6, 1),
							new SayTextAction("Udało mi się naprawić fragment glifu. Proszę oto i ono!")
						).fire(player, sentence, raiser);
					} else {
						String questMap = getRandomMap();
						int[] cords = getRandomCoordinates();
						int[] similiarCords = sendApproximateCoordinates(player, cords[0], cords[1]);

						raiser.say("Niestety, nie udało się naprawić. Może w innym miejscu znajdziesz nowy fragment!"
							+ " Przy twojej nieobecności badałem ten region " + getQuestMapName(questMap) + "."
							+ " Spróbuj tam poszukać! Przybliżone miejsce gdzie mogą się znajdować fragmenty to"
							+ " (#'" + similiarCords[0] + "', #'" + similiarCords[1] + "').");
						setStartQuestAction(player, questMap, cords);
					}
				} else {
					raiser.say("Nie rozumiem, jakiego rodzaju fragment próbujesz naprawić.");
				}
			}
		};
	}

	/**
	 * Returns the chance of repairing a fragment based on its type.
	 * 
	 * @param fragmentType
	 *	 The type of the fragment (e.g., "zniszczony", "spękany", "nadkruszony").
	 * @return
	 *	 The percentage chance of successfully repairing the fragment.
	 */
	private double getRepairChance(final String fragmentType) {
		switch (fragmentType) {
			case "zniszczony":
				return 25.0;
			case "spękany":
				return 50.0;
			case "nadkruszony":
				return 75.0;
			default:
				return 0.0;
		}
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Odnalezienie Fragmentów Glifów",
				"Omar, wędrowiec z pustyni, poprosił cię o odnalezienie fragmentów starożytnych glifów.",
				false
			);
		step_1();
		step_2();
	}

	@Override
	public String getName() {
		return "Odnalezienie Fragmentów Glifów";
	}

	@Override
	public int getMinLevel() {
		return 75;
	}

	@Override
	public String getRegion() {
		return Region.DESERT;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return true;
	}
}
