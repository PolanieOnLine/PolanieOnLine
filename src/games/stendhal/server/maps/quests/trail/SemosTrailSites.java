/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Map;
import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Shared scenery and a carrier; each visitor's delivery progress stays
 * personal.
 */
public final class SemosTrailSites implements ZoneConfigurator {
	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		switch (zone.getName()) {
		case "int_semos_bank":
			evidence(zone, TrailEvidence.Kind.SEMOS_PAYMENT, 11, 24);
			break;
		case "int_semos_library":
			evidence(zone, TrailEvidence.Kind.SEMOS_ARCHIVE, 4, 28);
			break;
		case "0_semos_city":
			evidence(zone, TrailEvidence.Kind.SEMOS_CRATE, 46, 40);
			final SpeakerNPC carrier = new SpeakerNPC("Tragarz Olek") {
				@Override
				protected void createDialog() {
					addGreeting("Cześć. Czekam na przesyłkę.");
					addJob("Przenoszę skrzynie i worki. Jak ktoś płaci, to noszę.");
					addHelp("Jeśli czegoś szukasz, zapytaj w gospodzie. Ja tylko czekam na zlecenie.");
					addGoodbye("Trzymaj się.");
					add(ConversationStates.ATTENDING, "", null,
							ConversationStates.ATTENDING,
							"Chyba czekam na kogoś innego.", null);
				}
			};
			carrier.setEntityClass("seller3npc");
			carrier.setGender("M");
			carrier.setDescription(
					"Oto Olek. Czeka przy skrzyni, co chwilę zerkając w stronę gospody.");
			carrier.setPosition(48, 40);
			carrier.setDirection(Direction.LEFT);
			zone.add(carrier);
			break;
		default:
			throw new IllegalArgumentException(
					"No Semos story site in " + zone.getName());
		}
	}

	private void evidence(StendhalRPZone zone, TrailEvidence.Kind kind, int x,
			int y) {
		final TrailEvidence prop = new TrailEvidence(kind);
		prop.setPosition(x, y);
		zone.add(prop);
	}
}
