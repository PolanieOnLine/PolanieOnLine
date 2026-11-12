/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.compass;

import static org.junit.Assert.*;

import games.stendhal.common.Level;
import games.stendhal.common.QuestMarker;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.navigation.NavigationGoal;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.ProductionRecipe;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.koscielisko.stones.KowalJacekNPC;
import games.stendhal.server.maps.krakow.woodcutter.CharcoalBurnerNPC;
import games.stendhal.server.maps.quests.compass.CompassProgress.Stage;
import games.stendhal.server.maps.quests.compass.CompassScenes.Clue;
import games.stendhal.server.maps.quests.trail.TrailSceneRegistry;
import games.stendhal.server.maps.zakopane.city.GawedziarzNPC;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

/** One complete playable route plus focused ownership, replay and production checks. */
public class WanderersCompassTest {
  private final List<StendhalRPZone> zones = new ArrayList<>();
  private final Map<String, SpeakerNPC> previousNPCs = new HashMap<>();
  private StendhalRPZone forest, town, charcoal, smith;
  private Player player;
  private WanderersCompassQuest quest;

  @BeforeClass
  public static void init() throws Exception {
    QuestHelper.setUpBeforeClass();
  }

  @Before
  public void setup() {
    for (String name :
        new String[] {"Sabała", "Smolisław", "Kowal Jacek", CompassProgress.WANDERER})
      previousNPCs.put(name, NPCList.get().remove(name));
    forest = zone(CompassProgress.FOREST);
    town = zone("0_zakopane_s");
    charcoal = zone("0_krakow_wisla");
    smith = zone("int_koscielisko_stones_room");
    new GawedziarzNPC().configureZone(town, null);
    new CharcoalBurnerNPC().configureZone(charcoal, null);
    new KowalJacekNPC().configureZone(smith, null);
    quest = new WanderersCompassQuest();
    quest.addToWorld();
    player = PlayerTestHelper.createPlayer("compass-story");
    player.setXP(Level.getXP(30));
    player.setLevel(30);
    town.add(player);
    MockStendhalRPRuleProcessor.get().addPlayer(player);
  }

  private StendhalRPZone zone(String name) {
    StendhalRPZone zone = new StendhalRPZone(name, 128, 128);
    zones.add(zone);
    MockStendlRPWorld.get().addRPZone(zone);
    return zone;
  }

  @After
  public void cleanup() {
    MockStendhalRPRuleProcessor.get().clearPlayers();
    for (StendhalRPZone zone : zones) MockStendlRPWorld.get().removeZone(zone);
    for (String name : previousNPCs.keySet()) {
      NPCList.get().remove(name);
      if (previousNPCs.get(name) != null) NPCList.get().add(previousNPCs.get(name));
    }
  }

  private void move(StendhalRPZone zone) {
    if (player.getZone() != zone) {
      player.getZone().remove(player);
      zone.add(player);
    }
  }

  private void refresh() {
    TrailSceneRegistry.refreshLater(player);
    TurnNotifier turns = TurnNotifier.get();
    int start = Math.max(0, turns.getCurrentTurnForDebugging());
    for (int t = start + 1; t <= start + 3; t++) turns.logic(t);
  }

  private SpeakerNPC visit(String name) {
    SpeakerNPC npc = NPCList.get().get(name);
    if (CompassProgress.WANDERER.equals(name)) {
      move(forest);
      refresh();
      for (Entity e : forest.getEntitiesOfClass(SpeakerNPC.class))
        if (PlayerPrivateScene.isOwnedBy(e, player)
            && CompassProgress.WANDERER.equals(TrailSceneRegistry.speakerName(e)))
          npc = (SpeakerNPC) e;
    }
    move(npc.getZone());
    player.setPosition(npc.getX(), npc.getY() + 1);
    npc.getEngine().setCurrentState(ConversationStates.IDLE);
    assertTrue(npc.getEngine().step(player, "hi"));
    return npc;
  }

  private void say(String name, String text) {
    SpeakerNPC npc = visit(name);
    assertTrue(text, npc.getEngine().step(player, text));
  }

  private void accept() {
    SpeakerNPC npc = visit("Sabała");
    assertTrue(npc.getEngine().step(player, "zadanie"));
    assertTrue(npc.getEngine().step(player, "na manowcach"));
    assertTrue(npc.getEngine().step(player, "tak"));
    assertEquals(Stage.MISSING, CompassProgress.stage(player));
  }

  private void stage(Stage expected) {
    assertEquals(expected, CompassProgress.stage(player));
    assertNotNull(WanderersCompassQuest.target(player));
  }

