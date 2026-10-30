package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;

/** Shared layout panels for themed client dialogs. The window paints the background. */
final class DialogPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    DialogPanel(final LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
    }

    @Override protected void paintComponent(final Graphics graphics) {
        // Preserve the parent window's themed background.
    }

    static JPanel section(final String title, final JComponent body) {
        final JPanel panel = new DialogPanel(new BorderLayout());
        final Style style = StyleUtil.getStyle();
        final Border frame = style == null ? BorderFactory.createEtchedBorder() : style.getBorderDown();
        final TitledBorder border = BorderFactory.createTitledBorder(frame, title);
        border.setTitleFont(new JLabel().getFont().deriveFont(Font.BOLD));
        if (style != null) { border.setTitleColor(style.getForeground()); }
        panel.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(5, 6, 5, 6)));
        panel.add(body);
        return panel;
    }
}
