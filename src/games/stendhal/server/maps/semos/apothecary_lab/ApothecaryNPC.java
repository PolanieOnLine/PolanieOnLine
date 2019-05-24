/**
 * 
 */
package games.stendhal.server.maps.semos.apothecary_lab;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author AntumDeluge
 *
 */
public class ApothecaryNPC implements ZoneConfigurator {

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

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Jameson") {

	        @Override
			protected void createPath() {
				List<Node> nodes=new LinkedList<Node>();
				nodes.add(new Node(7,9));
				nodes.add(new Node(16,9));
				nodes.add(new Node(16,12));
				nodes.add(new Node(19,12));
				nodes.add(new Node(19,16));
				nodes.add(new Node(19,12));
				nodes.add(new Node(15,12));
				nodes.add(new Node(15,17));
				nodes.add(new Node(15,12));
				nodes.add(new Node(7,12));
				setPath(new FixedPath(nodes, true));
			}

	        @Override
			protected void createDialog() {
				addGreeting("Cześć i witaj w moim laboratorium.");
				addJob("Kiedyś byłem #aptekarzem, ale teraz jestem na emeryturze.");
				addHelp("Przykro mi, ale nie jestem w stanie tobie pomóc.");
				addOffer("Nie mam nic do zaoferowania.");
				addReply("Klaas", "Oh tak mój stary, dobry przyjaciel. Powinienem często podróżować do #'Athor', aby zdobyć bardzo rzadkie zioło #kokuda. Poznałem Klaasa bardzo dobrze.");
				addReply("kokuda", "Kokuda to zioło, które można znaleść tylko w środku labiryntu na wyspie #Athor.");
				addReply("Athor", "Jeszcze nie odwiedziłeś Athor? To piękna wyspa. Wspaniałe miejsce na oderwanie się od trosk życia codziennego. Ale trzymaj się z daleka od terytorium kanibali. Jeśli zaproszą cię na obiad to możesz nigdy nie zobaczyć domu.");
				addReply(Arrays.asList("Apothecary", "aptekarzem"), "I was head researcher of a team that worked for one of Faimouni's most powerful leaders. However this leader became corrupt and demanded that I use my skills to make deadly weapons of war. Anyway, I escaped and have been hiding out here ever since.");
				addGoodbye("Proszę nikomu nie mów o moim laboratorium.");
			}
		};

		// The NPC sprite from data/sprites/npc/
	    npc.setEntityClass("apothecarynpc");
		// set a description for when a player does 'Look'
		npc.setDescription("Oto Jameson, stale pracuje z dala od ludzi.");
		// Set the initial position to be the first node on the Path you defined above.
		npc.setPosition(7, 9);
		npc.initHP(100);

		zone.add(npc);   
	}
}
