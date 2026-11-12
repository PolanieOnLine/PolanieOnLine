/* Copyright (C) 2026 - PolanieOnLine;
GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.navigation.NavigationGoal;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Uses the same authored tiles and progress predicates as the playable story. */
public final class TrailNavigationTargets {
  private TrailNavigationTargets() {}

  public static List<NavigationGoal> targets(final Player player) {
    final Map<Step, List<NavigationGoal>> goals = new EnumMap<>(Step.class);
    for (Step step : Step.values()) {
      if (active(player, step)) goals.put(step, new ArrayList<>());
    }
    earlyClues(player, goals);
    for (MineTrailEvidence.Kind k : MineTrailEvidence.Kind.values())
      if (MineTrailEvidence.needed(player, k))
        add(player, goals, k.step, MineTrailProgress.ZONE, k.x, k.y, k.name);
    for (AdosKeepSites.Kind k : AdosKeepSites.Kind.values())
      if (AdosKeepSites.needed(player, k)) add(player, goals, k.step, k.zone, k.x, k.y, k.name);
    for (OathTrailSites.Kind k : OathTrailSites.Kind.values()) {
      if (!OathTrailSites.needed(player, k)) continue;
      Step step;
      switch (k) {
        case SHIELD:
        case NAMES:
          step = Step.OATH_NAMES;
          break;
        case GRAVE:
          step = active(player, Step.OATH_BURIAL) ? Step.OATH_BURIAL : Step.OATH_GRAVE;
          break;
        case ORDER:
          step = Step.OATH_TRUTH;
          break;
        case BANNER:
          step = Step.OATH_BANNER;
          break;
        default:
          step = Step.OATH_BURIAL;
      }
      add(player, goals, step, k.zone, k.x, k.y, k.name);
    }
    for (WarsawFortressSites.Kind k : WarsawFortressSites.Kind.values())
      if (WarsawFortressSites.needed(player, k))
        add(player, goals, k.step, WarsawFortressProgress.ZONE, k.x, k.y, k.name);
    for (CoastTrailSites.Kind k : CoastTrailSites.Kind.values())
      if (CoastTrailSites.needed(player, k)) add(player, goals, k.step, k.zone, k.x, k.y, k.name);
    for (SaltMineSites.Kind k : SaltMineSites.Kind.values()) {
      if (!SaltMineSites.needed(player, k)) continue;
      for (Step step :
          new Step[] {
            Step.SALT_SHIFT,
            Step.SALT_RESCUE,
            Step.SALT_SAMPLE,
            Step.SALT_SOURCE,
            Step.SALT_CREATURE,
            Step.SALT_RETURN
          }) {
        if (active(player, step)) add(player, goals, step, SaltMineProgress.MINE, k.x, k.y, k.name);
      }
    }
    for (DragonTrailSites.Kind k : DragonTrailSites.Kind.values())
      if (DragonTrailSites.needed(player, k)) add(player, goals, k.step, k.zone, k.x, k.y, k.name);
    final boolean needsAssembly = needsBellAssembly(player);
    for (BellTrailSites.Kind k : BellTrailSites.Kind.values()) {
      // The upper bell tile is decorative; both tiles perform the same interaction.
      if (k == BellTrailSites.Kind.BELL_TOP || !BellTrailSites.needed(player, k)) continue;
      if (k == BellTrailSites.Kind.ASSEMBLY && !needsAssembly) continue;
      if (needsAssembly
          && (k == BellTrailSites.Kind.FIRST_STOP
              || k == BellTrailSites.Kind.BRIDGE_STOP
              || k == BellTrailSites.Kind.SAFE_CAMP)) continue;
      Step step =
          k == BellTrailSites.Kind.LOG || k == BellTrailSites.Kind.INSCRIPTION
              ? Step.BELL_VOICE
              : k == BellTrailSites.Kind.BELL
                  ? (active(player, Step.BELL_GUARD) ? Step.BELL_GUARD : Step.BELL_VOICE)
                  : k == BellTrailSites.Kind.WEST_WARD || k == BellTrailSites.Kind.EAST_WARD
                      ? Step.BELL_GUARD
                      : Step.BELL_ESCAPE;
      add(player, goals, step, BellTrailProgress.ISLANDS, k.x, k.y, k.name);
    }
    encounters(player, goals);
    dungeon(player, goals);
    for (Step step : Step.values()) {
      if (goals.containsKey(step) && goals.get(step).isEmpty()) npcGoals(player, goals, step);
    }
    final List<NavigationGoal> result = new ArrayList<>();
    for (List<NavigationGoal> list : goals.values()) result.addAll(list);
    return result;
  }

  private static boolean active(Player player, Step step) {
    String state = player.getQuest(step.slot, 0);
    return state != null && !"done".equals(state) && !"rejected".equals(state);
  }

