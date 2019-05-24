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
package games.stendhal.server.maps.magic.bricabrac;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds an witch NPC She is a trader for bric-a-brac items.
 *
 * @author kymara
 */
public class BuyerNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Vonda") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 12));
				nodes.add(new Node(12, 12));
				nodes.add(new Node(12, 8));
				nodes.add(new Node(27, 8));
				nodes.add(new Node(27, 5));
				nodes.add(new Node(27, 10));
				nodes.add(new Node(8, 10));
				nodes.add(new Node(8, 12));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć.");
				addJob("Kolekcjonuję graty i drobiazgi. Czasami sprzedaję przedmioty, ale przeważnie trzymam je. Jeżeli masz jakieś relikty na #handel to byłabym szczęśliwa.");
				addHelp("Mogłabym opowiedzieć Ci o tych wspaniałych przedmiotach: białym #dzbanku, #trumnach, #sukience, #tarczy, #zbroi, #narzędziach, #dywanie, #kwiatkach, #zegarze i #maszynie #do #szycia wszystkie są fascynujące!");
				addReply(
						Arrays.asList("pot", "dzbanku"),
						"Masz na myśli orientalny dzbanek koloru białego i niebieskiego. Są oryginalne, wykonane przez antycznych ludzi zwanych oni. Są bardzo rzadkie.");
				addReply(
						Arrays.asList("coffins", "trumnach"),
						"Te trumny zostały zrabowane z podziemnych katakumb. Musiałam zapłacić niezłą sumkę za tę parę.");
				addReply(
						Arrays.asList("dress", "sukience"),
						"Kocham tę piękną różową sukienkę. Była noszona przez księżniczkę elfów Tywysogę.");
				addReply(
						Arrays.asList("shield", "tarczy"),
						"To naprawdę przerażająca tarcza czyż nie? Widać na niej jakieś inskrypcje z tyłu obok devil knights, ale nie rozumiem tego.");
				addReply(
						Arrays.asList("rug", "dywanie"),
						"To jest prawdziwy dywan z dalekiego wschodu. Nigdy nie widziałam takiego tylko tanie podróbki. Proszę nie nanieś na niego błota!");
				addReply(
						Arrays.asList("flowers", "kwiatkach"),
						"Ach! Te kwiatki rosną z magii elfów. Kupiłam je od wspaniałej kwiaciarki w Nalwor.");
				addReply(
						Arrays.asList("clock", "zegarze"),
						"Ten zegar stojący jest jedną z najnowocześniejszych rzeczy w mojej kolekcji. Jeżeli chcesz wiedzieć to drwal Woody może rozpoznać wykonawcę.");
				addReply(
						Arrays.asList("tools", "narzędziach"),
						"Te narzędzia na tylnej ścianie są naprawdę antyczne! Zostały wykorzystane przez Xoderos z Semos do wykonania zegara stojącego czyż to nie jest niesamowite!");
				addReply(
						Arrays.asList("armor", "zbroi"),
						"Ach, ta zbroja został zrobiona w Deniran. Mało wiem o niej.");
				addReply(
						Arrays.asList("sewing machine", "maszynie do szycia"),
						"To jest moja ulubiona rzecz. Została wykonana przez człowieka o imieniu Zinger. Maszyna do szycia wciąż pracuje jak wtedy, gdy została zrobiona.");
				addQuest("Nie mam żadnych życzeń.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buymagic")), false);
				addOffer("W dużej książce znajduje się cennik reliktów i magicznych przedmiotów, które chciałabym kupić.");
				addGoodbye("Dowidzenia.");
			}
		};

		npc.setDescription("Oto Vonda, czarownica, która chyba lubi chaos...");
		npc.setEntityClass("witch2npc");
		npc.setPosition(4, 12);
		npc.initHP(100);
		zone.add(npc);
	}
}
