/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** Starts the tier selected in the current Challenge Arena conversation. */
public final class StartSelectedChallengeArenaAction implements ChatAction {
	@Override
	public void fire(final Player player, final Sentence sentence,
			final EventRaiser raiser) {
		if (player == null) {
			return;
		}
		final ChallengeArenaTier tier = selectedTier(player);
		if (tier == null) {
			raiser.say("Najpierw wybierz jedną z prób Areny Wyzwań.");
			return;
		}

		new StartChallengeArenaAction(tier).fire(player, sentence, raiser);
		final ChallengeArenaState state = ChallengeArenaState.parse(
				player.getQuest(ChallengeArenaState.QUEST_SLOT));
		if (state != null
				&& state.getLifecycle() == ChallengeArenaState.Lifecycle.ACTIVE) {
			player.setQuest(SelectChallengeArenaTierAction.SELECTION_SLOT, "");
		}
	}

	private ChallengeArenaTier selectedTier(final Player player) {
		final String value = player.getQuest(SelectChallengeArenaTierAction.SELECTION_SLOT);
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return ChallengeArenaTier.valueOf(value.trim());
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
}