  private static boolean needsBellAssembly(Player player) {
    return BellTrailSites.needed(player, BellTrailSites.Kind.ASSEMBLY)
        && BellTrailEscort.group(player).size() != 2;
  }

  private static void add(
      Player player,
      Map<Step, List<NavigationGoal>> goals,
      Step step,
      String zone,
      int x,
      int y,
      String label) {
    if (goals.containsKey(step))
      goals
          .get(step)
          .add(new NavigationGoal(zone, x, y, label, step.slot + ":" + player.getQuest(step.slot)));
  }

  private static void npc(
      Player player, Map<Step, List<NavigationGoal>> goals, Step step, String name) {
    SpeakerNPC npc = SingletonRepository.getNPCList().get(name);
    if (npc == null && "Drilenun".equals(name))
      npc = SingletonRepository.getNPCList().get("Lieutenant Drilenun");
    if (npc != null && npc.getZone() != null)
      add(player, goals, step, npc.getZone().getName(), npc.getX(), npc.getY(), name);
  }

  private static void earlyClues(Player p, Map<Step, List<NavigationGoal>> goals) {
    for (TrailEvidence.Kind k : TrailEvidence.Kind.values()) {
      if (!TrailEvidence.needed(p, k)) continue;
      switch (k) {
        case SHIPMENT:
          add(p, goals, k.step, "int_zakopane_shop", 7, 10, k.title);
          break;
        case REGISTRY:
          add(p, goals, k.step, "int_zakopane_townhall", 17, 33, k.title);
          break;
        case TRACKS:
          add(p, goals, k.step, TrailQuestLine.FOREST, 82, 62, k.title);
          break;
        case DOCUMENTS:
          add(p, goals, k.step, TrailQuestLine.FOREST, 60, 61, k.title);
          break;
        case SEMOS_PAYMENT:
          add(p, goals, k.step, "int_semos_bank", 11, 24, k.title);
          break;
        case SEMOS_ARCHIVE:
          add(p, goals, k.step, "int_semos_library", 3, 26, k.title);
          break;
        case SEMOS_CRATE:
          add(p, goals, k.step, "0_semos_city", 46, 40, k.title);
          break;
        default:
          break;
      }
    }
    if (p.isQuestInState(Step.SEAL.slot, "guard"))
      add(
          p,
          goals,
          Step.SEAL,
          TrailQuestLine.FOREST,
          TrailQuestLine.STOCK_X,
          TrailQuestLine.STOCK_Y,
          TrailQuestLine.GUARD);
    if (p.isQuestInState(Step.MINE_DISPATCH.slot, "guard"))
      add(
          p,
          goals,
          Step.MINE_DISPATCH,
          MineTrailProgress.ZONE,
          MineTrailProgress.COURIER_X,
          MineTrailProgress.COURIER_Y,
          MineTrailProgress.COURIER);
    if (p.isQuestInState(Step.MOUNTAIN_CARGO.slot, "cargo"))
      add(p, goals, Step.MOUNTAIN_CARGO, TrailQuestLine.FOREST, 90, 65, "skrzynia z bronią");
  }

  private static void encounters(Player p, Map<Step, List<NavigationGoal>> goals) {
    for (MountainTrailProgress.Encounter e : MountainTrailProgress.Encounter.values())
      if (MountainTrailProgress.needs(p, e))
        add(p, goals, e.step, TrailQuestLine.FOREST, e.x, e.y, e.creature);
    if (AdosKeepProgress.needsGuardian(p))
      add(
          p,
          goals,
          Step.KEEP_GUARDIAN,
          AdosKeepProgress.KEEP,
          AdosKeepProgress.GUARDIAN_X,
          AdosKeepProgress.GUARDIAN_Y,
          AdosKeepProgress.GUARDIAN);
    for (OathTrailProgress.Encounter e : OathTrailProgress.Encounter.values())
      if (OathTrailProgress.needs(p, e)) add(p, goals, e.step, e.zone, e.x, e.y, e.name);
    for (WarsawFortressProgress.Encounter e : WarsawFortressProgress.Encounter.values())
      if (WarsawFortressProgress.needs(p, e))
        add(p, goals, e.step, WarsawFortressProgress.ZONE, e.x, e.y, e.name);
    for (CoastTrailProgress.Encounter e : CoastTrailProgress.Encounter.values())
      if (CoastTrailProgress.needs(p, e)) add(p, goals, e.step, e.zone, e.x, e.y, e.name);
    for (SaltMineProgress.Encounter e : SaltMineProgress.Encounter.values())
      if (SaltMineProgress.needs(p, e))
        add(p, goals, e.step, SaltMineProgress.MINE, e.x, e.y, e.name);
    for (DragonTrailProgress.Encounter e : DragonTrailProgress.Encounter.values())
      if (DragonTrailProgress.needs(p, e))
        add(p, goals, e.step, DragonTrailProgress.NORTH, e.x, e.y, e.name);
    for (BellTrailProgress.Encounter e : BellTrailProgress.Encounter.values())
      if (BellTrailProgress.needs(p, e)
          && (e.step != Step.BELL_ESCAPE || !needsBellAssembly(p)))
        add(p, goals, e.step, BellTrailProgress.ISLANDS, e.x, e.y, e.name);
  }

