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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropInfostringItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasInfostringItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerOwnsItemIncludingBankCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.Pair;

/**
 * NOTE: quest slot templates for testing
 * ---------------------------
 * fetch_dessert;inprogress;paella;udko=2,pomidor=3,czosnek=3,pstrąg=1,okoń=1,cebula=2,;gulab;mąka=2,ekstrakt litworowy=2,miód pitny=2,cukier=4,;1337207220454
 * deliver_decentmeal;inprogress;paella;czosnek=2,udko=3,okoń=1,osełka masła=2,cebula=1,pomidor=4,pstrąg=1;macedonia;jabłko=3,gruszka=4,arbuz=2,banan=6;1566438628928
 * done;incomplete;paella;czosnek=2,udko=3,okoń=1,osełka masła=2,cebula=1,pomidor=4,pstrąg=1;macedonia;jabłko=3,gruszka=4,arbuz=2,banan=6;1566439102692;1
 * done;complete;paella;czosnek=2,udko=3,okoń=1,osełka masła=2,cebula=1,pomidor=4,pstrąg=1;macedonia;jabłko=3,gruszka=4,arbuz=2,banan=6;1566439102692;1
 * ---------------------------
 */

/**
 * QUEST: Meal for Groongo, The Troublesome Customer
 * <p>
 * PARTICIPANTS:
 * <ul>
 *  <li> Groongo Rahnnt, The Fado's Hotel Restaurant Troublesome Customer
 *  <li> Stefan, The Fado's Hotel Restaurant Chef
 * </ul>
 *
 * STEPS:
 * <ul>
 *  <li> Groongo is hungry, asks the player to bring him a decent meal,
 *  <li> The player talks to Stefan and he will tell him what ingredients he's missing,
 *  <li> The player goes fetching the ingredients for the main dish,
 *  <li> The player brings Stefan the ingredients he needs,
 *  <li> Stefan tells the player to ask Groongo which dessert he would like along the main dish,
 *  <li> The player checks back with Groongo to ask for a dessert of his choice,
 *  <li> The player tells Stefan which dessert Groongo wants along with the main dish,
 *  <li> Stefan tells the player which ingredients he's missing for preparing the dessert,
 *  <li> The player goes fetching the ingredients for the dessert and brings them to Stefan,
 *  <li> Stefan tells the player how much time (10-15mins) he requires to prepare Groongo's decent meal,
 *  <li> After enough time has elapsed, the player gets the decent meal from Stefan,
 *  <li> The player may deliver the decent meal to Grongo
 *  <li> Groongo is finaly happy and gives the player a reward of some kind, hints player to say 'thanks' to Stefan
 *  <li> The player then has a limited time to get a better reward
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> A 'normal' reward from Groongo,
 * <li> A 'better' reward from Stefan (possibly)
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 *  <li>unlimited, with a delay between attempts
 * </ul>
 *
 * @author omero
 */

public class MealForGroongo extends AbstractQuest {
    private static Logger logger = Logger.getLogger(MealForGroongo.class);
    /**
     * QUEST_SLOT will be used to hold the different states of the quest.
     * Each sub slot will only always serve one purpose as stated below.
     *
     * QUEST_SLOT sub slot 0
     * used to hold the main states, which can be:
     * - rejected, the player has refused to undertake the quest
     * - fetch_maindish, the player is collecting ingredients for main dish
     * - check_dessert, the player needs to ask Groongo which dessert he wants along main dish
     * - tell_dessert, the player has to tell Stefan about Groongo's choice for dessert
     * - fetch_dessert, the player is collecting the ingredients for dessert
     * - prepare_decentmeal, the player has to wait a little before the decent meal is ready
     * - deliver_decentmeal, decent meal for Groongo is ready for delivery
     * - done, the player has completed the quest
     *
     * QUEST_SLOT sub slot 1
     * will hold how the quest was finally ended:
     * - incomplete
     * - complete
     *
     * After the troublesome customer gets delivered his decent meal,
     * the player may check back with Stefan once more and
     * say the trigger word 'thanks' before BRINGTHANKS_DELAY has elapsed.
     *
     * Since we don't want to reuse any of the QUEST_SLOT sub slots,
     * when BRINGTHANKS_DELAY hasn't expired yet,
     * checking QUEST_SLOT sub slot 0 (done) and QUEST_SLOT sub slot 6 (timestamp)
     * or checking any other combination of the quest's sub slots
     * would allow the player to say 'thanks' repeatedly
     * and get 'better reward' multiple times.
     *
     * QUEST_SLOT sub slot 0 gets marked as 'done'
     * as soon as player delivers meal to customer and
     * QUEST_SLOT sub slot 1 gets marked 'incomplete'.
     *
     * If the player checks back with Stefan in time,
     * the QUEST_SLOT sub slot 1 gets marked 'complete'
     * and the quest is finally ended.
     *
     * If the player doesn't check back with Stefan within BRINGTHANKS_DELAY,
     * the 'better reward' from Stefan is lost.
     *
     * QUEST_SLOT sub slot 2
     * - holds main dish short name
     *
     * QUEST_SLOT sub slot 3
     * - holds the ingredients to fetch for the main dish
     *
     * QUEST_SLOT sub slot 4
     * - holds dessert short name
     *
     * QUEST_SLOT sub slot 5
     * - holds the ingredients to fetch for the dessert
     *
     * QUEST_SLOT sub slot 6
     * - when quest is running, holds a timestamp for waiting the preparation of decent meal
     * - when quest is done, holds the timestamp of when last decent meal was delivered
     *
     * QUEST_SLOT sub slot 7
     * - counts how many times a decent meal has been delivered
     */
    public static final String QUEST_SLOT = "meal_for_groongo";

    //How long it takes Chef Stefan to prepare a decent meal (main dish and dessert)
    //private static final int MEALREADY_DELAY = 30;
    //FIXME omero: for testing only
    private static final int MEALREADY_DELAY = 2;

    //How much time the player has to get a better reward
    //private static final int BRINGTHANKS_DELAY = 1;
    //FIXME omero: for testing only
    private static final int BRINGTHANKS_DELAY = 10;

    //Every when the quest can be repeated
    //private static final int REPEATQUEST_DELAY = 1 * MathHelper.MINUTES_IN_ONE_DAY;
    // FIXME omero: for testing only
    private static final int REPEATQUEST_DELAY = 10;

    // how much XP is given as the reward
    private static final int XP_REWARD = 1000;

    // which main dishes Groongo will ask for the quest
    private static final List<String> REQUIRED_MAINDISH =
            Arrays.asList(
                "paella",
                "ciorba",
                "lasagne",
                "schnitzel",
                "consomme",
                "paidakia",
                "couscous",
                "kushari"
            );

    // which desserts Groongo will ask for the quest
    private static final List<String> REQUIRED_DESSERT =
            Arrays.asList(
                "macedonia",
                "brigadeiro",
                "vatrushka",
                "cake",
                "tarte",
                "slagroomtart",
                "kirschtorte",
                "gulab"
            );

    @Override
    public void addToWorld() {
        fillQuestInfo(
            "Jedzenie dla Groongo Rahnnt",
            "Groongo jest głodny i potrzebuje skromny posiłek w restauracji hotelu w Fado.",
            true);
        stageBeginQuest();
        stageCollectIngredientsForMainDish();
        stageCheckForDessert();
        stageCollectIngredientsForDessert();
        stageWaitForMeal();
        stageDeliverMeal();

    }

