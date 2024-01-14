package games.stendhal.server.maps;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.entity.item.scroll.StonemistScroll;
import games.stendhal.server.entity.player.Player;

public class AlternativeZone implements ZoneConfigurator {
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
				StonemistScroll stonemist = (StonemistScroll) SingletonRepository.getEntityManager().getItem("mgielny kamień");
				stonemist.teleportBack(player);
			}
		});
	}
}
