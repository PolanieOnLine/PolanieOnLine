/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.zakopane.hospital;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/** Uses the normal producer journal, worksite and inventory-safe collection. */
public final class JadzkaAntidoteProduction {
	public static final String SLOT = "jadzka_antidote_production";
	public static final String UNLOCK = "jadzka_antidote_recipe";
	private JadzkaAntidoteProduction() { }

	public static void add(final SpeakerNPC npc) {
		final Map<String, Integer> ingredients = new LinkedHashMap<>();
		ingredients.put("mięta", 1); ingredients.put("babka lekarska", 1); ingredients.put("buteleczka wody", 1);
		final ProducerBehaviour producer = new ProducerBehaviour(SLOT,
				Arrays.asList("uwarz", "warzyć", "warzyc", "brew", "prepara"), "antidotum", ingredients, 30);
		new ProducerAdder().addProducer(npc, producer, UNLOCK, null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("antidotum", "antidote", "antídoto"),
				null, ConversationStates.ATTENDING, null, (p, s, n) -> n.say(p.isQuestCompleted(UNLOCK)
						? "Na jedną porcję antidotum przynieś mi liście mięty, babkę lekarską i buteleczkę wody — po jednej sztuce. Przygotowanie zajmie pół minuty. Powiedz na przykład #'uwarz 5 antidotum', jeśli chcesz zamówić kilka porcji."
						: "Najpierw pomóż mi uzupełnić zapasy ziół. Zapytaj o #zamówienie zielarskie. Po pierwszej dostawie będę mogła warzyć dla ciebie antidotum."));
		npc.add(ConversationStates.ATTENDING, producer.getProductionActivity(),
				(p, s, n) -> !p.isQuestCompleted(UNLOCK), ConversationStates.ATTENDING,
				"Najpierw wykonaj moje #zamówienie zielarskie. Potem chętnie przygotuję dla ciebie antidotum.", null);
	}
}
