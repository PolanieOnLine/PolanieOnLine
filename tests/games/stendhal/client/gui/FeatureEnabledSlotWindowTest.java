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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.swing.SwingUtilities;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.util.UserInterfaceTestHelper;

public class FeatureEnabledSlotWindowTest {
	private static final String FEATURE = "test_feature_window";

	@BeforeClass
	public static void init() {
		UserInterfaceTestHelper.initUserInterface();
	}

	@Test
	public void persistedVisibilityCannotRevealDisabledWindow() throws Exception {
		final TestFeatureWindow window = new TestFeatureWindow();
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				window.setVisible(true);
			}
		});

		assertFalse(window.isVisible());
	}

	@Test
	public void featureEventsControlVisibilityAcrossWindowRestores() throws Exception {
		final TestFeatureWindow window = new TestFeatureWindow();
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				window.featureEnabled(FEATURE, "");
			}
		});
		assertTrue(window.isVisible());

		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				window.featureDisabled(FEATURE);
				window.setVisible(true);
				window.restoreFeatureVisibility();
			}
		});
		assertFalse(window.isVisible());
	}

	private static final class TestFeatureWindow extends FeatureEnabledSlotWindow {
		private static final long serialVersionUID = 1L;

		TestFeatureWindow() {
			super(FEATURE, 1, 1);
		}
	}
}
