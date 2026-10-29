package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.server.actions.ProductionAction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.ProductionRecipe;
import games.stendhal.server.entity.player.Player;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.BitSet;
import games.stendhal.server.core.pathfinder.Node;
import org.apache.log4j.Logger;

/** A visible, clickable workstation backed by its NPC's original recipes. */
public final class ProductionWorksite extends UseableEntity {
    private static final int[][] STEPS = {{1,0},{-1,0},{0,1},{0,-1}};
    private final SpeakerNPC producer;

    public ProductionWorksite(SpeakerNPC producer) {
        this.producer = producer;
        setEntityClass("furniture");
        put("name", "charcoal_mound");
        put("state", 0);
        put("title", "Mielerz");
        setDescription("Oto mielerz Smolisława. Użyj go, aby zlecić wypalanie węgla drzewnego lub odebrać gotowe zamówienie.");
        setSize(4, 3);
        // The map already blocks the mound's base; the smoke remains walkable.
        setResistance(0);
    }

    public SpeakerNPC getProducer() { return producer; }

    private ProductionWorksite(SpeakerNPC npc, String kind, int width) {
        this.producer = npc;
        setEntityClass("furniture"); put("name", "production_table_" + kind + (width == 1 ? "_small" : ""));
        put("state", 0); put("title", "Stanowisko produkcyjne: " + npc.getName());
        setDescription("Oto stanowisko produkcyjne. Użyj go, aby zlecić pracę lub odebrać gotowe zamówienie.");
        setSize(width, 1); setResistance(100);
    }