  private CompassScenes.Prop prop(Clue clue) {
    move(forest);
    refresh();
    for (Entity e : forest.getEntitiesOfClass(CompassScenes.Prop.class))
      if (PlayerPrivateScene.isOwnedBy(e, player) && ((CompassScenes.Prop) e).clue == clue)
        return (CompassScenes.Prop) e;
    throw new AssertionError("Missing personal clue " + clue);
  }

  private void use(Clue clue) {
    CompassScenes.Prop prop = prop(clue);
    player.setPosition(prop.getX(), prop.getY() + 1);
    assertTrue(clue.name(), prop.onUsed(player));
  }

  private void item(String name, int count) {
    games.stendhal.server.entity.item.Item item =
        SingletonRepository.getEntityManager().getItem(name);
    assertNotNull(name, item);
    if (item instanceof StackableItem) ((StackableItem) item).setQuantity(count);
    assertTrue(player.equipToInventoryOnly(item));
  }

  private void kill(int role) {
    move(forest);
    refresh();
    for (Entity e : new ArrayList<Entity>(forest.getEntitiesOfClass(CompassGuard.class)))
      if (PlayerPrivateScene.isOwnedBy(e, player) && e.getX() == CompassGuard.X[role]) {
        CompassGuard guard = (CompassGuard) e;
        player.setPosition(guard.getX(), guard.getY() + 1);
        guard.onDead(player);
        return;
      }
    throw new AssertionError("Missing encounter " + role);
  }

  private void completeEscortStop(Clue clue) {
    CompassScenes.Prop stop = prop(clue);
    player.setPosition(stop.getX(), stop.getY() + 1);
    assertFalse("Player alone cannot complete escort", stop.onUsed(player));
    for (Entity e : forest.getEntitiesOfClass(CompassScenes.Escort.class))
      if (PlayerPrivateScene.isOwnedBy(e, player)) e.setPosition(stop.getX() + 1, stop.getY());
    assertTrue(stop.onUsed(player));
  }

  @Test
  public void completeTwentyStagesUsingRealScenesAndCharcoalProduction() {
    EnumSet<Stage> visited = EnumSet.noneOf(Stage.class);
    accept();
    stage(Stage.MISSING);
    visited.add(CompassProgress.stage(player));
    Clue[] trail = {Clue.TRACKS, Clue.BAG, Clue.MARKS, Clue.NOTES};
    for (Clue clue : trail) {
      use(clue);
      visited.add(CompassProgress.stage(player));
    }
    stage(Stage.JUNCTIONS);
    use(Clue.MOSS);
    use(Clue.STUMP);
    visited.add(CompassProgress.stage(player));
    kill(0);
    kill(1);
    say(CompassProgress.WANDERER, "pomoc");
    stage(Stage.BANDAGE);
    visited.add(Stage.BANDAGE);
    use(Clue.HERBS);
    item("sukno", 1);
    say(CompassProgress.WANDERER, "opatrunek");
    stage(Stage.FIRE);
    visited.add(Stage.FIRE);
    item("polano", 3);
    use(Clue.FIRE);
    stage(Stage.MEAL);
    visited.add(Stage.MEAL);
    item("mięso", 1);
    item("buteleczka wody", 1);
    use(Clue.MEAL);
    stage(Stage.ESCORT);
    visited.add(Stage.ESCORT);
    completeEscortStop(Clue.STOP);
    move(town);
    move(forest);
    refresh();
    for (Entity e : forest.getEntitiesOfClass(CompassScenes.Escort.class))
      if (PlayerPrivateScene.isOwnedBy(e, player)) {
        ((CompassScenes.Escort) e).logic();
        assertEquals(60, e.getX());
      }
    completeEscortStop(Clue.SHELTER);
    stage(Stage.REMAINS);
    visited.add(Stage.REMAINS);
    use(Clue.REMAINS);
    visited.add(CompassProgress.stage(player));
    use(Clue.LETTER);
    visited.add(CompassProgress.stage(player));
    say("Sabała", "list");
    stage(Stage.CHARCOAL);
    visited.add(Stage.CHARCOAL);
    SpeakerNPC burner = visit("Smolisław");
    ProductionRecipe recipe = ProductionRecipe.forNPC(burner).get(0);
    item("polano", 2);
    item("money", 5);
    assertTrue(recipe.start(burner, player, 1));
    assertFalse(player.isEquipped("polano", 1));
    player.setQuest(
        recipe.getQuestSlot(), "1;węgiel drzewny;" + (System.currentTimeMillis() - 31000));
    recipe.collect(burner, player);
    assertTrue(player.isQuestCompleted(recipe.getQuestSlot()));
    item("polano", 2);
    item("money", 5);
    assertTrue(recipe.start(burner, player, 1));
    assertFalse(player.isQuestCompleted(recipe.getQuestSlot()));
    say("Smolisław", "węgiel dla Jacka");
    stage(Stage.NEEDLE);
    visited.add(Stage.NEEDLE);
    item("sztabka żelaza", 1);
    say("Kowal Jacek", "igła do kompasu");
    long due = CompassProgress.time(player);
    assertTrue(due > System.currentTimeMillis());
    say("Kowal Jacek", "igła kompasu");
    assertEquals(due, CompassProgress.time(player));
    CompassProgress.set(player, Stage.NEEDLE, 0, System.currentTimeMillis() - 1);
    say("Kowal Jacek", "igła kompasu");
    stage(Stage.CASE);
    visited.add(Stage.CASE);
    kill(2);
    kill(3);
    use(Clue.CASE);
    visited.add(CompassProgress.stage(player));
    use(Clue.ASSEMBLY);
    visited.add(CompassProgress.stage(player));
    use(Clue.CROSS_MOSS);
    use(Clue.CROSS_STUMP);
    use(Clue.CROSS_WATER);
    stage(Stage.BURIAL);
    visited.add(Stage.BURIAL);
    use(Clue.BURIAL);
    stage(Stage.RETURN);
    visited.add(Stage.RETURN);
    assertEquals(EnumSet.allOf(Stage.class), visited);
    while (player.equipToInventoryOnly(SingletonRepository.getEntityManager().getItem("miecz"))) {
      // Fill the inventory to exercise the real reward guard.
    }
    say("Sabała", "kompas wędrowca");
    stage(Stage.RETURN);
    assertFalse(player.isEquipped(WanderersCompassQuest.REWARD));
    while (player.drop("miecz", 1)) {
      // Free room for the keepsake before collecting the reward.
    }
    int beforeReward = player.getXP();
    say("Sabała", "naprawiony kompas");
    assertTrue(player.isQuestCompleted(WanderersCompassQuest.SLOT));
    assertEquals(beforeReward + 5000, player.getXP());
    assertEquals(1, player.getInt("compass_unlocked"));
    assertTrue(player.isEquipped(WanderersCompassQuest.REWARD));
    assertNull(WanderersCompassQuest.target(player));
    int xp = player.getXP();
    say("Sabała", "kompas wędrowca");
    assertEquals(xp, player.getXP());
    move(forest);
    refresh();
    for (Entity e : forest.getEntitiesOfClass(CompassScenes.Prop.class))
      assertFalse(PlayerPrivateScene.isOwnedBy(e, player));
    assertFalse(quest.getQuestInfo(player).isMainQuest());
  }

