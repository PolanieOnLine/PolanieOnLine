/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.woodcutter;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/** A charcoal burner working on the clearing beside the woodcutter's hut. */
public class CharcoalBurnerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Smolisław") {
			@Override
			protected void createPath() {
				setPath(new FixedPath(Arrays.asList(
						new Node(57, 58), new Node(57, 59),
						new Node(56, 59), new Node(56, 58)), true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj na mojej polanie! Uważaj na dym z #mielerza.");
				addJob("Jestem węglarzem. Powoli wypalam drewno w #mielerzu, żeby otrzymać węgiel drzewny.");
				addReply(Arrays.asList("mielerz", "mielerza"),
						"Pod tą warstwą ziemi leżą ciasno ułożone polana. Pilnuję dopływu powietrza, żeby drewno nie spłonęło na popiół.");
				addGoodbye("Do zobaczenia! Oby wiatr nie zaniósł ci dymu prosto w oczy.");
			}
		};
		npc.setDescription("Oto Smolisław, węglarz pilnujący mielerza na leśnej polanie.");
		npc.setEntityClass("charcoalburnernpc");
		npc.setGender("M");
		npc.setPosition(57, 58);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
