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

public class OutfitLenderNPC implements ZoneConfigurator {
	// outfits to last for 10 hours normally
	public static final int endurance = 10 * 60;

	// this constant is to vary the price. N=1 normally but could be a lot smaller on special occasions
	private static final double N = 1;

	private final static HashMap<String, Pair<Outfit, Boolean>> outfitTypes = new HashMap<String, Pair<Outfit, Boolean>>();
	private final static Map<String, Integer> priceList = new HashMap<String, Integer>();

	// for the client to know which bases should not be hidden in the preview
	private final static List<String> hideBaseOverrides = Arrays.asList("koń", "klacz", "obcy", "statek", "duży statek");

	private SpeakerNPC npc;

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
		final Pair<Outfit, Boolean> JUMPSUIT = new Pair<Outfit, Boolean>(new Outfit(null, 68, null, null, null, null, null, null, null), true);
		final Pair<Outfit, Boolean> DUNGAREES = new Pair<Outfit, Boolean>(new Outfit(null, 69, null, null, null, null, null, null, null), true);
		final Pair<Outfit, Boolean> GREEN_DRESS = new Pair<Outfit, Boolean>(new Outfit(null, 63, null, null, null, null, null, null, null), true);

		final Pair<Outfit, Boolean> GOWN = new Pair<Outfit, Boolean>(new Outfit(null, 73, null, null, null, null, null, null, null), true);
		final Pair<Outfit, Boolean> NOOB = new Pair<Outfit, Boolean>(new Outfit(null, 65, null, null, null, null, null, null, null), true);
		final Pair<Outfit, Boolean> JESTER = new Pair<Outfit, Boolean>(new Outfit(null, 975, null, null, null, null, -1, 991, null), true);

		// these outfits must replace the current outfit (what's -1 simply isn't there)
		final Pair<Outfit, Boolean> BUNNY = new Pair<Outfit, Boolean>(new Outfit(998, 999, -1, -1, -1, -1, -1, -1, null), false);
		final Pair<Outfit, Boolean> HORSE = new Pair<Outfit, Boolean>(new Outfit(997, -1, -1, -1, -1, -1, -1, -1, null), false);
		final Pair<Outfit, Boolean> GIRL_HORSE = new Pair<Outfit, Boolean>(new Outfit(996, -1, -1, -1, -1, -1, -1, -1, null), false);
		final Pair<Outfit, Boolean> ALIEN = new Pair<Outfit, Boolean>(new Outfit(995, -1, -1, -1, -1, -1, -1, -1, null), false);
		final Pair<Outfit, Boolean> STATEK = new Pair<Outfit, Boolean>(new Outfit(950, -1, -1, -1, -1, -1, -1, -1, null), false);
		final Pair<Outfit, Boolean> DUZY_STATEK = new Pair<Outfit, Boolean>(new Outfit(951, -1, -1, -1, -1, -1, -1, null, null), false);

