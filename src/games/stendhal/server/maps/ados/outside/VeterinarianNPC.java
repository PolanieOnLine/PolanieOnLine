/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.outside;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.ItemTools;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.creature.DomesticAnimal;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.shop.ShopType;
import games.stendhal.server.entity.npc.shop.ShopsList;
import games.stendhal.server.entity.player.Player;

public class VeterinarianNPC implements ZoneConfigurator {
	private final ShopsList shops = SingletonRepository.getShopsList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildZooArea(zone);
	}

	private void buildZooArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Dr. Feelgood") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(53, 28));
				nodes.add(new Node(53, 40));
				nodes.add(new Node(62, 40));
				nodes.add(new Node(62, 32));
				nodes.add(new Node(63, 32));
				nodes.add(new Node(63, 40));
				nodes.add(new Node(51, 40));
				nodes.add(new Node(51, 28));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				//Behaviours.addHelp(this,
				//				   "...");
				add(ConversationStates.ATTENDING, Arrays.asList("heal", "ulecz"), null, ConversationStates.ATTENDING, null, new HealPetsAction());
				addJob("Jestem weterynarzem.");

				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("healing",
						ShopType.ITEM_SELL)) {
					@Override
					public int getUnitPrice(final String item) {
						// Player gets 20 % rebate
						return (int) (0.8f * priceCalculator.calculatePrice(item, null));
					}
				});

				addGoodbye("Pa pa!");
			}
			// remaining behaviour is defined in maps.quests.ZooFood.
		};

		npc.setDescription("Oto Dr. Feelgood. Jest ekspertem w swoim zawodzie.");
		npc.setEntityClass("doctornpc");
		npc.setGender("M");
		npc.setPosition(53, 28);
		//npc.setDirection(Direction.DOWN);
		npc.setCollisionAction(CollisionAction.STOP);
		zone.add(npc);
	}
	
	/**
	 * Action for healing pets
	 */
	private static class HealPetsAction implements ChatAction {
		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			List<DomesticAnimal> healed = new LinkedList<DomesticAnimal>();

			for (DomesticAnimal pet : player.getAnimals()) {
				if (pet.heal() > 0) {
					healed.add(pet);
				}
			}

			/*
			 * Feelgood is only concerned about the animals if there's some that
			 * needs healing, and won't suggest trading in that case.
			 */
			int numHealed = healed.size(); 
			if (numHealed > 0) {
				StringBuilder msg = new StringBuilder("Twój ");
				// if we ever get the ability to have more than 2 pets this
				// needs to be changed.
				msg.append(getPetName(healed.get(0)));
				if (numHealed > 1) {
					msg.append(" i ");
					msg.append(getPetName(healed.get(1)));
				}
				msg.append(" ");
				msg.append(Grammar.isare(numHealed));
				msg.append(" uzdrowiony. Bardziej troszcz się o ");
				msg.append(Grammar.itthem(numHealed));
				msg.append(" w przyszłości.");

				npc.say(msg.toString());
			} else {
				npc.say("Przykro mi, ale jestem tylko licencjonowanym uzdrowicielem zwierząt. (Ale... ciii! Mogę złożyć Tobie #'ofertę'.)");
			}
		}

		/**
		 * Get a generic name for a pet that Feelgood can use. He does not
		 * know what the player calls the pet so he calls cats cats etc.
		 *
		 * @param pet the animal whose name is wanted
		 * @return printable name of the animal type
		 */
		private String getPetName(DomesticAnimal pet) {
			String type = pet.get("type");
			return ItemTools.itemNameToDisplayName(type);
		}
	}
}
