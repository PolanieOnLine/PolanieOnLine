/***************************************************************************
 *                   (C) Copyright 2024 - Polanie OnLine                   *
 ***************************************************************************/
package games.stendhal.client.gui.npc;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.entity.NPC;
import games.stendhal.client.gui.chattext.ChatTextController;
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

	/**
	 * Available chat option provided by the server.
	 */
	public static final class ChatOption {
		private final String trigger;
		private final String label;
		private final boolean requiresParameters;

		public ChatOption(final String trigger, final String label, final boolean requiresParameters) {
			this.trigger = trigger;
			this.label = label;
			this.requiresParameters = requiresParameters;
		}

		public String getTrigger() {
			return trigger;
		}

		public String getLabel() {
			return label;
		}

		public boolean requiresParameters() {
			return requiresParameters;
		}
	}

	private static final class InteractionContext {
		private final int objectId;
		private final WeakReference<NPC> npcRef;
		private final String baseName;
		private final String baseTitle;
		private List<ChatOption> chatOptions;

		InteractionContext(final NPC npc) {
			objectId = npc.getObjectID();
			npcRef = new WeakReference<NPC>(npc);
			baseName = npc.getName();
			baseTitle = npc.getTitle();
			chatOptions = Collections.emptyList();
		}

		NPC getNpc() {
			return npcRef.get();
		}

		boolean matches(final String npcName, final String npcTitle) {
			final NPC npc = npcRef.get();
			if (npc == null) {
				return false;
			}

			if ((npcName != null) && npcName.equalsIgnoreCase(npc.getName())) {
				return true;
			}

			if ((npcTitle != null) && npcTitle.equalsIgnoreCase(npc.getTitle())) {
				return true;
			}

			if ((baseName != null) && (npcName != null) && baseName.equalsIgnoreCase(npcName)) {
				return true;
			}

			if ((baseTitle != null) && (npcTitle != null) && baseTitle.equalsIgnoreCase(npcTitle)) {
				return true;
			}

			return false;
		}

		int getObjectId() {
			return objectId;
		}

		List<ChatOption> getChatOptions() {
			return chatOptions;
		}

		void setChatOptions(final List<ChatOption> options) {
			if ((options == null) || options.isEmpty()) {
				chatOptions = Collections.emptyList();
				return;
			}
			chatOptions = Collections.unmodifiableList(new ArrayList<ChatOption>(options));
		}
	}

	private final Map<Integer, InteractionContext> activeContexts = new HashMap<Integer, InteractionContext>();

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
		return activeContexts.containsKey(npc.getObjectID());
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
		InteractionContext context = activeContexts.get(objectId);
		if (context == null) {
			context = new InteractionContext(npc);
			activeContexts.put(objectId, context);
		}
		sendChat(GREETING_COMMAND);
	}

	/**
	 * Updates known chat options for active NPC interactions.
	 *
	 * @param npcName name provided by the server
	 * @param npcTitle title provided by the server
	 * @param options chat options
	 */
	public void updateChatOptions(final String npcName, final String npcTitle, final List<ChatOption> options) {
		final List<Integer> staleContexts = new ArrayList<Integer>();
		for (final InteractionContext context : activeContexts.values()) {
			final NPC npc = context.getNpc();
			if (npc == null) {
				staleContexts.add(Integer.valueOf(context.getObjectId()));
				continue;
			}
			if (context.matches(npcName, npcTitle)) {
				context.setChatOptions(options);
			}
		}
		for (final Integer id : staleContexts) {
			activeContexts.remove(id.intValue());
		}
	}

	/**
	 * Retrieves the latest chat options received for the npc.
	 *
	 * @param npc target npc
	 * @return immutable list of options
	 */
	public List<ChatOption> getChatOptions(final NPC npc) {
		if (npc == null) {
			return Collections.emptyList();
		}
		final InteractionContext context = activeContexts.get(npc.getObjectID());
		if (context == null) {
			return Collections.emptyList();
		}
		return context.getChatOptions();
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
	 * Executes provided chat option.
	 *
	 * @param npc target npc
	 * @param option option to execute
	 */
	public void performChatOption(final NPC npc, final ChatOption option) {
		if ((option == null) || !isInteracting(npc)) {
			return;
		}
		final int objectId = npc.getObjectID();
		if (option.requiresParameters()) {
			final ChatTextController chat = ClientSingletonRepository.getChatTextController();
			chat.setChatLine(option.getTrigger() + " ");
			chat.setFocus();
		} else {
			sendChat(option.getTrigger());
			if (isFarewell(option.getTrigger())) {
				activeContexts.remove(objectId);
			}
		}
	}

	private boolean isFarewell(final String trigger) {
		if (trigger == null) {
			return false;
		}
		final String normalized = trigger.trim().toLowerCase();
		return normalized.equals("bywaj") || normalized.equals("bye") || normalized.equals(GOODBYE_COMMAND);
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
		final int objectId = npc.getObjectID();
		if (activeContexts.remove(objectId) != null) {
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
