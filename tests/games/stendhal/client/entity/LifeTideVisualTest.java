package games.stendhal.client.entity;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.gui.LifeTideOverlay;
import games.stendhal.client.gui.stats.HPIndicator;
import games.stendhal.client.util.UserInterfaceTestHelper;
import games.stendhal.common.NotificationType;
import marauroa.common.game.RPObject;

public class LifeTideVisualTest {
	@BeforeClass
	public static void initializeUI() {
		UserInterfaceTestHelper.initUserInterface();
	}

	@Test
	public void naturalDecayAndCombatDamageHaveSeparateFloaters() {
		final RecordingEntity entity = new RecordingEntity();
		final RPObject object = object(12000, 40);
		entity.initialize(object);
		assertTrue(entity.floaters.isEmpty());
		assertEquals(0.2, entity.getSurplusHPRatio(), 0.00001);
		entity.onChangedAdded(object, object(11880, 60));
		assertEquals(Arrays.asList("LIFE_TIDE:-20", "NEGATIVE:-100"), entity.floaters);
		entity.floaters.clear();
		entity.onChangedAdded(object, object(11860, 80));
		assertEquals(Arrays.asList("LIFE_TIDE:-20"), entity.floaters);
	}

	@Test
	public void counterOnlyUpdateCanSeparateDecayFromSimultaneousHealing() {
		final RecordingEntity entity = new RecordingEntity();
		final RPObject object = object(12000, 0);
		entity.initialize(object);
		final RPObject changes = new RPObject();
		changes.put("life_tide_decay", 20);
		entity.onChangedAdded(object, changes);
		assertEquals(Arrays.asList("LIFE_TIDE:-20", "POSITIVE:+20"), entity.floaters);
	}

	@Test
	public void overlayCoversOnlySurplusAndBreathesWithoutLeakingGraphicsState() {
		final BufferedImage low = renderOverlay(0, 0.2);
		final BufferedImage high = renderOverlay(1200, 0.2);
		assertNotEquals(Color.GREEN.getRGB(), low.getRGB(19, 1));
		assertEquals("Diagonal gaps reveal the original HP bar", Color.GREEN.getRGB(), low.getRGB(6, 1));
		assertEquals(Color.GREEN.getRGB(), low.getRGB(20, 1));
		assertEquals(Color.GREEN.getRGB(), high.getRGB(99, 1));
		assertNotEquals(low.getRGB(0, 1), high.getRGB(0, 1));
		assertTrue("Breathing must be visible on the thin health bar",
				new Color(high.getRGB(0, 1)).getRed() - new Color(low.getRGB(0, 1)).getRed() >= 50);
		assertEquals(Color.GREEN.getRGB(), renderOverlay(0, 0).getRGB(0, 1));
	}

	@Test
	public void hudShowsTheSamePartialOverlayAndRemovesItAtNormalMaximum() {
		final HPIndicator indicator = new HPIndicator();
		indicator.setSize(102, 6);
		indicator.getModel().setMaxRepresentation(100);
		indicator.setHP(10000, 12000);
		final BufferedImage image = new BufferedImage(102, 6, BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			indicator.paintComponent(graphics);
			assertNotEquals(Color.GREEN.getRGB(), image.getRGB(19, 2));
			assertEquals(Color.GREEN.getRGB(), image.getRGB(22, 2));
			indicator.setHP(10000, 10000);
			indicator.paintComponent(graphics);
			assertEquals(Color.GREEN.getRGB(), image.getRGB(19, 2));
		} finally {
			graphics.dispose();
		}
	}

	@Test
	public void idleHudPulseRepaintsWithoutHPUpdatesAndStopsAtNormalHP() throws Exception {
		final RecordingIndicator indicator = new RecordingIndicator();
		try {
			SwingUtilities.invokeAndWait(() -> {
				indicator.setHP(10000, 11915);
				indicator.repaints = new CountDownLatch(2);
			});
			assertTrue("Idle surplus must keep requesting repaints",
					indicator.repaints.await(1, TimeUnit.SECONDS));
			SwingUtilities.invokeAndWait(() -> {
				indicator.setHP(10000, 10000);
				indicator.repaints = new CountDownLatch(1);
			});
			assertFalse("Normal HP must not keep the animation timer alive",
					indicator.repaints.await(200, TimeUnit.MILLISECONDS));
		} finally {
			SwingUtilities.invokeAndWait(indicator::removeNotify);
		}
	}

	private static class RecordingIndicator extends HPIndicator {
		volatile CountDownLatch repaints;

		@Override
		public boolean isShowing() {
			return true;
		}

		@Override
		public void repaint() {
			if (repaints != null) {
				repaints.countDown();
			}
		}
	}

	private BufferedImage renderOverlay(final long time, final double ratio) {
		final BufferedImage image = new BufferedImage(100, 4, BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics = image.createGraphics();
		try {
			graphics.setColor(Color.GREEN);
			graphics.fillRect(0, 0, 100, 4);
			LifeTideOverlay.draw(graphics, 0, 0, 100, 4, ratio, time);
			assertEquals(Color.GREEN, graphics.getColor());
		} finally {
			graphics.dispose();
		}
		return image;
	}

	private RPObject object(final int hp, final int counter) {
		final RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 1);
		object.put("hp", hp);
		object.put("base_hp", 10000);
		object.put("life_tide_decay", counter);
		return object;
	}

	private static class RecordingEntity extends RPEntity {
		final List<String> floaters = new ArrayList<>();

		@Override
		protected void addTextIndicator(final String text, final NotificationType type) {
			floaters.add(type.name() + ":" + text);
		}

		@Override
		public boolean isInHearingRange() {
			return true;
		}
	}
}
