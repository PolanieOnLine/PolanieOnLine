/* $Id: CaptainNPC.java,v 1.23 2013/06/10 22:13:14 bluelads99 Exp $ */
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
package games.stendhal.server.maps.athor.ship;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.QuestCompletedSellerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.athor.ship.AthorFerry.Status;

import java.util.Arrays;
import java.util.Map;

/** Factory for the Scuba Diver on Athor Ferry. */

public class ScubaNPC implements ZoneConfigurator  {

	private Status ferrystate;
	private final ShopList shops = SingletonRepository.getShopList();

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Edward") {

			@Override
			public void createDialog() {
				addGoodbye("Żegnaj...");
				addHelp("Hm może chcesz wyruszyć po przygody?");
				addOffer("Licencjonowanym nurkom mogę sprzedać #zbroję #akwalungową.");
				new SellerAdder().addSeller(this, new QuestCompletedSellerBehaviour("get_diving_license", "Nie mogę sprzedać każdemu #zbroi #akwalungowej!", shops.get("sellScubaStuff")), false);
				addJob("Jestem pomocnikiem na tym statku.");
				
				//scuba gear phrases
				addReply(Arrays.asList("scuba gear", "zbroja akwalungowa", "zbroi akwalungowej", "zbroję akwalungową"),"Potrzebujesz zbroi akwalungowej, aby odkrywać piękny podwodny morski świat.");
				addReply(Arrays.asList("scuba", "akwalungowa", "akwalungowej", "akwalungową"),"Potrzebujesz zbroi akwalungowej, aby odkrywać piękny podwodny morski świat.");
				addReply(Arrays.asList("gear", "zboja", "zbroi", "zbroję"),"Potrzebujesz zbroi akwalungowej, aby odkrywać piękny podwodny morski świat.");
				//clue for the player.
				addReply(Arrays.asList("study", "studiowanie", "uczyć"),"Idź do biblioteki i poszukaj Podręcznik Nurkowania.");
				
				//quest phrases;
				addReply(Arrays.asList("license", "licencja", "licencji"),"Podwodne nurkowanie może być niebezpieczne nim dam ci zbroję akwalungową to musisz zdać #egzamin.");
				addReply("Mizuno","Czy znam to imię? Hmm... tak! Zastanaiwając się czasami widzimy człowieka o tym imieniu, który idzie przez #bagno podczas naszej przerwy na lądzie.");
				addReply(Arrays.asList("swamp", "bagno"),"Zanjduje się na północ od doków, ale strzeż się moczarów odkąd pojawili się tam #Blordroughty.");
				addReply(Arrays.asList("Blordrough", "Blordroughty"),"Demoniczny władca Blordroughtów prowadzi tam wojnę od kilku lat, aż do dnia, gdy trafili na koalicję drzewnych elfów i sił Deniran. Trzy armie walczyły zaciekle, ale w końcu demoniczny władca przypomocy jeziora wypłukał wszystko do morza.");
				add(ConversationStates.ATTENDING,
						"status",
						null,
						ConversationStates.ATTENDING,
						null,
						new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						npc.say(ferrystate.toString());
					}
				});

			}

			@Override
			protected void onGoodbye(final RPEntity player) {
				// Turn back to the sea.
				setDirection(Direction.LEFT);
			}	
		};

		new AthorFerry.FerryListener() {
			
			@Override
			public void onNewFerryState(final Status status) {
				ferrystate = status;
				switch (status) {
				case ANCHORED_AT_MAINLAND:
				case ANCHORED_AT_ISLAND:
					// capital letters symbolize shouting
					npc.say("RZUCIĆ CUMY!");
					break;

				default:
					npc.say("PODNIEŚ KOTWICĘ! ODPŁYWAMY!");
					break;
				}
				// Turn back to the wheel
				npc.setDirection(Direction.DOWN);

			}
		};
		
		npc.setPosition(17, 40);
		npc.setEntityClass("pirate_sailornpc");
		npc.setDescription ("Oto wytrawny żeglarz, ale zaabsorbowany czymś innym.");
		npc.setDirection(Direction.LEFT);
		zone.add(npc);	
	}
}
