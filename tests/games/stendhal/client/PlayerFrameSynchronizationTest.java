package games.stendhal.client;

import static org.junit.Assert.assertEquals;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

import games.stendhal.client.entity.Entity;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.gui.j2d.RemovableSprite;
import games.stendhal.client.gui.j2d.entity.Entity2DView;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.TextSprite;

public class PlayerFrameSynchronizationTest {
	@After
	public void resetFrame() {
		GameScreenSpriteHelper.endFrame();
		GameScreenSpriteHelper.setWorldWidth(0);
		GameScreenSpriteHelper.setWorldHeight(0);
		GameScreenSpriteHelper.setScale(1.0);
		GameScreenSpriteHelper.setTexts(new ArrayList<RemovableSprite>());
	}

	@Test
	public void followedPlayerDoesNotWobbleWhenCameraAndPlayerMoveTogether() {
		for (double dpi : new double[] {1.0, 1.25, 1.5, 2.0}) {
			for (double zoom : new double[] {1.0, 1.3, 1.5, 2.0}) {
				Point2D previous = null;
				Point2D previousAttachment = null;
				final TestEntity entity = new TestEntity();
				final ProbeView view = new ProbeView();
				view.initialize(entity);
				for (int frame = 0; frame < 90; frame++) {
					final double movement = frame * 0.23;
					entity.move((90.4 + movement) / 32, (91.7 + movement) / 32);
					final Graphics2D destination = new BufferedImage(300, 300,
							BufferedImage.TYPE_INT_ARGB).createGraphics();
					destination.scale(dpi, dpi);
					final PixelArtViewport viewport = new PixelArtViewport(destination, zoom,
							20.1 + movement, 21.2 + movement);
					GameScreenSpriteHelper.beginFrame(viewport);
					final Graphics2D world = viewport.createWorldGraphics(destination);
					try {
						view.draw(world);
						final Point2D body = view.anchor;
						final Point2D attachment = new Point2D.Double(
								GameScreenSpriteHelper.getEntityScreenX(entity, true) - GameScreenSpriteHelper.getScreenViewX(),
								GameScreenSpriteHelper.getEntityScreenY(entity) - GameScreenSpriteHelper.getScreenViewY());
						view.drawTop(world);
						assertEquals(body.getX(), view.anchor.getX(), 0.000001);
						assertEquals(body.getY(), view.anchor.getY(), 0.000001);
						if (previous != null) {
							final String message = "own player must not alternate between camera rounding phases: dpi="
									+ dpi + ", zoom=" + zoom + ", frame=" + frame;
							assertEquals(message, previous.getX(), body.getX(), 0.000001);
							assertEquals(message, previous.getY(), body.getY(), 0.000001);
							assertEquals("attached text must remain still while following the player", previousAttachment, attachment);
						}
						previous = body;
						previousAttachment = attachment;
					} finally {
						world.dispose();
						destination.dispose();
						GameScreenSpriteHelper.endFrame();
					}
				}
			}
		}
	}

	@Test
	public void arenaAndAdminRemoteViewsRemainAvailableUntilThePlayerMoves() {
		final PlayerCameraFocus focus = new PlayerCameraFocus();
		focus.capturePlayer(32, 47);
		focus.requestView(32, 19);
		for (int frame = 0; frame < 30; frame++) {
			focus.capturePlayer(32, 47);
			org.junit.Assert.assertFalse(focus.followsPlayer());
			assertEquals(32.0, focus.getX(), 0.0);
			assertEquals(19.0, focus.getY(), 0.0);
		}
		focus.capturePlayer(32.2, 47);
		org.junit.Assert.assertTrue(focus.followsPlayer());
		assertEquals(32.2, focus.getX(), 0.0);
		assertEquals(47.0, focus.getY(), 0.0);
		focus.requestView(5, 6);
		focus.clearRemoteView();
		org.junit.Assert.assertTrue("zone changes must clear the old remote focus", focus.followsPlayer());
	}

