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
package games.stendhal.server.maps.semos.wizardstower;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;

import java.util.Arrays;
import java.util.Map;

/**
 * Zekiel, the guardian statue of the Wizards Tower (Zekiel in the spire)
 *
 * @see games.stendhal.server.maps.quests.ZekielsPracticalTestQuest
 * @see games.stendhal.server.maps.semos.wizardstower.WizardsGuardStatueNPC
 */
public class WizardsGuardStatueSpireNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildZekielSpire(zone);
	}

	private void buildZekielSpire(final StendhalRPZone zone) {
		final SpeakerNPC zekielspire = new SpeakerNPC("Zekiel") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Pozdrawiam ponownie wędrowcze!");
				addHelp("Znajdujesz się w #wieży. Na szczyt wieży możesz dostać się teleportem przede mną. Teleport znajdujący się za mną przeniesie cię z powrotem do wejścia");
				addJob("Jestem opiekunem tej #magicznej wieży i #sprzedawcą w #sklepie.");
				addGoodbye("Żegnaj!");
				addOffer("Mogę wykonać #specjalne itemy z materiałów, które posiadam w magazynie. Tylko powiedz mi co chcesz. Jednak do większości itemów będę potrzebował dodatkowych składników.");
				addReply(Arrays.asList("store", "storekeeper", "sklep", "sklepie", "sprzedawcą"),
				        "Mogę wykonać #specjalne itemy z materiałów, które posiadam w magazynie. Tylko powiedz mi co chcesz. Jednak do większości itemów będę potrzebował dodatkowych składników.");

				addReply(Arrays.asList("special", "specjalne"),
				        "Teraz produkuję #Ognisty #Miecz #Demonów i #Wzmocniona #Lwia #Tarcza. Mogę czytać w twojej głowie, ale nie mogę tego tutaj robić. Więć musisz mi powiedzieć, który specjalny przedmiot chcesz dostać, a ja ci powiem czy mogę ci pomóc.");
//				addReply(Arrays.asList("special", "specjalne"),
//				        "Przykro mi, teraz nie mam czasu. Spróbuj ponownie za kilka tygodni, może wtedy będę gotowy do pomocy.");

				addReply(Arrays.asList("wizard", "wizards", "wieży"),
				        "Siedmiu czarodzieji należy do magicznego kręgu. To jest #Erastus, #Elana, #Ravashack, #Jaer, #Cassandra, #Silvanus and #Malleus");
				addReply("erastus", "Erastus jest arcymagiem w tym kręgu. Jest arcymistrzem swego fachu, najmądrzejszym i najwspanialszym z całego grona. Jako jedyny nie uczestniczy w teście praktycznym.");
				addReply("elana", "Elana jest życzliwą i przyjazną wróżką. Jest opiekunką wszystkich żywych stworzeń na ziemi. Korzysta z magi żeby je ratować i leczyć.");
				addReply("ravashack", "Ravashack jest bardzo potężnym nekromanem. Studiował czarną magię od wieków. Ravashack używa czarnej magii do zdobycia przewagi nad rywalem w walce z lichami.");
				addReply("jaer", "Jaer jest mistrzem iluzji. Uroczy i kapryśny jak wietrzyk w upalne dni. Specjalizuje się w użyciu wiatru. Ma wielu sojuszników na równinach mistycznych duchów.");
				addReply("cassandra", "Cassandra jest piękną kobietą, ale przede wszystkim potężna czarodziejką. Domeną Cassandry jest woda i lód. Może być zimna jak lód by tylko osiągnąć swój cel.");
				addReply("silvanus", "Silvanus to stary druid i najprawdopodobniej najstarszy z wszystkich elfów. Jest przyjacielem zwierząt, drzew, wróżek oraz entów. Jego domeną jest Ziemia.");
				addReply("malleus", "Malleus to najpotężniejszy magik oraz przywódca destruktywnych magów. Jego domeną jest ogień. Żył z demonami aby zrozumieć ich ambicje.");
				
				//behavior for enhancing lion shield
				add(ConversationStates.ATTENDING,
						Arrays.asList("enhanced lion shield", "shields", "shield", "wzmocniona lwia tarcza", "tarcza"),
						ConversationStates.INFORMATION_1,
					    "Mogę zmienić tarczę płytową w wzmocnioną lwią tarczę przy użyciu żelaza, ale potrzebuję ośmiu kawałków żelaza i tarczy, aby to zrobić. Czy chcesz wzmocnioną lwią tarczę?",
					    null);
				add(ConversationStates.INFORMATION_1, 
						ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new NotCondition(new PlayerHasItemWithHimCondition("żelazo", 8)),
								new PlayerHasItemWithHimCondition("tarcza płytowa", 1)),
						ConversationStates.ATTENDING,
						"Nie masz wystarczającej ilości Żelaza. Potrzebuję 8 sztabek żelaza i tarczy płytowej.",
						null);
				add(ConversationStates.INFORMATION_1, 
						ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new NotCondition(new PlayerHasItemWithHimCondition("tarcza płytowa", 1)),
								new PlayerHasItemWithHimCondition("żelazo", 8)),
						ConversationStates.ATTENDING,
						"Nie masz tarczy dla mnie do wzmocnienia. Potrzebuję tarczy płytowej i 8 sztabek żelaza.",
						null);
				add(ConversationStates.INFORMATION_1, 
						ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new PlayerHasItemWithHimCondition("żelazo", 8),
								new PlayerHasItemWithHimCondition("tarcza płytowa", 1)),
						ConversationStates.ATTENDING,
						"Oto twoja wzmocniona lwia tarcza.",
						new MultipleActions(
							new DropItemAction("żelazo", 8),
							new DropItemAction("tarcza płytowa", 1),
							new EquipItemAction("wzmocniona lwia tarcza", 1, true),
							new IncreaseXPAction(250)));
					add(ConversationStates.INFORMATION_1, 
						ConversationPhrases.NO_MESSAGES, 
						null,
						ConversationStates.ATTENDING,
						"Dobrze. Powiedz mi kiedy będziesz chciał wzmocnioną lwią tarczę.", 
						null);
					
					//behavior for forging a demon fire sword
					add(ConversationStates.ATTENDING,
							Arrays.asList("demon fire sword", "swords", "sword", "ognisty miecz demonów", "miecz"),
							ConversationStates.INFORMATION_1,
						    "Mogę wykuć dla ciebie ognisty miecz demonów jeśli przyniesiesz miecz demonów i miecz ognisty.",
						    null);
					add(ConversationStates.INFORMATION_1, 
							ConversationPhrases.YES_MESSAGES,
							new AndCondition(
									new NotCondition(new PlayerHasItemWithHimCondition("miecz ognisty", 1)),
									new PlayerHasItemWithHimCondition("miecz demonów", 1)),
							ConversationStates.ATTENDING,
							"Nie masz miecza ognistego. Potrzebuję obu miecz demonów i miecza ognistego.",
							null);
					add(ConversationStates.INFORMATION_1, 
							ConversationPhrases.YES_MESSAGES,
							new AndCondition(
									new NotCondition(new PlayerHasItemWithHimCondition("miecz demonów", 1)),
									new PlayerHasItemWithHimCondition("miecz ognisty", 1)),
							ConversationStates.ATTENDING,
							"Nie masz miecza demonów. Potrzebuję obu miecza ognistego i miecz demonów.",
							null);
					add(ConversationStates.INFORMATION_1, 
							ConversationPhrases.YES_MESSAGES,
							new AndCondition(
									new PlayerHasItemWithHimCondition("miecz demonów", 1),
									new PlayerHasItemWithHimCondition("miecz ognisty", 1)),
							ConversationStates.ATTENDING,
							"Oto twój Ognisty Miecz Demonów.",
							new MultipleActions(
								new DropItemAction("miecz demonów", 1),
								new DropItemAction("miecz ognisty", 1),
								new EquipItemAction("ognisty miecz demonów", 1, true),
								new IncreaseXPAction(11250)));
						add(ConversationStates.INFORMATION_1, 
							ConversationPhrases.NO_MESSAGES, 
							null,
							ConversationStates.ATTENDING,
							"Dobrze. Powiedz mi kiedy będziesz chciał, abym wykuł dla ciebie ognisty miecz demonów.", 
							null);

