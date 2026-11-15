/* Copyright (C) 2026 - PolanieOnLine;
GPL version 2 or later. */
package games.stendhal.server.core.navigation;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.Spot;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.SimplePathfinder;
import games.stendhal.server.entity.mapstuff.portal.AccessCheckingPortal;
import games.stendhal.server.entity.mapstuff.portal.OneWayPortalDestination;
import games.stendhal.server.entity.mapstuff.portal.PasswordPortal;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.mapstuff.portal.Teleporter;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.quests.compass.WanderersCompassQuest;
import games.stendhal.server.maps.quests.trail.TrailNavigationTargets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.WeakHashMap;
import marauroa.common.game.IRPZone;

/** Optional, private guidance. Never moves players or invokes quest/portal actions. */
public final class CompassService {
	public static final String TARGETS = "compass_targets";
	public static final String UNLOCKED = "compass_unlocked";
	private static final long TOPOLOGY_LIFETIME = 60000L;
	private static long topologyExpires;
	private static Map<String, List<Edge>> topology = Collections.emptyMap();
	private static final Map<Player, Map<String, CachedRoute>> routes = new WeakHashMap<>();
	private static final Map<Player, Map<String, CachedPath>> localPaths = new WeakHashMap<>();

	private CompassService() {}

	/** A teleport changes the current route leg even when the map stays the same. */
	public static void onTeleport(Player player) {
		routes.remove(player);
		localPaths.remove(player);
	}

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
			for (NavigationGoal goal : main)
				publish(player, next, "main:" + goal.stage + ":" + goal.zone + ":" + goal.x + "," + goal.y, "main", goal);
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
			values.put(key, local.x + "\t" + local.y + "\t" + kind + "\t" + sanitize(local.label)
					+ (local.remainingSteps >= 0 ? "\t" + local.remainingSteps : ""));
	}

	static String sanitize(String label) {
		return label == null ? "" : label.replaceAll("[\\p{Cntrl}]", " ");
	}

	/** Remote goals are translated to a reachable local path step, never foreign coordinates. */
	public static NavigationGoal localTarget(Player player, NavigationGoal goal) {
		final StendhalRPZone current = player.getZone();
		if (current == null || goal == null) return null;
		if (current.getName().equals(goal.zone)) {
			NavigationGoal local = waypoint(player, goal, goal.x, goal.y, false, false);
			if (local != null) return local;
		}
		ensureTopology(current.getName(), goal.zone);
		final String key = goal.zone + "\t" + goal.x + "," + goal.y;
		Map<String, CachedRoute> cache = routes.computeIfAbsent(player, p -> new HashMap<>());
		CachedRoute cached = cache.get(key);
		if (cached != null && cached.valid(player)) {
			NavigationGoal local = cached.waypoint(player, goal);
			if (local != null) return local;
		}
		Set<BlockedStep> unreachable = new HashSet<>();
		// Retry when the shortest zone route starts in an isolated room of this map.
		while (true) {
			List<Edge> path = findRoute(player, goal, unreachable);
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
			unreachable.add(candidate.failed == null ? new BlockedStep(null, first) : candidate.failed);
		}
	}

	private static NavigationGoal waypoint(
			Player player, NavigationGoal goal, int x, int y, boolean portal, boolean passage) {
		Map<String, CachedPath> cache = localPaths.computeIfAbsent(player, p -> new HashMap<>());
		String key = player.getZone().getName() + "\t" + x + "," + y + "," + portal;
		if (cache.size() > 64) cache.clear();
		return cache.computeIfAbsent(key, ignored -> new CachedPath(x, y, portal)).guide(player, goal, passage);
	}

	private static List<Node> path(StendhalRPZone zone, int startX, int startY, int x, int y, boolean portal) {
		Rectangle destination = portal || zone.collides(x, y)
				? new Rectangle(x - 1, y - 1, 3, 3) : new Rectangle(x, y, 1, 1);
		return new SimplePathfinder(zone, startX, startY, destination,
				Math.max(80, (zone.getWidth() + zone.getHeight()) * 4)).getPath();
	}

	private static int walkingDistance(StendhalRPZone zone, int startX, int startY, int x, int y, boolean portal) {
		List<Node> nodes = path(zone, startX, startY, x, y, portal);
		if (nodes.isEmpty()) return -1;
		Node last = nodes.get(nodes.size() - 1);
		return nodes.size() - 1 + Math.abs(last.getX() - x) + Math.abs(last.getY() - y);
	}

	private static List<Edge> findRoute(Player player, NavigationGoal goal, Set<BlockedStep> unreachable) {
		PriorityQueue<RoutePosition> queue = new PriorityQueue<>(Comparator.comparingInt(p -> p.distance));
		Map<Edge, Edge> previous = new HashMap<>();
		Map<Edge, Integer> distances = new HashMap<>();
		queue.add(new RoutePosition(player.getZone().getName(), null, 0));
		Edge best = null;
		int bestDistance = Integer.MAX_VALUE;
		while (!queue.isEmpty()) {
			RoutePosition position = queue.remove();
			if (position.distance >= bestDistance) {
				break;
			}
			if (position.incoming != null && position.distance != distances.get(position.incoming)) {
				continue;
			}
			Spot start = position.incoming == null
					? new Spot(player.getZone(), player.getX(), player.getY()) : arrival(position.incoming);
			if (start == null) {
				continue;
			}
			if (position.zone.equals(goal.zone)
					&& !unreachable.contains(new BlockedStep(position.incoming, null))) {
				int lastLeg = walkingDistance(start.getZone(), start.getX(), start.getY(), goal.x, goal.y, false);
				if (lastLeg >= 0 && position.distance + lastLeg < bestDistance) {
					bestDistance = position.distance + lastLeg;
					best = position.incoming;
				}
			}
			for (Edge edge : topology.getOrDefault(position.zone, Collections.emptyList())) {
				if (unreachable.contains(new BlockedStep(position.incoming, edge)) || !edge.allowed(player)) {
					continue;
				}
				int lowerBound = position.distance + Math.abs(start.getX() - edge.x) + Math.abs(start.getY() - edge.y);
				if (lowerBound >= Math.min(bestDistance, distances.getOrDefault(edge, Integer.MAX_VALUE))) {
					continue;
				}
				int leg = walkingDistance(start.getZone(), start.getX(), start.getY(), edge.x, edge.y, edge.portal != null);
				if (leg < 0) {
					continue;
				}
				int distance = position.distance + leg + (edge.portal == null ? 1 : 0);
				if (distance < bestDistance && distance < distances.getOrDefault(edge, Integer.MAX_VALUE)) {
					distances.put(edge, distance);
					previous.put(edge, position.incoming);
					queue.add(new RoutePosition(edge.to, edge, distance));
				}
			}
		}
		List<Edge> path = new ArrayList<>();
		for (Edge back = best; back != null; back = previous.get(back)) {
			path.add(back);
		}
		Collections.reverse(path);
		return path;
	}

	private static final class RoutePosition {
		final String zone;
		final Edge incoming;
		final int distance;

		RoutePosition(String zone, Edge incoming, int distance) {
			this.zone = zone;
			this.incoming = incoming;
			this.distance = distance;
		}
	}

	private static final class BlockedStep {
		final Edge incoming, outgoing;

		BlockedStep(Edge incoming, Edge outgoing) {
			this.incoming = incoming;
			this.outgoing = outgoing;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof BlockedStep && ((BlockedStep) other).incoming == incoming
					&& ((BlockedStep) other).outgoing == outgoing;
		}

		@Override
		public int hashCode() {
			return 31 * System.identityHashCode(incoming) + System.identityHashCode(outgoing);
		}
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
				Spot coordinate = portal instanceof Teleporter ? ((Teleporter) portal).getDestinationSpot() : null;
				String destinationName = coordinate == null ? portal.getDestinationZone() : coordinate.getZone().getName();
				StendhalRPZone destination = byName.get(destinationName);
				if (destination == null
						|| (coordinate == null && !portal.loaded())
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
		// Existing itineraries remain selected while their passages are still accessible.
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
			Spot coordinate = portal instanceof Teleporter ? ((Teleporter) portal).getDestinationSpot() : null;
			String destination = coordinate == null ? portal.getDestinationZone() : coordinate.getZone().getName();
			if (portal.getZone() == null
					|| !from.equals(portal.getZone().getName())
					|| !to.equals(destination)
					|| portal.isHidden()) return false;
			return !(portal instanceof AccessCheckingPortal)
					|| ((AccessCheckingPortal) portal).isAccessibleForNavigation(player);
		}
	}

	private static final class CachedRoute {
		final List<Edge> edges;
		final Map<Integer, Integer> tails = new HashMap<>();
		BlockedStep failed;
		int currentIndex;

		CachedRoute(List<Edge> edges) {
			this.edges = edges;
		}

		int index(String zone) {
			for (int i = currentIndex; i < edges.size(); i++) if (edges.get(i).from.equals(zone)) {
				currentIndex = i;
				return i;
			}
			for (int i = 0; i < currentIndex; i++) if (edges.get(i).from.equals(zone)) {
				currentIndex = i;
				return i;
			}
			return -1;
		}

		boolean valid(Player player) {
			int start = index(player.getZone().getName());
			if (start < 0) return false;
			for (int i = start; i < edges.size(); i++) if (!edges.get(i).allowed(player)) return false;
			return true;
		}

		int tail(int index, NavigationGoal goal) {
			if (tails.containsKey(index)) return tails.get(index);
			Edge edge = edges.get(index);
			Spot arrival = arrival(edge);
			if (arrival == null) {
				failed = new BlockedStep(index == 0 ? null : edges.get(index - 1), edge);
				return -1;
			}
			Edge next = index + 1 < edges.size() ? edges.get(index + 1) : null;
			int x = next == null ? goal.x : next.x;
			int y = next == null ? goal.y : next.y;
			int distance = walkingDistance(arrival.getZone(), arrival.getX(), arrival.getY(), x, y,
					next != null && next.portal != null);
			if (distance < 0) {
				failed = new BlockedStep(edge, next);
				return -1;
			}
			if (next != null) {
				int further = tail(index + 1, goal);
				if (further < 0) return -1;
				distance += further + (next.portal == null ? 1 : 0);
			}
			tails.put(index, distance);
			return distance;
		}

		NavigationGoal waypoint(Player player, NavigationGoal goal) {
			int index = index(player.getZone().getName());
			if (index < 0) return null;
			Edge first = edges.get(index);
			StendhalRPZone zone = player.getZone();
			int remainder = tail(index, goal);
			if (remainder < 0) return null;
			if (first.portal == null && player.getX() == first.x && player.getY() == first.y) {
				return new NavigationGoal(
						zone.getName(),
						first.x + first.crossingX,
						first.y + first.crossingY,
						"Przejście do: " + goal.label,
						goal.stage, remainder + 1);
			}
			NavigationGoal local = CompassService.waypoint(player, goal, first.x, first.y, first.portal != null, true);
			return local == null ? null : new NavigationGoal(local.zone, local.x, local.y, local.label, local.stage,
					local.remainingSteps + remainder + (first.portal == null ? 1 : 0));
		}
	}

	private static Spot arrival(Edge edge) {
		StendhalRPZone destination = SingletonRepository.getRPWorld().getZone(edge.to);
		if (destination == null) return null;
		if (edge.portal instanceof Teleporter) return ((Teleporter) edge.portal).getDestinationSpot();
		if (edge.portal == null) {
			StendhalRPZone from = SingletonRepository.getRPWorld().getZone(edge.from);
			return new Spot(destination, from.getX() + edge.x + edge.crossingX - destination.getX(),
					from.getY() + edge.y + edge.crossingY - destination.getY());
		}
		Portal portal = destination.getPortal(edge.portal.getDestinationReference());
		if (portal == null) return null;
		int x = portal.getX(), y = portal.getY();
		if (portal.hasOffset()) {
			int offset = portal.getOffset();
			int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
			int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};
			if (offset >= 0 && offset < dx.length) {
				x += dx[offset];
				y += dy[offset];
			}
		}
		return new Spot(destination, x, y);
	}

	private static final class CachedPath {
		final int x, y;
		final boolean portal;
		List<Node> nodes;
		Node anchor;
		int anchorIndex;
		int progress;

		CachedPath(int x, int y, boolean portal) {
			this.x = x;
			this.y = y;
			this.portal = portal;
		}

		NavigationGoal guide(Player player, NavigationGoal goal, boolean passage) {
			StendhalRPZone zone = player.getZone();
			String label = passage ? "Przejście do: " + goal.label : goal.label;
			int direct = Math.abs(player.getX() - x) + Math.abs(player.getY() - y);
			if (direct <= (portal ? 1 : 0))
				return new NavigationGoal(zone.getName(), x, y, label, goal.stage, direct);
			if (nodes == null || nodes.isEmpty()) recalculate(player);
			if (nodes.isEmpty()) return null;
			int nearest = nearest(player);
			if (nearest > 0) {
				recalculate(player);
				if (nodes.isEmpty()) {
					return null;
				}
				nearest = nearest(player);
			}
			if (anchor != null && (player.getX() != anchor.getX() || player.getY() != anchor.getY())
					&& zone.collidesOnLine(player.getX(), player.getY(), anchor.getX(), anchor.getY())) {
				recalculate(player);
				if (nodes.isEmpty()) return null;
				nearest = nearest(player);
			}
			if (anchor == null || (player.getX() == anchor.getX() && player.getY() == anchor.getY())
					|| (nearest == 0 && progress > anchorIndex)) {
				anchor = null;
				anchorIndex = nodes.size() - 1;
				if (progress + 1 < nodes.size()) {
					int dx = nodes.get(progress + 1).getX() - nodes.get(progress).getX();
					int dy = nodes.get(progress + 1).getY() - nodes.get(progress).getY();
					for (int i = progress + 1; i + 1 < nodes.size(); i++) {
						if (nodes.get(i + 1).getX() - nodes.get(i).getX() != dx
								|| nodes.get(i + 1).getY() - nodes.get(i).getY() != dy) {
							anchorIndex = i;
							break;
						}
					}
				}
				anchor = nodes.get(anchorIndex);
			}
			if (anchor == null) return null;
			Node end = nodes.get(nodes.size() - 1);
			int remaining = Math.abs(player.getX() - anchor.getX()) + Math.abs(player.getY() - anchor.getY())
					+ nodes.size() - 1 - anchorIndex + Math.abs(end.getX() - x) + Math.abs(end.getY() - y);
			return new NavigationGoal(zone.getName(), anchor.getX(), anchor.getY(), label, goal.stage, remaining);
		}

		private void recalculate(Player player) {
			nodes = path(player.getZone(), player.getX(), player.getY(), x, y, portal);
			progress = 0;
			anchor = null;
		}

		private int nearest(Player player) {
			int best = Integer.MAX_VALUE;
			int selected = progress;
			for (int i = 0; i < nodes.size(); i++) {
				Node node = nodes.get(i);
				int distance = Math.abs(player.getX() - node.getX()) + Math.abs(player.getY() - node.getY());
				if (distance < best || (distance == best && Math.abs(i - progress) < Math.abs(selected - progress))) {
					best = distance;
					selected = i;
				}
			}
			progress = selected;
			return best;
		}
	}
}
