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
// Based on ../games/stendhal/server/maps/ados/felinashouse/CatSellerNPC.java
package games.stendhal.server.maps.koscielisko.plains;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.creature.Owczarek;
import games.stendhal.server.entity.creature.OwczarekPodhalanski;
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

public class DogSellerNPC implements ZoneConfigurator {

	public static final int BUYING_PRICE_OWCZAREK = 300;
	public static final int BUYING_PRICE_OWCZAREK_PODHALANSKI = 1000;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildZakopaneSouthArea(zone);
	}

	private void buildZakopaneSouthArea(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("Stary Baca") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(100, 120));
				nodes.add(new Node(115, 120));
				nodes.add(new Node(115, 110));
				nodes.add(new Node(115, 120));
				nodes.add(new Node(100, 120));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				class DogSellerBehaviour extends SellerBehaviour {
					DogSellerBehaviour(final Map<String, Integer> items) {
						super(items);
					}

					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
						if (res.getAmount() > 1) {
							seller.say("Hmm... Nie sądzę, abyś mógł zaopiekować się więcej niż jednym owczarkiem naraz.");
							return false;
						} else if (player.hasPet()) {
							say("Dlaczego nie upewnisz się i nie poszukasz zwierzątka, które już masz?");
							return false;
						} else {
							if (!player.drop("money", getCharge(res,player))) {
								seller.say("Nie masz tyle pieniędzy.");
								return false;
							}
							if (res.getChosenItemName().equals("owczarek podhalański")) {

								seller.say("Proszę bardzo, mały owczarek podhalański! Twój owczarek żywi się udkiem, mięsem, szynką, kiełbasą swojską, stekiem, oscypek lub żyntycą. Ciesz się!");

								final OwczarekPodhalanski owczarek_podhalanski = new OwczarekPodhalanski(player);
								Entity sellerEntity = seller.getEntity();
								owczarek_podhalanski.setPosition(sellerEntity.getX(), sellerEntity.getY() + 1);

								//player.setPet(owczarek);
								player.setPet(owczarek_podhalanski);
								player.notifyWorldAboutChanges();

								return true;
							} else {
								seller.say("Proszę bardzo oto mały owczarek! Twój owczarek żywi się udkiem, mięsem, szynką, kiełbasą swojską, stekiem, oscypek lub żyntycą. Ciesz się!");

								final Owczarek owczarek = new Owczarek(player);
								Entity sellerEntity = seller.getEntity();
								owczarek.setPosition(sellerEntity.getX(), sellerEntity.getY() + 1);

								//player.setPet(owczarek);
								player.setPet(owczarek);
								player.notifyWorldAboutChanges();

								return true;
							}
						}
					}
				}

				final Map<String, Integer> items = new HashMap<String, Integer>();
				items.put("owczarek", BUYING_PRICE_OWCZAREK);
				items.put("owczarek podhalański", BUYING_PRICE_OWCZAREK_PODHALANSKI);

				addGreeting();
				addJob("Sprzedaję owczarki. Kiedy je sprzedaję są małymi owczarkami, ale kiedy się takim owczarkiem #zaopiekujesz to wyrasta na pięknego owczarka.");
				addHelp("Sprzedaję owczarki, aby kupić jednego wystarczy mi powiedzieć #kupię #owczarek. Jeżeli jesteś nowy w tym interesie to mogę Ci powiedzieć jak #podróżować i jak #opiekować się owczarkami. Jeżeli znajdziesz dzikiego owczarka to możesz go #przygarnąć.");
				addGoodbye();
				new SellerAdder().addSeller(this, new DogSellerBehaviour(items));
				addReply(Arrays.asList("care", "zaopiekujesz", "opiekować"),
						"Owczarki kochają meat, ham, kiełbasa swojska, steak i oscypek. Wystarczy położyć kawałek na ziemi, a owczarek podejdzie i zje. Możesz sprawdzić jego wagę klikając prawym przyciskiem  na niego i wybierając 'Zobacz'. Jego waga będzie rosła po zjedzeniu każdego ham.");
				addReply(Arrays.asList("travel", "podróżować"),
						"Gdy zmieniasz miejsce pobytu twój owczarek powinien być blisko Ciebie, aby nie zginął. Jeżeli nie zwraca na Ciebie uwagi wystarczy powiedzieć #owczarek aby go zawołać. Jeśli zdecydujesz się porzucić go to kliknij na siebie prawym przyciskiem i wybierz 'Porzuć zwierzątko', ale szczerze mówiąc sądzę, że takie zachowanie jest odrażające.");
				addReply("sell",
						"Sprzedać??? Jakiego rodzaju potworem jesteś? Dlaczego w ogóle chciałbyś sprzedać swojego pięknego owczarka?");
				addReply(Arrays.asList("own", "przygarnąć"),
						"Jeżeli znajdziesz dzikiego lub porzuconego owczarka to, aby go przygarnąć naciśnij na nim prawy przycisk myszki i wybierz 'Przygarnij', a wtedy zacznie chodzić za tobą. Owczarki stają się trochę dzikie bez właściciela!");
			}
		};

		npc.setEntityClass("npcstarybaca");
		npc.setPosition(100, 120);
		npc.initHP(100);
		npc.setDescription("Stary Baca opiekuje się owczarkami.");
		zone.add(npc);
	}
}
