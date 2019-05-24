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
package games.stendhal.server.maps.sedah.gatehouse;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.BehaviourAction;
import games.stendhal.server.entity.npc.behaviour.impl.Behaviour;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds a gatekeeper NPC Bribe him with at least 300 money to get the key for
 * the Sedah city walls. He stands in the doorway of the gatehouse till the
 * interior is made.
 *
 * @author kymara
 */
public class GateKeeperNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Revi Borak") {

			@Override
			protected void createPath() {
				// not moving.
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.isEquipped("klucz do bram Sedah")) {
							// toss a coin to see if he notices player still has
							// the gate key
							if (Rand.throwCoin() == 1) {
								player.drop("klucz do bram Sedah");
								raiser.say("Nie powinieneś kraść tego klucza! Będę miał go z powrotem.");
							} else {
								raiser.say("Witam ponownie.");
							}
						} else {
							raiser.say("Czego chcesz?");
						}
					}
				});
				addReply(Arrays.asList("nothing", "nic"), "Dobrze.");
				addReply(Arrays.asList("key", "klucz"), "Jestem otwarty na propozycję...");
				addJob("Jestem strażnikiem imperialnego miasta Sedah. Nie powinienem nikogo wpuszczać, ale możesz złożyć mi #ofertę.");
				addHelp("Bez tego klucza nie dostaniesz się do imperialnego miasta Sedah. Mogę ci go dać gdy powiesz #'łapówka <kwota jaką uważasz>'.");
				addQuest("Jedyną rzecz jaką potrzebuje są zimne twarde pieniądze.");
				addOffer("Jedynie #łapówka może mnie skłonić do przekazania klucza do tej bramy.");

				addReply(Arrays.asList("bribe", "łapówka"), null,
					new BehaviourAction(new Behaviour("money"), Arrays.asList("bribe", "łapówka"), "offer") {
						@Override
						public void fireSentenceError(Player player, Sentence sentence, EventRaiser raiser) {
							raiser.say(sentence.getErrorString() + " Próbujesz mnie oszukać? Przekup mnie określoną kwotą!");
						}

						@Override
						public void fireRequestOK(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
				        	final int amount = res.getAmount();

				        	if (sentence.getExpressions().size() == 1) {
        						// player only said 'bribe'
        						raiser.say("Łapówka bez pieniędzy to nie łapówka! Przekup mnie jakąś kwotą!");
				        	} else {
				        		if (amount < 500) {
									// Less than 300 is not money for him
									raiser.say("Myślisz, że tyle mnie zadowoli?! Więcej zarabia sprzątaczka!");
								} else {
									if (player.isEquipped("money", amount)) {
										player.drop("money", amount);
										raiser.say("Ok, mam twoje pieniądze, a tutaj masz klucz.");
										final Item key = SingletonRepository.getEntityManager().getItem(
												"klucz do bram Sedah");
										player.equipOrPutOnGround(key);
									} else {
										// player bribed enough but doesn't have
										// the cash
										raiser.say("Oszuście! Nie masz " + amount + " money!");
									}
								}
							}
			        	}

						@Override
						public void fireRequestError(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
							if (res.getChosenItemName() == null) {
								fireRequestOK(res, player, sentence, raiser);
        			        } else {
        						// This bit is just in case the player says 'bribe X potatoes', not money
        						raiser.say("Niczym mnie nie możesz przekupić poza pieniędzmi!");
							}
						}
				});

				addGoodbye("Dowidzenia. Nie mów, że Cię nie ostrzegałem!");
			}
		};

		npc.setDescription("Oto wyglądający na silnego żołnierz. Wygląda na skłonnego do wpółpracy za odpowiednią kwotę.");
		/*
		 * We don't seem to be using the recruiter images that lenocas made for
		 * the Fado Raid area so I'm going to put him to use here. If the raid
		 * part ever gets done, this image can change.
		 */
		npc.setEntityClass("recruiter2npc");
		npc.setPosition(120, 67);
		npc.initHP(100);
		zone.add(npc);
	}
}
