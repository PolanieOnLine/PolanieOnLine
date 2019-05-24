/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.mines;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a NPC in Semos Mine (name:Barbarus) who is a miner and informs players about his job
 * 
 * @author storyteller and Vanessa Julius 
 *
 */
public class MinerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Barbarus") {
		    
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(57, 78));
				nodes.add(new Node(55, 78));
                nodes.add(new Node(55, 80));
                nodes.add(new Node(53, 80));  
                nodes.add(new Node(53, 82));
                nodes.add(new Node(55, 82)); 
                nodes.add(new Node(55, 84)); 
                nodes.add(new Node(59, 84));
                nodes.add(new Node(59, 78));
                nodes.add(new Node(58, 78));
                nodes.add(new Node(57, 78));
               	setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Powodzenia!");
				addReply("good luck", "Good luck! I hope you'll leave this mine healthy!");
				addReply("glück auf", "Glüüück Auf, Glück Auf...! *sing");
				addReply("powodzenia", "Powodzenia! Mam nadzieje, że wyjdziesz cało z tej kopalni!");
				addHelp("Zawsze pamiętaj swoją drogę! Inaczej możesz się zgubić w tych tunelach, które wiądą daleko wgłąb góry! I... nim zapomnę: W #kopalni jest coś niebezpiecznego... Słyszałem dziwne #dźwięki od czasu do czasu, które wydobywają się gdzieś z dołu...");
				addReply(Arrays.asList("mine", "kopalni"),"Ta kopalnia to jeden wielki system tuneli wykopanych w górze dawno temu. Nikt nie zna wszystkich dróg w tunelach może z wyjątkiem krasnali, harhar... *kaszlnięcie*");
				addReply(Arrays.asList("sounds", "dźwięki"),"Dźwięki są bardzo dziwne... Czasami brzmią jak krzyk z daleka..., a czasami jak rozkazy żołnierzy... Słyszałem też parę razy kroki w mroku... to naprawdę przerażające...");
				addOffer("Mogę sprzedać tobie użyteczne narzędzie do wydobywania węgla. Większość przyjaciół, którzy korzystali z nich w pracy zostawiło mi je jakiś czas temu. Możesz kupić #kilofy, które mi zostawili. Dałbym ci także jedzenie i picie, ale nie zostało mi dużo... Wciąż pracuję więc potrzebuję dla siebie do pracy. Przykro mi... Ale mogę ci pokazać ręcznie narysowaną #mapę kopalni jeżeli chcesz.");
				addReply(Arrays.asList("picks", "kilofy"), "Potrzebujesz kilofa, aby wydobyć węgiel ze ścian w Semos Mine.");
				final Map<String, Integer> offerings = new HashMap<String, Integer>();
                offerings.put("kilof", 400);
                new SellerAdder().addSeller(this, new SellerBehaviour(offerings), false);
				addQuest("Przykro mi, ale jak widzisz jestem pokryty pyłem i wciąż nie skończyłem swojej pracy. Nie mogę teraz myśleć o zadaniach dla ciebie, ale możesz mi pomóc przynosząc trochę węgla."); 
				addJob("Jestem górnikiem. Pracuję ciężko w kopalni. Jeżeli pójdziesz w głąb ziemi to będzie coraz cieplej i będzie więcej pyłu. Jak widzisz ciężko jest tutaj coś zobaczyć w tym słabym świetle...");
				addReply(Arrays.asList("map", "mapę", "mapa"), "To jest mapa Semos Mine, którą sam narysowałem jakiś czas temu. Może ci pomóc znaleść drogę, ale uważaj nie wszystko jest dokładne tak jak powinno!",
						new ExamineChatAction("map-semos-mine.png", "Semos Mine", "Rough map of Semos Mine"));
				addGoodbye("Miło było cię zobaczyć. Powodzenia!");
				
			}
		};

		npc.setDescription("Oto Barbarus. Wygląda na brudnego i bardzo spoconego. Jego twarz i ramiona są prawie czarne ponieważ są pokryte pyłem.");
		npc.setEntityClass("minernpc");
		npc.setPosition(57, 78);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
