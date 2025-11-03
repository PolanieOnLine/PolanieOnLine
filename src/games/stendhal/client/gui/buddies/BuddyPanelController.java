/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.buddies;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.Icon;

import java.awt.event.ActionEvent;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;

/**
 * Controller object for the buddy list.
 */
public final class BuddyPanelController implements PropertyChangeListener {
	/**
	 * Controller instance. The first class referring (j2dClient) this class
	 * will need the panel anyway, so it's OK to instantiate it right away.
	 */
	private static final BuddyPanelController instance = new BuddyPanelController();

	private static final Icon SEARCH_ICON = new Icon() {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D graphics = (Graphics2D) g.create();
			try {
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				graphics.setColor(new Color(70, 70, 70));
				graphics.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				int diameter = 8;
				int ringX = x + 2;
				int ringY = y + 2;
				graphics.drawOval(ringX, ringY, diameter, diameter);
				int handleStartX = ringX + diameter - 1;
				int handleStartY = ringY + diameter - 1;
				graphics.drawLine(handleStartX, handleStartY, x + getIconWidth() - 2, y + getIconHeight() - 2);
			} finally {
				graphics.dispose();
			}
		}

		@Override
		public int getIconWidth() {
			return 14;
		}

		@Override
		public int getIconHeight() {
			return 14;
		}
	};

	private final JComponent buddyPanel;
	private final BuddyListModel model;

	/**
	 * Creates a new BuddyPanelController.
	 */
	private BuddyPanelController() {
		// The panel is actually just the background
		buddyPanel = new JPanel();
		buddyPanel.setLayout(new SBoxLayout(SBoxLayout.VERTICAL, SBoxLayout.COMMON_PADDING));
		// now the actual sorted list
		model = new BuddyListModel();

		JComponent filterRow = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, SBoxLayout.COMMON_PADDING);
		final JToggleButton filterToggle = new JToggleButton(SEARCH_ICON);
		filterToggle.setMargin(new Insets(2, 4, 2, 4));
		filterToggle.setText(null);
		filterToggle.setFocusable(false);
		filterToggle.setToolTipText("Wyszukaj znajomego");
		filterRow.add(filterToggle);
		final JTextField filterField = new JTextField();
		filterField.setColumns(12);
		filterField.setVisible(false);
		filterField.setToolTipText("Filtruj listę znajomych według nazwy");
		filterField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				model.setFilter(filterField.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				model.setFilter(filterField.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				model.setFilter(filterField.getText());
			}
		});
		final Runnable hideFilter = new Runnable() {
			@Override
			public void run() {
				if (!filterField.getText().isEmpty()) {
					filterField.setText("");
				}
				filterField.setVisible(false);
				filterRow.revalidate();
				filterRow.repaint();
			}
		};
		final Runnable showFilter = new Runnable() {
			@Override
			public void run() {
				filterField.setVisible(true);
				filterRow.revalidate();
				filterRow.repaint();
				filterField.requestFocusInWindow();
			}
		};
		filterField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "buddyFilterHide");
		filterField.getActionMap().put("buddyFilterHide", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (filterToggle.isSelected()) {
					filterToggle.setSelected(false);
					hideFilter.run();
				}
			}
		});
		filterToggle.addActionListener(e -> {
			if (filterToggle.isSelected()) {
				showFilter.run();
			} else {
				hideFilter.run();
			}
		});
		filterRow.add(filterField, SLayout.EXPAND_X);
		buddyPanel.add(filterRow, SLayout.EXPAND_X);

		JList<Buddy> list = new BuddyPanel(model);
		buddyPanel.add(list, SLayout.EXPAND_X);
	}

	/**
	 * Get the graphical component.
	 *
	 * @return buddy panel
	 */
	public Component getComponent() {
		return buddyPanel;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (evt == null) {
			return;
		}
		//remove
		@SuppressWarnings("unchecked")
		Map<String, String> oldBuddies = (Map<String, String>) evt.getOldValue();
		if (oldBuddies != null) {
			for (final Entry<String, String> entry : oldBuddies.entrySet()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						model.removeBuddy(entry.getKey());
					}
				});
			}
		}
		//change online status
		@SuppressWarnings("unchecked")
		Map<String, String> newBuddies = (Map<String, String>) evt.getNewValue();
		if (newBuddies != null) {
			for (final Entry<String, String> entry : newBuddies.entrySet()) {
				final boolean online = Boolean.parseBoolean(entry.getValue());
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						model.setOnline(entry.getKey(), online);
					}
				});
			}
		}
	}

	/**
	 * Get the controller instance.
	 *
	 * @return controller
	 */
	public static BuddyPanelController get() {
		return instance;
	}
}
