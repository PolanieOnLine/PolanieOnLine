/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions;

import games.stendhal.common.NotificationType;
import games.stendhal.server.actions.admin.AdministrationAction;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.group.Group;
import games.stendhal.server.entity.player.GagManager;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.GroupChangeEvent;
import marauroa.common.game.RPAction;

import org.apache.log4j.Logger;

/**
 * handles the management of player groups.
 *
 * @author hendrik
 */
public class GroupManagementAction implements ActionListener {
	private static Logger logger = Logger.getLogger(GroupManagementAction.class);

	/**
	 * registers the trade action
	 */
	public static void register() {
		CommandCenter.register("group_management", new GroupManagementAction());
	}

	/**
	 * processes the requested action.
	 * 
	 * @param player the caller of the action
	 * @param action the action to be performed
	 */
	@Override
	public void onAction(final Player player, final RPAction action) {

		// vaidate parameters
		String actionStr = action.get("action");
		String params = action.get("params");
		if ((actionStr == null) || (params == null)) {
			logger.warn("missing action attribute in RPAction " + action);
			return;
		}

		new GameEvent(player.getName(), "group", params, actionStr).raise();

		// get target player
		Player targetPlayer = null;
		if (!actionStr.equals("lootmode") && !actionStr.equals("part") && !actionStr.equals("status") && !actionStr.equals("kick")) {
			targetPlayer = SingletonRepository.getRuleProcessor().getPlayer(params);
			if (targetPlayer == null) {
				if (params.trim().equals("")) {
					player.sendPrivateText(NotificationType.ERROR, "Użyj nazwy docelowego wojownika w tym poleceniu.");
				} else {
					player.sendPrivateText(NotificationType.ERROR, "Wojownik " + params + " nie jest teraz dostępny");
				}
				return;
			}
		}

		// dispatch sub actions
		if (actionStr.equals("invite")) {
			invite(player, targetPlayer);
		} else if (actionStr.equals("join")) {
			join(player, targetPlayer);
		} else if (actionStr.equals("leader")) {
			leader(player, targetPlayer);
		} else if (actionStr.equals("lootmode")) {
			lootmode(player, params);
		} else if (actionStr.equals("kick")) {
			kick(player, params);
		} else if (actionStr.equals("part")) {
			part(player);
		} else if (actionStr.equals("status")) {
			status(player);
		} else {
			unknown(player, actionStr, params);
		}
	}

	/**
	 * invited a player to join a group
	 *
	 * @param player Player who invites
	 * @param targetPlayer player who is invited
	 */
	private void invite(Player player, Player targetPlayer) {

		// TODO: make checks reusable, some of these checks are used at many places

		if (!player.getChatBucket().checkAndAdd(0)) {
			return;
		}
		if (player == targetPlayer) {
			player.sendPrivateText(NotificationType.ERROR, "Nie możesz zaprosić siebie do grupy.");
			return;
		}
		if (targetPlayer.isGhost() && (player.getAdminLevel() < AdministrationAction.getLevelForCommand("ghostmode").intValue())) {
			player.sendPrivateText(NotificationType.ERROR, "Wojownik " + targetPlayer.getName() + " nie jest teraz dostępny");
			return;
		}

		if (GagManager.checkIsGaggedAndInformPlayer(player)) {
			return;
		}

		if (targetPlayer.getIgnore(player.getName()) != null) {
			return;
		}

		if (targetPlayer.getAwayMessage() != null) {
			player.sendPrivateText("" + targetPlayer.getName() + " jest oddalony.");
			return;
		}

		if (targetPlayer.getGrumpyMessage() != null) {
			player.sendPrivateText(targetPlayer.getName() + " zamknął swój umysł i szuka spokoju z wyjątkiem najbliższych przyjaciół");
			return;
		}

		// check if the target player is already in a group
		Group group = SingletonRepository.getGroupManager().getGroup(targetPlayer.getName());
		if (group != null) {
			player.sendPrivateText(NotificationType.ERROR, targetPlayer.getName() + " już należy do grupy.");
			return;
		}

		// get group, create it, if it does not exist
		SingletonRepository.getGroupManager().createGroup(player.getName());
		group = SingletonRepository.getGroupManager().getGroup(player.getName());

		// check leader
		if (!group.hasLeader(player.getName())) {
			player.sendPrivateText(NotificationType.ERROR, "Tylko lider grupy może zapraszać.");
			return;
		}

		// check if there is space left in the group
		if (group.isFull()) {
			player.sendPrivateText(NotificationType.ERROR, "Twoja grupa jest w komplecie.");
			return;
		}

		// invite
		group.invite(player, targetPlayer);
		player.sendPrivateText("Zaprosiłeś wojownika " + targetPlayer.getName() + ", aby dołączył do grupy.");
	}

