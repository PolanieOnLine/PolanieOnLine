/***************************************************************************
 *                   Copyright (C) 2003-2024 - Arianne                     *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

public class Shovel extends AreaUseItem {
	private static final Logger logger = Logger.getLogger(Shovel.class);

	private static final EntityManager em = SingletonRepository.getEntityManager();

	private static final String ring_quest_slot = "lost_engagement_ring";
	private static final String ring_quest_info = "pierścionek Ariego";

	private static final String glyph_fragment_quest_slot = "glyph_fragment";
	private static final Map<String, Double> fragmentChances = new HashMap<>();
	static {
		fragmentChances.put("zniszczony", 60.0);
		fragmentChances.put("spękany", 30.0);
		fragmentChances.put("nadkruszony", 15.0);
	}
	private static final Map<String, String> fragmentSprite = new HashMap<>();
	static {
		fragmentSprite.put("zniszczony", "damaged_glyph_fragment");
		fragmentSprite.put("spękany", "cracked_glyph_fragment");
		fragmentSprite.put("nadkruszony", "crumbled_glyph_fragment");
	}

	public Shovel(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item Item to copy.
	 */
	public Shovel(final Shovel item) {
		super(item);
	}

	@Override
	protected boolean onUsedInArea(final RPEntity user, final StendhalRPZone zone, final int x, final int y) {
		if (user instanceof Player) {
			final Player player = (Player) user;

			if (zone.getName().equals("0_athor_island")) {
				if (player.hasQuest(ring_quest_slot)) {
					final String[] slot = player.getQuest(ring_quest_slot).split(";");
					if (slot.length > 1 && !slot[0].equals("found_ring") && !slot[0].equals("done")) {
						try {
							final int ringX = Integer.parseInt(slot[0]);
							final int ringY = Integer.parseInt(slot[1]);

							if (x == ringX && y == ringY) {
								final Item ring = em.getItem("pierścień zaręczynowy");
								ring.setItemData(ring_quest_info);
								ring.setDescription("W wiecznym oddaniu Emmie.");
								ring.setBoundTo(player.getName());

								player.equipOrPutOnGround(ring);
								player.sendPrivateText(player.getGenderVerb("Znalazłeś") + " pierścionek.");

								// shift coordinates in case ring is lost & we need to access them again
								player.setQuest(ring_quest_slot, "found_ring;" + slot[0] + ";" + slot[1]);
							} else if (nearItem(x, y, ringX, ringY)) {
								player.sendPrivateText(
									"Widzisz ślady stóp na piasku. Ktoś musiał tu być.");
							}
						} catch (final NumberFormatException e) {
							logger.error("Malformatted quest slot " + ring_quest_slot, e);
						}
					}
				}
			}

			if (player.hasQuest(glyph_fragment_quest_slot)) {
				final String[] fragmentSlot = player.getQuest(glyph_fragment_quest_slot).split(";");
				if (fragmentSlot.length > 3 && "start".equals(fragmentSlot[0])) {
					final String expectedMap = fragmentSlot[2];
					final int fragmentX = Integer.parseInt(fragmentSlot[3]);
					final int fragmentY = Integer.parseInt(fragmentSlot[4]);
					final int attemptCount = Integer.parseInt(fragmentSlot[5]);

					if (zone.getName().equals(expectedMap) && nearDigRange(x, y, fragmentX, fragmentY, 1)) {
						String fragment = determineFragment();
						if (fragment != null) {
							final Item item = em.getItem("fragment glifu");

							String image = fragmentSprite.get(fragment);
							if (image != null) {
								item.setEntitySubclass(image);
							}
							item.setItemData(fragment + " " + item.getName());
							item.setDescription("Oto " + item.getItemData() + ". Dostarcz go Omarowi, jest szansa jeszcze jego zrekonstruowania!");

							player.addXP(1);
							player.equipOrPutOnGround(item);
							player.sendPrivateText(player.getGenderVerb("Znalazłeś") + " " + fragment + " " + item.getName() + "!");

							setGlifQuestAction(player, "found_fragment", fragment, expectedMap, fragmentX, fragmentY);
						} else {
							if (attemptCount < 2) {
								player.sendPrivateText("Spróbuj wkopać się głębiej!");
								setGlifQuestAction(player, "start", expectedMap, fragmentX, fragmentY, (attemptCount + 1));
							} else {
								int[] newCoordinates = getRandomCoordinates(zone);
								setGlifQuestAction(player, "start", expectedMap, newCoordinates[0], newCoordinates[1], 0);
								sendApproximateCoordinates(player, newCoordinates[0], newCoordinates[1]);
							}
						}
					} else if (zone.getName().equals(expectedMap) && nearItem(x, y, fragmentX, fragmentY)) {
						player.sendPrivateText("Widzisz ślady stóp na piasku. Ktoś już czegoś tutaj szukał, może fragment znajduje się niedaleko.");
					} else if (!zone.getName().equals(expectedMap)) {
						player.sendPrivateText("Tutaj tego chyba nie znajdę...");
					}
				}
			}
		}

		return true;
	}

	private void setGlifQuestAction(Player player, String questStatus, String mapName, int cordX, int cordY, int attempt) {
		setGlifQuestAction(player, questStatus, "", mapName, cordX, cordY, attempt);
	}

	private void setGlifQuestAction(Player player, String questStatus, String fragment, String mapName, int cordX, int cordY) {
		setGlifQuestAction(player, questStatus, fragment, mapName, cordX, cordY, 0);
	}

	private void setGlifQuestAction(Player player, String questStatus, String fragment, String mapName, int cordX, int cordY, int attempt) {
		player.setQuest(glyph_fragment_quest_slot, 0, questStatus);
		player.setQuest(glyph_fragment_quest_slot, 1, fragment);
		player.setQuest(glyph_fragment_quest_slot, 2, mapName);
		player.setQuest(glyph_fragment_quest_slot, 3, Integer.toString(cordX));
		player.setQuest(glyph_fragment_quest_slot, 4, Integer.toString(cordY));
		player.setQuest(glyph_fragment_quest_slot, 5, Integer.toString(attempt + 1));
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
	private void sendApproximateCoordinates(Player player, int fragmentX, int fragmentY) {
		Random random = new Random();

		// Generate random offsets within a small range (e.g., -5 to 5) for both X and Y.
		int approxX = fragmentX + random.nextInt(11) - 5;
		int approxY = fragmentY + random.nextInt(11) - 5;

		// Send a message to the player with the approximate coordinates.
		player.sendPrivateText("Niestety, w tym miejscu nie " + player.getGenderVerb("znalazłeś") + " tego czego szukasz. Czujesz, że fragment glifu może znajdować się w pobliżu: (" + approxX + ", " + approxY + ").");
	}

	/**
	 * Determines the type of fragment the player has found based on defined chances.
	 * 
	 * @return
	 *	 The type of fragment found or null if no fragment is found.
	 */
	private String determineFragment() {
		Random random = new Random();
		double randValue = random.nextDouble() * 100;

		double cumulativeChance = 0.0;
		for (Map.Entry<String, Double> entry : fragmentChances.entrySet()) {
			cumulativeChance += entry.getValue();
			if (randValue <= cumulativeChance) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Generates random X and Y coordinates within the map boundaries.
	 * 
	 * @param zone
	 *	 The zone (map) where the new coordinates should be generated.
	 * @return
	 *	 An array containing the new random X and Y coordinates.
	 */
	private int[] getRandomCoordinates(final StendhalRPZone zone) {
		Random random = new Random();

		int mapWidth = zone.getWidth();
		int mapHeight = zone.getHeight();

		int newX = random.nextInt(mapWidth);
		int newY = random.nextInt(mapHeight);

		return new int[] { newX, newY };
	}

	/**
	 * Checks if the player's digging attempt is within for example a 3x3 grid around the specified coordinates.
	 *
	 * @param px
	 *	 The player's X coordinate.
	 * @param py
	 *	 The player's Y coordinate.
	 * @param targetX
	 *	 The exact X coordinate of the target location.
	 * @param targetY
	 *	 The exact Y coordinate of the target location.
	 */
	private boolean nearDigRange(final int px, final int py, final int rx, final int ry, final int range) {
		return Math.abs(rx - px) <= range && Math.abs(ry - py) <= range;
	}

	/**
	 * Checks if a position is within 10 steps of where item is buried.
	 *
	 * @param px
	 *	 Player's X coordinate.
	 * @param py
	 *	 Player's Y coordinate.
	 * @param rx
	 *	 Item's X coordinate.
	 * @param ry
	 *	 Item's Y coordinate.
	 */
	private boolean nearItem(final int px, final int py, final int rx, final int ry) {
		return Math.abs(rx - px) < 6 && Math.abs(ry - py) < 6;
	}
}
