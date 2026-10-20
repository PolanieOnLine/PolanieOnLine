package games.stendhal.server.maps.zakopane.home;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */
public class JanislawNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Janisław") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj, wędrowcze. Ogrzej się chwilę i powiedz, czy potrzebujesz #pomocy.");
				addJob("Dawniej przemierzałem lasy i góry, tropiąc bestie, które nie dawały ludziom spokoju. Dziś nogi już nie te, ale oko do tropów wciąż mam dobre.");
				addHelp("Jeśli niestraszna ci leśna głusza, mam dla ciebie #zadania. Znam tropy na łowy #łatwe, #średnie i #trudne. Za dobrą robotę uczciwie zapłacę, dorzucę skrzynię i podzielę się tym, czego sam nauczyłem się na polowaniach.");
				addGoodbye("Idź ostrożnie. W głuszy lepiej dwa razy spojrzeć za siebie niż raz dać się zaskoczyć.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.RIGHT);
			}
		};

		npc.setDescription("Oto Janisław, stary myśliwy. Sam już rzadko rusza w głuszę, ale chętnie wskaże trop komuś, kto nie boi się bestii.");
		npc.setEntityClass("npcstarybaca");
		npc.setGender("M");
		npc.setDirection(Direction.RIGHT);
		npc.setPosition(2, 14);
		zone.add(npc);
	}
}
