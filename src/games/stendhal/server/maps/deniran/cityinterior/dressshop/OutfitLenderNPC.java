/***************************************************************************
 *                 Copyright © 2020-2024 - Faiumoni e. V.                  *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityinterior.dressshop;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
import games.stendhal.common.constants.SkinColor;
import games.stendhal.common.constants.SoundID;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.sign.ShopSign;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.OutfitChangerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.OutfitChangerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ShowOutfitListEvent;
import games.stendhal.server.events.SoundEvent;
import games.stendhal.server.util.TimeUtil;

public class OutfitLenderNPC implements ZoneConfigurator {
	private static final Logger logger = Logger.getLogger(OutfitLenderNPC.class);

	private SpeakerNPC lender;
	// how long player can wear outfit (10 hours)
	private static final int endurance = 10 * TimeUtil.MINUTES_IN_HOUR;

	private enum OutfitType {
		// set hair to -1 to not be drawn
		BEAR_BLUE("hat=993,hair=-1"),
		BEAR_BROWN("hat=994,hair=-1"),
		SUPERSTENDHAL("dress=973,hat=992,hair=-1");

		String outfit_str;

		OutfitType(final String outfit) {
			outfit_str = outfit;
		}

		@Override
		public String toString() {
			return outfit_str;
		}
	};

	private class DeniranOutfit {
		private final String label;
		private final OutfitType outfitType;
		private final int price;

		public DeniranOutfit(final String label, final OutfitType outfitType, final int price) {
			this.label = label;
			this.outfitType = outfitType;
			this.price = price;
		}

		public String getLabel() {
			return label;
		}

		public int getPrice() {
			return price;
		}

		public Outfit getOutfit() {
			return new Outfit(outfitType.toString());
		}

		public String getOutfitString() {
			return outfitType.toString();
		}
	}

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		initNPC(zone);
		initShop(zone);
	}

	private void initNPC(final StendhalRPZone zone) {
		lender = new SpeakerNPC("Pierre");
		lender.setOutfit("body=2,head=3,eyes=9,dress=7,hat=21");
		lender.setOutfitColor("skin", SkinColor.LIGHT);
		lender.setOutfitColor("dress", 0x008080); // teal
		lender.setOutfitColor("hat", Color.BLUE);
		lender.setDescription("Oto " + lender.getName() + ", bardzo modny, młody człowiek.");
		lender.setGender("M");

		lender.addGreeting();
		lender.addGoodbye();
		final String helpReply = "Zapoznaj się z naszym katalogiem na biurku, aby sprawdzić stroje, które możesz #wypożyczyć.";
		lender.addHelp(helpReply);
		lender.addOffer(helpReply);
		lender.addJob("Prowadzę sklep z odzieżą Deniran. Daj mi znać, jeśli chcesz #wypożyczyć strój.");

		final List<Node> nodes = new LinkedList<Node>() {{
			add(new Node(9, 2));
			add(new Node(12, 2));
			add(new Node(12, 15));
			add(new Node(26, 15));
			add(new Node(26, 6));
			add(new Node(21, 6));
		}};

		lender.setPathAndPosition(new FixedPath(nodes, true));
		lender.setRetracePath();
		lender.addSuspend(15, Direction.DOWN, 0);

		zone.add(lender);
	}

	private void initShop(final StendhalRPZone zone) {
		final List<DeniranOutfit> outfitList = new LinkedList<DeniranOutfit>() {{
			add(new DeniranOutfit("niebieski niedźwiadek", OutfitType.BEAR_BLUE, 2500));
			add(new DeniranOutfit("brązowy niedźwiadek", OutfitType.BEAR_BROWN, 2500));
			add(new DeniranOutfit("super", OutfitType.SUPERSTENDHAL, 5000));
		}};

		final Map<String, Integer> prices = new LinkedHashMap<>();
		for (final DeniranOutfit outfit: outfitList) {
			prices.put(outfit.getLabel(), outfit.getPrice());
		}

		final OutfitChangerBehaviour behaviour = new OutfitChangerBehaviour(prices, endurance, "Twój strój się skończył.", true) {
			@Override
			public boolean transactAgreedDeal(final ItemParserResult res, final EventRaiser seller, final Player player) {
				final String outfitName = res.getChosenItemName();
				final int price = getCharge(res, player);

				if (!player.isEquipped("money", price)) {
					seller.say("Niestety, ale nie masz wystarczającej ilości pieniędzy!");
					return false;
				}

				DeniranOutfit selected = null;
				for (final DeniranOutfit current: outfitList) {
					if (current.getLabel().equals(outfitName)) {
						selected = current;
						break;
					}
				}

				if (selected == null) {
					logger.error("Could not determine outfit");
					return false;
				}

				player.drop("money", price);
				seller.addEvent(new SoundEvent(SoundID.COMMERCE, SoundLayer.CREATURE_NOISE));
				// remember purchases
				updatePlayerTransactions(player, seller.getName(), res);

				// preserve detail layer coloring
				final String detailColor = player.getOutfitColor("detail");
				if (flagIsSet("resetBeforeChange")) {
					// remove temp outfit before changing
					player.returnToOriginalOutfit();
				}
				player.setOutfit(selected.getOutfit().putOver(player.getOutfit()), true);
				if (detailColor != null) {
					player.setOutfitColor("detail", detailColor);
				}
				player.registerOutfitExpireTime(endurance);

				return true;
			}

			@Override
			public boolean wearsOutfitFromHere(final Player player) {
				final Outfit currentOutfit = player.getOutfit();

				for (final DeniranOutfit possibleOutfit: outfitList) {
					if (possibleOutfit.getOutfit().isPartOf(currentOutfit)) {
						return true;
					}
				}

				return false;
			}
		};

		new OutfitChangerAdder() {
			@Override
			protected String getReturnPhrase() {
				return "Możesz nosić przez " +  TimeUtil.timeUntil(60 * endurance)
				+ ". Jeśli będziesz chciał możesz #zwrócić, zanim skończy się czas.";
			};
		}.addOutfitChanger(lender, behaviour, Arrays.asList("rent", "wypożycz", "wypożyczyć"), false, true);


		// a catalog for players to browse
		final ShopSign catalog = new ShopSign(null, null, null, true) {
			@Override
			public boolean onUsed(final RPEntity user) {
				if (user instanceof Player) {
					final Player player = (Player) user;

					final StringBuilder toSend = new StringBuilder();
					final int outfitCount = outfitList.size();

					for (int idx = 0; idx < outfitCount; idx++) {
						final DeniranOutfit of = outfitList.get(idx);
						toSend.append(of.getLabel() + ";" + of.getOutfitString() + ";" + of.getPrice());
						if (idx < outfitCount - 1) {
							toSend.append(":");
						}
					}

					player.addEvent(new ShowOutfitListEvent("Sklep Odzieżowy Deniran", lender.getName() + " wypożycza następujące stroje", toSend.toString()));
					player.notifyWorldAboutChanges();

					return true;
				}

				return false;
			}
		};
		catalog.setEntityClass("book_turquoise");
		catalog.setPosition(9, 4);

		zone.add(catalog);
	}
}
