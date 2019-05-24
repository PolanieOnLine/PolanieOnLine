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
package games.stendhal.server.maps.semos.bakery;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A woman who bakes bread for players.
 *
 * Erna will lend tools.
 *
 * @author daniel / kymara
 */
public class ShopAssistantNPC implements ZoneConfigurator  {

	private static final int COST = 3000;
	private static final String QUEST_SLOT = "borrow_kitchen_equipment";

	private static final List<String> ITEMS = Arrays.asList("młynek do cukru", "moździerz z tłuczkiem");


	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Erna") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(26,9));
                nodes.add(new Node(26,6));
                nodes.add(new Node(28,6));
                nodes.add(new Node(28,2));
                nodes.add(new Node(28,5));
                nodes.add(new Node(22,5));
                nodes.add(new Node(22,4));
                nodes.add(new Node(22,7));
                nodes.add(new Node(26,7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("Jam jest czeladnikiem w tej piekarni.");
				addReply("mąkę",
				"Do naszej pracy potrzebujemy #mąki, którą mielą we młynie na północ stąd, ale wilki pożarły chłopca, który nam ją przynosił! Jeśli przyniesiesz nam mąkę w nagrodę upieczemy przepyszny chleb dla Ciebie. Powiedz tylko #upiecz.");
				addHelp("Chleb jest bardzo dobry, zwłaszcza dla takiego śmiałka jak ty, któremu już niedobrze, gdy widzi surowe mięsiwo. Mój szef Leander, robi najlepsze kanapki w okolicy!");
				addGoodbye();

		// Erna bakes bread if you bring her flour.
		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("mąka", 2);

		final ProducerBehaviour behaviour = new ProducerBehaviour("erna_bake_bread",
				Arrays.asList("bake", "upiecz"), "chleb", requiredResources, 10 * 60);

				new ProducerAdder().addProducer(this, behaviour,
		        "Witaj w piekarni w Semos! Możemy upiec pyszny chleb dla każdego kto pomoże nam, przynosząc #mąkę z młyna. Powiedz tylko #'upiecz'.");

				addOffer("Nasi dostawcy pizzy mogą #pożyczyć narzędzia kuchenne ode mnie.");

				add(ConversationStates.ATTENDING, Arrays.asList("borrow", "pożycz", "pożyczyć"),
				    new LevelLessThanCondition(6),
				    ConversationStates.ATTENDING,
				    "Przykro mi, ale nie pożyczam wyposażenia osobom z tak małym doświadczeniem jakim ty masz.",
				    null);

				add(ConversationStates.ATTENDING, Arrays.asList("borrow", "pożycz", "pożyczyć"),
				    new AndCondition(new LevelGreaterThanCondition(5), new QuestNotCompletedCondition("pizza_delivery")),
				    ConversationStates.ATTENDING,
				    "Musisz porozmawiać z Leanderem i zapytać go czy może ci pomóc z pizzą nim pozwolę Tobie coś pożyczyć.",
				    null);

				add(ConversationStates.ATTENDING, Arrays.asList("borrow", "pożycz", "pożyczyć"),
				    new AndCondition(
				        new LevelGreaterThanCondition(5),
				        new QuestCompletedCondition("pizza_delivery"),
				        new QuestNotActiveCondition(QUEST_SLOT)),
				    ConversationStates.ATTENDING,
				    "Pożyczę Tobie " + Grammar.enumerateCollectionWithHash(ITEMS) + ". Jeżeli jesteś zainteresowany to powiedz co potrzebujesz.",
				    null);

				// player already has borrowed something it didn't return and will pay for it
				add(ConversationStates.ATTENDING, Arrays.asList("borrow", "pożycz", "pożyczyć"),
				    new AndCondition(new QuestActiveCondition(QUEST_SLOT), new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT))),
				    ConversationStates.QUESTION_1,
				    "Nie zwróciłeś tego co ostatnio ci pożyczyłam! Czy chcesz za to zapłacić " + COST + " money?",
				    null);

