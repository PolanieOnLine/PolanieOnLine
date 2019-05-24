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
package games.stendhal.server.maps.quests.revivalweeks;

import java.util.Arrays;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.maps.ados.market.FishermansDaughterNPC;

/**
 * Caroline during the Mine Town Revival Weeks
 */
public class FishermansDaughterSellingNPC implements LoadableContent {
	private final ShopList shops = SingletonRepository.getShopList();
	private void createFishermansDaughterSellingNPC() {
		final StendhalRPZone zone2 = SingletonRepository.getRPWorld().getZone("0_semos_mountain_n2");
		final SpeakerNPC npc2 = new SpeakerNPC("Caroline") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć miło cię widzieć! Witaj w moim małym kramie w Mine Town!");
				addJob("Zostałam poproszona o popracowanie tutaj i sprzedawanie pysznych przekąsek i napojów podczas tygodnii Mine Town Revival. To wspaniała szansa na zdobycie umiejętności w gotowaniu w branży, w której teraz pracuję!");
				addHelp("Powinieneś zobaczyć i pograć w gry w Mine Town :) Mam nadzieję, że sprawdziłeś mój ulubiony #kolorowy #wygląd. Jest doskonały na Halloween i na kostiumy własnego pomysłu :)");
				addReply(Arrays.asList("outfit", "colouring", "outfit colouring", "kolorowy", "wygląd", "kolorowy wygląd"),
		        "Nowość przyszła w samą porę na Semos Mine Town Revival weeks i Halloween, Możesz pokolorować swój wygląd naciskając prawy przycisk i wybierając Ustaw Wygląd wybrać swój ulubiony kolor dla włosów i ubrania! To jest niesamowite!");
				addReply("susi", "Oh jest kochanym dzieckiem! Spotkałam ją tutaj. Jest taka szczęśliwa, że może świętować ze wszystkimi!");
				addOffer("Sprzedaję przekąski i napoje podczas Mine Town Weeks. Zobacz na listę.");
				addQuest("Słyszałam, że #Susi lubi poznawać nowych przyjaciół. Jest w domku. Lub możesz zapytać Fidorea o małe bieganie.");
				addGoodbye("Dowidzenia mam nadzieje, że będziesz się dobrze bawił!");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellrevivalweeks")), false);
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc2.setPosition(62, 106);
		npc2.setEntityClass("fishermansdaughternpc");
		npc2.setDirection(Direction.DOWN);
		npc2.initHP(100);
		zone2.add(npc2);
	}


	/**
	 * removes an NPC from the world and NPC list
	 *
	 * @param name name of NPC
	 */
	private void removeNPC(String name) {
		SpeakerNPC npc = NPCList.get().get(name);
		if (npc == null) {
			return;
		}
		npc.getZone().remove(npc);
	}

	@Override
	public void addToWorld() {
		removeNPC("Caroline");
		createFishermansDaughterSellingNPC();
	}


	/**
	 * removes Caroline from the Mine Town and places her back into her home in Ados.
	 *
	 * @return <code>true</code>, if the content was removed, <code>false</code> otherwise
	 */
	@Override
	public boolean removeFromWorld() {
		removeNPC("Caroline");

		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_ados_carolines_house_0");
		new FishermansDaughterNPC().createFishermansDaughterSellingNPC(zone);

		return true;
	}
}
