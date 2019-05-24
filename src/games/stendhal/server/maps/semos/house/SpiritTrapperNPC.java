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
package games.stendhal.server.maps.semos.house;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.MultiProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.MultiProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.TeleporterBehaviour;
import games.stendhal.server.entity.player.Player;

/**
 * Builds a shady Spirit Trapper NPC for the Empty Bottle quest.
 *
 * @author soniccuz based on FlowerSellerNPC by kymara and fishermanNPC by dine.
 */
public class SpiritTrapperNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
			final List<String> setZones = new ArrayList<String>();
			setZones.add("0_ados_swamp");
			setZones.add("0_ados_outside_w");
			setZones.add("0_ados_wall_n2");
			setZones.add("0_ados_wall_s");
			setZones.add("0_ados_city_s");
        	new TeleporterBehaviour(buildSemosHouseArea(), setZones, "0_ados", "..., ....");
	}

	private SpeakerNPC buildSemosHouseArea() {
		final SpeakerNPC mizuno = new SpeakerNPC("Mizuno") {
	    	@Override
	    	protected void createPath() {
	    		// npc does not move
	    		setPath(null);
	    	}

	    	@Override
	    	protected void createDialog() {
	    		addGreeting("Czego potrzebujesz?");
	    		addJob("Wolę to zatrzymać dla siebie.");
	    		addHelp("Spójrz. Muszę wkrótce wyjechać, ale szybko. Jeżeli masz #'czarne perły' to sprzedam parę magicznych #strzał za nie. Chcesz #kupić trochę #'strzał'?");
	    		addOffer("Spójrz. Muszę wkrótce wyjechać, ale szybko, jeśli masz jakiekolwiek #'czarne perły' to sprzedam parę magicznych #strzał za nie. Chciałbyś może #kupić trochę #'strzał'?");
	    		addGoodbye("W takim razie idę... Nie mogę pracować w twoim towarzystwie.");

	    		addReply("arrows","Potrafię wykorzystać specjalną moc do zaczarowania strzał mocą żywiołów. Mam #lód, #ogień i #'światło'.");
	    		addReply(Arrays.asList("ice", "ice arrow", "fire", "fire arrow", "lód", "strzała lodowa", "ogień", "strzała ognia"),
	    				"Mogę stworzyć 1 z tych strzał za każdą przyniesioną mi czarną perłe.");
	    		addReply(Arrays.asList("light", "light arrow", "świato", "strzała światła"),
	    				"Strzały światła są trudne do wytworzenia, ale mogę stworzyć 1 za każde przyniesione do mnie 2 czarne perły.");
	    		addReply(Arrays.asList("czarne perły", "perły"),
	    				"Dla mnie one robią przyzwoite talizmany. Proponuję poszukać je u zabójców.");
			    // the rest is in the MessageInABottle quest

			    // Mizuno exchanges elemental arrows for black pearls.
				// (uses sorted TreeMap instead of HashMap)
	    		final HashSet<String> productsNames = new HashSet<String>();
	    			productsNames.add("strzała lodowa");
	    			productsNames.add("strzała ognia");
	    			productsNames.add("strzała światła");

	    		final Map<String, Integer> reqRes_iceArrow = new TreeMap<String, Integer>();
	    			reqRes_iceArrow.put("czarna perła", 1);
	    		final Map<String, Integer> reqRes_fireArrow = new TreeMap<String, Integer>();
	    			reqRes_fireArrow.put("czarna perła", 1);
	    		final Map<String, Integer> reqRes_lightArrow = new TreeMap<String, Integer>();
	    			reqRes_lightArrow.put("czarna perła", 2);

	    		final HashMap<String, Map<String, Integer>> requiredResourcesPerProduct = new HashMap<String, Map<String, Integer>>();
	    			requiredResourcesPerProduct.put("strzała lodowa", reqRes_iceArrow);
	    			requiredResourcesPerProduct.put("strzała ognia", reqRes_fireArrow);
	    			requiredResourcesPerProduct.put("strzała światła", reqRes_lightArrow);

	    		final HashMap<String, Integer> productionTimesPerProduct = new HashMap<String, Integer>();
	    			productionTimesPerProduct.put("strzała lodowa", 0 * 60);
	    			productionTimesPerProduct.put("strzała ognia", 0 * 60);
	    			productionTimesPerProduct.put("strzała światła", 0 * 60);

	    		final HashMap<String, Boolean> productsBound = new HashMap<String, Boolean>();
	    			productsBound.put("strzała lodowa", false);
	    			productsBound.put("strzała ognia", false);
	    			productsBound.put("strzała światła", false);

                class SpecialTraderBehaviour extends MultiProducerBehaviour {
                	public SpecialTraderBehaviour(String questSlot, List<String> productionActivity,
                			HashSet<String> productsNames,
                			HashMap<String, Map<String, Integer>> requiredResourcesPerProduct,
                			HashMap<String, Integer> productionTimesPerProduct,
                			HashMap<String, Boolean> productsBound) {
                		super(questSlot, productionActivity, productsNames, requiredResourcesPerProduct, productionTimesPerProduct,
                				productsBound);
                	}

                	@Override
                	public boolean askForResources(final ItemParserResult res, final EventRaiser npc, final Player player) {
                		int amount = res.getAmount();
                		String productName = res.getChosenItemName();

                		if (getMaximalAmount(productName, player) < amount) {
                			npc.say("Mogę jedynie " + getProductionActivity() + " "
                					+ Grammar.quantityplnoun(amount, productName, "a")
                					+ ", jeśli przyniesiesz mi "
                					+ getRequiredResourceNamesWithHashes(productName, amount) + ".");
                			return false;
                		} else {
                			res.setAmount(amount);
                			npc.say(Grammar.quantityplnoun(amount, productName, "a")
                					+ " za "
                					+ getRequiredResourceNamesWithHashes(productName, amount) + ". "
                					+ " Zgadzasz się?");

                			return true;
                		}
                	}

                	@Override
                	public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser npc, final Player player) {
                		int amount = res.getAmount();
                		String productName = res.getChosenItemName();

                		if (getMaximalAmount(productName, player) < amount) {
				            // The player tried to cheat us by placing the resource
				            // onto the ground after saying "yes"
                			npc.say("Hej! Jestem tutaj! Lepiej nie próbuj mnie oszukać...");
				            return false;
				        } else {
				            for (final Map.Entry<String, Integer> entry : getRequiredResourcesPerProduct(productName).entrySet()) {
				                final int amountToDrop = amount * entry.getValue();
				                player.drop(entry.getKey(), amountToDrop);
				            }
				            final long timeNow = new Date().getTime();
				            player.setQuest(getQuestSlot(), amount + ";" + productName + ";" + timeNow);

				            if (getProductionTime(productName, amount) == 0) {

				            	//If production time is 0 just give player the product
				            	final int numberOfProductItems = amount;
				            	final StackableItem products = (StackableItem) SingletonRepository.getEntityManager().getItem(productName);
				    			products.setQuantity(numberOfProductItems);

				    			if (isProductBound(productName)) {
				    				products.setBoundTo(player.getName());
				    			}

				    			if (player.equipToInventoryOnly(products)) {
				    				npc.say("Oto twoje " + Grammar.isare(numberOfProductItems)
									+ Grammar.quantityplnoun(numberOfProductItems,
											productName, "") + ".");

				    				player.setQuest(getQuestSlot(), "done");
				    				player.notifyWorldAboutChanges();
				    				player.incProducedCountForItem(productName, products.getQuantity());
				    				SingletonRepository.getAchievementNotifier().onProduction(player);
				    			} else {
				    				npc.say("Witaj z powrotem! Właśnie skończyłem twoje zamówienie. Ale teraz nie możesz wziąć "
				    						+ Grammar.plnoun(numberOfProductItems, productName)
				    						+ ". Wróć do mnie kiedy będziesz miał miejsce w plecaku.");
				    			}

				            	return true;
				            } else {
				            		npc.say("OK, to zacznę robić "
				                    + getProductionActivity() + " "
				                    + Grammar.quantityplnoun(amount, productName, "a")
				                    + " dla ciebie, ale to zajmie trochę czasu. Proszę wróć za "
				                    + getApproximateRemainingTime(player) + ".");
				            		return true;
				            	}
				        }
				    }
                }

                final MultiProducerBehaviour behaviour = new SpecialTraderBehaviour(
                        "arrow_trader",
                        Arrays.asList("buy", "kup", "kupię", "kupić"),
                        productsNames,
                        requiredResourcesPerProduct,
                        productionTimesPerProduct,
                        productsBound);

                    new MultiProducerAdder().addMultiProducer(this, behaviour,
                        "Czego potrzebujesz?");
			}
		};

		mizuno.setEntityClass("man_001_npc");
		mizuno.initHP(100);
		mizuno.setHP(80);
		mizuno.setCollisionAction(CollisionAction.REVERSE);
		mizuno.setDescription("Oto Mizuno. Jak duch nawiedza Ados robiąć nie wiadomo co.");

		// start in int_semos_house
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_semos_house");
		mizuno.setPosition(5, 6);
		zone.add(mizuno);

		return mizuno;
	}
}
