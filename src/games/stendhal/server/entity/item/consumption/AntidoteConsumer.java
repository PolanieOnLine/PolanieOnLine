package games.stendhal.server.entity.item.consumption;

import games.stendhal.server.entity.item.ConsumableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.StatusType;

/** Neutralizes active poison without granting immunity to future poisoning. */
final class AntidoteConsumer implements Feeder {
	@Override
	public boolean feed(final ConsumableItem item, final Player player) {
		if (item.getAmount() <= 0 || item.getQuantity() <= 0) {
			return false;
		}
		final int neutralized = player.getStatusList().neutralizePoison(item.getAmount());
		if (neutralized == 0) {
			player.sendPrivateText("Nie jesteś " + player.getGenderVerb("otruty") + ".");
			return false;
		}
		item.removeOne();
		player.sendPrivateText("Antidotum zneutralizowało " + neutralized + " PZ obrażeń od trucizny."
				+ (player.hasStatus(StatusType.POISONED) ? " Zatrucie wciąż działa." : ""));
		return true;
	}
}
