package games.stendhal.client.gui.j2d.entity;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.Test;
import games.stendhal.common.CreatureArmor;

public class CreatureArmorRendererTest {
	@Test
	public void onlyKnownTypesHaveNamesAndArmorIcons() {
		assertNull(CreatureArmor.fromId(null));
		assertNull(CreatureArmor.fromId("unknown"));
		assertNull(CreatureArmor.NONE.getSpritePath());
		assertEquals("Pancerz średni", CreatureArmor.fromId("medium").getLabel());
	}

	@Test
	public void sharedIconsHaveNativeSizeBinaryAlphaAndDistinctPixels() throws Exception {
		final java.util.Set<Integer> hashes = new java.util.HashSet<>();
		for (final CreatureArmor armor : new CreatureArmor[] {
				CreatureArmor.LIGHT, CreatureArmor.MEDIUM, CreatureArmor.HEAVY}) {
			final BufferedImage icon = ImageIO.read(new File(armor.getSpritePath()));
			assertEquals(16, icon.getWidth());
			assertEquals(16, icon.getHeight());
			assertEquals(0, icon.getRGB(0, 0));
			boolean visible = false;
			for (final int pixel : icon.getRGB(0, 0, 16, 16, null, 0, 16)) {
				final int alpha = pixel >>> 24;
				assertTrue(alpha == 0 || alpha == 255);
				visible |= alpha == 255;
			}
			assertTrue(visible);
			hashes.add(java.util.Arrays.hashCode(icon.getRGB(0, 0, 16, 16, null, 0, 16)));
		}
		assertEquals(3, hashes.size());
	}

	@Test
	public void frameDoesNotOverwriteHealthOrLeakGraphicsStateAndIconIsStatic() {
		final BufferedImage first = render(CreatureArmor.HEAVY);
		final BufferedImage again = render(CreatureArmor.HEAVY);
		assertArrayEquals(first.getRGB(0, 0, 80, 40, null, 0, 80),
				again.getRGB(0, 0, 80, 40, null, 0, 80));
		assertEquals(new Color(104, 116, 131).getRGB(), first.getRGB(29, 9));
		assertEquals(Color.GREEN.getRGB(), first.getRGB(30, 10));
		assertEquals(Color.GREEN.getRGB(), first.getRGB(61, 13));
		assertEquals(0, first.getRGB(11, 10));
		assertEquals(0, render(CreatureArmor.NONE).getRGB(29, 9));
		assertEquals(0, render(null).getRGB(29, 9));
	}

	private BufferedImage render(final CreatureArmor armor) {
		final BufferedImage image = new BufferedImage(80, 40, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			graphics.setColor(Color.GREEN);
			graphics.fillRect(30, 10, 32, 4);
			CreatureArmorRenderer.draw(armor, graphics, 30, 10, 32, 4);
			assertEquals(Color.GREEN, graphics.getColor());
		} finally {
			graphics.dispose();
		}
		return image;
	}
}