  @Test
  public void privateCluesRejectOtherPlayersDistantUseAndLateClicks() {
    accept();
    CompassScenes.Prop clue = prop(Clue.TRACKS);
    Player stranger = PlayerTestHelper.createPlayer("compass-stranger");
    forest.add(stranger);
    stranger.setPosition(clue.getX(), clue.getY() + 1);
    assertFalse(clue.onUsed(stranger));
    stage(Stage.MISSING);
    player.setPosition(1, 1);
    assertFalse(clue.onUsed(player));
    player.setPosition(clue.getX(), clue.getY() + 1);
    assertTrue(clue.onUsed(player));
    stage(Stage.BAG);
    assertFalse(clue.onUsed(player));
    stage(Stage.BAG);
    refresh();
    assertFalse(forest.has(clue.getID()));
  }

  @Test
  public void puzzleErrorsResetProgressAndMissingResourcesNeverConsumePartialCosts() {
    CompassProgress.enter(player, Stage.JUNCTIONS);
    use(Clue.MOSS);
    CompassScenes.Prop wrong = prop(Clue.FALSE);
    player.setPosition(wrong.getX(), wrong.getY() + 1);
    assertFalse(wrong.onUsed(player));
    assertEquals(0, CompassProgress.detail(player));
    CompassProgress.enter(player, Stage.MEAL);
    item("mięso", 1);
    CompassScenes.Prop pot = prop(Clue.MEAL);
    player.setPosition(pot.getX(), pot.getY() + 1);
    assertFalse(pot.onUsed(player));
    assertTrue(player.isEquipped("mięso", 1));
    stage(Stage.MEAL);
    CompassProgress.enter(player, Stage.NEEDLE);
    item("sztabka żelaza", 1);
    say("Kowal Jacek", "igła kompasu");
    assertEquals(0, CompassProgress.time(player));
    assertTrue(player.isEquipped("sztabka żelaza", 1));
    CompassProgress.enter(player, Stage.CHARCOAL);
    item("węgiel drzewny", 1);
    say("Smolisław", "węgiel do kompasu");
    stage(Stage.CHARCOAL);
  }

