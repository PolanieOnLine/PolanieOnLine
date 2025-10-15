/***************************************************************************
 *                   (C) Copyright 2024 - Polanie OnLine                   *
 ***************************************************************************/
package games.stendhal.client.gui.npc;

import java.util.HashSet;
import java.util.Set;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.entity.NPC;
import marauroa.common.game.RPAction;

/**
 * Manages the lifecycle of NPC interactions initiated through the context menu.
 */
public final class NPCInteractionManager {
	private static final String GREETING_COMMAND = "witaj";
	private static final String JOB_COMMAND = "praca";
	private static final String OFFER_COMMAND = "oferta";
	private static final String HELP_COMMAND = "pomoc";
	private static final String QUEST_COMMAND = "zadanie";
	private static final String GOODBYE_COMMAND = "do widzenia";

	private static final NPCInteractionManager INSTANCE = new NPCInteractionManager();

	private final Set<Integer> activeNpcIds = new HashSet<Integer>();

	private NPCInteractionManager() {
		// singleton
	}

	/**
	 * Retrieves singleton instance.
	 *
	 * @return manager instance
	 */
	public static NPCInteractionManager get() {
		return INSTANCE;
	}

	/**
	 * Checks whether the player is currently interacting with the provided NPC.
	 *
	 * @param npc target npc
	 * @return {@code true} if interaction is active
	 */
	public boolean isInteracting(final NPC npc) {
		if (npc == null) {
			return false;
		}
		return activeNpcIds.contains(npc.getObjectID());
	}

	/**
	 * Begins an interaction with the npc.
	 *
	 * @param npc target npc
	 */
	public void startInteraction(final NPC npc) {
	if (npc == null) {
	return;
	}
	final int objectId = npc.getObjectID();
	if (objectId < 0) {
	return;
	}
	if (!activeNpcIds.contains(objectId)) {
	activeNpcIds.add(objectId);
	}
	sendChat(GREETING_COMMAND);
	}

	/**
	 * Sends the job inquiry to the npc.
	 *
	 * @param npc target npc
	 */
	public void requestJob(final NPC npc) {
		if (!isInteracting(npc)) {
			return;
		}
		sendChat(JOB_COMMAND);
	}

	/**
	 * Sends the offer inquiry to the npc.
	 *
	 * @param npc target npc
	 */
	public void requestOffer(final NPC npc) {
		if (!isInteracting(npc)) {
			return;
		}
		sendChat(OFFER_COMMAND);
	}

	/**
	 * Sends the help inquiry to the npc.
	 *
	 * @param npc target npc
	 */
	public void requestHelp(final NPC npc) {
		if (!isInteracting(npc)) {
			return;
		}
		sendChat(HELP_COMMAND);
	}

	/**
	 * Sends the quest inquiry to the npc.
	 *
	 * @param npc target npc
	 */
	public void requestQuest(final NPC npc) {
		if (!isInteracting(npc)) {
			return;
		}
		sendChat(QUEST_COMMAND);
	}

	/**
	 * Ends the current interaction.
	 *
	 * @param npc target npc
	 */
	public void endInteraction(final NPC npc) {
	if (npc == null) {
	return;
	}
	if (activeNpcIds.remove(npc.getObjectID())) {
	sendChat(GOODBYE_COMMAND);
	}
	}
	
	private void sendChat(final String text) {
		final RPAction chat = new RPAction("chat");
		chat.put("type", "chat");
		chat.put("text", text);
		ClientSingletonRepository.getClientFramework().send(chat);
	}
}