    public static ProductionWorksite placeFor(SpeakerNPC npc) {
        BitSet customerArea = customerArea(npc.getZone());
        String product = ProductionRecipe.forNPC(npc).get(0).getProductName();
        Item template = SingletonRepository.getEntityManager().getItem(product, ItemCreationContext.restore());
        if (template == null) return null;
        String itemClass = template.getItemClass();
        String kind = "food".equals(itemClass) ? "cooking" : "drink".equals(itemClass) || "magia".equals(itemClass) ? "alchemy" : "crafting";
        if (product.contains("sztabka") || "jewellery".equals(itemClass) || "armor".equals(itemClass)
                || "sword".equals(itemClass) || "axe".equals(itemClass) || "club".equals(itemClass)
                || "ammunition".equals(itemClass)) kind = "smithing";
        if (!"cooking".equals(kind) && !"alchemy".equals(kind)) {
            for (String resource : ProductionRecipe.forNPC(npc).get(0).getResources().keySet())
                if (resource.contains("sztabka") || resource.contains("ruda ") || resource.contains("bryłka ")) kind = "smithing";
        }
        for (int width : new int[] {2, 1}) {
            ProductionWorksite worksite = new ProductionWorksite(npc, kind, width);
            for (int radius = 1; radius <= 6; radius++) {
                for (int dy = -radius; dy <= radius; dy++) for (int dx = -radius; dx <= radius; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) continue;
                    int x = npc.getX() + dx, y = npc.getY() + dy;
                    if (canPlace(npc, worksite, x, y, customerArea)) {
                        worksite.setPosition(x, y); npc.getZone().add(worksite); return worksite;
                    }
                }
            }
        }
        Logger.getLogger(ProductionWorksite.class).warn("No safe production worksite position for " + npc.getName() + " in " + npc.getZone().getID());
        return null;
    }

    private static boolean canPlace(SpeakerNPC npc, ProductionWorksite worksite, int x, int y, BitSet customerArea) {
        StendhalRPZone zone = npc.getZone();
        int width = (int) worksite.getWidth();
        if (x < 1 || y < 1 || x + width >= zone.getWidth() || y + 1 >= zone.getHeight()) return false;
        Rectangle footprint = new Rectangle(x, y, width, 1);
        for (int i = 0; i < width; i++) if (zone.collides(x + i, y) || zone.getEntityAt(x + i + .5, y + .5) != null) return false;
        for (Entity portal : zone.getPortals()) if (footprint.intersects(portal.getX() - 1, portal.getY() - 1, portal.getWidth() + 2, portal.getHeight() + 2)) return false;
        for (Object object : zone) if (object instanceof SpeakerNPC) {
            SpeakerNPC other = (SpeakerNPC) object;
            if (other.getPath() != null) {
                Node previous = null;
                for (Node node : other.getPath().getNodeList()) {
                    if (footprint.contains(node.getX(), node.getY())) return false;
                    if (previous != null && footprint.intersects(Math.min(previous.getX(), node.getX()), Math.min(previous.getY(), node.getY()),
                            Math.abs(previous.getX() - node.getX()) + 1, Math.abs(previous.getY() - node.getY()) + 1)) return false;
                    previous = node;
                }
            }
        }
        // Every adjacent walkable cell must still connect around the table locally.
        Set<Point> neighbors = new HashSet<>();
        for (int i = 0; i < width; i++) { neighbors.add(new Point(x + i, y - 1)); neighbors.add(new Point(x + i, y + 1)); }
        neighbors.add(new Point(x - 1, y)); neighbors.add(new Point(x + width, y));
        neighbors.removeIf(point -> blocked(zone, point.x, point.y));
        if (neighbors.isEmpty()) return false;
        boolean customerCanApproach = false;
        for (Point point : neighbors) if (customerArea.get(point.y * zone.getWidth() + point.x)) customerCanApproach = true;
        if (!customerCanApproach) return false;
        ArrayDeque<Point> pending = new ArrayDeque<>(); Set<Point> seen = new HashSet<>();
        Point first = neighbors.iterator().next(); pending.add(first); seen.add(first);
        while (!pending.isEmpty()) {
            Point point = pending.remove();
            for (int[] direction : STEPS) {
                Point next = new Point(point.x + direction[0], point.y + direction[1]);
                if (next.x < x - 4 || next.x > x + width + 4 || next.y < y - 4 || next.y > y + 4
                        || footprint.contains(next) || seen.contains(next) || blocked(zone, next.x, next.y)) continue;
                seen.add(next); pending.add(next);
            }
        }
        return seen.containsAll(neighbors);
    }

    /** Public access starts at portals/map edges, never at an NPC behind a counter. */
    private static BitSet customerArea(StendhalRPZone zone) {
        int width = zone.getWidth(), height = zone.getHeight();
        if (width == 0 || height == 0) return new BitSet();
        BitSet walkable = new BitSet(width * height), entrances = new BitSet(width * height);
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
            if (!zone.collides(x, y)) walkable.set(y * width + x);
        for (Object object : zone) if (object instanceof Entity && !(object instanceof RPEntity)) {
            Entity entity = (Entity) object;
            if (entity.getResistance() <= 95) continue;
            for (int y = Math.max(0, entity.getY()); y < Math.min(height, entity.getY() + entity.getHeight()); y++)
                for (int x = Math.max(0, entity.getX()); x < Math.min(width, entity.getX() + entity.getWidth()); x++) walkable.clear(y * width + x);
        }
        for (Entity portal : zone.getPortals()) {
            // Some portals occupy a collision tile; their walkable arrival neighbors count too.
            for (int y = Math.max(0, portal.getY() - 1); y < Math.min(height, portal.getY() + portal.getHeight() + 1); y++)
                for (int x = Math.max(0, portal.getX() - 1); x < Math.min(width, portal.getX() + portal.getWidth() + 1); x++)
                    if (walkable.get(y * width + x)) entrances.set(y * width + x);
        }
        for (int x = 0; x < width; x++) { entrances.set(x); entrances.set((height - 1) * width + x); }
        for (int y = 0; y < height; y++) { entrances.set(y * width); entrances.set(y * width + width - 1); }
        entrances.and(walkable);
        if (!entrances.isEmpty()) return flood(walkable, entrances, width, height);
        // Isolated/scripted rooms without portals use their main walkable floor area.
        BitSet visited = new BitSet(width * height), largest = new BitSet(width * height);
        for (int start = walkable.nextSetBit(0); start >= 0; start = walkable.nextSetBit(start + 1)) {
            if (visited.get(start)) continue;
            BitSet seed = new BitSet(); seed.set(start);
            BitSet component = flood(walkable, seed, width, height); visited.or(component);
            if (component.cardinality() > largest.cardinality()) largest = component;
        }
        return largest;
    }

    private static BitSet flood(BitSet walkable, BitSet entrances, int width, int height) {
        BitSet reached = (BitSet) entrances.clone(); ArrayDeque<Integer> pending = new ArrayDeque<>();
        for (int seed = entrances.nextSetBit(0); seed >= 0; seed = entrances.nextSetBit(seed + 1)) pending.add(seed);
        while (!pending.isEmpty()) {
            int cell = pending.remove(), x = cell % width, y = cell / width;
            for (int[] step : STEPS) {
                int nextX = x + step[0], nextY = y + step[1];
                if (nextX < 0 || nextY < 0 || nextX >= width || nextY >= height) continue;
                int next = nextY * width + nextX;
                if (walkable.get(next) && !reached.get(next)) { reached.set(next); pending.add(next); }
            }
        }
        return reached;
    }

    private static boolean blocked(StendhalRPZone zone, int x, int y) {
        if (x < 0 || y < 0 || x >= zone.getWidth() || y >= zone.getHeight() || zone.collides(x, y)) return true;
        for (Object object : zone) if (object instanceof ProductionWorksite && ((ProductionWorksite) object).getArea().contains(x + .5, y + .5)) return true;
        return false;
    }

    @Override public boolean onUsed(RPEntity user) {
        if (!(user instanceof Player) || getZone() == null || user.getZone() != getZone()
                || producer.getZone() != getZone() || !user.nextTo(this)) return false;
        ProductionAction.open((Player) user, this);
        return true;
    }
}
