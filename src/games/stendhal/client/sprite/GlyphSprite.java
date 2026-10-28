package games.stendhal.client.sprite;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Colour the whole sheet once, before splitting it into animation frames. */
public final class GlyphSprite {
    private static final Map<Sprite, Map<Integer, Sprite>> CACHE = new WeakHashMap<Sprite, Map<Integer, Sprite>>();

    private GlyphSprite() { }

    public static synchronized Sprite forLevel(Sprite sheet, int level) {
        if (level != 1 && level != 3) return sheet;
        Map<Integer, Sprite> levels = CACHE.get(sheet);
        if (levels == null) { levels = new HashMap<Integer, Sprite>(); CACHE.put(sheet, levels); }
        Sprite cached = levels.get(level);
        if (cached != null) return cached;
        BufferedImage image = new BufferedImage(sheet.getWidth(), sheet.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics(); sheet.draw(graphics, 0, 0); graphics.dispose();
        double saturation = level == 1 ? 0.55 : 1.55;
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
            int pixel = image.getRGB(x, y), red = (pixel >> 16) & 255, green = (pixel >> 8) & 255, blue = pixel & 255;
            double grey = red * 0.2126 + green * 0.7152 + blue * 0.0722;
            image.setRGB(x, y, (pixel & 0xff000000) | (channel(grey, red, saturation) << 16)
                    | (channel(grey, green, saturation) << 8) | channel(grey, blue, saturation));
        }
        Object reference = sheet.getReference() == null ? new Object() : sheet.getReference() + "#glyph-level-" + level;
        Sprite result = new ImageSprite(image, reference); levels.put(level, result); return result;
    }

    private static int channel(double grey, int value, double saturation) {
        return (int)Math.max(0, Math.min(255, Math.round(grey + (value - grey) * saturation)));
    }
}
