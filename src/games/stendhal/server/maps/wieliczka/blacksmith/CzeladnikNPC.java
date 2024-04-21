package games.stendhal.server.maps.wieliczka.blacksmith;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class CzeladnikNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("czeladnik Jaromir") {
//			@Override
//			protected void createPath() {
//				final List<Node> nodes = new LinkedList<Node>();
//				nodes.add(new Node(86, 70));
//				nodes.add(new Node(86, 74));
//				nodes.add(new Node(80, 74));
//				nodes.add(new Node(86, 74));
//				nodes.add(new Node(86, 70));
//				setPath(new FixedPath(nodes, true));
//			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("");
				addOffer("");
				addGoodbye();
			}
		};

		npc.setDescription("Oto czeladnik Jaromir. Mistrz szlifiarstwa klejnotów z których wydobywa z nich co najpiękniejsze.");
		npc.setEntityClass("blacksmithnpc");
		npc.setGender("M");
		npc.setPosition(1, 1);
		zone.add(npc);
	}
}