package games.stendhal.client.gui.j2d.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;

import org.junit.Test;

import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.entity.TextIndicator;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.TextSprite;
import games.stendhal.common.NotificationType;

public class RPEntityFloatingTextTest {
	@Test
	public void floatingDamageStaysNearTheSpriteAndIsPaintedOverArmorAndNames() throws Exception {
		for (int offset : new int[] { 0, 12 }) {
			TestView view = new TestView();
			view.statusBarYOffset = offset;
			HealthBar bar = new HealthBar(34, 4);
			bar.setHasArmor(true);
			bar.setArmorRatio(1.0);
			bar.setHPRatio(1.0);
			set(view, "healthBar", bar);
			set(view, "showHP", true);
			set(view, "showTitle", true);
			set(view, "titleSprite", TextSprite.createTextSprite("strażnik", Color.WHITE));
			Constructor<TextIndicator> constructor = TextIndicator.class
					.getDeclaredConstructor(String.class, NotificationType.class);
			constructor.setAccessible(true);
			TextIndicator indicator = constructor.newInstance("-100", NotificationType.NEGATIVE);
			Field age = TextIndicator.class.getDeclaredField("age");
			age.setAccessible(true);
			age.setInt(indicator, 300);
			RecordingSprite damage = new RecordingSprite();
			set(view, "floaters", Collections.singletonMap(indicator, damage));
			BufferedImage image = new BufferedImage(100, 120, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			try {
				view.drawTop(graphics, 20, 64, 34, 32);
			} finally {
				graphics.dispose();
			}
			assertTrue("Damage must start next to the sprite instead of above its name",
					damage.y + damage.getHeight() <= 64 + offset);
			for (int row = damage.y; row < damage.y + 10; row++) {
				assertEquals(Color.RED.getRGB(), image.getRGB(damage.x, row));
			}
			assertEquals(Color.GREEN.getRGB(), image.getRGB(30, 61 + offset));
			assertEquals(49 + offset, damage.y);
			int firstY = damage.y;
			age.setInt(indicator, 2000);
			graphics = image.createGraphics();
			try {
				view.drawTop(graphics, 20, 64, 34, 32);
			} finally {
				graphics.dispose();
			}
			assertTrue("Damage must stay within one tile of its starting height",
					firstY - damage.y <= 32);
		}
	}

	private void set(TestView view, String name, Object value) throws Exception {
		Field field = RPEntity2DView.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(view, value);
	}

	private static class TestView extends RPEntity2DView<RPEntity> {
		@Override
		protected Sprite getAnimationSprite() {
			return null;
		}
	}

	private static class RecordingSprite extends ImageSprite {
		private int x, y;

		RecordingSprite() {
			super(new BufferedImage(8, 10, BufferedImage.TYPE_INT_ARGB));
		}

		@Override
		public void draw(Graphics graphics, int x, int y) {
			this.x = x;
			this.y = y;
			graphics.setColor(Color.RED);
			graphics.fillRect(x, y, 8, 10);
		}
	}
}
