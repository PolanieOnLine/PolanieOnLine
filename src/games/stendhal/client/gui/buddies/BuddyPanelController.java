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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
		JLabel filterLabel = new JLabel("Szukaj znajomych:");
		filterRow.add(filterLabel);
		final JTextField filterField = new JTextField();
		filterField.setColumns(12);
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