    @Override
    public List<String> getHistory(final Player player) {

        final List<String> res = new ArrayList<String>();

        if (!player.hasQuest(QUEST_SLOT)) {
            return res;
        }
        final String questState = player.getQuest(QUEST_SLOT, 0);
        logger.warn("Quest state: <" + player.getQuest(QUEST_SLOT) + ">");

        res.add("Spotkałem Groongo Rahnnt w restauracji hotelu w Fado.");

        if ("rejected".equals(questState)) {
            res.add("Poprosił mnie, abym przyniósł mu jego posiłek, "
                + "ale nie byłem zainteresowany taką sprawą.");
        } else if ("done".equals(questState)) {
            res.add(
                "Przyniosłem mu " +
                getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                " jako danie główne i " +
                getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                " na deser."
            );
            if (isRepeatable(player)) {
                // enough time has passed, inform that the quest is available to be taken.
                res.add("Może zapytam go czy chce następny skromny posiłek.");
            } else {
                // inform about how much time has to pass before the quest can be taken again.
                long timestamp;
                try {
                    timestamp = Long.parseLong(player.getQuest(QUEST_SLOT, 6));
                } catch (final NumberFormatException e) {
                    timestamp = 0;
                }
                final long timeBeforeRepeatable = timestamp
                + REPEATQUEST_DELAY * MathHelper.MILLISECONDS_IN_ONE_MINUTE
                - System.currentTimeMillis();
                res.add(
                    "Będzie najedzony przez " +
                    TimeUtil.approxTimeUntil((int) (timeBeforeRepeatable / 1000L)) + ".");
            }
        } else {
            final ItemCollection missingIngredients = new ItemCollection();
            String ingredients = "";
            if ("fetch_maindish".equals(questState)) {
                ingredients = player.getQuest(QUEST_SLOT, 3);
                ingredients = ingredients.replaceAll(",", ";");
                missingIngredients.addFromQuestStateString(ingredients);
                res.add("Groongo zjadłby " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " jako danie główne. Pomagam Chef Stefan znaleść składniki do przygotowania tego." +
                    " Wciąż muszę przynieść " +
                    Grammar.enumerateCollection(missingIngredients.toStringList()) + "."
                );
            } else if ("check_dessert".equals(questState)) {
                res.add("Groongo zjadłby " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " jakod danie główne, a Chef Stefan już je przygotowuje."
                );
                res.add("Muszęteraz zapytać Groongo" +
                    " na, który deser ma ochotę."
                );
            } else if ("tell_dessert".equals(questState)) {
                res.add("Groongo zjadłby " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " jako danie główne."
                );
                res.add("Nie powiem Chef Stefan, że ma jeszcze przygotować " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " na deser."
                );
            } else if ("fetch_dessert".equals(questState)) {
                ingredients = player.getQuest(QUEST_SLOT, 5);
                ingredients = ingredients.replaceAll(",", ";");
                missingIngredients.addFromQuestStateString(ingredients);
                res.add("Groongo zjadłby " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " jako danie główne i " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " na deser."
                );
                res.add("Pomagam Chef Stefan w znalezieniu składników do deseru" +
                    " i wciąż muszę przynieść " +
                    Grammar.enumerateCollection(missingIngredients.toStringList()) + "."
                );
            } else if ("prepare_decentmeal".equals(questState)) {
                res.add("Groongo zjadłby " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " jako danie główne i " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " na deser. Czekam aż Chef Stefan skończy przygotowania."
                );
            } else if ("deliver_decentmeal".equals(questState)) {
                res.add("Groongo zjadłby " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " jako danie główne i " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " na deser. Zamierzam przynieść mu jego skromne jedzenie."
                );
            }
        }

        return res;

    }

    @Override
    public String getSlotName() {
        return QUEST_SLOT;
    }

    @Override
    public String getName() {
        return "MealForGroongo";
    }

    @Override
    public int getMinLevel() {
        // TODO omero: minlevel needs to be adjusted
        return 30;
    }

    @Override
    public String getRegion() {
        return Region.FADO_CITY;
    }

    @Override
    public String getNPCName() {
        return "Groongo Rahnnt";
    }

    @Override
    public boolean isRepeatable(final Player player) {
        return new AndCondition(
            new QuestCompletedCondition(QUEST_SLOT),
            new TimePassedCondition(QUEST_SLOT, 6, REPEATQUEST_DELAY)).fire(player,null, null);
    }

    @Override
    public boolean isCompleted(final Player player) {
        return new QuestCompletedCondition(QUEST_SLOT).fire(player, null, null);
    }

    // Groongo uses this to select a random main dish for the quest
    private String getRequiredMainDish() {
        return REQUIRED_MAINDISH.get(Rand.rand(REQUIRED_MAINDISH.size()));
    }

    // Groongo uses this to select a random dessert for the quest
    private String getRequiredDessert() {
        return REQUIRED_DESSERT.get(Rand.rand(REQUIRED_DESSERT.size()));
    }

    // used by both Groongo and Stefan to build sentences
    // avoids requiring player to type long and complicated fancy main dish names
    private String getRequiredMainDishFancyName(final String requiredMainDish) {
        final Map<String, String> requiredMainDishFancyName = new HashMap<String, String>();

        requiredMainDishFancyName.put("paella", "paella de pescado");
        requiredMainDishFancyName.put("ciorba", "ciorba de burta cu smantena");
        requiredMainDishFancyName.put("lasagne", "lasagne alla bolognese");
        requiredMainDishFancyName.put("schnitzel", "jaegerschnitzel mit pilzen");
        requiredMainDishFancyName.put("consomme", "consomme du jour");
        requiredMainDishFancyName.put("paidakia", "paidakia meh piperi");
        requiredMainDishFancyName.put("couscous", "couscous");
        requiredMainDishFancyName.put("kushari", "kushari");

        return requiredMainDishFancyName.get(requiredMainDish);
    }

    // used by both Groongo and Stefan to build sentences
    // avoids requiring the player to type long and complicated fancy dessert names
    private String getRequiredDessertFancyName(final String requiredDessert) {
        final Map<String, String> requiredDessertFancyName = new HashMap<String, String>();

        requiredDessertFancyName.put("brigadeiro", "brigadeiro a la amparo");
        requiredDessertFancyName.put("macedonia", "macedonia di frutta");
        requiredDessertFancyName.put("vatrushka", "old-fashioned vatrushka with cottage cheese");
        requiredDessertFancyName.put("cake", "classic carrot cake with fluffy cream cheese frosting");
        requiredDessertFancyName.put("tarte", "tarte avec la rhubarbe");
        requiredDessertFancyName.put("slagroomtart", "slagroomtart van der boer");
        requiredDessertFancyName.put("kirschtorte", "schwarzwalder kirschtorte");
        requiredDessertFancyName.put("gulab", "gulab jamun");

        return requiredDessertFancyName.get(requiredDessert);
    }

