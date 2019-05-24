/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.barbakan;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Build a NPC
 *
 * @author KarajuSs
 */
public class BarbakanGuardianNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Gwardzista") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(67, 34));
				nodes.add(new Node(77, 34));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem gwardzistą. Strzegę północnej bramy miasta Kraka i Brabakanu.");
				addHelp("Aktualnie ja sam nie potrzebuję żadnej pomocy, ale poczekaj jeszcze, słyszałem, że król Kraka poszukuje dzielnego wojownika i może mieć zlecenie dla Ciebie!");
				addOffer("Mogę Ci udzielić kilku #rad na temat #ekwipunku i #uzbrojenia prawdziwego wojownika oraz co Ciebie może spotkać poza naszymi bramami.");
				addReply("rad", "Pierwszą moją radą jest abyś uważał na potwory silniejsze od Ciebie samego! Nie powinieneś się do nich zbliżać dopóki nie zdobędziesz odpowiedniego doświadczenia w walce oraz idealnego #'ekwipunku' i #'uzbrojenia', które może Ci się przydać podczas walki!");
				addReply("ekwipunek", "Podczas walki w ekwipunku powinieneś posiadać coś co Ci umożliwi zregenerowanie swoich sił! Także zalecam, aby każdy wojownik posiadał różego rodzaju eliksiry, które własnie umożliwiają szybkie zregenerowanie się! Pamiętaj, aby mieć przy sobie również zapasową broń lub łuk i strzały czy również różdżkę i magię. Zawsze to Ci umożliwi trzymanie się na dystans z poważnymi potworami, które czyhają poza bramami miasta.");
				addReply("uzbrojenie", "Chyba najważnielszy element twoje wyposażenia. Uzbrojenie powinno być dopasowane do twoich aktualnych zdobytych doświadczeń, aby zmaksymalizować ilość obrony czy także ataku z broni! Najlepiej jakby zbroja miała jak największą ilość obrony dopasowaną do twojego poziomu, a broń, żeby posiadała jak największą siłę oraz musi być poręczna dla Ciebie! Bronie mające zazwyczaj najwięszką siłę są bardzo ciężkie dla zwykłych ludzi, którzy w ogóle nie trenowali, aby zostać wojownikami.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto żołnierz Gwardii Królewskiej strzegący północnej bramy miasta i Barbakanu.");
		npc.setEntityClass("barracksbuyernpc");
		npc.setPosition(72, 34);
		npc.initHP(100);
		zone.add(npc);
	}
}