				// player already has borrowed something it didn't return and will return it
				add(ConversationStates.ATTENDING, Arrays.asList("borrow", "pożycz", "pożyczyć"),
				    new AndCondition(new QuestActiveCondition(QUEST_SLOT), new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT)),
				    ConversationStates.QUESTION_2,
				    "Nie zwróciłeś tego co ostatnio ci pożyczyłam! Czy chcesz teraz zwrócić?",
				    null);

				// player wants to pay for previous item
				final List<ChatAction> payment = new LinkedList<ChatAction>();
				payment.add(new DropItemAction("money", COST));
				payment.add(new SetQuestAction(QUEST_SLOT, "done"));
				payment.add(new DecreaseKarmaAction(10));
				add(ConversationStates.QUESTION_1,
				    ConversationPhrases.YES_MESSAGES,
				    new PlayerHasItemWithHimCondition("money", COST),
				    ConversationStates.ATTENDING,
				    "Dziękuję. Daj mi znać jeżeli chcesz #pożyczyć jeszcze jakieś narzędzie.",
				    new MultipleActions(payment));

				// player already has borrowed something and wants to return it
				final List<ChatAction> returnitem = new LinkedList<ChatAction>();
				returnitem.add(new DropRecordedItemAction(QUEST_SLOT));
				returnitem.add(new SetQuestAction(QUEST_SLOT, "done"));
				add(ConversationStates.QUESTION_2,
				    ConversationPhrases.YES_MESSAGES,
				    new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT),
				    ConversationStates.ATTENDING,
				    "Dziękuję! Daj mi znać jeżeli chcesz #pożyczyć jeszcze jakieś narzędzie.",
				    new MultipleActions(returnitem));

				// don't want to pay for it now
				add(ConversationStates.QUESTION_1,
				    ConversationPhrases.NO_MESSAGES,
				    null,
				    ConversationStates.ATTENDING,
				    "Żaden problem. trzymaj tak długo ile potrzebujesz, ale nie możesz pożyczyć innych narzędzi dopóki nie zwrócisz ostatniego lub nie zapłacisz za niego.",
				    null);

				// does want to pay for it now
				add(ConversationStates.QUESTION_1,
				    ConversationPhrases.YES_MESSAGES,
				    new NotCondition(new PlayerHasItemWithHimCondition("money", COST)),
				    ConversationStates.ATTENDING,
				    "Przepraszam, ale wygląda na to, że nie masz wystarczająco dużo money ze sobą.",
				    null);

				// don't want to return it now
				add(ConversationStates.QUESTION_2,
				    ConversationPhrases.NO_MESSAGES,
				    null,
				    ConversationStates.ATTENDING,
				    "Żaden problem. trzymaj tak długo ile potrzebujesz, ale nie możesz pożyczyć innych narzędzi dopóki nie zwrócisz ostatniego lub nie zapłacisz za niego.",
				    null);

				// saying the item name and storing that item name into the quest slot, and giving the item
				for(final String itemName : ITEMS) {
				add(ConversationStates.ATTENDING,
					    itemName,
				    new AndCondition(
				        new LevelGreaterThanCondition(5),
				        new QuestCompletedCondition("pizza_delivery"),
				        new QuestNotActiveCondition(QUEST_SLOT)),
				    ConversationStates.ATTENDING,
				    null,
					    new ChatAction() {
							@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
								final Item item =  SingletonRepository.getEntityManager().getItem(itemName);
								if (item == null) {
									npc.say("Przykro mi, ale coś poszło źle. Czy mógłbyś poprawnie wypowiedzieć nazwę przedmiotu?");
								} else {
									player.equipOrPutOnGround(item);
									player.setQuest(QUEST_SLOT, itemName);
									npc.say("Oto on! Nie zapomnij #zwrócić go, bo w przeciwnym wypadku będziesz musiał za niego zapłacić!");
								}
							}
						});
				}

				// additionally add "sugar" as trigger word
				add(ConversationStates.ATTENDING,
					    "cukier",
					    new AndCondition(
					        new LevelGreaterThanCondition(5),
					        new QuestCompletedCondition("pizza_delivery"),
					        new QuestNotActiveCondition(QUEST_SLOT)),
					    ConversationStates.ATTENDING,
					    null,
					    new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
								npc.say("Przykro mi, ale nie mogę pożyczyć Tobie cukru, a tylko #młynek #do #cukru.");
					}
				});

				// too low level
				add(ConversationStates.ATTENDING,
					    ITEMS,
					    new LevelLessThanCondition(6),
					    ConversationStates.ATTENDING,
					    "Przykro mi, ale dopóki będziesz miał tyle doświadczenia w tym świecie to nie będę mogła ci zaufać.",
					    null);

				// currently has borrowed an item
				add(ConversationStates.ATTENDING,
					    ITEMS,
					    new QuestActiveCondition(QUEST_SLOT),
					    ConversationStates.ATTENDING,
					    "Nie możesz ponownie ode mnie pożyczyć dopóki nie #zwrócisz ostatniego narzędzia, który ci pożyczyłam.",
					    null);

				// haven't done pizza
				add(ConversationStates.ATTENDING,
					    ITEMS,
					    new QuestNotCompletedCondition("pizza_delivery"),
					    ConversationStates.ATTENDING,
					    "Tylko dostawcy pizzy mogą pożyczać narzędzia. Dostarcz coś dla Leandera, a wtedy zapytaj mnie ponownie.",
					    null);

				// player asks about pay from attending state
				add(ConversationStates.ATTENDING, Arrays.asList("pay", "zapłać"),
				    new QuestActiveCondition(QUEST_SLOT),
				    ConversationStates.QUESTION_1,
				    "Jeżeli zgubisz to co pożyczyłeś to możesz za to zapłacić " + COST + " money. Czy chcesz teraz zapłacić?",
				    null);

				// player asks about return from attending state
				add(ConversationStates.ATTENDING, Arrays.asList("return", "zwrot", "zwrócisz"),
				    new AndCondition(new QuestActiveCondition(QUEST_SLOT), new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT)),
				    ConversationStates.QUESTION_2,
				    "Czy chcesz teraz zwrócić to co pożyczyłeś?",
				    null);

				// player asks about return from attending state
				add(ConversationStates.ATTENDING, Arrays.asList("return", "zwrot"),
				    new AndCondition(new QuestActiveCondition(QUEST_SLOT), new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT))),
				    ConversationStates.QUESTION_1,
				    "Ni masz tego ze sobą! Czy chcesz teraz za to zapłacić " + COST + " money?",
				    null);

			}};
			npc.setPosition(26, 9);
			npc.setEntityClass("housewifenpc");
			npc.setDescription("Oto Erna. Pracuje już długo dla Leandera i jest jego lojalnym pomocnikiem.");
			zone.add(npc);
	}
}