    // used by Stefan
    /**
     * Returns required ingredients and quantities to collect for preparing the main dish
     *
     * @param requiredMainDish
     * @return A string composed of comma separated key=value token pairs.
     */
    private String getRequiredIngredientsForMainDish(final String requiredMainDish) {

        // All not-yet-existing ingredients commented out for testing purposes
        // All ingredients are temporary for developing purposes, subject to change

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_paella = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_paella.put("cebula", new Pair<Integer, Integer>(3,9));
        requiredIngredients_paella.put("czosnek", new Pair<Integer, Integer>(3,6));
        requiredIngredients_paella.put("pomidor", new Pair<Integer, Integer>(3,9));
        requiredIngredients_paella.put("udko", new Pair<Integer, Integer>(3,9));
        requiredIngredients_paella.put("okoń", new Pair<Integer, Integer>(1,6));
        requiredIngredients_paella.put("pstrąg", new Pair<Integer, Integer>(1,6));
        requiredIngredients_paella.put("osełka masła", new Pair<Integer, Integer>(1,6));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_ciorba = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_ciorba.put("mięso", new Pair<Integer, Integer>(3,9));
        requiredIngredients_ciorba.put("fasola pinto", new Pair<Integer, Integer>(3,9));
        requiredIngredients_ciorba.put("cebula", new Pair<Integer, Integer>(3,9));
        requiredIngredients_ciorba.put("czosnek", new Pair<Integer, Integer>(3,9));
        requiredIngredients_ciorba.put("mleko", new Pair<Integer, Integer>(1,5));
        requiredIngredients_ciorba.put("marchew", new Pair<Integer, Integer>(3,9));
        requiredIngredients_ciorba.put("ocet", new Pair<Integer, Integer>(1,3));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_lasagne = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_lasagne.put("mięso", new Pair<Integer, Integer>(3,9));
        requiredIngredients_lasagne.put("pomidor", new Pair<Integer, Integer>(3,9));
        requiredIngredients_lasagne.put("marchew", new Pair<Integer, Integer>(3,9));
        requiredIngredients_lasagne.put("ser", new Pair<Integer, Integer>(3,9));
        requiredIngredients_lasagne.put("mąka", new Pair<Integer, Integer>(3,9));
        requiredIngredients_lasagne.put("jajo", new Pair<Integer, Integer>(3,9));
        requiredIngredients_lasagne.put("oliwa z oliwek", new Pair<Integer, Integer>(1,5));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_schnitzel = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_schnitzel.put("ziemniaki", new Pair<Integer, Integer>(1,5));
        requiredIngredients_schnitzel.put("borowik", new Pair<Integer, Integer>(3,9));
        requiredIngredients_schnitzel.put("pieczarka", new Pair<Integer, Integer>(3,9));
        requiredIngredients_schnitzel.put("szynka", new Pair<Integer, Integer>(3,9));
        requiredIngredients_schnitzel.put("mięso", new Pair<Integer, Integer>(3,9));
        requiredIngredients_schnitzel.put("mleko", new Pair<Integer, Integer>(1,5));
        requiredIngredients_schnitzel.put("ser", new Pair<Integer, Integer>(3,9));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_consomme = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_consomme.put("cebula", new Pair<Integer, Integer>(3,9));
        requiredIngredients_consomme.put("czosnek", new Pair<Integer, Integer>(3,9));
        requiredIngredients_consomme.put("marchew", new Pair<Integer, Integer>(3,9));
        requiredIngredients_consomme.put("udko", new Pair<Integer, Integer>(3,9));
        requiredIngredients_consomme.put("mięso", new Pair<Integer, Integer>(3,9));
        requiredIngredients_consomme.put("sclaria", new Pair<Integer, Integer>(3,9));
        requiredIngredients_consomme.put("kekik", new Pair<Integer, Integer>(3,9));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_paidakia = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_paidakia.put("mięso", new Pair<Integer, Integer>(3,9));
        requiredIngredients_paidakia.put("ocet", new Pair<Integer, Integer>(1,3));
        requiredIngredients_paidakia.put("sclaria", new Pair<Integer, Integer>(3,9));
        requiredIngredients_paidakia.put("oliwa z oliwek", new Pair<Integer, Integer>(1,5));
        requiredIngredients_paidakia.put("papryka habanero", new Pair<Integer, Integer>(1,5));
        requiredIngredients_paidakia.put("kekik", new Pair<Integer, Integer>(3,9));
        requiredIngredients_paidakia.put("cytryna", new Pair<Integer, Integer>(1,5));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_kushari = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_kushari.put("ziemniaki", new Pair<Integer, Integer>(1,5));
        requiredIngredients_kushari.put("fasola pinto", new Pair<Integer, Integer>(3,9));
        requiredIngredients_kushari.put("cebula", new Pair<Integer, Integer>(3,9));
        requiredIngredients_kushari.put("czosnek", new Pair<Integer, Integer>(3,9));
        requiredIngredients_kushari.put("pomidor", new Pair<Integer, Integer>(3,9));
        requiredIngredients_kushari.put("oliwa z oliwek", new Pair<Integer, Integer>(1,5));
        requiredIngredients_kushari.put("papryka habanero", new Pair<Integer, Integer>(1,5));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_couscous = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_couscous.put("mąka", new Pair<Integer, Integer>(3,9));
        requiredIngredients_couscous.put("sok z chmielu", new Pair<Integer, Integer>(3,9));
        requiredIngredients_couscous.put("cukinia", new Pair<Integer, Integer>(3,9));
        requiredIngredients_couscous.put("cebula", new Pair<Integer, Integer>(3,9));
        requiredIngredients_couscous.put("czosnek", new Pair<Integer, Integer>(3,9));
        requiredIngredients_couscous.put("ocet", new Pair<Integer, Integer>(1,3));
        requiredIngredients_couscous.put("papryka habanero", new Pair<Integer, Integer>(1,5));

        //final HashMap<String, HashMap<String, Integer>> requiredIngredientsForMainDish = new HashMap<String, HashMap<String, Integer>>();
        final HashMap<String, HashMap<String, Pair<Integer, Integer>>> requiredIngredientsForMainDish =
                new HashMap<String, HashMap<String, Pair<Integer, Integer>>>();

        requiredIngredientsForMainDish.put("paella", requiredIngredients_paella);
        requiredIngredientsForMainDish.put("ciorba", requiredIngredients_ciorba);
        requiredIngredientsForMainDish.put("lasagne", requiredIngredients_lasagne);
        requiredIngredientsForMainDish.put("schnitzel", requiredIngredients_schnitzel);
        requiredIngredientsForMainDish.put("consomme", requiredIngredients_consomme);
        requiredIngredientsForMainDish.put("paidakia", requiredIngredients_paidakia);
        requiredIngredientsForMainDish.put("couscous", requiredIngredients_couscous);
        requiredIngredientsForMainDish.put("kushari", requiredIngredients_kushari);

        String ingredients = "";
        final HashMap<String, Pair<Integer, Integer>>  requiredIngredients = requiredIngredientsForMainDish.get(requiredMainDish);
        for (final Map.Entry<String, Pair<Integer, Integer>> entry : requiredIngredients.entrySet()) {
            ////logger.warn(" ingredient <" + entry.getKey() + "> quantities <" + entry.getValue() + ">");
            ingredients = ingredients + entry.getKey() + "=" + Rand.randUniform(entry.getValue().first(), entry.getValue().second()) + ",";
        }

        ////logger.warn(" ingredients <" + ingredients + ">");
        // strip the last comma from the returned string
        return ingredients.substring(0, ingredients.length()-1);
    }

