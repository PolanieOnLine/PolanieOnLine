/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.junit.Test;

import games.stendhal.client.gui.AdventCalendarDialog.Door;
import games.stendhal.client.gui.AdventCalendarDialog.RewardIcon;

public class AdventCalendarDoorTest {
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
