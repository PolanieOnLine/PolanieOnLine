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
package games.stendhal.server.maps.semos.townhall;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.MonologueBehaviour;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

public class LeaderNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String[] text = {"Super Trainer posłuchaj mnie. Twoje osiągnięcia są znakomite, ale rzadko polujesz na potwory. Brakuje Ci doświadczenia. Od współczynnika poziomu zależy jak mocno bijesz. Nie osiągnąłeś w pełni swojego potencjału.","XP Hunter uczyłem też Ciebie. Twoje przyzwyczajenie zawsze pozwala innym żołnierzom bronić się przed potworami. Oznacza to, że Twoje umiejętności nigdy się nie zwiększają. Tak, masz dobry poziom, ale Twoje umiejętności też od tego zależą!", "Cóż odpowiedni. Muszę powiedzieć, że masz dobry poziom i umiejętności. Oba są ważne, aby bić mocno potwory, a także możesz się sam bronić. Bardzo dobrze!"};
	  new MonologueBehaviour(buildSemosTownhallAreaLeader(zone), text, 1);
	}

	/**
	 * A leader of three cadets. He has an information giving role.
	 * @param zone zone to be configured with this npc
	 * @return the built NPC
	 */
	private SpeakerNPC buildSemosTownhallAreaLeader(final StendhalRPZone zone) {
		// We create an NPC
		final SpeakerNPC npc = new SpeakerNPC("Lieutenant Drilenun") {

			@Override
			protected void createPath() {
				// doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Och cześć. Rozmawialiśmy o przerwie. Moi trzej kadeci dostali właśnie nagrodę od Mayora za pomoc w obronie Semos.");
				addJob("Opiekuję się tymi trzema kadetami. Potrzebują wiele wskazówek. Muszę wracać. Posłuchaj ich może się czegoś nowego nauczysz!");
				addHelp("Mogę coś ci poradzić w kwestii broni ( #weapon ).");
				addQuest("Mogę ci doradzić w sprawie Twojej broni ( #weapon ).");
				addOffer("Mogę opowiedzieć o Twojej broni ( #weapon ) jeżeli mogę.");
				addGoodbye("Nie zapomnij posłuchać nauk dla moich trzech kadetów. Mogą się okazać pomocne!");
				add(ConversationStates.ATTENDING, "weapon", null, ConversationStates.ATTENDING,
				        null, new ChatAction() {

					        @Override
					        public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					        	final Item weapon = player.getWeapon();
					        	if (weapon != null) {
					        		String comment;
					        		// this is, loosely, the formula used for damage of a weapon (not taking level into account here)
					        		final float damage = (weapon.getAttack() + 1) / weapon.getAttackRate();
					        		if (weapon.getName().endsWith(" hand sword")) {
					        			// this is a special case, we deal with explicitly
					        			comment = "Widzę, że używasz dwóch mieczy. Zadają sporo strat, ale nie możesz wtedy nosić tarczy. Będzie ciężko z obroną, gdy ktoś cię zaatakuje.";
					        		} else if (damage >= 5) {
					        			comment = "Oto " + weapon.getName() + " to potężna broń. Jak na wagę to zadaje dobre straty.";
					        			if (weapon.getAttackRate() < 3) {
					        				comment += " Pomimo wagi jest użyteczna. Niski atak nie pomaga w starciu z silnymi potworami. Czasami cięższa będzie lepsza niż ta broń.";
					        			} else if (weapon.getAttackRate() > 6) {
					        				comment +=  " Powinna być użyteczna na silniejsze potwory. Pamiętaj, że coś słabego, a szybkiego może wystarczyć przeciwko potworom z niskim poziomem.";
					        			}
					        		} else {
					        			comment = "Nie sądzisz, że Twój " + weapon.getName() + " zadaje małe straty? Powinieneś poszukać czegoś z lepszym atakiem w stosunku do wagi.";
					        			if (weapon.getAttackRate() < 3) {
					        				comment += " W końcu możesz uderzać szybko. Może być dobre przeciwko słabszym potworom niż ty.";
					        			}
						       		}
					        		// simple damage doesn't take into account lifesteal. this is a decision the player must make, so inform them about the stats
					        		if (weapon.has("lifesteal")) {
					        			double lifesteal = weapon.getDouble("lifesteal");
					        			if (lifesteal > 0) {
					        				comment += " Pozytywne wysysanie życia " + lifesteal + " podnosi Twoje zdrowie, gdy go używasz.";
					        			} else {
					        				comment += " Negatywne wysysanie życia " + lifesteal + " odbije się na Twoim zdrowiu, gdy będziesz tego używał.";
					        			}
					        		}
					        		raiser.say(comment);
					        	} else {
					        		// player didn't have a weapon, as getWeapon returned null.
					        		raiser.say("Och, nie mogę nic opowiedzieć o Twojej broni o ile jakieś nie będziesz używał. To niezbyt mądre w dzisiejszych czasach!");
					        	}
							} 
					    }
				);
			}
		};
		npc.setLevel(150);
		npc.setDescription("Oto Lieutenant Drilenun, który rozmawia ze swoimi trzema kadetami.");
		npc.setEntityClass("bossmannpc");
		npc.setPosition(23, 15);
		npc.initHP(100);
		zone.add(npc);
		
		return npc;
	}
}
