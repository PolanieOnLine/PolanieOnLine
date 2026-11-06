package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Test;
import games.stendhal.common.Level;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.PrinceSupply;
import games.stendhal.server.maps.quests.KillMadGuards;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import games.stendhal.server.maps.quests.trail.WarsawFortressProgress.Encounter;
import games.stendhal.server.maps.quests.trail.WarsawFortressSites.Kind;
import games.stendhal.server.maps.warszawa.castle.PrinceNPC;
import games.stendhal.server.maps.warszawa.castle.KnightNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import marauroa.common.game.RPObject;

public class WarsawFortressStoryTest {
    private static final String[] NAMES={"Książę","rycerz Mark","Dobrosława","Wszemił"};
    private final Map<String,SpeakerNPC> oldNpcs=new HashMap<>();
    private final StendhalQuestSystem system=StendhalQuestSystem.get();
    private List<IQuest> loaded,previous;
    private StendhalRPZone outside,inside;
    private Player player;
    @BeforeClass public static void init() throws Exception {QuestHelper.setUpBeforeClass();}
    @Before @SuppressWarnings("unchecked") public void setup() throws Exception {
        Field f=StendhalQuestSystem.class.getDeclaredField("quests");f.setAccessible(true);
        loaded=(List<IQuest>)f.get(null);previous=new ArrayList<>(loaded);loaded.clear();
        for(String name:NAMES)oldNpcs.put(name,NPCList.get().remove(name));
        outside=new StendhalRPZone("0_warszawa_e",128,128);inside=new StendhalRPZone(WarsawFortressProgress.ZONE,56,46);
        MockStendlRPWorld.get().addRPZone(outside);MockStendlRPWorld.get().addRPZone(inside);
        new PrinceNPC().configureZone(outside,null);new KnightNPC().configureZone(outside,null);
        new WarsawFortressSites().configureZone(inside,null);
        for(Step step:Step.values())if(step.chapter.equals(TrailQuestLine.WARSAW_CHAPTER))system.loadQuest(new WarsawFortressQuest(step));
        player=PlayerTestHelper.createPlayer("warsaw-story");outside.add(player);MockStendhalRPRuleProcessor.get().addPlayer(player);
    }
    @After public void cleanup() {
        loaded.clear();loaded.addAll(previous);MockStendhalRPRuleProcessor.get().clearPlayers();
        for(String name:NAMES){NPCList.get().remove(name);if(oldNpcs.get(name)!=null)NPCList.get().add(oldNpcs.get(name));}
        MockStendlRPWorld.get().removeZone(outside);MockStendlRPWorld.get().removeZone(inside);
    }
    private void move(StendhalRPZone zone) {if(player.getZone()!=zone){player.getZone().remove(player);zone.add(player);}}
    private String say(String name,String topic) {SpeakerNPC npc=NPCList.get().get(name);assertTrue(name+": "+topic,npc.getEngine().step(player,topic));return getReply(npc);}
    private void visit(String name) {SpeakerNPC npc=NPCList.get().get(name);move(npc.getZone());player.setPosition(npc.getX(),npc.getY()+1);npc.getEngine().setCurrentState(ConversationStates.IDLE);say(name,"hi");}
    private void accept(String name,String topic) {visit(name);say(name,topic);say(name,"tak");}
    private void ready(int level) {player.setQuest("wegiel_na_opal","done");for(Step s:Step.values()){if(s==Step.WARSAW_GATE)break;player.setQuest(s.slot,"done");}player.setXP(Level.getXP(level));player.setLevel(level);}
    private WarsawFortressSites.Evidence evidence(Kind kind) {
        for(Object e:inside.getEntitiesOfClass(WarsawFortressSites.Evidence.class))if(((WarsawFortressSites.Evidence)e).kind==kind)return (WarsawFortressSites.Evidence)e;
        throw new AssertionError(kind);
    }
    private boolean use(Kind kind) {WarsawFortressSites.Evidence e=evidence(kind);move(inside);player.setPosition(e.getX(),e.getY()+1);return e.onUsed(player);}
    private WarsawFortressGuard guard(Encounter role) {
        for(CreatureRespawnPoint source:inside.getRespawnPointList())if(source.getX()==role.x&&source.getY()==role.y) {
            WarsawFortressGuard guard=(WarsawFortressGuard)source.getPrototypeCreature().getNewInstance();guard.setRespawnPoint(source);guard.setPosition(role.x,role.y);inside.add(guard);return guard;
        }
        throw new AssertionError(role);
    }
    private void kill(Encounter role) {move(inside);player.setPosition(role.x-1,role.y);WarsawFortressGuard guard=guard(role);guard.onDamaged(player,guard.getHP()+guard.getArmorHP());guard.onDead(player);}
    private void gate() {accept("rycerz Mark","zanim ruszy szturm");kill(Encounter.GATE);visit("Dobrosława");say("Dobrosława","zanim ruszy szturm");visit("rycerz Mark");say("rycerz Mark","zanim ruszy szturm");}
    @Test public void sixWarsawQuestsContinueAfterTheBurialAtLevel150() {
        int count=0;
        String previous=Step.OATH_BURIAL.slot;
        for (Step step:Step.values()) if ("Pod murami Warszawy".equals(step.chapter)) {
            assertEquals(150,step.minimumLevel);
            assertEquals(previous,step.previousSlot());
            previous=step.slot;
            count++;
        }
        assertEquals(6,count);
        visit("rycerz Mark");say("rycerz Mark","zanim ruszy szturm");assertFalse(player.hasQuest(Step.WARSAW_GATE.slot));
        ready(149);visit("rycerz Mark");say("rycerz Mark","zanim ruszy szturm");assertTrue(system.getMainQuests(player).isEmpty());
        ready(150);accept("rycerz Mark","zanim ruszy szturm");assertEquals("fight",player.getQuest(Step.WARSAW_GATE.slot));
        assertEquals(1,system.getMainQuests(player).size());
    }
    @Test public void wholeChapterCompletesWithPersonalAffixedRewardOnlyOnce() {
        ready(150);int xp=player.getXP();gate();
        accept("Książę","niewypłaconym żołdzie");assertTrue(use(Kind.SUPPLIES));assertTrue(use(Kind.LEDGER));visit("Książę");say("Książę","niewypłacony żołd");
        accept("rycerz Mark","ludzi w środku");visit("Dobrosława");say("Dobrosława","ludzie w środku");visit("Wszemił");say("Wszemił","ludzi w środku");visit("rycerz Mark");say("rycerz Mark","ludzie w środku");
        accept("Dobrosława","beczkach z olejem");assertFalse(use(Kind.OIL));assertTrue(use(Kind.ORDER));assertTrue(use(Kind.OIL));visit("Dobrosława");say("Dobrosława","beczki z olejem");
        accept("Książę","zarządcy Radomirze");assertFalse(use(Kind.PAPERS));kill(Encounter.STEWARD);assertTrue(use(Kind.PAPERS));visit("Książę");say("Książę","zarządca Radomir");
        accept("rycerz Mark","ocalenie załogi");assertFalse(use(Kind.GATE));visit("Wszemił");say("Wszemił","otwartą bramę");assertTrue(use(Kind.GATE));
        visit("rycerz Mark");say("rycerz Mark","ocalenie załogi");assertFalse(player.isQuestCompleted(Step.WARSAW_TRUCE.slot));
        visit("Książę");say("Książę","ocaleniu załogi");visit("rycerz Mark");say("rycerz Mark","ocalenie załogi");
        assertTrue(player.isQuestCompleted(Step.WARSAW_TRUCE.slot));assertEquals(180002,player.getXP()-xp);assertEquals(5000,player.getTotalNumberOf("money"));
        Item item=player.getFirstEquipped("magiczna zbroja płytowa");assertNotNull(item);assertEquals(ItemRarity.RARE,item.getRarity());assertTrue(ItemAffixState.hasAny(item));assertEquals(player.getName(),item.getBoundTo());
        xp=player.getXP();say("rycerz Mark","ocalenie załogi");assertEquals(xp,player.getXP());assertEquals(1,player.getTotalNumberOf("magiczna zbroja płytowa"));assertTrue(system.getMainQuests(player).isEmpty());
    }
    @Test public void evidencePersistsAndCannotBeUsedEarlyFromFarAwayOrForAnotherPlayer() {
        assertFalse(use(Kind.LEDGER));ready(150);gate();accept("Książę","niewypłacony żołd");assertTrue(use(Kind.LEDGER));assertFalse(use(Kind.LEDGER));
        Player restored=new Player(player);inside.remove(player);inside.add(restored);player=restored;assertEquals("clues;1",player.getQuest(Step.WARSAW_PAY.slot));
        player.setPosition(1,1);assertFalse(evidence(Kind.SUPPLIES).onUsed(player));
        Player other=PlayerTestHelper.createPlayer("warsaw-other");inside.add(other);other.setPosition(44,37);assertFalse(evidence(Kind.SUPPLIES).onUsed(other));
        assertTrue(use(Kind.SUPPLIES));assertEquals("report",player.getQuest(Step.WARSAW_PAY.slot));assertEquals(3,WarsawFortressProgress.history(player,Step.WARSAW_PAY).size());
    }
    @Test public void guardiansRequireQuestAndOriginalSpawnAndNeverBecomeDailyTargets() {
        move(inside);player.setPosition(26,37);WarsawFortressGuard guard=guard(Encounter.GATE);assertTrue(guard.getEnemyList().isEmpty());
        for(Encounter role:Encounter.values())assertTrue(SingletonRepository.getEntityManager().getCreature(role.name).isAbnormal());
        ready(150);player.setQuest(Step.WARSAW_GATE.slot,"fight");assertTrue(guard.getEnemyList().contains(player));
        Creature copy=SingletonRepository.getEntityManager().getCreature(Encounter.GATE.name);inside.add(copy);WarsawFortressProgress.recordKill(player,copy);assertEquals("fight",player.getQuest(Step.WARSAW_GATE.slot));
        CreatureRespawnPoint source=guard.getRespawnPoint();guard.setRespawnPoint(null);WarsawFortressProgress.recordKill(player,guard);assertEquals("fight",player.getQuest(Step.WARSAW_GATE.slot));guard.setRespawnPoint(source);
        player.setPosition(1,1);WarsawFortressProgress.recordKill(player,guard);assertEquals("fight",player.getQuest(Step.WARSAW_GATE.slot));
        player.setPosition(26,37);guard.onDamaged(player,guard.getHP()+guard.getArmorHP());guard.onDead(player);assertEquals("witness",player.getQuest(Step.WARSAW_GATE.slot));
        WarsawFortressGuard second=guard(Encounter.GATE);second.setTarget(player);second.logic();assertFalse(second.isAttacking());assertTrue(second.getNewInstance().isAbnormal());
    }
    @Test public void finalRewardSurvivesAFullBagAndRebirth() {
        ready(150);for(Step s:Step.values())if(s.chapter.equals(TrailQuestLine.WARSAW_CHAPTER))player.setQuest(s.slot,"done");player.setQuest(Step.WARSAW_TRUCE.slot,"ready");
        while(player.equip("bag",SingletonRepository.getEntityManager().getItem("miecz"))){}
        int xp=player.getXP();visit("rycerz Mark");say("rycerz Mark","ocalenie załogi");assertEquals("ready",player.getQuest(Step.WARSAW_TRUCE.slot));assertEquals(xp,player.getXP());
        RPObject first=player.getSlot("bag").iterator().next();player.getSlot("bag").remove(first.getID());player.setLevel(0);player.setXP(0);
        say("rycerz Mark","ocalenie załogi");assertTrue(player.isQuestCompleted(Step.WARSAW_TRUCE.slot));assertEquals(1,player.getTotalNumberOf("magiczna zbroja płytowa"));
    }
    @Test public void existingSideQuestOffersKeepTheirOwnConsent() {
        system.loadQuest(new PrinceSupply());system.loadQuest(new KillMadGuards());ready(150);
        visit("Książę");say("Książę","task");say("Książę","nie");assertEquals("rejected",player.getQuest("prince_supply"));
        assertFalse(player.hasQuest(Step.WARSAW_PAY.slot));
        visit("rycerz Mark");say("rycerz Mark","task");say("rycerz Mark","tak");assertTrue(player.isQuestInState("kill_madguards",0,"start"));assertFalse(player.hasQuest(Step.WARSAW_GATE.slot));
        say("rycerz Mark","zanim ruszy szturm");say("rycerz Mark","tak");assertEquals("fight",player.getQuest(Step.WARSAW_GATE.slot));
    }
    @Test public void armorCannotConsumeTheSpaceNeededForTheCoinReward() {
        ready(150);for(Step s:Step.values())if(s.chapter.equals(TrailQuestLine.WARSAW_CHAPTER))player.setQuest(s.slot,"done");player.setQuest(Step.WARSAW_TRUCE.slot,"ready");
        StackableItem coins=(StackableItem)SingletonRepository.getEntityManager().getItem("money");coins.setQuantity(1700);
        while(player.getSlotToEquip(coins)!=null) {
            String slot=player.getSlotToEquip(coins).getName();assertTrue(player.equip(slot,SingletonRepository.getEntityManager().getItem("miecz")));
        }
        RPObject first=player.getSlot("bag").iterator().next();player.getSlot("bag").remove(first.getID());
        int xp=player.getXP();visit("rycerz Mark");say("rycerz Mark","ocalenie załogi");
        assertEquals("ready",player.getQuest(Step.WARSAW_TRUCE.slot));assertEquals(xp,player.getXP());assertEquals(0,player.getTotalNumberOf("magiczna zbroja płytowa"));
        first=player.getSlot("bag").iterator().next();player.getSlot("bag").remove(first.getID());
        say("rycerz Mark","ocalenie załogi");assertTrue(player.isQuestCompleted(Step.WARSAW_TRUCE.slot));
        assertEquals(1700,player.getTotalNumberOf("money"));assertEquals(1,player.getTotalNumberOf("magiczna zbroja płytowa"));
        for(Object e:outside.getEntitiesOfClass(Item.class))assertNotEquals("money",((Item)e).getName());
    }
    @Test public void everySiteHasAnApproachAndBothPortalsAreOnWalkableTiles() throws Exception {
        LayerDefinition collision=TMXLoader.load("data/maps/interiors/warszawa/fortress.tmx").getLayer("collision");collision.build();
        Set<Integer> seen=new HashSet<>();Queue<Integer> queue=new ArrayDeque<>();seen.add(45*56+27);queue.add(45*56+27);
        while(!queue.isEmpty()){int p=queue.remove();for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int x=p%56+d[0],y=p/56+d[1],n=y*56+x;if(x>=0&&x<56&&y>=0&&y<46&&collision.getTileAt(x,y)==0&&seen.add(n))queue.add(n);}}
        for(Kind kind:Kind.values()){boolean reachable=false;for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)reachable|=seen.contains((kind.y+dy)*56+kind.x+dx);assertTrue(kind.name,reachable);}
        for(Encounter role:Encounter.values())assertTrue(role.name,seen.contains(role.y*56+role.x));
        assertTrue(seen.contains(10*56+44));assertTrue(seen.contains(23*56+14));
        collision=TMXLoader.load("data/maps/Level 0/warszawa/e.tmx").getLayer("collision");collision.build();assertEquals(0,collision.getTileAt(75,54));assertEquals(0,collision.getTileAt(75,55));
    }
}
