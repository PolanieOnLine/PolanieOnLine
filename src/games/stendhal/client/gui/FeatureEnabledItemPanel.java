/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
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

import java.awt.Container;
import javax.swing.SwingUtilities;

import games.stendhal.client.UserContext;
import games.stendhal.client.listener.FeatureChangeListener;
import games.stendhal.client.sprite.Sprite;

/**
 * An <code>ItemPanel</code> that is enabled/disabled with a feature change event.
 */
public class FeatureEnabledItemPanel extends ItemPanel implements FeatureChangeListener {

	final String feature;
	private volatile boolean featureAvailable;

	FeatureEnabledItemPanel(final String slotName, final Sprite placeholder) {
		super(slotName, placeholder);

		feature = slotName;

		// only visible if the player has the feature
		featureAvailable = UserContext.get().hasFeature(feature);
		setVisible(featureAvailable);
	}

	FeatureEnabledItemPanel(final String slotName, final Sprite placeholder, final String featureName) {
		super(slotName, placeholder);

		feature = featureName;

		// only visible if the player has the feature
		featureAvailable = UserContext.get().hasFeature(feature);
		setVisible(featureAvailable);
	}

	@Override
	public void featureDisabled(final String name) {
		if (name.equals(feature)) {
			featureAvailable = false;
			applyFeatureVisibility();
		}
	}

	@Override
	public void featureEnabled(final String name, final String value) {
		if (name.equals(feature)) {
			featureAvailable = true;
			applyFeatureVisibility();
		}
	}

	private void applyFeatureVisibility() {
		if (SwingUtilities.isEventDispatchThread()) {
			setVisible(featureAvailable);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// A queued event from the old character must not reveal a slot.
					setVisible(featureAvailable);
				}
			});
		}
	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);

		final Container parent = getParent();
		if (visible && parent != null && !parent.isVisible()) {
			// make sure the parent container is visible.
			parent.setVisible(true);
		}
	}
}
