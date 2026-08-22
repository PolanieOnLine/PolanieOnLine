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

import javax.swing.SwingUtilities;

import games.stendhal.client.UserContext;
import games.stendhal.client.listener.FeatureChangeListener;

/**
 * A slot window whose visibility is controlled by a player feature.
 * Persisted window layout must never reveal it before the feature is enabled.
 */
public class FeatureEnabledSlotWindow extends SlotWindow implements FeatureChangeListener {
	private static final long serialVersionUID = 1L;

	private final String feature;
	private volatile boolean featureEnabled;

	/**
	 * Create a feature controlled slot window.
	 *
	 * @param feature feature and window identifier
	 * @param width slot columns
	 * @param height slot rows
	 */
	protected FeatureEnabledSlotWindow(final String feature, final int width, final int height) {
		super(feature, width, height);
		this.feature = feature;
		featureEnabled = UserContext.get().hasFeature(feature);
		super.setVisible(featureEnabled);
	}

	@Override
	public final void setVisible(final boolean visible) {
		super.setVisible(visible && featureEnabled);
	}

	@Override
	public void featureDisabled(final String name) {
		if (!feature.equals(name)) {
			return;
		}

		featureEnabled = false;
		applyFeatureVisibility(false);
	}

	@Override
	public void featureEnabled(final String name, final String value) {
		if (!feature.equals(name)) {
			return;
		}

		featureEnabled = true;
		applyFeatureVisibility(true);
	}

	/** Reapply the current feature state after rebuilding the native frame. */
	public final void restoreFeatureVisibility() {
		applyFeatureVisibility(featureEnabled);
	}

	private void applyFeatureVisibility(final boolean visible) {
		if (SwingUtilities.isEventDispatchThread()) {
			setVisible(visible);
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setVisible(visible);
			}
		});
	}
}
