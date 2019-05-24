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
package games.stendhal.server.maps.kirdneh.inn;

import games.stendhal.common.Direction;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ListProducedItemDetailAction;
import games.stendhal.server.entity.npc.action.ListProducedItemsOfClassAction;
import games.stendhal.server.entity.npc.behaviour.impl.HealerBehaviour;
import games.stendhal.server.entity.npc.condition.TriggerIsProducedItemOfClassCondition;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds a Healer NPC for kirdneh. 
 * She likes a drink
 *
 * @author kymara
 */
public class HealerNPC implements ZoneConfigurator {

	/**
	 * Behaviour parse result in the current conversation.
	 * Remark: There is only one conversation between a player and the NPC at any time.
	 */
	private ItemParserResult currentBehavRes;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Katerina") {

			@Override
			protected void createPath() {
			    // sits still on stool
				setPath(null);
			}

			@Override
			protected void createDialog() {
			    addGreeting("Hik #całusik!");
                addReply("picie", null, new ListProducedItemsOfClassAction("drink","Lubię [#items]. *hic*"));  
				add(
					ConversationStates.ATTENDING,
					"",
					new TriggerIsProducedItemOfClassCondition("drink"),
					ConversationStates.ATTENDING,
					null,
					new ListProducedItemDetailAction()				
				);
                addReply(Arrays.asList("kiss", "całusik"), "łee kiepski");
                addReply(":*", "*:");
				addJob("Co? Echh. Leczenie. Tak. To wszystko.");
				addHealer(this, 1200);
				addHelp("Daj mi pieniądze na #picie, a uleczę. Hik kaska.");
				addQuest("Ba.");
				addGoodbye("pffff dowidzenia");
			}
		};

		npc.setDescription("Widzisz kobietę, która niegdyś uważana byłą za piękną, lecz teraz podupadła nieco, o czym świadczy jej strój...");
		npc.setEntityClass("womanonstoolnpc");
		npc.setPosition(25, 9);
		npc.setDirection(Direction.UP);
		npc.initHP(100);
		zone.add(npc);
	}

    // Don't want to use standard responses for Heal, in fact what to modify them all, so just configure it all here.
    private void addHealer(final SpeakerNPC npc, final int cost) {
	    final HealerBehaviour healerBehaviour = new HealerBehaviour(cost);
		final Engine engine = npc.getEngine();

		engine.add(ConversationStates.ATTENDING, 
				ConversationPhrases.OFFER_MESSAGES, 
				null, 
				false, 
				ConversationStates.ATTENDING, 
				"Daj mi pieniądze na picie, a uleczę. Hik kasa.", null);

		engine.add(ConversationStates.ATTENDING, 
				Arrays.asList("heal", "ulecz"), 
				null, 
				false, 
				ConversationStates.HEAL_OFFERED, 
		        null, new ChatAction() {
			        @Override
			        public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
			        	currentBehavRes = new ItemParserResult(true, "heal", 1, null);
                        String badboymsg = "";
			        	int cost = healerBehaviour.getCharge(currentBehavRes, player);
			        	if (player.isBadBoy()) {
			        		cost = cost * 2;
			        		badboymsg = " Dla takich jak ty jest drożej.";
			        		currentBehavRes.setAmount(2);
			        	}
			        	
						if (cost != 0) {
	                    	raiser.say("To kosztuje " + cost + " money, ok?" + badboymsg);
	                    }
			        }
		        });

		engine.add(ConversationStates.HEAL_OFFERED, 
				ConversationPhrases.YES_MESSAGES, 
				null,
		        false, 
		        ConversationStates.IDLE,
		        null, new ChatAction() {
			        @Override
			        public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				        if (player.drop("money", healerBehaviour.getCharge(currentBehavRes, player))) {
					        healerBehaviour.heal(player);
					        raiser.say("Lepiej teraz? Każdemu lepiej. Kocham Cię.");
				        } else {
					        raiser.say("Pff, nie ma pieniędzy, nie ma leczenia.");
				        }

						currentBehavRes = null;
			        }
		        });

		engine.add(ConversationStates.HEAL_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
		        false, 
		        ConversationStates.ATTENDING, 
		        "Czego chcesz?", null);
	}

}
