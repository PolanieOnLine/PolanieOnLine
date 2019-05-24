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
package games.stendhal.server.maps.semos.house;


import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayNPCNamesForUnstartedQuestsAction;
import games.stendhal.server.entity.npc.action.SayUnstartedQuestDescriptionFromNPCNameAction;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.condition.AdminCondition;
import games.stendhal.server.entity.npc.condition.TriggerIsNPCNameForUnstartedQuestCondition;
import games.stendhal.server.maps.Region;

import java.util.Arrays;
import java.util.Map;

/**
 * A young lady (original name: Skye) who is lovely to admins.
 */
public class AdminHelpNPC implements ZoneConfigurator {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		SpeakerNPC npc = new SpeakerNPC("Skye") {

			@Override
			public void createDialog() {
				addGreeting("Cześć! Wyglądasz dziś dobrze. W sumie to wyglądasz bardzo dobrze każdego dnia!");
				addJob("Jestem tutaj, aby Cię uszczęśliwić. Możesz łatwo tu wrócić używając #/teleportto do mnie. Także mogę Ci wyjaśnić tutejsze #portale.");
				addHelp("Mogę Cię uleczyć jeśli chcesz lub powiedzieć Tobie #miłe #rzeczy. Jeżeli chcesz coś wiedzieć co to #portale to pytaj. Powiedz tylko #ulecz jeżeli chcesz wyzdrowieć.");
				addOffer("Mogę wysłać cię na  #plac zabaw do gry w..!");
				addReply(Arrays.asList("nice", "miłe"), "Czy wiesz ilu wojowników myśli, że jesteś wspaniały w pomaganiu? Mogę Ci powiedzieć, że dużo ludzi.");
				addReply(Arrays.asList("things", "rzeczy"), "Więc jesteś jednym z graczy, który sprawdza wszystkie #niebieskie #słowa, nieprawdaż?? Nic dziwnego masz swoje powody.");
				addReply(Arrays.asList("blue", "niebieskie"), "Ach, nie smuć się :( Włącz może jakąś miłą muzykę ... ");
				addReply(Arrays.asList("words", "słowa"), "Czerwone są róże, fiołki niebieskie, a PolskaGra jest super boskie!");
				addReply(Arrays.asList("portals", "portale"), "Jeden ze Słońcem prowadzi do Semos. Pokazuję Ci, gdzie jest ten dom. Mam nadzieję, że wszystko zrozumiałeś. Są drzwi do banku, więzienia i Death Matcha w Ados. Oczywiście są portalami w jedną stronę i dzięki temu nikt nie będzie Ci przeszkadzał.");
				addQuest("Teraz chcesz wystawić moją cierpliwość na próbę?");
				add(ConversationStates.ATTENDING,
						Arrays.asList("playground", "plac"),
						new AdminCondition(500),
						ConversationStates.IDLE,
						"Miłej zabawy!",
						new TeleportAction("int_admin_playground", 20, 20, Direction.DOWN));
			    add(ConversationStates.ATTENDING,
						"semos",
						null,
						ConversationStates.ATTENDING,
						null,
						new SayNPCNamesForUnstartedQuestsAction(Region.SEMOS_CITY));
			    add(ConversationStates.ATTENDING,
						"nalwor",
						null,
						ConversationStates.ATTENDING,
						null,
						new SayNPCNamesForUnstartedQuestsAction(Region.NALWOR_CITY));
			    add(ConversationStates.ATTENDING,
						"ados",
						null,
						ConversationStates.ATTENDING,
						null,
						new SayNPCNamesForUnstartedQuestsAction(Region.ADOS_CITY));
			    add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(Region.SEMOS_CITY),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(Region.SEMOS_CITY));
			    add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(Region.NALWOR_CITY),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(Region.NALWOR_CITY));
			    add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(Region.ADOS_CITY),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(Region.ADOS_CITY));
				addGoodbye("Dowidzenia, pamiętaj, aby dbać o siebie. Pij mleko.");
	}

			@Override
			protected void createPath() {
				// do not walk so that admins can
				// idle here 24/7 without using cpu and bandwith.
			}
			
		};
		new HealerAdder().addHealer(npc, 0);
		npc.setPosition(16, 7);
		npc.setDescription("Oto Skye. Ona wie wszystko. Administratorzy powinni ją znać po za tym zawsze ma dla nich uśmiech na twarzy :)");
		npc.setDirection(Direction.DOWN);
		npc.setEntityClass("beautifulgirlnpc");
		zone.add(npc);
	}
	
}