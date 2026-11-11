/* Copyright (C) 2026 - PolanieOnLine;
GPL version 2 or later. */
package games.stendhal.server.core.navigation;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.entity.mapstuff.portal.AccessCheckingPortal;
import games.stendhal.server.entity.mapstuff.portal.OneWayPortalDestination;
import games.stendhal.server.entity.mapstuff.portal.PasswordPortal;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.compass.WanderersCompassQuest;
import games.stendhal.server.maps.quests.trail.TrailNavigationTargets;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import marauroa.common.game.IRPZone;

/** Optional, private guidance. Never moves players or invokes quest/portal actions. */
public final class CompassService {
  public static final String TARGETS = "compass_targets";
  public static final String UNLOCKED = "compass_unlocked";
  private static final long TOPOLOGY_LIFETIME = 60000L;
  private static final long ROUTE_LIFETIME = 8000L;
  private static long topologyExpires;
  private static Map<String, List<Edge>> topology = Collections.emptyMap();
  private static final Map<Player, Map<String, CachedRoute>> routes = new WeakHashMap<>();

  private CompassService() {}

  public static void update(final Player player) {
    if (player.isQuestCompleted("wanderers_compass")
        && (!player.has(UNLOCKED) || player.getInt(UNLOCKED) != 1)) player.put(UNLOCKED, 1);
    final Map<String, String> next = new LinkedHashMap<>();
    if (player.getZone() != null) next.put("_zone", player.getZone().getName());
    if (player.getZone() != null && player.has(UNLOCKED) && player.getInt(UNLOCKED) == 1) {
      final NavigationGoal side = WanderersCompassQuest.target(player);
      if (side != null) publish(player, next, "side:compass", "side", side);
      List<NavigationGoal> main = TrailNavigationTargets.targets(player);
      addOtherMainQuests(player, main);
      // Local objectives precede remote ones; ties remain deterministic.
      main.sort(
          Comparator.comparing((NavigationGoal g) -> !player.getZone().getName().equals(g.zone))
              .thenComparingDouble(g -> distance(player, g))
              .thenComparing(g -> g.stage)
              .thenComparing(g -> g.label));
      int index = 0;
      for (NavigationGoal goal : main) publish(player, next, "main:" + index++, "main", goal);
    }
    final Map<String, String> previous =
        player.hasMap(TARGETS) ? player.getMap(TARGETS) : Collections.emptyMap();
    if (next.equals(previous)) return;
    for (String key : new ArrayList<>(previous.keySet()))
      if (!next.containsKey(key)) player.remove(TARGETS, key);
    for (Map.Entry<String, String> entry : next.entrySet())
      if (!entry.getValue().equals(previous.get(entry.getKey())))
        player.put(TARGETS, entry.getKey(), entry.getValue());
    player.notifyWorldAboutChanges();
  }

  private static void addOtherMainQuests(Player player, List<NavigationGoal> main) {
    for (String slot : player.getQuests()) {
      IQuest quest = SingletonRepository.getStendhalQuestSystem().getQuestFromSlot(slot);
      if (quest == null
          || quest.getClass().getPackage().getName().endsWith(".trail")
          || !quest.getQuestInfo(player).isMainQuest()
          || !quest.isStarted(player)
          || quest.isCompleted(player)
          || "rejected".equals(player.getQuest(slot, 0))) continue;
      SpeakerNPC npc = SingletonRepository.getNPCList().get(quest.getNPCName());
      if (npc != null && npc.getZone() != null)
        main.add(
            new NavigationGoal(
                npc.getZone().getName(), npc.getX(), npc.getY(), npc.getName(), slot));
    }
  }

  private static double distance(Player player, NavigationGoal goal) {
    return player.getZone().getName().equals(goal.zone)
        ? Math.abs(player.getX() - goal.x) + Math.abs(player.getY() - goal.y)
        : Double.MAX_VALUE;
  }

  private static void publish(
      Player player, Map<String, String> values, String key, String kind, NavigationGoal goal) {
    NavigationGoal local = localTarget(player, goal);
    if (local != null)
      values.put(key, local.x + "\t" + local.y + "\t" + kind + "\t" + sanitize(local.label));
  }

  static String sanitize(String label) {
    return label == null ? "" : label.replaceAll("[\\p{Cntrl}]", " ");
  }

