/***************************************************************************
 *                   (C) Copyright 2003-2015 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item.consumption;

import java.util.Arrays;
import java.util.List;

import games.stendhal.common.NotificationType;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.ConsumableItem;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.DrunkStatus;
import games.stendhal.server.entity.status.EatStatus;
import games.stendhal.server.entity.status.StatusType;

/**
 * eats food or drinks
 */
class Eater implements Feeder {
	private static final int COUNT_CHOKING_TO_DEATH = 8;
	private static final int COUNT_CHOKING = 5;
	private static final int COUNT_FULL = 4;

	@Override
	public boolean feed(final ConsumableItem item, final Player player) {
		int count = player.getStatusList().countStatusByType(StatusType.EATING);
		if (count > COUNT_CHOKING_TO_DEATH) {
			int playerHP = player.getHP();
			int chokingDamage = damage(2 * playerHP / 3);
			player.setHP(playerHP - chokingDamage);

			player.sendPrivateText(NotificationType.NEGATIVE,
					player.getGenderVerb("Zjadłeś")
					+ " tak dużo, że "
					+ player.getGenderVerb("zwymiotowałeś")
					+ " na ziemię i "
					+ player.getGenderVerb("straciłeś")
					+ " " + Integer.toString(chokingDamage) + " punkt" + " życia.");

			final Item sick = SingletonRepository.getEntityManager().getItem("wymioty");
			player.getZone().add(sick);
			sick.setPosition(player.getX(), player.getY() + 1);
			player.getStatusList().removeAll(EatStatus.class);
			player.notifyWorldAboutChanges();
			return false;
		}

		if (count > COUNT_CHOKING) {
			// remove some HP so they know we are serious about this
			int playerHP = player.getHP();
			int chokingDamage = damage(playerHP / 3);
			player.setHP(playerHP - chokingDamage);

			player.sendPrivateText(NotificationType.NEGATIVE,
					player.getGenderVerb("Zjadłeś")
					+ " tak dużo na raz, że " 
					+ player.getGenderVerb("zadławiłeś")
					+ " się jedzeniem i "
					+ player.getGenderVerb("straciłeś")
					+ " " + Integer.toString(chokingDamage) + " punkt" + " życia. Jeżeli zjesz więcej to możesz się pochorować.");

			player.notifyWorldAboutChanges();
		} else if (count > COUNT_FULL) {
			player.sendPrivateText(NotificationType.PRIVMSG, "Jesteś teraz " + player.getGenderVerb("najedzony") + " i już więcej nie " + player.getGenderVerb("powinieneś") + " jeść.");
		}

		ConsumableItem splitOff = (ConsumableItem) item.splitOff(1);
		EatStatus status = new EatStatus(splitOff.getAmount(), splitOff.getFrecuency(), splitOff.getRegen());
		player.getStatusList().inflictStatus(status, splitOff);

		List<String> alcoholicDrinks = Arrays.asList("sok z chmielu", "napój z oliwką", "napój z winogron", "mocna nalewka litworowa", "leżakowana nalewka litworowa");
		if (alcoholicDrinks.contains(item.getName())) {
			DrunkStatus drunkStatus = new DrunkStatus();
			player.getStatusList().inflictStatus(drunkStatus, item);
		}
		return true;
	}

	/**
	 * Get damage done by overeating.
	 *
	 * @param maxDamage upper limit of damage
	 * @return random damage between 0 and maxDamage - 1
	 */
	private int damage(int maxDamage) {
		// Avoid calling rand(0)
		if (maxDamage > 0) {
			return Rand.rand(maxDamage);
		}
		return 0;
	}
}
