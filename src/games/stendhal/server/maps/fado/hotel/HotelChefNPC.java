/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.fado.hotel;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds a NPC in a house on Ados market (name:Stefan) who is the daughter of fisherman Fritz
 *
 * @author Vanessa Julius
 */
public class HotelChefNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Stefan") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(92, 9));
				nodes.add(new Node(98, 9));
	            nodes.add(new Node(98, 2));
	            nodes.add(new Node(93, 2));
	            nodes.add(new Node(93, 4));
	            nodes.add(new Node(91, 4));
	            nodes.add(new Node(91, 3));
	            nodes.add(new Node(90, 3));
	            nodes.add(new Node(90, 11));
	            nodes.add(new Node(98, 11));
	            nodes.add(new Node(98, 9));
	           	setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam w kuchni hotelu w Fado, nieznajomy!");
				addHelp("Jestem bardzo #zestresowany moją pracą tu. Przykro mi, ale żadnej rady nie mogę Ci teraz udzielić.");
				addReply(Arrays.asList("stressed", "zestresowany"), "To szczyt sezonu! Mamy wiele rezerwacji, co oznacza więcej #gości, czyli jeszcze więcej pracy.");
				addReply(Arrays.asList("guests", "gości"), "Wielu z nich odwiedza Fado, by #'pobrać się'. Rozumiem ich wybór. Fado to piękne miasto.");
				addReply(Arrays.asList("getting married", "pobrać się"), "Nie wiesz, że Fado słynie z największej ilości ślubów w całej Faiumoni? Musisz odwiedzić kościół, jest prześliczny!");
				addQuest("Jestem teraz tak zajęty myśleniem o tym, by ktoś pomógł mi #tutaj... ");
				addReply(Arrays.asList("somewhere", "tutaj"), "Tak, tutaj... Wątpię, że problem może być rozwiązany, kuchnia... Jest zbyt mała!");
				addJob("Kilka tygodni temu, dostałem propozycję pracy tutaj, w hotelowej kuchni. Wtedy tego nie wiedziałem, ale... jestem tutaj jedynym #'kucharzem'!");
				addReply(Arrays.asList("cook", "kucharzem"), "Bycie kucharzem jest wspaniałe! Kocham wszystkie rodzaje jedzenia i przypraw i uwielbiam eksperymentować z różnymi potrawami! Sprawia mi to wielką przyjemność.");
				addOffer("Kuchnia jest teraz zamknięta ale wkrótce znów będę mógł ją otworzyć. Muszę pomyśleć nad rozwiązaniem mojego #problemu...");
				addReply(Arrays.asList("problem", "problemu"), "Pracuję w tej hotelowej kuchni sam, całymi dniami! To strasznie męczące.");
				addGoodbye("Do widzenia! Miłego pobytu w Fado!");

				addReply(Arrays.asList("chicken", "egg", "milk", "butter", "udko", "jajo", "mleko", "osełka masła"),
	                    "Łatwe... Zawsze sprawdzam obszary rolnicze w pobliżu Semos...");

				//in nearby forests, plenty of the stuff
				addReply(Arrays.asList("porcini", "button mushroom", "sclaria", "kekik", "borowik", "pieczarka"),
	                    "Sprawdź w jakimś lesie, niedaleko Semos...");

				//around fado, plenty of the stuff
				addReply(Arrays.asList("garlic", "onion", "carrot", "courgette", "czosnek", "cebula", "marchew", "cukinia"),
	                    "Bardzo łatwe! Sprawdź w okolicy Fado ...");

				//dropped by easy critters, goblins, orcs, kalavan housewives, cannibals
                //also found in grocery stores and market places
                addReply(Arrays.asList("vinegar", "olive oil", "ocet", "oliwa z oliwek"),
                    "Kiedy będziesz wystarczająco odważny, walcz!" +
                    "W przeciwnym razie szukaj sklepu spożywczego lub rynku... " +
                    "Gdzieś nie daleko.");

                //the serra near Kalavan
				addReply(Arrays.asList("potato", "tomato", "pinto beans", "habanero pepper", "habanero peppers", "ziemniaki", "pomidor", "fasola pinto", "papryka habanero", "papryczki habanero"),
	                    "Nie jestem pewien, ale może gdzieś w okolicy ogrodów Kalavan...");

				addReply(Arrays.asList("meat", "cheese", "ham", "mięso", "ser", "szynka"),
	                    "Ehhhh... Nie możesz być taki kiepski!");

				addReply(Arrays.asList("beer", "flour", "sok z chmielu", "mąka"),
	                    "Ooohh... Nie możesz być taki kiepski!");

				addReply(Arrays.asList("perch", "trout", "okoń", "pstrąg"),
	                    "Ahahah... Niezła próba! Nigdy nie zdradzę ci moich ulubionych miejsc do łowienia ryb... Powinieneś więcej eksplorować świata!");

				//All ingredients for dessert should be trigger words
				//Ingredients for preparing dessert for the troublesome customer
				addReply(Arrays.asList("banana", "coconut", "pineapple", "banan", "kokos", "ananas"),
	                    "Egzotyczne owoce ... Może gdzieś w sklepie spożywczym lub na rynku... " +
	                    "Powinieneś więcej eksplorować!");

				addReply(Arrays.asList("apple", "pear", "watermelon", "jabłko", "gruszka", "arbuz"),
	                    "Hmm... nie taki egzotyczny owoc... Może w sklepie spożywczym lub gdzieś na rynku... " +
	                    "Powinieneś więcej eksplorować!");

				//the serra near Kalavan
                addReply(Arrays.asList("lemon", "cytryna"),
                		"Nie jestem pewien, ale może gdzieś w okolicy ogrodów Kalavan...");

				addReply(Arrays.asList("sugar", "cukier"),
	                    "Nie jest tak łatwo zdobyć w czasach wojny! Powinieneś trochę zmielić sam... " +
	                    "Lub znajdź kogoś, kto sprzedaje to!");
			}
		};

		npc.setDescription("Oto Stefan, młody szef kuchni z hotelu w Fado.");
		npc.setEntityClass("hotelchefnpc");
		npc.setGender("M");
		npc.setPosition(92, 9);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
