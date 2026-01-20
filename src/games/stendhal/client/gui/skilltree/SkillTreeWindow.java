package games.stendhal.client.gui.skilltree;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;

import games.stendhal.client.entity.EntityChangeListener;
import games.stendhal.client.entity.Player;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.InternalManagedWindow;
import games.stendhal.client.gui.j2DClient;

/**
 * Simple window that renders the fire mage skill tree description inside the client.
 */
public class SkillTreeWindow extends InternalManagedWindow implements EntityChangeListener<Player> {

    private static final long serialVersionUID = 5332171868805720673L;

    private final JEditorPane content;

    public SkillTreeWindow() {
        super("skilltree", "Drzewko umiejętności maga ognia");
        setHideOnClose(true);

        content = new JEditorPane();
        content.setContentType("text/html");
        content.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        content.setEditable(false);
        content.setOpaque(true);
        content.setBackground(new Color(0x1b1b1f));
        content.setForeground(new Color(0xdddddd));
        content.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        content.setFocusable(false);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        setContent(scrollPane);
        setMinimizable(true);
        Dimension preferred = new Dimension(640, 520);
        setPreferredSize(preferred);
        refreshContent();
    }

    public void showWindow() {
        if (getParent() == null) {
            j2DClient.get().addWindow(this);
            center();
        }

        setMinimized(false);
        setVisible(true);
        Container parent = getParent();
        if (parent instanceof JLayeredPane) {
            ((JLayeredPane) parent).moveToFront(this);
        }
        revalidate();
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        User user = User.get();
        if (user != null) {
            user.addChangeListener(this);
        }
        refreshContent();
    }

    @Override
    public void removeNotify() {
        User user = User.get();
        if (user != null) {
            user.removeChangeListener(this);
        }
        super.removeNotify();
    }

    @Override
    public void entityChanged(Player entity, Object property) {
        if (property == null || property == Player.PROP_SKILL_TREE) {
            refreshContent();
        }
    }

    private void refreshContent() {
        String html = SkillTreeContentBuilder.buildFireMageTree(SkillTreeModel.build());
        content.setText(html);
        content.setCaretPosition(0);
    }
}
