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
package games.stendhal.server.maps.semos.plains;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SeedSellerBehaviour;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * The miller (original name: Jenny). She mills flour for players who bring
 * grain. 
 */
public class MillerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}
	
	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Jenny") {
			@Override
			public void createDialog() {
				addJob("Prowadzę młyn, w którym mielę #kłosy na mąkę. Zaopatruję też piekarnię w Semos. Powiedz tylko #zmiel.");
				addReply("kłosy",
				        "Niedaleko jest farma. Zazwyczaj pozwalają ludziom tam zbierać kłosy zboża. Oczywiście potrzebujesz #kosy do ich ścięcia.");
				addReply("kosy",
					    "Kuźnia jest miejscem gdzie możesz ją dostać"); 
				addHelp("Znasz piekarnię w Semos? Jestem dumna, że używają mojej mąki, ale wilki znowu zjadły mojego dostawcę... albo może uciekł... hm.");
				addGoodbye();
				addOffer("Możesz #zasadzić moje nasiona, aby wyrosły z nich piękne kwiatki.");
				addReply(Arrays.asList("plant", "zasadzić"),"Twoje nasiona powinny zostać zasiane na żyznym gruncie. Szukaj brązowej ziemi nie daleko ścieżki koło której rośnie arandula na równinach semos. Nasiona będą tam kwitnąć. Możesz codziennie doglądać jak rośnie twój kwiatek. Gdy urośnie to będziesz mógł go zerwać. Obszar jest dostępny dla każdego i istniej prawdopodobieństwo, że ktoś inny zerwie twój kwiatek, ale na szczęście nasiona są tanie!");
			}

			/*
			 * (non-Javadoc)
			 * @see games.stendhal.server.entity.npc.SpeakerNPC#onGoodbye(games.stendhal.server.entity.RPEntity)
			 */
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
			
		};
		// Jenny mills flour if you bring her grain.
		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("zboże", 5);

		final ProducerBehaviour behaviour = new ProducerBehaviour("jenny_mill_flour",
				 Arrays.asList("mill", "zmiel"), "mąka", requiredResources, 2 * 60);
		new SellerAdder().addSeller(npc, new SeedSellerBehaviour());
		new ProducerAdder().addProducer(npc, behaviour,"Pozdrawiam! Nazywam się Jenny jestem szefową tutejszego młyna. Jeżeli przyniesiesz mi #kłosy zboża to zmielę je dla Ciebie na mąkę. Powiedz tylko #zmiel ilość #mąka.");
		npc.setPosition(19, 39);
		npc.setDescription("Oto Jenny. Pracuje w młynie.");	
		npc.setDirection(Direction.DOWN);
		npc.setEntityClass("woman_003_npc");
		zone.add(npc);
	}

}