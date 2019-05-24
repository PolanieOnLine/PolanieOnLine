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
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Quest to buy chocolate for a little girl called Elisabeth.
 * Ask her mother Carey for a quest and she will ask you to get some chocolate for her daughter.
 * Get some chocolate and bring it to Elisabeth.
 *
 * @author Vanessa Julius idea by miasma
 *
 *
 * QUEST: Chocolate for Elisabeth
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Elisabeth (a young girl who loves chocolate)</li>
 * <li>Carey (Elisabeth's mother)</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Elisabeth asks you to bring her a chocolate bar.</li>
 * <li>Get some chocolate .</li>
 * <li>Ask Carey if she allows you to give the chocolate to her daughter.</li>
 * <li>Make Elisabeth happy and get a lovely reward.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>a random flower</li>
 * <li>500 XP</li>
 * <li>10 karma</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Every 60 minutes</li>
 * </ul>
 */
public class ChocolateForElisabeth extends AbstractQuest {

	// constants
	private static final String QUEST_SLOT = "chocolate_for_elisabeth";

	/** The delay between repeating quests. */
	private static final int REQUIRED_MINUTES = 60;
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void chocolateStep() {
		final SpeakerNPC npc = npcs.get("Elisabeth");

		// first conversation with Elisabeth.
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.ATTENDING,
				"Nie pamiętam kiedy ostatnio czułam zapach dobrej tabliczki #czekolady...",
				null);

		npc.addReply(Arrays.asList("chocolate", "czekolada", "czekolady"), "Moja mama powiedziała mi, że czekoladę można znaleść w szkole zabójców, która jest #niebezpieczna. Powiedziała mi także, że ktoś sprzedaje ją w Ados...");

		npc.addReply(Arrays.asList("dangerous", "niebezpieczna"), "Paru bandytów czeka na drodze do szkoły, a zabójcy strzegą jej i dlatego moja mama i ja musimy zostać w Kirdneh ponieważ nie jest tam bezpiecznie...");

		// player is supposed to speak to mummy now
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("tabliczka czekolady")),
				ConversationStates.IDLE,
				"Moja mama chce wiedzieć kogo pytałam o tabliczkę czekolady :(",
				null);

		// player didn't get chocolate, meanie
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("tabliczka czekolady"))),
				ConversationStates.ATTENDING,
				"Mam nadzieje, że ktoś mi przyniesie tabliczkę czekolady...:(",
				null);

		// player got chocolate and spoke to mummy
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "mummy"), new PlayerHasItemWithHimCondition("tabliczka czekolady")),
				ConversationStates.QUESTION_1,
				"Wspaniale! Ta tabliczka czekolady jest dla mnie?",
				null);

		// player spoke to mummy and hasn't got chocolate
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "mummy"), new NotCondition(new PlayerHasItemWithHimCondition("tabliczka czekolady"))),
				ConversationStates.ATTENDING,
				"Mam nadziej, że ktoś przyniesie mi tabliczkę czekolady...:(",
				null);

		// player is in another state like eating
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStartedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "start"), new QuestNotInStateCondition(QUEST_SLOT, "mummy")),
				ConversationStates.ATTENDING,
				"Cześć.",
				null);

		// player rejected quest
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "rejected")),
				ConversationStates.ATTENDING,
				"Cześć.",
				null);

		// player asks about quest for first time (or rejected)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Chciałabym dostać tabliczkę czekolady. Chociaż jedną. Ciemno brązową lub słodką białą lub z posypką. Zdobędziesz jedną dla mnie?",
				null);

		// shouldn't happen
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Został mi jeszcze kawałek czekolady, którą mi przyniosłeś, dziękuję!",
				null);

		// player can repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"), new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Mam nadzieję, że jeżeli poproszę o następną tabliczkę czekolady to nie będę zbyt zachłanna. Czy możesz zdobyć następną?",
				null);

		// player can't repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"), new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				"Zjadłam za dużo czekolady. Nie czuję się dobrze.",
				null);

		// player should be bringing chocolate not asking about the quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "eating;"))),
				ConversationStates.ATTENDING,
				"Łaaaaaaaa! Gdzie jest moja czekolada ...",
				null);

		// Player agrees to get the chocolate
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dziękuję!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0));

		// Player says no, they've lost karma
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Dobrze poczekam aż moja mama znajdzie pomocną dłoń...",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// Player has got chocolate bar and spoken to mummy
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("tabliczka czekolady"));
		reward.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				// pick a random flower
				String rewardClass = Rand.rand(Arrays.asList("stokrotki","bielikrasa","bratek"));

				final StackableItem item = (StackableItem) SingletonRepository.getEntityManager().getItem(rewardClass);
				item.setQuantity(1);
				player.equipOrPutOnGround(item);
				player.notifyWorldAboutChanges();
			}
		});
		reward.add(new IncreaseXPAction(500));
		reward.add(new SetQuestAction(QUEST_SLOT, "eating;"));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT,1));
		reward.add(new IncreaseKarmaAction(10.0));
		reward.add(new InflictStatusOnNPCAction("tabliczka czekolady"));

		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasItemWithHimCondition("tabliczka czekolady"),
				ConversationStates.ATTENDING,
				"Dziękuję BARDZO! Jesteś świetny. Weź oto te kwiatki jako prezent.",
				new MultipleActions(reward));


		// player did have chocolate but put it on ground after question?
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(new PlayerHasItemWithHimCondition("tabliczka czekolady")),
				ConversationStates.ATTENDING,
				"Hej gdzie jest moja czekolada?!",
				null);

		// Player says no, they've lost karma
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Łaaaaaa! Jesteś wielkim tłuściochem.",
				new DecreaseKarmaAction(5.0));
	}

	private void meetMummyStep() {
		final SpeakerNPC mummyNPC = npcs.get("Carey");

		// player speaks to mummy before Elisabeth
		mummyNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(mummyNPC.getName()),
							new QuestNotStartedCondition(QUEST_SLOT)),
					ConversationStates.ATTENDING, "Cześć miło cię poznać.",
					null);

		// player is supposed to begetting chocolate
		mummyNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(mummyNPC.getName()),
							new QuestInStateCondition(QUEST_SLOT, "start")),
					ConversationStates.ATTENDING,
					"Oh już spotkałeś moją córkę Elisabeth. Wyglądasz na miłą osobę i byłabym wdzięczna gdybyś mógł przynieść jej tabliczkę czekolady ponieważ nie jestem zbyt #silna na to.",
					new SetQuestAction(QUEST_SLOT, "mummy"));

		mummyNPC.addReply(Arrays.asList("strong", "silna"), "Próbowałam zdobyć trochę czekolady dla Elisabeth parę razy, ale nie mogłam się przedostać przez morderców i bandytów kręcących się #tam.");

		mummyNPC.addReply(Arrays.asList("there", "tam"), "Przebywają w pobliżu zamku Ados. Uważaj na siebie! Słyszałam także o #kimś kto sprzedaje czekoladę.");

		mummyNPC.addReply(Arrays.asList("someone", "kimś"), "Nigdy nie odwiedziłam tej osoby ponieważ wydaje się być naprawdę... coż pracuje w Ados.");

		// any other state
		mummyNPC.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES, new GreetingMatchesNameCondition(mummyNPC.getName()), true,
					ConversationStates.ATTENDING, "Witaj ponownie.", null);
	}
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Czekolada dla Elisabeth",
				"Słodka, słodka czekolada! Nikt nie może bez niej żyć! A Elisabeth chciała by ją mieć...",
				true);
		chocolateStep();
		meetMummyStep();
	}


	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Elisabeth jest miłą małą dziweczynką żyjącą w Kirdneh razem ze swoją rodziną.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie lubię miłych małych dziewczynek.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start","mummy") || isCompleted(player)) {
			res.add("Mała Elisabeth potrzebuje tabliczkę czekolady.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start","mummy") && player.isEquipped("tabliczka czekolady") || isCompleted(player)) {
			res.add("Znalazłem pyszną tabliczkę czekolady dla Elisabeth.");
		}
        if ("mummy".equals(questState) || isCompleted(player)) {
            res.add("Rozmawiałem z Carey, matką Elisabeth i zgodziła się, abym mógł dać jej córce tabliczkę czekolady.");
        }
        if (isCompleted(player)) {
            if (isRepeatable(player)) {
                res.add("Przyniosłem trochę czekolady dla Elisabeth. Dała mi w zamian kwiatki. Może chciałaby więcej czekolady.");
            } else {
                res.add("Elisabeth je czekoladę, którą jej dałem i dała mi w zamian kwiaty.");
            }
		}
		return res;
	}
	@Override
	public String getName() {
		return "ChocolateForElisabeth";
	}

	// Getting to Kirdneh is not too feasible till this level
	@Override
	public int getMinLevel() {
		return 10;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"eating;"),
				 new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"eating;").fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}
	@Override
	public String getNPCName() {
		return "Elisabeth";
	}
}
