/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import org.junit.Test;

import games.stendhal.client.gui.AdventCalendarDialog.Door;
import games.stendhal.client.gui.AdventCalendarDialog.RewardIcon;

public class AdventCalendarDoorTest {
	@Test
	public void successfulClaimAnimatesOnceAndRefreshPreservesOpening() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final Door door = new Door(24);
			door.updateState("claimed", true);
			door.reveal(Arrays.asList(new RewardIcon("food", "potato", "ziemniaki", 25)), true);
			assertTrue("The claim should briefly swing the door open", door.isOpening());
			door.updateState("claimed", true);
			assertTrue("A refresh must not cancel the opening", door.isOpening());
			door.advanceOpening(250_000_000L);
			assertFalse("The animation must finish without a running timer", door.isOpening());
			assertTrue(door.showingRewards());
			door.updateState("claimed", true);
			door.reveal(Arrays.asList(new RewardIcon("food", "potato", "ziemniaki", 25)));
			assertFalse("Viewing an already received gift must not replay opening", door.isOpening());
			door.clearRewards();
		});
	}

	@Test
	public void openingFramesChangeUntilTheyMatchTheAlreadyOpenDoor() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final Door door = new Door(24);
			door.setSize(160, 176);
			door.updateState("claimed", true);
			final RewardIcon reward = new RewardIcon("food", "potato", "ziemniaki", 25);
			door.reveal(Arrays.asList(reward));
			final int[] opened = pixels(door);
			door.reveal(Arrays.asList(reward), true);
			final int[] closed = pixels(door);
			assertFalse("The first frame must cover the gift", Arrays.equals(closed, opened));
			door.advanceOpening(80_000_000L);
			final int[] middle = pixels(door);
			assertFalse("The leaves must move", Arrays.equals(closed, middle));
			assertFalse("The gift appears after the swing finishes", Arrays.equals(middle, opened));
			door.advanceOpening(200_000_000L);
			assertTrue("The final frame must match a normally opened door", Arrays.equals(opened, pixels(door)));
		});
	}

	private static int[] pixels(final Door door) {
		final BufferedImage frame = new BufferedImage(door.getWidth(), door.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = frame.createGraphics();
		try {
			door.paint(graphics);
		} finally {
			graphics.dispose();
		}
		return frame.getRGB(0, 0, frame.getWidth(), frame.getHeight(), null, 0, frame.getWidth());
	}

	@Test
	public void wideSingleLeafStaysWoodenAcrossItsFullWidthWhileOpening() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final Door door = new Door(12);
			door.setSize(160, 84);
			door.updateState("claimed", false);
			door.reveal(Arrays.asList(new RewardIcon("food", "potato", "ziemniaki", 1)), true);
			final int[] frame = pixels(door);
			assertEquals("Both halves of a wide leaf must repeat the wooden texture",
					frame[40 * 160 + 40], frame[40 * 160 + 120]);
			door.clearRewards();
		});
	}

	@Test
	public void clearingOrInvalidatingAClaimStopsItsAnimation() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			for (final String next : new String[] {"inactive", "future", "available", "expired"}) {
				final Door door = new Door(1);
				door.updateState("claimed", false);
				door.reveal(Arrays.asList(new RewardIcon("food", "potato", "ziemniaki", 1)), true);
				assertTrue(door.isOpening());
				door.updateState(next, false);
				assertFalse("State change must stop the timer: " + next, door.isOpening());
				assertFalse(door.showingRewards());
			}
			final Door closed = new Door(24);
			closed.updateState("claimed", true);
			closed.reveal(Arrays.asList(new RewardIcon("food", "potato", "ziemniaki", 1)), true);
			closed.clearRewards();
			assertFalse("Closing the calendar must stop all door animations", closed.isOpening());
		});
	}

	@Test
	public void onlyClaimedDoorCanRevealItsContentsAndRefreshKeepsThem() {
		final Door door = new Door(12);
		final RewardIcon reward = new RewardIcon("food", "potato", "ziemniaki", 25);
		door.updateState("available", false);
		door.reveal(Arrays.asList(reward));
		assertFalse(door.showingRewards());
		assertNull(door.getToolTipText());
		door.updateState("claimed", false);
		door.reveal(Arrays.asList(reward));
		assertTrue(door.showingRewards());
		assertTrue(door.getToolTipText().contains("ziemniaki × 25"));
		door.updateState("claimed", true);
		assertTrue("A normal refresh must not replace the current preview with a checkmark", door.showingRewards());
	}

	@Test
	public void closingAndReopeningKeepsClaimedAppearanceWithoutOldRewardPreviews() {
		final Door first = new Door(24);
		first.updateState("claimed", true);
		first.reveal(Arrays.asList(new RewardIcon("food", "potato", "ziemniaki", 25)));
		assertTrue(first.showingRewards());
		first.clearRewards();
		assertFalse(first.showingRewards());
		assertNull(first.getToolTipText());
		final Door reopened = new Door(24);
		reopened.updateState("claimed", true);
		assertFalse(reopened.showingRewards());
		assertNull(reopened.getToolTipText());
	}

	@Test
	public void inactiveOrUnclaimedStateRemovesPreviewContents() {
		for (final String next : new String[] {"inactive", "future", "available", "expired"}) {
			final Door door = new Door(3);
			door.updateState("claimed", false);
			door.reveal(Arrays.asList(new RewardIcon("food", "potato", "ziemniaki", 25)));
			door.updateState(next, false);
			assertFalse(next, door.showingRewards());
			assertNull(door.getToolTipText());
		}
	}

	@Test
	public void singleAndDoubleDoorsKeepThreeRewardSpritesInsideTheirRecess() {
		for (final int day : new int[] {3, 24}) {
			final Door door = new Door(day);
			final int width = day == 24 ? 160 : 76;
			final int height = day == 24 ? 176 : 84;
			door.setSize(width, height);
			door.updateState("claimed", true);
			door.reveal(Arrays.asList(new RewardIcon("food", "potato", "ziemniaki", 25000),
					new RewardIcon("food", "potato", "ziemniaki", 1),
					new RewardIcon("food", "potato", "ziemniaki", 20)));
			final Rectangle area = door.rewardArea(width, height);
			assertTrue(area.width > 0 && area.height > 0);
			assertTrue(new Rectangle(0, 0, width, height).contains(area));
			final BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D graphics = frame.createGraphics();
			try {
				door.paint(graphics);
			} finally {
				graphics.dispose();
			}
			assertTrue(door.showingRewards());
		}
	}
}
