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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.event.ActionEvent;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.gui.styled.StyledButtonUI;
import games.stendhal.client.sprite.DataLoader;
import games.stendhal.client.stendhal;

/**
 * Controller object for the buddy list.
 */
public final class BuddyPanelController implements PropertyChangeListener {
	/**
	 * Controller instance. The first class referring (j2dClient) this class
	 * will need the panel anyway, so it's OK to instantiate it right away.
	 */
	private static final BuddyPanelController instance = new BuddyPanelController();

	private static final Icon SEARCH_ICON = loadSearchIcon();

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
		configureFilterToggle(filterToggle);
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

	private static void configureFilterToggle(final JToggleButton toggle) {
		toggle.setMargin(new Insets(2, 6, 2, 6));
		toggle.setText(null);
		toggle.setFocusable(false);
		toggle.setFocusPainted(false);
		toggle.setIconTextGap(0);
		toggle.setPreferredSize(new Dimension(24, 24));
		toggle.setToolTipText("Wyszukaj znajomego");
		Style style = StyleUtil.getStyle();
		if (style != null) {
			toggle.setUI(new StyledButtonUI(style));
			toggle.setOpaque(false);
			toggle.setRolloverEnabled(true);
		}
	}

	private static Icon loadSearchIcon() {
		ImageIcon icon = createIcon(DataLoader.getResource("data/gui/loupe.png"));
		if (icon != null) {
			return icon;
		}
		File[] candidates = new File[] {
			new File("data/gui/loupe.png"),
			new File(stendhal.getGameFolder(), "data/gui/loupe.png"),
			resolveInstallIcon()
		};
		for (File candidate : candidates) {
			if (candidate == null) {
				continue;
			}
			icon = createIcon(candidate);
			if (icon != null) {
				return icon;
			}
		}
		return new SearchIcon();
	}

	private static ImageIcon createIcon(URL resource) {
		if (resource == null) {
			return null;
		}
		ImageIcon icon = new ImageIcon(resource);
		return (icon.getIconWidth() > 0) ? icon : null;
	}

	private static ImageIcon createIcon(File file) {
		if (!file.isFile()) {
			return null;
		}
		ImageIcon icon = new ImageIcon(file.getAbsolutePath());
		return (icon.getIconWidth() > 0) ? icon : null;
	}

	private static File resolveInstallIcon() {
		try {
			java.net.URL location = BuddyPanelController.class.getProtectionDomain().getCodeSource().getLocation();
			if (location == null) {
				return null;
			}
			File source = new File(location.toURI());
			File base = source.isFile() ? source.getParentFile() : source;
			if (base == null) {
				return null;
			}
			return new File(base, "data/gui/loupe.png");
		} catch (Exception ignored) {
			return null;
		}
	}

	private static final class SearchIcon implements Icon {
		private static final int SIZE = 18;

		@Override
		public void paintIcon(Component component, Graphics graphics, int x, int y) {
			Graphics2D g2 = (Graphics2D) graphics.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Style style = StyleUtil.getStyle();
				Color highlight;
				Color shadow;
				Color fill;
				if (style != null) {
					highlight = style.getHighLightColor();
					shadow = style.getShadowColor();
					Color base = style.getPlainColor();
					fill = (base != null) ? base.brighter() : style.getHighLightColor();
				} else {
					highlight = new Color(240, 240, 240);
					shadow = new Color(40, 40, 40);
					fill = new Color(160, 160, 160);
				}
				int ringDiameter = SIZE - 8;
				int ringX = x + 3;
				int ringY = y + 3;
				g2.setColor(fill);
				g2.fillOval(ringX, ringY, ringDiameter, ringDiameter);
				g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g2.setColor(shadow);
				g2.drawOval(ringX, ringY, ringDiameter, ringDiameter);
				g2.setColor(highlight);
				g2.drawOval(ringX - 1, ringY - 1, ringDiameter + 2, ringDiameter + 2);
				int handleStartX = ringX + ringDiameter - 1;
				int handleStartY = ringY + ringDiameter - 1;
				int handleEndX = x + SIZE - 4;
				int handleEndY = y + SIZE - 4;
				g2.setColor(shadow);
				g2.drawLine(handleStartX, handleStartY, handleEndX, handleEndY);
				g2.setColor(highlight);
				g2.drawLine(handleStartX - 1, handleStartY - 1, handleEndX - 1, handleEndY - 1);
			} finally {
				g2.dispose();
			}
		}

		@Override
		public int getIconWidth() {
			return SIZE;
		}

		@Override
		public int getIconHeight() {
			return SIZE;
		}
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
