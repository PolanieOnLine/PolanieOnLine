package games.stendhal.client.gui.skilltree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import games.stendhal.client.entity.User;
import games.stendhal.common.skilltree.SkillNodeDefinition;
import games.stendhal.common.skilltree.SkillTreeDefinition;
import games.stendhal.common.skilltree.SkillTrees;
import marauroa.common.game.RPObject;

/**
 * Builds an aggregated view of the fire mage skill tree based on the current player state.
 */
final class SkillTreeModel {

    private SkillTreeModel() {
    }

    static ViewModel build() {
        SkillTreeDefinition definition = SkillTrees.fireMage();
        User user = User.get();
        int availablePoints = definition.getDefaultAvailablePoints();
        Set<String> unlocked = new LinkedHashSet<String>();
        if ((user != null) && (user.getRPObject() != null)) {
            RPObject object = user.getRPObject();
            if (object.has("skill_points_available")) {
                availablePoints = object.getInt("skill_points_available");
            }
            if (object.has("skilltree_firemage")) {
                String raw = object.get("skilltree_firemage");
                if ((raw != null) && !raw.isEmpty()) {
                    for (String token : raw.split(",")) {
                        if (!token.isEmpty()) {
                            unlocked.add(token);
                        }
                    }
                }
            }
        }

        Map<String, List<NodeView>> branches = new LinkedHashMap<String, List<NodeView>>();
        for (SkillNodeDefinition node : definition.getNodes()) {
            NodeState state;
            boolean prerequisitesMet = unlocked.containsAll(node.getPrerequisites());
            if (unlocked.contains(node.getId()) || node.isAutomaticallyUnlocked()) {
                state = NodeState.UNLOCKED;
            } else if (!prerequisitesMet) {
                state = NodeState.BLOCKED_PREREQUISITES;
            } else if (availablePoints > 0) {
                state = NodeState.READY;
            } else {
                state = NodeState.BLOCKED_POINTS;
            }

            NodeView view = new NodeView(node, state);
            List<NodeView> list = branches.get(node.getBranch());
            if (list == null) {
                list = new ArrayList<NodeView>();
                branches.put(node.getBranch(), list);
            }
            list.add(view);
        }

        return new ViewModel(definition.getDisplayName(), definition.getClassDisplayName(), availablePoints, branches);
    }

    /** View model for the entire tree. */
    static final class ViewModel {
        private final String title;
        private final String className;
        private final int availablePoints;
        private final Map<String, List<NodeView>> branches;

        ViewModel(String title, String className, int availablePoints, Map<String, List<NodeView>> branches) {
            this.title = title;
            this.className = className;
            this.availablePoints = availablePoints;
            this.branches = branches;
        }

        String getTitle() {
            return title;
        }

        String getClassName() {
            return className;
        }

        int getAvailablePoints() {
            return availablePoints;
        }

        Map<String, List<NodeView>> getBranches() {
            return branches;
        }
    }

    /** Node information with calculated state. */
    static final class NodeView {
        private final SkillNodeDefinition definition;
        private final NodeState state;

        NodeView(SkillNodeDefinition definition, NodeState state) {
            this.definition = definition;
            this.state = state;
        }

        SkillNodeDefinition getDefinition() {
            return definition;
        }

        NodeState getState() {
            return state;
        }
    }

    enum NodeState {
        UNLOCKED,
        READY,
        BLOCKED_PREREQUISITES,
        BLOCKED_POINTS
    }
}