	@Test
	public void bodyNameBubbleAndEmojiUseTheSameSnapshotDespiteNewLogicUpdate() {
		for (final boolean ownPlayer : new boolean[] {true, false}) {
			final TestEntity entity = new TestEntity(ownPlayer);
			entity.move(10.2, 12.3);
			final ProbeView view = new ProbeView();
			view.initialize(entity);
			final Graphics2D destination = new BufferedImage(400, 400,
					BufferedImage.TYPE_INT_ARGB).createGraphics();
			destination.scale(1.25, 1.25);
			GameScreenSpriteHelper.setScale(1.3);
			GameScreenSpriteHelper.setTexts(new ArrayList<RemovableSprite>());
			final PixelArtViewport viewport = new PixelArtViewport(destination, 1.3, 280.4, 320.3);
			final Map<IEntity, EntityRenderPosition> positions = new IdentityHashMap<IEntity, EntityRenderPosition>();
			positions.put(entity, view.beginRenderFrame());
			GameScreenSpriteHelper.beginFrame(viewport, positions);
			final Graphics2D world = viewport.createWorldGraphics(destination);
			try {
				final CaptureSprite sprite = new CaptureSprite();
				final RemovableSprite bubble = new RemovableSprite(sprite, entity, 0);
				view.draw(world);
				final Point2D before = view.anchor;
				final Rectangle area = bubble.getArea();
				bubble.drawEmoji(destination, GameScreenSpriteHelper.getScreenViewX(),
						GameScreenSpriteHelper.getScreenViewY());
				final Point2D emoji = new Point2D.Double(sprite.x, sprite.y);
				entity.move(10.75, 12.9);
				view.draw(world);
				assertEquals("body must not consume a newer logic position in the same frame", before, view.anchor);
				view.drawTop(world);
				assertEquals("name/HP must share the body snapshot", before, view.anchor);
				assertEquals("bubble must not use the live owner position", area, bubble.getArea());
				bubble.drawEmoji(destination, GameScreenSpriteHelper.getScreenViewX(),
						GameScreenSpriteHelper.getScreenViewY());
				assertEquals(emoji, new Point2D.Double(sprite.x, sprite.y));
				view.endRenderFrame();
				GameScreenSpriteHelper.endFrame();
				positions.clear();
				positions.put(entity, view.beginRenderFrame());
				GameScreenSpriteHelper.beginFrame(viewport, positions);
				view.draw(world);
				org.junit.Assert.assertNotEquals("movement must appear in the following frame", before, view.anchor);
				org.junit.Assert.assertNotEquals(area, bubble.getArea());
			} finally {
				view.endRenderFrame();
				GameScreenSpriteHelper.endFrame();
				world.dispose();
				destination.dispose();
			}
		}
	}

	@Test
	public void renderedNameHealthBarAndBubblePixelsStayIdenticalWhileTracking() {
		for (final double dpi : new double[] {1.0, 1.25, 1.5}) {
			for (final double zoom : new double[] {1.0, 1.3, 1.5, 2.0}) {
				final TestEntity player = new TestEntity();
				final PaintedView view = new PaintedView();
				view.initialize(player);
				final TextSprite bubbleText = TextSprite.createTextSprite("Test chmurki", Color.WHITE);
				int[] previous = null;
				GameScreenSpriteHelper.setScale(zoom);
				GameScreenSpriteHelper.setTexts(new ArrayList<RemovableSprite>());
				for (int frame = 0; frame < 25; frame++) {
					final double distance = frame * 0.23;
					player.move((90.4 + distance) / 32, (91.7 + distance) / 32);
					final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
					final Graphics2D destination = image.createGraphics();
					destination.scale(dpi, dpi);
					final PixelArtViewport viewport = new PixelArtViewport(destination, zoom,
							20.1 + distance, 21.2 + distance);
					final Map<IEntity, EntityRenderPosition> positions = new IdentityHashMap<IEntity, EntityRenderPosition>();
					positions.put(player, view.beginRenderFrame());
					GameScreenSpriteHelper.beginFrame(viewport, positions);
					final Graphics2D world = viewport.createWorldGraphics(destination);
					final Graphics2D screenText = (Graphics2D) destination.create();
					try {
						view.draw(world);
						view.drawTop(world);
						screenText.translate(-GameScreenSpriteHelper.getScreenViewX(),
								-GameScreenSpriteHelper.getScreenViewY());
						new RemovableSprite(bubbleText, player, 0).draw(screenText);
						final int[] pixels = image.getRGB(0, 0, 400, 400, null, 0, 400);
						if (previous != null) {
							org.junit.Assert.assertArrayEquals("body/name/HP/bubble pixels must remain unchanged: dpi="
									+ dpi + ", zoom=" + zoom + ", frame=" + frame, previous, pixels);
						}
						previous = pixels;
					} finally {
						view.endRenderFrame();
						GameScreenSpriteHelper.endFrame();
						world.dispose();
						screenText.dispose();
						destination.dispose();
					}
				}
			}
		}
	}

