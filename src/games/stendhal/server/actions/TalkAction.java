/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.TALK;
import static games.stendhal.common.constants.Actions.TARGET;
import static games.stendhal.common.constants.Actions.TEXT;
import static games.stendhal.common.constants.Actions.ZONE;

import games.stendhal.server.actions.validator.StandardActionValidations;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.game.RPAction;

/** Starts a conversation with one NPC without broadcasting player speech. */
public class TalkAction implements ActionListener {
	public static void register() {
		CommandCenter.register(TALK, new TalkAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		if (player.getZone() == null || !action.has(ZONE)
				|| !player.getZone().getName().equals(action.get(ZONE))
				|| !action.has(TARGET)) {
			return;
		}
		final String target = action.get(TARGET);
		if (target == null || !target.startsWith("#")) {
			return;
		}
		final Entity entity;
		try {
			entity = EntityHelper.entityFromZoneByID(
					Integer.parseInt(target.substring(1)), player.getZone());
		} catch (final NumberFormatException e) {
			return;
		}
		if (!(entity instanceof SpeakerNPC)) {
			return;
		}
		final SpeakerNPC npc = (SpeakerNPC) entity;
		if (!npc.canStartConversationWith(player)) {
			player.sendPrivateText("Podejdź bliżej, aby porozmawiać.");
			return;
		}
		// Reuse chat permissions and throttling, never the public chat action.
		final RPAction greeting = new RPAction();
		greeting.put(TEXT, "hi");
		if (!StandardActionValidations.CHAT.validateAndInformPlayer(player, greeting)) {
			return;
		}
		npc.requestConversation(player);
		player.removeAwayStatus();
	}
}