  private static void dungeon(Player p, Map<Step, List<NavigationGoal>> goals) {
    String zone =
        p.getZone() != null
                && "Kryjówka Radana".equals(p.getZone().getAttributes().get("readable_name"))
            ? p.getZone().getName()
            : null;
    if (active(p, Step.RADAN_PASSAGE))
      add(
          p,
          goals,
          Step.RADAN_PASSAGE,
          RadanDungeonSite.PUBLIC_ZONE,
          p.isQuestInState(Step.RADAN_PASSAGE.slot, "search")
              ? RadanDungeonSite.ENTRANCE_X - 1
              : RadanDungeonSite.ENTRANCE_X,
          RadanDungeonSite.ENTRANCE_Y,
          "boczny chodnik Radana");
    for (Step step : new Step[] {Step.RADAN_RESCUE, Step.RADAN_TRANSPORT, Step.RADAN_FINALE}) {
      if (!active(p, step) || p.isQuestInState(step.slot, "return")) continue;
      if (zone == null) {
        add(
            p,
            goals,
            step,
            RadanDungeonSite.PUBLIC_ZONE,
            RadanDungeonSite.ENTRANCE_X,
            RadanDungeonSite.ENTRANCE_Y,
            "wejście do kryjówki Radana");
      } else if (step == Step.RADAN_RESCUE) {
        if (p.isQuestInState(step.slot, "rescue")) add(p, goals, step, zone, 3, 6, "Bartek");
        else {
          int mask = RadanDungeonProgress.guards(p);
          if ((mask & 1) == 0) add(p, goals, step, zone, 6, 10, "człowiek Radana");
          if ((mask & 2) == 0) add(p, goals, step, zone, 11, 8, "strażnik więźnia");
        }
      } else if (step == Step.RADAN_TRANSPORT) {
        boolean chain = p.isQuestInState(step.slot, "chain");
        add(p, goals, step, zone, chain ? 25 : 22, 11, chain ? "łańcuch" : "hamulec");
      } else {
        add(
            p,
            goals,
            step,
            zone,
            p.isQuestInState(step.slot, "guard") ? 33 : 36,
            p.isQuestInState(step.slot, "guard") ? 21 : 23,
            p.isQuestInState(step.slot, "guard") ? "zbrojny Radana" : "Radan");
      }
    }
  }

