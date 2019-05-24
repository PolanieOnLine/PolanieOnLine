package games.stendhal.server.maps.kalavan.cottage_2;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides Martha, the apple pies confectioner NPC.
 * She has a twin sister: Gertha, the cherry pies confectioner NPC.
 *
 * @author omero
 */
public class ConfectionerApplePieNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Martha") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 3));
				nodes.add(new Node(3, 13));
				nodes.add(new Node(10, 13));
				nodes.add(new Node(10, 11));
				nodes.add(new Node(12, 11));
				nodes.add(new Node(12, 13));
				nodes.add(new Node(10, 13));
				nodes.add(new Node(10, 11));
				nodes.add(new Node(3, 11));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				
				addJob("Mieszkam tutaj z moja bliźniaczą siostrą #Gertha. Naszą pasją jest wypiekanie pysznych ciast z owocami!");

				addReply("gertha",
					"Jest moją bliźniaczą siostrą, z którą razem mieszkam... Jak ja także lubi piec ciasta z owocami! Powiedz #upiecz jeśli się zdecydujesz.");
				addReply("miód",
					"Powinineś znaleść tutejszego pszczelarza jest trochę na północny-zachód stąd...");
				addReply("mleko",
					"Powinieneś odwiedzić farmę. Znajduje się tam gdzie zobyczysz krowy...");
				addReply("mąka",
					"Ahh... Zdobywam mąkę z młyna, który jest na północ od miasta Semos!");
				addReply("jajo",
					"Jak znajdziesz kilka kur to znajdziesz też trochę jaj!");
				addReply("jabłko",
					"Mmm... Gdy od czasu do czasu podróżuje z Semos do Ados to zawsze zatrzymuje się w sadzie obok farmy przy drodze...");


				addHelp("Jeżeli to pomoże to mogę upiec jabłecznik dla Ciebie!");

				addOffer("Kocham #piec ciasta z jabłkami. Poproś mnie mówiąc #upiecz!");

                /** this is a teaser for a quest not yet available */
				addQuest("Teraz dopracowuję przepis na mój jabłecznik, ale w przyszłości możliwe, że będę chciała spróbować czegoś nowego. Dam ci znać.");

				addGoodbye("Uważaj na siebie!");

				// (uses sorted TreeMap instead of HashMap)
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("mąka", Integer.valueOf(2));
				requiredResources.put("miód", Integer.valueOf(1));
				requiredResources.put("mleko", Integer.valueOf(1));
				requiredResources.put("jajo", Integer.valueOf(1));
				requiredResources.put("jabłko", Integer.valueOf(1));

				final ProducerBehaviour behaviour = new ProducerBehaviour("martha_bake_applepie", Arrays.asList("bake", "upiecz"), "jabłecznik",
				        requiredResources, 15 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Cześć! Czy przyszedłeś, aby skosztować mojego pysznego jabłecznika? Mogę upiec go dla Ciebie!");
			}
		};

		npc.setEntityClass("confectionerapplepienpc");
		npc.setDirection(Direction.DOWN);
		npc.setPosition(4, 3);
		npc.initHP(100);
		npc.setDescription("Oto Martha. Kocha piec placki z jabłkami dla gości.");
		zone.add(npc);
	}
}
