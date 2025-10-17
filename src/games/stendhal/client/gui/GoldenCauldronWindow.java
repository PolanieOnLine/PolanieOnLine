/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Inspector;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.factory.EntityMap;
import games.stendhal.client.gui.layout.SBoxLayout;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import marauroa.common.game.RPObject;

/**
 * Dedicated window for the golden cauldron slots.
 */
public class GoldenCauldronWindow extends InternalManagedWindow implements Inspectable {
	private static final int MAX_DISTANCE = 4;

	private final SlotGrid grid;
	private final JLabel statusLabel;
	private final JButton mixButton;

	private IEntity parent;
	private ActionListener mixListener;

	public GoldenCauldronWindow(final String title) {
		super("golden_cauldron", title);

		setMinimizable(false);
		setCloseable(true);
		setHideOnClose(true);

		grid = new SlotGrid(4, 2);
		grid.setAcceptedTypes(EntityMap.getClass("item", null, null));

		statusLabel = new JLabel("Wrzuć składniki i kliknij \"Mieszaj\".");
		mixButton = new JButton("Mieszaj");
		mixButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (mixListener != null && mixButton.isEnabled()) {
					mixListener.actionPerformed(e);
				}
			}
		});

		final JPanel layout = new JPanel();
		layout.setLayout(new SBoxLayout(SBoxLayout.VERTICAL, 4));
		layout.add(statusLabel);
		layout.add(grid);
		final JPanel buttonRow = new JPanel();
		buttonRow.setLayout(new SBoxLayout(SBoxLayout.HORIZONTAL, 4));
		SBoxLayout.addSpring(buttonRow);
		buttonRow.add(mixButton);
		layout.add(buttonRow);

		final JPanel content = new JPanel(new BorderLayout());
		content.add(layout, BorderLayout.CENTER);
		setContent(content);
	}

	public void setSlot(final IEntity parent, final String slot) {
		this.parent = parent;
		grid.setSlot(parent, slot);
	}

	public void setInspector(final Inspector inspector) {
		grid.setInspector(inspector);
	}

	public void setStatusText(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				statusLabel.setText(text);
			}
		});
	}

	public void setMixEnabled(final boolean enabled) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mixButton.setEnabled(enabled);
			}
		});
	}

	public void setMixAction(final ActionListener listener) {
		mixListener = listener;
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		checkDistance();
	}

	@Override
	public void close() {
		grid.release();
		super.close();
	}

	private void checkDistance() {
		if (!isCloseEnough()) {
			close();
		}
	}

	private boolean isCloseEnough() {
		final User user = User.get();
		if (user == null || parent == null) {
			return true;
		}

		final RPObject root = parent.getRPObject().getBaseContainer();
		if (root != null && root.has("name")) {
			if (StendhalClient.get().getCharacter().equalsIgnoreCase(
				    root.get("name"))) {
				return true;
			}
		}

		return isCloseEnough(user.getX(), user.getY());
	}

	private boolean isCloseEnough(final double x, final double y) {
		final int px = (int) x;
		final int py = (int) y;

		final Rectangle2D area = parent.getArea();
		area.setRect(area.getX() - MAX_DISTANCE, area.getY() - MAX_DISTANCE,
			area.getWidth() + MAX_DISTANCE * 2, area.getHeight() + MAX_DISTANCE * 2);

		return area.contains(px, py);
	}
}
