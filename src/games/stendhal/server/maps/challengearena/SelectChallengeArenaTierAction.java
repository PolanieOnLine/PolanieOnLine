/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** Remembers the paid tier until the player confirms entry to the combat ring. */
public final class SelectChallengeArenaTierAction implements ChatAction {
	static final String SELECTION_SLOT = "challenge_arena_selection";

	private final ChallengeArenaTier tier;

	public SelectChallengeArenaTierAction(final ChallengeArenaTier tier) {
		this.tier = tier;
	}

	@Override
	public void fire(final Player player, final Sentence sentence,
			final EventRaiser raiser) {
		if (player == null || tier == null) {
			return;
		}
		player.setQuest(SELECTION_SLOT, tier.name());
		raiser.say("Wybrałeś próbę za " + tier.getStake()
				+ " sztuk złota. Gdy jesteś gotowy powiedz #wejście.");
	}
}
