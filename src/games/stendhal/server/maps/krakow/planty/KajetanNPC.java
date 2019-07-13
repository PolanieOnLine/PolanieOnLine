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
package games.stendhal.server.maps.krakow.planty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.creature.Goat;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;

/**
 * Build a NPC
 *
 * @author KarajuSs
 */
public class KajetanNPC implements ZoneConfigurator {
	public static final int BUYING_PRICE = 60;

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
		final SpeakerNPC npc = new SpeakerNPC("Kajetan") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(111, 110));
				nodes.add(new Node(111, 111));
				nodes.add(new Node(112, 111));
				nodes.add(new Node(112, 112));
				nodes.add(new Node(114, 112));
				nodes.add(new Node(114, 113));
				nodes.add(new Node(117, 113));
				nodes.add(new Node(114, 113));
				nodes.add(new Node(114, 112));
				nodes.add(new Node(112, 112));
				nodes.add(new Node(112, 111));
				nodes.add(new Node(111, 111));
				nodes.add(new Node(111, 110));
				nodes.add(new Node(103, 110));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				class GoatSellerBehaviour extends SellerBehaviour {
					GoatSellerBehaviour(final Map<String, Integer> items) {
						super(items);
					}

					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
						if (res.getAmount() > 1) {
							seller.say("Hmm... Nie sądzę, abyś mógł zaopiekować się więcej niż jedną kozą naraz.");
							return false;
						} else if (!player.hasGoat()) {
							if (!player.drop("money", getCharge(res,player))) {
								seller.say("Nie masz tyle pieniędzy.");
								return false;
							}
							seller.say("Proszę bardzo, oto mała koza! Opiekuj się nią dobrze...");

							final Goat goat = new Goat(player);
							StendhalRPAction.placeat(seller.getZone(), goat, seller.getX(), seller.getY() + 1);

							player.notifyWorldAboutChanges();

							return true;
						} else {
							say("Dlaczego się nie upewnisz i nie poszukasz kozy, którą już masz?");
							return false;
						}
					}
				}

				final Map<String, Integer> items = new HashMap<String, Integer>();
				items.put("goat", BUYING_PRICE);

				addGreeting("Witaj, co Cię do mnie sprowadza?");
				addJob("Zajmuję się wypasaniem kóz.");
				addOffer("Sprzedaję kozy. Aby kupić jedną wystarczy powiedzieć mi #'buy goat'. Jeżeli jesteś nowym w tym interesie to mogę Ci powiedzieć jak #podróżować z kozą, jak się nią #opiekować i powiem Ci, gdzie możesz ją #sprzedać. Jeżeli znajdziesz dziką kozę to możesz ją #przygarnąć.");
				// zakup kozy: 60 money, karmi się trawą
				addGoodbye();
				new SellerAdder().addSeller(this, new GoatSellerBehaviour(items));
				addReply(Arrays.asList("care", "opiekować"),
						"Moja koza kocha jeść wysoką trawę. Stań obok, a twoja koza podejdzie i zacznie jeść. Możesz sprawdzić jej wagę klikając na nią prawym przyciskiem i wybierając 'Zobacz'. Jej waga będzie rosła wraz ze zjedzeniem każdej trawy.");
				addReply(Arrays.asList("travel", "podróżować"),
						"Gdy zmieniasz miejsce pobytu twoja koza powinna być blisko Ciebie, aby nie zginęła. Jeżeli nie zwraca na Ciebie uwagi wystarczy powiedzieć #goat aby ją wezwać. Jeżeli zdecydujesz się ją porzucić to kliknij na siebie prawym przyciskiem i wybierz 'Porzuć kozę', ale szczerze sądzę, że takie zachowanie jest odrażające.");
				addReply(Arrays.asList("sell", "sprzedać"),
						"Kiedy twoja koza osiągnie wagę 100 to możesz ja zabrać do Targona w Krakowie, a on kupi ją od Ciebie.");
				addReply(Arrays.asList("own", "przygarnąć"),
						"Jeżeli znajdziesz dziką lub porzuconą kozę to aby ją przygarnąć możesz kliknąć na nią prawym przyciskiem i wybrać 'Przygarnij'.");
			}
		};

		npc.setDescription("Oto Kajetan. Zajmuje się wypasaniem kóz.");
		npc.setEntityClass("seller3npc");
		npc.setPosition(103, 110);
		npc.initHP(100);
		npc.setSounds(Arrays.asList("cough-11", "cough-2", "cough-3"));
		zone.add(npc);
	}
}
