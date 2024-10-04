/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityoutside;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.idle.WanderIdleBehaviour;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestRegisteredCondition;

public class CloverHunterNPC implements ZoneConfigurator {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		zone.add(buildNPC());
	}

	private SpeakerNPC buildNPC() {
		SpeakerNPC npc = new SpeakerNPC("Maple");
		npc.setEntityClass("cloverhunternpc");
		npc.setDescription("Oto Maple. Młoda kobieta szukająca czegoś na ziemii za pomocą szkła powiększającego.");
		npc.setPosition(68, 67);
		npc.setIdleBehaviour(new WanderIdleBehaviour());

		buildDialogue(npc);

		return npc;
	}

	private void buildDialogue(SpeakerNPC npc) {
		npc.addGreeting("Witaj, łowco koniczynek!");
		npc.addGoodbye("Niech szczęście świeci nad tobą jasno!");
		npc.addJob("Jestem łowczynią koniczynek. Szukam szczęśliwej czterolistnej #koniczyny.");
		npc.addHelp("Czterolistne #koniczyny są niezwykle rzadkie. Mówi się, że jeśli go znajdziesz, będziesz miał"
				+ " doskonałe szczęście!");
		npc.addOffer("Opowiem Wam trochę o #koniczynie.");
		npc.addReply(Arrays.asList("clover", "koniczynie", "koniczyny"), "Koniczyny mogą rosnąć niemal wszędzie, gdzie jest światło słoneczne. Więc nie"
				+ " szukaj w żadnych podziemiach. Te z czterema liśćmi są szczególnie rzadkie i ich"
				+ " znalezienie jest trudnym zadaniem.");

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new NotCondition(new QuestRegisteredCondition("lucky_four_leaf_clover")),
			ConversationStates.ATTENDING,
			"Nie, nie, dziękuję. Potrafię sama znaleźć czterolistną koniczynę.",
			null);
	}
}