    // used by Stefan
    /**
     * Returns required ingredients and quantities to collect for preparing the dessert
     *
     * @param requiredDessert
     * @return A string composed of semicolon separated key=value token pairs.
     */
    private String getRequiredIngredientsForDessert(final String requiredDessert) {
        // All ingredients are temporary for developing purposes, subject to change
        // All not-yet-existing ingredients commented out for testing purposes

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_brigadeiro = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_brigadeiro.put("mleko", new Pair<Integer, Integer>(1,5));
        requiredIngredients_brigadeiro.put("cukier", new Pair<Integer, Integer>(2,4));
        requiredIngredients_brigadeiro.put("osełka masła", new Pair<Integer, Integer>(2,4));
        requiredIngredients_brigadeiro.put("kokos", new Pair<Integer, Integer>(1,2));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_macedonia = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_macedonia.put("banan", new Pair<Integer, Integer>(1,6));
        requiredIngredients_macedonia.put("jabłko", new Pair<Integer, Integer>(3,9));
        requiredIngredients_macedonia.put("gruszka", new Pair<Integer, Integer>(3,9));
        requiredIngredients_macedonia.put("arbuz", new Pair<Integer, Integer>(1,4));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_slagroomtart = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_slagroomtart.put("mleko", new Pair<Integer, Integer>(1,5));
        requiredIngredients_slagroomtart.put("cukier", new Pair<Integer, Integer>(1,2));
        requiredIngredients_slagroomtart.put("jajo", new Pair<Integer, Integer>(3,9));
        requiredIngredients_slagroomtart.put("ananas", new Pair<Integer, Integer>(1,4));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_vatrushka = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_vatrushka.put("mąka", new Pair<Integer, Integer>(3,9));
        requiredIngredients_vatrushka.put("cukier", new Pair<Integer, Integer>(1,4));
        requiredIngredients_vatrushka.put("ser", new Pair<Integer, Integer>(3,9));
        requiredIngredients_vatrushka.put("wisienka", new Pair<Integer, Integer>(1,3));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_cake = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_cake.put("mąka", new Pair<Integer, Integer>(3,9));
        requiredIngredients_cake.put("cukier", new Pair<Integer, Integer>(1,4));
        requiredIngredients_cake.put("ser", new Pair<Integer, Integer>(3,9));
        requiredIngredients_cake.put("marchew", new Pair<Integer, Integer>(3,9));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_tarte = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_tarte.put("mąka", new Pair<Integer, Integer>(3,9));
        requiredIngredients_tarte.put("cukier", new Pair<Integer, Integer>(1,4));
        requiredIngredients_tarte.put("shake czekoladowy", new Pair<Integer, Integer>(1,5));
        requiredIngredients_tarte.put("mandragora", new Pair<Integer, Integer>(1,3));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_kirschtorte = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_kirschtorte.put("mąka", new Pair<Integer, Integer>(3,9));
        requiredIngredients_kirschtorte.put("cukier", new Pair<Integer, Integer>(1,5));
        requiredIngredients_kirschtorte.put("osełka masła", new Pair<Integer, Integer>(1,2));
        requiredIngredients_kirschtorte.put("mleko", new Pair<Integer, Integer>(1,5));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_gulab = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_gulab.put("mąka", new Pair<Integer, Integer>(3,9));
        requiredIngredients_gulab.put("ekstrakt litworowy", new Pair<Integer, Integer>(1,5));
        requiredIngredients_gulab.put("cukier", new Pair<Integer, Integer>(2,6));
        requiredIngredients_gulab.put("miód pitny", new Pair<Integer, Integer>(1,5));

        final HashMap<String, HashMap<String, Pair<Integer, Integer>>> requiredIngredientsForDessert =
                new HashMap<String, HashMap<String, Pair<Integer, Integer>>>();

        requiredIngredientsForDessert.put("brigadeiro", requiredIngredients_brigadeiro);
        requiredIngredientsForDessert.put("macedonia", requiredIngredients_macedonia);
        requiredIngredientsForDessert.put("vatrushka", requiredIngredients_vatrushka);
        requiredIngredientsForDessert.put("cake", requiredIngredients_cake);
        requiredIngredientsForDessert.put("tarte", requiredIngredients_tarte);
        requiredIngredientsForDessert.put("slagroomtart", requiredIngredients_slagroomtart);
        requiredIngredientsForDessert.put("kirschtorte", requiredIngredients_kirschtorte);
        requiredIngredientsForDessert.put("gulab", requiredIngredients_gulab);

        String ingredients = "";
        final HashMap<String, Pair<Integer, Integer>>  requiredIngredients = requiredIngredientsForDessert.get(requiredDessert);
        for (final Map.Entry<String, Pair<Integer, Integer>> entry : requiredIngredients.entrySet()) {
            ////logger.warn(" ingredient <" + entry.getKey() + "> quantities <" + entry.getValue() + ">");
            ingredients = ingredients + entry.getKey() + "=" + Rand.randUniform(entry.getValue().first(), entry.getValue().second()) + ",";
        }

        ////logger.warn(" ingredients <" + ingredients + ">");
        // strip the last comma from the returned string
        return ingredients.substring(0, ingredients.length()-1);

    }

    // Stefan uses this to advance the quest:
    // - after the player has gathered all of required ingredients for the main dish
    // - after the player has asked Groongo which dessert he'd like along the main dish
    // - after the player has gathered all of required ingredients for the dessert
    class advanceQuestInProgressAction implements ChatAction {
        @Override
        public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {
            if ("fetch_maindish".equals(player.getQuest(QUEST_SLOT, 0))) {
                player.setQuest(QUEST_SLOT, 0, "check_dessert");
                SpeakerNPC.say(
                    "Doskonale! Zacznę przygotowywa " +
                    Grammar.article_noun(
                            getRequiredMainDishFancyName(
                                    player.getQuest(QUEST_SLOT, 2)), true) + " natychmiast." +
                    " Tymczasem proszę zapytaj naszego kłopotliwego klienta," +
                    " który deser chciałby sobie zjeść!");
            } else if ("tell_dessert".equals(player.getQuest(QUEST_SLOT, 0))) {
                player.setQuest(QUEST_SLOT, 0, "fetch_dessert");
                SpeakerNPC.say("Rzeczywiście pyszny wybór!");
            } else if ("fetch_dessert".equals(player.getQuest(QUEST_SLOT, 0))) {
                final long timestamp = Long.parseLong(player.getQuest(QUEST_SLOT, 6));
                final long timeToWaitForMealReady = timestamp
                + MEALREADY_DELAY * MathHelper.MILLISECONDS_IN_ONE_MINUTE
                - System.currentTimeMillis();
                player.setQuest(QUEST_SLOT, 0, "prepare_decentmeal");
                SpeakerNPC.say(
                    "Doskonale! Przyzwoity posiłek dla naszego kłopotliwego klienta będzie gotowy za " +
                    TimeUtil.approxTimeUntil((int) (timeToWaitForMealReady / 1000L)) + ".");
            } else if ("prepare_decentmeal".equals(player.getQuest(QUEST_SLOT, 0))) {
                final Item decentMeal = SingletonRepository.getEntityManager().getItem("skromny posiłek");
                final String decentMealDescription =
                        	getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                        " jako danie główne i " +
                        	getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                        " na deser.";
                decentMeal.setInfoString("Skromny posiłek dla Groongo");
                decentMeal.setBoundTo(player.getName());
                decentMeal.setDescription(
                    "Oto pokryty kopułą skromny posiłek, który składa się z " +
                    decentMealDescription);
                if (player.equipToInventoryOnly(decentMeal)) {
                    player.setQuest(QUEST_SLOT, 0, "deliver_decentmeal");
                    SpeakerNPC.say(
                        "Jesteś! Właśnie skończyłem przygotowywać " + decentMealDescription +
                        " Zanieś ten skromny posiłek naszemu kłopotliwemu klientowi!" +
                        " I uważaj, aby go nie zepsuć ani nie upuścić na swojej drodze!"
                    );
                } else {
                    SpeakerNPC.say(
                        "Skromny posiłek dla naszego kłopotliwego klienta jest gotowy do dostarczenia..." +
                        " Dostarcz teraz ten skromny posiłek klientowi i daj mi znać."
                    );
                }
            }
            //logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");
        }
    }

    // Groongo uses this to select one main dish among the defined ones
    // the quest is initiated
    class chooseMainDishAction implements ChatAction {
        @Override
        public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {
            String requiredMainDish = getRequiredMainDish();
            String requiredIngredientsForMainDish = getRequiredIngredientsForMainDish(requiredMainDish);

            int attempts = 0;
            String requiredOldMainDish;
            try {
                requiredOldMainDish = player.getQuest(QUEST_SLOT, 2);
            } catch (final NullPointerException e) {
                requiredOldMainDish = "none";
            }
            while (requiredMainDish.equals(requiredOldMainDish) && attempts <= 5 ) {
                requiredMainDish = getRequiredMainDish();
                requiredIngredientsForMainDish = getRequiredIngredientsForMainDish(requiredMainDish);
                attempts++;
            }

            ////logger.warn("Attempts for new main dish <" + attempts + ">");
            player.setQuest(QUEST_SLOT, 0, "fetch_maindish");
            player.setQuest(QUEST_SLOT, 1, "inprogress");
            player.setQuest(QUEST_SLOT, 2, requiredMainDish);
            player.setQuest(QUEST_SLOT, 3, requiredIngredientsForMainDish);

            SpeakerNPC.say(
                    "Dzisiaj naprawdę mam ochotę spróbować " +
                    getRequiredMainDishFancyName(requiredMainDish) +
                    ". Teraz idź i poproś szefa kuchni Stefana o przygotowanie dla mnie #" + requiredMainDish + ", jeśli możesz..."
            );
            //logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");
        }
    }

