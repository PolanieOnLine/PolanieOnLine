/***************************************************************************
 *                      (C) Copyright 2020 - Stendhal                      *
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

import java.util.Map;

import games.stendhal.common.NotificationType;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.SlotOwner;

/** 
 * If ring has been used by player, he will be invisible for monsters.
 * When a player attacks a creature, player become visible.
 *
 * @author KarajuSs
 */
public class RingOfInvisibility extends Item {
	public RingOfInvisibility(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public RingOfInvisibility(final RingOfInvisibility item) {
		super(item);
	}

	public static boolean isInvisible(final Player player) {
		return player.isInvisibleToCreatures();
	}

	public static void setVisible(Player player, boolean setVisible) {
		String on = Grammar.genderVerb(player.getGender(), "Założyłeś") + " pierścień na palec! Używaj go mądrze!";
		String off = "Pierścień stracił swoją moc ponieważ został zdjęty z dłoni.";

		if (setVisible == true) {
			player.sendPrivateText(NotificationType.EMOTE, on);

			player.stopAttack();
			player.setInvisible(setVisible);
			player.setVisibility(50);
		} else {
			player.sendPrivateText(NotificationType.ERROR, off);

			player.setInvisible(setVisible);
		  	player.setVisibility(100);
		}
	}

	@Override
	public boolean onEquipped(final RPEntity entity, final String slot) {
		if ((slot.equals("finger") || slot.equals("fingerb")) && entity instanceof Player) {
			setVisible((Player) entity, true);
		}

		return super.onEquipped(entity, slot);
	}

	@Override
	public boolean onUnequipped() {
		super.onUnequipped();

		SlotOwner owner = this.getContainerOwner();
		if (owner == null) {
			return false;
		}

		if (owner instanceof Player) {
			if (isInvisible((Player) owner)) {
				setVisible((Player) owner, false);

			  	return true;
			}
		}
		return false;
	}
}