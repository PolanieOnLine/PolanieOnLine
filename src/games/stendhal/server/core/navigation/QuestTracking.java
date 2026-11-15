/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.navigation;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.IQuest;
import marauroa.common.game.RPObject;

/** One persistent side-quest choice; possession and active state are checked on the server. */
public final class QuestTracking {
	public static final String SELECTED = "compass_tracked";
	public static final String TITLE = "compass_tracked_title";
	public static final String AVAILABLE = "compass_available";

	private QuestTracking() { }

	public static boolean available(Player player) {
		if (!player.isQuestCompleted("wanderers_compass") || !player.hasSlot("bag")) return false;
		for (RPObject object : player.getSlot("bag")) {
			if (object instanceof Item) {
				Item item = (Item) object;
				if ("kompas wędrowca".equals(item.getName())
						&& (!item.isBound() || player.getName().equals(item.getBoundTo()))) return true;
			}
		}
		return false;
	}

	public static boolean eligible(Player player, IQuest quest) {
		return quest != null && !quest.getQuestInfo(player).isMainQuest()
				&& quest.isStarted(player) && !quest.isCompleted(player)
				&& quest.isVisibleOnQuestStatus(player)
				&& !"rejected".equals(player.getQuest(quest.getSlotName(), 0));
	}

	public static IQuest current(Player player) {
		if (!player.has(SELECTED)) return null;
		IQuest quest = SingletonRepository.getStendhalQuestSystem().getQuestFromSlot(player.get(SELECTED));
		return eligible(player, quest) ? quest : null;
	}

	public static boolean select(Player player, String slot) {
		if (slot == null) return false;
		if (slot.isEmpty()) {
			if (player.has(SELECTED)) player.remove(SELECTED);
			return true;
		}
		IQuest quest = SingletonRepository.getStendhalQuestSystem().getQuestFromSlot(slot);
		if (!available(player) || !eligible(player, quest)) return false;
		player.put(SELECTED, slot);
		return true;
	}

	/** Refresh owner-only UI state without sending unchanged attributes every turn. */
	public static boolean refresh(Player player) {
		boolean changed = false;
		IQuest quest = current(player);
		if (quest == null && player.has(SELECTED)) { player.remove(SELECTED); changed = true; }
		int available = available(player) ? 1 : 0;
		if (!player.has(AVAILABLE) || player.getInt(AVAILABLE) != available) {
			player.put(AVAILABLE, available); changed = true;
		}
		String title = quest == null ? "" : quest.getQuestInfo(player).getName();
		if (!player.has(TITLE) || !title.equals(player.get(TITLE))) {
			player.put(TITLE, title); changed = true;
		}
		return changed;
	}
}
