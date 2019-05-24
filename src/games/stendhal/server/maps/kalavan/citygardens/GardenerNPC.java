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
package games.stendhal.server.maps.kalavan.citygardens;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/**
 * Builds the gardener in Kalavan city gardens.
 *
 * @author kymara
 */
public class GardenerNPC implements ZoneConfigurator {

	private static final String QUEST_SLOT = "sue_swap_kalavan_city_scroll";
    private static final Integer MAX_LUNCHES = 7;

	//
	// ZoneConfigurator
	//

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
		final SpeakerNPC npc = new SpeakerNPC("Sue") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(100, 123));
				nodes.add(new Node(110, 123));
				nodes.add(new Node(110, 110));
				nodes.add(new Node(119, 110));
				nodes.add(new Node(119, 122));
				nodes.add(new Node(127, 122));
				nodes.add(new Node(127, 111));
				nodes.add(new Node(118, 111));
				nodes.add(new Node(118, 123));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				class SpecialProducerBehaviour extends ProducerBehaviour { 
					SpecialProducerBehaviour(final List<String> productionActivity,
                        final String productName, final Map<String, Integer> requiredResourcesPerItem,
											 final int productionTimePerItem) {
						super(QUEST_SLOT, productionActivity, productName,
							  requiredResourcesPerItem, productionTimePerItem, false);
					}

					@Override
						public boolean askForResources(ItemParserResult res, final EventRaiser npc, final Player player) {
						int amount = res.getAmount();

						if (player.hasQuest(QUEST_SLOT) && player.getQuest(QUEST_SLOT).startsWith("done;")) {
							// she is eating. number of lunches is in tokens[1]
							final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
							// delay is number of lunches * one day - eats one lunch per day
							final long delay = (Long.parseLong(tokens[1])) * MathHelper.MILLISECONDS_IN_ONE_DAY;
							final long timeRemaining = (Long.parseLong(tokens[2]) + delay)
								- System.currentTimeMillis();
							if (timeRemaining > 0) {
								npc.say("Wciąż jem drugie śniadanie, które ostatnio mi przyniosłeś. Wystarczy mi go na następne "
                                        + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000))
                                        + "!");
                                return false;
							}
					    } 
						if (amount > MAX_LUNCHES) {
							npc.say("Nie mogę wziąć więcej kanapek niż raz na tydzień! Staną się czerstwe!");
							return false;
						} else if (getMaximalAmount(player) < amount) {
							npc.say("Chciałbym tylko " + getProductionActivity() + " "
									+ Grammar.quantityplnoun(amount, getProductName(), "a")
									+ " jeżeli mi przyniesiesz "
									+ getRequiredResourceNamesWithHashes(amount) + ".");
							return false;
						} else {
							res.setAmount(amount);
							npc.say("W takim razie chcę "
									+ getRequiredResourceNamesWithHashes(amount)
									+ ". Przyniesiesz to?");
							return true;
						}
					}

					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser npc, final Player player) {
						int amount = res.getAmount();

						if (getMaximalAmount(player) < amount) {
							// The player tried to cheat us by placing the resource
							// onto the ground after saying "yes"
							npc.say("Hej! Tutaj jestem! Lepiej nie próbuj mnie oszukać...");
							return false;
						} else {
							for (final Map.Entry<String, Integer> entry : getRequiredResourcesPerItem().entrySet()) {
                                final int amountToDrop = amount * entry.getValue();
                                player.drop(entry.getKey(), amountToDrop);
							}
							final long timeNow = new Date().getTime();
							player.setQuest(QUEST_SLOT, amount + ";" + getProductName() + ";"
											+ timeNow);
							npc.say("Dziękuję! Wróć za "
									+ getApproximateRemainingTime(player) + ", a będę miał dla Ciebie " 
									+ Grammar.quantityplnoun(amount, getProductName(), "a") + ".");
							return true;
						}
					}

					@Override
					public void giveProduct(final EventRaiser npc, final Player player) {
						final String orderString = player.getQuest(QUEST_SLOT);
						final String[] order = orderString.split(";");
						final int numberOfProductItems = Integer.parseInt(order[0]);
						// String productName = order[1];
						final long orderTime = Long.parseLong(order[2]);
						final long timeNow = new Date().getTime();
						if (timeNow - orderTime < getProductionTime(numberOfProductItems) * 1000) {
							npc.say("Witaj ponownie! Oo wciąż nie mam twoich zwojów! Wróć za "
									+ getApproximateRemainingTime(player) + ", aby je odebrać.");
						} else {
                        final StackableItem products = (StackableItem) SingletonRepository.getEntityManager().getItem(
                                        getProductName());

                        products.setQuantity(numberOfProductItems);

                        if (isProductBound()) {
							products.setBoundTo(player.getName());
                        }

                        player.equipOrPutOnGround(products);
                        npc.say("Witaj z powrotem! Schowałem drugie śniadanie na później. W zamian weź "
								+ Grammar.quantityplnoun(numberOfProductItems,
                                                        getProductName(), "a") + ".");
                        // store the number of lunches given and the time so we know how long she eats for
						player.setQuest(QUEST_SLOT, "done" + ";" + numberOfProductItems + ";"
										+ System.currentTimeMillis());
                        // give some XP as a little bonus for industrious workers
                        player.addXP(15 * numberOfProductItems);
                        player.notifyWorldAboutChanges();
						}
					}
				}
				addReply(ConversationPhrases.YES_MESSAGES, "Bardzo ciepło...");
				addReply(ConversationPhrases.NO_MESSAGES, "Lepszy niż deszczowy!");
				addJob("Jestem ogrodnikiem. Mam nadzieję, że podobają Ci się rabatki.");
				addHelp("Jeżeli przyniesiesz mi #drugie #śniadanie to wymienię go na magiczny zwój. Powiedz tylko #wymień.");
				addOffer("Moje pomidory i czosnek mają się dobrze. Mam wystarczająco dużo, aby trochę sprzedać.");
				final Map<String, Integer> offerings = new HashMap<String, Integer>();
                offerings.put("pomidor", 30);
                offerings.put("czosnek", 50);
                new SellerAdder().addSeller(this, new SellerBehaviour(offerings), false);
				addReply(Arrays.asList("drugie", "śniadanie", "lunch"), "Poproszę filiżankę herbaty!");
				addReply(Arrays.asList("sandwich", "kanapka"), "Mmm.. Chciałbym z szynką i serem.");
				addReply(Arrays.asList("zwój kalavan", "scroll"), "To magiczny zwój, który może cię zabrać do Kalavan. Nie pytaj mnie jak działa!");
				
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();	
				requiredResources.put("filiżanka herbaty", 1);
				requiredResources.put("kanapka", 1);

				final ProducerBehaviour behaviour = new SpecialProducerBehaviour(Arrays.asList("swap", "wymień"), "zwój kalavan", requiredResources, 1 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Piękny [daylightphase]. Nieprawdaż?");
				addQuest("Kocham filiżankę #herbaty. Przy uprawianiu ogrodu zawsze chce się pić. Jeżeli przyniesiesz mi też #sandwich to wymienię ją za magiczny zwój. Powiedz tylko #wymień.");
				addReply(Arrays.asList("tea", "cup of tea", "herbata", "herbaty", "filiżanka herbaty"), "Starsza Granny Graham może zaparzyć filiżankę herbaty. Ona jest tam w tej dużej chacie.");
				addGoodbye("Dowidzenia. Podziwiaj resztę ogrodów.");
			}
		};

		npc.setEntityClass("gardenernpc");
		npc.setPosition(100, 123);
		npc.initHP(100);
		npc.setDescription("Widzisz Sue. Jej kwiaty są przepiękne. Ona naprawdę ma do tego rękę.");
		zone.add(npc);
	}

}
