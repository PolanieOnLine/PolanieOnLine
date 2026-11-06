/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.entity.player;

import java.util.Arrays;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.quests.trail.TrailQuestInvitations;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** One private invitation per character and offered main quest. */
public final class MainQuestInvitationService {
	private static final String NOTIFIED_SLOT = "main_quest_invitations";
	private static final String[][] STARTERS = { { "meet_pietrek", "Pietrek",
			"Wieści od Pietrka: Pierwsze kroki bywają trudne. Wstąp do domku bohaterów w centrum Zakopanego, pokażę ci, jak sobie poradzić." },
			{ "pomoc_adasiowi", "Adaś",
					"Wieści od Adasia: Mamcia chce znieść zapasy do piwnicy, ale boję się tych szkodników. Zajrzyj do mnie do domku bohaterów w Zakopanem C." },
			{ "scythe_fryderyk", "Fryderyk",
					"Wieści od Fryderyka: Plony czekają, a moja kosa już do niczego się nie nadaje. Odwiedź mnie w Zakopanem S, może coś poradzimy." },
			{ "wegiel_na_opal", "Stasek",
					"Wieści od Staska: Słyszałem, jak pomagasz sąsiadom. Przyda mi się opał na zimę. Wstąp do sklepu obok mostu w centrum Zakopanego." } };

	private MainQuestInvitationService() {
	}

	/** Also handles login and a level requirement met while already online. */
	public static void update(Player player) {
		if (player.getZone() == null) {
			return;
		}
		// Older characters may have unlocked Stasek without the whole tutorial.
		if (!player.isQuestCompleted("wegiel_na_opal")) {
			for (String[] starter : STARTERS) {
				if (!player.isQuestCompleted(starter[0])) {
					invite(player, starter[0], starter[1], starter[2]);
					return;
				}
			}
		}
		for (Step step : Step.values()) {
			if (!player.isQuestCompleted(step.slot)) {
				if (step.available(player) && !player.hasQuest(step.slot)) {
					invite(player, step.slot, step.npc,
							TrailQuestInvitations.forStep(step));
				}
				return;
			}
		}
	}

	private static void invite(Player player, String slot, String npcName,
			String text) {
		if (text == null || (player.hasQuest(slot)
				&& !player.isQuestInState(slot, 0, "rejected"))) {
			return;
		}
		String notified = player.getQuest(NOTIFIED_SLOT);
		if (notified != null
				&& Arrays.asList(notified.split(";")).contains(slot)) {
			return;
		}
		if (!SingletonRepository.getStendhalQuestSystem().isLoadedSlot(slot)) {
			return;
		}
		SpeakerNPC npc = NPCList.get().get(npcName);
		if (npc == null || npc.getZone() == null) {
			return;
		}
		// The quest slot is saved with the character, unlike login/session
		// state.
		player.setQuest(NOTIFIED_SLOT,
				notified == null || notified.isEmpty() ? slot
						: notified + ";" + slot);
		if (npc.getZone() == player.getZone() && npc.getAttending() == player
				&& npc.isTalking()) {
			return;
		}
		// This does not call the player-to-player whisper/reply action.
		player.sendPrivateText(NotificationType.QUEST_INVITATION, text);
	}
}
