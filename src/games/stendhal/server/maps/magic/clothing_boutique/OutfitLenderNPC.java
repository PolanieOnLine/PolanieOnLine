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
package games.stendhal.server.maps.magic.clothing_boutique;

import games.stendhal.common.Direction;
import games.stendhal.common.constants.Occasion;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.behaviour.adder.OutfitChangerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.OutfitChangerBehaviour;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Pair;

public class OutfitLenderNPC implements ZoneConfigurator {

	// outfits to last for 10 hours normally
	public static final int endurance = 10 * 60;

	// this constant is to vary the price. N=1 normally but could be a lot smaller on special occasions
	private static final double N = 1;

	private static HashMap<String, Pair<Outfit, Boolean>> outfitTypes = new HashMap<String, Pair<Outfit, Boolean>>();

	private String jobReply;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		if (Occasion.MINETOWN) {
			jobReply = "Pracuję w butiku, które znajduje się w Magic City. To nie jest zwykły sklep. Używamy tutaj magii, aby ubrać naszych klientów w te fantastyczne kostiumy. Zapytaj o #'ofertę'.";
		} else {
			jobReply= "Pracuję w tym butiku z ubraniami. To nie jest zwykły sklep. Używamy tutaj magii, aby ubrać naszych klientów w te kostiumy. Zapytaj o #'ofertę'.";
		}
		initOutfits();
		buildBoutiqueArea(zone);
	}

	private void initOutfits() {
		// these outfits must be put on over existing outfit
		// (what's null doesn't change that part of the outfit)
		// so true means we put on over
		// FIXME: Use new outfit system
		final Pair<Outfit, Boolean> JUMPSUIT = new Pair<Outfit, Boolean>(new Outfit(null, null, null, Integer.valueOf(83), null), true);
		final Pair<Outfit, Boolean> DUNGAREES = new Pair<Outfit, Boolean>(new Outfit(null, null, null, Integer.valueOf(84), null), true);
		final Pair<Outfit, Boolean> GREEN_DRESS = new Pair<Outfit, Boolean>(new Outfit(null, null, null,	Integer.valueOf(78), null), true);

		final Pair<Outfit, Boolean> GOWN = new Pair<Outfit, Boolean>(new Outfit(null, null, null, Integer.valueOf(82), null), true);
		final Pair<Outfit, Boolean> NOOB = new Pair<Outfit, Boolean>(new Outfit(null, null, null, Integer.valueOf(80), null), true);
		final Pair<Outfit, Boolean> GLASSES = new Pair<Outfit, Boolean>(new Outfit(null, null, Integer.valueOf(99), null, null), true);
		final Pair<Outfit, Boolean> GLASSES_2 = new Pair<Outfit, Boolean>(new Outfit(null, null, Integer.valueOf(79), null, null), true);
		final Pair<Outfit, Boolean> HAT = new Pair<Outfit, Boolean>(new Outfit(null, Integer.valueOf(199), null, null, null), true);

		// these outfits must replace the current outfit (what's null simply isn't there)
		final Pair<Outfit, Boolean> BUNNY = new Pair<Outfit, Boolean>(new Outfit(null, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(81), Integer.valueOf(98)), false);
		final Pair<Outfit, Boolean> HORSE = new Pair<Outfit, Boolean>(new Outfit(0, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(00), Integer.valueOf(97)), false);
		final Pair<Outfit, Boolean> GIRL_HORSE = new Pair<Outfit, Boolean>(new Outfit(0, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(00), Integer.valueOf(96)), false);
		final Pair<Outfit, Boolean> ALIEN = new Pair<Outfit, Boolean>(new Outfit(null, Integer.valueOf(00), Integer.valueOf(98), Integer.valueOf(00), Integer.valueOf(95)), false);

		outfitTypes.put("jumpsuit", JUMPSUIT);
		outfitTypes.put("dungarees", DUNGAREES);
		outfitTypes.put("green dress", GREEN_DRESS);
		outfitTypes.put("gown", GOWN);
		outfitTypes.put("orange", NOOB);
		outfitTypes.put("bunny suit", BUNNY);
		outfitTypes.put("glasses", GLASSES);
		outfitTypes.put("other glasses", GLASSES_2);
		outfitTypes.put("hat", HAT);
		outfitTypes.put("horse", HORSE);
		outfitTypes.put("girl horse", GIRL_HORSE);
		outfitTypes.put("alien", ALIEN);
	}

	private void buildBoutiqueArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Liliana") {
			@Override
			protected void createPath() {
			    final List<Node> nodes = new LinkedList<Node>();
			    nodes.add(new Node(16, 5));
			    nodes.add(new Node(16, 16));
			    nodes.add(new Node(26, 16));
			    nodes.add(new Node(26, 5));
			    setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				class SpecialOutfitChangerBehaviour extends OutfitChangerBehaviour {
					SpecialOutfitChangerBehaviour(final Map<String, Integer> priceList, final int endurance, final String wearOffMessage) {
						super(priceList, endurance, wearOffMessage);
					}

					@Override
					public void putOnOutfit(final Player player, final String outfitType) {

						final Pair<Outfit, Boolean> outfitPair = outfitTypes.get(outfitType);
						final Outfit outfit = outfitPair.first();
						final boolean type = outfitPair.second();
						if (type) {
							player.setOutfit(outfit.putOver(player.getOutfit()), true);
						} else {
							player.setOutfit(outfit, true);
						}
						player.registerOutfitExpireTime(endurance);
					}
					// override transact agreed deal to only make the player rest to a normal outfit if they want a put on over type.
					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
						final String outfitType = res.getChosenItemName();
						final Pair<Outfit, Boolean> outfitPair = outfitTypes.get(outfitType);
						final boolean type = outfitPair.second();

						if (type) {
							if (player.getOutfit().getBody() > 77
									&& player.getOutfit().getBody() < 99) {
								seller.say("Już masz magiczne ubranie, które gryzie się z resztą - mógłbyś założyć coś bardziej konwencjonalnego i zapytać ponownie? Dziękuję!");
								return false;
							}
						}

						int charge = getCharge(res, player);

						if (player.isEquipped("money", charge)) {
							player.drop("money", charge);
							putOnOutfit(player, outfitType);
							return true;
						} else {
							seller.say("Przepraszam, ale nie masz wystarczająco dużo pieniędzy!");
							return false;
						}
					}

					// These outfits are not on the usual OutfitChangerBehaviour's
					// list, so they need special care when looking for them
					@Override
					public boolean wearsOutfitFromHere(final Player player) {
						final Outfit currentOutfit = player.getOutfit();

						for (final Pair<Outfit, Boolean> possiblePair : outfitTypes.values()) {
							if (possiblePair.first().isPartOf(currentOutfit)) {
								return true;
							}
						}
						return false;
					}
				}
				final Map<String, Integer> priceList = new HashMap<String, Integer>();
				priceList.put("jumpsuit", (int) (N * 500));
				priceList.put("dungarees", (int) (N * 500));
				priceList.put("green dress", (int) (N * 500));
				priceList.put("gown", (int) (N * 750));
				priceList.put("orange", (int) (N * 500));
				priceList.put("bunny suit", (int) (N * 800));
				priceList.put("glasses", (int) (N * 400));
				priceList.put("other glasses", (int) (N * 400));
				priceList.put("hat", (int) (N * 400));
				priceList.put("horse", (int) (N * 1200));
				priceList.put("girl horse", (int) (N * 1200));
				priceList.put("alien", (int) (N * 1200));
				addGreeting("Cześć! W czym mogę pomóc?");
				addQuest("Nic nie mogę dla Ciebie znaleść.");
				add(
					ConversationStates.ATTENDING,
					ConversationPhrases.OFFER_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"Powiedz mi jeżeli chciałbyś #'wypożyczyć gown', #'black dress', #'glasses', #'other glasses', #'hat', kostium #'alien', kostium #'horse', #'girl horse', #'jumpsuit', #'dungarees', #'suit bunny' lub kostium #'orange'.",
					new ExamineChatAction("outfits1.png", "Kostiumy", "Różna cena"));
				addJob(jobReply);
				// addJob("Normalnie pracuję w butiku. Używamy magii, aby ubierać naszych klientów w fantastyczne stroje. Jestem tutaj ze względu na Mine Town Revival Weeks, gdzie #oferujemy nasze stroje po korzystnych cenach!");
				addHelp("Nasze wynajęte kostiumy zdejmują się po pewnym czasie, ale zawsze możesz wrócić po następne!");
				addGoodbye("Dowidzenia!");
				final OutfitChangerBehaviour behaviour = new SpecialOutfitChangerBehaviour(priceList, endurance, "Twoje magiczne ubranie zostało już zdjęte.");
				new OutfitChangerAdder().addOutfitChanger(this, behaviour,  Arrays.asList("hire", "wypożycz"), false, false);
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				if (Occasion.MINETOWN) {
					setDirection(Direction.DOWN);
				}
			}
		};

		npc.setEntityClass("slim_woman_npc");
		npc.initHP(100);
		npc.setDescription("Widzisz Liliana. Pracuje w butiku Magic City.");

		if (Occasion.MINETOWN) {
			npc.clearPath();
			npc.stop();
			npc.setDirection(Direction.DOWN);
			npc.setPosition(53, 9);
		} else {
			npc.setPosition(16, 5);
		}

		zone.add(npc);
	}
}
