/***************************************************************************
*                      (C) Copyright 2024 - PolanieOnLine                 *
***************************************************************************/
/***************************************************************************
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 2 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
***************************************************************************/
package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.IMPROVE_DO;
import static games.stendhal.common.constants.Actions.IMPROVE_LIST;
import static games.stendhal.common.constants.Actions.ITEM_ID;
import static games.stendhal.common.constants.Actions.NPC;

import java.util.Locale;
import java.util.Map;

import games.stendhal.common.NotificationType;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ImproverAdder;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ImproveListEvent;
import games.stendhal.server.events.ImproveResultEvent;
import games.stendhal.server.util.EntityHelper;
import games.stendhal.server.core.engine.GameEvent;
import marauroa.common.game.RPAction;

public class ImproveAction implements ActionListener {
	private static final String BLACKSMITH_KEYWORD = "Tworzymir";
	private final boolean performUpgrade;

	private ImproveAction(final boolean performUpgrade) {
		this.performUpgrade = performUpgrade;
	}

	public static void register() {
		CommandCenter.register(IMPROVE_LIST, new ImproveAction(false));
		CommandCenter.register(IMPROVE_DO, new ImproveAction(true));
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		final SpeakerNPC npc = requireBlacksmith(player, action);
		if (npc == null) {
			return;
		}

		if (performUpgrade) {
			handleUpgrade(player, action, npc);
		} else {
			handleList(player, npc);
		}
	}

	private SpeakerNPC requireBlacksmith(final Player player, final RPAction action) {
		final String npcName = action.get(NPC);
		if (npcName == null) {
			player.sendPrivateText(NotificationType.ERROR, "Brakuje informacji o kowalu.");
			return null;
		}

		final Entity target = EntityHelper.entityFromTargetName(npcName, player);
		if (!(target instanceof SpeakerNPC) || !isTworzymir(target)) {
			player.sendPrivateText(NotificationType.ERROR, "Musisz być przy kowalu Tworzymirze, aby ulepszać przedmioty.");
			return null;
		}

		return (SpeakerNPC) target;
	}

	private boolean isTworzymir(final Entity entity) {
		return entity != null && entity.getName() != null && entity.getName().contains(BLACKSMITH_KEYWORD);
	}

		private void handleList(final Player player, final SpeakerNPC npc) {
		final ImproverAdder improverAdder = new ImproverAdder();
		final StringBuilder builder = new StringBuilder();
		boolean firstItem = true;

		builder.append("[");
		for (final Item item : player.getAllCarriedItems()) {
			if (!improverAdder.canImproveItem(item) || item.getID() == null) {
				continue;
			}

			final Map<String, Integer> requirements = improverAdder.getUpgradeRequirements(item);
			if (requirements == null) {
				continue;
			}

			final int fee = improverAdder.computeUpgradeFee(player, item);
			final double chance = improverAdder.getSuccessProbability(player, item) * 100.0;

			if (!firstItem) {
				builder.append(",");
			}
			firstItem = false;

			builder.append("{");
			builder.append("\"id\":").append(item.getID().getObjectID()).append(",");
			builder.append("\"name\":\"").append(escape(item.getName())).append("\",");
			final String icon = extractIcon(item);
			if (icon != null) {
				builder.append("\"icon\":\"").append(escape(icon)).append("\",");
			}
			builder.append("\"improve\":").append(item.getImprove()).append(",");
			builder.append("\"max\":").append(item.getMaxImproves()).append(",");
			builder.append("\"cost\":").append(fee).append(",");
			builder.append("\"chance\":").append(String.format(Locale.US, "%.2f", chance)).append(",");
			builder.append("\"requirements\":").append(buildRequirementsJson(requirements));
			builder.append("}");
		}
		builder.append("]");

		player.addEvent(new ImproveListEvent(npc.getName(), builder.toString()));
		player.notifyWorldAboutChanges();
	}

		private void handleUpgrade(final Player player, final RPAction action, final SpeakerNPC npc) {
		if (!action.has(ITEM_ID)) {
			player.sendPrivateText(NotificationType.ERROR, "Nie wybrano przedmiotu do ulepszenia.");
			return;
		}

		final int itemId = action.getInt(ITEM_ID);
		final Item item = findItem(player, itemId);
		if (item == null) {
			sendResult(player, npc, false, "Nie znaleziono wybranego przedmiotu.", itemId, null, null);
			return;
		}

		final ImproverAdder improverAdder = new ImproverAdder();
		if (!improverAdder.canImproveItem(item)) {
			sendResult(player, npc, false, "Tego przedmiotu nie da się ulepszyć.", itemId, item.getName(), extractIcon(item));
			return;
		}

		final Map<String, Integer> requirements = improverAdder.getUpgradeRequirements(item);
		if (requirements == null) {
			sendResult(player, npc, false, "Brak danych o wymaganych surowcach.", itemId, item.getName(), extractIcon(item));
			return;
		}

		final int fee = improverAdder.computeUpgradeFee(player, item);
		if (!MoneyUtils.hasEnoughMoney(player, fee)) {
			sendResult(player, npc, false, "Nie masz wystarczająco pieniędzy.", itemId, item.getName(), extractIcon(item));
			return;
		}

		if (!improverAdder.hasRequiredResources(player, item)) {
			sendResult(player, npc, false, "Brakuje wymaganych surowców.", itemId, item.getName(), extractIcon(item));
			return;
		}

		MoneyUtils.removeMoney(player, fee);
		improverAdder.dropNeededResources(player, item);

		final boolean success = improverAdder.isSuccessful(player, item);
		String message;
		if (success) {
			item.upgradeItem();
			player.incImprovedForItem(player.getName(), 1);
			player.incImprovedForItem(item.getName(), 1);
			new GameEvent(player.getName(), "upgraded-item", item.getName(), "+" + Integer.toString(item.getImprove())).raise();
			message = "Przedmiot został ulepszony.";
		} else {
			MoneyUtils.giveMoney(player, (int) (fee * 0.4));
			message = "Ulepszenie nie powiodło się. Zwracam część kosztów.";
		}

		sendResult(player, npc, success, message, itemId, item.getName(), extractIcon(item));
		player.notifyWorldAboutChanges();
	}

		private String extractIcon(final Item item) {
			if (item == null) {
				return null;
			}

			if (item.has("subclass")) {
				return item.get("subclass");
			}

			if (item.has("class")) {
				return item.get("class");
			}

			return null;
		}

		private void sendResult(final Player player, final SpeakerNPC npc, final boolean success, final String message,
				final int itemId, final String itemName, final String icon) {
			player.addEvent(new ImproveResultEvent(npc.getName(), success, message, itemId, itemName, icon));
		}

	private Item findItem(final Player player, final int itemId) {
		for (final Item item : player.getAllCarriedItems()) {
			if (item.getID() != null && item.getID().getObjectID() == itemId) {
				return item;
			}
		}

		return null;
	}

	private String buildRequirementsJson(final Map<String, Integer> requirements) {
		final StringBuilder builder = new StringBuilder();
		boolean first = true;

		builder.append("{");
		for (final Map.Entry<String, Integer> entry : requirements.entrySet()) {
			if (!first) {
				builder.append(",");
			}
			first = false;
			builder.append("\"").append(escape(entry.getKey())).append("\":").append(entry.getValue());
		}
		builder.append("}");
		return builder.toString();
	}

	private String escape(final String input) {
		if (input == null) {
			return "";
		}
		return input.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
