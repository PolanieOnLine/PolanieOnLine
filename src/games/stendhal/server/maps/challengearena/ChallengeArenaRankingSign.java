/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.mapstuff.sign.SignFromHallOfFameLoader;

/** Displays the ten best Challenge Arena competitors. */
public final class ChallengeArenaRankingSign extends Sign {
	private static final int SIGN_LENGTH = 10;

	public ChallengeArenaRankingSign() {
		put("class", "transparent");
		setText("Najlepsi wojownicy Areny Wyzwań");
		updatePlayers();
	}

	public void updatePlayers() {
		final SignFromHallOfFameLoader loader = new SignFromHallOfFameLoader(
				this, "Najlepsi wojownicy Areny Wyzwań\n",
				ChallengeArenaRankingService.FAME_TYPE, SIGN_LENGTH, false, true);
		TurnNotifier.get().notifyInTurns(0, loader);
	}
}
