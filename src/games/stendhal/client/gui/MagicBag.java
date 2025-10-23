/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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

@SuppressWarnings("serial")
class MagicBag extends SlotWindow implements FeatureChangeListener {
	/**
	 * Create a magic bag.
	 */
	public MagicBag() {
		// Remember if you change these numbers change also a number in
		// src/games/stendhal/server/entity/RPEntity.java
		super("magicbag", 6, 1);
		// A panel window; forbid closing
		setCloseable(false);
	}

	/**
	 * Disable the magic bag.
	 */
        private void disableMagicBag() {
                if (!SwingUtilities.isEventDispatchThread()) {
                        SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                        disableMagicBag();
                                }
                        });
                        return;
                }
                if (isVisible()) {
                        setVisible(false);
                }
        }

	/**
	 * A feature was disabled.
	 *
	 * @param name
	 *            The name of the feature.
	 */
	@Override
	public void featureDisabled(final String name) {
		if (name.equals("magicbag")) {
			disableMagicBag();
		}
	}

	/**
	 * A feature was enabled.
	 *
	 * @param name
	 *            The name of the feature.
	 * @param value
	 *            Optional feature specific data.
	 */
	@Override
	public void featureEnabled(final String name, final String value) {
		if (name.equals("magicbag")) {
                        SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                        if (!isVisible()) {
                                                setVisible(true);
                                        }
                                }
                        });
                }
        }

        @Override
        public void setVisible(final boolean visible) {
                boolean allow = visible;
                if (visible && !UserContext.get().hasFeature("magicbag")) {
                        allow = false;
                }
                super.setVisible(allow);
        }
}
