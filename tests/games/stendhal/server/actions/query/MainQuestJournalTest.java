package games.stendhal.server.actions.query;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import java.lang.reflect.Field;
import java.util.*;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.core.rule.quest.StarterQuestLine;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.maps.quests.*;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import org.junit.Test;
import games.stendhal.common.constants.Events;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;

public class MainQuestJournalTest {
    @BeforeClass public static void classes() { MockStendlRPWorld.get(); PlayerTestHelper.generatePlayerRPClasses(); }
    private final StendhalQuestSystem system=StendhalQuestSystem.get();
    private List<IQuest> loaded;
    private List<IQuest> previous;
    private final Map<String,SpeakerNPC> previousNpcs=new HashMap<>();
    private static final String[] SLOTS={"meet_pietrek","pomoc_adasiowi","scythe_fryderyk","wegiel_na_opal"};

    @Before @SuppressWarnings("unchecked") public void isolateStory() throws Exception {
        MockStendhalRPRuleProcessor.get();
        Field field=StendhalQuestSystem.class.getDeclaredField("quests");field.setAccessible(true);
        loaded=(List<IQuest>)field.get(null);previous=new ArrayList<>(loaded);loaded.clear();
        int id=100;
        for(String name:new String[]{"Pietrek","Adaś","Fryderyk","Stasek"}) {
            previousNpcs.put(name,NPCList.get().remove(name));
            SpeakerNPC npc=new SpeakerNPC(name);npc.setID(new RPObject.ID(id++,"main-quest-test"));
            npc.addGreeting();NPCList.get().add(npc);
        }
        system.loadQuest(new MeetPietrek());
        system.loadQuest(new BuiltQuest(new PomocChlopcowi().story()));
        system.loadQuest(new ScytheForFryderyk());system.loadQuest(new WegielNaOpal());
        system.loadQuest(new AbstractQuest() {
            public String getSlotName(){return "starter_test_side";}
            public String getName(){return "Poboczne testowe";}
            public void addToWorld(){fillQuestInfo(getName(),"Side quest",false);}
            public List<String> getHistory(Player player){return Collections.emptyList();}
        });
    }
    @After public void restoreStory() {
        loaded.clear();loaded.addAll(previous);
        for(Map.Entry<String,SpeakerNPC> entry:previousNpcs.entrySet()) {
            NPCList.get().remove(entry.getKey());if(entry.getValue()!=null)NPCList.get().add(entry.getValue());
        }
    }

    @Test public void freshCharacterSeesFirstObjectiveWithoutAcceptingOrReceivingAnything() {
        Player player=PlayerTestHelper.createPlayer("main-journal-fresh");
        assertEquals(Collections.singletonList("Spotkanie Pietrka"),system.getMainQuests(player));
        assertTrue(system.getQuestProgressDetails(player,"Spotkanie Pietrka").get(0).contains("domku bohaterów"));
        assertTrue(system.getSideQuests(player).isEmpty());
        assertFalse(player.hasQuest("meet_pietrek"));assertEquals(0,player.getTotalNumberOf("puklerz"));
    }

    @Test public void manuscriptDisplayTitlesResolveTheirChapterInTheJournalResponse() {
        Player player=PlayerTestHelper.createPlayer("main-journal-manuscript");player.setQuest("meet_pietrek","done");
        RPAction request=new RPAction();request.put("progress_type","Zadania główne");request.put("item","Pomoc Adasiowi");
        new ProgressStatusQueryAction().onAction(player,request);
        RPEvent result=null;
        for(RPEvent event:player.events()) if(Events.PROGRESS_STATUS_CHANGE.equals(event.getName()))result=event;
        assertNotNull(result);assertTrue(result.get("description").contains(StarterQuestLine.CHAPTER));
        assertTrue(result.getList("data").get(0).contains("Adasiem"));
        assertFalse(player.hasQuest("pomoc_adasiowi"));
    }

