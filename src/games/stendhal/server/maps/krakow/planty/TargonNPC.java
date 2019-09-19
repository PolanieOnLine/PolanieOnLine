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

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.Goat;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.krakow.planty.KajetanNPC;
import games.stendhal.server.util.Area;
import marauroa.common.game.RPObject;

/**
 * Build a NPC
 *
 * @author KarajuSs
 */
public class TargonNPC implements ZoneConfigurator {
	// The goat pen where Targon moves what he buys
	/** Left X coordinate of the goat pen */ 
	private static final int GOAT_PEN_X = 105;
	/** Top Y coordinate of the goat pen */
	private static final int GOAT_PEN_Y = 23;
	/** Width of the goat pen */
	private static final int GOAT_PEN_WIDTH = 16;
	/** Height of the goat pen */
	private static final int GOAT_PEN_HEIGHT = 6;
	/** 
	 * The maximum number of goat Targon keeps in his goat pen.
	 */ 
	private static final int MAX_GOAT_IN_PEN = 8;
	/** The area covering the goat pen in Krakow */
	private Area pen;

	public class GoatBuyerSpeakerNPC extends SpeakerNPC {
		public GoatBuyerSpeakerNPC(String name) {
			super(name);
			// HP needs to be > 0 for Targon to appear in the killer list for
			// the wielki zły wilk
			setBaseHP(100);
			setHP(100);
		}
		/**
		 * Get the area of the goat pen where bought goat 
		 * should be moved.
		 * 
		 * @param zone the zone of the goat pen
		 * @return area of the goat pen
		 */
		private Area getPen(StendhalRPZone zone) {
			if (pen == null) {
				Rectangle2D rect = new Rectangle2D.Double();
				rect.setRect(GOAT_PEN_X, GOAT_PEN_Y, GOAT_PEN_WIDTH, GOAT_PEN_HEIGHT);
				pen = new Area(zone, rect);
			}
			
			return pen;
		}
		
		/**
		 * Try to get rid of the big bad wolf that could have spawned in the pen,
		 * but miss it sometimes.
		 *  
		 * @param zone the zone to check
		 */
		private void killWolves(StendhalRPZone zone) {
			if (Rand.throwCoin() == 1) { 
				for (RPObject obj : zone) {
					if (obj instanceof Creature) {
						Creature wolf = (Creature) obj;
						
						if ("wilk".equals(wolf.get("subclass")) && getPen(zone).contains(wolf)) {
							wolf.onDamaged(this, wolf.getHP());
								return;
						}
					}
				}
			}
		}
		
		/**
		 * Get a list of the goat in the pen.
		 * 
		 * @param zone the zone to check
		 * @return list of goat in the pen
		 */
		private List<Goat> goatInPen(StendhalRPZone zone) {
			List<Goat> goat = new LinkedList<Goat>();
			Area pen = getPen(zone);
			
			for (RPEntity entity : zone.getPlayerAndFriends()) {
				if (entity instanceof Goat) {
					if (pen.contains(entity)) {
						goat.add((Goat) entity);
					}
				}
			}
			
			return goat;
		}
		
