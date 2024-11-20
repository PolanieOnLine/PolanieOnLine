/***************************************************************************
 *                    Copyright © 2003-2024 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityinterior.potionsshop;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.mapstuff.sign.ShopSign;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.NPCEmoteAction;
import games.stendhal.server.entity.npc.action.PlaySoundAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.condition.PlayerNextToCondition;
import games.stendhal.server.entity.npc.shop.ShopType;
import games.stendhal.server.entity.npc.shop.ShopsList;
import games.stendhal.server.entity.player.Player;

public class PotionsDealerNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
		buildSigns(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Wanda");

		final List<Node> nodes = Arrays.asList(
				new Node(6, 5),
				new Node(9, 5)
		);

		final ShopsList shops = SingletonRepository.getShopsList();
		final Map<String, Integer> pricesBuy = shops.get("deniranpotionsbuy", ShopType.ITEM_BUY);
		final Map<String, Integer> pricesSell = shops.get("deniranpotionssell", ShopType.ITEM_SELL);

		npc.addGreeting("Witamy w sklepie z miksturami w mieście Deniran.");
		npc.addJob("Zarządzam tym sklepem z miksturami. Zapytaj mnie o moje #ceny.");
		npc.addHelp("Jeśli chcesz coś sprzedać, zapytaj mnie o moje #ceny, a powiem ci, co oferuję.");
		npc.addQuest("Nie mam dla ciebie nic do zrobienia. Lecz próbuję uzupełnić moje zapasy. Jeśli chcesz pomóc,"
				+ " po prostu zapytaj, a powiem ci o #cenach, które płacę za mikstury i trucizny.");

		npc.add(ConversationStates.ANY,
				Arrays.asList("price", "prices", "cena", "ceny", "cenach"),
				null,
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final int buyCount = pricesBuy.size();
						final int sellCount = pricesSell.size();

						final StringBuilder sb = new StringBuilder("Skupuję");
						int idx = 0;
						for (final String itemName: pricesBuy.keySet()) {
							if (buyCount > 1 && idx == buyCount - 1) {
								sb.append(" oraz");
							}
							sb.append(" " + itemName + " za " + Integer.toString(pricesBuy.get(itemName)));
							if (buyCount > 1 && idx < buyCount - 1) {
								sb.append(",");
							}
							idx++;
						}
						sb.append("\n\nSprzedaję również");
						idx = 0;
						for (final String itemName: pricesSell.keySet()) {
							if (sellCount > 1 && idx == sellCount - 1) {
								sb.append(" oraz");
							}
							sb.append(" " + itemName + " za " + Integer.toString(pricesSell.get(itemName)));
							if (sellCount > 1 && idx < sellCount - 1) {
								sb.append(",");
							}
							idx++;
						}

						raiser.say(sb.toString());
					}
				});

		npc.add(ConversationStates.ANY,
				ConversationPhrases.GOODBYE_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new MultipleActions(
						new PlaySoundAction("kiss-female-01"),
						new SayTextAction("Proszę przyjść ponownie.")
				)
		);

		npc.add(ConversationStates.IDLE,
				Arrays.asList("kiss", "buziak", "buziaczek", "pocałunek", "całus"),
				new PlayerNextToCondition(),
				ConversationStates.IDLE,
				null,
				new MultipleActions(
						new PlaySoundAction("kiss-female-01"),
						new NPCEmoteAction("buziaki", "w policzek")
				));

		npc.setPosition(nodes.get(0).getX(), nodes.get(0).getY());
		npc.setPath(new FixedPath(nodes, true));
		npc.setCollisionAction(CollisionAction.STOP);
		npc.setEntityClass("potionsdealernpc");
		npc.setGender("F");

		zone.add(npc);
	}

	private void buildSigns(final StendhalRPZone zone) {
		final ShopSign buys = new ShopSign("deniranpotionsbuy", "Sklepik Wandy (skupuje)", "Możesz sprzedać te rzeczy u Wandy.", false);
		buys.setEntityClass("book_blue");
		buys.setPosition(5, 6);

		final ShopSign sells = new ShopSign("deniranpotionssell", "Sklepik Wandy (sprzedaje)", "Możesz kupić te rzeczy od Wandy.", true);
		sells.setEntityClass("book_red");
		sells.setPosition(10, 6);

		zone.add(buys);
		zone.add(sells);
	}
}