/**				// behavior on special item BLANK SCROLL
				add(ConversationStates.ATTENDING,
				    Arrays.asList("blank scroll", "scrolls"),
				    ConversationStates.INFORMATION_1,
				    "I will create a blank scroll for you, but I need eight pieces of wood for that. The blank scroll can be enchanted by wizards. Do you want a blank scroll?",
				    null);
				add(ConversationStates.INFORMATION_1, 
					ConversationPhrases.YES_MESSAGES,
					new NotCondition(new PlayerHasItemWithHimCondition("wood", 8)),
					ConversationStates.ATTENDING,
					"You don't have enough wood, I will need eight pieces.", 
					null);
				add(ConversationStates.INFORMATION_1, 
					ConversationPhrases.YES_MESSAGES,
					new PlayerHasItemWithHimCondition("wood", 8),
					ConversationStates.ATTENDING,
					"There is your blank scroll.",
					new MultipleActions(
						new DropItemAction("wood", 8),
						new EquipItemAction("blank scroll", 1, true),
						new IncreaseXPAction(250)));
				add(ConversationStates.INFORMATION_1, 
					ConversationPhrases.NO_MESSAGES, 
					null,
					ConversationStates.ATTENDING,
					"Well, maybe later. Just tell me when you want a blank scroll.", 
					null);

				//behavior on special item RIFT CLOAK
				add(ConversationStates.ATTENDING,
				    Arrays.asList("rift cloak"),
				    ConversationStates.INFORMATION_2,
				    "I will create a rift cloak for you, but I have to fuse a carbuncle and a sapphire in the magic. The cloak is useless in battle and will protect you only one time, when entering a magical rift."+
					" The rift disintegrates the cloak instead of you. There is no way to get the cloak back. If you want to enter the rift again, you will need a new rift cloak. Shall I create one for you?",
				     null);
				add(ConversationStates.INFORMATION_2, 
					ConversationPhrases.YES_MESSAGES,
					new AndCondition(
							new NotCondition(new PlayerHasItemWithHimCondition("carbuncle", 1)),
							new PlayerHasItemWithHimCondition("sapphire", 1)),
					ConversationStates.ATTENDING,
					"You don't have a carbuncle, I will need a sapphire and a carbuncle.",
					null);
				add(ConversationStates.INFORMATION_2, 
					ConversationPhrases.YES_MESSAGES,
					new AndCondition(
							new NotCondition(new PlayerHasItemWithHimCondition("sapphire", 1)),
							new PlayerHasItemWithHimCondition("carbuncle", 1)),
					ConversationStates.ATTENDING,
					"You don't have a sapphire, I will need a carbuncle and a sapphire.",
					null);
				add(ConversationStates.INFORMATION_2, ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new PlayerHasItemWithHimCondition("sapphire", 1),
								new PlayerHasItemWithHimCondition("carbuncle", 1)),
					ConversationStates.ATTENDING,
					"There is your rift cloak. Don't forget that it protects you only one time, before it is destroyed. So be sure that you are ready for what awaits you in the rift.",
					new MultipleActions(
							new DropItemAction("carbuncle", 1),
							new DropItemAction("sapphire", 1),
							new EquipItemAction("rift cloak", 1, true),
							new IncreaseXPAction(5000)));
				add(ConversationStates.INFORMATION_2, 
					ConversationPhrases.NO_MESSAGES, 
					null,
					ConversationStates.ATTENDING,
					"Don't forget that you can't enter a magical rift without a rift cloak.",
					null);
*/

				//behavior on special item XARUHWAIYZ PHIAL
			} //remaining behavior defined in maps.quests.ZekielsPracticalTestQuest
		};

		zekielspire.setDescription("Oto Zekiel, opiekun magicznej wieży.");
		zekielspire.setEntityClass("transparentnpc");
		zekielspire.setAlternativeImage("zekiel");
		zekielspire.setPosition(15, 15);
		zekielspire.initHP(100);
		zone.add(zekielspire);
	}
}
