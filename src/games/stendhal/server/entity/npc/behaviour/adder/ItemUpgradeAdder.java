/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.entity.npc.behaviour.adder;

import java.util.Arrays;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.actions.ItemUpgradeAction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeService;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

/** Adds the shared item-upgrade UI workflow to an NPC. */
public final class ItemUpgradeAdder {
	private static final List<String> UPGRADE_PHRASES = Arrays.asList(
			"improve", "upgrade", "ulepsz", "ulepszyć", "udoskonalić");
	private static final List<String> CHECK_PHRASES = Arrays.asList(
			"check", "see", "how much", "sprawdź", "zobacz", "ile");

	public void add(final ItemUpgradeNPC upgradeNpc) {
		upgradeNpc.put("job_producer", "");
		upgradeNpc.add(ConversationStates.ATTENDING, UPGRADE_PHRASES, null,
				ConversationStates.ATTENDING, null,
				requestUpgradeAction(upgradeNpc));
		upgradeNpc.add(ConversationStates.ATTENDING, CHECK_PHRASES, null,
				ConversationStates.ATTENDING, null,
				requestUpgradeCheckAction(upgradeNpc));
	}

	private ChatAction requestUpgradeAction(final ItemUpgradeNPC upgradeNpc) {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence,
					final EventRaiser raiser) {
				final String preferredName = stripPhrase(sentence.getTrimmedText(),
						UPGRADE_PHRASES);
				ItemUpgradeAction.openWindow(player, upgradeNpc, preferredName);
				if (ItemUpgradeService.getInstance().findUpgradeCandidates(player)
						.isEmpty()) {
					upgradeNpc.say("Nie masz przy sobie przedmiotu, który potrafię ulepszyć.");
				} else {
					upgradeNpc.say("Wybierz konkretny przedmiot w otwartym oknie ulepszania.");
				}
			}
		};
	}

	private ChatAction requestUpgradeCheckAction(
			final ItemUpgradeNPC upgradeNpc) {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence,
					final EventRaiser raiser) {
				final String itemName = stripPhrase(sentence.getTrimmedText(),
						CHECK_PHRASES);
				if (itemName.length() == 0) {
					upgradeNpc.say("Powiedz: #sprawdź <#'nazwa przedmiotu'> albo po prostu #ulepsz, aby otworzyć okno.");
					return;
				}
				final Item item = SingletonRepository.getEntityManager()
						.getItem(itemName);
				if (item == null) {
					upgradeNpc.say("Pierwsze słyszę o takim wyposażeniu #\'"
							+ itemName + "\'.");
				} else if (item.hasUpgradeLimit()) {
					upgradeNpc.say("Przedmiot #\'" + itemName
							+ "\' mogę ulepszyć maksymalnie do poziomu #+"
							+ item.getMaxUpgradeLevel() + ". Powiedz #ulepsz, aby otworzyć okno.");
				} else {
					upgradeNpc.say("Wyposażenia takiego jak #\'" + itemName
							+ "\' nie jestem w stanie ulepszyć.");
				}
			}
		};
	}

	private String stripPhrase(final String request,
			final List<String> phrases) {
		final String normalized = request == null ? "" : request.trim();
		for (final String phrase : phrases) {
			if (normalized.equalsIgnoreCase(phrase)) {
				return "";
			}
			if (normalized.toLowerCase().startsWith(phrase.toLowerCase() + " ")) {
				return normalized.substring(phrase.length()).trim();
			}
		}
		return "";
	}

	/** NPC marker used by the action layer to validate legal interaction. */
	public static class ItemUpgradeNPC extends SpeakerNPC {
		public ItemUpgradeNPC(final String name) {
			super(name);
		}

		@Override
		public void onGoodbye(final RPEntity attending) {
			if (attending instanceof Player) {
				ItemUpgradeService.getInstance()
						.clearPendingAttempt((Player) attending);
			}
		}
	}
}