  /** Remote goals are translated to a reachable local path step, never foreign coordinates. */
  public static NavigationGoal localTarget(Player player, NavigationGoal goal) {
    final StendhalRPZone current = player.getZone();
    if (current == null || goal == null) return null;
    if (current.getName().equals(goal.zone)) return goal;
    // The Radan exit is a coordinate Teleporter, not a reference-linked portal.
    if ("Kryjówka Radana".equals(current.getAttributes().get("readable_name")))
      return waypoint(player, goal, 6, 24, true);
    ensureTopology(current.getName(), goal.zone);
    final String key = current.getName() + "\t" + goal.zone + "\t" + goal.x + "," + goal.y;
    Map<String, CachedRoute> cache = routes.computeIfAbsent(player, p -> new HashMap<>());
    CachedRoute cached = cache.get(key);
    if (cached != null
        && cached.expires > System.currentTimeMillis()
        && valid(player, cached.edges)) {
      NavigationGoal local = cached.waypoint(player, goal);
      if (local != null) return local;
    }
    Set<Edge> unreachable = new HashSet<>();
    // Retry when the shortest zone route starts in an isolated room of this map.
    while (true) {
      List<Edge> path = findRoute(player, current.getName(), goal.zone, unreachable);
      if (path.isEmpty()) {
        cache.remove(key);
        return null;
      }
      Edge first = path.get(0);
      CachedRoute candidate = new CachedRoute(path);
      NavigationGoal local = candidate.waypoint(player, goal);
      if (local != null) {
        if (cache.size() > 64) cache.clear();
        cache.put(key, candidate);
        return local;
      }
      unreachable.add(first);
    }
  }

  private static NavigationGoal waypoint(
      Player player, NavigationGoal goal, int x, int y, boolean portal) {
    StendhalRPZone zone = player.getZone();
    if (Math.abs(player.getX() - x) + Math.abs(player.getY() - y) <= (portal ? 1 : 0))
      return new NavigationGoal(zone.getName(), x, y, "Przejście do: " + goal.label, goal.stage);
    // Useable doors can sit in collision. Route to their immediate surroundings.
    Rectangle destination = portal ? new Rectangle(x - 1, y - 1, 3, 3) : new Rectangle(x, y, 1, 1);
    List<Node> path =
        Path.searchPath(
            player,
            zone,
            player.getX(),
            player.getY(),
            destination,
            Math.max(80, (zone.getWidth() + zone.getHeight()) * 4),
            false);
    if (path.isEmpty()) return null;
    return visibleWaypoint(player, goal, path);
  }

  private static NavigationGoal visibleWaypoint(
      Player player, NavigationGoal goal, List<Node> path) {
    for (int i = path.size() - 1; i >= 0; i--) {
      Node node = path.get(i);
      if (!player
          .getZone()
          .collidesOnLine(player.getX(), player.getY(), node.getX(), node.getY())) {
        return new NavigationGoal(
            player.getZone().getName(),
            node.getX(),
            node.getY(),
            "Przejście do: " + goal.label,
            goal.stage);
      }
    }
    return null;
  }

  private static boolean valid(Player player, List<Edge> edges) {
    for (Edge edge : edges) if (!edge.allowed(player)) return false;
    return !edges.isEmpty();
  }

  private static List<Edge> findRoute(
      Player player, String from, String target, Set<Edge> unreachable) {
    ArrayDeque<String> queue = new ArrayDeque<>();
    Map<String, Edge> previous = new HashMap<>();
    queue.add(from);
    previous.put(from, null);
    while (!queue.isEmpty()) {
      String zone = queue.remove();
      for (Edge edge : topology.getOrDefault(zone, Collections.emptyList())) {
        if (unreachable.contains(edge) || previous.containsKey(edge.to) || !edge.allowed(player))
          continue;
        previous.put(edge.to, edge);
        if (target.equals(edge.to)) {
          List<Edge> path = new ArrayList<>();
          for (Edge back = edge; back != null; back = previous.get(back.from)) path.add(back);
          Collections.reverse(path);
          return path;
        }
        queue.add(edge.to);
      }
    }
    return Collections.emptyList();
  }

  /** Cache geometry only. Player access is checked for every chosen route on every update. */
  private static void ensureTopology(String current, String target) {
    if (System.currentTimeMillis() < topologyExpires
        && topology.containsKey(current)
        && topology.containsKey(target)) return;
    List<StendhalRPZone> zones = new ArrayList<>();
    for (IRPZone zone : SingletonRepository.getRPWorld()) zones.add((StendhalRPZone) zone);
    Map<String, List<Edge>> graph = new HashMap<>();
    Map<String, StendhalRPZone> byName = new HashMap<>();
    for (StendhalRPZone zone : zones) {
      graph.put(zone.getName(), new ArrayList<>());
      byName.put(zone.getName(), zone);
    }
    for (StendhalRPZone zone : zones) {
      for (Portal portal : zone.getPortals()) {
        StendhalRPZone destination = byName.get(portal.getDestinationZone());
        if (destination == null
            || !portal.loaded()
            || portal instanceof OneWayPortalDestination
            || portal.isHidden()
            || portal instanceof PasswordPortal) continue;
        graph
            .get(zone.getName())
            .add(
                new Edge(
                    zone.getName(), destination.getName(), portal.getX(), portal.getY(), portal));
      }
    }
    // Exterior zones meet along ordinary world borders, including underground maps.
    for (StendhalRPZone a : zones) {
      if (a.isInterior()) continue;
      for (StendhalRPZone b : zones) {
        if (a == b || b.isInterior() || a.getLevel() != b.getLevel()) continue;
        borders(graph, a, b);
      }
    }
    topology = graph;
    topologyExpires = System.currentTimeMillis() + TOPOLOGY_LIFETIME;
    routes.clear();
  }

