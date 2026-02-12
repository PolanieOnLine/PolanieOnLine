package games.stendhal.client.gui.skilltree;

import java.util.List;
import java.util.Map;

import games.stendhal.common.skilltree.SkillNodeDefinition;

/**
 * Builds an HTML representation of the fire mage skill tree.
 */
final class SkillTreeContentBuilder {

    private SkillTreeContentBuilder() {
    }

    static String buildFireMageTree(SkillTreeModel.ViewModel model) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><head><style>");
        builder.append("body { font-family: 'DejaVu Sans Mono', 'Consolas', monospace; color: #ddd; background:#1b1b1f; }");
        builder.append("h1 { text-align: center; margin-bottom: 4px; color: #ffb347; }");
        builder.append("h2 { margin: 12px 0 4px; color: #ffa000; }");
        builder.append("ul { list-style: none; margin: 0; padding-left: 12px; }");
        builder.append("li { margin: 2px 0; }");
        builder.append(".legend { margin: 8px 0; font-size: 12px; color: #aaa; }");
        builder.append(".points { margin: 4px 0 12px; font-size: 13px; color: #f5f5f5; text-align: center; }");
        builder.append(".state-unlocked { color: #4caf50; }");
        builder.append(".state-ready { color: #ffca28; }");
        builder.append(".state-blocked { color: #9e9e9e; }");
        builder.append(".code { display: inline-block; width: 70px; }");
        builder.append(".footer { margin-top: 16px; font-size: 12px; color: #aaa; }");
        builder.append("</style></head><body>");

        builder.append("<h1>").append(escape(model.getTitle())).append(": ")
                .append(escape(model.getClassName())).append("</h1>");
        builder.append("<div class='points'>Punkty umiejętności: <strong>")
                .append(model.getAvailablePoints()).append("</strong> dostępne</div>");

        builder.append("<div class='legend'>[✔] odblokowane · [☆] gotowe do kupna · [✖] brak wymagań · [⛔] brak punktów</div>");

        for (Map.Entry<String, List<SkillTreeModel.NodeView>> entry : model.getBranches().entrySet()) {
            builder.append("<h2>").append(escape(entry.getKey())).append("</h2>");
            builder.append("<ul>");
            for (SkillTreeModel.NodeView nodeView : entry.getValue()) {
                SkillNodeDefinition node = nodeView.getDefinition();
                SkillTreeModel.NodeState state = nodeView.getState();
                builder.append("<li class='").append(cssClass(state)).append("'>");
                builder.append(symbol(state)).append(" <span class='code'>[")
                        .append(escape(node.getId())).append("]</span> ");
                builder.append("<strong>").append(escape(node.getDisplayName())).append("</strong> – ");
                builder.append(escape(node.getDescription()));
                appendPrerequisites(builder, node);
                builder.append("</li>");
            }
            builder.append("</ul>");
        }

        builder.append("<div class='footer'>Kliknij węzeł w oknie, aby przeczytać opis. Odblokuj nowe runy komendą <strong>/skilltree unlock &lt;kod&gt;</strong>.</div>");
        builder.append("</body></html>");
        return builder.toString();
    }

    private static void appendPrerequisites(StringBuilder builder, SkillNodeDefinition node) {
        if (!node.getPrerequisites().isEmpty() && !node.isAutomaticallyUnlocked()) {
            builder.append(" <span style='color:#888;'>[wymaga: ");
            boolean first = true;
            for (String prereq : node.getPrerequisites()) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(escape(prereq));
                first = false;
            }
            builder.append("]</span>");
        }
    }

    private static String cssClass(SkillTreeModel.NodeState state) {
        switch (state) {
        case UNLOCKED:
            return "state-unlocked";
        case READY:
            return "state-ready";
        default:
            return "state-blocked";
        }
    }

    private static String symbol(SkillTreeModel.NodeState state) {
        switch (state) {
        case UNLOCKED:
            return "[✔]";
        case READY:
            return "[☆]";
        case BLOCKED_POINTS:
            return "[⛔]";
        default:
            return "[✖]";
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
