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

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import games.stendhal.client.gui.wt.core.WtWindowManager;

/**
 * Applies the selected main window mode without changing the game's render
 * resolution. Borderless and exclusive modes therefore work together with
 * the existing screen scaling option.
 */
public final class WindowModeController {
	/** Property used to persist the selected main window mode. */
	public static final String WINDOW_MODE_PROPERTY = "ui.window_mode";

	private static final Map<JFrame, WindowedState> WINDOWED_STATES =
			new WeakHashMap<JFrame, WindowedState>();

	private WindowModeController() {
	}

	/**
	 * @return configured mode, safely falling back to a normal window
	 */
	public static WindowMode getConfiguredMode() {
		String value = WtWindowManager.getInstance().getProperty(
				WINDOW_MODE_PROPERTY, WindowMode.WINDOWED.getPropertyValue());
		return WindowMode.fromProperty(value);
	}

	/**
	 * Persist a selected mode. It is applied to the game window on the next
	 * client start.
	 *
	 * @param mode selected mode
	 */
	public static void select(WindowMode mode) {
		persist(mode);
	}

	/**
	 * Persist and dynamically apply a mode to the running game client.
	 *
	 * @param frame main game frame
	 * @param mode selected mode
	 */
	public static void select(final JFrame frame, final WindowMode mode) {
		persist(mode);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				apply(frame, mode);
			}
		});
	}

	private static void persist(WindowMode mode) {
		WtWindowManager manager = WtWindowManager.getInstance();
		manager.setProperty(WINDOW_MODE_PROPERTY, mode.getPropertyValue());
		manager.save();
	}

	/**
	 * Apply the persisted mode to a frame.
	 *
	 * @param frame main frame
	 */
	public static void applyConfigured(JFrame frame) {
		apply(frame, getConfiguredMode());
	}

	/**
	 * Fit changed content in a normal window without shrinking either
	 * fullscreen mode back to the content's preferred size.
	 *
	 * @param frame main frame
	 */
	static void fitToPreferredSize(JFrame frame) {
		fitToPreferredSize(frame, getConfiguredMode());
	}

	static void fitToPreferredSize(JFrame frame, WindowMode mode) {
		if (mode.isWindowed() && !frame.isUndecorated()) {
			frame.pack();
		} else {
			refresh(frame);
		}
	}

	/**
	 * Apply a mode to a frame. Exclusive fullscreen gracefully falls back to a
	 * borderless fullscreen window when the platform does not support it.
	 *
	 * @param frame main frame
	 * @param mode selected mode
	 */
	static void apply(JFrame frame, WindowMode mode) {
		List<ManagedWindowState> managedWindowStates = captureManagedWindowStates(frame);
		GraphicsConfiguration configuration = frame.getGraphicsConfiguration();
		GraphicsDevice device = configuration.getDevice();
		boolean wasVisible = frame.isVisible();
		boolean targetUndecorated = !mode.isWindowed();
		boolean decorationChange = frame.isUndecorated() != targetUndecorated;

		if (!mode.isWindowed() && !frame.isUndecorated()) {
			WINDOWED_STATES.put(frame,
					new WindowedState(frame.getBounds(), frame.getExtendedState()));
		}

		leaveExclusiveFullScreen(frame);

		// Recreating an already decorated normal window was the cause of the
		// empty client contents seen until the first minimize or resize.
		if (decorationChange && frame.isDisplayable()) {
			frame.dispose();
		}

		if (decorationChange) {
			frame.setUndecorated(targetUndecorated);
		}
		frame.setResizable(mode.isWindowed());

		try {
			if (mode.isWindowed()) {
				WindowedState state = WINDOWED_STATES.remove(frame);
				if (state != null) {
					frame.setExtendedState(JFrame.NORMAL);
					frame.setBounds(state.bounds);
					frame.setExtendedState(state.extendedState);
				} else if (!frame.isDisplayable()) {
					frame.pack();
				}
				showAndRefresh(frame, wasVisible);
				return;
			}

			frame.setExtendedState(JFrame.NORMAL);
			if (mode == WindowMode.FULLSCREEN && device.isFullScreenSupported()
					&& device.getFullScreenWindow() == null) {
				try {
					device.setFullScreenWindow(frame);
					refresh(frame);
					return;
				} catch (RuntimeException e) {
					// Some window managers report support but reject exclusive mode.
					if (device.getFullScreenWindow() == frame) {
						device.setFullScreenWindow(null);
					}
				}
			}

			frame.setBounds(configuration.getBounds());
			showAndRefresh(frame, wasVisible);
		} finally {
			restoreManagedWindowStates(managedWindowStates);
		}
	}

	private static List<ManagedWindowState> captureManagedWindowStates(final JFrame frame) {
		List<ManagedWindowState> states = new ArrayList<ManagedWindowState>();
		captureManagedWindowStates(frame.getContentPane(), states);
		return states;
	}

	private static void captureManagedWindowStates(final Component component,
			final List<ManagedWindowState> states) {
		if (component instanceof ManagedWindow) {
			ManagedWindow window = (ManagedWindow) component;
			states.add(new ManagedWindowState(window, window.isVisible(), window.isMinimized()));
		}
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				captureManagedWindowStates(child, states);
			}
		}
	}

	private static void restoreManagedWindowStates(final List<ManagedWindowState> states) {
		for (ManagedWindowState state : states) {
			state.window.setMinimized(state.minimized);
			state.window.setVisible(state.visible);
		}
	}

	private static void leaveExclusiveFullScreen(JFrame frame) {
		for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			if (device.getFullScreenWindow() == frame) {
				device.setFullScreenWindow(null);
				return;
			}
		}
	}

	private static void showAndRefresh(JFrame frame, boolean wasVisible) {
		if (wasVisible && !frame.isVisible()) {
			frame.setVisible(true);
		}
		refresh(frame);
	}

	/**
	 * Validate and repaint the complete frame after its native peer or bounds
	 * have changed.
	 *
	 * @param frame frame to refresh
	 */
	static void refresh(final JFrame frame) {
		frame.invalidate();
		frame.validate();
		frame.getContentPane().revalidate();
		frame.getContentPane().repaint();
		frame.repaint();
		Toolkit.getDefaultToolkit().sync();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (frame.isDisplayable()) {
					frame.validate();
					frame.repaint();
				}
			}
		});
	}

	private static final class WindowedState {
		private final Rectangle bounds;
		private final int extendedState;

		WindowedState(Rectangle bounds, int extendedState) {
			this.bounds = new Rectangle(bounds);
			this.extendedState = extendedState;
		}
	}

	private static final class ManagedWindowState {
		private final ManagedWindow window;
		private final boolean visible;
		private final boolean minimized;

		ManagedWindowState(ManagedWindow window, boolean visible, boolean minimized) {
			this.window = window;
			this.visible = visible;
			this.minimized = minimized;
		}
	}
}
