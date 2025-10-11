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
	private boolean anchoredToOwner = true;

	ReserveSetWindow(Character owner, JComponent content) {
		super("reserve_set", "Schowek");
		this.owner = owner;
		setContent(content);
		setCloseable(true);
		setVisible(false);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent event) {
				owner.onReserveWindowVisibilityChange(true);
			}

			@Override
			public void componentHidden(ComponentEvent event) {
				owner.onReserveWindowVisibilityChange(false);
			}
		});
		addWindowDragListener(new WindowDragListener() {
			@Override
			public void startDrag(Component component) {
				anchoredToOwner = false;
			}

			@Override
			public void endDrag(Component component) {
				// Nothing to do.
			}

			@Override
			public void windowDragged(Component component, Point point) {
				// Nothing to do.
			}
		});
	}

	void attach() {
		if (added) {
			return;
		}
		added = true;
		j2DClient.get().addWindow(this);
	}

	void showBeside(Component anchor) {
		if (!added) {
			return;
		}
		setVisible(true);
		setMinimized(false);
		if (anchoredToOwner) {
			Component parent = getParent();
			Point location = anchor.getLocation();
			if ((parent != null) && (anchor.getParent() != parent)) {
				location = SwingUtilities.convertPoint(anchor.getParent(), location, parent);
			}
			location.translate(anchor.getWidth() + 4, 0);
			moveTo(location.x, location.y);
		}
		raise();
	}

	void bringToFront() {
		if (!added) {
			return;
		}
		setVisible(true);
		setMinimized(false);
		raise();
	}
}