		outfitTypes.put("kombinezon", JUMPSUIT);
		outfitTypes.put("ogrodniczki", DUNGAREES);
		outfitTypes.put("zielone ubranie", GREEN_DRESS);
		outfitTypes.put("suknia", GOWN);
		outfitTypes.put("pomarańczowy", NOOB);
		outfitTypes.put("strój królika", BUNNY);
		outfitTypes.put("figlarz", JESTER);
		outfitTypes.put("koń", HORSE);
		outfitTypes.put("klacz", GIRL_HORSE);
		outfitTypes.put("obcy", ALIEN);
		outfitTypes.put("statek", STATEK);
		outfitTypes.put("duży statek", DUZY_STATEK);
	}

	private void buildBoutiqueArea(final StendhalRPZone zone) {
		npc = new SpeakerNPC("Liliana") {
			@Override
			protected void createPath() {
			    final List<Node> nodes = new LinkedList<Node>();
			    nodes.add(new Node(16, 5));
			    nodes.add(new Node(16, 16));
			    nodes.add(new Node(26, 16));
			    nodes.add(new Node(26, 5));
			    setPath(new FixedPath(nodes, true));
			}

			final List<String> skinLayerOutfits = Arrays.asList("koń", "klacz", "obcy", "statek", "duży statek");
			final List<String> dressLayerOutfits = Arrays.asList("pomarańczowy", "zielone ubranie", "ogrodniczki",
				"kombinezon", "strój królika", "suknia", "figlarz");

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
						// preserve color of detail layer
						final String detailColor = player.getOutfitColor("detail");

						// remove temporary outfits to avoid visual conflicts
						player.returnToOriginalOutfit();

						if (type) {
							player.setOutfit(outfit.putOver(player.getOutfit()), true);
						} else {
							player.setOutfit(outfit, true);
						}

						if (skinLayerOutfits.contains(outfitType)) {
							player.unsetOutfitColor("skin");
						}
						if (dressLayerOutfits.contains(outfitType)) {
							player.unsetOutfitColor("dress");
						}

						if (detailColor != null) {
							player.setOutfitColor("detail", detailColor);
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
							if (player.getOutfit().getLayer("body") == 50 && player.getOutfit().getLayer("body") == 51
								&& player.getOutfit().getLayer("body") > 80 && player.getOutfit().getLayer("body") < 99) {
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

				priceList.put("kombinezon", (int) (N * 500));
				priceList.put("ogrodniczki", (int) (N * 500));
				priceList.put("zielone ubranie", (int) (N * 500));
				priceList.put("suknia", (int) (N * 750));
				priceList.put("pomarańczowy", (int) (N * 500));
				priceList.put("strój królika", (int) (N * 800));
				priceList.put("figlarz", (int) (N * 400));
				priceList.put("koń", (int) (N * 1200));
				priceList.put("klacz", (int) (N * 1200));
				priceList.put("obcy", (int) (N * 1200));
				priceList.put("statek", (int) (N * 3500));
				priceList.put("duży statek", (int) (N * 5000));

				addGreeting("Cześć! W czym mogę pomóc?");
				addQuest("Nic nie mogę dla Ciebie znaleźć.");
				add(
					ConversationStates.ATTENDING,
					ConversationPhrases.OFFER_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"Powiedz mi jeżeli chciałbyś #'wypożyczyć suknia', #'zielone ubranie', kostium #'obcy', kostium #'koń', #'klacz', #'figlarz', #'kombinezon', #'ogrodniczki', #'strój królika', #'pomarańczowy', #'statek' lub kostium #'duży statek'.",
					createPreviewAction());

				addJob(jobReply);
				// addJob("Normalnie pracuję w butiku. Używamy magii, aby ubierać naszych klientów w fantastyczne stroje. Jestem tutaj ze względu na Mine Town Revival Weeks, gdzie #oferujemy nasze stroje po korzystnych cenach!");
				addHelp("Nasze wynajęte kostiumy zdejmują się po pewnym czasie, ale zawsze możesz wrócić po następne!");
				addGoodbye("Do widzenia!");
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

		npc.setDescription("Oto Liliana. Pracuje w butiku w magicznym mieście.");
		npc.setEntityClass("slim_woman_npc");
		npc.setGender("F");

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

	private ChatAction createPreviewAction() {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final StringBuilder outfitString = new StringBuilder();
				final int outfitCount = outfitTypes.size();

				int idx = 0;
				for (final String outfitName: outfitTypes.keySet()) {
					outfitString.append(outfitName + ";" + outfitTypes.get(outfitName).first().toString() + ";" + priceList.get(outfitName));
					if (hideBaseOverrides.contains(outfitName)) {
						outfitString.append(";showall");
					} else {
						outfitString.append(";"); // avoid index out of range exception
					}

					if (outfitName.equals("koń") || outfitName.equals("klacz")
							|| outfitName.equals("statek") || outfitName.equals("duży statek")) {
						// show side-facing frame for horses
						outfitString.append(";1;3");
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
