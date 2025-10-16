/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.List;
import java.util.Locale;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

/**
 * Sets NPC to reverse path on collision
 *
 * @author AntumDeluge
 */
public class SetNPCPathReversible extends ScriptImpl {

	private static final String PREVIOUS_ACTION_ATTRIBUTE = "collision_action_before_reverse";

	@Override
	public void execute(Player admin, List<String> args) {
		super.execute(admin, args);
		if (args.size() != 2) {
			admin.sendPrivateText(NotificationType.ERROR,
				"/script SetNPCPathReversible npc true|false");
			return;
		}
		final String npcName = args.get(0);
		final SpeakerNPC npc = NPCList.get().get(npcName);
		if (npc == null) {
			admin.sendPrivateText(NotificationType.ERROR,
				"Brak NPC o nazwie \"" + npcName + "\".");
			return;
		}
		final String reverseOnCollision = args.get(1).toLowerCase(Locale.ROOT);
		if (reverseOnCollision.equals("true")) {
			enableReverse(npc);
		} else if (reverseOnCollision.equals("false")) {
			disableReverse(npc);
		} else {
			admin.sendPrivateText(NotificationType.ERROR,
				"Nieznany argument \"" + reverseOnCollision
					+ "\". Proszę używaj \"true\" lub \"false\".");
		}
	}

	private void enableReverse(SpeakerNPC npc) {
		if (npc.get(PREVIOUS_ACTION_ATTRIBUTE) == null) {
			final CollisionAction current = npc.getCollisionAction();
			if (current == null) {
				npc.remove(PREVIOUS_ACTION_ATTRIBUTE);
			} else {
				npc.put(PREVIOUS_ACTION_ATTRIBUTE, current.name());
			}
		}
		npc.setCollisionAction(CollisionAction.REVERSE);
	}

	private void disableReverse(SpeakerNPC npc) {
		final String storedAction = npc.get(PREVIOUS_ACTION_ATTRIBUTE);
		if (storedAction != null) {
			try {
				npc.setCollisionAction(CollisionAction.valueOf(storedAction));
			} catch (IllegalArgumentException ex) {
				npc.setCollisionAction(null);
			}
			npc.remove(PREVIOUS_ACTION_ATTRIBUTE);
		} else {
			npc.setCollisionAction(null);
		}
	}
}