  @Test
  public void markersFollowTheCurrentSpeakerAndReadiness() {
    SpeakerNPC sabala = NPCList.get().get("Sabała");
    SpeakerNPC burner = NPCList.get().get("Smolisław");
    SpeakerNPC jack = NPCList.get().get("Kowal Jacek");
    assertEquals(QuestMarker.AVAILABLE, quest.getNPCQuestMarker(player, sabala));
    CompassProgress.enter(player, Stage.CHARCOAL);
    assertEquals(QuestMarker.NONE, quest.getNPCQuestMarker(player, sabala));
    assertEquals(QuestMarker.IN_PROGRESS, quest.getNPCQuestMarker(player, burner));
    player.incProducedForItem("węgiel drzewny", 1);
    item("węgiel drzewny", 1);
    assertEquals(QuestMarker.READY, quest.getNPCQuestMarker(player, burner));
    CompassProgress.enter(player, Stage.NEEDLE);
    assertEquals(QuestMarker.IN_PROGRESS, quest.getNPCQuestMarker(player, jack));
    item("sztabka żelaza", 1);
    assertEquals(QuestMarker.READY, quest.getNPCQuestMarker(player, jack));
    CompassProgress.set(player, Stage.NEEDLE, 0, System.currentTimeMillis() + 60000);
    assertEquals(QuestMarker.IN_PROGRESS, quest.getNPCQuestMarker(player, jack));
    CompassProgress.set(player, Stage.NEEDLE, 0, System.currentTimeMillis() - 1);
    assertEquals(QuestMarker.READY, quest.getNPCQuestMarker(player, jack));
    CompassProgress.set(player, Stage.WOLVES, 0, 0);
    SpeakerNPC wounded = visit(CompassProgress.WANDERER);
    assertEquals(QuestMarker.IN_PROGRESS, quest.getNPCQuestMarker(player, wounded));
    CompassProgress.set(player, Stage.WOLVES, 3, 0);
    assertEquals(QuestMarker.READY, quest.getNPCQuestMarker(player, wounded));
    CompassProgress.enter(player, Stage.BANDAGE);
    assertEquals(QuestMarker.IN_PROGRESS, quest.getNPCQuestMarker(player, wounded));
    CompassProgress.set(player, Stage.BANDAGE, 1, 0);
    assertEquals(QuestMarker.IN_PROGRESS, quest.getNPCQuestMarker(player, wounded));
    item("sukno", 1);
    assertEquals(QuestMarker.READY, quest.getNPCQuestMarker(player, wounded));
    CompassProgress.enter(player, Stage.RECOGNITION);
    assertEquals(QuestMarker.READY, quest.getNPCQuestMarker(player, sabala));
    CompassProgress.enter(player, Stage.RETURN);
    assertEquals(QuestMarker.READY, quest.getNPCQuestMarker(player, sabala));
    player.setQuest(WanderersCompassQuest.SLOT, "done");
    assertEquals(QuestMarker.NONE, quest.getNPCQuestMarker(player, sabala));
  }

  @Test
  public void navigationFollowsTheNextWaymarkAndReturnsToFirstAfterAMistake() {
    CompassProgress.enter(player, Stage.CROSSROADS);
    for (Clue clue : new Clue[] {Clue.CROSS_MOSS, Clue.CROSS_STUMP, Clue.CROSS_WATER}) {
      CompassScenes.Prop prop = prop(clue);
      NavigationGoal target = WanderersCompassQuest.target(player);
      assertEquals(prop.getX(), target.x);
      assertEquals(prop.getY(), target.y);
      use(clue);
    }
    stage(Stage.BURIAL);
    CompassProgress.enter(player, Stage.CROSSROADS);
    use(Clue.CROSS_MOSS);
    CompassScenes.Prop wrong = prop(Clue.CROSS_WATER);
    player.setPosition(wrong.getX(), wrong.getY() + 1);
    assertFalse(wrong.onUsed(player));
    CompassScenes.Prop first = prop(Clue.CROSS_MOSS);
    assertEquals(first.getX(), WanderersCompassQuest.target(player).x);
  }

  @Test
  public void levelGateAppliesOnlyAtAcceptance() {
    player.setLevel(14);
    SpeakerNPC sabala = visit("Sabała");
    assertTrue(sabala.getEngine().step(player, "manowce"));
    assertEquals(ConversationStates.ATTENDING, sabala.getEngine().getCurrentState());
    assertFalse(player.hasQuest(WanderersCompassQuest.SLOT));
    player.setLevel(15);
    accept();
    player.setLevel(1);
    use(Clue.TRACKS);
    stage(Stage.BAG);
  }
}
