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
package games.pol.server.maps.zakopane.plains;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.pol.server.maps.zakopane.city.BacaZbyszekNPC;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.Area;
import marauroa.common.game.RPObject;

/**
 * @author zekkeq
 */
public class DobromirNPC implements ZoneConfigurator {
	// The sheep pen where Dobromir moves what he buys
	/** Left X coordinate of the sheep pen */ 
	private static final int SHEEP_PEN_X = 15;
	/** Top Y coordinate of the sheep pen */
	private static final int SHEEP_PEN_Y = 86;
	/** Width of the sheep pen */
	private static final int SHEEP_PEN_WIDTH = 9;
	/** Height of the sheep pen */
	private static final int SHEEP_PEN_HEIGHT = 5;
	/** 
	 * The maximum number of sheep Dobromir keeps in his sheep pen.
	 */ 
	private static final int MAX_SHEEP_IN_PEN = 4;
	/** The area covering the sheep pen in Zakopane */
	private Area pen;

	public class SheepBuyerSpeakerNPC extends SpeakerNPC {
		public SheepBuyerSpeakerNPC(String name) {
			super(name);
			// HP needs to be > 0 for Dobromir to appear in the killer list for
			// the wielki zły wilk
			setBaseHP(100);
			setHP(100);
		}
		/**
		 * Get the area of the sheep pen where bought sheep 
		 * should be moved.
		 * 
		 * @param zone the zone of the sheep pen
		 * @return area of the sheep pen
		 */
		private Area getPen(StendhalRPZone zone) {
			if (pen == null) {
				Rectangle2D rect = new Rectangle2D.Double();
				rect.setRect(SHEEP_PEN_X, SHEEP_PEN_Y, SHEEP_PEN_WIDTH, SHEEP_PEN_HEIGHT);
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
		 * Get a list of the sheep in the pen.
		 * 
		 * @param zone the zone to check
		 * @return list of sheep in the pen
		 */
		private List<Sheep> sheepInPen(StendhalRPZone zone) {
			List<Sheep> sheep = new LinkedList<Sheep>();
			Area pen = getPen(zone);
			
			for (RPEntity entity : zone.getPlayerAndFriends()) {
				if (entity instanceof Sheep) {
					if (pen.contains(entity)) {
						sheep.add((Sheep) entity);
					}
				}
			}
			
			return sheep;
		}
		
		/**
		 * Move a bought sheep to the den if there's space, or remove it 
		 * from the zone otherwise. Remove old sheep if there'd be more
		 * than <code>MAX_SHEEP_IN_PEN</code> after the addition.
		 * 
		 * @param sheep the sheep to be moved
		 */
		public void moveSheep(Sheep sheep) {
			// The area of the sheep den.
			int x = Rand.randUniform(SHEEP_PEN_X, SHEEP_PEN_X + SHEEP_PEN_WIDTH - 1);
			int y = Rand.randUniform(SHEEP_PEN_Y, SHEEP_PEN_Y + SHEEP_PEN_HEIGHT - 1);
			StendhalRPZone zone = sheep.getZone();
			List<Sheep> oldSheep = sheepInPen(zone);
			
			killWolves(zone);
			/*
			 * Keep the amount of sheep reasonable. Dobromir
			 * a business man and letting the sheep starve 
			 * would be bad for busines.
			 */
			if (oldSheep.size() >= MAX_SHEEP_IN_PEN) {
				// Sato sells the oldest sheep
				zone.remove(oldSheep.get(0));
			}
			
			if (!StendhalRPAction.placeat(zone, sheep, x, y)) {
				// there was no room for the sheep. Simply eat it
				sheep.getZone().remove(sheep);  
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
		final SpeakerNPC npc = new SheepBuyerSpeakerNPC("Dobromir") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(19, 92));
				nodes.add(new Node(25, 92));
				nodes.add(new Node(25, 84));
				nodes.add(new Node(13, 84));
				nodes.add(new Node(13, 92));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Skupuję owce w Zakopane oraz transportuje je do Semos.");
				addHelp("Skupuję owieczki i sądzę, że to uczciwa cena. Jeżeli się zdecydujesz na sprzedaż to powiedz #'sprzedam sheep', a dobijemy interes!");
				addOffer("Jeżeli przyniesiesz mi owcę, która została przez Ciebie wypasiona to zostaniesz odpowiednio wynagrodzony.");
				addGoodbye();
			}
		};

		final Map<String, Integer> buyitems = new HashMap<String, Integer>();
		buyitems.put("sheep", 300);
		new BuyerAdder().addBuyer(npc, new SheepBuyerBehaviour(buyitems), true);
		npc.setDescription("Oto Dobromir. Zajmuje się handlem owiec.");
		npc.setEntityClass("npc_dobromir");
		npc.setPosition(19, 92);
		npc.setSounds(Arrays.asList("hiccup-1", "sneeze-1"));
		zone.add(npc);
	}

	private static class SheepBuyerBehaviour extends BuyerBehaviour {
		SheepBuyerBehaviour(final Map<String, Integer> items) {
			super(items);
		}

		private int getValue(ItemParserResult res, final Sheep sheep) {
			return Math.round(getUnitPrice(res.getChosenItemName()) * ((float) sheep.getWeight() / (float) Sheep.MAX_WEIGHT));
		}

		@Override
		public int getCharge(ItemParserResult res, final Player player) {
			if (player.hasSheep()) {
				final Sheep sheep = player.getSheep();
				return getValue(res, sheep);
			} else {
				// npc's answer was moved to BuyerAdder. 
				return 0;
			}
		}

		@Override
		public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
			// res.getAmount() is currently ignored.

			final Sheep sheep = player.getSheep();

			if (sheep != null) {
				if (seller.getEntity().squaredDistance(sheep) > 5 * 5) {
					seller.say("Nie widzę stąd tej owcy! Przyprowadź ją bliżej, abym mógł sprawdzić.");
				} else if (getValue(res, sheep) < BacaZbyszekNPC.BUYING_PRICE) {
					// prevent newbies from selling their sheep too early
					seller.say("Ta owieczka wygląda na zbyt chudą. Nakarm ją trawą i wróć, gdy będzie grubsza.");
				} else {
					seller.say("Dziękuję! Oto twoje pieniądze.");
					payPlayer(res, player);
					player.removeSheep(sheep);

					player.notifyWorldAboutChanges();
					if(seller.getEntity() instanceof SheepBuyerSpeakerNPC) {
						((SheepBuyerSpeakerNPC)seller.getEntity()).moveSheep(sheep);
					} else {
						// only to prevent that an error occurs and the sheep does not disappear
						sheep.getZone().remove(sheep);
					}

					return true;
				}
			} else {
				seller.say(player.getTitle() + " nie posiadasz owieczki! W co próbujesz pogrywać?");
			}

			return false;
		}
	}
}
