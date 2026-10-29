/* Copyright (C) 2026 PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.styled.styles;

import java.awt.Color;
import java.awt.Font;
import javax.swing.border.Border;
import games.stendhal.client.gui.styled.PixmapBorder;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.sprite.Sprite;

/** The client's default wood style, also usable before the game libraries load. */
public final class WoodStyle implements Style {
    private final Sprite background;
    private final Border border;
    private final Border borderDown;
    private Font font = new Font("Dialog", Font.PLAIN, 12);

    public WoodStyle(final Sprite background) {
        this.background = background;
        border = new PixmapBorder(background, true);
        borderDown = new PixmapBorder(background, false);
    }

    public void setFontSize(final int size) {
        font = new Font("Dialog", Font.PLAIN, size);
    }

    @Override public Sprite getBackground() { return background; }
    @Override public Border getBorder() { return border; }
    @Override public Border getBorderDown() { return borderDown; }
    @Override public Font getFont() { return font; }
    @Override public Color getForeground() { return Color.WHITE; }
    @Override public Color getHighLightColor() { return new Color(163, 120, 97); }
    @Override public Color getShadowColor() { return new Color(50, 25, 12); }
    @Override public Color getPlainColor() { return new Color(107, 72, 50); }
}
