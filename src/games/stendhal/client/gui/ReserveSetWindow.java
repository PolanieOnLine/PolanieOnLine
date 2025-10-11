/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

class ReserveSetWindow extends InternalManagedWindow {
	/**
	 * serial version uid.
	 */
	private static final long serialVersionUID = -6792704385484299338L;

	private final Character owner;
	private boolean added;
	private boolean suppressVisibilityEvents;

	ReserveSetWindow(Character owner, JComponent content) {
		super("reserve_set", "Schowek");
		this.owner = owner;
		setContent(content);
		setCloseable(false);
		setMinimizable(false);
		setMovable(false);
		setHideOnClose(true);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent event) {
				if (!suppressVisibilityEvents) {
					owner.onReserveWindowVisibilityChange(true);
				}
			}

			@Override
			public void componentHidden(ComponentEvent event) {
				if (!suppressVisibilityEvents) {
					owner.onReserveWindowVisibilityChange(false);
				}
			}
		});
	}

	void attach() {
		if (added) {
			return;
		}
		added = true;
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					attach();
				}
			});
			return;
		}
		suppressVisibilityEvents = true;
		setVisible(false);
		j2DClient.get().addWindow(this);
		if (getParent() != null) {
			setMinimized(false);
		}
		suppressVisibilityEvents = false;
	}

	void showBeside(final Component anchor) {
		if (!added) {
			return;
		}
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showBeside(anchor);
				}
			});
			return;
		}
		setVisible(true);
		setMinimized(false);
		Component parent = getParent();
		Point location = anchor.getLocation();
		if ((parent != null) && (anchor.getParent() != parent)) {
			location = SwingUtilities.convertPoint(anchor.getParent(), location, parent);
		}
		location.translate(anchor.getWidth() + 4, 0);
		moveTo(location.x, location.y);
		raise();
	}

}