		/**
		 * Move a bought goat to the den if there's space, or remove it 
		 * from the zone otherwise. Remove old goat if there'd be more
		 * than <code>MAX_GOAT_IN_PEN</code> after the addition.
		 * 
		 * @param goat the goat to be moved
		 */
		public void moveGoat(Goat goat) {
			// The area of the goat den.
			int x = Rand.randUniform(GOAT_PEN_X, GOAT_PEN_X + GOAT_PEN_WIDTH - 1);
			int y = Rand.randUniform(GOAT_PEN_Y, GOAT_PEN_Y + GOAT_PEN_HEIGHT - 1);
			StendhalRPZone zone = goat.getZone();
			List<Goat> oldGoat = goatInPen(zone);
			
			killWolves(zone);
			/*
			 * Keep the amount of goat reasonable. Targon
			 * a business man and letting the goat starve 
			 * would be bad for busines.
			 */
			if (oldGoat.size() >= MAX_GOAT_IN_PEN) {
				// Sato sells the oldest goat
				zone.remove(oldGoat.get(0));
			}
			
			if (!StendhalRPAction.placeat(zone, goat, x, y)) {
				// there was no room for the goat. Simply eat it
				goat.getZone().remove(goat);  
			}
		}
	}

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
		final SpeakerNPC npc = new GoatBuyerSpeakerNPC("Targon") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(95, 32));
				nodes.add(new Node(95, 31));
				nodes.add(new Node(92, 31));
				nodes.add(new Node(92, 25));
				nodes.add(new Node(100, 25));
				nodes.add(new Node(100, 31));
				nodes.add(new Node(99, 31));
				nodes.add(new Node(99, 32));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Skupuję kozy w Krakowie, ogólnie zajmuje się handlem kóz.");
				addHelp("Skupuję kozy i sądzę, że to uczciwa cena. Jeżeli się zdecydujesz na sprzedaż to powiedz #'sprzedam goat', a zrobimy interes!");
				addOffer("Jeżeli przyniesiesz mi kozę, która została przez Ciebie wypasiona to zostaniesz odpowiednio wynagrodzony.");
				// 350 money za kozę ważącą 100 kg
				addGoodbye();
			}
		};

		final Map<String, Integer> buyitems = new HashMap<String, Integer>();
		buyitems.put("goat", 350);
		new BuyerAdder().addBuyer(npc, new GoatBuyerBehaviour(buyitems), true);
		npc.setDescription("Oto Targon. Kupuje tylko w pełni wypasione kozy i płaci za nie całkiem dobrze.");
		npc.setEntityClass("buyernpc");
		npc.setPosition(98, 32);
		npc.setSounds(Arrays.asList("hiccup-1", "sneeze-1"));
		zone.add(npc);
	}

	private static class GoatBuyerBehaviour extends BuyerBehaviour {
		GoatBuyerBehaviour(final Map<String, Integer> items) {
			super(items);
		}

		private int getValue(ItemParserResult res, final Goat goat) {
			return Math.round(getUnitPrice(res.getChosenItemName()) * ((float) goat.getWeight() / (float) Goat.MAX_WEIGHT));
		}

		@Override
		public int getCharge(ItemParserResult res, final Player player) {
			if (player.hasGoat()) {
				final Goat goat = player.getGoat();
				return getValue(res, goat);
			} else {
				// npc's answer was moved to BuyerAdder. 
				return 0;
			}
		}

		@Override
		public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
			// res.getAmount() is currently ignored.

			final Goat goat = player.getGoat();

			if (goat != null) {
				if (seller.getEntity().squaredDistance(goat) > 5 * 5) {
					seller.say("Nie widzę stąd tej kozy! Przyprowadź ją bliżej, abym mógł sprawdzić.");
				} else if (getValue(res, goat) < KajetanNPC.BUYING_PRICE) {
					// prevent newbies from selling their goat too early
					seller.say("Ta koza wygląda na zbyt chudą. Nakarm ją trawą i wróć, gdy będzie grubsza.");
				} else {
					seller.say("Dziękuję! Oto twoje pieniądze.");
					payPlayer(res, player);
					player.removeGoat(goat);

					player.notifyWorldAboutChanges();
					if(seller.getEntity() instanceof GoatBuyerSpeakerNPC) {
						((GoatBuyerSpeakerNPC)seller.getEntity()).moveGoat(goat);
					} else {
						// only to prevent that an error occurs and the goat does not disappear
						goat.getZone().remove(goat);
					}

					return true;
				}
			} else {
				seller.say(player.getTitle() + " nie posiadasz kozy! W co próbujesz pogrywać?");
			}

			return false;
		}
	}
}
