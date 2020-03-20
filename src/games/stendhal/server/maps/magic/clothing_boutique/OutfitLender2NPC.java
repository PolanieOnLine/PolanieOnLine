/***************************************************************************
 *                   (C) Copyright 2003-2015 - Stendhal                    *
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.constants.Occasion;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.OutfitChangerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.OutfitChangerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ShowOutfitListEvent;
import marauroa.common.Pair;

public class OutfitLender2NPC implements ZoneConfigurator {

	// outfits to last for 10 hours normally
	public static final int endurance = 10 * 60;

	// this constant is to vary the price. N=1 normally but could be a lot smaller on special occasions
	private static final double N = 1;

	private static HashMap<String, Pair<Outfit, Boolean>> outfitTypes = new HashMap<String, Pair<Outfit, Boolean>>();
	private final static Map<String, Integer> priceList = new HashMap<String, Integer>();

	// for the client to know which bases should not be hidden in the preview
	private final static Map<String, String> hideBaseOverrides = new HashMap<String, String>() {{
		put("piernikowy ludzik", "showbody");
		put("czarny kot", "showbody");
		put("biały kot", "showbody");
		put("zielony szlam", "showbody");
		put("czerwony szlam", "showbody");
		put("fioletowy szlam", "showbody");
		put("niebieski szlam", "showbody");
		put("twarz goblina", "showhead");
		put("twarz cosia", "showhead");
	}};

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		initOutfits();
		buildBoutiqueArea(zone);
	}

	private void initOutfits() {
		// these outfits must be put on over existing outfit
		// (what's null doesn't change that part of the outfit)
		// so true means we put on over
		final Pair<Outfit, Boolean> GOBLIN_FACE = new Pair<Outfit, Boolean>(new Outfit(null, null, 988, -1, -1, -1, null), true);
		final Pair<Outfit, Boolean> THING_FACE = new Pair<Outfit, Boolean>(new Outfit(null, null, 987, -1, -1, -1, null), true);
		final Pair<Outfit, Boolean> Umbrella = new Pair<Outfit, Boolean>(new Outfit(null, null, null, null, null, null, 7), true);

		// these outfits must replace the current outfit (what's null simply isn't there)
		final Pair<Outfit, Boolean> PURPLE_SLIME = new Pair<Outfit, Boolean>(new Outfit(93, -1, -1, -1, -1, -1, null), false);
		final Pair<Outfit, Boolean> GREEN_SLIME = new Pair<Outfit, Boolean>(new Outfit(89, -1, -1, -1, -1, -1, null), false);
		final Pair<Outfit, Boolean> RED_SLIME = new Pair<Outfit, Boolean>(new Outfit(88, -1, -1, -1, -1, -1, null), false);
		final Pair<Outfit, Boolean> BLUE_SLIME = new Pair<Outfit, Boolean>(new Outfit(91, -1, -1, -1, -1, -1, null), false);
		final Pair<Outfit, Boolean> GINGERBREAD_MAN = new Pair<Outfit, Boolean>(new Outfit(92, -1, -1, -1, -1, -1, null), false);
		final Pair<Outfit, Boolean> WHITE_CAT = new Pair<Outfit, Boolean>(new Outfit(78, -1, -1, -1, -1, -1, null), false);
		final Pair<Outfit, Boolean> BLACK_CAT = new Pair<Outfit, Boolean>(new Outfit(79, -1, -1, -1, -1, -1, null), false);

		outfitTypes.put("twarz goblina", GOBLIN_FACE);
		outfitTypes.put("twarz cosia", THING_FACE);
		outfitTypes.put("parasol", Umbrella);
		outfitTypes.put("fioletowy szlam", PURPLE_SLIME);
		outfitTypes.put("zielony szlam", GREEN_SLIME);
		outfitTypes.put("czerwony szlam", RED_SLIME);
		outfitTypes.put("niebieski szlam", BLUE_SLIME);
		outfitTypes.put("piernikowy ludzik", GINGERBREAD_MAN);
		outfitTypes.put("biały kot", WHITE_CAT);
		outfitTypes.put("czarny kot", BLACK_CAT);
	}

	private void buildBoutiqueArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Saskia") {
			@Override
			protected void createPath() {
			    final List<Node> nodes = new LinkedList<Node>();
			    nodes.add(new Node(5, 7));
			    nodes.add(new Node(5, 20));
			    nodes.add(new Node(9, 20));
			    nodes.add(new Node(9, 7));
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

						// remove temporary outfits to avoid visual conflicts
						player.returnToOriginalOutfit();

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
							if (player.getOutfit().getLayer("body") > 80
									&& player.getOutfit().getLayer("body") < 99) {
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
							seller.say("Nie masz wystarczająco dużo money!");
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

				priceList.put("twarz goblina", (int) (N * 500));
				priceList.put("twarz cosia", (int) (N * 500));
				priceList.put("fioletowy szlam", (int) (N * 3000));
				priceList.put("czerwony szlam", (int) (N * 3000));
				priceList.put("niebieski szlam", (int) (N * 3000));
				priceList.put("zielony szlam", (int) (N * 3000));
				priceList.put("piernikowy ludzik", (int) (N * 1200));
				priceList.put("parasol", (int) (N * 300));
				priceList.put("czarny kot", (int) (N * 4500));
				priceList.put("biały kot", (int) (N * 4500));

			    addGreeting("Witaj. Mam nadzieję, że podziwiasz ten wspaniały sklep.");
				addQuest("Wygląda wspaniale!");
				add(
					ConversationStates.ATTENDING,
					ConversationPhrases.OFFER_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"Powiedz mi, które chciałbyś ubranie."
					+ " Powiedz #'wypożycz twarz goblina',"
					+ " #'wypożycz twarz cosia', #'wypożycz parasol',"
					+ " #'wypożycz fioletowy szlam', #'wypożycz zielony szlam',"
					+ " #'wypożycz czerwony szlam', #'wypożycz niebieski szlam',"
					+ " #'wypożycz piernikowy ludzik',"
					+ " #'wypożycz biały kot' lub #'wypożycz czarny kot'.",
					createPreviewAction());

				addJob("Pracuję przy pomocy magii! Zapytaj o #ofertę.");
				addHelp("Mogę rzucić zaklęcie, aby ubrać Ciebie w magiczne ubranie. Zdejmuje się po pewnym czasie. Mam nadzieję, że mogę coś #zaoferować. Jeżeli nie to Liliana może wypożyczyć jakieś stroje.");
				addGoodbye("Dowidzenia!");
				final OutfitChangerBehaviour behaviour = new SpecialOutfitChangerBehaviour(priceList, endurance, "Twoje magiczne ubranie zostało zdjęte.");
				new OutfitChangerAdder().addOutfitChanger(this, behaviour, Arrays.asList("hire", "wypożyczę"), false, false);
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				if (Occasion.MINETOWN) {
					setDirection(Direction.DOWN);
				}
			}
		};

		npc.setEntityClass("wizardwomannpc");
		npc.initHP(100);
		npc.setDescription("Oto Saskia. Pracuje w butiku w magicznym mieście.");

		if (Occasion.MINETOWN) {
			npc.clearPath();
			npc.stop();
			npc.setDirection(Direction.DOWN);
			npc.setPosition(42, 9);
		} else {
			npc.setPosition(5, 7);
		}

		zone.add(npc);
	}

	private ChatAction createPreviewAction() {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final StringBuilder outfitString = new StringBuilder();
				final int outfitCount = outfitTypes.size();

				int idx = 0;
				for (final String outfitName: outfitTypes.keySet()) {
					outfitString.append(outfitName + ";" + outfitTypes.get(outfitName).first().toString() + ";" + priceList.get(outfitName));
					if (hideBaseOverrides.containsKey(outfitName)) {
						outfitString.append(";" + hideBaseOverrides.get(outfitName));
					} else {
						outfitString.append(";"); // avoid index out of range exception
					}

					if (idx < outfitCount - 1) {
						outfitString.append(":");
					}
					idx++;
				}

				player.addEvent(new ShowOutfitListEvent("Kostiumy", "Tutejsza wypożyczalnia kostiumów", outfitString.toString()));
				player.notifyWorldAboutChanges();
			}
		};
	}
}
