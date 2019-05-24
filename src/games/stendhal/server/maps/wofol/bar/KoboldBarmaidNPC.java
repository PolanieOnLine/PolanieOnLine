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
package games.stendhal.server.maps.wofol.bar;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
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

/**
  * Provides Wrviliza, the kobold barmaid in Wo'fol.
  * She's Wrvil's wife.
  *
  * Offers a quest wich rewards the player with some bottles of V.S.O.P. koboldish torcibud.
  *
  * @author omero
  */
public class KoboldBarmaidNPC implements ZoneConfigurator {

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

		final SpeakerNPC npc = new SpeakerNPC("Wrviliza") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(14, 2));
				nodes.add(new Node(14, 3));
				nodes.add(new Node(11, 3));
				nodes.add(new Node(11, 2));
				nodes.add(new Node(6, 2));
				nodes.add(new Node(6, 3));
				nodes.add(new Node(9, 3));
				nodes.add(new Node(9, 2));
				setPath(new FixedPath(nodes, true));
			}

			@Override

			protected void createDialog() {
				class TorcibudSellerBehaviour extends SellerBehaviour {
					TorcibudSellerBehaviour(final Map<String, Integer> items) {
						super(items);
					}

					/**
					  * Wrviliza will sell her mild or strong koboldish torcibud
					  * only when the player can afford the price and carries as many empty bottles
					  * as the requested amount in his inventory.
					  */
					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
						String chosenItemName = res.getChosenItemName();
						final Item item = getAskedItem(chosenItemName);
						int amount = res.getAmount();
						String requiredContainer = "";

						if ("nalewka litworowa".equals(chosenItemName)) {
							requiredContainer = "wąska butelka";
						} else if ("mocna nalewka litworowa".equals(chosenItemName)) {
							requiredContainer = "butla czwórniaczka";
						}

						int price = getCharge(res, player);
						if (player.isBadBoy()) {
						        price = (int) (BAD_BOY_BUYING_PENALTY * price);
						}
						
						if ("wąska butelka".equals(requiredContainer) || "butla czwórniaczka".equals(requiredContainer)) {
							if (!player.isEquipped(requiredContainer, amount) || !player.isEquipped("money", price)) {
								seller.say("Hau! Mogę sprzedać Tobie tylko "
									+ Grammar.plnoun(amount, chosenItemName)
									+ " jeżeli spotkałeś się z ceną " + price + " i masz " + amount + " pustą "
									+ Grammar.plnoun(amount, requiredContainer) + ".");
							        return false;
							}
						} else if (!player.isEquipped("money", price)) {
								seller.say("Hau! Mogę sprzedać Tobie tylko "
									+ Grammar.plnoun(amount, chosenItemName)
									+ " jeżeli masz wystarczająco dużo money.");
						        return false;
						}
						
                        /**
                         * If the user tries to buy several of a non-stackable item,
                         * he is forced to buy only one.
                         */
                        if (item instanceof StackableItem) {
                            ((StackableItem) item).setQuantity(amount);
                        } else {
                            res.setAmount(1);
                        }
						
						if (player.equipToInventoryOnly(item)) {
							player.drop("money", price);
							if (!"".equals(requiredContainer)) {
						               	player.drop(requiredContainer, amount);
							}
							seller.say("Hau! Oto "
									+ Grammar.isare(amount) + " your "
									+ Grammar.plnoun(amount, chosenItemName) + "!");
							return true;
					        } else {
					                seller.say("Hau.. W tej chwili nie możesz dodać więcej "
 					                       + Grammar.plnoun(amount, chosenItemName)
                    				       + " do plecaka.");
					                return false;
					        }
					}
				}

                // edit prices here and they'll be correct everywhere else
                final int MILD_KOBOLDISH_TORCIBUD_PRICE = 95;
                final int STRONG_KOBOLDISH_TORCIBUD_PRICE = 195;
                
				final Map<String, Integer> items = new HashMap<String, Integer>();
				//beer and wine have higher than average prices here.
				items.put("sok z chmielu", 18);
				items.put("napój z winogron", 25);
				items.put("nalewka litworowa", MILD_KOBOLDISH_TORCIBUD_PRICE);
				items.put("mocna nalewka litworowa", STRONG_KOBOLDISH_TORCIBUD_PRICE);

				new SellerAdder().addSeller(this, new TorcibudSellerBehaviour(items));

				addGreeting(
					"Hau! Witam w Kobold's Den bar wanderer!"
						+ " Jestem Wrviliza, żona #Wrvila."
						+ " Jeżeli chcesz, abym #zaoferowała trochę napojów to daj znać!");
				addJob("Hau! Oferuję napój z winogron, sok z chmielu i mój specjał #'nalewka litworowa' lub #'mocna nalewka litworowa'.");
				addHelp("Hau... Jeżeli jesteś spragniony to mogę #zaoferować trochę napojów. Jeżeli nie zauważałeś to jest pub!");
				addGoodbye("Hau... DOwidzenia i powodzenia!");

				addReply(Arrays.asList("wine","beer","napój z winogron","sok z chmielu"),
						"Hau! Ugasi twoje pragnienie za kilka monet...");
				addReply(Arrays.asList("mild","nalewka litworowa"),
						"Hau! Nie jest to #'mocna nalewka litworowa'. Daj mi pustą #wąską #butelkę i "
                            + MILD_KOBOLDISH_TORCIBUD_PRICE + " money... Hau!");
				addReply(Arrays.asList("strong","mocna nalewka litworowa"),
						"Hau! Nie jest to #'nalewka litworowa'. Daj mi pustą #butlę #czwórniaczkę i "
                            + STRONG_KOBOLDISH_TORCIBUD_PRICE + " money... Hau!");
				addReply(Arrays.asList("torcibud","mocna nalewka litworowa"),
						"Hau! Prawdziwy sekretny koboldzki składnik receptury! Zapytaj mnie o #ofertę!");
				addReply("wrvil",
						"Hau! Będzie moim mężem. Prowadzi sklep w północnej części Wo'fol...");
				addReply(Arrays.asList("eared bottle","butlę czwórniaczkę","butlę","czwórniaczkę"),
						"Hau! szeroka butla z uszami przy szyjce... To nie możesz być ty skoro nie widziałeś żadnej!");
				addReply(Arrays.asList("slim bottle","wąską butelkę","wąską","butelkę"),
						"Hau! Węższa butelka na dole i trochę szersza u góry ... Jestem pewna, że już ją widziałeś!");

                /**
                 * Additional behavior code is in games.stendhal.server.maps.quests.KoboldishTorcibud
                 */
			}
		};

		npc.setEntityClass("koboldbarmaidnpc");
		npc.setPosition(9, 3);
		npc.initHP(100);
		npc.setDescription("Oto Wrviliza kelnerka kobold.");
		zone.add(npc);

	}
}