	/**
	 * joins a group
	 *
	 * @param player Player who wants to join a group
	 * @param targetPlayer leader of the group
	 */
	private void join(Player player, Player targetPlayer) {
		// check if the player is already in a group
		Group group = SingletonRepository.getGroupManager().getGroup(player.getName());
		if (group != null) {
			player.sendPrivateText(NotificationType.ERROR, "Jesteś już członkiem grupy.");
			return;
		}

		// check if the target player is in a group and that this group has invited the player
		group = SingletonRepository.getGroupManager().getGroup(targetPlayer.getName());
		if ((group == null) || !group.hasBeenInvited(player.getName())) {
			player.sendPrivateText(NotificationType.ERROR, "Nie zostałeś zaproszony do grupy lub zaproszenie wygasło.");
			return;
		}

		// check if there is space left in the group
		if (group.isFull()) {
			player.sendPrivateText(NotificationType.ERROR, "Grupa jest już w komplecie.");
			return;
		}

		group.addMember(player.getName());
	}


	/**
	 * changes the leadership of the group
	 *
	 * @param player Player who invites
	 * @param targetPlayer new leader
	 */
	private void leader(Player player, Player targetPlayer) {

		// check if the player is in a group
		Group group = SingletonRepository.getGroupManager().getGroup(player.getName());
		if (group == null) {
			player.sendPrivateText(NotificationType.ERROR, "Nie jesteś członkiem grupy.");
			return;
		}

		// check leader
		group = SingletonRepository.getGroupManager().getGroup(player.getName());
		if (!group.hasLeader(player.getName())) {
			player.sendPrivateText(NotificationType.ERROR, "Tylko lider grupy może wyznaczyć nowego lidera.");
			return;
		}

		// check if the target player is a member of this group
		if (!group.hasMember(targetPlayer.getName())) {
			player.sendPrivateText(NotificationType.ERROR, targetPlayer.getName() + " nie jest członkiem twojej grupy.");
			return;
		}

		// set leader
		group.setLeader(targetPlayer.getName());
	}



	/**
	 * changes the lootmode
	 *
	 * @param player leader
	 * @param lootmode new lootmode
	 */
	private void lootmode(Player player, String lootmode) {

		// check if the player is already in a group
		Group group = SingletonRepository.getGroupManager().getGroup(player.getName());
		if (group == null) {
			player.sendPrivateText(NotificationType.ERROR, "Nie jesteś członkiem grupy.");
			return;
		}

		// check leader
		group = SingletonRepository.getGroupManager().getGroup(player.getName());
		if (!group.hasLeader(player.getName())) {
			player.sendPrivateText(NotificationType.ERROR, "Tylko lider grupy może zmienić zbieracza.");
			return;
		}

		// check if the loot mode is valid
		if ((lootmode == null) || (!lootmode.equals("single") && !lootmode.equals("shared"))) {
			player.sendPrivateText(NotificationType.ERROR, "Poprawny tryb zbierania jest \"single\" i \"shared\".");
			return;
		}

		// set leader
		group.setLootmode(lootmode);
	}

	/**
	 * leave the group
	 *
	 * @param player player who wants to leave
	 */
	private void part(Player player) {
		Group group = SingletonRepository.getGroupManager().getGroup(player.getName());
		if (group == null) {
			player.sendPrivateText(NotificationType.ERROR, "Nie jesteś członkiem grupy.");
			return;
		}

		group.removeMember(player.getName());
	}


	/**
	 * status report about the group
	 *
	 * @param player player who wants a status report
	 */
	private void status(Player player) {
		Group group = SingletonRepository.getGroupManager().getGroup(player.getName());
		if (group == null) {
			// Send an empty event if the player is not a group member, and let
			// the client sort out that it was not about parting from a group.
			player.addEvent(new GroupChangeEvent());
			player.notifyWorldAboutChanges();
			return;
		}

		group.sendGroupChangeEvent(player);
	}


	/**
	 * kicks a player from the group
	 *
	 * @param player the leader doing the kick
	 * @param targetPlayer the kicked player
	 */
	private void kick(Player player, String targetPlayer) {

		// if someone tries to kick himself, do a normal /part
		if (player.getName().equals(targetPlayer)) {
			part(player);
			return;
		}

		// check in group
		Group group = SingletonRepository.getGroupManager().getGroup(player.getName());
		if (group == null) {
			player.sendPrivateText(NotificationType.ERROR, "Nie jesteś członkiem tej grupy.");
			return;
		}

		// check leader
		if (!group.hasLeader(player.getName())) {
			player.sendPrivateText(NotificationType.ERROR, "Tylko lider grupy może wyrzucać członków.");
			return;
		}

		// is the target player a member of this group?
		if (!group.hasMember(targetPlayer)) {
			player.sendPrivateText(NotificationType.ERROR, targetPlayer + " nie jest członkiem twojej grupy.");
			return;
		}
		
		// tell the members of the kick and remove the target player
		group.sendGroupMessage("Group", targetPlayer + " został wyrzucony przez " + player.getName());
		group.removeMember(targetPlayer);
	}

	/**
	 * sends an error messages on invalid an actions
	 * 
	 * @param player Player who executed the action
	 * @param action name of action
	 * @param params parameters for the action
	 */
	private void unknown(Player player, String action, String params) {
		player.sendPrivateText(NotificationType.ERROR, "Nieznana akcja grupy: " + action + " z parametrami " + params);
	}
}