    // Groongo uses this to tell the player which dessert he'd like along with the main dish
    // the quest is advanced further
    class chooseDessertAction implements ChatAction {
        @Override
        public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {
            final String requiredMainDish = player.getQuest(QUEST_SLOT, 2);
            String requiredDessert = getRequiredDessert();
            String requiredIngredientsForDessert = getRequiredIngredientsForDessert(requiredDessert);

            int attempts = 0;
            String requiredOldDessert;
            try {
                requiredOldDessert = player.getQuest(QUEST_SLOT, 3);
            } catch (final NullPointerException e) {
                requiredOldDessert = "none";
            }
            while (requiredDessert.equals(requiredOldDessert) && attempts <= 5 ) {
                requiredDessert = getRequiredDessert();
                requiredIngredientsForDessert = getRequiredIngredientsForDessert(requiredDessert);
                attempts++;
            }

            //logger.warn("Attempts for new dessert <" + attempts + ">");
            player.setQuest(QUEST_SLOT, 0, "tell_dessert");
            player.setQuest(QUEST_SLOT, 4, requiredDessert);
            player.setQuest(QUEST_SLOT, 5, requiredIngredientsForDessert);

            SpeakerNPC.say(
                    "Rzeczywiście, nie powinienem zapomnieć poprosić o deser! " +
                    "Z " + getRequiredMainDishFancyName(requiredMainDish) +
                    " spróbuję " + getRequiredDessertFancyName(requiredDessert) +
                    ". Teraz idź i poproś szefa kuchni Stefana o przygotowanie dla mnie " + requiredDessert +
                    ", na deser!"
            );
            //logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");
        }
    }

