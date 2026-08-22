/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.util.UserInterfaceTestHelper;

public class WindowModeTest {
	@BeforeClass
	public static void init() {
		UserInterfaceTestHelper.initUserInterface();
	}

	@Test
	public void resolvesPersistedModes() {
		assertEquals(WindowMode.WINDOWED, WindowMode.fromProperty("windowed"));
		assertEquals(WindowMode.BORDERLESS, WindowMode.fromProperty("borderless"));
		assertEquals(WindowMode.FULLSCREEN, WindowMode.fromProperty("fullscreen"));
	}

	@Test
	public void unknownAndMissingValuesUseSafeWindowedMode() {
		assertEquals(WindowMode.WINDOWED, WindowMode.fromProperty(null));
		assertEquals(WindowMode.WINDOWED, WindowMode.fromProperty("broken"));
		assertTrue(WindowMode.WINDOWED.isWindowed());
	}

	@Test
	public void exposesPolishLabelsForTheGameClientSelectors() {
		assertEquals("Okno", WindowMode.WINDOWED.toString());
		assertEquals("Pełny ekran w oknie", WindowMode.BORDERLESS.toString());
		assertEquals("Pełny ekran", WindowMode.FULLSCREEN.toString());
	}

	@Test
	public void doesNotRecreateAnAlreadyWindowedFrame() {
		CountingFrame frame = new CountingFrame();
		try {
			frame.pack();
			WindowModeController.apply(frame, WindowMode.WINDOWED);
			assertEquals(0, frame.disposeCount);
			assertFalse(frame.isUndecorated());
			assertTrue(frame.isResizable());
		} finally {
			frame.dispose();
		}
	}

	@Test
	public void dynamicallySwitchesBetweenWindowAndBorderlessModes() {
		CountingFrame frame = new CountingFrame();
		try {
			frame.setBounds(40, 50, 320, 240);
			frame.pack();
			frame.setBounds(40, 50, 320, 240);
			Rectangle windowedBounds = frame.getBounds();

			WindowModeController.apply(frame, WindowMode.BORDERLESS);
			assertTrue(frame.isUndecorated());
			assertFalse(frame.isResizable());

			WindowModeController.apply(frame, WindowMode.WINDOWED);
			assertFalse(frame.isUndecorated());
			assertTrue(frame.isResizable());
			assertEquals(windowedBounds, frame.getBounds());
			assertEquals(1, frame.disposeCount);
		} finally {
			frame.dispose();
		}
	}

	@Test
	public void preservesManagedWindowVisibilityAndMinimization() {
		CountingFrame frame = new CountingFrame();
		try {
			JPanel content = new JPanel();
			InternalManagedWindow hidden = createManagedWindow("mode_test_hidden");
			InternalManagedWindow visible = createManagedWindow("mode_test_visible");
			content.add(hidden);
			content.add(visible);
			frame.setContentPane(content);
			frame.pack();
			frame.setVisible(true);

			hidden.setMinimized(true);
			hidden.setVisible(false);
			visible.setMinimized(false);
			visible.setVisible(true);

			WindowModeController.apply(frame, WindowMode.BORDERLESS);

			assertFalse(hidden.isVisible());
			assertTrue(hidden.isMinimized());
			assertTrue(visible.isVisible());
			assertFalse(visible.isMinimized());
		} finally {
			frame.dispose();
		}
	}

	@Test
	public void contentUpdatesOnlyPackNormalWindows() {
		CountingFrame frame = new CountingFrame();
		try {
			frame.setContentPane(new JPanel());
			frame.pack();
			frame.packCount = 0;
			Rectangle bounds = new Rectangle(20, 30, 640, 480);
			frame.setBounds(bounds);

			WindowModeController.fitToPreferredSize(frame, WindowMode.BORDERLESS);
			WindowModeController.fitToPreferredSize(frame, WindowMode.FULLSCREEN);

			assertEquals(0, frame.packCount);
			assertEquals(bounds, frame.getBounds());

			WindowModeController.fitToPreferredSize(frame, WindowMode.WINDOWED);
			assertEquals(1, frame.packCount);
		} finally {
			frame.dispose();
		}
	}

	private InternalManagedWindow createManagedWindow(String name) {
		InternalManagedWindow window = new InternalManagedWindow(name, name);
		window.setContent(new JLabel(name));
		return window;
	}

	private static final class CountingFrame extends JFrame {
		private static final long serialVersionUID = 1L;
		private int disposeCount;
		private int packCount;

		@Override
		public void pack() {
			packCount++;
			super.pack();
		}

		@Override
		public void dispose() {
			disposeCount++;
			super.dispose();
		}
	}
}
