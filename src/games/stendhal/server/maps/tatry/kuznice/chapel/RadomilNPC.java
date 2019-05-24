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
package games.stendhal.server.maps.tatry.kuznice.chapel;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.BehaviourAction;
import games.stendhal.server.entity.npc.behaviour.adder.FreeHealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.Behaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;

public class RadomilNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();
	// ile karmy potrzeba do zdjecia czaszki
	private static final int AMOUNT = 200;

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
		final SpeakerNPC npc = new SpeakerNPC("Radomil") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 2));
				nodes.add(new Node(14, 2));
				nodes.add(new Node(14, 7));
				nodes.add(new Node(13, 7));
				nodes.add(new Node(13, 8));
				nodes.add(new Node(6, 8));
				nodes.add(new Node(6, 7));
				nodes.add(new Node(5, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Zajmuję się tą kapliczką oraz posiadam niezwykłą moc, która pomaga mi uleczyć rany.");
				addHelp("Mogę Cię #'uleczyć' lub #zdjąć z ciebie #'czaszkę'.");
				new FreeHealerAdder().addHealer(this, 0);
				addOffer("Potrafię również zdjąć z Ciebie piętno zabójcy. Powiedz mi tylko #'zdejmij czaszkę'.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("eliksiry")));

				addReply("zdejmij", null,
						new BehaviourAction(new Behaviour("czaszkę"), Arrays.asList("remove", "zdejmij"), "offer") {
					@Override
					public void fireSentenceError(Player player, Sentence sentence, EventRaiser raiser) {
						raiser.say(sentence.getErrorString() + " Próbujesz mnie oszukać?");
					}

					@Override
					public void fireRequestOK(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {

						if (player.getKarma() < AMOUNT) {
								raiser.say("Nie pomogłeś wystarczającej liczbie osób! Twoja karma to: " + player.getKarma() + ". Przyjdź kiedy indziej.");
						} else {
							if (player.getKarma() >= AMOUNT) {
								player.rehabilitate();
								raiser.say("Zdjąłem z Ciebie piętno zabójcy. Uważaj na siebie!");
							} else {
								raiser.say("Twoja karma jest na poziomie" + player.getKarma() + ", a potrzebujesz conajmniej " + AMOUNT + ", abym mógł zdjąć z Ciebie piętno zabójcy.");
							}
						}
					}
				});
				addGoodbye();
			}
		};

		npc.setEntityClass("npcwikary");
		npc.setPosition(5, 2);
		zone.add(npc);
	}
}
