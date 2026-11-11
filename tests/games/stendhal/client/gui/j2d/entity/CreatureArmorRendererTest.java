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
	public void surplusOverlayCoversOnlyItsShareAndDisappearsAtNormalHealth() {
		final HealthBar bar = new HealthBar(102, 6);
		bar.setHPRatio(1.0);
		bar.setSurplusRatio(0.2);
		final BufferedImage image = new BufferedImage(102, 6, BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			bar.draw(graphics, 0, 0);
			assertNotEquals(Color.GREEN.getRGB(), image.getRGB(20, 2));
			assertEquals(Color.GREEN.getRGB(), image.getRGB(21, 2));
			assertEquals(Color.BLACK.getRGB(), image.getRGB(0, 2));
			bar.setSurplusRatio(0);
			bar.draw(graphics, 0, 0);
			assertEquals(Color.GREEN.getRGB(), image.getRGB(20, 2));
		} finally {
			graphics.dispose();
		}
	}

	@Test
	public void surplusHealthRendersAsAFullBarWithoutInvalidColours() {
		final HealthBar bar = new HealthBar(34, 6);
		bar.setHPRatio(1.2);
		final BufferedImage image = new BufferedImage(34, 6, BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			bar.draw(graphics, 0, 0);
			assertEquals(Color.GREEN.getRGB(), image.getRGB(16, 2));
		} finally {
			graphics.dispose();
		}
	}
	@Test
	public void armorKeepsThreePixelsWhileHealthKeepsItsOriginalHeight() {
		final HealthBar bar = new HealthBar(34, 4);
		bar.setHPRatio(1.0);
		bar.setHasArmor(true);
		bar.setArmorRatio(0.5);
		assertEquals(8, bar.getHeight());
		final BufferedImage image = new BufferedImage(34, 8, BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			bar.draw(graphics, 0, 0);
			assertEquals(new Color(185, 196, 207).getRGB(), image.getRGB(8, 2));
			assertEquals(new Color(55, 63, 72).getRGB(), image.getRGB(24, 2));
			assertEquals(Color.BLACK.getRGB(), image.getRGB(8, 4));
			assertEquals(Color.GREEN.getRGB(), image.getRGB(8, 6));
			assertEquals(Color.BLACK.getRGB(), image.getRGB(8, 7));
			bar.setArmorRatio(1.0);
			bar.draw(graphics, 0, 0);
			assertEquals(new Color(185, 196, 207).getRGB(), image.getRGB(24, 2));
			assertEquals(Color.GREEN.getRGB(), image.getRGB(24, 6));
			bar.setArmorRatio(0.0);
			bar.draw(graphics, 0, 0);
			assertEquals(8, bar.getHeight());
			assertEquals(new Color(55, 63, 72).getRGB(), image.getRGB(8, 2));
			assertEquals(Color.GREEN.getRGB(), image.getRGB(8, 6));
			bar.setHasArmor(false);
			assertEquals(4, bar.getHeight());
			bar.draw(graphics, 0, 0);
			assertEquals(Color.GREEN.getRGB(), image.getRGB(8, 2));
		} finally {
			graphics.dispose();
		}
	}

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