  private static void borders(Map<String, List<Edge>> graph, StendhalRPZone a, StendhalRPZone b) {
    boolean east = a.getX() + a.getWidth() == b.getX();
    boolean west = b.getX() + b.getWidth() == a.getX();
    boolean south = a.getY() + a.getHeight() == b.getY();
    boolean north = b.getY() + b.getHeight() == a.getY();
    if (east || west) {
      int start = Math.max(a.getY(), b.getY()),
          end = Math.min(a.getY() + a.getHeight(), b.getY() + b.getHeight());
      int x = east ? a.getWidth() - 1 : 0, bx = east ? 0 : b.getWidth() - 1;
      int run = -1;
      for (int y = start; y <= end; y++) {
        boolean open = y < end && !a.collides(x, y - a.getY()) && !b.collides(bx, y - b.getY());
        if (open && run < 0) run = y;
        if (!open && run >= 0) {
          int midpoint = (run + y - 1) / 2;
          graph
              .get(a.getName())
              .add(
                  new Edge(
                      a.getName(), b.getName(), x, midpoint - a.getY(), null, east ? 1 : -1, 0));
          run = -1;
        }
      }
    }
    if (south || north) {
      int start = Math.max(a.getX(), b.getX()),
          end = Math.min(a.getX() + a.getWidth(), b.getX() + b.getWidth());
      int y = south ? a.getHeight() - 1 : 0, by = south ? 0 : b.getHeight() - 1;
      int run = -1;
      for (int x = start; x <= end; x++) {
        boolean open = x < end && !a.collides(x - a.getX(), y) && !b.collides(x - b.getX(), by);
        if (open && run < 0) run = x;
        if (!open && run >= 0) {
          int midpoint = (run + x - 1) / 2;
          graph
              .get(a.getName())
              .add(
                  new Edge(
                      a.getName(), b.getName(), midpoint - a.getX(), y, null, 0, south ? 1 : -1));
          run = -1;
        }
      }
    }
  }

  private static final class Edge {
    final String from, to;
    final int x, y;
    final int crossingX, crossingY;
    final Portal portal;

    Edge(String from, String to, int x, int y, Portal portal) {
      this(from, to, x, y, portal, 0, 0);
    }

    Edge(String from, String to, int x, int y, Portal portal, int crossingX, int crossingY) {
      this.from = from;
      this.to = to;
      this.x = x;
      this.y = y;
      this.portal = portal;
      this.crossingX = crossingX;
      this.crossingY = crossingY;
    }

    boolean allowed(Player player) {
      if (portal == null) return true;
      if (portal.getZone() == null
          || !from.equals(portal.getZone().getName())
          || !to.equals(portal.getDestinationZone())
          || portal.isHidden()) return false;
      return !(portal instanceof AccessCheckingPortal)
          || ((AccessCheckingPortal) portal).isAccessibleForNavigation(player);
    }
  }

  private static final class CachedRoute {
    final List<Edge> edges;
    final long expires = System.currentTimeMillis() + ROUTE_LIFETIME;
    List<Node> localPath;

    CachedRoute(List<Edge> edges) {
      this.edges = edges;
    }

    NavigationGoal waypoint(Player player, NavigationGoal goal) {
      Edge first = edges.get(0);
      StendhalRPZone zone = player.getZone();
      if (player.getX() == first.x && player.getY() == first.y) {
        return new NavigationGoal(
            zone.getName(),
            first.x + first.crossingX,
            first.y + first.crossingY,
            "Przejście do: " + goal.label,
            goal.stage);
      }
      if (localPath != null) {
        // Reuse the path while following it; walking elsewhere invalidates it.
        boolean following = false;
        for (Node node : localPath) {
          if (Math.abs(player.getX() - node.getX()) + Math.abs(player.getY() - node.getY()) <= 1) {
            following = true;
            break;
          }
        }
        if (following) {
          return visibleWaypoint(player, goal, localPath);
        }
        localPath = null;
      }
      if (first.portal != null
          && Math.abs(player.getX() - first.x) + Math.abs(player.getY() - first.y) <= 1) {
        return new NavigationGoal(
            zone.getName(), first.x, first.y, "Przejście do: " + goal.label, goal.stage);
      }
      Rectangle destination =
          first.portal != null
              ? new Rectangle(first.x - 1, first.y - 1, 3, 3)
              : new Rectangle(first.x, first.y, 1, 1);
      localPath =
          Path.searchPath(
              player,
              zone,
              player.getX(),
              player.getY(),
              destination,
              Math.max(80, (zone.getWidth() + zone.getHeight()) * 4),
              false);
      return localPath.isEmpty() ? null : visibleWaypoint(player, goal, localPath);
    }
  }
}
