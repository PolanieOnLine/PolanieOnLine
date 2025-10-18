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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
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
	private Timer countdownTimer;
	private long readyAt;
	private String baseStatus;

	public GoldenCauldronWindow(final String title) {
		super("golden_cauldron", title);

		setMinimizable(false);
		setCloseable(true);
		setHideOnClose(true);

		grid = new SlotGrid(4, 2);
		grid.setOpaque(false);
		grid.setAcceptedTypes(EntityMap.getClass("item", null, null));

		statusLabel = new JLabel("Kocioł nie pracuje.");
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mixButton = new JButton("Mieszaj");
		mixButton.setFocusable(false);
		mixButton.setAlignmentX(Component.CENTER_ALIGNMENT);
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
		layout.setBorder(new EmptyBorder(4, 6, 6, 6));
		layout.setOpaque(false);
		layout.add(statusLabel);
		layout.add(grid);
		layout.add(mixButton);

		final JPanel content = new JPanel(new BorderLayout());
		content.setOpaque(false);
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

	public void updateStatus(final String text, final long readyAtTimestamp) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				baseStatus = (text == null) ? "" : text;
				readyAt = readyAtTimestamp;
				restartCountdown();
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
		stopCountdown();
		super.close();
	}

	private void checkDistance() {
		if (!isCloseEnough()) {
			close();
		}
	}

	private void restartCountdown() {
		stopCountdown();

		if (readyAt > System.currentTimeMillis()) {
			countdownTimer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent event) {
					refreshStatusLabel();
				}
			});
			countdownTimer.setRepeats(true);
			countdownTimer.setInitialDelay(0);
			countdownTimer.start();
			refreshStatusLabel();
		} else {
			statusLabel.setText(baseStatus);
		}
	}

	private void refreshStatusLabel() {
		if (readyAt <= System.currentTimeMillis()) {
			stopCountdown();
			statusLabel.setText(baseStatus);
			return;
		}

		final long remainingMillis = readyAt - System.currentTimeMillis();
		final long totalSeconds = Math.max(0L, remainingMillis / 1000L);
		final long minutes = totalSeconds / 60L;
		final long seconds = totalSeconds % 60L;
		final StringBuilder builder = new StringBuilder();
		if (baseStatus != null && !baseStatus.isEmpty()) {
			builder.append(baseStatus).append(' ');
		}
		builder.append("Pozostało ");
		if (minutes > 0) {
			if (minutes < 10) {
				builder.append('0');
			}
			builder.append(minutes).append(':');
			if (seconds < 10) {
				builder.append('0');
			}
			builder.append(seconds);
		} else {
			builder.append(seconds).append(" s");
		}
		builder.append('.');
		statusLabel.setText(builder.toString());
	}

	private void stopCountdown() {
		if (countdownTimer != null) {
			countdownTimer.stop();
			countdownTimer = null;
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
