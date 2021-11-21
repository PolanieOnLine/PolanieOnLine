package games.stendhal.server.entity.item;

import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.BleedingStatus;
import marauroa.common.game.RPObject;

public class Potion extends Drink {
	private final static Logger logger = Logger.getLogger(Potion.class);

	public Potion(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public Potion(final Potion item) {
		super(item);
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (user instanceof Player) {
			final Player player = (Player) user;

			if (isContained()) {
				// We modify the base container if the object change.
				RPObject base = getContainer();

				while (base.isContained()) {
					base = base.getContainer();
				}

				if (!user.nextTo((Entity) base)) {
					user.sendPrivateText("Eliksir jest zbyt daleko.");
					return false;
				}
			} else {
				if (!nextTo(user)) {
					user.sendPrivateText("Eliksir jest zbyt daleko.");
					return false;
				}
			}

			feeder.feed(this, player);
			player.getStatusList().removeAll(BleedingStatus.class);
			player.notifyWorldAboutChanges();
			return true;
		} else {
			logger.error("user is no instance of Player but: " + user, new Throwable());
			return false;
		}
	}
}