  private static void npcGoals(Player p, Map<Step, List<NavigationGoal>> goals, Step step) {
    String state = p.getQuest(step.slot, 0);
    String name = step.npc;
    switch (step) {
      case DELIVERY:
        name = "Gościrad";
        break;
      case GUEST:
        name = "jagna".equals(state) ? "Jagna" : "Brzezdom";
        break;
      case DEBT:
        if (!"report".equals(state)) {
          int mask = TrailQuestLine.witnesses(p);
          if ((mask & 1) == 0) npc(p, goals, step, "Stasek");
          if ((mask & 2) == 0) npc(p, goals, step, "Jagna");
          return;
        }
        name = "Gazda Wojtek";
        break;
      case SEMOS_LETTER:
        name = "Drilenun";
        break;
      case SEMOS_SIGN:
        name = "Ceryl";
        break;
      case SEMOS_PICKUP:
        name =
            "wood".equals(state)
                ? "Margaret"
                : "return".equals(state) ? "Drilenun" : "Tragarz Olek";
        break;
      case MINE_LEDGER:
      case MINE_TRACKS:
        name = "Barbarus";
        break;
      case MINE_VOICES:
        name = "Zynn Iwuhos";
        break;
      case MINE_DISPATCH:
        name = "identified".equals(state) ? "Phalk" : "Drilenun";
        break;
      case MOUNTAIN_MINERS:
        name = "search".equals(state) ? MountainTrailProgress.MINER : "Barbarus";
        break;
      case MOUNTAIN_THREATS:
        if (!"report".equals(state)) {
          int mask = MountainTrailProgress.mask(p, step);
          if ((mask & 1) == 0) npc(p, goals, step, "Stasek");
          if ((mask & 2) == 0) npc(p, goals, step, "Jagna");
          return;
        }
        name = "Gazda Wojtek";
        break;
      case MOUNTAIN_MEETING:
        name = "rescue".equals(state) ? MountainTrailProgress.DRIVER : "Brzezdom";
        break;
      case MOUNTAIN_CARGO:
        name = "stasek".equals(state) ? "Stasek" : "Drilenun";
        break;
      case MOUNTAIN_RETURN:
        if (!"ready".equals(state)) {
          int mask = MountainTrailProgress.mask(p, step);
          if ((mask & 1) == 0) npc(p, goals, step, "Gazda Wojtek");
          if ((mask & 2) == 0) npc(p, goals, step, "Drilenun");
          if ((mask & 4) == 0) npc(p, goals, step, "Barbarus");
          return;
        }
        name = "Gazda Wojtek";
        break;
      case KEEP_WITNESSES:
        if (!"report".equals(state)) {
          int mask = AdosKeepProgress.mask(p, step);
          if ((mask & 1) == 0) npc(p, goals, step, "Zogfang");
          if ((mask & 2) == 0) npc(p, goals, step, "Hagnurk");
          return;
        }
        name = "Julius";
        break;
      case KEEP_RETURN:
        name = "ready".equals(state) ? "Julius" : "Hagnurk";
        break;
      case OATH_WATCH:
        name = "witness".equals(state) ? OathTrailProgress.SENTRY : "Wagabundus";
        break;
      case OATH_BURIAL:
        name = "release".equals(state) ? OathTrailProgress.SENTRY : "Wagabundus";
        break;
      case WARSAW_GATE:
        name =
            "witness".equals(state) ? WarsawFortressProgress.HEALER : WarsawFortressProgress.MARK;
        break;
      case WARSAW_WITNESSES:
        if (!"report".equals(state)) {
          int mask = WarsawFortressProgress.mask(p, step);
          if ((mask & 1) == 0) npc(p, goals, step, WarsawFortressProgress.SOLDIER);
          if ((mask & 2) == 0) npc(p, goals, step, WarsawFortressProgress.HEALER);
          return;
        }
        name = WarsawFortressProgress.MARK;
        break;
      case WARSAW_TRUCE:
        name =
            "truce".equals(state)
                ? WarsawFortressProgress.SOLDIER
                : "prince".equals(state)
                    ? WarsawFortressProgress.PRINCE
                    : WarsawFortressProgress.MARK;
        break;
      case COAST_WRECK:
        name = "survivor".equals(state) ? CoastTrailProgress.SAILOR : CoastTrailProgress.FERRYMAN;
        break;
      case COAST_CARGO:
        name = "rosa".equals(state) ? CoastTrailProgress.BARMAID : CoastTrailProgress.KNIGHT;
        break;
      case COAST_RETURN:
        name = "lookout".equals(state) ? CoastTrailProgress.SAILOR : CoastTrailProgress.FERRYMAN;
        break;
      case SALT_SHIFT:
        name = SaltMineProgress.FOREMAN;
        break;
      case SALT_RESCUE:
        if ("miners".equals(state)) {
          int mask = SaltMineProgress.mask(p, step, "report");
          if ((mask & 1) == 0) npc(p, goals, step, SaltMineProgress.BOGDAN);
          if ((mask & 2) == 0) npc(p, goals, step, SaltMineProgress.WOJMIR);
          return;
        }
        name = SaltMineProgress.FOREMAN;
        break;
      case SALT_SAMPLE:
        name = SaltMineProgress.SCHOLAR;
        break;
      case SALT_SOURCE:
        name = SaltMineProgress.FOREMAN;
        break;
      case SALT_RETURN:
        name = SaltMineProgress.MAYOR;
        break;
      case EMBER_CAMP:
        name = "witness".equals(state) ? DragonTrailProgress.SURVIVOR : "Racirad";
        break;
      case EMBER_TRANSPORT:
        name = "Robercik";
        break;
      case EMBER_GUIDE:
      case EMBER_EGGS:
        name = DragonTrailProgress.GUIDE;
        break;
      case EMBER_RETURN:
        name = "Racirad";
        break;
      case BELL_RETURNED:
        name = "witness".equals(state) ? BellTrailProgress.WITNESS : "Festris";
        break;
      case BELL_WOUNDED:
        if (!"report".equals(state)) {
          int mask = BellTrailProgress.counter(p, step);
          if ((mask & 1) == 0) npc(p, goals, step, BellTrailProgress.SCHOLAR);
          if ((mask & 2) == 0) npc(p, goals, step, BellTrailProgress.INJURED);
          return;
        }
        name = BellTrailProgress.LEADER;
        break;
      case BELL_ESCAPE:
        name = "return".equals(state) ? "Festris" : BellTrailProgress.LEADER;
        break;
      default:
        break;
    }
    npc(p, goals, step, name);
  }
}
