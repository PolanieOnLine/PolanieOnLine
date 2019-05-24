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
 
import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingUntilTimeReachedAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToFutureRandomTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimeReachedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;
import games.stendhal.server.util.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import marauroa.common.game.IRPZone;

import org.apache.log4j.Logger;

/**
 * QUEST: V.S.O.P. Koboldish Torcibud
 * <p>
 * PARTICIPANTS: <ul><li> Wrviliza, the Kobold Barmaid in Wo'fol bar </ul>
 * <p>
 * STEPS: <ul><li> Wrviliza will ask some items and ingredients 
 * to refurbish her stock of supplies for making her famous koboldish torcibud
 * <li> gather the items and ingredients and bring them to the bar
 * <li> some bottles of V.S.O.P. Koboldish Torcibud will be put on the bar counter
 * <li> drinking a bottle of V.S.O.P. Koboldish Torcibud will heal lost HP
 * <li> accepting the quest grants some Karma
 * <li> rejecting the quest wastes some Karma
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li> healing V.S.O.P. Koboldish Torcibud
 * <li> 1750 XP
 * <li> some karma (20 + (20 | -20))
 * </ul>
 * <p>
 * REPETITIONS: <ul><li> unlimited
 * <li>once every 3 to 6 days (randomly determined at quest completion time</ul>
 *
 * @author omero
 */
public class KoboldishTorcibud extends AbstractQuest {
 
    private static Logger logger = Logger.getLogger(KoboldishTorcibud.class);

    /**
     * QUEST_SLOT will be used to hold the different states of the quest.
     *
     * QUEST_SLOT sub slot 0 will hold the main states, which can be:
     * - rejected, the player has refused to undertake the quest
     * - done, the player has completed the quest
     * - a list of semi-colon separated key=value pairs.
     *
     * When the quest is completed,
     * QUEST_SLOT sub slot 1 will hold a timestamp randomly determined to be
     * from 3 to 6 days in the future.
     */
    public static final String QUEST_SLOT = "koboldish_torcibud";

    // the torcibud quest cannot be repeated before 3 to 6 days
    private static final int MIN_DELAY = 3 * MathHelper.MINUTES_IN_ONE_DAY;
    private static final int MAX_DELAY = 6 * MathHelper.MINUTES_IN_ONE_DAY;

    // how much XP is given as the reward
    private static final int XP_REWARD = 1750;

    // a template of the items that wrviliza will ask for the quest,
    // it is only used to initialize the triggers in phase_2.
    private static final String REQUIRED_ITEMS_TEMPLATE =
        "butla czwórniaczka=0;" +
        "wąska butelka=0;" +
        "mandragora=0;" +
        "karczoch=0;" +
        "arandula=0;" +
        "sclaria=0;" +
        "kekik=0;" +
        "ekstrakt litworowy=0";

    @Override
    public void addToWorld() {
        fillQuestInfo(
            "Koboldish Torcibud",
            "Wrviliza potrzebuje składników do przygotowania jej sławnej Nalewki Litworowej.",
            true);
        phase_1();
        phase_2();
    }

