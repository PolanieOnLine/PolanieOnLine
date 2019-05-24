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
package games.stendhal.server.maps.quests;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropInfostringItemAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasInfostringItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * QUEST: Supplies For Phalk
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Phalk, the dwarf guarding Semos mines</li>
 * <li>Wrvil, a kobold weapon trader in Wo'fol</li>
 * <li>Mrotho, in Ados barracks</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>1. Phalk asks for some food and drink</li>
 * <li>2. Once you brought him the food and drink, Phalk asks you to collect his clothes from Wrvil and Mrotho</li>
 * <li>3. Wrvil gives you Phalk's special dwarf cloak but you must pay for it in arrows</li>
 * <li>4. Mrotho gives you Phalk's special golden armor but you must pay for it in gold bars</li>
 * <li>5. Phalk accepts only the special items from Wrvil and Mrotho with his name on</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>Dwarvish armor</li>
 * <li>5000 XP in total</li>
 * <li>Karma</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Not repeatable.</li>
 * </ul>
 */
 
 public class SuppliesForPhalk extends AbstractQuest {
 
 	private static final String QUEST_SLOT = "supplies_for_phalk";
 	
 	private static Logger logger = Logger.getLogger(SuppliesForPhalk.class);
 	
 	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void askForFood() {
		final SpeakerNPC npc = npcs.get("Phalk");	
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Jestem tutaj od dłuższego czasu i nie mogę opuścić tego miejsca. Czy mógłbyś przynieść mi trochę jedzenia? ",
				null);
							
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję za przyniesienie mi trochę jedzenia i ubrań. Sądzę, że teraz mogę stać tutaj i ostrzegać ludzi przez kilka miesięcy.",
				null);
		

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Wspaniale! Jestem strasznie głodny i spragniony. 3 #kanapki , 3 butelki #'soku z chmielu' i 3 kieliszki #'napój z winogron' powinny wystarczyć. Proszę przynieś mi to i powiedz #jedzenie!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Och to nie jest miłe..., ale dobrze. Może następna osoba mi pomoże.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
		
		npc.addReply("sok z chmielu", "Oczywiście w tawernie!");
		npc.addReply("napój z winogron", "Oczywiście w tawernie!");
		npc.addReply(Arrays.asList("sandwiches", "sandwich","kanapki","kanapka","sandwiche"), "Zapytaj w piekarni!");
	}
	
	private void receiveFood() {
	final SpeakerNPC npc = npcs.get("Phalk");	
	
		npc.add(ConversationStates.ATTENDING, Arrays.asList("food", "jedzenie", "jedzenia"),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Masz 3 kanapki, 3 butelki soku z chmielu i 3 szklanki napoju z winogron?",
				null);
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(600));
		actions.add(new DropItemAction("kanapka",3));
		actions.add(new DropItemAction("sok z chmielu",3));
		actions.add(new DropItemAction("napój z winogron",3));
		// the extra parts in the quest state are for wrvil and mrotho not to give them cloaks and armor twice
		actions.add(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "clothes;none;none", 2.0));		
		actions.add(new InflictStatusOnNPCAction("kanapka"));
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new PlayerHasItemWithHimCondition("kanapka",3),
						new PlayerHasItemWithHimCondition("sok z chmielu",3),
						new PlayerHasItemWithHimCondition("napój z winogron",3)),
				ConversationStates.ATTENDING, 
				"Dziękuję!!! Jest jeszcze jedna rzecz, którą mógłbyś zrobić dla mnie: moje ubrania są stare i podarte. Potrzebuję nowego #płaszcza i nowej #zbroi. Proszę przynieś mi je i powiedz #ubrania.",
				new MultipleActions(actions)
		);
		
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
				new NotCondition(
						new AndCondition(
								new PlayerHasItemWithHimCondition("kanapka",3),
								new PlayerHasItemWithHimCondition("sok z chmielu",3),
								new PlayerHasItemWithHimCondition("napój z winogron",3)))),
				ConversationStates.ATTENDING, 
				"Jestem tutaj od dłuższego czasu i co więcej jestem naprawdę głodny. Nie oszukasz mnie.",
				null);
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, 
				ConversationPhrases.NO_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "start"), 
				ConversationStates.ATTENDING,
				"Phi! To odejdź! Ale bądź pewny, że nie dostaniesz nagrody jeżeli nie przyniesiesz mi przedmiotów!", 
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING,
				"Już cię poprosiłem o przyniesienie #jedzenia!",
				null);
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("cloak", "płaszcza"),
				new QuestInStateCondition(QUEST_SLOT, 0, "clothes") ,
				ConversationStates.ATTENDING, 
				"Znam Wrvila (mieszka w Wofol) ma nowy płaszcz dla mnie. Powiedz mu moje imię.",
				null);
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("armor", "zbroi"),
				new QuestInStateCondition(QUEST_SLOT, 0, "clothes") ,
				ConversationStates.ATTENDING, 
				"Mrotho (mieszka w Ados) powiedział mi, że poszuka dla mnie złotej zbroji. Powiedz mu moje imię.",
				null);
		
	}
	
	private void getCloak() {
	final SpeakerNPC npc = npcs.get("Wrvil");
	
		npc.add(ConversationStates.ATTENDING, "Phalk",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "none")) ,
				ConversationStates.ATTENDING, 
				"Aaach, jego płaszcz... tak jest gotowy, ale wciąż czekam na #zapłatę!",
				null);
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("payment", "zapłatę"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "none")),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Och tak! Kosztuje to 20 strzał żelaznych. Nasze ofiary nie przynoszą ich z  powrotem ;) Masz je?",
				null);	
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, 
				ConversationPhrases.NO_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "none")), 
				ConversationStates.ATTENDING,
				"Cóż nie mogę dać tobie płaszcza! Najpierw zapłata!", 
				null);
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(200));
		actions.add(new DropItemAction("strzała żelazna",20));
		actions.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item cloak = SingletonRepository.getEntityManager().getItem("płaszcz krasnoludzki");
				cloak.setInfoString("Phalk");
				cloak.setDescription("Oto piękny nowy płaszcz krasnoludzki z imieniem 'Phalk' wyszytym na metce przez Wrvila.");
				// remember the description
				cloak.setPersistent(true);
				cloak.setBoundTo(player.getName());
				player.equipOrPutOnGround(cloak);
			}
		});
		// the extra parts in the quest state are for wrvil and mrotho not to give them cloaks and armor twice
		actions.add(new SetQuestAction(QUEST_SLOT, 1, "cloak"));
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "none")),
						new PlayerHasItemWithHimCondition("strzała żelazna",20)),
				ConversationStates.ATTENDING, 
				"Dobrze, trzymaj.",
				new MultipleActions(actions)
		);
		
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "none"),
				new NotCondition(new PlayerHasItemWithHimCondition("strzała żelazna",20))),
				ConversationStates.ATTENDING, 
				"Jesteś typem kłamcy. Nieprawdaż? Wróć, gdy będziesz miał zapłatę.",
				null);
		
		
		// player got the cloak already but lost it?
		npc.add(ConversationStates.ATTENDING, "Phalk",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "cloak")) ,
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Weź ten płaszcz, który dałem ci dla Phalk. Jeżeli zgubisz go to w zamian będziesz musiał zapłacić 250 money. Czy chcesz zamienić się z Phalkiem?",
				null);
		
		final List<ChatAction> actions2 = new LinkedList<ChatAction>();
		actions2.add(new DropItemAction("money",250));
		actions2.add(new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final Item cloak = SingletonRepository.getEntityManager().getItem("płaszcz krasnoludzki");
					cloak.setInfoString("Phalk");
					cloak.setDescription("Oto nowy płaszcz krasnoludzki z imieniem 'Phalk' wyszytym w metkę Wrvila.");
					// remember the description
					cloak.setPersistent(true);
					cloak.setBoundTo(player.getName());
					player.equipOrPutOnGround(cloak);
				}
			});		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "cloak")),
						new PlayerHasItemWithHimCondition("money",250)),
				ConversationStates.ATTENDING, 
				"Dobrze oto on.",
				new MultipleActions(actions2)
		);
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "cloak"),
				new NotCondition(new PlayerHasItemWithHimCondition("money",250))),
				ConversationStates.ATTENDING, 
				"Nie masz wystarczająco dużo pieniędzy.",
				null);
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.NO_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 1, "cloak")),
				ConversationStates.ATTENDING, 
				"Dobrze, ale Phalk akceptuje tylko płaszcz krasnoludzki ode mnie z jego imieniem wyszytym.",
				null);

	}
	
	private void getArmor() {
		final SpeakerNPC npc = npcs.get("Mrotho");
		
		npc.add(ConversationStates.ATTENDING, "Phalk",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none")) ,
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Ooops jego zbroja...poczekaj.. gdzie jest.. aach tutaj jest. Dał ci też #zapłatę dla mnie?",
				null);
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, Arrays.asList("payment", "zapłatę"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none")),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Cóż.. zbroja będzie kosztować 20 gold barów. Masz je?",
				null);	
		
		// incase player goes on to ask something else, accept payment from attending too.
		npc.add(ConversationStates.ATTENDING, Arrays.asList("payment", "zapłatę"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none")),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Zbroja będzie kosztować 20 sztabek złota. Masz je?",
				null);	
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, 
				ConversationPhrases.NO_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none")), 
				ConversationStates.ATTENDING,
				"Ba! Nie dam tobie zbroi bez zapłaty!", 
				null);
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(200));
		actions.add(new DropItemAction("sztabka złota",20));
		actions.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item armor = SingletonRepository.getEntityManager().getItem("złota zbroja");
				armor.setInfoString("Phalk");
				armor.setDescription("Oto błyszczący golden armor z imieniem 'Phalk' wygrawerowanym w środku.");
				// remember the description
				armor.setPersistent(true);
				armor.setBoundTo(player.getName());
				player.equipOrPutOnGround(armor);
			}
		});
		// the extra parts in the quest state are for wrvil and mrotho not to give them cloaks and armor twice
		actions.add(new SetQuestAction(QUEST_SLOT, 2, "armor"));
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none")),
						new PlayerHasItemWithHimCondition("sztabka złota",20)),
				ConversationStates.ATTENDING, 
				"Dobrze, trzymaj.",
				new MultipleActions(actions)
		);
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "none"),
				new NotCondition(new PlayerHasItemWithHimCondition("sztabka złota",20))),
				ConversationStates.ATTENDING, 
				"Wojskowa dyscyplina jest poważna. Nie próbuj mnie oszukać.",
				null);
		
		// player got the armor already but lost it?
		npc.add(ConversationStates.ATTENDING, "Phalk",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "armor")) ,
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Weź tą zbroję i daj ją Phalkowi. Jeżeli zgubisz ją to w zamian będziesz msuiał zapłacić 10000 money. Czy chcesz zapłacić za zamianę z Phalkiem?",
				null);
		
		final List<ChatAction> actions2 = new LinkedList<ChatAction>();
		actions2.add(new DropItemAction("money",10000));
		actions2.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item armor = SingletonRepository.getEntityManager().getItem("złota zbroja");
				armor.setInfoString("Phalk");
				armor.setDescription("Oto lśniąca złota zbroja z nazwą 'Phalk' wyrytą na niej.");
				// remember the description
				armor.setPersistent(true);
				armor.setBoundTo(player.getName());
				player.equipOrPutOnGround(armor);
			}
		});		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "armor")),
						new PlayerHasItemWithHimCondition("money",10000)),
				ConversationStates.ATTENDING, 
				"Dobrze oto ona.",
				new MultipleActions(actions2)
		);
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "armor"),
				new NotCondition(new PlayerHasItemWithHimCondition("money",10000))),
				ConversationStates.ATTENDING, 
				"Nie masz wystarczająco dużo pieniędzy.",
				null);
		
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, ConversationPhrases.NO_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),new QuestInStateCondition(QUEST_SLOT, 2, "armor")),
				ConversationStates.ATTENDING, 
				"Dobrze, ale Phalk przyjmie zbroję tylko ode mnie ze swoim imieniem wyrytym na niej.",
				null);
		
	}
		
	
	private void receiveClothes() {
	final SpeakerNPC npc = npcs.get("Phalk");	
	
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(4000));
		actions.add(new DropInfostringItemAction("złota zbroja","Phalk"));
		actions.add(new DropInfostringItemAction("płaszcz krasnoludzki","Phalk"));
		actions.add(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "done", 5.0));	
		actions.add(new EquipItemAction("zbroja krasnoludzka", 1, true));
		
		npc.add(ConversationStates.ATTENDING, Arrays.asList("clothes", "ubrania"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),
				new PlayerHasInfostringItemWithHimCondition("złota zbroja","Phalk"),
				new PlayerHasInfostringItemWithHimCondition("płaszcz krasnoludzki","Phalk")),
				ConversationStates.ATTENDING, 
				"Och tak! Dziękuję bardzo! Zapłata?? Erm... *kaszlnięcie* Dam ci moją starą zbroję jako zapłatę.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("clothes", "ubrania"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),
				new NotCondition(
						new AndCondition(
								new PlayerHasInfostringItemWithHimCondition("złota zbroja","Phalk"),
								new PlayerHasInfostringItemWithHimCondition("płaszcz krasnoludzki","Phalk")))),
				ConversationStates.ATTENDING, 
				"Hm chcę specjalnej złotej #zbroji od Mrotho i krasnoludzkiego #płaszcza od Wrvila. Powiedz im moje imię, a dadzą ci to co zrobili dla mnie.",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, 0, "clothes"),
				ConversationStates.ATTENDING,
				"Czekam na ciebie aż przyniesiesz mi nowe #ubrania od Wrvila i Mrotho.",
				null);
	}
	
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zapasy dla Phalka",
				"Phalk strażnik krasnali w Semos Mine potrzebuje zapasów.",
				false);
		askForFood();
		receiveFood();
		getCloak();
		getArmor();
		receiveClothes();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Rozmawiałem z Phalk, który pilnuje przejścia w Semos Mines.");
			res.add("Phalk poprosił mnie, żebym przyniósł mu 3 kanapki, 3 soki z chmielu i 3 napoje z winogron.");
			if ("rejected".equals(questState)) {
				res.add("Nie mam ochoty pomagać Phalkowi.");
				return res;
			} 
			if ("start".equals(questState)) {
				if(player.isEquipped("kanapka",3)) {
					res.add("Mam kanapki!");
				}
				if(player.isEquipped("sok z chmielu",3)) {
					res.add("Mam sok z chmielu!");
				}
				if(player.isEquipped("napój z winogron",3)) {
					res.add("Mam napój z winogron!");
				}
				return res;
			} 
			res.add("Teraz Phalk potrzebuje płaszcza od Wrvil i zbroje od Mrotho.");
			if (questState.startsWith("clothes")) {
				if(new QuestInStateCondition(QUEST_SLOT, 1, "cloak").fire(player,null, null)){
					res.add("Mam płaszcz, za który musiałem zapłacić!");
				}
				if(new QuestInStateCondition(QUEST_SLOT, 2, "armor").fire(player,null, null)){
					res.add("Mrotho dał mi złotą zbroie dla Phalka, ale musiałem pokryć dług.");
				}
				return res;
			} 
			res.add("Oddałem Phalkowi ekwipunek w nagrodę dostałem zbroję krasnoludzką!");
			if (isCompleted(player)) {
				return res;
			}
			// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
			final List<String> debug = new ArrayList<String>();
			debug.add("Stan zadania to: " + questState);
			logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
			return debug;
	}
	
	@Override
	public String getName() {
		return "SuppliesForPhalk";
	}
	
	@Override
	public int getMinLevel() {
		return 30;
	}
	@Override
	public String getNPCName() {
		return "Phalk";
	}
		
	@Override
	public String getRegion() {
		return Region.SEMOS_MINES;
	}
}
 
