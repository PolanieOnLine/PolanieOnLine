package games.stendhal.server.maps.semos.village;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class FightTrainingNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param zone       The zone to be configured.
	 * @param attributes Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Rochar-Zith") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj! Jak może jeszcze posłużyć moje zardzewiałe ramię?");
				addJob("Jestem trenerem walki, który może ci pokazać, jak łatwo pokonać wrogów.");
				addHelp("Poproś mnie o #'trening', a pokażę ci kilka tajemnic dotyczących walki.");
				addReply(Arrays.asList("train", "trening", "trenować", "ćwiczenie", "ćwiczyć"), "Podczas #walki ty i twój przeciwnik atakujecie się nawzajem aż do śmierci.");
				addReply(Arrays.asList("combat", "walka", "walki", "walczyć"), "W walce obowiązują 2 podstawowe statystyki, #waga i #obrażenia.");
				addReply(Arrays.asList("damage", "obrażenia"),
						"Im wyższe OBRAŻENIA twej broni, tym więcej PŻ przeciwnik straci przy każdym trafieniu.");
				addReply(Arrays.asList("rate", "waga"), "Im mniejsza WAGA twojej broni, tym szybciej będziesz atakował.");
				addGoodbye("Dopóki się nie spotkamy!");
			}
		};

		npc.setDescription("Oto Rochar-Zith, stojący w tym miejscu nieruchomo, wpatrując się w horyzont.");
		npc.setOutfit("body=0,head=0,eyes=27,dress=36");
		npc.setOutfitColor("dress", 0x4b5320);
		npc.setGender("M");
		npc.setPosition(23, 30);
		zone.add(npc);
	}
}