    @Override
    public List<String> getHistory(final Player player) {

        final List<String> res = new ArrayList<String>();

        if (!player.hasQuest(QUEST_SLOT)) {
            return res;
        }

        res.add("Zawarłem znajomość z koboldką Wrviliza barmankąw barze w Wo'fol.");

        /**
         * NOTE:
         * Retrieving QUEST_SLOT without sub slot index 0 will break the if/then logic below.
         */
        final String questState = player.getQuest(QUEST_SLOT, 0);
        logger.debug("Quest state: <" + questState + ">");
        
        if ("rejected".equals(questState)) {
            res.add("Poprosiła mnie o pomoc w remoncie jej magazynu na zapasy potrzebne do zrobienia jej Nalewki Litworowej, "
                + " ale mam pilne sprawy do załatwienia.");
        } else if ("done".equals(questState)) {
            res.add("Pomogłem w remoncie jej magazynu na zapasy, z których przygotowuje swój Nalewkę Litworową.");
            if (isRepeatable(player)) {
                // enough time has passed, inform that the quest can be taken now.
                res.add("Może zapytam ją ponownie czy nie potrzebuje więcej rzeczy do jej magazynu na zapasy.");
            } else {
                // inform about how much time has to pass before the quest can be taken again.
                long timestamp;
                try {
                    timestamp = Long.parseLong(player.getQuest(QUEST_SLOT, 1));
                } catch (final NumberFormatException e) {
                    timestamp = 0;
                }
                final long timeRemaining = timestamp - System.currentTimeMillis();
                res.add("Jej magazyn na zapasy będzie zaopatrzony na " + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
            }
        } else {
            final ItemCollection missingItems = new ItemCollection();
            /**
             * NOTE:
             * The quest is running,
             * QUEST_SLOT sub slot 0 holds a semicolon separated list of key=value token pairs.
             * Do not use player.getQuest(QUEST_SLOT, 0) as that would only retrieve the first token pair.
             */
            missingItems.addFromQuestStateString(player.getQuest(QUEST_SLOT));
            res.add("Pomagam jej w uzupełnieniu magazynu w zapasy potrzebne do sporządzenia Nalewki Litworowej."
                + " Wciąż mam do dostarczenia jej " + Grammar.enumerateCollection(missingItems.toStringListWithHash()));
        }

        return res;
    }
 
    @Override
    public String getSlotName() {
        return QUEST_SLOT;
    }
 
    @Override
        public String getName() {
        return "KoboldishTorcibud";
    }

    // Not for kids but not too hard to undertake either.
    @Override
        public int getMinLevel() {
        return 18;
    }
    
	@Override
	public String getRegion() {
		return Region.SEMOS_MINES;
	}

	@Override
	public String getNPCName() {
		return "Wrviliza";
	}

    @Override
    public boolean isRepeatable(final Player player) {
        return new AndCondition(
            new QuestCompletedCondition(QUEST_SLOT),
            new TimeReachedCondition(QUEST_SLOT, 1)).fire(player,null, null);
    }

    @Override
    public boolean isCompleted(final Player player) {
        return new QuestCompletedCondition(QUEST_SLOT).fire(player, null, null);
    }

    /**
     * Returns the items to collect and required quantity
     *
     * @param pLevel The level of the player undertaking the quest, affects the required quantity of some items.
     * @return A string composed of semi-colon separated key=value token pairs.
     */
    private String getRequiredItemsCollection(int pLevel) {

        int FACTOR_BOTTLE_SLIM = 60;
        int FACTOR_BOTTLE_EARED = 80;
		int FACTOR_MANDRAGORA = 100;

        int required_bottle_eared = Rand.roll1D6() + pLevel / FACTOR_BOTTLE_EARED;
        int required_bottle_slim = Rand.roll1D6() + pLevel / FACTOR_BOTTLE_SLIM;
		int required_mandragora = Rand.randUniform(1,3) + pLevel / FACTOR_MANDRAGORA;

        return
            "butla czwórniaczka=" + required_bottle_eared + ";" +
            "wąska butelka=" + required_bottle_slim + ";" +
            "mandragora=" + required_mandragora + ";" +
            "karczoch=" + Rand.roll1D6() + ";" +
            "arandula=" + Rand.roll1D6() + ";" +
            "sclaria=" + Rand.roll1D6() + ";" +
            "kekik=" + Rand.roll1D6() + ";" +
            "ekstrakt litworowy=" + Rand.roll1D6() + ";";
    }

    /**
     * The player meets the Kobold Barmaid Wrviliza and possibly gets a quest from her
     */
    public void phase_1() {
     
        final SpeakerNPC npc = npcs.get("Wrviliza");

        // Player sends his greetings and never asked for a quest is handled in NPC class

        // Player sends his greetings to Wrviliza and has rejected the quest in the past
        npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
            		new QuestInStateCondition(QUEST_SLOT, "rejected")),
            ConversationStates.QUEST_OFFERED,
            "Hau! Witam ponownie... Wróciłeś, aby mi pomóc w przyniesieniu #składników d zrobienia dobrej #nalewki?",
            null);