    // Groongo uses this to remind the player of what he has to bring him
    // depending on which stage the quest currently is
    class checkQuestInProgressAction implements ChatAction {
        @Override
        public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {
            final String questState = player.getQuest(QUEST_SLOT, 0);
            String meal = "";
            String question = "";
            if  (
                    "fetch_maindish".equals(questState)) {
                meal = getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2));
                question = "Przyniosłeś mi to?";
            } else if (
                    "check_dessert".equals(questState)) {
                meal = getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2));
                //question = " Should I also choose some #dessert to go with that?";
                // not a question but a way to give the player a hint about how to 'ask' for which dessert
                question = " Może powinienem również wybrać #deser, aby pójść z tym...";
            } else if (
                    "tell_dessert".equals(questState) ||
                    "fetch_dessert".equals(questState) ||
                    "prepare_decentmeal".equals(questState) ||
                    "deliver_decentmeal".equals(questState)) {
                meal =
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " jako danie główne i " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " na deser";
                question = "Przyniosłeś mi je?";
            }

            SpeakerNPC.say(
                    "Bah! Nadal czekam na " + meal + ". To właśnie nazywam porządnym posiłkiem! " + question
            );
            //logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");
        }
    }

    // Stefan uses this to tell the player what ingredients he needs
    // for preparing the main dish
    class checkIngredientsForMainDishAction implements ChatAction {
        @Override
        public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {
            final ItemCollection missingIngredients = new ItemCollection();
            missingIngredients.addFromQuestStateString(player.getQuest(QUEST_SLOT, 3).replace(",", ";"));

            SpeakerNPC.say(
                    "Ach! Prosi o to nasz kłopotliwy klient " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) + "..." +
                    " Tym razem do tego potrzebuję składników, których w tej chwili brakuje: " +
                    Grammar.enumerateCollection(missingIngredients.toStringList()) + "..." +
                    "Czy przyniesiesz je wszystkie razem?"
            );
            //logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");
        }
    }

    // Stefan uses this to tell the player what ingredients he needs
    // for preparing the dessert
    class checkIngredientsForDessertAction implements ChatAction {
        @Override
        public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {
            final ItemCollection missingIngredients = new ItemCollection();
            missingIngredients.addFromQuestStateString(player.getQuest(QUEST_SLOT, 5).replace(",", ";"));

            SpeakerNPC.say(
                    "Więc! Nasz kłopotliwy klient zdecydował się na to " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " na deser. Do tego potrzebuję kilku innych składników, których mi brakuje: " +
                    Grammar.enumerateCollection(missingIngredients.toStringListWithHash()) +
                    ". Czy przyniesiesz je wszystkie razem?"
            );
            //logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");
        }
    }

    // Stefan uses this
    // when the player has said he has all the ingredients
    // for preparing either the main dish or the dessert
    class collectAllRequestedIngredientsAtOnceAction implements ChatAction {
        private final ChatAction triggerActionOnCompletion;
        private final ConversationStates stateAfterCompletion;

        public collectAllRequestedIngredientsAtOnceAction (
                ChatAction completionAction,
                ConversationStates stateAfterCompletion) {

            this.triggerActionOnCompletion = completionAction;
            this.stateAfterCompletion = stateAfterCompletion;
        }

        @Override
        public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
            final String questState = player.getQuest(QUEST_SLOT, 0);
            ItemCollection missingIngredients = getMissingIngredients(player, questState);
            ItemCollection missingIngredientsToFetch = getMissingIngredients(player, questState);
            boolean playerHasAllIngredients = true;
            // preliminary check. we don't take anything from the player, yet
            for (final Map.Entry<String, Integer> ingredient : missingIngredients.entrySet()) {
                final int amount = player.getNumberOfEquipped(ingredient.getKey());
                if ( amount < ingredient.getValue()) {
                    playerHasAllIngredients = false;
                } else {
                    missingIngredientsToFetch.removeItem(ingredient.getKey(), ingredient.getValue());
                }
            }

            if (playerHasAllIngredients) {
                for (final Map.Entry<String, Integer> ingredient : missingIngredients.entrySet()) {
                    player.drop(ingredient.getKey(), ingredient.getValue());
                }
                triggerActionOnCompletion.fire(player, sentence, raiser);
                raiser.setCurrentState(this.stateAfterCompletion);
            } else {
                raiser.say(
                    "Uh oh! Do przygotowania " + getWhatToPrepare(player, questState) +
                    " potrzebuję " + Grammar.enumerateCollection(missingIngredientsToFetch.toStringListWithHash()) +
                    " Proszę, przynieś mi wszystkie te składniki naraz, jak już prosiłem!"
                );
            }

            //logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");
        }

        String getWhatToPrepare(final Player player, final String questState) {
            String res = "";
            if  ("fetch_maindish".equals(questState)) {
                res =  getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2));
            } else if ("fetch_dessert".equals(questState)) {
                res =  getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4));
            }

            return res;

        }

        ItemCollection getMissingIngredients(final Player player, final String questState) {
            final ItemCollection missingIngredients = new ItemCollection();

            String requiredIngredients = "";
            if  ("fetch_maindish".equals(questState)) {
                requiredIngredients = player.getQuest(QUEST_SLOT, 3);
            } else if ("fetch_dessert".equals(questState)) {
                requiredIngredients = player.getQuest(QUEST_SLOT, 5);
            }

            requiredIngredients = requiredIngredients.replaceAll(",", ";");
            missingIngredients.addFromQuestStateString(requiredIngredients);

            return missingIngredients;
        }
    }

    // The quest is started or rejected by first interacting with Groongo Rahnnt
    public void stageBeginQuest() {
        final SpeakerNPC npc = npcs.get("Groongo Rahnnt");

        // Player greets Groongo,
        // quest has been rejected in the past
        npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "rejected")),
            ConversationStates.QUEST_OFFERED,
            "Gah! Oto stoję, pokryty pajęczynami... " +
            "Oczekiwanie na skromny posiłek ... Przez eony! " +
            "Pozwól, że cię zapytam ... Czy możesz przynieść mi teraz skromny posiłek?",
            null
        );

        // Player has done the quest in the past,
        // time enough has elapsed to take the quest again
        npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(
                new QuestCompletedCondition(QUEST_SLOT),
                new TimePassedCondition(QUEST_SLOT, 6, REPEATQUEST_DELAY)),
            ConversationStates.QUEST_OFFERED,
            "Och, wciąż tu jesteś, rozumiem! Przyniosłbyś mi kolejny skromny #'posiłek'?",
            null
        );

        // Player has done the quest in the past,
        // not enough time has elapsed to take the quest again
        npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(
                new QuestCompletedCondition(QUEST_SLOT),
                new NotCondition(
                    new TimePassedCondition(QUEST_SLOT, 6, REPEATQUEST_DELAY))),
            ConversationStates.ATTENDING,
            null,
            new SayTimeRemainingAction(QUEST_SLOT, 6, REPEATQUEST_DELAY,
                "Nie jestem już aż tak głodny... przynajmniej teraz...")
        );

        // Player greets Groongo, quest is not running
        // Groongo confronts player with a question
        // Player has YES/NO option
        npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(
                new QuestNotStartedCondition(QUEST_SLOT),
                new NotCondition(
                    new QuestCompletedCondition(QUEST_SLOT))),
            ConversationStates.QUEST_OFFERED,
            "Najwyższy czas!" +
            " Czekałem tu tak długo, że mam pajęczyny pod pachami..." +
            " Przyniesiesz mi teraz skromny #'posiłek'?",
            null
        );

        // Player is curious about meal when offered the quest
        // quest not running yet
        npc.add(ConversationStates.QUEST_OFFERED,
            Arrays.asList("meal", "posiłek"),
            new OrCondition(
                new QuestNotStartedCondition(QUEST_SLOT),
                new QuestCompletedCondition(QUEST_SLOT)),
            ConversationStates.QUEST_OFFERED,
            "Bah! Chcę zjeść skromny posiłek i spróbować czegoś innego niż zupy lub ciasta!" +
            " Przyniesiesz mi to, o co zapytam?",
            null
        );

        // Player has just accepted the quest,
        // Player says 'meal' again,
        // give some hints, quest is running
        npc.add(ConversationStates.QUEST_STARTED,
            Arrays.asList("meal", "posiłek"),
            new OrCondition(
                new QuestNotStartedCondition(QUEST_SLOT),
                new QuestCompletedCondition(QUEST_SLOT)),
            ConversationStates.IDLE,
            "Właśnie powiedziałem ci, czego chcę! Teraz idź i powiedz szefowi kuchni, Stefanowi, żeby przygotował mój skromny posiłek!",
            null
        );

        // Player has just accepted the quest,
        // Player says short name of a main dish,
        // give final hints, quest is running
        Iterator<String> i = REQUIRED_MAINDISH.iterator();
        while (i.hasNext()) {
            npc.add(ConversationStates.QUEST_STARTED,
                i.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                ConversationStates.IDLE,
                "Jestem pewien, że szef kuchni, Stefan wie, jak to przygotować, znajdziesz go w kuchni." +
                " Teraz odejdź i nie wracaj bez mojego przyzwoitego posiłku!",
                null
            );
        }
        // Player accepts the quest,
        // quest is started
        npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.YES_MESSAGES,
            new OrCondition(
                new QuestNotStartedCondition(QUEST_SLOT),
                new QuestCompletedCondition(QUEST_SLOT)),
            ConversationStates.QUEST_STARTED,
            null,
            new chooseMainDishAction()
        );

        // Player rejects the quest,
        // has not rejected it last time
        // Groongo turns idle and some Karma is lost.
        npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            new AndCondition(
                new QuestNotActiveCondition(QUEST_SLOT),
                new QuestNotInStateCondition(QUEST_SLOT, 0, "rejected")),
            ConversationStates.IDLE,
            "Przestań mnie męczyć i zgub się w lochu!",
            new MultipleActions(
                new SetQuestAction(QUEST_SLOT, 0, "rejected"),
                new DecreaseKarmaAction(20.0))
        );

        // Player rejects the quest,
        // Player has rejected the quest last time,
        // Groongo turns idle and some (more) Karma is lost.
        npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, 0, "rejected"),
            ConversationStates.IDLE,
            "Odejdź ode mnie i zgub się w lesie!",
            new MultipleActions(
                new SetQuestAction(QUEST_SLOT, 0, "rejected"),
                new DecreaseKarmaAction(20.0))
        );
    }

    // the quest is advanced by next interacting with Stefan
    public void stageCollectIngredientsForMainDish() {
        final SpeakerNPC npc = npcs.get("Stefan");

        npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish")),
            ConversationStates.ATTENDING,
            "Cześć!" +
            " Jestem tak zajęty, że nigdy nie opuszczę tej kuchni..." +
            " Nie mów mi, że muszę teraz przygotować kolejny #posiłek!",
            null
        );

        // Player remembers generic instructions from Groongo,
        // Player says 'meal'
        // Ask if he has the required ingredients
        npc.add(ConversationStates.ATTENDING,
            Arrays.asList("meal", "posiłek"),
            new AndCondition(
                new GreetingMatchesNameCondition(npc.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish")),
            ConversationStates.QUESTION_1,
            null,
            new checkIngredientsForMainDishAction()
        );

        // Player remembers Groongo asked for a specific main dish
        // Player says one of the known REQUIRED_MAINDISH
        // Add all the main dishes trigger words
        Iterator<String> i = REQUIRED_MAINDISH.iterator();
        while (i.hasNext()) {
            npc.add(ConversationStates.ATTENDING,
                i.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                ConversationStates.QUESTION_1,
                null,
                new checkIngredientsForMainDishAction()
            );
        }

        // Player has been asked if he has the ingredients for main dish,
        // Player answers negatively
        npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
            ConversationStates.ATTENDING,
            "Szkoda! Proszę, przynieś mi te wszystkie składniki naraz!",
            null
        );

        // Player has been asked if he has the ingredients for main dish,
        // Player answers affirmatively,
        // the quest is possibly advanced to the next step
        npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            null,
            ConversationStates.IDLE,
            null,
            new collectAllRequestedIngredientsAtOnceAction(
                new advanceQuestInProgressAction(),
                ConversationStates.IDLE)
        );
    }

    // the quest is advanced further by interacting with both Groongo and Stefan again
    public void stageCheckForDessert() {
        final SpeakerNPC npc_chef = npcs.get("Stefan");
        final SpeakerNPC npc_customer = npcs.get("Groongo Rahnnt");

        // Player checks back with Stefan
        // Player doesn't yet know which dessert Groongo would like
        npc_chef.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_chef.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert")),
            ConversationStates.ATTENDING,
            "Och, wróciłeś tak szybko ..." +
            " I nie sprawdziłeś, który #deser" +
            " chciałby mieć nasz kłopotliwy klient!",
            null
        );

        // give some hints about what to do next
        // when the player says dessert
        npc_chef.add(ConversationStates.ATTENDING,
            Arrays.asList("dessert", "deser"),
            new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert"),
            ConversationStates.IDLE,
            "Wiem, jak przygotować kilka rodzajów deserów..." +
            " Lepiej skontaktuj się z naszym kłopotliwym klientem, aby dowiedzieć się, który preferuje!",
            null
        );

        // give some hints about what to do next
        // when the player says one of the main dishes
        Iterator<String> i = REQUIRED_MAINDISH.iterator();
        while (i.hasNext()) {
            npc_chef.add(ConversationStates.ATTENDING,
                i.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert"),
                ConversationStates.ATTENDING,
                "Już to przygotowuję..." +
                " Powinieneś teraz sprawdzić z naszym kłopotliwym castomerem," +
                " jaki #deser chciałby zjeść.",
                null
            );
        }

        // Player knows which dessert Groongo wants
        // quest is running
        npc_chef.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_chef.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert")),
            ConversationStates.ATTENDING,
            "Oto jesteś..." +
            " Nadal zastanawiam się, jaki #deser chciałby nasz kłopotliwy klient" +
            " wraz ze swoim głównym daniem...",
            null
        );

        // Player knows which dessert Groongo wants,
        // Advance the quest
        npc_chef.add(ConversationStates.ATTENDING,
            Arrays.asList("dessert", "deser"),
            new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
            ConversationStates.QUESTION_1,
            null,
            new MultipleActions (
                new advanceQuestInProgressAction(),
                new checkIngredientsForDessertAction()
            )
        );

        // Player knows which dessert Groongo wants,
        // Add all desserts as triggers
        // Advance the quest
        Iterator<String> j = REQUIRED_DESSERT.iterator();
        while (j.hasNext()) {
            npc_chef.add(ConversationStates.ATTENDING,
                j.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
                ConversationStates.QUESTION_1,
                null,
                new MultipleActions (
                    new advanceQuestInProgressAction(),
                    new checkIngredientsForDessertAction()
                )
            );
        }

        // Player checks back with Groongo,
        // quest is running
        npc_customer.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert")),
            ConversationStates.ATTENDING,
            "Ach, już wróciłeś..." +
            " I nadal nie widzę #posiłku, o który prosiłem!",
            null
        );

        // Player says meal to be reminded,
        // will trigger checkQuestInProgressAction()
        // that will give hints about dessert
        // quest is running
        npc_customer.add(
            ConversationStates.ATTENDING,
            Arrays.asList("meal", "posiłek"),
            new AndCondition(
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert")),
            ConversationStates.ATTENDING,
            null,
            new checkQuestInProgressAction()
        );

        // Player says dessert to have Groongo choose which one he'd like
        // Advance the quest
        npc_customer.add(
            ConversationStates.ATTENDING,
            Arrays.asList("dessert", "deser"),
            new AndCondition(
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert")),
            ConversationStates.IDLE,
            null,
            new chooseDessertAction()
        );
    }

    // the quest is advanced further again by interacting with Stefan
    public void stageCollectIngredientsForDessert() {
        final SpeakerNPC npc = npcs.get("Stefan");

        npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert")),
            ConversationStates.ATTENDING,
            "Ach, wróciłeś! Obawiam się, że wciąż brakuje mi składników do przygotowania dobrego #'deseru'...",
            null
        );

        // Player remembers generic instructions from Groongo,
        // Player says 'dessert'
        npc.add(ConversationStates.ATTENDING,
            Arrays.asList("dessert", "deser"),
            new AndCondition(
                new GreetingMatchesNameCondition(npc.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert")),
            ConversationStates.QUESTION_1,
            null,
            new checkIngredientsForDessertAction()
        );

        // Player says one of the defined REQUIRED_MAINDISH
        // when involved in fetching the ingredients for dessert already
        // Add all the main dishes trigger words
        Iterator<String> i = REQUIRED_MAINDISH.iterator();
        while (i.hasNext()) {
            npc.add(ConversationStates.ATTENDING,
                i.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                ConversationStates.ATTENDING,
                "Już to przygotowuję..." +
                " Brakuje mi teraz niektórych składników do przygotowania #deseru dla naszego kłopotliwego klienta!",
                null
            );
        }

        // Player says one of the defined REQUIRED_DESSERT
        // remind him of the ingredients he has to fetch
        // Add all the desserts trigger words
        Iterator<String> j = REQUIRED_DESSERT.iterator();
        while (j.hasNext()) {
            npc.add(ConversationStates.ATTENDING,
                j.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                ConversationStates.QUESTION_1,
                null,
                new checkIngredientsForDessertAction()
            );
        }

        // Player has been asked if he has the ingredients for dessert,
        // Player answers negatively
        npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
            ConversationStates.IDLE,
            "Och, weź je więc szybko! Oczywiście, proszę przynieść mi wszystkie składniki jednocześnie!",
            null
        );

        // Player has been asked if he has the ingredients for dessert,
        // Player answers affirmatively,
        // the quest is possibly advanced to the next step
        npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
            ConversationStates.IDLE,
            null,
            new collectAllRequestedIngredientsAtOnceAction(
                new MultipleActions(
                    // meal will be ready in MEALREADY_DELAY from now
                    new SetQuestToTimeStampAction(QUEST_SLOT, 6),
                    new advanceQuestInProgressAction()
                ),
                ConversationStates.IDLE)
        );

    }

    // the states for interacting with both Groongo and Stefan
    // when the quest has reached its almost final stage
    public void stageWaitForMeal() {
        final SpeakerNPC npc_chef = npcs.get("Stefan");
        final SpeakerNPC npc_customer = npcs.get("Groongo Rahnnt");

        // Player checks back with Stefan when the decent meal is being prepared
        npc_chef.add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_chef.getName()),
                new AndCondition(
                    new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal"),
                    new NotCondition(
                        new TimePassedCondition(QUEST_SLOT, 6, MEALREADY_DELAY)))),
            ConversationStates.IDLE,
            null,
            new SayTimeRemainingAction(QUEST_SLOT, 6, MEALREADY_DELAY,
                "Proszę wróć za chwilę." +
                " Posiłek dla naszego kłopotliwego klienta nie będzie wcześniej gotowy.")
        );

        // Player checks back with Stefan when the decent meal is ready
        npc_chef.add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_chef.getName()),
                new AndCondition(
                    new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal"),
                    new TimePassedCondition(QUEST_SLOT, 6, MEALREADY_DELAY))),
            ConversationStates.IDLE,
            null,
            new advanceQuestInProgressAction()
        );

        // Player says his greetings to Groongo,
        // the quest is running
        npc_customer.add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new OrCondition(
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal"))),
            ConversationStates.QUESTION_1,
            "Jesteś! Czy mój #posiłek jest już gotowy?",
            null
        );

        // Player wants to be reminded
        // add trigger words for both 'meal' and 'dessert'
        npc_customer.add(ConversationStates.QUESTION_1,
            Arrays.asList("meal", "dessert", "posiłek", "deser"),
            new OrCondition(
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal")),
            ConversationStates.QUESTION_1,
            null,
            new checkQuestInProgressAction()
        );

        // Player wants to be reminded
        // add trigger words for each of the short names of a main dish
        Iterator<String> i = REQUIRED_MAINDISH.iterator();
        while (i.hasNext()) {
            npc_customer.add(ConversationStates.QUESTION_1,
                i.next(),
                new OrCondition(
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal")),
                ConversationStates.QUESTION_1,
                null,
                new checkQuestInProgressAction()
            );
        }

        // Player wants to be reminded
        // add trigger words for each of the short names of a dessert
        Iterator<String> j = REQUIRED_DESSERT.iterator();
        while (j.hasNext()) {
            npc_customer.add(ConversationStates.QUESTION_1,
                j.next(),
                new OrCondition(
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal")),
                ConversationStates.QUESTION_1,
                null,
                new checkQuestInProgressAction()
            );
        }

        // Player answers no
        // quest running, not completed yet
        npc_customer.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            new QuestNotCompletedCondition(QUEST_SLOT),
            ConversationStates.IDLE,
            "GAHHH! Wracaj tylko z moim wcześniejszym zamówionym posiłkiem, leniwcu!",
            null
        );

        // Player answers yes (when not having the decent meal with him)
        // quest running
        npc_customer.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(
                new QuestNotCompletedCondition(QUEST_SLOT),
                new NotCondition(new PlayerHasItemWithHimCondition("decent meal"))),
            ConversationStates.IDLE,
            "GAAAH! Kogo próbujesz oszukać?! Wejdź do tej kuchni i wróć z moim posiłkiem, TERAAAAZ!",
            null
        );
    }

    // the states for interacting with both Groongo and Stefan
    // when the quest is in its final stage
    public void stageDeliverMeal() {
        final SpeakerNPC npc_chef = npcs.get("Stefan");
        final SpeakerNPC npc_customer = npcs.get("Groongo Rahnnt");

        /**
         * This is intended to be the better end of the quest.
         * After getting his decent meal, Groongo hints the player
         * to bring his #thanks to Chef Stefan.
         */
        final List<ChatAction> betterEndQuestActions = new LinkedList<ChatAction>();
        betterEndQuestActions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
        betterEndQuestActions.add(new SetQuestAction(QUEST_SLOT, 1, "complete"));
        betterEndQuestActions.add(new IncreaseKarmaAction(10.0));
        betterEndQuestActions.add(
            new ChatAction() {
                @Override
                public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
                    final int amountOfMoneys = Rand.randUniform(1200, 4800);
                    final int amountOfSandwiches = Rand.randUniform(12, 48);
                    final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
                    final StackableItem sandwich = (StackableItem) SingletonRepository.getEntityManager().getItem("kanapka");

                    money.setQuantity(amountOfMoneys);
                    sandwich.setQuantity(amountOfSandwiches);
                    sandwich.setBoundTo(player.getName());
                    sandwich.setDescription("Oto eksperymentalna kanapka przygotowana przez szefa kuchni Stefana.");
                    sandwich.put("amount", player.getBaseHP()/2);
                    sandwich.put("frequency", 10);
                    sandwich.put("regen", 50);
                    sandwich.put("persistent", 1);

                    npc.say("Bardzo dobrze! Ponieważ byłeś mi bardzo pomocny..." +
                        " Proszę przyjąć " +
                        Grammar.thisthese(amountOfSandwiches) + " eksperymentalne kanapki oraz " +
                        Grammar.thisthese(amountOfMoneys) + " " +
                        Grammar.quantityNumberStrNoun(amountOfMoneys, "money") +
                        " w nagrodę!"
                    );

                    player.equipOrPutOnGround(money);
                    player.equipOrPutOnGround(sandwich);

                    //logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");

                }
            }
        );

        npc_chef.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new QuestCompletedCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 1, "incomplete"),
                new NotCondition(
                    new TimePassedCondition(QUEST_SLOT, 6, BRINGTHANKS_DELAY))
            ),
            ConversationStates.QUESTION_1,
            "Więc ... Co nasz kłopotliwy klient powiedział o posiłku?",
            null
        );

        npc_chef.add(ConversationStates.QUESTION_1,
                Arrays.asList("thanks", "dziękuje", "dziękuję", "podziękowanie", "podziękowania", "podziękował"),
                new AndCondition(
                    new QuestCompletedCondition(QUEST_SLOT),
                    new QuestInStateCondition(QUEST_SLOT, 1, "incomplete"),
                    new NotCondition(
                        new TimePassedCondition(QUEST_SLOT, 6, BRINGTHANKS_DELAY))
                ),
                ConversationStates.QUESTION_1,
                //the player gets better reward for his troubles
                "Ach! Dobrze to słyszeć..." +
                " Dziękuję, że pomogłeś mi z tym kłopotliwym klientem...",
                new MultipleActions(betterEndQuestActions)
            );

        // Player says his greetings to Groongo,
        // the quest is running
        npc_customer.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal")),
            ConversationStates.QUESTION_1,
            "Och, wróciłeś! Czy w końcu masz mój #posiłek?",
            null
        );

        // Player says meal to be reminded of what is still missing
        // quest is running
        npc_customer.add(ConversationStates.QUESTION_1,
            Arrays.asList("meal", "dessert", "posiłek", "deser"),
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal")),
            ConversationStates.QUESTION_1,
            null,
            new checkQuestInProgressAction()
        );

        // Player answers no
        // waiting for Stefan?
        npc_customer.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal")),
            ConversationStates.IDLE,
            "Pospiesz się, idź i przynieś to!",
            null);

        /**
         * This is intended to be the normal end of the quest.
         * After getting his decent meal, Groongo hints the player
         * to bring his #thanks to Chef Stefan.
         */
        final List<ChatAction> normalEndQuestActions = new LinkedList<ChatAction>();
        normalEndQuestActions.add(new DropInfostringItemAction("skromny posiłek","Skromny posiłek dla Groongo"));
        normalEndQuestActions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
        normalEndQuestActions.add(new SetQuestAction(QUEST_SLOT, 1, "incomplete"));
        normalEndQuestActions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 6));
        normalEndQuestActions.add(new IncrementQuestAction(QUEST_SLOT, 7, 1));
        normalEndQuestActions.add(new IncreaseXPAction(XP_REWARD));
        normalEndQuestActions.add(new IncreaseKarmaAction(50.0));
        normalEndQuestActions.add(new InflictStatusOnNPCAction("kanapka"));
        normalEndQuestActions.add(
            new ChatAction() {
                @Override
                public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
                    final int amountOfMoneys = Rand.randUniform(1000, 1500);
                    final int amountOfPies = Rand.randUniform(10, 15);
                    final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
                    final StackableItem pie = (StackableItem) SingletonRepository.getEntityManager().getItem("tarta");
                    money.setQuantity(amountOfMoneys);
                    pie.setQuantity(amountOfPies);

                    npc.say("Proszę, weź " +
                        amountOfPies + " tarte oraz " +
                        amountOfMoneys + " money" +
                        " w nagrodę! Przekaż szefowi kuchni, Stefanowi moje specjalne i bardzo zasłużone #podziękowania" +
                        " za przygotowanie mi tak porządnego posiłku! Jestem pewien, że doceni..."
                    );

                    player.equipOrPutOnGround(money);
                    player.equipOrPutOnGround(pie);

                    //logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");
                }
            }
        );

        // Player answers yes and he indeed has the meal for Groongo
        npc_customer.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal"),
                new PlayerHasInfostringItemWithHimCondition("skromny posiłek", "Skromny posiłek dla Groongo")),
            ConversationStates.IDLE,
            null,
            new MultipleActions(normalEndQuestActions)
        );

        // player has lost meal
        npc_chef.add(ConversationStates.ATTENDING,
        	Arrays.asList("meal", "dessert"),
        	new AndCondition(
        		new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal"),
        		new NotCondition(new PlayerOwnsItemIncludingBankCondition("skromny posiłek"))
        	),
        	ConversationStates.QUESTION_2,
        	"Straciłeś posiłek!? Mogę zrobić kolejny, ale będę potrzebował, abyś ponownie przyniósł mi składniki. Czy jesteś na to gotowy?",
        	null
        );

        // player wants another
        npc_chef.add(ConversationStates.QUESTION_2,
        	ConversationPhrases.YES_MESSAGES,
        	new AndCondition(
            		new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal"),
            		new NotCondition(new PlayerOwnsItemIncludingBankCondition("skromny posiłek"))
            	),
        	ConversationStates.QUESTION_1,
        	null,
        	new MultipleActions(
        		new SetQuestAction(QUEST_SLOT, 0, "fetch_maindish"),
        		new checkIngredientsForMainDishAction()
        	)
        );

        // player wants another meal but found decent meal
        npc_chef.add(ConversationStates.QUESTION_2,
        	ConversationPhrases.YES_MESSAGES,
        	new AndCondition(
            		new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal"),
            		new PlayerOwnsItemIncludingBankCondition("skromny posiłek")
            	),
        	ConversationStates.IDLE,
        	"Wygląda na to, że znalazłeś skromny posiłek!",
        	null
        );

        // player does not want another meal
        npc_chef.add(ConversationStates.QUESTION_2,
        	ConversationPhrases.NO_MESSAGES,
       		new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal"),
       		ConversationStates.IDLE,
       		"Och? Przypomniałeś gdzie zostawiłeś posiłek?",
       		null
        );
    }
}
