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
package games.stendhal.client.events;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import games.stendhal.client.GameScreen;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.ScrolledViewport;
import games.stendhal.client.gui.imageviewer.ImageViewWindow;
import games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent.HeaderRenderer;
import games.stendhal.client.gui.imageviewer.ViewPanel;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

/**
 * @author KarajuSs
 */
class AchievementsLogEvent extends Event<RPEntity> {
	private static final Logger logger = Logger.getLogger(AchievementsLogEvent.class);

	private static final int PAD = 5;

	@Override
	public void execute() {
		if (!event.has("achievements")) {
			logger.warn("Could not create achievements: Event does not have \"achievements\" attribute");
			return;
		}

		new ImageViewWindow("Dziennik osiągnięć", createViewPanel());
	}

	@SuppressWarnings("serial")
	private ViewPanel createViewPanel() {
		return new ViewPanel() {
			@Override
			public void prepareView(Dimension maxSize) {
				Dimension screenSize = GameScreen.get().getSize();
				final int maxPreferredWidth = screenSize.width - 80;

				final JTable table = createTable();

				table.setEnabled(false);
				table.setFillsViewportHeight(true);
				table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

				TableColumn col = table.getColumnModel().getColumn(0);
				DefaultTableCellRenderer r = new DefaultTableCellRenderer();
				r.setHorizontalAlignment(SwingConstants.CENTER);
				col.setCellRenderer(r);

				col = table.getColumnModel().getColumn(1);
				col.setCellRenderer(new SpriteCellRenderer());

				col = table.getColumnModel().getColumn(2);
				col.setCellRenderer(new DescriptionCellRenderer());

				HeaderRenderer hr = new HeaderRenderer();
				Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
				while (cols.hasMoreElements()) {
					TableColumn c = cols.nextElement();
					c.setHeaderRenderer(hr);
				}

				adjustColumnWidths(table);
				adjustRowHeights(table);

				ScrolledViewport viewPort = new ScrolledViewport(table);
				/*
				 * maxPreferredWidth is incorrect, but java does not seem to support
				 * max-width property for div's, so all the cells report the same
				 * preferred width anyway.
				 */
				viewPort.getComponent().setPreferredSize(new Dimension(maxPreferredWidth,
						Math.min(screenSize.height - 100, table.getPreferredSize().height
								+ hr.getPreferredSize().height + 4 * PAD)));
				viewPort.getComponent().setBackground(table.getBackground());
				add(viewPort.getComponent(), BorderLayout.CENTER);

				setVisible(true);
			}

			private JTable createTable() {
				final String[] columnNames = { "#", "Ikona", "Opis" };

				final List<String> achievements = Arrays.asList(event.get("achievements").split(";"));

				final Object[][] data = new Object[achievements.size()][];
				int i = 0;
				for (final String a: achievements) {
					data[i] = createDataRow(a.split(":"));
					i++;
				}

				return new JTable(data, columnNames);
			}

			private Object[] createDataRow(final String[] achievements) {
				final Object[] rval = new Object[3];

				final String category = achievements[0];
				final String title = achievements[1];
				final String desc = achievements[2];

				final Boolean reached = achievements[3].equals("true");

				rval[0] = "";
				rval[1] = getAchievementImage(category, reached);
				rval[2] = getAchievementDesc(title, desc);

				if (reached) {
					rval[0] = "✔";
				}

				return rval;
			}

			private void adjustColumnWidths(JTable table) {
				TableColumnModel model = table.getColumnModel();
				for (int column = 0; column < table.getColumnCount(); column++) {
					TableColumn tc = model.getColumn(column);
					int width = tc.getWidth();
					for (int row = 0; row < table.getRowCount(); row++) {
						Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
						width = Math.max(width, comp.getPreferredSize().width);
					}

					tc.setPreferredWidth(width);
				}
			}
			private void adjustRowHeights(JTable table) {
				for (int row = 0; row < table.getRowCount(); row++) {
					int rowHeight = table.getRowHeight();

					for (int column = 0; column < table.getColumnCount(); column++) {
						Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
						rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
					}

					table.setRowHeight(row, rowHeight);
				}
			}
		};
	}

	@SuppressWarnings("serial")
	private static class DescriptionCellRenderer extends DefaultTableCellRenderer {
		private final Border border = BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD);

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			setBorder(border);
			return this;
		}
	}

	@SuppressWarnings("serial")
	private static class SpriteCellRenderer extends JComponent implements TableCellRenderer {
		private Sprite sprite;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object color,
				boolean isSelected, boolean hasFocus, int row, int col) {
			Object obj = table.getValueAt(row, col);
			if (obj instanceof Sprite) {
				sprite = (Sprite) obj;
			} else {
				sprite = null;
			}
			return this;
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension d = new Dimension();
			if (sprite != null) {
				d.width = sprite.getWidth() + 2 * PAD;
				d.height = sprite.getHeight() + 2 * PAD;
			}
			return d;
		}

		@Override
		protected void paintComponent(Graphics g) {
			if (sprite != null) {
				sprite.draw(g, (getWidth() - sprite.getWidth()) / 2, (getHeight() - sprite.getHeight()) / 2);
			}
		}
	}

	private String getAchievementDesc(String title, String desc) {
		String description = "<html><span style=\"font-weight: bold;\">" + title + "</span><br/>"
				+ "<span style=\"font-weight: normal; font-style: italic;\">" + desc + ".</span></html>";

		return description;
	}

	private Sprite getAchievementImage(String category, boolean fulfilled) {
		String imagePath = "/data/sprites/achievements/" + category + ".png";

		Sprite sprite = SpriteStore.get().getColoredSprite(imagePath, Color.LIGHT_GRAY);
		if (fulfilled) {
			sprite = SpriteStore.get().getSprite(imagePath);
		}

		if (sprite.getWidth() > sprite.getHeight()) {
			sprite = SpriteStore.get().getAnimatedSprite(sprite, 100);
		}

		return sprite;
	}
}