        // Player asks for a quest
        npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.QUEST_OFFERED,
            "Hau! Mój magazyn z zapasami do przygotowania litworowej #nalewki kurczy się. Czy chcesz mi pomóc z uzupełnieniem #składników?",
            null
        );

        // Player has previously done the quest, enough time has passed since completing it
        npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(
                new QuestCompletedCondition(QUEST_SLOT),
                new TimeReachedCondition(QUEST_SLOT, 1)),
            ConversationStates.QUEST_OFFERED,
            "Hau! W rzeczy samej/ Potrzebuję paru #składników do zrobienia litworowej #nalewki. Pomożesz mi?",
            null);

        // Player has done the quest already but it's too early to get another
        npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(
                new QuestCompletedCondition(QUEST_SLOT),
                new NotCondition(new TimeReachedCondition(QUEST_SLOT, 1))),
            ConversationStates.ATTENDING, null,
            new SayTimeRemainingUntilTimeReachedAction(QUEST_SLOT, 1,
                "Hau! Dziękuję mój magazyn z zapasami do zrobienia dobrej #nalewki wystarczy na"));

        // Player is curious about torcibud when offered the quest
        npc.add(ConversationStates.QUEST_OFFERED,
            "nalewki",
            new QuestNotStartedCondition(QUEST_SLOT),
            ConversationStates.QUEST_OFFERED,
            "Hau. Zrobię jeszcze jedną, gdy będę mieć wystaczająco dużo #składników!",
            null);

        // Player is curious about stuff, ingredients or supplies when offered the quest
        // NOTE:
        // the answer given by the NPC does not contain any trigger words on pourpose
        // as it should only hint what's kind of things are needed.
        // some details about the required items are given only once the quest has been accepted.
        npc.add(ConversationStates.QUEST_OFFERED,
            Arrays.asList("stuff","ingredients","supplies", "składnik", "składników", "składnikami"),
            new OrCondition(
                new QuestNotStartedCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, "rejected")),
            ConversationStates.QUEST_OFFERED,
            "Hau! Kilka butelek, karczoch, pare ziół i ekstrakt litworowy... Coś takiego. Pomożesz mi?",
            null);

        // Player accepts the quest and gets to know what Wrviliza needs (switch to phase_2)
        npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.YES_MESSAGES, null,
            ConversationStates.QUESTION_1, null,
            new MultipleActions(
            		new ChatAction() {
            			@Override
            			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
            				int pLevel = player.getLevel();
            				player.setQuest(QUEST_SLOT, getRequiredItemsCollection(pLevel));
            			}
            		},
            		// here we have been careful to say the items from the collection only after the quest slot was set, 
            		// because in this quest, the amounts depend on level, above.
            		new SayRequiredItemsFromCollectionAction(
                        QUEST_SLOT,
                        "Hau! Teraz potrzebuję [items]. Czy przypadkiem nie masz czegoś ze sobą?")));

        // Player is not inclined to comply with the request and has not already rejected it once
        npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            new AndCondition(
                new QuestNotActiveCondition(QUEST_SLOT),
                new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
            ConversationStates.ATTENDING,
            "Hau... Sądzę, że będę musiała się zapytać kogoś pozytywnie nastawionego!",
            new MultipleActions(
            		new SetQuestAction(QUEST_SLOT, "rejected"),
            		new DecreaseKarmaAction(20.0)));

        //Player is not inclined to comply with the request and he has rejected it last time
        //If player wants to buy any beverage here, he should really take the quest now
        //Wrviliza holds a grudge by turning idle again.
        npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, "rejected"),
            ConversationStates.IDLE,
            "Hau... Sądze, że można iść suchym wąwozem, a następnie...",
            new MultipleActions(
            		new SetQuestAction(QUEST_SLOT, "rejected"),
            		new DecreaseKarmaAction(20.0)));
    }

    /**
     * The player meets the Kobold Barmaid Wrviliza after he has started the quest
     */
    public void phase_2() {

        final SpeakerNPC npc = npcs.get("Wrviliza");

        // Player says his greetings to Wrviliza and the quest is running
        npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
            		new QuestActiveCondition(QUEST_SLOT)),
            ConversationStates.QUESTION_1,
            "Hau! Witaj ponownie. Przyniosłeś jakiś #składnik dla mnie?",
            null);

        // Player is curious about artichokes
        npc.add(ConversationStates.ATTENDING,
            Arrays.asList("artichoke","artichokes","karczoch"),
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Hau! Nie tak popularne warzywa,"
                + " ale wiem, że rosną jakieś na farmie"
                + " blisko miasta Semos.",
            null);

        // Player is curious about fierywater
        npc.add(ConversationStates.ATTENDING,
            Arrays.asList("ekstrakt litworowy"),
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Hau! To jest potężny płyn."
                + " Kupuję mój na bazarze w Ados, gdy tylko mam czas na podróż.",
            null);

        // Player is curious about herbs
        npc.add(ConversationStates.ATTENDING,
            Arrays.asList("arandula","sclaria","kekik"),
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Hau! Zioło rośnie na równinach lub w lasach... Łatwo znaleść!",
            null);

        // Player is curious about mandragora
        npc.add(ConversationStates.ATTENDING,
            Arrays.asList("mandragora"),
            new QuestActiveCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Hau! Jedyne co wiem, to że jest rzadkim korzeniem rosnącym w lesie"
                + " szczególnie blisko miejsc nasiąkniętych magią..."
                + " Nie jest łatwo go znaleść!",
            null);

        // Player says stuff to be reminded of what is still missing
        npc.add(ConversationStates.QUESTION_1,
            Arrays.asList("stuff", "składnik"), null,
            ConversationStates.QUESTION_1,
            null,
            new SayRequiredItemsFromCollectionAction(
                QUEST_SLOT,
                "Hau! Wciąż potrzebuję [items]. Przyniosłeś coś?"));

        // Player answers yes when asked if he has brought any items
        npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES, null,
            ConversationStates.QUESTION_1,
            "Dobrze co przyniosłeś?",
            null);
    
        // Player answers no when asked if he has brought any items
        npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            new QuestNotCompletedCondition(QUEST_SLOT),
            ConversationStates.ATTENDING,
            "Hau! Nie spiesz się... nie ma pośpiechu! Co cię tutaj sprowadziło?",
            null);

        // create the ChatAction to reward the player
        ChatAction addRewardAction = new ChatAction() {
            @Override
            public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
                final StackableItem
                    koboldish_torcibud_vsop = (StackableItem)
                        SingletonRepository
                            .getEntityManager().getItem("leżakowana nalewka litworowa");
                final int torcibud_bottles = 1 + Rand.roll1D6();
                koboldish_torcibud_vsop.setQuantity(torcibud_bottles);
                koboldish_torcibud_vsop.setBoundTo(player.getName());
                // vsop torcibud will heal up to 75% of the player's base HP he has when getting rewarded
                // all vsop koboldish torcibud is persistent (set in the xml for this item) so this value will last
                koboldish_torcibud_vsop.put("amount", player.getBaseHP()*75/100);

                //player.equipOrPutOnGround(koboldish_torcibud_vsop);
                //put the rewarded bottles on the counter
                final IRPZone zone = SingletonRepository.getRPWorld().getZone("int_wofol_bar");
                koboldish_torcibud_vsop.setPosition(3, 3);
                zone.add(koboldish_torcibud_vsop);

                npc.say(
                    "Hau! Weź "
                    + Integer.toString(torcibud_bottles)
                    + " butelkę mojej leżakowanej nalewki litworowej z najlepszymi życzeniami ode mnie!");
            }
        };

        // Player collected all the items. grant the XP before handing out the torcibud
        ChatAction completeAction = new MultipleActions(
            new SetQuestAction(QUEST_SLOT, "done"),
            new SetQuestToFutureRandomTimeStampAction(QUEST_SLOT, 1, MIN_DELAY, MAX_DELAY),
            new IncreaseXPAction(XP_REWARD),
            new IncreaseKarmaAction(20),
            addRewardAction);

        // add triggers for the item names
        final ItemCollection items = new ItemCollection();
        items.addFromQuestStateString(REQUIRED_ITEMS_TEMPLATE);
        for (final Map.Entry<String, Integer> item : items.entrySet()) {
            npc.add(ConversationStates.QUESTION_1,
                item.getKey(), null,
                ConversationStates.QUESTION_1, null,
                new CollectRequestedItemsAction(
                		item.getKey(),
                        QUEST_SLOT,
                        "Hau! Masz coś jeszcze?",
                        "Hau! Już mi to przyniosłeś!",
                        completeAction, ConversationStates.ATTENDING));
        }
    }
}