	@Test
	public void npcKeepsItsExistingMapAlignedRendering() {
		final TestEntity npc = new TestEntity(false);
		npc.move(3.2, 4.7);
		final ProbeView view = new ProbeView();
		view.initialize(npc);
		Point2D previous = null;
		double previousCameraX = 0;
		for (int frame = 0; frame < 40; frame++) {
			final Graphics2D destination = new BufferedImage(300, 300,
					BufferedImage.TYPE_INT_ARGB).createGraphics();
			destination.scale(1.25, 1.25);
			final PixelArtViewport viewport = new PixelArtViewport(destination, 1.3, 20 + frame * 0.23, 21);
			GameScreenSpriteHelper.beginFrame(viewport);
			final Graphics2D world = viewport.createWorldGraphics(destination);
			try {
				view.draw(world);
				final double cameraX = world.getTransform().getTranslateX();
				if (previous != null) {
					assertEquals("NPC pixels must still follow the map", cameraX - previousCameraX,
							view.anchor.getX() - previous.getX(), 0.000001);
				}
				previous = view.anchor;
				previousCameraX = cameraX;
			} finally {
				world.dispose();
				destination.dispose();
			}
		}
	}

	private static class TestEntity extends Entity {
		private final boolean ownPlayer;
		TestEntity() { this(true); }
		TestEntity(boolean ownPlayer) { this.ownPlayer = ownPlayer; }
		void move(double x, double y) {
			this.x = x;
			this.y = y;
			onPosition(x, y);
		}
		@Override
		public boolean isUser() { return ownPlayer; }
		@Override
		public double getWidth() { return 1.0; }
	}

	private static class ProbeView extends Entity2DView<TestEntity> {
		Point2D anchor;
		@Override
		protected void buildRepresentation(TestEntity entity) { }
		@Override
		protected void draw(Graphics2D g, int x, int y, int width, int height) {
			anchor = g.getTransform().transform(new Point2D.Double(x, y), null);
		}
		@Override
		protected void drawTop(Graphics2D g, int x, int y, int width, int height) {
			draw(g, x, y, width, height);
		}
	}

	private static class CaptureSprite extends ImageSprite {
		int x;
		int y;
		CaptureSprite() { super(new BufferedImage(24, 15, BufferedImage.TYPE_INT_ARGB)); }
		@Override
		public void draw(Graphics graphics, int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	private static class PaintedView extends ProbeView {
		private final TextSprite name = TextSprite.createTextSprite("karajuss", Color.YELLOW);
		@Override
		protected void draw(Graphics2D g, int x, int y, int width, int height) {
			super.draw(g, x, y, width, height);
			g.setColor(Color.BLUE);
			g.fillRect(x, y, 20, 32);
		}
		@Override
		protected void drawTop(Graphics2D g, int x, int y, int width, int height) {
			super.draw(g, x, y, width, height);
			g.setColor(Color.GREEN);
			g.fillRect(x - 2, y - 10, 36, 3);
			name.drawPixelAligned(g, x - 10, y - 30);
		}
	}
}
