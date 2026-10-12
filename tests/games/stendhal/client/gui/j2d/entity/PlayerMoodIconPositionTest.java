/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.j2d.entity;

import static org.junit.Assert.*;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import games.stendhal.client.entity.Player;
import games.stendhal.client.gui.j2d.entity.helpers.HorizontalAlignment;
import games.stendhal.client.gui.j2d.entity.helpers.VerticalAlignment;
import games.stendhal.client.sprite.Sprite;

public class PlayerMoodIconPositionTest {
	@Test
	public void moodIconsUseLoweredLeftAndRightAnchors() throws Exception {
		final RecordingView view = new RecordingView();
		final TestPlayer player = new TestPlayer();
		final RPEntity2DView<Player>.AbstractStatusIconManager away = manager(view, Player.PROP_AWAY);
		final RPEntity2DView<Player>.AbstractStatusIconManager grumpy = manager(view, Player.PROP_GRUMPY);
		player.away = true;
		player.grumpy = true;
		away.check(Player.PROP_AWAY, player);
		away.apply();
		grumpy.check(Player.PROP_GRUMPY, player);
		grumpy.apply();
		assertEquals(new Point(26, 6), view.position(sprite(away)));
		assertEquals(new Point(0, 6), view.position(sprite(grumpy)));
	}

	@Test
	public void togglingEitherMoodDoesNotLoseOrAccumulateConfiguredOffsets() throws Exception {
		final RecordingView view = new RecordingView();
		final TestPlayer player = new TestPlayer();
		final RPEntity2DView<Player>.AbstractStatusIconManager away = manager(view, Player.PROP_AWAY);
		final RPEntity2DView<Player>.AbstractStatusIconManager grumpy = manager(view, Player.PROP_GRUMPY);
		for (int repeat = 0; repeat < 3; repeat++) {
			player.away = true;
			player.grumpy = true;
			away.check(Player.PROP_AWAY, player);
			away.apply();
			grumpy.check(Player.PROP_GRUMPY, player);
			grumpy.apply();
			assertEquals(new Point(26, 6), view.position(sprite(away)));
			assertEquals(new Point(0, 6), view.position(sprite(grumpy)));
			player.away = false;
			away.check(Player.PROP_AWAY, player);
			away.apply();
			assertNull(view.position(sprite(away)));
			assertEquals(new Point(0, 6), view.position(sprite(grumpy)));
			player.grumpy = false;
			grumpy.check(Player.PROP_GRUMPY, player);
			grumpy.apply();
			assertTrue(view.positions.isEmpty());
		}
	}

	@Test
	public void playerKillerIconStillStacksBesideGrumpyAndReturnsWhenGrumpyIsHidden() throws Exception {
		final RecordingView view = new RecordingView();
		final TestPlayer player = new TestPlayer();
		final RPEntity2DView<Player>.AbstractStatusIconManager grumpy = manager(view, Player.PROP_GRUMPY);
		final RPEntity2DView<Player>.AbstractStatusIconManager skull = manager(view, Player.PROP_PLAYER_KILLER);
		player.grumpy = true;
		player.killer = true;
		grumpy.check(Player.PROP_GRUMPY, player);
		grumpy.apply();
		skull.check(Player.PROP_PLAYER_KILLER, player);
		skull.apply();
		assertEquals(new Point(sprite(grumpy).getWidth(), 0), view.position(sprite(skull)));
		player.grumpy = false;
		grumpy.check(Player.PROP_GRUMPY, player);
		grumpy.apply();
		assertEquals(new Point(0, 0), view.position(sprite(skull)));
	}

	@SuppressWarnings("unchecked")
	private RPEntity2DView<Player>.AbstractStatusIconManager manager(final RecordingView view,
			final Object property) throws Exception {
		final Field managers = RPEntity2DView.class.getDeclaredField("iconManagers");
		managers.setAccessible(true);
		for (final Object value : (List<?>) managers.get(view)) {
			final Field observed = value.getClass().getSuperclass().getDeclaredField("property");
			observed.setAccessible(true);
			if (observed.get(value) == property) {
				return (RPEntity2DView<Player>.AbstractStatusIconManager) value;
			}
		}
		throw new AssertionError("Missing mood manager");
	}

	private Sprite sprite(final RPEntity2DView<Player>.AbstractStatusIconManager manager) throws Exception {
		final Field field = manager.getClass().getSuperclass().getDeclaredField("sprite");
		field.setAccessible(true);
		return (Sprite) field.get(manager);
	}

	private static final class RecordingView extends Player2DView<Player> {
		private final Map<Sprite, Point> positions = new IdentityHashMap<>();
		@Override public int getWidth() { return 48; }
		@Override public void attachSprite(final Sprite sprite, final HorizontalAlignment horizontal,
				final VerticalAlignment vertical, final int x, final int y) {
			assertEquals(VerticalAlignment.TOP, vertical);
			positions.put(sprite, new Point(horizontal == HorizontalAlignment.RIGHT
					? getWidth() - sprite.getWidth() + x : x, y));
		}
		@Override public void detachSprite(final Sprite sprite) { positions.remove(sprite); }
		private Point position(final Sprite sprite) { return positions.get(sprite); }
	}

	private static final class TestPlayer extends Player {
		private boolean away, grumpy, killer;
		@Override public boolean isAway() { return away; }
		@Override public boolean isGrumpy() { return grumpy; }
		@Override public boolean isBadBoy() { return killer; }
	}
}
