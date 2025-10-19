package games.stendhal.server.skilltree;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import games.stendhal.common.constants.Nature;
import games.stendhal.common.skilltree.SkillNodeDefinition;
import games.stendhal.common.skilltree.SkillTreeDefinition;
import games.stendhal.common.skilltree.SkillTrees;
import games.stendhal.server.entity.player.Player;

/**
 * Core operations for the prototype fire mage skill tree.
 */
public final class SkillTreeService {

    private static final String ATTR_AVAILABLE_POINTS = "skill_points_available";
    private static final String ATTR_UNLOCKED_NODES = "skilltree_firemage";

    private static final SkillTreeDefinition FIRE_MAGE = SkillTrees.fireMage();

    private SkillTreeService() {
    }

    public static SkillTreeDefinition getDefinition() {
        return FIRE_MAGE;
    }

    /**
     * Ensures the player has default attributes assigned for the fire mage tree.
     *
     * @param player current player
     */
    public static void ensureInitialized(Player player) {
        boolean changed = false;
        if (!player.has(ATTR_AVAILABLE_POINTS)) {
            player.put(ATTR_AVAILABLE_POINTS, FIRE_MAGE.getDefaultAvailablePoints());
            changed = true;
        }
        if (!player.has(ATTR_UNLOCKED_NODES)) {
            Set<String> autoUnlocked = new HashSet<String>();
            for (SkillNodeDefinition node : FIRE_MAGE.getNodes()) {
                if (node.isAutomaticallyUnlocked()) {
                    autoUnlocked.add(node.getId());
                }
            }
            player.put(ATTR_UNLOCKED_NODES, serialize(autoUnlocked));
            changed = true;
        } else {
            Set<String> unlocked = parseUnlocked(player.get(ATTR_UNLOCKED_NODES));
            boolean additions = false;
            for (SkillNodeDefinition node : FIRE_MAGE.getNodes()) {
                if (node.isAutomaticallyUnlocked() && !unlocked.contains(node.getId())) {
                    unlocked.add(node.getId());
                    additions = true;
                }
            }
            if (additions) {
                player.put(ATTR_UNLOCKED_NODES, serialize(unlocked));
                changed = true;
            }
        }

        if (changed) {
            player.notifyWorldAboutChanges();
        }
    }

    public static int getAvailablePoints(Player player) {
        ensureInitialized(player);
        return player.getInt(ATTR_AVAILABLE_POINTS);
    }

    private static void setAvailablePoints(Player player, int value) {
        player.put(ATTR_AVAILABLE_POINTS, value);
    }

    public static Set<String> getUnlockedNodes(Player player) {
        ensureInitialized(player);
        return parseUnlocked(player.get(ATTR_UNLOCKED_NODES));
    }

    private static void setUnlockedNodes(Player player, Set<String> unlocked) {
        player.put(ATTR_UNLOCKED_NODES, serialize(unlocked));
    }

    private static String serialize(Set<String> unlocked) {
        TreeSet<String> sorted = new TreeSet<String>(unlocked);
        return String.join(",", sorted);
    }

    private static Set<String> parseUnlocked(String raw) {
        Set<String> result = new HashSet<String>();
        if ((raw == null) || raw.isEmpty()) {
            return result;
        }
        result.addAll(Arrays.asList(raw.split(",")));
        result.remove("");
        return result;
    }

    public static SkillTreeUnlockResult unlock(Player player, String nodeId) {
        ensureInitialized(player);
        Map<String, SkillNodeDefinition> nodes = FIRE_MAGE.getNodeIndex();
        SkillNodeDefinition node = nodes.get(nodeId);
        if (node == null) {
            return SkillTreeUnlockResult.UNKNOWN_NODE;
        }

        Set<String> unlocked = getUnlockedNodes(player);
        if (unlocked.contains(nodeId)) {
            return SkillTreeUnlockResult.ALREADY_UNLOCKED;
        }

        if (getAvailablePoints(player) < 1) {
            return SkillTreeUnlockResult.NOT_ENOUGH_POINTS;
        }

        for (String prereq : node.getPrerequisites()) {
            if (!unlocked.contains(prereq)) {
                return SkillTreeUnlockResult.PREREQUISITES_MISSING;
            }
        }

        unlocked.add(nodeId);
        setUnlockedNodes(player, unlocked);
        setAvailablePoints(player, getAvailablePoints(player) - 1);
        player.notifyWorldAboutChanges();
        return SkillTreeUnlockResult.SUCCESS;
    }

    public static double getDamageModifier(Player player, Nature nature) {
        if (nature != Nature.FIRE) {
            return 1.0;
        }
        ensureInitialized(player);
        Set<String> unlocked = getUnlockedNodes(player);
        double bonus = 0.0;
        for (String nodeId : unlocked) {
            SkillNodeDefinition node = FIRE_MAGE.getNodeIndex().get(nodeId);
            if (node != null) {
                bonus += node.getDamageModifierBonus();
            }
        }
        return 1.0 + bonus;
    }
}

