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

	private static final String glif_fragments_quest_slot = "glif_fragments";
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

			if (player.hasQuest(glif_fragments_quest_slot)) {
				final String[] fragmentSlot = player.getQuest(glif_fragments_quest_slot).split(";");
				if (fragmentSlot.length > 3 && "start".equals(fragmentSlot[0])) {
					final String expectedMap = fragmentSlot[1];
					final int fragmentX = Integer.parseInt(fragmentSlot[2]);
					final int fragmentY = Integer.parseInt(fragmentSlot[3]);

					if (zone.getName().equals(expectedMap) && x == fragmentX && y == fragmentY) {
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

							player.setQuest(glif_fragments_quest_slot, "found_fragment;" + fragment + ";" + fragmentSlot[1] + ";" + fragmentSlot[2] + ";" + fragmentSlot[3]);
						} else {
							player.sendPrivateText("Niestety, nie udało ci się wykopać żadnego fragmentu. Spróbuj w innym miejscu.");

							int[] newCoordinates = getRandomCoordinates(zone);
							player.setQuest(glif_fragments_quest_slot, "start;" + expectedMap + ";" + newCoordinates[0] + ";" + newCoordinates[1]);
							sendApproximateCoordinates(player, newCoordinates[0], newCoordinates[1]);
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
		player.sendPrivateText("Czujesz, że fragment glifu może znajdować się w pobliżu: (" + approxX + ", " + approxY + ").");
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
