package games.stendhal.server.maps.challengearena;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.useable.UseableEntity;
import games.stendhal.server.entity.player.Player;

/** The shared lobby's orbs select a private fight, not the empty shared ring. */
public final class ChallengeArenaObservationOrb extends UseableEntity {
	public ChallengeArenaObservationOrb() {
		setResistance(0);
	}

	@Override public String describe() {
		return "Oto kula obserwacji. Użyj jej, aby wybrać gracza i obejrzeć jego walkę na arenie.";
	}

	@Override public boolean onUsed(final RPEntity user) {
		if (!(user instanceof Player) || !nextTo(user)) {
			user.sendPrivateText("Podejdź bliżej kuli obserwacji.");
			return false;
		}
		return ChallengeArenaObservation.open((Player) user);
	}
}
