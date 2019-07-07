/* $Id: WikaryNPC.java,v 1.25 2011/06/21 02:28:01 Legolas Exp $ */
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
 //Zrobiony na podstawie GateKeeperNPC z Sedah city

package games.stendhal.server.maps.zakopane.church;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.BehaviourAction;
import games.stendhal.server.entity.npc.behaviour.impl.Behaviour;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a gatekeeper NPC Bribe him with at least 300 money to get the key for
 * the Sedah city walls. He stands in the doorway of the gatehouse till the
 * interior is made.
 *
 * @author kymara
 */
public class WikaryNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Wikary") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 12));
				nodes.add(new Node(8, 12));
				nodes.add(new Node(8, 11));
				nodes.add(new Node(10, 11));
				nodes.add(new Node(10, 10));
				nodes.add(new Node(13, 10));
				nodes.add(new Node(13, 11));
				nodes.add(new Node(15, 11));
				nodes.add(new Node(15, 12));
				nodes.add(new Node(20, 12));
				nodes.add(new Node(15, 12));
				nodes.add(new Node(15, 11));
				nodes.add(new Node(13, 11));
				nodes.add(new Node(13, 10));
				nodes.add(new Node(10, 10));
				nodes.add(new Node(10, 11));
				nodes.add(new Node(8, 11));
				nodes.add(new Node(8, 12));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.isEquipped("gigantyczny eliksir")) {
							// give some money to get
							// giant potion
							if (Rand.throwCoin() == 1) {
								raiser.say("Widzę, że potrzebujesz więcej.");
							} else {
								raiser.say("Witaj ponownie.");
							}
						} else {
							raiser.say("Witaj! Czyżbyś przybył tutaj, aby złożyć #'datek'?");
						}
					}
				});

				addReply("nic", "Dobrze.");
				addJob("Jestem wikarym w Zakopanem. Zbieram pieniądze na zakup organów do kaplicy, zapytaj mnie o #ofertę, aby dowiedzieć się jak mogę Cię wynagrodzić za ten gest.");
				addHelp("Wspomóż zakup organów do kaplicy, mówiąc #datek #<ilość> #money lub zdejmij klątwę z siebie, mówiąc #zdejmij #<ilość> #money");
				addQuest("Jedyną rzecz jaką potrzebuje to #datek na organy do kaplicy. Po za tym za nie wielką opłatą mogę #zdjąć z Ciebie piętno zabójcy.");
				addOffer("Dam ci miksturę, która cię uleczy w zamian za drobny #datek na organy.");

				addReply("datek", null,
					new BehaviourAction(new Behaviour("money"), Arrays.asList("charity", "datek"), "offer") {
					@Override
					public void fireSentenceError(Player player, Sentence sentence, EventRaiser raiser) {
						raiser.say(sentence.getErrorString() + " Próbujesz mnie oszukać?");
					}

					@Override
						public void fireRequestOK(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
						final int amount = res.getAmount();

						if (sentence.getExpressions().size() == 1) {
							// player only said 'datek'
							raiser.say("Nie masz tyle pieniędzy, aby wesprzeć zakup organów. Przyjdź kiedy indziej.");
						} else {
							if (amount < 4000) {
								// Less than 4000 is not money for him
								raiser.say("Nie masz tyle pieniędzy, aby wesprzeć zakup organów. Przyjdź kiedy indziej.");
							} else {
								if (player.isEquipped("money", amount)) {
									player.drop("money", amount);
									raiser.say("Bóg Ci zapłać. Jesteśmy coraz bliżej zakupu nowych organów!");
									final Item drink = SingletonRepository.getEntityManager().getItem(
											"gigantyczny eliksir");
									player.equipOrPutOnGround(drink);
								} else {
									// player gave enough but doesn't have
									// the cash
									raiser.say("Nie masz " + amount + " money, aby wesprzeć zakup organów. Przyjdź kiedy indziej.");
								}
							}
						}
					}

					@Override
					public void fireRequestError(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (res.getChosenItemName() == null) {
							fireRequestOK(res, player, sentence, raiser);
						} else {
							// This bit is just in case the player says 'datek X potatoes', not money
							raiser.say("Potrzebuję pieniędzy na zakup organów.");
						}
					}
				});

				addReply("zdejmij", null,
						new BehaviourAction(new Behaviour("money"), Arrays.asList("remove", "zdejmij"), "offer") {
					@Override
					public void fireSentenceError(Player player, Sentence sentence, EventRaiser raiser) {
						raiser.say(sentence.getErrorString() + " Próbujesz mnie oszukać?");
					}

					@Override
						public void fireRequestOK(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
						final int amount = res.getAmount();

						if (sentence.getExpressions().size() == 1) {
							// player only said 'zdejmij'
							raiser.say("Nie masz tyle pieniędzy, abym mógł zdjąć z Ciebie piętno zabójcy. Przyjdź kiedy indziej.");
						} else {
							if (amount < 100000) {
								// Less than 100000 is not money for him
								raiser.say("Nie masz tyle pieniędzy, abym mógł zdjąć z Ciebie piętno zabójcy. Przyjdź kiedy indziej.");
							} else {
								if (player.isEquipped("money", amount)) {
									player.drop("money", amount);
									player.rehabilitate();
									raiser.say("Zdjąłem z Ciebie piętno zabójcy. Uważaj na siebie!");
								} else {
									// player gave enough but doesn't have
									// the cash
									raiser.say("Nie masz " + amount + " money, abym mógł zdjąć z Ciebie piętno zabójcy.");
								}
							}
						}
					}

					@Override
					public void fireRequestError(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (res.getChosenItemName() == null) {
							fireRequestOK(res, player, sentence, raiser);
						} else {
							// This bit is just in case the player says 'datek X potatoes', not money
							raiser.say("Potrzebuję pieniędzy na zakup organów.");
						}
					}
				});

				addGoodbye("Bywaj!");
			}
		};

		npc.setDescription("Oto Wikary. Zbiera pieniądze na zakup organów do kaplicy.");
		/*
		 * We don't seem to be using the recruiter images that lenocas made for
		 * the Fado Raid area so I'm going to put him to use here. If the raid
		 * part ever gets done, this image can change.
		 */
		npc.setEntityClass("npcwikary");
		npc.setPosition(3, 12);
		npc.initHP(100);
		zone.add(npc);
	}
}
