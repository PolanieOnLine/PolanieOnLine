package games.stendhal.server.maps.desert.blackriver;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.mapstuff.useable.GoldenCauldronEntity;

public class GoldenCauldronCorner implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildWitch(zone);
		buildCauldron(zone);
	}

	private void buildWitch(final StendhalRPZone zone) {
		final SpeakerNPC witch = new SpeakerNPC("Draconia") {
			@Override
			protected void createDialog() {
				addGreeting("Szelest run niesie się znad kotła. Witaj, poszukiwaczu legend.");
				addJob("Warzę esencje dla tych, którzy dążą do trzeciego wąsa złotej ciupagi.");
				addOffer("Jeśli potrzebujesz #'wywaru wąsatych smoków', powiem jak go przygotować.");
				addReply(Arrays.asList("wywar", "przepis", "mieszanie", "mieszaj"),
									"Do kotła dołóż #'3 pióro azazela'#,' #'10 magiczna krew'#,' #'5 smocza krew'# oraz #'1 cudowna krew'#. "
									+ "Stań obok kotła, kliknij prawym przyciskiem i wybierz 'Otwórz' – pojawi się okno z ośmioma miejscami na składniki oraz przyciskiem 'Mieszaj'. "
									+ "Gdy bulgot ucichnie, zabierz wywar do Hadrina."
				addHelp("Pamiętaj, że musisz mieć wszystkie składniki przy sobie – kocioł nie znosi braków.");
				addGoodbye("Niech kocioł szepcze ci o zwycięstwie.");
			}
		};

		witch.setDescription("Draconia, pustynna wiedźma, dogląda runicznego kotła Józka.");
		witch.setEntityClass("witchnpc");
		witch.setGender("F");
		witch.setDirection(Direction.LEFT);
		witch.setPosition(4, 6);
		zone.add(witch);
	}

	private void buildCauldron(final StendhalRPZone zone) {
		final GoldenCauldronEntity cauldron = new GoldenCauldronEntity();
		cauldron.setPosition(5, 6);
		zone.add(cauldron);
	}
}
