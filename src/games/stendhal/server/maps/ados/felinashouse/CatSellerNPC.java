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
package games.stendhal.server.maps.ados.felinashouse;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.creature.Cat;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CatSellerNPC implements ZoneConfigurator {

	public static final int BUYING_PRICE = 100;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildHouseArea(zone);
	}

	private void buildHouseArea(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("Felina") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(6, 8));
				nodes.add(new Node(11, 8));
				nodes.add(new Node(11, 17));
				nodes.add(new Node(19, 17));
				nodes.add(new Node(19, 21));
				nodes.add(new Node(14, 21));
				nodes.add(new Node(14, 16));
				nodes.add(new Node(10, 16));
				nodes.add(new Node(10, 4));
				nodes.add(new Node(6, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				class CatSellerBehaviour extends SellerBehaviour {
					CatSellerBehaviour(final Map<String, Integer> items) {
						super(items);
					}

					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
						if (res.getAmount() > 1) {
							seller.say("Hmm... Nie sądzę, abyś mógł zaopiekować się więcej niż jednym kotem naraz.");
							return false;
						} else if (player.hasPet()) {
							say("Dlaczego nie upewnisz się i nie poszukasz zwierzątka, które już masz?");
							return false;
						} else {
							if (!player.drop("money", getCharge(res,player))) {
								seller.say("Nie masz tyle pieniędzy.");
								return false;
							}
							seller.say("Proszę bardzo, mały słodki kiciuś! Twój kotek żywi się każdym kawałkiem kurczaka lub rybą, którą położysz na ziemi. Ciesz się!");

							final Cat cat = new Cat(player);

							Entity sellerEntity = seller.getEntity();
							cat.setPosition(sellerEntity.getX(), sellerEntity.getY() + 1);

							player.setPet(cat);
							player.notifyWorldAboutChanges();

							return true;
						}
					}
				}

				final Map<String, Integer> items = new HashMap<String, Integer>();
				items.put("cat", BUYING_PRICE);

				addGreeting();
				addJob("Sprzedaję koty. Kiedy je sprzedaję są małymi kociętami, ale kiedy się takim kotkiem #zaopiekujesz to wyrasta na dużego kota.");
				addHelp("Sprzedaję koty, aby kupić jednego wystarczy mi powiedzieć #buy #cat. Jeżeli jesteś nowy w tym interesie to mogę Ci powiedzieć jak #podróżować i jak #opiekować się kotami. Jeżeli znajdziesz dzikiego kota to możesz go #przygarnąć.");
				addGoodbye();
				new SellerAdder().addSeller(this, new CatSellerBehaviour(items));
				addReply(Arrays.asList("zaopiekujesz", "care"),
						"Koty kochają kurczaka i rybę. Wystarczy położyć kawałek na ziemi, a kot podejdzie i zje. Możesz sprawdzić jego wagę klikając prawym przyciskiem  na niego i wybierając 'Zobacz'. Jego waga będzie rosła po zjedzeniu każdego kawałka kurczaka.");
				addReply(Arrays.asList("podróżować", "travel"),
						"Gdy zmieniasz miejsce pobytu twój kot powinien być blisko Ciebie, aby nie zginął. Jeżeli nie zwraca na Ciebie uwagi wystarczy powiedzieć #cat aby go zawołać. Jeśli zdecydujesz się porzucić go to kliknij na siebie prawym przyciskiem i wybierz 'Porzuć zwierzątko', ale szczerze mówiąc sądzę, że takie zachowanie jest odrażające.");
				addReply("sell",
						"Sprzedać??? Jakiego rodzaju potworem jesteś? Dlaczego w ogóle chciałbyś sprzedać swojego pięknego kota?");
				addReply(Arrays.asList("przygarnąć", "own"),
						"Jeżeli znajdziesz dzikiego lub porzuconego kota to, aby go przygarnąć możesz kliknąć na niego prawym przyciskiem i wybrać 'Przygarnij', a wtedy zacznie chodzić za tobą. Koty stają się trochę wściekłe bez właściciela!");
			}
		};

		npc.setEntityClass("woman_009_npc");
		npc.setPosition(6, 8);
		npc.initHP(100);
		npc.setDescription("Felina opiekuje się kotami. Ich miauczenie dochodzi z każdego kąta.");
		zone.add(npc);
		
		// Also put a cat in her bedroom (people can't Own it as it is behind a fence)
		final Cat hercat = new Cat();
                hercat.setPosition(19, 3);
                zone.add(hercat);

	}
}
