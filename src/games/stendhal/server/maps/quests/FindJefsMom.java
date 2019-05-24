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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerCanEquipItemCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;


/**
 * QUEST: Find Jefs mum
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Jef</li>
 * <li>Amber</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Jef waits for his mum in Kirdneh for a longer time now and is frightened that something happened to her</li>
 * <li> You go to find Amber somewhere in Fado forest</li>
 * <li> She gives you a flower which you have to bring to Jef</li>
 * <li> You return and give the flower to Jef</li>
 * <li> Jef will reward you well</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 800 XP</li>
 * <li> Red lionfish which Jef got by someone who made holidays on Amazon island earlier (between 1-6)</li>
 * <li> Karma: 15</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> Once every 4320 minutes. (3 days)</li>
 * </ul>
 *
 * @author Vanessa Julius
 *
 */
public class FindJefsMom extends AbstractQuest {

	// 4320 minutes (3 days)
	private static final int REQUIRED_MINUTES = 4320;

	private static final String QUEST_SLOT = "find_jefs_mom";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void offerQuestStep() {
		final SpeakerNPC npc = npcs.get("Jef");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new OrCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestInStateCondition(QUEST_SLOT, 0, "rejected")),
			ConversationStates.QUEST_OFFERED,
			"Tęsknię za moją mamusią! Chciała pójść na rynek i do tej pory nie wróciła. Mógłbyś jej poszukać?",
			null);

		// player asks about quest which he has done already and he is allowed to repeat it
				npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"), new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED, 
				"Minęło już trochę czasu, odkąd rozglądałeś się za moją mamą. Czy mogę Cię prosić, abyś poszukał jej raz jeszcze i powiedział mi, czy miewa się dobrze, ok?",
				null);

		// player asks about quest but time didn't pass yet
		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1,REQUIRED_MINUTES))), 
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Nie chcę przeszkadzać mojej mamie, nim nie wróci z powrotem, więc nie musisz jej nic przekazać. Może zapytaj mnie raz jeszcze puźniej..."));


		// Player agrees to find mum
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dziękuję Ci bardzo! Mam nadzieję, że #mama trzyma się świetnie i prędko wróci! Proszę, powiedz jej moje imię, #Jef, żeby udownodnić, że to ja Cię do niej wysłałem. Jeśli znajdziesz ją i wrócisz do mnie, dam Ci coś w zamian, żeby pokazać Ci, jak wdzięczny jestem.",
			new MultipleActions(new SetQuestAction(QUEST_SLOT, 0, "start")));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
			"Oh. Dobrze. Nie potrafię Cię zrozumieć... Wyglądasz na przepracowanego bohatera, więc nie będę Cię prosić o pomoc.",
			new MultipleActions(new SetQuestAction(QUEST_SLOT, 0, "rejected"),
					new DecreaseKarmaAction(10.0)));

		// Player asks for quest but is already on it

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Mam nadzieję, że znajdziesz mamę szybko i powiesz mi, czy jest #cała i #zdrowa.",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("mum", "mother", "mom", "mama"),
				null,
				ConversationStates.ATTENDING,
				"Moja mama Amber poszła na rynek coś kupić do jedzenia. Niestety #jeszcze nie wróciła.",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				"jeszcze",
				null,
				ConversationStates.ATTENDING,
				"Moja mama, Amber, zostawiła mnie tu, żeby kupić coś do jedzenia na rynku, ale wciąż nie wraca. Jedyne co wiem to to, że wcześniej zerwała ze swoim chłopakiem. Nazywał się #Roger #Frampton. Znajdziesz ją?",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				"Jef",
				null,
				ConversationStates.ATTENDING,
				"Tak, to ja :) Mamusia powiedziała mi kiedyś, że tak kocha to imię, że zdecydowała się mi je dać :) To jak, poszukasz jej dla mnie?",
				null);

		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("Roger Frampton", "Roger", "Frampton"),
				null,
				ConversationStates.ATTENDING,
				"Był ukochanym mamy, nim się rozstali. Może on ma jakieś informacje, co się z nią teraz dzieje? Obecnie on sprzedaje domy gdzieś w Kirdneh. To jak poszukasz mamusi?",
				null);

	}

	private void findMomStep() {
		final SpeakerNPC amber = npcs.get("Amber");

        // give the flower if it's at least 5 days since the player activated the quest the last time, and set the time slot again
		amber.add(ConversationStates.ATTENDING, "Jef",
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"start"),
							 new PlayerCanEquipItemCondition("bielikrasa")),
                          
			ConversationStates.IDLE, 
			"O, rozumiem. :) Mój syn Jef poprosił Cię, żebyś mnie poszukał. Co za kochany i troskliwy chłopiec! Proszę, daj mu tę bielikrasę. Kocham te kwiaty! Bedzie wiedział, że ze mną wszystko #w #porządku, jeśli mu ją dasz!",
			new MultipleActions(new EquipItemAction("bielikrasa", 1, true), 
                                new SetQuestAction(QUEST_SLOT, 0, "found_mom"))); 
                             

		// don't put the flower on the ground - if player has no space, tell them
		amber.add(ConversationStates.ATTENDING, "Jef",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
								 new NotCondition(new PlayerCanEquipItemCondition("bielikrasa"))),
				ConversationStates.IDLE, 
				"Oh, chciałam dać Ci kwiatek dla mojego syna, żeby pokazać mu, że ze mną wszystko w porządku, ale widzę, że nie masz już miejsca w torbie. Wróć do mnie, jak będziesz mieć nieco miejsca w plecaku!",
				null);

        // don't give the flower if the quest state isn't start
	    amber.add(ConversationStates.ATTENDING, "Jef",
		     	new AndCondition(new NotCondition(new QuestActiveCondition(QUEST_SLOT))),
		    	ConversationStates.IDLE,
		    	"Nie ufam Ci. Twój głos drżał, gdy wymieniałeś imię mojego syna. Założę się, że ma się świetnie i jest bezpieczny.", 
		    	null);

	    amber.add(ConversationStates.ATTENDING, "Jef",
	    		new AndCondition(
	    				new QuestInStateCondition(QUEST_SLOT, "found_mom"),
	    				new PlayerHasItemWithHimCondition("bielikrasa")),
	    		ConversationStates.IDLE,
	    		"Proszę daj ten kwiatek mojemu synowi i daj mu znać, że u mnie wszystko #dobrze.",
	    		null);
	    
	    // replace flower if lost
	    amber.add(ConversationStates.ATTENDING, Arrays.asList("Jef", "flower", "zantedeschia", "kwiat", "bielikrasa"),
	    		new AndCondition(
	    				new QuestInStateCondition(QUEST_SLOT, 0, "found_mom"),
	    				new NotCondition(new PlayerHasItemWithHimCondition("bielikrasa"))),
	    		ConversationStates.IDLE,
	    		"Oh zgubiłeś kwiatek? Obawiam się, że już ich nie mam. Porozmaiwaj z Jenny przy młynie. Może będzie mogła ci pomóc.",
	    		null); 

	}

	private void bringFlowerToJefStep() {
		final SpeakerNPC npc = npcs.get("Jef");

		ChatAction addRandomNumberOfItemsAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				//add random number of red lionfish
				final StackableItem red_lionfish = (StackableItem) SingletonRepository.getEntityManager()
						.getItem("skrzydlica");
				int redlionfishamount;
				redlionfishamount = Rand.roll1D6();
				red_lionfish.setQuantity(redlionfishamount);
				player.equipOrPutOnGround(red_lionfish);
				npc.say("Dziękuję! Weź te " + Integer.toString(redlionfishamount) + " ryby! Mam kilka od pewnego gościa, który odwiedził wyspę Amazonek jakiś czas temu, może będziesz ich potrzebować.");

			}
		};
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("flower", "zantedeschia", "fine", "amber", "done", "cała", "zdrowa", "dobrze", "kwiat", "bielikrasa"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "found_mom"), new PlayerHasItemWithHimCondition("bielikrasa")),
				ConversationStates.ATTENDING, null,
				new MultipleActions(new DropItemAction("bielikrasa"), 
                                    new IncreaseXPAction(800),
                                    new IncreaseKarmaAction(15),
									addRandomNumberOfItemsAction,
									new IncrementQuestAction(QUEST_SLOT, 2, 1),
									new SetQuestToTimeStampAction(QUEST_SLOT,1),
									new SetQuestAction(QUEST_SLOT, 0, "done")));


	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Znajdź matkę Jefa",
				"Jef, mały chłopiec w Kirdneh, czeka na swoją mamę Amber, która nie wróciła z zakupów na rynku.",
				false);
		offerQuestStep();
		findMomStep();
		bringFlowerToJefStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Znalazłem Jefa w Kirdneh. Czeka tam na swoją mamę.");
        final String questStateFull = player.getQuest(QUEST_SLOT);
        final String[] parts = questStateFull.split(";");
        final String questState = parts[0];

        if ("rejected".equals(questState)) {
			res.add("Znalezienie jego mamy kosztuje mnie teraz zbyt wiele czasu, dlatego musiałem odrzucić jego prośbę o pomoc w znalezieniu jej.");
		}
		if ("start".equals(questState)) {
			res.add("Jef poprosił mnie, żebym rozejrzał się za jego matką, Amber, która nie wróciła z zakupów na rynku. Mam nadzieję, że wysłucha mnie, gdy powiem imię jej syna - Jefa.");
		}
		if ("found_mom".equals(questState)) {
			res.add("Znalazłem Amber, mamę Jefa, kiedy spacerowała gdzieś w lesie Fado. Dała mi kwiaty dla swojego syna i powiedziała mi, że te kwiaty powiedzą mu, że z nią wszystko w porządku.");
		}
		if ("done".equals(questState)) {
            if (isRepeatable(player)) {
                res.add("Przyniosłem Jefowi bielikrasę, a on bardzo się ucieszył, gdy dowiedził się, że jego mama, Amber, trzyma się dobrze. Chociaż Jef nie chce, żebym pilnował jej znowu, tzeba zapytać go, czy nie zmienił zdania.");
            } else {
                res.add("Przyniosłem Jefowi bielikrasę, a on bardzo się ucieszył, gdy dowiedział się, że jego mama, Amber, trzyma się dobrze. Chce zostawić mamę samą na jakiś czas.");
            }
		}

		return res;

	}

	@Override
	public String getName() {
		return "FindJefsMom";
	}


	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"done"),
				 new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}
	@Override
	public String getNPCName() {
		return "Jef";
	}
}
