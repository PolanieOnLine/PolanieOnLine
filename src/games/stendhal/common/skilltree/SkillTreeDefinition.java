package games.stendhal.common.skilltree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a fully defined skill tree.
 */
public final class SkillTreeDefinition {

    private final String key;
    private final String displayName;
    private final String classDisplayName;
    private final List<SkillNodeDefinition> nodes;
    private final Map<String, SkillNodeDefinition> nodeIndex;
    private final int defaultAvailablePoints;

    public SkillTreeDefinition(String key, String displayName, String classDisplayName,
            List<SkillNodeDefinition> nodes, int defaultAvailablePoints) {
        this.key = key;
        this.displayName = displayName;
        this.classDisplayName = classDisplayName;
        this.nodes = Collections.unmodifiableList(new ArrayList<SkillNodeDefinition>(nodes));
        Map<String, SkillNodeDefinition> map = new LinkedHashMap<String, SkillNodeDefinition>();
        for (SkillNodeDefinition node : nodes) {
            map.put(node.getId(), node);
        }
        this.nodeIndex = Collections.unmodifiableMap(map);
        this.defaultAvailablePoints = defaultAvailablePoints;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getClassDisplayName() {
        return classDisplayName;
    }

    public List<SkillNodeDefinition> getNodes() {
        return nodes;
    }

    public Map<String, SkillNodeDefinition> getNodeIndex() {
        return nodeIndex;
    }

    public int getDefaultAvailablePoints() {
        return defaultAvailablePoints;
    }
}

