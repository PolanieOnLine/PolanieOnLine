package games.stendhal.server.maps.zakopane.gorasmoka;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.entity.item.scroll.WhiteBalloonScroll;
import games.stendhal.server.entity.player.Player;

/**
 * Adds the listener for teleporting back to the islands if you login in the clouds
 */
public class AddWhiteBalloonListener implements ZoneConfigurator {
	private static final String BALLOON = "biały balonik";

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		SingletonRepository.getLoginNotifier().addListener(new LoginListener() {
			@Override
			public void onLoggedIn(final Player player) {
				WhiteBalloonScroll scroll = (WhiteBalloonScroll) SingletonRepository.getEntityManager().getItem(BALLOON);
				scroll.teleportBack(player);
			}
		});
	}
}
