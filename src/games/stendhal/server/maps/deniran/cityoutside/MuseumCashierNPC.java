/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityoutside;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;

/**
 * The cashier stands near the entrance to the museum. One has to talk to him and pay the fee to enter the museum. 
 * The idea is, that Iker needs more pocket money and so came up with the idea of turning the old empty building 
 * into the museum he heart of in a joke to take gullible tourists for a ride. So he is the manager but cleverly
 * denies being it to dodge responsibility.
 * 
 * The taking of the fee is in MuseumEntranceFee.java and the door condition in deniran.xml
 * 
 * An addition could be to make him attackable, and when a player does so, the father appears next to the player,
 * knocking the player out. The player would come back to consciousness outside of the city, with the HP halved 
 * and some money and items like food missing, not rings, armor, or weapons. 
 * 
 * Iker is a Basque name meaning visit, visitation
 * 
 * @author kribbel
 */
public class MuseumCashierNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMuseumCashierNPC(zone);
	}

	private void buildMuseumCashierNPC(final StendhalRPZone zone) {
		final SpeakerNPC museumcashier = new SpeakerNPC("Iker") {
	 		@Override
            protected void createDialog() {
	 			add(ConversationStates.IDLE,ConversationPhrases.GREETING_MESSAGES,ConversationStates.ATTENDING,null,
	 				new MultipleActions(
	 					new SayTextAction("!me pozdrawia cię w zbyt uprzejmym tonie."),
	 					new SayTextAction("Witam, mam nadzieję, że zainteresuje Cię #wizyta w słynnym Deniran Muzeum Powietrza i Przestrzeni.")
	 				)
	 			);
                addHelp("Najpierw zapłać opłatę za wstęp, a następnie możesz przejść przez drzwi do #odwiedzenia słynnego Deniran Muzeum Powietrza i Przestrzeni.");
                addJob("Jestem bardzo dumny z bycia kasjerem słynnego Deniran Muzeum Powietrza i Przestrzeni.");
                addOffer("Jeśli chcesz opłacić #wejście, mogę ci przyznać dostęp do słynnego Deniran Muzeum Powietrza i Przestrzeni.");
                add(ConversationStates.ATTENDING,
                	Arrays.asList("refund","fraud","hoax","scam","swindle","rip-of","swizz","screw","screwed","diddle","diddled",
                			"zwrot","oszustwo","żart","zdzierstwo","szwajc","wkręt","pijany","ocyganić","pomieszany"),
                	ConversationStates.IDLE,
                	null,
                	new MultipleActions(
                		new SayTextAction("Zasmuca nas, że nie jesteś zadowolony z naszych usług."),
                		new SayTextAction("!me ma poważny wyraz twarzy."),
                		new SayTextAction("Bez zwrotów!"),
                		new SayTextAction("!me znów ma bardzo przyjazny wyraz twarzy."),
                		new SayTextAction("Zapraszamy ponownie!")
                	)
                );
	 			add(ConversationStates.ATTENDING,
	 				Arrays.asList("parents","father","dad","mother","mom","rodzice","ojciec","tata","matka","mama"),
	 				ConversationStates.ATTENDING,
	 				"Rozmowa z rodzicami ci nie pomoże. Musisz porozmawiać z #dyrektorem.",
	 				null
		 		);
                addReply(Arrays.asList("manager", "menedżer"),"Bardzo przepraszam, menedżer jest obecnie niedostępny.");
                addReply(Arrays.asList("director", "dyrektor"),"Bardzo przepraszam, dyrektor jest obecnie niedostępny.");
                addReply(Arrays.asList("curator", "kurator"),"Bardzo przepraszam, kurator jest obecnie niedostępny.");
                addGoodbye("Proszę zaszczycić nas ponownie swoją wizytą.");
	 		}

	 		//Doesn't let the NPC face DOWN when it ends the conversation by itself by going IDLE but this would be preferred
            @Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}

            @Override
			public void onRejectedAttackStart(RPEntity attacker) {
            	say("!me krzyczy.");
				say("Tatko! Tatko! Pomóż! Zostałem zaatakowany przez " + attacker.getName() + "!");
			}
        };

        // I took this as a placeholder, it's the ghost in Ados. A boy clad in something more formal would be better.
        museumcashier.setEntityClass("kid7npc");
        museumcashier.setDescription("Oto Iker. To mądry i nastawiony na biznes młody człowiek.");
        museumcashier.setGender("M");
        museumcashier.setPosition(24, 47);
        museumcashier.setDirection(Direction.DOWN);

        zone.add(museumcashier);   
	}
}