    @Test public void alreadyStartedMainTasksStayVisibleAndLegacyOpenListIsPreserved() {
        Player player=PlayerTestHelper.createPlayer("main-journal-partial");
        player.setQuest("pomoc_adasiowi","done;0;1");player.setQuest("scythe_fryderyk","start");
        player.setQuest("starter_test_side","start");
        assertEquals(Arrays.asList("Spotkanie Pietrka","Kosa Fryderyka"),system.getMainQuests(player));
        assertEquals(Collections.singletonList("Poboczne testowe"),system.getSideQuests(player));
        assertTrue(system.getOpenQuests(player).containsAll(Arrays.asList("Kosa Fryderyka","Poboczne testowe")));
    }

    @Test public void completedStagesLeadToNextTaskAndCoalObjectiveUsesActualInventory() {
        Player player=PlayerTestHelper.createPlayer("main-journal-steps");
        player.setQuest(SLOTS[0],"done");assertEquals(Collections.singletonList("Pomoc Adasiowi"),system.getMainQuests(player));
        player.setQuest(SLOTS[1],"done;0;1");assertEquals(Collections.singletonList("Kosa Fryderyka"),system.getMainQuests(player));
        player.setQuest(SLOTS[2],"done");assertEquals(Collections.singletonList("Węgiel na Opał"),system.getMainQuests(player));
        player.setQuest(SLOTS[3],"węgiel=50");
        assertTrue(StarterQuestLine.objective(player,SLOTS[3]).contains("0/50"));
        StackableItem coal=(StackableItem)SingletonRepository.getEntityManager().getItem("węgiel",ItemRarity.COMMON);
        coal.setQuantity(50);player.getSlot("bag").add(coal);
        assertTrue(StarterQuestLine.objective(player,SLOTS[3]).startsWith("Zanieś"));
        assertEquals("węgiel=50",player.getQuest(SLOTS[3]));assertEquals(50,player.getTotalNumberOf("węgiel"));
        player.getSlot("bag").remove(coal.getID());player.getSlot("bank").add(coal);
        assertTrue("Banked items cannot be handed to Stasek",StarterQuestLine.objective(player,SLOTS[3]).contains("0/50"));
    }

    @Test public void oldCompletedCharactersKeepTheirStatesAndReceiveNoRewardsAgain() {
        Player player=PlayerTestHelper.createPlayer("main-journal-old");
        for(String slot:SLOTS)player.setQuest(slot,"done");
        assertTrue(system.getMainQuests(player).isEmpty());assertEquals(4,system.getCompletedQuests(player).size());
        for(String slot:SLOTS) {system.getQuestProgressDetails(player,system.getQuestFromSlot(slot).getName());assertEquals("done",player.getQuest(slot));}
        assertEquals(0,player.getTotalNumberOf("puklerz"));assertEquals(0,player.getTotalNumberOf("buty skórzane"));
    }

    @Test public void rejectedAndCombatProgressGetUsefulCurrentObjectives() {
        Player player=PlayerTestHelper.createPlayer("main-journal-objectives");
        player.setQuest("meet_pietrek","start;szczur,1,0,0,0");
        assertTrue(StarterQuestLine.objective(player,"meet_pietrek").startsWith("Pokonaj"));
        player.incSoloKillCount("szczur");assertTrue(StarterQuestLine.objective(player,"meet_pietrek").startsWith("Wróć"));
        player.setQuest("meet_pietrek","done");player.setQuest("pomoc_adasiowi","rejected");
        assertEquals(Collections.singletonList("Pomoc Adasiowi"),system.getMainQuests(player));
        assertEquals(StarterQuestLine.ADAS,StarterQuestLine.objective(player,"pomoc_adasiowi"));
    }

    @Test public void journalOffersMainAndSideCategories() {
        final Player player=PlayerTestHelper.createPlayer("main-journal-categories");
        new ProgressStatusQueryAction().onAction(player,new RPAction());
        RPEvent result=null;
        for(final RPEvent event:player.events()) if(Events.PROGRESS_STATUS_CHANGE.equals(event.getName()))result=event;
        assertNotNull(result);
        assertEquals("Zadania główne",result.getList("data").get(0));
        assertTrue(result.getList("data").contains("Zadania poboczne"));
        assertTrue(result.getList("data").contains("Ukończone zadania"));
    }
}
