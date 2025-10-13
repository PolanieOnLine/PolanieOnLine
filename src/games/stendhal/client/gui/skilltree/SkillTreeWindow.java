package games.stendhal.client.gui.skilltree;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import games.stendhal.client.gui.InternalManagedWindow;
import games.stendhal.client.gui.j2DClient;

/**
 * Simple window that renders the fire mage skill tree description inside the client.
 */
public class SkillTreeWindow extends InternalManagedWindow {

    private static final long serialVersionUID = 5332171868805720673L;

    private final JTextArea content;

    public SkillTreeWindow() {
        super("skilltree", "Drzewko umiejętności maga ognia");
        setHideOnClose(true);

        content = new JTextArea(SkillTreeContentBuilder.buildFireMageTree());
        content.setEditable(false);
        content.setOpaque(false);
        content.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        content.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        content.setLineWrap(false);
        content.setWrapStyleWord(false);
        content.setFocusable(false);
        content.setCaretPosition(0);
        content.setColumns(60);
        content.setRows(30);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        setContent(scrollPane);
        setMinimizable(true);
        Dimension preferred = content.getPreferredScrollableViewportSize();
        setPreferredSize(new Dimension(preferred.width + 30, preferred.height + 30));
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
}
